package com.hk.luatela.installer;

import com.hk.file.FileUtil;
import com.hk.luatela.patch.LuaBase;
import com.hk.luatela.patch.PatchComparison;
import com.hk.luatela.patch.PatchExport;
import com.hk.str.HTMLText;

import java.io.File;

public class PatchUpCommand extends PatchCommand
{
	@Override
	void handle(LuaBase base, PatchComparison comparison)
	{
		if(comparison.unchanged)
		{
			System.out.println("Model Set unchanged");
			return;
		}

		PatchExport export = comparison.export();

		HTMLText txt = export.toLua(new HTMLText());

		String code = txt.create();

		File patches = new File(base.dataroot, ".patches");

		if(!patches.exists())
			patches.mkdirs();

		// OUTPUT TO PATCH FILE

//		FileUtil.resetFile(patchFile, code);
	}

	@Override
	void help()
	{
		HTMLText txt = new HTMLText();

		txt.prln("This command attempts to patch the current model");
		txt.prln("along with the patchy model set.").ln();

		txt.prln("Parameters:").tabUp();
//		String str;

		help(txt);

		txt.tabDown();
		System.out.println(txt.create());
	}
}
