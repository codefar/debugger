package com.su.debugger.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Created by su on 14-6-3.
 */
public final class NetworkUtil {

    private static final String TAG = NetworkUtil.class.getSimpleName();

    private NetworkUtil() {
    }

    private static void log(String fieldName, String content) {
        Log.w(TAG, fieldName + ": " + content);
    }

    /*
    * 获取mac地址
    * */
    public static String getMacAddress() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            WifiManager wifi = (WifiManager) GeneralInfoHelper.getContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            return info.getMacAddress();
        } else {
            return getMacMacAddress23();
        }
    }

    @NonNull
    private static String getMacMacAddress23() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!"wlan0".equalsIgnoreCase(nif.getName())) {
                    continue;
                }

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception e) {
            Log.i(TAG, e.getMessage(), e);
        }
        return "";
    }

    /**
     * 获取系统代理信息
     */
    @NonNull
    public static String[] getSystemProxy(Context context) {
        String proxyAddress;
        String proxyPort;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            proxyAddress = System.getProperty("http.proxyHost");
            String portStr = System.getProperty("http.proxyPort");
            proxyPort = portStr != null ? portStr : "-1";
        } else {
            proxyAddress = android.net.Proxy.getHost(context);
            proxyPort = String.valueOf(android.net.Proxy.getPort(context));
        }
        return new String[]{proxyAddress, proxyPort};
    }

    /**
     * 判断当前网络是否可用
     */
    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) GeneralInfoHelper.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    /*
    * 获取当前联网方式
    * */
    public static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getType();
            }
        }

        return -1;
    }

    /**
     * Unknown network class. {@hide}
     */
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    /**
     * Class of broadly defined "2G" networks. {@hide}
     */
    public static final int NETWORK_CLASS_2_G = 1;
    /**
     * Class of broadly defined "3G" networks. {@hide}
     */
    public static final int NETWORK_CLASS_3_G = 2;
    /**
     * Class of broadly defined "4G" networks. {@hide}
     */
    public static final int NETWORK_CLASS_4_G = 3;
    /**
     * Current network is GSM {@hide}
     */
    public static final int NETWORK_TYPE_GSM = 16;
    /**
     * Current network is TD_SCDMA {@hide}
     */
    public static final int NETWORK_TYPE_TD_SCDMA = 17;
    /**
     * Current network is IWLAN {@hide}
     */
    public static final int NETWORK_TYPE_IWLAN = 18;

    /**
     * from level api 23
     * same as TelephonyManager.getNetworkClass(networkType) (hide)<br/>
     * Return general class of network type, such as "3G" or "4G". In cases
     * where classification is contentious, this method is conservative.
     */
    public static int getNetworkClass(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_GSM:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case NETWORK_TYPE_TD_SCDMA:
                return NETWORK_CLASS_3_G;
            case TelephonyManager.NETWORK_TYPE_LTE:
            case NETWORK_TYPE_IWLAN:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }

    public static boolean is3g4g(int networkClass) {
        return networkClass == NETWORK_CLASS_3_G || networkClass == NETWORK_CLASS_4_G;
    }

    public static String getNetworkClassName(int networkClass) {
        switch (networkClass) {
            case NETWORK_TYPE_GSM:
                return "NETWORK_TYPE_GSM";
            case NETWORK_CLASS_2_G:
                return "NETWORK_CLASS_2_G";
            case NETWORK_CLASS_3_G:
                return "NETWORK_CLASS_3_G";
            case NETWORK_CLASS_4_G:
                return "NETWORK_CLASS_4_G";
            default:
                return "NETWORK_CLASS_UNKNOWN";
        }
    }

    /**
     * Over the air Administration.
     * {@hide}
     */
    public static final int TYPE_MOBILE_FOTA = 10;

    /**
     * IP Multimedia Subsystem.
     * {@hide}
     */
    public static final int TYPE_MOBILE_IMS = 11;

    /**
     * Carrier Branded Services.
     * {@hide}
     */
    public static final int TYPE_MOBILE_CBS = 12;

    /**
     * A Wi-Fi p2p connection. Only requesting processes will have access to
     * the peers connected.
     * {@hide}
     */
    public static final int TYPE_WIFI_P2P = 13;

    /**
     * The network to use for initially attaching to the network
     * {@hide}
     */
    public static final int TYPE_MOBILE_IA = 14;

    /**
     * Emergency PDN connection for emergency services.  This
     * may include IMS and MMS in emergency situations.
     * {@hide}
     */
    public static final int TYPE_MOBILE_EMERGENCY = 15;

    /**
     * The network that uses proxy to achieve connectivity.
     * {@hide}
     */
    public static final int TYPE_PROXY = 16;

    /**
     * level api 23
     * Returns a non-localized string representing a given network type.
     * ONLY used for debugging output.
     *
     * @param type the type needing naming
     * @return a String for the given type, or a string version of the type ("87")
     * if no name is known.
     * {@hide}
     */
    public static String getNetworkTypeName(int type) {
        switch (type) {
            case ConnectivityManager.TYPE_MOBILE:
                return "MOBILE";
            case ConnectivityManager.TYPE_WIFI:
                return "WIFI";
            case ConnectivityManager.TYPE_MOBILE_MMS:
                return "MOBILE_MMS";
            case ConnectivityManager.TYPE_MOBILE_SUPL:
                return "MOBILE_SUPL";
            case ConnectivityManager.TYPE_MOBILE_DUN:
                return "MOBILE_DUN";
            case ConnectivityManager.TYPE_MOBILE_HIPRI:
                return "MOBILE_HIPRI";
            case ConnectivityManager.TYPE_WIMAX:
                return "WIMAX";
            case ConnectivityManager.TYPE_BLUETOOTH:
                return "BLUETOOTH";
            case ConnectivityManager.TYPE_DUMMY:
                return "DUMMY";
            case ConnectivityManager.TYPE_ETHERNET:
                return "ETHERNET";
            case TYPE_MOBILE_FOTA:
                return "MOBILE_FOTA";
            case TYPE_MOBILE_IMS:
                return "MOBILE_IMS";
            case TYPE_MOBILE_CBS:
                return "MOBILE_CBS";
            case TYPE_WIFI_P2P:
                return "WIFI_P2P";
            case TYPE_MOBILE_IA:
                return "MOBILE_IA";
            case TYPE_MOBILE_EMERGENCY:
                return "MOBILE_EMERGENCY";
            case TYPE_PROXY:
                return "PROXY";
            case ConnectivityManager.TYPE_VPN:
                return "VPN";
            default:
                return Integer.toString(type);
        }
    }

    /*
    * 获取当前联网方式
    * */
    public static String getConnectedName(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                return mNetworkInfo.getTypeName();
            }
        }

        return "unknown";
    }

    /**
     * 获取当前网络类型
     * same as TelephonyManager.getNetworkTypeName(int)<br/>
     * we can not call TelephonyManager.getNetworkTypeName(int) directly, it's a hidden method.
     */
    public static String getMobileNetworkTypeName(int type) {
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "CDMA - EvDo rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "CDMA - EvDo rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "CDMA - EvDo rev. B";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "CDMA - 1xRTT";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "CDMA - eHRPD";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iDEN";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "HSPA+";
            default:
                return "UNKNOWN";
        }
    }
}
