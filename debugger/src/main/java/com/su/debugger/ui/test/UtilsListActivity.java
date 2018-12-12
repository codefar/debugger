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
                "OkHttp拦截器，可以在通知栏方便的看到网络请求REQUEST/RESPONSE所有情况。目前不支持多进程，也不支持预览请求中的二进制文件",
                "https://github.com/jgilfelt/chuck"));
        mInfoList.add(new OpenSourceInfo("okhttp-logging-interceptor",
                "square",
                "官方OkHttp拦截器，在logcat中输出网络请求REQUEST/RESPONSE所有情况",
                "https://github.com/square/okhttp/tree/master/okhttp-logging-interceptor"));
        mInfoList.add(new OpenSourceInfo("leakcanary",
                "square",
                "android/java内存泄漏检测工具",
                "https://github.com/square/leakcanary"));
        mInfoList.add(new OpenSourceInfo("AndroidPerformanceMonitor（BlockCanary）",
                "markzhai",
                "BlockCanary是一个Android平台的一个非侵入式的性能监控组件，应用只需要实现一个抽象类，提供一些该组件需要的上下文环境，就可以在平时使用应用的时候检测主线程上的各种卡慢问题，并通过组件提供的各种信息分析出原因并进行修复。\n" +
                        "取名为BlockCanary则是为了向LeakCanary致敬，顺便本库的UI部分是从LeakCanary改来的，之后可能会做一些调整。",
                "https://github.com/markzhai/AndroidPerformanceMonitor"));
        mInfoList.add(new OpenSourceInfo("ViewServer",
                "romainguy",
                "可以使user版使用HierarchyViewer工具",
                "https://github.com/romainguy/ViewServer"));
        mInfoList.add(new OpenSourceInfo("dexcount-gradle-plugin",
                "KeepSafe",
                "用于监视字段/方法引用数限制的gradle插件。\n不支持以下版本的Android build工具3.0.0-alpha1 - 3.0.0-beta5。在此之下或之上的版本都支持。",
                "https://github.com/KeepSafe/dexcount-gradle-plugin"));
        mInfoList.add(new OpenSourceInfo("android-classyshark",
                "google",
                "二进制文件检查工具。支持库文件（.dex, .aar, .so），可执行文件（.apk, .jar, .class）和所有Android二进制xml（AndroidManifest、资源、布局）等",
                "https://github.com/google/android-classyshark"));
        mInfoList.add(new OpenSourceInfo("debugger",
                "su",
                "android调试工具，可搭配okhttp进行mock，可模拟停机维护状态，可以全局切换域名。" +
                        "可修改权限，查看四大组件信息，启动activity。可查看并修改SharedPreference。" +
                        "可查看通知设置，系统代理。" +
                        "可调试WebView，可调试通过JavascriptInterface注解暴露要给WebView的接口，可通过rhino调试js。" +
                        "可查看手机基本信息（屏幕、系统、网络、硬件、各类ID）",
                "https://github.com/su1216/debugger"));
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
