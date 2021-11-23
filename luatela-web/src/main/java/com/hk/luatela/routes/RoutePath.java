package com.hk.luatela.routes;

import com.hk.lua.*;

import java.nio.file.Path;
import java.util.Arrays;

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
		pathMetatable.rawSet("path", Lua.newFunc(RoutePath::new));
		pathMetatable.rawSet("getfile", Lua.newFunc(RoutePath::getFile));
	}
}
