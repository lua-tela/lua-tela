package com.hk.luatela.luacompat;

import com.hk.json.Json;
import com.hk.json.JsonArray;
import com.hk.json.JsonObject;
import com.hk.luatela.LuaTelaTest;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

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
	public void testGet() throws IOException
	{
		HttpGet request;
		HttpResponse response;
		StringBuilder query;

		request = new HttpGet("/request/get");
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		assertEquals("20000", EntityUtils.toString(response.getEntity()));

		query = new StringBuilder("key=value");
		request = new HttpGet("/request/get/single?" + query);

		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		assertEquals("20001", EntityUtils.toString(response.getEntity()));

		query = new StringBuilder("ampersand=%26");
		request = new HttpGet("/request/get/ampersand?" + query);

		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		assertEquals("20002", EntityUtils.toString(response.getEntity()));

		query = new StringBuilder("B00B135");
		request = new HttpGet("/request/get/querycookie?" + query);

		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		assertEquals("20003", EntityUtils.toString(response.getEntity()));

		query = new StringBuilder(64);
		for (int i = 0; i < 128; i++)
		{
			query.append("/request/get/");
			query.append(i % 2 == 0 ? "get" : "post");
			query.append("-catdog?");

			for (int j = 0; j < 6; j++)
			{
				if(j == 3)
					query.append('=');

				query.append(catdog[j][(i >> (j + 1)) & 1]);
			}

			if(i % 2 == 0)
				response = client.execute(LuaTelaTest.config, new HttpGet(query.toString()));
			else
				response = client.execute(LuaTelaTest.config, new HttpPost(query.toString()));
			query.setLength(0);

			assertEquals(200, response.getStatusLine().getStatusCode());

			assertEquals("20004", EntityUtils.toString(response.getEntity()));
		}

		query = new StringBuilder("key1=value%31&key2=&key4&keyA=2&keyB=%26&keyC&keyD=8");
		request = new HttpGet("/request/get/multiple?" + query);

		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		assertEquals("20005", EntityUtils.toString(response.getEntity()));

		query = new StringBuilder("flag1&flag2&flag3");
		request = new HttpGet("/request/get/flags?" + query);

		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());

		assertEquals("20006", EntityUtils.toString(response.getEntity()));
	}

	@Test
	public void testPost() throws IOException
	{
		HttpPost request;
		HttpResponse response;
		String query;

		response = client.execute(LuaTelaTest.config, new HttpGet("/request/post"));
		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("21000", EntityUtils.toString(response.getEntity()));

		request = new HttpPost("/request/post/single");

		query = "key=value";
		request.setEntity(new StringEntity(query, ContentType.APPLICATION_FORM_URLENCODED));

		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("21001", EntityUtils.toString(response.getEntity()));

		request = new HttpPost("/request/post/multiple");

		query = "and=his&name=was&john=cena";
		request.setEntity(new StringEntity(query, ContentType.APPLICATION_FORM_URLENCODED));

		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("21002", EntityUtils.toString(response.getEntity()));

		request = new HttpPost("/request/post/empty");

		query = "empty1&empty2=";
		request.setEntity(new StringEntity(query, ContentType.APPLICATION_FORM_URLENCODED));

		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("21003", EntityUtils.toString(response.getEntity()));

		StringBuilder sb = new StringBuilder(32);
		for (int i = 0; i < 64; i++)
		{
			for (int j = 0; j < 6; j++)
			{
				if(j == 3)
					sb.append('=');

				sb.append(catdog[j][(i >> j) & 1]);
			}

			request = new HttpPost("/request/post/catdog");
			request.setEntity(new StringEntity(sb.toString(), ContentType.APPLICATION_FORM_URLENCODED));
			sb.setLength(0);

			response = client.execute(LuaTelaTest.config, request);

			assertEquals(200, response.getStatusLine().getStatusCode());

			assertEquals("21004", EntityUtils.toString(response.getEntity()));
		}
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

	@Test
	public void testBody() throws IOException
	{
		HttpGet getRequest;
		HttpPost request;
		HttpResponse response;

		getRequest = new HttpGet("/request/body");
		response = client.execute(LuaTelaTest.config, getRequest);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("17000", EntityUtils.toString(response.getEntity()));

		request = new HttpPost("/request/body/string1");
		request.setEntity(new StringEntity("this is my string"));
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("17001", EntityUtils.toString(response.getEntity()));

		request = new HttpPost("/request/body/string2");
		request.setEntity(new StringEntity("quavo, offset, and takeoff"));
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("17002", EntityUtils.toString(response.getEntity()));

		request = new HttpPost("/request/body/tostring1");
		request.setEntity(new StringEntity("this is my string"));
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("17003", EntityUtils.toString(response.getEntity()));

		request = new HttpPost("/request/body/tostring2");
		request.setEntity(new StringEntity("quavo, offset, and takeoff"));
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("17004", EntityUtils.toString(response.getEntity()));

		request = new HttpPost("/request/body/toreader");
		request.setEntity(new StringEntity("but it's tru tho\nikno it's tru tho\nrep the north like I'm Trudeau"));
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("17005", EntityUtils.toString(response.getEntity()));

		String lyrics = "I miss the old Kanye, straight from the 'Go Kanye\n" +
				"Chop up the soul Kanye, set on his goals Kanye\n" +
				"I hate the new Kanye, the bad mood Kanye\n" +
				"The always rude Kanye, spaz in the news Kanye\n" +
				"I miss the sweet Kanye, chop up the beats Kanye\n" +
				"I gotta to say at that time I'd like to meet Kanye\n" +
				"See I invented Kanye, it wasn't any Kanyes\n" +
				"And now I look and look around and there's so many Kanyes\n" +
				"I used to love Kanye, I used to love Kanye\n" +
				"I even had the pink polo, I thought I was Kanye\n" +
				"What if Kanye made a song about Kanye\n" +
				"Called \"I Miss The Old Kanye, \" man that would be so Kanye\n" +
				"That's all it was Kanye, we still love Kanye\n" +
				"And I love you like Kanye loves Kanye";
		request = new HttpPost("/request/body/tofile");
		request.setEntity(new StringEntity(lyrics));
		// these lyrics are uploaded to the server which in turn saves
		// it to a resource file under rap.txt
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("17006", EntityUtils.toString(response.getEntity()));

		// we can then retrieve this resource file to compare content
		// >:D
		getRequest = new HttpGet("/res/rap.txt");
		response = client.execute(LuaTelaTest.config, getRequest);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals(lyrics, EntityUtils.toString(response.getEntity()));

		assertTrue(Files.deleteIfExists(Paths.get("src/test/webapp/base/res/rap.txt")));

		request = new HttpPost("/request/body/json");
		JsonObject obj = new JsonObject();
		obj.put("title", "The Shawshank Redemption");
		obj.put("director", "Frank Darabont");
		obj.put("screenplay", "Stephen King");
		obj.put("releasedate", "September 22, 1994");

		JsonArray arr = new JsonArray();
		arr.add("Morgan Freeman");
		arr.add("Tim Robbins");
		arr.add("Clancy Brown");
		arr.add("Bob Gunton");
		arr.add("James Whitmore");
		obj.put("cast", arr);

		request.setEntity(new StringEntity(Json.write(obj)));
		response = client.execute(LuaTelaTest.config, request);

		assertEquals(200, response.getStatusLine().getStatusCode());
		assertEquals("17007", EntityUtils.toString(response.getEntity()));
	}

	@After
	public void tearDown()
	{
		client = null;
	}

	private static final String[][] catdog = {
		{ "c", "%63" }, { "a", "%61" }, { "t", "%74" },
		{ "d", "%64" }, { "o", "%6F" }, { "g", "%67" }
	};
}
