package main.db;

import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaUserdata;
import com.hk.util.KeyValue;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Query extends LuaUserdata
{
    private final LuaBase db;
    private final Model model;
    private final List<Filter> filters;
    private boolean opened, closed;
    private PreparedStatement stmt;
    private ResultSet set;
    private String[] only;
    private KeyValue<Integer>[] orderBys;
    private Integer offset, limit;
    
    public Query(LuaBase db, Model model)
    {
        this.db = db;
        this.model = model;
        filters = new ArrayList<>();
        opened = closed = false;
        metatable = queryMetatable;
    }
    
    public static Query only(LuaObject... args)
    {        
        Query query = checkQuery("only", args);
                
        if(query.opened)
            throw alreadyOpened();
        
        String[] only = new String[args.length - 1];
        String str;
        for(int i = 1; i < args.length; i++)
        {
            if(args[i].isString())
                str = args[i].getString();
            else if(args[i] instanceof DataField)
                str = ((DataField) args[i]).name;
            else
                throw new LuaException("bad argument #" + (i + 1) + " to 'only' (expected string, got " + args[i].name() + ")");
            
            if(query.model.getField(str) == null)
                throw new LuaException("bad argument #" + (i + 1) + " to 'only' (no field under '" + query.model.name + "' named '" + str + "')");

            only[i - 1] = str;
        }
        
        if(only.length == 0)
            query.only = null;
        else
            query.only = only;

        return query;
    }
    
    public static Query where(LuaObject... args)
    {        
        Query query = checkQuery("where", args);
        
        if(query.opened)
            throw alreadyOpened();
        
        for(int i = 1; i < args.length; i++)
        {
            if(args[i] instanceof Filter)
            {
                // TODO: validate filter with query
                throw new UnsupportedOperationException();
            }
            else if(args[i].isTable())
            {
                LuaObject key;
                String str;
                for(Map.Entry<LuaObject, LuaObject> ent : args[i].getEntries())
                {
                    key = ent.getKey();
                    if(key.isString())
                    {
                        int state = Filters.EQUALS;
                        str = key.getString();
                        if(str.endsWith("__lessthan"))
                            state = Filters.LESS_THAN;
                        else if(str.endsWith("__grtrthan"))
                            state = Filters.GRTR_THAN;
                        else if(str.endsWith("__lseqthan"))
                            state = Filters.LSEQ_THAN;
                        else if(str.endsWith("__greqthan"))
                            state = Filters.GREQ_THAN;
                        
                        if(state != Filters.EQUALS)
                            str = str.substring(0, str.length() - 10);
                        
                        DataField field = query.model.getField(str);
                        
                        if(field != null)
                            query.filters.add(new Filters.CompareFilter(query.model, field, ent.getValue(), state));
                        else
                            throw new LuaException("unexpected key '" + str + "' in table (no field with this name)");
                    }
                    else
                        throw new LuaException("unexpected value in table (expected string, got " + key.name() + ")");
                }
            }
            else
                throw new LuaException("bad argument #" + (i + 1) + " to 'where' (expected table or FILTER*, got " + args[i].name() + ")");
        }

        return query;
    }
    
    public static Query orderBy(LuaObject... args)
    {        
        Query query = checkQuery("orderBy", args);
        
        if(query.opened)
            throw alreadyOpened();
        
        KeyValue<Integer>[] orderBys = new KeyValue[args.length - 1];
        for(int i = 1; i < args.length; i++)
        {
            if(args[i].isString())
            {
                int state = 0;
                String s = args[i].getString();
                
                if(s.startsWith("+"))
                {
                    state = 1;
                    s = s.substring(1);
                }
                else if(s.startsWith("-"))
                {
                    state = -1;
                    s = s.substring(1);
                }
                
                if(query.model.getField(s) == null)
                    throw new LuaException("bad argument #" + (i + 1) + " to 'orderBy' (string isn't valid field name, '" + args[i].name() + "')");
                
                orderBys[i - 1] = new KeyValue<>(s, state);
            }
            else
                throw new LuaException("bad argument #" + (i + 1) + " to 'orderBy' (expected string, got " + args[i].name() + ")");
        }
        query.orderBys = orderBys;
        
        return query;
    }
    
    public static Query offset(LuaObject... args)
    {        
        Query query = checkQuery("offset", args);
        
        if(query.opened)
            throw alreadyOpened();
        
        if(args.length > 1 && args[1].isInteger())
        {
            int offset = (int) args[1].getInteger();
            
            if(offset < 0)
                throw new LuaException("bad argument #2 to 'offset' (integer should not be negative)");
            else
                query.offset = offset;
            
            if(args.length > 2)
            {
                if(args[2].isInteger())
                {
                    int limit = (int) args[2].getInteger();

                    if(limit < 0)
                        throw new LuaException("bad argument #3 to 'offset' (integer should not be negative)");
                    else
                        query.limit = limit;
                }
                else
                    throw new LuaException("bad argument #3 to 'offset' (expected integer, got " + (args.length > 1 ? args[1].name() : "nil") + ")");
            }
        }
        else
            throw new LuaException("bad argument #2 to 'offset' (expected integer, got " + (args.length > 1 ? args[1].name() : "nil") + ")");
        
        return query;
    }
    
    public static Query limit(LuaObject... args)
    {        
        Query query = checkQuery("limit", args);
        
        if(query.opened)
            throw alreadyOpened();
        
        if(args.length > 1 && args[1].isInteger())
        {
            int limit = (int) args[1].getInteger();
            
            if(limit < 0)
                throw new LuaException("bad argument #2 to 'limit' (integer should not be negative)");
            else
                query.limit = limit;
        }
        else
            throw new LuaException("bad argument #2 to 'limit' (expected integer, got " + (args.length > 1 ? args[1].name() : "nil") + ")");
        
        return query;
    }
    
    public static Query open(LuaObject... args)
    {
        Query query = checkQuery("open", args);
        
        if(query.opened)
            throw alreadyOpened();

        try
        {
            query.opened = true;
            
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            
            if(query.only != null)
            {
                for(int i = 0; i < query.only.length; i++)
                {
                    sb.append(query.only[i]);
                    
                    if(i < query.only.length - 1)
                        sb.append(", ");
                }
            }
            else
            {
                sb.append('*');
            }
            sb.append(" FROM `").append(query.model.tblName).append("`");
            if(!query.filters.isEmpty())
            {
                sb.append(" WHERE ");
                for(int i = 0; i < query.filters.size(); i++)
                {
                    query.filters.get(i).append(query, sb);
                    
                    if(i < query.filters.size() - 1)
                        sb.append(" AND ");
                }
            }
            
            if(query.orderBys != null && query.orderBys.length > 0)
            {
                sb.append(" ORDER BY ");
                for(int i = 0; i < query.orderBys.length; i++)
                {
                    sb.append('`').append(query.orderBys[i].key).append('`');
                    
                    switch(query.orderBys[i].value)
                    {
                        case 1:
                            sb.append(" ASC");
                            break;
                        case -1:
                            sb.append(" DESC");
                            break;
                    }
                    
                    if(i < query.orderBys.length - 1)
                        sb.append(", ");
                }
            }
            if(query.limit != null)
            {
               sb.append(" LIMIT ");
               if(query.offset != null)
                   sb.append(query.offset).append(", ");
               
               sb.append(query.limit);
            }
            sb.append(';');
            
            query.db.total.append(sb).append("\n\n");
            query.stmt = query.db.prepared(sb.toString());
            
            int count = 1;
            for (Filter filter : query.filters)
                count += filter.apply(query, query.stmt, count);
            
            query.set = query.stmt.executeQuery();
        }
        catch (Exception ex)
        {
            throw new LuaException(ex.getLocalizedMessage());
        }
        return query;
    }
    
    public static LuaObject next(LuaObject... args)
    {
        Query query = checkQuery("next", args);
        
        if(query.closed)
            throw alreadyClosed();
        else if(!query.opened)
            throw notOpenYet();
        
        try
        {
            if(!query.set.next())
                return Lua.nil();

            Instance ins = new Instance(query.model, false);
            ResultSetMetaData meta = query.set.getMetaData();
            int cols = meta.getColumnCount();
            DataField field;
            for (int i = 1; i <= cols; i++)
            {
                field = query.model.getField(meta.getColumnName(i));
                ins.values.put(field.name, field.toLuaObject(query.set, i));
            }

            return ins;
        }
        catch (Exception ex)
        {
            throw new LuaException(ex.getLocalizedMessage());
        }
    }
    
    public static Query close(LuaObject... args)
    {
        Query query = checkQuery("close", args);
        
        if(!query.opened)
            throw notOpenYet();
        else if(query.closed)
            throw alreadyClosed();
        
        try
        {
            query.closed = true;
            query.set.close();
            query.stmt.close();
        }
        catch (Exception ex)
        {
            throw new LuaException(ex.getLocalizedMessage());
        }
        return query;
    }
    
    public static LuaObject isOpened(LuaObject... args)
    {
        return Lua.newBoolean(checkQuery("isOpened", args).opened);
    }
    
    public static LuaObject isClosed(LuaObject... args)
    {
        return Lua.newBoolean(checkQuery("isClosed", args).closed);
    }
    
    @Override
    public String name()
    {
        return NAME;
    }

    @Override
    public Object getUserdata()
    {
        return this;
    }

    @Override
    public String getString()
    {
        String s = "";
        if(closed)
            s += "closed ";
        else if(opened)
            s += "opened";

        return s + "select query";
    }
    
    @Override
    public LuaObject doCall(LuaObject[] args)
    {
        if(closed)
            return Lua.nil();
        
        if(!opened)
            open(this);

        LuaObject obj = next(this);

        if(obj.isNil())
            close(this);
            
        return obj;
    }
    
    private static LuaException alreadyClosed()
    {
        return new LuaException("query already closed");
    }
    
    private static LuaException alreadyOpened()
    {
        return new LuaException("query already opened");
    }
    
    private static LuaException notOpenYet()
    {
        return new LuaException("query not opened yet");
    }
    
    private static Query checkQuery(String method, LuaObject[] args)
    {
        if(args.length >= 1 && args[0] instanceof Query)
            return (Query) args[0];
        else
            throw new LuaException("bad argument #1 to '" + method + "' (expected QUERY*, got " + (args.length >= 1 ? args[0].name() : "nil") + ")");
    }
    
    private static final LuaObject queryMetatable = Lua.newTable();
    private static final String NAME = "QUERY*";
    
    static
    {
        LuaObject f;
        
        queryMetatable.setIndex("__name", NAME);
        queryMetatable.setIndex("__index", queryMetatable);
        queryMetatable.setIndex("only", Lua.newFunc(Query::only));
        
        f = Lua.newFunc(Query::where);
        queryMetatable.setIndex("where", f);
        queryMetatable.setIndex("and", f);

        queryMetatable.setIndex("orderBy", Lua.newFunc(Query::orderBy));
        queryMetatable.setIndex("offset", Lua.newFunc(Query::offset));
        queryMetatable.setIndex("limit", Lua.newFunc(Query::limit));
        
        queryMetatable.setIndex("open", Lua.newFunc(Query::open));
        queryMetatable.setIndex("next", Lua.newFunc(Query::next));
        queryMetatable.setIndex("close", Lua.newFunc(Query::close));
        queryMetatable.setIndex("isOpened", Lua.newFunc(Query::isOpened));
        queryMetatable.setIndex("isClosed", Lua.newFunc(Query::isClosed));
    }

    static abstract class Filter extends LuaUserdata
    {
        public abstract void append(Query query, StringBuilder sb);        

        public abstract int apply(Query query, PreparedStatement stmt, int index);
    
        @Override
        public String name()
        {
            return "FILTER*";
        }

        @Override
        public Object getUserdata()
        {
            return this;
        }

        @Override
        public String getString()
        {
            return "filter";
        }
    }
}
