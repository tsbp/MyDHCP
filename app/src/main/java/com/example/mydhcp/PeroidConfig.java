package com.example.mydhcp;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;


public class PeroidConfig extends Activity /*implements OnClickListener*/ {


    TextView etTime;
    EditText etTemp;
    int myHour = 14;
    int myMinute = 35;
    int DIALOG_TIME = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peroid_config);



        final TextView etTime = (TextView)findViewById(R.id.setTime);
        final EditText etTemp = (EditText)findViewById(R.id.setTemp);

        Intent intent = getIntent();

        String time = intent.getStringExtra("pTime");
        String temp = intent.getStringExtra("pTemp");

        etTime.setText(time);
        etTemp.setText(temp);

        etTime.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(PeroidConfig.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        etTime.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        Button okBtn = (Button) findViewById(R.id.okBtnPeriod);
        okBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent();
                intent.putExtra("rTime", etTime.getText().toString());
                intent.putExtra("rTemp", etTemp.getText().toString());
                setResult(RESULT_OK, intent);
                finish();

            }
        });
    }



}
