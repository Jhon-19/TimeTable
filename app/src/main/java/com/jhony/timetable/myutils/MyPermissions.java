package com.jhony.timetable.myutils;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * from https://blog.csdn.net/qq_30034925/article/details/70141728
 */
public class MyPermissions {

    //返回是否拥有该权限
    public static boolean isOwnPermisson(Activity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    //请求权限
    public static void requestPermission(Activity activity, String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(activity, permission)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{permission},//需要请求多个权限，可以在这里添加
                    requestCode);
        }
    }
}


