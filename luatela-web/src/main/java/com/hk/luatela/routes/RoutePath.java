package com.hk.luatela.routes;

import com.hk.lua.*;
import com.hk.luatela.InitializationException;

import java.nio.file.Files;
import java.nio.file.Path;

class RoutePath extends LuaUserdata
{
	private final String path;

	RoutePath(LuaInterpreter interp, LuaObject[] objs)
	{
		StringBuilder sb = new StringBuilder();

		for (LuaObject obj : objs)
		{
			if (obj.isString() || obj.isNumber() || obj instanceof RoutePath)
				sb.append(obj.getString(interp));
		}

		path = sb.toString();

		metatable = pathMetatable;
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

	private static LuaObject getFile(LuaInterpreter interp, LuaObject[] args)
	{
		RoutePath path = checkFirst("getfile", args);
		Path dataroot = interp.getExtra("dataroot", Path.class);
		String str = path.path;
		if(str.startsWith("\\") || str.startsWith("/"))
			str = str.substring(1);
		return Lua.newString(dataroot.resolve(str).toString());
	}

	private static LuaObject toPage(LuaInterpreter interp, LuaObject[] args)
	{
		RoutePath path = checkFirst("topage", args);
		Routes routes = interp.getExtra("routes", Routes.class);
		Path pages = interp.getExtra("dataroot", Path.class).resolve("pages");
		String str;

		if(args.length > 1)
		{
			if(args[1].isString() || args[1] instanceof RoutePath)
				str = args[1].getString();
			else
				throw new LuaException("bad argument #2 to 'topage' (expected nothing or string)");
		}
		else
			str = path.path;

		if(str.startsWith("\\") || str.startsWith("/"))
			str = str.substring(1);

		if(!str.endsWith(".lua"))
			str += ".lua";

		Path page = pages.resolve(str);
		routes.newRoute(new SingleRoute(routes, path.path, page, true));
		return null;
	}

	private static LuaObject toSource(LuaInterpreter interp, LuaObject[] args)
	{
		RoutePath path = checkFirst("tosource", args);
		Routes routes = interp.getExtra("routes", Routes.class);
		Path dataroot = interp.getExtra("dataroot", Path.class);
		String str;

		if(args.length > 1)
		{
			if(args[1].isString() || args[1] instanceof RoutePath)
				str = args[1].getString();
			else
				throw new LuaException("bad argument #2 to 'tosource' (expected nothing or string)");
		}
		else
			str = path.path;

		if(str.startsWith("\\") || str.startsWith("/"))
			str = str.substring(1);

		if(!str.endsWith(".lua"))
			str += ".lua";

		Path file = dataroot.resolve(str);

		routes.newRoute(new SingleRoute(routes, path.path, file, false));
		return null;
	}

	private static LuaObject toTemplate(LuaInterpreter interp, LuaObject[] args)
	{
		RoutePath path = checkFirst("totemplate", args);
		Routes routes = interp.getExtra("routes", Routes.class);
		Path templates = interp.getExtra("dataroot", Path.class).resolve("templates");
		String str;

		if(args.length > 1)
		{
			if(args[1].isString() || args[1] instanceof RoutePath)
				str = args[1].getString();
			else
				throw new LuaException("bad argument #2 to 'totemplate' (expected nothing or string)");
		}
		else
			str = path.path;

		if(str.startsWith("\\") || str.startsWith("/"))
			str = str.substring(1);

		String[] exts = { "", ".html", ".lua", ".html.lua", ".lua.html" };
		Path template, found = null;
		for(String ext : exts)
		{
			template = templates.resolve(str + ext);

			if(Files.exists(template))
			{
				found = template;
				break;
			}
		}

		if(found == null)
			throw new InitializationException("Template not found in directory: " + templates);

		routes.newRoute(new TemplateRoute(routes, path.path, found));
		return null;
	}

	private static RoutePath checkFirst(String method, LuaObject[] args)
	{
		if(args.length < 1 || !(args[0] instanceof RoutePath))
			throw new LuaException("bad argument #1 to '" + method + "' (expected path object)");
		return (RoutePath) args[0];
	}

	private static final LuaObject pathMetatable;

	static
	{
		pathMetatable = Lua.newTable();

		pathMetatable.rawSet("__name", "*PATH");
		pathMetatable.rawSet("__index", pathMetatable);
		pathMetatable.rawSet("path", Lua.newMethod(RoutePath::new));
		pathMetatable.rawSet("getfile", Lua.newMethod(RoutePath::getFile));
		pathMetatable.rawSet("topage", Lua.newMethod(RoutePath::toPage));
		pathMetatable.rawSet("tosource", Lua.newMethod(RoutePath::toSource));
		pathMetatable.rawSet("totemplate", Lua.newMethod(RoutePath::toTemplate));
	}
}
