package com.hk.luatela.patch.models;

import com.hk.func.BiConsumer;
import com.hk.lua.*;
import com.hk.luatela.patch.DatabaseException;
import com.hk.luatela.patch.models.fields.*;

import java.util.Collections;
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
	public static final Map<String, DataField.Builder> fieldBuilders;

	static
	{
		Map<String, DataField.Builder> builders = new HashMap<>();
		DataField.Builder builder;

		builder = new DataField.Builder("string", StringField::new);
		builders.put("string", builder);

		builder = new DataField.Builder("float", FloatField::new);
		builders.put("float", builder);

		builder = new DataField.Builder("integer", IntegerField::new);
		builders.put("integer", builder);

		builder = new DataField.Builder("id", IDField::new);
		builders.put("id", builder);

		fieldBuilders = Collections.unmodifiableMap(builders);

		for(Map.Entry<String, DataField.Builder> entry : fieldBuilders.entrySet())
		{
			String k = entry.getKey();
			DataField.Builder v = entry.getValue();
			fields.put(k, Lua.newFunc((interp, args) -> {
				Lua.checkArgs(k, args, LuaType.TABLE);

				args[0].rawSet("__builder", v);
				return args[0];
			}));
		}
	}
}
