package com.hk.luatela.patch;

import com.hk.luatela.patch.models.ModelSet;
import com.hk.str.HTMLText;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.Assert.*;

public class PatchExportTest
{
	private File modelDir;

	@Before
	public void setUp() {
		modelDir = new File("src/test/resources/models");
	}

	@Test
	public void testSingleModel() throws FileNotFoundException, DatabaseException
	{
		ModelSet notEmptySet;

		notEmptySet = LuaBase.loadModelSet(new File(modelDir, "single_model.lua"));

		PatchComparison comparison = new PatchComparison(new ModelSet(), notEmptySet);
		assertNull(comparison.attemptCompare());

		PatchExport export = comparison.export(0).excludeHeader();
		assertNotNull(export);

		String exportName = export.getName();
		assertNotNull(exportName);

		assertTrue(exportName.startsWith("patch-1"));

		HTMLText txt = new HTMLText();
		assertSame(txt, export.toLua(txt));

		String code = "if patchNo ~= 1 then\n" +
				"\treturn false\n" +
				"end\n" +
				"patchNo = patchNo + 1\n" +
				"\n" +
				"models['point'] = {}\n" +
				"models['point']['x'] = { 'float', {primary=true} }\n" +
				"models['point']['y'] = { 'float', {primary=true} }\n" +
				"models['point']['z'] = { 'float', {primary=true} }\n" +
				"\n" +
				"return true";
		assertEquals(code, txt.create());
	}
}