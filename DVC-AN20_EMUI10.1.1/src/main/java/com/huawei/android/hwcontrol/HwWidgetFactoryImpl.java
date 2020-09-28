package com.huawei.android.hwcontrol;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Dialog;
import android.app.WindowConfiguration;
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
import android.hwcontrol.HwWidgetFactory;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.IMonitor;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ActionMenuPresenter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FastScrollerEx;
import android.widget.HwParallelWorker;
import android.widget.HwParallelWorkerImpl;
import android.widget.HwSmartSlideOptimize;
import android.widget.HwSpringBackHelper;
import android.widget.HwWidgetAppAttrsHelper;
import android.widget.HwWidgetColumn;
import android.widget.IHwSplineOverScroller;
import android.widget.IHwWechatOptimize;
import android.widget.ListView;
import android.widget.OverScroller;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.sr.HwAISRImageViewTaskManager;
import android.widget.sr.SRBitmapManager;
import android.widget.sr.SRInfo;
import android.widget.sr.Utils;
import com.android.internal.R;
import com.android.internal.app.AlertController;
import com.android.internal.app.MicroAlertController;
import com.android.internal.app.WindowDecorActionBar;
import com.android.internal.policy.PhoneWindow;
import com.android.internal.widget.DecorCaptionView;
import com.android.internal.widget.DecorCaptionViewBridge;
import huawei.android.widget.HwFastScroller;
import huawei.android.widget.HwOverflowMenuButton;
import huawei.android.widget.HwSmartSlideOptimizeImpl;
import huawei.android.widget.HwSplineOverScrollerImpl;
import huawei.android.widget.HwSpringBackHelperImpl;
import huawei.android.widget.HwWechatOptimizeImpl;
import huawei.android.widget.HwWidgetAppAttrsHelperImpl;
import huawei.android.widget.HwWidgetColumnImpl;
import huawei.com.android.internal.app.HwAlertController;
import huawei.com.android.internal.widget.HwWidgetUtils;

public class HwWidgetFactoryImpl implements HwWidgetFactory.Factory {
    private static final int AUTOSIZE_STEP_GRANULARITY = 1;
    private static final int COLOR_DARKER_BACKGROUND = -16744961;
    private static final int COLOR_DARK_BACKGROUND = -14106426;
    private static final int DEFAULT_INDEX = -1;
    private static final int DEV_DPI = SystemProperties.getInt(REAL_RO_DENSITY, SystemProperties.getInt(RO_DENSITY, 160));
    static final boolean IS_DEBUG = false;
    private static final boolean IS_FORCE_FULL_ANIM_ENABLE = SystemProperties.getBoolean("hw_sc.force_full_anim_enable", false);
    private static final int LIGHTNESS_THRESHOLD = 255;
    private static final int MODE_INDEX_SUB = 2;
    private static final String PERSIST_DENSITY = "persist.sys.dpi";
    private static final int REAL_DPI = SystemProperties.getInt(PERSIST_DENSITY, DEV_DPI);
    private static final String REAL_RO_DENSITY = "ro.sf.real_lcd_density";
    public static final int REPORRT_SR_COUNT_INT = 0;
    private static final String RO_DENSITY = "ro.sf.lcd_density";
    static final String TAG = "HwWidgetFactoryImpl";
    private static final int TV_DATA_DARK = 2;
    private static final int TV_DATA_DIV = 3;
    private static final int TV_DATA_EMPHASIZE = 0;
    private static final int TV_DATA_LIGHT = 1;
    private static int[] sDarkColors = {-1762269, -44681, -53905, -1499549, -49023, -4560696, -5552196, -2080517, -5635841, -6982195, -8497214, -8630785, -10149889, -8812853, -10720320, -11309570, -12756226, -13611010, -11110404, -11617041, -12230946, -12889906, -9926145, -11701249, -11703809, -43230, -765666, -1684967, -49920, -6190977, -8875876, -11168294, -11498034, -5088571, -1740242, -12275252, -10523207, -26624, -14575885};
    private static int[] sLightColors = {-16729900, -14244198, -16728155, -12403391, -13914325, -15407339, -15546624, -8604862, -9920712, -10167017, -4142541, -5262293, -6382300, -5314048, -3364096, -291840, -689152};
    private final int[] mColorPrimaryAttrs = {16843827};
    private HwWidgetFactory.DisplayMode mDisplayMode = null;
    private int mNoColorfulAttrId = 0;

    public boolean isHwTheme() {
        return false;
    }

    public boolean isHwTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(33620020, typedValue, true);
        if (typedValue.type == 16) {
            return true;
        }
        return false;
    }

    public boolean isHwLightTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(33620020, typedValue, true);
        if (typedValue.type == 16 && typedValue.data > 0 && typedValue.data % 3 == 1) {
            return true;
        }
        return false;
    }

    public boolean isHwDarkTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(33620020, typedValue, true);
        if (typedValue.type == 16 && typedValue.data > 0 && typedValue.data % 3 == 2) {
            return true;
        }
        return false;
    }

    public boolean isHwEmphasizeTheme(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(33620020, typedValue, true);
        if (typedValue.type == 16 && typedValue.data > 0 && typedValue.data % 3 == 0) {
            return true;
        }
        return false;
    }

    public int getThemeIdImpl(Bundle data, Resources res) {
        String themeName;
        if (data == null || (themeName = data.getString("hwc-theme")) == null) {
            return 0;
        }
        return res.getIdentifier(themeName, null, null);
    }

    public int getHuaweiRealThemeImpl(int theme) {
        return theme;
    }

    public boolean initAddtionalStyle(Context context, AttributeSet attrs) {
        return isHwTheme(context);
    }

    public HwWidgetFactory.HwTextView newHwTextView(Context context, TextView view, AttributeSet attrs) {
        return new TextViewFactory();
    }

    public HwWidgetFactory.HwToast newHwToast(Context context, Toast view, AttributeSet attrs) {
        return new ToastFactory(context, view, attrs);
    }

    public WindowDecorActionBar getHuaweiActionBarImpl(Activity activity) {
        WindowDecorActionBar actionBar = new WindowDecorActionBar(activity);
        if (isHwTheme(activity)) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayUseLogoEnabled(false);
        }
        return actionBar;
    }

    public WindowDecorActionBar getHuaweiActionBarImpl(Dialog dialog) {
        WindowDecorActionBar actionBar = new WindowDecorActionBar(dialog);
        if (isHwTheme(dialog.getContext())) {
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayUseLogoEnabled(false);
        }
        return actionBar;
    }

    public AlertController newHwAlertController(Context context, DialogInterface dialogInterface, Window window) {
        if (!isHwTheme(context)) {
            return AlertController.create(context, dialogInterface, window);
        }
        TypedArray alertDialogAttributes = context.obtainStyledAttributes(null, R.styleable.AlertDialog, 16842845, 0);
        int controllerType = alertDialogAttributes.getInt(12, 0);
        alertDialogAttributes.recycle();
        if (controllerType != 1) {
            return new HwAlertController(context, dialogInterface, window);
        }
        return new MicroAlertController(context, dialogInterface, window);
    }

    public View newHwOverflowMenuButton(Context context, ActionMenuPresenter actionMenuPresenter) {
        if (!isHwTheme(context)) {
            return null;
        }
        return new HwOverflowMenuButton(context, actionMenuPresenter);
    }

    public FastScrollerEx getHwFastScroller(AbsListView absListView, int fastScrollStyle, Context context) {
        if (!isHwTheme(context)) {
            return new FastScrollerEx(absListView, fastScrollStyle);
        }
        return new HwFastScroller(absListView, fastScrollStyle);
    }

    public IHwSplineOverScroller getHwSplineOverScrollerImpl(OverScroller.SplineOverScroller spline, Context context) {
        return new HwSplineOverScrollerImpl(spline, context);
    }

    public IHwWechatOptimize getHwWechatOptimizeImpl() {
        return HwWechatOptimizeImpl.getInstance();
    }

    public HwSmartSlideOptimize getHwSmartSlideOptimizeImpl(Context context) {
        return HwSmartSlideOptimizeImpl.getInstance(context);
    }

    public HwWidgetFactory.HwDialogStub newHwDialogStub(Context context, Window window, Dialog dialog, AttributeSet attrs) {
        if (!isHwTheme(context)) {
            return new HwDialogStubImpl(context, window, dialog, 0);
        }
        return new HwDialogStubImpl(context, window, dialog, 2);
    }

    public Drawable getEdgeEffectImpl(Resources res) {
        return res.getDrawable(33751554);
    }

    public Drawable getGlowEffectImpl(Resources res) {
        return res.getDrawable(33751555);
    }

    public HwParallelWorker getHwParallelWorkerImpl(ListView view) {
        return new HwParallelWorkerImpl(view);
    }

    public void setImmersionStyle(Context context, TextView textView, int colorResDark, int colorResLight, int colorfulResLight, boolean isDefaultDark) {
        int colorRes = isDefaultDark ? colorResDark : colorResLight;
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
        if (textView != null) {
            textView.setTextColor(context.getResources().getColorStateList(colorRes));
        }
    }

    public int getImmersionResource(Context context, int resLight, int colorfulResLight, int resDark, boolean isDefaultDark) {
        int res = isDefaultDark ? resDark : resLight;
        if (isHwDarkTheme(context)) {
            return resDark;
        }
        if (HwWidgetUtils.isActionbarBackgroundThemed(context)) {
            return res;
        }
        if (getSuggestionForgroundColorStyle(context) == 0) {
            return (colorfulResLight == 0 || !isPrimaryColorfulEnabled(context, 0)) ? resLight : colorfulResLight;
        }
        return resDark;
    }

    public Drawable getCompoundButtonDrawable(TypedValue typedValue, Context context, Drawable old) {
        return old;
    }

    public int getCompoundButtonDrawableRes(Context context, int oldResId) {
        return oldResId;
    }

    public Drawable getTrackDrawable(TypedValue typedValue, Context context, Drawable old) {
        return old;
    }

    public Drawable getFastScrollerThumbDrawable(TypedValue typedValue, Context context, Drawable old) {
        return old;
    }

    public void setTextColorful(View child, Context context, boolean isHwTheme) {
    }

    public void setProgressDrawableTiled(ProgressBar progressBar, TypedValue typedValue, Context context) {
    }

    public void setIndeterminateDrawableTiled(ProgressBar progressBar, TypedValue typedValue, Context context) {
    }

    public boolean isPrimaryColorfulEnabled(Context context, int color) {
        Resources res;
        int primaryColor = color;
        if (color == 0 && (res = context.getResources()) != null) {
            primaryColor = res.getColor(33882232);
        }
        if (primaryColor == 0 || primaryColor == -197380 || isColorfulThemeEmpty(context)) {
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
        if ((context instanceof Activity) && (((Activity) context).getWindow() instanceof PhoneWindow)) {
            PhoneWindow phoneWindow = ((Activity) context).getWindow();
            if (phoneWindow.getIsForcedStatusBarColor()) {
                return phoneWindow.getForcedStatusBarColor();
            }
        }
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(this.mColorPrimaryAttrs);
        int color = typedArray.getColor(0, -197380);
        typedArray.recycle();
        return color;
    }

    public int getPrimaryColor(Context context) {
        Resources res;
        int appDefColor = getAppDefinedPrimaryColor(context);
        if (!(Color.alpha(appDefColor) == 0 || (res = context.getResources()) == null)) {
            int primaryColor = res.getColor(33882555);
            if (isPrimaryColorfulEnabled(context, primaryColor)) {
                return primaryColor;
            }
        }
        return appDefColor;
    }

    private boolean isColorfulThemeEmpty(Context context) {
        if (this.mNoColorfulAttrId == 0) {
            this.mNoColorfulAttrId = context.getResources().getIdentifier("androidhwext:attr/immersionNoColorful", null, null);
        }
        int i = this.mNoColorfulAttrId;
        if (i <= 0) {
            return false;
        }
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{i});
        boolean isColorfulEmpty = typedArray.getBoolean(0, false);
        typedArray.recycle();
        return isColorfulEmpty;
    }

    public int getSuggestionForgroundColorStyle(int colorBackground) {
        if (colorBackground == -197380) {
            return 0;
        }
        if (colorBackground == COLOR_DARK_BACKGROUND || colorBackground == COLOR_DARKER_BACKGROUND) {
            return 1;
        }
        int i = 0;
        while (true) {
            int[] iArr = sDarkColors;
            if (i >= iArr.length) {
                int i2 = 0;
                while (true) {
                    int[] iArr2 = sLightColors;
                    if (i2 >= iArr2.length) {
                        int red = Color.red(colorBackground);
                        int green = Color.green(colorBackground);
                        int blue = Color.blue(colorBackground);
                        int maxValue = green > blue ? green : blue;
                        int minValue = green < blue ? green : blue;
                        if ((red > maxValue ? red : maxValue) + (red < minValue ? red : minValue) >= 255) {
                            return 0;
                        }
                        return 1;
                    } else if (colorBackground == iArr2[i2]) {
                        return 0;
                    } else {
                        i2++;
                    }
                }
            } else if (colorBackground == iArr[i]) {
                return 1;
            } else {
                i++;
            }
        }
    }

    public int getSuggestionForgroundColorStyle(Context context) {
        return getSuggestionForgroundColorStyle(getPrimaryColor(context));
    }

    public boolean isBlackActionBar(Context context) {
        int primaryColor = getPrimaryColor(context);
        return Color.red(primaryColor) == 0 && Color.green(primaryColor) == 0 && Color.blue(primaryColor) == 0;
    }

    public HwWidgetFactory.DisplayMode getDisplayMode(Context context) {
        HwWidgetFactory.DisplayMode displayMode = this.mDisplayMode;
        if (displayMode != null) {
            return displayMode;
        }
        if (DEV_DPI == REAL_DPI) {
            this.mDisplayMode = HwWidgetFactory.DisplayMode.Normal;
            return this.mDisplayMode;
        }
        int index = -1;
        int[] dpis = context.getResources().getIntArray(33816577);
        if (dpis.length <= 0) {
            return HwWidgetFactory.DisplayMode.Normal;
        }
        int i = dpis[0];
        int i2 = REAL_DPI;
        if (i >= i2) {
            index = 0;
        } else if (dpis[dpis.length - 1] <= i2) {
            index = dpis.length - 1;
        } else {
            int i3 = 1;
            while (true) {
                if (i3 >= dpis.length - 1) {
                    break;
                } else if (dpis[i3] == REAL_DPI) {
                    index = i3;
                    break;
                } else {
                    i3++;
                }
            }
        }
        if (index < 0) {
            return HwWidgetFactory.DisplayMode.Normal;
        }
        HwWidgetFactory.DisplayMode[] modes = HwWidgetFactory.DisplayMode.values();
        if (index >= modes.length - 1) {
            index = modes.length - 2;
        }
        this.mDisplayMode = modes[index + 1];
        return this.mDisplayMode;
    }

    public HwRippleForeground getHwRippleForeground(RippleDrawable owner, Rect bounds, boolean isBounded, boolean isSoftwareForced, int type) {
        return new HwRippleForegroundProxy(owner, bounds, isBounded, isSoftwareForced, type);
    }

    public HwLoadingDrawable getHwLoadingDrawable(Resources res, int size, int color) {
        return new HwLoadingDrawableImpl(res, size, color);
    }

    public HwLoadingDrawable getHwLoadingDrawable(Resources res, int size) {
        return new HwLoadingDrawableImpl(res, size);
    }

    public DecorCaptionViewBridge getHwDecorCaptionView(LayoutInflater inflater) {
        Context context = ActivityThread.currentApplication().getApplicationContext();
        boolean isRtlSupport = context.getApplicationInfo().hasRtlSupport();
        int layoutDirection = context.getResources().getConfiguration().getLayoutDirection();
        if (!isRtlSupport && layoutDirection == 1) {
            View rtlDecorView = inflater.inflate(34013288, (ViewGroup) null);
            if (rtlDecorView instanceof DecorCaptionViewBridge) {
                return (DecorCaptionViewBridge) rtlDecorView;
            }
        }
        View decorView = inflater.inflate(34013283, (ViewGroup) null);
        if (decorView instanceof DecorCaptionViewBridge) {
            return (DecorCaptionViewBridge) decorView;
        }
        return null;
    }

    public void autoTextSize(TextView textView, Context context, float originTextSize) {
        if (textView == null || context == null) {
            Log.w(TAG, "autoTextSize, textView = " + textView + ", context = " + context);
            return;
        }
        float density = context.getResources().getDisplayMetrics().scaledDensity;
        int maxSizeInPix = (int) originTextSize;
        int minSizeInPix = context.getResources().getDimensionPixelSize(34472197);
        float step = 1.0f * density;
        if (maxSizeInPix <= 0 || minSizeInPix <= 0 || step <= 0.0f) {
            Log.w(TAG, "maxSizeInPix = " + maxSizeInPix + ", minSizeInPix = " + minSizeInPix + ", step = " + step);
        } else if (textView.getMaxLines() == 1 && maxSizeInPix > minSizeInPix) {
            textView.setHorizontallyScrolling(false);
            int autoSizeValuesLength = ((int) Math.floor((double) (((float) (maxSizeInPix - minSizeInPix)) / step))) + 1;
            int[] autoSizeInPxTextSizes = new int[autoSizeValuesLength];
            autoSizeInPxTextSizes[autoSizeValuesLength - 1] = maxSizeInPix;
            for (int i = 0; i < autoSizeValuesLength - 1; i++) {
                autoSizeInPxTextSizes[i] = Math.round(((float) minSizeInPix) + (((float) i) * step));
            }
            textView.setAutoSizeTextTypeUniformWithPresetSizes(autoSizeInPxTextSizes, 0);
            textView.setAutoSizeTextTypeWithDefaults(0);
        }
    }

    public void reportSrBigData(int eventId, int keyEventId, Object reportMsg) {
        IMonitor.EventStream eventStream = IMonitor.openEventStream(eventId);
        if (keyEventId == 0) {
            if (eventStream == null || reportMsg == null) {
                Log.d(TAG, "reportSrBigData NullPointerException when keyEventId = 0");
            } else {
                try {
                    if (reportMsg instanceof Integer) {
                        eventStream.setParam((short) 0, ((Integer) reportMsg).intValue());
                        IMonitor.sendEvent(eventStream);
                    }
                } catch (ClassCastException e) {
                    Log.d(TAG, "reportSrBigData ClassCastException when keyEventId = 0");
                }
            }
        }
        IMonitor.closeEventStream(eventStream);
    }

    public Drawable getDesireCompoundButtonDrawable(Context context, CompoundButton view, TypedArray typedArray, int defButtonArrayIndex) {
        if (typedArray == null) {
            Log.w(TAG, "typedArray is null!");
            return null;
        }
        Drawable buttonDrawable = typedArray.getDrawable(defButtonArrayIndex);
        if (context == null || view == null) {
            Log.w(TAG, "context or view is null!");
            return buttonDrawable;
        } else if ((!IS_EMUI_SUPERLITE && !IS_EMUI_LITE && !IS_NOVA_PERF) || IS_FORCE_FULL_ANIM_ENABLE || !isHwTheme(context)) {
            return buttonDrawable;
        } else {
            int indicatorAttrId = 0;
            int defButtonDrawableResId = typedArray.getResourceId(defButtonArrayIndex, 0);
            if (view instanceof RadioButton) {
                if (defButtonDrawableResId == 33751175 || defButtonDrawableResId == 33751174) {
                    indicatorAttrId = 33620059;
                }
            } else if (!(view instanceof CheckBox)) {
                return buttonDrawable;
            } else {
                if (defButtonDrawableResId == 33751086 || defButtonDrawableResId == 33751085) {
                    indicatorAttrId = 33620058;
                } else if (defButtonDrawableResId == 33751343 || defButtonDrawableResId == 33751256) {
                    indicatorAttrId = 33620143;
                }
            }
            TypedValue typedValue = new TypedValue();
            if (context.getTheme().resolveAttribute(indicatorAttrId, typedValue, true)) {
                return context.getDrawable(typedValue.resourceId);
            }
            Log.w(TAG, "indicatorAttr resolved failed!");
            return buttonDrawable;
        }
    }

    public Drawable getDesireCheckMarkDrawable(Context context, TypedArray typedArray, int defCheckMarkTypedArrayIndex) {
        int indicatorAttr;
        if (typedArray == null) {
            Log.w(TAG, "typedArray is null!");
            return null;
        }
        Drawable checkMarkdrawable = typedArray.getDrawable(defCheckMarkTypedArrayIndex);
        if (context == null) {
            Log.w(TAG, "context is null!");
            return checkMarkdrawable;
        } else if ((!IS_EMUI_SUPERLITE && !IS_EMUI_LITE && !IS_NOVA_PERF) || IS_FORCE_FULL_ANIM_ENABLE || !isHwTheme(context)) {
            return checkMarkdrawable;
        } else {
            TypedValue typedValue = new TypedValue();
            int defCheckMarkDrawableResId = typedArray.getResourceId(defCheckMarkTypedArrayIndex, 0);
            if (defCheckMarkDrawableResId == 33751175 || defCheckMarkDrawableResId == 33751174) {
                indicatorAttr = 33620059;
            } else if (defCheckMarkDrawableResId == 33751086 || defCheckMarkDrawableResId == 33751085) {
                indicatorAttr = 33620058;
            } else if (defCheckMarkDrawableResId == 33751343 || defCheckMarkDrawableResId == 33751256) {
                indicatorAttr = 33620143;
            } else {
                Log.w(TAG, "checkMark no drawable was found!");
                return checkMarkdrawable;
            }
            if (context.getTheme().resolveAttribute(indicatorAttr, typedValue, true)) {
                return context.getDrawable(typedValue.resourceId);
            }
            Log.w(TAG, "indicatorAttr resolved failed!");
            return checkMarkdrawable;
        }
    }

    public Drawable getDesireSwitchThumbDrawable(Context context, Drawable defSwitchThumbDrawable) {
        if (context == null) {
            Log.w(TAG, "context is null!");
            return defSwitchThumbDrawable;
        } else if ((!IS_EMUI_SUPERLITE && !IS_EMUI_LITE) || IS_FORCE_FULL_ANIM_ENABLE || !isHwTheme(context)) {
            return defSwitchThumbDrawable;
        } else {
            TypedValue typedValue = new TypedValue();
            if (!context.getTheme().resolveAttribute(33620060, typedValue, true)) {
                return defSwitchThumbDrawable;
            }
            return context.getDrawable(typedValue.resourceId);
        }
    }

    public Drawable getDesireSwitchTrackDrawable(Context context, Drawable defSwitchTrackDrawable) {
        if (context == null) {
            Log.w(TAG, "context is null!");
            return defSwitchTrackDrawable;
        } else if ((!IS_EMUI_SUPERLITE && !IS_EMUI_LITE) || IS_FORCE_FULL_ANIM_ENABLE || !isHwTheme(context)) {
            return defSwitchTrackDrawable;
        } else {
            TypedValue typedValue = new TypedValue();
            if (context.getTheme().resolveAttribute(33620061, typedValue, true)) {
                return context.getDrawable(typedValue.resourceId);
            }
            Log.w(TAG, "trackAttr resolved failed!");
            return defSwitchTrackDrawable;
        }
    }

    public boolean isSuperResolutionSupport() {
        return Utils.isSuperResolutionSupport();
    }

    public boolean checkIsInSRWhiteList(Context context) {
        return false;
    }

    public boolean checkIsInSRBlackList(Object object) {
        return true;
    }

    public boolean checkIsFullScreen(Context context, int width, int height) {
        return false;
    }

    public boolean checkMatchResolution(Drawable drawable) {
        return false;
    }

    public SRBitmapManager getSRBitmapManager() {
        return null;
    }

    public HwAISRImageViewTaskManager getHwAISRImageViewTaskManager(Context context) {
        return null;
    }

    public SRInfo createSRInfo() {
        return null;
    }

    public HwSpringBackHelper getHwSpringBackHelperImpl() {
        return new HwSpringBackHelperImpl();
    }

    public DecorCaptionView getHwMultiWindowCaptionView(LayoutInflater inflater, int windowMode) {
        if (WindowConfiguration.isHwFreeFormWindowingMode(windowMode)) {
            View decorCaptionView = inflater.inflate(34013358, (ViewGroup) null);
            if (decorCaptionView instanceof DecorCaptionView) {
                return (DecorCaptionView) decorCaptionView;
            }
        } else if (!WindowConfiguration.isHwSplitScreenWindowingMode(windowMode)) {
            return null;
        } else {
            View decorCaptionView2 = inflater.inflate(34013360, (ViewGroup) null);
            if (decorCaptionView2 instanceof DecorCaptionView) {
                return (DecorCaptionView) decorCaptionView2;
            }
        }
        return null;
    }

    public HwWidgetColumn getHwWidgetColumnImpl(Context context) {
        return new HwWidgetColumnImpl(context);
    }

    public HwWidgetAppAttrsHelper getHwWidgetAppAttrsHelperImpl(Context context) {
        return new HwWidgetAppAttrsHelperImpl(context);
    }
}
