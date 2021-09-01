package com.hk.luatela.routes;

import com.hk.luatela.LuaTela;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Comparator;

abstract class Route
{
	abstract boolean matches(String url, String path);

	abstract void serve(LuaTela luaTela, HttpServletRequest request, HttpServletResponse response);

	static class Comp implements Comparator<Route>
	{
		@Override
		public int compare(Route o1, Route o2)
		{
			return 0;
		}
	}
}
