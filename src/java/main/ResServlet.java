package main;

import com.hk.io.IOUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import main.context.LuaContext;
import main.context.LuaServletContext;

public class ResServlet  extends HttpServlet
{
    public static void serveFile(LuaContext ctx, String file) throws ServletException, IOException
    {
        serveFile(ctx, file, null);
    }
    
    public static void serveFile(LuaContext ctx, String file, String mime) throws ServletException, IOException
    {
        Path pth = Paths.get(file);
        
        if(!Files.exists(pth))
        {
            ctx.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        if(mime == null)
            mime = ctx.getMimeType(file);
        
        ctx.setContentLength(Files.size(pth));
        
        if(mime != null)
            ctx.setContentType(mime);
        
        InputStream in = Files.newInputStream(pth);
        OutputStream out = ctx.getOutput();
        IOUtil.copyTo(in, out);
        out.close();
        in.close();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        ServletContext sc = getServletContext();
        String path = "/base/" + request.getRequestURI().substring(request.getContextPath().length(), request.getRequestURI().length());
        serveFile(new LuaServletContext(request, response), sc.getRealPath(path));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
    
    @Override
    public String getServletInfo()
    {
        return "Resource Servlet";
    }
}
