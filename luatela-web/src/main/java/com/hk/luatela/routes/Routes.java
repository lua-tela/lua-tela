package com.hk.luatela.routes;

import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaLibrary;
import com.hk.luatela.InitializationException;
import com.hk.luatela.LuaTela;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;

public class Routes
{
	public int size = 0;
	private final TreeSet<Route> routeSet;

	public Routes(Path routesPath)
	{
		if(!Files.exists(routesPath))
			throw new InitializationException("'routes.lua' not found in data root directory");

		LuaInterpreter interp;
		try
		{
			interp = Lua.reader(Files.newBufferedReader(routesPath));

			interp.compile();
		}
		catch (LuaException e)
		{
			System.err.println("There was a problem compiling '" + routesPath + "'");
			throw e;
		}
		catch (IOException e)
		{
			throw new InitializationException(e);
		}

		LuaLibrary.importStandard(interp);
		interp.importLib(new LuaLibrary<>(null, RouteLibrary.class));

		interp.setExtra("routes", this);

		routeSet = new TreeSet<>(new Route.Comp());

		interp.execute();
	}

	boolean newRoute(Route route)
	{
		return false;
	}
}