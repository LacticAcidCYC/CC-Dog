package com.example.eric.lbstest.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.eric.lbstest.EyeLocationViewActivity;
import com.example.eric.lbstest.R;
import com.example.eric.lbstest.classes.ElectricEye;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by APLEE on 2017/5/7.
 */

public class EyeListAdapter extends RecyclerView.Adapter<EyeListAdapter.EyeViewHolder> {

    private Context context;

    private List<ElectricEye> eyeList = new ArrayList<>();

    public EyeListAdapter(Context context, List<ElectricEye> eyeList) {
        this.context = context;
        this.eyeList = eyeList;
    }

    @Override
    public EyeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_custom_eye, parent, false);
        final EyeViewHolder viewHolder = new EyeViewHolder(view);
        viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EyeLocationViewActivity.class);
                intent.putExtra("EyeLat", eyeList.get(viewHolder.getAdapterPosition()).getEndY());
                intent.putExtra("EyeLong", eyeList.get(viewHolder.getAdapterPosition()).getEndX());
                context.startActivity(intent);
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EyeViewHolder holder, int position) {
        holder.img.setImageResource(R.mipmap.camera);
        holder.tv_address.setText("位置: " + eyeList.get(position).getFormattedAddress());
        holder.tv_long.setText(String.valueOf("经度: " + eyeList.get(position).getEndX()));
        holder.tv_lat.setText(String.valueOf("纬度: " + eyeList.get(position).getEndY()));
    }

    @Override
    public int getItemCount() {
        return eyeList.size();
    }

    static class EyeViewHolder extends RecyclerView.ViewHolder {

        ImageView img;

        TextView tv_address; //地址信息

        TextView tv_lat; //纬度

        TextView tv_long; //经度

        RelativeLayout relativeLayout;

        public EyeViewHolder(View itemView) {
            super(itemView);
            relativeLayout = (RelativeLayout) itemView;
            img = (ImageView) itemView.findViewById(R.id.icon_eye);
            tv_address = (TextView) itemView.findViewById(R.id.tv_address);
            tv_lat = (TextView) itemView.findViewById(R.id.tv_latitude);
            tv_long = (TextView) itemView.findViewById(R.id.tv_longitude);
        }
    }
}
