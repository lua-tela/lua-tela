package com.hk.luatela;

import com.hk.json.Json;
import com.hk.json.JsonObject;
import com.hk.json.JsonValue;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class LuaTelaTest extends Assert
{
	private HttpClient client;

	@Before
	public void setUp()
	{
		client = new HttpClient();
		client.setHostConfiguration(config);
	}

	@Test
	public void testServer() throws IOException
	{
		GetMethod response = new GetMethod();

		assertEquals(200, client.executeMethod(response));

	}

	@Test
	public void testIndex() throws IOException
	{
		GetMethod response = new GetMethod("/");

		assertEquals(200, client.executeMethod(response));

		assertEquals("this is the index, here is my magic number: 70831",
				response.getResponseBodyAsString());
	}

	@Test
	public void testContact() throws IOException
	{
		GetMethod response = new GetMethod("/contact");

		assertEquals(200, client.executeMethod(response));

		String html = "<!DOCTYPE html>\n" +
				"<html>\n" +
				"\t<head>\n" +
				"\t\t<title>Hello World!</title>\n" +
				"\t</head>\n" +
				"\t<body>\n" +
				"\t\tThis is my line, there are many like it but this one is mine.\n" +
				"\t\t<magic number=\"27913\"/>\n" +
				"\t</body>\n" +
				"</html>";
		assertEquals(html, response.getResponseBodyAsString());
	}

	@Test
	public void testFallthrough() throws IOException
	{
		// Assert that when a page does not exist, it attempts to fall
		// to a parent path

		GetMethod response = new GetMethod("/does-not/exist");

		assertEquals(200, client.executeMethod(response));
		assertTrue(response.getResponseBodyAsString().contains("70831"));

		response = new GetMethod("/contact/does-not/exist");

		assertEquals(200, client.executeMethod(response));
		assertTrue(response.getResponseBodyAsString().contains("27913"));
	}

	@Test
	public void testPaths() throws IOException
	{
		GetMethod response = new GetMethod("/johndoe-homepage/contact-me");

		assertEquals(200, client.executeMethod(response));

		JsonObject json = Json.read(response.getResponseBodyAsString()).getObject();

		assertTrue(json.contains("key"));
		assertTrue(json.get("key").isString());
		assertEquals("value", json.getString("key"));

		assertEquals("/johndoe-homepage/contact-me", json.getString("path"));
	}

	public static HostConfiguration config;

	static
	{
		try
		{
			config = new HostConfiguration();
			JsonValue val = Json.read(LuaTelaTest.class.getResource("/host.json"));
			JsonObject obj = val.getObject();

			if(obj.contains("host"))
			{
				String host = obj.getString("host");
				if(obj.contains("port"))
				{
					int port = obj.getInt("port");
					if(obj.contains("protocol"))
						config.setHost(host, port, obj.getString("protocol"));
					else
						config.setHost(host, port);
				}
				else
					config.setHost(host);
			}
			else
				throw new IllegalStateException("No host.json resource to read host from?");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
