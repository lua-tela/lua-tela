package com.hk.luatela.database;

import java.io.File;
import java.sql.Connection;

public class LuaBase
{
	private Connection connection;
	private final File dataroot, patches;

	public LuaBase(File dataroot)
	{
		this.dataroot = dataroot;
		patches = new File(dataroot, ".patches");
	}

	public void setConnection(Connection connection)
	{
		this.connection = connection;
	}
}
