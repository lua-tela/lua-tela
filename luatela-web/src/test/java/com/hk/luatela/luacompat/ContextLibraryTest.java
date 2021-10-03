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

    @Test
    public void testAttributes() throws IOException
    {
        HttpGet request;
        HttpResponse response;

        request = new HttpGet("/context/attributes");
        response = client.execute(LuaTelaTest.config, request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("14000", EntityUtils.toString(response.getEntity()));

        int start = 0, abs;
        int[] changes = { 1, 2, -1, 5, 7, 9, -3, -6 };

        for(int change : changes)
        {
            abs = Math.abs(change);
            if(abs > 5)
            {
                start += change;
                request = new HttpGet("/context/attributes/set/" + start);
                response = client.execute(LuaTelaTest.config, request);

                assertEquals("for " + change, 200, response.getStatusLine().getStatusCode());
                assertEquals("for " + change, Integer.toString(14000 + start), EntityUtils.toString(response.getEntity()));
            }
            else
            {
                String url = "/context/attributes/";
                if(change < 0)
                    url += "sub";
                else
                    url += "add";

                for(int i = 0; i < abs; i++)
                {
                    request = new HttpGet(url);
                    response = client.execute(LuaTelaTest.config, request);

                    if(change < 0)
                        start--;
                    else
                        start++;

                    assertEquals("for " + change, 200, response.getStatusLine().getStatusCode());
                    assertEquals("for " + change, Integer.toString(14000 + start), EntityUtils.toString(response.getEntity()));
                }
            }
        }

        request = new HttpGet("/context/attributes");
        response = client.execute(LuaTelaTest.config, request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals(Integer.toString(14000 + start), EntityUtils.toString(response.getEntity()));

        assertNotEquals(0, start);

        request = new HttpGet("/context/attributes/done");
        response = client.execute(LuaTelaTest.config, request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("14000", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testMimeType() throws IOException
    {
        HttpGet request;
        HttpResponse response;

        request = new HttpGet("/context/mime-type");
        response = client.execute(LuaTelaTest.config, request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("15000", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testEscaping() throws IOException
    {
        HttpGet request;
        HttpResponse response;

        request = new HttpGet("/context/escaping");
        response = client.execute(LuaTelaTest.config, request);

        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("16000", EntityUtils.toString(response.getEntity()));
    }
}