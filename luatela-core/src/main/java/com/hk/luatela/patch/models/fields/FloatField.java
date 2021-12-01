package com.hk.luatela.patch.models.fields;

import com.hk.luatela.patch.models.Model;

public class FloatField extends DataField
{
	public FloatField(Model parent, String name)
	{
		super(parent, name);
	}

	@Override
	public String name()
	{
		return "*FIELD_FLOAT";
	}
}
