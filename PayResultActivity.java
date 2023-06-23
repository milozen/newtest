package com.zhanghuang.wxapi;

import android.content.Context;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhanghuang.R;
import com.zhanghuang.dialog.BaseDialog;
import com.zhanghuang.modes.PayResultMode;
import com.zhanghuang.net.RequestData;

public class PayResultActivity extends BaseDialog {
  private static final String TAG = PayResultActivity.class.getSimpleName();
  private RequestData requestData;
  ImageView resultIcon;
  TextView resultTipText;
  TextView moneyText;
  TextView productName;
  TextView tidText;
  TextView timeText;
  LinearLayout contentLayout;
  Button existBtn;

  private final Context baseContent;
  private PayResultMode data;
  private boolean success;


  public PayResultActivity(Context context) {
    super(context);
    baseContent = context;
    requestData = new RequestData(getContext());
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.wx_pay_result);
    contentLayout = findViewById(R.id.pay_result_content_view);
    resultIcon = findViewById(R.id.pay_result_icon);
    resultTipText = findViewById(R.id.pay_result_tip);
    moneyText = findViewById(R.id.pay_result_money);
    productName = findViewById(R.id.pay_result_name);
    tidText = findViewById(R.id.pay_result_tid);
    timeText = findViewById(R.id.pay_result_time);
    existBtn = findViewById(R.id.pay_result_exit_text);

    existBtn.setOnClickListener((View view)-> {
      this.hide();
    });
  }

  @Override
  public void show() {
    super.show();
    contentLayout.setVisibility(View.GONE);
    showResult(this.success, this.data);
  }

  public void updateData(boolean success, PayResultMode data) {
    this.success = success;
    this.data = data;
  }


  public void showResult(boolean success, PayResultMode data) {
    if (success) {
      Log.i(TAG, "pay success");
      resultIcon.setImageResource(R.drawable.result_success);
      resultTipText.setText("支付成功");
      resultTipText.setTextColor(ContextCompat.getColor(baseContent, R.color.result_success));
    } else {
      resultIcon.setImageResource(R.drawable.result_error);
      resultTipText.setText("支付失败");
      resultTipText.setTextColor(ContextCompat.getColor(baseContent, R.color.result_error));
    }
    if (data != null) {
      contentLayout.setVisibility(View.VISIBLE);
      moneyText.setText(data.priceShow());
      productName.setText(data.getProduct_name());
      tidText.setText(data.getTid());
      timeText.setText(data.getTrade_time());
    } else {
      contentLayout.setVisibility(View.GONE);
    }
  }
}
