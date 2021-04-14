package main;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

@WebListener
public class AppListener implements javax.servlet.ServletContextListener
{
    @Override
    public void contextInitialized(ServletContextEvent sce)
    {
        ServletContext ctx = sce.getServletContext();
        
        PoolProperties p = new PoolProperties();
        
        if(ctx.getInitParameter("dataurl") != null && !ctx.getInitParameter("dataurl").trim().isEmpty())
        {
            p.setUrl(ctx.getInitParameter("dataurl"));
        }
        else
        {
            // jdbc:mysql://localhost:3306/testdb?useSSL=false&allowPublicKeyRetrieval=true
            String host = ctx.getInitParameter("datahost");
            String port = ctx.getInitParameter("dataport");
            String base = ctx.getInitParameter("database");
            StringBuilder sb = new StringBuilder(15 + host.length() + port.length() + base.length());
            sb.append("jdbc:mysql://");
            sb.append(host);
            sb.append(':');
            sb.append(port);
            sb.append('/');
            sb.append(base);
            
            String dataext = ctx.getInitParameter("dataext");
            if(dataext != null && !dataext.trim().isEmpty())
                sb.append('?').append(dataext);
            p.setUrl(sb.toString());
        }
        p.setDriverClassName("com.mysql.cj.jdbc.Driver");
        p.setUsername(ctx.getInitParameter("username"));
        p.setPassword(ctx.getInitParameter("password"));
        p.setJmxEnabled(true);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(100);
        p.setInitialSize(10);
        p.setMaxWait(10000);
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(30000);
        p.setMinIdle(10);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState");
        DataSource datasource = new DataSource();
        datasource.setPoolProperties(p);
        
        ctx.setAttribute("datasource", datasource);
        System.out.println("########### START UP ###########");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce)
    {
        System.out.println("########### SHUT DOWN ###########");
    }
}
