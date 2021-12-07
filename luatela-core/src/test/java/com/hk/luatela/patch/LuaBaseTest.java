package com.hk.luatela.patch;

import com.hk.lua.Lua;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaLibrary;
import com.hk.luatela.patch.models.Model;
import com.hk.luatela.patch.models.ModelLibrary;
import com.hk.luatela.patch.models.ModelSet;
import com.hk.luatela.patch.models.fields.DataField;
import com.hk.luatela.patch.models.fields.FloatField;
import com.hk.luatela.patch.models.fields.IDField;
import com.hk.luatela.patch.models.fields.StringField;
import com.hk.str.HTMLText;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

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
	public void testModelLibrary() throws FileNotFoundException
	{
		File file = new File(root, "model_library.lua");

		ModelSet modelSet = new ModelSet();

		LuaInterpreter interp = Lua.reader(file);

		interp.setExtra(ModelSet.KEY, modelSet);

		Lua.importStandard(interp);

		interp.importLib(new LuaLibrary<>(null, ModelLibrary.class));

		interp.compile();

		assertEquals(Lua.TRUE, interp.execute());
	}

	@Test
	public void testEmpty() throws FileNotFoundException, DatabaseException
	{
		File dataroot = new File(root, "base-empty");
		File patches = new File(dataroot, "patches");

		if(!dataroot.exists())
			assertTrue(dataroot.mkdirs());

		LuaBase base = new LuaBase(dataroot);

		if(patches.exists())
			assertTrue(patches.delete());

		assertEquals(0, base.loadPatches());

		assertFalse(patches.exists());

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
	public void testJustModels() throws FileNotFoundException, DatabaseException
	{
		File dataroot = new File(root, "base-just-model");

		LuaBase base = new LuaBase(dataroot);
		assertEquals(0, base.loadPatches());
		PatchComparison comparison = base.checkNew();
		assertNotNull(comparison);
		assertNull(comparison.attemptCompare());
		assertFalse(comparison.unchanged);

		assertNotNull(comparison.addedModels);
		assertEquals(1, comparison.addedModels.size());
		assertEquals("student_grade", comparison.addedModels.get(0).name);

		assertNotNull(base.getModelSet());
		assertEquals(1, base.getModelSet().size());
		assertNotNull(base.getModelSet().getModel("student_grade"));

		assertSame(base.getModelSet().getModel("student_grade"), comparison.addedModels.get(0));

		assertFalse(new File(dataroot, "patches").exists());
	}

	@Test
	public void testJustPatchUnchanged() throws FileNotFoundException, DatabaseException
	{
		File dataroot = new File(root, "base-just-patch-unchanged");

		LuaBase base = new LuaBase(dataroot);
		assertEquals(1, base.loadPatches());
		PatchComparison comparison = base.checkNew();
		assertNotNull(comparison);
		assertNull(comparison.attemptCompare());
		assertTrue(comparison.unchanged);

		assertNotNull(base.getPatchModelSet().getModel("point"));
	}
}