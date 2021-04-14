package main.db;

import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaObject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FloatField extends DataField
{
    protected final LuaObject def;
    protected boolean allowNull;
    
    public FloatField(LuaObject tbl)
    {
        super(tbl);
        LuaObject def1 = tbl.isNil() ? Lua.nil() : tbl.getIndex("default");
        if(!def1.isNil())
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
            ins.values.put(name, def);
    }
    
    @Override
    public void appendCreate(Model model, StringBuilder sb)
    {
        sb.append('`');
        sb.append(name);
        sb.append("` DOUBLE");
        if(def != null)
            sb.append(" DEFAULT ").append(def);
        if(!allowNull)
            sb.append(" NOT NULL");
    }
    
    @Override
    public LuaObject clean(LuaObject obj) throws LuaException
    {
        if(obj.isNumber() || obj.isNil() && allowNull)
            return obj;
        else if(obj.isString())
            return Lua.newNumber(obj.getFloat());
        else
            throw new LuaException("expected float" + (allowNull ? " or nil" : ""));
    }

    @Override
    public LuaObject toLuaObject(ResultSet set, int index) throws SQLException
    {
        return Lua.newNumber(set.getDouble(index));
    }
    
    @Override
    public void toJavaObject(PreparedStatement stmt, int index, LuaObject value) throws SQLException
    {
        stmt.setDouble(index, value.getFloat());
    }
}
