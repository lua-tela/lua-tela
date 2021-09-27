package com.hk.luatela.luacompat;

import com.hk.lua.LuaInterpreter;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;

public class LuaTemplateTest
{
    @Test
    public void testJsonData() throws IOException, LuaTemplate.TemplateException
    {
        InputStream stream = LuaTemplateTest.class.getResourceAsStream("/templates/data.lua.json");

        if(stream == null)
            throw new NullPointerException();

        LuaTemplate template = new LuaTemplate(new InputStreamReader(stream));

        StringWriter wtr = new StringWriter();
        LuaInterpreter interp = template.create(wtr);

        interp.execute();
        wtr.close();
        assertEquals("{\n    \"numbers\": [\n        1,2,4,8,16,32,64,128,256,512,1024\n    ]\n}", wtr.toString());
    }

    @Test
    public void testHTMLData() throws IOException, LuaTemplate.TemplateException
    {
        InputStream stream = LuaTemplateTest.class.getResourceAsStream("/templates/index.lua.html");

        if(stream == null)
            throw new NullPointerException();

        LuaTemplate template = new LuaTemplate(new InputStreamReader(stream));

        StringWriter wtr = new StringWriter();
        LuaInterpreter interp = template.create(wtr);

        interp.execute();
        wtr.close();
        assertEquals("<!DOCTYPE html>\n" +
                "<html>\n" +
                "    <title>Hello World!</title>\n" +
                "    <h3>Table</h3>\n" +
                "    <table>\n" +
                "        <tr>\n" +
                "            <th>Key</th>\n" +
                "            <th>Value</th>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td>all</td>\n" +
                "            <td>my</td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td>friends</td>\n" +
                "            <td>is</td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "            <td>a</td>\n" +
                "            <td>lowrider</td>\n" +
                "        </tr>\n" +
                "    </table>\n" +
                "</html>", wtr.toString());
    }
}