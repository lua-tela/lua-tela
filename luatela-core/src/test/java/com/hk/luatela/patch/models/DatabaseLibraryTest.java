package com.hk.luatela.patch.models;

import com.hk.lua.Lua;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaLibrary;
import com.hk.luatela.patch.DatabaseException;
import com.hk.luatela.patch.LuaBase;
import com.hk.luatela.patch.PatchComparison;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.Assert.*;

public class DatabaseLibraryTest
{
	private File root;
	private Connection conn;

	@Before
	public void setUp() throws IOException, ClassNotFoundException, SQLException
	{
		root = new File("src/test/resources");

		Properties properties = new Properties();
		properties.load(DatabaseLibraryTest.class.getResourceAsStream("/database.properties"));
		String title = properties.getProperty("database.title");
		String user = properties.getProperty("database.user");
		String pass = properties.getProperty("database.pass");

		Class.forName("com.mysql.cj.jdbc.Driver");

		conn = DriverManager.getConnection("jdbc:mysql://" + user + ":" + pass + "@localhost:3306/" + title);
	}

	@Test
	public void testLibrary() throws FileNotFoundException, DatabaseException
	{
		assertNotNull(conn);

		File dataroot = new File(root, "base-just-patch-unchanged");

		LuaBase base = new LuaBase(dataroot);
		base.setConnection(conn);

		assertEquals(1, base.loadPatches());
		PatchComparison comparison = base.checkNew();
		assertNotNull(comparison);
		assertNull(comparison.attemptCompare());
		assertTrue(comparison.unchanged);

		File file = new File(root, "database_library.lua");
		LuaInterpreter interp = Lua.reader(file);

		base.attach(interp);

		Lua.importStandard(interp);

		interp.importLib(new LuaLibrary<>("database", DatabaseLibrary.class));

		interp.compile();

		assertEquals(Lua.TRUE, interp.execute());
	}
}