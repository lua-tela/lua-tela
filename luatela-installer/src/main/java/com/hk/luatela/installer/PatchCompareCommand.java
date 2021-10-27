package com.hk.luatela.installer;

import com.hk.luatela.patch.PatchComparison;
import com.hk.str.HTMLText;

import static com.hk.luatela.installer.Installer.splitToLinesByLen;

public class PatchCompareCommand extends PatchCommand
{
	@Override
	void doCompare(PatchComparison comparison)
	{
		super.doCompare(comparison);

		comparison.printSummary(System.out);
	}

	@Override
	void help()
	{
		HTMLText txt = new HTMLText();

		txt.prln("This command attempts to compare the current");
		txt.prln("models to the previously patched models.").ln();

		txt.prln("Parameters:").tabUp();
		String str;

		txt.prln("--dataroot [data root directory]").tabUp();
		str = "This parameter should point to the 'dataroot' " +
				"directory, which contains the route and model info.\n" +
				"By default, if not provided, it will point to the 'base' " +
				"folder in the current working directory. " +
				"This folder is important and required for Lua " +
				"Tela to properly initialize.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.tabDown();
		System.out.println(txt.create());
	}
}
