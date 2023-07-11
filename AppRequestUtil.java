package com.zhanghuang.net;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.zhanghuang.events.RequestErrorEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

/**
 * Created by yuanlei on 2017/3/16.
 */

public class AppRequestUtil {
    private static final AppRequestUtil app_request_util = new AppRequestUtil();

    private AppRequestUtil() {}

    public static AppRequestUtil getInstance(){
        return app_request_util;
    }

    private final Response.ErrorListener errorListener = volleyError -> {
        RequestErrorEvent event = new RequestErrorEvent();
        event.setType(1);
        event.setErrorMessage(volleyError.getMessage());
        Log.e("AppRequestUtil", volleyError.getMessage());
        EventBus.getDefault().post(event);
    };

    //post请求
    public void postRequest(Context context, String typeUrl, Map<String, String> map, RequestListener listener){
        AppRequest request = new AppRequest(Request.Method.POST,typeUrl,map,listener, errorListener);
        request.setTag(context);
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        request.setShouldCache(true);
        VolleyUtil.getQueue(context).add(request);

    }

    //get请求
    public void getRequest(Context context,String typeUrl,RequestListener listener){
        AppRequest request = new AppRequest(Request.Method.GET,typeUrl,null,listener, errorListener);
        request.setTag(context);
        request.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 1, 1.0f));
        request.setShouldCache(true);
        VolleyUtil.getQueue(context).add(request);
    }
}
