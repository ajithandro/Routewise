package com.tnedicca.routewise.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.views.CustomButton;
import com.tnedicca.routewise.views.CustomTextView;
import com.tnedicca.routewise.views.CustomWebView;

/**
 * Created by Vishal on 13-01-2017.
 */

public class Terms extends AppCompatActivity implements View.OnClickListener {

    private CustomTextView screenTitle;
    private ImageView backIcon;
    private ImageView infoIcon;

    private RouteWise mInstance;
    private CustomWebView drivingDataConditionText;
    private CustomWebView sharingDataConditionText;
    private CustomWebView incurredChargesConditionText;
    private CustomWebView liabilityConditionText;
    private CustomButton declineButton;
    private CustomButton acceptButton;
    private boolean keepMeLoggedIn;
    private CustomWebView termsView;
    private RouteLog logger;
    private ProgressBar mProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.terms);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(Terms.class);

        screenTitle = findViewById(R.id.action_bar_title);
        backIcon = findViewById(R.id.back_icon);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);

        screenTitle.setCustomText(getString(R.string.terms_and_condition));
        infoIcon.setVisibility(View.INVISIBLE);
//        backIcon.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        init();
        super.onResume();
    }

    private void init() {
        Intent intent = getIntent();
        keepMeLoggedIn = intent.getBooleanExtra(AppConstant.KEEP_ME_LOGGED_IN, false);

        termsView = findViewById(R.id.terms_view);
        drivingDataConditionText = findViewById(R.id.drive_view);
        sharingDataConditionText = findViewById(R.id.share_view);
        incurredChargesConditionText = findViewById(R.id.incure_view);
        liabilityConditionText = findViewById(R.id.liab_view);
        mProgress = findViewById(R.id.progress);

        acceptButton = findViewById(R.id.accept);
        declineButton = findViewById(R.id.decline);
        declineButton.setOnClickListener(this);
        acceptButton.setOnClickListener(this);

        termsView.setWebViewClient(new myWebClient());
        termsView.getSettings().setJavaScriptEnabled(true);

        String text = getString(R.string.terms_and_condition_text);
        text = text.replace("®", "<sup><small>®</small></sup>");
        termsView.setText(text);
        text = getString(R.string.driving_data_condition);
        text = text.replace("®", "<sup><small>®</small></sup>");
        drivingDataConditionText.setText(text);
        text = getString(R.string.sharing_data_condition);
        text = text.replace("®", "<sup><small>®</small></sup>");
        sharingDataConditionText.setText(text);
        text = getString(R.string.incurred_charges_condition);
        text = text.replace("®", "<sup><small>®</small></sup>");
        incurredChargesConditionText.setText(text);
        text = getString(R.string.liability_condition);
        text = text.replace("®", "<sup><small>®</small></sup>");
        liabilityConditionText.setText(text);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.decline:
            case R.id.back_layout:
                onBackPressed();
                break;
            case R.id.accept:
                Intent registrationIntent = new Intent(Terms.this, Registration.class);
                registrationIntent.putExtra(AppConstant.KEEP_ME_LOGGED_IN, keepMeLoggedIn);
                startActivity(registrationIntent);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                break;
            default:
                logger.info(getString(R.string.log_terms_and_condition_onclick_default) + " for view id : " + v.getId());
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
