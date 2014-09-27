package com.example.acer.wifidirecthostservice;



import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;


/**
 *
 */
public class RemoteScreen extends Activity
{

    public BlackView blackView;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        blackView = new BlackView(this);
        setContentView(blackView);
        Log.d("RemoteScreen", "Screen started");
        startService(new Intent(this, HostService.class).putExtra("PORT", 8988));
        blackView.setOnTouchListener(blackView);
        //Simulate();


    }

    public void Simulate()
    {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;
        float x = 230.0f;
        float y = 230.0f;
        MotionEvent motionEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
        blackView.dispatchTouchEvent(motionEvent);
        Log.d("Simulate", "Dispatched touch event");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        blackView.pause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        blackView.resume();
    }
}
