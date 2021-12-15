package com.hk.luatela.dialect.mysql;

import com.hk.luatela.dialect.Dialect;

public class MySQLDialect implements Dialect
{
	@Override
	public Query select(FieldMeta... fields)
	{
		return null;
	}

	@Override
	public QueryValue value(Object value)
	{
		return null;
	}

	@Override
	public TableMeta table(Owner owner, String name)
	{
		return null;
	}
}
