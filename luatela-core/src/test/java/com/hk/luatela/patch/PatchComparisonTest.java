package com.hk.luatela.patch;

import com.hk.luatela.patch.models.ModelSet;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class PatchComparisonTest
{
	private File modelDir;

	@Before
	public void setUp()
	{
		modelDir = new File("src/test/resources/models");
	}

	@Test
	public void testEmptyToSingle() throws FileNotFoundException, DatabaseException
	{
		ModelSet notEmptySet;
		PatchComparison comparison;

		notEmptySet = LuaBase.loadModelSet(new File(modelDir, "student_grade.lua"));

		comparison = new PatchComparison(wrap(new ModelSet()), notEmptySet);

		assertNull(comparison.attemptCompare());
		assertFalse(comparison.unchanged);

		assertNotNull(comparison.newModels);
		assertEquals(1, comparison.newModels.length);
		assertEquals("student_grade", comparison.newModels[0].name);
		assertEquals(6, comparison.newModels[0].getFields().size());

		notEmptySet = LuaBase.loadModelSet(new File(modelDir, "single_model.lua"));

		comparison = new PatchComparison(wrap(new ModelSet()), notEmptySet);

		assertNull(comparison.attemptCompare());
		assertFalse(comparison.unchanged);

		assertNotNull(comparison.newModels);
		assertEquals(1, comparison.newModels.length);
		assertEquals("point", comparison.newModels[0].name);
		assertEquals(3, comparison.newModels[0].getFields().size());
	}

	private LuaBase wrap(ModelSet set)
	{
		try
		{
			LuaBase base = new LuaBase(modelDir);
			base.patchModelSet = set;
			return base;
		}
		catch (FileNotFoundException e)
		{
			fail(e.getLocalizedMessage());
			return null;
		}
	}

	@Test
	public void testEmptyToMultiple() throws FileNotFoundException, DatabaseException
	{
		ModelSet notEmptySet;
		PatchComparison comparison;

		notEmptySet = LuaBase.loadModelSet(new File(modelDir, "test_models.lua"));

		comparison = new PatchComparison(wrap(new ModelSet()), notEmptySet);

		assertNull(comparison.attemptCompare());
		assertFalse(comparison.unchanged);

		assertNotNull(comparison.newModels);
		assertEquals(3, comparison.newModels.length);
		assertEquals("test_model1", comparison.newModels[0].name);
		assertEquals("test_model2", comparison.newModels[1].name);
		assertEquals("test_model3", comparison.newModels[2].name);
	}
}