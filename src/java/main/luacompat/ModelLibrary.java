package main.luacompat;

import com.hk.func.BiConsumer;
import com.hk.func.BiFunction;
import com.hk.lua.Environment;
import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaObject;
import com.hk.lua.LuaType;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import main.db.BooleanField;
import main.db.DataField;
import main.db.LuaBase;
import main.db.FloatField;
import main.db.IDField;
import main.db.IntegerField;
import main.db.Model;
import main.db.StringField;
import main.db.TimestampField;

public enum ModelLibrary implements BiConsumer<Environment, LuaObject>, BiFunction<Environment, LuaObject[], LuaObject>
{
    model() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args0)
        {
            Lua.checkArgs(toString(), args0, LuaType.STRING);
            final String name = args0[0].getString();
            return Lua.newFunc((LuaObject[] args) -> {
                Lua.checkArgs(toString() + " '" + name + "'", args, LuaType.TABLE);
                LuaBase db = env.interp.getExtra("db", LuaBase.class);
                if(db.getModel(LuaBase.LUA, name) == null)
                    return db.addModel(new Model(env.interp, LuaBase.LUA, name, args[0]));
                else
                    return null;
            });
        }
    },
    field() {
        @Override
        public LuaObject apply(Environment env, LuaObject[] args)
        {
            Lua.checkArgs(toString(), args, LuaType.STRING);
            String name = args[0].getString();
            LuaObject func = fields.get(name.toLowerCase());
            if(func == null)
                throw new LuaException("Unknown field data type '" + name + "'");
            return func;
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
    
    private static LuaObject fieldFunc(String name, Function<LuaObject, DataField> supp)
    {
        return Lua.newFunc((LuaObject[] args) -> {
            Lua.checkArgs("field '" + name + "'", args, LuaType.TABLE);
            return supp.apply(args[0]);
        });
    }
    
    private static final Map<String, LuaObject> fields = new HashMap<>();
    static
    {
        LuaObject f;
        
        f = fieldFunc("integer", IntegerField::new);
        fields.put("integer", f);
        fields.put("int", f);
        
        f = fieldFunc("float", FloatField::new);
        fields.put("float", f);
        fields.put("double", f);
        fields.put("dec", f);

        f = fieldFunc("string", StringField::new);
        fields.put("string", f);
        fields.put("str", f);
        
        f = fieldFunc("boolean", BooleanField::new);
        fields.put("boolean", f);
        fields.put("bool", f);

        f = fieldFunc("timestamp", TimestampField::new);
        fields.put("timestamp", f);

        f = fieldFunc("id", IDField::new);
        fields.put("id", f);
//        
//        fields.put("date", fieldFunc("date", DateField::new));
//        fields.put("time", fieldFunc("time", TimeField::new));
//        fields.put("datetime", fieldFunc("datetime", DateTimeField::new));
    }
}
