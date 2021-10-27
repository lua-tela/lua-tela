package com.hk.luatela.patch;

import com.hk.luatela.patch.models.Model;
import com.hk.luatela.patch.models.ModelSet;

import java.io.PrintStream;

public class PatchComparison
{
	private final LuaBase base;
	private final ModelSet before, after;
	public boolean unchanged = false;
	public Model[] newModels;

	public PatchComparison(LuaBase base, ModelSet after)
	{
		this.before = base.patchModelSet;
		this.base = base;
		this.after = after;
	}

	public Decision attemptCompare()
	{
		if(before.size() == 0)
		{
			if(after.size() != 0)
			{
				int i = 0;
				newModels = new Model[after.size()];
				for (Model model : after)
					newModels[i++] = model;
			}
			else
				unchanged = true;
		}
		else
		{
			for (Model model1 : before)
			{

			}
			throw new Error("TODO: Compare model names and their fields");
		}

		return null;
	}

	public void printSummary(PrintStream out)
	{
		if(unchanged)
		{
			out.println("Model Set unchanged");
			return;
		}

		if(newModels != null && newModels.length > 0)
		{
			out.println("Found " + newModels.length + " new model" + (newModels.length == 1 ? "" : "s"));
			for (Model newModel : newModels)
				out.println(newModel.name);
		}
	}

	public interface Decision
	{
	}
}