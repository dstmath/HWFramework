package com.huawei.android.hwcontrol;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.HwLoadingDrawable;
import android.graphics.drawable.HwLoadingDrawableImpl;
import android.graphics.drawable.HwRippleForeground;
import android.graphics.drawable.HwRippleForegroundProxy;
import android.graphics.drawable.RippleDrawable;
import android.hwcontrol.HwWidgetFactory.DisplayMode;
import android.hwcontrol.HwWidgetFactory.Factory;
import android.hwcontrol.HwWidgetFactory.HwDialogStub;
import android.hwcontrol.HwWidgetFactory.HwTextView;
import android.hwcontrol.HwWidgetFactory.HwToast;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.IMonitor;
import android.util.IMonitor.EventStream;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ActionMenuPresenter;
import android.widget.FastScroller;
import android.widget.IHwSplineOverScroller;
import android.widget.IHwWechatOptimize;
import android.widget.OverScroller.SplineOverScroller;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.app.AlertController;
import com.android.internal.app.WindowDecorActionBar;
import com.android.internal.policy.PhoneWindow;
import com.android.internal.widget.AbsHwDecorCaptionView;
import huawei.android.widget.HwFastScroller;
import huawei.android.widget.HwOverflowMenuButton;
import huawei.android.widget.HwSplineOverScrollerImpl;
import huawei.android.widget.HwWechatOptimizeImpl;
import huawei.com.android.internal.app.HwActionBarImpl;
import huawei.com.android.internal.app.HwAlertController;
import huawei.com.android.internal.widget.HwWidgetUtils;

public class HwWidgetFactoryImpl implements Factory {
    private static final int AUTOSIZE_STEP_GRANULARITY = 1;
    static final boolean DEBUG = false;
    private static final int DEV_DPI = SystemProperties.getInt(REAL_RO_DENSITY, SystemProperties.getInt(RO_DENSITY, PduHeaders.PREVIOUSLY_SENT_BY));
    private static final String PERSIST_DENSITY = "persist.sys.dpi";
    private static final int REAL_DPI = SystemProperties.getInt(PERSIST_DENSITY, DEV_DPI);
    private static final String REAL_RO_DENSITY = "ro.sf.real_lcd_density";
    public static final int REPORRT_SR_COUNT_INT = 0;
    private static final String RO_DENSITY = "ro.sf.lcd_density";
    static final String TAG = "HwWidgetFactoryImpl";
    private static int[] sColorfulDark = new int[]{-1762269, -44681, -53905, -1499549, -49023, -4560696, -5552196, -2080517, -5635841, -6982195, -8497214, -8630785, -10149889, -8812853, -10720320, -11309570, -12756226, -13611010, -11110404, -11617041, -12230946, -12889906, -9926145, -11701249, -11703809, -43230, -765666, -1684967, -49920, -6190977, -8875876, -11168294, -11498034, -5088571, -1740242, -12275252, -10523207, -26624, -14575885};
    private static int[] sColorfulLight = new int[]{-16729900, -14244198, -16728155, -12403391, -13914325, -15407339, -15546624, -8604862, -9920712, -10167017, -4142541, -5262293, -6382300, -5314048, -3364096, -291840, -689152};
    private final int[] colorPrimaryAtts = new int[]{16843827};
    private DisplayMode mDisplayMode = null;
    private int mNoColorfulAttrId = 0;

    public boolean isHwTheme() {
        return false;
    }

    public boolean isHwTheme(Context context) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(33620020, tv, true);
        if (tv.type == 16) {
            return true;
        }
        return false;
    }

    public boolean isHwLightTheme(Context context) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(33620020, tv, true);
        if (tv.type == 16 && tv.data > 0 && tv.data % 3 == 1) {
            return true;
        }
        return false;
    }

    public boolean isHwDarkTheme(Context context) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(33620020, tv, true);
        if (tv.type == 16 && tv.data > 0 && tv.data % 3 == 2) {
            return true;
        }
        return false;
    }

    public boolean isHwEmphasizeTheme(Context context) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(33620020, tv, true);
        if (tv.type == 16 && tv.data > 0 && tv.data % 3 == 0) {
            return true;
        }
        return false;
    }

    public int getThemeIdImpl(Bundle data, Resources res) {
        String themeName = data.getString("hwc-theme");
        if (themeName != null) {
            return res.getIdentifier(themeName, null, null);
        }
        return 0;
    }

    public int getHuaweiRealThemeImpl(int theme) {
        return theme;
    }

    public boolean initAddtionalStyle(Context context, AttributeSet attrs) {
        return isHwTheme(context);
    }

    public HwTextView newHwTextView(Context context, TextView view, AttributeSet attrs) {
        TextViewFactory mHwTextView = new TextViewFactory();
        boolean isHwTheme = isHwTheme(context);
        return mHwTextView;
    }

    public HwToast newHwToast(Context context, Toast view, AttributeSet attrs) {
        return new ToastFactory(context, view, attrs);
    }

    public WindowDecorActionBar getHuaweiActionBarImpl(Activity activity) {
        if (isHwTheme(activity)) {
            return new HwActionBarImpl(activity);
        }
        return new WindowDecorActionBar(activity);
    }

    public WindowDecorActionBar getHuaweiActionBarImpl(Dialog dialog) {
        if (isHwTheme(dialog.getContext())) {
            return new HwActionBarImpl(dialog);
        }
        return new WindowDecorActionBar(dialog);
    }

    public AlertController newHwAlertController(Context context, DialogInterface di, Window window) {
        if (isHwTheme(context)) {
            return new HwAlertController(context, di, window);
        }
        return new AlertController(context, di, window);
    }

    public View newHwOverflowMenuButton(Context context, ActionMenuPresenter actionMenuPresenter) {
        if (isHwTheme(context)) {
            return new HwOverflowMenuButton(context, actionMenuPresenter);
        }
        return null;
    }

    public FastScroller getHwFastScroller(AbsListView absListView, int fastScrollStyle, Context context) {
        if (isHwTheme(context)) {
            return new HwFastScroller(absListView, fastScrollStyle);
        }
        return new FastScroller(absListView, fastScrollStyle);
    }

    public IHwSplineOverScroller getHwSplineOverScrollerImpl(SplineOverScroller sos, Context context) {
        return new HwSplineOverScrollerImpl(sos, context);
    }

    public IHwWechatOptimize getHwWechatOptimizeImpl() {
        return HwWechatOptimizeImpl.getInstance();
    }

    public HwDialogStub newHwDialogStub(Context context, Window window, Dialog dialog, AttributeSet attrs) {
        if (isHwTheme(context)) {
            return new HwDialogStubImpl(context, window, dialog, 2);
        }
        return new HwDialogStubImpl(context, window, dialog, 0);
    }

    public Drawable getEdgeEffectImpl(Resources res) {
        return res.getDrawable(33751554);
    }

    public Drawable getGlowEffectImpl(Resources res) {
        return res.getDrawable(33751555);
    }

    public void setImmersionStyle(Context context, TextView textView, int colorResDark, int colorResLight, int colorfulResLight, boolean defaultIsDark) {
        int colorRes = defaultIsDark ? colorResDark : colorResLight;
        if (isHwDarkTheme(context)) {
            colorRes = colorResDark;
        } else if (!HwWidgetUtils.isActionbarBackgroundThemed(context)) {
            if (getSuggestionForgroundColorStyle(context) != 0) {
                colorRes = colorResDark;
            } else if (colorfulResLight == 0 || !isPrimaryColorfulEnabled(context, 0)) {
                colorRes = colorResLight;
            } else {
                colorRes = colorfulResLight;
            }
        }
        textView.setTextColor(context.getResources().getColorStateList(colorRes));
    }

    public int getImmersionResource(Context context, int resLight, int colorfulResLight, int resDark, boolean defaultIsDark) {
        int res = defaultIsDark ? resDark : resLight;
        if (isHwDarkTheme(context)) {
            return resDark;
        }
        if (HwWidgetUtils.isActionbarBackgroundThemed(context)) {
            return res;
        }
        if (getSuggestionForgroundColorStyle(context) != 0) {
            return resDark;
        }
        if (colorfulResLight == 0 || !isPrimaryColorfulEnabled(context, 0)) {
            return resLight;
        }
        return colorfulResLight;
    }

    public Drawable getCompoundButtonDrawable(TypedValue tv, Context context, Drawable old) {
        Drawable dr = old;
        return old;
    }

    public int getCompoundButtonDrawableRes(Context context, int oldResId) {
        return oldResId;
    }

    public Drawable getTrackDrawable(TypedValue tv, Context context, Drawable old) {
        return old;
    }

    public Drawable getFastScrollerThumbDrawable(TypedValue tv, Context context, Drawable old) {
        return old;
    }

    public void setTextColorful(View child, Context context, boolean isHwTheme) {
    }

    public void setProgressDrawableTiled(ProgressBar progressBar, TypedValue tv, Context context) {
    }

    public void setIndeterminateDrawableTiled(ProgressBar progressBar, TypedValue tv, Context context) {
    }

    public boolean isPrimaryColorfulEnabled(Context context, int color) {
        int primaryColor = color;
        if (color == 0) {
            Resources res = context.getResources();
            if (res != null) {
                primaryColor = res.getColor(33882232);
            }
        }
        if (primaryColor == 0 || primaryColor == -197380 || (isNoColorfulTheme(context) ^ 1) == 0) {
            return false;
        }
        return true;
    }

    public boolean isColorfulEnabled(Context context) {
        return false;
    }

    public int getControlColor(Resources res) {
        return 0;
    }

    private int getAppDefinedPrimaryColor(Context context) {
        if (context instanceof Activity) {
            Activity ac = (Activity) context;
            if (ac.getWindow() instanceof PhoneWindow) {
                PhoneWindow pw = (PhoneWindow) ac.getWindow();
                if (pw.getIsForcedStatusBarColor()) {
                    int forcedColor = pw.getForcedStatusBarColor();
                    Log.d(TAG, "getAppDefinedPrimaryColor has Color=0x" + Integer.toHexString(forcedColor) + " set by setStatusBarColor()");
                    return forcedColor;
                }
            }
        }
        TypedArray ta = context.getTheme().obtainStyledAttributes(this.colorPrimaryAtts);
        int color = ta.getColor(0, -197380);
        ta.recycle();
        return color;
    }

    public int getPrimaryColor(Context context) {
        int appDefColor = getAppDefinedPrimaryColor(context);
        if (Color.alpha(appDefColor) == 0) {
            return appDefColor;
        }
        Resources res = context.getResources();
        if (res != null) {
            if (!(res.getImpl() == null || res.getImpl().getHwResourcesImpl() == null)) {
                res.setPackageName(context.getPackageName());
                res.getImpl().getHwResourcesImpl().setPackageName(context.getPackageName());
            }
            int primaryColor = res.getColor(33882232);
            if (isPrimaryColorfulEnabled(context, primaryColor)) {
                return primaryColor;
            }
        }
        return appDefColor;
    }

    private boolean isNoColorfulTheme(Context context) {
        if (this.mNoColorfulAttrId == 0) {
            this.mNoColorfulAttrId = context.getResources().getIdentifier("androidhwext:attr/immersionNoColorful", null, null);
        }
        if (this.mNoColorfulAttrId <= 0) {
            return false;
        }
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{this.mNoColorfulAttrId});
        boolean isNoColorful = ta.getBoolean(0, false);
        ta.recycle();
        return isNoColorful;
    }

    public int getSuggestionForgroundColorStyle(int colorBackground) {
        if (colorBackground == -197380) {
            return 0;
        }
        if (colorBackground == -14106426 || colorBackground == -16744961) {
            return 1;
        }
        for (int i : sColorfulDark) {
            if (colorBackground == i) {
                return 1;
            }
        }
        for (int i2 : sColorfulLight) {
            if (colorBackground == i2) {
                return 0;
            }
        }
        int r = Color.red(colorBackground);
        int g = Color.green(colorBackground);
        int b = Color.blue(colorBackground);
        int maxValue = g > b ? g : b;
        int minValue = g < b ? g : b;
        return (r > maxValue ? r : maxValue) + (r < minValue ? r : minValue) >= 255 ? 0 : 1;
    }

    public int getSuggestionForgroundColorStyle(Context context) {
        return getSuggestionForgroundColorStyle(getPrimaryColor(context));
    }

    public boolean isBlackActionBar(Context context) {
        int primaryColor = getPrimaryColor(context);
        int r = Color.red(primaryColor);
        int g = Color.green(primaryColor);
        int b = Color.blue(primaryColor);
        if (r == 0 && g == 0 && b == 0) {
            return true;
        }
        return false;
    }

    public DisplayMode getDisplayMode(Context context) {
        if (this.mDisplayMode != null) {
            return this.mDisplayMode;
        }
        if (DEV_DPI == REAL_DPI) {
            this.mDisplayMode = DisplayMode.Normal;
            return this.mDisplayMode;
        }
        int index = -1;
        int[] dpis = context.getResources().getIntArray(33816577);
        if (dpis.length > 0) {
            if (REAL_DPI <= dpis[0]) {
                index = 0;
            } else if (REAL_DPI >= dpis[dpis.length - 1]) {
                index = dpis.length - 1;
            } else {
                for (int i = 1; i < dpis.length - 1; i++) {
                    if (REAL_DPI == dpis[i]) {
                        index = i;
                        break;
                    }
                }
            }
        }
        if (index < 0) {
            return DisplayMode.Normal;
        }
        DisplayMode[] modes = DisplayMode.values();
        if (index >= modes.length - 1) {
            index = modes.length - 2;
        }
        this.mDisplayMode = modes[index + 1];
        return this.mDisplayMode;
    }

    public HwRippleForeground getHwRippleForeground(RippleDrawable owner, Rect bounds, boolean isBounded, boolean forceSoftware, int type) {
        return new HwRippleForegroundProxy(owner, bounds, isBounded, forceSoftware, type);
    }

    public HwLoadingDrawable getHwLoadingDrawable(Resources res, int size, int color) {
        return new HwLoadingDrawableImpl(res, size, color);
    }

    public HwLoadingDrawable getHwLoadingDrawable(Resources res, int size) {
        return new HwLoadingDrawableImpl(res, size);
    }

    public AbsHwDecorCaptionView getHwDecorCaptionView(LayoutInflater inflater) {
        return (AbsHwDecorCaptionView) inflater.inflate(34013283, null);
    }

    public void autoTextSize(TextView textview, Context context, float originTextSize) {
        if (textview == null || context == null) {
            Log.w(TAG, "autoTextSize, textview = " + textview + ", context = " + context);
            return;
        }
        float density = context.getResources().getDisplayMetrics().scaledDensity;
        int maxSizeInPix = (int) originTextSize;
        int minSizeInPix = context.getResources().getDimensionPixelSize(34472197);
        int step = (int) (1.0f * density);
        if (maxSizeInPix <= 0 || minSizeInPix <= 0 || step <= 0) {
            Log.w(TAG, "maxSizeInPix = " + maxSizeInPix + ", minSizeInPix = " + minSizeInPix + ", step = " + step);
            return;
        }
        if (textview.getMaxLines() == 1 && maxSizeInPix > minSizeInPix) {
            textview.setHorizontallyScrolling(false);
            textview.setAutoSizeTextTypeUniformWithConfiguration(minSizeInPix, maxSizeInPix, step, 0);
            textview.setAutoSizeTextTypeWithDefaults(0);
        }
    }

    public void reportSrBigData(int eventId, int keyEventId, Object reportMsg) {
        EventStream eStream = IMonitor.openEventStream(eventId);
        switch (keyEventId) {
            case 0:
                try {
                    eStream.setParam((short) 0, ((Integer) reportMsg).intValue());
                    IMonitor.sendEvent(eStream);
                    break;
                } catch (ClassCastException e) {
                    Log.d(TAG, "reportSrBigData ClassCastException when keyEventId = 0");
                    break;
                } catch (NullPointerException e2) {
                    Log.d(TAG, "reportSrBigData NullPointerException when keyEventId = 0");
                    break;
                }
        }
        IMonitor.closeEventStream(eStream);
    }
}
