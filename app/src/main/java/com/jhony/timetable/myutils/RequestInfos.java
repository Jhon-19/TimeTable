package com.jhony.timetable.myutils;

import android.util.Log;

import com.jhony.timetable.datas.KbData;
import com.jhony.timetable.datas.SjkData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestInfos {
    private static final String TAG = "GetInfos";

    private final OkHttpClient client;
    private Headers mHeaders;
    private String currentTime;
    private String csrftoken;
    private String mm;

    //cookie中的信息
    private String JSESSION = null;
    private String route = null;

    public RequestInfos(){
        //单例请求client
        //手动管理cookie
        client = new OkHttpClient.Builder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                //禁止重定向
                .followRedirects(false)
                .followSslRedirects(false)
                .build();
        //设置请求头
        String agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.104 Safari/537.36";
        mHeaders = new Headers.Builder()
                .add("Host:bkxk.whu.edu.cn")
                .add("Connection:keep-alive")
                .add("Cache-Control:max-age=0")
                .add("Upgrade-Insecure-Requests:1")
                .add("User-Agent", agent)
                .add("Accept:text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .add("Referer:http://bkxk.whu.edu.cn/xtgl/login_slogin.html")
                .add("Accept-Language:zh-CN,zh;q=0.9")
//                .add("Cookie:route=689b2645651dc72b978923307db37ead; JSESSIONID=3FFF9F06092A9604109C439535774914")
                .build();
    }

    public static void build(){
        new RequestInfos().getCsrftoken();
    }

    //获取csrftoken
    private void getCsrftoken() {
        //获取访问时间
        currentTime = String.valueOf(System.currentTimeMillis());
        String loginSite = "http://bkxk.whu.edu.cn/xtgl/login_slogin.html";
        Request requestOfCsrftoken = new Request.Builder()
                .url(loginSite)
                .headers(mHeaders)
                .get()
                .build();
        client.newCall(requestOfCsrftoken).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "fail of token");
            }

            //获取csrftoken
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "success of token");
                String result = response.body().string();
                Document soup = Jsoup.parse(result);
                csrftoken = soup.select("#csrftoken").val();

                setCookies(response.headers().values("Set-Cookie"));
                getPublicKey();
            }
        });
    }

    //okhttp cookie管理机制存在bug，需要手动设置cookie到请求头中
    private void setCookies(List<String> cookies) {
        //仅修改JSESSIONID即可，不能全部替换Set-Cookie里面的内容
        for (String item : cookies) {
            String temp = item.toString();
            if (temp.contains("JSESSION")) {
                JSESSION = temp.split(";")[0];
            } else if (temp.contains("route")) {
                route = temp.split(";")[0];
            }
        }

        String cookie = JSESSION + ";" + route;
        System.out.println(cookie);
        mHeaders = mHeaders.newBuilder()
                .add("Cookie", cookie)
                .build();
        System.out.println(mHeaders.get("Cookie"));
    }

    //获取公钥并加密
    private void getPublicKey() {
        String publicKeySite = "http://bkxk.whu.edu.cn/xtgl/login_getPublicKey.html?time=" + currentTime;
        Request requestOfKey = new Request.Builder()
                .url(publicKeySite)
                .headers(mHeaders)
                .get()
                .build();
        client.newCall(requestOfKey).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "fail of key");
            }

            //获取公钥
            @Override
            public void onResponse(Call call, Response response) throws IOException {
//                Log.i(TAG, "susscess of key");
                String result = response.body().string();
                String modulus = null;
                String exponent = null;
                try {
                    JSONObject json = new JSONObject(result);
                    modulus = json.getString("modulus");
                    exponent = json.getString("exponent");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                Log.i(TAG, modulus+"\n"+exponent);
                //加密
                String password = "zl08252000";
                String temp = RSAEncoder.RSAEncrypt(password, HB64.b64tohex(modulus), HB64.b64tohex(exponent));
                mm = HB64.hex2b64(temp);

                tryPost();
            }
        });
    }

    //尝试登陆
    private void tryPost() {
        String postSite = "http://bkxk.whu.edu.cn/xtgl/login_slogin.html?time=" + currentTime;
        RequestBody body = new FormBody.Builder()
                .add("csrftoken", csrftoken)
                .add("yhm", "2018302030036")
                .add("mm", mm)
                .build();
        Request request = new Request.Builder()
                .url(postSite)
                .headers(mHeaders)
                .post(body)
                .build();
        //尝试登录教务系统
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "success of login");
                String result = response.body().string();

                //登陆成功后设置返回的cookie，原cookie已失效
                setCookies(response.headers().values("Set-Cookie"));

//                getMainPage(response.headers().get("Location"));
                getTable();
            }
        });
    }

    //使用登录成功的cookie登录教务系统，手动重定向
    private void getMainPage(String url) {
        Request requestOfMainPage = new Request.Builder()
                .url(url)
                .headers(mHeaders)
                .get()
                .build();
        client.newCall(requestOfMainPage).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "succes of mainPage");

                getTable();
            }
        });

    }

    //获取课表
    private void getTable() {
        String tableSite = "http://bkxk.whu.edu.cn/kbcx/xskbcx_cxXsKb.html?gnmkdm=N2151";
        RequestBody body = new FormBody.Builder()
                .add("xnm", "2020")
                .add("xqm", "12")
                .build();
        Request requestOfTable = new Request.Builder()
                .url(tableSite)
                .headers(mHeaders)
                .post(body)
                .build();
        client.newCall(requestOfTable).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "failed of table");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "success of table");
                String result = response.body().string();
                try {
                    JSONObject jsons = new JSONObject(result);
                    handleSjk(jsons);
                    handleKb(jsons);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //测试数据库部分
                List<KbData> datas = LitePal.findAll(KbData.class);
//                Log.i(TAG, String.valueOf(datas.size()));
                for(KbData data : datas){
                    Log.i(TAG, data.getKcmc());
                }
            }
        });
    }

    //处理json数据1
    private void handleSjk(JSONObject jsons) throws JSONException {
        //处理实验课
        JSONArray sjkList = jsons.getJSONArray("sjkList");

        JSONObject course;
//        SjkData[] datas = new SjkData[sjkList.length()];
        SjkData data;
        for (int i = 0; i < sjkList.length(); i++) {
            course = sjkList.getJSONObject(i);
            data = new SjkData();

            data.setJsxm(course.getString("jsxm"));
            data.setKcmc(course.getString("kcmc"));
            data.setQtkcgs(course.getString("qtkcgs"));
            data.setSjkcgs(course.getString("sjkcgs"));
            data.saveOrUpdate("mKcmc=?", data.getKcmc());
//            datas[i] = data;
        }

//        Log.i(TAG, Arrays.toString(datas));
    }

    //处理json数据2
    private void handleKb(JSONObject jsons) throws JSONException {
        //处理理论课数据
        JSONArray kbList = jsons.getJSONArray("kbList");

        JSONObject course;
//        KbData[] datas = new KbData[kbList.length()];
        KbData data;
//        Log.i(TAG, String.valueOf(kbList.length()));
        for (int i = 0; i < kbList.length(); i++) {
            course = kbList.getJSONObject(i);
            data = new KbData();

            data.setCdmc(course.getString("cdmc"));
            data.setJc(course.getString("jc"));
            data.setKcmc(course.getString("kcmc"));
            data.setKcxszc(course.getString("kcxszc"));
            data.setKcxz(course.getString("kcxz"));
            data.setKhfsmc(course.getString("khfsmc"));
            data.setXm(course.getString("xm"));
            data.setXqjmc(course.getString("xqjmc"));
            data.setZcd(course.getString("zcd"));
            data.setZcmc(course.getString("zcmc"));

            data.saveOrUpdate("mKcmc=? and mXqjmc=?", data.getKcmc(), data.getXqjmc());
//            datas[i] = data;
        }

//        Log.i(TAG, Arrays.toString(datas));
    }
}
