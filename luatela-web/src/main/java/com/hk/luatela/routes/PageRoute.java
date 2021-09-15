package com.hk.luatela.routes;

import com.hk.lua.*;
import com.hk.luatela.InitializationException;
import com.hk.luatela.LuaContext;
import com.hk.luatela.LuaTela;
import com.hk.luatela.luacompat.HTMLLibrary;
import com.hk.luatela.luacompat.RequestLibrary;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class PageRoute extends Route
{
	private final LuaFactory factory;
	public final String path;

	PageRoute(LuaTela luaTela, String path, Path source)
	{
		super(luaTela);
		this.path = path;
		System.out.println("Mapping '" + path + "' to " + source);

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

	@Override
	boolean matches(String url, String ctx, String path)
	{
		return path.startsWith(this.path);
	}

	@Override
	void serve(LuaContext context) throws IOException
	{
		LuaInterpreter interp = factory.build();

		interp.setExtra("context", context);

		LuaLibrary.importStandard(interp);
		luaTela.injectInfoVars(interp);
		interp.importLib(new LuaLibrary<>("html", HTMLLibrary.class));
		interp.importLib(new LuaLibrary<>("request", RequestLibrary.class));

		Object res = interp.execute();
		if(res instanceof LuaObject && !((LuaObject) res).isNil())
			handle(interp, (LuaObject) res, context.response.getWriter(), context.path);
	}
}
