package com.zhanghuang;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.zhanghuang.base.BaseActivity;
import com.zhanghuang.modes.BaseMode;
import com.zhanghuang.modes.StringMode;
import com.zhanghuang.net.RequestData;
import com.zhanghuang.netinterface.BaseInterface;
import com.zhanghuang.util.Constants;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by yuanlei on 2017/3/22.
 */

public class Login extends BaseActivity {

    @BindView(R.id.login_user_edit)
    EditText userEdit;
    @BindView(R.id.login_pass_edit)
    EditText passEdit;

    @BindView(R.id.login_checkbox_box)
    CheckBox checkBox;

    private RequestData rd;
    private String phone;
    private String pass;

    private String uid;
    private String type;
    private String avatar;
    private String nick;
    private String sex;
    private boolean alterShowed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private void initData(){
         rd =  new RequestData(this);
    }

    private void initView(){

    }

    @OnClick(R.id.login_login_button)
    public void loginClick(){
        phone = userEdit.getText().toString();
        pass = passEdit.getText().toString();
        if (phone.isEmpty() || phone.length() != 11){
            Toast.makeText(Login.this,"请输入手机号",Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.isEmpty() || pass.equals("") || pass.length() < 6){
            Toast.makeText(Login.this,"请输入密码",Toast.LENGTH_SHORT).show();
            return;
        }
        if (!checkBox.isChecked()) {
            Toast.makeText(Login.this,"请阅读《用户协议》和《隐私政策》后勾选同意",Toast.LENGTH_SHORT).show();
            return;
        }
        rd.login(phone, pass, (success, result, message, err) -> {
            if (success){
                StringMode _data = (StringMode) result;
                SharedPreferences.Editor editor = MainApplication._pref.edit();
                editor.putString(Constants.PREF_TOKEN, _data.getData());
                editor.putBoolean(Constants.PREF_ISLOGIN,true);
                editor.putString(Constants.PREF_MOBILE,phone);
                editor.putString(Constants.PREF_PASS,pass);
                editor.apply();
                finish();
            }else{
                Toast.makeText(Login.this,err,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.login_regist_text)
    public void registClick(){
        Intent in = new Intent(Login.this,RegistActivity.class);
        startActivity(in);
    }

    @OnClick(R.id.login_forget_pass_text)
    public void forgetPass(){
        Intent in = new Intent(Login.this,ForgetPassActivity.class);
        startActivity(in);
    }

    @OnClick(R.id.login_wx_image)
    public void wxClick(){
        UMShareAPI.get(this).getPlatformInfo(Login.this,SHARE_MEDIA.WEIXIN,umAuthListener);
    }

    @OnClick(R.id.login_sina_image)
    public void sinaClick(){
        UMShareAPI.get(this).getPlatformInfo(Login.this,SHARE_MEDIA.SINA,umAuthListener);
    }

    @OnClick(R.id.login_qq_image)
    public void qqClick(){
        UMShareAPI.get(this).getPlatformInfo(Login.this,SHARE_MEDIA.QQ,umAuthListener);
    }

    @OnClick(R.id.login_jump_login_text)
    public void jumpClick(){
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    private UMAuthListener umAuthListener = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {}

        @Override
        public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
            if (share_media == SHARE_MEDIA.WEIXIN){
                type = "weixin";
            }else if(share_media == SHARE_MEDIA.QQ){
                type = "qq";
            }else{
                type = "weibo";
            }
            uid = map.get("uid");
            avatar = map.get("iconurl");
            sex = map.get("gender");
            nick = map.get("name");
            String accessToken = map.get("accessToken");
            rd.checkHasBind(checkBindIf,uid,accessToken,type);
        }

        @Override
        public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
            Toast.makeText(Login.this,throwable.getMessage(),Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media, int i) {}
    };

    @Override
    public String getPageName() {
        return "登录";
    }


    private BaseInterface checkBindIf =new BaseInterface() {
        @Override
        public void response(boolean success, BaseMode result, String message, String err) {
            if (success){
                boolean isBind = result.isStatus();
                if (isBind){
                    String mobile = result.getMessage();
                    String token = result.getErr();
                    SharedPreferences.Editor editor = MainApplication._pref.edit();
                    editor.putString(Constants.PREF_TOKEN,token );
                    editor.putBoolean(Constants.PREF_ISLOGIN,true);
                    editor.putString(Constants.PREF_MOBILE,mobile);
                    editor.apply();
                    finish();
                }else{
                    Intent in = new Intent(Login.this,BindChoiceActivity.class);
                    Bundle b = new Bundle();
                    b.putString("uid",uid);
                    b.putString("type",type);
                    b.putString("avatar",avatar);
                    b.putString("nick",nick);
                    b.putString("sex",sex);
                    in.putExtra("bundle",b);
                    startActivity(in);
                    finish();
                }
            }else{
                Toast.makeText(Login.this,err,Toast.LENGTH_SHORT).show();
            }
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
        //String url = "file:///android_asset/privacy.html";
        String url = "file:///android_asset/inside.html";
        alertWebview(url);
    }
    @OnClick(R.id.login_btn_user)
    public void showAgreement() {
        String url = "file:///android_asset/eula.htm";
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
