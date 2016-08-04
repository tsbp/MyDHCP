package com.example.mydhcp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class LanConfigActivity extends Activity {


    private EditText addrLow, addrHigh;//, ssid, ssidpas;
    //int addrH, addrL;

    public static String configReference = "lanConfig";
    //private static String REQUEST_ACTION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan_config);

        addrLow  = (EditText) findViewById(R.id.addrLow);
        addrHigh = (EditText) findViewById(R.id.addrHigh);
//        ssid     = (EditText) findViewById(R.id.setSSID);
//        ssidpas  = (EditText) findViewById(R.id.setSSIDPAS);

        loadConfig();
        Protocol.mode = 4;

        Button okBtn = (Button) findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveConfig();

//                REQUEST_ACTION =
//                        "SSID"  + ssid.getText().toString() +
//                        "$" + ssidpas.getText().toString();
                //wifiRequestData(MainDHCPActivity.curIPbytes);
                finish();
            }
        });
    }
    //==============================================================================================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Protocol.mode = 1;
    }
    //==============================================================================================
    private void saveConfig() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(configReference, "addrLow" + addrLow.getText().toString() +
                "addrHigh" + addrHigh.getText().toString());/* +
                "SSID" + ssid.getText().toString() +
                "SSIDPAS" + ssidpas.getText().toString());*/

        editor.apply();
    }
    //==============================================================================================
    private void loadConfig() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String conf = sharedPreferences.getString(configReference, "") ;
        String adLow;//, adHigh, s, spas;

        if(conf.contains("addrLow") && conf.contains("addrHigh"))
        {
            adLow = conf.substring(conf.indexOf("addrLow") + 7, conf.indexOf("addrHigh"));
            addrLow.setText(adLow);

//            if(conf.contains("SSID")) {
//                adHigh = conf.substring(conf.indexOf("addrHigh") + 8, conf.indexOf("SSID"));
//                addrHigh.setText(adHigh);
//            }
//            if(conf.contains("SSIDPAS")) {
//                s = conf.substring(conf.indexOf("SSID") + 4, conf.indexOf("SSIDPAS"));
//                spas = conf.substring(conf.indexOf("SSIDPAS") + 7, conf.length());
//                ssid.setText(s);
//                ssidpas.setText(spas);
//            }
        }

    }

    //==============================================================================================
//    boolean wifiRequestData (byte[] aBytes)
//    {
//        try
//        {
//            if( InetAddress.getByAddress(aBytes).isReachable(500))
//            {
//                UDPAction.hostIP = InetAddress.getByAddress(aBytes);
//                UDPAction tsk = new UDPAction();
//                tsk.setOnTaskFinishedEvent(new UDPAction.OnTaskExecutionFinished() {
//                    @Override
//                    public void OnTaskFihishedEvent(String result)
//                    {
//                        //dataProcessing(result);
//                    }
//
//                });
//                tsk.execute();
//                return  true;
//            }
//        }
//        catch(IOException e)
//        {
//            e.printStackTrace();
//        }
//        return  false;
//    }
}
