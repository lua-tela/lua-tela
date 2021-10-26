package com.hk.luatela.installer;

import com.hk.str.HTMLText;

import java.util.LinkedList;
import java.util.Map;

import static com.hk.luatela.installer.Installer.splitToLinesByLen;

public class PrintOutCommand extends Installer.Command
{
	void execute(LinkedList<String> arguments)
	{
		boolean pProps = Installer.getFlag(arguments, "-props");
		boolean pEnv = Installer.getFlag(arguments, "-env");

		if(!arguments.isEmpty())
			System.err.println("\nUnexpected command line parameter(s): " + arguments + "\n");

		if(pProps || !pEnv)
		{
			System.out.println();
			System.out.println("[==[ Printing Properties ]==]");
			System.out.println();

			System.getProperties().list(System.out);
		}

		if(pEnv || !pProps)
		{
			System.out.println();
			System.out.println("[==[ Printing Environment ]==]");
			System.out.println();

			Map<String, String> envs = System.getenv();
			for (Map.Entry<String, String> env : envs.entrySet())
				System.out.println(env.getKey() + "=" + env.getValue());
		}

		System.out.println();
	}

	void help()
	{
		HTMLText txt = new HTMLText();

		txt.prln("This command prints various environment");
		txt.prln("and system properties info for debugging.").ln();

		txt.prln("Parameters:").tabUp();
		String str;

		txt.prln("-env").tabUp();
		str = "This flag will enable it so that the environment " +
				"properties are printed. If 'props' flag isn't present " +
				"then system properties will not be printed.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.prln("-props").tabUp();
		str = "This flag will enable it so that the JVM system " +
				"properties are printed. If 'env' flag isn't present " +
				"then environment properties will not be printed.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		txt.tabDown();
		System.out.println(txt.create());
	}
}