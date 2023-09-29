package com.hk.luatela.patch;

import com.hk.dialect.Dialect;

public class SequelTranslator
{
	private final LuaBase base;
	private final Dialect dialect;

	public SequelTranslator(LuaBase base, Dialect dialect)
	{
		this.base = base;
		this.dialect = dialect;
	}
}
