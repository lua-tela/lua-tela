package com.hk.luatela;

import com.hk.luatela.servlet.ResourceServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class LuaTela
{
	public final ServletContext context;
	public final String resourcePath;
	public final Path dataRoot, resourceRoot;

	public LuaTela(ServletContext context)
	{
		if(context.getAttribute(QUALIKEY) != null)
			throw new IllegalArgumentException("Lua-Tela Servlet already initialized");

		this.context = context;

		String resourcePath = context.getInitParameter("resourcepath");

		if(resourcePath == null)
			resourcePath = "res";

		if(resourcePath.startsWith("/"))
			resourcePath = resourcePath.substring(1);
		if(resourcePath.endsWith("/"))
			resourcePath = resourcePath.substring(0, resourcePath.length() - 1);

		if(!resourcePath.matches("[a-zA-Z0-9/\\-.]+"))
			throw new IllegalArgumentException("resourcepath should only contain letters (a-z), numbers (0-9), slashes (/), dashes (-), and periods (.)");

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

		context.setAttribute(QUALIKEY, this);
	}

	private Path getFile(ServletContext context, String name, boolean required)
	{
		String string = context.getInitParameter(name);

		if(string == null)
		{
			if(required)
				throw new IllegalArgumentException(name + " must be included in web.xml");

			return null;
		}

		int idx = string.indexOf(':');

		if(idx <= 0 || idx == string.length() - 1)
			throw new IllegalArgumentException(name + " must be in '[type]:[path]' format.");

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
					throw new IllegalArgumentException("expected " + name + " to be an absolute path");
				break;
			default:
				throw new IllegalArgumentException(name + " type must be 'rel' (relative to wd), 'pth' (relative to webapp), or 'abs' (absolute).");
		}

		Path fileRoot = Paths.get(path);

		if(!Files.exists(fileRoot) || !Files.isDirectory(fileRoot))
			throw new IllegalArgumentException(name + " not found: " + fileRoot);

		return fileRoot;
	}

	public static final String QUALIKEY = LuaTela.class.getName();
}
