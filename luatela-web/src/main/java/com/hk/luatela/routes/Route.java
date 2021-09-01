package com.hk.luatela.routes;

import com.hk.luatela.LuaTela;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;

public abstract class Route
{
	protected final LuaTela luaTela;

	Route(LuaTela luaTela)
	{
		this.luaTela = luaTela;
	}

	abstract boolean matches(String url, String ctx, String path);

	abstract void serve(HttpServletRequest request, HttpServletResponse response);

	static class Comp implements Comparator<Route>
	{
		@Override
		public int compare(Route o1, Route o2)
		{
			if(o1 instanceof PageRoute && o2 instanceof PageRoute)
				return Integer.compare(((PageRoute) o2).path.length(), ((PageRoute) o1).path.length());
			else
				throw new Error("should not be possible (yet)");
		}
	}
}
