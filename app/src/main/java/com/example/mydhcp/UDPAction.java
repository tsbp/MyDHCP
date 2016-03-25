package com.example.mydhcp;

import android.os.AsyncTask;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class UDPAction extends AsyncTask<Void, Void, Void> {

    public static String BROADCAST_ACTION;
    public static InetAddress hostIP;

    String ret = "";

    //==========================================================================================
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if      (MainDHCPActivity.mode == 3) BROADCAST_ACTION = settingsActivity.REQUEST_ACTION;
            else if (MainDHCPActivity.mode == 4) BROADCAST_ACTION = LanConfigActivity.REQUEST_ACTION;
            else                                 BROADCAST_ACTION = MainDHCPActivity.BC_ACTION;

            if(BROADCAST_ACTION.contains("I1"))
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.UK);
                Calendar cal = Calendar.getInstance();
                String timeString = dateFormat.format(cal.getTime());
                BROADCAST_ACTION += timeString;
            }
        }
        //==========================================================================================
        @Override
        protected Void doInBackground(Void... params) {

            DatagramSocket ds = null;
            try
            {
                ds = new DatagramSocket(7777);
                DatagramPacket dp;
                dp = new DatagramPacket(BROADCAST_ACTION.getBytes(),
                                        BROADCAST_ACTION.getBytes().length,
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
                    String modifiedSentence =
                            new String(receivePacket.getData());
                    InetAddress returnIPAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();
                    ret = returnIPAddress + ":" + port + "\r\ndata:" + modifiedSentence;
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
            if(ret.contains(BROADCAST_ACTION) && MainDHCPActivity.mode == 0)
                st = "its my ip";
            //info2.setText(st);

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
