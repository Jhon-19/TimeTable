package com.jhony.timetable.myutils;

public class MyParser {

    //解析周数,返回开始和结束周
    public static int[] parseWeekspan(String weekspan){
        int[] weeks = new int[2];
        String[] weekArr = weekspan.split("[-周]");
        weeks[0] = Integer.parseInt(weekArr[0]);
        weeks[1] = Integer.parseInt(weekArr[1]);
        return weeks;
    }

    //解析节数,返回第几节以及跨度
    public static int[] parseTimespan(String timespan){
        int[] times = new int[2];
        String[] timeArr = timespan.split("[-节]");
//        Log.i(TAG, Arrays.toString(timeArr));
        int start = Integer.parseInt(timeArr[0]);
        int stop = Integer.parseInt(timeArr[1]);
        int span = stop-start+1;
        times[0] = start;
        times[1] = span;
        return times;
    }

    //解析星期几
    public static int parseWeekday(String weekday){
        char day = weekday.charAt(weekday.length()-1);//获取最后一个字符
        int dayInt = -1;
        switch (day){
            case '日':
                dayInt = 0;
                break;
            case '一':
                dayInt = 1;
                break;
            case '二':
                dayInt = 2;
                break;
            case '三':
                dayInt = 3;
                break;
            case '四':
                dayInt = 4;
                break;
            case '五':
                dayInt = 5;
                break;
            case '六':
                dayInt = 6;
                break;
        }
        return dayInt;
    }

    //解析用户设置的显示项
    public static int[] parseItemsChecked(String itemStr){
        String[] itemsArr = itemStr.split(",");
        int[] items = new int[itemsArr.length];
        for(int i = 0; i < itemsArr.length; i++){
            items[i] = Integer.parseInt(itemsArr[i]);
        }
        return items;
    }

    //解析Day
    public static String parseDay(int day){
        String dayStr = null;
        switch (day) {
            case 0:
                dayStr = "周日";
                break;
            case 1:
                dayStr = "周一";
                break;
            case 2:
                dayStr = "周二";
                break;
            case 3:
                dayStr = "周三";
                break;
            case 4:
                dayStr = "周四";
                break;
            case 5:
                dayStr = "周五";
                break;
            case 6:
                dayStr = "周六";
                break;
        }
        return dayStr;
    }
}
