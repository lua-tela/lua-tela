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

		if(comparison.unchanged)
			System.out.println("Model Set unchanged");
		else
			comparison.printSummary(System.out);
	}

	@Override
	void help()
	{
		HTMLText txt = new HTMLText();

		txt.prln("This command attempts to compare the current");
		txt.prln("models to the previously patched models.").ln();

		txt.prln("Parameters:").tabUp();
//		String str;

		help(txt);

		txt.tabDown();
		System.out.println(txt.create());
	}
}
