package main.db;

import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaObject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class TimestampField extends DataField
{
    protected final LuaObject def;
    protected boolean allowNull;
    
    public TimestampField(LuaObject tbl)
    {
        super(tbl);
        LuaObject def1 = tbl.isNil() ? Lua.nil() : tbl.getIndex("default");
        if(def1.isString() && def1.getString().equals("now"))
            def = Lua.newString("now");
        else if(!def1.isNil())
            def = clean(def1);
        else
            def = null;
        
        LuaObject allowNull1 = tbl.isNil() ? Lua.nil() : tbl.getIndex("null");
        if(!allowNull1.isNil())
            allowNull = allowNull1.getBoolean();
        else
            allowNull = false;
    }

    @Override
    public void initiate(Model model, Instance ins)
    {
        if(def != null)
            ins.values.put(name, clean(def));
    }

    @Override
    public void appendCreate(Model model, StringBuilder sb)
    {
        sb.append('`');
        sb.append(name);
        sb.append("` TIMESTAMP");
        if(def != null)
        {
            if(def.getString().equals("now"))
                sb.append(" DEFAULT CURRENT_TIMESTAMP");
            else
                sb.append(" DEFAULT FROM_UNIXTIME(").append(clean(def).getInteger() / 1000).append(')');
        }
        if(!allowNull)
            sb.append(" NOT NULL");
    }
    
    @Override
    public LuaObject clean(LuaObject obj) throws LuaException
    {
        if(obj.isInteger() || obj.isNil() && allowNull)
            return obj;
        else if(obj.isString() && obj.getString().equals("now"))
            return Lua.newNumber(System.currentTimeMillis());
        else
            throw new LuaException("expected millis as integer" + (allowNull ? " or nil" : ""));
    }

    @Override
    public LuaObject toLuaObject(ResultSet set, int index) throws SQLException
    {        
        return Lua.newNumber(set.getTimestamp(index).getTime());
    }
    
    @Override
    public void toJavaObject(PreparedStatement stmt, int index, LuaObject value) throws SQLException
    {
        stmt.setTimestamp(index, new Timestamp(value.getInteger()));
    }
}
