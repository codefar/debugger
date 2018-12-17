package com.su.debugger.ui.test;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.su.debugger.AppHelper;
import com.su.debugger.R;
import com.su.debugger.entity.OpenSourceInfo;
import com.su.debugger.utils.SearchableHelper;
import com.su.debugger.widget.recycler.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by su on 2018/1/13.
 */

public class UtilsListActivity extends BaseAppCompatActivity implements RecyclerItemClickListener.OnItemClickListener, SearchView.OnQueryTextListener {

    private static final String TAG = UtilsListActivity.class.getSimpleName();
    private List<OpenSourceInfo> mInfoList = new ArrayList<>();
    private List<OpenSourceInfo> mFilterInfoList = new ArrayList<>();
    private RecyclerViewAdapter mAdapter;
    private SearchableHelper mSearchableHelper = new SearchableHelper(OpenSourceInfo.class);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_template_recycler_list);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
        initData();
        mAdapter = new RecyclerViewAdapter(this);
        recyclerView.setAdapter(mAdapter);
        filter("");
    }

    private void initData() {
        mInfoList.add(new OpenSourceInfo("chuck",
                "jgilfelt",
                "An in-app HTTP inspector for Android OkHttp clients",
                "https://github.com/jgilfelt/chuck"));
        mInfoList.add(new OpenSourceInfo("okhttp-logging-interceptor",
                "square",
                "An OkHttp interceptor which logs HTTP request and response data",
                "https://github.com/square/okhttp/tree/master/okhttp-logging-interceptor"));
        mInfoList.add(new OpenSourceInfo("leakcanary",
                "square",
                "A memory leak detection library for Android and Java",
                "https://github.com/square/leakcanary"));
        mInfoList.add(new OpenSourceInfo("AndroidPerformanceMonitor（BlockCanary）",
                "markzhai",
                "A transparent ui-block detection library for Android. (known as BlockCanary)",
                "https://github.com/markzhai/AndroidPerformanceMonitor"));
        mInfoList.add(new OpenSourceInfo("ViewServer",
                "romainguy",
                "Local server for Android's HierarchyViewer",
                "https://github.com/romainguy/ViewServer"));
        mInfoList.add(new OpenSourceInfo("dexcount-gradle-plugin",
                "KeepSafe",
                "A Gradle plugin to report the number of method references in your APK on every build",
                "https://github.com/KeepSafe/dexcount-gradle-plugin"));
        mInfoList.add(new OpenSourceInfo("Apktool",
                "iBotPeaches",
                "A tool for reverse engineering Android apk files",
                "https://github.com/iBotPeaches/Apktool"));
        mInfoList.add(new OpenSourceInfo("android-classyshark",
                "google",
                "Analyse 3rd party SDKs in your Android app (APK) ",
                "https://github.com/google/android-classyshark"));
        mInfoList.add(new OpenSourceInfo("stetho",
                "facebook",
                "Stetho is a debug bridge for Android applications, enabling the powerful Chrome Developer Tools and much more",
                "https://github.com/facebook/stetho"));
        mInfoList.add(new OpenSourceInfo("acra",
                "ACRA",
                "Application Crash Reports for Android",
                "https://github.com/ACRA/acra"));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSearchableHelper.initSearchToolbar(mToolbar, this);
        setTitle("debug相关开源库列表");
    }

    @Override
    public int menuRes() {
        return R.menu.debugger_search_menu;
    }

    private void filter(String str) {
        mFilterInfoList.clear();
        mSearchableHelper.clear();
        if (TextUtils.isEmpty(str)) {
            mFilterInfoList.addAll(mInfoList);
            mAdapter.notifyDataSetChanged();
            return;
        }
        for (OpenSourceInfo search : mInfoList) {
            if (mSearchableHelper.isConformSplitFilter(str, search)) {
                mFilterInfoList.add(search);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, int position) {
        OpenSourceInfo info = mFilterInfoList.get(position);
        if (!TextUtils.isEmpty(info.getUrl())) {
            AppHelper.startWebView(this, info.getName(), info.getUrl(), true);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filter(newText);
        return false;
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {
        private final LayoutInflater mLayoutInflater;

        RecyclerViewAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(mLayoutInflater.inflate(R.layout.debugger_item_open_source_info, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            OpenSourceInfo item = mFilterInfoList.get(position);
            holder.desc.setText(item.getDesc());
            holder.name.setText(item.getName());
            mSearchableHelper.refreshFilterColor(holder.name, position, "name");
            mSearchableHelper.refreshFilterColor(holder.desc, position, "desc");
        }

        @Override
        public int getItemCount() {
            return mFilterInfoList.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView desc;
        private TextView name;

        ViewHolder(View view) {
            super(view);
            desc = view.findViewById(R.id.desc);
            name = view.findViewById(R.id.name);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
