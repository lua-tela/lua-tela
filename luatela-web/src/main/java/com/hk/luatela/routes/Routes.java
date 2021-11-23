package com.hk.luatela.routes;

import com.hk.collections.lists.SortedList;
import com.hk.lua.*;
import com.hk.luatela.InitializationException;
import com.hk.luatela.LuaContext;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class Routes
{
	private final Path dataroot;
	private final SortedList<Route> routeSet;
	final Consumer<LuaInterpreter> preparer;

	public Routes(Consumer<LuaInterpreter> preparer, Path routesPath)
	{
		dataroot = routesPath.getParent();
		this.preparer = preparer;
		String source = routesPath.getFileName().toString();
		if(!Files.exists(routesPath))
			throw new InitializationException("'" + source + "' not found in data root directory (" + routesPath.getParent() + ")");

		LuaInterpreter interp;
		try
		{
			interp = Lua.reader(Files.newBufferedReader(routesPath), source);

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

		interp.setExtra("dataroot", dataroot);
		interp.getGlobals().setVar("path", Lua.newFunc(RoutePath::new));

		preparer.accept(interp);

		interp.setExtra("routes", this);

		routeSet = new SortedList<>(new Route.Comp());

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