package com.jhony.timetable.datas;

import org.litepal.crud.LitePalSupport;

public class SjkData extends LitePalSupport {
    private int id;
    private String mJsxm;
    private String mKcmc;
    private String mQtkcgs;
    private String mSjkcgs;

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

    public String getQtkcgs() {
        return mQtkcgs;
    }

    public void setQtkcgs(String qtkcgs) {
        mQtkcgs = qtkcgs;
    }

    public String getSjkcgs() {
        return mSjkcgs;
    }

    public void setSjkcgs(String sjkcgs) {
        mSjkcgs = sjkcgs;
    }

    @Override
    public String toString() {
        return this.getJsxm()+" "+getKcmc()+" "+getQtkcgs()+" "+getSjkcgs()+"\n";
    }
}
