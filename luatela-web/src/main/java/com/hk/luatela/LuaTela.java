package com.hk.luatela;

import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaLibrary;
import com.hk.luatela.luacompat.ContextLibrary;
import com.hk.luatela.patch.DatabaseException;
import com.hk.luatela.patch.LuaBase;
import com.hk.luatela.routes.Routes;
import com.hk.luatela.runner.Runner;
import com.hk.luatela.servlet.ResourceServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LuaTela
{
	public final LuaBase base;
	public final ServletContext context;
	public final String resourcePath;
	public final Path dataroot, resourceRoot;
	public final Routes routes;
	public final Runner runner;

	public LuaTela(ServletContext context)
	{
		if(context.getAttribute(QUALIKEY) != null)
			throw new InitializationException("Lua-Tela Servlet already initialized");

		this.context = context;

		dataroot = getFile(context, "dataroot", true);

		String resourcePath = context.getInitParameter("resourcepath");

		if(resourcePath != null)
		{
			if (resourcePath.startsWith("/"))
				resourcePath = resourcePath.substring(1);
			if (resourcePath.endsWith("/"))
				resourcePath = resourcePath.substring(0, resourcePath.length() - 1);

			if (!resourcePath.matches("[a-zA-Z0-9/\\-.]+"))
				throw new InitializationException("resourcepath should only contain letters (a-z), numbers (0-9), slashes (/), dashes (-), and periods (.)");
		}
		else
			resourcePath = "res";

		this.resourcePath = resourcePath;

		Path resourceRoot = getFile(context, "resourceroot", false);

		if(resourceRoot == null)
			resourceRoot = dataroot.resolve(resourcePath);

		this.resourceRoot = resourceRoot;

		ServletRegistration.Dynamic registration = context.addServlet(
				ResourceServlet.class.getName(), ResourceServlet.class);
		registration.setLoadOnStartup(1);
		registration.addMapping("/" + resourcePath + "/*");

		routes = new Routes(this::injectInto, dataroot.resolve("routes.lua"));
		runner = new Runner(this::injectInto, dataroot.resolve("init.lua"));

		context.setAttribute(QUALIKEY, this);

		try
		{
			base = new LuaBase(dataroot.toFile());
		}
		catch (FileNotFoundException e)
		{
			throw new UncheckedIOException(e);
		}
	}

	public void initialize(PrintStream out)
	{
		routes.collect(out);

		int patches;
		try
		{
			patches = base.loadPatches();
		}
		catch (DatabaseException e)
		{
			throw new InitializationException(e);
		}

		out.println("Loaded " + patches + " patch" + (patches == 1 ? "." : "es."));

		try
		{
			base.checkNew();
		}
		catch (FileNotFoundException e)
		{
			throw new InitializationException("'models.lua' not found in dataroot directory: " + dataroot, e);
		}
		catch (DatabaseException e)
		{
			throw new InitializationException(e);
		}

		runner.collect(out);

		out.println("Using Data-Root: \"" + dataroot + "\"");
		out.println("Using Resource-Root: \"" + resourceRoot + "\"");
		out.println("Using Resource-Path: \"" + resourcePath + "\"");
		out.print("Loaded " + routes.size());
		out.println(" route" + (routes.size() == 1 ? "" : "s") + " roots");
		int tasks = runner.getTasks();
		if(tasks > 0)
		{
			out.print("Loaded " + tasks + " task");
			out.println(tasks == 1 ? "" : "s");
		}
	}

	public void shutdown()
	{
		runner.close();
	}

	public void injectInto(LuaInterpreter interp)
	{
		LuaBase.injectRequire(interp, dataroot);
		interp.setExtra(QUALIKEY, this);

		interp.importLib(new LuaLibrary<>("context", ContextLibrary.class));

//		Environment globals = interp.getGlobals();
//
//		globals.setVar("dataroot", Lua.newFunc((interp1, args) -> Lua.newString(dataRoot.toString())));
//		globals.setVar("resourceroot", Lua.newFunc((interp1, args) -> Lua.newString(resourceRoot.toString())));
	}

	private Path getFile(ServletContext context, String name, boolean required)
	{
		Path result = null;
		String string = context.getInitParameter(name);

		if (string == null)
		{
			if (required)
				throw new InitializationException(name + " must be included in web.xml");
		}
		else
		{
			try
			{
				Path fullPath = Paths.get(string);
				if (Files.exists(fullPath))
					return fullPath.toAbsolutePath();
			}
			catch (InvalidPathException ignored)
			{}

			int idx = string.indexOf(':');
			if (idx <= 0 || idx == string.length() - 1)
				throw new InitializationException(name + " must be in '[type]:[path]' format.");
			String path = string.substring(idx + 1);
			switch (string.substring(0, idx))
			{
				case "pth":
					path = context.getRealPath(path.replace(File.separator, "/"));
					break;
				case "rel":
					path = System.getProperty("user.dir") + File.separator + path;
					break;
				case "abs":
					if (!Paths.get(path).isAbsolute())
						throw new InitializationException("expected " + name + " to be an absolute path");
					break;
				default:
					throw new InitializationException(name + " type must be 'rel' (relative to wd), 'pth' (relative to webapp), or 'abs' (absolute).");
			}
			Path fileRoot = Paths.get(path);
			if (!Files.exists(fileRoot) || !Files.isDirectory(fileRoot))
				throw new InitializationException(name + " not found: " + fileRoot);
			result = fileRoot;
		}

		return result;
	}

	public static String escapeHTML(String s)
	{
		StringBuilder out = new StringBuilder(Math.max(16, s.length()));
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&')
			{
				out.append("&#");
				out.append((int) c);
				out.append(';');
			}
			else
			{
				out.append(c);
			}
		}
		return out.toString();
	}

	public static final String QUALIKEY = LuaTela.class.getName();
}
