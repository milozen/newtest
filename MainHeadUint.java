package com.zhanghuang.view;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zhanghuang.ActivitiesActivity;
import com.zhanghuang.AdInfoActivity;
import com.zhanghuang.Login;
import com.zhanghuang.MainApplication;
import com.zhanghuang.R;
import com.zhanghuang.UserInfoActivity;
import com.zhanghuang.modes.StisticsInfo;
import com.zhanghuang.modes.User;
import com.zhanghuang.util.Constants;
import com.zhanghuang.util.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by yuanlei on 2017/3/24.
 */

public class MainHeadUint extends LinearLayout {

    private final Context ctx;

    private SimpleDraweeView avatarImage;
    private SimpleDraweeView vipIcon;
    private SimpleDraweeView adImage;
    private FrameLayout adContainer;
    private TextView timesText;
    private LinearLayout recordContainer;
    private LinearLayout userContainer;

    private TextView tvLocation;

    private DetailTimeView lastTime;
    private DetailTimeView totalTime;

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
        timesText = findViewById(R.id.main_head_unit_zz_times_text);
        recordContainer = findViewById(R.id.main_head_unit_zz_record_container);
        userContainer = findViewById(R.id.main_head_unit_user_container);
        tvLocation = findViewById(R.id.location);
        lastTime = findViewById(R.id.last_time);
        totalTime = findViewById(R.id.total_time);
    }

    public void setAd(String url){
        adContainer.setVisibility(VISIBLE);
        adImage.setImageURI(url);
        adImage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent in = new Intent(ctx, AdInfoActivity.class);
                ctx.startActivity(in);
            }
        });

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
        avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
                    Intent in = new Intent(ctx, UserInfoActivity.class);
                    ctx.startActivity(in);
                } else {
                    Intent in = new Intent(ctx, Login.class);
                    ctx.startActivity(in);
                }
            }
        });
        userContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
                    Intent in = new Intent(ctx, UserInfoActivity.class);
                    ctx.startActivity(in);
                } else {
                    Intent in = new Intent(ctx, Login.class);
                    ctx.startActivity(in);
                }
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
        recordContainer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
                    Intent in = new Intent(ctx, ActivitiesActivity.class);
                    ctx.startActivity(in);
                } else {
                    Intent in = new Intent(ctx, Login.class);
                    ctx.startActivity(in);
                }
            }
        });
    }
}
