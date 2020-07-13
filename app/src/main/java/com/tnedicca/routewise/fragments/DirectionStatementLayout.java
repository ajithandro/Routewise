package com.tnedicca.routewise.fragments;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tnedicca.routewise.R;
import com.tnedicca.routewise.views.CustomTextView;

/**
 * Created by new on 24-02-2017.
 */

public class DirectionStatementLayout extends Fragment {

    CustomTextView statementView;
    CustomTextView distanceView;
    String statement;
    String distance;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myFragmentView = inflater.inflate(R.layout.direction_statement_layout, container, false);
        statementView = myFragmentView.findViewById(R.id.statement_text);
        distanceView = myFragmentView.findViewById(R.id.distance);
        statementView.setText(statement);
        distanceView.setText(distance);
        return myFragmentView;
    }

    public void setStatement(String statementText){
        statement = statementText;
    }

    public void setDistance(String distanceText){
        distance = distanceText;
    }
}
