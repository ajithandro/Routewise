package com.tnedicca.routewise.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
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
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.views.CustomButton;
import com.tnedicca.routewise.views.CustomRadioButton;
import com.tnedicca.routewise.views.CustomTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Aachu on 30-01-2017.
 */
public class ForgotPassword extends Activity implements View.OnClickListener {

    public SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;
    private RouteWise mInstance;
    private String mCurrentUser;
    private String mCurrentPhone;
    private boolean mResetScreen;
    private boolean mEmailType;

    private CustomTextView screenTitle;
    private ImageView backIcon;
    private ImageView infoIcon;

    ArrayList<String> volleyArray = new ArrayList<String>();
    private CustomRadioButton mEmailRadio;
    private CustomRadioButton mPhoneRadio;
    private CustomTextView mEmail;
    private CustomTextView mPhone;
    private CustomButton mSubmit;
    private int lineNumber;
    private boolean keepMeLoggedIn;
    private String resetId;
    private boolean isEmail;
    private String mailType;
    private ProgressDialog progressDialog;
    private RouteLog logger;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(ForgotPassword.class);

        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        progressDialog = new ProgressDialog(this);

        Bundle intent = getIntent().getExtras();

        mCurrentUser = intent.getString(AppConstant.USER_EMAIL, "");
        mCurrentPhone = intent.getString(AppConstant.PHONE_NUMBER, "0");
        mResetScreen = intent.getBoolean(AppConstant.RESET_SCREEN, false);
        mEmailType = intent.getBoolean(AppConstant.EMAIL_TYPE, false);
        keepMeLoggedIn = intent.getBoolean(AppConstant.KEEP_ME_LOGGED_IN, false);

        getDetails();
        screenTitle = findViewById(R.id.action_bar_title);
        backIcon = findViewById(R.id.back_icon);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);

        screenTitle.setCustomText(getString(R.string.forgot));
        infoIcon.setVisibility(View.INVISIBLE);
//        backIcon.setOnClickListener(this);

        mEmail = findViewById(R.id.email_id);
        mPhone = findViewById(R.id.phone_number);
        mEmailRadio = findViewById(R.id.radio_email);
        mPhoneRadio = findViewById(R.id.radio_phone);
        mSubmit = findViewById(R.id.next_button);

        setValues();
        mEmailRadio.setOnClickListener(this);
        mPhoneRadio.setOnClickListener(this);

        mEmail.setAlpha(0.5f);
        mPhone.setAlpha(0.5f);
        mSubmit.setAlpha(0.5f);
//        if (!BuildConfig.RELEASE) {
//            mEmailRadio.setChecked(true);
//            checkRadioButtons(true);
//            mSubmit.performClick();
//        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    private void setValues() {
        String phone = mCurrentPhone.replaceAll("(\\d{2})(\\d+)(\\d{2})", "$1 xxxx $3");
        String email = mCurrentUser.replaceAll("([\\w\\W]{3})([\\w\\W]+)([\\w\\W]{5})", "$1 xxxx $3");
        if (phone.equals("0")) {
            phone = "xx xxxxxx xx";
            mPhoneRadio.setEnabled(false);
            mPhoneRadio.setOnClickListener(null);
        } else {
            mPhoneRadio.setEnabled(true);
            mPhoneRadio.setOnClickListener(this);
        }
        mEmail.setCustomText(email);
        mPhone.setCustomText(phone);
    }

    private void getDetails() {
        String email_id = "";
        boolean isEmail = false;
        if (mCurrentPhone == "0") {
            email_id = mCurrentUser;
            isEmail = true;
        } else {
            email_id = mCurrentPhone;
            isEmail = false;
        }
        JSONObject tempjson = new JSONObject();
        try {
            tempjson.put(AppConstant.EMAIL, email_id);
            tempjson.put(AppConstant.LOGIN_TYPE, isEmail);
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error(getString(R.string.log_error_at) + " " + lineNumber, e);
        }
        String id = mInstance.myLibrary.dataEncrypt(tempjson.toString());

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(AppConstant.LOADING);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String url = BuildConfig.REST_URL + AppConstant.FORGOT_URL;
        int method = Request.Method.GET;
        String tag = AppConstant.RESET;
        makeJsonArryReq(url, method, tag, id);
    }

    private void submit() {
        boolean emailRadio = mEmailRadio.isChecked();
        boolean phoneRadio = mPhoneRadio.isChecked();
        if (emailRadio) {
            resetId = mCurrentUser;
            isEmail = true;
        } else if (phoneRadio) {
            resetId = mCurrentPhone;
            isEmail = false;
        }

        if (mResetScreen) {
            mailType = AppConstant.MAIL_FORGOT;
        } else {
            if (mEmailType) {
                mailType = AppConstant.MAIL_REGISTER;
            } else {
                mailType = AppConstant.MAIL_ACTIVATION;
            }
        }

        JSONObject tempjson = new JSONObject();
        try {
            tempjson.put(AppConstant.EMAIL, resetId);
            tempjson.put(AppConstant.LOGIN_TYPE, isEmail);
            tempjson.put(AppConstant.MAIL_TYPE, mailType);
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error(getString(R.string.log_error_at) + " " + lineNumber, e);
        }
        String id = mInstance.myLibrary.dataEncrypt(tempjson.toString());

        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(AppConstant.LOADING);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String url = BuildConfig.REST_URL + AppConstant.FORGOT_PASSWORD_URL;
        int method = Request.Method.GET;
        String tag = AppConstant.FORGOT;
        makeJsonArryReq(url, method, tag, id);
    }

    private void checkRadioButtons(boolean email) {
        if (email) {
            mPhoneRadio.setChecked(false);
            mEmail.setAlpha(1f);
            mPhone.setAlpha(0.5f);
        } else {
            mEmailRadio.setChecked(false);
            mEmail.setAlpha(0.5f);
            mPhone.setAlpha(1f);
        }
        mSubmit.setAlpha(1f);
        mSubmit.setOnClickListener(this);
    }

    //Volley Request
    private void makeJsonArryReq(String url, final int method, final String tag, final String id) {
        JsonArrayRequest req = new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                volleyArray.remove(tag);
//                logger.info( getString(R.string.received_response));
                response(tag, response, 200);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyArray.remove(tag);
                logger.info( getString(R.string.received_error));
                error(tag, error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put(AppConstant.REST_KEY_CONTENT, AppConstant.REST_KEY_CONTENT_TYPE);
                headers.put(AppConstant.REST_KEY_API, mInstance.myLibrary.apiKeyEncrypter());
                if (tag.equals(AppConstant.RESET) || tag.equals(AppConstant.FORGOT))
                    headers.put(AppConstant.REST_ID, id);
                return headers;
            }

            @Override
            public byte[] getBody() {
                return null;
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
            displayLoginScreen();
        } else {
            JSONObject result = response.optJSONObject(0);
            int status = response.optInt(1);
            String message = result.optString("message");
            if (status == AppConstant.RESPONSE_USER_ALREADY_REGISTERED) {
                mInstance.myLibrary.DisplayToast(this, "User Not Registered", Toast.LENGTH_SHORT, Gravity.CENTER);
                displayLoginScreen();
            } else if (status == AppConstant.RESPONSE_PHONE_NUMBER_ALREADY_REGISTERED) {
                mInstance.myLibrary.DisplayToast(this, "Phone Number Not Registered", Toast.LENGTH_SHORT, Gravity.CENTER);
                displayLoginScreen();
            } else if (status == AppConstant.RESPONSE_SUCCESS || status == AppConstant.RESPONSE_GOOGLE_ANONYMUS) {
                if (check.equals(AppConstant.RESET)) {
                    mCurrentUser = result.optString(AppConstant.EMAIL);
                    mCurrentPhone = result.optString(AppConstant.PHONE_NUMBER);
                    setValues();
                } else if (check.equals(AppConstant.FORGOT)) {
                    mCurrentUser = result.optString(AppConstant.EMAIL);
                    String activationCode = result.optString(AppConstant.ACTIVATION_CODE);

                    Intent intent = new Intent(this, Approval.class);
                    intent.putExtra(AppConstant.ACTIVATION_CODE, activationCode);
                    intent.putExtra(AppConstant.USER, mCurrentUser);
                    intent.putExtra(AppConstant.KEEP_ME_LOGGED_IN, keepMeLoggedIn);
                    intent.putExtra(AppConstant.RESET_SCREEN, mResetScreen);
                    intent.putExtra(AppConstant.RESET_SETTINGS, false);
                    intent.putExtra(AppConstant.RESET_ID, resetId);
                    intent.putExtra(AppConstant.LOGIN_TYPE, isEmail);
                    intent.putExtra(AppConstant.MAIL_TYPE, mailType);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.anim_slide_in_left, R.anim.anim_slide_out_left);
                }
            }
        }
    }

    //handles error response from Volley
    private void error(String check, VolleyError error) {
        progressDialog.dismiss();
        try {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                int statusCode = error.networkResponse.statusCode;
                byte[] s = error.networkResponse.data;
                String message = new String(s);
                logger.info("message : " + message);
                JSONArray response = new JSONArray(message);
                response(check, response, statusCode);
            } else {
                mInstance.myLibrary.DisplayToast(this, AppConstant.FAILED_TOAST_MSG, Toast.LENGTH_SHORT, Gravity.CENTER);
                displayLoginScreen();
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

    private void displayLoginScreen() {
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        }, 1000);
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
        int id = v.getId();
        switch (id) {
            case R.id.back_layout:
                onBackPressed();
                break;
            case R.id.radio_email:
                checkRadioButtons(true);
                break;
            case R.id.radio_phone:
                checkRadioButtons(false);
                break;
            case R.id.next_button:
                submit();
                break;
            default:
                logger.info("default statement executed for onClick for view id : " + id);
                break;
        }
    }
}
