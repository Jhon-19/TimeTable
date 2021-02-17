package com.jhony.timetable.mycourses;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

public class CourseView extends AppCompatTextView
            implements View.OnTouchListener {

    private static final String TAG = "CourseView";

    private GestureDetector mDetector;

    private int mCourseId;

    //设置手势监听器
    public void setCourseDetector(int courseId) {
        mCourseId = courseId;
        mDetector = new GestureDetector(getContext(), new MyCourseGestureListener(mCourseId));
        mDetector.setIsLongpressEnabled(true);
    }

    //获取courseId
    public int getCourseId() {
        return mCourseId;
    }

    public CourseView(Context context) {
        this(context, null);
    }

    @SuppressLint("ClickableViewAccessibility")
    public CourseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        super.setOnTouchListener(this);
        super.setClickable(true);
    }

    @SuppressLint({"ClickableViewAccessibility", "DrawAllocation"})
    @Override
    protected void onDraw(Canvas canvas) {

        //获取屏幕分辨率
        //单位为像素px,需转化为dp, dp = density*px
        float density = this.getResources().getDisplayMetrics().density;
        int width = this.getWidth();
        int height = this.getHeight();

        //解析文本
        String[] textArr = this.getText().toString().split("(\\$color\\$?)");
        int color = Integer.parseInt(textArr[0].substring(4), 16);//低版本先读取rgb信息
        int alpha = Integer.parseInt(textArr[0].substring(2, 4), 16);//再读取alpha信息
//        Log.i(TAG, color+" "+alpha);
        String text = textArr[1];
//        Log.i(TAG, Arrays.toString(textArr));

        //绘制背景
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(alpha);
        float radius = 10*density;
        canvas.drawRoundRect(density/2, density/2, width-density, height-density, radius, radius, paint);

        //绘制文本
        TextPaint textPaint = new TextPaint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(19*density);
        canvas.translate(0, 5*density);
        StaticLayout layout = new StaticLayout(text, textPaint, Math.round(width-density),
                Layout.Alignment.ALIGN_NORMAL, 1f, 0,
                false);
        layout.draw(canvas);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return mDetector.onTouchEvent(motionEvent);
    }
}
