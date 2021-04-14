package main.db;

import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaLibrary;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import main.context.LuaContext;
import main.luacompat.ContextLibrary;
import main.luacompat.DatabaseLibrary;
import main.luacompat.HTMLLibrary;
import main.luacompat.ModelLibrary;
import main.routes.RouteException;
import main.routes.Routes;

public class LuaBase
{
    public final File base;
    public final Connection conn;
    public final Routes routes;
    private final Map<String, Model> namedModels;
    public final StringBuilder total;
    private boolean admin, setup;
    private boolean registerModels;

    public LuaBase(LuaContext ctx, File base)
    {
        this.base = base;
        
        base.mkdirs();
        new File(base, "pages").mkdir();
        new File(base, "uploads").mkdir();
        new File(base, "libs").mkdir();
        new File(base, "res").mkdir();
        try
        {
            routes = ctx.getRoutes(base);
        }
        catch (RouteException ex)
        {
            throw new RuntimeException("Exception reading routes.json", ex);
        }
        
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
//            Class.forName("com.mysql.cj.jdbc.Driver");
            
            conn = ctx.getConnection();
//            
//            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/testdb?useSSL=false&allowPublicKeyRetrieval=true", "root", "hkrocks1");
        }
        catch (ClassNotFoundException | SQLException ex)
        {
             throw new RuntimeException(ex);
        }
        namedModels = new HashMap<>();
        total = new StringBuilder();
    }
    
    public boolean loaded()
    {
        try
        {
            return conn != null && !conn.isClosed();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    public Model addModel(Model model)
    {
        namedModels.put(model.tblName, model);
        
        if(registerModels)
        {
            model.dropTable();
            model.createTable();
        }
        
        return model;
    }
    
    public Model getModel(int owner, String name)
    {
        String s;
        switch(owner)
        {
            case LUA: s = "lua"; break;
            case SYS: s = "sys"; break;
            default: throw new IllegalArgumentException("only expected EllBase.USR or EllBase.SYS, not " + owner + ".");
        }
        return namedModels.get(s + "_" + name);
    }
    
    public Collection<Model> getModels()
    {
        return namedModels.values();
    }
    
    public PreparedStatement prepared(String sql) throws SQLException
    {
        return conn.prepareStatement(sql);
    }
    
    public Statement statement() throws SQLException
    {
        return conn.createStatement();
    }
    
    public int update(String sql) throws SQLException
    {
        Statement s = conn.createStatement();
        int rs = s.executeUpdate(sql);
        s.close();
        return rs;
    }
    
    public void close()
    {
//        System.out.println(total);
        try
        {
            conn.close();
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    public LuaInterpreter setupInterpreter(LuaContext ctx, boolean admin, boolean setup, boolean registerModels)
    {
        LuaInterpreter interp;
        try
        {
            interp = Lua.reader(new File(base, "models.lua"));
        }
        catch (FileNotFoundException ex)
        {
            throw new RuntimeException("Needs a 'models.lua', even if it's empty, in the '" + base.getAbsolutePath() + "' directory");
        }
        interp.setExtra("db", this);
        interp.setExtra("context", ctx);
        
        if(admin)
        {
            this.registerModels = registerModels;
            interp.getGlobals().setVar("_ADMIN", Lua.newBoolean(this.admin = true));
            if(setup)
                interp.getGlobals().setVar("_SETUP", Lua.newBoolean(this.setup = true));
        }

        interp.importLib(LuaLibrary.BASIC);
        interp.importLib(LuaLibrary.COROUTINE);
        interp.importLib(LuaLibrary.STRING);
        interp.importLib(LuaLibrary.TABLE);
        interp.importLib(LuaLibrary.MATH);
        interp.importLib(LuaLibrary.IO);
        interp.importLib(LuaLibrary.OS);
        interp.importLib(LuaLibrary.JSON);
        interp.importLib(LuaLibrary.HASH);
        interp.importLib(new LuaLibrary<>("context", ContextLibrary.class));
        interp.importLib(new LuaLibrary<>("html", HTMLLibrary.class));
        interp.importLib(new LuaLibrary<>(null, ModelLibrary.class));
        interp.importLib(new LuaLibrary<>("db", DatabaseLibrary.class));
        interp.getGlobals().setVar("require", Lua.newFunc((LuaObject[] args) -> {
            Lua.checkArgs("require", args, LuaType.STRING);
            String file = args[0].getString();
            if(!file.endsWith(".lua"))
                file += ".lua";
            if(file.startsWith("/"))
                file = file.substring(1);
                        
            File f = new File(base, "libs" + File.separator + file);

            if(f.exists())
            {
                try
                {
                    interp.require(file, new FileReader(f));
                    return Lua.newBoolean(true);
                }
                catch(IOException ex)
                {
                    throw new LuaException("IOException: " + ex.getLocalizedMessage());
                }
            }
            
            throw new LuaException("No such library found for path '" + file + "'");
        }));
        LuaObject saveFunc = interp.getGlobals().getVar("db").getIndex("save");
        interp.setExtra("instance.saveFunc", saveFunc);

        return interp;
    }
    
    public static String escapeHTML(String s)
    {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&')
            {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            }
            else
            {
                out.append(c);
            }
        }
        return out.toString();
    }
    
    public static final int LUA = 1;
    public static final int SYS = 2;
}
