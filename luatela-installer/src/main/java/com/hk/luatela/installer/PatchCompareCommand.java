package com.hk.luatela.installer;

import com.hk.luatela.patch.PatchComparison;
import com.hk.luatela.patch.models.Model;
import com.hk.str.HTMLText;

import java.util.Map;

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

			System.out.println();
		}

		if(comparison.removedModels != null && !comparison.removedModels.isEmpty())
		{
			System.out.println("Removed " + comparison.removedModels.size() + " model" + (comparison.removedModels.size() == 1 ? "" : "s"));
			for (Model removedModel : comparison.removedModels)
				System.out.println("\t- " + removedModel.name);

			System.out.println();
		}

		if(comparison.renamedModels != null && !comparison.renamedModels.isEmpty())
		{
			System.out.println("Renamed " + comparison.renamedModels.size() + " model" + (comparison.renamedModels.size() == 1 ? "" : "s"));
			for (Map.Entry<String, Model> entry : comparison.renamedModels.entrySet())
				System.out.println("\t- " + entry.getKey() + " to " + entry.getValue().name);

			System.out.println();
		}
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
