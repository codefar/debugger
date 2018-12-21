package com.su.debugger.ui.test.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.su.debugger.R;
import com.su.debugger.db.DbInfoProvider;
import com.su.debugger.entity.TableColumn;
import com.su.debugger.ui.test.BaseAppCompatActivity;
import com.su.debugger.widget.recycler.BaseRecyclerAdapter;
import com.su.debugger.widget.recycler.PreferenceItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class TableInfoActivity extends BaseAppCompatActivity {
    public static final String TAG = TableInfoActivity.class.getSimpleName();
    private String mDatabaseName;
    private String mTableName;
    private String mTableSql;
    private RecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_template_recycler_list);
        Intent intent = getIntent();
        mTableSql = intent.getStringExtra("sql");
        mDatabaseName = intent.getStringExtra("database_name");
        mTableName = intent.getStringExtra("table_name");
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        recyclerView.addItemDecoration(decoration);
        mAdapter = new RecyclerViewAdapter();
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(mTableName + "结构");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<TableColumn> columns = new ArrayList<>();
        DbInfoProvider dbInfoProvider = DbInfoProvider.getInstance(this, mDatabaseName);
        Cursor cursor = dbInfoProvider.getTableInfo(mTableName);
        if (cursor == null) {
            mAdapter.updateData(columns);
            return;
        }
        cursor.moveToFirst();
        do {
            TableColumn column = new TableColumn();
            column.setPk(cursor.getInt(cursor.getColumnIndex("pk")) == 1);
            column.setCid(cursor.getInt(cursor.getColumnIndex("cid")));
            column.setName(cursor.getString(cursor.getColumnIndex("name")));
            column.setType(cursor.getString(cursor.getColumnIndex("type")));
            column.setNotNull(cursor.getInt(cursor.getColumnIndexOrThrow("notnull")) == 1);
            columns.add(column);
        } while (cursor.moveToNext());
        mAdapter.updateData(columns);
    }

    public void info(@NonNull MenuItem item) {
        new AlertDialog.Builder(this)
                .setTitle("建表语句")
                .setMessage(mTableSql)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }

    @Override
    public int menuRes() {
        return R.menu.debugger_info_menu;
    }

    private class RecyclerViewAdapter extends BaseRecyclerAdapter<TableColumn> {

        RecyclerViewAdapter() {
            super(new ArrayList<>());
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.debugger_item_column;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            TableColumn item = getData().get(position);
            TextView cidView = holder.getView(R.id.cid);
            TextView nameView = holder.getView(R.id.name);
            View pkView = holder.getView(R.id.pk);
            TextView typeView = holder.getView(R.id.type);
            View notnullView = holder.getView(R.id.notnull);
            cidView.setText("cid: " + item.getCid());
            nameView.setText("name: " + item.getName());
            typeView.setText("type: " + item.getType());
            pkView.setVisibility(item.isPk() ? View.VISIBLE : View.GONE);
            notnullView.setVisibility(item.isNotNull() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
