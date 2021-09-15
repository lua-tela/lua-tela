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

            StringBuilder lua = new StringBuilder();
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

                        if(c == '{')
                        {
                            // value
                        }
                        else if(c == '[')
                        {
                            // block
                        }
                    }
                }

                lua.append(c);
            }

            source.close();

            System.out.println(lua);
        }

        private void readValue(StringBuilder lua)
        {

        }

        private void readBlock(StringBuilder lua)
        {

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
