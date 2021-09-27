package com.hk.luatela.luacompat;

import com.hk.luatela.LuaTelaTest;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class RequestLibraryTest
{
	private HttpClient client;

	@Before
	public void setUp()
	{
		client = new HttpClient();
		client.setHostConfiguration(LuaTelaTest.config);
	}

	@Test
	public void testIndex() throws IOException
	{
		GetMethod getMethod = new GetMethod("/request/params");
		int resultCode = client.executeMethod(getMethod);

		assertEquals(200, resultCode);
		assertEquals("params", getMethod.getResponseBodyAsString());
	}

	@Test
	public void testHasParam() throws IOException
	{
		GetMethod getMethod = new GetMethod("/request/params/hasParam");
		getMethod.setQueryString(new NameValuePair[]{
				new NameValuePair("key1", "value1")
		});

		int resultCode = client.executeMethod(getMethod);

		assertEquals(200, resultCode);
		assertEquals("params", getMethod.getResponseBodyAsString());
	}
}
