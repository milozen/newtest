package com.zhanghuang;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.qq.e.comm.managers.GDTAdSdk;
import com.squareup.leakcanary.LeakCanary;
import com.tencent.tauth.Tencent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.zhanghuang.db.DaoManager;
import com.zhanghuang.util.Constants;
import com.zhanghuang.util.SharedPreferencesHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.zhanghuang.util.Constants.PREF_ZZ_IS_VIP;
import static com.zhanghuang.util.Constants.PREF_ZZ_VIP_EXP_TIME;

/**
 * Created by yuanlei on 2017/3/14.
 */

public class MainApplication extends Application {
    public static String imei = null;
    public static String ver = "1.0.0";

    public static int screenWidth = 0;
    public static int screenHeight = 0;
    public static MainApplication application;
    public static SharedPreferences _pref;
    private static Context mContext;
    SharedPreferencesHelper sharedPreferencesHelper;

    static {
        PlatformConfig.setWeixin("wx5846c59e6f1304a9", "f45d156ea73b746e854ac9b7944fdaac");
        PlatformConfig.setQQZone("1106055004", "ugZxwEv1VXSne1H6");
        PlatformConfig.setSinaWeibo("1690321612", "b5fdc7c8d5463df0be52ec9d90f1c512", "");
    }

    public static MainApplication getApplication() {
        return application;
    }

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferencesHelper=new SharedPreferencesHelper(this,"umeng");
        application = this;
        Fresco.initialize(this);
        //注释掉广告初始化
        //GDTAdSdk.init(this, "1200582662");
        _pref = getSharedPreferences(Constants.PREF_NAME, 0);
        mContext = getApplicationContext();
        DaoManager.getInstance();
        _pref = getSharedPreferences(Constants.PREF_NAME, 0);


        LeakCanary.install(this);


        // 加载系统默认设置，字体不随用户设置变化
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config, res.getDisplayMetrics());

        //设置LOG开关，默认为false
        UMConfigure.setLogEnabled(true);
        //友盟预初始化
        UMConfigure.preInit(getApplicationContext(),"58eb2d094544cb15d2001334","Umeng");
        //判断是否同意隐私协议，uminit为1时为已经同意，直接初始化umsdk
        if(sharedPreferencesHelper.getSharedPreference("uminit","").equals("1")){
            //友盟正式初始化
            UmInitConfig umInitConfig=new UmInitConfig();
            umInitConfig.UMinit(getApplicationContext());
            //QQ官方sdk授权
            Tencent.setIsPermissionGranted(true);
            //广告初始化
            GDTAdSdk.init(this, "1203042382");
        }
    }

    public static boolean isVip() {
        if (!_pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
            return false;
        }
        int vip = _pref.getInt(PREF_ZZ_IS_VIP, 0);
        long vipExpTime = _pref.getLong(PREF_ZZ_VIP_EXP_TIME, 0);
        if (vip == 0) {
            return false;
        }
        if (vip == 9) {
            return true;
        }
        Date now = new Date();
        return now.getTime() <= vipExpTime;
    }

    public static boolean showFirstAd() {
        return !isVip();
    }

    public static boolean isInReview() {
        return _pref.getBoolean(Constants.PREF_IS_REVIEW, false);
    }

    public static boolean showVideoAd() {
        Date d = new Date(System.currentTimeMillis());
        String day = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d);
        String dayStore = _pref.getString(Constants.PREF_SAVE_VIDEO_DAY,"");
        int todayCount = _pref.getInt(Constants.PREF_SAVE_VIDEO_COUNT, 0);
        int totalCount = _pref.getInt(Constants.PREF_SAVE_AD_COUNT, 1);
        if (!day.equals(dayStore)) {
            todayCount = 0;
        }
        return (!isVip() && todayCount < totalCount && !isInReview());
    }

}
