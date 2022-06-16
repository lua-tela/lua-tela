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
		LuaObject length = properties.rawGet("length");

		if(!length.isNil())
		{
			if(!length.isInteger())
				throw new LuaException("expected length of field '" + name + "' to be an integer");

			this.length = length.getInt();
		}

		return super.accept(properties);
	}

	@Override
	public Map<String, String> exportProps(Map<String, String> map)
	{
		map = super.exportProps(map);

		map.put("length", String.valueOf(this.length));

		return map;
	}

	@Override
	public String name()
	{
		return "*FIELD_STRING";
	}

	public int getMaxLength()
	{
		return length;
	}
}
