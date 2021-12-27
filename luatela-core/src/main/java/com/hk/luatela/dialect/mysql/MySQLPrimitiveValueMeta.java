package com.hk.luatela.dialect.mysql;

import com.hk.luatela.dialect.Dialect.*;
import com.hk.str.HTMLText;

public class MySQLPrimitiveValueMeta implements QueryValue, MySQLDialect.MySQLDialectOwner
{
	private final Object value;
	private final PrimitiveType type;

	MySQLPrimitiveValueMeta(Object value, PrimitiveType type)
	{
		this.value = value;
		this.type = type;
	}

	@Override
	public Condition is(QueryTest test, QueryValue value)
	{
		return null;
	}

	@Override
	public QueryValue op(QueryOperator op, QueryValue value)
	{
		return null;
	}

	@Override
	public HTMLText print(HTMLText txt)
	{
		throw new UnsupportedOperationException();
	}

	enum PrimitiveType
	{
		NULL, DOUBLE, INTEGER, STRING, BOOLEAN, DATE;
	}
}
