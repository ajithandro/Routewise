package com.tnedicca.routewise.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.hardware.Sensor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.kyleduo.switchbutton.SwitchButton;
import com.tnedicca.routewise.BuildConfig;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.Library;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.fragments.FragmentView;
import com.tnedicca.routewise.receivers.SensorsRecivers;
import com.tnedicca.routewise.views.CustomButton;
import com.tnedicca.routewise.views.CustomEditTextView;
import com.tnedicca.routewise.views.CustomTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by Vishal on 30-12-2016.
 */

public class MainActivity extends Activity implements View.OnClickListener, View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener, Runnable {

    public SharedPreferences mPrefer;
    ArrayList<String> volleyArray = new ArrayList<>();
    private RouteWise mInstance;
    private CustomEditTextView mUserName;
    private CustomEditTextView mPassword;
    private CustomButton mlogin;
    private CustomTextView mForgotPassword;
    private CustomTextView mRegister;
    private SwitchButton mKeepMeLogedIn;
    private CustomTextView mPrivacyPolicy;
    private CustomTextView mLoginError;
    private ImageView mUserValidationImage;
    private ImageView mPasswordValidationImage;
    private int lineNumber;
    private String token;
    private boolean keepMeLoggedIn = true;
    private SharedPreferences.Editor edit;
    private ProgressDialog progressDialog;
    private FragmentView mEmailView;
    private FragmentView mPasswordView;
    private CustomTextView mPasswordError;
    private String mEmail;
    private String mPwd;
    private String activationCode;
    private String mCurrentUser;
    private RouteLog logger;
    private boolean quit;
    private Handler handler1;
    private Handler handler2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);

        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(MainActivity.class);

        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        keepMeLoggedIn = mPrefer.getBoolean(AppConstant.KEEP_ME_LOGGED_IN, true);

        Bundle intent = getIntent().getExtras();
        if (intent != null) {
            quit = intent.getBoolean(AppConstant.QUIT_APP);
            keepMeLoggedIn = intent.getBoolean(AppConstant.KEEP_ME_LOGGED_IN, keepMeLoggedIn);
        }

        if (quit) {
            finish();
            edit = mPrefer.edit();
            edit.remove(AppConstant.KEEP_ME_LOGGED_IN);
            edit.apply();
        } else
            init();
    }

    private void init() {
        progressDialog = new ProgressDialog(this);

        mCurrentUser = mPrefer.getString(AppConstant.USER, null);
        mlogin = findViewById(R.id.login_button);
        mForgotPassword = findViewById(R.id.forgot_password);
        mRegister = findViewById(R.id.register);
        mKeepMeLogedIn = findViewById(R.id.keep_me_loged_in_switch);
        mKeepMeLogedIn.setOnCheckedChangeListener(this);
        mPrivacyPolicy = findViewById(R.id.privacy_policy);
        mEmailView = (FragmentView) getFragmentManager().findFragmentById(R.id.email_view);
        mPasswordView = (FragmentView) getFragmentManager().findFragmentById(R.id.password_view);
        mUserName = mEmailView.getView().findViewById(R.id.edit_text);
        mUserName.setTag(AppConstant.USERNAME_TAG);
        mPassword = mPasswordView.getView().findViewById(R.id.edit_text);
        mPassword.setTag(AppConstant.PASSWORD_TAG);
        mLoginError = mEmailView.getView().findViewById(R.id.error_msg);
        mPasswordError = mPasswordView.getView().findViewById(R.id.error_msg);
        mPasswordError.setVisibility(View.INVISIBLE);
        mUserValidationImage = mEmailView.getView().findViewById(R.id.validation_image);
        mPasswordValidationImage = mPasswordView.getView().findViewById(R.id.validation_image);
        mUserName.setFocusable(true);
        mUserName.setFocusableInTouchMode(true);
        mUserName.requestFocus();
        mUserName.setHint(R.string.email);
        mPassword.setHint(R.string.password);
        mUserName.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        mUserName.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        mPassword.setImeOptions(EditorInfo.IME_ACTION_GO);
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPassword.setCustomFont(MainActivity.this, getString(R.string.font_malgun_semi_light));
        mLoginError.setVisibility(View.INVISIBLE);
        mUserValidationImage.setVisibility(View.INVISIBLE);
        mPasswordError.setVisibility(View.INVISIBLE);
        mPasswordValidationImage.setVisibility(View.INVISIBLE);
        mPrivacyPolicy.setOnClickListener(this);
        mForgotPassword.setOnClickListener(this);
        mlogin.setOnClickListener(this);
        mRegister.setOnClickListener(this);
        mUserName.setOnFocusChangeListener(this);
        mPassword.setOnFocusChangeListener(this);

        if (!BuildConfig.RELEASE) {
            mUserName.setCustomText("ram.achu5@gmail.com");
            mPassword.setCustomText("Achuthan-90");
//            mUserName.setCustomText("testuser1@tnedicca.com");
//            mPassword.setCustomText("Tnedicca$123");
//            mForgotPassword.performClick();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    //Volley Request
    private void makeJsonArryReq(String url, final int method, final String tag, final JSONObject data) {
        JsonArrayRequest req = new JsonArrayRequest(url, response -> {
            volleyArray.remove(tag);
//            logger.info(getString(R.string.received_response));
            response(tag, response, 200);
        }, error -> {
            volleyArray.remove(tag);
            logger.info(getString(R.string.received_error));
            error(tag, error);
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(AppConstant.REST_KEY_CONTENT, AppConstant.REST_KEY_CONTENT_TYPE);
                headers.put(AppConstant.REST_KEY_API, mInstance.myLibrary.apiKeyEncrypter());
                if (tag.equals(AppConstant.SUBSCRIBE_AGAIN))
                    headers.put(AppConstant.REST_ACCESS_TOKEN, token);
                return headers;
            }

            @Override
            public byte[] getBody() {
                JSONObject finalToken = new JSONObject();
                JSONObject tempjson = new JSONObject();
                try {
                    if (tag.equals(AppConstant.SUBSCRIBE_AGAIN)) {
                    } else if (tag.equals(AppConstant.LOGIN)) {
                        tempjson = data;
                    }
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
    private void response(String check, JSONArray response, int code) {
        progressDialog.dismiss();
        mInstance.myLibrary.checkResponse(response, code, this);
        if (code == AppConstant.RESPONSE_BAD_REQUEST || code == AppConstant.RESPONSE_SERVER_ERROR) {
            mInstance.myLibrary.DisplayToast(this, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
        } else if (code == AppConstant.RESPONSE_DEVICE_CHANGED) {
            if (check.equals(AppConstant.LOGIN)) {
                mInstance.myLibrary.alertBox(this, "Register Device", getString(R.string.new_device_message), "AGREE", "DISAGREE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logger.info("Device Changed");
                        buildLoginRequest(true);
                    }
                });
            }
        } else {
            JSONObject result = response.optJSONObject(0);
            int status = response.optInt(1);
            String message = result.optString("message");
            if (status == 200) {
                logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.log_message) + " " + getString(R.string.success_message) + " " + mEmail);
            } else if (status == 205) {
                logger.info(getString(R.string.server_response) + getString(R.string.response_code) + status + getString(R.string.log_message_for_incorrect_password) + " " + mEmail);
            } else {
                logger.info(getString(R.string.server_response) + getString(R.string.response_code) + getString(R.string.log_message) + " " + message + " for Email : " + mEmail);
            }
            if (check.equals(AppConstant.LOGIN)) {
                displayServerMsg(result, status);
            } else if (check.equals(AppConstant.SUBSCRIBE_AGAIN)) {
                moveToDashboard();
            }
        }
    }

    //handles error response from Volley
    private void error(String check, VolleyError error) {
        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                int statusCode = error.networkResponse.statusCode;
                byte[] s = error.networkResponse.data;
                String message = new String(s);
                logger.info("message : " + message);
                JSONArray response = new JSONArray(message);
                response(check, response, statusCode);
            } else {
                mInstance.myLibrary.DisplayToast(this, AppConstant.CONNECTION_TIMEOUT, Toast.LENGTH_SHORT, Gravity.CENTER);
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
    }

    private void subscribeAgain() {
        String url = BuildConfig.REST_URL + AppConstant.SUBSCRIBE_URL;
        int method = Request.Method.POST;
        String tag = AppConstant.SUBSCRIBE_AGAIN;
        makeJsonArryReq(url, method, tag, null);
    }

    private void buildLoginRequest(boolean change) {
        JSONObject tempjson = new JSONObject();
        try {
            String device_id = mInstance.myLibrary.getUniqueId(MainActivity.this);
            String temp = Library.getOriginalString(mEmail, true);

            tempjson.put(AppConstant.USER_EMAIL, temp);
            tempjson.put(AppConstant.DEVICE_ID, device_id);
            tempjson.put(AppConstant.CHANGE_DEVICE_ID, change);
            if (mPwd != null)
                tempjson.put(AppConstant.PASSWORD, mInstance.myLibrary.passwordEncrypter(mPwd));
            tempjson.put(AppConstant.LOGIN_TYPE, true);
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error(getString(R.string.log_error_at) + " " + lineNumber, e);
        }
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(AppConstant.LOADING);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String url = BuildConfig.REST_URL + AppConstant.LOGIN_URL;
        int method = Request.Method.POST;
        String tag = AppConstant.LOGIN;
        makeJsonArryReq(url, method, tag, tempjson);
    }

    private void subscribe(JSONObject result) {
        token = result.optString(AppConstant.TOKEN);
        double temp1 = result.optDouble(AppConstant.DATE);
        long tempDate = (long) temp1 * 1000;
        Date date = new Date(tempDate);
        String time = mInstance.unsubscribeDateFormat.format(date);

        logger.info("Subscribe Initiated");
        String message = getString(R.string.subscribe_message);
        message = message.replace("$localDate", time);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.subscribe_title));
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                subscribeAgain();
            }
        });

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton(getString(R.string.no), null);

        // Showing Alert Message
        alertDialog.show();
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
        switch (v.getId()) {
            case R.id.login_button:
                mInstance.myLibrary.hideKeyBoard(this.getCurrentFocus(), getApplicationContext());
                login();
                break;
            case R.id.register:
                if (mInstance.dataStatus) {
                    keepMeLoggedIn = mKeepMeLogedIn.isChecked();
                    Intent intent = new Intent(MainActivity.this, Terms.class);
                    intent.putExtra(AppConstant.KEEP_ME_LOGGED_IN, keepMeLoggedIn);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                } else {
                    mInstance.myLibrary.DisplayToast(this, AppConstant.NO_INTERNET_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
                }
                break;
            case R.id.forgot_password:
                forgotPassword();
                break;
            case R.id.privacy_policy:
                Intent privacyPolicyIntent = new Intent(MainActivity.this, PrivacyPolicy.class);
                startActivity(privacyPolicyIntent);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                break;
            default:
                logger.info("default statement executed for onClick for view id : " + v.getId());
                break;
        }
    }

    //validates input fields and initiates login
    private void login() {
        mEmail = mUserName.getCustomText();
        mPwd = mPassword.getCustomText();
        keepMeLoggedIn = mKeepMeLogedIn.isChecked();
        boolean isEmailEmpty;
        boolean isPasswordEmpty;
        boolean isEmailValid = false;
        boolean isPasswordValid = false;
        if (mEmail.isEmpty()) {
            isEmailEmpty = true;
        } else {
            isEmailEmpty = false;
            if (mInstance.myLibrary.isValidMail(mEmail)) {
                isEmailValid = true;
                displayMessage(AppConstant.VALID_EMAIL);
            } else {
                isEmailValid = false;
                logger.info(getString(R.string.log_on_login_button_clicked) + " " + AppConstant.INVALID_EMAIL + " : " + mEmail);
                displayMessage(AppConstant.INVALID_EMAIL);
            }
        }
        if (!mPwd.isEmpty()) {
            isPasswordEmpty = false;
            if (!mInstance.myLibrary.isValidPasswordFormat(mPwd)) {
                isPasswordValid = false;
                logger.info(getString(R.string.log_on_login_button_clicked) + " " + AppConstant.INVALID_PASSWORD + " format");
                displayMessage(AppConstant.INVALID_PASSWORD);
            } else {
                isPasswordValid = true;
                displayMessage(AppConstant.VALID_PASSWORD);
            }
        } else {
            isPasswordEmpty = true;
        }
        if (isEmailEmpty) {
            displayMessage(AppConstant.BLANK_EMAIL);
        }
        if (isPasswordEmpty) {
            displayMessage(AppConstant.BLANK_PASSWORD);
        }
        if (!isEmailEmpty && isEmailValid && !isPasswordEmpty && isPasswordValid) {
            if (mInstance.dataStatus) {
                buildLoginRequest(false);
                logger.info(getString(R.string.log_connecting_rest_api));
            } else {
                mInstance.myLibrary.DisplayToast(this, AppConstant.NO_INTERNET_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
            }
        }
    }

    private void forgotPassword() {
        boolean send = false;
        boolean email_type = true;
        mEmail = mUserName.getCustomText();
        if (!mEmail.isEmpty()) {
            if (mInstance.myLibrary.isValidMail(mEmail)) {
                email_type = true;
                send = true;
            } else {
                if (mInstance.myLibrary.isValidPhone(mEmail)) {
                    email_type = false;
                    send = true;
                }
            }
        }

        if (send) {
            keepMeLoggedIn = mKeepMeLogedIn.isChecked();
            Intent forgot = new Intent(MainActivity.this, ForgotPassword.class);
            if (email_type)
                forgot.putExtra(AppConstant.USER_EMAIL, mEmail);
            else
                forgot.putExtra(AppConstant.PHONE_NUMBER, mEmail);
            forgot.putExtra(AppConstant.RESET_SCREEN, true);
            forgot.putExtra(AppConstant.EMAIL_TYPE, false);
            forgot.putExtra(AppConstant.KEEP_ME_LOGGED_IN, keepMeLoggedIn);
            startActivity(forgot);
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        } else {
            setMessageOnValidationError(mLoginError, mUserValidationImage, getString(R.string.blank_email_phone_msg));
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Object obj = v.getTag();
        int tag = (int) obj;
        switch (tag) {
            case AppConstant.USERNAME_TAG:
                onUserNameFocusChange(hasFocus);
                break;
            case AppConstant.PASSWORD_TAG:
                onPasswordFocusChange(hasFocus);
                break;
            default:
                logger.info("default statement executed for onFocusChange for view tag : " + tag);
                break;
        }
    }

    //Method to validate username on focus changed
    private void onUserNameFocusChange(boolean hasFocus) {
        String user = mUserName.getCustomText();
        if (!hasFocus) {
            if (!user.isEmpty()) {
                if (!mInstance.myLibrary.isValidMail(user)) {
                    logger.info(getString(R.string.log_on_focus_changed) + " " + AppConstant.INVALID_EMAIL + " : " + mEmail);
                    displayMessage(AppConstant.INVALID_EMAIL);
                } else {
                    displayMessage(AppConstant.VALID_EMAIL);
                }
            } else {
                displayMessage(AppConstant.BLANK_EMAIL);
            }
        } else {
            displayMessage(AppConstant.CLEAR_EMAIL_MESSAGE);
        }
    }

    //Method to validate password on focus changed
    private void onPasswordFocusChange(boolean hasFocus) {
        String password = mPassword.getCustomText();
        if (!hasFocus) {
            if (!password.isEmpty()) {
                if (!mInstance.myLibrary.isValidPasswordFormat(password)) {
                    logger.info(getString(R.string.log_on_focus_changed) + " " + AppConstant.INVALID_PASSWORD + " format");
                    displayMessage(AppConstant.INVALID_PASSWORD);
                } else {
                    displayMessage(AppConstant.VALID_PASSWORD);
                }
            } else {
                displayMessage(AppConstant.BLANK_PASSWORD);
            }
        } else {
            displayMessage(AppConstant.CLEAR_PASSSWORD_MESSAGE);
        }
    }

    //handles display of various error messages
    private void displayMessage(String messageType) {
        if (messageType.equals(AppConstant.BLANK_EMAIL)) {
            setMessageOnValidationError(mLoginError, mUserValidationImage, getString(R.string.blank_email_address_msg));
        } else if (messageType.equals(AppConstant.BLANK_PASSWORD)) {
            setMessageOnValidationError(mPasswordError, mPasswordValidationImage, getString(R.string.blank_password_msg));
        } else if (messageType.equals(AppConstant.INVALID_EMAIL)) {
            setMessageOnValidationError(mLoginError, mUserValidationImage, getString(R.string.invalid_email_address_msg));
        } else if (messageType.equals(AppConstant.INVALID_PASSWORD)) {
            setMessageOnValidationError(mPasswordError, mPasswordValidationImage, getString(R.string.invalid_password_msg));
        } else if (messageType.equals(AppConstant.VALID_EMAIL)) {
            setMessageOnValidationPass(mLoginError, mUserValidationImage);
        } else if (messageType.equals(AppConstant.VALID_PASSWORD)) {
            setMessageOnValidationPass(mPasswordError, mPasswordValidationImage);
        } else if (messageType.equals(AppConstant.CLEAR_EMAIL_MESSAGE)) {
            clearMessage(mLoginError, mUserValidationImage);
        } else if (messageType.equals(AppConstant.CLEAR_PASSSWORD_MESSAGE)) {
            clearMessage(mPasswordError, mPasswordValidationImage);
        }
    }

    private void displayApproval(JSONObject result) {
        token = result.optString(AppConstant.TOKEN);
        activationCode = result.optString(AppConstant.ACTIVATION_CODE);
        handler1 = new Handler();
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoginError.requestFocus();
                Intent intent = new Intent(MainActivity.this, Approval.class);
                intent.putExtra(AppConstant.TOKEN, token);
                intent.putExtra(AppConstant.ACTIVATION_CODE, activationCode);
                intent.putExtra(AppConstant.USER, mEmail);
                intent.putExtra(AppConstant.MAIL_TYPE, AppConstant.MAIL_ACTIVATION);
                intent.putExtra(AppConstant.KEEP_ME_LOGGED_IN, keepMeLoggedIn);
                intent.putExtra(AppConstant.RESET_SCREEN, false);
                intent.putExtra(AppConstant.RESET_ID, mEmail);
                intent.putExtra(AppConstant.LOGIN_TYPE, true);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        if (handler1 != null)
            handler1.removeCallbacksAndMessages(null);
        if (handler2 != null)
            handler2.removeCallbacksAndMessages(null);
        super.onPause();
    }

    //handle error message from server and displaying Dashboard for successful login
    private void displayServerMsg(JSONObject result, int code) {
        switch (code) {
            case AppConstant.RESPONSE_SUCCESS:
            case AppConstant.RESPONSE_GOOGLE_ANONYMUS:
                displayDashboard(result);
                break;
            case AppConstant.RESPONSE_USER_UNAVAILABLE:
                setMessageForResponse(mLoginError, getString(R.string.user_unavailable));
                break;
            case AppConstant.RESPONSE_LOGIN_FAILED:
                setMessageForResponse(mPasswordError, getString(R.string.wrong_password));
                break;
            case AppConstant.RESPONSE_ACTIVATION_PENDING:
                setMessageForResponse(mLoginError, getString(R.string.activation_pending));
                displayApproval(result);
                break;
            case AppConstant.RESPONSE_USER_UNSUBSCRIBED:
//                setMessageForResponse(mLoginError, getString(R.string.user_unsubscribed));
                subscribe(result);
                break;
            default:
                logger.info("default statement executed for displayServerMsg for code : " + code);
                progressDialog.dismiss();
                break;
        }
    }

    //method to display dashboard screen
    private void displayDashboard(JSONObject result) {
        token = result.optString(AppConstant.ACCESS_TOKEN);
        mEmail = result.optString(AppConstant.USER_EMAIL);
        if (token != null || token != "") {
            moveToDashboard();
        } else {
            mPasswordError.setText(R.string.wrong_password);
            mPasswordError.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstant.RESPONSE_6_SPLASH)
            mPassword.post(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppConstant.RESPONSE_6_LOCATION) {
            ArrayList<Integer> show = new ArrayList();
            for (int i = 0; i < permissions.length; i++) {
                String temp = permissions[i];
                if (temp.equals(Manifest.permission.ACCESS_COARSE_LOCATION))
                    show.add(i);
                else if (temp.equals(Manifest.permission.ACCESS_FINE_LOCATION))
                    show.add(i);

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (temp.equals(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                        show.add(i);
                    else if (temp.equals(Manifest.permission.ACTIVITY_RECOGNITION))
                        show.add(i);
                }
            }

            if (show.size() > 0) {
                boolean allGranted = true;
//                boolean denied = false;
                for (int i = 0; i < show.size(); i++) {
                    boolean granted = grantResults[show.get(i)] == PackageManager.PERMISSION_GRANTED;
                    if (!granted) {
                        allGranted = false;
                    }
                }

                if (allGranted) {
                    mPassword.post(this);
                } else {
                    Intent permi = new Intent(this, DummyActivity.class);
                    permi.putExtra(AppConstant.INTENT_PERMISSION, true);
                    startActivityForResult(permi, AppConstant.RESPONSE_6_SPLASH);
                    overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                }
            } else if (show.size() == 0)
                mPassword.post(this);
        }
    }

    private void checkPermission() {
        String[] permissions;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION};
        } else {
            permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        }
        if (mInstance.myLibrary.check6Compact(this, permissions, AppConstant.RESPONSE_6_LOCATION, R.string.permission_loc)) {
            mPassword.post(this);
        }
    }

    private void moveToDashboard() {
        keepMeLoggedIn = mKeepMeLogedIn.isChecked();
        mInstance.subscribed = true;
        mInstance.tracking_enabled = true;
        mInstance.auto_enable = false;
        edit = mPrefer.edit();
        edit.putString(AppConstant.TOKEN, token);
        edit.putString(AppConstant.USER, mEmail);
        edit.putBoolean(AppConstant.KEEP_ME_LOGGED_IN, keepMeLoggedIn);
        edit.putBoolean(AppConstant.SUBSCRIBED, mInstance.subscribed);
        edit.putBoolean(AppConstant.TRACKING_ENABLED, mInstance.tracking_enabled);
        edit.putBoolean(AppConstant.AUTO_TRACKING, mInstance.auto_enable);
        edit.commit();
        mInstance.myLibrary.getDBAdapter();
        logger.info(getString(R.string.log_keep_me_logged_in) + keepMeLoggedIn);
        logger.info(mEmail + " " + getString(R.string.log_logged_in));

        boolean send = false;
        if (mInstance.myLibrary.isgpsavailable(this)) {
            send = true;
        } else {
            mInstance.myLibrary.DisplayToast(this, "Please enable location services", Toast.LENGTH_LONG, Gravity.CENTER);
            mInstance.myLibrary.showGPSDisabledAlertToUser(MainActivity.this);
        }
        if (send) {
            AutoLogin();
        }
    }
    private void AutoLogin() {
        handler2 = new Handler();
        handler2.postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, MainMenu.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
        }, 1000);
    }

    //Method to set response error
    private void setMessageForResponse(CustomTextView textView, String message) {
        textView.setText(message);
        textView.setVisibility(View.VISIBLE);
        mUserValidationImage.setVisibility(View.INVISIBLE);
        mPasswordValidationImage.setVisibility(View.INVISIBLE);
    }

    //Method to set error message and cross mark on validation failed
    private void setMessageOnValidationError(CustomTextView textView, ImageView imageView, String message) {
        textView.setText(message);
        textView.setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.ic_erroricon);
        imageView.setVisibility(View.VISIBLE);
    }

    //Method to set tick mark on validation pass
    private void setMessageOnValidationPass(CustomTextView textView, ImageView imageView) {
        imageView.setImageResource(R.drawable.ic_noerroricon);
        imageView.setVisibility(View.VISIBLE);
        textView.setVisibility(View.INVISIBLE);
    }

    //Method to clear error message of particular field
    private void clearMessage(CustomTextView textView, ImageView imageView) {
        textView.setVisibility(View.INVISIBLE);
        imageView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.keep_me_loged_in_switch:
                keepMeLoggedIn = isChecked;
                logger.info(getString(R.string.log_keep_me_logged_in_button_checked) + " " + isChecked);
                break;
            default:
                logger.info("default statement executed for onCheckedChanged for buttonview id : " + buttonView.getId());
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
                this.finish();
                return true;
            default:
                logger.info("default statement executed for onOptionsSelected for item id" + item.getItemId());
                break;
        }
        return true;
    }

    @Override
    public void run() {
        boolean update = mPrefer.getBoolean(AppConstant.UPDATE_APP, false);
        boolean mandate = mPrefer.getBoolean(AppConstant.UPDATE_APP_MANDATE, false);
        if (!update && !mandate) {
            checkAutoLogin();
        } else {
            mInstance.myLibrary.showUpdatePopup(update, mandate, this);
        }
    }

    public void checkAutoLogin() {
        boolean send = false;
        if (mInstance.myLibrary.isgpsavailable(MainActivity.this)) {
            send = true;
        } else {
            mInstance.myLibrary.DisplayToast(MainActivity.this, "Please enable location services", Toast.LENGTH_LONG, Gravity.CENTER);
            mInstance.myLibrary.showGPSDisabledAlertToUser(MainActivity.this);
        }
        boolean loggedIn = mPrefer.contains(AppConstant.KEEP_ME_LOGGED_IN);
        boolean goDashborad = false;
        if (loggedIn)
            goDashborad = keepMeLoggedIn;
        logger.info("loggedIn : " + loggedIn + "   goDashborad : " + goDashborad);

        if (send && goDashborad) {
            mInstance.myLibrary.DisplayToast(MainActivity.this, "Auto login progress", Toast.LENGTH_LONG, Gravity.BOTTOM);
            AutoLogin();
        }
    }

//    public void checkUpdate(boolean update, boolean mandate) {
//        if (update || mandate) {
//            mInstance.myLibrary.alertBox(this, "Update", getString(R.string.new_device_message), "UPDATE", "NOT NOW", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    checkAutoLogin();
//                }
//            });
//            mInstance.myLibrary.updateApp(false, false);
//        }
//    }

}