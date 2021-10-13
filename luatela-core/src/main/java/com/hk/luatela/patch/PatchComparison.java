package com.hk.luatela.patch;

import com.hk.luatela.patch.models.ModelSet;

public class PatchComparison
{
	private final ModelSet before, after;

	public PatchComparison(ModelSet before, ModelSet after)
	{
		this.before = before;
		this.after = after;
	}

	public Decision attemptCompare()
	{
		return null;
	}

	interface Decision
	{
		boolean isComplete();
	}
}
