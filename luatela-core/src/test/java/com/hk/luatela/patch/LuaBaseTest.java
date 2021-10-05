package com.hk.luatela.patch;

import com.hk.luatela.patch.models.Model;
import com.hk.luatela.patch.models.ModelSet;
import com.hk.luatela.patch.models.fields.DataField;
import com.hk.luatela.patch.models.fields.FloatField;
import com.hk.luatela.patch.models.fields.StringField;
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
		File dataroot = new File(root, "empty-base");
		File patches = new File(dataroot, ".patches");

		if(!dataroot.exists())
			assertTrue(dataroot.mkdirs());

		LuaBase base = new LuaBase(dataroot);

		if(patches.exists())
			assertTrue(patches.delete());

		base.loadPatches();

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
	public void testJustModels() throws FileNotFoundException, DatabaseException
	{
		File dataroot = new File(root, "just-models");

		LuaBase base = new LuaBase(dataroot);
		base.loadPatches();
		base.checkNew();

		ModelSet modelSet = base.getModelSet();
		assertNotNull(modelSet);
		assertEquals(1, modelSet.size());

		Model model = modelSet.getModel("student_grade");
		assertNotNull(model);

		Map<String, DataField> fieldMap = model.getFieldMap();
		assertNotNull(fieldMap);

		assertEquals(5, fieldMap.size());

		assertTrue(fieldMap.get("firstName") instanceof StringField);
		assertTrue(fieldMap.get("lastName") instanceof StringField);
		assertTrue(fieldMap.get("mathicsGrade") instanceof FloatField);
		assertTrue(fieldMap.get("scienceGrade") instanceof FloatField);
		assertTrue(fieldMap.get("englishGrade") instanceof FloatField);
	}
}