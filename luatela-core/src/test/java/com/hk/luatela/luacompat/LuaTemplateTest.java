package com.hk.luatela.luacompat;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LuaTemplateTest
{
    @Test
    public void testStuff() throws IOException, LuaTemplate.TemplateException
    {
        new LuaTemplate(new InputStreamReader(LuaTemplateTest.class.getResourceAsStream("/templates/index.lua.html")));
    }
}