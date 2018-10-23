package com.example.eric.lbstest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.example.eric.lbstest.utils.ActivityCollector;
import com.example.eric.lbstest.utils.SystemStatusManager;
import com.example.eric.lbstest.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NavigateActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.tv_current_pos)
    TextView tv_current_pos;

    @BindView(R.id.tv_destination)
    TextView tv_destination;

    @BindView(R.id.tv_car)
    TextView tv_car;

    @BindView(R.id.tv_bus)
    TextView tv_bus;

    @BindView(R.id.tv_walk)
    TextView tv_walk;

    @BindView(R.id.tv_bike)
    TextView tv_bike;

    //导航相关字符
    private String mSdcardPath=null;

    private static final String APP_FOLDER_NAME="navPath";

    public static final String ROUTE_PLAN_NODE = "routePlanNode";

    private String authinfo = null;

    //起始点与终点位置
    private LatLng latLng_start;

    private LatLng latLng_end;

    private String start_place;

    private String end_place;

    //请求码
    private final static int REQUEST_POS_CODE = 1;

    private final static int REQUEST_DES_CODE = 2;

    //选中标志
    private static boolean NAV_CAR = true;

    private static boolean NAV_BUS = false;

    private static boolean NAV_WALK = false;

    private static boolean NAV_BIKE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setTranslucentStatus();
        //SDKInitializer.initialize(getApplicationContext());
        
        setContentView(R.layout.activity_navigate);
        ButterKnife.bind(this);

        ActivityCollector.addActivity(this);

        Intent intent = getIntent();
        latLng_start = intent.getParcelableExtra("CurrentLatLng");
        /*
        ToastUtils.showShort(NavigateActivity.this, String.valueOf(latLng_start.latitude) + "\n"
                + String.valueOf(latLng_start.longitude));*/

        //initSdcardPath();//先获得SD卡的路径
        initViews();
    }

    private void initSdcardPath() {
        if (initDir()) {
            initNaviPath();
        }
    }

    private boolean initDir() {
        //创建一个文件夹用于保存在路线导航过程中语音导航语音文件的缓存，防止用户再次开启同样的导航直接从缓存中读取即可
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            mSdcardPath=Environment.getExternalStorageDirectory().toString();
        }else{
            mSdcardPath=null;
        }
        if (mSdcardPath==null) {
            return false;
        }
        File file=new File(mSdcardPath,APP_FOLDER_NAME);
        if (!file.exists()) {
            try {
                file.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        Toast.makeText(NavigateActivity.this, mSdcardPath, Toast.LENGTH_SHORT).show();
        return true;
    }

    private void initNaviPath() {
        //初始化导航路线的导航引擎
        BNOuterTTSPlayerCallback ttsCallback = null;

        BaiduNaviManager.getInstance().init(NavigateActivity.this, mSdcardPath, APP_FOLDER_NAME, new NaviInitListener() {

            @Override
            public void onAuthResult(int status, String msg) {
                if (status==0) {
                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败!"+msg;
                }
                NavigateActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(NavigateActivity.this, authinfo, Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void initSuccess() {
                Toast.makeText(NavigateActivity.this, "百度导航引擎初始化成功", Toast.LENGTH_LONG).show();
            }

            @Override
            public void initStart() {
                Toast.makeText(NavigateActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_LONG).show();
            }

            @Override
            public void initFailed() {
                Toast.makeText(NavigateActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_LONG).show();
            }
        }, ttsCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_POS_CODE && resultCode == RESULT_OK) {
            ToastUtils.showShort(NavigateActivity.this, "current position get!!!");

            //获取key
            start_place = data.getStringExtra("Position");
            tv_current_pos.setText(start_place);
            latLng_start = data.getParcelableExtra("LatLng");
            /*ToastUtils.showShort(NavigateActivity.this, String.valueOf(latLng_start.latitude) + "\n"
                + String.valueOf(latLng_start.longitude));*/

        } else if(requestCode == REQUEST_DES_CODE && resultCode == RESULT_OK) {
            ToastUtils.showShort(NavigateActivity.this, "destination get!!!");

            //获取key
            end_place = data.getStringExtra("Position");
            tv_destination.setText(end_place);
            latLng_end = data.getParcelableExtra("LatLng");
            /*ToastUtils.showShort(NavigateActivity.this, String.valueOf(latLng_end.latitude) + "\n"
                    + String.valueOf(latLng_end.longitude));*/
        }
    }

    private void initViews() {
        tv_current_pos.setOnClickListener(this);
        tv_current_pos.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent posIntent = new Intent(NavigateActivity.this, SearchPositionActivity.class);
                startActivityForResult(posIntent, REQUEST_POS_CODE);
                return true;
            }
        });

        tv_destination.setOnClickListener(this);
        tv_destination.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent posIntent = new Intent(NavigateActivity.this, SearchPositionActivity.class);
                startActivityForResult(posIntent, REQUEST_DES_CODE);
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;

            case R.id.start_search:
                if(TextUtils.isEmpty(tv_current_pos.getText())) {
                    ToastUtils.showShort(NavigateActivity.this, "Please input your starting point!");
                    break;
                }
                if(TextUtils.isEmpty(tv_destination.getText())) {
                    ToastUtils.showShort(NavigateActivity.this, "Please input your destination!");
                    break;
                }
                //initBNRoutePlan(latLng_start,latLng_end);
/*
                Intent intent = new Intent();
                intent.putExtra("StartLaTLng", latLng_start);
                intent.putExtra("EndLaTLng", latLng_end);
                if(NAV_CAR) {
                    intent.putExtra("NavMode", 1);//1代表驾车路线规划
                } else if(NAV_BUS) {
                    intent.putExtra("NavMode", 2);//2代表公交路线规划
                } else if(NAV_WALK) {
                    intent.putExtra("NavMode", 3);//3代表步行路线规划
                } else if(NAV_BIKE) {
                    intent.putExtra("NavMode", 4);//4代表骑车路线规划
                }
                setResult(RESULT_OK, intent);
                NavigateActivity.this.finish();
*/

                Intent intent = new Intent(NavigateActivity.this, NavigateGuideActivity.class);
                intent.putExtra("StartLaTLng", latLng_start);
                intent.putExtra("EndLaTLng", latLng_end);
                if(NAV_CAR) {
                    intent.putExtra("NavMode", 1);//1代表驾车路线规划
                } else if(NAV_BUS) {
                    intent.putExtra("NavMode", 2);//2代表公交路线规划
                } else if(NAV_WALK) {
                    intent.putExtra("NavMode", 3);//3代表步行路线规划
                } else if(NAV_BIKE) {
                    intent.putExtra("NavMode", 4);//4代表骑车路线规划
                }
                startActivity(intent);
                break;

            case R.id.tv_car:
                if(!NAV_CAR) {
                    tv_car.setTextColor(Color.parseColor("#FFFFFFFF"));
                    NAV_CAR = true;
                    if(NAV_BUS) {
                        tv_bus.setTextColor(Color.parseColor("#AAFFFFFF"));
                        NAV_BUS = false;
                    } else if(NAV_WALK) {
                        tv_walk.setTextColor(Color.parseColor("#AAFFFFFF"));
                        NAV_WALK = false;
                    } else if(NAV_BIKE) {
                        tv_bike.setTextColor(Color.parseColor("#AAFFFFFF"));
                        NAV_BIKE = false;
                    }
                }

                break;

            case R.id.tv_bus:
                if(!NAV_BUS) {
                    tv_bus.setTextColor(Color.parseColor("#FFFFFFFF"));
                    NAV_BUS = true;
                    if(NAV_CAR) {
                        tv_car.setTextColor(Color.parseColor("#AAFFFFFF"));
                        NAV_CAR = false;
                    } else if(NAV_WALK) {
                        tv_walk.setTextColor(Color.parseColor("#AAFFFFFF"));
                        NAV_WALK = false;
                    } else if(NAV_BIKE) {
                        tv_bike.setTextColor(Color.parseColor("#AAFFFFFF"));
                        NAV_BIKE = false;
                    }
                }

                break;

            case R.id.tv_walk:
                if(!NAV_WALK) {
                    tv_walk.setTextColor(Color.parseColor("#FFFFFFFF"));
                    NAV_WALK = true;
                    if(NAV_CAR) {
                        tv_car.setTextColor(Color.parseColor("#AAFFFFFF"));
                        NAV_CAR = false;
                    } else if(NAV_BUS) {
                        tv_bus.setTextColor(Color.parseColor("#AAFFFFFF"));
                        NAV_BUS = false;
                    } else if(NAV_BIKE) {
                        tv_bike.setTextColor(Color.parseColor("#AAFFFFFF"));
                        NAV_BIKE = false;
                    }
                }

                break;

            case R.id.tv_bike:
                if(!NAV_BIKE) {
                    tv_bike.setTextColor(Color.parseColor("#FFFFFFFF"));
                    NAV_BIKE = true;
                    if(NAV_CAR) {
                        tv_car.setTextColor(Color.parseColor("#AAFFFFFF"));
                        NAV_CAR = false;
                    } else if(NAV_BUS) {
                        tv_bus.setTextColor(Color.parseColor("#AAFFFFFF"));
                        NAV_BUS = false;
                    } else if(NAV_WALK) {
                        tv_walk.setTextColor(Color.parseColor("#AAFFFFFF"));
                        NAV_WALK = false;
                    }
                }

                break;

            case R.id.tv_current_pos:
                Intent posIntent = new Intent(this, SearchPositionActivity.class);
                startActivityForResult(posIntent, REQUEST_POS_CODE);
                break;

            case R.id.tv_destination:
                Intent desIntent = new Intent(this, SearchPositionActivity.class);
                startActivityForResult(desIntent, REQUEST_DES_CODE);
                break;

            default:
                break;
        }
    }

    private void initBNRoutePlan(LatLng latLng_start, LatLng latLng_end) {
        BNRoutePlanNode startNode = new BNRoutePlanNode(latLng_start.longitude, latLng_start.latitude, start_place, null, BNRoutePlanNode.CoordinateType.BD09LL);//根据得到的起点的信息创建起点节点
        BNRoutePlanNode endNode = new BNRoutePlanNode(latLng_end.longitude, latLng_end.latitude, end_place, null, BNRoutePlanNode.CoordinateType.BD09LL);//根据得到的终点的信息创建终点节点
        if (startNode!=null&&endNode!=null) {
            ArrayList<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(startNode);//将起点和终点加入节点集合中
            list.add(endNode);
            BaiduNaviManager.getInstance().launchNavigator(NavigateActivity.this, list, 1, true, new MyRoutePlanListener(list) );
        }
    }

    class MyRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private ArrayList<BNRoutePlanNode>mList=null;

        public MyRoutePlanListener(ArrayList<BNRoutePlanNode> list) {
            mList = list;
        }

        @Override
        public void onJumpToNavigator() {
            Intent intent=new Intent(NavigateActivity.this, PathGuideActivity.class);
            intent.putExtra(ROUTE_PLAN_NODE, mList);//将得到所有的节点集合传入到导航的Activity中去
            startActivity(intent);
        }

        @Override
        public void onRoutePlanFailed() {
            Toast.makeText(NavigateActivity.this, "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void setTranslucentStatus() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            Window win=getWindow();
            WindowManager.LayoutParams winParams=win.getAttributes();
            final int bits=WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
            winParams.flags |=bits;
            win.setAttributes(winParams);
        }
        SystemStatusManager tintManager = new SystemStatusManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setStatusBarTintResource(0);
        tintManager.setNavigationBarTintEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
