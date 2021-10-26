package com.hk.luatela.patch;

import com.hk.luatela.patch.models.Model;
import com.hk.luatela.patch.models.ModelSet;

public class PatchComparison
{
	private final ModelSet before, after;
	public boolean unchanged = false;
	public Model[] newModels;

	public PatchComparison(ModelSet before, ModelSet after)
	{
		this.before = before;
		this.after = after;
	}

	public Decision attemptCompare()
	{
		if(before.size() == 0 && after.size() > 0)
		{
			int i = 0;
			newModels = new Model[after.size()];
			for (Model model : after)
				newModels[i++] = model;
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

	interface Decision
	{
	}
}