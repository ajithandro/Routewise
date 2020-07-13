package com.tnedicca.routewise.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.kyleduo.switchbutton.SwitchButton;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.Password;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.views.CustomButton;
import com.tnedicca.routewise.views.CustomEditTextView;
import com.tnedicca.routewise.views.CustomTextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import cn.jeesoft.widget.pickerview.CharacterPickerWindow;
import cn.jeesoft.widget.pickerview.OnOptionChangedListener;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by new on 17-02-2017.
 */

public class Settings extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, OnOptionChangedListener, PopupWindow.OnDismissListener {

    long endTime;
    private RouteWise mInstance;
    private ViewGroup rootView;
    private SwitchButton mKeepMeLoggedInSwitch;
    private SwitchButton mTrackingEnabledSwitch;
    private CustomEditTextView mDaysField;
    private CustomEditTextView mHoursField;
    private CustomEditTextView mMinutesField;
    private CustomTextView mSecsRemainingField;
    private RadioButton mCellularRadio;
    private RadioButton mWifiRadio;
    private RadioButton mBothRadio;
    private RelativeLayout timer;
    private CustomButton mResetPassword;
    private CustomButton mSaveLogs;
    private SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;
    private Context context;
    private CharacterPickerWindow daysPicker;
    private CharacterPickerWindow hoursPicker;
    private CharacterPickerWindow minutesPicker;
    private ArrayList daysList;
    private ArrayList hoursList;
    private ArrayList minutesList;
    private int days;
    private int hours;
    private int minutes;
    private int secs;
    private MyCounter myCounter;
    private boolean isTimerRunning;
    private RouteLog logger;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(Settings.class);
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.settings, container, false);
        mKeepMeLoggedInSwitch = rootView.findViewById(R.id.settings_keep_me_loged_in_switch);
        mTrackingEnabledSwitch = rootView.findViewById(R.id.tracking_enabled_switch);
        mDaysField = rootView.findViewById(R.id.days_field);
        mHoursField = rootView.findViewById(R.id.hours_field);
        mMinutesField = rootView.findViewById(R.id.minutes_field);
        mSecsRemainingField = rootView.findViewById(R.id.within_secs);
        timer = rootView.findViewById(R.id.timer);
        mCellularRadio = rootView.findViewById(R.id.radio_cellular);
        mWifiRadio = rootView.findViewById(R.id.radio_wifi);
        mBothRadio = rootView.findViewById(R.id.radio_both);
        mResetPassword = rootView.findViewById(R.id.reset_password_button);
        mSaveLogs = rootView.findViewById(R.id.save_logs_button);
        mDaysField.setTag(AppConstant.DAYS_TAG);
        mHoursField.setTag(AppConstant.HOURS_TAG);
        mMinutesField.setTag(AppConstant.MINUTES_TAG);
        mResetPassword.setTag(AppConstant.RESET_PASSWORD_TAG);
        mCellularRadio.setTag(AppConstant.CELLULAR_RADIO_TAG);
        mWifiRadio.setTag(AppConstant.WIFI_RADIO_TAG);
        mBothRadio.setTag(AppConstant.BOTH_RADIO_TAG);
        mSaveLogs.setTag(AppConstant.SAVE_LOGS_TAG);
        mKeepMeLoggedInSwitch.setChecked(mPrefer.getBoolean(AppConstant.KEEP_ME_LOGGED_IN, false));
        mKeepMeLoggedInSwitch.setOnCheckedChangeListener(this);
        mTrackingEnabledSwitch.setOnCheckedChangeListener(this);
        mDaysField.setOnClickListener(this);
        mHoursField.setOnClickListener(this);
        mMinutesField.setOnClickListener(this);
        mCellularRadio.setOnClickListener(this);
        mWifiRadio.setOnClickListener(this);
        mBothRadio.setOnClickListener(this);
        mResetPassword.setOnClickListener(this);
        mSaveLogs.setOnClickListener(this);
        daysPicker.setOnoptionsSelectListener(this);
        hoursPicker.setOnoptionsSelectListener(this);
        minutesPicker.setOnoptionsSelectListener(this);
        daysPicker.setOnDismissListener(this);
        hoursPicker.setOnDismissListener(this);
        minutesPicker.setOnDismissListener(this);
        mDaysField.clearFocus();
        String dataUploadType = mPrefer.getString(AppConstant.DATA_UPLOAD_CONDITION, "");
        if (dataUploadType.equals(AppConstant.MOBILE)) {
            mCellularRadio.setChecked(true);
            mInstance.uploadDataTypeSelected = AppConstant.MOBILE;
        } else if (dataUploadType.equals(AppConstant.WIFI)) {
            mWifiRadio.setChecked(true);
            mInstance.uploadDataTypeSelected = AppConstant.WIFI;
        } else {
            mBothRadio.setChecked(true);
            mInstance.uploadDataTypeSelected = AppConstant.BOTH;
        }
        edit = mPrefer.edit();
        edit.putString(AppConstant.DATA_UPLOAD_CONDITION, mInstance.uploadDataTypeSelected);
        edit.commit();
        return rootView;
    }

    private void init() {
        context = getActivity().getApplication();
        mPrefer = context.getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        setTimerLists();
        daysPicker = new CharacterPickerWindow(context, AppConstant.DAYS_PICKER_TAG);
        hoursPicker = new CharacterPickerWindow(context, AppConstant.HOURS_PICKER_TAG);
        minutesPicker = new CharacterPickerWindow(context, AppConstant.MINUTES_PICKER_TAG);
        daysPicker.setPicker(daysList);
        hoursPicker.setPicker(hoursList);
        hoursPicker.setSelectOptions(23);
        minutesPicker.setPicker(minutesList);
        minutesPicker.setSelectOptions(1);
        days = 0;
        hours = 0;
        minutes = 0;
    }

    private void checkRadioButtons(boolean cellular, boolean wifi, boolean both) {
        mCellularRadio.setChecked(cellular);
        mWifiRadio.setChecked(wifi);
        mBothRadio.setChecked(both);
        if (mCellularRadio.isChecked()) {
            mInstance.uploadDataTypeSelected = AppConstant.MOBILE;
        } else if (mWifiRadio.isChecked()) {
            mInstance.uploadDataTypeSelected = AppConstant.WIFI;
        } else {
            mInstance.uploadDataTypeSelected = AppConstant.BOTH;
        }
        edit = mPrefer.edit();
        edit.putString(AppConstant.DATA_UPLOAD_CONDITION, mInstance.uploadDataTypeSelected);
        edit.commit();
    }


    private void onKeepMeLoggedInCheckedChange(boolean isChecked) {
        logger.info(getString(R.string.log_keep_me_logged_in_button_checked) + " " + isChecked);
        edit = mPrefer.edit();
        edit.putBoolean(AppConstant.KEEP_ME_LOGGED_IN, isChecked);
        edit.commit();
    }

    private void onTrackingEnabledCheckedChange(boolean isChecked) {
        mInstance.myLibrary.setViewAndChildrenEnabled(timer, !isChecked);
        if (myCounter != null) {
            myCounter.cancel();
        }
        if (!isChecked) {
            if (mInstance.tracking_enabled) {
                mInstance.tracking_enabled = false;
                mInstance.auto_enable = !mInstance.tracking_enabled;
            }
            mHoursField.setText("2");
            mMinutesField.setText("0");
            hoursPicker.setSelectOptions(22);
            minutesPicker.setSelectOptions(0);
            days = 0;
            hours = 2;
            minutes = 0;
            long startTime = Calendar.getInstance().getTimeInMillis();
            endTime = startTime + (days * 24 * 60 * 60 + hours * 60 * 60 + minutes * 60) * 1000;
            edit = mPrefer.edit();
            edit.putLong(AppConstant.AUTO_ENABLE_TIME_START, startTime);
            edit.putLong(AppConstant.AUTO_ENABLE_TIME, endTime);
            edit.putBoolean(AppConstant.TRACKING_ENABLED, mInstance.tracking_enabled);
            edit.putBoolean(AppConstant.AUTO_TRACKING, mInstance.auto_enable);
            edit.commit();

            mDaysField.clearFocus();
            myCounter = new MyCounter(endTime - Calendar.getInstance().getTimeInMillis(), 1000);
            myCounter.start();
            isTimerRunning = true;
        } else {
            if (!mInstance.tracking_enabled) {
                mInstance.tracking_enabled = true;
                mInstance.auto_enable = !mInstance.tracking_enabled;
            }
            mDaysField.setText("0");
            mHoursField.setText("2");
            mMinutesField.setText("0");
            mSecsRemainingField.setText("0");
            edit = mPrefer.edit();
            edit.remove(AppConstant.AUTO_ENABLE_TIME_START);
            edit.remove(AppConstant.AUTO_ENABLE_TIME);
            edit.putBoolean(AppConstant.TRACKING_ENABLED, mInstance.tracking_enabled);
            edit.putBoolean(AppConstant.AUTO_TRACKING, mInstance.auto_enable);
            edit.commit();
            days = 0;
            hours = 2;
            minutes = 0;
        }
    }

    private void setTimerLists() {
        daysList = new ArrayList();
        hoursList = new ArrayList();
        minutesList = new ArrayList();
        for (int i = 30; i >= 0; i--) {
            daysList.add(Integer.toString(i));
        }
        for (int i = 23; i >= 0; i--) {
            hoursList.add(Integer.toString(i));
        }
        for (int i = 59; i >= 0; i--) {
            minutesList.add(Integer.toString(i));
        }
    }

    private void initTimer() {
        endTime = mPrefer.getLong(AppConstant.AUTO_ENABLE_TIME, 0);
        long currentTime = Calendar.getInstance().getTimeInMillis();
        mTrackingEnabledSwitch.setChecked(mInstance.tracking_enabled);
        if (!mInstance.tracking_enabled) {
            if (endTime < currentTime) {
                isTimerRunning = false;
                mTrackingEnabledSwitch.setChecked(true);
            } else {
                calculateTime(endTime - currentTime);
                isTimerRunning = true;
                setTimerOnResume(false);
            }
        }
    }

    private void setTimerOnResume(boolean isEnabled) {
        mInstance.myLibrary.setViewAndChildrenEnabled(timer, !isEnabled);
        myCounter = new MyCounter(endTime - Calendar.getInstance().getTimeInMillis(), 1000);
        myCounter.start();
        isTimerRunning = true;
    }

    private void calculateTime(long timeInMillis) {
        long timeInSec = timeInMillis / 1000;
        days = (int) timeInSec / (60 * 60 * 24);
        timeInSec = timeInSec % (60 * 60 * 24);
        hours = (int) (timeInSec / (60 * 60));
        timeInSec = timeInSec % (60 * 60);
        minutes = (int) (timeInSec / 60);
        secs = (int) (timeInSec % 60);
        mDaysField.setText("" + days);
        mHoursField.setText("" + hours);
        mMinutesField.setText("" + minutes);
        mSecsRemainingField.setText("" + secs);
        daysPicker.setSelectOptions(30 - days);
        hoursPicker.setSelectOptions(23 - hours);
        minutesPicker.setSelectOptions(59 - minutes);
    }

    private void showResetPassword() {
        Intent intent = new Intent(context, Password.class);
        intent.putExtra(AppConstant.USER, mPrefer.getString(AppConstant.USER, ""));
        intent.putExtra(AppConstant.KEEP_ME_LOGGED_IN, mPrefer.getBoolean(AppConstant.KEEP_ME_LOGGED_IN, false));
        intent.putExtra(AppConstant.RESET_SETTINGS, true);
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }

    @Override
    public void onClick(View v) {
        mInstance.myLibrary.hideKeyBoard(this.getActivity().getCurrentFocus(), context);
        Object obj = v.getTag();
        int tag = (int) obj;
        switch (tag) {
            case AppConstant.CELLULAR_RADIO_TAG:
                checkRadioButtons(true, false, false);
                break;
            case AppConstant.WIFI_RADIO_TAG:
                checkRadioButtons(false, true, false);
                break;
            case AppConstant.BOTH_RADIO_TAG:
                checkRadioButtons(false, false, true);
                break;
            case AppConstant.DAYS_TAG:
                if (myCounter != null) {
                    myCounter.cancel();
                    mSecsRemainingField.setText("0");
                }
                daysPicker.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case AppConstant.HOURS_TAG:
                if (myCounter != null) {
                    myCounter.cancel();
                    mSecsRemainingField.setText("0");
                }
                hoursPicker.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case AppConstant.MINUTES_TAG:
                if (myCounter != null) {
                    myCounter.cancel();
                    mSecsRemainingField.setText("0");
                }
                minutesPicker.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            case AppConstant.RESET_PASSWORD_TAG:
                showResetPassword();
                break;
            case AppConstant.SAVE_LOGS_TAG:
                checkPermission();
                break;
            default:
                logger.info("default statement executed for onClick view tag : " + tag);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        switch (id) {
            case R.id.settings_keep_me_loged_in_switch:
                onKeepMeLoggedInCheckedChange(isChecked);
                break;
            case R.id.tracking_enabled_switch:
                onTrackingEnabledCheckedChange(isChecked);
                break;
            default:
                logger.info("default statement executed for onCheckedChanged for buttonView id : " + id);
                break;
        }
    }

    @Override
    public void onOptionChanged(int tag, int option1, int option2, int option3) {
        switch (tag) {
            case AppConstant.DAYS_PICKER_TAG:
                String day = daysList.get(option1).toString();
                mDaysField.setText(day);
                daysPicker.setSelectOptions(option1);
                days = Integer.parseInt(day);
                break;
            case AppConstant.HOURS_PICKER_TAG:
                String hour = hoursList.get(option1).toString();
                mHoursField.setText(hour);
                hoursPicker.setSelectOptions(option1);
                hours = Integer.parseInt(hour);
                break;
            case AppConstant.MINUTES_PICKER_TAG:
                String minute = minutesList.get(option1).toString();
                mMinutesField.setText(minute);
                minutesPicker.setSelectOptions(option1);
                minutes = Integer.parseInt(minute);
                break;
            default:
                logger.info("default statement executed for onOptionChanged for picker tag : " + tag);
                break;
        }
    }

    @Override
    public void onDismiss() {
        long startTime = Calendar.getInstance().getTimeInMillis();
        long day = days * 24 * 60 * 60;
        long hour = hours * 60 * 60;
        long minute = minutes * 60;
        long total = (day + hour + minute) * 1000;
        endTime = startTime + total;
        myCounter = new MyCounter(endTime - Calendar.getInstance().getTimeInMillis(), 1000);
        myCounter.start();
        isTimerRunning = true;
        edit = mPrefer.edit();
        edit.putLong(AppConstant.AUTO_ENABLE_TIME_START, startTime);
        edit.putLong(AppConstant.AUTO_ENABLE_TIME, endTime);
        edit.commit();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isTimerRunning) {
            if (myCounter != null) {
                myCounter.cancel();
            }
            mInstance.setAlarm(context, endTime, AppConstant.AUTO_TRACK, AppConstant.AUTO_TRACK_CODE);
        } else {
            edit = mPrefer.edit();
            edit.remove(AppConstant.AUTO_ENABLE_TIME_START);
            edit.remove(AppConstant.AUTO_ENABLE_TIME);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initTimer();
        mInstance.removeAlarm(context, AppConstant.AUTO_TRACK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppConstant.RESPONSE_6_STORAGE) {
            ArrayList<Integer> show = new ArrayList();
            for (int i = 0; i < permissions.length; i++) {
                String temp = permissions[i];
                if (temp.equals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    show.add(i);
            }
            if (show.size() > 0) {
                if (grantResults[show.get(0)] == PackageManager.PERMISSION_GRANTED) {
                    saveLogs();
                } else {
                    mInstance.myLibrary.DisplayToast(context, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
                }
            } else if (show.size() == 0)
                saveLogs();
        }
    }

    private void checkPermission() {
        if (mInstance.myLibrary.check6Compact(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, AppConstant.RESPONSE_6_STORAGE, R.string.permission_storage)) {
            saveLogs();
        }
    }

    private void saveLogs() {
        File f1 = new File(AppConstant.KEY_LOG_FILE_PATH);
        String storageDir1 = AppConstant.KEY_LOG_PATH;
        File target1 = new File(Environment.getExternalStorageDirectory(), storageDir1);
        File f2 = new File(AppConstant.KEY_FULL_DB_STORAGE_PATH);
        String storageDir2 = AppConstant.KEY_DB_PATH;
        File target2 = new File(Environment.getExternalStorageDirectory(), storageDir2);
        if (!target1.exists())
            target1.mkdirs();
        if (!target2.exists())
            target2.mkdirs();
        try {
            mInstance.myLibrary.copyDirectory(f1, target1);
            mInstance.myLibrary.copyDirectory(f2, target2);
            mInstance.myLibrary.DisplayToast(context, "Find log file @ SD Card" + AppConstant.KEY_TARGET_DIR, Toast.LENGTH_SHORT, Gravity.BOTTOM);
        } catch (Exception e) {
            mInstance.myLibrary.DisplayToast(context, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        }
    }

    public class MyCounter extends CountDownTimer {

        public MyCounter(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            mTrackingEnabledSwitch.setChecked(true);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            calculateTime(endTime - Calendar.getInstance().getTimeInMillis());
        }
    }
}
