package huawei.android.widget;

import android.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
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
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.ForwardingListener;
import android.widget.ListPopupWindow;
import com.android.internal.view.menu.MenuPopup;
import huawei.android.widget.utils.ReflectUtil;
import huawei.android.widget.utils.ResLoader;

public class HwOverflowMenuButton extends Button implements ActionMenuView.ActionMenuChildView {
    private static final int DICHOTOMY_SIZE = 2;
    private static final int INVALID_ID = -1;
    private static final boolean IS_DEBUG = false;
    private static final String STYLEABLE_TYPE = "styleable";
    private static final String TAG = "HwOverflowMenuButton";
    private ActionMenuPresenter mActionMenuPresenter;
    private final int[] mActionMenuTextColorAttrs = {16843617};
    private boolean mIsSmartColored;
    private ColorStateList mMenuIconColor;
    private ColorStateList mMenuTextColor;
    private ResLoader mResLoader;
    private ColorStateList mTintColor;
    private int mTintRes;
    private ForwardingListener mTouchListener = new ForwardingListener(this) {
        /* class huawei.android.widget.HwOverflowMenuButton.AnonymousClass1 */

        public ListPopupWindow getPopup() {
            MenuPopup popup;
            ActionMenuPresenter.OverflowPopup overflowPopup = HwOverflowMenuButton.this.getOverflowPopup();
            if (overflowPopup == null || (popup = overflowPopup.getPopup()) == null) {
                return null;
            }
            return popup.getMenuPopup();
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

        public boolean onTouch(View view, MotionEvent event) {
            this.mForwarding = this.mForwarding && getPopup() != null;
            return HwOverflowMenuButton.super.onTouch(view, event);
        }
    };

    public HwOverflowMenuButton(Context context, ActionMenuPresenter actionMenuPresenter) {
        super(context, null, 16843510);
        this.mActionMenuPresenter = actionMenuPresenter;
        this.mResLoader = ResLoader.getInstance();
        int[] hwToolbarMenuAttrs = this.mResLoader.getIdentifierArray(context, STYLEABLE_TYPE, "HwToolbarMenu");
        setClickable(true);
        setFocusable(true);
        setVisibility(0);
        setEnabled(true);
        update();
        setBackground(this.mContext.getDrawable(33751847));
        Resources.Theme theme = this.mResLoader.getTheme(context);
        if (theme != null) {
            int menuItemStyleAttrId = this.mResLoader.getIdentifier(context, "attr", "hwToolbarMenuItemStyle");
            TypedArray toolbarTypedArray = theme.obtainStyledAttributes(hwToolbarMenuAttrs);
            TypedArray menuStyleTypedArray = theme.obtainStyledAttributes(null, hwToolbarMenuAttrs, menuItemStyleAttrId, 0);
            int textAppearanceId = toolbarTypedArray.getResourceId(ResLoader.getInstance().getIdentifier(context, STYLEABLE_TYPE, "HwToolbarMenu_hwToolbarMenuTextAppearance"), -1);
            setTextAppearance(context, textAppearanceId);
            TypedArray textTypedArray = theme.obtainStyledAttributes(textAppearanceId, R.styleable.TextAppearance);
            this.mMenuTextColor = textTypedArray.getColorStateList(3);
            this.mMenuIconColor = menuStyleTypedArray.getColorStateList(this.mResLoader.getIdentifier(context, STYLEABLE_TYPE, "HwToolbarMenu_hwToolbarMenuItemColor"));
            menuStyleTypedArray.recycle();
            textTypedArray.recycle();
            toolbarTypedArray.recycle();
        }
        setOnTouchListener(this.mTouchListener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ActionMenuPresenter.OverflowPopup getOverflowPopup() {
        Object object = ReflectUtil.getObject(this.mActionMenuPresenter, "mOverflowPopup", ActionMenuPresenter.class);
        if (object instanceof ActionMenuPresenter.OverflowPopup) {
            return (ActionMenuPresenter.OverflowPopup) object;
        }
        return null;
    }

    private void tintIconDrawable(Drawable icon, boolean isShowText) {
        int resTint;
        ColorStateList colorStateList;
        this.mResLoader.getIdentifier(this.mContext, "color", "hwtoolbar_menu_emui");
        if (!(getParent() instanceof HwToolbarMenuView) || (colorStateList = this.mMenuTextColor) == null) {
            if (!isShowText) {
                resTint = HwWidgetFactory.getImmersionResource(this.mContext, 33882140, 0, 33882141, false);
            } else if (HwWidgetFactory.isHwDarkTheme(this.mContext)) {
                resTint = 33882141;
            } else {
                resTint = 33882140;
            }
            if (HwWidgetFactory.isHwEmphasizeTheme(this.mContext)) {
                resTint = 33882402;
            }
            TypedArray typedArray = this.mContext.getTheme().obtainStyledAttributes(this.mActionMenuTextColorAttrs);
            setTextColor(typedArray.getColorStateList(0));
            typedArray.recycle();
            if (this.mTintRes != resTint) {
                this.mTintRes = resTint;
                this.mTintColor = this.mContext.getColorStateList(resTint);
                icon.setTintList(this.mTintColor);
                return;
            }
            return;
        }
        setTextColor(colorStateList);
        ColorStateList colorStateList2 = this.mMenuIconColor;
        if (colorStateList2 != null) {
            icon.setTintList(colorStateList2);
            this.mTintColor = this.mMenuIconColor;
        }
    }

    private void updateCompoundDrawables(boolean isShowText) {
        Drawable icon = getCompoundIcon();
        if (icon == null) {
            icon = this.mContext.getDrawable(33751081);
        }
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        if (isShowText) {
            setCompoundDrawables(null, icon, null, null);
        } else {
            setCompoundDrawables(icon, null, null, null);
        }
        ColorStateList smartIconColor = getSmartIconColor();
        ColorStateList smartTitleColor = getSmartTitleColor();
        if (smartIconColor == null || smartTitleColor == null) {
            this.mIsSmartColored = false;
            tintIconDrawable(icon, isShowText);
            return;
        }
        icon.setTintList(smartIconColor);
        setTextColor(smartTitleColor);
        this.mIsSmartColored = true;
    }

    private boolean showHwTextWithAction() {
        ViewParent menuView = getParent();
        if (menuView != null) {
            ViewParent container = menuView.getParent();
            if ((container instanceof View) && ((View) container).getId() == 16909435) {
                return true;
            }
        }
        if (this.mActionMenuPresenter.getToolBarAttachOverlay()) {
            return true;
        }
        return false;
    }

    private void update() {
        boolean isShowText = showHwTextWithAction();
        setText(isShowText ? getContext().getResources().getString(33685549) : null);
        updateCompoundDrawables(isShowText);
        setId(34603080);
    }

    public void updateTextAndIcon() {
        update();
    }

    @Override // android.view.View
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

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateTextAndIcon();
        requestLayout();
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onDraw(Canvas canvas) {
        Drawable icon = getCompoundIcon();
        if ((icon instanceof VectorDrawable) && !this.mIsSmartColored) {
            icon.setTintList(this.mTintColor);
        }
        super.onDraw(canvas);
    }

    private Drawable getCompoundIcon() {
        Drawable[] icons = getCompoundDrawables();
        for (Drawable icon : icons) {
            if (icon != null) {
                return icon;
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Drawable icon = getCompoundIcon();
        if (icon != null && !showHwTextWithAction()) {
            setPadding((getMeasuredWidth() - icon.getBounds().width()) / 2, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.TextView, android.view.View
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        super.onLayout(isChanged, left, top, right, bottom);
        updateTextAndIcon();
    }

    @Override // android.view.View
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setCanOpenPopup(true);
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
