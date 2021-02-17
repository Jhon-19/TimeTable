package com.jhony.timetable;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.jhony.timetable.mycourses.UpdateCourses;
import com.jhony.timetable.mydatas.KbData;
import com.jhony.timetable.mydatas.SjkData;
import com.jhony.timetable.myrequests.RequestInfos;
import com.jhony.timetable.myutils.MyBitmaps;
import com.jhony.timetable.myutils.MyParser;
import com.jhony.timetable.myutils.MyPermissions;
import com.jhony.timetable.myutils.MyPreferences;
import com.jhony.timetable.mycourses.CourseView;

import org.litepal.LitePal;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

/**
 * 管理用户设置信息
 */
public class TimeTableFragment extends Fragment {
    private static final String TAG = "TimeTableFragment";

    //背景
    private LinearLayout mBackground;

    //请求读取相册的请求码
    public static final int REQUEST_IMAGE_CODE = 2;

    //请求读取内存权限的请求码
    public static final int REQUEST_STORAGE_CODE = 3;

    public static Fragment newInstance() {
        return new TimeTableFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_table, container, false);
        //Activity启动后再初始化数据库
        LitePal.initialize(Objects.requireNonNull(getActivity()));
        //初始化背景
        mBackground = view.findViewById(R.id.background);
        setBackground();

        UpdateCourses.build(this.getActivity(), view).updateCourses();

        return view;
    }

    private void setBackground(){
        String uriStr = MyPreferences.getBackground(getActivity());
        if(uriStr != null){
            Uri uri = Uri.parse(uriStr);
            handleUriBitmap(uri, false);
        }else{
            Resources resources = getResources();
            Drawable drawable = resources.getDrawable(R.drawable.beach, null);
            mBackground.setBackground(drawable);
        }
    }

    //应用退出销毁实例
    @Override
    public void onDestroy() {
        super.onDestroy();
        UpdateCourses.destroyUc();
    }

    //请求相册图片
    private void chooseImages(){
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_IMAGE_CODE);
    }

    //请求权限
    private void requestPermission(){
        if(getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            chooseImages();
        }else{
            this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_CODE);
        }
    }

    //请求权限的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_STORAGE_CODE){
            if(grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED){
                chooseImages();
            }else{
                Toast.makeText(getActivity(), "请授予权限以访问相册...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //返回信息
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CODE){
            if(data != null){
                Uri originalUri = data.getData();
                handleUriBitmap(originalUri, true);
                if(data.getData() != null){
                    MyPreferences.setBackground(getActivity(), data.getData().toString());
                }
            }
        }
    }

    //处理Uri图片
    private void handleUriBitmap(Uri originalUri, boolean showToast){
        File file = null;
        if (originalUri != null) {
            file = MyBitmaps.getFileFromMediaUri(getActivity(), originalUri);
        }
        Bitmap photoBmp = null;
        try {
            photoBmp = MyBitmaps.getBitmapFormUri(getActivity(), Uri.fromFile(file));
            int degree = MyBitmaps.getBitmapDegree(file.getAbsolutePath());
            //把图片旋转为正的方向
            Bitmap newbitmap = MyBitmaps.rotateBitmapByDegree(photoBmp, degree);
            mBackground.setBackground(new BitmapDrawable(getResources(), newbitmap));
            if(showToast){
                Toast.makeText(getActivity(), "更换背景成功!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //用户信息对话框
    public void showUserInfos() {
        String userName = MyPreferences.getUserName(getActivity());
        String password = MyPreferences.getPassword(getActivity());

        //获取视图
        View userView = View.inflate(getActivity(), R.layout.dialog_user_info, null);

        EditText userNameText = userView.findViewById(R.id.user_name_text);
        EditText passwordText = userView.findViewById(R.id.password_text);
        Spinner spinner = userView.findViewById(R.id.current_week);
        Button importCourses = userView.findViewById(R.id.import_courses);
        Button addCourses = userView.findViewById(R.id.add_courses);
        Button resetTable = userView.findViewById(R.id.reset_table);
        Button changeBackground = userView.findViewById(R.id.change_background);

        //隐藏的视图
        HorizontalScrollView userCourse = userView.findViewById(R.id.user_course);
        EditText userCourseName = userView.findViewById(R.id.user_course_name);
        EditText userClassroom = userView.findViewById(R.id.user_classroom);
        Spinner userStartWeek = userView.findViewById(R.id.user_start_week);
        Spinner userEndWeek = userView.findViewById(R.id.user_end_week);
        Spinner userDay = userView.findViewById(R.id.user_day);
        Spinner userStartJoint = userView.findViewById(R.id.user_start_joint);
        Spinner userEndJoint = userView.findViewById(R.id.user_end_joint);

        //周数
        String[] weeks = new String[24];
        for(int i = 0; i < weeks.length; i++){
            weeks[i] = String.valueOf(i+1);
        }
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, weeks);
        userStartWeek.setAdapter(adapter2);
        userEndWeek.setAdapter(adapter2);
        //星期几
        String[] days = {"日", "一", "二", "三", "四", "五", "六"};
        ArrayAdapter<String> adapter3 = new ArrayAdapter<>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, days);
        userDay.setAdapter(adapter3);
        //节数
        String[] joints = new String[13];
        for(int i = 0; i < joints.length; i++){
            joints[i] = String.valueOf(i+1);
        }
        ArrayAdapter<String> adapter4 = new ArrayAdapter<>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, joints);
        userStartJoint.setAdapter(adapter4);
        userEndJoint.setAdapter(adapter4);

        //初始化name和password输入框以及下拉列表
        userNameText.setText(userName);
        passwordText.setText(password);
        String[] weeksArr = new String[25];
        weeksArr[0] = "假期";
        for (int i = 1; i < weeksArr.length; i++) {
            weeksArr[i] = "第" + i + "周";
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.support_simple_spinner_dropdown_item, weeksArr);
        spinner.setAdapter(adapter);

        //为按钮添加事件
        importCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = userNameText.getText().toString();
                String pw = passwordText.getText().toString();
                if(!name.isEmpty() && !pw.isEmpty()){
                    //请求教务系统信息
                    RequestInfos.build(name, pw, getActivity());
                    MyPreferences.setUserName(getActivity(), name);
                    MyPreferences.setPassword(getActivity(), pw);
                    MyPreferences.setHasCourses(getActivity(), true);
                }
            }
        });
        addCourses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userCourse.setVisibility(View.VISIBLE);
            }
        });
        resetTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LitePal.deleteAll(KbData.class);
                LitePal.deleteAll(SjkData.class);
                MyPreferences.setHasCourses(getActivity(), false);
                Toast.makeText(getActivity(), "重置成功", Toast.LENGTH_SHORT).show();
            }
        });
        changeBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimeTableActivity.getFragment().requestPermission();
            }
        });

        //弹出用户设置对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("用户设置")
                .setView(userView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //设置起始周
                        int position = spinner.getSelectedItemPosition();
                        int startWeek;
                        if(position == 0){
                            startWeek = 0;
                        }else{
                            int currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
                            startWeek = currentWeek-position+1;
                        }
                        MyPreferences.setStartWeek(getActivity(), startWeek);

                        //将添加的课程存入数据库
                        String courseName = userCourseName.getText().toString();
                        String classroom = userClassroom.getText().toString();
                        int startWeekId = userStartWeek.getSelectedItemPosition()+1;
                        int endWeekId = userEndWeek.getSelectedItemPosition()+1;
                        int dayId = userDay.getSelectedItemPosition();
                        int startJointId = userStartJoint.getSelectedItemPosition()+1;
                        int endJointId = userEndJoint.getSelectedItemPosition()+1;

                        if(!courseName.isEmpty() && !classroom.isEmpty()){
                            KbData data = new KbData();
                            data.setKcmc(courseName);
                            data.setCdmc(classroom);
                            //防止起始周在后
                            if(startWeekId > endWeekId){
                                int temp = startWeekId;
                                startWeekId = endWeekId;
                                endWeekId = temp;
                            }
                            data.setZcd(startWeekId+"-"+endWeekId+"周");
                            data.setXqjmc(MyParser.parseDay(dayId));
                            //防止起始节数在后
                            if(startJointId > endJointId){
                                int temp = startJointId;
                                startJointId = endJointId;
                                endJointId = temp;
                            }
                            data.setJc(startJointId+"-"+endJointId+"节");
                            data.save();
                            MyPreferences.setHasCourses(getActivity(), true);
                        }
                        UpdateCourses.build().updateCourses();
                    }
                })
                .create()
                .show();
    }
}
