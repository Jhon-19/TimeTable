package com.jhony.timetable.mycourses;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.jhony.timetable.TimeTableActivity;

public class MyHeaderGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final String TAG = "MyHeaderGestureListener";

    //长按弹出设置框
    @Override
    public void onLongPress(MotionEvent e) {
//        Log.i(TAG, "longPress");
        TimeTableActivity.getFragment().showUserInfos();
    }

    //双击选择周数等信息
    @Override
    public boolean onDoubleTap(MotionEvent e) {
//        Log.i(TAG, "doubleTap");
        UpdateCourses.build().showChangeWeek();
        return true;
    }
}
