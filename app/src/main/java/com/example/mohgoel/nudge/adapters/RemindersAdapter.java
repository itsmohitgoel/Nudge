package com.example.mohgoel.nudge.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mohgoel.nudge.AddEditActivity;
import com.example.mohgoel.nudge.R;
import com.example.mohgoel.nudge.beans.ReminderItem;
import com.example.mohgoel.nudge.utils.Utility;

import java.util.ArrayList;

/**
 * Created by MOHGOEL on 13-May-18.
 */

public class RemindersAdapter extends RecyclerView.Adapter<RemindersAdapter.ViewHolder> {
    private final int VIEW_TYPE_HEADER = 0;
    private final int VIEW_TYPE_ROW = 1;
    private Context mContext;
    private ArrayList<ReminderItem> mDataList;

    public RemindersAdapter(Context context, ArrayList<ReminderItem> list) {
        this.mContext = context;
        this.mDataList = list;
    }

    @Override
    public RemindersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutID = -1;

        layoutID = R.layout.view_reminder_item;

        View view = LayoutInflater.from(mContext).inflate(layoutID, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RemindersAdapter.ViewHolder holder, final int position) {
        holder.tvReminderName.setText(mDataList.get(position).getName());
        if (Long.parseLong(mDataList.get(position).getRemindOn()) > 0) {
            holder.tvReminderTime.setText(Utility.getFriedlyDate(mContext, mDataList.get(position).getRemindOn()));
            holder.row.setBackgroundColor(mContext.getResources().getColor(R.color.foam));
        }else {
            holder.tvReminderTime.setText(Utility.getFriedlyDate(mContext, mDataList.get(position).getCreatedOn()));
        }
        holder.row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent detailIntent = new Intent(mContext, AddEditActivity.class);
                detailIntent.putExtra(AddEditActivity.REMINDER_PARCELABLE, mDataList.get(position));
                mContext.startActivity(detailIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }



    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReminderName;
        TextView tvReminderTime;
        LinearLayout row;

        public ViewHolder(View itemView) {
            super(itemView);
            row = (LinearLayout) itemView.findViewById(R.id.view_row_item);
            tvReminderName = (TextView) itemView.findViewById(R.id.reminder_name_textview);
            tvReminderTime = (TextView) itemView.findViewById(R.id.reminder_time_textview);
        }
    }
}
