package com.hk.luatela.patch.models.fields;

import com.hk.luatela.patch.models.Model;

public class IntegerField extends DataField
{
	public IntegerField(Model parent, String name)
	{
		super(parent, name);
	}

	@Override
	public String name()
	{
		return "*FIELD_INTEGER";
	}
}
