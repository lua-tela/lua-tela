package com.hk.luatela.installer;

import com.hk.luatela.patch.LuaBase;
import com.hk.luatela.patch.PatchComparison;
import com.hk.luatela.patch.PatchExport;
import com.hk.str.HTMLText;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;

import static com.hk.luatela.installer.Installer.splitToLinesByLen;

public class PatchUpCommand extends PatchCommand
{
	private boolean print;

	@Override
	void getParams(LinkedList<String> arguments)
	{
		super.getParams(arguments);

		print = Installer.getFlag(arguments, "-print");
	}

	@Override
	void handle(LuaBase base, PatchComparison comparison)
	{
		if(comparison.unchanged)
		{
			System.out.println("Model Set unchanged\n");
			return;
		}

		PatchExport export = comparison.export();

		HTMLText txt = export.toLua(new HTMLText());

		String code = txt.create();

		try
		{
			Path patches = base.dataroot.toPath().resolve(".patches");

			if (!Files.exists(patches))
				Files.createDirectories(patches);

			Path patch = patches.resolve(export.getName() + ".lua");
			System.out.println("Next Patch File: " + patch);

			if (print)
			{
				System.out.println("----------- CODE -----------");
				System.out.println(code);
			}
			else
			{
				Files.deleteIfExists(patch);
				Files.createFile(patch);
				Files.write(patch, Collections.singleton(code));
				System.out.println("Successfully wrote patch!");
			}
		}
		catch (IOException exception)
		{
			exception.printStackTrace();
		}

		System.out.println();
	}

	@Override
	void help()
	{
		HTMLText txt = new HTMLText();

		txt.prln("This command attempts to patch the current model");
		txt.prln("along with the patchy model set.").ln();

		txt.prln("Parameters:").tabUp();
		String str;

		txt.prln("-print").tabUp();
		str = "This flag will print the patch instead of attempting " +
				"to write it to the patch file.";
		for(String line : splitToLinesByLen(str, 50))
			txt.prln(line);
		txt.tabDown();

		help(txt);

		txt.tabDown();
		System.out.println(txt.create());
	}
}
