package com.hk.luatela.routes;

import com.hk.luatela.LuaTela;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;

class PageRoute extends Route
{
	PageRoute(String path, Path source)
	{

	}

	@Override
	boolean matches(String url, String path)
	{
		return false;
	}

	@Override
	void serve(LuaTela luaTela, HttpServletRequest request, HttpServletResponse response)
	{

	}
}
