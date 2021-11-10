package com.hk.luatela.installer;

import com.hk.file.FileUtil;
import com.hk.str.HTMLText;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.hk.luatela.installer.Installer.splitToLinesByLen;

public class InitCommand extends Installer.Command
{
	void execute(LinkedList<String> arguments)
	{
		boolean removeFirst = Installer.getFlag(arguments, "-rf");
		boolean overwrite = Installer.getFlag(arguments, "-overwrite");
		boolean parents = Installer.getFlag(arguments, "-parents");
		String datarootParam = Installer.getParam(arguments, "--dataroot");

		if(datarootParam == null)
			datarootParam = Installer.getBase("base");

		checkArgs(arguments);

		Path dataroot = Paths.get(datarootParam).toAbsolutePath();

		try
		{
			if (Files.exists(dataroot) && !Files.isDirectory(dataroot))
				throw new IllegalStateException("dataroot parameter points to file not dataroot.");

			if (removeFirst && Files.exists(dataroot))
				FileUtil.deleteDirectory(datarootParam);

			if (!parents)
			{
				if(!Files.exists(dataroot.getParent()))
					throw new IllegalStateException("Cannot create init directory, missing parent. (use '-parents' flag)");

				if(!Files.exists(dataroot))
					Files.createDirectory(dataroot);
			}
			else
				Files.createDirectories(dataroot);

			Path pages = dataroot.resolve("pages");
			if(!Files.exists(pages))
				Files.createDirectory(pages);

			HTMLText txt;
			List<String> skipped = new LinkedList<>();
			Map<String, Consumer<HTMLText>> files = new HashMap<>();
			files.put("routes.lua", this::writeRoutes);
			files.put("models.lua", this::writeModels);
			files.put("pages/index.lua", this::writeIndexPage);

			for (Map.Entry<String, Consumer<HTMLText>> entry : files.entrySet())
			{
				Path file = dataroot.resolve(entry.getKey());
				if (!Files.exists(file) || overwrite)
				{
					txt = new HTMLText();
					entry.getValue().accept(txt);
					FileUtil.resetFile(file.toFile(), txt.create());
				}
				else
					skipped.add(entry.getKey());
			}

			if(!skipped.isEmpty())
			{
				for (String s : skipped)
					System.out.println("Skipped " + s + " as it already exists.");

				System.out.println("Use the -overwrite flag to overwite");
				System.out.println("the file(s) that already exist.");
			}
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}

	private void writeRoutes(HTMLText txt)
	{
		txt.wr("path('/').topage('index')");
	}

	private void writeModels(HTMLText txt)
	{
		txt.wr("print 'Loading models'");
	}

	private void writeIndexPage(HTMLText txt)
	{
		txt.wrln("response.setContentType('text/html')").ln().wr("return 'OK'");
	}

	void help()
	{
		HTMLText txt = new HTMLText();

		txt.prln("This command generates various beginner files to");
		txt.prln("correctly be able to run a version of Lua Tela.").ln();
		txt.prln("The files that this command generates are:").ln().tabUp();

		txt.prln("- routes.lua (with one simple route to index.lua)");
		txt.prln("- models.lua (with no models or patches)");
		txt.prln("- pages/index.lua (just return 'OK')");

		txt.tabDown().ln();

		txt.prln("Parameters:").tabUp();
		String str;

		txt.prln("--dataroot").tabUp();
		str = "This parameter will specify the directory where the " +
				"files will be generated. Also called the " +
				"'init directory'. If not specified, then it will " +
				"default to the 'base' folder in the current directory.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("-rf").tabUp();
		str = "This flag will signal to remove the contents of the " +
				"init directory. (Which can be specified using the " +
				"'--dataroot' parameter.)";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("-overwrite").tabUp();
		str = "This flag will signal to overwrite the files that are " +
				"to be written. If they already exist, they will be " +
				"overwritten if this flag is included.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("-parents").tabUp();
		str = "This flag will signal to create any parent directories " +
				"to lead to the init directory.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.tabDown();
		System.out.println(txt.create());
	}
}