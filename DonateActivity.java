package com.zhanghuang;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.zhanghuang.events.UpdateUserEvent;
import com.zhanghuang.util.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class DonateActivity extends AppCompatActivity {

    private Button btnSupportAuthorMembership;
    private Button btnSupportAuthorAds;
    private Button btnSupportAuthorLater;
    private ProductActivity productActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donate);

        btnSupportAuthorMembership = findViewById(R.id.btn_support_author_membership);
        btnSupportAuthorAds = findViewById(R.id.btn_support_author_ads);
        btnSupportAuthorLater = findViewById(R.id.btn_support_author_later);

        // 在 onCreate 方法中注册 EventBus
        EventBus.getDefault().register(this);

        btnSupportAuthorMembership.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle membership support
                if (productActivity == null) {
                    productActivity = new ProductActivity(DonateActivity.this);
                }
                productActivity.show();
            }
        });

        btnSupportAuthorAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Play the ad
                // After the ad is played, increase the VIP by one day

            }
        });

        btnSupportAuthorLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Handle later support
                Intent in = new Intent(DonateActivity.this, AddRecordActivityNew.class);
                startActivity(in);
                finish();
            }
        });
    }

    // 在 onDestroy 方法中取消注册 EventBus
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    // 添加处理 UpdateUserEvent 的方法
    @Subscribe
    public void onUpdateUserEvent(UpdateUserEvent event) {
        Intent in = new Intent(DonateActivity.this, AddRecordActivityNew.class);
        startActivity(in);
        finish();
    }
}
