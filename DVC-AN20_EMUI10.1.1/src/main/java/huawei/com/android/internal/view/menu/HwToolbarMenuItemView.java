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
import android.view.View;
import android.view.ViewParent;
import com.android.internal.view.menu.ActionMenuItemView;
import com.android.internal.view.menu.MenuItemImpl;
import huawei.android.widget.HwSmartColorListener;
import huawei.android.widget.utils.HwWidgetUtils;
import huawei.android.widget.utils.ReflectUtil;
import huawei.android.widget.utils.ResLoader;
import huawei.android.widget.utils.graphics.drawable.HwAnimatedGradientDrawable;

public class HwToolbarMenuItemView extends ActionMenuItemView {
    private static final int INVALIDE_VALUE = -1;
    private static final String TAG = "HwToolbarMenuItemView";
    private static final String TYPE_STYLEABLE = "styleable";
    private Drawable mIcon;
    private int mIconSize;
    private boolean mIsExpandedFormat;
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
        this.mResLoader = ResLoader.getInstance();
        int[] hwToolbarMenuStyleables = this.mResLoader.getIdentifierArray(context, TYPE_STYLEABLE, "HwToolbarMenu");
        int menuAppearanceStyleable = this.mResLoader.getIdentifier(context, TYPE_STYLEABLE, "HwToolbarMenu_hwToolbarMenuTextAppearance");
        Resources.Theme theme = this.mResLoader.getTheme(context);
        if (theme != null) {
            TypedArray a = theme.obtainStyledAttributes(hwToolbarMenuStyleables);
            int textAppearanceId = a.getResourceId(menuAppearanceStyleable, -1);
            if (textAppearanceId != -1) {
                TypedArray menuStyleTypedArray = theme.obtainStyledAttributes(attrs, hwToolbarMenuStyleables, this.mResLoader.getIdentifier(context, "attr", "hwToolbarMenuItemStyle"), 0);
                TypedArray textTypedArray = theme.obtainStyledAttributes(textAppearanceId, R.styleable.TextAppearance);
                this.mMenuItemTextColor = textTypedArray.getColorStateList(3);
                this.mMenuItemIconColor = menuStyleTypedArray.getColorStateList(this.mResLoader.getIdentifier(context, TYPE_STYLEABLE, "HwToolbarMenu_hwToolbarMenuItemColor"));
                menuStyleTypedArray.recycle();
                textTypedArray.recycle();
            }
            a.recycle();
        }
        Resources res = this.mResLoader.getResources(context);
        this.mIconSize = res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, "dimen", "hwtoolbar_split_menuitem_iconsize"));
        HwAnimatedGradientDrawable bgDrawable = HwWidgetUtils.getHwAnimatedGradientDrawable(context, defStyle);
        bgDrawable.setCornerRadius((float) res.getDimensionPixelSize(this.mResLoader.getIdentifier(context, "dimen", "emui_corner_radius_clicked")));
        setBackground(bgDrawable);
    }

    public void initialize(MenuItemImpl itemData, int menuType) {
        HwToolbarMenuItemView.super.initialize(itemData, menuType);
        setSelected(itemData.isChecked());
    }

    public void updateTextButtonVisibility() {
        HwToolbarMenuItemView.super.updateTextButtonVisibility();
        if (getParent() != null) {
            boolean z = true;
            boolean isVisible = !TextUtils.isEmpty(this.mTitle);
            if (this.mIcon != null && (!getItemData().showsTextAsAction() || (!getAllowTextWithIcon1() && !this.mIsExpandedFormat))) {
                z = false;
            }
            setText((isVisible & z) | showHwTextWithAction() ? this.mTitle : null);
        }
    }

    @Deprecated
    public void setToolBarAttachOverlay(boolean isAttach) {
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        HwToolbarMenuItemView.super.setTitle(title);
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
        int i;
        setIconDirect1(icon);
        if (!(icon == null || (i = this.mIconSize) == 0)) {
            icon.setBounds(0, 0, i, i);
        }
        updateTextAndIcon();
    }

    public void setExpandedFormat(boolean isExpandedFormat) {
        this.mIsExpandedFormat = isExpandedFormat;
    }

    public void setChecked(boolean isChecked) {
        setSelected(isChecked);
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
                ColorStateList colorStateList = this.mMenuItemIconColor;
                if (colorStateList != null) {
                    if (icon != null) {
                        icon.setTintList(colorStateList);
                    }
                    setTextColor(this.mMenuItemTextColor);
                    return;
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
    public void onLayout(boolean isChanged, int left, int top, int right, int bottom) {
        HwToolbarMenuItemView.super.onLayout(isChanged, left, top, right, bottom);
        if (isChanged) {
            updateTextAndIcon();
        }
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        Drawable icon = this.mIcon;
        ColorStateList colorStateList = this.mMenuItemIconColor;
        if (!(colorStateList == null || icon == null || this.mIsSmartColored)) {
            icon.setTintList(colorStateList);
        }
        HwToolbarMenuItemView.super.onDraw(canvas);
    }

    private void setIconDirect1(Drawable icon) {
        this.mIcon = icon;
        ReflectUtil.setObject("mIcon", this, icon, ActionMenuItemView.class);
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
