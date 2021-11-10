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

	public PatchExport export()
	{
		return new PatchExport(base, this);
	}

	public interface Decision
	{
	}
}