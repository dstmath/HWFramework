package com.android.internal.view.menu;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.util.AttributeSet;
import android.util.HwPCUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AbsListView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.view.menu.MenuView;

public class ListMenuItemView extends LinearLayout implements MenuView.ItemView, AbsListView.SelectionBoundsAdjuster {
    private static final String TAG = "ListMenuItemView";
    private Drawable mBackground;
    private CheckBox mCheckBox;
    private LinearLayout mContent;
    private boolean mForceShowIcon;
    private ImageView mGroupDivider;
    private boolean mHasListDivider;
    private ImageView mIconView;
    private LayoutInflater mInflater;
    private MenuItemImpl mItemData;
    private int mMenuType;
    private boolean mPreserveIconSpacing;
    private RadioButton mRadioButton;
    private TextView mShortcutView;
    private Drawable mSubMenuArrow;
    private ImageView mSubMenuArrowView;
    private int mTextAppearance;
    private Context mTextAppearanceContext;
    private TextView mTitleView;

    public ListMenuItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MenuView, defStyleAttr, defStyleRes);
        this.mBackground = a.getDrawable(5);
        this.mTextAppearance = a.getResourceId(1, -1);
        this.mPreserveIconSpacing = a.getBoolean(8, false);
        this.mTextAppearanceContext = context;
        this.mSubMenuArrow = a.getDrawable(7);
        TypedArray b = context.getTheme().obtainStyledAttributes(null, new int[]{16843049}, 16842861, 0);
        this.mHasListDivider = b.hasValue(0);
        a.recycle();
        b.recycle();
    }

    public ListMenuItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 16844018);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        setBackgroundDrawable(this.mBackground);
        this.mTitleView = (TextView) findViewById(16908310);
        if (HwWidgetFactory.isHwEmphasizeTheme(this.mContext) && !HwPCUtils.isValidExtDisplayId(this.mContext)) {
            this.mTitleView.setTextColor(this.mContext.getColorStateList(33882415));
        }
        if (this.mTextAppearance != -1) {
            this.mTitleView.setTextAppearance(this.mTextAppearanceContext, this.mTextAppearance);
        }
        this.mShortcutView = (TextView) findViewById(16909321);
        this.mSubMenuArrowView = (ImageView) findViewById(16909385);
        if (this.mSubMenuArrowView != null) {
            this.mSubMenuArrowView.setImageDrawable(this.mSubMenuArrow);
        }
        this.mGroupDivider = (ImageView) findViewById(16908950);
        this.mContent = (LinearLayout) findViewById(16908290);
    }

    public void initialize(MenuItemImpl itemData, int menuType) {
        this.mItemData = itemData;
        this.mMenuType = menuType;
        setVisibility(itemData.isVisible() ? 0 : 8);
        setTitle(itemData.getTitleForItemView(this));
        setCheckable(itemData.isCheckable());
        setShortcut(itemData.shouldShowShortcut(), itemData.getShortcut());
        setIcon(itemData.getIcon());
        setEnabled(itemData.isEnabled());
        setSubMenuArrowVisible(itemData.hasSubMenu());
        setContentDescription(itemData.getContentDescription());
    }

    private void addContentView(View v) {
        addContentView(v, -1);
    }

    private void addContentView(View v, int index) {
        if (this.mContent != null) {
            this.mContent.addView(v, index);
        } else {
            addView(v, index);
        }
    }

    public void setForceShowIcon(boolean forceShow) {
        this.mForceShowIcon = forceShow;
        this.mPreserveIconSpacing = forceShow;
    }

    public void setTitle(CharSequence title) {
        if (title != null) {
            this.mTitleView.setText(title);
            if (this.mTitleView.getVisibility() != 0) {
                this.mTitleView.setVisibility(0);
            }
        } else if (this.mTitleView.getVisibility() != 8) {
            this.mTitleView.setVisibility(8);
        }
    }

    public MenuItemImpl getItemData() {
        return this.mItemData;
    }

    public void setCheckable(boolean checkable) {
        CompoundButton otherCompoundButton;
        CompoundButton compoundButton;
        if (checkable || this.mRadioButton != null || this.mCheckBox != null) {
            if (this.mItemData.isExclusiveCheckable()) {
                if (this.mRadioButton == null) {
                    insertRadioButton();
                }
                compoundButton = this.mRadioButton;
                otherCompoundButton = this.mCheckBox;
            } else {
                if (this.mCheckBox == null) {
                    insertCheckBox();
                }
                compoundButton = this.mCheckBox;
                otherCompoundButton = this.mRadioButton;
            }
            if (checkable) {
                compoundButton.setChecked(this.mItemData.isChecked());
                int newVisibility = checkable ? 0 : 8;
                if (compoundButton.getVisibility() != newVisibility) {
                    compoundButton.setVisibility(newVisibility);
                }
                if (!(otherCompoundButton == null || otherCompoundButton.getVisibility() == 8)) {
                    otherCompoundButton.setVisibility(8);
                }
            } else {
                if (this.mCheckBox != null) {
                    this.mCheckBox.setVisibility(8);
                }
                if (this.mRadioButton != null) {
                    this.mRadioButton.setVisibility(8);
                }
            }
        }
    }

    public void setChecked(boolean checked) {
        CompoundButton compoundButton;
        if (this.mItemData.isExclusiveCheckable()) {
            if (this.mRadioButton == null) {
                insertRadioButton();
            }
            compoundButton = this.mRadioButton;
        } else {
            if (this.mCheckBox == null) {
                insertCheckBox();
            }
            compoundButton = this.mCheckBox;
        }
        compoundButton.setChecked(checked);
    }

    private void setSubMenuArrowVisible(boolean hasSubmenu) {
        if (this.mSubMenuArrowView != null) {
            this.mSubMenuArrowView.setVisibility(hasSubmenu ? 0 : 8);
        }
    }

    public void setShortcut(boolean showShortcut, char shortcutKey) {
        int newVisibility = (!showShortcut || !this.mItemData.shouldShowShortcut()) ? 8 : 0;
        if (newVisibility == 0) {
            this.mShortcutView.setText(this.mItemData.getShortcutLabel());
        }
        if (this.mShortcutView.getVisibility() != newVisibility) {
            this.mShortcutView.setVisibility(newVisibility);
        }
    }

    public void setIcon(Drawable icon) {
        boolean showIcon = this.mItemData.shouldShowIcon() || this.mForceShowIcon;
        if (!showIcon && !this.mPreserveIconSpacing) {
            return;
        }
        if (this.mIconView != null || icon != null || this.mPreserveIconSpacing) {
            if (this.mIconView == null) {
                insertIconView();
            }
            if (icon != null || this.mPreserveIconSpacing) {
                this.mIconView.setImageDrawable(showIcon ? icon : null);
                if (this.mIconView.getVisibility() != 0) {
                    this.mIconView.setVisibility(0);
                }
            } else {
                this.mIconView.setVisibility(8);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mIconView != null && this.mPreserveIconSpacing) {
            ViewGroup.LayoutParams lp = getLayoutParams();
            LinearLayout.LayoutParams iconLp = (LinearLayout.LayoutParams) this.mIconView.getLayoutParams();
            if (lp.height > 0 && iconLp.width <= 0) {
                iconLp.width = lp.height;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void insertIconView() {
        this.mIconView = (ImageView) getInflater().inflate(17367171, this, false);
        addContentView(this.mIconView, 0);
    }

    private void insertRadioButton() {
        this.mRadioButton = (RadioButton) getInflater().inflate(17367173, this, false);
        addContentView(this.mRadioButton);
    }

    private void insertCheckBox() {
        this.mCheckBox = (CheckBox) getInflater().inflate(17367170, this, false);
        addContentView(this.mCheckBox);
    }

    public boolean prefersCondensedTitle() {
        return false;
    }

    public boolean showsIcon() {
        return this.mForceShowIcon;
    }

    private LayoutInflater getInflater() {
        if (this.mInflater == null) {
            this.mInflater = LayoutInflater.from(this.mContext);
        }
        return this.mInflater;
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (this.mItemData != null && this.mItemData.hasSubMenu()) {
            info.setCanOpenPopup(true);
        }
    }

    public void setGroupDividerEnabled(boolean groupDividerEnabled) {
        if (this.mGroupDivider != null) {
            this.mGroupDivider.setVisibility((this.mHasListDivider || !groupDividerEnabled) ? 8 : 0);
        }
    }

    public void adjustListItemSelectionBounds(Rect rect) {
        if (this.mGroupDivider != null && this.mGroupDivider.getVisibility() == 0) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.mGroupDivider.getLayoutParams();
            rect.top += this.mGroupDivider.getHeight() + lp.topMargin + lp.bottomMargin;
        }
    }
}
