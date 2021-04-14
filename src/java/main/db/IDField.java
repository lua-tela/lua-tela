package main.db;

import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaObject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IDField extends DataField
{    
    public IDField(LuaObject tbl)
    {
        super(tbl);
    }

    @Override
    public void initiate(Model model, Instance ins)
    {
    }
    
    @Override
    public void appendCreate(Model model, StringBuilder sb)
    {
        sb.append('`');
        sb.append(name);
        sb.append("` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT");
    }

    @Override
    public boolean isAuto()
    {
        return true;
    }
    
    @Override
    public LuaObject clean(LuaObject obj) throws LuaException
    {
        if(obj.isInteger())
            return obj;
        else if(obj.isString())
            return Lua.newNumber(obj.getInteger());
        else
            throw new LuaException("expected integer");
    }

    @Override
    public LuaObject toLuaObject(ResultSet set, int index) throws SQLException
    {
        return Lua.newNumber(set.getLong(index));
    }
    
    @Override
    public void toJavaObject(PreparedStatement stmt, int index, LuaObject value) throws SQLException
    {
        stmt.setLong(index, value.getInteger());
    }
}
