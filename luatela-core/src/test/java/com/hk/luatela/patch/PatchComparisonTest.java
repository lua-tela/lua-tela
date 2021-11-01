package com.hk.luatela.patch;

import com.hk.luatela.patch.models.ModelSet;
import com.hk.str.HTMLText;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;

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

		PatchExport export = comparison.export();
		assertNotNull(export);

		String exportName = export.getName();
		assertNotNull(exportName);

		assertTrue(exportName.startsWith("patch-1"));

		HTMLText txt = new HTMLText();

		assertSame(txt, export.toLua(txt));

		String code = txt.create();

		assertTrue(code.contains("models['point'] ="));
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

	private LuaBase wrap(ModelSet set)
	{
		try
		{
			LuaBase base = new LuaBase(modelDir);
			set.startStitch();
			base.patchModelSet = set;
			set.endStitch();
			return base;
		}
		catch (FileNotFoundException e)
		{
			throw new UncheckedIOException(e);
		}
	}
}