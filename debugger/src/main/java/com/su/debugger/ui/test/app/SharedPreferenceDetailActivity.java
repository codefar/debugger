package com.su.debugger.ui.test.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.su.debugger.R;
import com.su.debugger.ui.test.BaseAppCompatActivity;
import com.su.debugger.utils.SpHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SharedPreferenceDetailActivity extends BaseAppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String TAG = SharedPreferenceDetailActivity.class.getSimpleName();
    private Resources mResources;
    private String mSharedPreferenceName;
    private ItemAdapter mAdapter;
    private int mSelected;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_template_page_list_view);
        mResources = getResources();
        mSharedPreferenceName = getIntent().getStringExtra("name");
        mAdapter = new ItemAdapter(this);
        ListView listView = findViewById(R.id.list_view);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
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
                .setSingleChoiceItems(R.array.sp_class, 0, (dialog, which) -> mSelected = which)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    Item item = new Item();
                    try {
                        item.setValueClass(Class.forName(mResources.getStringArray(R.array.sp_class)[mSelected]));
                        keyDialog(item);
                    } catch (ClassNotFoundException e) {
                        Log.w(TAG, e);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void keyDialog(final Item item) {
        final EditText inputView = new EditText(this);
        inputView.setText(item.getValue());
        new AlertDialog.Builder(this)
                .setTitle("key")
                .setView(inputView)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    String key = inputView.getText().toString();
                    if (TextUtils.isEmpty(key)) {
                        Toast.makeText(SharedPreferenceDetailActivity.this, "key不可以为空", Toast.LENGTH_SHORT).show();
                        keyDialog(item);
                        return;
                    }
                    item.setKey(key);
                    valueDialog(item);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void valueDialog(Item item) {
        final EditText inputView = new EditText(this);
        inputView.setText(item.getValue());
        new AlertDialog.Builder(this)
                .setTitle("key: " + item.getKey())
                .setView(inputView)
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    createOrUpdate(item, inputView.getText().toString());
                    loadData();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (TextUtils.equals(SpHelper.NAME, mSharedPreferenceName)) {
            Toast.makeText(this, "禁止修改debugger自身的shared preference文件", Toast.LENGTH_LONG).show();
            return;
        }
        Item item = (Item) mAdapter.getItem(position);
        valueDialog(item);
    }

    private static class ItemAdapter extends BaseAdapter {
        private List<Item> mList = new ArrayList<>();
        private LayoutInflater mInflater;

        ItemAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.debugger_item_sp_entry, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Item item = mList.get(position);
            holder.keyView.setText(item.getKey());
            holder.valueView.setText(item.getValue());
            holder.valueClassView.setText(item.getValueClass().getName());
            return convertView;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        void updateData(@NonNull List<Item> list) {
            mList = new ArrayList<>(list);
            notifyDataSetChanged();
        }
    }

    private static class ViewHolder {
        private TextView keyView;
        private TextView valueView;
        private TextView valueClassView;

        ViewHolder(View convertView) {
            keyView = convertView.findViewById(R.id.key);
            valueView = convertView.findViewById(R.id.value);
            valueClassView = convertView.findViewById(R.id.value_class);
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

    public void add(MenuItem item) {
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
