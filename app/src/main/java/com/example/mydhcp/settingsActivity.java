package com.example.mydhcp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class settingsActivity extends Activity {

    private String[] time;
    private String[] temp;

    private ListView lvMain;
    private List<String> aStrings = new ArrayList<>();
    private List<String> bStrings = new ArrayList<>();

    private final int MODE_RECEIVE_CONFIG = 0;
    private final int MODE_RECEIVE_WEEK   = 1;
    private final int MODE_SEND_CONFIG    = 2;
    private final int MODE_SEND_WEEK      = 3;
    private int mode = MODE_RECEIVE_CONFIG;
    private int currentPeroid;

    private int selectedRow;
    private TextView dayType, tvResp;
    private String sDayType, weekString ;

    private Button bAdd, bSave;//, bDel;

    //private int cgfChanged = 0;

    //public static String REQUEST_ACTION;
    public static byte[] _BC_ACTION = {Protocol.READ_WEEK_CONFIGS};

    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dayType = (TextView) findViewById(R.id.dayType);

        pb = (ProgressBar)findViewById(R.id.pbConfig);
        tvResp = (TextView)findViewById(R.id.confResponse);
        lvMain = (ListView) findViewById(R.id.lvMain);

        bSave = (Button) findViewById(R.id.btnSave);
        bSave.setVisibility(View.GONE);
        Button bLoad = (Button) findViewById(R.id.btnLoad);
        Button bLoadHolly = (Button) findViewById(R.id.btnLoadHolly);
        bAdd  = (Button) findViewById(R.id.btnAdd);
        bAdd.setVisibility(View.GONE);
        //bDel  = (Button) findViewById(R.id.btnDel);
        Button bWeek = (Button) findViewById(R.id.btnWeek);
        //================================================
        bSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch(mode)
                {
                    case MODE_RECEIVE_WEEK:
                        mode = MODE_SEND_WEEK;
                        _BC_ACTION = new byte[8];
                        _BC_ACTION[0] = Protocol.SAVE_WEEK_CONFIGS;
                        for(int i = 0; i < 7; i++)
                            _BC_ACTION[i+1] = weekString.getBytes()[i];

                        pb.setVisibility(View.VISIBLE);
                        wifiRequestData(MainDHCPActivity.curIPbytes);
                        break;

                    case MODE_RECEIVE_CONFIG:
                        if (time != null) {
                            pb.setVisibility(View.VISIBLE);
                            mode = MODE_SEND_CONFIG;
                            currentPeroid = 1;

                            _BC_ACTION = new byte[8];
                            _BC_ACTION[0] = Protocol.SAVE_DAY_CONFIGS;

                            formBuffer();
                            wifiRequestData(MainDHCPActivity.curIPbytes);
                        }
                        break;
                }
                bSave.setVisibility(View.GONE);
                //cgfChanged = 0;
            }
        });
        //================================================
        bWeek.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                bSave.setVisibility(View.INVISIBLE);
                mode = MODE_RECEIVE_WEEK;
                _BC_ACTION[0] = Protocol.READ_WEEK_CONFIGS;
                pb.setVisibility(View.VISIBLE);
                sDayType = "Неделя";
                wifiRequestData(MainDHCPActivity.curIPbytes);
            }
        });
        //================================================
        bLoad.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                bSave.setVisibility(View.INVISIBLE);
                mode = MODE_RECEIVE_CONFIG;
                _BC_ACTION = new byte[2];
                _BC_ACTION[0] = Protocol.READ_DAY_CONFIGS;
                _BC_ACTION[1] = (byte)0x01;
                pb.setVisibility(View.VISIBLE);
                sDayType = "Рабочий день";
                wifiRequestData(MainDHCPActivity.curIPbytes);
            }
        });
        //================================================
        bLoadHolly.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                bSave.setVisibility(View.INVISIBLE);
                mode = MODE_RECEIVE_CONFIG;
                _BC_ACTION = new byte[2];
                _BC_ACTION[0] = Protocol.READ_DAY_CONFIGS;
                _BC_ACTION[1] = (byte)0x81;
                pb.setVisibility(View.VISIBLE);
                pb.setVisibility(View.VISIBLE);
                sDayType = "Выходной";
                wifiRequestData(MainDHCPActivity.curIPbytes);
            }
        });

        //================================================
        bAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(time.length < 9)
                {
                    List<String> tmpTime = new ArrayList<>();
                    List<String> tmpTemp = new ArrayList<>();
                    if(time != null)
                        for(int k = 0; k < time.length; k++)
                        {
                            tmpTime.add(time[k]);
                            tmpTemp.add(temp[k]);
                        }
                    tmpTime.add("00:00");
                    tmpTemp.add("19.0");

                    time = new String[tmpTime.size()];
                    temp = new String[tmpTemp.size()];

                    tmpTime.toArray(time);
                    tmpTemp.toArray(temp);
                    sortByTime();
                    updateListviewTemperature();
                }
                bSave.setVisibility(View.VISIBLE);
            }
        });
    }
    //==============================================================================================
    private boolean wifiRequestData (byte[] aBytes)
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        time[selectedRow] =  data.getStringExtra("rTime");
        temp[selectedRow] =  data.getStringExtra("rTemp");
        sortByTime();
        updateListviewTemperature();
    }
    //==============================================================================================
    private void formBuffer()
    {
       if (sDayType.contains("Выходной")) _BC_ACTION[1] = (byte)0x80;
        else                               _BC_ACTION[1] = (byte)0x00;
        _BC_ACTION[2] = (byte) currentPeroid;
        _BC_ACTION[3] = (byte) time.length;
        _BC_ACTION[4] = Byte.valueOf(time[currentPeroid-1].substring(0,2));
        _BC_ACTION[5] = Byte.valueOf(time[currentPeroid-1].substring(3,5));

        short shrt = Short.valueOf(temp[currentPeroid-1].substring(0,2) + temp[currentPeroid-1].substring(3,4));
        _BC_ACTION[6] = (byte)(shrt & 0xff);
        _BC_ACTION[7] = (byte)((shrt >> 8) & 0xff);
    }
    //==============================================================================================
    private final String ATTRIBUTE_NAME_REF = "ref";
    private final String ATTRIBUTE_NAME_TIME = "time";
    private final String ATTRIBUTE_NAME_TEMP = "temper";
    //==============================================================================================
    private void sortByTime()
    {
        for(int i = 0; i < time.length; i++)
        {
            for(int j = i+1; j < time.length; j++)
            {
                String tStrA = time[i].replace(":","");
                String tStrB = time[j].replace(":","");
                if (Integer.valueOf(tStrA) > Integer.valueOf(tStrB))
                {
                    String t = time[i];
                    time[i] = time[j];
                    time[j] = t;
                    String tt = temp[i];
                    temp[i] = temp[j];
                    temp[j]= tt;
                }
            }
        }
    }
    //==============================================================================================
    private void updateListviewTemperature()
    {

        ArrayList<Map<String, Object>> data = new ArrayList<>(
                time.length);
        Map<String, Object> m;
        for (int i = 0; i < time.length; i++) {
            m = new HashMap<>();
            m.put(ATTRIBUTE_NAME_REF, i+1);
            m.put(ATTRIBUTE_NAME_TIME, time[i]);
            m.put(ATTRIBUTE_NAME_TEMP, temp[i]);

            data.add(m);
        }
        String[] from = {ATTRIBUTE_NAME_REF, ATTRIBUTE_NAME_TIME, ATTRIBUTE_NAME_TEMP};
        int[] to = {R.id.tvRef, R.id.tvTime, R.id.tvTemp};
        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.item, from, to);
        lvMain = (ListView) findViewById(R.id.lvMain);
        lvMain.setAdapter(sAdapter);
        //==========================================================
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectedRow = position;
                Intent intent = new Intent(settingsActivity.this, PeroidConfig.class);
                intent.putExtra("pTime", time[position]);
                intent.putExtra("pTemp", temp[position]);
                startActivityForResult(intent, 1);
                bSave.setVisibility(View.VISIBLE);
            }
        });
        //==========================================================
        lvMain.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int position, long id) {
                selectedRow = position;
                AlertDialog.Builder adb=new AlertDialog.Builder(settingsActivity.this);
                adb.setTitle("Delete?");
                adb.setMessage("Удалить период " + (position+1) + "?");
                //final int positionToRemove = position;
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (time != null) {
                            List<String> tmpTime = new ArrayList<>();
                            List<String> tmpTemp = new ArrayList<>();
                            for (int k = 0; k < position; k++) {
                                tmpTime.add(time[k]);
                                tmpTemp.add(temp[k]);
                            }
                            for (int k = position + 1; k < time.length; k++) {
                                tmpTime.add(time[k]);
                                tmpTemp.add(temp[k]);
                            }
                            time = new String[tmpTime.size()];
                            temp = new String[tmpTemp.size()];

                            tmpTime.toArray(time);
                            tmpTemp.toArray(temp);
                            sortByTime();
                            updateListviewTemperature();
                            bSave.setVisibility(View.VISIBLE);
                        }
                    }});
                adb.show();
                return true;
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
    private final String wDays[] = {"Понедельник","Вторник","Среда","Четверг","Пятница","Суббота","Воскресенье"};
    private final String ATTRIBUTE_NAME_WDAY = "wday";
    private final String ATTRIBUTE_NAME_IMAGE = "image";
    private char[] day;
    //==============================================================================================
    private void updateListviewWeek(final String aStr)
    {
        int img;
        day = aStr.toCharArray();
        ArrayList<Map<String, Object>> data = new ArrayList<>(
                wDays.length);
        Map<String, Object> m;
        for (int i = 0; i < wDays.length; i++) {
            m = new HashMap<>();
            m.put(ATTRIBUTE_NAME_REF,  i + 1);
            if (day[i] == 'H') img = R.drawable.beer;
            else               img = R.drawable.shovel;
            m.put(ATTRIBUTE_NAME_IMAGE, img);
            m.put(ATTRIBUTE_NAME_WDAY, wDays[i]);
            data.add(m);
        }
        String[] from = {ATTRIBUTE_NAME_REF, ATTRIBUTE_NAME_IMAGE, ATTRIBUTE_NAME_WDAY};
        int[] to = {R.id.tvRefW, R.id.ivDay, R.id.chbDay};
        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.itemweek, from, to);
        lvMain.setAdapter(sAdapter);
        //==========================================================
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(day[position] == 'H') day[position] = 'W';
                else                     day[position] = 'H';
                weekString = String.valueOf(day);
                updateListviewWeek(weekString);
                bSave.setVisibility(View.VISIBLE);
            }
        });
    }
    //==============================================================================================
    private void dataProcessing (String aStr)
    {
        tvResp.setText(aStr);
        bAdd.setEnabled(true);
        bAdd.setVisibility(View.VISIBLE);
        //bDel.setEnabled(true);

        switch(mode)
        {
            case MODE_RECEIVE_CONFIG:
                receiveConfig( aStr.substring(aStr.indexOf("data:") + 5, aStr.length()));
                break;

            case MODE_SEND_CONFIG:
                if(aStr.length() > 10)
                    sendConfig(aStr.substring(aStr.indexOf("data:") + 5, aStr.length()));
                break;

            case MODE_RECEIVE_WEEK:
                bAdd.setEnabled(false);
                bAdd.setVisibility(View.GONE);
                //bDel.setEnabled(false);
                receiveWeek();
                break;

            case MODE_SEND_WEEK:
                sendWeek(aStr.substring(aStr.indexOf("data:") + 5, aStr.length()));
                break;
        }

       // ret = "no answer";
    }
    //==============================================================================================
    private void sendWeek(String resp)
    {
        if(UDPAction.answer[0] == Protocol.OK_ANS)
        {
            pb.setVisibility(View.INVISIBLE);
            tvResp.setText(R.string.saved);
            mode = MODE_RECEIVE_WEEK;
        }
        else
        {
//            SendTask tsk = new SendTask();
//            tsk.execute();
            wifiRequestData(MainDHCPActivity.curIPbytes);
        }
    }
    //==============================================================================================
    private void receiveWeek()
    {
        //if((resp.indexOf("\n\r") == 9) && (resp.indexOf("WC") == 0) )
        if(UDPAction.answer[0] == Protocol.READ_WEEK_CONFIGS_ANS)
        {
            weekString = new String(UDPAction.answer).substring(1,8);//resp.substring(1,8);
            updateListviewWeek(weekString);
            dayType.setBackgroundColor(Color.BLUE);
            dayType.setTextColor(Color.YELLOW);
            dayType.setText(sDayType);
            pb.setVisibility(View.INVISIBLE);
            tvResp.setText(R.string.Done);
        }
        else
        {
            wifiRequestData(MainDHCPActivity.curIPbytes);
        }
    }
    //==============================================================================================
    private void sendConfig(String resp)
    {
        if(UDPAction.answer[0] == Protocol.OK_ANS)
        {
            if(currentPeroid < time.length)
            {
                currentPeroid++;
                formBuffer();
                wifiRequestData(MainDHCPActivity.curIPbytes);

            }
            else
            {
                pb.setVisibility(View.INVISIBLE);
                tvResp.setText(R.string.saved);
                mode = MODE_RECEIVE_CONFIG;
            }
        }
        else
        {
           // formBuffer();
            wifiRequestData(MainDHCPActivity.curIPbytes);
        }
    }
    //==============================================================================================
    private void receiveConfig(String resp)
    {
        String s;

        if(UDPAction.answer[0] == Protocol.READ_DAY_CONFIGS_ANS )
        {


            int msgNumb    = (int)UDPAction.answer[1];
            int partsCount = (int)UDPAction.answer[2];

            if(msgNumb >= partsCount)
            {
                String a = "", b = "";
                if(UDPAction.answer[3] < 10)  a = "0";
                if(UDPAction.answer[4] < 10)  b = "0";

                aStrings.add(a + String.valueOf(UDPAction.answer[3]) + ":" + b + String.valueOf(UDPAction.answer[4]));
                a = new String (((UDPAction.answer[5] &0xff)| (UDPAction.answer[6] & 0xff) << 8) + "");
                bStrings.add(a.substring(0,2) + "." + a.substring(2,3));

                time = new String[aStrings.size()];
                temp = new String[bStrings.size()];
                time = aStrings.toArray(time);
                temp = bStrings.toArray(temp);

                updateListviewTemperature();
                pb.setVisibility(View.INVISIBLE);
                tvResp.setText(R.string.Done);
                aStrings.clear();
                bStrings.clear();

                dayType.setBackgroundColor(Color.BLUE);
                dayType.setTextColor(Color.YELLOW);
                dayType.setText(sDayType);
            }
            else
            {
                String a = "", b = "";
                if(UDPAction.answer[3] < 10)  a = "0";
                if(UDPAction.answer[4] < 10)  b = "0";

                aStrings.add(a + String.valueOf(UDPAction.answer[3]) + ":" + b + String.valueOf(UDPAction.answer[4]));
                a = new String (((UDPAction.answer[5] &0xff)| (UDPAction.answer[6] & 0xff) << 8) + "");
                bStrings.add(a.substring(0,2) + "." + a.substring(2,3));

                _BC_ACTION[1] =(byte)((_BC_ACTION[1] & 0xf0) | (msgNumb + 1));
                wifiRequestData(MainDHCPActivity.curIPbytes);
            }
        }
        else
        {
            //wifiRequestData(MainDHCPActivity.curIPbytes);
        }

    }

}
