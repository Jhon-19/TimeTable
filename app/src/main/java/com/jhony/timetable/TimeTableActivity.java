package com.jhony.timetable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

public class TimeTableActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return TimeTableFragment.newInstance();
    }
}