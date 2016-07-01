package com.example.mydhcp;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UDPAction extends AsyncTask<Void, Void, Void> {


    public static byte[] _BROADCAST_ACTION;
    public static InetAddress hostIP;
    public static byte [] answer;

    String ret = "";
    byte[] pack;

    //==========================================================================================
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            switch(Protocol.mode)
            {
                case Protocol.ESP8266_SEARCH:
                case Protocol.ESP8266_DATA_RECEIVE:
                    _BROADCAST_ACTION = MainDHCPActivity._BC_ACTION;
                    if(_BROADCAST_ACTION[0] == Protocol.PLOT_DATA)
                    {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddHHmmss", Locale.UK);
                        Calendar cal = Calendar.getInstance();
                        String timeString = dateFormat.format(cal.getTime());

                        byte[] timeBuf = new byte[6];
                        for(int i = 0; i < 6; i++)
                            timeBuf[i] = (byte)Integer.parseInt(timeString.substring(i*2,(i*2 +2)));

                        pack = new byte[_BROADCAST_ACTION.length + timeBuf.length];

                        for(int i = 0; i < _BROADCAST_ACTION.length; i++)
                            pack[i] = _BROADCAST_ACTION[i];

                        for(int i = 0; i <timeBuf.length; i++)
                            pack[i + _BROADCAST_ACTION.length] = timeBuf[i];
                    }
                    break;

                case Protocol.ESP8266_CONFIG:
                    pack = settingsActivity._BC_ACTION;
                    break;

                case Protocol.ESP8266_LANSET:
                    pack = LanConfigActivity.REQUEST_ACTION.getBytes();
                    break;

                case Protocol.ESP8266_UST:
                    pack = ustanovki.REQUEST_ACTION;
                    break;
            }
        }
        //==========================================================================================
        private InetAddress returnIPAddress;

        //==========================================================================================
        @Override
        protected Void doInBackground(Void... params) {

            answer = null;
            DatagramSocket ds = null;
            try
            {
                ds = new DatagramSocket(7777);
                DatagramPacket dp;
                dp = new DatagramPacket(pack,
                                        pack.length,
                                        hostIP, 7777);
                ds.setBroadcast(true);
                ds.send(dp);
                //===================
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket =
                        new DatagramPacket(receiveData, receiveData.length);

                ds.setSoTimeout(2000);

                try {
                    ds.receive(receivePacket);
//                    String modifiedSentence =
//                            new String(receivePacket.getData());
                    answer = receivePacket.getData();
                    returnIPAddress = receivePacket.getAddress();
//                    int port = receivePacket.getPort();
                    //ret = returnIPAddress + ":" + port + "\r\ndata:" + modifiedSentence;
                    ret = new String(answer);
                }
                catch (SocketTimeoutException ste)
                {
                    //System.out.println("Timeout Occurred: Packet assumed lost");
                    ret = "NO DATA";
                }
                //===================
                ds.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (ds != null)
                {
                    ds.close();
                }
            }
            return null;
        }
        //==============================================================================================
        private OnTaskExecutionFinished _task_finished_event;
        //==============================================================================================
        public interface OnTaskExecutionFinished
        {
            public void OnTaskFihishedEvent(String Reslut);
        }
        //==============================================================================================
        public void setOnTaskFinishedEvent(OnTaskExecutionFinished _event)
        {
            if(_event != null)
            {
                this._task_finished_event = _event;
            }
        }
        //==============================================================================================
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            String st = ret;
            if(returnIPAddress == hostIP)
                st = "its my ip";
//            //info2.setText(st);

            if(this._task_finished_event != null)
            {
                this._task_finished_event.OnTaskFihishedEvent(st);
            }
//            else
//            {
//                Log.d("SomeClass", "task_finished even is null");
//            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

        }




}
