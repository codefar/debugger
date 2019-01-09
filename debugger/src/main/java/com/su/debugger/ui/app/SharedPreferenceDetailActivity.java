package com.su.debugger.ui.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.su.debugger.R;
import com.su.debugger.ui.BaseAppCompatActivity;
import com.su.debugger.utils.SpHelper;
import com.su.debugger.widget.recycler.BaseRecyclerAdapter;
import com.su.debugger.widget.recycler.PreferenceItemDecoration;
import com.su.debugger.widget.recycler.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SharedPreferenceDetailActivity extends BaseAppCompatActivity implements RecyclerItemClickListener.OnItemClickListener {

    public static final String TAG = SharedPreferenceDetailActivity.class.getSimpleName();
    private Resources mResources;
    private String mSharedPreferenceName;
    private RecyclerViewAdapter mAdapter;
    private int mSelected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_template_recycler_list);
        mResources = getResources();
        mSharedPreferenceName = getIntent().getStringExtra("name");
        mAdapter = new RecyclerViewAdapter();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new PreferenceItemDecoration(this, 0, 0));
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(mSharedPreferenceName + ".xml");
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        List<Item> list = new ArrayList<>();
        SharedPreferences sp = getSharedPreferences(mSharedPreferenceName, Context.MODE_PRIVATE);
        Map<String, ?> map = sp.getAll();
        Set<? extends Map.Entry<String, ?>> entrySet = map.entrySet();
        for (Map.Entry<String, ?> entry : entrySet) {
            Item item = new Item();
            item.setKey(entry.getKey());
            Class<?> clazz = entry.getValue().getClass();
            item.setValueClass(clazz);
            if (Set.class.isAssignableFrom(clazz)) {
                item.setValue(JSON.toJSONString(entry.getValue()));
            } else {
                item.setValue(String.valueOf(entry.getValue()));
            }
            list.add(item);
        }
        mAdapter.updateData(list);
    }

    private void createOrUpdate(Item item, String newValue) {
        SharedPreferences sp = getSharedPreferences(mSharedPreferenceName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        Class<?> clazz = item.getValueClass();
        String key = item.getKey();
        if (clazz == Integer.class) {
            editor.putInt(key, Integer.parseInt(newValue));
        } else if (clazz == Long.class) {
            editor.putLong(key, Long.parseLong(newValue));
        } else if (clazz == Boolean.class) {
            editor.putBoolean(key, Boolean.parseBoolean(newValue));
        } else if (clazz == Float.class) {
            editor.putFloat(key, Float.parseFloat(newValue));
        } else if (clazz == String.class) {
            editor.putString(key, newValue);
        } else if (Set.class.isAssignableFrom(clazz)) {
            List<String> list = JSON.parseArray(newValue, String.class);
            Set<String> set = new HashSet<>(list);
            editor.putStringSet(key, set);
        }
        editor.commit();
    }

    private void classDialog() {
        new AlertDialog.Builder(this)
                .setTitle("类型")
                .setSingleChoiceItems(R.array.debugger_sp_class, 0, (dialog, which) -> mSelected = which)
                .setPositiveButton(R.string.debugger_confirm, (dialog, which) -> {
                    Item item = new Item();
                    try {
                        item.setValueClass(Class.forName(mResources.getStringArray(R.array.debugger_sp_class)[mSelected]));
                        keyDialog(item);
                    } catch (ClassNotFoundException e) {
                        Log.w(TAG, e);
                    }
                })
                .setNegativeButton(R.string.debugger_cancel, null)
                .show();
    }

    private void keyDialog(final Item item) {
        final EditText inputView = new EditText(this);
        inputView.setText(item.getValue());
        new AlertDialog.Builder(this)
                .setTitle("key")
                .setView(inputView)
                .setPositiveButton(R.string.debugger_confirm, (dialog, which) -> {
                    String key = inputView.getText().toString();
                    if (TextUtils.isEmpty(key)) {
                        Toast.makeText(SharedPreferenceDetailActivity.this, "key不可以为空", Toast.LENGTH_SHORT).show();
                        keyDialog(item);
                        return;
                    }
                    item.setKey(key);
                    valueDialog(item);
                })
                .setNegativeButton(R.string.debugger_cancel, null)
                .show();
    }

    private void valueDialog(Item item) {
        final EditText inputView = new EditText(this);
        inputView.setText(item.getValue());
        new AlertDialog.Builder(this)
                .setTitle("key: " + item.getKey())
                .setView(inputView)
                .setPositiveButton(R.string.debugger_confirm, (dialog, which) -> {
                    createOrUpdate(item, inputView.getText().toString());
                    loadData();
                })
                .setNegativeButton(R.string.debugger_cancel, null)
                .show();
    }

    @Override
    public void onItemClick(View view, int position) {
        if (TextUtils.equals(SpHelper.NAME, mSharedPreferenceName)) {
            Toast.makeText(this, "禁止修改debugger自身的shared preference文件", Toast.LENGTH_LONG).show();
            return;
        }
        Item item = mAdapter.getData().get(position);
        valueDialog(item);
    }

    private static class RecyclerViewAdapter extends BaseRecyclerAdapter<Item> {

        private RecyclerViewAdapter() {
            super(new ArrayList<>());
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.debugger_item_sp_entry;
        }

        @Override
        protected void bindData(@NonNull BaseViewHolder holder, int position, int itemType) {
            Item item = getData().get(position);
            ((TextView) holder.getView(R.id.key)).setText(item.getKey());
            ((TextView) holder.getView(R.id.value)).setText(item.getValue());
            ((TextView) holder.getView(R.id.value_class)).setText(item.getValueClass().getName());
        }
    }

    private static class Item {
        private String key;
        private String value;
        private Class<?> valueClass;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Class<?> getValueClass() {
            return valueClass;
        }

        public void setValueClass(Class<?> valueClass) {
            this.valueClass = valueClass;
        }
    }

    public void add(@NonNull MenuItem item) {
        classDialog();
    }

    @Override
    public int menuRes() {
        if (TextUtils.equals(SpHelper.NAME, mSharedPreferenceName)) {
            return super.menuRes();
        }
        return R.menu.debugger_add_menu;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
