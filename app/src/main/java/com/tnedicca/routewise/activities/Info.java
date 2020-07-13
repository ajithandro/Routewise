package com.tnedicca.routewise.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.views.CustomTextView;

/**
 * Created by Aachu on 27-01-2017.
 */
public class Info extends Activity implements View.OnClickListener{

    public SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;

    private CustomTextView screenTitle;
    private ImageView menuIcon;
    private ImageView infoIcon;
    private RouteWise mInstance;
    private RouteLog logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(Info.class);

        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        screenTitle = findViewById(R.id.action_bar_title);
        menuIcon = findViewById(R.id.back_icon);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);

        screenTitle.setCustomText(getString(R.string.info));
//        menuIcon.setOnClickListener(this);
        infoIcon.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        logger.info("Returned to the Dashboard.");
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.back_layout:
                onBackPressed();
                break;
            default:
                logger.info(getString(R.string.onclick_default) + id);
                break;
        }
    }
}
