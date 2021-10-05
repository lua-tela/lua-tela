package com.hk.luatela.patch.models.fields;

import com.hk.lua.LuaException;
import com.hk.lua.LuaObject;
import com.hk.luatela.patch.models.Model;

import java.util.Map;

public class StringField extends DataField
{
	private int length;

	public StringField(Model parent, String name)
	{
		super(parent, name);
	}

	@Override
	DataField accept(LuaObject properties)
	{
		for(Map.Entry<LuaObject, LuaObject> entry : properties.getEntries())
		{
			if(!entry.getKey().isString())
				throw new LuaException("unexpected non-string field property '" + entry.getKey().name() + "'");

			if ("length".equals(entry.getKey().getString())) {
				length = (int) entry.getValue().getInteger();
			} else {
				throw new LuaException("unexpected field property '" + entry.getKey().getString() + "'");
			}
		}

		return this;
	}

	@Override
	public String name()
	{
		return "*FIELD_STRING";
	}
}
