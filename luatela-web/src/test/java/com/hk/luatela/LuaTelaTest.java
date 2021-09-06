package com.hk.luatela;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class LuaTelaTest
{
	private HttpClient client;

	@Before
	public void setUp()
	{
		client = new HttpClient();
	}

	@Test
	public void doStuff()
	{
		try
		{
			GetMethod getMethod = new GetMethod("http://localhost:8080/contact");
			int resultCode = client.executeMethod(getMethod);
			System.out.println(getMethod);
			System.out.println("resultCode = " + resultCode);
			System.out.println("result:");
			System.out.println(getMethod.getResponseBodyAsString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
