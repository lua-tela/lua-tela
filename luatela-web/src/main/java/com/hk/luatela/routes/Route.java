package com.hk.luatela.routes;

import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.luatela.LuaContext;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;

public abstract class Route
{
	protected final Routes routes;

	Route(Routes routes)
	{
		this.routes = routes;
	}

	abstract boolean matches(String url, String ctx, String path);

	abstract void serve(LuaContext context) throws ServletException, IOException;

	boolean hasPath()
	{
		return false;
	}

	String getPath()
	{
		return null;
	}

	static void handle(LuaInterpreter interp, LuaObject obj, PrintWriter writer, String path) throws IOException
	{
		if(obj.isFunction())
		{
			boolean cont;
			long pass = 1;
			do
			{
				LuaObject r = obj.callFunction(interp, path, pass++);
				cont = r.getBoolean();

				if(cont)
					handle(interp, r, writer, path);
			} while(cont);
		}
		else if(obj.isTable())
		{
			long len = obj.getLength();

			if(len > 0)
			{
				for(long i = 1; i <= len; i++)
					handle(interp, obj.getIndex(interp, i), writer, path);
			}
			else
				handle(interp, obj.getIndex(interp, path), writer, path);
		}
		else if(obj.isBoolean())
		{
			if(obj.getBoolean())
				writer.flush();
		}
		else
		{
			writer.write(obj.getString());
		}
	}

	static class Comp implements Comparator<Route>
	{
		@Override
		public int compare(Route o1, Route o2)
		{
			if(o1.hasPath() && o2.hasPath())
				return Integer.compare(o2.getPath().length(), o1.getPath().length());
			else
				throw new Error("should not be possible (yet)");
		}
	}
}
