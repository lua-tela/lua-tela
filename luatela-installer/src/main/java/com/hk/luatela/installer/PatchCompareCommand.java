package com.hk.luatela.installer;

import com.hk.luatela.patch.PatchComparison;
import com.hk.luatela.patch.models.Model;
import com.hk.str.HTMLText;

import static com.hk.luatela.installer.Installer.splitToLinesByLen;

public class PatchCompareCommand extends PatchCommand
{
	@Override
	void doCompare(PatchComparison comparison)
	{
		super.doCompare(comparison);

		if(comparison.unchanged)
		{
			System.out.println("Model Set unchanged\n");
			return;
		}

		if(comparison.newModels != null && comparison.newModels.length > 0)
		{
			System.out.println("Found " + comparison.newModels.length + " new model" + (comparison.newModels.length == 1 ? "" : "s"));
			for (Model newModel : comparison.newModels)
				System.out.println("\t- " + newModel.name);
		}

		System.out.println();
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
