package com.hk.luatela;

import com.hk.json.Json;
import com.hk.json.JsonObject;
import com.hk.json.JsonValue;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Objects;

public class LuaTelaTest extends Assert
{
	private HttpClient client;

	@Before
	public void setUp()
	{
		client = HttpClients.createDefault();
	}

	@Test
	public void testServer() throws IOException
	{
		HttpGet request = new HttpGet();

		HttpResponse response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
	}

	@Test
	public void testIndex() throws IOException
	{
		HttpGet request = new HttpGet("/");

		HttpResponse response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		String content = EntityUtils.toString(response.getEntity());
		assertEquals("this is the index, here is my magic number: 70831", content);
	}

	@Test
	public void testContact() throws IOException
	{
		HttpGet request = new HttpGet("/contact");

		HttpResponse response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

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
		assertEquals(html, EntityUtils.toString(response.getEntity()));
	}

	@Test
	public void testFallthrough() throws IOException
	{
		// Assert that when a page does not exist, it attempts to fall
		// to a parent path

		HttpGet request = new HttpGet("/does-not/exist");
		HttpResponse response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		assertTrue(EntityUtils.toString(response.getEntity()).contains("70831"));

		request = new HttpGet("/contact/does-not/exist");

		response = client.execute(LuaTelaTest.config, request);
		assertEquals(200, response.getStatusLine().getStatusCode());
	}

	@Test
	public void testPaths() throws IOException
	{
		HttpGet request = new HttpGet("/johndoe-homepage/contact-me");

		HttpResponse response = client.execute(LuaTelaTest.config, request);
		assertEquals(200, response.getStatusLine().getStatusCode());

		JsonObject json = Json.read(response.getEntity().getContent()).getObject();

		assertTrue(json.contains("key"));
		assertTrue(json.get("key").isString());
		assertEquals("value", json.getString("key"));

		assertEquals("/johndoe-homepage/contact-me", json.getString("path"));
	}

	public static HttpHost config;

	static
	{
		try
		{
			JsonValue val = Json.read(Objects.requireNonNull(LuaTelaTest.class.getResource("/host.json")));
			JsonObject obj = val.getObject();
			String host;
			int port;

			if(obj.contains("host"))
			{
				host = obj.getString("host");
				if(obj.contains("port"))
				{
					port = obj.getInt("port");
					if(obj.contains("protocol"))
						config = new HttpHost(host, port, obj.getString("protocol"));
					else
						config = new HttpHost(host, port);
				}
				else
					config = new HttpHost(host);
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
