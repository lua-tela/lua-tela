package test;

import java.io.File;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.context.LuaAppContext;
import main.db.LuaBase;
import main.routes.Routes.Route;

public class TestMain
{
    public static void main(String[] args)
    {
        String path = "index";
        String sessID = "123BOOB123";
        boolean loop = false;
        
        File curr = new File(System.getProperty("user.dir"), "run");
        Scanner in = new Scanner(System.in);
        do
        {
            LuaAppContext ctx = new LuaAppContext(curr, "https://thekayani.com", path, sessID);
            ctx.setDataSource("jdbc:mysql://localhost:3306/testdb?useSSL=false&allowPublicKeyRetrieval=true", "root", "hkrocks1");

            LuaBase base = new LuaBase(ctx, new File(curr, "base"));

            try
            {
                Route route = base.routes.findRoute(ctx.path);

                if(route != null)
                {
                    ctx.setStatus(200);
                    route.serve(base, ctx, "get", ctx.url, ctx.path);
                }
            }
            catch (Exception ex)
            {
                Logger.getLogger(TestMain.class.getName()).log(Level.SEVERE, null, ex);
            }

            System.out.println(ctx.url + ctx.path + " (" + ctx.status + ") [" + ctx.contentType + "]");
            if(ctx.redirectURL != null)
                System.out.println("redirect to " + ctx.redirectURL);
            else
                System.out.println(ctx.toString());
            
            path = loop ? in.nextLine() : null;
        } while(path != null && !path.isEmpty());
        
        in.close();
    }
}