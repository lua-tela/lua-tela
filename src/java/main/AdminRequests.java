package main;

import com.hk.file.FileUtil;
import com.hk.io.IOUtil;
import com.hk.io.StringBuilderWriter;
import com.hk.json.Json;
import com.hk.json.JsonObject;
import com.hk.lua.Lua;
import com.hk.lua.LuaException;
import com.hk.lua.LuaInterpreter;
import com.hk.str.HTMLText;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import main.context.LuaContext;
import main.db.LuaBase;
import main.routes.RouteException;
import main.routes.Routes.Route;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;

class AdminRequests
{
    private final ServletContext context;
    private final LuaContext ctx;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final String method;
    private final String url;
    private final String path;
    private final File base;
    private boolean alreadyDone;
    
    AdminRequests(LuaContext ctx, HttpServletRequest request, HttpServletResponse response, String method)
    {
        this.context = request.getServletContext();
        this.ctx = ctx;
        this.request = request;
        this.response = response;
        this.method = method;
        path = request.getRequestURI();
        url = request.getRequestURL().substring(0, request.getRequestURL().length() - path.length());
        base = new File(context.getRealPath("/base"));
    }
    
    boolean adminRequest() throws Exception
    {   
        response.setContentType("text/html;charset=UTF-8");
        
        HTMLText txt = new HTMLText();
        
        txt.pr("<!DOCTYPE html>");
        txt.open("html");
        txt.open("head");
        txt.prln("<meta name=\"viewport\" content=\"width=device-width, height=device-height, initial-scale=1.0, minimum-scale=1.0\">");
        txt.prln("<link rel=\"stylesheet\" href=\"" + url + "/res/w3.css\">");
        txt.el("script", "", "src", "https://ajax.googleapis.com/ajax/libs/angularjs/1.6.9/angular.min.js");
        txt.pr("<title>");
        txt.makeVar("title", "Administrator");
        txt.wrln("</title>");
        
        txt.prln("<!-- HEAD -->");
        txt.tabs();
        txt.makeVar("headscript");
        txt.ln();
        txt.close("head");
        txt.open("body", "style", "background-color: #FFF; margin: 8px;");
        HttpSession sess = request.getSession();
        if(sess.getAttribute("admin") != null && (Boolean) sess.getAttribute("admin"))
        {
            if(request.getParameter("logout") != null)
            {
                txt.el("script", "window.location.href = \"/admin\"");
                sess.invalidate();
            }
            else
            {
                txt.open("div", "class", "w3-container");
                txt.open("h4", "class", "w3-container w3-bottombar w3-border-blue");
                txt.prln("Administrator Controls");
                txt.el("a", "Logout", "class", "w3-small w3-right", "href", url + "/admin?logout=true");
                txt.el("span", "Hello Admin!", "style", "margin: 0 4px;", "class", "w3-small w3-right");
                txt.close("h4");
                
                LuaBase db = new LuaBase(ctx, base);
               
                if(path.startsWith("/admin/create-page"))
                {
                    createPageRequest(txt, db);
                }
                else if(path.startsWith("/admin/models"))
                {
                    modelsRequest(txt, db);
                }
                else if(path.startsWith("/admin/libs"))
                {
                    librariesRequest(txt, db);
                }
                else if(path.startsWith("/admin/res"))
                {
                    resourcesRequest(txt, db);
                }
                else if(path.startsWith("/admin/edit-page/"))
                {
                    editPageRequest(txt, db);
                }
                else
                {
                    txt.open("div", "class", "w3-panel w3-padding w3-light-blue");
                    txt.el("a", "Create New Page", "class", "w3-white w3-btn", "href", url + "/admin/create-page");
                    txt.el("a", "Lua Libraries", "class", "w3-white w3-btn", "href", url + "/admin/libs");
                    txt.el("a", "Static Resources", "class", "w3-white w3-btn", "href", url + "/admin/res");
                    txt.el("a", "Models", "class", "w3-white w3-btn w3-right", "href", url + "/admin/models");
                    txt.close("div");

                    txt.open("div", "class", "w3-responsive");
                    txt.open("table", "class", "w3-table-all w3-card");
                    txt.open("tr");
                    txt.el("th", "Page URL");
                    txt.el("th", "File Name");
                    txt.el("th", "Actions");
                    txt.close("tr");
                    
                    txt.open("tr");
                    txt.el("td", "<i class=\"w3-small w3-text-gray\">" + url + "/</i>admin");
                    txt.el("td", "<span class=\"w3-text-red\">RESERVED</span>");
                    
                    txt.open("td");
                    txt.el("span", "View", "class", "w3-green w3-btn w3-disabled");
                    txt.el("span", "Edit", "class", "w3-yellow w3-btn w3-disabled");
                    txt.el("span", "Delete", "class", "w3-red w3-btn w3-disabled");
                    txt.close("td");
                    
                    txt.close("tr");
                    
                    List<Route> routes = db.routes.getRoutes();
                    Collections.sort(routes, (Route o1, Route o2) -> o1.path.compareToIgnoreCase(o2.path));
                    
                    String p;
                    for(Route route : routes)
                    {
                        p = route.path.substring(1);
                        txt.open("tr");
                        txt.el("td", "<i class=\"w3-small w3-text-gray\">" + url + "/</i>" + p);
                        txt.el("td", "<span class=\"w3-small\">/base/pages/</span>" + route.source + ".lua");

                        txt.open("td");
                        txt.el("a", "View", "class", "w3-green w3-btn", "href", url + "/" + p);
                        txt.el("a", "Edit", "class", "w3-yellow w3-btn", "href", url + "/admin/edit-page/" + p);
                        txt.el("a", "Delete", "class", "w3-red w3-btn", "href", url + "/admin/edit-page/delete/" + p);
                        txt.close("td");
                        txt.close("tr");
                    }
                    
                    txt.close("table");
                    txt.close("div");
                }
                
                txt.close("div");
                db.close();
            }
        }
        else
        {
            loginRequest(txt, method, request, url + path);
        }
        
        if(!alreadyDone)
        {
            txt.close("body");
            txt.close("html");
            
            PrintWriter writer = response.getWriter();
            writer.print(txt.create());
            writer.close();
        }
        return true;
    }

    static void loginRequest(HTMLText txt, String method, HttpServletRequest request, String redirect) throws NoSuchAlgorithmException
    {
        HttpSession sess = request.getSession();
        
        txt.setVar("title", "Administrator Login");
        
        if(method.equalsIgnoreCase("post"))
        {
            System.out.println("Submitted form...");
            MessageDigest hasher = MessageDigest.getInstance("SHA-1");
            String user = request.getParameter("user");
            String pass = request.getParameter("pass");
//            String date = request.getParameter("date");
            System.out.println("user = " + user);
            System.out.println("pass = " + pass);
//            System.out.println("date = " + date);

//            boolean isUser, isPass, isDate;
//            isUser = isPass = isDate = false;
            boolean isUser, isPass;
            isUser = isPass = false;

            if(user != null && !user.trim().isEmpty())
            {
                String userHash = new BigInteger(1, hasher.digest(user.getBytes())).toString(16).toUpperCase();
                isUser = userHash.equals("12442D2FF021A1645C3D98C4244AC08C8B3DCD77");
            }
            if(pass != null && !pass.trim().isEmpty())
            {
                String passHash = new BigInteger(1, hasher.digest(pass.getBytes())).toString(16).toUpperCase();
                isPass = passHash.equals("CAB655C509CFAFD36BCE98889B358F01CD995228");
            }
//            if(date != null && !date.trim().isEmpty())
//            {
//                String dateHash = new BigInteger(1, hasher.digest(date.getBytes())).toString(16).toUpperCase();
//                isDate = dateHash.equals("BC0D0820AAC3F583DFB11CDE9F4DA39A1C996C76");
//            }

//            if(isUser && isPass && isDate)
            if(isUser && isPass)
            {
                sess.setAttribute("admin", true);
                txt.el("script", "window.location.href = \"" + redirect + "\";");
            }
            else
            {
                txt.el("b", "Invalid Login!", "style", "color: #F00;").br().br();
                txt.el("a", "Back", "href", redirect);
            }
        }
        else
        {
            txt.open("form", "action", redirect, "method", "post");
            txt.el("label", "Username: ");
            txt.el("input", null, "type", "text", "name", "user").br().br();

            txt.el("label", "Password: ");
            txt.el("input", null, "type", "password", "name", "pass").br().br();

//            txt.el("label", "Date of Birth: ");
//            txt.el("input", null, "type", "date", "name", "date").br().br();

            txt.el("input", null, "type", "submit", "value", "Login");
            txt.close("form");
        }
    }

    private void createPageRequest(HTMLText txt, LuaBase db) throws SQLException
    {
        txt.setVar("title", "Administrator - Create Page");
        txt.el("a", "Home", "class", "w3-blue w3-right w3-btn", "href", url + "/admin");

        txt.open("div");
        txt.el("h3", "Create New Page");
        String defURL = "contact";
        String defFile = "Contact";
        String error = null;
        boolean submitted = false;
        if("post".equalsIgnoreCase(method))
        {
            defURL = request.getParameter("url");
            defFile = request.getParameter("file");

            try
            {
                db.routes.addRoute(defURL, defFile);
            }
            catch(RouteException ex)
            {
                error = ex.getLocalizedMessage();
            }

            if(error == null)
            {
                submitted = true;
                
                try
                {
                    File routes = new File(base, "routes.json");
                    JsonObject obj = Json.read(routes).getObject();
                    obj.put("/" + defURL, defFile);
                    Json.writer(routes).unsetSlashEscape().setPrettyPrint().put(obj).close();
                    
                    File src = new File(base, "pages" + File.separator + defFile + ".lua");
                    if(!src.exists())
                        FileUtil.resetFile(src, FileUtil.getFileContents(new File(base, "default.lua")));

                    txt.open("div", "class", "w3-panel w3-green");
                    txt.el("h3", "Success");
                    txt.el("p", "Successsfully created the page at <a href=\"" + url + "/" + defURL + "\">" + url + "/" + defURL + "</a>.");
                    txt.close("div");
                }
                catch (IOException ex)
                {
                    Logger.getLogger(AdminRequests.class.getName()).log(Level.SEVERE, null, ex);
                    txt.open("div", "class", "w3-panel w3-red");
                    txt.el("h3", "An Error Occurred");
                    txt.el("p", "There was an issue creating the record.");
                    txt.close("div");
                }
            }
        }

        if(!submitted)
        {
            if(error != null)
            {
                txt.el("li", error, "class", "w3-padding w3-text-red");
            }

            txt.open("form", "action", url + "/admin/create-page", "method", "post");

            txt.el("label", "Page URL").br();
            txt.el("span", url + "/");
            txt.el("input", null, "class", "w3-border-0 w3-border-bottom", "type", "text", "name", "url", "value", defURL);
            txt.el("span", "/*").br().br();

            txt.el("label", "Source File").br();
            txt.el("input", null, "class", "w3-border-0 w3-border-bottom", "type", "text", "name", "file", "value", defFile, "required", "true");
            txt.el("span", ".lua").br().br();

            txt.el("input", null, "class", "w3-blue w3-btn", "type", "submit");
            txt.close("form");
        }
        txt.close("div");
    }

    private void modelsRequest(HTMLText txt, LuaBase db)
    {
        txt.setVar("title", "Administrator - Models");
        txt.el("a", "Home", "class", "w3-blue w3-right w3-btn", "href", url + "/admin");
        txt.el("h3", "Models");

        if(path.substring(13).startsWith("/edit"))
        {
            txt.setVar("headscript",
                    "<script src=\"" + url + "/res/lib/codemirror.js\"></script>\n" +
                    "\t\t<link rel=\"stylesheet\" href=\"" + url + "/res/lib/codemirror.css\">\n" +
                    "\t\t<script src=\"" + url + "/res/mode/lua/lua.js\"></script>");
            String content;
            if("post".equals(method) && request.getParameter("sv") != null)
            {
                boolean setup = request.getParameter("st") != null;
                boolean registerModels = request.getParameter("rm") != null;
                String code = request.getParameter("sv");
                boolean success = false;
                
                try
                {
                    Lua.reader(code).compile();
                    success = true;
                }
                catch(LuaException ex)
                {
                    txt.open("div", "class", "w3-panel w3-red w3-card");
                    txt.el("h3", "There was an issue compiling the source");
                    txt.setVar("line", ex.getLocalizedMessage().split("\\:")[1]);
                    StringBuilderWriter sbr = new StringBuilderWriter();
                    ex.printStackTrace(new PrintWriter(sbr));
                    txt.el("pre", LuaBase.escapeHTML(sbr.toString()));
                    txt.close("div");
                }
                
                if(success)
                {
                    FileUtil.resetFile(new File(base, "models.lua"), code);
                    
                    LuaInterpreter interp = db.setupInterpreter(ctx, true, setup, registerModels);
                    Object result = interp.execute();
                    txt.el("span", "Execution Complete.");
                    if(result != null)
                        txt.wrln(result.toString());
                }
                
                txt.open("form", "method", "post", "action", url + path);
                txt.el("input", null, "class", "w3-btn w3-yellow", "type", "hidden", "name", "ed", "value", LuaBase.escapeHTML(code));
                txt.el("input", null, "class", "w3-btn w3-yellow", "type", "submit", "value", "Back To Editing");
                txt.close("form");
            }
            else
            {
                if("post".equals(method) && request.getParameter("ed") != null)
                    content = request.getParameter("ed").trim();
                else
                    content = FileUtil.getFileContents(new File(base, "models.lua"));

                txt.open("form", "action", url + path, "method", "post");
                txt.open("div", "class", "w3-border");
                txt.el("textarea", LuaBase.escapeHTML(content), "style", "font-family: monospace; width: 100%; height: 400px;", "id", "cd", "name", "sv");
                txt.close("div");
                txt.open("script");
                txt.prln("var editor = CodeMirror.fromTextArea(document.getElementById('cd'), { lineNumbers: true, styleSelectedText: true });");
                txt.close("script");

                txt.el("label", "When checked, this will enable the <code>_SETUP</code> flag in the Lua code.");
                txt.br();
                txt.el("input", null, "class", "w3-blue w3-check", "type", "checkbox", "name", "st", "value", "true", "checked", "trues");
                txt.el("label", "Enable Setup").br().br();

                txt.el("label", "When checked, this will drop the table and recreate "
                        + "it! <span class=\"w3-text-red\">WARNING: THIS WILL DELETE "
                        + "EVERYTHING IN THE TABLE AND RECREATE IT!</span>");
                txt.br();
                txt.el("input", null, "class", "w3-red w3-check", "type", "checkbox", "name", "rm", "value", "true");
                txt.el("label", "Register Models").br();

                txt.br();
                txt.el("input", null, "class", "w3-blue w3-btn", "type", "submit", "value", "Save and Run models.lua");
                txt.close("form");
            }

            txt.br().br().el("a", "Back To Models", "class", "w3-blue w3-btn", "href", url + "/admin/models");
        }
        else if(path.substring(13).startsWith("/reload"))
        {
            if(path.substring(13).startsWith("/reload/now"))
            {
                db.setupInterpreter(ctx, true, true, true).execute();
                txt.el("script", "window.location.href = '" + url + "';");
            }
            else
            {
                txt.el("p", "This will delete everything within the tables and "
                        + "recreate them! <b>USE WITH EXTREME CAUTION</b>. You "
                        + "will be redirected back to the home page afterwards.");
                txt.el("a", "Continue", "class", "w3-red w3-btn", "href", url + "/admin/models/reload/now");
            }
        }
        else
        {
            txt.el("p", "The database is accessed and controlled using <span class=\"w3-black w3-round\""
                    + " style=\"padding-left: 4px; padding-right: 4px;\">models</span>. These"
                    + " models control the database and allow you to create, read, "
                    + "update, and delete records which is a.k.a. (CRUD):");

            txt.el("a", "Edit Models", "class", "w3-blue w3-right w3-btn", "href", url + "/admin/models/edit");
            Object res = db.setupInterpreter(ctx, true, false, false).execute();
            
            if(res != null)
                txt.wrln(res.toString());
            
            txt.el("b", "Current Models");
            txt.open("ul");
            db.getModels().stream().forEach((model) -> txt.el("li", model.name));
            txt.close("ul");
            
            txt.el("hr", null);
            
            txt.open("div", "class", "w3-panel w3-card w3-padding w3-red");
            txt.el("p", "This will DELETE ALL TABLES AND RECREATE THEM!");
            txt.el("a", "RELOAD ALL TABLES", "class", "w3-btn w3-white", "href", url + "/admin/models/reload");
            txt.close("div");
        }
    }

    private void librariesRequest(HTMLText txt, LuaBase db)
    {
        String pth = path.substring(11);
        txt.setVar("title", "Administrator - Libraries");
        txt.el("a", "Home", "class", "w3-blue w3-right w3-btn", "href", url + "/admin");

        txt.el("h3", "Lua Libraries");
        
        List<File> files = FileUtil.getAllFiles(new File(db.base, "libs"));
        Iterator<File> itr = files.iterator();
        
        while(itr.hasNext())
        {
            if(!itr.next().getName().endsWith(".lua"))
                itr.remove();
        }
        
        int state = 0;
        if(pth.startsWith("-edit/"))
            state = 1;
        else if(pth.startsWith("-upload/"))
            state = 2;
        else if(pth.startsWith("-download/"))
            state = 3;
        else if(pth.startsWith("-edit-new"))
            state = -1;
        else if(pth.startsWith("-upload-new"))
            state = -2;
        
        if(state < 0)
        {
            pth = pth.substring(state == -1 ? 9 : 11);
            txt.el("h4", "New Library");
                        
            if(!pth.isEmpty())
            {
                File file = new File(db.base, "libs" + File.separator + pth.replace("/", File.separator));
                if(file.exists())
                {
                    txt.open("div", "class", "w3-panel w3-red w3-card");
                    txt.el("h3", "This file already exists!");
                    txt.el("a", "Try again", "class", "w3-btn w3-blue", "href", url + path);
                    txt.close("div");
                }
                else
                {
                    switch(state)
                    {
                        case -1:
                            lbEditRequest(txt, db, file);
                            break;
                        case -2:
                            lbUploadRequest(txt, db, file);
                            break;
                    }
                }
            }
            else
            {
                txt.setVar("headscript", "<script>\n"
                        + "\tfunction headTo()\n"
                        + "\t{\n"
                        + "\t\twindow.location.href = '" + url + path + "/' + document.getElementById('nf').value;\n"
                        + "\t\treturn false;\n"
                        + "\t}\n"
                        + "</script>");
                
                txt.open("form", "onsubmit", "return headTo();");

                txt.el("label", "File Path:").br();
                
                txt.open("p", "class", "w3-text-gray");
                txt.el("i", "Examples").br();
                txt.el("code", "colors.lua").br();
                txt.el("code", "im/a/folder/file.lua").br();
                txt.close("p");
                
                txt.el("input", null, "class", "w3-input w3-border", "type", "text", "id", "nf").br().br();
                
                txt.el("input", null, "class", "w3-btn w3-blue", "type", "submit", "value", "Continue");
                txt.close("form");
            }
        }
        else if(files.isEmpty())
        {
            txt.open("div", "class", "w3-margin");
            txt.el("a", "Create Library", "class", "w3-btn w3-blue", "href", url + "/admin/libs-edit-new");
            txt.el("a", "Upload Library", "class", "w3-btn w3-blue", "href", url + "/admin/libs-upload-new");
            txt.close("div");

            txt.open("p");
            txt.el("i", "Seems to be nothing here!");
            txt.close("p");
        }
        else if(pth.isEmpty())
        {
            txt.el("p", "These Lua files will reside within <code>/base/libs"
                    + "</code> to be accessible to all other Lua files and "
                    + "pages.");
            
            txt.open("div", "class", "w3-margin");
            txt.el("a", "Create Library", "class", "w3-btn w3-blue", "href", url + "/admin/libs-edit-new");
            txt.el("a", "Upload Library", "class", "w3-btn w3-blue", "href", url + "/admin/libs-upload-new");
            txt.close("div");
            
            txt.open("table", "class", "w3-table-all");
            txt.open("tr");
            txt.el("th", "Directory");
            txt.el("th", "File Name");
            txt.el("th", "Actions");
            txt.close("tr");
            for(File file : files)
            {
                File dir = file.getParentFile();
                String name = db.base.toPath().relativize(file.toPath()).toString().replace(File.separator, "/");
                txt.open("tr");
                txt.el("td", "<code>" + File.separator + db.base.getParentFile().toPath().relativize(dir.toPath()).toString() + "</code>");
                txt.el("td", "<code>" + file.getName() + "</code>");
                txt.open("td");
                txt.el("a", "Edit", "class", "w3-btn w3-green", "href", url + "/admin/libs-edit/" + name);
                txt.el("a", "Upload", "class", "w3-btn w3-yellow", "href", url + "/admin/libs-upload/" + name);
                txt.el("a", "Download", "class", "w3-btn w3-blue", "href", url + "/admin/libs-download/" + name);
                txt.close("td");
                txt.close("tr");
            }
            txt.close("table");
        }
        else if(state > 0)
        {
            pth = pth.substring(state == 1 ? 6 : state == 2 ? 8 : 10);

            File file = new File(db.base, pth.replace("/", File.separator));
            if(file.exists())
            {
                switch(state)
                {
                    case 1:
                        lbEditRequest(txt, db, file);
                        break;
                    case 2:
                        lbUploadRequest(txt, db, file);
                        break;
                    case 3:
                        lbDownloadRequest(txt, db, file);
                        break;
                }
            }
            else
            {
                txt.open("p");
                txt.el("i", "Seems to be nothing here!");
                txt.close("p");
            }
        }
        else
        {
            try
            {
                response.sendRedirect(url + "/admin/libs");
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }
    
    private void lbEditRequest(HTMLText txt, LuaBase db, File libFile)
    {
        txt.el("h4", "Editing Library: '" + libFile.getName() + "'");
        
        int line = -1;
        if("post".equals(method) && request.getParameter("sv") != null)
        {
            String rawCode = request.getParameter("sv");

            txt.el("p", "In the event the program cannot be compiled, a log of the error will be reported below.");
            txt.open("form", "method", "post", "action", url + path);
            txt.pr("<input class=\"w3-btn w3-yellow\" type=\"hidden\" name=\"ln\" value=\"");
            txt.makeVar("line", "-1");
            txt.wrln("\"/>");
            txt.el("input", null, "class", "w3-btn w3-yellow", "type", "hidden", "name", "ed", "value", LuaBase.escapeHTML(rawCode));
            txt.el("input", null, "class", "w3-btn w3-yellow", "type", "submit", "value", "Back To Editing");
            txt.close("form");

            try
            {
                LuaInterpreter interp = Lua.reader(rawCode);
                interp.compile();
                libFile.getParentFile().mkdirs();
                FileUtil.resetFile(libFile, rawCode);
                
                txt.open("div", "class", "w3-panel w3-green w3-card");
                txt.el("h3", "Success");
                txt.el("p", "The program has been compiled and stored.");
                txt.close("div");
            }
            catch(LuaException ex)
            {
                txt.open("div", "class", "w3-panel w3-red w3-card");
                txt.el("h3", "There was an issue compiling the source");
                txt.setVar("line", ex.getLocalizedMessage().split("\\:")[1]);
                StringBuilderWriter sbr = new StringBuilderWriter();
                ex.printStackTrace(new PrintWriter(sbr));
                txt.el("pre", LuaBase.escapeHTML(sbr.toString()));
                txt.close("div");
            }
        }
        else
        {
            txt.setVar("headscript",
                    "<script src=\"" + url + "/res/lib/codemirror.js\"></script>\n" +
                    "\t\t<link rel=\"stylesheet\" href=\"" + url + "/res/lib/codemirror.css\">\n" +
                    "\t\t<script src=\"" + url + "/res/mode/lua/lua.js\"></script>");
            String content = "";
            if("post".equals(method) && request.getParameter("ed") != null)
            {
                content = request.getParameter("ed").trim();
                line = Integer.parseInt(request.getParameter("ln")) - 1;
            }
            else if(libFile.exists())
                content = FileUtil.getFileContents(libFile);
            
            txt.open("form", "action", url + path, "method", "post");
            txt.open("div", "class", "w3-border");
            txt.el("textarea", LuaBase.escapeHTML(content), "style", "font-family: monospace; width: 100%; height: 400px;", "id", "cd", "name", "sv");
            txt.close("div");
            txt.open("script");
            txt.prln("var editor = CodeMirror.fromTextArea(document.getElementById('cd'), { lineNumbers: true, styleSelectedText: true });");
            if(line >= 0)
            {
                int chend = content.split("\n")[line].length();
                txt.prln("editor.markText({line: " + line + ", ch: 0}, {line: " + line + ", ch: " + chend + "}, {className: 'w3-pale-red'});");
            }
            txt.close("script");
            
            txt.br();
            txt.el("input", null, "class", "w3-blue w3-btn", "type", "submit", "value", "Save");
            txt.close("form");
        }
    }
    
    private void lbUploadRequest(HTMLText txt, LuaBase db, File libFile)
    {
        txt.el("h4", "Upload Library to: '" + libFile.getName() + "'");
        if("post".equals(method) && request.getContentType().contains("multipart/form-data"))
        {
            try
            {
                DiskFileItemFactory diff = new DiskFileItemFactory();
                diff.setSizeThreshold(500 * 1024); // 500 KB
                File uploads = new File(base, "uploads");
                uploads.mkdirs();
                diff.setRepository(uploads);

                ServletFileUpload sfu = new ServletFileUpload(diff);
                sfu.setFileSizeMax(500 * 1000 * 1024); // 500 MB

                FileItemIterator itr = sfu.getItemIterator(request);
                
                while(itr.hasNext())
                {
                    FileItemStream fis = itr.next();
                    
                    if(!fis.isFormField() && "file".equals(fis.getFieldName()))
                    {
                        File tmp = new File(uploads, libFile.getName());
                        InputStream in = fis.openStream();
                        OutputStream out = new FileOutputStream(tmp);
                        IOUtil.copyTo(in, out);
                        out.close();
                        in.close();
                        boolean error = true;
                        
                        LuaInterpreter interp = Lua.reader(tmp);
                        try
                        {
                            libFile.getParentFile().mkdirs();
                            interp.compile();
                            libFile.delete();
                            tmp.renameTo(libFile);
                            error = false;
                        }
                        catch(LuaException ex)
                        {
                            txt.open("div", "class", "w3-panel w3-red w3-card");
                            txt.el("h3", "There was an issue compiling the source");
                            StringBuilderWriter sbr = new StringBuilderWriter();
                            ex.printStackTrace(new PrintWriter(sbr));
                            txt.el("pre", LuaBase.escapeHTML(sbr.toString()));
                            txt.close("div");
                        }
                        catch(Exception ex)
                        {
                            txt.open("div", "class", "w3-panel w3-red w3-card");
                            txt.el("h3", "There was an issue storing the source");
                            txt.el("pre", LuaBase.escapeHTML(ex.getLocalizedMessage()));
                            txt.close("div");
                        }
                        
                        if(!error)
                        {
                            txt.open("div", "class", "w3-panel w3-green w3-card");
                            txt.el("h3", "Success");
                            txt.el("p", "The library has been compiled and stored.");
                            txt.close("div");
                        }
                        tmp.delete();
                    }
                }
            }
            catch (FileUploadException | IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        else
        {
            txt.open("form", "action", url + path, "method", "post", "enctype", "multipart/form-data");
            txt.el("label", "Upload Lua File:");
            txt.el("input", null, "type", "file", "name", "file").br().br();
            txt.el("input", null, "type", "submit", "value", "Upload");
            txt.close("form");
        }
    }
    
    private void lbDownloadRequest(HTMLText txt, LuaBase db, File libFile)
    {
        response.setContentType("text/x-lua");
        
        response.setHeader("Content-disposition", "attachment; filename=\"" + libFile.getName() + "\"");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Expires", "-1");
        
        
        try
        {
            InputStream in = new FileInputStream(libFile);
            OutputStream out = response.getOutputStream();
            IOUtil.copyTo(in, out);
            in.close();
            out.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        alreadyDone = true;
    }

    private void resourcesRequest(HTMLText txt, LuaBase db)
    {
        String pth = path.substring(10);
        txt.setVar("title", "Administrator - Resources");
        txt.el("a", "Home", "class", "w3-blue w3-right w3-btn", "href", url + "/admin");

        txt.el("h3", "View Resources");
        
        List<File> files = FileUtil.getAllFiles(new File(db.base, "res"));
        
        int state = 0;
        if(pth.startsWith("-edit/"))
            state = 1;
        else if(pth.startsWith("-upload/"))
            state = 2;
        else if(pth.startsWith("-download/"))
            state = 3;
        else if(pth.startsWith("-edit-new"))
            state = -1;
        else if(pth.startsWith("-upload-new"))
            state = -2;
        
        if(state < 0)
        {
            pth = pth.substring(state == -1 ? 9 : 11);
            txt.el("h4", "New Resource");
                        
            if(!pth.isEmpty())
            {
                File file = new File(db.base, "res" + File.separator + pth.replace("/", File.separator));
                if(file.exists())
                {
                    txt.open("div", "class", "w3-panel w3-red w3-card");
                    txt.el("h3", "This file already exists!");
                    txt.el("a", "Try again", "class", "w3-btn w3-blue", "href", url + path);
                    txt.close("div");
                }
                else
                {
                    switch(state)
                    {
                        case -1:
                            rsEditRequest(txt, db, file);
                            break;
                        case -2:
                            rsUploadRequest(txt, db, file);
                            break;
                    }
                }
            }
            else
            {
                txt.setVar("headscript", "<script>\n"
                        + "\tfunction headTo()\n"
                        + "\t{\n"
                        + "\t\twindow.location.href = '" + url + path + "/' + document.getElementById('nf').value;\n"
                        + "\t\treturn false;\n"
                        + "\t}\n"
                        + "</script>");
                
                txt.open("form", "onsubmit", "return headTo();");

                txt.el("label", "File Path:").br();
                
                txt.open("p", "class", "w3-text-gray");
                txt.el("i", "Examples").br();
                txt.el("code", "logo.png").br();
                txt.el("code", "im/a/folder/index.js").br();
                txt.close("p");
                
                txt.el("input", null, "class", "w3-input w3-border", "type", "text", "id", "nf").br().br();
                
                txt.el("input", null, "class", "w3-btn w3-blue", "type", "submit", "value", "Continue");
                txt.close("form");
            }
        }
        else if(files.isEmpty())
        {
            txt.open("div", "class", "w3-margin");
            txt.el("a", "Create Resource", "class", "w3-btn w3-blue", "href", url + "/admin/res-edit-new");
            txt.el("a", "Upload Resource", "class", "w3-btn w3-blue", "href", url + "/admin/res-upload-new");
            txt.close("div");

            txt.open("p");
            txt.el("i", "Seems to be nothing here!");
            txt.close("p");
        }
        else if(pth.isEmpty())
        {
            txt.el("p", "Resources include JavaScript files, media files"
                    + ", CSS files, and possibly any other files that "
                    + "can be included in the HTML and can be accessed "
                    + "using a HTTP connection. In Lua, you are able "
                    + "to shorthand assets using their name instead of "
                    + "their fully qualified names.");
            
            txt.open("div", "class", "w3-margin");
            txt.el("a", "Create Resource", "class", "w3-btn w3-blue", "href", url + "/admin/res-edit-new");
            txt.el("a", "Upload Resource", "class", "w3-btn w3-blue", "href", url + "/admin/res-upload-new");
            txt.close("div");
            
            txt.open("table", "class", "w3-table-all");
            txt.open("tr");
            txt.el("th", "Directory");
            txt.el("th", "File Name");
            txt.el("th", "Actions");
            txt.close("tr");
            for(File file : files)
            {
                File dir = file.getParentFile();
                String name = db.base.toPath().relativize(file.toPath()).toString().replace(File.separator, "/");
                txt.open("tr");
                txt.el("td", "<code>" + File.separator + db.base.getParentFile().toPath().relativize(dir.toPath()).toString() + "</code>");
                txt.el("td", "<code>" + file.getName() + "</code>");
                txt.open("td");
                txt.el("a", "Edit", "class", "w3-btn w3-green", "href", url + "/admin/res-edit/" + name);
                txt.el("a", "Upload", "class", "w3-btn w3-yellow", "href", url + "/admin/res-upload/" + name);
                txt.el("a", "Download", "class", "w3-btn w3-blue", "href", url + "/admin/res-download/" + name);
                txt.close("td");
                txt.close("tr");
            }
            txt.close("table");
        }
        else if(state > 0)
        {
            pth = pth.substring(state == 1 ? 6 : state == 2 ? 8 : 10);

            File file = new File(db.base, pth.replace("/", File.separator));
            if(file.exists())
            {
                switch(state)
                {
                    case 1:
                        rsEditRequest(txt, db, file);
                        break;
                    case 2:
                        rsUploadRequest(txt, db, file);
                        break;
                    case 3:
                        rsDownloadRequest(txt, db, file);
                        break;
                }
            }
            else
            {
                txt.open("p");
                txt.el("i", "Seems to be nothing here!");
                txt.close("p");
            }
        }
        else
        {
            try
            {
                response.sendRedirect(url + "/admin/res");
            }
            catch (IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }

    private void rsEditRequest(HTMLText txt, LuaBase db, File resFile)
    {
        txt.el("h4", "Editing Resource: '" + resFile.getName() + "'");
        
        if("post".equals(method) && request.getParameter("sv") != null)
        {
            String rawContents = request.getParameter("sv");

            txt.el("p", "In the event the program cannot be compiled, a log of the error will be reported below.");
            txt.open("form", "method", "post", "action", url + path);
            txt.el("input", null, "type", "hidden", "name", "ed", "value", LuaBase.escapeHTML(rawContents));
            txt.el("input", null, "class", "w3-btn w3-yellow", "type", "submit", "value", "Back To Editing");
            txt.close("form");

            try
            {
                resFile.getParentFile().mkdirs();
                FileUtil.resetFile(resFile, rawContents);
                
                txt.open("div", "class", "w3-panel w3-green w3-card");
                txt.el("h3", "Success");
                txt.el("p", "The resource has been stored.");
                txt.close("div");
            }
            catch(Exception ex)
            {
                txt.open("div", "class", "w3-panel w3-red w3-card");
                txt.el("h3", "There was an issue storing the resource");
                txt.el("pre", LuaBase.escapeHTML(ex.getLocalizedMessage()));
                txt.close("div");
            }
        }
        else
        {
            txt.setVar("headscript",
                    "<script src=\"" + url + "/res/lib/codemirror.js\"></script>\n" +
                    "\t\t<link rel=\"stylesheet\" href=\"" + url + "/res/lib/codemirror.css\">");
            String content = "";
            if("post".equals(method) && request.getParameter("ed") != null)
            {
                content = request.getParameter("ed").trim();
            }
            else if(resFile.exists())
                content = FileUtil.getFileContents(resFile);
            
            txt.open("form", "action", url + path, "method", "post");
            txt.open("div", "class", "w3-border");
            txt.el("textarea", LuaBase.escapeHTML(content), "style", "font-family: monospace; width: 100%; height: 400px;", "id", "cd", "name", "sv");
            txt.close("div");
            
            txt.br();
            txt.el("input", null, "class", "w3-blue w3-btn", "type", "submit", "value", "Save");
            txt.close("form");
        }
    }
    
    private void rsUploadRequest(HTMLText txt, LuaBase db, File resFile)
    {
        txt.el("h4", "Upload Resource to: '" + resFile.getName() + "'");
        if("post".equals(method) && request.getContentType().contains("multipart/form-data"))
        {
            try
            {
                DiskFileItemFactory diff = new DiskFileItemFactory();
                diff.setSizeThreshold(500 * 1024); // 500 KB
                File uploads = new File(base, "uploads");
                uploads.mkdirs();
                diff.setRepository(uploads);

                ServletFileUpload sfu = new ServletFileUpload(diff);
                sfu.setFileSizeMax(500 * 1000 * 1024); // 500 MB

                FileItemIterator itr = sfu.getItemIterator(request);
                
                while(itr.hasNext())
                {
                    FileItemStream fis = itr.next();
                    
                    if(!fis.isFormField() && "file".equals(fis.getFieldName()))
                    {
                        File tmp = new File(uploads, resFile.getName());
                        InputStream in = fis.openStream();
                        OutputStream out = new FileOutputStream(tmp);
                        IOUtil.copyTo(in, out);
                        out.close();
                        in.close();
                        boolean error = true;
                        
                        try
                        {
                            resFile.getParentFile().mkdirs();
                            resFile.delete();
                            tmp.renameTo(resFile);
                            error = false;
                        }
                        catch(Exception ex)
                        {
                            txt.open("div", "class", "w3-panel w3-red w3-card");
                            txt.el("h3", "There was an issue storing the resource");
                            txt.el("pre", LuaBase.escapeHTML(ex.getLocalizedMessage()));
                            txt.close("div");
                        }
                        
                        if(!error)
                        {
                            txt.open("div", "class", "w3-panel w3-green w3-card");
                            txt.el("h3", "Success");
                            txt.el("p", "The resource has been stored.");
                            txt.close("div");
                        }
                        tmp.delete();
                    }
                }
            }
            catch (FileUploadException | IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        else
        {
            txt.open("form", "action", url + path, "method", "post", "enctype", "multipart/form-data");
            txt.el("label", "Upload Resource File:");
            txt.el("input", null, "type", "file", "name", "file").br().br();
            txt.el("input", null, "type", "submit", "value", "Upload");
            txt.close("form");
        }
    }
    
    private void rsDownloadRequest(HTMLText txt, LuaBase db, File file)
    {
        response.setContentType(context.getMimeType(file.getPath()));
        
        response.setHeader("Content-disposition", "attachment; filename=\"" + file.getName() + "\"");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Expires", "-1");
        
        
        try
        {
            InputStream in = new FileInputStream(file);
            OutputStream out = response.getOutputStream();
            IOUtil.copyTo(in, out);
            in.close();
            out.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        alreadyDone = true;
    }
    
    private void editPageRequest(HTMLText txt, LuaBase db) throws SQLException
    {
        txt.setVar("title", "Administrator - Edit Page");
        txt.el("a", "Home", "class", "w3-blue w3-right w3-btn", "href", url + "/admin");

        String newPath = path.substring(17);
        boolean source = newPath.startsWith("source/");
        boolean upload = newPath.startsWith("upload/");
        boolean delete = newPath.startsWith("delete/");
        boolean download = newPath.startsWith("download/");
        if(source || upload || download || delete)
            newPath = newPath.substring(download ? 9 : 7);
        Route route = db.routes.getRoute('/' + newPath);

        if(route == null)
        {
            txt.el("h2", "Edit Page '" + newPath + "'");
            txt.open("div", "class", "w3-panel w3-red");
            txt.el("h3", "Page Not Found");
            txt.el("p", "Can't find the registered page for the '" + newPath + "' path.");
            txt.close("div");
        }
        else
        {
            txt.el("h2", newPath.isEmpty() ? "Edit Index Page" : "Edit Page '" + newPath + "'");
            if(source)
            {
                try
                {
                    epSource(txt, db, route.source, newPath);
                }
                catch(IOException e)
                {
                    txt.open("div", "class", "w3-padding w3-margin w3-red");
                    txt.el("h3", "Error reading source");
                    StackTraceElement[] arr = e.getStackTrace();
                    txt.open("p");
                    for(StackTraceElement ste : arr)
                    {
                        txt.prln(ste.toString());
                    }
                    txt.close("p");
                    txt.close("div");
                }
            }
            else if(upload)
            {
                epUpload(txt, db, route.source);
            }
            else if(download)
            {
                epDownload(txt, db, route.source);
            }
            else if(delete)
            {
                epDelete(txt, db, route.source);
            }
            else
            {
                txt.open("div", "class", "w3-container", "style", "padding-bottom: 24px;");
                txt.open("div", "class", "w3-half");
                txt.el("p", "Here you will be able to control the "
                        + "page details and edit the contents of the "
                        + "page. You have the choice to upload the "
                        + "source file, or to edit the source file "
                        + "directly on the site.");

                txt.el("a", "Upload Source", "class", "w3-blue w3-btn", "href", url + "/admin/edit-page/upload/" + newPath);
                txt.el("a", "Download Source", "class", "w3-blue w3-btn", "href", url + "/admin/edit-page/download/" + newPath);
                txt.el("a", "Edit Source", "class", "w3-blue w3-btn", "href", url + "/admin/edit-page/source/" + newPath);

                txt.close("div");
                txt.close("div");
            }
        }
    }

    private void epSource(HTMLText txt, LuaBase db, String fileName, String newPath) throws IOException
    {
        txt.setVar("title", "Administrator - Edit Page Source");
        File src = new File(base, "pages" + File.separator + fileName + ".lua");
        int line = -1;
        if("post".equals(method) && request.getParameter("sv") != null)
        {
            String rawCode = request.getParameter("sv");

            txt.el("p", "In the event the program cannot be compiled, a log of the error will be reported below.");
            txt.open("form", "method", "post", "action", url + path);
            txt.pr("<input class=\"w3-btn w3-yellow\" type=\"hidden\" name=\"ln\" value=\"");
            txt.makeVar("line", "-1");
            txt.wrln("\"/>");
            txt.el("input", null, "class", "w3-btn w3-yellow", "type", "hidden", "name", "ed", "value", LuaBase.escapeHTML(rawCode));
            txt.el("input", null, "class", "w3-btn w3-yellow", "type", "submit", "value", "Back To Editing");
            txt.close("form");

            try
            {
                LuaInterpreter interp = Lua.reader(rawCode);
                interp.compile();
                FileUtil.resetFile(src, rawCode);
                
                txt.open("div", "class", "w3-panel w3-green w3-card");
                txt.el("h3", "Success");
                txt.el("p", "The program has been compiled and stored.");
                txt.close("div");
            }
            catch(LuaException ex)
            {
                txt.open("div", "class", "w3-panel w3-red w3-card");
                txt.el("h3", "There was an issue compiling the source");
                txt.setVar("line", ex.getLocalizedMessage().split("\\:")[1]);
                StringBuilderWriter sbr = new StringBuilderWriter();
                ex.printStackTrace(new PrintWriter(sbr));
                txt.el("pre", LuaBase.escapeHTML(sbr.toString()));
                txt.close("div");
            }
        }
        else
        {
            txt.setVar("headscript",
                    "<script src=\"" + url + "/res/lib/codemirror.js\"></script>\n" +
                    "\t\t<link rel=\"stylesheet\" href=\"" + url + "/res/lib/codemirror.css\">\n" +
                    "\t\t<script src=\"" + url + "/res/mode/lua/lua.js\"></script>");
            String content;
            if("post".equals(method) && request.getParameter("ed") != null)
            {
                content = request.getParameter("ed").trim();
                line = Integer.parseInt(request.getParameter("ln")) - 1;
            }
            else if(src.exists())
            {
                content = new String(Files.readAllBytes(src.toPath()));
            }
            else
            {
                txt.open("div", "class", "w3-panel w3-yellow w3-card");
                txt.el("h3", "There is a problem fetching the source");
                txt.el("p", "Showing default text, should you choose to save it.");
                txt.close("div");
                content = FileUtil.getFileContents(new File(base, "default.lua"));
            }
            txt.open("form", "action", url + path, "method", "post");
            txt.open("div", "class", "w3-border");
            txt.el("textarea", LuaBase.escapeHTML(content), "style", "font-family: monospace; width: 100%; height: 400px;", "id", "cd", "name", "sv");
            txt.close("div");
            txt.open("script");
            txt.prln("var editor = CodeMirror.fromTextArea(document.getElementById('cd'), { lineNumbers: true, styleSelectedText: true });");
            if(line >= 0)
            {
                int chend = content.split("\n")[line].length();
                txt.prln("editor.markText({line: " + line + ", ch: 0}, {line: " + line + ", ch: " + chend + "}, {className: 'w3-pale-red'});");
            }
            txt.close("script");
            
            txt.br();
            txt.el("input", null, "class", "w3-blue w3-btn", "type", "submit", "value", "Save");
            txt.close("form");
        }
    }
    
    private void epUpload(HTMLText txt, LuaBase db, String fileName)
    {
        txt.setVar("title", "Administrator - Upload Page Source");
        if("post".equals(method))
        {
            try
            {
                DiskFileItemFactory diff = new DiskFileItemFactory();
                diff.setSizeThreshold(500 * 1024); // 500 KB
                File src = new File(base, "pages" + File.separator + fileName + ".lua");
                File uploads = new File(base, "uploads");
                uploads.mkdirs();
                diff.setRepository(uploads);

                ServletFileUpload sfu = new ServletFileUpload(diff);
                sfu.setFileSizeMax(500 * 1000 * 1024); // 500 MB

                FileItemIterator itr = sfu.getItemIterator(request);
                
                while(itr.hasNext())
                {
                    FileItemStream fis = itr.next();
                    
                    if(!fis.isFormField() && "file".equals(fis.getFieldName()))
                    {
                        File tmp = new File(uploads, fileName + ".lua");
                        InputStream in = fis.openStream();
                        OutputStream out = new FileOutputStream(tmp);
                        IOUtil.copyTo(in, out);
                        out.close();
                        in.close();
                        boolean error = true;
                        
                        LuaInterpreter interp = Lua.reader(tmp);
                        try
                        {
                            interp.compile();
                            src.delete();
                            tmp.renameTo(src);
                            error = false;
                        }
                        catch(LuaException ex)
                        {
                            txt.open("div", "class", "w3-panel w3-red w3-card");
                            txt.el("h3", "There was an issue compiling the source");
                            StringBuilderWriter sbr = new StringBuilderWriter();
                            ex.printStackTrace(new PrintWriter(sbr));
                            txt.el("pre", LuaBase.escapeHTML(sbr.toString()));
                            txt.close("div");
                        }
                        catch(Exception ex)
                        {
                            txt.open("div", "class", "w3-panel w3-red w3-card");
                            txt.el("h3", "There was an issue storing the source");
                            txt.el("pre", LuaBase.escapeHTML(ex.getLocalizedMessage()));
                            txt.close("div");
                        }
                        
                        if(!error)
                        {
                            txt.open("div", "class", "w3-panel w3-green w3-card");
                            txt.el("h3", "Success");
                            txt.el("p", "The program has been compiled and stored.");
                            txt.close("div");
                        }
                        tmp.delete();
                    }
                }
            }
            catch (FileUploadException | IOException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        else
        {
            txt.open("form", "action", url + path, "method", "post", "enctype", "multipart/form-data");
            txt.el("p", "You can upload a file directly to 'base/pages/" + fileName + ".lua");
            txt.el("label", "Upload Lua File:");
            txt.el("input", null, "type", "file", "name", "file").br().br();
            txt.el("input", null, "type", "submit", "value", "Upload");
            txt.close("form");
        }
    }
    
    private void epDownload(HTMLText txt, LuaBase db, String fileName)
    {
        response.setContentType("text/x-lua");
        
        response.setHeader("Content-disposition", "attachment; filename=\"" + fileName + ".lua\"");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Expires", "-1");
        
        File src = new File(base, "pages" + File.separator + fileName + ".lua");
        
        try
        {
            InputStream in = new FileInputStream(src);
            OutputStream out = response.getOutputStream();
            IOUtil.copyTo(in, out);
            in.close();
            out.close();
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        alreadyDone = true;
    }
    
    private void epDelete(HTMLText txt, LuaBase db, String fileName)
    {
        txt.setVar("title", "Administrator - Delete Page");
        if("post".equals(method))
        {
            String cf = request.getParameter("cf");
            if(cf != null && cf.equals("delete"))
            {
                try
                {
                    
                    
                    txt.el("i", "Succssfully deleted!", "class", "w3-large w3-text-green");
                }
                catch(Exception ex)
                {
                    txt.open("div", "class", "w3-large w3-text-red");
                    txt.el("h3", "There was an issue delete the page");
                    txt.el("span", "We encountered an error while attempting to"
                            + " delete this page.").br();
                    txt.el("span", LuaBase.escapeHTML(ex.getLocalizedMessage())).br();
                    txt.close("div");
                }
            }
            else
                txt.el("span", "Cannot delete, invalid confirmation");
        }
        else
        {
            txt.open("div", "class", "w3-panel w3-red");
            txt.open("form", "action", url + path, "method", "post");
            txt.el("p", "Enter the word 'delete' to confirm deletion of the page");
            txt.el("input", null, "class", "w3-input", "type", "text", "name", "cf").br();
            txt.el("input", null, "class", "w3-blue w3-btn", "type", "submit");
            txt.close("form").br();
            txt.close("div");
        }
    }
    
    private void exportException(HTMLText txt, String title, Exception ex)
    {
        txt.open("div", "class", "w3-padding w3-margin w3-red");
        txt.el("h3", title);
        StackTraceElement[] arr = ex.getStackTrace();
        txt.open("p");
        txt.el("b", ex.getMessage()).br();
        for(StackTraceElement ste : arr)
        {
            txt.pr(ste.toString().trim()).br();
        }
        txt.close("p");
        txt.close("div");
    }
}