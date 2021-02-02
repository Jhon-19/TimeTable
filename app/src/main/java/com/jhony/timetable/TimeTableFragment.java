package com.jhony.timetable;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TimeTableFragment extends Fragment {
    private static final String TAG = "TimeTableFragment";
    private final OkHttpClient client;
    private final Headers mHeaders;
    private String currentTime;
    private String csrftoken;
    private CookieJar mCookie;

    private TextView temp;

    public TimeTableFragment() {
        setCookie();
        //单例请求client并设置cookie
        client = new OkHttpClient.Builder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .cookieJar(mCookie)
                .build();
        //设置请求头
        String agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.104 Safari/537.36";
        mHeaders = new Headers.Builder()
                .add("Host", "bkxk.whu.edu.cn")
                .add("User-Agent", agent)
                .add("Origin", "http://bkxk.whu.edu.cn")
                .add("Referer", "http://bkxk.whu.edu.cn/xtgl/login_slogin.html")
                .add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
//                .add("Accept-Encoding", "gzip, deflate")
                .add("Accept-Language", "zh-CN,zh;q=0.9")
                .add("Cache-Control", "max-age=0")
                .add("Upgrade-Insecure-Requests", "1")
                .add("Connection", "keep-alive")
                .build();
    }

    public static Fragment newInstance() {
        return new TimeTableFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_table, container, false);

//        temp = view.findViewById(R.id.temp);
//        test();
        getCsrftoken();
        return view;
    }

    private void test() {
//        Log.i(TAG, String.valueOf(currentTime));
    }

    //设置cookie，保持一个session
    private void setCookie(){
        //初始化Cookie管理器
        mCookie = new CookieJar() {
            //Cookie缓存区
            private final Map<String, List<Cookie>> cookiesMap = new HashMap<String, List<Cookie>>();

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                //移除相同的url的Cookie
                String host = url.host();
                List<Cookie> cookiesList = cookiesMap.get(host);
                if (cookiesList != null){
                    cookiesMap.remove(host);
                }
                //再重新添加
                cookiesMap.put(host, cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl arg0) {
                // TODO Auto-generated method stub
                List<Cookie> cookiesList = cookiesMap.get(arg0.host());
                //注：这里不能返回null，否则会报NULLException的错误。
                //原因：当Request 连接到网络的时候，OkHttp会调用loadForRequest()
                return cookiesList != null ? cookiesList : new ArrayList<Cookie>();
            }
        };
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
//                Log.i(TAG, "success of token");
                String result = response.body().string();
//                Log.i(TAG, response.body().string());
                Document soup = Jsoup.parse(result);
                csrftoken = soup.select("#csrftoken").attr("value");
//                Log.i(TAG, csrftoken);
                saveCookie(response, requestOfCsrftoken);

                getPublicKey(requestOfCsrftoken);
            }
        });
    }

    //保存cookie
    private void saveCookie(Response response, Request request){
        //获取返回数据的头部
        Headers headers = response.headers();
        HttpUrl url = request.url();
        //获取头部的Cookie,注意：可以通过Cooke.parseAll()来获取
        List<Cookie> cookies = Cookie.parseAll(url, headers);
        //防止header没有Cookie的情况
        if (cookies != null){
            //存储到Cookie管理器中
            client.cookieJar().saveFromResponse(url, cookies);//这样就将Cookie存储到缓存中了
        }
    }

    //获取cookie
    private String getCookie(Request request){
        //获取需要提交的CookieStr
        StringBuilder cookieStr = new StringBuilder();
        //从缓存中获取Cookie
        List<Cookie> cookies = client.cookieJar().loadForRequest(request.url());
        //将Cookie数据弄成一行
        for(Cookie cookie : cookies){
            cookieStr.append(cookie.name()).append("=").append(cookie.value()+";");
        }
        return cookieStr.toString();
    }

    //获取公钥并加密
    private void getPublicKey(Request requestOfCsrftoken){

        String cookie = getCookie(requestOfCsrftoken);

        String publicKeySite = "http://bkxk.whu.edu.cn/xtgl/login_getPublicKey.html?time=" + currentTime;
        JSONObject resultKey;
        Request requestOfKey = new Request.Builder()
                .url(publicKeySite)
                .header("Cookie", cookie)
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
//                Log.i(TAG, result);
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
                String mm = HB64.hex2b64(temp);

                saveCookie(response, requestOfKey);

                tryPost(mm, requestOfKey);
            }
        });
    }

    //尝试登陆
    private void tryPost(String mm, Request requestOfKey) {
        String cookie = getCookie(requestOfKey);
        String postSite = "http://bkxk.whu.edu.cn/xtgl/login_slogin.html?time=" + currentTime;
        RequestBody body = new FormBody.Builder()
                .add("yhm", "2018302030036")
                .add("mm", mm)
                .add("csrftoken", csrftoken)
                .build();
        Request request = new Request.Builder()
                .url(postSite)
                .header("cookie", cookie)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i(TAG, "failed");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "successed of login");
//                Log.i(TAG, response.body().string().indexOf("学生课表查询") != -1 ? "success": "failed");
//                getTable();
//                Log.i(TAG, response.body().string().substring(5000));
            }
        });
    }

    //获取课表
    private void getTable(){
        String tableSite = "http://bkxk.whu.edu.cn/kbcx/xskbcx_cxXsKb.html?gnmkdm=N2151";
        RequestBody body = new FormBody.Builder()
                .add("xnm", "2020")
                .add("xqm", "12")
                .build();
        Request requestOfTable = new Request.Builder()
                .url(tableSite)
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
                //                    JSONObject json = new JSONObject(result);
//                Log.i(TAG, result);
            }
        });
    }

    private void encode() {
        String password = "zl08252000";
        String modulus = "ALuELfJPlldTm5cBAvuZ+5O2kol8WPrTeS1voPRkYXmj6xDUdmvjNLJlIdxu1jKZLXWvSCQVIrdIDcKQe9LfJWMUGJBawMO3hN5kXgJOL9JBMndUhEwJi6o0jFk1I9s9i3NLPxGIo9/DUNtX2yN6elr2QfJKtBnLc+XdErAP3x4N";
        String exponent = "AQAB";
        String mm = "jH3oSexxQKlF7gDrnq6EV8OGvydCEhiETlsQNAmm/DEaz3+S8s/3lZHGM8b5GB1sJ/VS2C8YK+muImrgIIJZeSMC+EvlFNa2H1J4m2RzjDE76pcUdYFCA4bJ/ITVQI1m2OhiOz4te+xi8HNxozJfs8FhYuQTQajKp/w9qS4pvRM=";

        String temp = RSAEncoder.RSAEncrypt(password, HB64.b64tohex(modulus), HB64.b64tohex(exponent));
        mm = HB64.hex2b64(temp);
    }
}
