package com.hk.luatela.dialect.mysql;

import com.hk.luatela.dialect.Dialect;
import com.hk.luatela.dialect.Dialect.*;
import com.hk.str.HTMLText;

public class MySQLCondition implements Condition, MySQLDialect.MySQLDialectOwner
{
	public MySQLCondition(QueryTest test, QueryValue value)
	{
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
	public Condition and(Condition condition)
	{
		return null;
	}

	@Override
	public Condition or(Condition condition)
	{
		return null;
	}

	@Override
	public Condition not()
	{
		return null;
	}

	@Override
	public HTMLText print(HTMLText txt)
	{
		return null;
	}
}
