package main.db;

import com.hk.lua.Lua;
import com.hk.lua.Lua.LuaMethod;
import com.hk.lua.LuaException;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaUserdata;
import java.util.HashMap;
import java.util.Map;

public class Instance extends LuaUserdata
{
    public final Model model;
    public boolean generated, changed;
    public final Map<String, LuaObject> values;
    protected final Map<String, LuaObject> edited;
    private LuaObject saveFunc;
    
    public Instance(Model model, boolean generated)
    {
        this.model = model;
        this.generated = generated;
        values = new HashMap<>();
        edited = new HashMap<>();
        changed = false;
        metatable = model.insMetatable;
        saveFunc = model.interp.getExtraLua("instance.saveFunc");
    }
    
    public Instance save()
    {
        return model.save(this);
    }
    
    public void put(String name, LuaObject val)
    {
        if(!generated)
        {
            changed = true;
            edited.put(name, val);
        }
        else
            values.put(name, val);
    }
    
    @Override
    public LuaObject doIndex(LuaObject key)
    {
        LuaObject val = null;
        String k = null;
        if(key.isString())
        {
            k = key.getString();
            if(edited.containsKey(k))
                val = edited.get(k);
            else
                val = values.get(k);

            if(val == null && model.getField(k) != null)
                return Lua.nil();
        }
        if(val == null)
        {
            if("save".equals(k))
                return saveFunc;
            else
                return super.doIndex(key);
        }
        else
            return val;
    }
    
    @Override
    public void doNewIndex(LuaObject key, LuaObject val)
    {
        if(key.isString())
        {
            DataField df = model.getField(key.getString());
            if(df != null)
            {
                try
                {
                    put(key.getString(), df.clean(val));
                    return;
                }
                catch(LuaException ex)
                {
                    throw new LuaException("Cannot clean " + key.getString() + " field, " + ex.getLocalizedMessage());
                }
            }
        }
        
        if(key.isString() && "save".equals(key.getString()))
            saveFunc = val;
        else
            super.doNewIndex(val, key);
    }

    @Override
    public String name()
    {
        return "INSTANCE*";
    }

    @Override
    public Object getUserdata()
    {
        return this;
    }
    
    @Override
    public String getString()
    {
        if(metatable.isNil() || metatable.getIndex("__tostring").isNil())
            return "'" + model.name + "' instance";
        return super.getString();
    }
}
