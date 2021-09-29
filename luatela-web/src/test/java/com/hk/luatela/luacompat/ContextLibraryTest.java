package com.hk.luatela.luacompat;

import com.hk.luatela.LuaTelaTest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ContextLibraryTest
{
    private HttpClient client;

    @Before
    public void setUp() throws Exception
    {
        client = HttpClients.createDefault();
    }

    @Test
    public void testPaths() throws IOException
    {
        HttpGet request;
        HttpResponse response;

        request = new HttpGet("/context/paths");
        response = client.execute(LuaTelaTest.config, request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("13000", EntityUtils.toString(response.getEntity()));
    }
}