package com.hk.luatela.patch.models;

import com.hk.luatela.patch.DatabaseException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ModelSet implements Iterable<Model>
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

	@Override
	public Iterator<Model> iterator()
	{
		return new ModelIterator();
	}

	private class ModelIterator implements Iterator<Model>
	{
		private final Iterator<Map.Entry<String, Model>> itr;

		private ModelIterator()
		{
			itr = models.entrySet().iterator();
		}

		@Override
		public boolean hasNext()
		{
			return itr.hasNext();
		}

		@Override
		public Model next()
		{
			return itr.next().getValue();
		}
	}

	public static final String KEY = ModelSet.class.getName();
}
