package com.hk.luatela.patch;

import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaLibrary;
import com.hk.luatela.patch.models.ModelLibrary;
import com.hk.luatela.patch.models.ModelSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.sql.Connection;

public class LuaBase
{
	private final File dataroot;
	private Connection connection;
	ModelSet patchModelSet;
	private ModelSet modelSet;

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

	public int loadPatches()
	{
		if(patchModelSet != null)
			throw new IllegalStateException("Already loaded patchy model set");

		patchModelSet = new ModelSet();
		File patchesDir = new File(dataroot, ".patches");

		if(!patchesDir.exists())
		{
			patchesDir.mkdirs();
			return 0;
		}

		File[] patches = patchesDir.listFiles();

		if(patches != null)
		{
			// one by one, apply patches to fresh model set
			// compare to newly read model set, and recreate patch
			for(File patch : patches)
				throw new Error("APPLY PATCHES TOGETHER");
		}

		return patches != null ? patches.length : 0;
	}

	public PatchComparison checkNew() throws FileNotFoundException, DatabaseException
	{
		File models = new File(dataroot, "models.lua");

		if(!models.exists())
			throw new FileNotFoundException(models.getAbsolutePath() + " (models.lua required for db)");

		return new PatchComparison(this, modelSet = loadModelSet(models));
	}

	static ModelSet importModelSet(ModelSet modelSet, File models) throws FileNotFoundException, DatabaseException
	{
		LuaInterpreter interp = Lua.reader(models);

		interp.setExtra(ModelSet.KEY, modelSet);

		LuaLibrary.importStandard(interp);

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

	public ModelSet getModelSet()
	{
		return modelSet;
	}
}
