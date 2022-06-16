package com.hk.luatela.routes;

import com.hk.lua.LuaException;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaLibrary;
import com.hk.lua.LuaObject;
import com.hk.luatela.InitializationException;
import com.hk.luatela.LuaContext;
import com.hk.luatela.luacompat.HTMLLibrary;
import com.hk.luatela.luacompat.LuaTemplate;
import com.hk.luatela.luacompat.RequestLibrary;
import com.hk.luatela.luacompat.ResponseLibrary;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class TemplateRoute extends Route
{
	public final String path;
	private final LuaTemplate template;

	public TemplateRoute(Routes routes, String path, Path template)
	{
		super(routes);
		this.path = path;
		routes.out.println("Mapping '" + path + "' to template at " + template);

		try
		{
			this.template = new LuaTemplate(Files.newBufferedReader(template));
		}
		catch (IOException e)
		{
			throw new InitializationException(e);
		}
		catch (LuaException | LuaTemplate.TemplateException e)
		{
			e.printStackTrace();
			throw new InitializationException("There was a problem compiling template " + template);
		}
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
		PrintWriter writer = context.response.getWriter();

		LuaInterpreter interp = template.create(writer);
		interp.setExtra("context", context);

//		Lua.importStandard(interp);
		routes.preparer.accept(interp);
		interp.importLib(new LuaLibrary<>("html", HTMLLibrary.class));
		interp.importLib(new LuaLibrary<>("request", RequestLibrary.class));
		interp.importLib(new LuaLibrary<>("response", ResponseLibrary.class));

		LuaObject res = interp.execute();
		if(res instanceof LuaObject && !res.isNil())
			handle(interp, res, context, 1);
	}
}
