package com.tnedicca.routewise.activities;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.fragment.app.FragmentActivity;

import android.view.View;
import android.widget.ImageView;


import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.AppConstant;

/**
 * Created by Aachu on 13-02-2017.
 */
public class DummyActivity extends FragmentActivity {

    private RouteWise mInstance;
    private boolean permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = RouteWise.getInstance();
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extra = intent.getExtras();
            if (extra != null) {
                permission = extra.getBoolean(AppConstant.INTENT_PERMISSION);
            }
        }
        if (savedInstanceState != null) {
            permission = savedInstanceState.getBoolean(AppConstant.INTENT_PERMISSION);
        }
        if (permission) {
            setContentView(R.layout.permission);

            ImageView app_logo = findViewById(R.id.app_logo);
            app_logo.setImageResource(R.mipmap.ic_launcher);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putBoolean(AppConstant.INTENT_PERMISSION, permission);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstant.RESPONSE_6_SPLASH) {
            String[] permissions;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION};
            } else {
                permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
            }
            boolean check = mInstance.myLibrary.checkpermission(this, permissions);
            if (check)
                onBackPressed();
        }
    }

    @Override
    public void onBackPressed() {
        if (permission) {
            start_launcher();
        } else
            super.onBackPressed();
    }

    private void start_launcher() {
        Intent report = new Intent(this, Launcher.class);
        report.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(report);
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    public void allow(View v) {
        if (permission) {
            Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
            myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
            startActivityForResult(myAppSettings, AppConstant.RESPONSE_6_SPLASH);
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        }
    }
}
