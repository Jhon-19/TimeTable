package com.jhony.timetable.mycourses;

import android.annotation.SuppressLint;
import android.widget.CompoundButton;

import com.jhony.timetable.R;

public class MyCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
    //记录选中状态
    private boolean isShowCurrent;
    private boolean isShowUnCurrent;

    public MyCheckedChangeListener(boolean isShowCurrent, boolean isShowUnCurrent) {
        this.isShowCurrent = isShowCurrent;
        this.isShowUnCurrent = isShowUnCurrent;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
        UpdateCourses updateCourses = UpdateCourses.build();
        switch(checkBox.getId()){
            case R.id.show_current:
                isShowCurrent = isChecked;
                updateCourses.showCurrentCourse(isShowCurrent, isShowUnCurrent);
                break;
            case R.id.show_un_current:
                isShowUnCurrent = isChecked;
                updateCourses.showCurrentCourse(isShowCurrent, isShowUnCurrent);
                break;
        }
    }
}
