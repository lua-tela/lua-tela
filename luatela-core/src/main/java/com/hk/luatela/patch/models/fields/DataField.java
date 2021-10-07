package com.hk.luatela.patch.models.fields;

import com.hk.lua.Lua;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaUserdata;
import com.hk.luatela.patch.models.Model;

import java.util.function.BiFunction;

public abstract class DataField extends LuaUserdata implements Comparable<DataField>
{
	public final Model parent;
	public final String name;
	boolean primary;

	DataField(Model parent, String name)
	{
		this.parent = parent;
		this.name = name;
	}

	DataField accept(LuaObject properties)
	{
		LuaObject primary = properties.rawGet("primary");
		if(!primary.isNil())
			this.primary = primary.getBoolean();

		return this;
	}

	@Override
	public DataField getUserdata()
	{
		return this;
	}

	@Override
	public String getString(LuaInterpreter interp)
	{
		return name;
	}

	public boolean isPrimary()
	{
		return primary;
	}

	@Override
	public int compareTo(DataField o)
	{
		return Boolean.compare(primary, o.primary);
	}

	public static class Builder extends LuaUserdata
	{
		private final String name;
		private final BiFunction<Model, String, DataField> provider;

		public Builder(String name, BiFunction<Model, String, DataField> provider)
		{
			this.name = name;
			this.provider = provider;
		}

		public DataField provide(Model model, String name, LuaObject properties)
		{
			return provider.apply(model, name).accept(properties);
		}

		@Override
		public String name()
		{
			return "*FIELDBUILDER";
		}

		@Override
		public Builder getUserdata()
		{
			return this;
		}

		@Override
		public String getString(LuaInterpreter interp)
		{
			return "\"" + name + "\" " + name();
		}
	}
}
