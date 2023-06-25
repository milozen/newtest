package com.zhanghuang;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
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

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.zhanghuang.base.BaseBackActivity;
import com.zhanghuang.bean.RecordBean;
import com.zhanghuang.db.DaoManager;
import com.zhanghuang.entity.RecordBeanDao;
import com.zhanghuang.events.AddRecordEvent;
import com.zhanghuang.events.UpdateUserEvent;
import com.zhanghuang.modes.BaseMode;
import com.zhanghuang.modes.User;
import com.zhanghuang.net.RequestData;
import com.zhanghuang.netinterface.BaseInterface;
import com.zhanghuang.util.AndroidUtil;
import com.zhanghuang.util.Constants;
import com.zhanghuang.util.TimeUtil;
import com.zhanghuang.net.RequestData;

import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by yuanlei on 2017/3/28.
 */

public class UploadRecordActivity extends BaseBackActivity {

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

  @BindView(R.id.upload_record_view_buy_vip_button)
  Button buyVipBtn;
  @BindView(R.id.upload_record_vip_tip_container)
  LinearLayout buyVipTipContainer;

  private SimpleDateFormat simpleDateFormat;
  private String startTimes;
  private String endTimes;
  private String intro;
  private String recordId;
  private String duration;

  private RequestData rd;

  private ProductActivity productActivity;

  @Override
  protected void onStart() {
    super.onStart();
    EventBus.getDefault().register(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    EventBus.getDefault().unregister(this);
  }
  
  @Override
  protected void init(Bundle savedInstanceState) {
    getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.bg_dark_gray)));
    initData();
  }

  @Override
  public int getLayoutId() {
    return R.layout.upload_record_view;
  }

  private void initData() {
    rd = new RequestData(this);

    simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
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

    longText.setText(TimeUtil.dateDiffSingle(t));
    if (MainApplication._pref.getBoolean(Constants.PREF_SOUND_ON, true)) {
      int[] times = TimeUtil.getSoundTime(t * 1000);
      setMediaSound(times[0], times[1], times[2]);
    }

    // check vip
    updateVipInfo();
  }

  private void updateVipInfo() {
    //更新下用户信息
    rd.getUserInfo(getUserInfoIf);
    Log.i("INFO","USER IS "+ MainApplication.isVip());
    if (!MainApplication.isVip() ) {
      buyVipTipContainer.setVisibility(View.VISIBLE);
      buyVipBtn.setVisibility(View.VISIBLE);
    } else {
      buyVipTipContainer.setVisibility(View.GONE);
      buyVipBtn.setVisibility(View.GONE);
    }
  }

  // 添加处理 UpdateUserEvent 的方法,充值成功或者失败
  @Subscribe
  public void onUpdateUserEvent(UpdateUserEvent event) {
    //关闭充值窗口
    Log.i("INFO","记录页面充值成功");
    if (productActivity != null) {
      productActivity.dismiss();
    }
    //更新界面
    updateVipInfo();
  }

  private final BaseInterface getUserInfoIf = new BaseInterface() {
    @Override
    public void response(boolean success, BaseMode result, String message, String err) {
      if (success) {
        Log.i("INFO","USERINFO success");
        if (MainApplication.isVip()) {
          Log.i("INFO","IS VIP");
        } else {
          Log.i("INFO","NO VIP");
        }
      }
    }
  };


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
    String title = simpleDateFormat.format(new Date(System.currentTimeMillis()));
    intro = introEdit.getText().toString();
    if (intro == null || intro.equals("")) {
      intro = "";
    }
    if (recordId == null || recordId.equals("")) {
      recordId = null;
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

  @OnClick(R.id.upload_record_view_buy_vip_button)
  public void buy_vip() {
    if (productActivity == null) {
      productActivity = new ProductActivity(UploadRecordActivity.this);
    }
    productActivity.show();
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
    RecordBeanDao recordBeanDao = DaoManager.getInstance().getDaoSession().getRecordBeanDao();

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

  private final BaseInterface saveZzIf = (boolean success, BaseMode result, String message, String err) -> {
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

    if (hour > 0) {
      mp1.setNextMediaPlayer(hourMp);
      hourMp.setNextMediaPlayer(hourMp1);
      if (minute > 0) {
        hourMp1.setNextMediaPlayer(minuteMp);
        minuteMp.setNextMediaPlayer(minuteMp1);
        if (second > 0) {
          minuteMp1.setNextMediaPlayer(secondMp);
          secondMp.setNextMediaPlayer(secondMp1);
          secondMp1.setOnCompletionListener(mp -> {
            mp1.release();
            hourMp.release();
            hourMp1.release();
            minuteMp.release();
            minuteMp1.release();
            secondMp.release();
            secondMp1.release();
          });
        } else {
          minuteMp1.setOnCompletionListener(mp -> {
            mp1.release();
            hourMp.release();
            hourMp1.release();
            minuteMp.release();
            minuteMp1.release();
            secondMp.release();
            secondMp1.release();
          });
        }
      } else {
        if (second > 0) {
          hourMp1.setNextMediaPlayer(secondMp);
          secondMp.setNextMediaPlayer(secondMp1);
          secondMp1.setOnCompletionListener(mp -> {
            mp1.release();
            hourMp.release();
            hourMp1.release();
            minuteMp.release();
            minuteMp1.release();
            secondMp.release();
            secondMp1.release();
          });
        } else {
          hourMp1.setOnCompletionListener(mp -> {
            mp1.release();
            hourMp.release();
            hourMp1.release();
            minuteMp.release();
            minuteMp1.release();
            secondMp.release();
            secondMp1.release();
          });
        }
      }
    } else {
      if (minute > 0) {
        mp1.setNextMediaPlayer(minuteMp);
        minuteMp.setNextMediaPlayer(minuteMp1);
        if (second > 0) {
          minuteMp1.setNextMediaPlayer(secondMp);
          secondMp.setNextMediaPlayer(secondMp1);
          secondMp1.setOnCompletionListener(mp -> {
            mp1.release();
            hourMp.release();
            hourMp1.release();
            minuteMp.release();
            minuteMp1.release();
            secondMp.release();
            secondMp1.release();
          });
        } else {
          minuteMp1.setOnCompletionListener(mp -> {
            mp1.release();
            hourMp.release();
            hourMp1.release();
            minuteMp.release();
            minuteMp1.release();
            secondMp.release();
            secondMp1.release();
          });
        }
      } else {
        mp1.setNextMediaPlayer(secondMp);
        secondMp.setNextMediaPlayer(secondMp1);
        secondMp1.setOnCompletionListener(mp -> {
          mp1.release();
          hourMp.release();
          hourMp1.release();
          minuteMp.release();
          minuteMp1.release();
          secondMp.release();
          secondMp1.release();
        });

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
}

