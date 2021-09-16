package com.hk.luatela.luacompat;

import com.hk.lua.Environment;
import com.hk.lua.Lua;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import org.junit.Test;

import java.io.*;

public class LuaTemplateTest
{
    @Test
    public void testStuff()
    {
        String code = "local _17bec62c9f1 = _17bec62c9f1;\n" +
//                "\n" +
//                "    function foogaze(exp)\n" +
//                "        return 2 ^ exp\n" +
//                "    end\n" +
//                "\n" +
//                "_17bec62c9f1([[{\n" +
//                "    \"numbers\": [\n" +
//                "        1]]);\n" +
//                " for i=1, 10 do \n" +
//                "_17bec62c9f1([[,]]);\n" +
//                "_17bec62c9f1( foogaze(i) );\n" +
//                "_17bec62c9f1([[\n" +
//                "        ]]);\n" +
//                " end \n" +
//                "_17bec62c9f1([[]\n" +
//                "}]]);";
                "_17bec62c9f1([[]   }]]);";

        LuaInterpreter interp = Lua.reader(code);

        interp.getGlobals().setVar("_17bec62c9f1", Lua.newFunc((interp2, args) -> {
            for(LuaObject arg : args)
            {
                System.out.println(arg.getString(interp2));
            }
            return null;
        }));

        interp.execute();
    }

    @Test
    public void testJsonData() throws IOException, LuaTemplate.TemplateException
    {
        InputStream stream = LuaTemplateTest.class.getResourceAsStream("/templates/data.lua.json");

        if(stream == null)
            throw new NullPointerException();

        LuaTemplate template = new LuaTemplate(new InputStreamReader(stream));

        Writer wtr = new CharArrayWriter();
        LuaInterpreter interp = template.create(wtr);

        Object result = interp.execute();
        wtr.close();
        System.out.println("-----------------");
        System.out.println(wtr);

        if(result instanceof LuaObject)
            System.out.println("result = " + result);
    }
}