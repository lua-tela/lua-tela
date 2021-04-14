package main.luacompat;

import com.hk.file.FileUtil;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import main.context.LuaContext;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemHeaders;

public class SimpleFileItem implements FileItem
{
    private final LuaContext ctx;
    private final String name, content;
    private final File file;
    private final Map<String, String> headers;
    private final FileItemHeaders fihs;
    private final boolean isFormField;
    
    public SimpleFileItem(LuaContext ctx, String name, String content)
    {
        this.ctx = ctx;
        this.name = name;
        this.content = content;
        
        isFormField = true;
        file = null;
        headers = new HashMap<>();
        fihs = headers();
    }
    
    public SimpleFileItem(LuaContext ctx, String name, File file)
    {
        this.ctx = ctx;
        this.name = name;
        this.file = file;
        
        isFormField = false;
        content = null;
        headers = new HashMap<>();
        fihs = headers();
    }
    
    private FileItemHeaders headers()
    {
        return new FileItemHeaders() {
            @Override
            public String getHeader(String string)
            {
                return headers.get(string);
            }

            @Override
            public Iterator<String> getHeaders(String string)
            {
                return Arrays.asList(headers.get(string)).iterator();
            }

            @Override
            public Iterator<String> getHeaderNames()
            {
                return headers.keySet().iterator();
            }
        };
    }
    
    @Override
    public InputStream getInputStream() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType()
    {
        return ctx.getMimeType(file.getName());
    }

    @Override
    public String getName()
    {
        return file.getName();
    }

    @Override
    public boolean isInMemory()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getSize()
    {
        return isFormField ? content.length() : file.length();
    }

    @Override
    public byte[] get()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString(String string) throws UnsupportedEncodingException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getString()
    {
        if(isFormField)
            return FileUtil.getFileContents(file);
        else
            return content;
    }

    @Override
    public void write(File file) throws Exception
    {
        Files.copy(this.file.toPath(), file.toPath());
    }

    @Override
    public void delete()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getFieldName()
    {
        return name;
    }

    @Override
    public void setFieldName(String string)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFormField()
    {
        return isFormField;
    }

    @Override
    public void setFormField(boolean bln)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public FileItemHeaders getHeaders()
    {
        return fihs;
    }

    @Override
    public void setHeaders(FileItemHeaders fih)
    {
        throw new UnsupportedOperationException();
    }
}
