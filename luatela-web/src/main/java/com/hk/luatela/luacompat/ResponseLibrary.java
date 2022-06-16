package com.hk.luatela.luacompat;

import com.hk.lua.*;
import com.hk.luatela.LuaContext;
import com.hk.luatela.servlet.ResourceServlet;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.function.BiConsumer;

@SuppressWarnings("unused")
public enum ResponseLibrary implements BiConsumer<Environment, LuaObject>, Lua.LuaMethod
{
    setContentType() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            LuaContext ctx = interp.getExtra("context", LuaContext.class);

            ctx.response.setContentType(args[0].getString());

            return Lua.TRUE;
        }

        @Override
        public void accept(Environment env, LuaObject table)
        {
            super.accept(env, table);

            env.interp.getExtra("context", LuaContext.class)
                    .response.setContentType("text/plain");
        }
    },
    setContentEncoding() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);

            LuaContext ctx = interp.getExtra("context", LuaContext.class);

            ctx.response.setCharacterEncoding(args[0].getString());

            return Lua.TRUE;
        }
    },
    setContentSize() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.INTEGER);
            LuaContext ctx = interp.getExtra("context", LuaContext.class);

            ctx.response.setContentLengthLong(args[0].getLong());

            return Lua.TRUE;
        }
    },
    setHeader() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING, LuaType.STRING);
            LuaContext ctx = interp.getExtra("context", LuaContext.class);

            ctx.response.setHeader(args[0].getString(), args[1].getString());
            return Lua.TRUE;
        }
    },
    setStatus() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.INTEGER);
            LuaContext ctx = interp.getExtra("context", LuaContext.class);

            ctx.response.setStatus(args[0].getInt());
            return Lua.TRUE;
        }
    },
    serve() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            String contentType = null;

            if(args.length > 1)
            {
                if(args[1].isString())
                    contentType = args[1].getString();
                else if(!args[1].isNil())
                    throw new LuaException("bad argument #2 to '" + name() + "' (string or nil expected)");
            }

            LuaContext ctx = interp.getExtra("context", LuaContext.class);

            try
            {
                String str = args[0].getString();

                if(str.startsWith("/") || str.startsWith("\\") || str.startsWith(File.separator))
                    str = str.substring(1);
                
                Path path = ctx.luaTela.resourceRoot.resolve(str);
                return Lua.newBool(ResourceServlet.serveFile(
                        ctx.luaTela.context, ctx.response,
                        path, contentType));
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    },
    redirect() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            LuaContext ctx = interp.getExtra("context", LuaContext.class);

            try
            {
                ctx.response.sendRedirect(args[0].getString());
            }
            catch (IOException ex)
            {
                throw new UncheckedIOException(ex);
            }

            return Lua.TRUE;
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