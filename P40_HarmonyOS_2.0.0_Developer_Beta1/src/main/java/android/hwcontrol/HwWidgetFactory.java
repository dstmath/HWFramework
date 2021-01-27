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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ActionMenuPresenter;
import android.widget.AppSecurityPermissions;
import android.widget.CompoundButton;
import android.widget.DefaultHwWechatOptimizeImpl;
import android.widget.Editor;
import android.widget.HwCompoundEventDetector;
import android.widget.HwCompoundEventDetectorDummy;
import android.widget.HwGenericEventDetector;
import android.widget.HwGenericEventDetectorDummy;
import android.widget.HwImageViewZoom;
import android.widget.HwImageViewZoomDummy;
import android.widget.HwKeyEventDetector;
import android.widget.HwKeyEventDetectorDummy;
import android.widget.HwParallelWorker;
import android.widget.HwParallelWorkerDummy;
import android.widget.HwPlume;
import android.widget.HwPlumeDummy;
import android.widget.HwSmartSlideOptimize;
import android.widget.HwSmartSlideOptimizeImplDummy;
import android.widget.HwSplineOverScrollerDummy;
import android.widget.HwSpringBackHelper;
import android.widget.HwSpringBackHelperDummy;
import android.widget.HwWidgetAppAttrsHelper;
import android.widget.HwWidgetAppAttrsHelperDummy;
import android.widget.HwWidgetColumn;
import android.widget.HwWidgetColumnDummy;
import android.widget.IHwSplineOverScroller;
import android.widget.IHwWechatOptimize;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.OverScroller;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.app.AlertController;
import com.android.internal.app.WindowDecorActionBar;
import com.android.internal.widget.DecorCaptionView;
import com.android.internal.widget.DecorCaptionViewBridge;
import huawei.android.utils.HwRTBlurUtils;

public class HwWidgetFactory {
    public static final int DEFAULT_PRIMARY_COLOR = -197380;
    private static final boolean IS_EMUI3_0 = true;
    private static final Object LOCK = new Object();
    private static final String TAG = "HwWidgetFactory";
    private static Factory sFactory = null;

    public enum DisplayMode {
        Normal,
        Small,
        Medium,
        Large
    }

    public interface Factory {
        public static final boolean IS_EMUI_LITE = SystemProperties.getBoolean(HwRTBlurUtils.EMUI_LITE, false);
        public static final boolean IS_EMUI_SUPERLITE = "SuperLite".equals(SystemProperties.get("ro.build.hw_emui_feature_level", ""));
        public static final boolean IS_NOVA_PERF = SystemProperties.getBoolean("ro.config.hw_nova_performance", false);

        void autoTextSize(TextView textView, Context context, float f);

        Drawable getCompoundButtonDrawable(TypedValue typedValue, Context context, Drawable drawable);

        int getCompoundButtonDrawableRes(Context context, int i);

        HwCompoundEventDetector getCompoundEventDetector(Context context);

        int getControlColor(Resources resources);

        Drawable getDesireCheckMarkDrawable(Context context, TypedArray typedArray, int i);

        Drawable getDesireCompoundButtonDrawable(Context context, CompoundButton compoundButton, TypedArray typedArray, int i);

        Drawable getDesireSwitchThumbDrawable(Context context, Drawable drawable);

        Drawable getDesireSwitchTrackDrawable(Context context, Drawable drawable);

        DisplayMode getDisplayMode(Context context);

        Drawable getEdgeEffectImpl(Resources resources);

        Drawable getFastScrollerThumbDrawable(TypedValue typedValue, Context context, Drawable drawable);

        HwGenericEventDetector getGenericEventDetector(Context context);

        Drawable getGlowEffectImpl(Resources resources);

        WindowDecorActionBar getHuaweiActionBarImpl(Activity activity);

        WindowDecorActionBar getHuaweiActionBarImpl(Dialog dialog);

        int getHuaweiRealThemeImpl(int i);

        DecorCaptionViewBridge getHwDecorCaptionView(LayoutInflater layoutInflater);

        Object getHwFastScroller(AbsListView absListView, int i, Context context);

        HwLoadingDrawable getHwLoadingDrawable(Resources resources, int i);

        HwLoadingDrawable getHwLoadingDrawable(Resources resources, int i, int i2);

        DecorCaptionView getHwMultiWindowCaptionView(LayoutInflater layoutInflater, int i);

        HwParallelWorker getHwParallelWorkerImpl(ListView listView);

        HwPlume getHwPlume(Context context);

        HwRippleForeground getHwRippleForeground(RippleDrawable rippleDrawable, Rect rect, boolean z, boolean z2, int i);

        HwSmartSlideOptimize getHwSmartSlideOptimizeImpl(Context context);

        IHwSplineOverScroller getHwSplineOverScrollerImpl(OverScroller.SplineOverScroller splineOverScroller, Context context);

        HwSpringBackHelper getHwSpringBackHelperImpl();

        IHwWechatOptimize getHwWechatOptimizeImpl();

        HwWidgetAppAttrsHelper getHwWidgetAppAttrsHelperImpl(Context context);

        HwWidgetColumn getHwWidgetColumnImpl(Context context);

        HwImageViewZoom getImageViewZoom(Context context, ImageView imageView);

        int getImmersionResource(Context context, int i, int i2, int i3, boolean z);

        HwKeyEventDetector getKeyEventDetector(Context context);

        int getPrimaryColor(Context context);

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

    public static HwTextView getHwTextView(Context context, TextView view, AttributeSet attrs) {
        Factory implObject = getImplObject();
        if (implObject == null) {
            return null;
        }
        return implObject.newHwTextView(context, view, attrs);
    }

    public static HwToast getHwToast(Context context, Toast toast, AttributeSet attrs) {
        Factory implObject = getImplObject();
        if (implObject == null) {
            return null;
        }
        return implObject.newHwToast(context, toast, attrs);
    }

    public static int getThemeId(Bundle bundle, Resources resources) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getThemeIdImpl(bundle, resources);
        }
        return 0;
    }

    public static int getHuaweiRealTheme(int theme) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHuaweiRealThemeImpl(theme);
        }
        return 0;
    }

    public static WindowDecorActionBar getHuaweiActionBarImpl(Activity activity) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHuaweiActionBarImpl(activity);
        }
        return new WindowDecorActionBar(activity);
    }

    public static WindowDecorActionBar getHuaweiActionBarImpl(Dialog dialog) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHuaweiActionBarImpl(dialog);
        }
        return new WindowDecorActionBar(dialog);
    }

    public static AlertController getHwAlertController(Context context, DialogInterface dialogInterface, Window window) {
        Factory implObject = getImplObject();
        if (implObject == null) {
            return new AlertController(context, dialogInterface, window);
        }
        return implObject.newHwAlertController(context, dialogInterface, window);
    }

    public static View getHwOverflowMenuButton(Context context, ActionMenuPresenter actionMenuPresenter) {
        Factory implObject = getImplObject();
        if (implObject == null) {
            return null;
        }
        return implObject.newHwOverflowMenuButton(context, actionMenuPresenter);
    }

    public static Object getHwFastScroller(AbsListView absListView, int fastScrollStyle, Context context) {
        Factory implObject = getImplObject();
        if (implObject == null) {
            return null;
        }
        return implObject.getHwFastScroller(absListView, fastScrollStyle, context);
    }

    public static IHwSplineOverScroller getHwSplineOverScroller(OverScroller.SplineOverScroller splineOverScroller, Context context) {
        Factory implObject = getImplObject();
        if (implObject == null) {
            return new HwSplineOverScrollerDummy();
        }
        return implObject.getHwSplineOverScrollerImpl(splineOverScroller, context);
    }

    public static IHwWechatOptimize getHwWechatOptimize() {
        Factory implObject = getImplObject();
        return implObject == null ? new DefaultHwWechatOptimizeImpl() : implObject.getHwWechatOptimizeImpl();
    }

    public static HwSmartSlideOptimize getHwSmartSlideOptimize(Context context) {
        Factory implObject = getImplObject();
        if (implObject == null) {
            return new HwSmartSlideOptimizeImplDummy();
        }
        return implObject.getHwSmartSlideOptimizeImpl(context);
    }

    public static HwDialogStub getHwDialogStub(Context context, Window window, Dialog dialog, AttributeSet attrs) {
        Factory implObject = getImplObject();
        if (implObject == null) {
            return null;
        }
        return implObject.newHwDialogStub(context, window, dialog, attrs);
    }

    public static HwParallelWorker getHwParallelWorker(ListView listView) {
        Factory implObject = getImplObject();
        return implObject == null ? new HwParallelWorkerDummy() : implObject.getHwParallelWorkerImpl(listView);
    }

    public static boolean checkIsHwTheme(Context context, AttributeSet attrs) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.initAddtionalStyle(context, attrs);
        }
        return false;
    }

    public static boolean isHwTheme(Context context) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.isHwTheme(context);
        }
        return false;
    }

    public static boolean isHwLightTheme(Context context) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.isHwLightTheme(context);
        }
        return false;
    }

    public static boolean isHwEmphasizeTheme(Context context) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.isHwEmphasizeTheme(context);
        }
        return false;
    }

    public static boolean isHwDarkTheme(Context context) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.isHwDarkTheme(context);
        }
        return false;
    }

    public static Drawable getEdgeEffect(Resources resources) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getEdgeEffectImpl(resources);
        }
        return null;
    }

    public static Drawable getGlowEffect(Resources resources) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getGlowEffectImpl(resources);
        }
        return null;
    }

    public static void setImmersionStyle(Context context, TextView textView, int colorResDark, int colorResLight, int colorfulResLight, boolean isDarkDefault) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            implObject.setImmersionStyle(context, textView, colorResDark, colorResLight, colorfulResLight, isDarkDefault);
        }
    }

    public static int getImmersionResource(Context context, int resLight, int colorfulResLight, int resDark, boolean isDarkDefault) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getImmersionResource(context, resLight, colorfulResLight, resDark, isDarkDefault);
        }
        return 0;
    }

    public static boolean isColorfulEnabled(Context context) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.isColorfulEnabled(context);
        }
        return false;
    }

    public static Drawable getCompoundButtonDrawable(TypedValue typedValue, Context context, Drawable oldDrawable) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getCompoundButtonDrawable(typedValue, context, oldDrawable);
        }
        return oldDrawable;
    }

    public static int getCompoundButtonDrawableRes(Context context, int oldResId) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getCompoundButtonDrawableRes(context, oldResId);
        }
        return oldResId;
    }

    public static Drawable getTrackDrawable(TypedValue typedValue, Context context, Drawable oldDrawable) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getTrackDrawable(typedValue, context, oldDrawable);
        }
        return oldDrawable;
    }

    public static Drawable getFastScrollerThumbDrawable(TypedValue typedValue, Context context, Drawable oldDrawable) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getFastScrollerThumbDrawable(typedValue, context, oldDrawable);
        }
        return oldDrawable;
    }

    public static void setTextColorful(View child, Context context, boolean isHwTheme) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            implObject.setTextColorful(child, context, isHwTheme);
        }
    }

    public static void setProgressDrawableTiled(ProgressBar progressBar, TypedValue typedValue, Context context) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            implObject.setProgressDrawableTiled(progressBar, typedValue, context);
        }
    }

    public static void setIndeterminateDrawableTiled(ProgressBar progressBar, TypedValue typedValue, Context context) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            implObject.setIndeterminateDrawableTiled(progressBar, typedValue, context);
        }
    }

    public static int getControlColor(Resources resources) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getControlColor(resources);
        }
        return 0;
    }

    public static int getPrimaryColor(Context context) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getPrimaryColor(context);
        }
        return DEFAULT_PRIMARY_COLOR;
    }

    public static int getSuggestionForgroundColorStyle(int colorBackground) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getSuggestionForgroundColorStyle(colorBackground);
        }
        return 0;
    }

    public static int getSuggestionForgroundColorStyle(Context context) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getSuggestionForgroundColorStyle(context);
        }
        return 0;
    }

    public static boolean isBlackActionBar(Context context) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.isBlackActionBar(context);
        }
        return false;
    }

    public static DisplayMode getDisplayMode(Context context) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getDisplayMode(context);
        }
        return DisplayMode.Normal;
    }

    public static HwRippleForeground getHwRippleForeground(RippleDrawable rippleDrawable, Rect rectBounds, boolean isBounded, boolean isForceSoftware, int type) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwRippleForeground(rippleDrawable, rectBounds, isBounded, isForceSoftware, type);
        }
        return null;
    }

    public static HwLoadingDrawable getHwLoadingDrawable(Resources resources, int size) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwLoadingDrawable(resources, size);
        }
        return null;
    }

    public static HwLoadingDrawable getHwLoadingDrawable(Resources resources, int size, int color) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwLoadingDrawable(resources, size, color);
        }
        return null;
    }

    public static DecorCaptionViewBridge getHwDecorCaptionView(LayoutInflater inflater) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwDecorCaptionView(inflater);
        }
        return null;
    }

    public static void autoTextSize(TextView textview, Context context, float originTextSize) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            implObject.autoTextSize(textview, context, originTextSize);
        }
    }

    public static void reportSrBigData(int eventId, int keyEventId, Object reportMsg) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            implObject.reportSrBigData(eventId, keyEventId, reportMsg);
        }
    }

    public static Drawable getDesireCompoundButtonDrawable(Context context, CompoundButton compoundButton, TypedArray typedArray, int defButtonArrayIndex) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getDesireCompoundButtonDrawable(context, compoundButton, typedArray, defButtonArrayIndex);
        }
        if (typedArray != null) {
            return typedArray.getDrawable(defButtonArrayIndex);
        }
        Log.w(TAG, "typedArray is null!");
        return null;
    }

    public static Drawable getDesireCheckMarkDrawable(Context context, TypedArray typedArray, int defCheckMarkTypedArrayIndex) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getDesireCheckMarkDrawable(context, typedArray, defCheckMarkTypedArrayIndex);
        }
        if (typedArray != null) {
            return typedArray.getDrawable(defCheckMarkTypedArrayIndex);
        }
        Log.w(TAG, "typedArray is null!");
        return null;
    }

    public static Drawable getDesireSwitchThumbDrawable(Context context, Drawable defSwitchThumbDrawable) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getDesireSwitchThumbDrawable(context, defSwitchThumbDrawable);
        }
        return defSwitchThumbDrawable;
    }

    public static Drawable getDesireSwitchTrackDrawable(Context context, Drawable defSwitchTrackDrawable) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getDesireSwitchTrackDrawable(context, defSwitchTrackDrawable);
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

    public static DecorCaptionView getHwMultiWindowCaptionView(LayoutInflater inflater, int windowMode) {
        Factory implObject = getImplObject();
        if (implObject != null) {
            return implObject.getHwMultiWindowCaptionView(inflater, windowMode);
        }
        return null;
    }

    public static HwSpringBackHelper getHwSpringBackHelper() {
        Factory implObject = getImplObject();
        return implObject == null ? new HwSpringBackHelperDummy() : implObject.getHwSpringBackHelperImpl();
    }

    private static Factory getImplObject() {
        synchronized (LOCK) {
            if (sFactory != null) {
                return sFactory;
            }
            try {
                Object object = Class.forName("com.huawei.android.hwcontrol.HwWidgetFactoryImpl").newInstance();
                if (object instanceof Factory) {
                    sFactory = (Factory) object;
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                Log.e(TAG, ": reflection exception");
            }
            return sFactory;
        }
    }

    public static HwWidgetColumn getHwWidgetColumn(Context context) {
        Factory factory = getImplObject();
        return factory == null ? new HwWidgetColumnDummy() : factory.getHwWidgetColumnImpl(context);
    }

    public static HwWidgetAppAttrsHelper getHwWidgetAppAttrsHelper(Context context) {
        Factory factory = getImplObject();
        return factory == null ? new HwWidgetAppAttrsHelperDummy() : factory.getHwWidgetAppAttrsHelperImpl(context);
    }

    public static HwKeyEventDetector getKeyEventDetector(Context context) {
        Factory implObject = getImplObject();
        return implObject == null ? new HwKeyEventDetectorDummy(context) : implObject.getKeyEventDetector(context);
    }

    public static HwGenericEventDetector getGenericEventDetector(Context context) {
        Factory implObject = getImplObject();
        if (implObject == null) {
            return new HwGenericEventDetectorDummy(context);
        }
        return implObject.getGenericEventDetector(context);
    }

    public static HwCompoundEventDetector getCompoundEventDetector(Context context) {
        Factory implObject = getImplObject();
        if (implObject == null) {
            return new HwCompoundEventDetectorDummy(context);
        }
        return implObject.getCompoundEventDetector(context);
    }

    public static HwImageViewZoom getImageViewZoom(Context context, ImageView imageView) {
        Factory factory = getImplObject();
        return factory == null ? new HwImageViewZoomDummy() : factory.getImageViewZoom(context, imageView);
    }

    public static HwPlume getHwPlume(Context context) {
        Factory factory = getImplObject();
        return factory == null ? new HwPlumeDummy() : factory.getHwPlume(context);
    }

    public static class PermissionInformation {
        public static AppSecurityPermissions.PermissionItemView getHwPermItemView(boolean isShowMoneyItemLayout, LayoutInflater inflater) {
            int i;
            if (isShowMoneyItemLayout) {
                i = 34013197;
            } else {
                i = 34013196;
            }
            View view = inflater.inflate(i, (ViewGroup) null);
            if (view instanceof AppSecurityPermissions.PermissionItemView) {
                return (AppSecurityPermissions.PermissionItemView) view;
            }
            return null;
        }

        public static void setPositiveButton(AlertDialog.Builder builder, final AlertDialog dialog) {
            builder.setPositiveButton(33685725, new DialogInterface.OnClickListener() {
                /* class android.hwcontrol.HwWidgetFactory.PermissionInformation.AnonymousClass1 */

                @Override // android.content.DialogInterface.OnClickListener
                public void onClick(DialogInterface dialoginterface, int which) {
                    AlertDialog alertDialog = AlertDialog.this;
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                    }
                }
            });
        }

        public static View getPermissionImageView(AppSecurityPermissions.PermissionItemView permissionItemView) {
            return permissionItemView.findViewById(34603081);
        }

        public static View getPermissionTextView(AppSecurityPermissions.PermissionItemView permissionItemView) {
            return permissionItemView.findViewById(34603082);
        }

        public static View getPermissionItemViewOld(LayoutInflater inflater) {
            return inflater.inflate(34013198, (ViewGroup) null);
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
}
