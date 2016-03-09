package com.example.mydhcp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class LanConfigActivity extends Activity {


    EditText addrLow, addrHigh;
    int addrH, addrL;

    public static String configReference = "lanConfig";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lan_config);

        addrLow   = (EditText) findViewById(R.id.addrLow);
        addrHigh = (EditText) findViewById(R.id.addrHigh);

        loadConfig();

        Button okBtn = (Button) findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                saveConfig();
                finish();
            }
        });
    }

    void saveConfig() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(configReference, "addrLow" + addrLow.getText().toString() +
                                          "addrHigh"+ addrHigh.getText().toString()
                                          /*+ "port" + port.getText().toString()*/);

        editor.apply();
    }

    void loadConfig() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String conf = sharedPreferences.getString(configReference, "") ;
        String adLow, adHigh;

        if(conf != null && conf.contains("addrLow"))
        {
            adLow   = conf.substring(conf.indexOf("addrLow") + 7,conf.indexOf("addrHigh"));
            adHigh  = conf.substring(conf.indexOf("addrHigh") + 8, conf.length());
        }
        else return;
        addrLow.setText(adLow);
        addrHigh.setText(adHigh);
//        addrH = Integer.parseInt(adHigh);
//        addrL = Integer.parseInt(adLow);
    }
}
