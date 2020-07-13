package com.tnedicca.routewise.fragments;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.interfaces.FragmentLifecycle;
import com.tnedicca.routewise.views.CustomTextView;
import com.tnedicca.routewise.views.SeekArc;

public class ScreenSlidePageFragment extends Fragment implements FragmentLifecycle {

    private SeekArc dialProgress;
    private ViewGroup rootView;
    private RouteWise mInstance;

    private int progressValue = 0;
    private ValueAnimator animator;
    private float pagerValue;
    private int pagerNo;
    private CustomTextView score;
    private CustomTextView text;

    private int mScreenDensity;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mSdk;
    private FragmentActivity act;
    private SharedPreferences mPrefer;
    private CustomTextView bottomText;
    private RelativeLayout bottomLayout;
    private RouteLog logger;

    public ScreenSlidePageFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(ScreenSlidePageFragment.class);

        pagerValue = getArguments().getFloat(AppConstant.PAGER_VALUE);
        pagerNo = getArguments().getInt(AppConstant.PAGE_NO);
        rootView = (ViewGroup) inflater.inflate(R.layout.doughnut, container, false);
//        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.doughnut_container);

        act = getActivity();
        mPrefer = act.getSharedPreferences(AppConstant.PREFERENCE, Context.MODE_PRIVATE);

        mSdk = mPrefer.getInt(AppConstant.SDK, 0);
        mScreenDensity = mPrefer.getInt(AppConstant.SCREEN_DENSITY, 0);
        mScreenWidth = mPrefer.getInt(AppConstant.SCREEN_WIDTH, 0);
        mScreenHeight = mPrefer.getInt(AppConstant.SCREEN_HEIGHT, 0);

        dialProgress = rootView.findViewById(R.id.seekArcComplete);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        score = rootView.findViewById(R.id.risk_score);
        text = rootView.findViewById(R.id.risk_text);
        bottomText = rootView.findViewById(R.id.bottom_text);

        String scoreText = "LOW";
        String scoreValue = "";
        String bottomValue = "";
        if (pagerNo == 0) {
            score.autoResize = false;
            progressValue = Math.round(pagerValue * 10);
            if (pagerValue > 6.9) {
                scoreText = "HIGH";
            } else if (pagerValue > 3.9 && pagerValue < 7) {
                scoreText = "MEDIUM";
            }

            float temp = (float) mInstance.myLibrary.round(pagerValue, 1);
            float checkDecimal = temp % 1;
            if (checkDecimal == 0.0) {
                int newValue = (int) temp;
                scoreValue = newValue + "";
            } else
                scoreValue = temp + "";

            bottomValue = "MY ROUTE RISK SCORE";
            score.setCustomText(scoreValue);
//            logger.info("User Risk Level : " + scoreText);
//            logger.info("User RISK Score : " + temp);
        } else if (pagerNo == 1) {
            score.autoResize = true;
            progressValue = 100;
            float miles = (float) (pagerValue * 0.000621371);
            int temp = Math.round(miles);
//            temp = 1000;
            scoreValue = temp + "mi";

            int length = scoreValue.length() - 2;
            SpannableString ss1 = new SpannableString(scoreValue);
            ss1.setSpan(new RelativeSizeSpan(0.3f), length, length + 2, 0); // set size
//            ss1.setSpan(new ForegroundColorSpan(Color.RED), 0, 5, 0);// set color

            bottomValue = "TOTAL MILES DRIVEN";
            score.setCustomText(ss1);
            text.setVisibility(View.INVISIBLE);
//            logger.info("User Travelled Distance : " + temp + " miles");
        }
        text.setCustomText(scoreText);
        bottomText.setCustomText(bottomValue);
    }

    @Override
    public void onResume() {
        onResumeFragment(pagerNo, pagerValue);
        super.onResume();
    }

    private void simulateProgress(int value) {
        animator = ValueAnimator.ofInt(0, value);
//        mInstance.logger.info("value : " + value);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                dialProgress.setProgress(progress);
                dialProgress.invalidate();
            }
        });
        animator.setRepeatCount(0);
        animator.setDuration(2000);
        animator.start();
    }

    @Override
    public void onResumeFragment(int position, float value) {
        int tempValue = 0;
        if (position == 0)
            tempValue = Math.round(value * 10);
        else if (position == 1)
            tempValue = 100;
        simulateProgress(tempValue);
    }
}