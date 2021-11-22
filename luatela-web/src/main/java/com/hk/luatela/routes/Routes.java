package com.hk.luatela.routes;

import com.hk.array.Concat;
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
	private final SortedList<Route> routeSet;
	final Consumer<LuaInterpreter> preparer;

	public Routes(Consumer<LuaInterpreter> preparer, Path routesPath)
	{
		this.preparer = preparer;
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

	static class RoutePath extends LuaUserdata
	{
		private final String path;

		RoutePath(LuaInterpreter interp, LuaObject[] objs)
		{
			StringBuilder sb = new StringBuilder();

			for(LuaObject obj : objs)
			{
				if(obj.isString() || obj.isNumber() || obj instanceof RoutePath)
					sb.append(obj.getString(interp));
			}

			path = sb.toString();
		}

		@Override
		public String name()
		{
			return "*PATH";
		}

		@Override
		public String getUserdata()
		{
			return path;
		}

		@Override
		public String getString(LuaInterpreter interp)
		{
			return path;
		}
	}
}