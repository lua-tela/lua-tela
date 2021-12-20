package com.hk.luatela.dialect;

import com.hk.str.HTMLText;

public interface Dialect
{
	Query select(FieldMeta... fields);

	QueryValue value(Object value);

	TableMeta table(Owner owner, String name);

	static String toString(DialectOwner o)
	{
		return o.print(new HTMLText()).create();
	}

	interface Query extends DialectOwner
	{
		Query from(TableMeta... tables);

		Query where(Condition... conditions);

	}

	interface QueryValue extends DialectOwner
	{
		Condition isEqual(QueryValue value);

		Condition isNotEqual(QueryValue value);

		Condition isLessThan(QueryValue value);

		Condition isLessThanOrEQ(QueryValue value);

		Condition isGreaterThan(QueryValue value);

		Condition isGreaterThanOrEQ(QueryValue value);

		Condition isLike(QueryValue value);

		QueryValue add(QueryValue value);

		QueryValue subtract(QueryValue value);

		QueryValue multiply(QueryValue value);

		QueryValue divide(QueryValue value);

		QueryValue modulo(QueryValue value);

		QueryValue bitwiseAnd(QueryValue value);

		QueryValue bitwiseOr(QueryValue value);

		QueryValue bitwiseExclusiveOr(QueryValue value);
	}

	interface FieldMeta extends QueryValue
	{
		boolean isValue();
	}

	interface TableMeta extends DialectOwner
	{
		FieldMeta field(String name);
	}

	interface Condition extends QueryValue
	{
		Condition and(Condition condition);

		Condition or(Condition condition);

		Condition not();
	}

	interface DialectOwner
	{
		Dialect dialect();

		HTMLText print(HTMLText txt);
	}

	enum Owner
	{
		SYSTEM, LUA, USER;

		public String getPrefix()
		{
			switch(this)
			{
				case SYSTEM:
					return "sys";
				case LUA:
					return "lua";
				case USER:
					return "usr";
				default:
					throw new IllegalStateException("Unexpected value: " + this);
			}
		}
	}
}
