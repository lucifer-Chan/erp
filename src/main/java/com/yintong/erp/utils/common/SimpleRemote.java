package com.yintong.erp.utils.common;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import java.io.IOException;
import java.util.Map;
import org.springframework.util.StringUtils;

/**
 * @author lucifer.chan
 * @create 2018-08-16 上午12:52
 * 远程访问
 **/
public class SimpleRemote {
    private OkHttpClient client;

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static SimpleRemote instance;

    private SimpleRemote() {
        client = new OkHttpClient();
    }

    /**
     * singleTon 考虑线程安全
     *
     * @return
     */
    public static SimpleRemote instance() {
        if(null == instance) {
            synchronized(SimpleRemote.class) {
                if(null == instance) instance = new SimpleRemote();
            }
        }
        return instance;
    }


    public String get(String url, Map<String, String> params) throws IOException {
        url = CommonUtil.makeURL(url, params);
        final Request request = new Request.Builder().url(url).build();
        return client.newCall(request).execute().body().string();
    }

    public String post(String url, Map<String, String> params, String body) throws IOException {
        url = CommonUtil.makeURL(url, params);
        RequestBody requestBody = RequestBody.create(JSON, StringUtils.isEmpty(body) ? "{}" : body);
        final Request request = new Request.Builder().url(url).post(requestBody).build();
        return client.newCall(request).execute().body().string();
    }

}
