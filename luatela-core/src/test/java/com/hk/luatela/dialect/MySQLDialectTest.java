package com.hk.luatela.dialect;

import com.hk.luatela.dialect.mysql.MySQLDialect;
import org.junit.Test;

import static org.junit.Assert.*;

public class MySQLDialectTest
{
	@SuppressWarnings("ConstantConditions")
	@Test
	public void testStuff()
	{
		Dialect d = null; // new MySQLDialect();

		Dialect.TableMeta points = d.table(Dialect.Owner.LUA, "points");
		Dialect.FieldMeta x = points.field("x");

//		CREATE TABLE `lua_points` (
//			`x` DOUBLE,
//			`y` DOUBLE,
//			PRIMARY KEY (`x`,`y`)
//		) ENGINE=MyISAM;

//		SELECT `points`.`x` FROM `points` WHERE `points`.`x` = 1
		d.select(x).from(points).where(x.isEqual(d.value("1")));
	}
}