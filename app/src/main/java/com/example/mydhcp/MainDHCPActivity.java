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

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

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

    final int ESP8266_SEARCH = 0;
    final int ESP8266_DATA_RECEIVE = 1;
    final int ESP8266_CONFIG = 3;
    final int ESP8266_LANSET = 4;
    final int ESP8266_UST = 5;
    public static int mode;

    final boolean IP_IS_REACHABLE = true;

    boolean ESP8266_PRESENT = false;

    /** Called when the activity is first created. */
    @Override

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build());

        setContentView(R.layout.activity_main_dhcp);
        wifii = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        mode = ESP8266_SEARCH;

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
                BC_ACTION = "I1";
                //UDPAction.BROADCAST_ACTION = BC_ACTION;

                if(mode == ESP8266_SEARCH) startLanSearch();
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
                mode = ESP8266_CONFIG;
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
        if(wifii.isWifiEnabled())
        {
            pbWait.setVisibility(View.VISIBLE);
            curIPbytes = scan_network();
            curIPbytes[3] = (byte)wifiRequest;
            continueLanSearch();

        }
        else
        {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Wifi is off. Turn on wifi and restart app", Toast.LENGTH_LONG);
            t.show();
            btnStat.setBackgroundResource(R.drawable.wifi_off);
        }
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
        mode = ESP8266_SEARCH;
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
    //==============================================================================================
    void dataProcessing (String aStr)
    {
        switch(mode)
        {
            case ESP8266_SEARCH:

                if (aStr.contains("NO DATA") || aStr.contains("its my ip"))
                {
                    ndString += aStr + " from IP:" + wifiRequest +"\r\n";
                    //info1.setText(ndString);
                    wifiRequest--;
                    curIPbytes[3] = (byte)wifiRequest;
                    continueLanSearch();
                }
                else
                {
                    if(aStr.contains("I146"))
                    {
                        //info2.setText("ESP8266 present on" + aStr);
                        ESP8266_PRESENT = true;
                        pbWait.setVisibility(View.INVISIBLE);
                        btnStat.setBackgroundResource(R.drawable.heater_icon);
                        mode = ESP8266_DATA_RECEIVE;
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
            case ESP8266_DATA_RECEIVE:
                //======== check if pack is complete ===============================================
                String dataString  = aStr.substring(aStr.indexOf("data:") +5, aStr.indexOf("data:") +35);
                int a = dataString.indexOf("\n\r");
                if(a == 28)
                {
                    int msgNumber;
                    int msgCount;
                    String dataType;
                    try
                    {
                        msgNumber = Integer.parseInt(dataString.substring(1,2));
                        msgCount  = Integer.parseInt(dataString.substring(2,3));
                        dataType =  dataString.substring(0,1);
                        ndString += dataString;
                        if (msgNumber < msgCount)
                        {
                            BC_ACTION = dataType +  (msgNumber + 1);
                            wifiRequestData(curIPbytes);
                        }
                        else if(msgNumber == msgCount)
                        {
                           if(dataType.contains("I"))
                           {
                               dataType = "O";

                               // parse data
                               int[] inData = parseData("I", ndString);
                               plot.aBuf = inData;
                               plot.aColor = new int[]{150, 102, 204, 255};
                               inCanvas.invalidate();

                               inTemp.setText(revertValue(ndString));
                               ndString = "";

                               BC_ACTION = dataType +  1;
                               wifiRequestData(curIPbytes);
                           }
                            else
                           {
                               int[] outData = parseData("O", ndString);
                               plot.aBuf = outData;
                               plot.aColor = new int[]{120, 255, 255, 0};
                               outCanvas.invalidate();

                               outTemp.setText(revertValue(ndString));
                               pbWait.setVisibility(View.INVISIBLE);
                               ndString = "";
                           }
                        }
                    }
                    catch(Exception e)
                    {
                        ndString = "Error";
                    }
                }
                else wifiRequestData(curIPbytes);
                //tvData.setText(ndString);
                //==================================================================================
                break;

        }
    }
    //==============================================================================================
    int [] parseData(String aDataRef, String aDataString)
    {
        int [] dataBuf = new int[24];
        int bCounter = 0;
        for(int k = 0; k < 4; k++)
        {
           // int l = aDataString.length();
            String tmpStr = aDataString.substring(k*30, k*30 + 30);//30 = 4+6*4+2
            if(tmpStr.substring(0,1).contains(aDataRef))
            {
                for(int i = 0; i < 6; i++)
                {
                    String s = tmpStr.substring(4+i*4, 8+i*4);
                    dataBuf[bCounter]= Integer.parseInt(s.substring(1));
                    if (s.contains("-")) dataBuf[bCounter] *= -1;
                    bCounter++;
                }
            }
        }
        return dataBuf;
    }
    //==============================================================================================
    String revertValue(String aStr)
    {
        String ss = aStr.substring(aStr.length() - 6, aStr.length() - 3);
        ss = ss.replace("00", "0") + ".";
        if (ss.contains("+0") && !ss.contains("+0.")) ss = ss.replace("+0", "+");
        if (ss.contains("-0") && !ss.contains("-0.")) ss = ss.replace("-0", "-");
        return (ss + aStr.substring(aStr.length() - 3, aStr.length() - 2));
    }
}

