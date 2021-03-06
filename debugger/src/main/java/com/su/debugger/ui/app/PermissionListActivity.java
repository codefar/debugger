package com.su.debugger.ui.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.su.debugger.R;
import com.su.debugger.entity.PermissionInfoWrapper;
import com.su.debugger.ui.BaseAppCompatActivity;
import com.su.debugger.widget.recycler.BaseRecyclerAdapter;
import com.su.debugger.widget.recycler.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by su on 17-5-27.
 * 权限列表
 */
public class PermissionListActivity extends BaseAppCompatActivity implements RecyclerItemClickListener.OnItemClickListener {
    private static final String TAG = PermissionListActivity.class.getSimpleName();
    /**
     * from {@link android.content.pm.PermissionInfo}.
     */
    public static final int FLAG_REMOVED = 1 << 1;
    public static final int REQ_CODE = 1;

    private String mPackageName;
    private PackageManager mPm;
    private List<PermissionInfoWrapper> mDataList = new ArrayList<>();
    private RecyclerViewAdapter mAdapter;

    public static void startActivity(@NonNull Context context) {
        context.startActivity(new Intent(context, PermissionListActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_template_recycler_list);
        mPm = getPackageManager();
        mPackageName = getPackageName();
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new RecyclerViewAdapter(this, mDataList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, this));
        refreshData();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("Permission列表");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    public void refreshData() {
        mDataList.clear();
        try {
            PackageInfo packageInfo = mPm.getPackageInfo(mPackageName, PackageManager.GET_PERMISSIONS);
            if (packageInfo.requestedPermissions != null) {
                for (String permission : packageInfo.requestedPermissions) {
                    try {
                        PermissionInfo permissionInfo = mPm.getPermissionInfo(permission, 0);
                        if ((permissionInfo.flags & PermissionInfo.FLAG_INSTALLED) == 0
                                || (permissionInfo.flags & FLAG_REMOVED) != 0) {
                            continue;
                        }
                        boolean isHasPermission = PackageManager.PERMISSION_GRANTED == mPm.checkPermission(permission, getPackageName());
                        mDataList.add(new PermissionInfoWrapper(permissionInfo, isHasPermission));
                    } catch (PackageManager.NameNotFoundException e) {
                        //ignore
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "包名错误： " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        rearrangeData(mDataList);
        mAdapter.notifyDataSetChanged();
    }

    private void rearrangeData(@NonNull List<PermissionInfoWrapper> list) {
        Collections.sort(list, (o1, o2) -> {
            if (o1.isDangerous() && !o2.isDangerous()) {
                return -1;
            } else if (o2.isDangerous() && !o1.isDangerous()) {
                return 1;
            } else {
                if (o1.isHasPermission() && !o2.isHasPermission()) {
                    return 1;
                }
                if (!o1.isHasPermission() && o2.isHasPermission()) {
                    return -1;
                }

                //PermissionInfo中group可能为null
                if (o1.isDangerous() && o2.isDangerous()) {
                    if (o1.group.equalsIgnoreCase(o2.group)) {
                        return o1.name.compareToIgnoreCase(o2.name);
                    } else {
                        return o1.group.compareToIgnoreCase(o2.group);
                    }
                } else {
                    return o1.name.compareToIgnoreCase(o2.name);
                }
            }
        });
    }

    @Override
    public void onItemClick(View view, int position) {
        PermissionInfoWrapper wrapper = mDataList.get(position);
        if (wrapper.isHasPermission()) {
            return;
        }
        permissionRequest(wrapper.name);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void permissionRequest(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            makeHintDialog(permission).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQ_CODE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean granted = true;
        for (int grantResult : grantResults) {
            granted = granted && grantResult == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                break;
            }
        }
        if (!granted) {
            makeHintDialog(permissions[0]);
        }
    }

    public AlertDialog makeHintDialog(String permission) {
        PermissionInfoWrapper wrapper = findPermissionInfoWrapperByPermission(permission);
        return new AlertDialog.Builder(this)
                .setTitle("权限申请")
                .setMessage(wrapper.name + "\n" + wrapper.loadDescription(mPm))
                .setPositiveButton(R.string.debugger_set_permission, (dialog, which) ->
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null))))
                .setNegativeButton(R.string.debugger_cancel, null)
                .show();
    }

    @NonNull
    private PermissionInfoWrapper findPermissionInfoWrapperByPermission(@NonNull String permission) {
        for (PermissionInfoWrapper wrapper : mDataList) {
            if (TextUtils.equals(wrapper.name, permission)) {
                return wrapper;
            }
        }
        throw new IllegalArgumentException("can not find permission in the app permission list: " + permission);
    }

    private static class RecyclerViewAdapter extends BaseRecyclerAdapter<PermissionInfoWrapper> {

        private Resources mResources;
        private PackageManager mPm;

        private RecyclerViewAdapter(@NonNull Context context, @NonNull List<PermissionInfoWrapper> data) {
            super(data);
            mPm = context.getPackageManager();
            mResources = context.getResources();
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.debugger_item_permission;
        }

        @Override
        protected void bindData(@NonNull final BaseViewHolder holder, final int position, int itemType) {
            final PermissionInfoWrapper info = getData().get(position);
            TextView nameTextView = holder.getView(R.id.name);
            if (info.isHasPermission()) {
                nameTextView.setText(info.name + " ✔");
                nameTextView.setTextColor(mResources.getColor(R.color.first_text));
            } else {
                nameTextView.setText(info.name + " ✘");
                nameTextView.setTextColor(mResources.getColor(R.color.error_hint));
            }
            int level = info.protectionLevel;
            TextView levelView = holder.getView(R.id.level);
            levelView.setText("level: " + protectionToString(level));
            if ((level & PermissionInfo.PROTECTION_MASK_BASE) == PermissionInfo.PROTECTION_DANGEROUS) {
                levelView.setTextColor(mResources.getColor(R.color.error_hint));
            } else {
                levelView.setTextColor(mResources.getColor(R.color.second_text));
            }
            TextView groupView = holder.getView(R.id.group);
            if (TextUtils.isEmpty(info.group)) {
                groupView.setVisibility(View.GONE);
            } else {
                groupView.setVisibility(View.VISIBLE);
                groupView.setText(info.group);
            }
            TextView descView = holder.getView(R.id.desc);
            CharSequence desc = info.loadDescription(mPm);
            if (TextUtils.isEmpty(desc)) {
                descView.setVisibility(View.GONE);
            } else {
                descView.setVisibility(View.VISIBLE);
                descView.setText(desc);
            }
        }
    }

    /**
     * @see android.content.pm.PermissionInfo
     */
    public static String protectionToString(int level) {
        String protectLevel = "????";
        switch (level & PermissionInfo.PROTECTION_MASK_BASE) {
            case PermissionInfo.PROTECTION_DANGEROUS:
                protectLevel = "dangerous";
                break;
            case PermissionInfo.PROTECTION_NORMAL:
                protectLevel = "normal";
                break;
            case PermissionInfo.PROTECTION_SIGNATURE:
                protectLevel = "signature";
                break;
            case PermissionInfo.PROTECTION_SIGNATURE_OR_SYSTEM:
                protectLevel = "signatureOrSystem";
                break;
            default:
                break;
        }
        return protectLevel;
    }

    @MenuRes
    @Override
    public int menuRes() {
        return R.menu.debugger_setting_menu;
    }

    public void goSetting(@NonNull MenuItem item) {
        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.debugger_setting_menu, menu);
        return true;
    }

    @Override
    protected String getTag() {
        return TAG;
    }
}
