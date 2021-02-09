package com.jhony.timetable;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jhony.timetable.myutils.RequestInfos;

import org.litepal.LitePal;

public class TimeTableFragment extends Fragment {
    private static final String TAG = "TimeTableFragment";

    //保存课表信息的数据库
    private SQLiteDatabase db;

    public static Fragment newInstance() {
        return new TimeTableFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_table, container, false);
        //Activity启动后再初始化数据库
        LitePal.initialize(getActivity());
        db = LitePal.getDatabase();

        //请求教务系统信息
        RequestInfos.build();

        return view;
    }


}
