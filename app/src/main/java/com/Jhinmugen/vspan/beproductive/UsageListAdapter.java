package com.Jhinmugen.vspan.beproductive;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vspan.beproductive.R;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by vspan on 12/11/2017.
 */

public class UsageListAdapter extends RecyclerView.Adapter<UsageListAdapter.ViewHolder> implements Serializable {

    private List<CustomUsageStats> mCustomUsageStatsList = new ArrayList<>();
    private ArrayList<String> applicationNames = new ArrayList<>();
    private ArrayList<Long> applicationRunTimes = new ArrayList<>();
    private DateFormat mDateFormat = new SimpleDateFormat();
    Context context;

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPackageName;
        private final TextView mLastTimeUsed;
        private final ImageView mAppIcon;
        private final TextView mTimeUsed;

        public ViewHolder(View v) {
            super(v);
            mPackageName = (TextView) v.findViewById(R.id.textview_package_name);
            mLastTimeUsed = (TextView) v.findViewById(R.id.textview_last_time_used);
            mAppIcon = (ImageView) v.findViewById(R.id.app_icon);
            mTimeUsed = (TextView) v.findViewById(R.id.textview_time_used);


        }


        public TextView getLastTimeUsed() {
            return mLastTimeUsed;
        }

        public TextView getPackageName() {
            return mPackageName;
        }

        public ImageView getAppIcon() {
            return mAppIcon;
        }

        public TextView getmTimeUsed() {
            return mTimeUsed;
        }
    }

    public UsageListAdapter(Context context) {
        this.context = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.usage_row, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.getPackageName().setText(
                mCustomUsageStatsList.get(position).usageStats.getPackageName());
        long lastTimeUsed = mCustomUsageStatsList.get(position).usageStats.getLastTimeUsed();
        long timeUsed = mCustomUsageStatsList.get(position).usageStats.getTotalTimeInForeground();
        viewHolder.getLastTimeUsed().setText(mDateFormat.format(new Date(lastTimeUsed)));
        viewHolder.getmTimeUsed().setText(String.format("%d min,%d sec", TimeUnit.MILLISECONDS.toMinutes(timeUsed),
                TimeUnit.MILLISECONDS.toSeconds(timeUsed) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeUsed))));
        viewHolder.getAppIcon().setImageDrawable(mCustomUsageStatsList.get(position).appIcon);


        View.OnClickListener viewListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ActivityMoreStatistics.class);
                intent.putStringArrayListExtra("nameList", applicationNames);
                intent.putExtra("foreground", applicationRunTimes);
                intent.putExtra("current name", mCustomUsageStatsList.get(position).usageStats.getPackageName());
                intent.putExtra("current time", mCustomUsageStatsList.get(position).usageStats.getTotalTimeInForeground());
                intent.putExtra("first last time used", mCustomUsageStatsList.get(0).usageStats.getLastTimeUsed());
                intent.putExtra("last last time used", mCustomUsageStatsList.get((mCustomUsageStatsList.size() - 1)).usageStats.getLastTimeUsed());


                context.startActivity(intent);

            }
        };

        viewHolder.mAppIcon.setOnClickListener(viewListener);
    }

    @Override
    public int getItemCount() {
        return mCustomUsageStatsList.size();
    }

    public void setCustomUsageStatsList(List<CustomUsageStats> customUsageStats) {
        mCustomUsageStatsList = customUsageStats;
        for (int i = 0; i < mCustomUsageStatsList.size(); i++) {
            applicationNames.add(mCustomUsageStatsList.get(i).usageStats.getPackageName());
            applicationRunTimes.add(mCustomUsageStatsList.get(i).usageStats.getTotalTimeInForeground());
        }

    }


}