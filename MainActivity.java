package com.zhanghuang;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RadioGroup;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;
import com.zhanghuang.base.BaseActivity;
import com.zhanghuang.modes.VsnMode;
import com.zhanghuang.net.RequestData;
import com.zhanghuang.util.AppUpdate;
import com.zhanghuang.util.Constants;
import com.zhanghuang.util.DeviceUtil;
import com.zhanghuang.view.NoScrollViewpager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.main_viewpager)
    NoScrollViewpager viewpager;
    @BindView(R.id.rg_bottom_bar)
    RadioGroup rgBottomBar;
    @BindView(R.id.fl_start)
    FrameLayout flStart;
    private MainFragAdapter adapter;

    private RequestData rd;

    // APP_ID 替换为你的应用从官方网站申请到的合法appID
    private static final String APP_ID = "wx5846c59e6f1304a9";
    // IWXAPI 是第三方app和微信通信的openApi接口
    private IWXAPI api;
    private BroadcastReceiver broadcastReceiver;

    @SuppressLint("StaticFieldLeak")
    public static MainActivity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initData();
        initView();
        getVersion();

        DisplayMetrics dm = getResources().getDisplayMetrics();
        MainApplication.screenHeight = dm.heightPixels;
        MainApplication.screenWidth = dm.widthPixels;

//        DeviceUtil.getDeviceInfo(this);

        if (BuildConfig.DEBUG) {

            String mac = DeviceUtil.getLocalMacAddressFromWifiInfo(this);
            Log.i("MAC", mac);
        }
        activity = this;
        regToWx();
    }

    private void initData() {
        adapter = new MainFragAdapter(this.getSupportFragmentManager());
        MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false);
        if (!MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
            Intent in = new Intent(MainActivity.this, Login.class);
            startActivity(in);
        } else if (MainApplication._pref.getBoolean(Constants.PREF_IS_ZZ_START, false)) {
            Intent in = new Intent(MainActivity.this, AddRecordActivityNew.class);
            startActivity(in);
        }
        rd = new RequestData(this);
        rd.fetchCfg((success, result, message, err) -> {
        });
        rd.getVersion((success, result, message, err) -> {
            if (result instanceof VsnMode) {
                results = (VsnMode) result;
                checkUpdate();
            }
        });
    }

    private VsnMode results;

    private void checkUpdate() {
        int per = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (PackageManager.PERMISSION_GRANTED != per) {
            //申请读sd卡权限
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1000);
        } else {
            AppUpdate.getInstance().checkAppVer(MainActivity.this, results);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            checkUpdate();
        }
    }

    private void initView() {
        viewpager.setAdapter(adapter);
        viewpager.setCurrentItem(Constants.MAIN_FRAG);
        rgBottomBar.setOnClickListener(v -> Log.i("Click", "rgBootmBarClick"));
        rgBottomBar.setOnCheckedChangeListener((group, checkedId) -> {

            int select = -1;
            switch (checkedId) {
                case R.id.rb_home:
                    select = 0;
                    break;
                case R.id.rb_zixun:
                    select = 1;
                    break;
                case R.id.rb_found:
                    select = 2;
                    break;
                case R.id.rb_person:
                    select = 3;
                    break;
            }
            viewpager.setCurrentItem(select);
        });
    }




    @Override
    public String getPageName() {
        return MainActivity.class.getSimpleName();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getPageName());

    }
    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getPageName());
    }

    private void getVersion() {
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            MainApplication.ver = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @OnClick({R.id.fl_start})
    public void onClick() {
        if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
            Intent in = new Intent(MainActivity.this, AddRecordActivityNew.class);
            startActivity(in);
        } else {
            Intent in = new Intent(MainActivity.this, Login.class);
            startActivity(in);
        }
    }

    private void regToWx() {
        // 通过WXAPIFactory工厂，获取IWXAPI的实例
        api = WXAPIFactory.createWXAPI(this, Constants.APP_ID, true);

        // 将应用的appId注册到微信
        api.registerApp(Constants.APP_ID);
        if (broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // 将该app注册到微信
                    api.registerApp(Constants.APP_ID);
                }
            };
            //建议动态监听微信启动广播进行注册到微信
            registerReceiver(broadcastReceiver, new IntentFilter(ConstantsAPI.ACTION_REFRESH_WXAPP));
        }
    }

//    @Override
//    protected void onStop() {
//        if (broadcastReceiver != null) {
//            unregisterReceiver(broadcastReceiver);
//        }
//        super.onStop();
//    }

}
