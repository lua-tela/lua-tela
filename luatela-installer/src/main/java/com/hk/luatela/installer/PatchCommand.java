package com.hk.luatela.installer;

import com.hk.luatela.patch.DatabaseException;
import com.hk.luatela.patch.LuaBase;
import com.hk.luatela.patch.PatchComparison;
import com.hk.str.HTMLText;
import com.hk.util.KeyValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.Reference;
import java.util.LinkedList;

import static com.hk.luatela.installer.Installer.splitToLinesByLen;

public abstract class PatchCommand extends Installer.Command
{
	protected String dataroot;

	@Override
	void execute(LinkedList<String> arguments)
	{
		getParams(arguments);

		checkArgs(arguments);

		LuaBase base = getBase();

		loadPatches(base);

		PatchComparison comparison = getComparison(base);
		if(comparison == null)
			return;

		doCompare(comparison);

		handle(base, comparison);
	}

	void handle(LuaBase base, PatchComparison comparison)
	{}

	void getParams(LinkedList<String> arguments)
	{
		dataroot = Installer.getParam(arguments, "--dataroot");

		if(dataroot == null)
			dataroot =  Installer.getBase("base");
	}

	LuaBase getBase()
	{
		try
		{
			return new LuaBase(new File(dataroot).getAbsoluteFile());
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}
	}

	void loadPatches(LuaBase base)
	{
		try
		{
			double[] elapsed = new double[1];
			int patches = base.loadPatches(elapsed);
			System.out.print("Loaded " + patches + " patch" + (patches == 1 ? "" : "es"));
			System.out.println(" in " + ((int) (elapsed[0] * 100) / 100D) + "ms");
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
		}
	}

	PatchComparison getComparison(LuaBase base)
	{
		PatchComparison comparison;
		try
		{
			comparison = base.checkNew();
		}
		catch (FileNotFoundException e)
		{
			System.err.println("'models.lua' not found");
			System.err.println(e.getLocalizedMessage());
			return null;
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
			return null;
		}
		return comparison;
	}

	void doCompare(PatchComparison comparison)
	{
		PatchComparison.Decision decision;

		while((decision = comparison.attemptCompare()) != null)
		{
			throw new Error("TODO: get user input for next decision of models");
		}
	}

	void help(HTMLText txt)
	{
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
	}
}
