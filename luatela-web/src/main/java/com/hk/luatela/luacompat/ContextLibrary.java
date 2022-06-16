package com.hk.luatela.luacompat;

import com.hk.lua.*;
import com.hk.lua.Lua.LuaMethod;
import com.hk.luatela.LuaTela;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@SuppressWarnings({"unused", "unchecked"})
public enum ContextLibrary implements BiConsumer<Environment, LuaObject>, LuaMethod
{
    realPath() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);

            LuaTela luaTela = interp.getExtra(LuaTela.QUALIKEY, LuaTela.class);
            return Lua.newString(luaTela.context.getRealPath(args[0].getString()));
        }
    },
    dataPath() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);

            LuaTela luaTela = interp.getExtra(LuaTela.QUALIKEY, LuaTela.class);
            String str = args[0].getString();
            if(str.startsWith("/") || str.startsWith("\\") || str.startsWith(File.separator))
                str = str.substring(1);
            return Lua.newString(luaTela.dataroot.resolve(str).toString());
        }
    },
    resPath() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);

            LuaTela luaTela = interp.getExtra(LuaTela.QUALIKEY, LuaTela.class);
            String str = args[0].getString();
            if(str.startsWith("/") || str.startsWith("\\") || str.startsWith(File.separator))
                str = str.substring(1);
            return Lua.newString(luaTela.resourceRoot.resolve(str).toString());
        }
    },
    hasAttr() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);

            LuaTela luaTela = interp.getExtra(LuaTela.QUALIKEY, LuaTela.class);
            Map<String, LuaObject> map = (Map<String, LuaObject>) luaTela.context.getAttribute("lua");
            return Lua.newBool(map.containsKey(args[0].getString()));
        }
    },
    getAttr() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);

            LuaTela luaTela = interp.getExtra(LuaTela.QUALIKEY, LuaTela.class);
            Map<String, LuaObject> map = (Map<String, LuaObject>) luaTela.context.getAttribute("lua");
            return map.get(args[0].getString());
        }
    },
    getAttrNames() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            LuaTela luaTela = interp.getExtra(LuaTela.QUALIKEY, LuaTela.class);
            Map<String, LuaObject> map = (Map<String, LuaObject>) luaTela.context.getAttribute("lua");
            return Lua.newLuaObject(map.keySet());
        }
        
        @Override
        public void accept(Environment env, LuaObject table)
        {
            super.accept(env, table);

            LuaTela luaTela = env.interp.getExtra(LuaTela.QUALIKEY, LuaTela.class);
            Object lua = luaTela.context.getAttribute("lua");

            if(!(lua instanceof HashMap))
            {
                if(lua != null)
                    System.err.println("Unexpected object set in 'lua' attribute '" + lua + "'");

                luaTela.context.setAttribute("lua", new HashMap<>());
            }
        }
    },
    setAttr() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING, LuaType.ANY);

            LuaTela luaTela = interp.getExtra(LuaTela.QUALIKEY, LuaTela.class);
            Map<String, LuaObject> map = (Map<String, LuaObject>) luaTela.context.getAttribute("lua");
            map.put(args[0].getString(), args[1]);
            return args[1];
        }
    },
    removeAttr() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);

            LuaTela luaTela = interp.getExtra(LuaTela.QUALIKEY, LuaTela.class);
            Map<String, LuaObject> map = (Map<String, LuaObject>) luaTela.context.getAttribute("lua");
            LuaObject obj = map.remove(args[0].getString());
            return obj == null ? Lua.NIL : obj;
        }
    },
    escapeHTML() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            return Lua.newString(LuaTela.escapeHTML(args[0].getString()));
        }
    },
    getMimeType() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);

            LuaTela luaTela = interp.getExtra(LuaTela.QUALIKEY, LuaTela.class);
            return Lua.newString(luaTela.context.getMimeType(args[0].getString()));
        }
    },
    res() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);

            LuaTela luaTela = interp.getExtra(LuaTela.QUALIKEY, LuaTela.class);
            return Lua.newString("/" + luaTela.resourcePath + "/" + URLEncoder.encode(args[0].getString(), StandardCharsets.UTF_8));
        }
    },
    urlEncode() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            return Lua.newString(URLEncoder.encode(args[0].getString(), StandardCharsets.UTF_8));
        }
    },
    urlDecode() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            return Lua.newString(URLDecoder.decode(args[0].getString(), StandardCharsets.UTF_8));
        }
    };

    @Override
    public LuaObject call(LuaInterpreter interp, LuaObject[] args)
    {
        throw new Error();
    }

    @Override
    public void accept(Environment env, LuaObject table)
    {
        String name = toString();
        if(name != null && !name.trim().isEmpty())
            table.setIndex(env.interp, name, Lua.newMethod(this));
    }
}
