package main.luacompat;

import com.hk.func.BiConsumer;
import com.hk.func.BiFunction;
import com.hk.lua.Environment;
import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import main.context.LuaContext;

public enum RequestLibrary implements BiConsumer<Environment, LuaObject>, BiFunction<Environment, LuaObject[], LuaObject>
{
    hasParam() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            
            Map<String, LuaObject> params = env.interp.getExtra("params", Map.class);
            
            if(params == null)
            {
                LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
                return Lua.newBoolean(ctx.getParam(args[0].getString()) != null);
            }
            else
            {
                return Lua.newBoolean(params.containsKey(args[0].getString()));
            }
        }
    },
    getParam() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            
            Map<String, LuaObject> params = env.interp.getExtra("params", Map.class);
            
            if(params == null)
            {
                LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
                String s = ctx.getParam(args[0].getString());
                if(s == null && args.length > 1)
                    return args[1];
                else
                    return Lua.newString(s);
            }
            else
            {
                LuaObject obj = params.get(args[0].getString());
                if(obj == null)
                    return args.length > 1 ? args[1] : Lua.nil();
                else
                    return obj;
            }
        }
    },
    host() {
        @Override
        public void accept(Environment env, LuaObject table)
        {
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            if(ctx != null)
                table.setIndex(toString(), Lua.newString(ctx.getHost()));
        }
    },
    port() {
        @Override
        public void accept(Environment env, LuaObject table)
        {
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            if(ctx != null)
                table.setIndex(toString(), Lua.newNumber(ctx.getPort()));
        }
    },
    user() {
        @Override
        public void accept(Environment env, LuaObject table)
        {
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            if(ctx != null)
                table.setIndex(toString(), Lua.newString(ctx.getUser()));
        }
    },
    address() {
        @Override
        public void accept(Environment env, LuaObject table)
        {
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            if(ctx != null)
                table.setIndex(toString(), Lua.newString(ctx.getAddress()));
        }
    },
    method() {
        @Override
        public void accept(Environment env, LuaObject table)
        {
            String method = env.interp.getExtra("method", String.class);
            if(method != null)
                table.setIndex(toString(), Lua.newString(method));
        }
    },
    url() {
        @Override
        public void accept(Environment env, LuaObject table)
        {
            String url = env.interp.getExtra("url", String.class);
            if(url != null)
                table.setIndex(toString(), Lua.newString(env.interp.getExtra("url", String.class)));
        }
    },
    path() {
        @Override
        public void accept(Environment env, LuaObject table)
        {
            String path = env.interp.getExtra("path", String.class);
            if(path != null)
                table.setIndex(toString(), Lua.newString(env.interp.getExtra("path", String.class)));
        }
    },
    isNewSess() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            return Lua.newBoolean(ctx.isNewSess());
        }
        
        @Override
        public void accept(Environment env, LuaObject table)
        {
            super.accept(env, table);
            
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            
            if(ctx.isNewSess())
                ctx.setSess("lua", new HashMap<>());
        }
    },
    hasSess() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);

            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            Map<String, Object> map = (Map<String, Object>) ctx.getSess("lua");
            return Lua.newBoolean(map.containsKey(args[0].getString()));
        }
    },
    getSess() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);

            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            Map<String, Object> map = (Map<String, Object>) ctx.getSess("lua");
            return Lua.newLuaObject(map.get(args[0].getString()));
        }
    },
    setSess() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING, LuaType.ANY);

            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            Map<String, Object> map = (Map<String, Object>) ctx.getSess("lua");
            map.put(args[0].getString(), toObj(args[1]));
            return Lua.nil();
        }
    },
    removeSess() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            Map<String, LuaObject> map = (Map<String, LuaObject>) ctx.getAttribute("lua");
            if(args.length > 0)
            {
                Lua.checkArgs(toString(), args, LuaType.STRING);
                LuaObject obj = map.remove(args[0].getString());
                return obj == null ? Lua.nil() : obj;
            }
            else
                ctx.removeSess();

            return Lua.nil();
        }
    },
    getSessID() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            return Lua.newString(env.interp.getExtra("context", LuaContext.class).getSessID());
        }
    },
    FILES() {
        @Override
        public void accept(Environment env, LuaObject table)
        {
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            LuaObject tbl = ctx.getFileTable(env.interp);
            if(tbl != null)
                table.setIndex(name(), tbl);
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
    
    private static Object toObj(LuaObject val)
    {
        switch(val.type())
        {
            case NIL:
                return null;
            case BOOLEAN:
                return val.getBoolean();
            case STRING:
                return val.getString();
            case FLOAT:
                return val.getFloat();
            case INTEGER:
                return val.getInteger();
            case TABLE:
                Map<Object, Object> map = new LinkedHashMap<>();
                for(Map.Entry<LuaObject, LuaObject> ent : val.getEntries())
                    map.put(toObj(ent.getKey()), toObj(ent.getValue()));

                return map;
            default:
                throw new LuaException("Cannot set " + val.name() + " in the session");
        }
    }
}
