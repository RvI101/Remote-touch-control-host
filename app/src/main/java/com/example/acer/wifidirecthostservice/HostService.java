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
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class HostService extends Service {

    static int PORT;
    static boolean server_running = true;
    public static ServerSocket serverSocket;
    NotificationCompat.Builder mBuilder;
    int mId = 13;

    public HostService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate()
    {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, RemoteScreen.class), PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.wifibeam).setContentTitle("ServerService running").setContentText("Starting").addAction(R.drawable.screen, "Screen", pendingIntent);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, ServiceActivity.class);
        //Intent closeIntent = new Intent(this)
        Log.d("HostService", "Service started");
        mBuilder.setOngoing(true);
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
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        PORT = intent.getExtras().getInt("PORT");
        try {
            serverSocket = new ServerSocket(PORT);
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder.setContentText("Socket opened");
            mNotificationManager.notify(mId, mBuilder.build());
            Log.d(ServiceActivity.TAG, "Server: Socket opened");

            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            String cmd = "/system/bin/input tap 230 230\n";
            for(int i = 1; i <= 10; i++)
                os.writeBytes(cmd);
            os.writeBytes("exit\n");
            os.flush();
            os.close();
            process.waitFor();

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {}


        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //Removing the persistent notification
        mNotificationManager.cancel(mId);
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
    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public static class ServerAsyncTask extends AsyncTask<Void, Void, String> {

        private final Context context;
        //private final TextView statusText;
        private String filename;
        /**
         * @param context
         */
        public ServerAsyncTask(Context context) {
            this.context = context;
            filename = Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/wifip2pshared.txt";

        }

        @Override
        protected String doInBackground(Void... params) {
            final File f = new File(filename);
            try {
                //May have to try and make this a persistent connection across the lifetime of the program to save on time
                while(server_running) {
                    Socket client = serverSocket.accept();
                    Log.d(ServiceActivity.TAG, "Server: connection done");



                    File dirs = new File(f.getParent());
                    if (!dirs.exists())
                        dirs.mkdirs();
                    f.createNewFile();

                    Log.d(ServiceActivity.TAG, "server: copying files ");
                    BufferedReader inputstream = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    BufferedWriter outputstream = new BufferedWriter(new FileWriter(f, true));
                    copyFile(inputstream, outputstream);
                    //String line = inputstream.readLine();
                    //Log.d("doinBack", line);
                    //serverSocket.close();
                    //server_running = false;
                }
                    return f.getAbsolutePath();
            } catch (IOException e) {
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
               try{
                   serverSocket.close();
               }catch(IOException e){
                   e.printStackTrace();
               }


                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "text/plain");
                context.startActivity(intent);


            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {

        }

    }
}
