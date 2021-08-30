package com.hk.luatela.listener;

import com.hk.luatela.LuaTela;
import com.hk.luatela.servlet.ResourceServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class MainListener implements ServletContextListener
{
	@Override
	public void contextInitialized(ServletContextEvent event)
	{
		LuaTela luaTela = new LuaTela(event.getServletContext());

		System.out.println("Using Data-Root: \"" + luaTela.dataRoot + "\"");
		System.out.println("Using Resource-Root: \"" + luaTela.resourceRoot + "\"");
		System.out.println("Using Resource-Path: \"" + luaTela.resourcePath + "\"");

		System.out.println("############## START UP ##############");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event)
	{
		System.out.println("############## SHUT DOWN ##############");
	}
}
