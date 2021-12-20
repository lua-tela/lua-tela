package com.hk.luatela.dialect.mysql;

import com.hk.luatela.dialect.Dialect;
import com.hk.luatela.dialect.mysql.MySQLPrimitiveValueMeta.PrimitiveType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

public class MySQLDialect implements Dialect
{
	@Override
	public Query select(FieldMeta... fields)
	{
		return new MySQLQuery(fields, null, null);
	}

	@Override
	public QueryValue value(Object value)
	{
		if(value == null)
			return new MySQLPrimitiveValueMeta(null, PrimitiveType.NULL);
		else if(value instanceof Double || value instanceof Float || value instanceof BigDecimal)
			return new MySQLPrimitiveValueMeta(value, PrimitiveType.DOUBLE);
		else if(value instanceof Long || value instanceof Integer ||
				value instanceof Short || value instanceof Byte || value instanceof BigInteger)
			return new MySQLPrimitiveValueMeta(value, PrimitiveType.INTEGER);
		else if(value instanceof CharSequence)
			return new MySQLPrimitiveValueMeta(value, PrimitiveType.STRING);
		else if(value instanceof Boolean)
			return new MySQLPrimitiveValueMeta(value, PrimitiveType.BOOLEAN);
		else if(value instanceof Date)
			return new MySQLPrimitiveValueMeta(value, PrimitiveType.DATE);
		else
			throw new UnsupportedOperationException("Cannot be turned into MySQL primitive: " + value);
	}

	@Override
	public TableMeta table(Owner owner, String name)
	{
		return new MySQLTableMeta(owner, name);
	}

	private static MySQLDialect instance;

	public static MySQLDialect getInstance()
	{
		if(instance == null)
			instance = new MySQLDialect();

		return instance;
	}

	interface MySQLDialectOwner extends DialectOwner
	{
		@Override
		default Dialect dialect() {
			return instance;
		}
	}
}
