package com.hk.luatela;

import com.hk.luatela.routes.Routes;
import com.hk.luatela.servlet.ResourceServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LuaTela
{
	public final ServletContext context;
	public final String resourcePath;
	public final Path dataRoot, resourceRoot;
	private final Routes routes;

	public LuaTela(ServletContext context)
	{
		if(context.getAttribute(QUALIKEY) != null)
			throw new InitializationException("Lua-Tela Servlet already initialized");

		this.context = context;

		String resourcePath = context.getInitParameter("resourcepath");

		if(resourcePath == null)
			resourcePath = "res";

		if(resourcePath.startsWith("/"))
			resourcePath = resourcePath.substring(1);
		if(resourcePath.endsWith("/"))
			resourcePath = resourcePath.substring(0, resourcePath.length() - 1);

		if(!resourcePath.matches("[a-zA-Z0-9/\\-.]+"))
			throw new InitializationException("resourcepath should only contain letters (a-z), numbers (0-9), slashes (/), dashes (-), and periods (.)");

		this.resourcePath = resourcePath;

		dataRoot = getFile(context, "dataroot", true);
		Path resourceRoot = getFile(context, "resourceroot", false);

		if(resourceRoot == null)
			resourceRoot = dataRoot.resolve(resourcePath);

		this.resourceRoot = resourceRoot;

		ServletRegistration.Dynamic registration = context.addServlet(
				ResourceServlet.class.getName(), ResourceServlet.class);
		registration.setLoadOnStartup(1);
		registration.addMapping("/" + resourcePath + "/*");

		this.routes = new Routes(dataRoot.resolve("routes.lua"));

		context.setAttribute(QUALIKEY, this);
	}

	public void output(PrintStream out)
	{
		out.println("Using Data-Root: \"" + dataRoot + "\"");
		out.println("Using Resource-Root: \"" + resourceRoot + "\"");
		out.println("Using Resource-Path: \"" + resourcePath + "\"");
		out.print("Loaded " + routes.size);
		out.println(" route" + (routes.size == 1 ? "" : "s") + " roots");
	}

	private Path getFile(ServletContext context, String name, boolean required)
	{
		String string = context.getInitParameter(name);

		if(string == null)
		{
			if(required)
				throw new InitializationException(name + " must be included in web.xml");

			return null;
		}

		int idx = string.indexOf(':');

		if(idx <= 0 || idx == string.length() - 1)
			throw new InitializationException(name + " must be in '[type]:[path]' format.");

		String path = string.substring(idx + 1);

		switch(string.substring(0, idx))
		{
			case "rel":
				path = context.getRealPath(path.replace(File.separator, "/"));
				break;
			case "pth":
				path = System.getProperty("user.dir") + File.separator + path;
				break;
			case "abs":
				if(!Paths.get(path).isAbsolute())
					throw new InitializationException("expected " + name + " to be an absolute path");
				break;
			default:
				throw new InitializationException(name + " type must be 'rel' (relative to wd), 'pth' (relative to webapp), or 'abs' (absolute).");
		}

		Path fileRoot = Paths.get(path);

		if(!Files.exists(fileRoot) || !Files.isDirectory(fileRoot))
			throw new InitializationException(name + " not found: " + fileRoot);

		return fileRoot;
	}

	public static final String QUALIKEY = LuaTela.class.getName();
}
