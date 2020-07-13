package com.tnedicca.routewise.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.Library;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.views.CustomButton;
import com.tnedicca.routewise.views.CustomEditTextView;
import com.tnedicca.routewise.views.CustomTextView;
import com.tnedicca.routewise.fragments.FragmentView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import cn.jeesoft.widget.pickerview.CharacterPickerWindow;
import cn.jeesoft.widget.pickerview.OnOptionChangedListener;


/**
 * Created by Vishal on 05-01-2017.
 */

public class Registration extends AppCompatActivity implements View.OnFocusChangeListener, AdapterView.OnItemClickListener, TextWatcher, View.OnClickListener, OnOptionChangedListener {

    private CustomTextView screenTitle;
    private ImageView backIcon;
    private ImageView infoIcon;

    private RouteWise mInstance;
    private FragmentView mEmailAddressView;
    private FragmentView mPasswordView;
    private FragmentView mConfirmPasswordView;
    private FragmentView mFullNameView;
    private FragmentView mPhoneNumberView;
    private FragmentView mStreetAddressView;
    private FragmentView mVehicleMakeView;
    private FragmentView mVehicleModelView;
    private FragmentView mVehicleYearView;
    private CustomTextView mEmailErrorMsg;
    private CustomEditTextView mEmailField;
    private ImageView mEmailValidationImage;
    private CustomTextView mPasswordErrorMsg;
    private CustomEditTextView mPasswordField;
    private ImageView mPasswordValidationImage;
    private CustomTextView mConfirmPasswordErrorMsg;
    private CustomEditTextView mConfirmPasswordField;
    private ImageView mConfirmPasswordValidationImage;
    private CustomTextView mFullNameErrorMsg;
    private CustomEditTextView mFullNameField;
    private ImageView mFullNameValidationImage;
    private CustomTextView mPhoneNumberErrorMsg;
    private CustomEditTextView mPhoneNumberField;
    private ImageView mPhoneNumberValidationImage;
    private CustomTextView mStreetAddressErrorMsg;
    private CustomEditTextView mStreetAddressField;
    private ImageView mStreetAddressValidationImage;
    private CustomTextView mVehicleMakeErrorMsg;
    private CustomEditTextView mVehicleMakeField;
    private ImageView mVehicleMakeValidationImage;
    private CustomTextView mVehicleModelErrorMsg;
    private CustomEditTextView mVehicleModelField;
    private ImageView mVehicleModelValidationImage;
    private CustomTextView mVehicleYearErrorMsg;
    private CustomEditTextView mVehicleYearField;
    private ImageView mVehicleYearValidationImage;
    private String mEmail;
    private String mPassword;
    private String mConfirmPassword;
    private String mFullName;
    private String mPhoneNumber;
    private String mStreetAddress;
    private String mVehicleMake;
    private String mVehicleModel;
    private String mVehicleYear;
    private boolean isStreetSelected;
    private boolean isVehicleMakeSelected;
    private boolean isVehicleModelSelected;
    private boolean isVehicleYearSelected;
    ArrayList<String> volleyArray = new ArrayList<String>();
    private int lineNumber;
    private ArrayList vehicleMakeArrayList;
    private ArrayList vehicleModelArrayList;
    private ArrayList refinedVehicleModelArrayList;
    private HashMap<Integer, String> vehicleMakeHashMap;
    private HashMap<String, Integer> vehicleModelHashMap;
    private boolean areAllFieldsValid;
    private CustomButton mNextButton;
    private ProgressDialog progressDialog;
    private CharacterPickerWindow vehicleMakePicker;
    private CharacterPickerWindow vehicleModelPicker;
    private CharacterPickerWindow vehicleYearPicker;
    private ArrayList yearList;
    private String token;
    private String activationCode;
    private SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;
    private ScrollView scrollView;
    private boolean keepMeLoggedIn;
    private int vehicleMakeIndex = -1;
    private RouteLog logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(Registration.class);

        screenTitle = findViewById(R.id.action_bar_title);
        backIcon = findViewById(R.id.back_icon);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);
        menuLayout.setTag(AppConstant.BACK_ICON_TAG_REGISTRATION);

        screenTitle.setCustomText(getString(R.string.registration));
        infoIcon.setVisibility(View.INVISIBLE);
//        backIcon.setOnClickListener(this);

        vehicleMakeHashMap = new HashMap<>();
        progressDialog = new ProgressDialog(Registration.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        getVehicleDetailsList(AppConstant.QUERY_VEHICLE_MAKE);
        getVehicleDetailsList(AppConstant.QUERY_VEHICLE_MODEL);
        init();
    }

    // initialising all the variables and resources
    private void init() {
        isStreetSelected = false;
        isVehicleMakeSelected = false;
        isVehicleModelSelected = false;
        isVehicleYearSelected = false;
        areAllFieldsValid = false;
        keepMeLoggedIn = getIntent().getBooleanExtra(AppConstant.KEEP_ME_LOGGED_IN, false);
        vehicleMakePicker = new CharacterPickerWindow(this, AppConstant.VEHICLE_MAKE_PICKER_TAG);
        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        scrollView = findViewById(R.id.scrollView);
        vehicleModelPicker = new CharacterPickerWindow(this, AppConstant.VEHICLE_MODEL_TAG);
        setYearAdapter();
        vehicleYearPicker = new CharacterPickerWindow(this, AppConstant.VEHICLE_YEAR_TAG);
        vehicleYearPicker.setPicker(yearList);
        vehicleMakePicker.setTag(AppConstant.VEHICLE_MAKE_PICKER_TAG);
        vehicleModelPicker.setTag(AppConstant.VEHICLE_MODEL_PICKER_TAG);
        vehicleYearPicker.setTag(AppConstant.VEHICLE_YEAR_PICKER_TAG);
        vehicleMakePicker.setOnoptionsSelectListener(this);
        vehicleModelPicker.setOnoptionsSelectListener(this);
        vehicleYearPicker.setOnoptionsSelectListener(this);

        mEmailAddressView = (FragmentView) getFragmentManager().findFragmentById(R.id.email_address_view);
        mEmailField = mEmailAddressView.getView().findViewById(R.id.edit_text);
        mEmailErrorMsg = mEmailAddressView.getView().findViewById(R.id.error_msg);
        mEmailValidationImage = mEmailAddressView.getView().findViewById(R.id.validation_image);
        mEmailField.setHint(R.string.email);
        mEmailField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mEmailField.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        mPasswordView = (FragmentView) getFragmentManager().findFragmentById(R.id.create_password_view);
        mPasswordField = mPasswordView.getView().findViewById(R.id.edit_text);
        mPasswordErrorMsg = mPasswordView.getView().findViewById(R.id.error_msg);
        mPasswordValidationImage = mPasswordView.getView().findViewById(R.id.validation_image);
        mPasswordField.setHint(R.string.create_password);
        mPasswordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPasswordField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mPasswordField.setCustomFont(Registration.this, getString(R.string.font_malgun_semi_light));

        mConfirmPasswordView = (FragmentView) getFragmentManager().findFragmentById(R.id.confirm_password_view);
        mConfirmPasswordField = mConfirmPasswordView.getView().findViewById(R.id.edit_text);
        mConfirmPasswordErrorMsg = mConfirmPasswordView.getView().findViewById(R.id.error_msg);
        mConfirmPasswordValidationImage = mConfirmPasswordView.getView().findViewById(R.id.validation_image);
        mConfirmPasswordField.setHint(R.string.confirm_password);
        mConfirmPasswordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mConfirmPasswordField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mConfirmPasswordField.setCustomFont(Registration.this, getString(R.string.font_malgun_semi_light));

        mFullNameView = (FragmentView) getFragmentManager().findFragmentById(R.id.full_name_view);
        mFullNameField = mFullNameView.getView().findViewById(R.id.edit_text);
        mFullNameErrorMsg = mFullNameView.getView().findViewById(R.id.error_msg);
        mFullNameValidationImage = mFullNameView.getView().findViewById(R.id.validation_image);
        mFullNameField.setHint(R.string.full_name);
        mFullNameField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        mFullNameField.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        mPhoneNumberView = (FragmentView) getFragmentManager().findFragmentById(R.id.phone_number_view);
        mPhoneNumberField = mPhoneNumberView.getView().findViewById(R.id.edit_text);
        mPhoneNumberErrorMsg = mPhoneNumberView.getView().findViewById(R.id.error_msg);
        mPhoneNumberValidationImage = mPhoneNumberView.getView().findViewById(R.id.validation_image);
        mPhoneNumberField.setHint(getString(R.string.phone_number) + " (optional)");
        mPhoneNumberField.setInputType(InputType.TYPE_CLASS_PHONE);
        mPhoneNumberField.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        mStreetAddressView = (FragmentView) getFragmentManager().findFragmentById(R.id.street_address_view);
        mStreetAddressField = mStreetAddressView.getView().findViewById(R.id.edit_text);
        mStreetAddressErrorMsg = mStreetAddressView.getView().findViewById(R.id.error_msg);
        mStreetAddressValidationImage = mStreetAddressView.getView().findViewById(R.id.validation_image);
        mStreetAddressField.setHint(R.string.street_address);
        mStreetAddressField.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        mStreetAddressField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mStreetAddressField.addTextChangedListener(this);
        mStreetAddressField.setOnClickListener(this);

        mVehicleMakeView = (FragmentView) getFragmentManager().findFragmentById(R.id.vehicle_make_view);
        mVehicleMakeField = mVehicleMakeView.getView().findViewById(R.id.edit_text);
        mVehicleMakeErrorMsg = mVehicleMakeView.getView().findViewById(R.id.error_msg);
        mVehicleMakeValidationImage = mVehicleMakeView.getView().findViewById(R.id.validation_image);
        mVehicleMakeField.setHint(R.string.vehicle_make);
        mVehicleMakeField.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        mVehicleMakeField.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        mVehicleModelView = (FragmentView) getFragmentManager().findFragmentById(R.id.vehicle_model_view);
        mVehicleModelField = mVehicleModelView.getView().findViewById(R.id.edit_text);
        mVehicleModelErrorMsg = mVehicleModelView.getView().findViewById(R.id.error_msg);
        mVehicleModelValidationImage = mVehicleModelView.getView().findViewById(R.id.validation_image);
        mVehicleModelField.setHint(R.string.vehicle_model);
        mVehicleModelField.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        mVehicleModelField.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        mVehicleYearView = (FragmentView) getFragmentManager().findFragmentById(R.id.vehicle_year_view);
        mVehicleYearField = mVehicleYearView.getView().findViewById(R.id.edit_text);
        mVehicleYearErrorMsg = mVehicleYearView.getView().findViewById(R.id.error_msg);
        mVehicleYearValidationImage = mVehicleYearView.getView().findViewById(R.id.validation_image);
        mVehicleYearField.setHint(R.string.vehicle_year);
        mVehicleYearField.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        mVehicleYearField.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        mInstance.myLibrary.disableSoftInputFromAppearing(mVehicleMakeField);
        mInstance.myLibrary.disableSoftInputFromAppearing(mVehicleModelField);
        mInstance.myLibrary.disableSoftInputFromAppearing(mVehicleYearField);
        mNextButton = findViewById(R.id.next_button);

        mNextButton.setOnClickListener(this);
        mStreetAddressField.setOnClickListener(this);
        mVehicleMakeField.setOnClickListener(this);
        mVehicleModelField.setOnClickListener(this);
        mVehicleYearField.setOnClickListener(this);
        mEmailField.setOnFocusChangeListener(this);
        mPasswordField.setOnFocusChangeListener(this);
        mConfirmPasswordField.setOnFocusChangeListener(this);
        mFullNameField.setOnFocusChangeListener(this);
        mPhoneNumberField.setOnFocusChangeListener(this);
        mStreetAddressField.setOnFocusChangeListener(this);
        mVehicleMakeField.setOnFocusChangeListener(this);
        mVehicleModelField.setOnFocusChangeListener(this);
        mVehicleYearField.setOnFocusChangeListener(this);

        mEmailErrorMsg.setVisibility(View.INVISIBLE);
        mEmailValidationImage.setVisibility(View.INVISIBLE);
        mPasswordErrorMsg.setVisibility(View.INVISIBLE);
        mPasswordValidationImage.setVisibility(View.INVISIBLE);
        mConfirmPasswordErrorMsg.setVisibility(View.INVISIBLE);
        mConfirmPasswordValidationImage.setVisibility(View.INVISIBLE);
        mPhoneNumberErrorMsg.setVisibility(View.INVISIBLE);
        mPhoneNumberValidationImage.setVisibility(View.INVISIBLE);
        mFullNameErrorMsg.setVisibility(View.INVISIBLE);
        mFullNameValidationImage.setVisibility(View.INVISIBLE);
        mStreetAddressErrorMsg.setVisibility(View.INVISIBLE);
        mStreetAddressValidationImage.setVisibility(View.INVISIBLE);
        mVehicleMakeErrorMsg.setVisibility(View.INVISIBLE);
        mVehicleMakeValidationImage.setVisibility(View.INVISIBLE);
        mVehicleModelErrorMsg.setVisibility(View.INVISIBLE);
        mVehicleModelValidationImage.setVisibility(View.INVISIBLE);
        mVehicleYearErrorMsg.setVisibility(View.INVISIBLE);
        mVehicleYearValidationImage.setVisibility(View.INVISIBLE);
        setTags();

        mVehicleMakeField.addTextChangedListener(this);
        mVehicleModelField.addTextChangedListener(this);
        mVehicleYearField.addTextChangedListener(this);
        //addOnTextChangedListener();

        if(!BuildConfig.RELEASE){
            mEmailField.setCustomText("testuser1@tnedicca.com");
            mPasswordField.setCustomText("Tnedicca$123");
            mConfirmPasswordField.setCustomText("Tnedicca$123");
            mFullNameField.setCustomText("Mobile User");
        }
    }

    // setting year list to 1070 to next year
    private void setYearAdapter() {
        yearList = new ArrayList();
        yearList.add(" ");
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = thisYear + 1; i >= 1970; i--) {
            yearList.add(Integer.toString(i));
        }
    }

    // setting tags for all fields variables
    private void setTags() {
        mEmailField.setTag(AppConstant.EMAIL_TAG);
        mPasswordField.setTag(AppConstant.CREATE_PASSWORD_TAG);
        mConfirmPasswordField.setTag(AppConstant.CONFIRM_PASSWORD_TAG);
        mFullNameField.setTag(AppConstant.FULL_NAME_TAG);
        mPhoneNumberField.setTag(AppConstant.PHONE_NUMBER_TAG);
        mStreetAddressField.setTag(AppConstant.STREET_ADDRESS_TAG);
        mVehicleMakeField.setTag(AppConstant.VEHICLE_MAKE_TAG);
        mVehicleModelField.setTag(AppConstant.VEHICLE_MODEL_TAG);
        mVehicleYearField.setTag(AppConstant.VEHICLE_YEAR_TAG);
        mNextButton.setTag(AppConstant.NEXT_BUTTON_TAG);
    }

    //Method to validate username on focus changed
    private void onEmailFocusChange(boolean hasFocus) {
        mEmail = mEmailField.getCustomText();
        if (!hasFocus) {
            validateEmail();
        } else {
            displayMessage(AppConstant.CLEAR_EMAIL_MESSAGE);
        }
    }

    //Method to validate password on focus changed
    private void onPasswordFocusChange(boolean hasFocus) {
        mPassword = mPasswordField.getCustomText();
        if (!hasFocus) {
            validateCreatePassword();
        } else {
            displayMessage(AppConstant.CLEAR_PASSSWORD_MESSAGE);
        }
    }

    // method to validate confirm password on focus changed
    private void onConfirmPasswordFocusChange(boolean hasFocus) {
        mConfirmPassword = mConfirmPasswordField.getCustomText();
        if (!hasFocus) {
            validateConfirmPassword();
        } else {
            displayMessage(AppConstant.CLEAR_CONFIRM_PASSWORD);
        }
    }

    //method to validate name on focus change
    private void onFullNameFocusChange(boolean hasFocus) {
        mFullName = mFullNameField.getCustomText();
        if (!hasFocus) {
            validateFullName();
        } else {
            displayMessage(AppConstant.CLEAR_FULL_NAME);
        }
    }

    // method to validate phone number on focus change
    private void onPhoneNumberFocusChange(boolean hasFocus) {
        mPhoneNumber = mPhoneNumberField.getCustomText();
        if (!hasFocus) {
            validatePhoneNumber();
        } else {
            displayMessage(AppConstant.CLEAR_PHONE_NUMBER);
        }
    }

    // method to validate street address on focus change
    private void onStreetAddressFocusChange(boolean hasFocus) {
        mStreetAddress = mStreetAddressField.getCustomText();
        if (!hasFocus) {
            validateStreetAddress();
        } else {
            displayMessage(AppConstant.CLEAR_STREET_NAME);
            Intent intent = new Intent(Registration.this, AddressSuggestion.class);
            startActivityForResult(intent, AppConstant.ADDRESS_REQUEST_CODE);
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        }
    }

    // call volley requests for getting vehicle details list
    private void getVehicleDetailsList(String requestType) {
        int method = Request.Method.GET;
        String url = BuildConfig.REST_URL + AppConstant.VEHICLE_URL;
        if (mInstance.dataStatus) {
            progressDialog.setMessage(AppConstant.LOADING);
            progressDialog.show();
            makeJsonArryReq(url, method, AppConstant.VEHICLE_MAKE, requestType);
        } else {
            mInstance.myLibrary.DisplayToast(this, AppConstant.NO_INTERNET_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
            displayLoginScreenImmediately();
        }
    }

    // method to validate vehicle make on focus change
    private void onVehicleMakeFocusChange(View v, boolean hasFocus) {
        mVehicleMake = mVehicleMakeField.getCustomText();
        if (hasFocus) {
            mInstance.myLibrary.hideKeyBoard(this.getCurrentFocus(), getApplicationContext());
            displayMessage(AppConstant.CLEAR_VEHICLE_MAKE);
            if (vehicleMakeArrayList != null && vehicleMakeArrayList.size() > 0) {
                vehicleMakePicker.showAtLocation(v, Gravity.BOTTOM, 0, 0);
            } else {
                mInstance.myLibrary.DisplayToast(this, AppConstant.UNABLE_TO_FETCH, Toast.LENGTH_SHORT, Gravity.CENTER);
            }
        } else {
            validateVehicleMake();
        }
    }

    // method to validate vehicle model on focus change
    private void onVehicleModelFocusChange(View v, boolean hasFocus) {
        mVehicleModel = mVehicleModelField.getCustomText();
        if (hasFocus) {
            mInstance.myLibrary.hideKeyBoard(this.getCurrentFocus(), getApplicationContext());
            if (refinedVehicleModelArrayList == null || refinedVehicleModelArrayList.size() <= 1) {
                refinedVehicleModelArrayList = new ArrayList();
                refinedVehicleModelArrayList.add(" ");
                for (Map.Entry<Integer, String> entry : vehicleMakeHashMap.entrySet()) {
                    if (entry.getValue().equals(mVehicleMake)) {
                        for (Map.Entry<String, Integer> entry1 : vehicleModelHashMap.entrySet()) {
                            if (entry.getKey().equals(entry1.getValue())) {
                                String name = entry1.getKey().trim();
                                if (!name.isEmpty()) {
                                    refinedVehicleModelArrayList.add(entry1.getKey());
                                }
                            }
                        }
                    }
                }
                if (refinedVehicleModelArrayList != null && refinedVehicleModelArrayList.size() > 1) {
                    vehicleModelPicker.setPicker(refinedVehicleModelArrayList);
                    displayMessage(AppConstant.CLEAR_VEHICLE_MODEL);
                    vehicleModelPicker.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                }
            } else {
                displayMessage(AppConstant.CLEAR_VEHICLE_MODEL);
                vehicleModelPicker.showAtLocation(v, Gravity.BOTTOM, 0, 0);
            }
        } else {
            validateVehicleModel();
        }
    }

    // method to validate vehicle year on focus change
    private void onVehicleYearFocusChange(View v, boolean hasFocus) {
        mVehicleYear = mVehicleYearField.getCustomText();
        if (hasFocus) {
            mInstance.myLibrary.hideKeyBoard(this.getCurrentFocus(), getApplicationContext());
            displayMessage(AppConstant.CLEAR_VEHICLE_YEAR);
            vehicleYearPicker.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        } else {
            validateVehicleYear();
        }
    }

    // displayes various messages post validation
    private void displayMessage(String messageType) {
        switch (messageType) {
            case AppConstant.BLANK_EMAIL:
                mInstance.myLibrary.setMessageOnValidationError(mEmailErrorMsg, mEmailValidationImage, getString(R.string.blank_email_address_msg));
                break;
            case AppConstant.BLANK_PASSWORD:
                mInstance.myLibrary.setMessageOnValidationError(mPasswordErrorMsg, mPasswordValidationImage, getString(R.string.blank_password_msg));
                break;
            case AppConstant.INVALID_EMAIL:
                mInstance.myLibrary.setMessageOnValidationError(mEmailErrorMsg, mEmailValidationImage, getString(R.string.invalid_email_address_msg));
                break;
            case AppConstant.INVALID_PASSWORD:
                mInstance.myLibrary.setMessageOnValidationError(mPasswordErrorMsg, mPasswordValidationImage, getString(R.string.invalid_password_msg));
                break;
            case AppConstant.VALID_EMAIL:
                mInstance.myLibrary.setMessageOnValidationPass(mEmailErrorMsg, mEmailValidationImage);
                break;
            case AppConstant.VALID_PASSWORD:
                mInstance.myLibrary.setMessageOnValidationPass(mPasswordErrorMsg, mPasswordValidationImage);
                break;
            case AppConstant.CLEAR_EMAIL_MESSAGE:
                mInstance.myLibrary.clearMessage(mEmailErrorMsg, mEmailValidationImage);
                break;
            case AppConstant.CLEAR_PASSSWORD_MESSAGE:
                mInstance.myLibrary.clearMessage(mPasswordErrorMsg, mPasswordValidationImage);
                break;
            case AppConstant.BLANK_CONFIRM_PASSWORD:
                mInstance.myLibrary.setMessageOnValidationError(mConfirmPasswordErrorMsg, mConfirmPasswordValidationImage, getString(R.string.blank_confirm_password_msg));
                break;
            case AppConstant.INVALID_CONFIRM_PASSWORD:
                mInstance.myLibrary.setMessageOnValidationError(mConfirmPasswordErrorMsg, mConfirmPasswordValidationImage, getString(R.string.invalid_password_msg));
                break;
            case AppConstant.CONFIRM_PASSWORD_MATCHED:
                mInstance.myLibrary.setMessageOnValidationPass(mConfirmPasswordErrorMsg, mConfirmPasswordValidationImage);
                break;
            case AppConstant.CONFIRM_PASSWORD_NOT_MATCHED:
                mInstance.myLibrary.setMessageOnValidationError(mConfirmPasswordErrorMsg, mConfirmPasswordValidationImage, getString(R.string.password_not_matched_msg));
                break;
            case AppConstant.CLEAR_CONFIRM_PASSWORD:
                mInstance.myLibrary.clearMessage(mConfirmPasswordErrorMsg, mConfirmPasswordValidationImage);
                break;
            case AppConstant.BLANK_FULL_NAME:
                mInstance.myLibrary.setMessageOnValidationError(mFullNameErrorMsg, mFullNameValidationImage, getString(R.string.blank_full_name_msg));
                break;
            case AppConstant.INVALID_FULL_NAME:
                mInstance.myLibrary.setMessageOnValidationError(mFullNameErrorMsg, mFullNameValidationImage, getString(R.string.full_name_error_msg));
                break;
            case AppConstant.VALID_FULL_NAME:
                mInstance.myLibrary.setMessageOnValidationPass(mFullNameErrorMsg, mFullNameValidationImage);
                break;
            case AppConstant.CLEAR_FULL_NAME:
                mInstance.myLibrary.clearMessage(mFullNameErrorMsg, mFullNameValidationImage);
                break;
            case AppConstant.BLANK_PHONE_NUMBER:
                mInstance.myLibrary.setMessageOnValidationError(mPhoneNumberErrorMsg, mPhoneNumberValidationImage, getString(R.string.blank_phone_number_msg));
                break;
            case AppConstant.VALID_PHONE_NUMBER:
                mInstance.myLibrary.setMessageOnValidationPass(mPhoneNumberErrorMsg, mPhoneNumberValidationImage);
                break;
            case AppConstant.INVALID_PHONE_NUMBER:
                mInstance.myLibrary.setMessageOnValidationError(mPhoneNumberErrorMsg, mPhoneNumberValidationImage, getString(R.string.invalid_phone_number_msg));
                break;
            case AppConstant.CLEAR_PHONE_NUMBER:
                mInstance.myLibrary.clearMessage(mPhoneNumberErrorMsg, mPhoneNumberValidationImage);
                break;
            case AppConstant.BLANK_STREET_NAME:
                mInstance.myLibrary.setMessageOnValidationError(mStreetAddressErrorMsg, mStreetAddressValidationImage, getString(R.string.blank_address_msg));
                break;
            case AppConstant.INVALID_STREET_NAME:
                mInstance.myLibrary.setMessageOnValidationError(mStreetAddressErrorMsg, mStreetAddressValidationImage, getString(R.string.invalid_address_msg));
                break;
            case AppConstant.VALID_STREET_NAME:
                mInstance.myLibrary.setMessageOnValidationPass(mStreetAddressErrorMsg, mStreetAddressValidationImage);
                break;
            case AppConstant.CLEAR_STREET_NAME:
                mInstance.myLibrary.clearMessage(mStreetAddressErrorMsg, mStreetAddressValidationImage);
                break;
            case AppConstant.BLANK_VEHICLE_MAKE:
                mInstance.myLibrary.setMessageOnValidationError(mVehicleMakeErrorMsg, mVehicleMakeValidationImage, getString(R.string.blank_vehicle_make_msg));
                break;
            case AppConstant.INVALID_VEHICLE_MAKE:
                mInstance.myLibrary.setMessageOnValidationError(mVehicleMakeErrorMsg, mVehicleMakeValidationImage, getString(R.string.vehicle_make_msg));
                break;
            case AppConstant.VALID_VEHICLE_MAKE:
                mInstance.myLibrary.setMessageOnValidationPass(mVehicleMakeErrorMsg, mVehicleMakeValidationImage);
                break;
            case AppConstant.CLEAR_VEHICLE_MAKE:
                mInstance.myLibrary.clearMessage(mVehicleMakeErrorMsg, mVehicleMakeValidationImage);
                break;
            case AppConstant.BLANK_VEHICLE_MODEL:
                mInstance.myLibrary.setMessageOnValidationError(mVehicleModelErrorMsg, mVehicleModelValidationImage, getString(R.string.blank_vehicle_model_msg));
                break;
            case AppConstant.INVALID_VEHICLE_MODEL:
                mInstance.myLibrary.setMessageOnValidationError(mVehicleModelErrorMsg, mVehicleModelValidationImage, getString(R.string.vehicle_model_msg));
                break;
            case AppConstant.VALID_VEHICLE_MODEL:
                mInstance.myLibrary.setMessageOnValidationPass(mVehicleModelErrorMsg, mVehicleModelValidationImage);
                break;
            case AppConstant.CLEAR_VEHICLE_MODEL:
                mInstance.myLibrary.clearMessage(mVehicleModelErrorMsg, mVehicleModelValidationImage);
                break;
            case AppConstant.BLANK_VEHICLE_YEAR:
                mInstance.myLibrary.setMessageOnValidationError(mVehicleYearErrorMsg, mVehicleYearValidationImage, getString(R.string.blank_vehicle_year_msg));
                break;
            case AppConstant.INVALID_VEHICLE_YEAR:
                mInstance.myLibrary.setMessageOnValidationError(mVehicleYearErrorMsg, mVehicleYearValidationImage, getString(R.string.vehicle_year_msg));
                break;
            case AppConstant.VALID_VEHICLE_YEAR:
                mInstance.myLibrary.setMessageOnValidationPass(mVehicleYearErrorMsg, mVehicleYearValidationImage);
                break;
            case AppConstant.CLEAR_VEHICLE_YEAR:
                mInstance.myLibrary.clearMessage(mVehicleYearErrorMsg, mVehicleYearValidationImage);
                break;
            default:
                logger.info("default statement for display message executed for message type : " + messageType);
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Object obj = v.getTag();
        int tag = (int) obj;
        switch (tag) {
            case AppConstant.EMAIL_TAG:
                onEmailFocusChange(hasFocus);
                break;
            case AppConstant.CREATE_PASSWORD_TAG:
                onPasswordFocusChange(hasFocus);
                break;
            case AppConstant.CONFIRM_PASSWORD_TAG:
                onConfirmPasswordFocusChange(hasFocus);
                break;
            case AppConstant.FULL_NAME_TAG:
                onFullNameFocusChange(hasFocus);
                break;
            case AppConstant.PHONE_NUMBER_TAG:
                onPhoneNumberFocusChange(hasFocus);
                break;
            case AppConstant.STREET_ADDRESS_TAG:
                onStreetAddressFocusChange(hasFocus);
                break;
            case AppConstant.VEHICLE_MAKE_TAG:
                onVehicleMakeFocusChange(v, hasFocus);
                break;
            case AppConstant.VEHICLE_MODEL_TAG:
                onVehicleModelFocusChange(v, hasFocus);
                break;
            case AppConstant.VEHICLE_YEAR_TAG:
                onVehicleYearFocusChange(v, hasFocus);
                break;
            default:
                logger.error(getString(R.string.log_registration_onfocuschange_default) + tag);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        mStreetAddressField.setText((String) adapterView.getItemAtPosition(position));
        isStreetSelected = true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.hashCode() == mStreetAddressField.getText().hashCode()) {
            isStreetSelected = false;
        } else if (s.hashCode() == mVehicleMakeField.getText().hashCode()) {
            isVehicleMakeSelected = false;
            vehicleMakePicker.showAtLocation(mVehicleMakeField.getRootView(), Gravity.BOTTOM, 0, 0);
        } else if (s.hashCode() == mVehicleModelField.getText().hashCode()) {
            isVehicleModelSelected = false;
            if (refinedVehicleModelArrayList != null && refinedVehicleModelArrayList.size() != 0) {
                vehicleModelPicker.showAtLocation(mVehicleModelField.getRootView(), Gravity.BOTTOM, 0, 0);
            }
        } else if (s.hashCode() == mVehicleYearField.getText().hashCode()) {
            isVehicleYearSelected = false;
            vehicleYearPicker.showAtLocation(mVehicleYearField.getRootView(), Gravity.BOTTOM, 0, 0);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    //Volley Request
    private void makeJsonArryReq(String url, final int method, final String tag, final String queryType) {
        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                volleyArray.remove(tag);
//                logger.info(getString(R.string.received_response));
                response(tag, response, 200, queryType);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyArray.remove(tag);
                logger.info(getString(R.string.received_error));
                error(tag, error, queryType);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(AppConstant.REST_KEY_CONTENT, AppConstant.REST_KEY_CONTENT_TYPE);
                headers.put(AppConstant.REST_KEY_API, mInstance.myLibrary.apiKeyEncrypter());
                if (queryType.equals(AppConstant.QUERY_VEHICLE_MAKE)) {
                    headers.put(AppConstant.REST_KEY_TYPE, AppConstant.VEHICLE_MAKE);
                } else if (queryType.equals(AppConstant.QUERY_VEHICLE_MODEL)) {
                    headers.put(AppConstant.REST_KEY_TYPE, AppConstant.VEHICLE_MODEL);
                }
                return headers;
            }

            @Override
            public byte[] getBody() {
                return getBodyForSubmit(queryType);
            }

            @Override
            public int getMethod() {
                return method;
            }

            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                String jsonString = null;
                try {
                    jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    JSONArray jsonArray = new JSONArray(jsonString);
                    jsonArray.put(response.statusCode);
                    return Response.success(jsonArray, HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException e) {
                    lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                    logger.error("Error at " + lineNumber, e);
                    return Response.error(new ParseError(e));
                } catch (JSONException e) {
                    lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                    logger.error("Error at " + lineNumber, e);
                    return Response.error(new ParseError(e));
                }
            }
        };

        req.setRetryPolicy(new DefaultRetryPolicy(AppConstant.REST_RETRY_MILLISECONDS, AppConstant.REST_RETRY, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        // Adding request to request queue
        RouteWise.getInstance().addToRequestQueue(req, tag);
        volleyArray.add(tag);
    }

    //handles diferent response codes from Volley
    private void response(String check, JSONArray response, int code, String queryType) {
        progressDialog.dismiss();
        mInstance.myLibrary.checkResponse(response, code, this);
        if (code == AppConstant.RESPONSE_BAD_REQUEST || code == AppConstant.RESPONSE_SERVER_ERROR) {
            mInstance.myLibrary.DisplayToast(this, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
            if (queryType.equals(AppConstant.QUERY_VEHICLE_MAKE) || queryType.equals(AppConstant.QUERY_VEHICLE_MODEL)) {
                displayLoginScreenImmediately();
            }
        } else {
            // handling response for submitting registration data
            if (queryType.equals(AppConstant.SUBMIT_REGISTRATION_DATA)) {
                JSONObject result = response.optJSONObject(0);
                int status = response.optInt(1);
                String message = result.optString("message");
                if (status == 200) {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.log_message) + " " + getString(R.string.registration_submit_succes));
                } else if (status == 202) {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.user_already_registered) + " " + mEmail);
                } else if (status == 203) {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.phone_number_already_registered) + " " + mPhoneNumber);
                }
                if (check.equals(AppConstant.REGISTER_URL)) {
                    displayServerMsg(result, status);
                }
            }
            // handling response for vehicle make request
            else if (queryType.equals(AppConstant.QUERY_VEHICLE_MAKE)) {
                vehicleMakeArrayList = new ArrayList();
                vehicleMakeHashMap = new HashMap<>();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Object obj = response.get(i);
                        if (obj.getClass().equals(JSONObject.class)) {
                            JSONObject tempObject = (JSONObject) response.get(i);
                            if (tempObject.has("name")) {
                                vehicleMakeHashMap.put(tempObject.getInt("id"), tempObject.getString("name"));
                                String name = tempObject.getString("name");
                                if (name != null && !name.isEmpty()) {
                                    vehicleMakeArrayList.add(name);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                        logger.error("Error at " + lineNumber, e);
                    }
                }
                vehicleMakePicker.setPicker(vehicleMakeArrayList);
            }
            // handling response for vehicle model request
            else if (queryType.equals(AppConstant.QUERY_VEHICLE_MODEL)) {
                vehicleModelArrayList = new ArrayList();
                vehicleModelHashMap = new HashMap<>();
                mVehicleMake = mVehicleMakeField.getCustomText();
                for (int i = 0; i < response.length(); i++) {
                    try {
                        Object obj = response.get(i);
                        if (obj.getClass().equals(JSONObject.class)) {
                            JSONObject tempObject = (JSONObject) response.get(i);
                            if (tempObject.has("name")) {
                                vehicleModelHashMap.put(tempObject.getString("name"), tempObject.getInt("vehicle_make_id"));
                                vehicleModelArrayList.add(tempObject.getString("name"));
                            }
                        }
                    } catch (JSONException e) {
                        lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                        logger.error("Error at " + lineNumber, e);
                    }
                }
            }
        }
    }

    // start Approval Activity
    private void displayApproval(JSONObject result) {
        token = result.optString(AppConstant.TOKEN);
        activationCode = result.optString(AppConstant.ACTIVATION_CODE);
        Intent intent = new Intent(Registration.this, Approval.class);
        intent.putExtra(AppConstant.TOKEN, token);
        intent.putExtra(AppConstant.ACTIVATION_CODE, activationCode);
        intent.putExtra(AppConstant.USER, mEmail);
        intent.putExtra(AppConstant.MAIL_TYPE, AppConstant.MAIL_REGISTER);
        intent.putExtra(AppConstant.KEEP_ME_LOGGED_IN, keepMeLoggedIn);
        intent.putExtra(AppConstant.RESET_SCREEN, false);
        intent.putExtra(AppConstant.RESET_ID, mEmail);
        intent.putExtra(AppConstant.LOGIN_TYPE, true);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
    }

    private void displayLoginScreenImmediately() {
        Intent intent = new Intent(Registration.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    private void displayPopup() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(AppConstant.DEFAULT_DB_NAME).setMessage(getString(R.string.user_already_registered) + ", continue with").setCancelable(false);
        alertDialogBuilder.setPositiveButton(getString(R.string.login), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(Registration.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.forgot), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent forgot = new Intent(Registration.this, ForgotPassword.class);
                forgot.putExtra(AppConstant.USER_EMAIL, mEmail);
                forgot.putExtra(AppConstant.RESET_SCREEN, true);
                forgot.putExtra(AppConstant.EMAIL_TYPE, false);
                forgot.putExtra(AppConstant.KEEP_ME_LOGGED_IN, keepMeLoggedIn);
                startActivity(forgot);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
            }
        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.setCancelable(false);
        alert.show();
    }

    // display server message
    private void displayServerMsg(JSONObject result, int code) {
        switch (code) {
            case AppConstant.RESPONSE_SUCCESS:
            case AppConstant.RESPONSE_GOOGLE_ANONYMUS:
                displayApproval(result);
                break;
            case AppConstant.RESPONSE_USER_ALREADY_REGISTERED:
                scrollView.fullScroll(ScrollView.FOCUS_UP);
                mInstance.myLibrary.DisplayToast(this, getString(R.string.user_already_registered), Toast.LENGTH_LONG, Gravity.BOTTOM);
                mInstance.myLibrary.setMessageOnValidationError(mEmailErrorMsg, mEmailValidationImage, getString(R.string.user_already_registered));
                displayPopup();
                break;
            case AppConstant.RESPONSE_PHONE_NUMBER_ALREADY_REGISTERED:
                mInstance.myLibrary.DisplayToast(this, getString(R.string.phone_number_already_registered), Toast.LENGTH_LONG, Gravity.BOTTOM);
                mInstance.myLibrary.setMessageOnValidationError(mPhoneNumberErrorMsg, mPhoneNumberValidationImage, getString(R.string.phone_number_already_registered));
                break;
            default:
                logger.info("default executed for displayServerMsg for response code : " + code);
                break;
        }
    }

    //handles error response from Volley
    private void error(String check, VolleyError error, String queryType) {
        progressDialog.dismiss();
        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                int statusCode = error.networkResponse.statusCode;
                byte[] s = error.networkResponse.data;
                String message = new String(s);
                System.out.println(message);
                logger.info("message : " + message);
                JSONArray response = new JSONArray(message);
                response(check, response, statusCode, queryType);
            } else {
                if (queryType.equals(AppConstant.QUERY_VEHICLE_MAKE) || queryType.equals(AppConstant.QUERY_VEHICLE_MODEL)) {
                    mInstance.myLibrary.DisplayToast(this, AppConstant.UNABLE_TO_FETCH, Toast.LENGTH_SHORT, Gravity.CENTER);
                    displayLoginScreenImmediately();
                } else {
                    mInstance.myLibrary.DisplayToast(this, AppConstant.CONNECTION_TIMEOUT, Toast.LENGTH_SHORT, Gravity.CENTER);
                }
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }

    // configuring body for volley request
    private byte[] getBodyForSubmit(String queryType) {
        JSONObject finalToken = new JSONObject();
        JSONObject tempjson = new JSONObject();
        // body for submitting registration data
        if (queryType.equals(AppConstant.SUBMIT_REGISTRATION_DATA)) {
            try {
                String device_id = mInstance.myLibrary.getUniqueId(this);
                String temp = Library.getOriginalString(mEmail, true);
                tempjson.put(AppConstant.EMAIL, temp);
                if (mPassword != null)
                    tempjson.put(AppConstant.PSWRD, mInstance.myLibrary.passwordEncrypter(mPassword));
                tempjson.put(AppConstant.USERNAME, mFullName);
                tempjson.put(AppConstant.DEVICE_ID, device_id);
                tempjson.put(AppConstant.PHONE_NUMBER, mPhoneNumber.trim());
                tempjson.put(AppConstant.ADDRESS, Library.getOriginalString(mStreetAddress, true));
                tempjson.put(AppConstant.VEHICLE_MAKE, mVehicleMake.trim());
                tempjson.put(AppConstant.VEHICLE_MODEL, mVehicleModel.trim());
                tempjson.put(AppConstant.VEHICLE_YEAR, mVehicleYear.trim());
            } catch (Exception e) {
                lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                logger.error(getString(R.string.log_error_at) + " " + lineNumber, e);
            }
        }
        String query = mInstance.myLibrary.dataEncrypt(tempjson.toString());
        try {
            finalToken.put("json", query);
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
        return finalToken.toString().getBytes();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!volleyArray.isEmpty()) {
            for (int i = 0; i < volleyArray.size(); i++) {
                mInstance.getRequestQueue().cancelAll(volleyArray.get(i));
            }
        }
    }

    @Override
    public void onClick(View v) {
        mInstance.myLibrary.hideKeyBoard(this.getCurrentFocus(), getApplicationContext());
        Object obj = v.getTag();
        int tag = (int) obj;
        switch (tag) {
            case AppConstant.BACK_ICON_TAG_REGISTRATION:
                onBackPressed();
                break;
            case AppConstant.NEXT_BUTTON_TAG:
                validateAllFields();
                if (areAllFieldsValid) {
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage(AppConstant.PROCESSING_MSG);
                    progressDialog.show();
                    int method = Request.Method.POST;
                    String url = BuildConfig.REST_URL + AppConstant.REGISTER_URL;
                    if (mInstance.dataStatus) {
                        makeJsonArryReq(url, method, AppConstant.REGISTER_URL, AppConstant.SUBMIT_REGISTRATION_DATA);
                    } else {
                        logger.info(getString(R.string.log_connecting_rest_api));
                        progressDialog.dismiss();
                        mInstance.myLibrary.DisplayToast(this, AppConstant.NO_INTERNET_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
                    }
                } else
                    mEmailErrorMsg.requestFocus();
                break;
            case AppConstant.STREET_ADDRESS_TAG:
                displayMessage(AppConstant.CLEAR_STREET_NAME);
                Intent intent = new Intent(Registration.this, AddressSuggestion.class);
                startActivityForResult(intent, AppConstant.ADDRESS_REQUEST_CODE);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                break;
            case AppConstant.VEHICLE_MAKE_TAG:
                if (vehicleMakeArrayList != null && vehicleMakeArrayList.size() > 0) {
                    vehicleMakePicker.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                } else {
                    mInstance.myLibrary.DisplayToast(this, AppConstant.UNABLE_TO_FETCH, Toast.LENGTH_SHORT, Gravity.CENTER);
                }
                break;
            case AppConstant.VEHICLE_MODEL_TAG:
                if (refinedVehicleModelArrayList != null && refinedVehicleModelArrayList.size() > 1) {
                    //vehicleModelPicker.setPicker(refinedVehicleModelArrayList);
                    displayMessage(AppConstant.CLEAR_VEHICLE_MODEL);
                    vehicleModelPicker.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                }
                break;
            case AppConstant.VEHICLE_YEAR_TAG:
                vehicleYearPicker.showAtLocation(v, Gravity.BOTTOM, 0, 0);
                break;
            default:
                logger.info("OnClick default executed for onClick for view tag : " + tag);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppConstant.ADDRESS_REQUEST_CODE:
                mStreetAddressField.setCustomText(data.getStringExtra("ADDRESS"));
                isStreetSelected = true;
                break;
            default:
                logger.info("onActivityResult default executed for onActivityResult for requestCode : " + requestCode);
                break;
        }
    }

    // validates email
    private boolean validateEmail() {
        mEmail = mEmailField.getCustomText();
        if (!mEmail.isEmpty()) {
            if (!mInstance.myLibrary.isValidMail(mEmail)) {
                logger.info(getString(R.string.log_on_focus_changed) + " " + AppConstant.INVALID_EMAIL + " : " + mEmail);
                displayMessage(AppConstant.INVALID_EMAIL);
                return false;
            } else {
                displayMessage(AppConstant.VALID_EMAIL);
                return true;
            }
        } else {
            displayMessage(AppConstant.BLANK_EMAIL);
            return false;
        }
    }

    // validates password format
    private boolean validateCreatePassword() {
        mPassword = mPasswordField.getCustomText();
        if (!mPassword.isEmpty()) {
            if (!mInstance.myLibrary.isValidPasswordFormat(mPassword)) {
                logger.info(getString(R.string.log_on_focus_changed) + " " + AppConstant.INVALID_PASSWORD + " format");
                displayMessage(AppConstant.INVALID_PASSWORD);
                return false;
            } else {
                displayMessage(AppConstant.VALID_PASSWORD);
                mConfirmPassword = mConfirmPasswordField.getCustomText();
                if (mConfirmPassword != null && !mConfirmPassword.isEmpty()) {
                    onConfirmPasswordFocusChange(false);
                }
                return true;
            }
        } else {
            displayMessage(AppConstant.BLANK_PASSWORD);
            return false;
        }
    }

    // validate if confirm password is same as password
    private boolean validateConfirmPassword() {
        mConfirmPassword = mConfirmPasswordField.getCustomText();
        if (mConfirmPassword.isEmpty()) {
            displayMessage(AppConstant.BLANK_CONFIRM_PASSWORD);
            return false;
        } else if (!mInstance.myLibrary.isValidPasswordFormat(mPassword)) {
            logger.info(getString(R.string.log_on_focus_changed) + " " + AppConstant.INVALID_CONFIRM_PASSWORD + " format");
            displayMessage(AppConstant.INVALID_CONFIRM_PASSWORD);
            return false;
        } else if (mConfirmPassword.equals(mPassword)) {
            displayMessage(AppConstant.CONFIRM_PASSWORD_MATCHED);
            return true;
        } else {
            logger.info(getString(R.string.log_on_focus_changed) + " " + AppConstant.INVALID_PASSWORD + " format");
            displayMessage(AppConstant.CONFIRM_PASSWORD_NOT_MATCHED);
            return false;
        }
    }

    // validates name
    private boolean validateFullName() {
        mFullName = mFullNameField.getCustomText();
        if (mFullName.isEmpty()) {
            displayMessage(AppConstant.BLANK_FULL_NAME);
            return false;
        } else {
            if (!mInstance.myLibrary.isValidName(mFullName)) {
                logger.info(getString(R.string.full_name_error_msg));
                displayMessage(AppConstant.INVALID_FULL_NAME);
                return false;
            } else {
                displayMessage(AppConstant.VALID_FULL_NAME);
                return true;
            }
        }
    }

    // validates phone number
    private boolean validatePhoneNumber() {
        mPhoneNumber = mPhoneNumberField.getCustomText();
        if (mPhoneNumber.isEmpty()) {
            displayMessage(AppConstant.CLEAR_PHONE_NO);
            return true;
        } else if (!mInstance.myLibrary.isValidPhone(mPhoneNumber)) {
            logger.info(getString(R.string.invalid_phone_number_msg));
            displayMessage(AppConstant.INVALID_PHONE_NUMBER);
            return false;
        } else {
            displayMessage(AppConstant.VALID_PHONE_NUMBER);
            return true;
        }
    }

    //validates street address
    private boolean validateStreetAddress() {
        mStreetAddress = mStreetAddressField.getCustomText();
        if (mStreetAddress.isEmpty()) {
            displayMessage(AppConstant.CLEAR_STREET_NAME);
            return true;
        } else if (isStreetSelected) {
            displayMessage(AppConstant.VALID_STREET_NAME);
            return true;
        } else {
            mStreetAddressField.setCustomText("");
            displayMessage(AppConstant.INVALID_STREET_NAME);
            return false;
        }
    }

    // validates vehicle make
    private boolean validateVehicleMake() {
        mVehicleMake = mVehicleMakeField.getCustomText();
        String vehicleMake = mVehicleMake.trim();
        if (vehicleMake.isEmpty()) {
            displayMessage(AppConstant.CLEAR_VEHICLE_MAKE);
            return true;
        } else if (isVehicleMakeSelected) {
            displayMessage(AppConstant.VALID_VEHICLE_MAKE);
            return true;
        } else {
            mVehicleMakeField.setCustomText("");
            mVehicleModelField.setCustomText("");
            displayMessage(AppConstant.INVALID_VEHICLE_MAKE);
            return false;
        }
    }

    // validates vehicle model
    private boolean validateVehicleModel() {
        mVehicleModel = mVehicleModelField.getCustomText();
        String vehicleModel = mVehicleModel.trim();
        if (vehicleModel.isEmpty()) {
            displayMessage(AppConstant.CLEAR_VEHICLE_MODEL);
            return true;
        } else if (isVehicleModelSelected) {
            displayMessage(AppConstant.VALID_VEHICLE_MODEL);
            return true;
        } else {
            mVehicleModelField.setCustomText("");
            displayMessage(AppConstant.INVALID_VEHICLE_MODEL);
            return false;
        }
    }

    // validates vehicle year
    private boolean validateVehicleYear() {
        mVehicleYear = mVehicleYearField.getCustomText();
        String vehicleYear = mVehicleYear.trim();
        if (vehicleYear.isEmpty()) {
            displayMessage(AppConstant.CLEAR_VEHICLE_YEAR);
            return true;
        } else if (isVehicleYearSelected) {
            displayMessage(AppConstant.VALID_VEHICLE_YEAR);
            return true;
        } else {
            mVehicleYearField.setCustomText("");
            displayMessage(AppConstant.INVALID_VEHICLE_YEAR);
            return false;
        }
    }

    // initiates validation of all fields and check if all fields are valid or not
    private void validateAllFields() {
        validateEmail();
        areAllFieldsValid = validateEmail() & validateCreatePassword() & validateConfirmPassword() & validateFullName() & validatePhoneNumber() & validateStreetAddress() & validateVehicleMake() & validateVehicleModel() & validateVehicleYear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(Registration.this, Terms.class);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                this.finish();
                return true;
            default:
                logger.info("default statement for onOptionsItemSelected executed for onOptionsItemSelected for item id : " + item.getItemId());
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onOptionChanged(int tag, int option1, int option2, int option3) {
        switch (tag) {
            case AppConstant.VEHICLE_MAKE_PICKER_TAG:
                String vehicleMake = vehicleMakeArrayList.get(option1).toString();
                if (!vehicleMake.isEmpty()) {
                    mVehicleMakeField.setText(vehicleMake);
                    vehicleMakePicker.setSelectOptions(option1);
                    if (vehicleMakeIndex != option1) {
                        refinedVehicleModelArrayList = null;
                        mVehicleModelField.setText("");
                        mVehicleModelValidationImage.setVisibility(View.INVISIBLE);
                    }
                    isVehicleMakeSelected = true;
                    vehicleMakeIndex = option1;
                } else {
                    isVehicleMakeSelected = false;
                }
                break;
            case AppConstant.VEHICLE_MODEL_PICKER_TAG:
                String vehicleModel = refinedVehicleModelArrayList.get(option1).toString();
                mVehicleModelField.setText(vehicleModel);
                vehicleModelPicker.setSelectOptions(option1);
                isVehicleModelSelected = true;
                break;
            case AppConstant.VEHICLE_YEAR_PICKER_TAG:
                String vehicleYear = yearList.get(option1).toString();
                mVehicleYearField.setText(vehicleYear);
                vehicleYearPicker.setSelectOptions(option1);
                isVehicleYearSelected = true;
                break;
            default:
                logger.info("default statement for onOptionChanged executed with tag : " + tag);
                break;
        }
    }
}
