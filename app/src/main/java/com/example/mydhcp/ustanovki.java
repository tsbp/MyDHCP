package com.example.mydhcp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class ustanovki extends Activity {

    TextView delta;
    CheckBox swap;
    SeekBar sb;

    //public static String REQUEST_ACTION;
    public static byte[] REQUEST_ACTION;// = {Protocol.SAVE_USTANOVKI};


    final int MODE_RECEIVE_UST = 0;
    final int MODE_SEND_UST    = 1;
    int mode = MODE_RECEIVE_UST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ustanovki);

        delta = (TextView)(findViewById(R.id.delta_value));
        swap = (CheckBox)(findViewById(R.id.chbSensSwap));
        sb = (SeekBar) findViewById(R.id.seekBar);

        Protocol.mode = 5;

        REQUEST_ACTION = new byte[1];
        REQUEST_ACTION[0] = Protocol.READ_USTANOVKI;
        wifiRequestData(MainDHCPActivity.curIPbytes);

        Button okBtn = (Button) findViewById(R.id.okBtn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                REQUEST_ACTION = new byte[3];
                REQUEST_ACTION[0] = Protocol.SAVE_USTANOVKI;
                REQUEST_ACTION[1] = Byte.valueOf(delta.getText().toString().replace(".",""));
                if(swap.isChecked())REQUEST_ACTION[2] = 1;
                else                REQUEST_ACTION[2] = 0;
                wifiRequestData(MainDHCPActivity.curIPbytes);
                Protocol.mode = 1;
                finish();
            }
        });

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String s = String.format("%2.1f", (0.025 * progress));
                s = s.replace(',','.');
                delta.setText( s );
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
    void dataProcessing (String aStr) {
        //tvResp.setText(aStr);

        switch (mode) {
            case MODE_RECEIVE_UST:
                receiveUst(aStr);
                break;

            case MODE_SEND_UST:
                sendUst();
                break;

        }
    }

    //==============================================================================================
    void sendUst()
    {
        if(UDPAction.answer[0] != Protocol.OK_ANS)
            wifiRequestData(MainDHCPActivity.curIPbytes);
    }
    //==============================================================================================
    void receiveUst(String aStr)
    {
        if(UDPAction.answer != null)
            if(UDPAction.answer[0] != Protocol.READ_USTANOVKI)
                {
                    //delta.setText("" + UDPAction.answer[1]);
                    //delta.setText(String.valueOf((float) UDPAction.answer[1] / 10));
                    try
                    {
                        sb.setProgress(UDPAction.answer[1] * 4);
                    }
                    catch (Exception e)
                    {
                        sb.setProgress(5);
                    }



                    if(UDPAction.answer[2] != 0) swap.setChecked(true);
                    else swap.setChecked(false);
                }
    }
}
