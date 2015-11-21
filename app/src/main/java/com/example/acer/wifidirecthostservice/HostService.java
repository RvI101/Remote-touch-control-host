package com.example.acer.wifidirecthostservice;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.otto.ThreadEnforcer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HostService extends Service implements Runnable {

    static boolean cursormode = false;
    static int PORT;
    public static Bus bus;
    static boolean server_running = true;
    public static ServerSocket serverSocket;
    NotificationCompat.Builder mBuilder;
    int mId = 13;
    DisplayMetrics dm = new DisplayMetrics();
    static int width = 1200;
    static int height = 1900;

    public HostService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, RemoteScreen.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.wifibeam).setContentTitle("ServerService running").setContentText("Starting").addAction(R.drawable.screen, "Screen", pendingIntent);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, ServiceActivity.class);
        //Intent closeIntent = new Intent(this)
        Log.d("HostService", "Service started");
        mBuilder.setOngoing(true);
        bus = new Bus(ThreadEnforcer.ANY);
        bus.register(this);
        // The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ServiceActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(mId, mBuilder.build());
        Intent i = new Intent();
        i.setAction("com.example.acer.wifidirecthostservice.CursorService");
        startService(i);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PORT = intent.getExtras().getInt("PORT");
        //width = intent.getExtras().getInt("WIDTH");
        //height = intent.getExtras().getInt("HEIGHT");
        Log.d("width, height", width + "x" + height);//both are zero from intent, needs to be investigated
        try {
            serverSocket = new ServerSocket(PORT);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder.setContentText("Socket opened");
            mNotificationManager.notify(mId, mBuilder.build());
            Log.d(ServiceActivity.TAG, "Server: Socket opened");
            new ServerAsyncTask(this).execute();
            //sthread = new Thread(this);
            //sthread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return START_STICKY;
    }
    @Subscribe
    public void injection(String s)
    {
        Log.d("inject","called new async task");
        new InjectAsyncTask(this, s).execute();
    }
    @Override
    public void onDestroy() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //Removing the persistent notification
        mNotificationManager.cancel(mId);
        Intent i = new Intent();
        i.setAction("com.example.acer.wifidirecthostservice.CursorService");
        stopService(i);
    }

    public static boolean copyFile(BufferedReader inputStream, BufferedWriter out) {
        String line;
        int len;
        Log.d("copyFile", "Right before copying loop");
        try {
            while ((line = inputStream.readLine()) != null) {
                out.write(line);
                Log.d("copyFile", line);
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(ServiceActivity.TAG, e.toString());
            return false;
        }
        return true;
    }
    private void cmdTurnCursorServiceOn() {
        Intent i = new Intent();
        i.setAction("com.example.acer.wifidirecthostservice.CursorService");
        startService(i);
    }

    private void cmdTurnCursorServiceOff() {
        Intent i = new Intent();
        i.setAction("com.example.acer.wifidirecthostservice.CursorService");
        stopService(i);
    }

    public static void cmdShowCursor() {
        if (Singleton.getInstance().m_CurService != null)
            Singleton.getInstance().m_CurService.ShowCursor(true);
    }

    public static void cmdHideCursor() {
        if (Singleton.getInstance().m_CurService != null)
            Singleton.getInstance().m_CurService.ShowCursor(false);
    }
    @Override
    public void run() {
//Not called
        try {
            Thread.sleep(1000);
            List<String> envList = new ArrayList<String>();
            Map<String, String> envMap = System.getenv();
            for (String envName : envMap.keySet()) {
                envList.add(envName + "=" + envMap.get(envName));
            }
            String[] environment = envList.toArray(new String[0]);
            String command = "/system/bin/input swipe 250 450 720 600\n" +
                    "/system/bin/input swipe 250 450 800 700";
            try {
                Runtime.getRuntime().exec(
                        new String[]{"su", "-c", command},
                        environment);
            } catch (IOException e) {
                Log.e("Error", Log.getStackTraceString(e));
            }

        } catch (Exception e) {
        }
    }

    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    //public static class SimulateAsyncTask
    public static class ServerAsyncTask extends AsyncTask<Void, Void, String>{

        private final Context context;
        //private final TextView statusText;
        private String filename;
        float x;
        float y;
        int action;
        static int iteration = 0;
        float x0;
        float y0;
        String line;
        boolean cursormode = false;
        Thread athread;




        /**
         * @param context
         */
        public ServerAsyncTask(Context context) {
            this.context = context;
            filename = Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/wifip2pshared.txt";

            x = 0f;
            y = 0f;


        }

        @Override
        protected String doInBackground(Void... params) {
            //final File f = new File(filename);
            try {
                //May have to try and make this a persistent connection across the lifetime of the program to save on time
                while (server_running) {
                    Socket client = serverSocket.accept();
                    Log.d("Server", "Server: connection done");
                    BufferedReader inputstream = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    line = inputstream.readLine();
                    bus.post(line);
                    //new InjectAsyncTask(line).execute();
                    //Thread inject = new Thread(this);
                    //inject.start();
                   /* String parts[] = line.split(" ");
                    x = Float.parseFloat(parts[0]) * width;
                    y = Float.parseFloat(parts[1]) * height;
                    action = Integer.parseInt(parts[2]);
                    Log.d("Server: x y action", x / width + "," + y / height + "," + action);

                    iteration++;
                    //synchronized (athread)
                    /*{
                        athread.notify();
                    }*/
                    /*if(cursormode == true)
                    {
                        Singleton.getInstance().m_CurService.Update((int)x, (int)y, true);

                    }
                    if(x<0 && y<0)
                    {
                        cmdShowCursor();
                        Singleton.getInstance().m_CurService.Update((int)-x, (int)-y, true);
                        cursormode = true;
                    }
                    if(action == 2) {
                        cursormode = false;
                        cmdHideCursor();
                    }
                    String command[];*/
               /* if(iteration % 2 == 0)
                {
                    x0 = x;
                    y0 = y;
                }*/

                    //command = "/system/bin/input swipe " + x + " " + y + " " + x0 + " " + y0 + "\n";

                    //inject.join();

                    //BufferedReader inputstream = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    //BufferedWriter outputstream = new BufferedWriter(new FileWriter(f, true));
                    //copyFile(inputstream, outputstream);
                    //serverSocket.close();
                    //server_running = false;
                }
                return "Success";
            } catch (Exception e) {
                Log.e(ServiceActivity.TAG, e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            //Need to implement a more dynamic and fluid way to view the data sent, i.e, a mirrored remote screen or a textview
            if (result != null) {
                //StringBuilder builder = new StringBuilder(statusText.getText());
                //builder.append(" " + result);
                //statusText.setText(builder.toString());
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            //athread.start();
        }



     /*   @Override
        public void run() {
            while (true) {

                try {
                    synchronized (athread) {
                       athread.wait(iteration);
                    }
                    String command[];
                    //command = "/system/bin/input swipe " + x + " " + y + " " + x0 + " " + y0 + "\n";
                   // command = getCommand(action);
                    Log.d("At end of generated swipe", "At" + System.currentTimeMillis());
                    for (int i = 0; i < command.length; i++) {
                        try {
                            Runtime.getRuntime().exec(
                                    new String[]{"su", "-c", command[i]},
                                    environment);

                        } catch (IOException e) {
                            Log.e("Error", Log.getStackTraceString(e));
                        }
                    }

                } catch (Exception e) {
                }
            }
        }*/
    }

    public static class InjectAsyncTask extends AsyncTask<Void, Void, String> {

        String line;
        float x;
        float y;
        int action;
        int iteration = 0;
        List<String> envList;
        Map<String, String> envMap;
        String[] environment;

        /**
         * @param context
         * @param command
         */
        public InjectAsyncTask(Context context, String command) {
            line = command;
            envList = new ArrayList<String>();
            envMap = System.getenv();
            for (String envName : envMap.keySet()) {
                envList.add(envName + "=" + envMap.get(envName));
            }
            environment = envList.toArray(new String[0]);
        }

        public String[] getCommand(int action) {
            String command[];
            switch (action) {
                case 0://down
                    command = new String[]{"sendevent /dev/input/event0 3 57 " + iteration + "\n", "sendevent /dev/input/event0 3 53 " + x + "\n",
                            "sendevent /dev/input/event0 3 54 " + y + "\n", "sendevent /dev/input/event0 0 0 0\n"};
                    Log.d("down", x + "," + y);
                    break;
                case 1://move/drag
                    command = new String[]{"sendevent /dev/input/event0 3 53 " + x + "\n", "sendevent /dev/input/event0 3 54 " + y + "\n",
                            "sendevent /dev/input/event0 0 0 0\n"};
                    Log.d("move", x + "," + y);
                    break;
                case 2://up
                    command = new String[]{"sendevent /dev/input/event0 3 57 -1\n", "sendevent /dev/input/event0 0 0 0\n"};
                    Log.d("up", x + "," + y);
                    break;
                default:
                    command = new String[]{"sendevent /dev/input/event0 3 53 " + x + "\n", "sendevent /dev/input/event0 3 54 " + y + "\n",
                            "sendevent /dev/input/event0 0 0 0\n"};
            }
            return command;
        }

        @Override
        protected void onPreExecute() {
            Log.d("Pre","PreExecute");
            doInBackground();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {

                Log.d("Wot","How");
                String parts[] = line.split(" ");
                x = Float.parseFloat(parts[0]) * width;
                y = Float.parseFloat(parts[1]) * height;
                action = Integer.parseInt(parts[2]);
                Log.d("Server: x y action", x / width + "," + y / height + "," + action);

                iteration++;
                //synchronized (athread)
                    /*{
                        athread.notify();
                    }*/
                if (cursormode == true) {
                    Singleton.getInstance().m_CurService.Update((int) x, (int) y, true);

                }
                if (x < 0 && y < 0) {
                    cmdShowCursor();
                    Singleton.getInstance().m_CurService.Update((int) -x, (int) -y, true);
                    cursormode = true;
                }
                if (action == 2) {
                    cursormode = false;
                    cmdHideCursor();
                }
                String command[];
                command = getCommand(action);
                //Log.d("At end of generated swipe", "At" + System.currentTimeMillis());
                for (int i = 0; i < command.length; i++) {
                    try {
                        Runtime.getRuntime().exec(
                                new String[]{"su", "-c", command[i]},
                                environment);

                    } catch (IOException e) {
                        Log.e("Error", Log.getStackTraceString(e));
                    }
                }

            } catch (Exception e) {
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result)
        {
            Log.d("Why","This can't be");
        }


    }
}



