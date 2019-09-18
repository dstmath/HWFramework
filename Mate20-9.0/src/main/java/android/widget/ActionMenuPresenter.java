package android.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.hwcontrol.HwWidgetFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ActionProvider;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ActionMenuView;
import com.android.internal.view.ActionBarPolicy;
import com.android.internal.view.menu.ActionMenuItemView;
import com.android.internal.view.menu.BaseMenuPresenter;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPopupHelper;
import com.android.internal.view.menu.MenuPresenter;
import com.android.internal.view.menu.MenuView;
import com.android.internal.view.menu.ShowableListMenu;
import com.android.internal.view.menu.SubMenuBuilder;
import java.util.ArrayList;
import java.util.List;

public class ActionMenuPresenter extends BaseMenuPresenter implements ActionProvider.SubUiVisibilityListener {
    private static final boolean ACTIONBAR_ANIMATIONS_ENABLED = false;
    private static final int ITEM_ANIMATION_DURATION = 150;
    private final SparseBooleanArray mActionButtonGroups = new SparseBooleanArray();
    /* access modifiers changed from: private */
    public ActionButtonSubmenu mActionButtonPopup;
    private int mActionItemWidthLimit;
    private View.OnAttachStateChangeListener mAttachStateChangeListener = new View.OnAttachStateChangeListener() {
        public void onViewAttachedToWindow(View v) {
        }

        public void onViewDetachedFromWindow(View v) {
            ActionMenuPresenter.this.mMenuView.getViewTreeObserver().removeOnPreDrawListener(ActionMenuPresenter.this.mItemAnimationPreDrawListener);
            ActionMenuPresenter.this.mPreLayoutItems.clear();
            ActionMenuPresenter.this.mPostLayoutItems.clear();
        }
    };
    private boolean mExpandedActionViewsExclusive;
    /* access modifiers changed from: private */
    public View mHwOverflowButton;
    /* access modifiers changed from: private */
    public ViewTreeObserver.OnPreDrawListener mItemAnimationPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        public boolean onPreDraw() {
            ActionMenuPresenter.this.computeMenuItemAnimationInfo(false);
            ActionMenuPresenter.this.mMenuView.getViewTreeObserver().removeOnPreDrawListener(this);
            ActionMenuPresenter.this.runItemAnimations();
            return true;
        }
    };
    private int mMaxItems;
    private boolean mMaxItemsSet;
    private int mMinCellSize;
    int mOpenSubMenuId;
    /* access modifiers changed from: private */
    public OverflowPopup mOverflowPopup;
    private Drawable mPendingOverflowIcon;
    private boolean mPendingOverflowIconSet;
    private ActionMenuPopupCallback mPopupCallback;
    protected int mPopupEndLocation;
    final PopupPresenterCallback mPopupPresenterCallback = new PopupPresenterCallback();
    protected int mPopupStartLocation;
    /* access modifiers changed from: private */
    public SparseArray<MenuItemLayoutInfo> mPostLayoutItems = new SparseArray<>();
    /* access modifiers changed from: private */
    public OpenOverflowRunnable mPostedOpenRunnable;
    /* access modifiers changed from: private */
    public SparseArray<MenuItemLayoutInfo> mPreLayoutItems = new SparseArray<>();
    private boolean mReserveOverflow;
    private boolean mReserveOverflowSet;
    /* access modifiers changed from: private */
    public List<ItemAnimationInfo> mRunningItemAnimations = new ArrayList();
    private boolean mStrictWidthLimit;
    private int mWidthLimit;
    private boolean mWidthLimitSet;

    public class ActionButtonSubmenu extends MenuPopupHelper {
        public ActionButtonSubmenu(Context context, SubMenuBuilder subMenu, View anchorView) {
            super(context, subMenu, anchorView, false, 16843844);
            if (!subMenu.getItem().isActionButton()) {
                setAnchorView(ActionMenuPresenter.this.mHwOverflowButton == null ? (View) ActionMenuPresenter.this.mMenuView : ActionMenuPresenter.this.mHwOverflowButton);
            }
            setPresenterCallback(ActionMenuPresenter.this.mPopupPresenterCallback);
        }

        /* access modifiers changed from: protected */
        public void onDismiss() {
            ActionButtonSubmenu unused = ActionMenuPresenter.this.mActionButtonPopup = null;
            ActionMenuPresenter.this.mOpenSubMenuId = 0;
            ActionMenuPresenter.super.onDismiss();
        }
    }

    private class ActionMenuPopupCallback extends ActionMenuItemView.PopupCallback {
        private ActionMenuPopupCallback() {
        }

        public ShowableListMenu getPopup() {
            if (ActionMenuPresenter.this.mActionButtonPopup != null) {
                return ActionMenuPresenter.this.mActionButtonPopup.getPopup();
            }
            return null;
        }
    }

    private static class ItemAnimationInfo {
        static final int FADE_IN = 1;
        static final int FADE_OUT = 2;
        static final int MOVE = 0;
        int animType;
        Animator animator;
        int id;
        MenuItemLayoutInfo menuItemLayoutInfo;

        ItemAnimationInfo(int id2, MenuItemLayoutInfo info, Animator anim, int animType2) {
            this.id = id2;
            this.menuItemLayoutInfo = info;
            this.animator = anim;
            this.animType = animType2;
        }
    }

    private static class MenuItemLayoutInfo {
        int left;
        int top;
        View view;

        MenuItemLayoutInfo(View view2, boolean preLayout) {
            this.left = view2.getLeft();
            this.top = view2.getTop();
            if (preLayout) {
                this.left = (int) (((float) this.left) + view2.getTranslationX());
                this.top = (int) (((float) this.top) + view2.getTranslationY());
            }
            this.view = view2;
        }
    }

    private class OpenOverflowRunnable implements Runnable {
        private OverflowPopup mPopup;

        public OpenOverflowRunnable(OverflowPopup popup) {
            this.mPopup = popup;
        }

        public void run() {
            if (ActionMenuPresenter.this.mMenu != null) {
                ActionMenuPresenter.this.mMenu.changeMenuMode();
            }
            View menuView = ActionMenuPresenter.this.mMenuView;
            if (!(menuView == null || menuView.getWindowToken() == null || !this.mPopup.tryShow())) {
                OverflowPopup unused = ActionMenuPresenter.this.mOverflowPopup = this.mPopup;
            }
            OpenOverflowRunnable unused2 = ActionMenuPresenter.this.mPostedOpenRunnable = null;
        }
    }

    public class OverflowMenuButton extends ImageButton implements ActionMenuView.ActionMenuChildView {
        private final float[] mTempPts = new float[2];

        public OverflowMenuButton(Context context) {
            super(context, null, 16843510);
            setClickable(true);
            setFocusable(true);
            setVisibility(0);
            setEnabled(true);
            setOnTouchListener(new ForwardingListener(this, ActionMenuPresenter.this) {
                public ShowableListMenu getPopup() {
                    if (ActionMenuPresenter.this.mOverflowPopup == null) {
                        return null;
                    }
                    return ActionMenuPresenter.this.mOverflowPopup.getPopup();
                }

                public boolean onForwardingStarted() {
                    ActionMenuPresenter.this.showOverflowMenu();
                    return true;
                }

                public boolean onForwardingStopped() {
                    if (ActionMenuPresenter.this.mPostedOpenRunnable != null) {
                        return false;
                    }
                    ActionMenuPresenter.this.hideOverflowMenu();
                    return true;
                }
            });
        }

        public boolean performClick() {
            if (super.performClick()) {
                return true;
            }
            playSoundEffect(0);
            ActionMenuPresenter.this.showOverflowMenu();
            return true;
        }

        public boolean needsDividerBefore() {
            return false;
        }

        public boolean needsDividerAfter() {
            return false;
        }

        public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfoInternal(info);
            info.setCanOpenPopup(true);
        }

        /* access modifiers changed from: protected */
        public boolean setFrame(int l, int t, int r, int b) {
            boolean changed = super.setFrame(l, t, r, b);
            Drawable d = getDrawable();
            Drawable bg = getBackground();
            if (!(d == null || bg == null)) {
                int width = getWidth();
                int height = getHeight();
                int halfEdge = Math.max(width, height) / 2;
                int offsetX = getPaddingLeft() - getPaddingRight();
                int centerX = (width + offsetX) / 2;
                int centerY = (height + (getPaddingTop() - getPaddingBottom())) / 2;
                bg.setHotspotBounds(centerX - halfEdge, centerY - halfEdge, centerX + halfEdge, centerY + halfEdge);
            }
            return changed;
        }
    }

    public class OverflowPopup extends MenuPopupHelper {
        public OverflowPopup(Context context, MenuBuilder menu, View anchorView, boolean overflowOnly) {
            super(context, menu, anchorView, overflowOnly, 16843844);
            ActionMenuPresenter.this.setPopupGravity(this);
            setPresenterCallback(ActionMenuPresenter.this.mPopupPresenterCallback);
        }

        /* access modifiers changed from: protected */
        public void onDismiss() {
            if (ActionMenuPresenter.this.mMenu != null) {
                ActionMenuPresenter.this.mMenu.close();
            }
            OverflowPopup unused = ActionMenuPresenter.this.mOverflowPopup = null;
            ActionMenuPresenter.super.onDismiss();
        }
    }

    private class PopupPresenterCallback implements MenuPresenter.Callback {
        private PopupPresenterCallback() {
        }

        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            boolean z = false;
            if (subMenu == null) {
                return false;
            }
            ActionMenuPresenter.this.mOpenSubMenuId = ((SubMenuBuilder) subMenu).getItem().getItemId();
            MenuPresenter.Callback cb = ActionMenuPresenter.this.getCallback();
            if (cb != null) {
                z = cb.onOpenSubMenu(subMenu);
            }
            return z;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            if (menu instanceof SubMenuBuilder) {
                menu.getRootMenu().close(false);
            }
            MenuPresenter.Callback cb = ActionMenuPresenter.this.getCallback();
            if (cb != null) {
                cb.onCloseMenu(menu, allMenusAreClosing);
            }
        }
    }

    private static class SavedState implements Parcelable {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public int openSubMenuId;

        SavedState() {
        }

        SavedState(Parcel in) {
            this.openSubMenuId = in.readInt();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.openSubMenuId);
        }
    }

    public ActionMenuPresenter(Context context) {
        super(context, 17367071, 17367070);
    }

    public ActionMenuPresenter(Context context, int menuLayout, int itemLayout) {
        super(context, menuLayout, itemLayout);
    }

    public void initForMenu(Context context, MenuBuilder menu) {
        ActionMenuPresenter.super.initForMenu(context, menu);
        Resources res = context.getResources();
        ActionBarPolicy abp = ActionBarPolicy.get(context);
        if (!this.mReserveOverflowSet) {
            this.mReserveOverflow = abp.showsOverflowMenuButton();
        }
        if (!this.mWidthLimitSet) {
            this.mWidthLimit = abp.getEmbeddedMenuWidthLimit();
        }
        if (!this.mMaxItemsSet) {
            this.mMaxItems = abp.getMaxActionButtons();
            this.mMaxItems = getMaxActionButtons(this.mMaxItems);
        }
        int width = this.mWidthLimit;
        if (this.mReserveOverflow) {
            if (this.mHwOverflowButton == null) {
                this.mHwOverflowButton = HwWidgetFactory.getHwOverflowMenuButton(this.mSystemContext, this);
                if (this.mHwOverflowButton == null) {
                    this.mHwOverflowButton = new OverflowMenuButton(this.mSystemContext);
                }
                if (this.mPendingOverflowIconSet) {
                    if (this.mHwOverflowButton instanceof OverflowMenuButton) {
                        ((OverflowMenuButton) this.mHwOverflowButton).setImageDrawable(this.mPendingOverflowIcon);
                    }
                    this.mPendingOverflowIcon = null;
                    this.mPendingOverflowIconSet = false;
                }
                int spec = View.MeasureSpec.makeMeasureSpec(0, 0);
                this.mHwOverflowButton.measure(spec, spec);
            }
            width -= this.mHwOverflowButton.getMeasuredWidth();
        } else {
            this.mHwOverflowButton = null;
        }
        this.mActionItemWidthLimit = width;
        this.mMinCellSize = (int) (56.0f * res.getDisplayMetrics().density);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (!this.mMaxItemsSet) {
            this.mMaxItems = ActionBarPolicy.get(this.mContext).getMaxActionButtons();
            this.mMaxItems = getMaxActionButtons(this.mMaxItems);
        }
        if (HwWidgetFactory.isHwTheme(this.mSystemContext)) {
            this.mHwOverflowButton = null;
        }
        if (this.mMenu != null) {
            this.mMenu.onItemsChanged(true);
        }
    }

    public void setWidthLimit(int width, boolean strict) {
        this.mWidthLimit = width;
        this.mStrictWidthLimit = strict;
        this.mWidthLimitSet = true;
    }

    public void setReserveOverflow(boolean reserveOverflow) {
        this.mReserveOverflow = reserveOverflow;
        this.mReserveOverflowSet = true;
    }

    public void setItemLimit(int itemCount) {
        this.mMaxItems = itemCount;
        this.mMaxItemsSet = true;
    }

    public void setExpandedActionViewsExclusive(boolean isExclusive) {
        this.mExpandedActionViewsExclusive = isExclusive;
    }

    public void setOverflowIcon(Drawable icon) {
        if (this.mHwOverflowButton == null) {
            this.mPendingOverflowIconSet = true;
            this.mPendingOverflowIcon = icon;
        } else if (this.mHwOverflowButton instanceof OverflowMenuButton) {
            ((OverflowMenuButton) this.mHwOverflowButton).setImageDrawable(icon);
        }
    }

    public Drawable getOverflowIcon() {
        if (this.mHwOverflowButton != null) {
            if (this.mHwOverflowButton instanceof OverflowMenuButton) {
                return ((OverflowMenuButton) this.mHwOverflowButton).getDrawable();
            }
        } else if (this.mPendingOverflowIconSet) {
            return this.mPendingOverflowIcon;
        }
        return null;
    }

    public MenuView getMenuView(ViewGroup root) {
        MenuView oldMenuView = this.mMenuView;
        MenuView result = ActionMenuPresenter.super.getMenuView(root);
        if (oldMenuView != result) {
            result.setPresenter(this);
            if (oldMenuView != null) {
                ((View) oldMenuView).removeOnAttachStateChangeListener(this.mAttachStateChangeListener);
            }
            ((View) result).addOnAttachStateChangeListener(this.mAttachStateChangeListener);
        }
        return result;
    }

    public View getItemView(MenuItemImpl item, View convertView, ViewGroup parent) {
        View actionView = item.getActionView();
        if (actionView == null || item.hasCollapsibleActionView()) {
            actionView = ActionMenuPresenter.super.getItemView(item, convertView, parent);
        }
        actionView.setVisibility(item.isActionViewExpanded() ? 8 : 0);
        ActionMenuView menuParent = (ActionMenuView) parent;
        ViewGroup.LayoutParams lp = actionView.getLayoutParams();
        if (!menuParent.checkLayoutParams(lp)) {
            actionView.setLayoutParams(menuParent.generateLayoutParams(lp));
        }
        return actionView;
    }

    public void bindItemView(MenuItemImpl item, MenuView.ItemView itemView) {
        itemView.initialize(item, 0);
        ActionMenuItemView actionItemView = (ActionMenuItemView) itemView;
        actionItemView.setItemInvoker(this.mMenuView);
        if (this.mPopupCallback == null) {
            this.mPopupCallback = new ActionMenuPopupCallback();
        }
        actionItemView.setPopupCallback(this.mPopupCallback);
    }

    public boolean shouldIncludeItem(int childIndex, MenuItemImpl item) {
        return item.isActionButton();
    }

    @Deprecated
    public boolean getToolBarAttachOverlay() {
        return false;
    }

    /* access modifiers changed from: private */
    public void computeMenuItemAnimationInfo(boolean preLayout) {
        ViewGroup menuView = this.mMenuView;
        int count = menuView.getChildCount();
        SparseArray items = preLayout ? this.mPreLayoutItems : this.mPostLayoutItems;
        for (int i = 0; i < count; i++) {
            View child = menuView.getChildAt(i);
            int id = child.getId();
            if (!(id <= 0 || child.getWidth() == 0 || child.getHeight() == 0)) {
                items.put(id, new MenuItemLayoutInfo(child, preLayout));
            }
        }
    }

    /* access modifiers changed from: private */
    public void runItemAnimations() {
        ObjectAnimator anim;
        for (int i = 0; i < this.mPreLayoutItems.size(); i++) {
            int id = this.mPreLayoutItems.keyAt(i);
            final MenuItemLayoutInfo menuItemLayoutInfoPre = this.mPreLayoutItems.get(id);
            int postLayoutIndex = this.mPostLayoutItems.indexOfKey(id);
            if (postLayoutIndex >= 0) {
                MenuItemLayoutInfo menuItemLayoutInfoPost = this.mPostLayoutItems.valueAt(postLayoutIndex);
                PropertyValuesHolder pvhX = null;
                PropertyValuesHolder pvhY = null;
                if (menuItemLayoutInfoPre.left != menuItemLayoutInfoPost.left) {
                    pvhX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, new float[]{(float) (menuItemLayoutInfoPre.left - menuItemLayoutInfoPost.left), 0.0f});
                }
                if (menuItemLayoutInfoPre.top != menuItemLayoutInfoPost.top) {
                    pvhY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, new float[]{(float) (menuItemLayoutInfoPre.top - menuItemLayoutInfoPost.top), 0.0f});
                }
                if (!(pvhX == null && pvhY == null)) {
                    for (int j = 0; j < this.mRunningItemAnimations.size(); j++) {
                        ItemAnimationInfo oldInfo = this.mRunningItemAnimations.get(j);
                        if (oldInfo.id == id && oldInfo.animType == 0) {
                            oldInfo.animator.cancel();
                        }
                    }
                    if (pvhX == null) {
                        anim = ObjectAnimator.ofPropertyValuesHolder(menuItemLayoutInfoPost.view, new PropertyValuesHolder[]{pvhY});
                    } else if (pvhY != null) {
                        anim = ObjectAnimator.ofPropertyValuesHolder(menuItemLayoutInfoPost.view, new PropertyValuesHolder[]{pvhX, pvhY});
                    } else {
                        anim = ObjectAnimator.ofPropertyValuesHolder(menuItemLayoutInfoPost.view, new PropertyValuesHolder[]{pvhX});
                    }
                    anim.setDuration(150);
                    anim.start();
                    this.mRunningItemAnimations.add(new ItemAnimationInfo(id, menuItemLayoutInfoPost, anim, 0));
                    anim.addListener(new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animation) {
                            for (int j = 0; j < ActionMenuPresenter.this.mRunningItemAnimations.size(); j++) {
                                if (((ItemAnimationInfo) ActionMenuPresenter.this.mRunningItemAnimations.get(j)).animator == animation) {
                                    ActionMenuPresenter.this.mRunningItemAnimations.remove(j);
                                    return;
                                }
                            }
                        }
                    });
                }
                this.mPostLayoutItems.remove(id);
            } else {
                float oldAlpha = 1.0f;
                for (int j2 = 0; j2 < this.mRunningItemAnimations.size(); j2++) {
                    ItemAnimationInfo oldInfo2 = this.mRunningItemAnimations.get(j2);
                    if (oldInfo2.id == id && oldInfo2.animType == 1) {
                        oldAlpha = oldInfo2.menuItemLayoutInfo.view.getAlpha();
                        oldInfo2.animator.cancel();
                    }
                }
                ObjectAnimator anim2 = ObjectAnimator.ofFloat(menuItemLayoutInfoPre.view, View.ALPHA, new float[]{oldAlpha, 0.0f});
                this.mMenuView.getOverlay().add(menuItemLayoutInfoPre.view);
                anim2.setDuration(150);
                anim2.start();
                this.mRunningItemAnimations.add(new ItemAnimationInfo(id, menuItemLayoutInfoPre, anim2, 2));
                anim2.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        int j = 0;
                        while (true) {
                            if (j >= ActionMenuPresenter.this.mRunningItemAnimations.size()) {
                                break;
                            } else if (((ItemAnimationInfo) ActionMenuPresenter.this.mRunningItemAnimations.get(j)).animator == animation) {
                                ActionMenuPresenter.this.mRunningItemAnimations.remove(j);
                                break;
                            } else {
                                j++;
                            }
                        }
                        ActionMenuPresenter.this.mMenuView.getOverlay().remove(menuItemLayoutInfoPre.view);
                    }
                });
            }
        }
        for (int i2 = 0; i2 < this.mPostLayoutItems.size(); i2++) {
            int id2 = this.mPostLayoutItems.keyAt(i2);
            int postLayoutIndex2 = this.mPostLayoutItems.indexOfKey(id2);
            if (postLayoutIndex2 >= 0) {
                MenuItemLayoutInfo menuItemLayoutInfo = this.mPostLayoutItems.valueAt(postLayoutIndex2);
                float oldAlpha2 = 0.0f;
                for (int j3 = 0; j3 < this.mRunningItemAnimations.size(); j3++) {
                    ItemAnimationInfo oldInfo3 = this.mRunningItemAnimations.get(j3);
                    if (oldInfo3.id == id2 && oldInfo3.animType == 2) {
                        oldAlpha2 = oldInfo3.menuItemLayoutInfo.view.getAlpha();
                        oldInfo3.animator.cancel();
                    }
                }
                ObjectAnimator anim3 = ObjectAnimator.ofFloat(menuItemLayoutInfo.view, View.ALPHA, new float[]{oldAlpha2, 1.0f});
                anim3.start();
                anim3.setDuration(150);
                this.mRunningItemAnimations.add(new ItemAnimationInfo(id2, menuItemLayoutInfo, anim3, 1));
                anim3.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animation) {
                        for (int j = 0; j < ActionMenuPresenter.this.mRunningItemAnimations.size(); j++) {
                            if (((ItemAnimationInfo) ActionMenuPresenter.this.mRunningItemAnimations.get(j)).animator == animation) {
                                ActionMenuPresenter.this.mRunningItemAnimations.remove(j);
                                return;
                            }
                        }
                    }
                });
            }
        }
        this.mPreLayoutItems.clear();
        this.mPostLayoutItems.clear();
    }

    private void setupItemAnimations() {
        computeMenuItemAnimationInfo(true);
        this.mMenuView.getViewTreeObserver().addOnPreDrawListener(this.mItemAnimationPreDrawListener);
    }

    public void updateMenuView(boolean cleared) {
        this.mMenuView.getParent();
        boolean z = false;
        if (this.mActionButtonPopup == null || !this.mActionButtonPopup.isShowing()) {
            this.mIsReuse = false;
        } else {
            this.mIsReuse = true;
        }
        ActionMenuPresenter.super.updateMenuView(cleared);
        this.mMenuView.requestLayout();
        if (this.mMenu != null) {
            ArrayList<MenuItemImpl> actionItems = this.mMenu.getActionItems();
            int count = actionItems.size();
            for (int i = 0; i < count; i++) {
                ActionProvider provider = actionItems.get(i).getActionProvider();
                if (provider != null) {
                    provider.setSubUiVisibilityListener(this);
                }
            }
        }
        ArrayList<MenuItemImpl> nonActionItems = this.mMenu != null ? this.mMenu.getNonActionItems() : null;
        boolean hasOverflow = false;
        if (this.mReserveOverflow && nonActionItems != null) {
            int count2 = nonActionItems.size();
            if (count2 == 1) {
                hasOverflow = !nonActionItems.get(0).isActionViewExpanded();
            } else {
                if (count2 > 0) {
                    z = true;
                }
                hasOverflow = z;
            }
        }
        if (hasOverflow) {
            if (this.mHwOverflowButton == null) {
                this.mHwOverflowButton = HwWidgetFactory.getHwOverflowMenuButton(this.mSystemContext, this);
                if (this.mHwOverflowButton == null) {
                    this.mHwOverflowButton = new OverflowMenuButton(this.mSystemContext);
                }
            }
            ViewGroup parent = (ViewGroup) this.mHwOverflowButton.getParent();
            if (parent != this.mMenuView) {
                if (parent != null) {
                    parent.removeView(this.mHwOverflowButton);
                }
                ActionMenuView menuView = this.mMenuView;
                menuView.addView(this.mHwOverflowButton, (ViewGroup.LayoutParams) menuView.generateOverflowButtonLayoutParams());
            }
        } else if (this.mHwOverflowButton != null && this.mHwOverflowButton.getParent() == this.mMenuView) {
            this.mMenuView.removeView(this.mHwOverflowButton);
        }
        this.mMenuView.setOverflowReserved(this.mReserveOverflow);
    }

    public boolean filterLeftoverView(ViewGroup parent, int childIndex) {
        if (parent.getChildAt(childIndex) == this.mHwOverflowButton) {
            return false;
        }
        return ActionMenuPresenter.super.filterLeftoverView(parent, childIndex);
    }

    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        int i = 0;
        if (!subMenu.hasVisibleItems()) {
            return false;
        }
        SubMenuBuilder topSubMenu = subMenu;
        while (topSubMenu.getParentMenu() != this.mMenu) {
            topSubMenu = topSubMenu.getParentMenu();
        }
        View anchor = findViewForItem(topSubMenu.getItem());
        if (anchor == null) {
            return false;
        }
        this.mOpenSubMenuId = subMenu.getItem().getItemId();
        boolean preserveIconSpacing = false;
        int count = subMenu.size();
        while (true) {
            if (i >= count) {
                break;
            }
            MenuItem childItem = subMenu.getItem(i);
            if (childItem.isVisible() && childItem.getIcon() != null) {
                preserveIconSpacing = true;
                break;
            }
            i++;
        }
        this.mActionButtonPopup = new ActionButtonSubmenu(this.mContext, subMenu, anchor);
        setMenuPopup(this.mActionButtonPopup, this.mPopupStartLocation, this.mPopupEndLocation);
        this.mActionButtonPopup.setForceShowIcon(preserveIconSpacing);
        this.mActionButtonPopup.show();
        ActionMenuPresenter.super.onSubMenuSelected(subMenu);
        return true;
    }

    private View findViewForItem(MenuItem item) {
        ViewGroup parent = this.mMenuView;
        if (parent == null) {
            return null;
        }
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            MenuView.ItemView childAt = parent.getChildAt(i);
            if ((childAt instanceof MenuView.ItemView) && childAt.getItemData() == item) {
                return childAt;
            }
        }
        return null;
    }

    public boolean showOverflowMenu() {
        if (!this.mReserveOverflow || isOverflowMenuShowing() || this.mMenu == null || this.mMenuView == null || this.mPostedOpenRunnable != null || this.mMenu.getNonActionItems().isEmpty() || this.mHwOverflowButton == null) {
            return false;
        }
        OverflowPopup overflowPopup = new OverflowPopup(this.mContext, this.mMenu, this.mHwOverflowButton, true);
        setMenuPopup(overflowPopup, this.mPopupStartLocation, this.mPopupEndLocation);
        this.mPostedOpenRunnable = new OpenOverflowRunnable(overflowPopup);
        this.mMenuView.post(this.mPostedOpenRunnable);
        ActionMenuPresenter.super.onSubMenuSelected(null);
        return true;
    }

    public boolean hideOverflowMenu() {
        if (this.mPostedOpenRunnable == null || this.mMenuView == null) {
            MenuPopupHelper popup = this.mOverflowPopup;
            if (popup == null) {
                return false;
            }
            popup.dismiss();
            return true;
        }
        this.mMenuView.removeCallbacks(this.mPostedOpenRunnable);
        this.mPostedOpenRunnable = null;
        return true;
    }

    public boolean dismissPopupMenus() {
        return hideOverflowMenu() | hideSubMenus();
    }

    public boolean hideSubMenus() {
        if (this.mActionButtonPopup == null) {
            return false;
        }
        this.mActionButtonPopup.dismiss();
        return true;
    }

    public boolean isOverflowMenuShowing() {
        return this.mOverflowPopup != null && this.mOverflowPopup.isShowing();
    }

    public boolean isOverflowMenuShowPending() {
        return this.mPostedOpenRunnable != null || isOverflowMenuShowing();
    }

    public boolean isOverflowReserved() {
        return this.mReserveOverflow;
    }

    public boolean flagActionItems() {
        int itemsSize;
        ArrayList<MenuItemImpl> visibleItems;
        ArrayList<MenuItemImpl> visibleItems2;
        ViewGroup parent;
        int requiredItems;
        boolean isAction;
        int widthLimit;
        boolean z;
        ActionMenuPresenter actionMenuPresenter = this;
        if (actionMenuPresenter.mMenu != null) {
            visibleItems = actionMenuPresenter.mMenu.getVisibleItems();
            itemsSize = visibleItems.size();
        } else {
            visibleItems = null;
            itemsSize = 0;
        }
        int maxActions = actionMenuPresenter.mMaxItems;
        int widthLimit2 = actionMenuPresenter.mActionItemWidthLimit;
        int querySpec = View.MeasureSpec.makeMeasureSpec(0, 0);
        ViewGroup parent2 = actionMenuPresenter.mMenuView;
        int requiredItems2 = 0;
        int requestedItems = 0;
        int firstActionWidth = 0;
        boolean hasOverflow = false;
        int maxActions2 = maxActions;
        for (int i = 0; i < itemsSize; i++) {
            MenuItemImpl item = visibleItems.get(i);
            if (item.requiresActionButton()) {
                requiredItems2++;
            } else if (item.requestsActionButton()) {
                requestedItems++;
            } else {
                hasOverflow = true;
            }
            if (actionMenuPresenter.mExpandedActionViewsExclusive && item.isActionViewExpanded()) {
                maxActions2 = 0;
            }
        }
        if (actionMenuPresenter.mReserveOverflow != 0 && (hasOverflow || requiredItems2 + requestedItems > maxActions2)) {
            maxActions2--;
        }
        int maxActions3 = maxActions2 - requiredItems2;
        SparseBooleanArray seenGroups = actionMenuPresenter.mActionButtonGroups;
        seenGroups.clear();
        int cellSize = 0;
        int cellsRemaining = 0;
        if (actionMenuPresenter.mStrictWidthLimit) {
            cellsRemaining = widthLimit2 / actionMenuPresenter.mMinCellSize;
            cellSize = actionMenuPresenter.mMinCellSize + ((widthLimit2 % actionMenuPresenter.mMinCellSize) / cellsRemaining);
        }
        int i2 = 0;
        while (i2 < itemsSize) {
            MenuItemImpl item2 = visibleItems.get(i2);
            int itemsSize2 = itemsSize;
            if (item2.requiresActionButton()) {
                View v = actionMenuPresenter.getItemView(item2, null, parent2);
                requiredItems = requiredItems2;
                if (actionMenuPresenter.mStrictWidthLimit != 0) {
                    cellsRemaining -= ActionMenuView.measureChildForCells(v, cellSize, cellsRemaining, querySpec, 0);
                } else {
                    v.measure(querySpec, querySpec);
                }
                int measuredWidth = v.getMeasuredWidth();
                int widthLimit3 = widthLimit2 - measuredWidth;
                if (firstActionWidth == 0) {
                    firstActionWidth = measuredWidth;
                }
                View view = v;
                int groupId = item2.getGroupId();
                if (groupId != 0) {
                    widthLimit = widthLimit3;
                    z = true;
                    seenGroups.put(groupId, true);
                } else {
                    widthLimit = widthLimit3;
                    z = true;
                }
                item2.setIsActionButton(z);
                visibleItems2 = visibleItems;
                parent = parent2;
                widthLimit2 = widthLimit;
            } else {
                requiredItems = requiredItems2;
                if (item2.requestsActionButton() != 0) {
                    int groupId2 = item2.getGroupId();
                    boolean inGroup = seenGroups.get(groupId2);
                    boolean isAction2 = (maxActions3 > 0 || inGroup) && widthLimit2 > 0 && (!actionMenuPresenter.mStrictWidthLimit || cellsRemaining > 0);
                    if (isAction2) {
                        boolean isAction3 = isAction2;
                        View v2 = actionMenuPresenter.getItemView(item2, null, parent2);
                        parent = parent2;
                        if (actionMenuPresenter.mStrictWidthLimit) {
                            int cells = ActionMenuView.measureChildForCells(v2, cellSize, cellsRemaining, querySpec, 0);
                            cellsRemaining -= cells;
                            if (cells == 0) {
                                isAction = false;
                            } else {
                                isAction = isAction3;
                            }
                        } else {
                            v2.measure(querySpec, querySpec);
                            isAction = isAction3;
                        }
                        int measuredWidth2 = v2.getMeasuredWidth();
                        widthLimit2 -= measuredWidth2;
                        if (firstActionWidth == 0) {
                            firstActionWidth = measuredWidth2;
                        }
                        View view2 = v2;
                        if (actionMenuPresenter.mStrictWidthLimit) {
                            isAction2 = (widthLimit2 >= 0) & isAction;
                        } else {
                            isAction2 = (widthLimit2 + firstActionWidth > 0) & isAction;
                        }
                    } else {
                        boolean z2 = isAction2;
                        parent = parent2;
                    }
                    if (!isAction2 || groupId2 == 0) {
                        if (inGroup) {
                            seenGroups.put(groupId2, false);
                            int j = 0;
                            while (j < i2) {
                                MenuItemImpl areYouMyGroupie = visibleItems.get(j);
                                ArrayList<MenuItemImpl> visibleItems3 = visibleItems;
                                if (areYouMyGroupie.getGroupId() == groupId2) {
                                    if (areYouMyGroupie.isActionButton()) {
                                        maxActions3++;
                                    }
                                    areYouMyGroupie.setIsActionButton(false);
                                }
                                j++;
                                visibleItems = visibleItems3;
                            }
                        }
                        visibleItems2 = visibleItems;
                    } else {
                        seenGroups.put(groupId2, true);
                        visibleItems2 = visibleItems;
                    }
                    if (isAction2) {
                        maxActions3--;
                    }
                    item2.setIsActionButton(isAction2);
                } else {
                    visibleItems2 = visibleItems;
                    parent = parent2;
                    item2.setIsActionButton(false);
                    i2++;
                    itemsSize = itemsSize2;
                    requiredItems2 = requiredItems;
                    parent2 = parent;
                    visibleItems = visibleItems2;
                    actionMenuPresenter = this;
                }
            }
            i2++;
            itemsSize = itemsSize2;
            requiredItems2 = requiredItems;
            parent2 = parent;
            visibleItems = visibleItems2;
            actionMenuPresenter = this;
        }
        int i3 = itemsSize;
        ViewGroup viewGroup = parent2;
        int i4 = requiredItems2;
        return true;
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        dismissPopupMenus();
        ActionMenuPresenter.super.onCloseMenu(menu, allMenusAreClosing);
    }

    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState();
        state.openSubMenuId = this.mOpenSubMenuId;
        return state;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState saved = (SavedState) state;
        if (saved.openSubMenuId > 0) {
            MenuItem item = this.mMenu.findItem(saved.openSubMenuId);
            if (item != null) {
                onSubMenuSelected(item.getSubMenu());
            }
        }
    }

    public void onSubUiVisibilityChanged(boolean isVisible) {
        if (isVisible) {
            ActionMenuPresenter.super.onSubMenuSelected(null);
        } else if (this.mMenu != null) {
            this.mMenu.close(false);
        }
    }

    public void setMenuView(ActionMenuView menuView) {
        if (menuView != this.mMenuView) {
            if (this.mMenuView != null) {
                this.mMenuView.removeOnAttachStateChangeListener(this.mAttachStateChangeListener);
            }
            this.mMenuView = menuView;
            menuView.initialize(this.mMenu);
            menuView.addOnAttachStateChangeListener(this.mAttachStateChangeListener);
        }
    }

    public OverflowPopup getOverflowPopup() {
        return this.mOverflowPopup;
    }

    /* access modifiers changed from: protected */
    public void setMenuPopup(MenuPopupHelper menuPopupHelper, int start, int end) {
        if (menuPopupHelper != null) {
            MenuPopupWindow menuPopupWindow = menuPopupHelper.getPopup().getMenuPopup();
            if (menuPopupWindow != null) {
                menuPopupWindow.mPopup.setPopupLocation(start, end);
            }
        }
    }

    public ActionButtonSubmenu getActionButtonPopup() {
        return this.mActionButtonPopup;
    }

    public OpenOverflowRunnable getPostedOpenRunnable() {
        return this.mPostedOpenRunnable;
    }

    public int getOpenSubMenuId() {
        return this.mOpenSubMenuId;
    }

    /* access modifiers changed from: protected */
    public View getOverflowButton() {
        return this.mHwOverflowButton;
    }

    /* access modifiers changed from: protected */
    public void setPopupGravity(MenuPopupHelper mph) {
        mph.setGravity(Gravity.END);
    }

    /* access modifiers changed from: protected */
    public int getMaxActionButtons(int maxItems) {
        return maxItems;
    }

    public void showOverflowMenuPending() {
    }

    public void setPopupLocation(int start, int end) {
    }
}
