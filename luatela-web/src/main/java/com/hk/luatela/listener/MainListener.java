package com.hk.luatela.listener;

import com.hk.luatela.LuaTela;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MainListener implements ServletContextListener
{
	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		LuaTela luaTela = new LuaTela(event.getServletContext());

		luaTela.initialize(System.out);

		System.out.println("############## START UP ##############");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event)
	{
		ServletContext ctx = event.getServletContext();
		LuaTela luaTela = (LuaTela) ctx.getAttribute(LuaTela.QUALIKEY);

		luaTela.shutdown();

		System.out.println("############## SHUT DOWN ##############");
	}
}
