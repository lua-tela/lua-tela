package main.context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import main.luacompat.SimpleFileItem;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

public class LuaAppContext extends LuaContext
{
    public final File directory;
    public final String url, path, sessionID;
    public final Map<String, Object> session;
    public final Map<String, String> params, headers;
    public final ByteArrayOutputStream out;
    public final PrintWriter writer;
    public final boolean isNewSession;
    public final Map<String, File> uploads;
    public String remoteHost, remoteUser, remoteAddress, contentType, redirectURL;
    public int remotePort, status;
    public long contentLength;
    private String dbURL, dbUser, dbPass;
    
    public LuaAppContext(File directory, String url, String path, String sessionID)
    {
        this.directory = directory;
        this.url = url;
        this.path = path.startsWith("/") ? path : "/" + path;
        this.sessionID = sessionID;
        
        if(isNewSession = !sessions.containsKey(sessionID))
            sessions.put(sessionID, session = new HashMap<>());
        else
            session = sessions.get(sessionID);
        params = new HashMap<>();
        headers = new HashMap<>();

        out = new ByteArrayOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
        uploads = new HashMap<>();
        
        remoteHost = remoteUser = remoteAddress = "";
        remotePort = 80;
        contentType = "text/html";
        contentLength = 0;
        status = 404;
    }
    
    public void setDataSource(String dbURL, String dbUser, String dbPass)
    {
        this.dbURL = dbURL;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
    }

    @Override
    public void setAttribute(String name, Object obj)
    {
        attrs.put(name, obj);
    }

    @Override
    public Object getAttribute(String name)
    {
        return attrs.get(name);
    }

    @Override
    public String getParam(String name)
    {
        return params.get(name);
    }

    @Override
    public String getRealPath(String path)
    {
        return new File(directory, path).getAbsolutePath();
    }

    @Override
    public String getMimeType(String ext)
    {
        try
        {
            return Files.probeContentType(Paths.get(ext));
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        return writer;
    }

    @Override
    public OutputStream getOutput() throws IOException
    {
        return out;
    }

    @Override
    public String getURL()
    {
        return url;
    }

    @Override
    public String getPath()
    {
        return path;
    }

    @Override
    public String getHost()
    {
        return remoteHost;
    }

    @Override
    public int getPort()
    {
        return remotePort;
    }

    @Override
    public String getUser()
    {
        return remoteUser;
    }

    @Override
    public String getAddress()
    {
        return remoteAddress;
    }

    @Override
    public String getSessID()
    {
        return sessionID;
    }

    @Override
    public boolean isNewSess()
    {
        return isNewSession;
    }

    @Override
    public Object getSess(String name)
    {
        return session.get(name);
    }

    @Override
    public void setSess(String name, Object value)
    {
        session.put(name, value);
    }

    @Override
    public void removeSess()
    {
        session.clear();
        sessions.remove(sessionID);
    }
    
    @Override
    public List<FileItem> getFileItems(File uploads) throws FileUploadException
    {
        FileItem[] fis = new FileItem[this.uploads.size() + params.size()];
        
        int count = 0;
        for(Map.Entry<String, File> ent : this.uploads.entrySet())
            fis[count++] = new SimpleFileItem(this, ent.getKey(), ent.getValue());
        
        for(Map.Entry<String, String> ent : params.entrySet())
            fis[count++] = new SimpleFileItem(this, ent.getKey(), ent.getValue());
        
        return Arrays.asList(fis);
    }

    @Override
    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    @Override
    public void setContentLength(long contentLength)
    {
        this.contentLength = contentLength;
    }

//    @Override
//    public void setContentEncoding(String encoding)
//    {
//        this.encoding = encoding;
//    }
    
    @Override
    public void setHeader(String title, String content)
    {
        headers.put(title, content);
    }

    @Override
    public void setStatus(int status)
    {
        this.status = status;
    }

    @Override
    public void redirect(String redirectURL) throws IOException
    {
        this.redirectURL = redirectURL;
    }
    
    public byte[] toByteArray()
    {
        return out.toByteArray();
    }
    
    @Override
    public String toString()
    {
        try
        {
            return out.toString("UTF-8");
        }
        catch (UnsupportedEncodingException ex)
        {
            throw new Error(ex);
        }
    }

    @Override
    public Connection getConnection() throws IllegalArgumentException, SQLException
    {
        return DriverManager.getConnection(dbURL, dbUser, dbPass);
    }
    
    
    
    public static final Map<String, Map<String, Object>> sessions = new HashMap<>();
    public static final Map<String, Object> attrs = new HashMap<>();
}
