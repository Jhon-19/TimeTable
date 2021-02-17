package com.jhony.timetable.mydatas;

import org.litepal.crud.LitePalSupport;

public class SjkData extends LitePalSupport {
    private int id;
    private String mJsxm;
    private String mKcmc;

    public int getId() {
        return id;
    }

    public String getJsxm() {
        return mJsxm;
    }

    public void setJsxm(String jsxm) {
        mJsxm = jsxm;
    }

    public String getKcmc() {
        return mKcmc;
    }

    public void setKcmc(String kcmc) {
        mKcmc = kcmc;
    }


    @Override
    public String toString() {
        return this.getJsxm()+" "+getKcmc()+"\n";
    }
}
