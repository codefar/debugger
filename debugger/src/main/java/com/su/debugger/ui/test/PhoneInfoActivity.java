package com.su.debugger.ui.test;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import com.su.debugger.R;
import com.su.debugger.entity.SystemInfo;
import com.su.debugger.utils.GeneralInfoHelper;
import com.su.debugger.utils.NetworkUtil;
import com.su.debugger.utils.SystemInfoHelper;
import com.su.debugger.utils.UiHelper;
import com.su.debugger.widget.recycler.BaseRecyclerAdapter;
import com.su.debugger.widget.recycler.GridItemSpaceDecoration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mahao on 17-5-27.
 * 调试功能列表 - 手机信息
 */
public class PhoneInfoActivity extends BaseAppCompatActivity {

    private static final String TAG = PhoneInfoActivity.class.getSimpleName();
    private List<SystemInfo> mData = new ArrayList<>();

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle("手机信息");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debugger_activity_system_info);
        RecyclerView recyclerView = findViewById(R.id.recycler);
        initData();
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new GridItemSpaceDecoration(3, 30));
        Animation animation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        LayoutAnimationController controller = new LayoutAnimationController(animation);
        //设置控件显示的顺序
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        //设置控件显示间隔时间
        controller.setDelay(0.5f);
        recyclerView.setLayoutAnimation(controller);
        MyAdapter adapter = new MyAdapter(mData);
        recyclerView.setAdapter(adapter);
    }

    private class MyAdapter extends BaseRecyclerAdapter<SystemInfo> {

        private MyAdapter(List<SystemInfo> data) {
            super(data);
        }

        @Override
        public int getLayoutId(int itemType) {
            return R.layout.debugger_item_system_info;
        }

        @Override
        protected void bindData(BaseViewHolder holder, final int position, int itemType) {
            ((TextView) holder.itemView).setText(getData().get(position).getTitle());
            holder.itemView.setOnClickListener(v ->
                    new AlertDialog.Builder(PhoneInfoActivity.this)
                            .setTitle(getData().get(position).getTitle())
                            .setMessage(getData().get(position).getDesc())
                            .setNegativeButton(R.string.close, null)
                            .setPositiveButton(R.string.share, (dialog, which) -> {
                                Intent intent = new Intent(Intent.ACTION_SEND);
                                intent.putExtra(Intent.EXTRA_TEXT, getData().get(position).getDesc());
                                intent.setType("text/plain");
                                startActivity(Intent.createChooser(intent, "分享到"));
                            })
                            .show()
            );
        }
    }

    private void initData() {
        mData.add(getScreenInfo());
        mData.add(getSystemInfo());
        mData.add(getNetWorkInfo());
        mData.add(getHardwareInfo());
        mData.add(getPhoneId());
    }

    private SystemInfo getPhoneId() {
        boolean isHasPermission = PackageManager.PERMISSION_GRANTED == getPackageManager().checkPermission(Manifest.permission.READ_PHONE_STATE, getPackageName());
        SystemInfo info = new SystemInfo();
        info.setTitle("本机ID");
        String desc = "";
        TelephonyManager tm = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        desc += "IMEI: " + (isHasPermission ? tm.getDeviceId() : "未授权");
        desc += "\n\n" + "SV: " + (isHasPermission ? tm.getDeviceSoftwareVersion() : "未授权");
        desc += "\n\n" + "手机号: " + (isHasPermission ? tm.getLine1Number() : "未授权");
        desc += "\n\n" + "IMSI: " + (isHasPermission ? tm.getSubscriberId() : "未授权");
        desc += "\n\n" + "SIM序列号: " + (isHasPermission ? tm.getSimSerialNumber() : "未授权");
        desc += "\n\n" + "Android ID: " + GeneralInfoHelper.getAndroidId();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            desc += "\n\n" + "设备序列号: " + (isHasPermission ? Build.getSerial() : "未授权");
        } else {
            desc += "\n\n" + "设备序列号: " + Build.SERIAL;
        }
        info.setDesc(desc);
        return info;
    }

    public SystemInfo getHardwareInfo() {
        SystemInfo info = new SystemInfo();
        info.setTitle("硬件");
        String desc = "";
        desc += "型号: " + Build.MODEL;
        desc += "\n\n" + "制造商: " + Build.MANUFACTURER;
        desc += "\n\n" + "主板: " + Build.BOARD;
        desc += "\n\n" + "设备: " + Build.DEVICE;
        desc += "\n\n" + "产品: " + Build.PRODUCT;
        desc += "\n\n" + "CPU 核数: " + Integer.toString(Runtime.getRuntime().availableProcessors());
        desc += "\n\n" + "CPU 位数: " + SystemInfoHelper.getCpuBit();
        desc += "\n\n" + "CPU 型号: " + SystemInfoHelper.getCpuName();
        desc += "\n\n" + "ABIs: " + TextUtils.join(", ", new String[]{Build.CPU_ABI, Build.CPU_ABI2});
        desc += "\n\n" + "存储: 共"
                + SystemInfoHelper.formatFileSize(SystemInfoHelper.getTotalExternalMemorySize(), false)
                + "," + SystemInfoHelper.formatFileSize(SystemInfoHelper.getAvailableExternalMemorySize(), false)
                + "可用";
        desc += "\n\n" + "内存: 共"
                + SystemInfoHelper.formatFileSize(SystemInfoHelper.getTotalMemorySize(), false)
                + "," + SystemInfoHelper.formatFileSize(SystemInfoHelper.getAvailableMemory(this), false)
                + "可用";
        info.setDesc(desc);
        return info;
    }

    private SystemInfo getNetWorkInfo() {
        SystemInfo info = new SystemInfo();
        info.setTitle("网络相关");
        String desc = "";
        String networkType = SystemInfoHelper.getNetworkType(this);
        String ssid = "";
        desc += "网络: " + networkType;
        if ("Wifi".equals(networkType)) {
            WifiManager mgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            if (mgr != null) {
                WifiInfo wifiInfo = mgr.getConnectionInfo();
                ssid = wifiInfo.getSSID();
            }
        }
        desc += "\n\n" + "Wifi SSID: " + ssid;
        desc += "\n\n" + "IPv4: " + NetworkUtil.getIpv4Address();
        desc += "\n\n" + "IPv6: " + NetworkUtil.getIpv6Address();
        desc += "\n\n" + "Mac地址: " + NetworkUtil.getMacAddress();
        info.setDesc(desc);
        return info;
    }

    private SystemInfo getSystemInfo() {
        SystemInfo info = new SystemInfo();
        info.setTitle("系统");
        String desc = "";
        desc += "Android " + Build.VERSION.RELEASE + " / " + SystemInfoHelper.getSystemVersionName(Build.VERSION.SDK_INT) + " / " + "API " + Build.VERSION.SDK_INT;
        desc += "\n\n" + "基带版本: " + Build.getRadioVersion();
        desc += "\n\n" + "Linux 内核版本: " + System.getProperty("os.version");
        desc += "\n\n" + "Http User Agent: " + System.getProperty("http.agent");
        info.setDesc(desc);
        return info;
    }

    public SystemInfo getScreenInfo() {
        SystemInfo info = new SystemInfo();
        info.setTitle("屏幕信息");
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int widthPixels = GeneralInfoHelper.getScreenWidth();
        int heightPixels = GeneralInfoHelper.getScreenHeight();
        String desc = "屏幕分辨率: " + widthPixels + " x " + heightPixels + " px";
        desc += "\n\n状态栏高度: " + GeneralInfoHelper.getStatusBarHeight() + " px";
        int navigationBarHeight = UiHelper.getNavigationBarHeight(this);
        if (navigationBarHeight > 0) {
            desc += "\n\n导航栏高度: " + navigationBarHeight + " px";
        }
        desc += "\n\n" + "密度: " + metrics.densityDpi + "dp" + " / " + SystemInfoHelper.getDpiInfo(metrics.densityDpi) + " / " + metrics.density + "x";
        desc += "\n\n" + "精确密度: " + metrics.xdpi + " x " + metrics.ydpi + " dp";

        Point point = UiHelper.getRealScreenSize(this);
        double screenDiagonalSize = UiHelper.getScreenDiagonalSize(metrics, point);
        float width = point.x / metrics.xdpi;
        float height = point.y / metrics.ydpi;
        DecimalFormat format = new DecimalFormat("0.0");
        desc += "\n\n" + "屏幕尺寸: " + format.format(width) + "''" + " x " + format.format(height) + "''" + " / " + format.format(screenDiagonalSize) + "英寸";
        info.setDesc(desc);
        return info;
    }

    @Override
    protected String getTag() {
        return TAG;
    }

}
