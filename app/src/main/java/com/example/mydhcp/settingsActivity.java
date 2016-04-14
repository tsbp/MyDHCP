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

    String[] time;
    String[] temp;

    ListView lvMain;
    List<String> aStrings = new ArrayList<>();
    List<String> bStrings = new ArrayList<>();

    final int MODE_RECEIVE_CONFIG = 0;
    final int MODE_RECEIVE_WEEK   = 1;
    final int MODE_SEND_CONFIG    = 2;
    final int MODE_SEND_WEEK      = 3;
    int mode = MODE_RECEIVE_CONFIG;
    int currentPeroid;

    int selectedRow;
    TextView dayType, tvResp;
    String sDayType, weekString ;

    Button bAdd, bDel;


    public static String REQUEST_ACTION;

    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        dayType = (TextView) findViewById(R.id.dayType);

        pb = (ProgressBar)findViewById(R.id.pbConfig);
        tvResp = (TextView)findViewById(R.id.confResponse);
        lvMain = (ListView) findViewById(R.id.lvMain);

        Button bSave = (Button) findViewById(R.id.btnSave);
        Button bLoad = (Button) findViewById(R.id.btnLoad);
        Button bLoadHolly = (Button) findViewById(R.id.btnLoadHolly);
        bAdd  = (Button) findViewById(R.id.btnAdd);
        bDel  = (Button) findViewById(R.id.btnDel);
        Button bWeek = (Button) findViewById(R.id.btnWeek);
        //================================================
        bSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch(mode)
                {
                    case MODE_RECEIVE_WEEK:
                        mode = MODE_SEND_WEEK;
                        REQUEST_ACTION = "CSAW" + weekString;
                        pb.setVisibility(View.VISIBLE);
                        wifiRequestData(MainDHCPActivity.curIPbytes);
                        break;

                    case MODE_RECEIVE_CONFIG:
                        if (time != null) {
                            pb.setVisibility(View.VISIBLE);
                            mode = MODE_SEND_CONFIG;
                            currentPeroid = 1;
                            formBuffer();
                            wifiRequestData(MainDHCPActivity.curIPbytes);
                        }
                        break;
                }
            }
        });
        //================================================
        bWeek.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mode = MODE_RECEIVE_WEEK;
                REQUEST_ACTION = "WEEK";
                pb.setVisibility(View.VISIBLE);
                sDayType = "Неделя";
                wifiRequestData(MainDHCPActivity.curIPbytes);
            }
        });
        //================================================
        bLoad.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mode = MODE_RECEIVE_CONFIG;
                REQUEST_ACTION = "CONFW1";
                pb.setVisibility(View.VISIBLE);
                sDayType = "Рабочий день";
                wifiRequestData(MainDHCPActivity.curIPbytes);
            }
        });
        //================================================
        bLoadHolly.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                mode = MODE_RECEIVE_CONFIG;
                REQUEST_ACTION = "CONFH1";
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
            }
        });
        //================================================
//        bDel.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if (time != null) {
//                    List<String> tmpTime = new ArrayList<>();
//                    List<String> tmpTemp = new ArrayList<>();
//                    for (int k = 0; k < (time.length - 1); k++) {
//                        tmpTime.add(time[k]);
//                        tmpTemp.add(temp[k]);
//                    }
//                    time = new String[tmpTime.size()];
//                    temp = new String[tmpTemp.size()];
//
//                    tmpTime.toArray(time);
//                    tmpTemp.toArray(temp);
//                    updateListviewTemperature();
//                }
//
//            }
//        });
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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            return;
        }

        time[selectedRow] = data.getStringExtra("rTime");;
        temp[selectedRow] =  data.getStringExtra("rTemp");
        sortByTime();
        updateListviewTemperature();
    }
    //==============================================================================================
    void formBuffer()
    {
        REQUEST_ACTION = "";
        REQUEST_ACTION = "CSAV";
        if (sDayType.contains("Выходной")) REQUEST_ACTION += "H" + currentPeroid;
        else                               REQUEST_ACTION += "W" + currentPeroid;
        REQUEST_ACTION +=      time.length +
                time[currentPeroid-1].substring(0,2) + time[currentPeroid-1].substring(3,5)+
                temp[currentPeroid-1].substring(0,2) + temp[currentPeroid-1].substring(3,4);
    }
    //==============================================================================================
    final String ATTRIBUTE_NAME_REF = "ref";
    final String ATTRIBUTE_NAME_TIME = "time";
    final String ATTRIBUTE_NAME_TEMP = "temper";
    //==============================================================================================
    void sortByTime()
    {
        for(int i = 0; i < time.length; i++)
        {
            for(int j = i+1; j < time.length; j++)
            {
                String tStrA = time[i].toString().replace(":","");
                String tStrB = time[j].toString().replace(":","");
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
    void updateListviewTemperature()
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
                final int positionToRemove = position;
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
        MainDHCPActivity.mode = 1;
    }
    //==============================================================================================
    final String wDays[] = {"Понедельник","Вторник","Среда","Четверг","Пятница","Суббота","Воскресенье"};
    final String ATTRIBUTE_NAME_WDAY = "wday";
    final String ATTRIBUTE_NAME_IMAGE = "image";
    char[] day;
    //==============================================================================================
    void updateListviewWeek(final String aStr)
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
            }
        });
    }
    //==============================================================================================
    void dataProcessing (String aStr)
    {
        tvResp.setText(aStr);
        bAdd.setEnabled(true);
        bDel.setEnabled(true);

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
                bDel.setEnabled(false);
                receiveWeek(aStr.substring(aStr.indexOf("data:") + 5, aStr.length()));
                break;

            case MODE_SEND_WEEK:
                sendWeek(aStr.substring(aStr.indexOf("data:") + 5, aStr.length()));
                break;
        }

       // ret = "no answer";
    }
    //==============================================================================================
    void sendWeek(String resp)
    {
        if(resp.contains("OKW"))
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
    void receiveWeek(String resp)
    {
        if((resp.indexOf("\n\r") == 9) && (resp.indexOf("WC") == 0) )
        {
            weekString = resp.substring(2,9);
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
    void sendConfig(String resp)
    {
        if(resp.contains("OK"))
        {
            if(currentPeroid <= time.length)
            {
                formBuffer();
                wifiRequestData(MainDHCPActivity.curIPbytes);
                currentPeroid++;
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
            formBuffer();
            wifiRequestData(MainDHCPActivity.curIPbytes);
        }
    }
    //==============================================================================================
    void receiveConfig(String resp)
    {
        String s;

        if((resp.indexOf("\n\r") == 10) && (resp.indexOf("C") == 0) )
        {
            resp = resp.substring(0, 10);
            s = resp.substring(1, 2);
            int msgNumb = Integer.parseInt(s);
            s = resp.substring(2, 3);
            int partsCount = Integer.parseInt(s);

            if(msgNumb == partsCount)
            {
                aStrings.add(resp.substring(3));
                bStrings.add(resp.substring(3));
                time = new String[aStrings.size()];
                temp = new String[bStrings.size()];
                time = aStrings.toArray(time);
                temp = bStrings.toArray(temp);

                for(int j = 0; j < time.length; j++)
                    time[j] = time[j].substring(0,2) + ":" + time[j].substring(2,4);
                for(int j = 0; j < temp.length; j++)
                {
                    String a1 = temp[j].substring(4, 6);
                    String a2 = temp[j].substring(6);
                    temp[j] = a1 + "." + a2;
                }

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
                aStrings.add(resp.substring(3));
                bStrings.add(resp.substring(3));
                REQUEST_ACTION = "CONF";
                if (sDayType.contains("Выходной")) REQUEST_ACTION += "H";
                else                               REQUEST_ACTION += "W";
                REQUEST_ACTION += String.valueOf(msgNumb+1);
                wifiRequestData(MainDHCPActivity.curIPbytes);
            }
        }
        else
        {
            wifiRequestData(MainDHCPActivity.curIPbytes);
        }

    }

}
