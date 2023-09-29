package com.hk.luatela.patch.models;

import com.hk.lua.*;
import com.hk.luatela.patch.DatabaseException;
import com.hk.luatela.patch.models.fields.DataField;

import java.util.HashMap;
import java.util.Map;

public class Instance extends LuaUserdata
{
	private final Model model;
	private boolean generated, changed;
	protected final Map<String, LuaObject> values, edited;

	public Instance(Model model, boolean generated)
	{
		this.model = model;
		this.values = new HashMap<>();
		this.edited = new HashMap<>();
		this.generated = generated;
		changed = false;
		this.metatable = model.instanceMetatable;

		model.getFields().forEach(dataField -> dataField.initialize(this));
	}

	public void put(String name, LuaObject val)
	{
		if(!generated)
		{
			changed = true;
			edited.put(name, val);
		}
		else
			values.put(name, val);
	}

	@Override
	public LuaObject doIndex(LuaInterpreter interp, LuaObject key)
	{
		if(key.isString())
		{
			String index = key.getString(interp);

			if(model.hasFieldNamed(index))
			{
				LuaObject val = edited.get(index);
				if(val == null)
					val = values.get(index);

				return val == null ? Lua.NIL : val;
			}
		}
		return super.doIndex(interp, key);
	}

	@Override
	public void doNewIndex(LuaInterpreter interp, LuaObject key, LuaObject value)
	{
		if(key.isString())
		{
			String index = key.getString(interp);
			DataField field = model.getFieldNamed(index);

			if(field != null)
			{
				put(index, field.clean(value));
				return;
			}
		}
		super.doNewIndex(interp, key, value);
	}

	public Model getModel()
	{
		return model;
	}

	@Override
	public String name()
	{
		return __NAME;
	}

	@Override
	public Instance getUserdata()
	{
		return this;
	}

	@Override
	public String getString(LuaInterpreter interp)
	{
		return "'" + model.name + "' instance";
	}

	static Instance apply(Instance ins, LuaObject values) throws LuaException
	{
		for (DataField field : ins.model.getFields())
			ins.values.put(field.name, field.clean(values.rawGet(field.name)));

		return ins;
	}

	public static final String __NAME = "*INSTANCE";
}
