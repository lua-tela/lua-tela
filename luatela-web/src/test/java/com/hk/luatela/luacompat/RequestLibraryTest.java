package com.hk.luatela.luacompat;

import com.hk.luatela.LuaTelaTest;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;

public class RequestLibraryTest
{
	private HttpClient client;

	@Before
	public void setUp()
	{
		client = HttpClients.createDefault();
	}

	@Test
	public void testParams() throws IOException
	{
		HttpGet request;
		HttpResponse response;

		request = new HttpGet("/request/params");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		assertEquals("20000", EntityUtils.toString(response.getEntity()));

		String query = URLEncodedUtils.format(Arrays.asList(
				new BasicNameValuePair("key1", "value1"),
				new BasicNameValuePair("key2", null),
				new BasicNameValuePair("keyA", "2"),
				new BasicNameValuePair("keyB", "4"),
				new BasicNameValuePair("keyD", "8")
		), StandardCharsets.UTF_8);
		request = new HttpGet("/request/params/test?" + query);

		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		assertEquals("20001", EntityUtils.toString(response.getEntity()));
	}

	@Test
	public void testPaths() throws IOException
	{
		HttpGet request;
		HttpResponse response;

		request = new HttpGet("/request/paths");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("30000", EntityUtils.toString(response.getEntity()));

		request = new HttpGet("/request/paths/this/is/my/path");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("30001", EntityUtils.toString(response.getEntity()));

		request = new HttpGet("/request/paths/fallthrough");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("30002", EntityUtils.toString(response.getEntity()));
	}

	@Test
	public void testSessions() throws IOException
	{
		HttpGet request;
		HttpResponse response;
		String cookie = null;
		String[] paths = { "new", "test", "check", "remove", "new" };
		int[] expected = { 40000, 40001, 40002, 40003, 40000 };

		for (int i = 0; i < paths.length; i++)
		{
			String path = paths[i];
			request = new HttpGet("/request/sessions/" + path);
			if (cookie != null)
				request.setHeader("Cookie", cookie);
			response = client.execute(LuaTelaTest.config, request);

			if (cookie == null)
			{
				Header setCookie = response.getFirstHeader("Set-Cookie");
				assertNotNull(setCookie);
				cookie = setCookie.getValue();
				assertNotNull(cookie);
				assertTrue(cookie.matches(".*JSESSIONID=.+;.*"));
			}

			assertEquals(200, response.getStatusLine().getStatusCode());
			assertEquals(Integer.toString(expected[i]), EntityUtils.toString(response.getEntity()));
		}
	}

	@Test
	public void testFiles() throws IOException
	{
		HttpGet getRequest;
		HttpPost postRequest;
		HttpResponse response;

		getRequest = new HttpGet("/request/files");
		response = client.execute(LuaTelaTest.config, getRequest);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("10000", EntityUtils.toString(response.getEntity()));

		postRequest = new HttpPost("/request/files/info.txt");

		MultipartEntityBuilder builder = MultipartEntityBuilder.create();

		InputStream infoTxt = RequestLibraryTest.class.getResourceAsStream("/info.txt");
		assertNotNull(infoTxt);
		builder.addPart("infotxt", new InputStreamBody(infoTxt, ContentType.DEFAULT_TEXT, "info.txt"));

		builder.addTextBody("alsopost", "exists");

		postRequest.setEntity(builder.build());

		response = client.execute(LuaTelaTest.config, postRequest);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("10001", EntityUtils.toString(response.getEntity()));
	}

	@Test
	public void testHeaders() throws IOException
	{
		HttpGet request;
		HttpResponse response;

		request = new HttpGet("/request/headers");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("70000", EntityUtils.toString(response.getEntity()));

		for(int i = 0; i < 9; i++)
		{
			int count = 1 << i;
			request = new HttpGet("/request/headers/test" + count);

			for (int j = 1; j <= count; j++)
				request.setHeader("Header" + j, "Value" + j);

			response = client.execute(LuaTelaTest.config, request);

			assertEquals(200, response.getStatusLine().getStatusCode());
			assertEquals(Integer.toString(70000 + count), EntityUtils.toString(response.getEntity()));
		}

		request = new HttpGet("/request/headers/test-names");

		String[] strs = "this is my header there are many like it but this one is mine".split(" ");
		for(String str : strs)
			request.setHeader(str, "");

		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals(Integer.toString(70005), EntityUtils.toString(response.getEntity()));
	}

	@After
	public void tearDown()
	{
		client = null;
	}
}
