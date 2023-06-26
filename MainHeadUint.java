package com.zhanghuang.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.comm.util.AdError;
import com.zhanghuang.ActivitiesActivity;
import com.zhanghuang.Login;
import com.zhanghuang.MainActivity;
import com.zhanghuang.MainApplication;
import com.zhanghuang.R;
import com.zhanghuang.UserInfoActivity;
import com.zhanghuang.modes.StisticsInfo;
import com.zhanghuang.modes.User;
import com.zhanghuang.util.ADUtil;
import com.zhanghuang.util.Constants;
import com.zhanghuang.util.TimeUtil;
import com.zhanghuang.util.ToastUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yuanlei on 2017/3/24.
 */

public class MainHeadUint extends LinearLayout implements UnifiedBannerADListener {

    private final Context ctx;

    private SimpleDraweeView avatarImage;
    private SimpleDraweeView vipIcon;
    private SimpleDraweeView adImage;
    private FrameLayout adContainer;
    private ImageView adDelImage;
    private TextView timesText;
    private LinearLayout recordContainer;
    private LinearLayout userContainer;

    private TextView tvLocation;

    private DetailTimeView lastTime;
    private DetailTimeView totalTime;
    private static final String TAG = MainHeadUint.class.getSimpleName();

    ViewGroup mBannerContainer;
    UnifiedBannerView mBannerView;

    public MainHeadUint(Context context) {
        super(context);
        this.ctx = context;
    }

    public MainHeadUint(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        avatarImage = findViewById(R.id.main_head_unit_avatar);
        vipIcon = findViewById(R.id.main_head_vip_icon);
        adImage = findViewById(R.id.main_head_unit_ad);
        adContainer = findViewById(R.id.main_head_unit_ad_container);
        adDelImage = findViewById(R.id.main_head_unit_del_ad_image);
        timesText = findViewById(R.id.main_head_unit_zz_times_text);
        recordContainer = findViewById(R.id.main_head_unit_zz_record_container);
        userContainer = findViewById(R.id.main_head_unit_user_container);
        tvLocation = findViewById(R.id.location);
        lastTime = findViewById(R.id.last_time);
        totalTime = findViewById(R.id.total_time);
        mBannerContainer = this.findViewById(R.id.main_head_unit_ad_container);
        this.setAd();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        // View is now attached
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // View is now detached, and about to be destroyed
        if (mBannerView != null) {
            mBannerView.destroy();
        }

    }


    public void setAd(){
        Date d = new Date(System.currentTimeMillis());
        String day = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d);
        String dayStore = MainApplication._pref.getString(Constants.PREF_AD_CLOSE_DAY,"");
        int todayCount = MainApplication._pref.getInt(Constants.PREF_ZZ_MAIN_BANNER_CLOSE_COUNT, 0);
        int totalCount = MainApplication._pref.getInt(Constants.PREF_MAIN_BANNER_COUNT, 1);
        if (!day.equals(dayStore)) {
            todayCount = 0;
        }
        if (!MainApplication.isVip() && todayCount < totalCount) {
            adContainer.setVisibility(VISIBLE);
            this.getBanner().loadAD();
        } else {
            adContainer.setVisibility(GONE);
        }

        adDelImage.setOnClickListener(v -> {
            adContainer.setVisibility(GONE);
            updateBannerCount();
        });
    }
    private void updateBannerCount() {
        int todayCount = MainApplication._pref.getInt(Constants.PREF_ZZ_MAIN_BANNER_CLOSE_COUNT, 0);
        Date d1 = new Date(System.currentTimeMillis());
        String day = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d1);
        String dayStore = MainApplication._pref.getString(Constants.PREF_AD_CLOSE_DAY,"");
        if (!day.equals(dayStore)) {
            todayCount = 0;
        }
        todayCount += 1;
        MainApplication._pref.edit()
            .putString(Constants.PREF_AD_CLOSE_DAY, day)
            .putInt(Constants.PREF_ZZ_MAIN_BANNER_CLOSE_COUNT, todayCount)
            .apply();
    }

    public void setUser(User user){
        if(user==null){
            return;
        }
        if(user.getAvatar()!=null){
            avatarImage.setImageURI(user.getAvatar());
        }
        if (MainApplication.isVip()) {
            vipIcon.setVisibility(View.VISIBLE);
        } else {
            vipIcon.setVisibility(View.INVISIBLE);
        }

        if(!TextUtils.isEmpty(user.getLocation())){
            tvLocation.setVisibility(VISIBLE);
            tvLocation.setText(user.getLocation());
        }
        avatarImage.setOnClickListener(v -> {
            if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
                Intent in = new Intent(ctx, UserInfoActivity.class);
                ctx.startActivity(in);
            } else {
                Intent in = new Intent(ctx, Login.class);
                ctx.startActivity(in);
            }
        });
        userContainer.setOnClickListener(v -> {
            if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
                Intent in = new Intent(ctx, UserInfoActivity.class);
                ctx.startActivity(in);
            } else {
                Intent in = new Intent(ctx, Login.class);
                ctx.startActivity(in);
            }
        });
    }

//    public void setUserTwo(String avatar,String showId,String level,String nick){
//        avatarImage.setImageURI(avatar == null ? "" : avatar);
//    }
//
//    public void setUserTwo(String avatar){
//        avatarImage.setImageURI(avatar == null ? "" : avatar);
//
//        userContainer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
//                    Intent in = new Intent(ctx, UserInfoActivity.class);
//                    ctx.startActivity(in);
//                } else {
//                    Intent in = new Intent(ctx, Login.class);
//                    ctx.startActivity(in);
//                }
//            }
//        });
//    }


    public void setRecord(StisticsInfo info){
        timesText.setText(String.valueOf(info.getCount()));
        int[] totalTimeValue;
        totalTimeValue = TimeUtil.timeVaues(info.getCount_time());
        totalTime.setTime(totalTimeValue);
        int[] lastTimeValue;
        lastTimeValue = TimeUtil.timeVaues(info.getLast_time());
        lastTime.setTime(lastTimeValue);
    }

    public void setRecord(String count, String countTime){
        String ct = "0";
        if (count != null && !count.equals("")){
            ct = count ;
        }
        timesText.setText(ct);
        int[] time  = null;
        if (countTime != null && !countTime.equals("") && !countTime.equals("0")){
            time = TimeUtil.timeVaues(Integer.parseInt(countTime));
        }
        totalTime.setTime(time);
        recordContainer.setOnClickListener(v -> {
            if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
                Intent in = new Intent(ctx, ActivitiesActivity.class);
                ctx.startActivity(in);
            } else {
                Intent in = new Intent(ctx, Login.class);
                ctx.startActivity(in);
            }
        });
    }

    protected UnifiedBannerView getBanner() {
        String editPosId = Constants.TAD_BANNER_1;
        if (mBannerView == null) {
            mBannerView = new UnifiedBannerView(MainActivity.activity, editPosId, this);
            mBannerView.setLoadAdParams(ADUtil.getLoadAdParams("banner"));
            mBannerContainer.removeAllViews();
            mBannerContainer.addView(mBannerView, getUnifiedBannerLayoutParams());
        }
        this.mBannerView.setRefresh(30);
        mBannerView.setNegativeFeedbackListener(() -> Log.d(TAG, "onComplainSuccess"));
        return this.mBannerView;
    }

    /**
     * banner2.0规定banner宽高比应该为6.4:1 , 开发者可自行设置符合规定宽高比的具体宽度和高度值
     *
     */
    private FrameLayout.LayoutParams getUnifiedBannerLayoutParams() {
        Point screenSize = new Point();
        MainActivity.activity.getWindowManager().getDefaultDisplay().getSize(screenSize);
        return new FrameLayout.LayoutParams(screenSize.x,  Math.round(screenSize.x / 6.4F));
    }

    @Override
    public void onNoAD(AdError adError) {
        String msg = String.format(Locale.getDefault(), "onNoAD, error code: %d, error msg: %s",
                adError.getErrorCode(), adError.getErrorMsg());
        ToastUtil.l(msg);
    }

    @Override
    public void onADReceive() {
        Log.i(TAG, "onADReceive" + ", ECPM: " + mBannerView.getECPM() + ", ECPMLevel: "
                + mBannerView.getECPMLevel() + ", adNetWorkName: " + mBannerView.getAdNetWorkName()
                + ", testExtraInfo:" + mBannerView.getExtraInfo().get("mp")
                + ", request_id:" + mBannerView.getExtraInfo().get("request_id"));
    }

    @Override
    public void onADExposure() {
        Log.i(TAG, "onADExposure");
    }

    @Override
    public void onADClosed() {
        Log.i(TAG, "onADClosed");
    }

    @Override
    public void onADClicked() {
        Log.i(TAG, "onADClicked : ");
    }

    @Override
    public void onADLeftApplication() {
        Log.i(TAG, "onADLeftApplication");
    }
}
