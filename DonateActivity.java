package com.zhanghuang;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

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
                // TODO: Handle ads support
                // 播放广告后，增加一天VIP
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
}
