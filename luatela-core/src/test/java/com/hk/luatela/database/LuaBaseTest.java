package com.hk.luatela.database;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class LuaBaseTest
{
	@Test
	public void test()
	{
		File res = new File("src/test/resources/base");

		LuaBase base = new LuaBase(res);
	}
}