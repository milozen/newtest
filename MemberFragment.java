package com.zhanghuang.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.zhanghuang.BindShowActivity;
import com.zhanghuang.FavActivity;
import com.zhanghuang.IntroAppActivity;
import com.zhanghuang.Login;
import com.zhanghuang.MainApplication;
import com.zhanghuang.NSplashActivity;
import com.zhanghuang.ProductActivity;
import com.zhanghuang.R;
import com.zhanghuang.UserInfoActivity;
import com.zhanghuang.base.BaseMainFragment;
import com.zhanghuang.events.UpdateUserEvent;
import com.zhanghuang.modes.BaseMode;
import com.zhanghuang.modes.User;
import com.zhanghuang.net.RequestData;
import com.zhanghuang.netinterface.BaseInterface;
import com.zhanghuang.util.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by yuanlei on 2017/3/14.
 * 会员中心页面
 */

public class MemberFragment extends BaseMainFragment {

    @BindView(R.id.member_view_avatar)
    SimpleDraweeView avatarImage;
    @BindView(R.id.member_view_level_text)
    TextView levelText;
    @BindView(R.id.member_view_exit_text)
    TextView exitText;

    @BindView(R.id.user_nick)
    TextView tvUserNick;
    @BindView(R.id.member_view_fav_container)
    TextView memberViewFavContainer;
    @BindView(R.id.member_view_bind_container)
    TextView memberViewBindContainer;
    @BindView(R.id.member_view_clear_cache_container)
    TextView memberViewClearCacheContainer;
    @BindView(R.id.member_view_intro_app_container)
    TextView memberViewIntroAppContainer;

    @BindView(R.id.vip_info_title)
    TextView vipInfoTitle;

    @BindView(R.id.vip_info_text)
    TextView vipInfoText;

    private static final String TAG = "MemberFragment";

    private ProductActivity productActivity;


    private RequestData rd;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rd = new RequestData(getContext());
    }

    @Override
    protected void init(View view, LayoutInflater inflater, Bundle savedInstanceState) {

    }

    @Override
    public int getLayoutId() {
        return R.layout.member_view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
    public void onResume() {
        super.onResume();
        if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
            rd.getUserInfo(getUserInfoIf);
            avatarImage.setImageURI(MainApplication._pref.getString(Constants.PREF_USER_AVATAR, ""));
            levelText.setText(MainApplication._pref.getString(Constants.PREF_USER_LEVEL, "初入桩"));
            setDrawableTop(R.mipmap.icon_login_collect, memberViewFavContainer);
            setDrawableTop(R.mipmap.icon_login_bind, memberViewBindContainer);
            setDrawableTop(R.mipmap.icon_login_clear_cache, memberViewClearCacheContainer);
            setDrawableTop(R.mipmap.icon_login_app_recommend, memberViewIntroAppContainer);
            exitText.setText("退出登录");
        } else {
            //未登录
            setDrawableTop(R.mipmap.icon_unlogin_collect, memberViewFavContainer);
            setDrawableTop(R.mipmap.icon_unlogin_bind, memberViewBindContainer);
            setDrawableTop(R.mipmap.icon_unlogin_clear_cache, memberViewClearCacheContainer);
            setDrawableTop(R.mipmap.icon_unlogin_app_recommend, memberViewIntroAppContainer);
            exitText.setText("登录");
        }
    }

    @Override
    public String getPageName() {
        return "更多";
    }

    @OnClick({R.id.member_view_avatar, R.id.member_header})
    public void toEditInfo() {
        if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
            Intent in = new Intent(getContext(), UserInfoActivity.class);
            startActivityForResult(in, 0);
        } else {
            Intent in = new Intent(getContext(), Login.class);
            startActivity(in);
        }
    }

    @OnClick(R.id.vip_info_container)
    public void showProductList() {
        if (productActivity == null) {
            productActivity = new ProductActivity(getContext());
        }
        productActivity.show();
    }




    @OnClick(R.id.member_view_fav_container)
    public void toFavView() {
        if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
            Intent in = new Intent(getContext(), FavActivity.class);
            startActivity(in);
        } else {
            Intent in = new Intent(getContext(), Login.class);
            startActivity(in);
        }
    }

    @OnClick(R.id.member_view_bind_container)
    public void toBindView() {
        if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
            Intent in = new Intent(getContext(), BindShowActivity.class);
            startActivity(in);
        } else {
            Intent in = new Intent(getContext(), Login.class);
            startActivity(in);
        }
    }

    @OnClick(R.id.member_view_clear_cache_container)
    public void clearCache() {
        // 创建AlertDialog.Builder对象
        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());

        // 设置弹窗标题和内容
        adb.setTitle("确定注销账号吗？");
        adb.setMessage("按照有关法律条文规定，您的账号资料将被保留六个月，六个月内登录账号即可恢复登录；六个月后，您的资料将被永久删除！");

        // 允许用户通过点击弹窗外部取消弹窗
        adb.setCancelable(true);

        // 设置"取消"按钮，点击后不进行任何操作
        adb.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击"取消"按钮后不进行任何操作
            }
        });

        // 设置"确定"按钮，点击后注销账号并退出登录
        adb.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 用户点击"确定"按钮后，注销账号并退出登录
                MainApplication._pref.edit()
                        .putBoolean(Constants.PREF_ISLOGIN, false)
                        .apply();

                // 清空用户信息
                avatarImage.setImageURI("");
                levelText.setText("");
                exitText.setText("登录");
                tvUserNick.setText("");

                // 更新图标
                setDrawableTop(R.mipmap.icon_unlogin_collect, memberViewFavContainer);
                setDrawableTop(R.mipmap.icon_unlogin_bind, memberViewBindContainer);
                setDrawableTop(R.mipmap.icon_unlogin_clear_cache, memberViewClearCacheContainer);
                setDrawableTop(R.mipmap.icon_unlogin_app_recommend, memberViewIntroAppContainer);

                //需要结束当前
                //finish();
                // 返回登录界面
                Intent intent = new Intent(getActivity(), com.zhanghuang.Login.class);
                getActivity().startActivity(intent);

            }
        });

        // 显示弹窗
        adb.show();
    }

//    private void finish() {
//    }


    @OnClick(R.id.member_view_intro_app_container)
    public void toIntroApp() {
        Intent in = new Intent(getContext(), IntroAppActivity.class);
        startActivity(in);
    }

    //@OnClick(R.id.privacy_policy_button)

    @OnClick(R.id.member_view_exit_text)
    public void exit() {
        if (MainApplication._pref.getBoolean(Constants.PREF_ISLOGIN, false)) {
            AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
            adb.setTitle("确定退出");
            adb.setCancelable(true);
            adb.setNeutralButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            adb.setNegativeButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    MainApplication._pref.edit()
                            .putBoolean(Constants.PREF_ISLOGIN, false)
                            .apply();
                    avatarImage.setImageURI("");
                    levelText.setText("");
                    exitText.setText("登录");
                    tvUserNick.setText("");
                    setDrawableTop(R.mipmap.icon_unlogin_collect, memberViewFavContainer);
                    setDrawableTop(R.mipmap.icon_unlogin_bind, memberViewBindContainer);
                    setDrawableTop(R.mipmap.icon_unlogin_clear_cache, memberViewClearCacheContainer);
                    setDrawableTop(R.mipmap.icon_unlogin_app_recommend, memberViewIntroAppContainer);
                    // 返回登录界面
                    Intent intent = new Intent(getActivity(), com.zhanghuang.Login.class);
                    getActivity().startActivity(intent);
                }
            });
            adb.show();
        } else {
            Intent in = new Intent(getContext(), Login.class);
            startActivity(in);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private final BaseInterface getUserInfoIf = new BaseInterface() {
        @Override
        public void response(boolean success, BaseMode result, String message, String err) {
            if (success) {
                User user = (User) result;
                avatarImage.setImageURI(user.getAvatar());
                levelText.setText(user.getName());
                tvUserNick.setText(user.getNick());

                if (MainApplication.isVip()) {
                    vipInfoTitle.setText(String.format("VIP过期时间: %s",
                        MainApplication._pref.getString(Constants.PREF_ZZ_VIP_EXP_STR, "-")));
                    vipInfoText.setText("续费");
                } else {
                    vipInfoTitle.setText(R.string.txt_no_join_vip);
                    vipInfoText.setText(R.string.buy_vip);
                }
            }
        }
    };

    private void setDrawableTop(int resId, TextView view) {
        Drawable drawable = ContextCompat.getDrawable(getContext(), resId);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        view.setCompoundDrawables(null, drawable, null, null);
    }

    @Subscribe
    public void onUpdateUser(UpdateUserEvent e) {
        Log.i(TAG, "receive UpdateUserEvent");
        rd.getUserInfo(getUserInfoIf);
    }

}
