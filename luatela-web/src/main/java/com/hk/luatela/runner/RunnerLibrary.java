package com.hk.luatela.runner;

import com.hk.lua.*;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public enum RunnerLibrary implements BiConsumer<Environment, LuaObject>, Lua.LuaMethod
{
	create() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Runner runner = interp.getExtra("runner", Runner.class);

			if(runner.wasCreated())
				throw new LuaException("Already created service!");

			runner.create();
			return null;
		}
	},
	time {
		@Override
		public void accept(Environment env, LuaObject table)
		{
			LuaObject tbl = Lua.newTable();

			TimeUnit[] values = TimeUnit.values();
			for (int i = 0; i < values.length; i++)
			{
				tbl.rawSet(i + 1L, values[i].name());
				tbl.rawSet(values[i].name(), values[i].name());
			}

			table.setIndex(env.interp, toString(), tbl);
		}
	},
	addSingle() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Lua.checkArgs(toString(), args, LuaType.FUNCTION, LuaType.INTEGER, LuaType.STRING);

			Runner runner = interp.getExtra("runner", Runner.class);

			String unit = args[2].getString().toUpperCase(Locale.ROOT);
			try
			{
				runner.scheduleSingle(args[0], args[1].getLong(), TimeUnit.valueOf(unit));
			}
			catch (IllegalArgumentException ex)
			{
				throw new LuaException("Unknown time unit: " + args[2]);
			}
			return null;
		}
	},
	add() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Lua.checkArgs(toString(), args, LuaType.FUNCTION, LuaType.INTEGER, LuaType.STRING);

			Runner runner = interp.getExtra("runner", Runner.class);

			String unit = args[2].getString().toUpperCase(Locale.ROOT);
			try
			{
				runner.schedule(args[0], args[1].getLong(), TimeUnit.valueOf(unit));
			}
			catch (IllegalArgumentException ex)
			{
				throw new LuaException("Unknown time unit: " + args[2]);
			}
			return null;
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
			table.setIndex(env.interp, name, Lua.newMethod(this));
	}
}
