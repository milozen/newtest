package com.zhanghuang;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zhanghuang.base.BaseBackActivity;
import com.zhanghuang.modes.StringMode;
import com.zhanghuang.net.RequestData;
import com.zhanghuang.netinterface.BaseInterface;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yuanlei on 2017/3/22.
 */

public class RegistActivity extends BaseBackActivity {
    @BindView(R.id.regist_phone_edit) EditText phoneEdit;
    @BindView(R.id.regist_code_edit) EditText codeEdit;
    @BindView(R.id.regist_pass_edit) EditText passEdit;
    @BindView(R.id.regist_get_code_text) TextView getCodeText;
    @BindView(R.id.regist_confirm_pass_edit) EditText confirmPassEdit;

    @BindView(R.id.login_checkbox_box)
    CheckBox checkBox;

    private RequestData rd;
    private boolean alterShowed;

    private String sCode;
    private int count = 60;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            count--;
            if (count == 0){
                getCodeText.setEnabled(true);
                getCodeText.setText("获取验证码");
                count = 60;
            }else {
                getCodeText.setText("重新获取"+String.valueOf(count));
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void init(Bundle savedInstanceState) {
        initData();
        initView();
    }

    @Override
    public int getLayoutId() {
        return R.layout.regist;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initData(){
        rd = new RequestData(this);
    }

    private void initView(){

    }

    @OnClick(R.id.regist_regist_button)
    public void registClick(){
        String phone = phoneEdit.getText().toString();
        String code = codeEdit.getText().toString();
        String pass = passEdit.getText().toString();
        String confimPass = confirmPassEdit.getText().toString();
        if (phone.isEmpty() || phone.length() != 11){
            Toast.makeText(RegistActivity.this,"请输入手机号",Toast.LENGTH_SHORT).show();
            return;
        }
        if (code.isEmpty() || code.equals("")){
            Toast.makeText(RegistActivity.this,"请输入验证码",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!code.equals(sCode)){
            Toast.makeText(RegistActivity.this,"验证码不正确",Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.isEmpty() || pass.equals("") || pass.length() < 6){
            Toast.makeText(RegistActivity.this,"请输入密码",Toast.LENGTH_SHORT).show();
            return;
        }
        if (confimPass.isEmpty() || confimPass.equals("") || !confimPass.equals(pass)){
            Toast.makeText(RegistActivity.this,"请再次输入密码",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!checkBox.isChecked()) {
            Toast.makeText(RegistActivity.this,"请阅读《用户协议》和《隐私政策》后勾选同意",Toast.LENGTH_SHORT).show();
            return;
        }
        rd.regist(phone,pass,registIf);
    }

    @OnClick(R.id.regist_get_code_text)
    public void codeClick(){
        String phone = phoneEdit.getText().toString();
        if (phone.isEmpty() || phone.length() != 11){
            Toast.makeText(RegistActivity.this,"请输入手机号",Toast.LENGTH_SHORT).show();
            return;
        }
        rd.getCode(phone, ((success, result, message, err) -> {
            if (success){
                StringMode _data = (StringMode) result;
                sCode = _data.getData();
                Toast.makeText(RegistActivity.this,"验证码发送成功!",Toast.LENGTH_SHORT).show();
                getCodeText.setEnabled(false);
                handler.postDelayed(runnable,1000);
            }else{
                Toast.makeText(RegistActivity.this,err,Toast.LENGTH_SHORT).show();
            }
        }));
    }

    @Override
    public String getPageName() {
        return "新用户注册";
    }


    private BaseInterface registIf = (success, result, message, err) -> {
        if (success){
            Toast.makeText(RegistActivity.this,"恭喜您！注册成功!",Toast.LENGTH_SHORT).show();
            
            
            
            finish();
        }else{
            Toast.makeText(RegistActivity.this,err,Toast.LENGTH_SHORT).show();
        }
    };
    // BEGIN OF 隐藏键盘
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，
     * 来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                top = l[1],
                bottom = top + v.getHeight(),
                right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }
    /**
     * 获取InputMethodManager，隐藏软键盘
     */
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
    // END OF 隐藏键盘

    @OnClick(R.id.login_btn_privacy)
    public void showPrivacy() {
        String url = "https://www.zhanzhuang.com.cn/privacy.html";
        alertWebview(url);
    }
    @OnClick(R.id.login_btn_user)
    public void showAgreement() {
        String url = "https://www.zhanzhuang.com.cn/eula.htm";
        alertWebview(url);
    }
    private void alertWebview(String url) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        WebView mwebView = new WebView(this);
        this.alterShowed = false;
        mwebView.loadUrl(url);
        mwebView.setWebViewClient( new WebViewClient() {
            //设置结束加载函数
            @Override
            public void onPageFinished(WebView view, String url) {
                if (!alterShowed) {
                    alterShowed = true;
                    builder.show();
                }

            }
        });
//        builder.setNegativeButton( "取消", null );
        builder.setView( mwebView );
        builder.setPositiveButton("确定", (dialog, which) -> {
            dialog.dismiss();
        });
//        builder.setNegativeButton("拒绝", (dialog, which) -> {
//            dialog.dismiss();
//            android.os.Process.killProcess(android.os.Process.myPid());
//        });
    }
}
