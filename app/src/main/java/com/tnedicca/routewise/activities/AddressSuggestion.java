package com.tnedicca.routewise.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.adapters.PlaceAutocompleteAdapter;
import com.tnedicca.routewise.classes.AppConstant;
import com.tnedicca.routewise.classes.RouteLog;
import com.tnedicca.routewise.views.CustomEditTextView;
import com.tnedicca.routewise.views.CustomTextView;

import java.util.ArrayList;

/**
 * Created by Vishal on 20-01-2017.
 */

public class AddressSuggestion extends AppCompatActivity implements AdapterView.OnItemClickListener, PlaceAutocompleteAdapter.AdapterCallback, TextWatcher, View.OnClickListener {

    private CustomEditTextView addressView;
    private PlaceAutocompleteAdapter streetAdapter;
    private ListView placeListView;
    private TextView searchTextView;
    public SharedPreferences mPrefer;
    private SharedPreferences.Editor edit;
    private ArrayAdapter recentSearchAdapter;
    private RouteWise mInstance;

    private CustomTextView screenTitle;
    private ImageView infoIcon;
    private ProgressBar mProgress;
    private boolean mSelected = false;
    private ArrayList recentSearchList;
    private ArrayList recentSearchListId;
    private boolean mCached = false;
    private Activity act;
    private RouteLog logger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_complete);

        screenTitle = findViewById(R.id.action_bar_title);
        infoIcon = findViewById(R.id.info_icon);
        RelativeLayout menuLayout = findViewById(R.id.back_layout);
        menuLayout.setOnClickListener(this);

        screenTitle.setCustomText(getString(R.string.address_suggestion));
        infoIcon.setVisibility(View.INVISIBLE);

        init();
    }

    private void init() {
        mPrefer = getSharedPreferences(AppConstant.PREFERENCE, MODE_PRIVATE);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(AddressSuggestion.class);

        mProgress = new ProgressBar(this);
        act = this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mProgress.setProgressDrawable(getDrawable(R.drawable.progressbar));
        else
            mProgress.setProgressDrawable(ContextCompat.getDrawable(this, R.drawable.progressbar));

        recentSearchList = new ArrayList();
        recentSearchListId = new ArrayList();
        for (int i = 0; i < 5; i++) {
            String data = mPrefer.getString(AppConstant.PLACE_ADDRESS + i, "");
            if (data != null && !data.isEmpty())
                recentSearchList.add(data);
            String data_id = mPrefer.getString(AppConstant.PLACE_ADDRESS_ID + i, "");
            if (data_id != null && !data_id.isEmpty())
                recentSearchListId.add(data_id);
        }

        searchTextView = findViewById(R.id.search_textview);
        placeListView = findViewById(R.id.places_list);
        addressView = findViewById(R.id.google_places_auto_complete);
        streetAdapter = new PlaceAutocompleteAdapter(this, mProgress, mInstance.myLibrary.getBounds(), placeListView, this);
        placeListView.setTextFilterEnabled(true);
        placeListView.setOnItemClickListener(this);
        addressView.addTextChangedListener(this);
        if (recentSearchList.size() != 0) {
            setCachedAddress();
        } else
            placeListView.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        // return to the calling activity on back pressed
        Intent returnIntent = new Intent();
        setResult(RESULT_CANCELED, returnIntent);
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        streetAdapter.resetUpdating(true);
        if (isFinishing())
            overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
    }

    private void setCachedAddress() {
        mCached = true;
        searchTextView.setText(R.string.searches);
        recentSearchAdapter = new ArrayAdapter(act, R.layout.list_item);
        recentSearchAdapter.addAll(recentSearchList);
        recentSearchAdapter.setNotifyOnChange(true);
        recentSearchAdapter.notifyDataSetChanged();
        placeListView.setAdapter(recentSearchAdapter);
        placeListView.setVisibility(View.VISIBLE);
//        streetAdapter.resetUpdating(true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!mCached) {
            if (parent == placeListView) {
                final AutocompletePrediction item = streetAdapter.getItem(position);
                final String placeId = item.getPlaceId();
                streetAdapter.getPlaceByID(placeId);
            }
        } else {
            String address = (String) recentSearchAdapter.getItem(position);
            String ids = null;
            for (int i = 0; i < recentSearchList.size(); i++) {
                String temp = recentSearchList.get(i).toString();
                if (temp.equals(address)) {
                    ids = recentSearchListId.get(i).toString();
                    break;
                }
            }
            streetAdapter.getPlaceByID(ids);
        }
    }

    @Override
    public void getPlaceDetails(Place place) {
        mInstance.myLibrary.hideKeyBoard(addressView, this);
        String address = place.getAddress();
        String addressId = place.getId();

        boolean isPresent = false;
        if (recentSearchList.size() == 0) {
            recentSearchList.add(address);
            recentSearchListId.add(addressId);
        } else {
            for (int i = 0; i < recentSearchListId.size(); i++) {
                if (addressId.equals(recentSearchListId.get(i).toString())) {
                    isPresent = true;
                    break;
                } else {
                    isPresent = false;
                }
            }
            if (!isPresent) {
                recentSearchList.add(address);
                recentSearchListId.add(addressId);
            }
            if (recentSearchList.size() > 5) {
                recentSearchList.remove(0);
                recentSearchListId.remove(0);
            }
        }

        edit = mPrefer.edit();
        for (int i = 0; i < recentSearchList.size(); i++) {
            edit.putString(AppConstant.PLACE_ADDRESS + i, recentSearchList.get(i).toString());
            edit.putString(AppConstant.PLACE_ADDRESS_ID + i, recentSearchListId.get(i).toString());
        }
        edit.commit();

        mSelected = true;
        addressView.setCustomText(address);

        final int len = address.length();
        addressView.post(new Runnable() {
            @Override
            public void run() {
                addressView.setSelection(len);
            }
        });
        Intent intent = new Intent();
        intent.putExtra("ADDRESS", address);
        setResult(1, intent);
        overridePendingTransition(R.anim.anim_slide_in_right, R.anim.anim_slide_out_right);
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        final String text = addressView.getText().toString();
        if (!mSelected) {
            if (text.length() == 0) {
                mSelected = false;
                searchTextView.setText(R.string.searches);
                if (recentSearchList != null)
                    setCachedAddress();
            } else if (text.length() > 2) {
//                streetAdapter.resetUpdating(false);
                searchTextView.setText(R.string.results);
                mCached = false;
                Thread back = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        streetAdapter.getResults(text);
                    }
                });
                back.start();
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (mSelected) {
            mSelected = false;
            if (!mCached) {
                mSelected = false;
                searchTextView.setText(R.string.searches);
                if (recentSearchList != null)
                    setCachedAddress();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_layout:
                onBackPressed();
                break;
            default:
                logger.info("default statement for onClick executed for view id : " + view.getId());
                break;
        }
    }

}
