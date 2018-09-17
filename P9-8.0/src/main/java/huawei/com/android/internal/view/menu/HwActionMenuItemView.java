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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import com.android.internal.view.menu.ActionMenuItemView;

public class HwActionMenuItemView extends ActionMenuItemView {
    private static final int DEFAULT_MENU_ICON_SIZE = -1;
    static final int MENU_ITEM_TOUCHABLE_X_RANGE = 58;
    private static final String TAG = "HwActionMenuItemView";
    private final int[] mActionMenuTextColorAttr;
    private float mDensity;
    private ColorStateList mTintColor;
    private int mTintRes;
    private int mTouchXRange;
    private boolean shouldBeProcessed;

    public HwActionMenuItemView(Context context) {
        this(context, null);
    }

    public HwActionMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwActionMenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mActionMenuTextColorAttr = new int[]{16843617};
        this.mDensity = context.getResources().getDisplayMetrics().density;
        this.mTouchXRange = (int) (this.mDensity * 58.0f);
    }

    public void updateTextButtonVisibility() {
        int i = 1;
        super.updateTextButtonVisibility();
        if (getParent() != null) {
            CharSequence title;
            boolean visible = TextUtils.isEmpty(getTitle()) ^ 1;
            if (getIcon() != null) {
                if (!getItemData().showsTextAsAction()) {
                    i = 0;
                } else if (!getAllowTextWithIcon()) {
                    i = getExpandedFormat();
                }
            }
            if (((visible & i) | showHwTextWithAction()) | getToolBarAttachOverlay()) {
                title = getTitle();
            } else {
                title = null;
            }
            setText(title);
        }
    }

    private boolean showHwTextWithAction() {
        ViewParent menuView = getParent();
        if (menuView != null) {
            ViewParent container = menuView.getParent();
            if (container != null && ((View) container).getId() == 16909314) {
                return true;
            }
        }
        return false;
    }

    public void setIcon(Drawable icon) {
        setIconDirect(icon);
        if (icon != null) {
            float scale;
            int width = icon.getIntrinsicWidth();
            int height = icon.getIntrinsicHeight();
            if (width > getMaxIconSize()) {
                scale = ((float) getMaxIconSize()) / ((float) width);
                width = getMaxIconSize();
                height = (int) (((float) height) * scale);
            }
            if (height > getMaxIconSize()) {
                scale = ((float) getMaxIconSize()) / ((float) height);
                height = getMaxIconSize();
                width = (int) (((float) width) * scale);
            }
            icon.setBounds(0, 0, width, height);
        }
        updateTextAndIcon();
    }

    private void updateTextAndIcon() {
        if (getParent() != null) {
            int resTint;
            updateTextButtonVisibility();
            if (hasText()) {
                if (HwWidgetFactory.isHwDarkTheme(this.mContext)) {
                    resTint = 33882141;
                } else {
                    resTint = 33882140;
                }
                setCompoundDrawables(null, getIcon(), null, null);
            } else {
                resTint = HwWidgetFactory.getImmersionResource(this.mContext, 33882140, 0, 33882141, false);
                setCompoundDrawables(getIcon(), null, null, null);
            }
            if (HwWidgetFactory.isHwEmphasizeTheme(this.mContext)) {
                resTint = 33882402;
            }
            Drawable icon = getIcon();
            if (!(this.mTintRes == resTint || icon == null || !(icon instanceof VectorDrawable))) {
                this.mTintRes = resTint;
                this.mTintColor = getContext().getColorStateList(resTint);
                icon.setTintList(this.mTintColor);
            }
            if (getItemData().isChecked()) {
                HwWidgetFactory.setImmersionStyle(getContext(), this, 33882410, 33882409, 0, false);
            } else {
                TypedArray ta = getContext().getTheme().obtainStyledAttributes(this.mActionMenuTextColorAttr);
                setTextColor(ta.getColorStateList(0));
                ta.recycle();
            }
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateTextAndIcon();
        requestLayout();
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateTextAndIcon();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == 0 && event.getX() > ((float) (getWidth() - this.mTouchXRange)) / 2.0f && event.getX() < (((float) (getWidth() - this.mTouchXRange)) / 2.0f) + ((float) this.mTouchXRange)) {
            this.shouldBeProcessed = true;
            return super.onTouchEvent(event);
        } else if (!this.shouldBeProcessed) {
            return false;
        } else {
            if (event.getAction() == 1) {
                this.shouldBeProcessed = false;
            }
            return super.onTouchEvent(event);
        }
    }

    protected void onDraw(Canvas canvas) {
        Drawable icon = getIcon();
        if (icon != null && (icon instanceof VectorDrawable)) {
            icon.setTintList(this.mTintColor);
        }
        super.onDraw(canvas);
    }

    protected int getMaxIconSize() {
        int maxIconSize = getContext().getResources().getDimensionPixelSize(34472086);
        return maxIconSize <= 0 ? super.getMaxIconSize() : maxIconSize;
    }

    protected boolean forceMeasureForMinWidth() {
        return true;
    }

    public boolean needsDividerBefore() {
        return false;
    }

    public boolean needsDividerAfter() {
        return false;
    }
}
