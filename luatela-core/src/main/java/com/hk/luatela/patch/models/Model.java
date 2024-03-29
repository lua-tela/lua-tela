package com.hk.luatela.patch.models;

import com.hk.lua.*;
import com.hk.luatela.patch.DatabaseException;
import com.hk.luatela.patch.models.fields.DataField;
import com.hk.luatela.patch.models.fields.IDField;

import java.util.*;

public final class Model extends LuaUserdata
{
	public final String name;
	private final Map<String, DataField> fieldMap;
	private List<DataField> fields;
	final LuaObject instanceMetatable;

	public Model(ModelSet set, String name) throws DatabaseException
	{
		this.name = name;
		fieldMap = new LinkedHashMap<>();

		set.addModel(this);

		metatable = Lua.newTable();
		instanceMetatable = Lua.newTable();
		setupMetatables();
	}

	private void setupMetatables()
	{
		metatable.rawSet("__index", metatable);
		metatable.rawSet("__name", name());

		LuaObject saveFunc = Lua.newMethod((interp, args) -> {
			if (args.length != 0 && args[0] instanceof Instance && ((Instance) args[0]).getModel() == this)
			{
				// TODO: fix this
				return null;
			}
			else
				throw Lua.badArgument(1, getString() + " save", "expected '" + name + "' instance");
		});
		metatable.rawSet("create", Lua.newMethod(this::create));
		metatable.rawSet("save", saveFunc);

		instanceMetatable.rawSet("__index", instanceMetatable);
		instanceMetatable.rawSet("__name", Instance.__NAME);
		instanceMetatable.rawSet("save", saveFunc);
	}

	private LuaObject create(LuaInterpreter interp, LuaObject[] args)
	{
		Lua.checkArgs(getString() + " create", args, LuaType.TABLE);
		return Instance.apply(new Instance(this, true), args[0]);
	}

	public void setFields(List<DataField> fields) throws DatabaseException
	{
		if(this.fields != null)
			throw new DatabaseException("Unexpected call to set fields, already contains fields");

		if(fields instanceof LinkedList)
			this.fields = fields;
		else
			this.fields = new LinkedList<>(fields);

		boolean hasPrimary = false;
		for(DataField field : this.fields)
		{
			hasPrimary |= field.isPrimary();
			fieldMap.put(field.name, field);
		}

		if(!hasPrimary)
			throw new DatabaseException("Model '" + name + "' doesn't seem to have primary field");

		Collections.sort(this.fields);
	}

	void readFields(LuaObject object) throws DatabaseException
	{
		if(fields != null)
			throw new DatabaseException("Unexpected call to read fields, already contains fields");

		Set<Map.Entry<LuaObject, LuaObject>> entries = object.getEntries();

		LinkedList<DataField> fields = new LinkedList<>();
		boolean hasPrimary = false;
		for(Map.Entry<LuaObject, LuaObject> entry : entries)
		{
			LuaObject key = entry.getKey();
			LuaObject value = entry.getValue();

			if(!key.isString())
				throw new DatabaseException("Unexpected non-string field in model table '" + key.name() + "'");
			if(!value.isTable())
				throw new DatabaseException("Unexpected non-table field value in model table '" + value.name() + "'");

			LuaObject obj = value.rawGet("__builder");
			String name = key.getString();
			if (!(obj instanceof DataField.Builder))
				throw new DatabaseException("Expected table to have the __builder attached to construct field");

			DataField.Builder builder = (DataField.Builder) obj;
			value.rawSet("__builder", Lua.NIL);
			DataField field = builder.provide(this, name, value);

			if(field.isPrimary())
				hasPrimary = true;

			fields.add(field);

			fieldMap.put(name, field);
		}

		if (!hasPrimary)
		{
			IDField idField = new IDField(this, "id", true);
			fieldMap.put(idField.name, idField);
			fields.addFirst(idField);
		}
		else
			Collections.sort(fields);

		this.fields = fields;
	}

	@Override
	public LuaObject doCall(LuaInterpreter interp, LuaObject[] args)
	{
		return create(interp, args);
	}

	public Map<String, DataField> getFieldMap()
	{
		return Collections.unmodifiableMap(fieldMap);
	}

	public List<DataField> getFields()
	{
		return Collections.unmodifiableList(fields);
	}

	public DataField getFieldNamed(String name)
	{
		return fieldMap.get(name);
	}

	public boolean hasFieldNamed(String name)
	{
		return fieldMap.containsKey(name);
	}

	@Override
	public String name()
	{
		return "*MODEL";
	}

	@Override
	public Model getUserdata()
	{
		return this;
	}

	@Override
	public String getString(LuaInterpreter interp)
	{
		return "model '" + name + "'";
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;

		if(o instanceof Model)
		{
			Model model = (Model) o;

			return Objects.equals(name, model.name) && Objects.equals(fields, model.fields);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(name, fields);
	}
}
