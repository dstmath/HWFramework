package com.huawei.server.wm;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.provider.FrontFingerPrintSettingsEx;
import com.huawei.android.util.HwNotchSizeUtil;
import com.huawei.android.util.SlogEx;
import com.huawei.singlehandlib.BuildConfig;
import com.huawei.utils.HwPartResourceUtils;
import huawei.android.provider.FrontFingerPrintSettings;
import java.util.Locale;

/* access modifiers changed from: package-private */
public final class SingleHandUtils {
    static final float DEFAULT_ROUND_RADIUS = 16.0f;
    public static final float DEFAULT_SCALE = 1.0f;
    static final String FILLET_RADIUS_SIZE = SystemPropertiesEx.get("ro.config.fillet_radius_size", BuildConfig.FLAVOR);
    public static final int GUEST_MODE = 1;
    private static final float INIT_CORNER_RATIO = 1.6666666f;
    private static final boolean IS_FOLDABLE;
    static final boolean IS_QUICK_SWITCH_FORCE_ENABLE = SystemPropertiesEx.getBoolean("hw_mc.launcher.quick_switch_force_enable", !SystemPropertiesEx.getBoolean("ro.build.hw_emui_lite.enable", false));
    public static final String KEY_SINGLE_HAND_SCREEN_SCALE = "single_hand_scales";
    static final String MAIN_SCREEN_RADIUS_SIZE = SystemPropertiesEx.get("hw_mc.launcher.main_screen_radius_size", BuildConfig.FLAVOR);
    public static final int NEW_MODE = 3;
    static final String SETTINGS_ACTIVITY = "com.android.settings.Settings$SingleHandScreenZoomSettingsActivity";
    static final String SETTINGS_PACKAGE_NAME = "com.android.settings";
    private static final String TAG = "SingleHandUtils";
    private static final String VIEW_INFO_ID_KEY = "freshman_guide_info_gesture_id";
    private static final String VIEW_TITLE_ID_KEY = "freshman_guide_title_gesture";
    public static final int VIRTUAL_MODE = 2;

    static {
        boolean z = false;
        if (!SystemPropertiesEx.get("ro.config.hw_fold_disp").isEmpty() || !SystemPropertiesEx.get("persist.sys.fold.disp.size").isEmpty()) {
            z = true;
        }
        IS_FOLDABLE = z;
    }

    private SingleHandUtils() {
    }

    private static boolean getCurrentNavWay(ContentResolver resolver) {
        return FrontFingerPrintSettings.isNaviBarEnabled(resolver) && !FrontFingerPrintSettings.isSingleNavBarAIEnable(resolver) && !FrontFingerPrintSettings.isGestureNavigationMode(resolver);
    }

    private static boolean isThreeKeyDevice() {
        return FrontFingerPrintSettingsEx.FRONT_FINGERPRINT_NAVIGATION && FrontFingerPrintSettingsEx.FRONT_FINGERPRINT_NAVIGATION_TRIKEY == 1;
    }

    public static int getCurrentEnteringMode(ContentResolver contentResolver) {
        if (getCurrentNavWay(contentResolver)) {
            return 2;
        }
        if (!IS_QUICK_SWITCH_FORCE_ENABLE || isThreeKeyDevice()) {
            return 3;
        }
        return 1;
    }

    private static void refreshGestureNew(View view, boolean isShown, Resources resources) {
        TextView textView = (TextView) view.findViewById(HwPartResourceUtils.getResourceId(VIEW_TITLE_ID_KEY));
        if (textView != null) {
            textView.setText(resources.getString(HwPartResourceUtils.getResourceId("freshman_guide_title_new_info")));
            TextView textView1 = (TextView) view.findViewById(HwPartResourceUtils.getResourceId(VIEW_INFO_ID_KEY));
            if (textView1 != null) {
                textView1.setText(resources.getString(HwPartResourceUtils.getResourceId("freshman_guide_info_gesture")));
                if (isShown) {
                    textView.setVisibility(0);
                    textView1.setVisibility(0);
                    return;
                }
                textView.setVisibility(4);
                textView1.setVisibility(4);
            }
        }
    }

    private static void refreshGestureGuide(View view, boolean isShown, Resources resources) {
        TextView textView = (TextView) view.findViewById(HwPartResourceUtils.getResourceId(VIEW_TITLE_ID_KEY));
        if (textView != null) {
            textView.setText(resources.getString(HwPartResourceUtils.getResourceId("freshman_guide_title_gesture_info")));
            SlogEx.i(TAG, "freshman_guide_title_gesture: " + textView.getTextSize());
            TextView textView1 = (TextView) view.findViewById(HwPartResourceUtils.getResourceId(VIEW_INFO_ID_KEY));
            if (textView1 != null) {
                textView1.setText(resources.getString(HwPartResourceUtils.getResourceId("freshman_guide_info_new")));
                if (isShown) {
                    textView.setVisibility(0);
                    textView1.setVisibility(0);
                    return;
                }
                textView.setVisibility(4);
                textView1.setVisibility(4);
            }
        }
    }

    private static void refreshTripleGuide(View view, boolean isShown, Resources resources) {
        TextView textView = (TextView) view.findViewById(HwPartResourceUtils.getResourceId(VIEW_TITLE_ID_KEY));
        if (textView != null) {
            textView.setText(resources.getString(HwPartResourceUtils.getResourceId("freshman_guide_title_triple_info")));
            TextView textView1 = (TextView) view.findViewById(HwPartResourceUtils.getResourceId(VIEW_INFO_ID_KEY));
            if (textView1 != null) {
                textView1.setText(resources.getString(HwPartResourceUtils.getResourceId("freshman_guide_info_gesture")));
                if (isShown) {
                    textView.setVisibility(0);
                    textView1.setVisibility(0);
                    return;
                }
                textView.setVisibility(4);
                textView1.setVisibility(4);
            }
        }
    }

    public static void showFreshmanGuide(View view, boolean isShown, Context context) {
        if (view != null && context != null && context.getResources() != null) {
            int enterMode = getCurrentEnteringMode(context.getContentResolver());
            SlogEx.i(TAG, "enterMode: " + enterMode);
            if (enterMode == 2) {
                refreshTripleGuide(view, isShown, context.getResources());
            } else if (enterMode == 1) {
                refreshGestureGuide(view, isShown, context.getResources());
            } else {
                refreshGestureNew(view, isShown, context.getResources());
            }
        }
    }

    private static float string2Float(String str) {
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            SlogEx.e(TAG, "string2Float NumberFormatException");
            return 0.0f;
        }
    }

    public static int pxFromDp(float dp, DisplayMetrics metrics) {
        return Math.round(TypedValue.applyDimension(1, dp, metrics));
    }

    public static float getDeviceRoundRadiusSize(DisplayMetrics displayMetrics) {
        float screenRadius = 0.0f;
        String radiusStr = BuildConfig.FLAVOR;
        if (IS_FOLDABLE) {
            radiusStr = MAIN_SCREEN_RADIUS_SIZE;
        }
        if (TextUtils.isEmpty(radiusStr)) {
            radiusStr = FILLET_RADIUS_SIZE;
        }
        if (!TextUtils.isEmpty(radiusStr)) {
            screenRadius = string2Float(radiusStr);
            try {
                screenRadius = (float) pxFromDp(screenRadius, displayMetrics);
            } catch (NumberFormatException e) {
                SlogEx.w(TAG, "get ro.config.fillet_radius_size config error");
            }
        }
        if (Float.compare(screenRadius, 0.0f) <= 0) {
            screenRadius = (float) HwNotchSizeUtil.getNotchCorner();
            if (Float.compare(screenRadius, 0.0f) <= 0) {
                screenRadius = ((float) pxFromDp(DEFAULT_ROUND_RADIUS, displayMetrics)) * INIT_CORNER_RATIO;
            }
        }
        SlogEx.i(TAG, "screenRadius:" + screenRadius + "radiusStr:" + radiusStr);
        return screenRadius;
    }

    public static void startSettingsIntent(Context context) {
        if (context != null) {
            Intent intent = new Intent();
            intent.addFlags(268435456);
            intent.setClassName(SETTINGS_PACKAGE_NAME, SETTINGS_ACTIVITY);
            try {
                context.startActivity(intent, ActivityOptions.makeCustomAnimation(context, 0, 0).toBundle());
            } catch (ActivityNotFoundException e) {
                SlogEx.i(TAG, "Settings failed");
            }
        }
    }

    public static boolean isMirrorLanguage() {
        return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1;
    }
}
