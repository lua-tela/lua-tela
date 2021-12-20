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
	public Condition isEqual(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Condition isNotEqual(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Condition isLessThan(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Condition isLessThanOrEQ(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Condition isGreaterThan(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Condition isGreaterThanOrEQ(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Condition isLike(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryValue add(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryValue subtract(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryValue multiply(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryValue divide(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryValue modulo(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryValue bitwiseAnd(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryValue bitwiseOr(QueryValue value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public QueryValue bitwiseExclusiveOr(QueryValue value)
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
