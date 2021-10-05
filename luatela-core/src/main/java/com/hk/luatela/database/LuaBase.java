package com.hk.luatela.database;

import com.hk.util.Requirements;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.util.Objects;

public class LuaBase
{
	private final File dataroot;
	private Connection connection;

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

	public void checkPatches()
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
			for(File patch : patches)
				System.out.println("Patch exists: " + patch);
		}
	}

	public void checkNew() throws FileNotFoundException
	{
		File models = new File(dataroot, "models.lua");

		if(!models.exists())
			throw new FileNotFoundException(models.getAbsolutePath() + " (models.lua required for db)");


	}
}
