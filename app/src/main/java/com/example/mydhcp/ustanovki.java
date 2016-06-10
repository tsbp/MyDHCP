package com.example.mydhcp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;


public class ustanovki extends Activity {

    TextView delta;
    CheckBox swap;

    public static String REQUEST_ACTION;

    final int MODE_RECEIVE_UST = 0;
    final int MODE_SEND_UST    = 1;
    int mode = MODE_RECEIVE_UST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ustanovki);

        delta = (TextView)(findViewById(R.id.delta));
        swap = (CheckBox)(findViewById(R.id.chbSensSwap));

        Protocol.mode = 5;

        REQUEST_ACTION = "GUST";
        wifiRequestData(MainDHCPActivity.curIPbytes);

        Button okBtn = (Button) findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                REQUEST_ACTION = "SUST" + delta.getText();
                if(swap.isChecked())REQUEST_ACTION += "S1";
                else                REQUEST_ACTION += "S0";
                wifiRequestData(MainDHCPActivity.curIPbytes);
                Protocol.mode = 1;
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
    boolean wifiRequestData (byte[] aBytes)
    {
        try
        {
            if( InetAddress.getByAddress(aBytes).isReachable(500))
            {
                UDPAction.hostIP = InetAddress.getByAddress(aBytes);
                UDPAction tsk = new UDPAction();
                tsk.setOnTaskFinishedEvent(new UDPAction.OnTaskExecutionFinished() {
                    @Override
                    public void OnTaskFihishedEvent(String result)
                    {
                        dataProcessing(result);
                    }

                });
                tsk.execute();
                return  true;
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return  false;
    }
    //==============================================================================================
    void dataProcessing (String aStr)
    {
        //tvResp.setText(aStr);

        switch(mode)
        {
            case MODE_RECEIVE_UST:
                receiveUst(aStr);
                break;

            case MODE_SEND_UST:
                    sendUst();
                break;

        }

        // ret = "no answer";
    }

    //==============================================================================================
    void sendUst()
    {
        REQUEST_ACTION += "SUST";
        wifiRequestData(MainDHCPActivity.curIPbytes);
    }
    //==============================================================================================
    void receiveUst(String aStr)
    {
        int a = aStr.indexOf("UST");
        if(aStr.contains("UST"))
        {
            delta.setText(aStr.substring(a+3,a + 6));
            if(aStr.charAt(a+7)== '1') swap.setChecked(true);
            else                       swap.setChecked(false);
        }
//        REQUEST_ACTION += "GUST";
//        wifiRequestData(MainDHCPActivity.curIPbytes);
    }
}
