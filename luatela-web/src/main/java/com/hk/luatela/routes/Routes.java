package com.hk.luatela.routes;

import com.hk.collections.lists.SortedList;
import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaLibrary;
import com.hk.luatela.InitializationException;
import com.hk.luatela.LuaContext;
import com.hk.luatela.LuaTela;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Routes
{
	public final LuaTela luaTela;
	private final SortedList<Route> routeSet;

	public Routes(LuaTela luaTela, Path routesPath)
	{
		this.luaTela = luaTela;
		if(!Files.exists(routesPath))
			throw new InitializationException("'routes.lua' not found in data root directory (" + routesPath.getParent() + ")");

		LuaInterpreter interp;
		try
		{
			interp = Lua.reader(Files.newBufferedReader(routesPath), "routes.lua");

			interp.compile();
		}
		catch (LuaException e)
		{
			e.printStackTrace();
			throw new InitializationException("There was a problem compiling '" + routesPath + "'");
		}
		catch (IOException e)
		{
			throw new InitializationException(e);
		}

		LuaLibrary.importStandard(interp);
		interp.importLib(new LuaLibrary<>(null, RouteLibrary.class));

		luaTela.injectInto(interp);

		interp.setExtra("routes", this);

		routeSet = new SortedList<>(new Route.Comp());

		//this is my test

		try
		{
			interp.execute();
		}
		catch(LuaException ex)
		{
			ex.printStackTrace();
			throw new InitializationException();
		}
	}

	public int size()
	{
		return routeSet.size();
	}

	public Route match(String url, String ctx, String path)
	{
		for(Route route : routeSet)
		{
			if(route.matches(url, ctx, path))
				return route;
		}
		return null;
	}

	public void attemptServe(Route route, LuaContext context) throws ServletException, IOException
	{
		route.serve(context);
	}

	boolean newRoute(Route route)
	{
		return routeSet.add(route);
	}
}