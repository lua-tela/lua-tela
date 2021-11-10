package com.hk.luatela.installer;

import com.hk.str.HTMLText;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

import static com.hk.luatela.installer.Installer.splitToLinesByLen;

public class MakeScriptCommand extends Installer.Command
{
	private final boolean unix;

	MakeScriptCommand(boolean unix)
	{
		this.unix = unix;
	}

	void execute(LinkedList<String> arguments)
	{
		String dir = Installer.getParam(arguments, "--directory");

		Path script;

		if(dir != null)
			script = Paths.get(dir);
		else
			script = Paths.get(System.getProperty("user.dir"));

		String fileName = Installer.getParam(arguments, "--filename");

		if(fileName == null)
			fileName = "luatela";

		String dataroot = Installer.getParam(arguments, "--dataroot");

		HTMLText txt = new HTMLText();
		if(unix)
		{
			if(!Installer.getFlag(arguments, "-no-extension"))
				fileName += ".sh";

			String target = Installer.getParam(arguments, "--target");

			exportBash(txt, dataroot, target);
		}
		else
		{
			if(!Installer.getFlag(arguments, "-no-extension"))
				fileName += ".bat";

			exportBatch(txt, dataroot);
		}
		script = script.resolve(fileName);

		boolean overwrite = Installer.getFlag(arguments, "-overwrite");

		checkArgs(arguments);

		try
		{
			boolean cont = true;

			if(Files.exists(script))
			{
				if (!overwrite)
				{
					cont = false;
					System.err.println("\nFile already exists!\n");
				}
				else
					Files.delete(script);
			}

			if(cont)
			{
				Files.write(script, txt.create().getBytes());
				script.toFile().setExecutable(true);

				System.out.println("Successfully wrote script file!\n");
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void exportBatch(HTMLText txt, String dataroot)
	{
		txt.prln("@echo off").ln();

		txt.pr("java -jar ");

		if(dataroot != null)
		{
			dataroot = dataroot.replace("\"", "\\\"");
			txt.wr("-D")
					.wr(Installer.BASE_PROPERTY)
					.wr("=\"")
					.wr(dataroot)
					.wr("\" ");
		}

		txt.wrln("luatela.jar %*");
	}

	private void exportBash(HTMLText txt, String dataroot, String target)
	{
		if(target == null)
			target = "/usr/bin/env bash";

		txt.prln("#!" + target).ln();

		txt.prln("java -jar luatela.jar \"$@\"");
	}

	void help()
	{
		HTMLText txt = new HTMLText();

		txt.prln("This command creates an OS dependant executable");
		txt.prln("that can be used instead of the 'java -jar' command.");

		txt.pr("This command is used to create a ")
				.wr(unix ? "bash" : "batch")
				.wrln(" script.").ln();

		txt.prln("Parameters:").tabUp();
		String str;

		if(unix)
		{
			txt.prln("--target").tabUp();
			str = "This parameter will set the bash header location. " +
					"This specifies the text after the '#!' on the " +
					"first line. By default it is '/usr/bin/env bash'.";
			for(String line : splitToLinesByLen(str, 50))
				txt.prln(line);
			txt.tabDown();
		}
		txt.prln("--filename").tabUp();
		str = "This parameter will specify the name of the script " +
				"when writing to the file. By default, the file name " +
				"is 'luatela' with the extension being OS dependant.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("--directory").tabUp();
		str = "This parameter will specify the directory where the " +
				"script will be generated. It should be the current " +
				"directory since this is where the 'jar' file would " +
				"be.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("--dataroot").tabUp();
		str = "This parameter will specify the dataroot directory " +
				"which is used in various places in Lua Tela. It " +
				"can be set as a system property within the script " +
				"for easier use.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("-no-extension").tabUp();
		str = "This flag will signal to not include the extension " +
				"for the file during writing. The extension is '" +
				(unix ? "sh" : "bat") + "'.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("-overwrite").tabUp();
		str = "This flag will signal to overwrite the file at this " +
				"location if it already exists.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.tabDown();
		System.out.println(txt.create());
	}
}