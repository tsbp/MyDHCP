package com.example.mydhcp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.net.*;
import android.net.wifi.WifiManager;
import android.widget.Toast;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
public class MainDHCPActivity extends Activity
{
    TextView info1, info2,tvData, inTemp, outTemp;
    Button btnGetdata, btnStat,setBtn;
    DhcpInfo d;
    WifiManager wifii;
    ProgressBar pbWait;
    public static byte[] curIPbytes;

    com.example.mydhcp.plot inCanvas;
    com.example.mydhcp.plot outCanvas;

    int wifiRequest = 110, wifiRequestLow = 1;



    final boolean IP_IS_REACHABLE = true;

    boolean ESP8266_PRESENT = false;

    /** Called when the activity is first created. */
    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

        setContentView(R.layout.activity_main_dhcp);
        wifii = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        Protocol.mode = Protocol.ESP8266_SEARCH;

        //info1 =  (TextView) findViewById(R.id.tv1);
        // info2 =  (TextView) findViewById(R.id.tv2);
        //tvData = (TextView) findViewById(R.id.tv3);
        pbWait = (ProgressBar) findViewById(R.id.progressBar);

        inTemp = (TextView) findViewById(R.id.inTemp);
        inTemp.setShadowLayer(5, 2, 2, Color.BLACK);

        outTemp = (TextView) findViewById(R.id.outTemp);
        outTemp.setShadowLayer(5, 2, 2, Color.BLACK);

        inCanvas = (com.example.mydhcp.plot) findViewById(R.id.inCanvas);
        outCanvas = (com.example.mydhcp.plot) findViewById(R.id.outCanvas);

        btnGetdata = (Button) findViewById(R.id.updtbtn);
        btnStat = (Button) findViewById(R.id.espbtn);
        setBtn = (Button) findViewById(R.id.setbtn);
        //==========================================================================================
        btnGetdata.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ndString = "";
                pbWait.setVisibility(View.VISIBLE);
                //BC_ACTION = "I1";

                _BC_ACTION[0] = Protocol.PLOT_DATA;
                _BC_ACTION[1] = 0;

                if( Protocol.mode ==  Protocol.ESP8266_SEARCH) startLanSearch();
                else
                {
                    wifiRequestData(curIPbytes);
                    curIPbytes[3] = (byte) wifiRequest;
                }

            }
        });
        //==========================================================================================
        setBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Protocol.mode =  Protocol.ESP8266_CONFIG;
                Intent intent = new Intent(MainDHCPActivity.this, settingsActivity.class);
                startActivity(intent);
            }
        });
        //==========================================================================================
//        getPreferences();
        startLanSearch();
    }
    //==============================================================================================
    void startLanSearch()
    {
        getPreferences();
        //if(wifii.isWifiEnabled())
        {
            pbWait.setVisibility(View.VISIBLE);
            curIPbytes = scan_network();
            curIPbytes[3] = (byte)wifiRequest;
            continueLanSearch();

        }
//        else
//        {
//            Toast t = Toast.makeText(getApplicationContext(),
//                    "Wifi is off. Turn on wifi and restart app", Toast.LENGTH_LONG);
//            t.show();
//            btnStat.setBackgroundResource(R.drawable.wifi_off);
//        }
    }
    //==============================================================================================
    void continueLanSearch()
    {
        while(/*wifiRequest>=wifiRequestLow && */!wifiRequestData(curIPbytes))
        {
            wifiRequest--;
            if(SearchIsComplete()) return;
            curIPbytes[3] = (byte)wifiRequest;
        }
    }
    //==============================================================================================
    boolean  SearchIsComplete()
    {
        if(wifiRequest < wifiRequestLow)
        {
            pbWait.setVisibility(View.INVISIBLE);
            Toast t = Toast.makeText(getApplicationContext(),
                    "Device not found", Toast.LENGTH_LONG);
            t.show();
            btnGetdata.setEnabled(true);
            //getPreferences();
            return true;
        }
        return false;
    }
    //==============================================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_dhc, menu);
        return true;
    }
    //==============================================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        Intent intent;

        switch(item.getItemId())
        {
            case  R.id.lanSettings:
                intent = new Intent(MainDHCPActivity.this, LanConfigActivity.class);
                startActivity(intent);
                break;
            case  R.id.espSettings:
                intent = new Intent(MainDHCPActivity.this, ustanovki.class);
                startActivity(intent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    //==============================================================================================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        BC_ACTION = "I1";
        Protocol.mode =  Protocol.ESP8266_SEARCH;
    }
    //==============================================================================================
    void getPreferences()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String conf = sharedPreferences.getString(LanConfigActivity.configReference, "");
        String ad;

        if (conf != null && conf.contains("addrLow")) {
            ad = conf.substring(conf.indexOf("addrLow") + 7, conf.indexOf("addrHigh"));
            wifiRequestLow = Integer.parseInt(ad);
            if(conf.contains("SSID"))                 ad = conf.substring(conf.indexOf("addrHigh") + 8, conf.indexOf("SSID"));
            else                                      ad = conf.substring(conf.indexOf("addrHigh") + 8, conf.length());
            wifiRequest = Integer.parseInt(ad);
        }
    }
    //==============================================================================================
    private byte[] scan_network()
    {
        d=wifii.getDhcpInfo();
        byte[] ip_bytes = BigInteger.valueOf(d.dns1).toByteArray();
        byte[] ipAddr = new byte[]{ (byte)ip_bytes[3], (byte) ip_bytes[2], (byte) ip_bytes[1], (byte) ip_bytes[0]};
        return ipAddr;
    }

    //==============================================================================================
    boolean wifiRequestData (byte[] aBytes)
    {
        try
        {
            if( InetAddress.getByAddress(aBytes).isReachable(500))
            {
                UDPAction.hostIP = InetAddress.getByAddress(aBytes);
                //UDPAction.BROADCAST_ACTION = "I1";
                UDPAction tsk = new UDPAction(/*getApplicationContext()*/);
                tsk.setOnTaskFinishedEvent(new UDPAction.OnTaskExecutionFinished() {
                    @Override
                    public void OnTaskFihishedEvent(String result)
                    {
                        dataProcessing(result);
                    }

                });
                tsk.execute();
                return  IP_IS_REACHABLE;
            }
        }
        catch(UnknownHostException e1)
        {
            e1.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return  !IP_IS_REACHABLE;
    }
    //==============================================================================================
    String ndString = "";
    public static String BC_ACTION = "I1";

    public static byte[] _BC_ACTION = {Protocol.PLOT_DATA, 1};
    //==============================================================================================
    void dataProcessing (String aStr)
    {
        byte [] buffer;//aStr.getBytes();
        buffer = UDPAction.answer;
        switch( Protocol.mode)
        {
            case  Protocol.ESP8266_SEARCH:

                if (aStr.contains("NO DATA") || (buffer[0] != Protocol.PLOT_DATA_ANS))
                {
                    ndString += aStr + " from IP:" + wifiRequest +"\r\n";
                    //info1.setText(ndString);
                    wifiRequest--;
                    curIPbytes[3] = (byte)wifiRequest;
                    continueLanSearch();
                }
                else
                {
                    if(buffer[0] == Protocol.PLOT_DATA_ANS/*aStr.contains("I146")*/)
                    {
                        //info2.setText("ESP8266 present on" + aStr);
                        ESP8266_PRESENT = true;
                        pbWait.setVisibility(View.INVISIBLE);
                        btnStat.setBackgroundResource(R.drawable.heater_icon);
                        Protocol.mode =  Protocol.ESP8266_DATA_RECEIVE;
                        Toast t = Toast.makeText(getApplicationContext(),
                                "Connected", Toast.LENGTH_SHORT);
                        t.setGravity(Gravity.BOTTOM, 0, 0);
                        t.show();
                        setBtn.setEnabled(true);
                        btnGetdata.setEnabled(true);
                    }
                    else
                    {
                        ndString += aStr + " from IP:" + wifiRequest +"\r\n";
                        //info1.setText(ndString);
                        if(wifiRequest>wifiRequestLow) wifiRequest--;
                        curIPbytes[3] = (byte)wifiRequest;
                        continueLanSearch();
                    }
                }
                break;

            case  Protocol.ESP8266_DATA_RECEIVE:
                if(buffer[0] == Protocol.PLOT_DATA_ANS)
                {
                    short[] pData = new short[24];

                    for(int i = 0; i < 24; i++)
                        pData[i] = ((short)((buffer[1 + i*2] & 0xff) | ((buffer[1+ i*2 +1] & 0xff) << 8)));

                    plot.aBuf = pData;
                    String sign = "";
                    if(pData[23] > 0) sign = "+";

                    if((_BC_ACTION[1] >> 7) == 0)
                    {
                        plot.aColor = new int[]{150, 102, 204, 255};
                        inCanvas.invalidate();
                        inTemp.setText(sign + String.valueOf((float)pData[23] / 10));

                        _BC_ACTION[0] = Protocol.PLOT_DATA;
                        _BC_ACTION[1] = (byte)0x80;
                        wifiRequestData(curIPbytes);
                    }
                    else
                    {
                        plot.aColor = new int[]{120, 255, 255, 0};
                        outCanvas.invalidate();
                        outTemp.setText(sign + String.valueOf((float)pData[23] / 10));//String.valueOf(pData[23]).substring(0,2) + "." + String.valueOf(pData[23]).substring(2,3));
                        pbWait.setVisibility(View.INVISIBLE);
                    }
                }
        }
    }

}

