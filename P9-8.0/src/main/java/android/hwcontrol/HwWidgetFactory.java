package android.hwcontrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.HwLoadingDrawable;
import android.graphics.drawable.HwRippleForeground;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ActionMenuPresenter;
import android.widget.AppSecurityPermissions.PermissionItemView;
import android.widget.Editor;
import android.widget.HwSplineOverScrollerDummy;
import android.widget.HwWechatOptimizeImplDummy;
import android.widget.IHwSplineOverScroller;
import android.widget.IHwWechatOptimize;
import android.widget.ImageView;
import android.widget.OverScroller.SplineOverScroller;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
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
        void autoTextSize(TextView textView, Context context, float f);

        Drawable getCompoundButtonDrawable(TypedValue typedValue, Context context, Drawable drawable);

        int getCompoundButtonDrawableRes(Context context, int i);

        int getControlColor(Resources resources);

        DisplayMode getDisplayMode(Context context);

        Drawable getEdgeEffectImpl(Resources resources);

        Drawable getFastScrollerThumbDrawable(TypedValue typedValue, Context context, Drawable drawable);

        Drawable getGlowEffectImpl(Resources resources);

        WindowDecorActionBar getHuaweiActionBarImpl(Activity activity);

        WindowDecorActionBar getHuaweiActionBarImpl(Dialog dialog);

        int getHuaweiRealThemeImpl(int i);

        AbsHwDecorCaptionView getHwDecorCaptionView(LayoutInflater layoutInflater);

        Object getHwFastScroller(AbsListView absListView, int i, Context context);

        HwLoadingDrawable getHwLoadingDrawable(Resources resources, int i);

        HwLoadingDrawable getHwLoadingDrawable(Resources resources, int i, int i2);

        HwRippleForeground getHwRippleForeground(RippleDrawable rippleDrawable, Rect rect, boolean z, boolean z2, int i);

        IHwSplineOverScroller getHwSplineOverScrollerImpl(SplineOverScroller splineOverScroller, Context context);

        IHwWechatOptimize getHwWechatOptimizeImpl();

        int getImmersionResource(Context context, int i, int i2, int i3, boolean z);

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

    public static class PermissionInformation {
        public static PermissionItemView getHwPermItemView(boolean showMoneyItemLayout, LayoutInflater inflater) {
            return (PermissionItemView) inflater.inflate(showMoneyItemLayout ? 34013197 : 34013196, null);
        }

        public static void setPositiveButton(Builder builder, final AlertDialog dialog) {
            builder.setPositiveButton(33685725, new OnClickListener() {
                public void onClick(DialogInterface dialoginterface, int i) {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });
        }

        public static View getPermissionImageView(PermissionItemView itemView) {
            return itemView.findViewById(34603081);
        }

        public static View getPermissionTextView(PermissionItemView mItemView) {
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
        Factory obj = getImplObject();
        if (obj == null) {
            return null;
        }
        return obj.newHwTextView(context, view, attrs);
    }

    public static HwToast getHwToast(Context context, Toast view, AttributeSet attrs) {
        Factory obj = getImplObject();
        if (obj == null) {
            return null;
        }
        return obj.newHwToast(context, view, attrs);
    }

    public static int getThemeId(Bundle data, Resources res) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getThemeIdImpl(data, res);
        }
        return 0;
    }

    public static int getHuaweiRealTheme(int theme) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiRealThemeImpl(theme);
        }
        return 0;
    }

    public static WindowDecorActionBar getHuaweiActionBarImpl(Activity activity) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiActionBarImpl(activity);
        }
        return new WindowDecorActionBar(activity);
    }

    public static WindowDecorActionBar getHuaweiActionBarImpl(Dialog dialog) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHuaweiActionBarImpl(dialog);
        }
        return new WindowDecorActionBar(dialog);
    }

    public static AlertController getHwAlertController(Context context, DialogInterface di, Window window) {
        Factory obj = getImplObject();
        return obj == null ? new AlertController(context, di, window) : obj.newHwAlertController(context, di, window);
    }

    public static View getHwOverflowMenuButton(Context context, ActionMenuPresenter actionMenuPresenter) {
        Factory obj = getImplObject();
        if (obj == null) {
            return null;
        }
        return obj.newHwOverflowMenuButton(context, actionMenuPresenter);
    }

    public static Object getHwFastScroller(AbsListView absListView, int fastScrollStyle, Context context) {
        return getImplObject().getHwFastScroller(absListView, fastScrollStyle, context);
    }

    public static IHwSplineOverScroller getHwSplineOverScroller(SplineOverScroller sos, Context context) {
        Factory obj = getImplObject();
        if (obj == null) {
            return new HwSplineOverScrollerDummy();
        }
        return obj.getHwSplineOverScrollerImpl(sos, context);
    }

    public static IHwWechatOptimize getHwWechatOptimize() {
        Factory obj = getImplObject();
        if (obj == null) {
            return new HwWechatOptimizeImplDummy();
        }
        return obj.getHwWechatOptimizeImpl();
    }

    public static HwDialogStub getHwDialogStub(Context context, Window window, Dialog dialog, AttributeSet attrs) {
        Factory obj = getImplObject();
        if (obj == null) {
            return null;
        }
        return obj.newHwDialogStub(context, window, dialog, attrs);
    }

    public static boolean checkIsHwTheme(Context context, AttributeSet attrs) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.initAddtionalStyle(context, attrs);
        }
        return false;
    }

    public static boolean isHwTheme(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.isHwTheme(context);
        }
        return false;
    }

    public static boolean isHwLightTheme(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.isHwLightTheme(context);
        }
        return false;
    }

    public static boolean isHwEmphasizeTheme(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.isHwEmphasizeTheme(context);
        }
        return false;
    }

    public static boolean isHwDarkTheme(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.isHwDarkTheme(context);
        }
        return false;
    }

    public static Drawable getEdgeEffect(Resources res) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getEdgeEffectImpl(res);
        }
        return null;
    }

    public static Drawable getGlowEffect(Resources res) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getGlowEffectImpl(res);
        }
        return null;
    }

    public static void setImmersionStyle(Context context, TextView textView, int colorResDark, int colorResLight, int colorfulResLight, boolean defaultIsDark) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.setImmersionStyle(context, textView, colorResDark, colorResLight, colorfulResLight, defaultIsDark);
        }
    }

    public static int getImmersionResource(Context context, int resLight, int colorfulResLight, int resDark, boolean defaultIsDark) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getImmersionResource(context, resLight, colorfulResLight, resDark, defaultIsDark);
        }
        return 0;
    }

    public static boolean isColorfulEnabled(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.isColorfulEnabled(context);
        }
        return false;
    }

    public static Drawable getCompoundButtonDrawable(TypedValue tv, Context context, Drawable old) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getCompoundButtonDrawable(tv, context, old);
        }
        return old;
    }

    public static int getCompoundButtonDrawableRes(Context context, int oldResId) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getCompoundButtonDrawableRes(context, oldResId);
        }
        return oldResId;
    }

    public static Drawable getTrackDrawable(TypedValue tv, Context context, Drawable old) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getTrackDrawable(tv, context, old);
        }
        return old;
    }

    public static Drawable getFastScrollerThumbDrawable(TypedValue tv, Context context, Drawable old) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getFastScrollerThumbDrawable(tv, context, old);
        }
        return old;
    }

    public static void setTextColorful(View child, Context context, boolean isHwTheme) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.setTextColorful(child, context, isHwTheme);
        }
    }

    public static void setProgressDrawableTiled(ProgressBar progressBar, TypedValue tv, Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.setProgressDrawableTiled(progressBar, tv, context);
        }
    }

    public static void setIndeterminateDrawableTiled(ProgressBar progressBar, TypedValue tv, Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.setIndeterminateDrawableTiled(progressBar, tv, context);
        }
    }

    public static int getControlColor(Resources res) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getControlColor(res);
        }
        return 0;
    }

    public static int getPrimaryColor(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getPrimaryColor(context);
        }
        return DEFAULT_PRIMARY_COLOR;
    }

    public static int getSuggestionForgroundColorStyle(int colorBackground) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getSuggestionForgroundColorStyle(colorBackground);
        }
        return 0;
    }

    public static int getSuggestionForgroundColorStyle(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getSuggestionForgroundColorStyle(context);
        }
        return 0;
    }

    public static boolean isBlackActionBar(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.isBlackActionBar(context);
        }
        return false;
    }

    public static DisplayMode getDisplayMode(Context context) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getDisplayMode(context);
        }
        return DisplayMode.Normal;
    }

    public static HwRippleForeground getHwRippleForeground(RippleDrawable owner, Rect bounds, boolean isBounded, boolean forceSoftware, int type) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwRippleForeground(owner, bounds, isBounded, forceSoftware, type);
        }
        return null;
    }

    public static HwLoadingDrawable getHwLoadingDrawable(Resources res, int size) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwLoadingDrawable(res, size);
        }
        return null;
    }

    public static HwLoadingDrawable getHwLoadingDrawable(Resources res, int size, int color) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwLoadingDrawable(res, size, color);
        }
        return null;
    }

    public static AbsHwDecorCaptionView getHwDecorCaptionView(LayoutInflater inflater) {
        Factory obj = getImplObject();
        if (obj != null) {
            return obj.getHwDecorCaptionView(inflater);
        }
        return null;
    }

    public static void autoTextSize(TextView textview, Context context, float originTextSize) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.autoTextSize(textview, context, originTextSize);
        }
    }

    public static void reportSrBigData(int eventId, int keyEventId, Object reportMsg) {
        Factory obj = getImplObject();
        if (obj != null) {
            obj.reportSrBigData(eventId, keyEventId, reportMsg);
        }
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
