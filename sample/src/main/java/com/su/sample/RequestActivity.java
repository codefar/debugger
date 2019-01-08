package com.su.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by su on 18-1-3.
 */

public class RequestActivity extends BaseAppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_main);
        TextView textView = findViewById(R.id.text);
        textView.setText("开启数据模拟或者停机维护后，此页面数据返回结果则变为相应模拟数据");
        textView.setText(R.string.app_name);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.post(() -> {
            mSwipeRefreshLayout.setRefreshing(true);
            onRefresh();
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("网络请求测试");
    }

    private void test() {
        Request.Builder builder = new Request.Builder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .url(Url.BANNER);
        Request request = builder.build();
        OkHttpClient client = RequestHelper.getClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Toast.makeText(RequestActivity.this, "网络错误", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    Object object;
                    try {
                        object = JSON.toJSONString(JSON.parse(result), true);
                    } catch (JSONException e) {
                        object = result;
                    }
                    String text = object == null ? "" : object.toString();
                    runOnUiThread(() -> {
                        mSwipeRefreshLayout.setRefreshing(false);
                        ((TextView) findViewById(R.id.content)).setText(text);
                    });
                } else {
                    Toast.makeText(RequestActivity.this, "请求错误", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        test();
    }
}
