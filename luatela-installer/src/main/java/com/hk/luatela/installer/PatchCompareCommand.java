package com.hk.luatela.installer;

import com.hk.luatela.patch.LuaBase;
import com.hk.str.HTMLText;

import java.util.LinkedList;
import java.util.Map;

public class PatchCompareCommand extends Installer.Command
{
	void execute(LinkedList<String> arguments)
	{
		System.out.println();
		System.out.println();
		System.out.println("Printing properties...");

		System.getProperties().list(System.out);

		System.out.println();
		System.out.println();
		System.out.println("Printing Environment...");

		Map<String, String> envs = System.getenv();
		for (Map.Entry<String, String> env : envs.entrySet())
			System.out.println(env.getKey() + "=" + env.getValue());

		System.out.println();
		System.out.println();
	}

	void help()
	{
		HTMLText txt = new HTMLText();

		txt.prln("This command compares previous models");
		txt.prln("patches with the current models.").ln();

		txt.prln("Parameters:").tabUp();

		txt.tabDown();
		System.out.println(txt.create());
	}
}
