package com.hk.luatela.patch.models;

import com.hk.lua.*;
import com.hk.luatela.patch.LuaBase;

import java.util.Objects;
import java.util.function.BiConsumer;

public enum DatabaseLibrary implements BiConsumer<Environment, LuaObject>, Lua.LuaMethod
{
	model() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Lua.checkArgs(name(), args, LuaType.STRING);

			LuaBase db = interp.getExtra(LuaBase.KEY, LuaBase.class);
			Objects.requireNonNull(db);
			return db.getModelSet().getModel(args[0].getString());
		}
	};

	@Override
	public void accept(Environment env, LuaObject table)
	{
		String name = toString();
		if(name != null && !name.trim().isEmpty())
			table.setIndex(env.interp, name, Lua.newMethod(this));
	}
}