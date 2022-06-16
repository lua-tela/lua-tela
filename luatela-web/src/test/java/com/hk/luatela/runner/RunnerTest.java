package com.hk.luatela.runner;

import com.hk.json.Json;
import com.hk.json.JsonArray;
import com.hk.json.JsonObject;
import com.hk.json.JsonValue;
import com.hk.lua.Lua;
import com.hk.luatela.routes.Routes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.junit.Assert.*;

public class RunnerTest
{
	private Path inits;

	@Before
	public void setUp() throws Exception
	{
		inits = Paths.get("src/test/resources/inits");

		Files.deleteIfExists(inits.resolve("onetime.json"));

		Path json = inits.resolve("tasks.json");
		Files.deleteIfExists(json);
		Json.write(json.toFile(), new JsonObject());
	}

	@Test
	public void test() throws IOException {
		int[] flag = new int[1];
		Runner runner = new Runner(interp -> {
			interp.getGlobals().setVar("finished", Lua.newMethod((interp1, args) -> {
				flag[0]++;
				return null;
			}));

			interp.getGlobals().setVar("inits", Lua.newString(inits.toString()));
		}, inits.resolve("init.lua"));

		assertNotNull(runner);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		runner.collect(new PrintStream(out));

		String output = out.toString().toLowerCase(Locale.ROOT);
		assertTrue(output.contains("service created"));

		assertEquals(1, flag[0]);
		assertEquals(2, runner.getTasks());

		try
		{
			Thread.sleep(6500);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		runner.close();

		JsonValue val = null;
		try
		{
			val = Json.read(inits.resolve("onetime.json").toFile());
		}
		catch (FileNotFoundException e)
		{
			fail(e.getLocalizedMessage());
		}
		assertEquals(Json.read("{\"my\":\"key\",\"magicnumber\":525600}"), val);
		try
		{
			val = Json.read(inits.resolve("tasks.json").toFile());
		}
		catch (FileNotFoundException e)
		{
			fail(e.getLocalizedMessage());
		}
		assertEquals(Json.read("{\"tasks\": 4}"), val);

		assertTrue(Files.deleteIfExists(inits.resolve("onetime.json")));
		assertTrue(Files.deleteIfExists(inits.resolve("tasks.json")));
	}

	@After
	public void tearDown() throws Exception
	{
	}
}