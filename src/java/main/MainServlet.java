package main;

import com.hk.str.HTMLText;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import main.context.LuaServletContext;
import main.db.LuaBase;
import main.routes.Routes.Route;

public class MainServlet extends HttpServlet
{
    protected void notFoundRequest(HttpServletRequest request, HttpServletResponse response, String method) throws Exception
    {
        String path = request.getRequestURI();
        String url = request.getRequestURL().substring(0, request.getRequestURL().length() - path.length());
        
        response.setContentType("text/html;charset=UTF-8");
        
        HTMLText txt = new HTMLText();
        
        txt.pr("<!DOCTYPE html>");
        txt.open("html");
        txt.open("head");
        txt.prln("<meta name=\"viewport\" content=\"width=device-width, height=device-height, initial-scale=1.0, minimum-scale=1.0\">");
        txt.prln("<link rel=\"stylesheet\" href=\"/res/w3.css\">");
        txt.el("title", "Not Found");
        txt.close("head");
        txt.open("body");
        txt.open("div", "class", "w3-padding w3-margin w3-red");
        txt.el("h3", "Not Found");
        txt.el("span", url + path);
        txt.close("div");
        txt.close("body");
        txt.close("html");

        PrintWriter writer = response.getWriter();
        writer.print(txt.create());
        writer.close();
    }
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response, String method) throws Exception
    {
        LuaServletContext ctx = new LuaServletContext(request, response);
        String path = ctx.getPath();
        String url = ctx.getURL();
        
        if(path.startsWith("/admin"))
        {
            AdminRequests req = new AdminRequests(ctx, request, response, method);
            if(!req.adminRequest())
            {
                notFoundRequest(request, response, method);
            }
        }
        else if(path.equals("/favicon.ico"))
        {
            ResServlet.serveFile(ctx, request.getServletContext().getRealPath("/base/res/favicon.ico"));
        }
        else
        {
            response.setContentType("text/html;charset=UTF-8");
            File base = new File(getServletContext().getRealPath("/base"));
            LuaBase db = new LuaBase(ctx, base);

            Route route = db.routes.findRoute(path);
            
            if(route != null)
            {
//              https://computingforgeeks.com/tomcat-7-with-letsencrypt-ssl-certificate/
//              https://computingforgeeks.com/bash-script-to-auto-renew-letsencrypt-ssl-certificate-on-tomcat/
                route.serve(db, ctx, method, url, path);
                
                // HTMLText txt = new HTMLText();
                // txt.pr("<!DOCTYPE html>");
                // txt.open("html");
                // txt.open("head");
                // txt.prln("<meta name=\"viewport\" content=\"width=device-width, height=device-height, initial-scale=1.0, minimum-scale=1.0\">");
                // txt.pr("<title>");
                // txt.makeVar("title");
                // txt.wrln("</title>");
                //
                // txt.prln("<!-- HEAD -->");
                // txt.tabs();
                // txt.makeVar("headscript");
                // txt.ln();
                // txt.close("head");
                // txt.open("body", "style", "background-color: #FFF; margin: 8px;");
                // AdminRequests.loginRequest(txt, method, request, url + path);
                // txt.close("body");
                // txt.close("html");
                // 
                // PrintWriter writer = response.getWriter();
                // writer.print(txt.create());
                // writer.close();
            }
            else
                notFoundRequest(request, response, method);

            db.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            processRequest(request, response, "get");
        }
        catch (Exception ex)
        {
            response.setContentType("text/plain;charset=UTF-8");
            ex.printStackTrace(response.getWriter());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        try
        {
            processRequest(request, response, "post");
        }
        catch (Exception ex)
        {
            response.setContentType("text/plain;charset=UTF-8");
            ex.printStackTrace(response.getWriter());
        }
    }
    
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }
}
