package main.context;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;

public class LuaServletContext extends LuaContext
{
    private final ServletContext ctx;
    private final HttpServletRequest req;
    private final HttpServletResponse res;
    private final String url, path;
    
    public LuaServletContext(HttpServletRequest req, HttpServletResponse res)
    {
        this.ctx = req.getServletContext();
        this.req = req;
        this.res = res;
        this.path = req.getRequestURI();
        this.url = req.getRequestURL().substring(0, req.getRequestURL().length() - path.length());
    }

    @Override
    public void setAttribute(String name, Object obj)
    {
        ctx.setAttribute(name, obj);
    }

    @Override
    public Object getAttribute(String name)
    {
        return ctx.getAttribute(name);
    }

    @Override
    public String getParam(String name)
    {
        return req.getParameter(name);
    }

    @Override
    public String getRealPath(String path)
    {
        return ctx.getRealPath(path);
    }

    @Override
    public String getMimeType(String ext)
    {
        return ctx.getMimeType(ext);
    }

    @Override
    public PrintWriter getWriter() throws IOException
    {
        return res.getWriter();
    }

    @Override
    public OutputStream getOutput() throws IOException
    {
        return res.getOutputStream();
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
        return req.getRemoteHost();
    }

    @Override
    public int getPort()
    {
        return req.getRemotePort();
    }

    @Override
    public String getUser()
    {
        return req.getRemoteUser();
    }

    @Override
    public String getAddress()
    {
        return req.getRemoteAddr();
    }

    @Override
    public String getSessID()
    {
        return req.getSession().getId();
    }

    @Override
    public boolean isNewSess()
    {
        return req.getSession().isNew();
    }

    @Override
    public Object getSess(String name)
    {
        return req.getSession().getAttribute(name);
    }

    @Override
    public void setSess(String name, Object value)
    {
        req.getSession().setAttribute(name, value);
    }

    @Override
    public void removeSess()
    {
        req.getSession().invalidate();
    }

    @Override
    public void setContentType(String contentType)
    {
        res.setContentType(contentType);
    }

    @Override
    public void setContentLength(long contentLength)
    {
        res.setContentLengthLong(contentLength);
    }

//    @Override
//    public void setContentEncoding(String enc)
//    {
//        res.setCharacterEncoding(enc);
//    }
    
    @Override
    public void setHeader(String title, String content)
    {
        res.setHeader(title, content);
    }

    @Override
    public void setStatus(int status)
    {
        res.setStatus(status);
    }

    @Override
    public void redirect(String url) throws IOException
    {
        res.sendRedirect(url);
    }

    @Override
    public List<FileItem> getFileItems(File uploads) throws FileUploadException
    {
        uploads.mkdirs();

        DiskFileItemFactory diff = new DiskFileItemFactory();
        diff.setSizeThreshold(500 * 1024); // 500 KB
        diff.setRepository(uploads);

        ServletFileUpload sfu = new ServletFileUpload(diff);
        sfu.setFileSizeMax(500 * 1000 * 1024); // 500 MB

        return sfu.parseRequest(new ServletRequestContext(req));
    }
}
