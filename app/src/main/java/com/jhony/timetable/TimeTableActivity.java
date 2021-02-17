package com.jhony.timetable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

public class TimeTableActivity extends SingleFragmentActivity {
    private static TimeTableFragment mFragment;

    @Override
    protected Fragment createFragment() {
        mFragment = (TimeTableFragment)TimeTableFragment.newInstance();
        return mFragment;
    }

    //外界使用fragment
    public static TimeTableFragment getFragment() {
        return mFragment;
    }
}