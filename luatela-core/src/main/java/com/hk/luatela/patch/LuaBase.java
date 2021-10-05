package com.hk.luatela.patch;

import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaLibrary;
import com.hk.luatela.patch.models.ModelLibrary;
import com.hk.luatela.patch.models.ModelSet;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;

public class LuaBase
{
	private final File dataroot;
	private Connection connection;
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

	public void loadPatches()
	{
		File patchesDir = new File(dataroot, ".patches");

		if(!patchesDir.exists())
		{
			patchesDir.mkdirs();
			return;
		}

		File[] patches = patchesDir.listFiles();

		if(patches != null)
		{
			// one by one, apply patches to fresh model set
			// compare to newly read model set, and recreate patch
			for(File patch : patches)
				System.out.println("Patch exists: " + patch + ", " + modelSet);
		}
	}

	public void checkNew() throws FileNotFoundException, DatabaseException
	{
		File models = new File(dataroot, "models.lua");

		if(!models.exists())
			throw new FileNotFoundException(models.getAbsolutePath() + " (models.lua required for db)");

		LuaInterpreter interp = Lua.reader(models);

		ModelSet modelSet = new ModelSet();
		interp.setExtra(ModelSet.KEY, modelSet);

		LuaLibrary.importStandard(interp);

		interp.importLib(new LuaLibrary<>(null, ModelLibrary.class));

		try
		{
			interp.compile();
		}
		catch(LuaException ex)
		{
			throw new DatabaseException("during models.lua compilation", ex);
		}

		interp.execute();

		if(this.modelSet == null)
			this.modelSet = modelSet;
		else
			throw new Error("COMPARE MODEL SETS");
	}

	public ModelSet getModelSet()
	{
		return modelSet;
	}
}
