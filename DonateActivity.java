package com.zhanghuang;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.ads.rewardvideo.ServerSideVerificationOptions;
import com.qq.e.comm.util.AdError;
import com.zhanghuang.events.UpdateUserEvent;
import com.zhanghuang.fragments.BannerAdFragment;
import com.zhanghuang.modes.BaseMode;
import com.zhanghuang.net.RequestData;
import com.zhanghuang.netinterface.BaseInterface;
import com.zhanghuang.util.ADUtil;
import com.zhanghuang.util.Constants;
import com.zhanghuang.util.DLog;
import com.zhanghuang.util.DeviceUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DonateActivity extends AppCompatActivity implements RewardVideoADListener {

    private Button btnSupportAuthorMembership;
    private Button btnSupportAuthorAds;
    private Button btnSupportAuthorLater;
    private ProductActivity productActivity;

    private RewardVideoAD mRewardVideoAD;
    private boolean mIsLoadSuccess = false;
    private boolean autoShowAd = false;

    private RequestData rd;

    private String oaid;

    // 添加这一行来定义userId变量
    private String userId = MainApplication._pref.getString(Constants.PREF_USER_SHOWID, "");

    private String mobile = MainApplication._pref.getString(Constants.PREF_MOBILE, "0000000000000");
    private String imei = "";

    private BannerAdFragment mBannerAdFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        btnSupportAuthorMembership = findViewById(R.id.btn_support_author_membership);
        btnSupportAuthorAds = findViewById(R.id.btn_support_author_ads);
        btnSupportAuthorLater = findViewById(R.id.btn_support_author_later);

        // 在 onCreate 方法中注册 EventBus
        EventBus.getDefault().register(this);

        //广告
        mRewardVideoAD = getRewardVideoAD();
        mIsLoadSuccess = false;
        loadAd();  // 预先加载广告

        //打印下 userId
        Log.i("INFO", "userId: " + userId);
        Log.i("INFO", "mobile: " + mobile);

        //顶部横幅广告
        mBannerAdFragment = new BannerAdFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.banner_container, mBannerAdFragment);
        fragmentTransaction.commit();

        btnSupportAuthorMembership.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle membership support
                if (productActivity == null) {
                    productActivity = new ProductActivity(DonateActivity.this);
                }
                productActivity.show();
            }
        });

        btnSupportAuthorAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Play the ad
                showAD();
            }
        });


        btnSupportAuthorLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(DonateActivity.this, AddRecordActivityNew.class);
                startActivity(in);
                finish();
            }
        });

        //打印下设备信息
        DeviceUtil.getOaid(this, new DeviceUtil.OaidCallback() {
            @Override
            public void onOaidReceived(String oaid) {
                Log.i("INFO", "oaid: " + oaid);
                DonateActivity.this.oaid = oaid;  // 设置 oaid 变量的值
            }
        });

        // 延时一段时间后发送设备信息
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 测试上报信息
                Log.i("INFO", "test sendDeviceInfo imei:" + imei);
                sendDeviceInfo();
            }
        }, 500);  // 延时0.5秒

    }

    // 在 onDestroy 方法中取消注册 EventBus
    @Override
    protected void onDestroy() {
        // 销毁广告实例
        // 将广告实例设置为 null，以便垃圾回收
        mRewardVideoAD = null;
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    // 添加处理 UpdateUserEvent 的方法,充值成功或者失败
    @Subscribe
    public void onUpdateUserEvent(UpdateUserEvent event) {
        //关闭充值窗口
        if (productActivity != null) {
            productActivity.dismiss();
        }
        //跳转站桩
        Intent in = new Intent(DonateActivity.this, AddRecordActivityNew.class);
        startActivity(in);
        finish();
    }

    protected RewardVideoAD getRewardVideoAD() {
        String editPosId = Constants.TAD_VIDEO;
        boolean volumeOn = false;
        RewardVideoAD rvad;
        if (mRewardVideoAD == null) {
            rvad = new RewardVideoAD(this, editPosId, this, volumeOn);
            //rvad.setNegativeFeedbackListener(() -> Log.i(TAG, "onComplainSuccess"));
            ServerSideVerificationOptions options = new ServerSideVerificationOptions.Builder()
                    //.setCustomData("APP's custom data") // 设置激励视频服务端验证的自定义信息
                    .setUserId(userId) // 设置服务端验证的用户信息 userId
                    .build();
            rvad.setServerSideVerificationOptions(options);
            rvad.setLoadAdParams(ADUtil.getLoadAdParams("reward_video"));
        } else {
            rvad = this.mRewardVideoAD;
        }
        return rvad;
    }

    protected void loadAd() {
        // 1. 初始化激励视频广告
        mRewardVideoAD = getRewardVideoAD();
        mIsLoadSuccess = false;
        // 2. 加载激励视频广告
        mRewardVideoAD.loadAD();
    }

    private void showAD() {
        if (ADUtil.isAdValid(mIsLoadSuccess, mRewardVideoAD != null
                && mRewardVideoAD.isValid(), true)) {
            mRewardVideoAD.showAD();
        } else {
            autoShowAd = true;
            loadAd();
        }
    }

    @Override
    public void onADLoad() {
        // 广告加载成功的回调
        mIsLoadSuccess = true;
    }

    @Override
    public void onVideoCached() {
        // 视频素材缓存成功的回调
    }

    @Override
    public void onADShow() {
        // 广告页面展示的回调
    }

    @Override
    public void onADExpose() {
        // 广告曝光的回调
    }

    @Override
    public void onReward(Map<String, Object> map) {
        // 视频播放完成，且达到奖励条件时的回调
        // 发布UpdateUserEvent事件
        // post 后端通知增加奖励
        // 获取服务端验证的唯一 ID
        Log.i("INFO", "onReward " + map.get(ServerSideVerificationOptions.TRANS_ID));
        Log.i("INFO", "userid " + userId);

    }


    @Override
    public void onADClick() {
        // 广告被点击的回调
    }

    @Override
    public void onVideoComplete() {
        // 视频播放完成的回调
    }

    @Override
    public void onADClose() {
        // 广告页面关闭的回调
        Intent in = new Intent(DonateActivity.this, AddRecordActivityNew.class);
        startActivity(in);
        finish();
    }


    @Override
    public void onError(AdError adError) {
        // 广告流程出错时的回调
        Log.e("ERROR", "广告加载失败: " + adError.getErrorCode() + ", " + adError.getErrorMsg());
    }

    public void closeActivity() {
        this.finish();
    }

    private final BaseInterface getUserInfoIf = new BaseInterface() {
        @Override
        public void response(boolean success, BaseMode result, String message, String err) {
            if (success) {
                if (MainApplication.isVip()) {
                    Log.i("INFO", "USER IS VIP");
                } else {
                    Log.i("INFO", "USER NO VIP");
                }
            }
        }
    };

    private void sendDeviceInfo() {
        String url = "https://api.buychatgpt.cn/report/reg";
        long convTime = System.currentTimeMillis() / 1000;

        // 将请求参数添加到URL
        url += "?os=0"
                + "&oaid=" + oaid
                + "&conv_time=" + String.valueOf(convTime)
                + "&mobile=" + mobile;

        Log.i("INFO", "Request URL: " + url);

        // 创建请求，注意这里使用了 StringRequest 而不是 JsonObjectRequest
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // 在这里处理响应
                Log.i("INFO", "Response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 在这里处理错误
                Log.e("ERROR", "Error: " + error.getMessage());
            }
        });

        // 添加请求到请求队列
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(request);
    }
}
