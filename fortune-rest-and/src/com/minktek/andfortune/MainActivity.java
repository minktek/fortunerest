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

public class MainActivity extends Activity 
                          implements OnClickListener, InterfaceMessage
{
    final String HOSTID = "HostIdentifier";
    final String FAILED_HOSTNAME = "0.0.0.0";

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

        // anytime we get restored, get a new fortune
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
    public String SendMessage(String message)
    {
        final String msg = message;
        runOnUiThread(new Runnable() 
            {
                @Override
                public void run() 
                {
                    if (msg != null) 
                    {
                        TextView tv = (TextView) findViewById(R.id.fortune_content);
                        tv.setText(msg);
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
            lrgio.setAddress(getHostname(), getPortnum());
            lrgio.execute(this);
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

