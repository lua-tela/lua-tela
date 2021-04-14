package main.db;

import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaObject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringField extends DataField
{
    protected final LuaObject def, length;
    protected boolean allowNull;
    
    public StringField(LuaObject tbl)
    {
        super(tbl);
        LuaObject def1 = tbl.isNil() ? Lua.nil() : tbl.getIndex("default");
        if(!def1.isNil())
            def = clean(def1);
        else
            def = null;
        
        LuaObject length1 = tbl.isNil() ? Lua.nil() : tbl.getIndex("length");
        if(!length1.isNil())
        {
            if(!length1.isInteger() || length1.getInteger() < 0)
                throw new LuaException("expected 'length' to be a positive integer");
            
            length = length1;
        }
        else
            throw new LuaException("expected 'length' with string field");
        
        LuaObject allowNull1 = tbl.isNil() ? Lua.nil() : tbl.getIndex("null");
        if(!allowNull1.isNil())
            allowNull = allowNull1.getBoolean();
        else
            allowNull = false;
    }

    @Override
    public void initiate(Model model, Instance ins)
    {
        if(def != null)
            ins.values.put(name, def);
    }
    
    @Override
    public void appendCreate(Model model, StringBuilder sb)
    {
        sb.append('`');
        sb.append(name);
        sb.append("` VARCHAR(");
        sb.append(length);
        sb.append(')');
        if(def != null)
            sb.append(" DEFAULT ").append('\'').append(escape(def.getString())).append('\'');
        if(!allowNull)
            sb.append(" NOT NULL");
    }
    
    @Override
    public LuaObject clean(LuaObject obj) throws LuaException
    {
        if(obj.isString() || obj.isNil() && allowNull)
            return obj;
        else if(obj.isNumber())
            return Lua.newString(obj.getString());
        else
            throw new LuaException("expected string" + (allowNull ? " or nil" : ""));
    }

    @Override
    public LuaObject toLuaObject(ResultSet set, int index) throws SQLException
    {
        return Lua.newString(set.getString(index));
    }
    
    @Override
    public void toJavaObject(PreparedStatement stmt, int index, LuaObject value) throws SQLException
    {
        stmt.setString(index, value.getString());
    }
    
    private static final HashMap<String,String> sqlTokens;
    private static Pattern sqlTokenPattern;

    static
    {           
        //MySQL escape sequences: http://dev.mysql.com/doc/refman/5.1/en/string-syntax.html
        String[][] search_regex_replacement = new String[][]
        {
                    //search string     search regex        sql replacement regex
                {   "\u0000"    ,       "\\x00"     ,       "\\\\0"     },
                {   "'"         ,       "'"         ,       "\\\\'"     },
                {   "\""        ,       "\""        ,       "\\\\\""    },
                {   "\b"        ,       "\\x08"     ,       "\\\\b"     },
                {   "\n"        ,       "\\n"       ,       "\\\\n"     },
                {   "\r"        ,       "\\r"       ,       "\\\\r"     },
                {   "\t"        ,       "\\t"       ,       "\\\\t"     },
                {   "\u001A"    ,       "\\x1A"     ,       "\\\\Z"     },
                {   "\\"        ,       "\\\\"      ,       "\\\\\\\\"  }
        };

        sqlTokens = new HashMap<>();
        String patternStr = "";
        for (String[] srr : search_regex_replacement)
        {
            sqlTokens.put(srr[0], srr[2]);
            patternStr += (patternStr.isEmpty() ? "" : "|") + srr[1];            
        }
        sqlTokenPattern = Pattern.compile('(' + patternStr + ')');
    }


    public static String escape(String s)
    {
        Matcher matcher = sqlTokenPattern.matcher(s);
        StringBuffer sb = new StringBuffer();
        while(matcher.find())
        {
            matcher.appendReplacement(sb, sqlTokens.get(matcher.group(1)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
