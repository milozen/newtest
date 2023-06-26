package com.zhanghuang.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.comm.util.AdError;
import com.zhanghuang.util.Constants;

public class BannerAdFragment extends Fragment implements UnifiedBannerADListener {
    private UnifiedBannerView mBannerView;
    private ViewGroup mBannerContainer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 创建UnifiedBannerView
        mBannerView = new UnifiedBannerView(getActivity(), Constants.TAD_BANNER_3, this);
        // 加载广告
        mBannerView.loadAD();
        // 返回UnifiedBannerView
        return mBannerView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 销毁UnifiedBannerView
        if (mBannerView != null) {
            mBannerView.destroy();
        }
    }

    @Override
    public void onNoAD(AdError adError) {
        // 广告加载失败的回调
    }

    @Override
    public void onADReceive() {
        // 广告加载成功的回调
        ViewGroup.LayoutParams params = mBannerView.getLayoutParams();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mBannerView.setLayoutParams(params);
    }

    @Override
    public void onADExposure() {
        // 广告曝光的回调
    }

    @Override
    public void onADClosed() {
        // 广告关闭的回调
    }

    @Override
    public void onADClicked() {
        // 广告被点击的回调
    }

    @Override
    public void onADLeftApplication() {
        // 广告离开应用的回调
    }
//    @Override
//    public void onADOpenOverlay() {
//        // 广告打开新窗口的回调
//    }

//    @Override
//    public void onADCloseOverlay() {
//        // 广告关闭新窗口的回调
//    }
}
