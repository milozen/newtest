package com.zhanghuang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.ads.rewardvideo.ServerSideVerificationOptions;
import com.qq.e.comm.util.AdError;
import com.zhanghuang.events.UpdateUserEvent;
import com.zhanghuang.util.ADUtil;
import com.zhanghuang.util.Constants;
import com.zhanghuang.util.DLog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class DonateActivity extends AppCompatActivity implements RewardVideoADListener {

    private Button btnSupportAuthorMembership;
    private Button btnSupportAuthorAds;
    private Button btnSupportAuthorLater;
    private ProductActivity productActivity;

    private RewardVideoAD mRewardVideoAD;
    private boolean mIsLoadSuccess = false;
    private boolean autoShowAd = false;

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
                // After the ad is played, increase the VIP by one day

            }
        });


        btnSupportAuthorLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Handle later support
                Intent in = new Intent(DonateActivity.this, AddRecordActivityNew.class);
                startActivity(in);
                finish();
            }
        });
    }

    // 在 onDestroy 方法中取消注册 EventBus
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    // 添加处理 UpdateUserEvent 的方法
    @Subscribe
    public void onUpdateUserEvent(UpdateUserEvent event) {
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
                    .setCustomData("APP's custom data") // 设置激励视频服务端验证的自定义信息
                    .setUserId("APP's user id for server verify") // 设置服务端验证的用户信息
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String vipExpireStr = MainApplication._pref.getString(Constants.PREF_ZZ_VIP_EXP_STR, "-");
        Date vipExpireDate = null;
        try {
            vipExpireDate = sdf.parse(vipExpireStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        // 如果VIP已经过期，从当前时间开始增加
        if (vipExpireDate.before(new Date())) {
            c.setTime(new Date());
        } else {
            c.setTime(vipExpireDate);
        }
        c.add(Calendar.DATE, 1);  // number of days to add
        vipExpireDate = c.getTime();  // vipExpireDate is now +1 day
        vipExpireStr = sdf.format(vipExpireDate);
        MainApplication._pref.edit().putString(Constants.PREF_ZZ_VIP_EXP_STR, vipExpireStr).apply();

        // 发布UpdateUserEvent事件
        EventBus.getDefault().post(new UpdateUserEvent());
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
    }

    @Override
    public void onError(AdError adError) {
        // 广告流程出错时的回调
        Log.e("ERROR", "广告加载失败: " + adError.getErrorCode() + ", " + adError.getErrorMsg());
    }

}
