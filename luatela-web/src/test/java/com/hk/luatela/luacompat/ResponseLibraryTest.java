package com.hk.luatela.luacompat;

import com.hk.luatela.LuaTelaTest;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ResponseLibraryTest
{
	private HttpClient client;

	@Before
	public void setUp()
	{
		client = HttpClients.createDefault();
	}

	@Test
	public void testContentType() throws IOException
	{
		HttpGet request;
		HttpResponse response;
		HttpEntity entity;
		ContentType contentType;

		request = new HttpGet("/response/content-type");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		entity = response.getEntity();
		assertEquals("60000", EntityUtils.toString(entity));

		contentType = ContentType.getLenient(entity);

		assertNotNull(contentType);
		assertEquals("text/plain", contentType.getMimeType());
		assertNotNull(contentType.getCharset());

		request = new HttpGet("/response/content-type.json");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		entity = response.getEntity();
		assertEquals("60001", EntityUtils.toString(entity));

		contentType = ContentType.getLenient(entity);

		assertNotNull(contentType);
		assertEquals("application/json", contentType.getMimeType());
		assertNotNull(contentType.getCharset());

		request = new HttpGet("/response/content-type.html");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		entity = response.getEntity();
		assertEquals("60002", EntityUtils.toString(entity));

		contentType = ContentType.getLenient(entity);

		assertNotNull(contentType);
		assertEquals("text/html", contentType.getMimeType());
		assertNotNull(contentType.getCharset());
	}

	@Test
	public void testContentSize() throws IOException
	{
		HttpGet request;
		HttpResponse response;
		HttpEntity entity;
		Header header;

		request = new HttpGet("/response/content-size");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		entity = response.getEntity();
		assertEquals("50000", EntityUtils.toString(entity));

		assertEquals(5, entity.getContentLength());
		header = response.getFirstHeader("Content-Length");
		assertNotNull(header);
		assertEquals("5", header.getValue());

		request = new HttpGet("/response/content-size/none");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		entity = response.getEntity();
		assertEquals("50001", EntityUtils.toString(entity));

		assertEquals(-1, entity.getContentLength());
		assertNull(response.getFirstHeader("Content-Length"));

		request = new HttpGet("/response/content-size/five");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		entity = response.getEntity();
		assertEquals("50002", EntityUtils.toString(entity));

		assertEquals(5, entity.getContentLength());
		header = response.getFirstHeader("Content-Length");
		assertNotNull(header);
		assertEquals("5", header.getValue());
	}

	@Test
	public void testHeaders() throws IOException
	{
		HttpGet request;
		HttpResponse response;
		HttpEntity entity;

		request = new HttpGet("/response/headers");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		assertNotNull(response.getFirstHeader("Date"));
		assertNotNull(response.getFirstHeader("Server"));

		entity = response.getEntity();
		assertEquals("80000", EntityUtils.toString(entity));

		for(int i = 0; i < 9; i++)
		{
			int count = 1 << i;
			request = new HttpGet("/response/headers/test" + count);
			response = client.execute(LuaTelaTest.config, request);

			assertEquals(200, response.getStatusLine().getStatusCode());

			for (int j = 1; j <= count; j++)
				assertEquals("Value" + j, response.getFirstHeader("Header" + j).getValue());

			entity = response.getEntity();
			assertEquals(Integer.toString(80000 + count), EntityUtils.toString(entity));
		}
	}

	@Test
	public void testStatus() throws IOException
	{
		HttpGet request;
		HttpResponse response;
		HttpEntity entity;

		request = new HttpGet("/response/status");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		entity = response.getEntity();
		assertEquals("90000", EntityUtils.toString(entity));

		request = new HttpGet("/response/status/not-found");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(404, response.getStatusLine().getStatusCode());

		entity = response.getEntity();
		assertEquals("90001", EntityUtils.toString(entity));

		request = new HttpGet("/response/status/found");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(302, response.getStatusLine().getStatusCode());

		entity = response.getEntity();
		assertEquals("90002", EntityUtils.toString(entity));

		request = new HttpGet("/response/status/accepted");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(202, response.getStatusLine().getStatusCode());

		entity = response.getEntity();
		assertEquals("90003", EntityUtils.toString(entity));

		request = new HttpGet("/response/status/forbidden");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(403, response.getStatusLine().getStatusCode());

		entity = response.getEntity();
		assertEquals("90004", EntityUtils.toString(entity));

		request = new HttpGet("/response/status/bad-gateway");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(502, response.getStatusLine().getStatusCode());

		entity = response.getEntity();
		assertEquals("90005", EntityUtils.toString(entity));
	}

	@After
	public void tearDown()
	{
		client = null;
	}
}