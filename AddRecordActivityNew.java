package com.zhanghuang;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.appcompat.widget.Toolbar;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zhanghuang.base.BaseActivity;
import com.zhanghuang.events.CountTimeEvent;
import com.zhanghuang.service.CountChangeListener;
import com.zhanghuang.service.CountService;
import com.zhanghuang.util.AndroidUtil;
import com.zhanghuang.util.Constants;
import com.zhanghuang.util.DLog;
import com.zhanghuang.util.DeviceUtil;
import com.zhanghuang.util.TimeUtil;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yuanlei on 2017/3/27.
 */

public class AddRecordActivityNew extends BaseActivity {

    public static final int COUNTING_SCREEN_LIGHT = 5; //无任何操作屏幕亮度
    public static final int COUNT_DOWN_START_VALUE = 30; //屏幕亮度变换倒计时起始值

    @BindView(R.id.add_record_view_start_text)
    TextView controlText;
    @BindView(R.id.add_record_view_cancel_container)
    TextView cancelContainer;
    @BindView(R.id.add_record_view_over_container)
    TextView overContainer;
    @BindView(R.id.add_record_view_second_text)
    TextView secondText;
    @BindView(R.id.add_record_view_time_show_text)
    TextView timeShowText;
    @BindView(R.id.tv_voice_tip)
    TextView tvVoiceTip;

    @BindView(R.id.zx_info_view_toolbar)
    Toolbar toolbar;
    @BindView(R.id.sb_set_volumn)
    AppCompatSeekBar sbSetVolumn;
    @BindView(R.id.zx_info_view_title)
    TextView zxInfoViewTitle;
    @BindView(R.id.add_record_view_set_image)
    ImageView addRecordViewSetImage;
    private boolean isBind;
    private int curLight = 0;


    private int count = 4;
    private static final Handler handler = new Handler();
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (count == 1) {
                if (MainApplication._pref.getBoolean(Constants.PREF_SOUND_ON, true)) {
                    playSound(R.raw.w_start);
                }
                if (binder != null) {
                    start();
                    controlText.setEnabled(true);
                }
                secondText.setVisibility(View.GONE);
                MainApplication._pref.edit().putLong(Constants.PREF_ZZ_START_REAL_TIME, SystemClock.elapsedRealtime())
                        .putLong(Constants.PREF_ZZ_START_TIME, System.currentTimeMillis())
                        .putBoolean(Constants.PREF_IS_ZZ_START, true)
                        .apply();
                setView();
            } else {
                count--;
                secondText.setVisibility(View.VISIBLE);
                if (count == 0) {
                    secondText.setText("开始");
                } else {
                    secondText.setText(String.valueOf(count));
                }
                handler.postDelayed(this, 1000);
            }
        }
    };

    private long duration = 0;

    private BroadcastReceiver receiver;

    private CountService.CountBinder binder;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBind = true;
            binder = (CountService.CountBinder) service;
            binder.setCountChangeListener(new CountChangeListener() {
                @Override
                public void countChange(int count) {
                    duration = count;
                    DLog.i("Count", TimeUtil.timeDiffSingle(count * 1000));
                    timeShowText.setText(TimeUtil.timeDiffSingle(count * 1000));
                }

                @Override
                public void unbind(boolean unbind) {
                    binder = null;
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            DLog.i("Count", "服务断开了");
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //防止屏幕休眠
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.add_record_view);
        ButterKnife.bind(this);
        //兼容性处理
        //toolbar 往下移一个状态栏的高度
        ((FrameLayout.LayoutParams) toolbar.getLayoutParams()).setMargins(0, DeviceUtil.getStatusBarHeight(this), 0, 0);
        if (!MainApplication._pref.getBoolean(Constants.PREF_IS_ZZ_START, false)) {
            setView();
            startMyService();
        }
        sbSetVolumn.setMax(DeviceUtil.getMaxVolumn(this));
        sbSetVolumn.setProgress(DeviceUtil.getCurVolumn(this));
        sbSetVolumn.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DeviceUtil.setVolumn(AddRecordActivityNew.this, progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //保存当前屏幕亮度
        curLight = AndroidUtil.getSystemBrightness(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MainApplication._pref.getBoolean(Constants.PREF_IS_ZZ_START, false)) {
            setView();
            startMyService();
        }
        if (MainApplication._pref.getBoolean(Constants.PREF_SOUND_ON, true)) {
            tvVoiceTip.setText("语音提示已开启");
        } else {
            tvVoiceTip.setText("语音提示已关闭");
        }
        //启动调整亮度定时器
        handler.postDelayed(countDown, 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //取消亮屏计时
        handler.removeCallbacks(countDown);
//        if (isServiceWork() && binder != null) {
//            if (isBind) {
//                unbindService(connection);
//                isBind = false;
//            }
//        }
    }

    private int countDownValue = COUNT_DOWN_START_VALUE;

    private Runnable countDown = new Runnable() {
        @Override
        public void run() {
            DLog.i("countdown", "countDownValue:  " + countDownValue);
            if (countDownValue != 0) {
                countDownValue--;
                handler.postDelayed(this, 1000);
            } else {
                AndroidUtil.setLight(AddRecordActivityNew.this, COUNTING_SCREEN_LIGHT);
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //用户触摸屏幕时恢复屏幕亮度,并重新开始计时

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            AndroidUtil.setLight(this, curLight);
            countDownValue = COUNT_DOWN_START_VALUE;
            handler.removeCallbacks(countDown);
            handler.postDelayed(countDown, 1000);//重新计时
        }
        return super.dispatchTouchEvent(ev);
    }

    private void setView() {
        if (!MainApplication._pref.getBoolean(Constants.PREF_IS_ZZ_START, false)) {
            controlText.setText("开始");
        } else {
            controlText.setText(MainApplication._pref.getBoolean(Constants.PREF_IS_ZZ_PAUSE, false) ? "继续" : "暂停");
        }
        cancelContainer.setVisibility(MainApplication._pref.getBoolean(Constants.PREF_IS_ZZ_START, false) ? View.INVISIBLE : View.VISIBLE);
        overContainer.setVisibility(MainApplication._pref.getBoolean(Constants.PREF_IS_ZZ_START, false) ? View.VISIBLE : View.INVISIBLE);
    }

    @OnClick(R.id.add_record_view_start_text)
    public void controlClick() {
        if (!MainApplication._pref.getBoolean(Constants.PREF_IS_ZZ_START, false)) {
            handler.postDelayed(runnable, 100);
            controlText.setEnabled(false);
            cancelContainer.setVisibility(View.INVISIBLE);
        } else {
            if (!MainApplication._pref.getBoolean(Constants.PREF_IS_ZZ_PAUSE, false)) {
                if (MainApplication._pref.getBoolean(Constants.PREF_SOUND_ON, true)) {
                    playSound(R.raw.w_pause);
                }
                pause();
                MainApplication._pref.edit().putBoolean(Constants.PREF_IS_ZZ_PAUSE, true)
                        .putLong(Constants.PREF_ZZ_PAUSE_TIME, SystemClock.elapsedRealtime())
                        .apply();
            } else {
                if (MainApplication._pref.getBoolean(Constants.PREF_SOUND_ON, true)) {
                    playSound(R.raw.w_resume);

                }
                continueZ();
                MainApplication._pref.edit().putBoolean(Constants.PREF_IS_ZZ_PAUSE, false)
                        .putLong(Constants.ZZ_CONTAINUE, SystemClock.elapsedRealtime())
                        .apply();
            }
            controlText.setText(MainApplication._pref.getBoolean(Constants.PREF_IS_ZZ_PAUSE, false) ? "继续" : "暂停");
        }
    }


    public void outPage() {
        over();
        stopMyService();
        MainApplication._pref.edit().putBoolean(Constants.PREF_IS_ZZ_START, false)
                .putBoolean(Constants.PREF_IS_ZZ_PAUSE, false)
                .putLong(Constants.PREF_ZZ_PAUSE_TIME_LONG, 0)
                .putLong(Constants.PREF_ZZ_START_REAL_TIME, 0)
                .putLong(Constants.PREF_ZZ_START_TIME, 0)
                .putLong(Constants.ZZ_CONTAINUE, 0)
                .apply();
    }


    @OnClick(R.id.add_record_view_over_container)
    public void overClick() {
        long startTime = MainApplication._pref.getLong(Constants.PREF_ZZ_START_TIME, 0);
        outPage();
        long endTime = System.currentTimeMillis();
        Intent in = new Intent(AddRecordActivityNew.this, UploadRecordActivity.class);
        in.putExtra(Constants.STARTTIME, startTime);
        in.putExtra(Constants.ENDTIME, endTime);
        in.putExtra(Constants.DURATION, duration);
        startActivity(in);
        finish();
    }


    @OnClick(R.id.add_record_view_cancel_container)
    public void cancelClick() {
        outPage();

        // 启动主界面的Activity
//        Intent in = new Intent(AddRecordActivityNew.this, MainActivity.class);
//        in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        startActivity(in);

        finish();
    }

    @OnClick(R.id.add_record_view_set_image)
    public void setClick() {
        Intent in = new Intent(AddRecordActivityNew.this, AddRecordSetActivityNew.class);
        startActivity(in);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (MainApplication._pref.getBoolean(Constants.PREF_IS_ZZ_START, false)) {
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle("确定退出站桩?");
                adb.setCancelable(true);
                adb.setNeutralButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                adb.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        outPage();

                        // 启动主界面的Activity
                        Intent in = new Intent(AddRecordActivityNew.this, MainActivity.class);
                        in.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(in);

                        finish();
                    }
                });
                adb.show();
                return true;
            } else {
                stopMyService();
                return super.onKeyDown(keyCode, event);
            }
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            sbSetVolumn.setProgress(DeviceUtil.getCurVolumn(this) - 1);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            sbSetVolumn.setProgress(DeviceUtil.getCurVolumn(this) + 1);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void playSound(int res) {
        MediaPlayer mp = MediaPlayer.create(this, res);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mp.start();
    }


    private boolean isServiceWork() {
        return AndroidUtil.isServiceWork(this, "com.zhanghuang.service.CountService");
    }

    private void startMyService() {
        Intent in = new Intent(AddRecordActivityNew.this, CountService.class);
        if (!isServiceWork()) {
            startService(in);
        }
        if (!isBind) {
            bindService(in, connection, BIND_IMPORTANT);
        }
    }

    private void stopMyService() {
        if (isBind) {
            unbindService(connection);
            isBind = false;
        }
        stopService(new Intent(AddRecordActivityNew.this, CountService.class));
    }

    @Override
    public String getPageName() {
        return "站桩";
    }

    private void start() {
        EventBus.getDefault().post(new CountTimeEvent(CountTimeEvent.EVENT_START));
    }

    private void pause() {
        EventBus.getDefault().post(new CountTimeEvent(CountTimeEvent.EVENT_PAUSE));
    }

    private void over() {
        EventBus.getDefault().post(new CountTimeEvent(CountTimeEvent.EVENT_OVER));
    }

    private void continueZ() {
        EventBus.getDefault().post(new CountTimeEvent(CountTimeEvent.EVENT_CONTINUE));
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        DLog.i("Count", "被干掉了");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        handler.removeCallbacks(countDown);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }


}
