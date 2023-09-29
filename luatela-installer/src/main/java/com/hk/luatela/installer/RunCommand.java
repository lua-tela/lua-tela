package com.hk.luatela.installer;

import com.hk.file.FileUtil;
import com.hk.io.IOUtil;
import com.hk.math.StorageUtils;
import com.hk.str.HTMLText;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.SecuredRedirectHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.hk.luatela.installer.Installer.splitToLinesByLen;

class RunCommand extends Installer.Command
{
	private Server server;
	private Path tempDir;

	void execute(LinkedList<String> arguments)
	{
		String dataroot = Installer.getParam(arguments, "--dataroot");

		arguments.addLast("--dataroot");
		arguments.addLast(dataroot == null ? Installer.getBase("rel:base") : dataroot);

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

			Path lib = tempDir.resolve("WEB-INF/lib");
			Files.list(lib)
				.filter(RunCommand::shouldExclude)
				.forEach(path -> {
					try
					{
						Files.move(path, Paths.get(path + ".tmp"));
					}
					catch (IOException ex)
					{
						throw new UncheckedIOException(ex);
					}
				});

			int httpPort = 8080;
			int httpsPort = 8443;
			boolean sslEnabled = false;

			String httpPortParam = Installer.getParam(arguments, "--httpPort");
			if(httpPortParam != null)
				httpPort = Integer.parseInt(httpPortParam);

			Resource keystoreResource = null;
			String keystorePath = Installer.getParam(arguments, "--keystore");
			String keystorePass = null, keyManagerPass = null;
			if(keystorePath != null)
			{
				String httpsPortParam = Installer.getParam(arguments, "--httpsPort");
				if(httpsPortParam != null)
					httpsPort = Integer.parseInt(httpsPortParam);

				keystoreResource = Resource.newResource(keystorePath);

				keystorePass = Installer.getParam(arguments, "--keystorePass");
				if(keystorePass == null)
					throw new IllegalStateException("Expected 'keystorePass' param alongside keystore");

				keyManagerPass = Installer.getParam(arguments, "--keyManagerPass");

				if(keyManagerPass == null)
					keyManagerPass = keystorePass;

				sslEnabled = true;
			}

			checkArgs(arguments);

			this.server = new Server();

			server.setRequestLog((request, response) -> {
				System.out.print('[');
				System.out.print(Installer.compact.format(request.getTimeStamp()));
				System.out.print("] [");
				System.out.print(response.getStatus());

				long size = response.getCommittedMetaData().getContentLength();
				if(size >= 0)
				{
					System.out.print("] [");
					double amnt = (double) size;
					String unit = null;

					if(size >= StorageUtils.GIGABYTE)
					{
						amnt /= StorageUtils.GIGABYTE;
						unit = "gb";
					}
					else if(size >= StorageUtils.MEGABYTE)
					{
						amnt /= StorageUtils.MEGABYTE;
						unit = "mb";
					}
					else if(size >= StorageUtils.KILOBYTE)
					{
						amnt /= StorageUtils.KILOBYTE;
						unit = "kb";
					}

					if (unit != null)
					{
						if(amnt == (double)(long) amnt)
							System.out.print((int) amnt);
						else
							System.out.print((int) (amnt * 100) / 100D);
						System.out.print(unit);
					}
					else
						System.out.print(size);
				}

				System.out.print("] ");
				System.out.print(request.getRemoteAddr());
				System.out.print(" - ");
				System.out.print(request.getRequestURL());

				System.out.println();
			});

			if(sslEnabled)
			{
				HttpConfiguration https = new HttpConfiguration();
				https.setSecureScheme("https");
				https.setSecurePort(httpsPort);
				https.addCustomizer(new SecureRequestCustomizer());

				ServerConnector httpConnector = new ServerConnector(server,
						new HttpConnectionFactory(https));
				httpConnector.setPort(httpPort);

				SslContextFactory sslContextFactory = new SslContextFactory.Server.Server();
				sslContextFactory.setKeyStoreResource(keystoreResource);
				sslContextFactory.setKeyStorePassword(keystorePass);
				sslContextFactory.setKeyManagerPassword(keyManagerPass);

				ServerConnector httpsConnector = new ServerConnector(server,
						new SslConnectionFactory(sslContextFactory, "http/1.1"),
						new HttpConnectionFactory(https));
				httpsConnector.setPort(httpsPort);

				server.setConnectors(new Connector[] { httpConnector, httpsConnector });
			}
			else
			{
				ServerConnector httpConnector = new ServerConnector(server);
				httpConnector.setPort(httpPort);

				server.setConnectors(new Connector[] { httpConnector });
			}

			WebAppContext context = new WebAppContext(tempDir.toString(), "/");

			if(sslEnabled)
			{
				HandlerList list = new HandlerList();
				list.addHandler(new SecuredRedirectHandler());
				list.addHandler(context);

				server.setHandler(list);
			}
			else
				server.setHandler(context);

			server.start();

			Thread.sleep(1000);

			System.out.println("Enter 'stop' to stop the server");
			do
			{
				line = Installer.nextLine();
			} while (!"stop".equalsIgnoreCase(line));
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	private static boolean shouldExclude(Path path)
	{
		String name = path.getFileName().toString();
		return name.matches("luatela-core.*\\.jar") ||
				name.matches("hklib.*\\.jar") ||
				name.matches("hkodb.*\\.jar") ||
				name.matches("mysql-connector-java.*\\.jar");
	}

	@Override
	void close()
	{
		if(server != null && server.isRunning())
		{
			try
			{
				server.stop();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		if(tempDir != null)
			FileUtil.deleteDirectory(tempDir.toFile());
	}

	void help()
	{
		HTMLText txt = new HTMLText();

		txt.prln("This command can run Lua Tela on");
		txt.prln("top of an Eclipse Jetty server.").ln();

		txt.prln("Parameters:").tabUp();

		String str;

		txt.prln("--dataroot [data root directory]").tabUp();
		str = "This parameter should point to the 'dataroot' " +
				"directory, which contains the route and model info.\n" +
				"By default, if not provided, it will point to the 'base' " +
				"folder in the current working directory. " +
				"This folder is important and required for Lua " +
				"Tela to properly initialize.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("--resourceroot [resource root directory]").tabUp();
		str = "This parameter should point to the 'resourceroot' " +
				"directory. This is the default directory in which " +
				"Lua Tela will search to serve static resource files, " +
				"such as css, js, or media files.\n" +
				"By default, if not provided, this folder will be " +
				"initialized using the 'dataroot' and 'resourcepath' " +
				"parameter. It will be initialized as: " +
				"'dataroot/resourcepath'";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("--resourcepath [web app resource path]").tabUp();
		str = "This parameter will be the URL path prefix that should " +
				"indicate when static files should be served.\n" +
				"By default, if not provided, this will default to " +
				"'res', meaning if someone was to browse " +
				"'http://yoursite/res', Lua Tela will look for the " +
				"resourceroot directory and find the file relative to " +
				"the path and serve it with the respective content type.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("--httpPort [0-65535 port number]").tabUp();
		str = "This parameter is the port that the HTTP protocol should " +
				"listen on. By default, this is set to listen to " +
				"requests coming in on port 8080.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("--httpsPort [0-65535 port number]").tabUp();
		str = "This parameter is the port that the HTTPS protocol should " +
				"listen on. This will only be used if the 'keystore' " +
				"parameter is enabled with a valid keystore resource.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("--keystore [file path or URL to keystore]").tabUp();
		str = "This parameter should point to a valid JKS file which. " +
				"will enable the HTTPS port and also redirect HTTP " +
				"requests to the HTTPS port. Alongside this parameter " +
				"are also the 'keystorePass' and 'keyManagerPass' " +
				"parameters which should also be specified.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("--keystorePass [password for JKS]").tabUp();
		str = "This parameter will be used to authenticate the " +
				"Java KeyStore. This parameter is required alongside " +
				"'keystore' and will only be used if that is present.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("--keyManagerPass [password for JKS key manager]").tabUp();
		str = "This parameter should be the key manager password for " +
				"the specified Java KeyStore using the 'keystore' " +
				"parameter.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("--datauser [user for MySQL database]").tabUp();
		str = "This parameter indicates the database user to login " +
				"with when creating a connection pool for web requests. " +
				"The default user for MySQL server is 'root' but that " +
				"is not preferred as it has access to various databases. " +
				"You should create a separate 'luatela' user to access " +
				"databases and tables with Lua Tela.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("--datapass [password for respective MySQL user]").tabUp();
		str = "This parameter is the password for the respective " +
				"'datauser' when establishing a connection pool for " +
				"requests.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("--dataurl [JDBC URL format for MySQL database]").tabUp();
		str = "This parameter is the JDBC URL format for establishing " +
				"a connection for a MySQL database. This parameter " +
				"usually looks something like so...\n" +
				"jdbc:mysql://mysql.db.server:3306/my_database?useSSL=false&serverTimezone=UTC\n" +
				"jdbc:mysql://[host-name]:[port]/[database]?[extra-parameters]";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.tabDown();
		System.out.println(txt.create());
	}
}
