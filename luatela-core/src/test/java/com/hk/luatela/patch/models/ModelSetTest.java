package com.hk.luatela.patch.models;

import com.hk.luatela.patch.DatabaseException;
import com.hk.luatela.patch.LuaBase;
import com.hk.luatela.patch.models.fields.DataField;
import com.hk.luatela.patch.models.fields.FloatField;
import com.hk.luatela.patch.models.fields.IDField;
import com.hk.luatela.patch.models.fields.StringField;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import static org.junit.Assert.*;

public class ModelSetTest
{
	private File modelDir;

	@Before
	public void setUp() {
		modelDir = new File("src/test/resources/models");
	}

	@Test
	public void testStudentGrade() throws FileNotFoundException, DatabaseException
	{
		ModelSet modelSet = LuaBase.loadModelSet(new File(modelDir, "student_grade.lua"));
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
	}
}