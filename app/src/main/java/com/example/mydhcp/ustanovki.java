package com.example.mydhcp;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;

/**
 * Created by notebook on 26.03.2016.
 */
public class ustanovki extends Activity {
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ustanovki);

        Button okBtn = (Button) findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                saveConfig();
//
//                REQUEST_ACTION =
//                        "SSID" + ssid.getText().toString() +
//                                "$" + ssidpas.getText().toString();
//                wifiRequestData(MainDHCPActivity.curIPbytes);
//                //MainDHCPActivity.mode = 1;
                finish();
            }
        });
    }
}
