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

		assertNotNull(comparison.newModels);
		assertEquals(1, comparison.newModels.length);
		assertEquals("student_grade", comparison.newModels[0].name);
		assertEquals(5 + 1, comparison.newModels[0].getFields().size());

		ModelSet modelSet = base.getModelSet();
		assertNotNull(modelSet);
		assertEquals(1, modelSet.size());

		Model model = modelSet.getModel("student_grade");
		assertNotNull(model);

		Map<String, DataField> fieldMap = model.getFieldMap();
		assertNotNull(fieldMap);

		assertEquals(5 + 1, fieldMap.size());

		assertTrue(fieldMap.get("id") instanceof IDField);

		assertTrue(fieldMap.get("firstName") instanceof StringField);
		assertEquals(64, ((StringField) fieldMap.get("firstName")).getMaxLength());

		assertTrue(fieldMap.get("lastName") instanceof StringField);
		assertEquals(64, ((StringField) fieldMap.get("lastName")).getMaxLength());

		assertTrue(fieldMap.get("mathicsGrade") instanceof FloatField);
		assertTrue(fieldMap.get("scienceGrade") instanceof FloatField);
		assertTrue(fieldMap.get("englishGrade") instanceof FloatField);

		assertTrue(fieldMap.get("id").isPrimary());
		assertFalse(fieldMap.get("firstName").isPrimary());
		assertFalse(fieldMap.get("lastName").isPrimary());
		assertFalse(fieldMap.get("mathicsGrade").isPrimary());
		assertFalse(fieldMap.get("scienceGrade").isPrimary());
		assertFalse(fieldMap.get("englishGrade").isPrimary());

		assertFalse(new File(dataroot, "patches").exists());

		PatchExport export = comparison.export(base.getPatchModelSet().getPatchCount());
		assertNotNull(export);

		String exportName = export.getName();
		assertNotNull(exportName);
		assertTrue(exportName.startsWith("patch-1"));

		HTMLText txt = new HTMLText();

		assertSame(txt, export.toLua(txt));
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
}