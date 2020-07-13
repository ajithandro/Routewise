package com.tnedicca.routewise.fragments;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tnedicca.routewise.R;

/**
 * Created by new on 18-01-2017.
 */

public class AutoCompleteFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.auto_complete_fragment, container, false);
    }
}
