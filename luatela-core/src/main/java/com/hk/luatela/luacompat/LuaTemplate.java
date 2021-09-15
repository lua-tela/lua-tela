package com.hk.luatela.luacompat;

import com.hk.lua.LuaFactory;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;

public class LuaTemplate
{
    private final LuaFactory factory;

    public LuaTemplate(Reader source) throws TemplateException, IOException
    {
        TemplateReader rdr = new TemplateReader(source);

        rdr.compile();

        factory = null;
    }

    private class TemplateReader
    {
        private final LineNumberReader source;

        TemplateReader(Reader source)
        {
            this.source = new LineNumberReader(source);
        }

        void compile() throws IOException
        {
            int i;
            char c;
            boolean trim;

            StringBuilder lua = new StringBuilder();
            loop:
            while((i = source.read()) >= 0)
            {
                c = (char) i;

                if(c == '{')
                {
                    source.mark(2);

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
                                lua.append("{-");
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
                                for(int j = lua.length() - 1; j >= 0; j--)
                                {
                                    if(!Character.isWhitespace(lua.charAt(j)))
                                        lua.setLength(j + 1);
                                }
                            }

                            if(c == '{')
                                trim = readValue(lua);
                            else
                                trim = readBlock(lua);

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
                                        source.reset();
                                }
                            }

                            continue;
                        }
                        else
                            source.reset();
                    }
                }

                lua.append(c);
            }

            source.close();

            System.out.println(lua);
        }

        private boolean readValue(StringBuilder lua)
        {
            return false;
        }

        private boolean readBlock(StringBuilder lua)
        {
            return false;
        }
    }

    public class TemplateException extends Exception
    {
        public TemplateException()
        {
            super();
        }

        public TemplateException(String message)
        {
            super(message);
        }

        public TemplateException(String message, Throwable cause)
        {
            super(message, cause);
        }

        public TemplateException(Throwable cause)
        {
            super(cause);
        }
    }
}
