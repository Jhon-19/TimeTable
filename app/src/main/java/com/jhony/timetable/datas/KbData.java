package com.jhony.timetable.datas;

import org.litepal.crud.LitePalSupport;

public class KbData extends LitePalSupport {
    private int id;
    private String mCdmc;
    private String mJc;
    private String mKcmc;
    private String mKcxszc;
    private String mKcxz;
    private String mKhfsmc;
    private String mXm;
    private String mXqjmc;
    private String mZcd;
    private String mZcmc;

    public int getId() {
        return id;
    }

    public String getCdmc() {
        return mCdmc;
    }

    public void setCdmc(String cdmc) {
        mCdmc = cdmc;
    }

    public String getJc() {
        return mJc;
    }

    public void setJc(String jc) {
        mJc = jc;
    }

    public String getKcmc() {
        return mKcmc;
    }

    public void setKcmc(String kcmc) {
        mKcmc = kcmc;
    }

    public String getKcxszc() {
        return mKcxszc;
    }

    public void setKcxszc(String kcxszc) {
        mKcxszc = kcxszc;
    }

    public String getKcxz() {
        return mKcxz;
    }

    public void setKcxz(String kcxz) {
        mKcxz = kcxz;
    }

    public String getKhfsmc() {
        return mKhfsmc;
    }

    public void setKhfsmc(String khfsmc) {
        mKhfsmc = khfsmc;
    }

    public String getXm() {
        return mXm;
    }

    public void setXm(String xm) {
        mXm = xm;
    }

    public String getXqjmc() {
        return mXqjmc;
    }

    public void setXqjmc(String xqjmc) {
        mXqjmc = xqjmc;
    }

    public String getZcd() {
        return mZcd;
    }

    public void setZcd(String zcd) {
        mZcd = zcd;
    }

    public String getZcmc() {
        return mZcmc;
    }

    public void setZcmc(String zcmc) {
        mZcmc = zcmc;
    }

    @Override
    public String toString() {
        return this.getCdmc()+" "+this.getJc()+" "+this.getKcmc()+" "+this.getKcxszc()+" "
                +this.getKcxz()+" "+this.getXm()+" "+this.getXqjmc()+" "+this.getZcd()+
                " "+this.getZcmc()+" "+this.getKhfsmc()+"\n";
    }
}
