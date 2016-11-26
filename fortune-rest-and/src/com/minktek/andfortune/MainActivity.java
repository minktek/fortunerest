package com.minktek.andfortune;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class MainActivity extends Activity 
                          implements OnClickListener, InterfaceMessage
{
    protected final String HOSTID = "HostIdentifier";
    protected final String FAILED_HOSTNAME = "0.0.0.0";
    protected final String PROTOCOL = "http://";
    public final String JSON_KEYNAME = "fortune";

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

        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        String host = validateHostname(sharedPref.getString(HOSTID, HOSTID));

        EditText et = (EditText) findViewById(R.id.hostname_content);
        et.setText(host);

        // anytime we get restored, get make a new request
        runIt();
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
        runIt();
    }

    @Override
    public String SendMessage(JSONObject jobj, String message)
    {
        final String msg = message;
        final JSONObject jresp = jobj;
        runOnUiThread(new Runnable() 
            {
                @Override
                public void run() 
                {
                    TextView tv = (TextView) findViewById(R.id.fortune_content);
                    if (jresp != null)
                    {
                        try
                        {
                            tv.setText(jresp.getString(JSON_KEYNAME));
                        }
                        catch (Exception e)
                        {
                            tv.setText(e.getLocalizedMessage());
                        }
                    }
                    else if (msg != null) 
                    {
                        tv.setText(msg);
                    }
                    else
                    {
                        tv.setText("Internal Error: Something really unexpected happened");
                    }
                    buttonEnable(true);
                }
            }
        );
        
        return msg;
    }

    protected void runIt()
    {
        buttonEnable(false);

        String hostname = getHostname();
        if (hostname == FAILED_HOSTNAME)
        {
            Toast.makeText(this, "Invalid Host", Toast.LENGTH_LONG).show();
        }
        else
        {
            LongRunningGetIO lrgio = new LongRunningGetIO();
            lrgio.setHandler(this);
            lrgio.execute(PROTOCOL + getHostname() + ":" + getPortnum() + "/" + JSON_KEYNAME);
        }
    }

    protected String validateHostname(String host)
    {
        final String format = "%d.%d.%d.%d";

        if (host == HOSTID)
        {
            WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wm.getConnectionInfo();
            int ip = wifiInfo.getIpAddress();

            return String.format(format, (ip & 0xff), (ip >> 8 & 0xff), 
                                (ip >> 16 & 0xff), (ip >> 24 & 0xff));        
        }

        /* XXX do IPv4 checks - dots, numbers, etc. */
        /*     if we fail validation, return "failed" hostname */

        return host;
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

} 

