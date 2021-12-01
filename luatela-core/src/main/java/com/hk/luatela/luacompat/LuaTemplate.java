package com.hk.luatela.luacompat;

import com.hk.lua.*;
import com.hk.str.StringUtil;

import java.io.*;
import java.util.Stack;

public class LuaTemplate
{
    private final LuaFactory factory;

    public LuaTemplate(Reader source) throws TemplateException, IOException
    {
        TemplateReader rdr = new TemplateReader(source);

        factory = rdr.compile();
        factory.compile();

        Lua.importStandard(factory);
    }

    public LuaInterpreter create(Writer writer)
    {
        LuaInterpreter interp = factory.build();

        interp.setExtra(LuaLibraryIO.EXKEY_STDOUT, new LuaWriter(writer));
        return interp;
    }

    private void print(StringBuilder txt, StringBuilder lua)
    {
        if(txt.length() == 0)
            return;

        int amt = 0;

        while(txt.indexOf("[" + StringUtil.repeat("=", amt) + "[") != -1 ||
                txt.indexOf("]" + StringUtil.repeat("=", amt) + "]") != -1)
            amt++;

        String s = StringUtil.repeat("=", amt);

        lua.append('\n').append("stdout:write([").append(s).append("[");
        if(txt.length() > 0 && txt.charAt(0) == '\n')
            lua.append('\n');
        lua.append(txt);
        lua.append(']').append(s).append("]);");

        txt.setLength(0);
    }

    private class TemplateReader
    {
        private final LineNumberReader source;

        TemplateReader(Reader source)
        {
            this.source = new LineNumberReader(source);
        }

        LuaFactory compile() throws TemplateException, IOException
        {
            int i;
            char c;
            boolean trim;

            StringBuilder txt = new StringBuilder();
            StringBuilder lua = new StringBuilder("local stdout = io.output()");
            loop:
            while((i = source.read()) >= 0)
            {
                c = (char) i;

                if(c == '{')
                {
                    i = source.read();

                    if(i >= 0)
                    {
                        c = (char) i;

                        trim = false;
                        if(c == '-')
                        {
                            i = source.read();

                            if(i == -1)
                            {
                                txt.append("{-");
                                continue;
                            }
                            else
                            {
                                c = (char) i;
                            }

                            trim = true;
                        }

                        if(c == '{' || c == '[')
                        {
                            if(trim)
                            {
                                int j;
                                for(j = txt.length() - 1; j >= 0; j--)
                                {
                                    if(!Character.isWhitespace(txt.charAt(j)))
                                        break;
                                }
                                txt.setLength(j + 1);
                            }

                            print(txt, lua);

                            trim = read(txt, lua, c == '[');

                            if(trim)
                            {
                                while(true)
                                {
                                    source.mark(1);
                                    i = source.read();

                                    if(i == -1)
                                        break loop;

                                    c = (char) i;

                                    if(!Character.isWhitespace(c))
                                    {
                                        source.reset();
                                        break;
                                    }
                                }
                            }

                            continue;
                        }
                        else
                            txt.append('{');
                    }
                }

                txt.append(c);
            }

            source.close();

            print(txt, lua);

            return Lua.factory(lua.toString());
        }

        private boolean read(StringBuilder txt, StringBuilder lua, boolean block) throws TemplateException, IOException
        {
            String type = block ? "block" : "value";
            boolean close, trim = false;
            int i;
            char c;
            Stack<Character> ops = new Stack<>();

            while(true)
            {
                i = source.read();

                if (i == -1)
                    throw new TemplateException("Unexpected <eof>, expected closing " + type);

                c = (char) i;

                close = false;
                switch (c) {
                    case '{':
                    case '(':
                    case '[':
                        ops.push(c);
                        txt.append(c);
                        break;
                    case ')':
                        if (!ops.isEmpty() && ops.peek() == '(')
                            ops.pop();
                        txt.append(c);
                        break;
                    case ']':
                        if (!ops.isEmpty() && ops.peek() == '[') {
                            ops.pop();
                            txt.append(c);
                        } else if (block && ops.isEmpty())
                            close = true;
                        else
                            txt.append(c);
                        break;
                    case '}':
                        if (!ops.isEmpty() && ops.peek() == '{') {
                            ops.pop();
                            txt.append(c);
                        } else if (!block && ops.isEmpty())
                            close = true;
                        else
                            txt.append(c);
                        break;
                    default:
                        txt.append(c);
                }

                if (ops.isEmpty() && close) {
                    source.mark(2);
                    i = source.read();

                    if (i == -1)
                        throw new TemplateException("Unexpected <eof>, expected closing " + type);

                    c = (char) i;

                    if (c == '-') {
                        trim = true;

                        i = source.read();

                        if (i == -1)
                            throw new TemplateException("Unexpected <eof>, expected closing " + type);

                        c = (char) i;
                    }

                    if (c == '}')
                        break;
                    else
                        source.reset();
                }
            }

            if(block)
                lua.append('\n').append(txt);
            else
                lua.append('\n').append("stdout:write(").append(txt).append(");");

            txt.setLength(0);

            return trim;
        }
    }

    public static class TemplateException extends Exception
    {
        private TemplateException(String message)
        {
            super(message);
        }
    }
}
