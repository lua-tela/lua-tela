package com.hk.luatela.routes;

import com.hk.lua.Lua;
import com.hk.lua.LuaFactory;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaLibrary;
import com.hk.luatela.InitializationException;
import com.hk.luatela.LuaTela;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
			factory = Lua.factory(Files.newBufferedReader(source));
		}
		catch (IOException e)
		{
			throw new InitializationException(e);
		}
	}

	@Override
	boolean matches(String url, String ctx, String path)
	{
		return path.startsWith(this.path);
	}

	@Override
	void serve(HttpServletRequest request, HttpServletResponse response)
	{
		LuaInterpreter interp = factory.build();

		LuaLibrary.importStandard(interp);
		luaTela.injectInfoVars(interp);

		interp.execute();
	}
}
