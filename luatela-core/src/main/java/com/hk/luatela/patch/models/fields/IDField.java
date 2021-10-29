package com.hk.luatela.patch.models.fields;

import com.hk.luatela.patch.models.Model;

public class IDField extends IntegerField
{
	public IDField(Model parent, String name)
	{
		super(parent, name);
	}

	public IDField(Model parent, String name, boolean primary)
	{
		super(parent, name);
		this.primary = primary;
	}

	@Override
	public String name()
	{
		return "*FIELD_ID";
	}
}
