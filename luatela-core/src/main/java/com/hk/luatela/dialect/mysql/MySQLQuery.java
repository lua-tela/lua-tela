package com.hk.luatela.dialect.mysql;

import com.hk.luatela.dialect.Dialect;
import com.hk.luatela.dialect.Dialect.*;
import com.hk.str.HTMLText;

public class MySQLQuery implements Query, MySQLDialect.MySQLDialectOwner
{
	final FieldMeta[] fields;
	final TableMeta[] tables;
	final Condition[] conditions;

	public MySQLQuery(FieldMeta[] fields, TableMeta[] tables, Condition[] conditions)
	{
		this.fields = fields;
		this.tables = tables;
		this.conditions = conditions;
	}

	@Override
	public Query from(TableMeta... tables)
	{
		return new MySQLQuery(fields, tables, null);
	}

	@Override
	public Query where(Condition... conditions)
	{
		return new MySQLQuery(fields, tables, conditions);
	}

	@Override
	public HTMLText print(HTMLText txt)
	{
		txt.wr("SELECT ");
		for (int i = 0; i < fields.length; i++)
		{
			fields[i].print(txt);

			if(i < fields.length - 1)
				txt.wr(", ");
		}
		txt.wr(" FROM ");
		for (int i = 0; i < tables.length; i++)
		{
			tables[i].print(txt);

			if(i < tables.length - 1)
				txt.wr(", ");
		}
		txt.wr(" WHERE ");
		for (int i = 0; i < conditions.length; i++)
		{
			conditions[i].print(txt);

			if(i < conditions.length - 1)
				txt.wr(" AND ");
		}
		return txt;
	}
}