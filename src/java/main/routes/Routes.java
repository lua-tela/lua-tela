package main.routes;

import com.hk.json.Json;
import com.hk.json.JsonFormatException;
import com.hk.json.JsonObject;
import com.hk.json.JsonValue;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaLibrary;
import com.hk.lua.LuaObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import main.context.LuaContext;
import main.db.LuaBase;
import main.luacompat.RequestLibrary;
import main.luacompat.ResponseLibrary;

public class Routes
{
    private final File file;
    private final long lastModified, lastRetrieved, length;
    private final List<Route> routes;
    private static final Pattern urlPattern = Pattern.compile("[a-zA-Z0-9_\\-\\/\\.]+");
    private static final Pattern fileNamePattern = Pattern.compile("[a-zA-Z0-9_\\-\\.]+");
    
    public Routes(File file) throws RouteException
    {
        this.file = file;
        this.lastModified = file.lastModified();
        this.lastRetrieved = System.currentTimeMillis();
        this.length = file.length();
        routes = new LinkedList<>();
        
        try
        {
            JsonValue val = Json.read(file);
            
            if(!val.isObject())
                throw new RouteException("Expected route file to be a json object, not '" + val.getType().name().toLowerCase() + "'");
            
            String s;
            JsonObject obj = val.getObject();
            for(Map.Entry<String, JsonValue> ent : obj)
            {
                s = ent.getKey();
                val = ent.getValue();
                
                if(!s.startsWith("/"))
                    throw new RouteException("Unexpected string key '" + s + "'");
                
                if(val.isString())
                    routes.add(new Route(s, val.getString()));
                else
                    throw new RouteException("Unexpected value '" + val + "' for key '" + s + "'");
            }
        }
        catch(JsonFormatException ex)
        {
            throw new RouteException("File not in valid json format: '" + file.getAbsolutePath() + "'");
        }
        catch(FileNotFoundException ex)
        {
            throw new RouteException("File not found: '" + file.getAbsolutePath() + "'");
        }
        
        Collections.sort(routes);
    }
    
    public boolean canDiscard()
    {
        if(lastModified != file.lastModified() || length != file.length())
            return true;
        
        return System.currentTimeMillis() - lastRetrieved > 5 * 60 * 60 * 1000;
    }
    
    public void addRoute(String path, String source) throws RouteException
    {
        routes.add(new Route(path, source));
        Collections.sort(routes);
    }
    
    public Route getRoute(String path)
    {
        for(Route route : routes)
        {
            if(route.path.equals(path))
                return route;
        }
        return null;
    }
    
    public Route findRoute(String path)
    {
        for(Route route : routes)
        {
            if(route.matches(path))
                return route;
        }
        return null;
    }
    
    public List<Route> getRoutes()
    {
        return new ArrayList<>(routes);
    }
    
    public static class Route implements Comparable<Route>
    {
        public final String path, source;
        private final Pattern pattern;
        
        public Route(String path, String source) throws RouteException
        {
            this.path = path;
            this.source = source;
            pattern = source == null ? null : Pattern.compile("^" + Pattern.quote(path) + ".*");

            if(!path.isEmpty() && !urlPattern.matcher(path).matches())
                throw new RouteException("Invalid page url: '" + path + "'. Only numbers, letters, periods, hyphens, underscores, and slashes.");
            if(!fileNamePattern.matcher(source).matches())
                throw new RouteException("Invalid file name: '" + source + "' for key '" + path + "'. Only numbers, letters, periods, hyphens, and underscores.");
        }

        public void serve(LuaBase db, LuaContext ctx, String method, String url, String path) throws Exception
        {
            LuaInterpreter interp = db.setupInterpreter(ctx, false, false, false);

            interp.setExtra("method", method);
            interp.setExtra("url", url);
            interp.setExtra("path", path);

            interp.importLib(new LuaLibrary<>("request", RequestLibrary.class));
            interp.importLib(new LuaLibrary<>("response", ResponseLibrary.class));
            interp.execute();

            File luaSource = new File(db.base, "pages" + File.separator + source + ".lua");
            LuaObject res = interp.require(luaSource.getName(), new FileReader(luaSource));
            if(res.getBoolean())
            {
                ctx.handle(res);
                ctx.getWriter().close();
            }
        }
        
        boolean matches(String path)
        {
            return pattern.matcher(path).matches();
        }
        
        public void append(JsonObject obj)
        {
            obj.put(path, source);
        }
        
        @Override
        public int compareTo(Routes.Route o)
        {
            return Integer.compare(o.path.length(), path.length());
        }
        
        @Override
        public String toString()
        {
            return '"' + path + '"';
        }
    }
}
