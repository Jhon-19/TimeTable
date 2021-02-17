package com.jhony.timetable.mycourses;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.jhony.timetable.TimeTableFragment;

public class MyCourseGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final String TAG = "MyGestureListener";

    private int mCourseId;

    public MyCourseGestureListener(int courseId) {
        mCourseId = courseId;
    }

    @Override
    public void onLongPress(MotionEvent e) {
//        Log.i(TAG, "longPress");
        //根据courseId让TimeTableFragment绘制AlertDialog
        UpdateCourses.build().showCourseInfo(mCourseId);
    }
}
