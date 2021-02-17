package com.jhony.timetable.mycourses;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class TimespanView extends androidx.appcompat.widget.AppCompatTextView {

    public TimespanView(Context context) {
        super(context);
    }

    public TimespanView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //获取density
        float density = getResources().getDisplayMetrics().density;
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);

        //第一行加粗，每行颜色不同
        paint.setTextSize(18*density);
        String[] textArr = this.getText().toString().split("\n");
        String index = textArr[0];
        String startTime = textArr[1];
        String stopTime = textArr[2];
        int width = this.getWidth();
        int height = this.getHeight();

        paint.setColor(0xFF82DAFD);
        paint.setStrokeWidth(9);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText(index, width/2, height*2/7, paint);

        //设置时间周期字体
        paint.setTextSize(14*density);
        paint.setColor(0xFF8288FD);
        paint.setStrokeWidth(5);
        paint.setTypeface(Typeface.DEFAULT);
        canvas.drawText(startTime, width/2, height*4/7, paint);
        paint.setColor(0xFF4F17E6);
        canvas.drawText(stopTime, width/2, height*6/7, paint);
    }
}
