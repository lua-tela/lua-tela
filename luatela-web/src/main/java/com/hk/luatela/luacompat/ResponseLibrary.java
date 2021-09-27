package com.hk.luatela.luacompat;

import com.hk.func.BiConsumer;
import com.hk.lua.*;
import com.hk.luatela.LuaContext;
import com.hk.luatela.servlet.ResourceServlet;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;

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

            return Lua.nil();
        }
    },
    setContentSize() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.INTEGER);
            LuaContext ctx = interp.getExtra("context", LuaContext.class);

            ctx.response.setContentLengthLong(args[0].getInteger());

            return Lua.nil();
        }
    },
    setHeader() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING, LuaType.STRING);
            LuaContext ctx = interp.getExtra("context", LuaContext.class);

            ctx.response.setHeader(args[0].getString(), args[1].getString());
            return Lua.nil();
        }
    },
    setStatus() {
        @Override
        public LuaObject call(LuaInterpreter interp, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.INTEGER);
            LuaContext ctx = interp.getExtra("context", LuaContext.class);

            ctx.response.setStatus((int) args[0].getInteger());
            return Lua.nil();
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
                if(args[1].type() != LuaType.STRING)
                    throw new LuaException("bad argument #2 to '" + name() + "' (string or nil expected)");

                contentType = args[1].getString();
            }

            LuaContext ctx = interp.getExtra("context", LuaContext.class);

            try
            {
                ResourceServlet.serveFile(ctx.luaTela.context, ctx.request, ctx.response,
                                            Paths.get(args[0].getString()), contentType);
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
            return Lua.nil();
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

            return Lua.nil();
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
            table.setIndex(env.interp, name, Lua.newFunc(this));
    }
}