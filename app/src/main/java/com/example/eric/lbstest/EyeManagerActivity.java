package com.example.eric.lbstest;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.baidu.mapapi.model.LatLng;
import com.example.eric.lbstest.adapter.EyeListAdapter;
import com.example.eric.lbstest.classes.ElectricEye;
import com.example.eric.lbstest.utils.ActivityCollector;
import com.example.eric.lbstest.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.listener.FindListener;

public class EyeManagerActivity extends AppCompatActivity {

    private static final String TAG = "EyeManagerActivity";

    private RecyclerView eye_list_recycler_view;

    private List<ElectricEye> eyeList;

    private EyeListAdapter myAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eye_manager_acitivity);
        ActivityCollector.addActivity(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Electric Eye Management");
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        /*
        Intent intent = getIntent();
        new_type = ElectricEye.getType(intent.getStringExtra("EyeType"));
        new_latLng = intent.getParcelableExtra("EyeLatLng");
        new_address = intent.getStringExtra("EyeAddress");
        addData();*/

        initData();
        initView();
    }

    private void initView() {
        eye_list_recycler_view = (RecyclerView) findViewById(R.id.eye_list_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(EyeManagerActivity.this);
        eye_list_recycler_view.setLayoutManager(layoutManager);
        eye_list_recycler_view.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));
        myAdapter = new EyeListAdapter(EyeManagerActivity.this, eyeList);
        eye_list_recycler_view.setAdapter(myAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateData();
            }
        });
    }

    private void initData() {
        eyeList = new ArrayList<>();
        BmobQuery<ElectricEye> query = new BmobQuery<ElectricEye>();
        query.findObjects(EyeManagerActivity.this, new FindListener<ElectricEye>() {
            @Override
            public void onSuccess(List<ElectricEye> list) {
                ToastUtils.showShort(EyeManagerActivity.this, "Successful Searching："+list.size()+"data in total.");
                for(ElectricEye electricEye : list) {
                    int type = electricEye.getType();
                    double longitude = electricEye.getEndX();
                    double latitude = electricEye.getEndY();
                    String address = electricEye.getFormattedAddress();
                    eyeList.add(new ElectricEye(type, longitude, latitude, address));
                    myAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError(int i, String msg) {
                ToastUtils.showShort(EyeManagerActivity.this, "Fail to search" + msg);
            }
        });
        /*
        eyeList.add(new ElectricEye(1, 108.845559, 34.128976, "陕西省西安市长安区兴隆街道"));
        eyeList.add(new ElectricEye(1, 108.845559, 34.128976, "陕西省西安市长安区兴隆街道"));
        eyeList.add(new ElectricEye(1, 108.845663, 34.129063, "陕西省西安市长安区兴隆街道"));
        eyeList.add(new ElectricEye(1, 108.854502, 34.13266, "陕西省西安市长安区郭杜街道"));
        eyeList.add(new ElectricEye(1, 108.847947, 34.132749, "陕西省西安市长安区兴隆街道闻天生态科技示范园"));
        eyeList.add(new ElectricEye(1, 108.854568, 34.132823, "陕西省西安市长安区郭杜街道"));*/
    }

    private void updateData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BmobQuery<ElectricEye> query = new BmobQuery<ElectricEye>();
                        query.findObjects(EyeManagerActivity.this, new FindListener<ElectricEye>() {
                            @Override
                            public void onSuccess(List<ElectricEye> list) {
                                ToastUtils.showShort(EyeManagerActivity.this, "Successful Searching：\"+list.size()+\"data in total.");
                                eyeList.clear();
                                for(ElectricEye electricEye : list) {
                                    int type = electricEye.getType();
                                    double longitude = electricEye.getEndX();
                                    double latitude = electricEye.getEndY();
                                    String address = electricEye.getFormattedAddress();
                                    eyeList.add(new ElectricEye(type, longitude, latitude, address));
                                    myAdapter.notifyDataSetChanged();
                                    swipeRefreshLayout.setRefreshing(false);
                                }
                            }

                            @Override
                            public void onError(int i, String msg) {
                                ToastUtils.showShort(EyeManagerActivity.this, "Fail to search" + msg);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.eye_manager_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;

            case R.id.report:
                //ToastUtils.showShort(EyeManagerActivity.this, "点击了上报按钮");
                Intent reportIntent = new Intent(EyeManagerActivity.this, ReportActivity.class);
                startActivity(reportIntent);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
