package com.tnedicca.routewise.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.tnedicca.routewise.R;
import com.tnedicca.routewise.activities.RouteWise;
import com.tnedicca.routewise.classes.SafeTrip;
import com.tnedicca.routewise.views.CustomTextView;

import java.util.ArrayList;

/**
 * Created by new on 08-02-2017.
 */

public class SafeRouteAdapter extends RecyclerView.Adapter<SafeRouteAdapter.DataObjectHolder> {
    private ArrayList<SafeTrip> mDataset;
    private MyClickListener myClickListener;
    private RouteWise mInstance = RouteWise.getInstance();

    public MyClickListener getMyClickListener() {
        return myClickListener;
    }

    public void setMyClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public class DataObjectHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final CustomTextView distanceValue;
        private final CustomTextView timeValue;
        private final CustomTextView riskValue;
        private final ImageView safeImage;
        private final CustomTextView safeText;
        private NetworkImageView mapImage;
        private CustomTextView tripName;

        public DataObjectHolder(View itemView) {
            super(itemView);
            mapImage = itemView.findViewById(R.id.map);
            tripName = itemView.findViewById(R.id.trip_name);
            distanceValue = itemView.findViewById(R.id.distance_value);
            timeValue = itemView.findViewById(R.id.time_value);
            riskValue = itemView.findViewById(R.id.route_risk_value);
            safeImage = itemView.findViewById(R.id.safe_image);
            safeText = itemView.findViewById(R.id.safe_text);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (myClickListener != null) {
                myClickListener.onItemClick(getAdapterPosition(), v);
            }
        }
    }

    public void setOnItemClickListener(MyClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    public SafeRouteAdapter(ArrayList<SafeTrip> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DataObjectHolder dataObjectHolder = null;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.safe_route_card, parent, false);
        dataObjectHolder = new DataObjectHolder(view);

        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        String url = mDataset.get(position).getUrl();
        String routeName = mDataset.get(position).getRouteName();
        String riskFactor = mDataset.get(position).getRiskFactor();
        String finalDur = mDataset.get(position).getFinalDur();
        String finalDis = mDataset.get(position).getFinalDis();
        boolean safe = mDataset.get(position).isSafe();

        ImageLoader imageLoader = mInstance.getImageLoader();
        holder.mapImage.setDefaultImageResId(R.drawable.map_example);
        holder.mapImage.setErrorImageResId(R.drawable.map_example);
        holder.mapImage.setImageUrl(url, imageLoader);
        holder.tripName.setCustomText(routeName);
        holder.distanceValue.setCustomText(finalDis);
        holder.timeValue.setCustomText(finalDur);
        holder.riskValue.setCustomText(riskFactor);

        if (safe) {
            holder.safeText.setVisibility(View.VISIBLE);
            holder.safeImage.setImageResource(R.drawable.ic_tickclick);
        } else {
            holder.safeText.setVisibility(View.INVISIBLE);
            holder.safeImage.setImageResource(R.drawable.ic_ticknoclick);
        }

    }

    public void addItem(SafeTrip dataObj, int index) {
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
}
