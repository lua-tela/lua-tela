package com.hk.luatela.dialect.mysql;

import com.hk.luatela.dialect.Dialect.*;
import com.hk.str.HTMLText;

public class MySQLFieldMeta implements FieldMeta, MySQLDialect.MySQLDialectOwner
{
	final String fieldName;

	public MySQLFieldMeta(MySQLTableMeta table, String name)
	{
		this.fieldName = table.tableName + ".`" + name + "`";
	}

	@Override
	public Condition is(QueryTest test, QueryValue value)
	{
		return new MySQLCondition(test, value);
	}

	@Override
	public QueryValue op(QueryOperator op, QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isValue()
	{
		return false;
	}

	@Override
	public HTMLText print(HTMLText txt)
	{
		return txt.wr(fieldName);
	}
}
