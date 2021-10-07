package com.hk.luatela.patch.models;

import com.hk.func.BiConsumer;
import com.hk.lua.*;
import com.hk.luatela.patch.DatabaseException;
import com.hk.luatela.patch.models.fields.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
public enum ModelLibrary implements BiConsumer<Environment, LuaObject>, Lua.LuaMethod
{
	model() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Lua.checkArgs(name(), args, LuaType.STRING);

			ModelSet modelSet = interp.getExtra(ModelSet.KEY, ModelSet.class);

			try
			{
				Model model = new Model(modelSet, args[0].getString());

				return Lua.newFunc((interp1, args1) -> {
					Lua.checkArgs(name(), args1, LuaType.TABLE);
					try
					{
						model.readFields(args1[0]);
					}
					catch (DatabaseException e)
					{
						throw new LuaException(e.getLocalizedMessage());
					}
					return model;
				});
			}
			catch (DatabaseException e)
			{
				throw new LuaException(e.getLocalizedMessage());
			}
		}
	},
	field() {
		@Override
		public LuaObject call(LuaInterpreter interp, LuaObject[] args)
		{
			Lua.checkArgs(name(), args, LuaType.STRING);
			LuaObject builder = fields.get(args[0].getString());

			if(builder == null)
				throw new LuaException("Unknown data field type '" + args[0].getString() + "'");
			return builder;
		}
	};

	@Override
	public void accept(Environment env, LuaObject table)
	{
		String name = toString();
		if(name != null && !name.trim().isEmpty())
			table.setIndex(env.interp, name, Lua.newFunc(this));
	}

	private static final Map<String, LuaObject> fields = new HashMap<>();

	private static LuaObject builder(String name, BiFunction<Model, String, DataField> provider)
	{
		DataField.Builder builder = new DataField.Builder(name, provider);
		return Lua.newFunc((interp, args) -> {
			Lua.checkArgs(name, args, LuaType.TABLE);

			args[0].rawSet("__builder", builder);
			return args[0];
		});
	}

	static
	{
		fields.put("string", builder("string", StringField::new));
		fields.put("float", builder("float", FloatField::new));
		fields.put("integer", builder("integer", IntegerField::new));
		fields.put("id", builder("id", IDField::new));
	}
}
