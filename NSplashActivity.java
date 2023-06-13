package com.zhanghuang;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.qq.e.ads.rewardvideo.ServerSideVerificationOptions;
import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;
import com.qq.e.ads.splash.SplashADZoomOutListener;
import com.qq.e.comm.listeners.ADRewardListener;
import com.qq.e.comm.util.AdError;
import com.tencent.tauth.Tencent;
import com.umeng.commonsdk.UMConfigure;
import com.zhanghuang.base.BaseActivity;
import com.zhanghuang.util.ADUtil;
import com.zhanghuang.util.Constants;
import com.zhanghuang.util.SharedPreferencesHelper;
import com.zhanghuang.util.SplashZoomOutManager;
import com.zhanghuang.util.ToastUtil;
import com.zhanghuang.util.ViewUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NSplashActivity extends BaseActivity implements SplashADZoomOutListener,
        ADRewardListener, View.OnClickListener {
    private static final String TAG = "SplashActivity";
    public boolean canJump = false;
    private SplashAD splashAD;
    private ViewGroup container;
    private ViewGroup zoomOutView;
    private ImageView splashHolder;
    private boolean needStartDemoList = true;

    private final boolean showingAd = false;
    private boolean isFullScreen = false;
    private Integer fetchDelay;
    private boolean alterShowed = false;

    View inflate;
    Dialog dialog;
    SharedPreferencesHelper sharedPreferencesHelper;

    /**
     * 为防止无广告时造成视觉上类似于"闪退"的情况，设定无广告时页面跳转根据需要延迟一定时间，demo
     * 给出的延时逻辑是从拉取广告开始算开屏最少持续多久，仅供参考，开发者可自定义延时逻辑，如果开发者采用demo
     * 中给出的延时逻辑，也建议开发者考虑自定义minSplashTimeWhenNoAD的值（单位ms）
     **/
    private int minSplashTimeWhenNoAD = 2000;
    /**
     * 记录拉取广告的时间
     */
    private long fetchSplashADTime = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean isZoomOut = false;
    private boolean isSupportZoomOut = true;
    private boolean isZoomOutInAnother = false;
    // 是否适配全面屏，默认是适配全面屏，即使用顶部状态栏和底部导航栏
    private boolean isNotchAdaptation = true;
    private boolean mLoadSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 获取开屏配置（是否适配全面屏等）
        sharedPreferencesHelper = new SharedPreferencesHelper(this, "umeng");
        if (sharedPreferencesHelper.getSharedPreference("uminit", "").equals("1")) {
            getSplashAdSettings();
            // 如需适配刘海屏水滴屏，必须在onCreate方法中设置全屏显示
            if (isNotchAdaptation) {
                hideSystemUI();
            }
            setContentView(R.layout.activity_splash);
            container = this.findViewById(R.id.splash_container);
            Intent intent = getIntent();
            splashHolder = findViewById(R.id.splash_holder);
            boolean needLogo = false;
            try {
                needLogo = intent.getBooleanExtra("need_logo", true);
                needStartDemoList = intent.getBooleanExtra("need_start_demo_list", true);
                isSupportZoomOut = intent.getBooleanExtra("support_zoom_out", false);
                isZoomOutInAnother = intent.getBooleanExtra("zoom_out_in_another", false);
                isFullScreen = intent.getBooleanExtra("is_full_screen", false);
                fetchDelay = (Integer) intent.getSerializableExtra("fetch_delay");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (MainApplication.showFirstAd()) {
                if (!needLogo) {
                    findViewById(R.id.app_logo).setVisibility(View.GONE);
                }

                if (Build.VERSION.SDK_INT >= 23) {
                    checkAndRequestPermission();
                } else {
                    // 如果是Android6.0以下的机器，建议在manifest中配置相关权限，这里可以直接调用SDK
                    fetchSplashAD(this, container, getPosId(), this);
                }
            } else {
                next();
            }
        } else {
            //隐私协议授权弹窗
//            dialog();
            showPrivacy();
        }


    }

    @SuppressLint("ResourceType")
    public void dialog() {

        dialog = new Dialog(this, R.style.dialog);
        inflate = LayoutInflater.from(NSplashActivity.this).inflate(R.layout.diaologlayout, null);
        TextView succsebtn = inflate.findViewById(R.id.succsebtn);
        TextView canclebtn = inflate.findViewById(R.id.caclebtn);

        succsebtn.setOnClickListener(v -> {
            // uminit为1时代表已经同意隐私协议，sp记录当前状态
            sharedPreferencesHelper.put("uminit", "1");
            UMConfigure.submitPolicyGrantResult(getApplicationContext(), true);
            // 友盟sdk正式初始化
            UmInitConfig umInitConfig = new UmInitConfig();
            umInitConfig.UMinit(getApplicationContext());
            //QQ官方sdk授权
            Tencent.setIsPermissionGranted(true);
            //关闭弹窗
            dialog.dismiss();


            next();
            finish();

        });

        canclebtn.setOnClickListener(v -> {
            dialog.dismiss();
            UMConfigure.submitPolicyGrantResult(getApplicationContext(), false);
            //不同意隐私协议，退出app
            android.os.Process.killProcess(android.os.Process.myPid());
        });

        dialog.setContentView(inflate);
        Window dialogWindow = dialog.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);

        dialog.setCancelable(false);
        dialog.show();
    }


    private String getPosId() {
        String posId = getIntent().getStringExtra("pos_id");
        return TextUtils.isEmpty(posId) ? Constants.TAD_SCRREN : posId;
    }

    /**
     * ----------非常重要----------
     * <p>
     * Android6.0以上的权限适配简单示例：
     * <p>
     * 如果targetSDKVersion >= 23，那么建议动态申请相关权限，再调用优量汇SDK
     * <p>
     * SDK不强制校验下列权限（即:无下面权限sdk也可正常工作），但建议开发者申请下面权限，尤其是READ_PHONE_STATE权限
     * <p>
     * READ_PHONE_STATE权限用于允许SDK获取用户标识,
     * 针对单媒体的用户，允许获取权限的，投放定向广告；不允许获取权限的用户，投放通投广告，媒体可以选择是否把用户标识数据提供给优量汇，并承担相应广告填充和eCPM单价下降损失的结果。
     * <p>
     * Demo代码里是一个基本的权限申请示例，请开发者根据自己的场景合理地编写这部分代码来实现权限申请。
     * 注意：下面的`checkSelfPermission`和`requestPermissions`方法都是在Android6.0的SDK中增加的API，如果您的App还没有适配到Android6.0以上，则不需要调用这些方法，直接调用优量汇SDK即可。
     */
    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        List<String> lackedPermission = new ArrayList<>();
        if (!(checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.READ_PHONE_STATE);
        }

        // 检查读写存储权限开始
        // 快手SDK所需相关权限，存储权限，此处配置作用于流量分配功能，关于流量分配，详情请咨询运营;如果您的APP不需要快手SDK的流量分配功能，则无需申请SD卡权限
        if (!(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (!(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            lackedPermission.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        // 检查读写存储权限结束
        // 如果需要的权限都已经有了，那么直接调用SDK
        if (lackedPermission.size() == 0) {
            fetchSplashAD(this, container, getPosId(), this);
        } else {
            // 否则，建议请求所缺少的权限，在onRequestPermissionsResult中再看是否获得权限
            String[] requestPermissions = new String[lackedPermission.size()];
            lackedPermission.toArray(requestPermissions);
            requestPermissions(requestPermissions, 1024);
        }
    }

    private boolean hasAllPermissionsGranted(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1024 && hasAllPermissionsGranted(grantResults)) {
            fetchSplashAD(this, container, getPosId(), this);
        } else {
            ToastUtil.l("应用缺少必要的权限！请点击\"权限\"，打开所需要的权限。");
            try {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (Exception e) {
            }
            finish();
        }
    }

    /**
     * 拉取开屏广告，开屏广告的构造方法有3种，详细说明请参考开发者文档。
     *
     * @param activity    展示广告的activity
     * @param adContainer 展示广告的大容器
     * @param posId       广告位ID
     * @param adListener  广告状态监听器
     */
    private void fetchSplashAD(Activity activity, ViewGroup adContainer,
                               String posId, SplashADListener adListener) {
        fetchSplashADTime = System.currentTimeMillis();
        splashAD = getSplashAd(activity, posId, adListener, fetchDelay);
        // 设置是否全屏显示
        setSystemUi();

        if (isFullScreen) {
            splashAD.fetchFullScreenAndShowIn(adContainer);
        } else {
            splashAD.fetchAndShowIn(adContainer);
        }

    }

    protected SplashAD getSplashAd(Activity activity, String posId,
                                   SplashADListener adListener, Integer fetchDelay) {
        SplashAD splashAD =
                new SplashAD(activity, posId, adListener, fetchDelay == null ? 2000 : fetchDelay);
        if (isFullScreen) {
            splashAD.setDeveloperLogo(getIntent().getIntExtra("developer_logo", 0));
        }
        ServerSideVerificationOptions options = new ServerSideVerificationOptions.Builder()
                .setCustomData("APP's custom data") // 设置插屏全屏视频服务端验证的自定义信息
                .setUserId("APP's user id for server verify") // 设置服务端验证的用户信息
                .build();
        splashAD.setServerSideVerificationOptions(options);
        splashAD.setLoadAdParams(ADUtil.getLoadAdParams("splash"));
        splashAD.setRewardListener(this);
        return splashAD;
    }

    @Override
    public void onADPresent() {
        Log.i("AD_TENCENT", "SplashADPresent");
    }

    @Override
    public void onADClicked() {
        Log.i("AD_TENCENT", "SplashADClicked");
    }

    /**
     * 倒计时回调，返回广告还将被展示的剩余时间。
     * 通过这个接口，开发者可以自行决定是否显示倒计时提示，或者还剩几秒的时候显示倒计时
     *
     * @param millisUntilFinished 剩余毫秒数
     */
    @Override
    public void onADTick(long millisUntilFinished) {
        Log.i("AD_TENCENT", "SplashADTick " + millisUntilFinished + "ms");
    }

    @Override
    public void onADExposure() {
        Log.i("AD_TENCENT", "SplashADExposure");
    }

    @Override
    public void onADLoaded(long expireTimestamp) {
        mLoadSuccess = true;
        Log.i("AD_TENCENT", "SplashADFetch expireTimestamp: " + expireTimestamp
                + ", eCPMLevel = " + splashAD.getECPMLevel() + ", ECPM: " + splashAD.getECPM()
                + ", testExtraInfo:" + splashAD.getExtraInfo().get("mp")
                + ", request_id:" + splashAD.getExtraInfo().get("request_id"));

    }



    @Override
    public void onADDismissed() {
        Log.i("AD_TENCENT", "SplashADDismissed");
        if (zoomOutView != null) {
            ViewUtils.removeFromParent(zoomOutView);
        }
        next();
    }

    @Override
    public void onNoAD(AdError error) {
        @SuppressLint("DefaultLocale")
        String str = String.format("LoadSplashADFail, eCode=%d, errorMsg=%s", error.getErrorCode(),
                error.getErrorMsg());
        Log.i("AD_TENCENT", str);
        handler.post(() -> ToastUtil.s(str));
        /**
         * 为防止无广告时造成视觉上类似于"闪退"的情况，设定无广告时页面跳转根据需要延迟一定时间，demo
         * 给出的延时逻辑是从拉取广告开始算开屏最少持续多久，仅供参考，开发者可自定义延时逻辑，如果开发者采用demo
         * 中给出的延时逻辑，也建议开发者考虑自定义minSplashTimeWhenNoAD的值
         **/
        long alreadyDelayMills = System.currentTimeMillis() - fetchSplashADTime;//从拉广告开始到onNoAD已经消耗了多少时间
        long shouldDelayMills = alreadyDelayMills > minSplashTimeWhenNoAD ? 0 : minSplashTimeWhenNoAD
                - alreadyDelayMills;//为防止加载广告失败后立刻跳离开屏可能造成的视觉上类似于"闪退"的情况，根据设置的minSplashTimeWhenNoAD
        // 计算出还需要延时多久
        handler.postDelayed(() -> {
            if (needStartDemoList) {
                try {
                    NSplashActivity.this.startActivity(new Intent(NSplashActivity.this, MainActivity.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            NSplashActivity.this.finish();
        }, shouldDelayMills);
    }

    @Override
    public void onReward(Map<String, Object> map) {
        // TRANS_ID 获取服务端验证的唯一 ID
        Log.i("AD_TENCENT", "onReward " + map.get(ServerSideVerificationOptions.TRANS_ID));
    }

    /**
     * 设置一个变量来控制当前开屏页面是否可以跳转，当开屏广告为普链类广告时，点击会打开一个广告落地页，此时开发者还不能打开自己的App主页。当从广告落地页返回以后，
     * 才可以跳转到开发者自己的App主页；当开屏广告是App类广告时只会下载App。
     */
    private void next() {
        if (canJump) {
            if (needStartDemoList) {
                try {
//                    if (MainApplication._pref.getBoolean(Constants.PREF_FIRST,true)){
//                        MainApplication._pref.edit().putBoolean(Constants.PREF_FIRST,false).apply();
//                        Intent in = new Intent(this ,GuildsActivity.class);
//                        this.startActivity(in);
//                    }else{
                        Intent in = new Intent(this,MainActivity.class);
                        this.startActivity(in);
//                    }
                } catch (Exception e) {
                }
            }
            if (isZoomOut && isZoomOutInAnother) {
                //防止移除view后显示底图导致屏幕闪烁
                Bitmap b = splashAD.getZoomOutBitmap();
                if (b != null) {
                    splashHolder.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    splashHolder.setImageBitmap(b);
                }
                SplashZoomOutManager zoomOutManager = SplashZoomOutManager.getInstance();
                zoomOutManager.setSplashInfo(splashAD, container.getChildAt(0),
                        getWindow().getDecorView());
                this.setResult(RESULT_OK);
            }
            this.finish();
        } else {
            canJump = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        canJump = false;
    }

    @Override
    public String getPageName() {
        return "启动页";
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 获取开屏配置（是否适配全面屏等）
        getSplashAdSettings();
        if (canJump) {
            next();
        }
        canJump = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * 开屏页一定要禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                return super.onKeyDown(keyCode, event);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    public void onZoomOut() {
        isZoomOut = true;
        Log.d("AD_TENCENT", "onZoomOut");
        if (isZoomOutInAnother) {
            next();
        } else {
            SplashZoomOutManager splashZoomOutManager = SplashZoomOutManager.getInstance();
            ViewGroup content = findViewById(android.R.id.content);
            zoomOutView = splashZoomOutManager.startZoomOut(container.getChildAt(0), content, content,
                    new SplashZoomOutManager.AnimationCallBack() {
                        @Override
                        public void animationStart(int animationTime) {
                            Log.d("AD_TENCENT", "animationStart:" + animationTime);
                        }

                        @Override
                        public void animationEnd() {
                            Log.d("AD_TENCENT", "animationEnd");
                            splashAD.zoomOutAnimationFinish();
                        }
                    });
            findViewById(R.id.splash_main).setVisibility(View.GONE);
        }
    }

    @Override
    public void onZoomOutPlayFinish() {
        Log.d("AD_TENCENT", "onZoomOutPlayFinish");
    }

    @Override
    public boolean isSupportZoomOut() {
        return isSupportZoomOut;
    }

    private void hideSystemUI() {
        int systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        systemUiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        Window window = this.getWindow();
        window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        // 五要素隐私详情页或五要素弹窗关闭回到开屏广告时，再次设置SystemUi
        window.getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> setSystemUi());

        // Android P 官方方法
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(params);
        }
    }

    private void showSystemUI() {
        int systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        Window window = this.getWindow();
        window.getDecorView().setSystemUiVisibility(systemUiVisibility);
        // 五要素隐私详情页或五要素弹窗关闭回到开屏广告时，再次设置SystemUi
        window.getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> setSystemUi());
    }

    private void setSystemUi() {
        if (!isNotchAdaptation) {
            showSystemUI();
        } else {
            hideSystemUI();
        }
    }

    private void getSplashAdSettings() {
        SharedPreferences sp = this.getSharedPreferences("com.qq.e.union.demo.debug", Context.MODE_PRIVATE);
        String splashAdNotchSetting = sp.getString("splashAdNotchAdaptation", "true");
        isNotchAdaptation = Boolean.parseBoolean(splashAdNotchSetting);
    }

    @Override
    public void onClick(View view) {

    }

    private void alertWebview(String url) {
        AlertDialog webviewDialoger = new AlertDialog.Builder(this)
            .setNegativeButton( "取消",  (dialog, which) -> {
                dialog.dismiss();
                UMConfigure.submitPolicyGrantResult(getApplicationContext(), false);
                //不同意隐私协议，退出app
                android.os.Process.killProcess(android.os.Process.myPid());
            })
            .setPositiveButton("确定", (dialog, which) -> {
                sharedPreferencesHelper.put("uminit", "1");
                UMConfigure.submitPolicyGrantResult(getApplicationContext(), true);
                /*** 友盟sdk正式初始化*/
                UmInitConfig umInitConfig = new UmInitConfig();
                umInitConfig.UMinit(getApplicationContext());
                //QQ官方sdk授权
                Tencent.setIsPermissionGranted(true);
                //关闭弹窗
                dialog.dismiss();
                next();
            })
            .create();
        WebView mwebView = new WebView(this);
        this.alterShowed = false;
        mwebView.loadUrl(url);
        mwebView.setWebViewClient( new WebViewClient() {
            //设置结束加载函数
            @Override
            public void onPageFinished(WebView view, String url) {
                if (!alterShowed) {
                    alterShowed = true;
                    webviewDialoger.show();
                    webviewDialoger.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
                    webviewDialoger.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                }
            }
        });
        webviewDialoger.setView(mwebView);
    }

    private void showPrivacy() {
        //显示隐私协议窗口
        String url = "file:///android_asset/inside.html";
        alertWebview(url);
    }
}
