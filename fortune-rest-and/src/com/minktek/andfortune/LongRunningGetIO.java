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

class LongRunningGetIO extends AsyncTask <InterfaceMessage, Void, String>
{
    protected final String JSON_KEYNAME = "fortune";
    protected final String PROTOCOL = "http://";

    protected String hostname;
    protected String portnum;

    public void setAddress(String host, String port)
    {
        hostname = host;
        portnum = port;
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
    protected String doInBackground(InterfaceMessage... msgs) 
    {
        // int count = msgs.length; XXX do I care?
        InterfaceMessage msg = msgs[0];

        String url = PROTOCOL + hostname + ":" + portnum + "/" + JSON_KEYNAME;
        String text = null;
        try 
        {
            HttpClient httpClient = new DefaultHttpClient();
            HttpContext hctx = new BasicHttpContext();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse response = httpClient.execute(httpGet, hctx); 
            HttpEntity entity = response.getEntity(); 
            JSONObject jresp = new JSONObject(getContentFromEntity(entity));
            text = jresp.getString(JSON_KEYNAME);
        } 
        catch (Exception e) 
        {
            text = e.getLocalizedMessage();
        }

        return msg.SendMessage(text);
    }
} 

