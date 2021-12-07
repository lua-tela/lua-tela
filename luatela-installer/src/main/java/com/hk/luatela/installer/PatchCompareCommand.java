package com.hk.luatela.installer;

import com.hk.luatela.patch.PatchComparison;
import com.hk.luatela.patch.models.Model;
import com.hk.str.HTMLText;

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

		if(comparison.addedModels != null && !comparison.addedModels.isEmpty())
		{
			System.out.println("Found " + comparison.addedModels.size() + " new model" + (comparison.addedModels.size() == 1 ? "" : "s"));
			for (Model newModel : comparison.addedModels)
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
