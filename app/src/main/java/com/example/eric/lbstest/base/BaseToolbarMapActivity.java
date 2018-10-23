package com.example.eric.lbstest.base;

import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.eric.lbstest.R;

import butterknife.BindView;

/**
 * Created by RSJ on 2017/4/20.
 * 基础类，并在这里使用Toolbar去掉了ActionBar
 * 1.所以配置文件中的加入android:theme="@style/AppTheme.NoActionBar"
 * 2.必须要含有Toolbar,且android:id="@+id/toolbar"
 */

public abstract class BaseToolbarMapActivity extends BaseMapActivity {

    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        /*
        toolbar.setNavigationIcon(R.drawable.ic_menu_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/
    }
}
