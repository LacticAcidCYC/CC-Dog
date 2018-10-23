package com.example.eric.lbstest;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.mapapi.model.LatLng;
import com.example.eric.lbstest.classes.ElectricEye;
import com.example.eric.lbstest.utils.ActivityCollector;
import com.example.eric.lbstest.utils.ToastUtils;
import com.example.eric.lbstest.views.CleanableEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.bmob.v3.listener.SaveListener;

public class AddEyeActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.tv_type)
    TextView tv_type;

    @BindView(R.id.et_address)
    CleanableEditText et_address;

    @BindView(R.id.et_location_desc)
    CleanableEditText et_location_desc;

    @BindView(R.id.et_contact_phone)
    CleanableEditText et_contact_phone;

    @BindView(R.id.btn_get_location)
    Button btn_get_location;

    private LatLng eye_location;

    private String eye_address;

    private AlertDialog alert = null;

    private AlertDialog.Builder builder = null;

    //请求码
    private final static int REQUEST_POS_CODE = 1; //请求获得电子眼地址

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_eye);
        ActivityCollector.addActivity(this);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Add New Electric Eyes");
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_POS_CODE && resultCode == RESULT_OK) {
            eye_location = data.getParcelableExtra("EyeLatLng");
            eye_address = data.getStringExtra("EyeAddress");
            ToastUtils.showShort(AddEyeActivity.this, eye_location.longitude + " " +
                    + eye_location.latitude + "\n" + eye_address);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn_get_location.setText("Choose a new place");
                    btn_get_location.setTextColor(getColor(R.color.blue));
                    et_address.setText(eye_address);
                }
            });
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_type:
                final String[] types = new String[] {"监控探头","固定测速","流动测速","闯红灯拍照",
                        "区间测速","逆行拍照","禁止违章环道","违章停车拍照","占用公交车道拍照"};
                alert = null;
                builder = new AlertDialog.Builder(AddEyeActivity.this);
                alert = builder
                        .setTitle("Please choose camera types:")
                        .setSingleChoiceItems(types, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
                                ToastUtils.showShort(AddEyeActivity.this, "You choose:" + types[which]);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tv_type.setText("Electric eye type：" + types[which]);
                                    }
                                });
                                alert.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .create();
                alert.show();
                break;

            case R.id.btn_get_location:
                //ToastUtils.showShort(AddEyeActivity.this, "点击“点击选择位置”按钮");
                Intent getLocationIntent = new Intent(AddEyeActivity.this, PickLocationOfEyeActivity.class);
                startActivityForResult(getLocationIntent, REQUEST_POS_CODE);
                break;

            case R.id.tv_example:
                //ToastUtils.showShort(AddEyeActivity.this, "点击“正确示例”按钮");
                alert = null;
                builder = new AlertDialog.Builder(AddEyeActivity.this);
                alert = builder
                        .setTitle("Locating Tips")
                        .setMessage("1. Drag the map can change the locating place;" + "\n" + "\n"
                                + "2. Put the red pin on the place you want" + "\n" + "\n"
                                + "3. Notice: do not put the pin on the middle of road or bridge.")
                        .setPositiveButton("confrim", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setCancelable(true)
                        .create();  //创建AlertDialog对象
                alert.show();
                break;

            case R.id.btn_submit:
                ToastUtils.showShort(AddEyeActivity.this, "Click 'Submit' button");
                final ElectricEye electricEye = new ElectricEye(ElectricEye.getType(tv_type.getText().toString()),
                        eye_location.longitude, eye_location.latitude, eye_address);
                electricEye.save(AddEyeActivity.this, new SaveListener() {
                    @Override
                    public void onSuccess() {
                        ToastUtils.showShort(AddEyeActivity.this,
                                "添加数据成功，返回objectId为："+ electricEye.getObjectId() + "\n" +
                                "数据在服务端的创建时间为：" + electricEye.getCreatedAt());
                    }

                    @Override
                    public void onFailure(int i, String s) {
                        ToastUtils.showShort(AddEyeActivity.this, "Adding error!");
                    }
                });
                finish();
                /*
                Intent eyeManagerIntent = new Intent(AddEyeActivity.this, EyeManagerActivity.class);
                eyeManagerIntent.putExtra("EyeType", tv_type.getText().toString());
                eyeManagerIntent.putExtra("EyeLatLng", eye_location);
                eyeManagerIntent.putExtra("EyeAddress", eye_address);
                eyeManagerIntent.putExtra("EyeAddressDesc", et_location_desc.getText().toString());
                eyeManagerIntent.putExtra("Contact", et_contact_phone.getText().toString());
                startActivity(eyeManagerIntent);*/
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
