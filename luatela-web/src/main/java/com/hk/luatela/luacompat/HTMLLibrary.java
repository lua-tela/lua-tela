package com.hk.luatela.luacompat;

import com.hk.func.BiConsumer;
import com.hk.lua.*;
import com.hk.str.HTMLText;

import java.util.*;

@SuppressWarnings("unused")
public enum HTMLLibrary implements BiConsumer<Environment, LuaObject>, Lua.LuaMethod
{
	create() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			return new HTMLUserdata(interp, new HTMLText());
		}
	};

	@Override
	public LuaObject call(LuaInterpreter interp, LuaObject[] args)
	{
		throw new Error();
	}

	@Override
	public void accept(Environment env, LuaObject table)
	{
		String name = toString();
		if(name != null && !name.trim().isEmpty())
			table.setIndex(env.interp, name, Lua.newFunc(this));
	}

	private static String[] getAttrs(LuaInterpreter interp, LuaObject[] args, int indx)
	{
		if(indx == args.length - 1 && args[indx].isTable())
		{
			Set<Map.Entry<LuaObject, LuaObject>> set = args[indx].getEntries();
			List<String> lst = new ArrayList<>(set.size() * 2);

			for(Map.Entry<LuaObject, LuaObject> ent : set)
			{
				lst.add(ent.getKey().getString());

				if(ent.getValue().isTable())
				{
					long end = ent.getValue().getLength();

					StringBuilder sb = new StringBuilder();
					for(long i = 1; i <= end; i++)
					{
						sb.append(ent.getValue().getIndex(interp, i));

						if(i < end)
							sb.append(' ');
					}
					lst.add(sb.toString());
				}
				else if(ent.getValue().getBoolean())
					lst.add(ent.getValue().getString());
				else
					lst.add(null);
			}

			return lst.toArray(new String[0]);
		}
		else
		{
			String[] attrs = new String[Math.max(args.length - indx, 0)];
			for(int i = indx; i < args.length; i++)
			{
				if(args[i].isTable())
				{
					long end = args[i].getLength();

					StringBuilder sb = new StringBuilder();
					for(long j = 1; j <= end; j++)
					{
						sb.append(args[i].getIndex(interp, j));

						if(j < end)
							sb.append(' ');
					}
					attrs[i - indx] = sb.toString();
				}
				else if(args[i].getBoolean())
					attrs[i - indx] = args[i].getString();
				else
					attrs[i - indx] = null;
			}

			return attrs;
		}
	}

	private static final LuaObject htmlMetatable;

	static
	{

		htmlMetatable = Lua.newTable();
		htmlMetatable.rawSet("__name", Lua.newString("HTMLText*"));
		htmlMetatable.rawSet("__index", htmlMetatable);
		htmlMetatable.rawSet("blockWS", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("blockWS", args, LuaType.USERDATA, LuaType.BOOLEAN);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).blockWS(args[1].getBoolean());
			else
				throw new LuaException("bad argument #1 to 'blockWS' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("br", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("br", args, LuaType.USERDATA);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).br();
			else
				throw new LuaException("bad argument #1 to 'br' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("close", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("close", args, LuaType.USERDATA, LuaType.STRING);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).close(args[1].getString());
			else
				throw new LuaException("bad argument #1 to 'close' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("closeBrace", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("closeBrace", args, LuaType.USERDATA);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).closeBrace();
			else
				throw new LuaException("bad argument #1 to 'closeBrace' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("getVar", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("getVar", args, LuaType.USERDATA, LuaType.STRING);
			if(args[0] instanceof HTMLUserdata)
				return Lua.newString(args[0].getUserdata(HTMLText.class).getVar(args[1].getString()));
			else
				throw new LuaException("bad argument #1 to 'getVar' (HTMLText* expected, got " + args[0].name() + ")");
		}));
		htmlMetatable.rawSet("getVars", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("getVars", args, LuaType.USERDATA);
			if(args[0] instanceof HTMLUserdata)
				return Lua.newLuaObject(Arrays.asList(args[0].getUserdata(HTMLText.class).getVars()));
			else
				throw new LuaException("bad argument #1 to 'getVars' (HTMLText* expected, got " + args[0].name() + ")");
		}));
		htmlMetatable.rawSet("hasVar", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("hasVar", args, LuaType.USERDATA, LuaType.STRING);
			if(args[0] instanceof HTMLUserdata)
				return Lua.newBool(args[0].getUserdata(HTMLText.class).hasVar(args[1].getString()));
			else
				throw new LuaException("bad argument #1 to 'hasVar' (HTMLText* expected, got " + args[0].name() + ")");
		}));
		htmlMetatable.rawSet("isBlocking", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("isBlocking", args, LuaType.USERDATA, LuaType.STRING);
			if(args[0] instanceof HTMLUserdata)
				return Lua.newBool(args[0].getUserdata(HTMLText.class).isBlocking());
			else
				throw new LuaException("bad argument #1 to 'isBlocking' (HTMLText* expected, got " + args[0].name() + ")");
		}));
		htmlMetatable.rawSet("ln", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("ln", args, LuaType.USERDATA);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).ln();
			else
				throw new LuaException("bad argument #1 to 'ln' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("makeVar", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("makeVar", args, LuaType.USERDATA, LuaType.STRING);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).makeVar(args[1].getString(), args.length > 2 ? args[2].getString() : null);
			else
				throw new LuaException("bad argument #1 to 'makeVar' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("el", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("el", args, LuaType.USERDATA, LuaType.STRING);
			if(args[0] instanceof HTMLUserdata)
			{
				HTMLText txt2 = args[0].getUserdata(HTMLText.class);
				String[] attrs = getAttrs(interp, args, 3);
				txt2.el(args[1].getString(), args.length > 2 && args[2].getBoolean() ? args[2].getString() : null, attrs);
			}
			else
				throw new LuaException("bad argument #1 to 'el' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("open", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("open", args, LuaType.USERDATA, LuaType.STRING);
			if(args[0] instanceof HTMLUserdata)
			{
				HTMLText txt2 = args[0].getUserdata(HTMLText.class);
				String[] attrs = getAttrs(interp, args, 2);
				txt2.open(args[1].getString(), attrs);
			}
			else
				throw new LuaException("bad argument #1 to 'open' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("openBrace", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("openBrace", args, LuaType.USERDATA);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).openBrace();
			else
				throw new LuaException("bad argument #1 to 'openBrace' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("pr", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("pr", args, LuaType.USERDATA, LuaType.STRING);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).pr(args[1].getString());
			else
				throw new LuaException("bad argument #1 to 'pr' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("prln", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("prln", args, LuaType.USERDATA, LuaType.STRING);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).prln(args[1].getString());
			else
				throw new LuaException("bad argument #1 to 'prln' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("setVar", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("setVar", args, LuaType.USERDATA, LuaType.STRING, LuaType.STRING);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).setVar(args[1].getString(), args[2].getString());
			else
				throw new LuaException("bad argument #1 to 'setVar' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("tabDown", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("tabDown", args, LuaType.USERDATA);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).tabDown();
			else
				throw new LuaException("bad argument #1 to 'tabDown' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("tabUp", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("tabUp", args, LuaType.USERDATA);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).tabUp();
			else
				throw new LuaException("bad argument #1 to 'tabUp' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("tabs", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("tabs", args, LuaType.USERDATA);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).tabs();
			else
				throw new LuaException("bad argument #1 to 'tabs' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("wr", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("wr", args, LuaType.USERDATA, LuaType.STRING);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).wr(args[1].getString());
			else
				throw new LuaException("bad argument #1 to 'wr' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
		htmlMetatable.rawSet("wrln", Lua.newFunc((LuaInterpreter interp, LuaObject[] args) -> {
			Lua.checkArgs("wrln", args, LuaType.USERDATA, LuaType.STRING);
			if(args[0] instanceof HTMLUserdata)
				args[0].getUserdata(HTMLText.class).wrln(args[1].getString());
			else
				throw new LuaException("bad argument #1 to 'wrln' (HTMLText* expected, got " + args[0].name() + ")");

			return args[0];
		}));
	}

	private static class HTMLUserdata extends LuaUserdata
	{
		private final HTMLText txt;

		public HTMLUserdata(LuaInterpreter interp2, HTMLText txt)
		{
			this.txt = txt;
			this.metatable = htmlMetatable;
		}

		@Override
		public String name()
		{
			return "HTMLText*";
		}

		@Override
		public Object getUserdata()
		{
			return txt;
		}

		@Override
		public String getString(LuaInterpreter interp)
		{
			return txt.create();
		}
	}
}
