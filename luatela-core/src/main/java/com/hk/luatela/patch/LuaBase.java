package com.hk.luatela.patch;

import com.hk.lua.*;
import com.hk.luatela.patch.models.ModelLibrary;
import com.hk.luatela.patch.models.ModelSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuaBase
{
	public final File dataroot;
	private Connection connection;
	private ModelSet patchModelSet, modelSet;

	public LuaBase(File dataroot) throws FileNotFoundException
	{
		if(!dataroot.exists())
			throw new FileNotFoundException(dataroot.getAbsolutePath());

		this.dataroot = dataroot;
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	public int loadPatches() throws DatabaseException
	{
		if(patchModelSet != null)
			throw new IllegalStateException("Already loaded patchy model set");

		patchModelSet = new ModelSet();
		File patchesDir = new File(dataroot, "patches");

		patchModelSet.startStitch();

		if(!patchesDir.exists())
		{
			patchModelSet.endStitch();
			return 0;
		}

		int count = applyPatches(patchModelSet, patchesDir);

		patchModelSet.endStitch();

		return count;
	}

	public PatchComparison checkNew() throws FileNotFoundException, DatabaseException
	{
		File models = new File(dataroot, "models.lua");

		if(!models.exists())
			throw new FileNotFoundException(models.getAbsolutePath() + " (models.lua required for db)");

		return new PatchComparison(patchModelSet, modelSet = loadModelSet(models));
	}

	public ModelSet getPatchModelSet()
	{
		return patchModelSet;
	}

	public ModelSet getModelSet()
	{
		return modelSet;
	}

	public static void injectRequire(LuaInterpreter interp, Path dataroot)
	{
		interp.getGlobals().setVar("require", Lua.newFunc((interp1, args) -> {
			Lua.checkArgs("require", args, LuaType.STRING);
			String src = args[0].getString();

			LuaObject result = Lua.NIL;
			try
			{
				result = interp1.require(src, Files.newBufferedReader(dataroot.resolve(src)));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

			return result;
		}));
	}

	static ModelSet importModelSet(ModelSet modelSet, File models) throws FileNotFoundException, DatabaseException
	{
		LuaInterpreter interp = Lua.reader(models);

		interp.setExtra(ModelSet.KEY, modelSet);

		Lua.importStandard(interp);
		injectRequire(interp, models.getParentFile().toPath());

		interp.importLib(new LuaLibrary<>(null, ModelLibrary.class));

		try
		{
			interp.compile();
		}
		catch(LuaException ex)
		{
			throw new DatabaseException("during " + models.getName() + " compilation", ex);
		}

		interp.execute();

		return modelSet;
	}

	static ModelSet loadModelSet(File models) throws FileNotFoundException, DatabaseException
	{
		return importModelSet(new ModelSet(), models);
	}

	static int applyPatches(ModelSet set, File dir) throws DatabaseException
	{
		Pattern patchName = Pattern.compile("patch-(\\d+)[-\\w]*\\.lua");
		File[] files = Objects.requireNonNull(dir.listFiles());

		Map<Integer, File> patches = new TreeMap<>();

		int patchNo;
		Matcher matcher;
		for(File file : files)
		{
			matcher = patchName.matcher(file.getName().toLowerCase());
			if(matcher.matches())
			{
				patchNo = Integer.parseInt(matcher.group(1));

				if(patches.containsKey(patchNo))
					throw new DatabaseException("Duplicate patch number: " + file);

				patches.put(patchNo, file);
			}
		}

		if(patches.isEmpty())
			return 0;

		LuaInterpreter interp = Lua.interpreter();

		for (Map.Entry<Integer, File> entry : patches.entrySet())
		{
			System.out.println(entry.getValue() + " (#" + entry.getKey() + ")");
		}

		return patches.size();
	}

	public static final DateFormat FULL_FORMAT = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
}
