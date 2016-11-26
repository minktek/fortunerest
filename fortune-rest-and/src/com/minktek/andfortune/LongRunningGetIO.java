package com.minktek.andfortune;

import java.io.InputStream;
import java.io.IOException;

import android.os.AsyncTask;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import org.json.JSONObject;

class LongRunningGetIO extends AsyncTask <String, Void, String>
{
    protected InterfaceMessage jsonHandler;

    public void setHandler(InterfaceMessage handler)
    {
        jsonHandler = handler;
    }

    protected String getContentFromEntity(HttpEntity entity) throws 
                         IllegalStateException, IOException 
    {
        InputStream in = entity.getContent();
        StringBuffer out = new StringBuffer();
        while (true) 
        {
            byte[] b = new byte[80];
            int n = in.read(b);
            if (n <= 0)
                break;
            out.append(new String(b, 0, n));
        }

        return out.toString();
    }

    @Override
    protected String doInBackground(String... urls) 
    {
        if (jsonHandler == null)
        {
            // XXX log a message - error
            return null;
        }

        // int count = urls.length; XXX do I care?
        String url = urls[0];

        String text = null;
        JSONObject jresp = null;
        try 
        {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext hctx = new BasicHttpContext();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet, hctx); 
            HttpEntity entity = response.getEntity(); 
            jresp = new JSONObject(getContentFromEntity(entity));
        } 
        catch (Exception e) 
        {
            text = e.getLocalizedMessage();
        }

        return jsonHandler.SendMessage(jresp, text);
    }
} 

