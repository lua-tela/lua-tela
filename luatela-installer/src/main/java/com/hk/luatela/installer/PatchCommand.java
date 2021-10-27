package com.hk.luatela.installer;

import com.hk.luatela.patch.DatabaseException;
import com.hk.luatela.patch.LuaBase;
import com.hk.luatela.patch.PatchComparison;
import com.hk.str.HTMLText;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.util.LinkedList;

import static com.hk.luatela.installer.Installer.splitToLinesByLen;

public abstract class PatchCommand extends Installer.Command
{
	@Override
	void execute(LinkedList<String> arguments)
	{
		LuaBase base = getBase(arguments);

		loadPatches(base);

		PatchComparison comparison = getComparison(base);
		if(comparison == null)
			return;

		doCompare(comparison);

		handle(base, comparison);
	}

	void handle(LuaBase base, PatchComparison comparison)
	{}

	LuaBase getBase(LinkedList<String> arguments)
	{
		LuaBase base;
		String dataroot = Installer.getParam(arguments, "--dataroot");

		if(dataroot == null)
			dataroot =  "base";

		try
		{
			base = new LuaBase(new File(dataroot));
		}
		catch (FileNotFoundException e)
		{
			throw new UncheckedIOException(e);
		}
		return base;
	}

	void loadPatches(LuaBase base)
	{
		int patches = base.loadPatches();
		System.out.println("Loaded " + patches + " patch" + (patches == 1 ? "." : "es."));
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
