package com.trucklog.Adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.menu.ListMenuItemView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.trucklog.Models.LoadModel;
import com.trucklog.R;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by rock on 1/31/17.
 */

public class LoadListAdapter extends BaseAdapter {
    ArrayList<LoadModel> arrayList;
    Context context;
    LayoutInflater inflater;
    public LoadListAdapter(Context context, ArrayList<LoadModel> arrayList) {
        this.context = context;
        this.arrayList = arrayList;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.item_load_listview, null);
        ViewHolder holder = new ViewHolder();
        holder.img_indicator = (ImageView)view.findViewById(R.id.img_item_indicator);
        holder.txt_indicator = (TextView)view.findViewById(R.id.txt_item_indicator);
        holder.txt_loadname = (TextView)view.findViewById(R.id.txt_item_loadname);
        if(arrayList.get(i).getStatus() == 3){
            //inactive
            holder.img_indicator.setImageResource(R.drawable.ic_pin_black_24dp);
            holder.img_indicator.setColorFilter(ContextCompat.getColor(context, R.color.deactivColor));
            holder.txt_indicator.setText("Inactive");
            holder.txt_indicator.setTextColor(ContextCompat.getColor(context, R.color.deactivColor));
        }
        else{
            //active
            holder.img_indicator.setImageResource(R.drawable.ic_check_black_24dp);
            holder.img_indicator.setColorFilter(ContextCompat.getColor(context, R.color.activeColor));
            holder.txt_indicator.setText("Active");
            holder.txt_indicator.setTextColor(ContextCompat.getColor(context, R.color.activeColor));
        }

        holder.txt_loadname.setText("Load " + arrayList.get(i).getSeq_index());
        if(arrayList.get(i).is_new() == true){
            holder.txt_loadname.setTypeface(null, Typeface.BOLD);
        }
        else{
            holder.txt_loadname.setTypeface(null, Typeface.NORMAL);
        }
        return view;
    }

    public static class ViewHolder{
        TextView txt_loadname, txt_indicator;
        ImageView img_indicator;
    }
}
