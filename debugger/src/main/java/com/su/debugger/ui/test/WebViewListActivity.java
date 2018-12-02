package com.su.debugger.ui.test;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.su.debugger.AppHelper;
import com.su.debugger.DebuggerSupplier;
import com.su.debugger.R;
import com.su.debugger.entity.NoteWebViewEntity;
import com.su.debugger.utils.GeneralInfoHelper;
import com.su.debugger.utils.IOUtil;
import com.su.debugger.utils.SearchableHelper;
import com.su.debugger.utils.UiHelper;
import com.su.debugger.widget.recycler.PreferenceItemDecoration;
import com.su.debugger.widget.recycler.RecyclerItemClickListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by su on 17-5-2.
 */
public class WebViewListActivity extends BaseAppCompatActivity implements RecyclerItemClickListener.OnItemClickListener, SearchView.OnQueryTextListener {
    private static final String TAG = WebViewListActivity.class.getSimpleName();
    private String mTitle;

    private RecyclerViewAdapter mAdapter;
    private List<NoteWebViewEntity> mNotes = new ArrayList<>();
    private List<NoteWebViewEntity> mFilterNotes = new ArrayList<>();
    private SearchableHelper mSearchableHelper = new SearchableHelper(NoteWebViewEntity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_template_recycler_list);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            mTitle = intent.getStringExtra("title");
        } else {
            mTitle = savedInstanceState.getString("title");
        }
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new PreferenceItemDecoration(this, UiHelper.dp2px(13, getResources().getDisplayMetrics()), 0));
        mAdapter = new RecyclerViewAdapter(this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
        makeData();
        filter("");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mSearchableHelper.initSearchToolbar(mToolbar, this);
        setTitle(mTitle);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("title", mTitle);
    }

    private void makeData() {
        BufferedReader reader = null;
        String str = null;
        StringBuilder buf = new StringBuilder();
        AssetManager manager = getAssets();
        try {
            reader = new BufferedReader(new InputStreamReader(manager.open("generated/webView.json"), "UTF-8"));
            while ((str = reader.readLine()) != null) {
                buf.append(str);
            }
            str = buf.toString();
        } catch (IOException e) {
            Toast.makeText(GeneralInfoHelper.getContext(), "请检查文件assets/generated/webView.json", Toast.LENGTH_LONG).show();
            Log.w(TAG, e);
        } finally {
            IOUtil.close(reader);
        }

        if (!TextUtils.isEmpty(str)) {
            List<NoteWebViewEntity> list = JSON.parseArray(str, NoteWebViewEntity.class);
            mNotes.addAll(list);
        }
        Collections.sort(mNotes);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(View view, int position) {
        DebuggerSupplier debugger = DebuggerSupplier.getInstance();
        NoteWebViewEntity noteWebView = mFilterNotes.get(position);
        if (noteWebView.isNeedLogin() && !debugger.isLogin()) {
            Toast.makeText(GeneralInfoHelper.getContext(), "登录可访问此页面", Toast.LENGTH_LONG).show();
        } else {
            AppHelper.startWebView(this, noteWebView);
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

    private void filter(String str) {
        mFilterNotes.clear();
        mSearchableHelper.clear();
        if (TextUtils.isEmpty(str)) {
            mFilterNotes.addAll(mNotes);
            mAdapter.notifyDataSetChanged();
            return;
        }
        for (NoteWebViewEntity search : mNotes) {
            if (mSearchableHelper.isConformSplitFilter(str, search)) {
                mFilterNotes.add(search);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {
        private final LayoutInflater mLayoutInflater;

        RecyclerViewAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(mLayoutInflater.inflate(R.layout.debugger_item_web_view_url_info, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
            NoteWebViewEntity item = mFilterNotes.get(position);
            holder.url.setText(item.getUrl());
            holder.desc.setText(item.getDescription());
            mSearchableHelper.refreshFilterColor(holder.url, position, "url");
            mSearchableHelper.refreshFilterColor(holder.desc, position, "description");
        }

        @Override
        public int getItemCount() {
            return mFilterNotes.size();
        }
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView url;
        private TextView desc;

        ViewHolder(View view) {
            super(view);
            url = view.findViewById(R.id.url);
            desc = view.findViewById(R.id.desc);
        }
    }

    @Override
    public int menuRes() {
        return R.menu.debugger_search_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
