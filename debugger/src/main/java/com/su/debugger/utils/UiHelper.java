package com.su.debugger.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.su.debugger.AppHelper;
import com.su.debugger.R;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by su on 15-11-18.
 */
public final class UiHelper {
    private static final String TAG = UiHelper.class.getSimpleName();

    private UiHelper() {}


    public static AlertDialog showTip(Context context, String tip) {
        if (TextUtils.isEmpty(tip)) {
            return null;
        }
        return new AlertDialog.Builder(context)
                .setMessage(tip)
                .setPositiveButton(R.string.known, null)
                .show();
    }

    public static AlertDialog showConfirm(Context context, String tip) {
        return new AlertDialog.Builder(context)
                .setMessage(tip)
                .setPositiveButton(R.string.confirm, null)
                .show();
    }

    public static ShapeDrawable getShapeDrawable(@ColorInt int color, int radius) {
        return getShapeDrawable(color, radius, radius, radius, radius);
    }

    public static ShapeDrawable getShapeDrawable(@ColorInt int color, int topLeft, int topRight, int bottomRight, int bottomLeft) {
        RoundRectShape roundRectShape = new RoundRectShape(new float[]{topLeft, topLeft, topRight, topRight, bottomRight, bottomRight, bottomLeft, bottomLeft}, null, null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(roundRectShape);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    public static LayerDrawable getBorderLayerDrawable(@ColorInt int color, @ColorInt int backgroundColor, int padding, int radius) {
        Drawable[] layers = new Drawable[2];
        layers[0] = getShapeDrawable(color, radius);
        layers[0].setState(new int[]{android.R.attr.state_enabled});
        layers[1] = getShapeDrawable(backgroundColor, radius);
        layers[1].setState(new int[]{android.R.attr.state_enabled});
        LayerDrawable layerDrawable = new LayerDrawable(layers);
        layerDrawable.setLayerInset(1, padding, padding, padding, padding);
        return layerDrawable;
    }

    public static StateListDrawable makeTransparentStateListDrawable(Resources resources, Bitmap bitmap, int alpha) {
        BitmapDrawable bd1 = new BitmapDrawable(resources, bitmap);
        BitmapDrawable bd2 = new BitmapDrawable(resources, bitmap);
        bd2.setAlpha(alpha);
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{-android.R.attr.state_pressed}, bd1);
        sld.addState(new int[]{android.R.attr.state_pressed}, bd2);
        return sld;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Drawable makeRippleDrawable(ColorStateList colorStateList, @NonNull Drawable drawable) {
        return new RippleDrawable(colorStateList, drawable, null);
    }

    public static Drawable makeDrawable(ColorStateList csl5, @NonNull Drawable drawable5, @NonNull Drawable drawable4) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return makeRippleDrawable(csl5, drawable5);
        } else {
            return drawable4;
        }
    }

    public static ColorStateList getColorControlHighlight(Context context) {
        TypedArray array = context.obtainStyledAttributes(new int[]{R.attr.colorControlHighlight});
        ColorStateList colorStateList = array.getColorStateList(0);
        array.recycle();
        return colorStateList;
    }

    public static int getActionBarHeight(Context context) {
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                actionBarHeight += dp2px(8);
            }
        }
        return actionBarHeight;
    }

    public static int dp2px(float dpValue) {
        DisplayMetrics displayMetrics = GeneralInfoHelper.getContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, displayMetrics);
    }

    public static int sp2px(float spValue) {
        DisplayMetrics displayMetrics = GeneralInfoHelper.getContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, displayMetrics);
    }

    public static int px2dp(float pxValue) {
        final float scale =  GeneralInfoHelper.getContext().getResources().getDisplayMetrics().density;
        return pxValue > 0 ? (int) (pxValue / scale + 0.5f) : -(int) (-pxValue / scale + 0.5f);
    }

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    //获取魅族smartbar高度
    public static int getSmartBarHeight(Context context) {
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object obj = clazz.newInstance();
            Field field = clazz.getField("mz_action_button_min_height");
            int height = Integer.parseInt(field.get(obj).toString());
            return context.getResources().getDimensionPixelSize(height);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            Log.w(TAG, e);
        }
        return 0;
    }

    //https://stackoverflow.com/questions/20264268/how-to-get-height-and-width-of-navigation-bar-programmatically
    public static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = new Point(GeneralInfoHelper.getScreenWidth(), GeneralInfoHelper.getScreenHeight());

        // navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }

        // navigation bar is not present
        return new Point();
    }

    public static int getNavigationBarHeight(Context context) {
        int height = getNavigationBarSize(context).y;
        if (height == 0 && AppHelper.isFlyme()) {
            height = getSmartBarHeight(context);
        }
        return height;
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (IllegalAccessException e) {} catch (InvocationTargetException e) {} catch (NoSuchMethodException e) {}
        }

        return size;
    }

    public static double getScreenDiagonalSize(DisplayMetrics displayMetrics, Point point) {
        return Math.sqrt(Math.pow(point.x / displayMetrics.xdpi, 2.0d) + Math.pow(point.y / displayMetrics.ydpi, 2.0d));
    }

    /**
     * 扩大View的触摸和点击响应范围,最大不超过其父View范围
     */
    public static void expandViewTouchDelegate(final View view, final int top,
                                               final int bottom, final int left, final int right) {
        ((View) view.getParent()).post(() -> {
            Rect bounds = new Rect();
            view.setEnabled(true);
            view.getHitRect(bounds);
            bounds.top -= top;
            bounds.bottom += bottom;
            bounds.left -= left;
            bounds.right += right;
            TouchDelegate touchDelegate = new TouchDelegate(bounds, view);
            if (View.class.isInstance(view.getParent())) {
                ((View) view.getParent()).setTouchDelegate(touchDelegate);
            }
        });
    }

    public static void expandViewTouchDelegate(final View view, final int extra) {
        expandViewTouchDelegate(view, extra, extra, extra, extra);
    }

    public static int getThemeColorByAttrId(Resources.Theme theme, int attr) {
        TypedValue typedValue = new TypedValue();
        theme.resolveAttribute(attr, typedValue, true);
        return typedValue.data;
    }

    public static void setStatusBarColor(Window window, int color) {
        setStatusBarColor(window, color, true);
    }

    public static void setStatusBarColor(Window window, int color, boolean isAddView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0及以上，不设置透明状态栏，设置会有半透明阴影
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (isAddView) {
                View statusBarView = new View(window.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        getStatusBarHeight(window.getContext()));
                statusBarView.setLayoutParams(params);
                statusBarView.setBackgroundColor(color);
                ViewGroup decorView = (ViewGroup) window.getDecorView();
                decorView.addView(statusBarView);
            }
            ViewGroup contentView = window.findViewById(android.R.id.content);
            View rootView = contentView.getChildAt(0);
            if (rootView instanceof ViewGroup) {
                rootView.setFitsSystemWindows(true);
            }
        }
    }

    /**
     * 设置颜色透明度
     *
     * @param alpha 目标透明度 eg:0xAA000000
     */
    public static int resetColorAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | alpha;
    }

    public static String color2rgbString(int color) {
        return String.format("#%06X", color);
    }

    public static void setNavigationBarColor(Window window, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(color);
        }
    }
}
