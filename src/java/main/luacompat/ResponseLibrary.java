package main.luacompat;

import com.hk.func.BiConsumer;
import com.hk.func.BiFunction;
import com.hk.lua.Environment;
import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.servlet.ServletException;
import main.context.LuaContext;
import main.ResServlet;

public enum ResponseLibrary implements BiConsumer<Environment, LuaObject>, BiFunction<Environment, LuaObject[], LuaObject>
{
    setContentType() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            
            ctx.setContentType(args[0].getString());

            return Lua.nil();
        }
    },
    setContentSize() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.INTEGER);
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            
            ctx.setContentLength(args[0].getInteger());
            
            return Lua.nil();
        }
    },
    setHeader() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING, LuaType.STRING);
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            
            ctx.setHeader(args[0].getString(), args[1].getString());
            return Lua.nil();
        }
    },
    setStatus() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.INTEGER);
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            
            ctx.setStatus((int) args[0].getInteger());
            return Lua.nil();
        }
    },
    serve() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            String contentType = null;
            
            if(args.length > 1)
            {
                if(args[1].type() != LuaType.STRING)
                    throw new LuaException("bad argument #2 to '" + name() + "' (string or nil expected)");
                
                contentType = args[1].getString();
            }
            
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            
            try
            {
                ResServlet.serveFile(ctx, args[0].getString(), contentType);
            }
            catch (ServletException | IOException ex)
            {
                throw new RuntimeException(ex);
            }
            return Lua.nil();
        }
    },
    redirect() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            LuaContext ctx = env.interp.getExtra("context", LuaContext.class);
            
            try
            {
                ctx.redirect(args[0].getString());
            }
            catch (IOException ex)
            {
                throw new UncheckedIOException(ex);
            }

            return Lua.nil();
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
