package com.su.debugger.ui.test.app;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.su.debugger.R;
import com.su.debugger.db.DbInfoProvider;
import com.su.debugger.entity.Table;
import com.su.debugger.ui.test.BaseAppCompatActivity;
import com.su.debugger.widget.recycler.BaseRecyclerAdapter;
import com.su.debugger.widget.recycler.PreferenceItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class DatabaseListActivity extends BaseAppCompatActivity {
    public static final String TAG = DatabaseListActivity.class.getSimpleName();
    private List<String> mGroupList = new ArrayList<>();
    private List<Database> mDatabaseList = new ArrayList<>();
    private DatabaseAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_template_recycler_list);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new DatabaseAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        PreferenceItemDecoration decoration = new PreferenceItemDecoration(this, 0, 0);
        recyclerView.addItemDecoration(decoration);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("数据库列表");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        mGroupList.clear();
        mDatabaseList.clear();
        String[] dbList = getApplicationContext().databaseList();
        if (dbList == null || dbList.length == 0) {
            return;
        }

        for (String dbName : dbList) {
            if (dbName.endsWith("-journal")) {
                continue;
            }
            mGroupList.add(dbName);
            DbInfoProvider dbInfoProvider = DbInfoProvider.getInstance(this, dbName);
            int version = dbInfoProvider.getDatabaseVersion();
            List<Table> tables = getTables(dbInfoProvider.getAllTables());
            Database database = new Database(dbName, version, tables);
            mDatabaseList.add(database);
            dbInfoProvider.closeDb();
        }
        mAdapter.updateData(mGroupList, mDatabaseList);
    }

    private List<Table> getTables(Cursor cursor) {
        List<Table> list = new ArrayList<>();
        if (cursor == null) {
            return list;
        }
        cursor.moveToFirst();
        do {
            String tableName = cursor.getString(cursor.getColumnIndex("name"));
            String tableSql = cursor.getString(cursor.getColumnIndex("sql"));
            Table table = new Table();
            table.setTableName(tableName);
            table.setTableSql(tableSql);
            list.add(table);
        } while (cursor.moveToNext());
        cursor.close();
        return list;
    }

    private class DatabaseAdapter extends RecyclerView.Adapter<BaseRecyclerAdapter.BaseViewHolder> {

        private List<String> mGroupList = new ArrayList<>();
        private List<Database> mDatabaseList = new ArrayList<>();

        private void updateData(@NonNull List<String> groupList, @NonNull List<Database> databaseList) {
            mGroupList = new ArrayList<>(groupList);
            mDatabaseList = new ArrayList<>(databaseList);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public BaseRecyclerAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(viewType), parent, false);
            return new BaseRecyclerAdapter.BaseViewHolder(view);
        }

        public int getLayoutId(int itemType) {
            if (itemType == BaseRecyclerAdapter.ITEM_TYPE_GROUP) {
                return R.layout.debugger_item_group_database;
            } else {
                return R.layout.debugger_item_table;
            }
        }

        private void bindGroupData(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
            int groupIndex = getPositions(position)[0];
            final String dbPath = mGroupList.get(groupIndex);
            final Database database = mDatabaseList.get(groupIndex);
            TextView databaseNameView = holder.getView(R.id.database_name);
            TextView versionView = holder.getView(R.id.version);
            TextView tablesView = holder.getView(R.id.tables);
            databaseNameView.setText(dbPath);
            versionView.setText("version: " + database.version);
            tablesView.setText("tables: " + database.tableCount);
            holder.getView(R.id.arrow).setSelected(!database.collapse);
            holder.itemView.setOnClickListener(v -> {
                database.collapse = !database.collapse;
                holder.getView(R.id.arrow).setSelected(!database.collapse);
                notifyDataSetChanged();
            });
        }

        private void bindChildData(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
            int[] positions = getPositions(position);
            String dbName = mGroupList.get(positions[0]);
            Table table = mDatabaseList.get(positions[0]).tableList.get(positions[1]);
            TextView tableNameView = holder.getView(R.id.table_name);
            tableNameView.setText(table.getTableName());
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(DatabaseListActivity.this, TableInfoActivity.class);
                intent.putExtra("sql", table.getTableSql());
                intent.putExtra("database_name", dbName);
                intent.putExtra("table_name", table.getTableName());
                startActivity(intent);
            });
        }

        @Override
        public void onBindViewHolder(@NonNull BaseRecyclerAdapter.BaseViewHolder holder, int position) {
            int type = getItemViewType(position);
            if (type == BaseRecyclerAdapter.ITEM_TYPE_GROUP) {
                bindGroupData(holder, position);
            } else {
                bindChildData(holder, position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            int pointer = -1;
            for (Database functions : mDatabaseList) {
                pointer++;
                if (pointer == position) {
                    return BaseRecyclerAdapter.ITEM_TYPE_GROUP;
                }
                int childrenSize = functions.collapse ? 0 : functions.tableList.size();
                pointer += childrenSize;
                if (pointer >= position) {
                    return BaseRecyclerAdapter.ITEM_TYPE_NORMAL;
                }
            }
            throw new IllegalStateException("wrong state");
        }

        private int[] getPositions(int position) {
            int[] positions = new int[2];
            int pointer = -1;
            int groupPosition = -1;
            int childPosition = -1;
            positions[0] = groupPosition;
            positions[1] = childPosition;
            for (Database functions : mDatabaseList) {
                pointer++;
                groupPosition++;
                positions[0] = groupPosition;
                int childrenSize = functions.collapse ? 0 : functions.tableList.size();
                if (pointer + childrenSize >= position) {
                    childPosition = position - pointer - 1;
                    positions[1] = childPosition;
                    return positions;
                }
                pointer += childrenSize;
            }
            return positions;
        }

        @Override
        public int getItemCount() {
            int size = 0;
            for (Database functions : mDatabaseList) {
                size++;
                int childrenSize = functions.collapse ? 0 : functions.tableList.size();
                size += childrenSize;
            }
            return size;
        }
    }

    private static class Database {
        private String databasePath;
        private int version;
        private List<Table> tableList;
        private int tableCount;
        private boolean collapse;

        public Database(String databasePath, int version, List<Table> tableList) {
            this.databasePath = databasePath;
            this.version = version;
            this.tableList = tableList;
            if (tableList != null) {
                tableCount = tableList.size();
            }
        }

        @Override
        public String toString() {
            return "Database{" +
                    "databasePath='" + databasePath + '\'' +
                    ", version=" + version +
                    ", tableList=" + tableList +
                    ", tableCount=" + tableCount +
                    '}';
        }
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
