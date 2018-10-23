package com.example.eric.lbstest.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.eric.lbstest.EyeManagerActivity;
import com.example.eric.lbstest.LoginActivity;
import com.example.eric.lbstest.R;
import com.example.eric.lbstest.utils.ActivityCollector;
import com.example.eric.lbstest.utils.ToastUtils;

import java.util.List;

import cn.bmob.v3.BmobUser;

/**
 * Created by APLEE on 2017/5/5.
 */

public class AccountPageAdapter extends RecyclerView.Adapter<AccountPageAdapter.MyViewHolder> {

    private Context context;

    private List<String> itemList;

    private AlertDialog alert = null;

    private AlertDialog.Builder builder = null;

    public AccountPageAdapter(Context context, List<String> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_account, parent, false);
        final MyViewHolder viewHolder = new MyViewHolder(view);
        viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                switch (position) {
                    case 0:
                        //电子眼管理
                        ToastUtils.showShort(context, "Electric Eye Management!");
                        Intent eyeManagerIntent = new Intent(context, EyeManagerActivity.class);
                        context.startActivity(eyeManagerIntent);
                        break;

                    case 1:
                        //设置
                        ToastUtils.showShort(context, "Setting!");
                        break;

                    case 2:
                        //帮助与反馈
                        ToastUtils.showShort(context, "Help & Feedback!");
                        break;

                    case 3:
                        //退出登录
                        alert = null;
                        builder = new AlertDialog.Builder(context);
                        alert = builder
                                .setTitle("System Reminder")
                                .setMessage("Are you sure you want to log out？")
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        alert.dismiss();
                                    }
                                })
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        BmobUser.logOut(context);
                                        Intent LoginIntent = new Intent(context, LoginActivity.class);
                                        context.startActivity(LoginIntent);
                                    }
                                })
                                .setCancelable(false)
                                .create();
                        alert.show();
                        break;

                    case 4:
                        //关闭应用程序
                        //ToastUtils.showShort(context, "关闭应用程序!");
                        alert = null;
                        builder = new AlertDialog.Builder(context);
                        alert = builder
                                .setTitle("System Reminder")
                                .setMessage("Are you sure you want to exit the app？")
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Toast.makeText(context, "您点击了否按钮", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Toast.makeText(context, "您点击了是按钮", Toast.LENGTH_SHORT).show();
                                        ActivityCollector.finishAll();
                                    }
                                })
                                .setCancelable(false)
                                .create();  //创建AlertDialog对象
                        alert.show();
                        break;

                    default:
                        break;
                }
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tv.setText(itemList.get(position));
        holder.img.setImageResource(R.mipmap.btn_type_choice);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        TextView tv;
        ImageView img;

        public MyViewHolder(View itemView) {
            super(itemView);
            relativeLayout = (RelativeLayout) itemView;
            tv = (TextView) itemView.findViewById(R.id.tv_item);
            img = (ImageView) itemView.findViewById(R.id.img_enter);
        }
    }
}
