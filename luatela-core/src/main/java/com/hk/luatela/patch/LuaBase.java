package com.hk.luatela.patch;

import com.hk.file.FileUtil;
import com.hk.lua.*;
import com.hk.luatela.patch.models.Model;
import com.hk.luatela.patch.models.ModelLibrary;
import com.hk.luatela.patch.models.ModelSet;
import com.hk.luatela.patch.models.fields.DataField;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.text.DateFormat;
import java.util.*;
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

	public static ModelSet loadModelSet(File models) throws FileNotFoundException, DatabaseException
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
		Lua.importStandard(interp);

		interp.getGlobals().setVar("models", Lua.newTable());
		interp.getGlobals().setVar("patchNo", Lua.ONE);

		try
		{
			for (Map.Entry<Integer, File> entry : patches.entrySet())
			{
				if(!interp.require(new FileReader(entry.getValue())).getBoolean())
					throw new DatabaseException("Patch #" + entry.getKey() + " returned a false value.");
			}
		}
		catch (FileNotFoundException e)
		{
			throw new Error(); // shouldn't really be possible...
		}

		patchNo = (int) interp.getGlobals().getVar("patchNo").getInteger();
		patchNo--;

		if(patchNo != patches.size())
		{
			System.out.println("------------------- WARNING -------------------");
			System.out.println("\tAmount of patch files doesn't match");
			System.out.println("\tinternal patch number?");
		}

		LuaObject models = interp.getGlobals().getVar("models");

		Model model;
		DataField field;
		LuaObject mdl, fld;
		String fieldType;
		for (Map.Entry<LuaObject, LuaObject> entry1 : models.getEntries())
		{
			mdl = entry1.getValue();

			if(!entry1.getKey().isString())
				throw new DatabaseException("Expected key of 'models' table to be a string: " + entry1.getKey());
			if(!mdl.isTable())
				throw new DatabaseException("Expected value, under key '" + entry1.getKey() + "', of 'models' table to be a table: " + mdl);
			model = new Model(set, entry1.getKey().getString());

			List<DataField> fields = new LinkedList<>();
			for (Map.Entry<LuaObject, LuaObject> entry2 : mdl.getEntries())
			{
				if(!entry2.getKey().isString())
					throw new DatabaseException("Expected key of model to be a string, not " + entry2.getKey());
				fld = entry2.getValue();
				if(!fld.isTable())
					throw new DatabaseException("Unexpected value in fields table: " + fld);
				if(fld.getLength() != 2 ||!fld.rawGet(1).isString() || !fld.rawGet(2).isTable())
					throw new DatabaseException("Field table should consist of a string and a table");
				fieldType = fld.rawGet(1).getString();
				fld = fld.rawGet(2);

				DataField.Builder builder = ModelLibrary.fieldBuilders.get(fieldType);

				if(builder == null)
					throw new DatabaseException("Unknown field type: " + fieldType);

				fields.add(builder.provide(model, entry2.getKey().getString(), fld));
			}
			model.setFields(fields);
		}

		return patches.size();
	}

	public static final DateFormat FULL_FORMAT = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
}
