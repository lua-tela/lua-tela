package main.db;

import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import com.hk.lua.LuaUserdata;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static main.db.LuaBase.SYS;
import static main.db.LuaBase.LUA;

public class Model extends LuaUserdata
{
    public final LuaInterpreter interp;
    public final LuaBase db;
    public final int owner;
    public final String name, tblName;
    public final LuaObject insMetatable;
    private final DataField[] fields;
    private final Map<String, DataField> fieldNames;
    
    public Model(LuaInterpreter interp, int owner, String name, LuaObject table)
    {
        this.interp = interp;
        this.db = interp.getExtra("db", LuaBase.class);
        this.owner = owner;
        this.name = name;
        String s;
        switch(owner)
        {
            case LUA: s = "lua"; break;
            case SYS: s = "sys"; break;
            default: throw new IllegalArgumentException("only expected EllBase.USR or EllBase.SYS, not " + owner + ".");
        }
        tblName = s + '_' + name;
        fieldNames = new HashMap<>();
        List<DataField> lst = new ArrayList<>();
        boolean primary = false;
        LuaObject mt = null;
        for(Map.Entry<LuaObject, LuaObject> ent : table.getEntries())
        {
            LuaObject key = ent.getKey();
            LuaObject val = ent.getValue();
            
            if(key.isString() && val instanceof DataField)
            {
                ((DataField) val).name = key.getString();
                primary = primary || ((DataField) val).isPrimary();
                fieldNames.put(key.getString(), (DataField) val);
                lst.add((DataField) val);
            }
            else if(key.isString() && key.getString().equals("__metatable"))
            {
                if(!val.isTable())
                    throw new LuaException("expected '__metatable' to be a table, not " + val.name());
                
                mt = val;
            }
            else
                throw new LuaException("unexpected entry in table, (" + key.getString() + "=" + val.getString() + ")");
        }
        if(mt != null)
            this.insMetatable = mt;
        else
            this.insMetatable = Lua.newTable();
        if(primary)
            Collections.sort(lst, (DataField o1, DataField o2) -> Boolean.compare(o2.isPrimary(), o1.isPrimary()));
        else
        {
            int amt = 0;
            String nm = "id";
            while(fieldNames.containsKey(nm))
            {
                amt++;
                nm = "id" + amt;
            }
            IDField field = new IDField(Lua.nil());
            field.name = nm;
            fieldNames.put(nm, field);
            lst.add(0, field.setPrimary());
        }
        
        this.fields = lst.toArray(new DataField[0]);
        
        LuaObject modelMetatable = Lua.newTable();
        modelMetatable.setIndex("__name", Lua.newString(name()));
        modelMetatable.setIndex("__index", modelMetatable);
        modelMetatable.setIndex("create", Lua.newFunc(this::create));
        modelMetatable.setIndex("get", Lua.newFunc(this::get));
        modelMetatable.setIndex("select", Lua.newFunc(this::select));
        
        LuaObject flds = Lua.newTable();
        for(DataField field : fields)
            flds.setIndex(field.name, field);
        
        modelMetatable.setIndex("fields", flds);
        modelMetatable.setIndex("meta", insMetatable);
        modelMetatable.setIndex("funcs", insMetatable);
        this.metatable = modelMetatable;
    }
    
    public DataField getField(String field)
    {
        return fieldNames.get(field);
    }
    
    public void createTable()
    {
        StringBuilder sb = new StringBuilder();

        sb.append("CREATE TABLE IF NOT EXISTS `");
        sb.append(tblName);
        sb.append("` (\n");
        String[] primaries = new String[fields.length];
        for(int i = 0; i < fields.length; i++)
        {
            sb.append('\t');

            DataField field = fields[i];
            if(field.isPrimary())
                primaries[i] = field.name;
            field.appendCreate(this, sb);

            sb.append(",\n");
        }
        sb.append("\tPRIMARY KEY(");
        for(String primary : primaries)
        {
            if(primary == null)
                continue;
            sb.append('`');
            sb.append(primary);
            sb.append("`, ");
        }
        sb.setLength(sb.length() - 2);
        sb.append(")\n);");
        
        try
        {
            String sql = sb.toString();
            db.total.append(sql).append("\n\n");
            db.update(sql);
        }
        catch (Exception ex)
        {
            throw new LuaException(ex.getLocalizedMessage());
//            throw new RuntimeException(ex);
        }
    }
    
    public void dropTable()
    {
        try
        {
            String sql = "DROP TABLE IF EXISTS `" + tblName + "`;";
            db.total.append(sql).append("\n\n");
            db.update(sql);
        }
        catch (Exception ex)
        {
            throw new LuaException(ex.getLocalizedMessage());
//            throw new RuntimeException(ex);
        }
    }
    
    private LuaObject create(LuaObject[] args)
    {
        Lua.checkArgs(getString() + " create", args, LuaType.TABLE);
        LuaObject tbl = args[0];
        try
        {
            Instance ins = new Instance(this, true);
            for(DataField field : fields)
                field.initiate(this, ins);
            
            for(Map.Entry<LuaObject, LuaObject> ent : tbl.getEntries())
            {
                if(ent.getKey().isString())
                {
                    DataField field = fieldNames.get(ent.getKey().getString());
                    ins.values.put(ent.getKey().getString(), field.clean(ent.getValue()));
                }
            }
            return ins;
        }
        catch (Exception ex)
        {
            throw new LuaException(ex.getLocalizedMessage());
        }
    }

    private LuaObject get(LuaObject[] args)
    {
        Lua.checkArgs(getString() + " get", args, LuaType.TABLE);
        LuaObject tbl = args[0];
        
        Query query = new Query(db, this);
        Query.where(query, tbl);
        Query.open(query);
        LuaObject obj = Query.next(query);
        
        if(!Query.next(query).isNil())
            throw new LuaException("more than one result, use select(...)");
        
        Query.close(query);
        
        return obj;
    }
    
    private LuaObject select(LuaObject[] args)
    {
        Query query = new Query(db, this);
        if(args.length > 0)
        {
            LuaObject[] tmp = new LuaObject[args.length + 1];
            tmp[0] = query;
            System.arraycopy(args, 0, tmp, 1, args.length);
            query = Query.only(tmp);
        }
        return query;
    }
    
    public Instance save(Instance ins)
    {
        if(ins.model != this)
            throw new IllegalArgumentException();
        
        StringBuilder sb = new StringBuilder();
        DataField autoField = null;
        PreparedStatement ps;
        
        try
        {
            if(ins.generated)
            {
                sb.append("INSERT INTO `").append(tblName).append("`(");
                for(DataField field : fields)
                {
                    if(field.isAuto())
                    {
                        autoField = field;
                        continue;
                    }

                    field.appendName(this, sb, false);
                    sb.append(", ");
                }
                sb.setLength(sb.length() - 2);
                sb.append(") VALUES (");
                for(DataField field : fields)
                {
                    if(field.isAuto())
                        continue;

    //                field.appendValue(this, sb, ins.values.get(field.name));
                    sb.append("?, ");
                }
                sb.setLength(sb.length() - 2);
                sb.append(");");
                ps = db.conn.prepareStatement(sb.toString(), Statement.RETURN_GENERATED_KEYS);
                
                int indx = 1;
                for(int i = 0; i < fields.length; i++)
                {
                    if(fields[i].isAuto())
                        continue;
                    
                    fields[i].toJavaObject(ps, indx++, ins.values.get(fields[i].name));
                }

                ins.generated = false;
            }
            else if(ins.changed)
            {            
                sb.append("UPDATE `").append(tblName).append("` SET ");

                DataField f;
                for(Map.Entry<String, LuaObject> e : ins.edited.entrySet())
                {
                    f = fieldNames.get(e.getKey());
                    f.appendName(this, sb, true);

                    sb.append(" = ?, ");
                }

                sb.setLength(sb.length() - 2);

                sb.append(" WHERE ");

                for(DataField field : fields)
                {
                    if(field.isPrimary())
                    {
                        field.appendName(this, sb, true);

                        sb.append(" = ? AND ");
//                        field.appendValue(this, sb, ins.values.get(field.name));
                    }
                }

                sb.setLength(sb.length() - 5);

                sb.append(";");
                ps = db.conn.prepareStatement(sb.toString());
                
                int i = 1;
                for(Map.Entry<String, LuaObject> e : ins.edited.entrySet())
                    fieldNames.get(e.getKey()).toJavaObject(ps, i++, e.getValue());
                
                LuaObject val;
                for(DataField field : fields)
                {
                    if(field.isPrimary())
                    {
                        val = ins.values.get(field.name);
                        if(val == null)
                            throw new LuaException("expected primary key '" + name + '.' + field.name + "' to be present to update");

                        field.toJavaObject(ps, i++, val);
                    }
                }

                ins.changed = false;
                ins.values.putAll(ins.edited);
                ins.edited.clear();
            }
            else
                ps = null;
        
            if(ps != null)
            {
                String sql = sb.toString();
                db.total.append(sql).append("\n\n");
                ps.executeUpdate();
                if(autoField != null)
                {
                    ResultSet set = ps.getGeneratedKeys();
                    if(set.next())
                        ins.values.put(autoField.name, Lua.newNumber(set.getLong(1)));

                    set.close();
                }
                ps.close();
            }
        }
        catch (Exception ex)
        {
            throw new LuaException(ex.getLocalizedMessage());
//            throw new RuntimeException(ex);
        }
        return ins;
    }
    
    @Override
    public final String name()
    {
        return "MODEL*";
    }

    @Override
    public Object getUserdata()
    {
        return this;
    }

    @Override
    public String getString()
    {
        return "model '" + name + "'";
    }
    
    @Override
    public LuaObject doCall(LuaObject[] args)
    {
        return create(args);
    }
}
