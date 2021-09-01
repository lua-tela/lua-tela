package com.hk.luatela.routes;

import com.hk.func.BiConsumer;
import com.hk.lua.*;
import com.hk.luatela.InitializationException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public enum RouteLibrary implements BiConsumer<Environment, LuaObject>, Lua.LuaMethod
{
	path() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject... args)
		{
			StringBuilder sb = new StringBuilder();

			for(int i = 0; i < args.length; i++)
			{
				if(!args[i].isString())
					throw new LuaException("bad argument #" + (i + 1) + " to 'path' (string expected)");

				sb.append(args[i].getString());
			}

			LuaObject str = Lua.newString(sb);

			LuaObject tbl = Lua.newTable();
			tbl.rawSet("_path", str);

			for(RouteLibrary method : values())
			{
				if(method.name().startsWith("to") || method.name().equals("path"))
				{
					tbl.rawSet(method.name(), Lua.newFunc((interp1, args1) ->
					{
						LuaObject[] args2 = new LuaObject[args1.length + 1];
						args2[0] = str;
						System.arraycopy(args1, 0, args2, 1, args1.length);
						return method.call(interp1, args2);
					}));
				}
			}
			return tbl;
		}
	},
	topage() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject... args)
		{
			if(args.length == 0)
				throw new LuaException("bad argument #1 to 'topage' (string or result of path() expected)");

			String path;

			if(args[0].isTable() && args[0].rawGet("_path").isString())
				path = args[0].rawGet("_path").getString();
			else if(args[0].isString())
				path = args[0].getString();
			else
				throw new LuaException("bad argument #1 to 'topage' (string or result of path() expected)");

			if(args.length < 2 || !args[1].isString())
				throw new LuaException("bad argument #2 to 'topage' (string expected)");

			String src = args[1].getString();
			if(src.startsWith(File.separator) || src.startsWith("/") || src.startsWith("\\"))
				src = src.substring(1);
			if(!src.endsWith(".lua"))
				src += ".lua";

			Routes routes = interp.getExtra("routes", Routes.class);

			if(src.startsWith("../"))
				src = src.substring(3);
			else
				src = "pages/" + src;

			Path source = routes.luaTela.dataRoot.resolve(src);

			if(!Files.exists(source))
				throw new LuaException("mapped page file not found: " + source);

			return Lua.newBoolean(routes.newRoute(new PageRoute(routes.luaTela, path, source)));
		}
	};

	@Override
	public void accept(Environment env, LuaObject table)
	{
		String name = toString();
		if(name != null && !name.trim().isEmpty())
			table.rawSet(Lua.newString(name), Lua.newFunc(this));
	}
}
