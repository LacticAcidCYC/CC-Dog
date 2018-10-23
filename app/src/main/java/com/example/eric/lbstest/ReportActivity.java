package com.example.eric.lbstest;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.eric.lbstest.utils.ActivityCollector;
import com.example.eric.lbstest.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReportActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.btn_add_eye)
    Button btn_add_eye;

    @BindView(R.id.btn_report_eye_error)
    Button btn_report_eye_error;

    @BindView(R.id.btn_report_other)
    Button btn_report_other;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ButterKnife.bind(this);
        ActivityCollector.addActivity(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Report");
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initView();
    }

    private void initView() {
        btn_add_eye.setOnClickListener(this);
        btn_report_eye_error.setOnClickListener(this);
        btn_report_other.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_eye:
                ToastUtils.showShort(ReportActivity.this, "Click “Add new eyes” button");
                Intent addEyeIntent = new Intent(ReportActivity.this, AddEyeActivity.class);
                startActivity(addEyeIntent);
                break;

            case R.id.btn_report_eye_error:
                ToastUtils.showShort(ReportActivity.this, "Click “Report” button");
                break;

            case R.id.btn_report_other:
                ToastUtils.showShort(ReportActivity.this, "Click “Other feedback” button");
                break;

            default:
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
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
