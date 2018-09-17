package huawei.android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.hwcontrol.HwWidgetFactory;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ActionMenuPresenter;
import android.widget.ActionMenuView.ActionMenuChildView;
import android.widget.Button;
import android.widget.ForwardingListener;
import android.widget.ListPopupWindow;
import com.android.internal.view.menu.MenuPopup;

public class HwOverflowMenuButton extends Button implements ActionMenuChildView {
    private ActionMenuPresenter mActionMenuPresenter;
    private final int[] mActionMenuTextColorAttr = new int[]{16843617};
    private ColorStateList mTintColor;
    private int mTintRes;

    public HwOverflowMenuButton(Context context, ActionMenuPresenter actionMenuPresenter) {
        super(context, null, 16843510);
        this.mActionMenuPresenter = actionMenuPresenter;
        setClickable(true);
        setFocusable(true);
        setVisibility(0);
        setEnabled(true);
        updateTextAndIcon();
        setOnTouchListener(new ForwardingListener(this) {
            public ListPopupWindow getPopup() {
                if (HwOverflowMenuButton.this.mActionMenuPresenter.getOverflowPopup() == null) {
                    return null;
                }
                MenuPopup mp = HwOverflowMenuButton.this.mActionMenuPresenter.getOverflowPopup().getPopup();
                if (mp != null) {
                    return mp.getMenuPopup();
                }
                return null;
            }

            public boolean onForwardingStarted() {
                HwOverflowMenuButton.this.mActionMenuPresenter.showOverflowMenu();
                return true;
            }

            public boolean onForwardingStopped() {
                if (HwOverflowMenuButton.this.mActionMenuPresenter.getPostedOpenRunnable() != null) {
                    return false;
                }
                HwOverflowMenuButton.this.mActionMenuPresenter.hideOverflowMenu();
                return true;
            }

            public boolean onTouch(View v, MotionEvent event) {
                boolean z = false;
                if (this.mForwarding && getPopup() != null) {
                    z = true;
                }
                this.mForwarding = z;
                return super.onTouch(v, event);
            }
        });
    }

    private void updateCompoundDrawables(boolean showText) {
        int resTint;
        Drawable icon = getCompoundIcon();
        if (icon == null) {
            icon = this.mContext.getDrawable(33751081);
        }
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        if (showText) {
            if (HwWidgetFactory.isHwDarkTheme(this.mContext)) {
                resTint = 33882141;
            } else {
                resTint = 33882140;
            }
            setCompoundDrawables(null, icon, null, null);
        } else {
            resTint = HwWidgetFactory.getImmersionResource(this.mContext, 33882140, 0, 33882141, false);
            setCompoundDrawables(icon, null, null, null);
        }
        if (HwWidgetFactory.isHwEmphasizeTheme(this.mContext)) {
            resTint = 33882402;
        }
        if (this.mTintRes != resTint) {
            this.mTintRes = resTint;
            this.mTintColor = this.mContext.getColorStateList(resTint);
            icon.setTintList(this.mTintColor);
        }
        TypedArray ta = this.mContext.getTheme().obtainStyledAttributes(this.mActionMenuTextColorAttr);
        setTextColor(ta.getColorStateList(0));
        ta.recycle();
    }

    private boolean showHwTextWithAction() {
        ViewParent menuView = getParent();
        if (menuView != null) {
            ViewParent container = menuView.getParent();
            if (container != null && ((View) container).getId() == 16909314) {
                return true;
            }
        }
        if (this.mActionMenuPresenter.getToolBarAttachOverlay()) {
            return true;
        }
        return false;
    }

    private void updateTextAndIcon() {
        boolean showText = showHwTextWithAction();
        String title = getContext().getResources().getString(33685549);
        if (!showText) {
            title = null;
        }
        setText(title);
        updateCompoundDrawables(showText);
        setId(34603080);
    }

    public boolean performClick() {
        if (super.performClick()) {
            return true;
        }
        playSoundEffect(0);
        this.mActionMenuPresenter.showOverflowMenu();
        return true;
    }

    public boolean needsDividerBefore() {
        return false;
    }

    public boolean needsDividerAfter() {
        return false;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateTextAndIcon();
        requestLayout();
    }

    protected void onDraw(Canvas canvas) {
        Drawable icon = getCompoundIcon();
        if (icon != null && (icon instanceof VectorDrawable)) {
            icon.setTintList(this.mTintColor);
        }
        super.onDraw(canvas);
    }

    private Drawable getCompoundIcon() {
        Drawable[] icons = getCompoundDrawables();
        for (int i = 0; i < icons.length; i++) {
            if (icons[i] != null) {
                return icons[i];
            }
        }
        return null;
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        updateTextAndIcon();
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setCanOpenPopup(true);
    }
}
