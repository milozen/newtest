package com.zhanghuang;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.ads.rewardvideo.ServerSideVerificationOptions;
import com.qq.e.comm.util.AdError;
import com.zhanghuang.base.BaseBackActivity;
import com.zhanghuang.bean.RecordBean;
import com.zhanghuang.db.DaoManager;
import com.zhanghuang.entity.RecordBeanDao;
import com.zhanghuang.events.AddRecordEvent;
import com.zhanghuang.events.UpdateUserEvent;
import com.zhanghuang.modes.BaseMode;
import com.zhanghuang.net.RequestData;
import com.zhanghuang.netinterface.BaseInterface;
import com.zhanghuang.util.ADUtil;
import com.zhanghuang.util.AndroidUtil;
import com.zhanghuang.util.Constants;
import com.zhanghuang.util.TimeUtil;
import com.zhanghuang.util.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yuanlei on 2017/3/28.
 */

public class UploadRecordActivity extends BaseBackActivity 
  implements RewardVideoADListener{

  @BindView(R.id.upload_record_view_start_time_text)
  TextView startText;
  @BindView(R.id.upload_record_view_end_time_text)
  TextView endText;
  @BindView(R.id.upload_record_view_long_text)
  TextView longText;
  @BindView(R.id.upload_record_view_intro_edit)
  EditText introEdit;
  @BindView(R.id.upload_record_view_progress)
  ProgressBar progressBar;
  @BindView(R.id.upload_record_view_commit_button)
  Button saveBtn;
  @BindView(R.id.upload_record_view_buy_vip_button)
  Button buyVipBtn;
  @BindView(R.id.upload_record_vip_tip_container)
  LinearLayout buyVipTipContainer;
  @BindView(R.id.upload_record_vip_tip)
  TextView buyVipTip;


  private SimpleDateFormat simpleDateFormat;
  private String startTimes;
  private String endTimes;
  private String intro;
  private String recordId;
  private String duration;

  private RequestData rd;
  private static final String TAG = UploadRecordActivity.class.getSimpleName();
  private RewardVideoAD mRewardVideoAD;
  private boolean mIsLoadSuccess = false;
  private boolean autoShowAd = false;
  private ProductActivity productActivity;

  @Override
  protected void init(Bundle savedInstanceState) {
    getWindow().setBackgroundDrawable(
        new ColorDrawable(ContextCompat.getColor(this, R.color.bg_dark_gray)));
    initData();
  }

  @Override
  public void onStart() {
    super.onStart();
    EventBus.getDefault().register(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    EventBus.getDefault().unregister(this);
  }

  @Override
  public int getLayoutId() {
    return R.layout.upload_record_view;
  }

  private void initData() {
    rd = new RequestData(this);

    simpleDateFormat = Constants.DEFAULT_DATETIME_FORMAT();
    Intent in = getIntent();
    long startTime = in.getLongExtra(Constants.STARTTIME, 0);
    long endTime = in.getLongExtra(Constants.ENDTIME, 0);
    intro = in.getStringExtra(Constants.RECORDINTRO);
    recordId = in.getStringExtra(Constants.RECORDID);
    long t = in.getLongExtra(Constants.DURATION, 0);
    duration = String.valueOf(t);
    startTimes = simpleDateFormat.format(new Date(startTime));
    endTimes = simpleDateFormat.format(new Date(endTime));
    startText.setText(startTimes);
    endText.setText(endTimes);
    // check vip
    updateVipInfo();



    longText.setText(TimeUtil.dateDiffSingle(t));
    if (MainApplication._pref.getBoolean(Constants.PREF_SOUND_ON, true)) {
      int[] times = TimeUtil.getSoundTime(t * 1000);
      setMediaSound(times[0], times[1], times[2]);
    }
  }

  private void updateVipInfo() {
    if (!MainApplication.isVip() && !MainApplication.isInReview()) {
      buyVipTipContainer.setVisibility(View.VISIBLE);
      buyVipBtn.setVisibility(View.VISIBLE);
      int totalCount = MainApplication._pref.getInt(Constants.PREF_SAVE_AD_COUNT, 1);
      buyVipTip.setText("VIP或者每天"+totalCount+"次后可直接保存.\nVIP可自动将记录保存至服务器.");
    } else {
      buyVipTipContainer.setVisibility(View.GONE);
      buyVipBtn.setVisibility(View.GONE);
    }
    if (MainApplication.isVip()) {
      saveBtn.setText("保存并上传");
    } else if (MainApplication.showVideoAd()) {
      loadAd();
      saveBtn.setText("观看视频并保存");
    } else {
      saveBtn.setText("保存");
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      AlertDialog.Builder adb = new AlertDialog.Builder(this);
      adb.setTitle("确定不保存站桩记录?");
      adb.setCancelable(true);
      adb.setNeutralButton("取消", (dialog, which) -> {

      });
      adb.setNegativeButton("确定", (dialog, which) -> onBackPressed());
      adb.show();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @OnClick(R.id.upload_record_view_commit_button)
  public void commit() {
    if (MainApplication.showVideoAd()) {
      showAD();
    } else {
      saveOrUploadRecord();
    }
  }

  @OnClick(R.id.upload_record_view_buy_vip_button)
  public void buyVip() {
    showProductList();
  }

  @Subscribe
  public void onUpdateUser(UpdateUserEvent e) {
    Log.i(TAG, "receive UpdateUserEvent");
    rd.getUserInfo(((success, result, message, err) -> {
      if (success) {
        updateVipInfo();
      }
    }));
  }

  public void showProductList() {
    if (productActivity == null) {
      productActivity = new ProductActivity(this);
    }
    productActivity.show();
  }

  private void saveOrUploadRecord() {
    boolean upload = MainApplication.isVip();
    String title = simpleDateFormat.format(new Date(System.currentTimeMillis()));
    intro = introEdit.getText().toString();
    if (intro == null || intro.equals("")) {
      intro = "";
    }
    if (recordId == null || recordId.equals("")) {
      recordId = null;
    }
    if (!upload) {
      saveRecord(title);
      return;
    }
    if (!AndroidUtil.checkNet(this)) {
      saveRecord(title);
      AlertDialog.Builder adb = new AlertDialog.Builder(this);
      adb.setTitle("上传失败，当前网络状况不佳");
      adb.setMessage("本地记录已保存至您手机，请稍后将其同步至服务器");
      adb.setCancelable(true);
      adb.setNeutralButton("稍后上传", (dialog, which) -> onBackPressed());
      adb.setNegativeButton("重试", (dialog, which) -> commit());
      adb.show();
      return;
    }
    rd.saveZz(recordId, title, startTimes, endTimes, duration, intro, saveZzIf);
    progressBar.setVisibility(View.VISIBLE);
  }

  private void updateVideoCount() {
    int todayCount = MainApplication._pref.getInt(Constants.PREF_SAVE_VIDEO_COUNT, 0);
    Date d1 = new Date(System.currentTimeMillis());
    String day = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(d1);
    String dayStore = MainApplication._pref.getString(Constants.PREF_SAVE_VIDEO_DAY,"");
    if (!day.equals(dayStore)) {
      todayCount = 0;
    }
    todayCount += 1;
    MainApplication._pref.edit()
        .putString(Constants.PREF_SAVE_VIDEO_DAY, day)
        .putInt(Constants.PREF_SAVE_VIDEO_COUNT, todayCount)
        .apply();
  }

  @Override
  public String getPageName() {
    return "上传记录";
  }

  private void saveRecord(String title) {

    Calendar c = Calendar.getInstance();
    c.setTime(new Date());
    int month = c.get(Calendar.MONTH);
    int year = c.get(Calendar.YEAR);
    RecordBeanDao recordBeanDao = 
      DaoManager.getInstance().getDaoSession().getRecordBeanDao();

    RecordBean rb = new RecordBean();
    rb.setTitle(title);
    rb.setBegin_time(startTimes);
    rb.setDesc(intro);
    rb.setDuration(duration);
    rb.setEnd_time(endTimes);
    rb.setHasUpload(false);
    rb.setYear(String.valueOf(year));
    rb.setMonth(String.valueOf(month + 1));
    recordBeanDao.insert(rb);
    EventBus.getDefault().post(new AddRecordEvent());
  }

  private final BaseInterface saveZzIf = 
    (boolean success, BaseMode result, String message, String err) -> {
    progressBar.setVisibility(View.GONE);
    if (success) {
        Toast.makeText(UploadRecordActivity.this, "保存成功！", Toast.LENGTH_SHORT).show();
        finish();
        EventBus.getDefault().post(new AddRecordEvent());
    } else {
        Toast.makeText(UploadRecordActivity.this, err, Toast.LENGTH_SHORT).show();
    }
  };

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      AlertDialog.Builder adb = new AlertDialog.Builder(this);
      adb.setTitle("确定不保存站桩记录?");
      adb.setCancelable(true);
      adb.setNeutralButton("取消", (dialog, which) -> {

      });
      adb.setNegativeButton("确定", (dialog, which) -> finish());
      adb.show();
      return false;
    } else {
      return super.onKeyDown(keyCode, event);
    }
  }

  private void setMediaSound(int hour, int minute, int second) {
    final MediaPlayer mp1 = MediaPlayer.create(this, R.raw.ding);
    final MediaPlayer hourMp = MediaPlayer.create(this, Constants.timeSounds[hour]);
    final MediaPlayer minuteMp = MediaPlayer.create(this, Constants.timeSounds[minute]);
    final MediaPlayer secondMp = MediaPlayer.create(this, Constants.timeSounds[second]);
    final MediaPlayer hourMp1 = MediaPlayer.create(this, R.raw.w_hour);
    final MediaPlayer minuteMp1 = MediaPlayer.create(this, R.raw.w_minute);
    final MediaPlayer secondMp1 = MediaPlayer.create(this, R.raw.w_second);

    MediaPlayer.OnCompletionListener listener = mp -> {
      mp1.release();
      hourMp.release();
      hourMp1.release();
      minuteMp.release();
      minuteMp1.release();
      secondMp.release();
      secondMp1.release();
    };
    if (hour > 0) {
      mp1.setNextMediaPlayer(hourMp);
      hourMp.setNextMediaPlayer(hourMp1);
      if (minute > 0) {
        hourMp1.setNextMediaPlayer(minuteMp);
        minuteMp.setNextMediaPlayer(minuteMp1);
        if (second > 0) {
          minuteMp1.setNextMediaPlayer(secondMp);
          secondMp.setNextMediaPlayer(secondMp1);
          secondMp1.setOnCompletionListener(listener);
        } else {
          minuteMp1.setOnCompletionListener(listener);
        }
      } else {
        if (second > 0) {
          hourMp1.setNextMediaPlayer(secondMp);
          secondMp.setNextMediaPlayer(secondMp1);
          secondMp1.setOnCompletionListener(listener);
        } else {
          hourMp1.setOnCompletionListener(listener);
        }
      }
    } else {
      if (minute > 0) {
        mp1.setNextMediaPlayer(minuteMp);
        minuteMp.setNextMediaPlayer(minuteMp1);
        if (second > 0) {
          minuteMp1.setNextMediaPlayer(secondMp);
          secondMp.setNextMediaPlayer(secondMp1);
          secondMp1.setOnCompletionListener(listener);
        } else {
          minuteMp1.setOnCompletionListener(listener);
        }
      } else {
        mp1.setNextMediaPlayer(secondMp);
        secondMp.setNextMediaPlayer(secondMp1);
        secondMp1.setOnCompletionListener(listener);

      }
    }

//    mp1.setNextMediaPlayer(hourMp);
//    hourMp.setNextMediaPlayer(hourMp1);
//    hourMp1.setNextMediaPlayer(minuteMp);
//    minuteMp.setNextMediaPlayer(minuteMp1);
//    minuteMp1.setNextMediaPlayer(secondMp);
//    secondMp.setNextMediaPlayer(secondMp1);
//    secondMp1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//      @Override
//      public void onCompletion(MediaPlayer mp) {
//        mp1.release();
//        hourMp.release();
//        hourMp1.release();
//        minuteMp.release();
//        minuteMp1.release();
//        secondMp.release();
//        secondMp1.release();
//      }
//    });
    mp1.start();
  }
  // begin of Tencent AD

  private void showAD() {
    if (ADUtil.isAdValid(mIsLoadSuccess, mRewardVideoAD != null 
          && mRewardVideoAD.isValid(), true)) {
      mRewardVideoAD.showAD();
    } else {
      autoShowAd = true;
      loadAd();
    }
  }

  protected RewardVideoAD getRewardVideoAD() {
    String editPosId = Constants.TAD_VIDEO;
    boolean volumeOn = false;
    RewardVideoAD rvad;
    if (mRewardVideoAD == null) {
      rvad = new RewardVideoAD(this, editPosId, this, volumeOn);
      rvad.setNegativeFeedbackListener(() -> Log.i(TAG, "onComplainSuccess"));
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
  // 显示激励视频 调用
  // mRewardVideoAD.showAD();
  // mRewardVideoAD.showAD(UploadRecordActivity.this);
  protected void loadAd(){
    // 1. 初始化激励视频广告
    mRewardVideoAD = getRewardVideoAD();
    mIsLoadSuccess = false;
    // 2. 加载激励视频广告
    mRewardVideoAD.loadAD();
  }

  @Override
  public void onADLoad() {
    ToastUtil.l("广告加载成功 ！ ");
    if (mRewardVideoAD.getRewardAdType() == RewardVideoAD.REWARD_TYPE_VIDEO) {
      Log.d(TAG, "eCPMLevel = " + mRewardVideoAD.getECPMLevel() + ", ECPM: " + mRewardVideoAD.getECPM()
          + " ,video duration = " + mRewardVideoAD.getVideoDuration()
          + ", testExtraInfo:" + mRewardVideoAD.getExtraInfo().get("mp")
          + ", request_id:" + mRewardVideoAD.getExtraInfo().get("request_id"));
    } else if (mRewardVideoAD.getRewardAdType() == RewardVideoAD.REWARD_TYPE_PAGE) {
      Log.d(TAG, "eCPMLevel = " + mRewardVideoAD.getECPMLevel()
          + ", ECPM: " + mRewardVideoAD.getECPM()
          + ", testExtraInfo:" + mRewardVideoAD.getExtraInfo().get("mp")
          + ", request_id:" + mRewardVideoAD.getExtraInfo().get("request_id"));
    }

    mIsLoadSuccess = true;
    if (ADUtil.isAdValid(autoShowAd, mRewardVideoAD != null && mRewardVideoAD.isValid(), true)) {
      autoShowAd = false;
      mRewardVideoAD.showAD();
    }
  }

  @Override
  public void onVideoCached() {
    Log.i(TAG, "onVideoCached");
  }

  @Override
  public void onADShow() {
    Log.i(TAG, "onADShow");
  }

  @Override
  public void onADExpose() {
    Log.i(TAG, "onADExpose");
  }

  // 激励视频广告激励发放，若选择了服务端验证，可以通过 ServerSideVerificationOptions#TRANS_ID 键从 map 中获取此次交易的 id；若未选择服务端验证，则不需关注 map 参数
  @Override
  public void onReward(Map<String, Object> map) {
    Log.i(TAG, "onReward " + map.get(ServerSideVerificationOptions.TRANS_ID));  // 获取服务端验证的唯一 ID
    mIsLoadSuccess = false;
    autoShowAd = false;
    updateVideoCount();
    saveOrUploadRecord();
  }

  @Override
  public void onADClick() {
    Log.i(TAG, "onADClick");
  }

  @Override
  public void onVideoComplete() {
    Log.i(TAG, "onVideoComplete");
  }

  @Override
  public void onADClose() {
    Log.i(TAG, "onADClose");
  }

  @Override
  public void onError(AdError adError) {
    String msg = String.format(Locale.getDefault(), "onError, error code: %d, error msg: %s",
        adError.getErrorCode(), adError.getErrorMsg());
    ToastUtil.s(msg);
    Log.i(TAG, "onError, adError=" + msg);
  }
}
