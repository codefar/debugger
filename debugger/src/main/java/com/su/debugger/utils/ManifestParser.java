package com.su.debugger.utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.Field;

public class ManifestParser {
    public static final String TAG = ManifestParser.class.getSimpleName();

    private Context mContext;
    private Resources mResources;
    private String mPackageName;

    public ManifestParser(@NonNull Context context) {
        mContext = context;
        mResources = context.getResources();
        mPackageName = context.getPackageName();
    }

    public String getManifest() {
        String manifest = "";
        XmlResourceParser parser = null;
        try {
            AssetManager assetManager = mContext.createPackageContext(mPackageName, 0).getAssets();
            parser = assetManager.openXmlResourceParser("AndroidManifest.xml");
            manifest = getManifest(parser);
        } catch (PackageManager.NameNotFoundException | IOException e) {
            Log.w(TAG, e);
        } finally {
            IOUtil.close(parser);
        }
        return manifest;
    }

    public int[] getSdkVersions() {
        int[] sdkVersions = new int[3];
        XmlResourceParser parser = null;
        try {
            AssetManager assetManager = mContext.createPackageContext(mPackageName, 0).getAssets();
            parser = assetManager.openXmlResourceParser("AndroidManifest.xml");
            getSdkVersions(parser, sdkVersions);
            parser.close();
        } catch (PackageManager.NameNotFoundException | IOException e) {
            Log.w(TAG, e);
        } finally {
            IOUtil.close(parser);
        }
        return sdkVersions;
    }

    private void getSdkVersions(@NonNull XmlResourceParser parser, @NonNull int[] sdkVersions) {
        String compileSdkVersion = "";
        String minSdkVersion = "";
        String targetSdkVersion = "";
        int count = 0;
        try {
            int eventType = parser.next();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType != XmlPullParser.START_TAG) {
                    eventType = parser.next();
                    continue;
                }
                String name = parser.getName();
                if (TextUtils.equals(name, "manifest")) {
                    compileSdkVersion = getAttributeValue(parser, "compileSdkVersion");
                    count++;
                } else if (TextUtils.equals(name, "uses-sdk")) {
                    minSdkVersion = getAttributeValue(parser, "minSdkVersion");
                    targetSdkVersion = getAttributeValue(parser, "targetSdkVersion");
                    count++;
                }
                if (count == 2) {
                    sdkVersions[0] = parseInt(compileSdkVersion);
                    sdkVersions[1] = parseInt(minSdkVersion);
                    sdkVersions[2] = parseInt(targetSdkVersion);
                    break;
                }
                eventType = parser.next();
            }
        } catch (IOException | XmlPullParserException e) {
            Log.w(TAG, e);
        }
    }

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String getManifest(XmlResourceParser parser) {
        StringBuilder sb = new StringBuilder();
        int indent = 0;
        try {
            int eventType = parser.next();
            int lastEventType = -1;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                        break;
                    case XmlPullParser.START_TAG:
                        sb.append("\n");
                        insertSpaces(sb, indent);
                        String name = parser.getName();
                        if (TextUtils.equals(name, "manifest")) {
                            sb.append("<manifest\n xmlns:android=\"http://schemas.android.com/apk/res/android\"");
                        } else {
                            sb.append("<" + name);
                        }
                        sb.append(getAttributes(parser));
                        sb.append(">");
                        indent += 1;
                        break;
                    case XmlPullParser.END_TAG:
                        indent -= 1;
                        if (lastEventType == XmlPullParser.START_TAG) {
                            sb.deleteCharAt(sb.length() - 1);
                            sb.append(" />");
                        } else {
                            sb.append("\n");
                            insertSpaces(sb, indent);
                            sb.append("</" + parser.getName() + ">");
                        }
                        break;
                    case XmlPullParser.TEXT:
                        sb.append(parser.getText());
                        break;
                    default:
                        Log.d("ManifestParser", "eventType: " + eventType);
                        break;
                }
                lastEventType = eventType;
                eventType = parser.next();
            }
            sb.append("\n");
        } catch (IOException | XmlPullParserException e) {
            Log.w(TAG, e);
        }
        return sb.toString();
    }

    @NonNull
    private String getAttributeValue(@NonNull XmlResourceParser parser, @NonNull String attributeName) {
        int count = parser.getAttributeCount();
        for (int i = 0; i < count; i++) {
            String name = parser.getAttributeName(i);
            if (!TextUtils.equals(name, attributeName)) {
                continue;
            }
            return parser.getAttributeValue(i);
        }
        return "";
    }

    private String getAttributes(@NonNull XmlResourceParser parser) {
        StringBuilder sb = new StringBuilder();
        int count = parser.getAttributeCount();
        for (int i = 0; i < count; i++) {
            sb.append(" ");
            String attributeName = parser.getAttributeName(i);
            String attributeValue = parser.getAttributeValue(i);
            String value;
            if (TextUtils.equals(attributeName, "screenOrientation")) {
                value = getScreenOrientation(Integer.parseInt(attributeValue));
            } else if (TextUtils.equals(attributeName, "launchMode")) {
                value = getLaunchMode(Integer.parseInt(attributeValue));
            } else if (TextUtils.equals(attributeName, "windowSoftInputMode")) {
                if (attributeValue.startsWith("0x")) {
                    attributeValue = attributeValue.substring(2);
                }
                value = softInputModeToString(Integer.parseInt(attributeValue, 16));
            } else {
                value = resolveValue(attributeValue);
            }
            sb.append("android:" + attributeName + "=\"" + value + "\"");
        }
        return sb.toString();
    }

    private void insertSpaces(@NonNull StringBuilder sb, int number) {
        if (sb == null)
            return;
        for (int i = 0; i < number; i++)
            sb.append(" ");
    }

    private String resolveValue(String value) {
        if (value == null || !value.startsWith("@"))
            return value;
        try {
            int num = Integer.parseInt(value.substring(1));
            String name = mResources.getResourceName(num);
            if (name.startsWith(mPackageName + ":")) {
                name = "@" + name.substring(mPackageName.length() + 1);
            }
            return name;
        } catch (RuntimeException e) {
            Log.w(TAG, e);
            return value;
        }
    }

    private static String getScreenOrientation(int orientation) {
        Class<ActivityInfo> clazz = ActivityInfo.class;
        Field[] fields = clazz.getDeclaredFields();
        try {
            for (Field field : fields) {
                if (!field.getName().startsWith("SCREEN_ORIENTATION_")) {
                    continue;
                }
                Integer value = (Integer) field.get(null);
                if (value == null || orientation != value) {
                    continue;
                }
                //去掉下划线，驼峰式命名
                char[] chars = field.getName().substring(19).toCharArray();
                char[] newChars = new char[chars.length];
                int pointer = 0;
                int length = chars.length;
                int count = 0;
                for (int i = 0; i < length; i++) {
                    if (chars[i] == 95) { //下划线
                        newChars[pointer] = chars[i + 1]; //大写变小写
                        count++;
                        i++; //略过下划线
                    } else {
                        chars[i] += 32;
                        newChars[pointer] = chars[i];
                    }
                    pointer++;
                }
                return new String(newChars, 0, length - count);
            }
        } catch (IllegalAccessException e) {
            Log.w(TAG, e);
        }
        return "unknown";
    }

    private static String getLaunchMode(int mode) {
        switch (mode) {
            case ActivityInfo.LAUNCH_SINGLE_TOP:
                return "singleTop";
            case ActivityInfo.LAUNCH_SINGLE_TASK:
                return "singleTask";
            case ActivityInfo.LAUNCH_SINGLE_INSTANCE:
                return "singleInstance";
            default:
                return "standard";
        }
    }

    /**
     * copy from android 9.0
     * see com.android.internal.view.InputMethodClient#softInputModeToString
     * */
    private static String softInputModeToString(final int softInputMode) {
        final StringBuilder sb = new StringBuilder();
        final int state = softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE;
        final int adjust = softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST;
        final boolean isForwardNav =
                (softInputMode & WindowManager.LayoutParams.SOFT_INPUT_IS_FORWARD_NAVIGATION) != 0;

        switch (state) {
            case WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED:
                sb.append("stateUnspecified");
                break;
            case WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED:
                sb.append("stateUnchanged");
                break;
            case WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN:
                sb.append("stateHidden");
                break;
            case WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN:
                sb.append("stateAlwaysHidden");
                break;
            case WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE:
                sb.append("stateVisible");
                break;
            case WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE:
                sb.append("stateAlwaysVisible");
                break;
            default:
                sb.append("stateUnknown(");
                sb.append(state);
                sb.append(")");
                break;
        }

        switch (adjust) {
            case WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED:
                sb.append("|adjustUnspecified");
                break;
            case WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE:
                sb.append("|adjustResize");
                break;
            case WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN:
                sb.append("|adjustPan");
                break;
            case WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING:
                sb.append("|adjustNothing");
                break;
            default:
                sb.append("|adjustUnknown(");
                sb.append(adjust);
                sb.append(")");
                break;
        }

        if (isForwardNav) {
            // This is a special bit that is set by the system only during the window navigation.
            sb.append("|isForwardNavigation");
        }

        return sb.toString();
    }
}
