package com.hk.luatela.patch.models;

import com.hk.luatela.patch.DatabaseException;

import java.util.HashMap;
import java.util.Map;

public class ModelSet
{
	private final Map<String, Model> models;

	public ModelSet()
	{
		models = new HashMap<>();
	}

	public int size()
	{
		return models.size();
	}

	public Model getModel(String name)
	{
		return models.get(name);
	}

	void addModel(Model model) throws DatabaseException
	{
		if(getModel(model.name) != null)
			throw new DatabaseException("duplicate model '" + model.name + "'");

		models.put(model.name, model);
	}

	public static final String KEY = ModelSet.class.getName();
}
