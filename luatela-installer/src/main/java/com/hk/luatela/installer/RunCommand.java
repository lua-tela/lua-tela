package com.hk.luatela.installer;

import com.hk.file.FileUtil;
import com.hk.io.IOUtil;
import com.hk.str.HTMLText;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.hk.luatela.installer.Installer.splitToLinesByLen;

class RunCommand
{
	private final Scanner in;

	RunCommand(Scanner in, LinkedList<String> arguments)
	{
		this.in = in;
		Path tempDir = null;
		try
		{
			tempDir = Files.createTempDirectory("luatela");

			InputStream stream = RunCommand.class.getResourceAsStream("/luatela.war");
			if(stream == null)
				throw new NullPointerException();

			ZipInputStream jarStream = new ZipInputStream(stream);

			ZipEntry e;

			while((e = jarStream.getNextEntry()) != null)
			{
				Path newPath = tempDir.resolve(e.getName());

				if(!e.isDirectory())
				{
					Files.createDirectories(newPath.getParent());
					OutputStream newStream = Files.newOutputStream(newPath);
					IOUtil.copyTo(jarStream, newStream, 8192);
					newStream.close();
				}
				else
					Files.createDirectories(newPath);

				jarStream.closeEntry();
			}

			jarStream.close();

			Path webXml = tempDir.resolve("WEB-INF/web.xml");
			Path tempXml = tempDir.resolve("WEB-INF/temp.xml");

			Pattern pattern = Pattern.compile(".*<param-name>([a-zA-Z_]+)</param-name>.*\\{\\{}}.*");
			BufferedReader reader = Files.newBufferedReader(webXml);
			BufferedWriter writer = Files.newBufferedWriter(tempXml);
			String line;
			while((line = reader.readLine()) != null)
			{
				Matcher matcher = pattern.matcher(line);
				if(matcher.matches())
				{
					String param = Installer.getParam(arguments, "--" + matcher.group(1));

					if(param == null)
						line = "";
					else
						line = line.replace("{{}}", param);
				}

				if(!line.trim().isEmpty())
					writer.append(line).append('\n');
			}
			reader.close();
			writer.close();

			Files.delete(webXml);
			Files.move(tempXml, webXml);

			Server server = new Server();

			ServerConnector connector = new ServerConnector(server);
			connector.setPort(8080);
			server.setConnectors(new Connector[] { connector });

			WebAppContext context = new WebAppContext(tempDir.toString(), "/");

			server.setHandler(context);

			server.start();

			while(true)
			{
				line = in.nextLine();

				if("stop".equalsIgnoreCase(line))
					break;

				System.out.println("Type 'stop' to stop the server");
			}

			server.stop();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			if(tempDir != null)
				FileUtil.deleteDirectory(tempDir.toFile());
		}
	}

	static void help()
	{
		HTMLText txt = new HTMLText();

		txt.prln("This command can run Lua Tela on");
		txt.prln("top of an Eclipse Jetty server.").ln();

		txt.prln("Parameters:").tabUp();

		String str;

		str = "This parameter should point to the 'dataroot' " +
				"directory, which contains the route and model info.\n" +
				"By default, if not provided, it will point to the " +
				"current working directory. This folder important and " +
				"required for Lua Tela to properly initialize.";
		txt.prln("--dataroot [data root directory]").tabUp();
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		str = "This parameter should point to the 'resourceroot' " +
				"directory. This is the default directory in which " +
				"Lua Tela will search to serve static resource files, " +
				"such as css, js, or media files.\n" +
				"By default, if not provided, this folder will be " +
				"initialized using the 'dataroot' and 'resourcepath' " +
				"parameter. It will be initialized as: " +
				"'dataroot/resourcepath'";
		txt.prln("--resourceroot [resource root directory]").tabUp();
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		str = "This parameter will be the URL path prefix that should " +
				"indicate when static files should be served.\n" +
				"By default, if not provided, this will default to " +
				"'res', meaning if someone was to browse " +
				"'http://yoursite/res', Lua Tela will look for the " +
				"resourceroot directory and find the file relative to " +
				"the path and serve it with the respective content type.";
		txt.prln("--resourcepath [web app resource path]").tabUp();
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		str = "This parameter indicates the database user to login " +
				"with when creating a connection pool for web requests. " +
				"The default user for MySQL server is 'root' but that " +
				"is not preferred as it has access to various databases. " +
				"You should create a separate 'luatela' user to access " +
				"databases and tables with Lua Tela.";
		txt.prln("--datauser [user for MySQL database]").tabUp();
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		str = "This parameter is the password for the respective " +
				"'datauser' when establishing a connection pool for " +
				"requests.";
		txt.prln("--datapass [password for respective MySQL user]").tabUp();
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		str = "This parameter is the JDBC URL format for establishing " +
				"a connection for a MySQL database. This parameter " +
				"usually looks something like so...\n" +
				"jdbc:mysql://mysql.db.server:3306/my_database?useSSL=false&serverTimezone=UTC\n" +
				"jdbc:mysql://[host-name]:[port]/[database]?[extra-parameters]";
		txt.prln("--dataurl [JDBC URL format for MySQL database]").tabUp();
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.tabDown();
		System.out.println(txt.create());
	}
}
