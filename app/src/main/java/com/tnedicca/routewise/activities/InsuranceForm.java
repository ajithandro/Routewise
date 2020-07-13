package com.tnedicca.routewise.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.tnedicca.routewise.views.CustomWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.jeesoft.widget.pickerview.CharacterPickerWindow;
import cn.jeesoft.widget.pickerview.OnOptionChangedListener;

/**
 * Created by Vishal on 31-01-2017.
 */

public class InsuranceForm extends AppCompatActivity implements View.OnClickListener, TextWatcher, View.OnFocusChangeListener, OnOptionChangedListener {

    ArrayList<String> volleyArray = new ArrayList<String>();
    private CustomTextView screenTitle;
    private ImageView backIcon;
    private ImageView infoIcon;
    private RouteWise mInstance;
    private FragmentView firstNameView;
    private FragmentView lastNameView;
    private FragmentView ageView;
    private FragmentView phoneNumberView;
    private FragmentView emailAddressView;
    private FragmentView garagingAddressView;
    private CustomTextView firstNameErrorMsg;
    private CustomEditTextView firstNameField;
    private ImageView firstNameValidationImage;
    private CustomTextView lastNameErrorMsg;
    private CustomEditTextView lastNameField;
    private ImageView lastNameValidationImage;
    private CustomTextView ageErrorMsg;
    private CustomEditTextView ageField;
    private ImageView ageValidationImage;
    private CustomTextView phoneNumberErrorMsg;
    private CustomEditTextView phoneNumberField;
    private ImageView phoneNumberValidationImage;
    private CustomTextView emailAddressErrorMsg;
    private CustomEditTextView emailAddressField;
    private ImageView emailAdderessValidationImage;
    private CustomTextView garagingAddressErrorMsg;
    private CustomEditTextView garagingAddressField;
    private ImageView garagingAddressValidationImage;
    private CustomButton cancelButton;
    private CustomButton submitButton;
    private String firstName;
    private String lastName;
    private String age;
    private String email;
    private String phoneNumber;
    private String garagingAddress;
    private boolean isAddressSelected;
    private boolean isAgeSelected;
    private SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;
    private CharacterPickerWindow agePicker;
    private ArrayList yearList;
    private int lineNumber;
    private ProgressDialog progressDialog;
    private boolean areAllFieldsValid;
    private boolean isFetchedBefore;
    private boolean hasAppliedBefore;
    private CustomWebView aggreement;
    private RouteLog logger;
    private ProgressBar mProgress;
    private Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.insurance_form);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(InsuranceForm.class);

        screenTitle = findViewById(R.id.action_bar_title);
        backIcon = findViewById(R.id.back_icon);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);
        menuLayout.setTag(AppConstant.BACK_ICON_TAG_INSURANCE);

        aggreement = findViewById(R.id.agreement_view);
        aggreement.setWebViewClient(new myWebClient());
        aggreement.getSettings().setJavaScriptEnabled(true);
        String text = getString(R.string.insurance_agreement);
        text = text.replace("®", "<sup><small>®</small></sup>");
        aggreement.setText(text);

        screenTitle.setCustomText(getString(R.string.title_insurance_quote));
        infoIcon.setVisibility(View.INVISIBLE);

        init();
    }

    //Initialization of variables
    private void init() {
        isAddressSelected = false;
        isAgeSelected = false;
        areAllFieldsValid = false;
        setYearAdapter();
        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        progressDialog = new ProgressDialog(InsuranceForm.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        agePicker = new CharacterPickerWindow(this, AppConstant.VEHICLE_YEAR_TAG);
        agePicker.setPicker(yearList);
        firstNameView = (FragmentView) getFragmentManager().findFragmentById(R.id.first_name_view);

        firstNameField = firstNameView.getView().findViewById(R.id.edit_text);
        firstNameErrorMsg = firstNameView.getView().findViewById(R.id.error_msg);
        firstNameValidationImage = firstNameView.getView().findViewById(R.id.validation_image);
        firstNameField.setHint(R.string.first_name);
        firstNameField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        firstNameField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        lastNameView = (FragmentView) getFragmentManager().findFragmentById(R.id.last_name_view);

        lastNameField = lastNameView.getView().findViewById(R.id.edit_text);
        lastNameErrorMsg = lastNameView.getView().findViewById(R.id.error_msg);
        lastNameValidationImage = lastNameView.getView().findViewById(R.id.validation_image);
        lastNameField.setHint(R.string.last_name);
        lastNameField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        lastNameField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        ageView = (FragmentView) getFragmentManager().findFragmentById(R.id.age_view);

        ageField = ageView.getView().findViewById(R.id.edit_text);
        ageErrorMsg = ageView.getView().findViewById(R.id.error_msg);
        ageValidationImage = ageView.getView().findViewById(R.id.validation_image);
        mProgress = findViewById(R.id.progress);

        ageField.setHint(R.string.age);
        ageField.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        ageField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mInstance.myLibrary.disableSoftInputFromAppearing(ageField);
        phoneNumberView = (FragmentView) getFragmentManager().findFragmentById(R.id.phone_number_view);

        phoneNumberField = phoneNumberView.getView().findViewById(R.id.edit_text);
        phoneNumberErrorMsg = phoneNumberView.getView().findViewById(R.id.error_msg);
        phoneNumberValidationImage = phoneNumberView.getView().findViewById(R.id.validation_image);
        phoneNumberField.setHint(R.string.phone_number);
        phoneNumberField.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneNumberField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        emailAddressView = (FragmentView) getFragmentManager().findFragmentById(R.id.email_address_view);

        emailAddressField = emailAddressView.getView().findViewById(R.id.edit_text);
        emailAddressErrorMsg = emailAddressView.getView().findViewById(R.id.error_msg);
        emailAdderessValidationImage = emailAddressView.getView().findViewById(R.id.validation_image);
        emailAddressField.setHint(R.string.email_address);
        emailAddressField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailAddressField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        garagingAddressView = (FragmentView) getFragmentManager().findFragmentById(R.id.garaging_address_view);

        garagingAddressField = garagingAddressView.getView().findViewById(R.id.edit_text);
        garagingAddressErrorMsg = garagingAddressView.getView().findViewById(R.id.error_msg);
        garagingAddressValidationImage = garagingAddressView.getView().findViewById(R.id.validation_image);
        garagingAddressField.setHint(R.string.garaging_address);
        garagingAddressField.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        garagingAddressField.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        cancelButton = findViewById(R.id.cancel_button);
        submitButton = findViewById(R.id.submit_button);
        ageField.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        submitButton.setOnClickListener(this);
        garagingAddressField.addTextChangedListener(this);
        garagingAddressField.setOnClickListener(this);
        firstNameField.setOnFocusChangeListener(this);
        lastNameField.setOnFocusChangeListener(this);
        ageField.setOnFocusChangeListener(this);
        phoneNumberField.setOnFocusChangeListener(this);
        emailAddressField.setOnFocusChangeListener(this);
        garagingAddressField.setOnFocusChangeListener(this);
        agePicker.setOnoptionsSelectListener(this);
        firstNameErrorMsg.setVisibility(View.INVISIBLE);
        firstNameValidationImage.setVisibility(View.INVISIBLE);
        lastNameErrorMsg.setVisibility(View.INVISIBLE);
        lastNameValidationImage.setVisibility(View.INVISIBLE);
        ageErrorMsg.setVisibility(View.INVISIBLE);
        ageValidationImage.setVisibility(View.INVISIBLE);
        phoneNumberErrorMsg.setVisibility(View.INVISIBLE);
        phoneNumberValidationImage.setVisibility(View.INVISIBLE);
        emailAddressErrorMsg.setVisibility(View.INVISIBLE);
        emailAdderessValidationImage.setVisibility(View.INVISIBLE);
        garagingAddressErrorMsg.setVisibility(View.INVISIBLE);
        garagingAddressValidationImage.setVisibility(View.INVISIBLE);
        setTags();
        isFetchedBefore = fetchDataFromPreference();
        if (!isFetchedBefore) {
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage(AppConstant.PROCESSING_MSG);
            progressDialog.show();
            int method = Request.Method.GET;
            String url = BuildConfig.REST_URL + AppConstant.INSURANCE_URL;
            if (mInstance.dataStatus) {
                makeJsonArryReq(url, method, AppConstant.INSURANCE_GET, AppConstant.QUERY_TYPE_ISURANCE_GET_DATA);
            } else {
                logger.info(getString(R.string.log_connecting_rest_api));
                progressDialog.dismiss();
                mInstance.myLibrary.DisplayToast(this, AppConstant.NO_INTERNET_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
            }
        }
        setFieldsValue();
    }

    public void setTags() {
        firstNameField.setTag(AppConstant.FIRST_NAME_FIELD_TAG);
        lastNameField.setTag(AppConstant.LAST_NAME_FIELD_TAG);
        ageField.setTag(AppConstant.AGE_FIELD_TAG);
        phoneNumberField.setTag(AppConstant.PHONE_NUMBER_FIELD_TAG);
        emailAddressField.setTag(AppConstant.EMAIL_ADDRESS_FIELD_TAG);
        garagingAddressField.setTag(AppConstant.GARAGING_ADDRESS_FIELD);
        cancelButton.setTag(AppConstant.CANCEL_BUTTON_TAG);
        submitButton.setTag(AppConstant.SUBMIT_BUTTON_TAG);
        agePicker.setTag(AppConstant.AGE_PICKER_TAG);
    }

    // setting year list to 1070 to next year
    private void setYearAdapter() {
        yearList = new ArrayList();
        yearList.add(" ");
        for (int i = 17; i < 150; i++) {
            yearList.add(Integer.toString(i));
        }
    }

    private boolean fetchDataFromPreference() {
        firstName = mPrefer.getString(AppConstant.INSURANCE_FIRST_NAME, "");
        lastName = mPrefer.getString(AppConstant.INSURANCE_LAST_NAME, "");
        age = mPrefer.getString(AppConstant.INSURANCE_AGE, "");
        phoneNumber = mPrefer.getString(AppConstant.INSURANCE_PHONE_NUMBER, "");
        email = mPrefer.getString(AppConstant.INSURANCE_EMAIL, "");
        garagingAddress = mPrefer.getString(AppConstant.INSURANCE_ADDRESS, "");
        return !firstName.isEmpty() || !lastName.isEmpty() || !age.isEmpty() || !phoneNumber.isEmpty() || !email.isEmpty() || !garagingAddress.isEmpty();
    }

    private void setFieldsValue() {
        firstNameField.setCustomText(firstName);
        lastNameField.setCustomText(lastName);
        ageField.setCustomText(age);
        phoneNumberField.setCustomText(phoneNumber);
        emailAddressField.setCustomText(email);
        garagingAddressField.setCustomText(garagingAddress);
        isAddressSelected = true;
        isAgeSelected = true;
        mInstance.myLibrary.getIndex(age, yearList, agePicker);
        firstNameField.requestFocus();
        mInstance.myLibrary.hideKeyBoard(this.getCurrentFocus(), getApplicationContext());
    }

    private void saveFieldsInPreference() {
        edit = mPrefer.edit();
        edit.putString(AppConstant.INSURANCE_FIRST_NAME, firstName);
        edit.putString(AppConstant.INSURANCE_LAST_NAME, lastName);
        edit.putString(AppConstant.INSURANCE_AGE, age);
        edit.putString(AppConstant.INSURANCE_PHONE_NUMBER, phoneNumber);
        edit.putString(AppConstant.INSURANCE_EMAIL, email);
        edit.putString(AppConstant.INSURANCE_ADDRESS, garagingAddress);
        edit.commit();
    }

    // displayes various messages post validation
    private void displayMessage(String messageType) {
        switch (messageType) {
            case AppConstant.BLANK_FIRST_NAME:
                mInstance.myLibrary.setMessageOnValidationError(firstNameErrorMsg, firstNameValidationImage, getString(R.string.blank_first_name_msg));
                break;
            case AppConstant.INVALID_FIRST_NAME:
                mInstance.myLibrary.setMessageOnValidationError(firstNameErrorMsg, firstNameValidationImage, getString(R.string.invalid_first_name_msg));
                break;
            case AppConstant.VALID_FIRST_NAME:
                mInstance.myLibrary.setMessageOnValidationPass(firstNameErrorMsg, firstNameValidationImage);
                break;
            case AppConstant.CLEAR_FIRST_NAME:
                mInstance.myLibrary.clearMessage(firstNameErrorMsg, firstNameValidationImage);
                break;
            case AppConstant.BLANK_LAST_NAME:
                mInstance.myLibrary.setMessageOnValidationError(lastNameErrorMsg, lastNameValidationImage, getString(R.string.blank_last_name_msg));
                break;
            case AppConstant.INVALID_LAST_NAME:
                mInstance.myLibrary.setMessageOnValidationError(lastNameErrorMsg, lastNameValidationImage, getString(R.string.invalid_last_name_msg));
                break;
            case AppConstant.VALID_LAST_NAME:
                mInstance.myLibrary.setMessageOnValidationPass(lastNameErrorMsg, lastNameValidationImage);
                break;
            case AppConstant.CLEAR_LAST_NAME:
                mInstance.myLibrary.clearMessage(lastNameErrorMsg, lastNameValidationImage);
                break;
            case AppConstant.BLANK_AGE:
                mInstance.myLibrary.setMessageOnValidationError(ageErrorMsg, ageValidationImage, getString(R.string.blank_age_msg));
                break;
            case AppConstant.INVALID_AGE:
                mInstance.myLibrary.setMessageOnValidationError(ageErrorMsg, ageValidationImage, getString(R.string.invalid_age_msg));
                break;
            case AppConstant.VALID_AGE:
                mInstance.myLibrary.setMessageOnValidationPass(ageErrorMsg, ageValidationImage);
                break;
            case AppConstant.CLEAR_AGE:
                mInstance.myLibrary.clearMessage(ageErrorMsg, ageValidationImage);
                break;
            case AppConstant.BLANK_PHONE_NO:
                mInstance.myLibrary.setMessageOnValidationError(phoneNumberErrorMsg, phoneNumberValidationImage, getString(R.string.blank_phone_no_msg));
                break;
            case AppConstant.INVALID_PHONE_NO:
                mInstance.myLibrary.setMessageOnValidationError(phoneNumberErrorMsg, phoneNumberValidationImage, getString(R.string.invalid_phone_no_msg));
                break;
            case AppConstant.VALID_PHONE_NO:
                mInstance.myLibrary.setMessageOnValidationPass(phoneNumberErrorMsg, phoneNumberValidationImage);
                break;
            case AppConstant.CLEAR_PHONE_NO:
                mInstance.myLibrary.clearMessage(phoneNumberErrorMsg, phoneNumberValidationImage);
                break;
            case AppConstant.BLANK_EMAIL_ADDRESS:
                mInstance.myLibrary.setMessageOnValidationError(emailAddressErrorMsg, emailAdderessValidationImage, getString(R.string.blank_email_adrs_msg));
                break;
            case AppConstant.VALID_EMAIL_ADDRESS:
                mInstance.myLibrary.setMessageOnValidationPass(emailAddressErrorMsg, emailAdderessValidationImage);
                break;
            case AppConstant.INVALID_EMAIL_ADDRESS:
                mInstance.myLibrary.setMessageOnValidationError(emailAddressErrorMsg, emailAdderessValidationImage, getString(R.string.invalid_email_adrs_msg));
                break;
            case AppConstant.CLEAR_EMAIL_ADDRESS:
                mInstance.myLibrary.clearMessage(emailAddressErrorMsg, emailAdderessValidationImage);
                break;
            case AppConstant.BLANK_GARAGING_ADDRESS:
                mInstance.myLibrary.setMessageOnValidationError(garagingAddressErrorMsg, garagingAddressValidationImage, getString(R.string.blank_garaging_address_msg));
                break;
            case AppConstant.INVALID_GARAGING_ADDRESS:
                mInstance.myLibrary.setMessageOnValidationError(garagingAddressErrorMsg, garagingAddressValidationImage, getString(R.string.invalid_garaging_address_msg));
                break;
            case AppConstant.VALID_GARAGING_ADDRESS:
                mInstance.myLibrary.setMessageOnValidationPass(garagingAddressErrorMsg, garagingAddressValidationImage);
                break;
            case AppConstant.CLEAR_GARAGING_ADDRESS:
                mInstance.myLibrary.clearMessage(garagingAddressErrorMsg, garagingAddressValidationImage);
                break;
            default:
                logger.info("default statement for display message executed for message type : " + messageType);
                break;
        }
    }

    //method to validate name on focus change
    private void onFirstNameFocusChange(boolean hasFocus) {
        firstName = firstNameField.getCustomText();
        if (!hasFocus) {
            validateFirstName();
        } else {
            displayMessage(AppConstant.CLEAR_FIRST_NAME);
        }
    }

    private void onLastNameFocusChange(boolean hasFocus) {
        lastName = lastNameField.getCustomText();
        if (!hasFocus) {
            validateLastName();
        } else {
            displayMessage(AppConstant.CLEAR_LAST_NAME);
        }
    }

    private void onAgeFocusChange(View v, boolean hasFocus) {
        age = ageField.getCustomText();
        if (!hasFocus) {
            validateAge();
        } else {
            mInstance.myLibrary.hideKeyBoard(this.getCurrentFocus(), getApplicationContext());
            displayMessage(AppConstant.CLEAR_AGE);
            agePicker.showAtLocation(v, Gravity.BOTTOM, 0, 0);
        }
    }

    // method to validate phone number on focus change
    private void onPhoneNumberFocusChange(boolean hasFocus) {
        phoneNumber = phoneNumberField.getCustomText();
        if (!hasFocus) {
            validatePhoneNumber();
        } else {
            displayMessage(AppConstant.CLEAR_PHONE_NO);
        }
    }

    //Method to validate username on focus changed
    private void onEmailFocusChange(boolean hasFocus) {
        email = emailAddressField.getCustomText();
        if (!hasFocus) {
            validateEmail();
        } else {
            displayMessage(AppConstant.CLEAR_EMAIL_ADDRESS);
        }
    }

    // method to validate street address on focus change
    private void onGaragingAddressFocusChange(boolean hasFocus) {
        garagingAddress = garagingAddressField.getCustomText();
        if (!hasFocus) {
            validateGaragingAddress();
        } else {
            displayMessage(AppConstant.CLEAR_GARAGING_ADDRESS);
            Intent intent = new Intent(InsuranceForm.this, AddressSuggestion.class);
            startActivityForResult(intent, AppConstant.ADDRESS_REQUEST_CODE);
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        }
    }

    // validates first name
    private boolean validateFirstName() {
        firstName = firstNameField.getCustomText();
        if (firstName.isEmpty()) {
            displayMessage(AppConstant.BLANK_FIRST_NAME);
            return false;
        } else {
            if (!mInstance.myLibrary.isValidName(firstName)) {
                logger.info(getString(R.string.invalid_first_name_msg));
                displayMessage(AppConstant.INVALID_FIRST_NAME);
                return false;
            } else {
                displayMessage(AppConstant.VALID_FIRST_NAME);
                return true;
            }
        }
    }

    // validates last name
    private boolean validateLastName() {
        lastName = lastNameField.getCustomText();
        if (lastName.isEmpty()) {
            displayMessage(AppConstant.BLANK_LAST_NAME);
            return false;
        } else {
            if (!mInstance.myLibrary.isValidName(lastName)) {
                logger.info(getString(R.string.invalid_last_name_msg));
                displayMessage(AppConstant.INVALID_LAST_NAME);
                return false;
            } else {
                displayMessage(AppConstant.VALID_LAST_NAME);
                return true;
            }
        }
    }

    // validates vehicle year
    private boolean validateAge() {
        age = ageField.getCustomText();
        String trimmedAge = age.trim();
        if (trimmedAge.isEmpty()) {
            displayMessage(AppConstant.BLANK_AGE);
            return false;
        } else if (isAgeSelected) {
            displayMessage(AppConstant.VALID_AGE);
            return true;
        } else {
            ageField.setCustomText("");
            displayMessage(AppConstant.INVALID_AGE);
            return false;
        }
    }

    // validates phone number
    private boolean validatePhoneNumber() {
        phoneNumber = phoneNumberField.getCustomText();
        if (phoneNumber.isEmpty()) {
            displayMessage(AppConstant.BLANK_PHONE_NO);
            return false;
        } else if (!mInstance.myLibrary.isValidPhone(phoneNumber)) {
            logger.info(getString(R.string.invalid_phone_no_msg));
            displayMessage(AppConstant.INVALID_PHONE_NO);
            return false;
        } else {
            displayMessage(AppConstant.VALID_PHONE_NO);
            return true;
        }
    }

    // validates email
    private boolean validateEmail() {
        email = emailAddressField.getCustomText();
        if (!email.isEmpty()) {
            if (!mInstance.myLibrary.isValidMail(email)) {
                logger.info(getString(R.string.log_on_focus_changed) + " " + AppConstant.INVALID_EMAIL + " : " + email);
                displayMessage(AppConstant.INVALID_EMAIL_ADDRESS);
                return false;
            } else {
                displayMessage(AppConstant.VALID_EMAIL_ADDRESS);
                return true;
            }
        } else {
            displayMessage(AppConstant.BLANK_EMAIL_ADDRESS);
            return false;
        }
    }

    //validates street address
    private boolean validateGaragingAddress() {
        garagingAddress = garagingAddressField.getCustomText();
        if (garagingAddress.isEmpty()) {
            displayMessage(AppConstant.BLANK_GARAGING_ADDRESS);
            return false;
        } else if (isAddressSelected) {
            displayMessage(AppConstant.VALID_GARAGING_ADDRESS);
            return true;
        } else {
            garagingAddressField.setCustomText("");
            displayMessage(AppConstant.INVALID_GARAGING_ADDRESS);
            return false;
        }
    }

    private void validateAllFields() {
        validateEmail();
        areAllFieldsValid = validateFirstName() & validateLastName() & validateAge() & validatePhoneNumber() & validateEmail() & validateGaragingAddress();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    @Override
    public void onClick(View view) {
        mInstance.myLibrary.hideKeyBoard(this.getCurrentFocus(), getApplicationContext());
        Object obj = view.getTag();
        int tag = (int) obj;
        switch (tag) {
            case AppConstant.BACK_ICON_TAG_INSURANCE:
            case AppConstant.CANCEL_BUTTON_TAG:
                onBackPressed();
                break;
            case AppConstant.SUBMIT_BUTTON_TAG:
                validateAllFields();
                if (areAllFieldsValid) {
                    progressDialog.setIndeterminate(true);
                    progressDialog.setMessage(AppConstant.PROCESSING_MSG);
                    progressDialog.show();
                    int method = Request.Method.POST;
                    String url = BuildConfig.REST_URL + AppConstant.INSURANCE_URL;
                    if (mInstance.dataStatus) {
                        makeJsonArryReq(url, method, AppConstant.INSURANCE_POST, AppConstant.QUERY_TYPE_ISURANCE_SEND_DATA);
                    } else {
                        logger.info(getString(R.string.log_connecting_rest_api));
                        progressDialog.dismiss();
                        mInstance.myLibrary.DisplayToast(this, AppConstant.NO_INTERNET_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
                    }
                } else {
                    firstNameErrorMsg.requestFocus();
                }
                break;
            case AppConstant.GARAGING_ADDRESS_FIELD:
                Intent intent = new Intent(InsuranceForm.this, AddressSuggestion.class);
                startActivityForResult(intent, AppConstant.ADDRESS_REQUEST_CODE);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                break;
            case AppConstant.AGE_FIELD_TAG:
                agePicker.showAtLocation(view, Gravity.BOTTOM, 0, 0);
                break;
            default:
                logger.info("OnClick default executed for onClick for view tag : " + tag);
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.hashCode() == garagingAddressField.getText().hashCode()) {
            isAddressSelected = false;
        } else if (charSequence.hashCode() == ageField.getText().hashCode()) {
            isAgeSelected = false;
            agePicker.showAtLocation(ageField.getRootView(), Gravity.BOTTOM, 0, 0);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        Object obj = view.getTag();
        int tag = (int) obj;
        switch (tag) {
            case AppConstant.FIRST_NAME_FIELD_TAG:
                onFirstNameFocusChange(hasFocus);
                break;
            case AppConstant.LAST_NAME_FIELD_TAG:
                onLastNameFocusChange(hasFocus);
                break;
            case AppConstant.AGE_FIELD_TAG:
                onAgeFocusChange(view, hasFocus);
                break;
            case AppConstant.PHONE_NUMBER_FIELD_TAG:
                onPhoneNumberFocusChange(hasFocus);
                break;
            case AppConstant.EMAIL_ADDRESS_FIELD_TAG:
                onEmailFocusChange(hasFocus);
                break;
            case AppConstant.GARAGING_ADDRESS_FIELD:
                onGaragingAddressFocusChange(hasFocus);
                break;
            default:
                logger.error(getString(R.string.log_registration_onfocuschange_default) + tag);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppConstant.ADDRESS_REQUEST_CODE:
                garagingAddressField.setCustomText(data.getStringExtra("ADDRESS"));
                isAddressSelected = true;
                break;
            default:
                logger.info("onActivityResult default executed for onActivityResult for requestCode : " + requestCode);
                break;
        }
    }

    @Override
    public void onOptionChanged(int tag, int option1, int option2, int option3) {
        switch (tag) {
            case AppConstant.AGE_PICKER_TAG:
                String age = yearList.get(option1).toString();
                ageField.setText(age);
                agePicker.setSelectOptions(option1);
                isAgeSelected = true;
                break;
            default:
                logger.info("default statement for onOptionChanged executed with tag : " + tag);
                break;
        }
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

    //Volley Request
    private void makeJsonArryReq(String url, final int method, final String tag, final String queryType) {
        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                volleyArray.remove(tag);
//                logger.info( getString(R.string.received_response));
                response(tag, response, 200, queryType);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyArray.remove(tag);
                logger.info( getString(R.string.received_error));
                error(tag, error, queryType);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(AppConstant.REST_KEY_CONTENT, AppConstant.REST_KEY_CONTENT_TYPE);
                headers.put(AppConstant.REST_KEY_API, mInstance.myLibrary.apiKeyEncrypter());
                headers.put(AppConstant.REST_ACCESS_TOKEN, mPrefer.getString(AppConstant.TOKEN, ""));
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

    // configuring body for volley request
    private byte[] getBodyForSubmit(String queryType) {
        JSONObject finalToken = new JSONObject();
        JSONObject tempjson = new JSONObject();
        // body for submitting registration data
        if (queryType.equals(AppConstant.QUERY_TYPE_ISURANCE_SEND_DATA)) {
            try {
                String temp = Library.getOriginalString(email, true);
                tempjson.put(AppConstant.EMAIL, temp);
                tempjson.put(AppConstant.FIRST_NAME, firstName);
                tempjson.put(AppConstant.LAST_NAME, lastName);
                tempjson.put(AppConstant.ADDRESS, Library.getOriginalString(garagingAddress, true));
                tempjson.put(AppConstant.PHONE_NUMBER, phoneNumber);
                tempjson.put(AppConstant.AGE, age);
            } catch (Exception e) {
                lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                logger.error(getString(R.string.log_error_at) + " " + lineNumber, e);
            }
            String query = mInstance.myLibrary.dataEncrypt(tempjson.toString());
            try {
                finalToken.put("json", query);
            } catch (Exception e) {
                lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
                logger.error("Error at " + lineNumber, e);
            }
            return finalToken.toString().getBytes();
        } else {
            return null;
        }
    }

    //handles diferent response codes from Volley
    private void response(String check, JSONArray response, int code, String queryType) {
        mInstance.myLibrary.checkResponse(response, code, this);
        if (code == AppConstant.RESPONSE_BAD_REQUEST || code == AppConstant.RESPONSE_SERVER_ERROR) {
            progressDialog.dismiss();
            mInstance.myLibrary.DisplayToast(this, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        } else {
            // handling response for submitting insurance data
            if (queryType.equals(AppConstant.QUERY_TYPE_ISURANCE_SEND_DATA)) {
                JSONObject result = response.optJSONObject(0);
                int status = response.optInt(1);
                String message = result.optString("message");
                if (status == 200) {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.log_message) + " " + getString(R.string.insurance_details_submitted) + " " + email);
                } else if (status == 501) {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.updation_failed));
                } else {
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status);
                }
                if (check.equals(AppConstant.INSURANCE_POST)) {
                    displayServerMsg(result, status);
                }
                progressDialog.dismiss();
            }
            // handling response for insurance data request
            else if (queryType.equals(AppConstant.QUERY_TYPE_ISURANCE_GET_DATA)) {
                JSONObject result;
                int status;
                progressDialog.dismiss();
                System.out.println(response.toString());
                if (response.length() > 1) {
                    result = response.optJSONObject(0);
                    status = response.optInt(1);
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.log_message) + " " + getString(R.string.insurance_details_fetched));
                    firstName = result.optString(AppConstant.FIRST_NAME);
                    lastName = result.optString(AppConstant.LAST_NAME);
                    age = result.optString(AppConstant.AGE);
                    phoneNumber = result.optString(AppConstant.PHONE_NUMBER);
                    email = result.optString(AppConstant.EMAIL);
                    garagingAddress = result.optString(AppConstant.ADDRESS);
                    setFieldsValue();
                    saveFieldsInPreference();
                } else {
                    status = response.optInt(0);
                    logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + " " + getString(R.string.not_applied_before));
                }
            }
        }
    }

    // display server message
    private void displayServerMsg(JSONObject result, int code) {
        progressDialog.dismiss();
        switch (code) {
            case AppConstant.RESPONSE_SUCCESS:
            case AppConstant.RESPONSE_GOOGLE_ANONYMUS:
                mInstance.myLibrary.DisplayToast(this, AppConstant.THANK_YOU_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
                edit = mPrefer.edit();
                edit.remove(AppConstant.INSURANCE_FIRST_NAME);
                edit.remove(AppConstant.INSURANCE_LAST_NAME);
                edit.remove(AppConstant.INSURANCE_AGE);
                edit.remove(AppConstant.INSURANCE_PHONE_NUMBER);
                edit.remove(AppConstant.INSURANCE_EMAIL);
                edit.remove(AppConstant.INSURANCE_ADDRESS);
                edit.commit();
                submitButton.requestFocus();
                displayMainMenu();
                break;
            case AppConstant.RESPONSE_UPDATION_FAILED:
                progressDialog.dismiss();
                mInstance.myLibrary.DisplayToast(this, AppConstant.CONNECTION_TIMEOUT, Toast.LENGTH_SHORT, Gravity.CENTER);
            default:
                logger.info("default executed for displayServerMsg for response code : " + code);
                break;
        }
    }

    //handles error response from Volley
    private void error(String check, VolleyError error, String queryType) {
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
                progressDialog.dismiss();
                mInstance.myLibrary.DisplayToast(this, AppConstant.CONNECTION_TIMEOUT, Toast.LENGTH_SHORT, Gravity.CENTER);
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }

    @Override
    protected void onPause() {
        if(handler != null)
        handler.removeCallbacksAndMessages(null);
        super.onPause();
    }

    private void displayMainMenu() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(InsuranceForm.this, MainMenu.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
            }
        }, 1000);
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
