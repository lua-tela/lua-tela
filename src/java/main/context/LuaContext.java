package main.context;

import com.hk.lua.Lua;
import com.hk.lua.LuaInterpreter;
import com.hk.lua.LuaObject;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.db.LuaBase;
import main.luacompat.FileUpload;
import main.routes.RouteException;
import main.routes.Routes;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;

public abstract class LuaContext
{
    public abstract void setAttribute(String name, Object obj);
    
    public abstract Object getAttribute(String name);
    
    public abstract String getParam(String name);
        
    public abstract String getRealPath(String path);

    public abstract String getMimeType(String ext);
    
    public abstract PrintWriter getWriter() throws IOException;
    
    public abstract OutputStream getOutput() throws IOException;
    
    public abstract String getURL();
    
    public abstract String getPath();

    public abstract String getHost();

    public abstract int getPort();

    public abstract String getUser();

    public abstract String getAddress();

    public abstract String getSessID();

    public abstract boolean isNewSess();

    public abstract Object getSess(String name);

    public abstract void setSess(String name, Object value);

    public abstract void removeSess();
    
    public abstract List<FileItem> getFileItems(File uploads) throws FileUploadException;
    
    public abstract void setContentType(String contentType);
    
    public abstract void setContentLength(long contentLength);
    
//    public abstract void setContentEncoding(String enc);
    
    public abstract void setHeader(String title, String content);
    
    public abstract void setStatus(int status);
    
    public abstract void redirect(String url) throws IOException;
    
    public Routes getRoutes(File base) throws RouteException
    {
        Routes rts = null;
        if(getAttribute("routes") != null)
        {
            rts = (Routes) getAttribute("routes");
            if(rts.canDiscard())
                rts = null;
        }
        if(rts == null)
            rts = new Routes(new File(base, "routes.json"));
        
        return rts;
    }
    
    public Connection getConnection() throws IllegalArgumentException, SQLException
    {
        return ((DataSource) getAttribute("datasource")).getConnection();
    }

    public LuaObject getFileTable(LuaInterpreter interp)
    {
        LuaBase db = interp.getExtra("db", LuaBase.class);
        LuaObject tbl = Lua.newTable();
        try
        {
            List<FileItem> lst = getFileItems(new File(db.base, "uploads"));

            long index = 1;
            FileUpload fu;
            Map<String, LuaObject> params = new HashMap<>();
            for(FileItem fi : lst)
            {
                if(fi.isFormField())
                {
                    params.put(fi.getFieldName(), Lua.newString(fi.getString()));
                }
                else if(fi.getSize() != 0)
                {
                    fu = new FileUpload(interp, fi);
                    tbl.setIndex(index++, fu);
                    tbl.setIndex(fi.getFieldName(), fu);
                    params.put(fi.getFieldName(), fu);
                }
            }
            if(index == 1)
                tbl = null;
            else
                interp.setExtra("params", params);
        }
        catch (FileUploadBase.InvalidContentTypeException ex)
        {
            tbl = null;
        }
        catch (FileUploadException ex)
        {
            throw new RuntimeException(ex);
        }
        return tbl;
    }
    
    public void handle(LuaObject obj) throws IOException
    {
        if(obj.isFunction())
        {
            boolean cont;
            long pass = 1;
            do
            {
                LuaObject r = obj.callFunction(getPath(), pass++);
                cont = r.getBoolean();

                if(cont)
                    handle(r);
            } while(cont);
        }
        else if(obj.isTable())
        {
            long len = obj.getLength();

            if(len > 0)
            {
                for(long i = 1; i <= len; i++)
                    handle(obj.getIndex(i));
            }
            else
            {
                handle(obj.getIndex(getPath()));
            }
        }
        else if(obj.isBoolean())
        {
            if(obj.getBoolean())
                getWriter().flush();
        }
        else
        {
            getWriter().write(obj.getString());
        }
    }
}