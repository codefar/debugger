package com.su.debugger.ui.test.app;

import android.content.Context;
import android.content.pm.FeatureInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.TextView;

import com.su.debugger.AppHelper;
import com.su.debugger.R;
import com.su.debugger.ui.test.BaseAppCompatActivity;
import com.su.debugger.widget.recycler.BaseRecyclerAdapter;

public class FeatureListActivity extends BaseAppCompatActivity {

    private static final String TAG = FeatureListActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_template_recycler_list);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(new RecyclerViewAdapter(this));
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("Feature需求列表");
    }


    private static class RecyclerViewAdapter extends BaseRecyclerAdapter<FeatureInfo> {

        private RecyclerViewAdapter(Context context) {
            super(AppHelper.getRequiredFeatures(context));
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.debugger_item_required_feature;
        }

        @Override
        protected void bindData(BaseViewHolder holder, int position, int itemType) {
            FeatureInfo featureInfo = getData().get(position);
            TextView nameView = (TextView) holder.getView(R.id.key);
            TextView versionView = (TextView) holder.getView(R.id.value);
            String name = featureInfo.name;
            if (TextUtils.isEmpty(name)) {
                name = "OpenGL ES";
                versionView.setText(featureInfo.getGlEsVersion());
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && featureInfo.version > 0) {
                    versionView.setText(String.valueOf(featureInfo.version));
                } else {
                    versionView.setText("");
                }
            }
            nameView.setText(name);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
