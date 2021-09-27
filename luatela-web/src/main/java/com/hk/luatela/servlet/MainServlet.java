package com.hk.luatela.servlet;

import com.hk.luatela.LuaContext;
import com.hk.luatela.LuaTela;
import com.hk.luatela.routes.Route;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainServlet extends HttpServlet
{
	protected void processRequest(HttpServletRequest request, HttpServletResponse response, String method) throws ServletException, IOException
	{
		LuaTela luaTela = ((LuaTela) getServletContext().getAttribute(LuaTela.QUALIKEY));
		if(request.getRequestURI().equals("/favicon.ico"))
		{
			Path path = luaTela.resourceRoot.resolve("favicon.ico");
			ResourceServlet.serveFile(getServletContext(), request, response, path);
		}
		else if(request.getRequestURI().equals("/robots.txt"))
		{
			Path path = luaTela.resourceRoot.resolve("robots.txt");
			ResourceServlet.serveFile(getServletContext(), request, response, path);
		}
		else
		{
			LuaContext context = new LuaContext(luaTela, request, response, method);
			Route route = luaTela.routes.match(context.url, context.ctx, context.path);

			if(route != null)
				luaTela.routes.attemptServe(route, context);
			else
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		processRequest(request, response, "get");
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		processRequest(request, response, "post");
	}

	@Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		processRequest(request, response, "head");
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		processRequest(request, response, "delete");
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		processRequest(request, response, "put");
	}
}