package com.hk.luatela.dialect;

public interface Dialect
{
	Query select(FieldMeta... fields);

	QueryValue value(Object value);

	TableMeta table(Owner owner, String name);

	interface Query
	{
		Query from(TableMeta... tables);

		Query where(Condition... conditions);

		Dialect dialect();

		String toString();
	}

	interface QueryValue
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

		Dialect dialect();

		String toString();
	}

	interface FieldMeta extends QueryValue
	{
		boolean isValue();

		Dialect dialect();

		String toString();
	}

	interface TableMeta
	{
		FieldMeta field(String name);

		Dialect dialect();

		String toString();
	}

	interface Condition extends QueryValue
	{
		Condition and(Condition condition);

		Condition or(Condition condition);

		Condition not();

		Dialect dialect();

		String toString();
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
