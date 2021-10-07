package com.hk.luatela.patch.models;

import com.hk.lua.Lua;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaUserdata;
import com.hk.luatela.patch.DatabaseException;
import com.hk.luatela.patch.models.fields.DataField;
import com.hk.luatela.patch.models.fields.IDField;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.function.Predicate;

public class Model extends LuaUserdata
{
	public final String name;
	private final Map<String, DataField> fieldMap;
	private List<DataField> fields;

	public Model(ModelSet set, String name) throws DatabaseException
	{
		this.name = name;
		fieldMap = new LinkedHashMap<>();

		set.addModel(this);
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
			value.rawSet("__builder", Lua.nil());
			DataField field = builder.provide(this, name, value);

			if(field.isPrimary())
				hasPrimary = true;

			fields.add(field);

			fieldMap.put(name, field);
		}

		if(hasPrimary)
			Collections.sort(fields);
		else
		{
			IDField idField = new IDField(this, "id", true);
			fieldMap.put(idField.name, idField);
			fields.addFirst(idField);
		}

		this.fields = fields;
	}

	public Map<String, DataField> getFieldMap()
	{
		return Collections.unmodifiableMap(fieldMap);
	}

	public List<DataField> getFields()
	{
		return Collections.unmodifiableList(fields);
	}

	public void iterateFields(Predicate<DataField> predicate)
	{
		for(DataField field : fields)
		{
			if(predicate.test(field))
				break;
		}
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
		return name;
	}
}
