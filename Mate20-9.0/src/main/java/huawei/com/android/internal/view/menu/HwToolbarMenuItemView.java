package huawei.com.android.internal.view.menu;

import android.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import com.android.internal.view.menu.ActionMenuItemView;
import huawei.android.widget.DecouplingUtil.ReflectUtil;
import huawei.android.widget.HwSmartColorListener;
import huawei.android.widget.HwWidgetUtils;
import huawei.android.widget.loader.ResLoader;
import huawei.android.widget.loader.ResLoaderUtil;

public class HwToolbarMenuItemView extends ActionMenuItemView {
    private static final String TAG = "HwToolbarMenuItemView";
    private boolean mExpandedFormat;
    private Drawable mIcon;
    private int mIconSize;
    private boolean mIsSmartColored;
    private ColorStateList mMenuItemIconColor;
    private ColorStateList mMenuItemTextColor;
    private ResLoader mResLoader;
    private CharSequence mTitle;

    public HwToolbarMenuItemView(Context context) {
        this(context, null);
    }

    public HwToolbarMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwToolbarMenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.i(TAG, TAG);
        this.mResLoader = ResLoader.getInstance();
        int[] hwToolbarMenuStyleable = this.mResLoader.getIdentifierArray(context, ResLoaderUtil.STAYLEABLE, "HwToolbarMenu");
        int menuAppearanceStyleable = this.mResLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "HwToolbarMenu_hwToolbarMenuTextAppearance");
        Resources.Theme theme = this.mResLoader.getTheme(context);
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(hwToolbarMenuStyleable);
            int textAppearanceId = a.getResourceId(menuAppearanceStyleable, -1);
            TypedArray menuStyleTa = theme.obtainStyledAttributes(attrs, hwToolbarMenuStyleable, this.mResLoader.getIdentifier(context, "attr", "hwToolbarMenuItemStyle"), 0);
            if (textAppearanceId != -1) {
                TypedArray ta = theme.obtainStyledAttributes(textAppearanceId, R.styleable.TextAppearance);
                this.mMenuItemTextColor = ta.getColorStateList(3);
                this.mMenuItemIconColor = menuStyleTa.getColorStateList(this.mResLoader.getIdentifier(context, ResLoaderUtil.STAYLEABLE, "HwToolbarMenu_hwToolbarMenuItemColor"));
                menuStyleTa.recycle();
                ta.recycle();
            }
            a.recycle();
        }
        this.mIconSize = this.mResLoader.getResources(context).getDimensionPixelSize(this.mResLoader.getIdentifier(context, ResLoaderUtil.DIMEN, "hwtoolbar_split_menuitem_iconsize"));
        setBackground(HwWidgetUtils.getHwAnimatedGradientDrawable(context, defStyle));
    }

    public void updateTextButtonVisibility() {
        HwToolbarMenuItemView.super.updateTextButtonVisibility();
        if (getParent() != null) {
            boolean z = true;
            boolean visible = !TextUtils.isEmpty(this.mTitle);
            if (this.mIcon != null && (!getItemData().showsTextAsAction() || (!getAllowTextWithIcon1() && !this.mExpandedFormat))) {
                z = false;
            }
            setText((visible & z) | showHwTextWithAction() ? this.mTitle : null);
        }
    }

    @Deprecated
    public void setToolBarAttachOverlay(boolean isAttach) {
    }

    public void setTitle(CharSequence title) {
        HwToolbarMenuItemView.super.setTitle(title);
        this.mTitle = title;
    }

    private boolean showHwTextWithAction() {
        ViewParent menuView = getParent();
        if (menuView != null) {
            ViewParent container = menuView.getParent();
            if (container != null && ((View) container).getId() == 16909362) {
                return true;
            }
        }
        return false;
    }

    public void setIcon(Drawable icon) {
        setIconDirect1(icon);
        if (!(icon == null || this.mIconSize == 0)) {
            icon.setBounds(0, 0, this.mIconSize, this.mIconSize);
        }
        updateTextAndIcon();
    }

    public void setExpandedFormat(boolean expandedFormat) {
        if (this.mExpandedFormat != expandedFormat) {
            this.mExpandedFormat = expandedFormat;
        }
    }

    public void updateTextAndIcon() {
        if (getParent() != null) {
            updateTextButtonVisibility();
            Drawable icon = this.mIcon;
            if (icon != null) {
                icon.setLayoutDirection(getLayoutDirection());
            }
            if (hasText()) {
                setCompoundDrawables(null, icon, null, null);
            } else {
                setCompoundDrawables(icon, null, null, null);
            }
            ColorStateList smartIconColor = getSmartIconColor();
            ColorStateList smartTitleColor = getSmartTitleColor();
            if (smartIconColor == null || smartTitleColor == null) {
                this.mIsSmartColored = false;
                if (this.mMenuItemIconColor != null) {
                    if (icon != null) {
                        icon.setTintList(this.mMenuItemIconColor);
                    }
                    setTextColor(this.mMenuItemTextColor);
                }
                return;
            }
            if (icon != null) {
                icon.setTintList(smartIconColor);
            }
            setTextColor(smartTitleColor);
            this.mIsSmartColored = true;
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        HwToolbarMenuItemView.super.onAttachedToWindow();
        updateTextAndIcon();
        requestLayout();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        HwToolbarMenuItemView.super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            updateTextAndIcon();
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        Drawable icon = this.mIcon;
        if (!(this.mMenuItemIconColor == null || icon == null || this.mIsSmartColored)) {
            icon.setTintList(this.mMenuItemIconColor);
        }
        HwToolbarMenuItemView.super.onDraw(canvas);
    }

    private void setIconDirect1(Drawable msetIcon) {
        this.mIcon = msetIcon;
        ReflectUtil.setObject("mIcon", this, msetIcon, ActionMenuItemView.class);
    }

    private boolean getAllowTextWithIcon1() {
        return ((Boolean) ReflectUtil.getObject(this, "mAllowTextWithIcon", ActionMenuItemView.class)).booleanValue();
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
