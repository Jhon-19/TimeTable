package com.jhony.timetable.myutils;

import android.content.Context;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.widget.CheckBox;

import java.util.Calendar;

public class MyPreferences {
    private static final String PREF_HAS_COURSES = "hasCourses";//是否含有课程信息
    private static final String PREF_START_WEEK = "startWeek";//记录起始周
    private static final String PREF_USER_NAME = "userName";//用户名
    private static final String PREF_PASSWORD = "password";//密码
    private static final String PREF_BACKGROUND = "background";//背景图片Uri

    //存储是否含有课程信息标志
    public static void setHasCourses(Context context, boolean hasCourses){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_HAS_COURSES, hasCourses)
                .apply();
    }

    //读取是否含有课程信息标志
    public static boolean getHasCourses(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_HAS_COURSES, false);
    }

    //存储开始周(相对于年)
    public static void setStartWeek(Context context, int startWeek){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_START_WEEK, startWeek)
                .apply();
    }

    //读取开始周(相对于年), 0表示假期
    public static int getStartWeek(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_START_WEEK, 0);
    }

    //设置颜色id的位置
    public static void setCourseColor(Context context, String courseName, int colorId){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(courseName, colorId)
                .apply();
    }

    //获取颜色id的位置
    public static int getColorId(Context context, String courseId){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(courseId, -1);
    }

    //存储用户设置的显示项
    public static void setItemsChecked(Context context, String courseId, String itemsChecked){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(courseId, itemsChecked)
                .apply();
    }

    //获取用户设置的显示项, 坐标连成的数组
    public static String getItemsChecked(Context context, String courseId){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(courseId, "-1");
    }

    //设置用户名
    public static void setUserName(Context context, String userName){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_USER_NAME, userName)
                .apply();
    }
    //获取用户名
    public static String getUserName(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_USER_NAME, null);
    }
    //设置密码
    public static void setPassword(Context context, String password){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_PASSWORD, password)
                .apply();
    }
    //获取密码
    public static String getPassword(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_PASSWORD, null);
    }

    //保存背景图片Uri
    public static void setBackground(Context context, String uri){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_BACKGROUND, uri)
                .apply();
    }
    //读取背景图片Uri
    public static String getBackground(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_BACKGROUND, null);
    }
}
