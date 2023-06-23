package com.zhanghuang;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.zhanghuang.adapter.ProductFragAdapter;
import com.zhanghuang.dialog.BaseDialog;
import com.zhanghuang.events.PayResultEvent;
import com.zhanghuang.events.UpdateUserEvent;
import com.zhanghuang.modes.PayResultMode;
import com.zhanghuang.modes.PreOrderMode;
import com.zhanghuang.modes.Product;
import com.zhanghuang.modes.ProductMode;
import com.zhanghuang.net.RequestData;
import com.zhanghuang.util.Constants;
import com.zhanghuang.wxapi.PayResultActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends BaseDialog {
    ListView listview;
    List<Product> list = new ArrayList<>();
    public final static String TAG = "ProductActivity";
    private RequestData requestData;
    private IWXAPI api;
    private final Context baseContent;
    private ProgressDialog progress;
    private String currentTid;
    private PayResultActivity payResultActivity;

    public ProductActivity(Context context) {
        super(context);
        baseContent = context;
    }


    private void initData() {
        requestData.productList((success, result, message, err) -> {
            ProductMode data = (ProductMode) result;
            list = data.getList();
            listview.setAdapter(new ProductFragAdapter(getContext(), list));
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_view);

        requestData = new RequestData(getContext());
        api = WXAPIFactory.createWXAPI(baseContent, Constants.APP_ID, false);

        listview = findViewById(R.id.product_list_view);

        listview.setOnItemClickListener((adapter, view, position, id) -> {
            Log.i(TAG, "onItemClick:: id: " + id);
            Log.i(TAG, "onItemClick:: position: " + position);
            showLoading();
            Product product = list.get(position);
            requestData.preOrder(product.getId(), (success, result, message, err) -> {
                if (success) {
                    PreOrderMode data = (PreOrderMode) result;
                    Log.i(TAG, data.toString());
                    beginPay(data);
                } else {
                    Log.i(TAG, "pre order error: " + err);
                    hideLoading();
                }
            });
        });
        initData();
        EventBus.getDefault().register(this);
    }


    public void beginPay(PreOrderMode data) {
        currentTid = data.getTid();
        PayReq request = new PayReq();
        request.appId = data.getAppId();
        request.partnerId = data.getPartnerId();
        request.prepayId= data.getPrepayId();
        request.packageValue = "Sign=WXPay";
        request.nonceStr= data.getNonceStr();
        request.timeStamp= data.getTimeStamp();
        request.sign= data.getSign();
        api.sendReq(request);
    }

    @Subscribe
    public void onPayResult(PayResultEvent event){
        Log.i(TAG, "pay result: " + event.isSuccess());
        if (event.isSuccess() && currentTid != null) {
            requestData.queryOrder(currentTid, (success, result, message, err) -> {
                hideLoading();
                if (success) {
                    Log.i(TAG, "支付成功");
                    this.hide();
                    showPayResult(success, (PayResultMode)result);
                    UpdateUserEvent e = new UpdateUserEvent();
                    EventBus.getDefault().post(e);
                } else {
                    Log.i(TAG, "支付失败");
                    showPayResult(false, null);
                }
            });
        } else {
            hideLoading();
            showPayResult(false, null);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void showLoading() {
        if (progress == null) {
            progress = new ProgressDialog(baseContent);
        }
        progress.setTitle("支付中");
        progress.setMessage("等待支付完成...");
        progress.setCancelable(false);
        progress.show();
    }

    private void hideLoading() {
        progress.dismiss();
    }

    private void showPayResult(boolean success, PayResultMode data) {
        if (payResultActivity == null) {
            payResultActivity = new PayResultActivity(baseContent);
        }
        if (data != null) {
            Log.i(TAG, data.getProduct_name() +" :: "+ data.getTid());
        }
        payResultActivity.updateData(success, data);
        payResultActivity.show();
    }
}
