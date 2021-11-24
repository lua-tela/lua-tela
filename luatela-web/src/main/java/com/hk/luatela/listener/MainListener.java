package com.hk.luatela.listener;

import com.hk.luatela.LuaTela;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class MainListener implements ServletContextListener
{
	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		LuaTela luaTela = new LuaTela(event.getServletContext());

		luaTela.collectRoutes(System.out);
		luaTela.output(System.out);

		System.out.println("############## START UP ##############");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event)
	{
		System.out.println("############## SHUT DOWN ##############");
	}
}
