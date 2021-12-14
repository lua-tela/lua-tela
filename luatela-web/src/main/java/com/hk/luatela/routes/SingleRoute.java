package com.hk.luatela.routes;

import com.hk.lua.*;
import com.hk.luatela.InitializationException;
import com.hk.luatela.LuaContext;
import com.hk.luatela.luacompat.HTMLLibrary;
import com.hk.luatela.luacompat.RequestLibrary;
import com.hk.luatela.luacompat.ResponseLibrary;

import java.io.IOException;
import java.nio.file.Path;

class SingleRoute extends Route
{
	public final String path;
	private final LuaFactory factory;
	private final Path source;

	SingleRoute(Routes routes, String path, Path source, boolean compile)
	{
		super(routes);
		this.path = path;
		this.source = source;
		routes.out.println("Mapping '" + path + "' to " + source);

		if(compile)
		{
			try
			{
				factory = Lua.factory(source.toFile());

				factory.compile();
			}
			catch (IOException e)
			{
				throw new InitializationException(e);
			}
			catch (LuaException e)
			{
				e.printStackTrace();
				throw new InitializationException("There was a problem compiling " + source);
			}
		}
		else
			factory = null;
	}

	@Override
	boolean hasPath()
	{
		return true;
	}

	@Override
	public String getPath()
	{
		return path;
	}

	@Override
	boolean matches(String url, String ctx, String path)
	{
		return path.startsWith(this.path);
	}

	@Override
	void serve(LuaContext context) throws IOException
	{
		LuaInterpreter interp;

		if(factory == null)
			interp = Lua.reader(source.toFile());
		else
			interp = factory.build();

		interp.setExtra("context", context);

		Lua.importStandard(interp);
		routes.preparer.accept(interp);
		interp.importLib(new LuaLibrary<>("html", HTMLLibrary.class));
		interp.importLib(new LuaLibrary<>("request", RequestLibrary.class));
		interp.importLib(new LuaLibrary<>("response", ResponseLibrary.class));

		Object res = interp.execute();
		if(res instanceof LuaObject && !((LuaObject) res).isNil())
			handle(interp, (LuaObject) res, context, 1);
	}
}
