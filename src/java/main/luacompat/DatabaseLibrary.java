package main.luacompat;

import com.hk.func.BiConsumer;
import com.hk.func.BiFunction;
import com.hk.lua.Environment;
import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import main.db.LuaBase;
import main.db.Instance;

public enum DatabaseLibrary implements BiConsumer<Environment, LuaObject>, BiFunction<Environment, LuaObject[], LuaObject>
{
    model() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            return env.interp.getExtra("db", LuaBase.class).getModel(LuaBase.LUA, args[0].getString());
        }
    },
    save() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            if(args.length >= 1 && args[0] instanceof Instance)
                return ((Instance) args[0]).save();
            else
                throw new LuaException("bad argument #1 to 'save' (expected INSTANCE*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
        }
    },
    total() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            return Lua.newString(env.interp.getExtra("db", LuaBase.class).total);
        }
    };
    
    @Override
    public LuaObject apply(Environment env, LuaObject[] args)
    {
        throw new Error();
    }
    
    @Override
    public void accept(Environment env, LuaObject table)
    {
        String name = toString();
        if(name != null && !name.trim().isEmpty())
            table.setIndex(name, Lua.newFunc((LuaObject[] args) -> apply(env, args)));
    }
}
