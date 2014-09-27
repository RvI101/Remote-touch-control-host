package com.example.acer.wifidirecthostservice;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;


/**
 * TODO: document your custom view class.
 */
public class BlackView extends SurfaceView implements Runnable, View.OnTouchListener{

    SurfaceHolder holder;
    Thread paintThread;
    boolean running = false;

    float tx;
    float ty;
    Paint mPaint;

    public BlackView(Context context) {
        super(context);
        holder = getHolder();
        tx = -100;
        ty = -100;
        mPaint = new Paint();
        mPaint.setColor(Color.CYAN);
    }

    public BlackView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public void resume()
    {
        paintThread = new Thread(this);
        running = true;
        paintThread.start();
    }
    public void pause()
    {
        running = false;
    }

    @Override
    public void run() {
        while(running)
        {
            if(!holder.getSurface().isValid())
                continue;
            Canvas canvas = holder.lockCanvas();
            canvas.drawARGB(120,0,0,0);
            canvas.drawCircle(tx, ty, 5, mPaint);
            holder.unlockCanvasAndPost(canvas);

        }
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        switch(motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                tx = motionEvent.getX();
                ty = motionEvent.getY();
                Log.d("onTouch", tx + "," + ty);
        }
        return false;
    }
}