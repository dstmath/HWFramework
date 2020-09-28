package huawei.com.android.internal.view.menu;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import com.android.internal.view.menu.ActionMenuItemView;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.HwSmartColorListener;

public class HwActionMenuItemView extends ActionMenuItemView {
    private static final int DEFAULT_MENU_ICON_SIZE = -1;
    private static final float HALF_RATE = 0.5f;
    private static final int MAX_ICON_SIZE = 32;
    static final int MENU_ITEM_TOUCHABLE_X_RANGE = 58;
    private static final String TAG = "HwActionMenuItemView";
    private final int[] mActionMenuTextColorAttrs;
    private Drawable mIcon;
    private boolean mIsExpandedFormat;
    private boolean mIsShouldBeProcessed;
    private boolean mIsSmartColored;
    private boolean mIsToolbarAttachOverlay;
    private int mMaxIconSize;
    private ColorStateList mTintColorList;
    private int mTintRes;
    private CharSequence mTitle;
    private int mTouchRangeX;

    public HwActionMenuItemView(Context context) {
        this(context, null);
    }

    public HwActionMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwActionMenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mActionMenuTextColorAttrs = new int[]{16843617};
        this.mIsToolbarAttachOverlay = false;
        Log.i(TAG, TAG);
        this.mTouchRangeX = (int) (58.0f * context.getResources().getDisplayMetrics().density);
        this.mMaxIconSize = (int) ((32.0f * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void updateTextButtonVisibility() {
        HwActionMenuItemView.super.updateTextButtonVisibility();
        if (getParent() != null) {
            boolean z = true;
            boolean isVisible = (!TextUtils.isEmpty(this.mTitle)) & (this.mIcon == null);
            if (!getItemData().showsTextAsAction() || (!getAllowTextWithIcon1() && !this.mIsExpandedFormat)) {
                z = false;
            }
            setText(((isVisible | z) | showHwTextWithAction()) | this.mIsToolbarAttachOverlay ? this.mTitle : null);
        }
    }

    public void setToolBarAttachOverlay(boolean isAttach) {
        this.mIsToolbarAttachOverlay = isAttach;
    }

    public void setTitle(CharSequence title) {
        HwActionMenuItemView.super.setTitle(title);
        this.mTitle = title;
    }

    private boolean showHwTextWithAction() {
        ViewParent container;
        ViewParent menuView = getParent();
        if (menuView == null || (container = menuView.getParent()) == null || ((View) container).getId() != 16909427) {
            return false;
        }
        return true;
    }

    public void setIcon(Drawable icon) {
        setIconDirect1(icon);
        if (icon != null) {
            int width = icon.getIntrinsicWidth();
            int height = icon.getIntrinsicHeight();
            if (width > getMaxIconSize1()) {
                float scale = ((float) getMaxIconSize1()) / ((float) width);
                width = getMaxIconSize1();
                height = (int) (((float) height) * scale);
            }
            if (height > getMaxIconSize1()) {
                float scale2 = ((float) getMaxIconSize1()) / ((float) height);
                height = getMaxIconSize1();
                width = (int) (((float) width) * scale2);
            }
            icon.setBounds(0, 0, width, height);
        }
        updateTextAndIcon();
    }

    public void setExpandedFormat(boolean isExpandedFormat) {
        if (this.mIsExpandedFormat != isExpandedFormat) {
            this.mIsExpandedFormat = isExpandedFormat;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r11v0, resolved type: huawei.com.android.internal.view.menu.HwActionMenuItemView */
    /* JADX WARN: Multi-variable type inference failed */
    public void updateTextAndIcon() {
        if (getParent() != null) {
            updateTextButtonVisibility();
            Drawable icon = this.mIcon;
            ColorStateList smartIconColorList = getSmartIconColor();
            ColorStateList smartTitleColorList = getSmartTitleColor();
            if (smartIconColorList == null || smartTitleColorList == null) {
                this.mIsSmartColored = false;
                setCompoundDrawables(icon);
                if (getItemData().isChecked()) {
                    HwWidgetFactory.setImmersionStyle(getContext(), this, 33882410, 33882409, 0, false);
                    return;
                }
                TypedArray array = getContext().getTheme().obtainStyledAttributes(this.mActionMenuTextColorAttrs);
                setTextColor(array.getColorStateList(0));
                array.recycle();
                return;
            }
            if (hasText()) {
                setCompoundDrawables(null, icon, null, null);
            } else {
                setCompoundDrawables(icon, null, null, null);
            }
            if (icon != null) {
                icon.setTintList(smartIconColorList);
            }
            setTextColor(smartTitleColorList);
            this.mIsSmartColored = true;
        }
    }

    private void setCompoundDrawables(Drawable icon) {
        int resTint;
        if (hasText()) {
            if (HwWidgetFactory.isHwDarkTheme(this.mContext)) {
                resTint = 33882141;
            } else {
                resTint = 33882140;
            }
            setCompoundDrawables(null, this.mIcon, null, null);
        } else {
            resTint = HwWidgetFactory.getImmersionResource(this.mContext, 33882140, 0, 33882141, false);
            setCompoundDrawables(this.mIcon, null, null, null);
        }
        if (HwWidgetFactory.isHwEmphasizeTheme(this.mContext)) {
            resTint = 33882402;
        }
        if (this.mTintRes != resTint && icon != null && (icon instanceof VectorDrawable)) {
            this.mTintRes = resTint;
            this.mTintColorList = getContext().getColorStateList(resTint);
            icon.setTintList(this.mTintColorList);
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        HwActionMenuItemView.super.onAttachedToWindow();
        updateTextAndIcon();
        requestLayout();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        HwActionMenuItemView.super.onLayout(isChanged, left, top, right, bottom);
        if (isChanged) {
            updateTextAndIcon();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        float rangeX = ((float) (getWidth() - this.mTouchRangeX)) * 0.5f;
        if (event.getAction() == 0 && event.getX() > rangeX && event.getX() < ((float) this.mTouchRangeX) + rangeX) {
            this.mIsShouldBeProcessed = true;
            return HwActionMenuItemView.super.onTouchEvent(event);
        } else if (!this.mIsShouldBeProcessed) {
            return false;
        } else {
            if (event.getAction() == 1) {
                this.mIsShouldBeProcessed = false;
            }
            return HwActionMenuItemView.super.onTouchEvent(event);
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        Drawable icon = this.mIcon;
        if ((icon instanceof VectorDrawable) && !this.mIsSmartColored) {
            icon.setTintList(this.mTintColorList);
        }
        HwActionMenuItemView.super.onDraw(canvas);
    }

    private int getMaxIconSize1() {
        int maxIconSize = getContext().getResources().getDimensionPixelSize(34472086);
        return maxIconSize <= 0 ? this.mMaxIconSize : maxIconSize;
    }

    private void setIconDirect1(Drawable drawable) {
        this.mIcon = drawable;
        ReflectUtil.setObject("mIcon", this, drawable, ActionMenuItemView.class);
    }

    private boolean getAllowTextWithIcon1() {
        Object object = ReflectUtil.getObject(this, "mAllowTextWithIcon", ActionMenuItemView.class);
        if (object instanceof Boolean) {
            return ((Boolean) object).booleanValue();
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean forceMeasureForMinWidth() {
        return true;
    }

    public boolean needsDividerBefore() {
        return false;
    }

    public boolean needsDividerAfter() {
        return false;
    }

    private ColorStateList getSmartIconColor() {
        ViewParent parent = getParent();
        if (parent instanceof HwSmartColorListener) {
            return ((HwSmartColorListener) parent).getSmartIconColor();
        }
        return null;
    }

    private ColorStateList getSmartTitleColor() {
        ViewParent parent = getParent();
        if (parent instanceof HwSmartColorListener) {
            return ((HwSmartColorListener) parent).getSmartTitleColor();
        }
        return null;
    }
}
