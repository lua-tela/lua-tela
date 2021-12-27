package com.hk.luatela.dialect;

import com.hk.luatela.dialect.mysql.MySQLDialect;
import com.hk.luatela.dialect.mysql.MySQLDialect.MySQLQueryOperator;
import com.hk.luatela.dialect.mysql.MySQLDialect.MySQLQueryTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class MySQLDialectTest
{
	@Test
	public void testStuff()
	{
//		Dialect d = new MySQLDialect();
//
//		Dialect.TableMeta points = d.table(Dialect.Owner.LUA, "points");
//		Dialect.FieldMeta x = points.field("x");
//
////		CREATE TABLE `lua_points` (
////			`x` DOUBLE,
////			`y` DOUBLE,
////			PRIMARY KEY (`x`,`y`)
////		) ENGINE=MyISAM;
//
//		Dialect.Query q;
//		String expected;
//
//		expected = "SELECT `points`.`x` FROM `points` WHERE `points`.`x` = 1";
//		q = d.select(x).from(points).where(x.is(MySQLQueryTest.EQUALS, d.value(1)));
//		assertEquals(expected, Dialect.toString(q));
//
//		expected = "SELECT `points`.`x` FROM `points` WHERE `points`.`x` + 1 = 2";
//		q = d.select(x).from(points).where(x.op(MySQLQueryOperator.ADD, d.value(1)).is(MySQLQueryTest.EQUALS, d.value(2)));
//		assertEquals(expected, Dialect.toString(q));
	}
}