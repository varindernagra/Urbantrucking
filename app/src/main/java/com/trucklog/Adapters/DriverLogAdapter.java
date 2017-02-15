package com.trucklog.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trucklog.Models.LogModel;
import com.trucklog.R;
import com.trucklog.Utils.Constants;
import com.trucklog.Utils.ConvertUtils;

import java.util.ArrayList;

/**
 * Created by rock on 2/4/17.
 */

public class DriverLogAdapter extends BaseAdapter {

    ArrayList<LogModel> dataProvider;
    Context context;
    LayoutInflater inflater;
    public DriverLogAdapter(ArrayList<LogModel> dataProvider, Context context) {
        this.dataProvider = dataProvider;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        int result;
        try {
            result = dataProvider.size();
        }catch (Exception e){
            result = 0;
        }
        return result;
    }

    @Override
    public LogModel getItem(int position) {
        return dataProvider.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.item_driver_log_details,null);
        TextView date_time = (TextView) convertView.findViewById(R.id.txt_driverlog_time);
        date_time.setText(ConvertUtils.getDate(getItem(position).getTime()));
        TextView staus = (TextView) convertView.findViewById(R.id.txt_driverlog_status);
        staus.setText(Constants.TITLE_DRIVER_TOGGLE[getItem(position).getStatus()-1]);
        TextView log = (TextView) convertView.findViewById(R.id.txt_driverlog_load);
        log.setText(getItem(position).getLocation());
        return convertView;
    }
}
