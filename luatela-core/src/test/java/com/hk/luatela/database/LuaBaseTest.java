package com.hk.luatela.database;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class LuaBaseTest
{
	private File root;

	@Before
	public void setUp()
	{
		root = new File("src/test/resources");
	}

	@Test
	public void testEmpty() throws FileNotFoundException
	{
		File dataroot = new File(root, "empty-base");
		File patches = new File(dataroot, ".patches");

		if(!dataroot.exists())
			assertTrue(dataroot.mkdirs());

		LuaBase base = new LuaBase(dataroot);

		if(patches.exists())
			assertTrue(patches.delete());

		base.checkPatches();

		assertTrue(patches.exists());
		assertTrue(patches.delete());

		try
		{
			base.checkNew();
			fail("expected FileNotFoundException");
		}
		catch (FileNotFoundException ex)
		{
			assertTrue(ex.getLocalizedMessage().contains("models.lua required"));
		}
	}

	@Test
	public void testJustModels() throws FileNotFoundException
	{
		File dataroot = new File(root, "just-models");

		LuaBase base = new LuaBase(dataroot);
		base.checkPatches();
		base.checkNew();


	}
}