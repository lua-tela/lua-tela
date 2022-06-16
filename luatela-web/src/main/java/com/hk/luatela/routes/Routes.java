package com.hk.luatela.routes;

import com.hk.collections.lists.SortedList;
import com.hk.lua.*;
import com.hk.luatela.InitializationException;
import com.hk.luatela.LuaContext;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class Routes
{
	private final SortedList<Route> routeSet;
	private LuaInterpreter interp;
	PrintStream out;
	final Consumer<LuaInterpreter> preparer;

	public Routes(Consumer<LuaInterpreter> preparer, Path routesPath)
	{
		Path dataroot = routesPath.getParent();
		this.preparer = preparer;
		String source = routesPath.getFileName().toString();
		if(!Files.exists(routesPath))
			throw new InitializationException("'" + source + "' not found in data root directory (" + dataroot + ")");

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

		Lua.importStandard(interp);

		interp.setExtra("routes", this);
		interp.setExtra("dataroot", dataroot);
		interp.getGlobals().setVar("path", Lua.newMethod(RoutePath::new));

		routeSet = new SortedList<>(new Route.Comp());
	}

	public void collect(PrintStream out)
	{
		this.out = out;
		preparer.accept(interp);
		try
		{
			interp.execute();
		}
		catch(LuaException ex)
		{
			ex.printStackTrace();
			throw new InitializationException();
		}
		finally
		{
			this.out = null;
			interp = null;
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

	void newRoute(Route route)
	{
		routeSet.add(route);
	}
}