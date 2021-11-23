package com.hk.luatela.routes;

import com.hk.lua.Lua;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class RoutesTest
{
	private Path routes;

	@Before
	public void setUp()
	{
		routes = Paths.get("src/test/resources/routes");
	}

	@Test
	public void test()
	{
		int[] flag = new int[1];
		Routes routes = new Routes(interp -> interp.getGlobals().setVar("finished", Lua.newFunc((interp1, args) ->
		{
			flag[0]++;
			return null;
		})), this.routes.resolve("routes.lua"));

		assertNotNull(routes);
		assertEquals(1, flag[0]);
	}
}