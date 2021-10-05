package com.hk.luatela.database;

import com.hk.func.BiConsumer;
import com.hk.lua.*;

public enum ModelLibrary implements BiConsumer<Environment, LuaObject>, Lua.LuaMethod
{
	model() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Lua.checkArgs(name(), args, LuaType.TABLE);

			return Lua.nil();
		}
	},
	field() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Lua.checkArgs(name(), args, LuaType.TABLE);

			return Lua.nil();
		}
	};

	@Override
	public void accept(Environment env, LuaObject table)
	{
		String name = toString();
		if(name != null && !name.trim().isEmpty())
			table.setIndex(env.interp, name, Lua.newFunc(this));
	}
}
