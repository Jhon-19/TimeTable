package com.jhony.timetable.mycourses;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jhony.timetable.R;
import com.jhony.timetable.TimeTableActivity;
import com.jhony.timetable.mydatas.KbData;
import com.jhony.timetable.mydatas.SjkData;
import com.jhony.timetable.myrequests.RequestInfos;
import com.jhony.timetable.myutils.MyParser;
import com.jhony.timetable.myutils.MyPreferences;

import org.litepal.LitePal;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 管理课程信息
 */
public class UpdateCourses {
    private static final String TAG = "UpdateCourses";

    private final Context mContext;
    //课表视图
    private final View mView;
    //屏幕像素密度
    private float mDensity;
    //每一列的宽度
    private float mColumnWidth;
    //当前周数
    private int mWeekIndex;
    //颜色id数组
    private final int[] colorId = {R.color.green, R.color.orange_dark, R.color.purple_dark,
            R.color.yellow, R.color.blue, R.color.pink, R.color.gray};
    //课程数据列表
    private static List<KbData> mKbDataList;
    private List<SjkData> mSjkDataList;
    //课表
    private final GridLayout mCourseTable;
    //courseView哈希表
    private HashMap<Integer, CourseView> mViewMap;
    //存放课程id的二维数组
    private int[][] mCourses;
    //记录周数列表显示与隐藏的状态
    private boolean isListShow;
    //周数列表
    private RecyclerView mWeekList;
    //实验课列表
    private RecyclerView mSjkList;
    //周数布局
    private LinearLayout mChangeListLayout;

    //周数adapter
    private WeekAdapter mWeekAdapter;
    //实践课adapter
    private SjkAdapter mSjkAdapter;

    //显示本周课程
    private CheckBox mShowCurrentBox;
    //显示非本周课程
    private CheckBox mShowUnCurrentBox;
    //记录选中状态
    private boolean isShowCurrent;
    private boolean isShowUnCurrent;

    //单例模式
    @SuppressLint("StaticFieldLeak")
    private static UpdateCourses updateCourses = null;

    private UpdateCourses(Context context, View view) {
        mContext = context;
        mView = view;
        mCourseTable = view.findViewById(R.id.course_table);
        //初始化RecyclerView
        mWeekList = view.findViewById(R.id.week_list);
        mSjkList = view.findViewById(R.id.sjk_list);
        mWeekList.setLayoutManager(
                new LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false));
        mSjkList.setLayoutManager(new LinearLayoutManager(mContext));

        mChangeListLayout = view.findViewById(R.id.change_list);
        mViewMap = new HashMap<>();
        mCourses = new int[14][8];
        isListShow = false;
        resetCourses();

        //获取课表属性
        getColumnWidth();

        //设置周列表视图
        setWeekList();
        //设置实践课列表
        setSjkList();

        //设置本周和非本周课程checkbox
        mShowCurrentBox = view.findViewById(R.id.show_current);
        mShowUnCurrentBox = view.findViewById(R.id.show_un_current);
        initCheckbox();
    }

    //初始化显示本周和非本周的课程的checkbox
    private void initCheckbox(){
        isShowCurrent = true;
        isShowUnCurrent = true;
        mShowCurrentBox.setChecked(true);
        mShowUnCurrentBox.setChecked(true);
        MyCheckedChangeListener checkedChangeListener =
                new MyCheckedChangeListener(isShowCurrent, isShowUnCurrent);

        //状态改变则更新视图
        mShowCurrentBox.setOnCheckedChangeListener(checkedChangeListener);
        mShowUnCurrentBox.setOnCheckedChangeListener(checkedChangeListener);
    }

    //设置周数列表
    private void setWeekList(){
        String[] weeks = new String[24];
        for(int i = 0; i < weeks.length; i++){
            weeks[i] = "第"+(i+1)+"周";
        }
        mWeekAdapter = new WeekAdapter(weeks);
        mWeekList.setAdapter(mWeekAdapter);
    }

    //设置实践课信息列表
    private void setSjkList(){
        mSjkDataList = new ArrayList<>();
        mSjkAdapter = new SjkAdapter(mSjkDataList);
        mSjkList.setAdapter(mSjkAdapter);
    }

    //实践课信息改变的修改
    private void invalidateSjkList(){
        mSjkDataList.removeAll(mSjkDataList);
        mSjkDataList.addAll(LitePal.findAll(SjkData.class));
        mSjkAdapter.notifyDataSetChanged();
    }

    //周数的ViewHolder
    private class WeekHolder extends RecyclerView.ViewHolder
                            implements View.OnClickListener {
        private TextView mWeekText;
        private String mWeek;

        public WeekHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.weeks_list_item, parent, false));

            //itemView为ViewHolder内置view
            mWeekText = itemView.findViewById(R.id.week_item);
            itemView.setOnClickListener(this);
        }

        public void bind(String week){
            mWeek = week;
            mWeekText.setText(mWeek);
        }

        @Override
        public void onClick(View view) {
            //ViewHolder获取列表中的位置
            int position = this.getLayoutPosition();
//            Log.i(TAG, String.valueOf(position));
            //计算偏移量
            int rawIndex = getRawCurrentIndex();
            int offset = position-rawIndex+1;
            changeWeek(offset);
        }
    }

    //周数的adapter
    private class WeekAdapter extends RecyclerView.Adapter<WeekHolder>{
        private String[] mWeeks;

        public WeekAdapter(String[] weeks) {
            mWeeks = weeks;
        }

        @NonNull
        @Override
        public WeekHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            return new WeekHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull WeekHolder holder, int position) {
            String week = mWeeks[position];
            holder.bind(week);
        }

        @Override
        public int getItemCount() {
            return mWeeks.length;
        }
    }

    //实践课ViewHolder
    private class SjkHolder extends RecyclerView.ViewHolder{
        private TextView mSjkText;
        private String mSjkInfo;

        public SjkHolder(LayoutInflater inflater, ViewGroup parent){
            super(inflater.inflate(R.layout.sjk_list_item, parent, false));

            mSjkText = itemView.findViewById(R.id.sjk_item);
        }

        public void bind(String sjkInfo){
            mSjkInfo = sjkInfo;
            mSjkText.setText(mSjkInfo);
        }
    }

    //实践课adapter
    private class SjkAdapter extends RecyclerView.Adapter<SjkHolder>{
        private List<SjkData> mSjkDataList;

        public SjkAdapter(List<SjkData> sjkDataList) {
            mSjkDataList = sjkDataList;
        }

        @NonNull
        @Override
        public SjkHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            return new SjkHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull SjkHolder holder, int position) {
            SjkData data = mSjkDataList.get(position);
            String sjkInfo = data.getKcmc()+" "+data.getJsxm();
            holder.bind(sjkInfo);
        }

        @Override
        public int getItemCount() {
            return mSjkDataList.size();
        }
    }

    //将数组初值置为-1
    private void resetCourses() {
        for (int i = 0; i < 14; i++) {
            for (int j = 0; j < 8; j++) {
                mCourses[i][j] = -1;
            }
        }
    }

    //唯一构造函数
    public static UpdateCourses build(Context context, View view) {
        if (updateCourses == null) {
            updateCourses = new UpdateCourses(context, view);
        }
        return updateCourses;
    }

    //获取唯一实例
    public static UpdateCourses build() {
        return updateCourses;
    }

    //应用退出则销毁实例
    public static void destroyUc() {
        updateCourses = null;
    }

    //刷新表头
    private void refreshHeader(View view, int weekOffset) {
        //从数据库中获取课表信息
        if(MyPreferences.getHasCourses(mContext)){
            mKbDataList = LitePal.findAll(KbData.class);
        }else{
            mKbDataList = new ArrayList<>();
        }
        Calendar currentTime = Calendar.getInstance();

        mWeekIndex = getRawCurrentIndex() + weekOffset;
        //根据偏移量日期后移
        currentTime.add(Calendar.DAY_OF_YEAR, 7 * weekOffset);
        //设置第x周
        HeaderView weekHeader = view.findViewById(R.id.week_index);
        weekHeader.setText("\n第" + mWeekIndex + "周");

        //表头id数组
        int[] daysId = {R.id.sunday, R.id.monday, R.id.tuesday, R.id.wednesday, R.id.thursday,
                R.id.friday, R.id.saturday};
        //确定当天是星期几
        int day = currentTime.get(Calendar.DAY_OF_WEEK) - 1;//day >= 0 && day <= 6
        TextView dayHeader = view.findViewById(daysId[day]);
        dayHeader.setText(getDate(currentTime, day));
        dayHeader.setBackgroundColor(mContext.getResources().getColor(R.color.blue, null));
        //设置每天的日期
        int index = day;
        index--;
        while (index >= 0) {
            currentTime.add(Calendar.DAY_OF_YEAR, -1);
            dayHeader = view.findViewById(daysId[index]);
            dayHeader.setText(getDate(currentTime, index));
            index--;
        }
        currentTime.add(Calendar.DAY_OF_YEAR, day);//回到当前时间
        index = day;
        index++;
        while (index <= 6) {
            currentTime.add(Calendar.DAY_OF_YEAR, 1);
            dayHeader = view.findViewById(daysId[index]);
            dayHeader.setText(getDate(currentTime, index));
            index++;
        }
        //日期复原
        currentTime.add(Calendar.DAY_OF_YEAR, 6 - day);
        currentTime.add(Calendar.DAY_OF_YEAR, -7 * weekOffset);

        if(mKbDataList.size() != 0){
            //为每门课设置颜色
            setColors();

            //查找并添加课程
            findCourse();
        }else{
            deleteOldInfo();
        }
        invalidateSjkList();
    }

    //获得未偏移的当前周数
    private int getRawCurrentIndex(){
        Calendar currentTime = Calendar.getInstance();
        int startWeek = MyPreferences.getStartWeek(mContext);
        if(startWeek == 0){
            startWeek = currentTime.get(Calendar.WEEK_OF_YEAR);
        }
        int currentWeek = currentTime.get(Calendar.WEEK_OF_YEAR);
        return currentWeek-startWeek+1;
    }

    //设置颜色
    private void setColors() {
        int index = 0;
        for (KbData data : mKbDataList) {
            if (MyPreferences.getColorId(mContext, data.getKcmc()) == -1) {
                MyPreferences.setCourseColor(mContext, data.getKcmc(), index);
                index++;
                index = index % 6;
            }
        }
    }

    //获取日期，返回周x月/日
    private String getDate(Calendar calendar, int day) {
        int month = calendar.get(Calendar.MONTH) + 1;
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String dayStr = MyParser.parseDay(day);

        return dayStr + "\n \n"+ month + "/" + dayOfMonth;
    }

    //查找并添加课程
    private void findCourse() {
        int startId = mKbDataList.get(0).getId();

        deleteOldInfo();

        //先显示非本周课程
        if(isShowUnCurrent){
            displayCourses(startId, false);
        }
        //再显示本周课程
        if(isShowCurrent){
            displayCourses(startId, true);
        }
    }

    //删除已有信息
    private void deleteOldInfo() {
        if(mViewMap.size() != 0){
            for (CourseView view : mViewMap.values()) {
                mCourseTable.removeView(view);
            }
            //迭代器删除所有元素
            Iterator<Map.Entry<Integer, CourseView>> iterator = mViewMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, CourseView> item = iterator.next();
                iterator.remove();
            }
        }
    }

    //显示课程信息
    private void displayCourses(int startId, boolean isCurrentWeek) {
        for (KbData data : mKbDataList) {
            String zcd = data.getZcd();//周数
            int[] weeks = MyParser.parseWeekspan(zcd);//确定周
            boolean flag = mWeekIndex >= weeks[0] && mWeekIndex <= weeks[1];//非本周
            if (isCurrentWeek) {
                flag = !flag;
            }
            if (flag) {
                continue;
            }
            String kcmc = data.getKcmc();//课程名
            String cdmc = data.getCdmc();//教室
            String kcxz = data.getKcxz();//课程性质
            String jc = data.getJc();//节数
            String kcxszc = data.getKcxszc();//课程学识
            String xm = data.getXm();//教师名称
            String zcmc = data.getZcmc();//教师职称
            String khfsmc = data.getKhfsmc();//课后实践
            String xqjmc = data.getXqjmc();//星期几
            String[] datasArr = {kcmc, cdmc, kcxz, jc, zcd, kcxszc, xm, zcmc, khfsmc, xqjmc};

            int day = MyParser.parseWeekday(xqjmc)+1;//确定列
            int[] times = MyParser.parseTimespan(jc);//确定行
            int courseId = data.getId() - startId;
            //添加课程
            //添加课程信息
            StringBuilder builder = new StringBuilder();
            if (!isCurrentWeek) {
                builder.append(" [非本周]");
            }
            String itemStr = MyPreferences.getItemsChecked(mContext, String.valueOf(courseId));
            if (!itemStr.contains("0")) {
                builder.append(kcmc).append(" @").append(cdmc);
            } else {
                int[] items = MyParser.parseItemsChecked(itemStr);
                for (int index : items) {
                    if (index == 1) {
                        builder.append("@");
                    }
                    builder.append(datasArr[index]).append(" ");
                }
            }

            String courseInfo = builder.toString();
            int colorId = 6;
            if (isCurrentWeek) {
                colorId = MyPreferences.getColorId(mContext, kcmc);
            }
            String color = getColorRGB(colorId);
            String text = generateCourseInfo(color, courseInfo);
            addCourse(times[0], day, times[1], text, courseId);
        }
    }

    //获取颜色的rgb字符串
    private String getColorRGB(int index) {
        Resources resources = mContext.getResources();
        int colorRGB = resources.getColor(colorId[index], null);
        BigInteger temp = new BigInteger(String.valueOf(colorRGB));
        temp = temp.add(new BigInteger("100000000", 16));
        return temp.toString(16).substring(2);
    }

    //课程信息第一行为课程的背景颜色，剩下为课程信息
    //颜色为16进制整数，包含RGB信息，透明度为66%，即0xA8
    private String generateCourseInfo(String color, String text) {
        return "0x80" + color + "$color$" + text;
    }

    //添加课程
    private void addCourse(int rowIndex, int columnIndex, int rowSpan, String text, int courseId) {
        //更新courses数组
        for (int i = 0; i < rowSpan; i++) {
            if (mCourses[rowIndex + i][columnIndex] != -1) {
                int tempIndex = mCourses[rowIndex + i][columnIndex];
                mCourseTable.removeView(mViewMap.get(tempIndex));
            }
            mCourses[rowIndex + i][columnIndex] = courseId;
        }

        //动态添加view
        CourseView couseView = new CourseView(mContext);
        couseView.setCourseDetector(courseId);
        GridLayout.Spec rowSpec = GridLayout.spec(rowIndex, rowSpan);
        GridLayout.Spec columnSpec = GridLayout.spec(columnIndex);
        //将Spec传入GridLayout.LayoutParams, 必须设置宽高, 否则视图异常
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(rowSpec, columnSpec);
        layoutParams.width = Math.round(mColumnWidth);
        layoutParams.height = (int) (100 * mDensity * rowSpan);
        couseView.setText(text);
        mCourseTable.addView(couseView, layoutParams);
        mViewMap.put(courseId, couseView);
    }

    //获取列宽度(px)
    private void getColumnWidth() {
        //获取屏幕像素密度和宽度
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        mDensity = metrics.density;
        //屏幕宽度
        int width = metrics.widthPixels;

        //px转化为dp需要乘以density
        float wholeTableWidth = width - 60 * mDensity;
        mColumnWidth = (wholeTableWidth / 7)+mDensity*3/2;//微调宽度
    }

    //根据courseId绘制alertDialog
    public void showCourseInfo(int courseId) {
        KbData data = mKbDataList.get(courseId);
        String kcmc = data.getKcmc();//课程名
        String cdmc = data.getCdmc();//教室
        String kcxz = data.getKcxz();//课程性质
        String jc = data.getJc();//节数
        String zcd = data.getZcd();//周数
        String kcxszc = data.getKcxszc();//课程学识
        String xm = data.getXm();//教师名称
        String zcmc = data.getZcmc();//教师职称
        String khfsmc = data.getKhfsmc();//课后实践
        String xqjmc = data.getXqjmc();//星期几
        String[] datasArr = {kcmc, cdmc, kcxz, jc, zcd, kcxszc, xm, zcmc, khfsmc, xqjmc};

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        View dialogView = View.inflate(mContext, R.layout.dialog_course_info, null);

        //设置选中的显示项
        int[] boxId = {R.id.kcmc_box, R.id.cdmc_box, R.id.kcxz_box, R.id.jc_box, R.id.zcd_box,
                R.id.kcxszc_box, R.id.xm_box, R.id.zcmc_box, R.id.khfsmc_box};
        String itemsChecked = MyPreferences.getItemsChecked(mContext, String.valueOf(courseId));
        if (!itemsChecked.contains("0")) {
            MyPreferences.setItemsChecked(mContext, String.valueOf(courseId), "0,1");
            CheckBox box = dialogView.findViewById(boxId[0]);
            box.setChecked(true);
            box = dialogView.findViewById(boxId[1]);
            box.setChecked(true);
        } else {
            int[] items = MyParser.parseItemsChecked(itemsChecked);
            CheckBox box = null;
            for (int index : items) {
                box = dialogView.findViewById(boxId[index]);
                box.setChecked(true);
            }
        }

        //显示信息填充
        LinearLayout courseInfo = dialogView.findViewById(R.id.course_info);
        TextView textView = null;
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        params.weight = 1;
        for (String info : datasArr) {
            textView = new TextView(mContext);
            textView.setText(info);
            textView.setGravity(Gravity.CENTER_VERTICAL);//设置textView文本垂直居中
            courseInfo.addView(textView, params);
        }

        //颜色下拉列表
        String[] colorsArr = {
                "浅绿", "淡橙", "暗紫", "浅黄", "淡蓝", "粉红", "浅灰"
        };
        ArrayAdapter<String> colors = new ArrayAdapter<>(mContext,
                R.layout.support_simple_spinner_dropdown_item, colorsArr);

        Spinner spinner = dialogView.findViewById(R.id.color_items);
        spinner.setAdapter(colors);
        //获取原来的颜色
        int index = MyPreferences.getColorId(mContext, kcmc);
        spinner.setSelection(index);

        builder.setTitle(kcmc)
                .setView(dialogView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //修改显示项
                        CheckBox box = null;
                        StringBuilder stringBuilder = new StringBuilder();
                        int index = 0;
                        for (int id : boxId) {
                            box = dialogView.findViewById(id);
                            if (box.isChecked()) {
                                stringBuilder.append(index);
                                stringBuilder.append(",");
                            }
                            index++;
                        }
                        String itemsChecked = stringBuilder.toString();
                        MyPreferences.setItemsChecked(mContext, String.valueOf(courseId), itemsChecked);

                        //修改颜色
//                        Log.i(TAG, String.valueOf(spinner.getSelectedItemPosition()));
                        MyPreferences.setCourseColor(mContext, kcmc, spinner.getSelectedItemPosition());
                        //修改同名课程信息
                        changeCourse(courseId, itemsChecked);
                    }
                })
                .create()
                .show();
    }

    //更新课表中一门课的信息
    private void changeCourse(int courseId, String itemsChecked) {
        KbData data = mKbDataList.get(courseId);
        String zcd = data.getZcd();//周数
        int[] weeks = MyParser.parseWeekspan(zcd);//确定周
        boolean flag = mWeekIndex >= weeks[0] && mWeekIndex <= weeks[1];//本周为true

        String kcmc = data.getKcmc();//课程名
        String cdmc = data.getCdmc();//教室
        String kcxz = data.getKcxz();//课程性质
        String jc = data.getJc();//节数
        String kcxszc = data.getKcxszc();//课程学识
        String xm = data.getXm();//教师名称
        String zcmc = data.getZcmc();//教师职称
        String khfsmc = data.getKhfsmc();//课后实践
        String xqjmc = data.getXqjmc();//星期几
        String[] datasArr = {kcmc, cdmc, kcxz, jc, zcd, kcxszc, xm, zcmc, khfsmc, xqjmc};

        //添加课程信息
        StringBuilder builder = new StringBuilder();
        if (!flag) {
            builder.append(" [非本周]");
        }
        String itemStr = MyPreferences.getItemsChecked(mContext, String.valueOf(courseId));
        if (!itemStr.contains("0")) {
            builder.append(kcmc).append(" @").append(cdmc);
        } else {
            int[] items = MyParser.parseItemsChecked(itemStr);
            for (int index : items) {
                if (index == 1) {
                    builder.append("@");
                }
                builder.append(datasArr[index]).append(" ");
            }
        }

        String courseInfo = builder.toString();
        int colorId = 6;
        if (flag) {
            colorId = MyPreferences.getColorId(mContext, kcmc);
        }
        String color = getColorRGB(colorId);
        String text = generateCourseInfo(color, courseInfo);
        mViewMap.get(courseId).setText(text);

        //将同名课程的课程名与颜色修改过来
        int startId = mKbDataList.get(0).getId();
        int currentId;
        for (KbData kbData : mKbDataList) {
            if (kbData.getKcmc().equals(kcmc)) {
                currentId = kbData.getId() - startId;
                if (currentId != courseId) {
                    mViewMap.get(currentId).setText(text);
                    MyPreferences.setItemsChecked(mContext, String.valueOf(currentId), itemsChecked);
                }
            }
        }
    }

    //更新课表, 流程为 refreshHeader->findCourse
    public void updateCourses() {
        refreshHeader(mView, 0);
    }

    //修改周
    private void changeWeek(int weekOffset) {
        refreshHeader(mView, weekOffset);
    }

    //显示或隐藏周数列表
    public void showChangeWeek(){
        isListShow = !isListShow;
        //显示
        if(isListShow){
            mChangeListLayout.setVisibility(View.VISIBLE);
        }else{//不显示
            mChangeListLayout.setVisibility(View.GONE);
        }
    }

    //显示本周课程或非本周课程
    public void showCurrentCourse(boolean isShowCurrent, boolean isShowUnCurrent){
        this.isShowCurrent = isShowCurrent;
        this.isShowUnCurrent = isShowUnCurrent;
        updateCourses();
    }
}
