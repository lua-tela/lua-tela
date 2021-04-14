package main.db;

import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaUserdata;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DataField extends LuaUserdata
{
    protected String name;
    private boolean primary = false;
    
    public DataField(LuaObject tbl)
    {
        LuaObject primary1 = tbl.isNil() ? Lua.nil() : tbl.getIndex("primary");
        if(!primary1.isNil())
        {
            if(!primary1.isBoolean())
                throw new LuaException("expected 'primary' to be a boolean");

            primary = primary1.getBoolean();
        }
    }
    
    public abstract void initiate(Model model, Instance ins);
    
    public abstract void appendCreate(Model model, StringBuilder sb);
    
    public abstract LuaObject toLuaObject(ResultSet set, int index) throws SQLException;

    public abstract void toJavaObject(PreparedStatement stmt, int index, LuaObject value) throws SQLException;
    
    public abstract LuaObject clean(LuaObject obj) throws LuaException;
    
    public DataField setPrimary()
    {
        primary = true;
        return this;
    }
    
    public boolean isPrimary()
    {
        return primary;
    }

    public boolean isAuto()
    {
        return false;
    }
    
    public void appendName(Model model, StringBuilder sb, boolean strict)
    {
        if(strict)
        {
            sb.append('`');
            sb.append(model.tblName);
            sb.append("`.");
        }
        sb.append('`');
        sb.append(name);
        sb.append('`');
    }
    
    public String getName()
    {
        return name;
    }

    @Override
    public String name()
    {
        return "FIELD*";
    }

    @Override
    public Object getUserdata()
    {
        return this;
    }
    
    @Override
    public String getString()
    {
        return "field '" + name + "'";
    }
}
