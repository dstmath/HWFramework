package com.android.internal.view.menu;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ActionMenuView.ActionMenuChildView;
import android.widget.ForwardingListener;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.R;
import com.android.internal.os.HwBootFail;
import com.android.internal.view.menu.MenuBuilder.ItemInvoker;
import com.android.internal.view.menu.MenuView.ItemView;

public class ActionMenuItemView extends TextView implements ItemView, OnClickListener, OnLongClickListener, ActionMenuChildView {
    private static final int MAX_ICON_SIZE = 32;
    private static final String TAG = "ActionMenuItemView";
    private boolean mAllowTextWithIcon;
    private boolean mExpandedFormat;
    private ForwardingListener mForwardingListener;
    private Drawable mIcon;
    private MenuItemImpl mItemData;
    private ItemInvoker mItemInvoker;
    private int mMaxIconSize;
    private int mMinWidth;
    private PopupCallback mPopupCallback;
    private int mSavedPaddingLeft;
    private CharSequence mTitle;
    private boolean mToolbarAttachOverlay;

    public static abstract class PopupCallback {
        public abstract ShowableListMenu getPopup();
    }

    private class ActionMenuItemForwardingListener extends ForwardingListener {
        public ActionMenuItemForwardingListener() {
            super(ActionMenuItemView.this);
        }

        public ShowableListMenu getPopup() {
            if (ActionMenuItemView.this.mPopupCallback != null) {
                return ActionMenuItemView.this.mPopupCallback.getPopup();
            }
            return null;
        }

        protected boolean onForwardingStarted() {
            boolean z = false;
            if (ActionMenuItemView.this.mItemInvoker == null || !ActionMenuItemView.this.mItemInvoker.invokeItem(ActionMenuItemView.this.mItemData)) {
                return false;
            }
            ShowableListMenu popup = getPopup();
            if (popup != null) {
                z = popup.isShowing();
            }
            return z;
        }
    }

    public ActionMenuItemView(Context context) {
        this(context, null);
    }

    public ActionMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActionMenuItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ActionMenuItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mToolbarAttachOverlay = false;
        Resources res = context.getResources();
        this.mAllowTextWithIcon = shouldAllowTextWithIcon();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ActionMenuItemView, defStyleAttr, defStyleRes);
        this.mMinWidth = a.getDimensionPixelSize(0, 0);
        a.recycle();
        this.mMaxIconSize = (int) ((32.0f * res.getDisplayMetrics().density) + 0.5f);
        setOnClickListener(this);
        setOnLongClickListener(this);
        this.mSavedPaddingLeft = -1;
        setSaveEnabled(false);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mAllowTextWithIcon = shouldAllowTextWithIcon();
        updateTextButtonVisibility();
    }

    private boolean shouldAllowTextWithIcon() {
        Configuration configuration = getContext().getResources().getConfiguration();
        int width = configuration.screenWidthDp;
        int height = configuration.screenHeightDp;
        if (width >= HwBootFail.PHASE_LOCK_SETTINGS_READY) {
            return true;
        }
        if ((width < DisplayMetrics.DENSITY_XXXHIGH || height < HwBootFail.PHASE_LOCK_SETTINGS_READY) && configuration.orientation != 2) {
            return false;
        }
        return true;
    }

    public void setPadding(int l, int t, int r, int b) {
        this.mSavedPaddingLeft = l;
        super.setPadding(l, t, r, b);
    }

    public MenuItemImpl getItemData() {
        return this.mItemData;
    }

    public void initialize(MenuItemImpl itemData, int menuType) {
        this.mItemData = itemData;
        setIcon(itemData.getIcon());
        setTitle(itemData.getTitleForItemView(this));
        setId(itemData.getItemId());
        setVisibility(itemData.isVisible() ? 0 : 8);
        setEnabled(itemData.isEnabled());
        if (itemData.hasSubMenu() && this.mForwardingListener == null) {
            this.mForwardingListener = new ActionMenuItemForwardingListener();
        }
    }

    public boolean onTouchEvent(MotionEvent e) {
        if (this.mItemData.hasSubMenu() && this.mForwardingListener != null && this.mForwardingListener.onTouch(this, e)) {
            return true;
        }
        return super.onTouchEvent(e);
    }

    public void onClick(View v) {
        if (this.mItemInvoker != null) {
            this.mItemInvoker.invokeItem(this.mItemData);
        }
    }

    public void setItemInvoker(ItemInvoker invoker) {
        this.mItemInvoker = invoker;
    }

    public void setPopupCallback(PopupCallback popupCallback) {
        this.mPopupCallback = popupCallback;
    }

    public boolean prefersCondensedTitle() {
        return true;
    }

    public void setCheckable(boolean checkable) {
    }

    public void setChecked(boolean checked) {
    }

    public void setExpandedFormat(boolean expandedFormat) {
        if (this.mExpandedFormat != expandedFormat) {
            this.mExpandedFormat = expandedFormat;
            if (this.mItemData != null) {
                this.mItemData.actionFormatChanged();
            }
        }
    }

    public void setToolBarAttachOverlay(boolean isAttach) {
        this.mToolbarAttachOverlay = isAttach;
    }

    protected void updateTextButtonVisibility() {
        CharSequence charSequence = null;
        int i = 1;
        boolean visible = !TextUtils.isEmpty(this.mTitle);
        if (this.mIcon != null) {
            if (!this.mItemData.showsTextAsAction()) {
                i = 0;
            } else if (!this.mAllowTextWithIcon) {
                i = this.mExpandedFormat;
            }
        }
        if (visible & i) {
            charSequence = this.mTitle;
        }
        setText(charSequence);
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
        if (icon != null) {
            float scale;
            int width = icon.getIntrinsicWidth();
            int height = icon.getIntrinsicHeight();
            if (width > this.mMaxIconSize) {
                scale = ((float) this.mMaxIconSize) / ((float) width);
                width = this.mMaxIconSize;
                height = (int) (((float) height) * scale);
            }
            if (height > this.mMaxIconSize) {
                scale = ((float) this.mMaxIconSize) / ((float) height);
                height = this.mMaxIconSize;
                width = (int) (((float) width) * scale);
            }
            icon.setBounds(0, 0, width, height);
        }
        setCompoundDrawables(icon, null, null, null);
        updateTextButtonVisibility();
    }

    public boolean hasText() {
        return !TextUtils.isEmpty(getText());
    }

    public void setShortcut(boolean showShortcut, char shortcutKey) {
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
        setContentDescription(this.mTitle);
        updateTextButtonVisibility();
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        onPopulateAccessibilityEvent(event);
        return true;
    }

    public void onPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        super.onPopulateAccessibilityEventInternal(event);
        CharSequence cdesc = getContentDescription();
        if (!TextUtils.isEmpty(cdesc)) {
            event.getText().add(cdesc);
        }
    }

    public boolean dispatchHoverEvent(MotionEvent event) {
        return onHoverEvent(event);
    }

    public boolean showsIcon() {
        return true;
    }

    public boolean needsDividerBefore() {
        return hasText() && this.mItemData.getIcon() == null;
    }

    public boolean needsDividerAfter() {
        return hasText();
    }

    public boolean onLongClick(View v) {
        if (hasText()) {
            return false;
        }
        int[] screenPos = new int[2];
        Rect displayFrame = new Rect();
        getLocationOnScreen(screenPos);
        getWindowVisibleDisplayFrame(displayFrame);
        Context context = getContext();
        int width = getWidth();
        int height = getHeight();
        int midy = screenPos[1] + (height / 2);
        int referenceX = screenPos[0] + (width / 2);
        if (v.getLayoutDirection() == 0) {
            referenceX = context.getResources().getDisplayMetrics().widthPixels - referenceX;
        }
        Toast cheatSheet = Toast.makeText(context, this.mItemData.getTitle(), 0);
        if (midy < displayFrame.height()) {
            cheatSheet.setGravity(8388661, referenceX, (screenPos[1] + height) - displayFrame.top);
        } else {
            cheatSheet.setGravity(81, 0, height);
        }
        cheatSheet.show();
        return true;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int targetWidth;
        boolean textVisible = hasText();
        if (textVisible && this.mSavedPaddingLeft >= 0) {
            super.setPadding(this.mSavedPaddingLeft, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        }
        if (!textVisible && this.mSavedPaddingLeft >= 0) {
            super.setPadding(this.mSavedPaddingLeft, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int oldMeasuredWidth = getMeasuredWidth();
        if (widthMode == RtlSpacingHelper.UNDEFINED) {
            targetWidth = Math.min(widthSize, this.mMinWidth);
        } else {
            targetWidth = this.mMinWidth;
        }
        if (widthMode != EditorInfo.IME_FLAG_NO_ENTER_ACTION && this.mMinWidth > 0 && (oldMeasuredWidth < targetWidth || (!textVisible && forceMeasureForMinWidth()))) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(targetWidth, EditorInfo.IME_FLAG_NO_ENTER_ACTION), heightMeasureSpec);
        }
        if (!textVisible && this.mIcon != null) {
            super.setPadding((getMeasuredWidth() - this.mIcon.getBounds().width()) / 2, getPaddingTop(), getPaddingRight(), getPaddingBottom());
        }
    }

    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(null);
    }

    protected CharSequence getTitle() {
        return this.mTitle;
    }

    protected Drawable getIcon() {
        return this.mIcon;
    }

    protected void setIconDirect(Drawable msetIcon) {
        this.mIcon = msetIcon;
    }

    protected boolean getAllowTextWithIcon() {
        return this.mAllowTextWithIcon;
    }

    protected boolean getExpandedFormat() {
        return this.mExpandedFormat;
    }

    protected boolean getToolBarAttachOverlay() {
        return this.mToolbarAttachOverlay;
    }

    protected int getMaxIconSize() {
        return this.mMaxIconSize;
    }

    protected boolean forceMeasureForMinWidth() {
        return false;
    }
}
