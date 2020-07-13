package com.tnedicca.routewise.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.Trip;
import com.tnedicca.routewise.views.CustomTextView;

import java.util.ArrayList;

/**
 * Created by new on 08-02-2017.
 */

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.DataObjectHolder> {
    private ArrayList<Trip> mDataset;
    private MyClickListener myClickListener;
    int viewCounter = 0;
    private RouteWise mInstance = RouteWise.getInstance();

    public MyRecyclerViewAdapter(ArrayList<Trip> myDataset) {
        mDataset = myDataset;
        viewCounter = 0;
    }

    public MyClickListener getMyClickListener() {
        return myClickListener;
    }

    public void setMyClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DataObjectHolder dataObjectHolder = null;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_trip_card, parent, false);
        dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        ImageLoader imageLoader = mInstance.getImageLoader();
        holder.mapImage.setDefaultImageResId(R.drawable.map_example);
        holder.mapImage.setErrorImageResId(R.drawable.map_example);
        holder.mapImage.setImageUrl(mDataset.get(position).getMapImage(), imageLoader);
        holder.tripDate.setText(mDataset.get(position).getTripDate());
        holder.startPoint.setText(mDataset.get(position).getStartPoint());
        holder.endPoint.setText(mDataset.get(position).getEndPoint());
        holder.tripLength.setText(mDataset.get(position).getTripLength());
        holder.riskScore.setText(mDataset.get(position).getRiskScore());
    }

    public void addItem(Trip dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }

    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public interface MyClickListener {
        void onItemClick(int position, View v);
    }

    public class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private NetworkImageView mapImage;
        private CustomTextView tripDate;
        private CustomTextView startPoint;
        private CustomTextView endPoint;
        private CustomTextView tripLength;
        private CustomTextView riskScore;

        public DataObjectHolder(View itemView) {
            super(itemView);
            mapImage = itemView.findViewById(R.id.map);
            tripDate = itemView.findViewById(R.id.trip_date);
            startPoint = itemView.findViewById(R.id.start_point);
            endPoint = itemView.findViewById(R.id.end_point);
            tripLength = itemView.findViewById(R.id.distance);
            riskScore = itemView.findViewById(R.id.risk_score);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (myClickListener != null) {
                myClickListener.onItemClick(getAdapterPosition(), v);
            }
        }
    }
}
