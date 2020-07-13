package com.tnedicca.routewise.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.views.CustomTextView;
import com.tnedicca.routewise.views.CustomWebView;

/**
 * Created by Vishal on 30-01-2017.
 */

public class PrivacyPolicy extends AppCompatActivity implements View.OnClickListener {

    RouteWise mInstance;

    private CustomTextView screenTitle;
    private ImageView backIcon;
    private ImageView infoIcon;

    private CustomWebView privacyPolicyTextView;
    private RouteLog logger;
    private ProgressBar mProgress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.privacy_policy);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(PrivacyPolicy.class);

        screenTitle = findViewById(R.id.action_bar_title);
        backIcon = findViewById(R.id.back_icon);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);

        screenTitle.setCustomText(getString(R.string.privacy_policy));
        infoIcon.setVisibility(View.INVISIBLE);
//        backIcon.setOnClickListener(this);

        mProgress = findViewById(R.id.progress);
        privacyPolicyTextView = findViewById(R.id.privacy_policy_view);
        privacyPolicyTextView.setWebViewClient(new myWebClient());
        privacyPolicyTextView.getSettings().setJavaScriptEnabled(true);
        String text = getString(R.string.privacy_policy_text);
        text = text.replace("®", "<sup><small>®</small></sup>");
        privacyPolicyTextView.setText(text);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
                onBackPressed();
                break;
            default:
                logger.info(getString(R.string.log_terms_and_condition_onclick_default) + " for view id : " + view.getId());
        }
    }

    public class myWebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            mProgress.setVisibility(View.VISIBLE);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mProgress.setVisibility(View.GONE);
        }
    }
}
