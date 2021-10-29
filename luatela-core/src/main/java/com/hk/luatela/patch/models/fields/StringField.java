package com.hk.luatela.patch.models.fields;

import com.hk.lua.LuaException;
import com.hk.lua.LuaObject;
import com.hk.luatela.patch.models.Model;
import com.hk.str.HTMLText;

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

			this.length = (int) length.getInteger();
		}

		return super.accept(properties);
	}

	@Override
	public void exportProps(HTMLText txt)
	{
		txt.wr("length=").wr(String.valueOf(this.length));
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
