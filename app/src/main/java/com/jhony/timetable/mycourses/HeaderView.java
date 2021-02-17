package com.jhony.timetable.mycourses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;

public class HeaderView extends AppCompatTextView
            implements View.OnTouchListener{
    private static final String TAG = "HeaderView";

    private GestureDetector mDetector;

    public HeaderView(Context context) {
        this(context, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setClickable(true);
        super.setOnTouchListener(this);
        mDetector = new GestureDetector(context, new MyHeaderGestureListener());
        mDetector.setIsLongpressEnabled(true);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
//        Log.i(TAG, "yes");
        return mDetector.onTouchEvent(motionEvent);
    }

    //绘制成按钮形
    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(0x3700B3);
        paint.setAlpha(100);

        float density = getResources().getDisplayMetrics().density;

        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), 9*density, 9*density, paint);
        canvas.save();
        super.onDraw(canvas);
        canvas.restore();
    }
}
