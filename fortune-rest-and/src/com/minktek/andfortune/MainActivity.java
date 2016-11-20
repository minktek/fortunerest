package com.minktek.andfortune;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.content.SharedPreferences;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import org.json.JSONObject;

public class MainActivity extends Activity implements OnClickListener
{
    final String HOSTID = "HostIdentifier";
    final String JSON_KEYNAME = "fortune";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button b = (Button)findViewById(R.id.my_button);
        b.setOnClickListener(this);
    }

    @Override
    public void onResume() 
    {
        super.onResume();  
        EditText et = (EditText) findViewById(R.id.hostname_content);

        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        String host = sharedPref.getString(HOSTID, HOSTID);
        if (host == HOSTID)
        {
            updateHostname(et);
        }
        else 
        {
            et.setText(host);
        }

        // anytime we get restored, get a new fortune
        buttonEnable(false);
        new LongRunningGetIO().execute();
    }

    @Override
    public void onPause() 
    {
        super.onPause(); 
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(HOSTID, getHostname());
        editor.commit();
    }

    @Override
    public void onClick(View arg0)
    {
        buttonEnable(false);
        new LongRunningGetIO().execute();
    }

    protected void updateHostname(EditText et)
    {
        final String format = "%d.%d.%d.%d";

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();

        String ipString = String.format(format, (ip & 0xff), (ip >> 8 & 0xff), 
                                        (ip >> 16 & 0xff), (ip >> 24 & 0xff));        
        et.setText(ipString);
    }

    protected String getHostname()
    {
        EditText et = (EditText) findViewById(R.id.hostname_content);
        return et.getText().toString();
    }

    // XXX nice to make me dynamic like the host/ipaddr
    protected String getPortnum()
    {
        return "5100";
    }

    protected void buttonEnable(Boolean which)
    {
        Button b = (Button)findViewById(R.id.my_button);
        if (which == true)
        {
            b.setText("Refresh");
        }
        else
        {
            b.setText("... Running ...");
        }
        b.setClickable(which);
    }

    private class LongRunningGetIO extends AsyncTask <Void, Void, String>
    {
        protected String getContentFromEntity(HttpEntity entity) throws IllegalStateException, IOException 
        {
            InputStream in = entity.getContent();
            StringBuffer out = new StringBuffer();
            while (true) 
            {
                byte[] b = new byte[4096];
                int n = in.read(b);
                if (n <= 0)
                    break;
                out.append(new String(b, 0, n));
            }

            return out.toString();
        }

        @Override
        protected String doInBackground(Void... params) 
        {
            String url = "http://" + getHostname() + ":" + getPortnum() + "/fortune";
            String text = null;
            try 
            {
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpGet httpGet = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpGet, localContext); 
                HttpEntity entity = response.getEntity(); 
                JSONObject jsonResponse = new JSONObject(getContentFromEntity(entity));
                text = jsonResponse.getString(JSON_KEYNAME);
            } 
            catch (Exception e) 
            {
                text = e.getLocalizedMessage();
            }
            return text;
        }

        protected void onPostExecute(String results) 
        {
            if (results != null) 
            {
                TextView tv = (TextView) findViewById(R.id.fortune_content);
                tv.setText(results);
            }
            buttonEnable(true);
        }
    } // class "LongRunningGetIO"
} // class "Main"

