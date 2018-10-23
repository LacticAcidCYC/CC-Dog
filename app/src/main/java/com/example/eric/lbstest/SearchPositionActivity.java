package com.example.eric.lbstest;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.eric.lbstest.adapter.SearchPositionAdapter;
import com.example.eric.lbstest.utils.ActivityCollector;
import com.example.eric.lbstest.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchPositionActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener {

    private Context mContext;

    //附近地点列表
    @BindView(R.id.lv_locator_search_position)
    ListView lv_locator_search_position;

    //列表适配器
    private SearchPositionAdapter searchPositionAdapter;

    //列表数据
    private List<SuggestionResult.SuggestionInfo> datas;

    //进度条
    @BindView(R.id.pb_location_search_load_bar)
    ProgressBar pb_location_search_load_bar;

    //建议查询
    private SuggestionSearch mSuggestionSearch;

    //输入框
    @BindView(R.id.et_search)
    EditText et_search;

    //返回
    @BindView(R.id.fl_search_back)
    FrameLayout fl_search_back;

    //发送
    @BindView(R.id.tv_search_send)
    TextView tv_search_send;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_position);
        ButterKnife.bind(this);
        ActivityCollector.addActivity(this);

        initViews();
    }

    private void initViews() {
        mContext = SearchPositionActivity.this;

        // 建议查询
        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult res) {
                /**
                 * 获取搜索的内容
                 */
                pb_location_search_load_bar.setVisibility(View.GONE);
                if (res == null || res.getAllSuggestions() == null) {
                    ToastUtils.showShort(mContext, "No results found!");
                    return;
                }
                //获取在线建议检索结果
                if (datas != null) {
                    datas.clear();
                    for (SuggestionResult.SuggestionInfo suggestionInfos : res.getAllSuggestions()) {
                        datas.add(suggestionInfos);
                    }
                    searchPositionAdapter.notifyDataSetChanged();
                }
            }
        });

        // 列表初始化
        datas = new ArrayList();
        searchPositionAdapter = new SearchPositionAdapter(this, datas);
        lv_locator_search_position.setAdapter(searchPositionAdapter);

        // 注册监听
        lv_locator_search_position.setOnItemClickListener(this);
        fl_search_back.setOnClickListener(this);
        tv_search_send.setOnClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        searchPositionAdapter.setSelectSearchItemIndex(position);
        searchPositionAdapter.notifyDataSetChanged();

        Intent intent = new Intent();
        // 设置坐标
        intent.putExtra("LatLng", datas.get(position).pt);
        intent.putExtra("Position", datas.get(position).key);
        intent.putExtra("City", datas.get(position).city);
        setResult(RESULT_OK, intent);

        SearchPositionActivity.this.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fl_search_back:
                SearchPositionActivity.this.finish();
                break;
            case R.id.tv_search_send:
                if (!TextUtils.isEmpty(et_search.getText().toString())) {
                    pb_location_search_load_bar.setVisibility(View.VISIBLE);
                    // 根据输入框的内容，进行搜索
                    mSuggestionSearch.requestSuggestion(new SuggestionSearchOption().keyword(et_search.getText().toString()).city(""));
                } else {
                    ToastUtils.showLong(mContext, "Please input the place");
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSuggestionSearch.destroy();
        ActivityCollector.removeActivity(this);
    }
}
