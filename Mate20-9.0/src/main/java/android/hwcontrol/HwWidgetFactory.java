package android.hwcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.HwLoadingDrawable;
import android.graphics.drawable.HwRippleForeground;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ActionMenuPresenter;
import android.widget.AppSecurityPermissions;
import android.widget.CompoundButton;
import android.widget.Editor;
import android.widget.HwSplineOverScrollerDummy;
import android.widget.HwWechatOptimizeImplDummy;
import android.widget.IHwSplineOverScroller;
import android.widget.IHwWechatOptimize;
import android.widget.ImageView;
import android.widget.OverScroller;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.sr.HwAISRImageViewTaskManager;
import android.widget.sr.SRBitmapManager;
import android.widget.sr.SRInfo;
import com.android.internal.app.AlertController;
import com.android.internal.app.WindowDecorActionBar;
import com.android.internal.widget.AbsHwDecorCaptionView;

public class HwWidgetFactory {
    public static final int DEFAULT_PRIMARY_COLOR = -197380;
    private static final boolean IS_EMUI3_0 = true;
    private static final String TAG = "HwWidgetFactory";
    private static final Object mLock = new Object();
    private static Factory obj = null;

    public enum DisplayMode {
        Normal,
        Small,
        Medium,
        Large
    }

    public interface Factory {
        public static final boolean IS_EMUI_LITE = SystemProperties.getBoolean("ro.build.hw_emui_lite.enable", false);
        public static final boolean IS_EMUI_SUPERLITE = "SuperLite".equals(SystemProperties.get("ro.build.hw_emui_feature_level", ""));
        public static final boolean IS_NOVA_PERF = SystemProperties.getBoolean("ro.config.hw_nova_performance", false);

        void autoTextSize(TextView textView, Context context, float f);

        boolean checkIsFullScreen(Context context, int i, int i2);

        boolean checkIsInSRBlackList(Object obj);

        boolean checkIsInSRWhiteList(Context context);

        boolean checkMatchResolution(Drawable drawable);

        SRInfo createSRInfo();

        Drawable getCompoundButtonDrawable(TypedValue typedValue, Context context, Drawable drawable);

        int getCompoundButtonDrawableRes(Context context, int i);

        int getControlColor(Resources resources);

        Drawable getDesireCheckMarkDrawable(Context context, TypedArray typedArray, int i);

        Drawable getDesireCompoundButtonDrawable(Context context, CompoundButton compoundButton, TypedArray typedArray, int i);

        Drawable getDesireSwitchThumbDrawable(Context context, Drawable drawable);

        Drawable getDesireSwitchTrackDrawable(Context context, Drawable drawable);

        DisplayMode getDisplayMode(Context context);

        Drawable getEdgeEffectImpl(Resources resources);

        Drawable getFastScrollerThumbDrawable(TypedValue typedValue, Context context, Drawable drawable);

        Drawable getGlowEffectImpl(Resources resources);

        WindowDecorActionBar getHuaweiActionBarImpl(Activity activity);

        WindowDecorActionBar getHuaweiActionBarImpl(Dialog dialog);

        int getHuaweiRealThemeImpl(int i);

        HwAISRImageViewTaskManager getHwAISRImageViewTaskManager(Context context);

        AbsHwDecorCaptionView getHwDecorCaptionView(LayoutInflater layoutInflater);

        Object getHwFastScroller(AbsListView absListView, int i, Context context);

        HwLoadingDrawable getHwLoadingDrawable(Resources resources, int i);

        HwLoadingDrawable getHwLoadingDrawable(Resources resources, int i, int i2);

        HwRippleForeground getHwRippleForeground(RippleDrawable rippleDrawable, Rect rect, boolean z, boolean z2, int i);

        IHwSplineOverScroller getHwSplineOverScrollerImpl(OverScroller.SplineOverScroller splineOverScroller, Context context);

        IHwWechatOptimize getHwWechatOptimizeImpl();

        int getImmersionResource(Context context, int i, int i2, int i3, boolean z);

        int getPrimaryColor(Context context);

        SRBitmapManager getSRBitmapManager();

        int getSuggestionForgroundColorStyle(int i);

        int getSuggestionForgroundColorStyle(Context context);

        int getThemeIdImpl(Bundle bundle, Resources resources);

        Drawable getTrackDrawable(TypedValue typedValue, Context context, Drawable drawable);

        boolean initAddtionalStyle(Context context, AttributeSet attributeSet);

        boolean isBlackActionBar(Context context);

        boolean isColorfulEnabled(Context context);

        boolean isHwDarkTheme(Context context);

        boolean isHwEmphasizeTheme(Context context);

        boolean isHwLightTheme(Context context);

        boolean isHwTheme();

        boolean isHwTheme(Context context);

        boolean isSuperResolutionSupport();

        AlertController newHwAlertController(Context context, DialogInterface dialogInterface, Window window);

        HwDialogStub newHwDialogStub(Context context, Window window, Dialog dialog, AttributeSet attributeSet);

        View newHwOverflowMenuButton(Context context, ActionMenuPresenter actionMenuPresenter);

        HwTextView newHwTextView(Context context, TextView textView, AttributeSet attributeSet);

        HwToast newHwToast(Context context, Toast toast, AttributeSet attributeSet);

        void reportSrBigData(int i, int i2, Object obj);

        void setImmersionStyle(Context context, TextView textView, int i, int i2, int i3, boolean z);

        void setIndeterminateDrawableTiled(ProgressBar progressBar, TypedValue typedValue, Context context);

        void setProgressDrawableTiled(ProgressBar progressBar, TypedValue typedValue, Context context);

        void setTextColorful(View view, Context context, boolean z);
    }

    public interface HwDialogStub {
        public static final int DIALOG_STUB_MASK_ANIMATOR = 1;
        public static final int DIALOG_STUB_MASK_DISMISS = 2;

        void dismissDialogFactory();

        int getMask();

        boolean hasButtons();

        void showDialogFactory();
    }

    public interface HwTextView {
        Editor getEditor(TextView textView);

        void initTextViewAddtionalStyle(Context context, AttributeSet attributeSet, TextView textView, Editor editor);

        void initialTextView(Context context, AttributeSet attributeSet, TextView textView);

        boolean isCustomStyle();

        boolean playIvtEffect(Context context, String str);

        boolean playIvtEffect(Context context, String str, Object obj, int i, int i2);

        void reLayoutAfterMeasure(TextView textView, Layout layout);

        void setError(TextView textView, Context context, CharSequence charSequence);
    }

    public interface HwToast {
        void initialToast(Context context, AttributeSet attributeSet);

        View layoutInflate(Context context);
    }

    public static class PermissionInformation {
        public static AppSecurityPermissions.PermissionItemView getHwPermItemView(boolean showMoneyItemLayout, LayoutInflater inflater) {
            return inflater.inflate(showMoneyItemLayout ? 34013197 : 34013196, null);
        }

        public static void setPositiveButton(AlertDialog.Builder builder, final AlertDialog dialog) {
            builder.setPositiveButton(33685725, (DialogInterface.OnClickListener) new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    if (AlertDialog.this != null) {
                        AlertDialog.this.dismiss();
                    }
                }
            });
        }

        public static View getPermissionImageView(AppSecurityPermissions.PermissionItemView itemView) {
            return itemView.findViewById(34603081);
        }

        public static View getPermissionTextView(AppSecurityPermissions.PermissionItemView mItemView) {
            return mItemView.findViewById(34603082);
        }

        public static View getPermissionItemViewOld(LayoutInflater inflater) {
            return inflater.inflate(34013198, null);
        }

        public static TextView getPermissionItemViewOldPermGrpView(View view) {
            return (TextView) view.findViewById(34603083);
        }

        public static TextView getPermissionItemViewOldPermDescView(View view) {
            return (TextView) view.findViewById(34603084);
        }

        public static ImageView getPermissionItemViewOldImgView(View view) {
            return (ImageView) view.findViewById(34603081);
        }
    }

    public static HwTextView getHwTextView(Context context, TextView view, AttributeSet attrs) {
        Factory obj2 = getImplObject();
        if (obj2 == null) {
            return null;
        }
        return obj2.newHwTextView(context, view, attrs);
    }

    public static HwToast getHwToast(Context context, Toast view, AttributeSet attrs) {
        Factory obj2 = getImplObject();
        if (obj2 == null) {
            return null;
        }
        return obj2.newHwToast(context, view, attrs);
    }

    public static int getThemeId(Bundle data, Resources res) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getThemeIdImpl(data, res);
        }
        return 0;
    }

    public static int getHuaweiRealTheme(int theme) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiRealThemeImpl(theme);
        }
        return 0;
    }

    public static WindowDecorActionBar getHuaweiActionBarImpl(Activity activity) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiActionBarImpl(activity);
        }
        return new WindowDecorActionBar(activity);
    }

    public static WindowDecorActionBar getHuaweiActionBarImpl(Dialog dialog) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHuaweiActionBarImpl(dialog);
        }
        return new WindowDecorActionBar(dialog);
    }

    public static AlertController getHwAlertController(Context context, DialogInterface di, Window window) {
        Factory obj2 = getImplObject();
        return obj2 == null ? new AlertController(context, di, window) : obj2.newHwAlertController(context, di, window);
    }

    public static View getHwOverflowMenuButton(Context context, ActionMenuPresenter actionMenuPresenter) {
        Factory obj2 = getImplObject();
        if (obj2 == null) {
            return null;
        }
        return obj2.newHwOverflowMenuButton(context, actionMenuPresenter);
    }

    public static Object getHwFastScroller(AbsListView absListView, int fastScrollStyle, Context context) {
        return getImplObject().getHwFastScroller(absListView, fastScrollStyle, context);
    }

    public static IHwSplineOverScroller getHwSplineOverScroller(OverScroller.SplineOverScroller sos, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 == null) {
            return new HwSplineOverScrollerDummy();
        }
        return obj2.getHwSplineOverScrollerImpl(sos, context);
    }

    public static IHwWechatOptimize getHwWechatOptimize() {
        Factory obj2 = getImplObject();
        if (obj2 == null) {
            return new HwWechatOptimizeImplDummy();
        }
        return obj2.getHwWechatOptimizeImpl();
    }

    public static HwDialogStub getHwDialogStub(Context context, Window window, Dialog dialog, AttributeSet attrs) {
        Factory obj2 = getImplObject();
        if (obj2 == null) {
            return null;
        }
        return obj2.newHwDialogStub(context, window, dialog, attrs);
    }

    public static boolean checkIsHwTheme(Context context, AttributeSet attrs) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.initAddtionalStyle(context, attrs);
        }
        return false;
    }

    public static boolean isHwTheme(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.isHwTheme(context);
        }
        return false;
    }

    public static boolean isHwLightTheme(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.isHwLightTheme(context);
        }
        return false;
    }

    public static boolean isHwEmphasizeTheme(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.isHwEmphasizeTheme(context);
        }
        return false;
    }

    public static boolean isHwDarkTheme(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.isHwDarkTheme(context);
        }
        return false;
    }

    public static Drawable getEdgeEffect(Resources res) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getEdgeEffectImpl(res);
        }
        return null;
    }

    public static Drawable getGlowEffect(Resources res) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getGlowEffectImpl(res);
        }
        return null;
    }

    public static void setImmersionStyle(Context context, TextView textView, int colorResDark, int colorResLight, int colorfulResLight, boolean defaultIsDark) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.setImmersionStyle(context, textView, colorResDark, colorResLight, colorfulResLight, defaultIsDark);
        }
    }

    public static int getImmersionResource(Context context, int resLight, int colorfulResLight, int resDark, boolean defaultIsDark) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getImmersionResource(context, resLight, colorfulResLight, resDark, defaultIsDark);
        }
        return 0;
    }

    public static boolean isColorfulEnabled(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.isColorfulEnabled(context);
        }
        return false;
    }

    public static Drawable getCompoundButtonDrawable(TypedValue tv, Context context, Drawable old) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getCompoundButtonDrawable(tv, context, old);
        }
        return old;
    }

    public static int getCompoundButtonDrawableRes(Context context, int oldResId) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getCompoundButtonDrawableRes(context, oldResId);
        }
        return oldResId;
    }

    public static Drawable getTrackDrawable(TypedValue tv, Context context, Drawable old) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getTrackDrawable(tv, context, old);
        }
        return old;
    }

    public static Drawable getFastScrollerThumbDrawable(TypedValue tv, Context context, Drawable old) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getFastScrollerThumbDrawable(tv, context, old);
        }
        return old;
    }

    public static void setTextColorful(View child, Context context, boolean isHwTheme) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.setTextColorful(child, context, isHwTheme);
        }
    }

    public static void setProgressDrawableTiled(ProgressBar progressBar, TypedValue tv, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.setProgressDrawableTiled(progressBar, tv, context);
        }
    }

    public static void setIndeterminateDrawableTiled(ProgressBar progressBar, TypedValue tv, Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.setIndeterminateDrawableTiled(progressBar, tv, context);
        }
    }

    public static int getControlColor(Resources res) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getControlColor(res);
        }
        return 0;
    }

    public static int getPrimaryColor(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getPrimaryColor(context);
        }
        return DEFAULT_PRIMARY_COLOR;
    }

    public static int getSuggestionForgroundColorStyle(int colorBackground) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getSuggestionForgroundColorStyle(colorBackground);
        }
        return 0;
    }

    public static int getSuggestionForgroundColorStyle(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getSuggestionForgroundColorStyle(context);
        }
        return 0;
    }

    public static boolean isBlackActionBar(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.isBlackActionBar(context);
        }
        return false;
    }

    public static DisplayMode getDisplayMode(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getDisplayMode(context);
        }
        return DisplayMode.Normal;
    }

    public static HwRippleForeground getHwRippleForeground(RippleDrawable owner, Rect bounds, boolean isBounded, boolean forceSoftware, int type) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwRippleForeground(owner, bounds, isBounded, forceSoftware, type);
        }
        return null;
    }

    public static HwLoadingDrawable getHwLoadingDrawable(Resources res, int size) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLoadingDrawable(res, size);
        }
        return null;
    }

    public static HwLoadingDrawable getHwLoadingDrawable(Resources res, int size, int color) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwLoadingDrawable(res, size, color);
        }
        return null;
    }

    public static AbsHwDecorCaptionView getHwDecorCaptionView(LayoutInflater inflater) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwDecorCaptionView(inflater);
        }
        return null;
    }

    public static void autoTextSize(TextView textview, Context context, float originTextSize) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.autoTextSize(textview, context, originTextSize);
        }
    }

    public static void reportSrBigData(int eventId, int keyEventId, Object reportMsg) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            obj2.reportSrBigData(eventId, keyEventId, reportMsg);
        }
    }

    public static Drawable getDesireCompoundButtonDrawable(Context context, CompoundButton view, TypedArray ta, int defButtonArrayIndex) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getDesireCompoundButtonDrawable(context, view, ta, defButtonArrayIndex);
        }
        if (ta != null) {
            return ta.getDrawable(defButtonArrayIndex);
        }
        Log.w(TAG, "typedArray is null!");
        return null;
    }

    public static Drawable getDesireCheckMarkDrawable(Context context, TypedArray ta, int defCheckMarkTypedArrayIndex) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getDesireCheckMarkDrawable(context, ta, defCheckMarkTypedArrayIndex);
        }
        if (ta != null) {
            return ta.getDrawable(defCheckMarkTypedArrayIndex);
        }
        Log.w(TAG, "typedArray is null!");
        return null;
    }

    public static Drawable getDesireSwitchThumbDrawable(Context context, Drawable defSwitchThumbDrawable) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getDesireSwitchThumbDrawable(context, defSwitchThumbDrawable);
        }
        return defSwitchThumbDrawable;
    }

    public static Drawable getDesireSwitchTrackDrawable(Context context, Drawable defSwitchTrackDrawable) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getDesireSwitchTrackDrawable(context, defSwitchTrackDrawable);
        }
        return defSwitchTrackDrawable;
    }

    public static boolean isEmuiLite() {
        return Factory.IS_EMUI_LITE;
    }

    public static boolean isEmuiSuperLite() {
        return Factory.IS_EMUI_SUPERLITE;
    }

    public static boolean isEmuiNovaPerformance() {
        return Factory.IS_NOVA_PERF;
    }

    public static boolean isSuperResolutionSupport() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.isSuperResolutionSupport();
        }
        return false;
    }

    public static boolean checkIsInSRWhiteList(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.checkIsInSRWhiteList(context);
        }
        return false;
    }

    public static boolean checkIsInSRBlackList(Object object) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.checkIsInSRBlackList(object);
        }
        return false;
    }

    public static boolean checkIsFullScreen(Context context, int w, int h) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.checkIsFullScreen(context, w, h);
        }
        return false;
    }

    public static boolean checkMatchResolution(Drawable drawable) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.checkMatchResolution(drawable);
        }
        return false;
    }

    public static SRBitmapManager getSRBitmapManager() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getSRBitmapManager();
        }
        return null;
    }

    public static HwAISRImageViewTaskManager getHwAISRImageViewTaskManager(Context context) {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.getHwAISRImageViewTaskManager(context);
        }
        return null;
    }

    public static SRInfo createSRInfo() {
        Factory obj2 = getImplObject();
        if (obj2 != null) {
            return obj2.createSRInfo();
        }
        return null;
    }

    private static Factory getImplObject() {
        if (obj != null) {
            return obj;
        }
        synchronized (mLock) {
            try {
                obj = (Factory) Class.forName("com.huawei.android.hwcontrol.HwWidgetFactoryImpl").newInstance();
            } catch (Exception e) {
                Log.e(TAG, ": reflection exception is " + e);
            }
        }
        if (obj != null) {
            Log.v(TAG, ": successes to get AllImpl object and return....");
            return obj;
        }
        Log.e(TAG, ": failes to get AllImpl object");
        return null;
    }
}
