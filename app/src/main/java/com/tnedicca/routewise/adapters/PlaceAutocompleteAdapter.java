/*
 * Copyright (C) 2015 Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.tnedicca.routewise.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.RouteLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.ContentValues.TAG;

public class PlaceAutocompleteAdapter extends ArrayAdapter<AutocompletePrediction> {

    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private final RouteLog logger;
    private final PlacesClient placesClient;
    private RouteWise mInstance;
    private ArrayList<AutocompletePrediction> mResultList;
    private LatLngBounds mBounds;
    private ProgressBar mProgress;
    private ListView mList;
    CharSequence mSequence;
    private int lineNumber;
    AdapterCallback mCallback;

    public interface AdapterCallback {
        void getPlaceDetails(Place place);
    }

    public PlaceAutocompleteAdapter(Context context, ProgressBar progress, LatLngBounds bounds, ListView list, AdapterCallback callback) {
        super(context, R.layout.list_places, R.id.text);
        mInstance = RouteWise.getInstance();
        logger = new RouteLog();
        logger.setLoggerClass(PlaceAutocompleteAdapter.class);

        Places.initialize(context, context.getResources().getString(R.string.google_places_key));
        placesClient = Places.createClient(context);

        mCallback = callback;
        mBounds = bounds;
        mProgress = progress;
        mList = list;
    }

    public void setBounds(LatLngBounds bounds) {
        mBounds = bounds;
    }

    public ArrayAdapter<AutocompletePrediction> getResults(CharSequence text) {
        mSequence = text;

        if (mSequence != null) {
            AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
            RectangularBounds bounds = RectangularBounds.newInstance(mBounds);

            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setLocationBias(bounds)
                    .setTypeFilter(TypeFilter.CITIES)
                    .setSessionToken(token)
                    .setQuery(mSequence + "")
                    .build();

            placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
                ArrayList<AutocompletePrediction> results = new ArrayList<>();
                for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                    results.add(prediction);
                }
                if (results != null && results.size() > 0) {
                    mResultList = results;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
                mList.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.GONE);
                mList.setAdapter(PlaceAutocompleteAdapter.this);
            }).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                }
            });
            return this;
        }
        return null;
    }

    public void getPlaceByID(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            mCallback.getPlaceDetails(place);
            Log.i(TAG, "Place found: " + place.getName());
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " + exception.getMessage());
            }
        });
    }

    @Override
    public int getCount() {
        int count = 0;
        if (mResultList != null)
            count = mResultList.size();
        return count;
    }

    @Override
    public AutocompletePrediction getItem(int position) {
        AutocompletePrediction send = null;
        try {
            int size = mResultList.size();
            if (size >= position)
                send = mResultList.get(position);
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
        }
        return send;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = null;
        try {
            row = super.getView(position, convertView, parent);

            AutocompletePrediction item = getItem(position);
            TextView textView1 = row.findViewById(R.id.text);
            if (item != null) {
                CharSequence text = item.getFullText(STYLE_BOLD);
                textView1.setText(text);
            }
        } catch (Exception e) {
            lineNumber = mInstance.myLibrary.getLineNumber(e, getClass().getName());
            logger.error("Error at " + lineNumber, e);
            row = convertView;
        }
        return row;
    }

//    @Override
//    public Filter getFilter() {
//        notifyDataSetChanged();
//        return new Filter() {
//            @Override
//            protected FilterResults performFiltering(CharSequence constraint) {
//                FilterResults results = new FilterResults();
//                if (failToast != null)
//                    failToast.cancel();
//                // Skip the autocomplete query if no constraints are given.
//                if (constraint != null) {
//                    constraint = mSequence;
//                    // Query the autocomplete API for the (constraint) search string.
//                    ArrayList<AutocompletePrediction> list = null;
////                    if (mInstance.basic.checkData(context))
//                    list = getAutocomplete(constraint);
////                    else
////                        mInstance.basic.DisplayToast(context, mInstance.DATA_ERROR_MESSAGE, Toast.LENGTH_SHORT, Gravity.CENTER);
//                    if (list != null) {
////                        // The API successfully returned results.
//                        results.values = list;
//                        results.count = list.size();
//                    }
//                }
//                return results;
//            }
//
//            @Override
//            protected void publishResults(CharSequence constraint, FilterResults results) {
//                if (results != null && results.count > 0) {
//                    // The API returned at least one result, update the data.
//                    mResultList = (ArrayList<AutocompletePrediction>) results.values;
//                    notifyDataSetChanged();
//                } else {
//                    // The API did not return any results, invalidate the data set.
//                    notifyDataSetInvalidated();
////                    failToast = mInstance.basic.DisplayToast((Activity) context, "Failed to get places", Toast.LENGTH_SHORT, Gravity.CENTER);
//                }
//                mProgress.setVisibility(View.GONE);
//            }
//
//            @Override
//            public CharSequence convertResultToString(Object resultValue) {
//                // Override this method to display a readable result in the AutocompleteTextView
//                // when clicked.
//                if (resultValue instanceof AutocompletePrediction) {
//                    return ((AutocompletePrediction) resultValue).getFullText(STYLE_BOLD);
//                } else {
//                    return super.convertResultToString(resultValue);
//                }
//            }
//        };
//    }

}