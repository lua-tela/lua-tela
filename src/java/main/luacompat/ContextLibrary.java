package main.luacompat;

import com.hk.func.BiConsumer;
import com.hk.func.BiFunction;
import com.hk.lua.Environment;
import com.hk.lua.Lua;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import main.context.LuaContext;
import main.db.LuaBase;

public enum ContextLibrary implements BiConsumer<Environment, LuaObject>, BiFunction<Environment, LuaObject[], LuaObject>
{
    realPath() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            return Lua.newString(ctx.getRealPath(args[0].getString()));
        }
    },
    hasAttr() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            Map<String, LuaObject> map = (Map<String, LuaObject>) ctx.getAttribute("lua");
            return Lua.newBoolean(map.containsKey(args[0].getString()));
        }
    },
    getAttr() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            Map<String, LuaObject> map = (Map<String, LuaObject>) ctx.getAttribute("lua");
            return Lua.newLuaObject(map.get(args[0].getString()));
        }
    },
    getAttrNames() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            Map<String, LuaObject> map = (Map<String, LuaObject>) ctx.getAttribute("lua");
            return Lua.newLuaObject(map.keySet());
        }
        
        @Override
        public void accept(Environment env, LuaObject table)
        {
            super.accept(env, table);
            
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            
            if(ctx != null)
                ctx.setAttribute("lua", new HashMap<>());
        }
    },
    setAttr() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING, LuaType.ANY);
            
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            Map<String, LuaObject> map = (Map<String, LuaObject>) ctx.getAttribute("lua");
            map.put(args[0].getString(), args[1]);
            return Lua.nil();
        }
    },
    removeAttr() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            Map<String, LuaObject> map = (Map<String, LuaObject>) ctx.getAttribute("lua");
            LuaObject obj = map.remove(args[0].getString());
            return obj == null ? Lua.nil() : obj;
        }
    },
    escapeHTML() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            return Lua.newString(LuaBase.escapeHTML(args[0].getString()));
        }
    },
    getMimeType() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);

            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            return Lua.newString(ctx.getMimeType(args[0].getString()));
        }
    },
    res() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);

            String url = env.interp.getExtra("url", String.class);
            try
            {
                return Lua.newString(url + "/res/" + URLEncoder.encode(args[0].getString(), "UTF-8"));
            }
            catch (UnsupportedEncodingException ex)
            {
                throw new Error();
            }
        }
    },
    urlEncode() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            try
            {
                return Lua.newString(URLEncoder.encode(args[0].getString(), "UTF-8"));
            }
            catch (UnsupportedEncodingException ex)
            {
                throw new Error();
            }
        }
    },
    urlDecode() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            try
            {
                return Lua.newString(URLDecoder.decode(args[0].getString(), "UTF-8"));
            }
            catch (UnsupportedEncodingException ex)
            {
                throw new Error();
            }
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
