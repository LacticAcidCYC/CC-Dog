package com.example.eric.lbstest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.eric.lbstest.adapter.AccountPageAdapter;
import com.example.eric.lbstest.utils.ActivityCollector;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobUser;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";

    private List<String> items = new ArrayList<>();

    private Context context;

    private AccountPageAdapter myAdapter;

    private RecyclerView recyclerView;

    private TextView tv_user_name;

    private TextView tv_mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        ActivityCollector.addActivity(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Account");
        //toolbar.setTitleMargin();
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        context = AccountActivity.this;
        initData();
        initViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //finish();
                Intent MainIntent = new Intent(AccountActivity.this, MainActivity.class);
                startActivity(MainIntent);
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initData() {
        items.add("Electric Eye Management");
        items.add("Setting");
        items.add("Help & Feedback");
        items.add("Sign out");
        items.add("Close app");
    }

    private void initViews() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_item);
        LinearLayoutManager layoutManager = new LinearLayoutManager(AccountActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, layoutManager.getOrientation()));

        myAdapter = new AccountPageAdapter(context, items);
        recyclerView.setAdapter(myAdapter);

        tv_user_name = (TextView) findViewById(R.id.username);
        tv_user_name.setText("Account name: " + (String) BmobUser.getObjectByKey(AccountActivity.this, "username"));

        //该项暂时设为手机号
        tv_mail = (TextView) findViewById(R.id.mail);
        tv_mail.setText("Phone number: " + (String) BmobUser.getObjectByKey(AccountActivity.this, "mobilePhoneNumber"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
