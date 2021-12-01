package com.hk.luatela.servlet;

import com.hk.io.IOUtil;
import com.hk.luatela.LuaTela;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("UnusedReturnValue")
public class ResourceServlet extends HttpServlet
{
	public static boolean serveFile(ServletContext context, HttpServletResponse response, Path path) throws IOException
	{
		return serveFile(context, response, path, null);
	}

	public static boolean serveFile(ServletContext context, HttpServletResponse response, Path path, String mime) throws IOException
	{
		if(!Files.exists(path))
		{
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return false;
		}
		if(Files.isDirectory(path))
		{
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return false;
		}

		if(mime == null)
			mime = context.getMimeType(path.toString());

		long len = Files.size(path);
		if(len > Integer.MAX_VALUE)
			response.setContentLengthLong(len);
		else
			response.setContentLength((int) len);

		if (mime != null)
			response.setContentType(mime);

		InputStream in = Files.newInputStream(path);
		OutputStream out = response.getOutputStream();
		IOUtil.copyTo(in, out);
		out.close();
		in.close();
		return true;
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		ServletContext context = getServletContext();
		LuaTela luaTela = (LuaTela) context.getAttribute(LuaTela.QUALIKEY);

		String path = request.getRequestURI().substring(luaTela.resourcePath.length() + 1);
		if(path.startsWith("/"))
			path = path.substring(1);

		serveFile(getServletContext(), response, luaTela.resourceRoot.resolve(path));
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	{
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Override
	protected void doHead(HttpServletRequest request, HttpServletResponse response)
	{
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
	{
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
	{
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}
}