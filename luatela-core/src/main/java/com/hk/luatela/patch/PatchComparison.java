package com.hk.luatela.patch;

import com.hk.luatela.patch.models.Model;
import com.hk.luatela.patch.models.ModelSet;

import java.util.*;

public class PatchComparison
{
	private final ModelSet before, after;
	public boolean unchanged = false;
	public List<Model> addedModels, removedModels;
	public Map<String, Model> renamedModels;

	public PatchComparison(ModelSet before, ModelSet after)
	{
		this.before = before;
		this.after = after;
	}

	public Decision attemptCompare()
	{
		Set<String> added = new HashSet<>();
		Set<String> deleted = new HashSet<>();

		for (Model model : before)
			deleted.add(model.name);
		for (Model model : after)
			added.add(model.name);

		Set<String> included = new HashSet<>(added);
		Set<String> tmp = new HashSet<>(added);
		included.retainAll(deleted);
		added.removeAll(deleted);
		deleted.removeAll(tmp);

//		System.out.println(added);
//		System.out.println(deleted);
//		System.out.println(included);

		unchanged = added.isEmpty() && deleted.isEmpty();
		if(unchanged)
		{
			for (String name : included)
			{
				Model a = before.getModel(name);
				Model b = after.getModel(name);

				if (!a.equals(b))
				{
					unchanged = false;
					break;
				}
			}

			if (unchanged)
				return null;
		}

		addedModels = new ArrayList<>();
		removedModels = new ArrayList<>();
		renamedModels = new HashMap<>();

		// this should have some leeway in the case that models are
		// renamed AND internally changed
		if(added.isEmpty() && !deleted.isEmpty())
		{
			// guaranteed removed models
			for(String name : deleted)
				removedModels.add(before.getModel(name));
		}
		else if(deleted.isEmpty() && !added.isEmpty())
		{
			// guaranteed added models
			for(String name : added)
				addedModels.add(after.getModel(name));
		}
		else if(!added.isEmpty() && !deleted.isEmpty())
		{
			Model afterModel;
			Set<String> removeBeforeNames = new HashSet<>();
			Set<String> removeAfterNames = new HashSet<>();
			for(String beforeName : deleted)
			{
				for(String afterName : added)
				{
					afterModel = after.getModel(afterName);
					if (before.getModel(beforeName).equals(afterModel))
					{
						removeBeforeNames.add(beforeName);
						removeAfterNames.add(afterName);
						renamedModels.put(beforeName, afterModel);
					}
				}
			}
			added.removeAll(removeAfterNames);
			deleted.removeAll(removeBeforeNames);

			for(String name : added)
				addedModels.add(after.getModel(name));
			for(String name : deleted)
				removedModels.add(before.getModel(name));
		}
		else // if(added.isEmpty() && deleted.isEmpty())
		{
			throw new Error("TODO: data field changes");
			// models possibly internally changed
		}

		return null;
	}

	public PatchExport export(int patchCount)
	{
		return new PatchExport(patchCount, this);
	}

	public interface Decision
	{
	}
}