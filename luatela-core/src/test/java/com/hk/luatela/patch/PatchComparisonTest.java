package com.hk.luatela.patch;

import com.hk.luatela.patch.models.Model;
import com.hk.luatela.patch.models.ModelSet;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class PatchComparisonTest {
	private File modelDir;

	@Before
	public void setUp() {
		modelDir = new File("src/test/resources/models");
	}

	@Test
	public void testEmptyToSingle() throws FileNotFoundException, DatabaseException
	{
		ModelSet notEmptySet;
		PatchComparison comparison;

		notEmptySet = LuaBase.loadModelSet(new File(modelDir, "student_grade.lua"));

		comparison = new PatchComparison(new ModelSet(), notEmptySet);

		assertNull(comparison.attemptCompare());
		assertFalse(comparison.unchanged);
		assertTrue(comparison.removedModels.isEmpty());
		assertTrue(comparison.renamedModels.isEmpty());

		assertNotNull(comparison.addedModels);
		assertEquals(1, comparison.addedModels.size());
		assertEquals("student_grade", comparison.addedModels.get(0).name);

		notEmptySet = LuaBase.loadModelSet(new File(modelDir, "single_model.lua"));

		comparison = new PatchComparison(new ModelSet(), notEmptySet);

		assertNull(comparison.attemptCompare());
		assertFalse(comparison.unchanged);

		assertNotNull(comparison.addedModels);
		assertEquals(1, comparison.addedModels.size());
		assertEquals("point", comparison.addedModels.get(0).name);
	}

	@Test
	public void testEmptyToMultiple() throws FileNotFoundException, DatabaseException
	{
		ModelSet notEmptySet;
		PatchComparison comparison;

		notEmptySet = LuaBase.loadModelSet(new File(modelDir, "test_models.lua"));

		comparison = new PatchComparison(new ModelSet(), notEmptySet);

		assertNull(comparison.attemptCompare());
		assertFalse(comparison.unchanged);
		assertTrue(comparison.removedModels.isEmpty());
		assertTrue(comparison.renamedModels.isEmpty());

		assertNotNull(comparison.addedModels);
		assertEquals(3, comparison.addedModels.size());
		assertEquals("test_model1", comparison.addedModels.get(0).name);
		assertEquals("test_model2", comparison.addedModels.get(1).name);
		assertEquals("test_model3", comparison.addedModels.get(2).name);
	}

	@Test
	public void testOneToTwoModels() throws FileNotFoundException, DatabaseException
	{
		ModelSet singleModel, doubleModel;
		PatchComparison comparison;

		singleModel = LuaBase.loadModelSet(new File(modelDir, "single_model.lua"));
		doubleModel = LuaBase.loadModelSet(new File(modelDir, "double_model.lua"));

		comparison = new PatchComparison(singleModel, doubleModel);

		assertNull(comparison.attemptCompare());
		assertFalse(comparison.unchanged);
		assertTrue(comparison.removedModels.isEmpty());
		assertTrue(comparison.renamedModels.isEmpty());

		assertNotNull(comparison.addedModels);
		assertEquals(1, comparison.addedModels.size());
		assertEquals("rectangle", comparison.addedModels.get(0).name);
	}

	@Test
	public void testTwoToOneModels() throws FileNotFoundException, DatabaseException
	{
		ModelSet singleModel, doubleModel;
		PatchComparison comparison;

		singleModel = LuaBase.loadModelSet(new File(modelDir, "single_model.lua"));
		doubleModel = LuaBase.loadModelSet(new File(modelDir, "double_model.lua"));

		comparison = new PatchComparison(doubleModel, singleModel);

		assertNull(comparison.attemptCompare());
		assertFalse(comparison.unchanged);
		assertTrue(comparison.addedModels.isEmpty());
		assertTrue(comparison.renamedModels.isEmpty());

		assertNotNull(comparison.removedModels);
		assertEquals(1, comparison.removedModels.size());
		assertEquals("rectangle", comparison.removedModels.get(0).name);
	}

	@Test
	public void testOneModelRenamed() throws FileNotFoundException, DatabaseException
	{
		ModelSet singleModel, renamedModel;
		PatchComparison comparison;

		singleModel = LuaBase.loadModelSet(new File(modelDir, "single_model.lua"));
		renamedModel = LuaBase.loadModelSet(new File(modelDir, "renamed_model.lua"));

		comparison = new PatchComparison(singleModel, renamedModel);

		assertNull(comparison.attemptCompare());
		assertFalse(comparison.unchanged);
		assertTrue(comparison.addedModels.isEmpty());
		assertTrue(comparison.removedModels.isEmpty());

		assertNotNull(comparison.renamedModels);
		assertEquals(1, comparison.renamedModels.size());
		assertTrue(comparison.renamedModels.containsKey("point"));
		Model renamed = comparison.renamedModels.get("point");
		assertNotNull(renamed);
		assertEquals("point3", renamed.name);
	}

	@Test
	public void testThreeModelsOneRenamed() throws FileNotFoundException, DatabaseException
	{
		ModelSet threeModels, threeModelsOneRenamed;
		PatchComparison comparison;

		threeModels = LuaBase.loadModelSet(new File(modelDir, "three_models.lua"));
		threeModelsOneRenamed = LuaBase.loadModelSet(new File(modelDir, "three_models_one_renamed.lua"));

		comparison = new PatchComparison(threeModels, threeModelsOneRenamed);

		assertNull(comparison.attemptCompare());
		assertFalse(comparison.unchanged);
		assertTrue(comparison.addedModels.isEmpty());
		assertTrue(comparison.removedModels.isEmpty());

		assertNotNull(comparison.renamedModels);
		assertEquals(1, comparison.renamedModels.size());
		assertTrue(comparison.renamedModels.containsKey("station_monthly_stats"));
		Model renamed = comparison.renamedModels.get("station_monthly_stats");
		assertNotNull(renamed);
		assertEquals("monthly_station_stats", renamed.name);
	}
}