package com.hk.luatela.routes;

import com.hk.lua.Lua;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
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
	@SuppressWarnings("CodeBlock2Expr")
	public void test()
	{
		int[] flag = new int[1];
		Routes routes = new Routes(interp -> {
			interp.getGlobals().setVar("finished", Lua.newFunc((interp1, args) ->
			{
				flag[0]++;
				return null;
			}));
		}, this.routes.resolve("routes.lua"));

		assertNotNull(routes);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		routes.collect(new PrintStream(out));

		assertEquals(1, flag[0]);

		String str = out.toString().replace("\r\n", "\n");

		String expected = "Mapping '/2beornot2be' to src\\test\\resources\\routes\\pages\\hamlet.lua\n" +
				"Mapping '/hamlet' to src\\test\\resources\\routes\\pages\\hamlet.lua\n" +
				"Mapping '/pages/hamlet' to src\\test\\resources\\routes\\pages\\hamlet.lua\n" +
				"Mapping '/ham-let' to src\\test\\resources\\routes\\pages\\hamlet.lua\n" +
				"Mapping '/index' to template at src\\test\\resources\\routes\\templates\\index.lua.html\n";
		expected = expected.replace("\\", "(\\\\|/)");
		assertTrue(str.matches(expected));
	}
}