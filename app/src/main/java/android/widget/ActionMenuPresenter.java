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
import android.os.Parcelable.Creator;
import android.util.Property;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.ActionProvider;
import android.view.ActionProvider.SubUiVisibilityListener;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnAttachStateChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.PathInterpolator;
import android.widget.ActionMenuView.ActionMenuChildView;
import com.android.internal.R;
import com.android.internal.view.ActionBarPolicy;
import com.android.internal.view.menu.ActionMenuItemView;
import com.android.internal.view.menu.ActionMenuItemView.PopupCallback;
import com.android.internal.view.menu.BaseMenuPresenter;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPopupHelper;
import com.android.internal.view.menu.MenuPresenter.Callback;
import com.android.internal.view.menu.MenuView;
import com.android.internal.view.menu.MenuView.ItemView;
import com.android.internal.view.menu.ShowableListMenu;
import com.android.internal.view.menu.SubMenuBuilder;
import java.util.ArrayList;
import java.util.List;

public class ActionMenuPresenter extends BaseMenuPresenter implements SubUiVisibilityListener {
    private static final int ITEM_ANIMATION_DURATION = 150;
    private static final int ITEM_ANIMATION_FADEIN_DURATION = 350;
    private static final int ITEM_ANIMATION_FADEOUT_DURATION = 350;
    private static final int ITEM_ANIMATION_MOVE_DURATION = 350;
    private final SparseBooleanArray mActionButtonGroups;
    private ActionButtonSubmenu mActionButtonPopup;
    private int mActionItemWidthLimit;
    private boolean mAnimationEnabled;
    private OnAttachStateChangeListener mAttachStateChangeListener;
    private boolean mExpandedActionViewsExclusive;
    private OnPreDrawListener mItemAnimationPreDrawListener;
    private int mMaxItems;
    private boolean mMaxItemsSet;
    private int mMinCellSize;
    int mOpenSubMenuId;
    private View mOverflowButton;
    private OverflowPopup mOverflowPopup;
    private Drawable mPendingOverflowIcon;
    private boolean mPendingOverflowIconSet;
    private ActionMenuPopupCallback mPopupCallback;
    protected int mPopupEndLocation;
    final PopupPresenterCallback mPopupPresenterCallback;
    protected int mPopupStartLocation;
    private SparseArray<MenuItemLayoutInfo> mPostLayoutItems;
    private OpenOverflowRunnable mPostedOpenRunnable;
    private SparseArray<MenuItemLayoutInfo> mPreLayoutItems;
    private boolean mReserveOverflow;
    private boolean mReserveOverflowSet;
    private List<ItemAnimationInfo> mRunningItemAnimations;
    private boolean mStrictWidthLimit;
    private boolean mToolbarAttachOverlay;
    private int mWidthLimit;
    private boolean mWidthLimitSet;

    /* renamed from: android.widget.ActionMenuPresenter.4 */
    class AnonymousClass4 extends AnimatorListenerAdapter {
        final /* synthetic */ ViewGroup val$container;
        final /* synthetic */ int val$id;
        final /* synthetic */ MenuItemLayoutInfo val$menuItemLayoutInfoPre;

        AnonymousClass4(ViewGroup val$container, MenuItemLayoutInfo val$menuItemLayoutInfoPre, int val$id) {
            this.val$container = val$container;
            this.val$menuItemLayoutInfoPre = val$menuItemLayoutInfoPre;
            this.val$id = val$id;
        }

        public void onAnimationStart(Animator animation) {
            int[] location = new int[2];
            this.val$container.getLocationOnScreen(location);
            this.val$menuItemLayoutInfoPre.view.setX((float) (this.val$menuItemLayoutInfoPre.location_x - location[0]));
            this.val$menuItemLayoutInfoPre.view.setY((float) (this.val$menuItemLayoutInfoPre.location_y - location[1]));
        }

        public void onAnimationEnd(Animator animation) {
            for (int j = 0; j < ActionMenuPresenter.this.mRunningItemAnimations.size(); j++) {
                if (((ItemAnimationInfo) ActionMenuPresenter.this.mRunningItemAnimations.get(j)).animator == animation) {
                    ActionMenuPresenter.this.mRunningItemAnimations.remove(j);
                    break;
                }
            }
            this.val$menuItemLayoutInfoPre.view.setTranslationY(0.0f);
            if (this.val$id == -1) {
                ActionMenuPresenter.this.mToolbarAttachOverlay = false;
            } else {
                ((ActionMenuItemView) this.val$menuItemLayoutInfoPre.view).setToolBarAttachOverlay(false);
            }
            if (this.val$container != null) {
                this.val$container.getOverlay().remove(this.val$menuItemLayoutInfoPre.view);
            }
        }
    }

    public class ActionButtonSubmenu extends MenuPopupHelper {
        public ActionButtonSubmenu(Context context, SubMenuBuilder subMenu, View anchorView) {
            super(context, subMenu, anchorView, false, R.attr.actionOverflowMenuStyle);
            if (!((MenuItemImpl) subMenu.getItem()).isActionButton()) {
                setAnchorView(ActionMenuPresenter.this.mOverflowButton == null ? (View) ActionMenuPresenter.this.mMenuView : ActionMenuPresenter.this.mOverflowButton);
            }
            setPresenterCallback(ActionMenuPresenter.this.mPopupPresenterCallback);
        }

        protected void onDismiss() {
            ActionMenuPresenter.this.mActionButtonPopup = null;
            ActionMenuPresenter.this.mOpenSubMenuId = 0;
            super.onDismiss();
        }
    }

    private class ActionMenuPopupCallback extends PopupCallback {
        private ActionMenuPopupCallback() {
        }

        public ShowableListMenu getPopup() {
            return ActionMenuPresenter.this.mActionButtonPopup != null ? ActionMenuPresenter.this.mActionButtonPopup.getPopup() : null;
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

        ItemAnimationInfo(int id, MenuItemLayoutInfo info, Animator anim, int animType) {
            this.id = id;
            this.menuItemLayoutInfo = info;
            this.animator = anim;
            this.animType = animType;
        }
    }

    private static class MenuItemLayoutInfo {
        int left;
        int location_x;
        int location_y;
        int top;
        View view;

        MenuItemLayoutInfo(View view, boolean preLayout) {
            this.left = view.getLeft();
            this.top = view.getTop();
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            this.location_x = location[0];
            this.location_y = location[1];
            if (preLayout) {
                this.left = (int) (((float) this.left) + view.getTranslationX());
                this.top = (int) (((float) this.top) + view.getTranslationY());
            }
            this.view = view;
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
            View menuView = (View) ActionMenuPresenter.this.mMenuView;
            if (!(menuView == null || menuView.getWindowToken() == null || !this.mPopup.tryShow())) {
                ActionMenuPresenter.this.mOverflowPopup = this.mPopup;
            }
            ActionMenuPresenter.this.mPostedOpenRunnable = null;
        }
    }

    public class OverflowMenuButton extends ImageButton implements ActionMenuChildView {
        private final float[] mTempPts;

        /* renamed from: android.widget.ActionMenuPresenter.OverflowMenuButton.1 */
        class AnonymousClass1 extends ForwardingListener {
            AnonymousClass1(View $anonymous0) {
                super($anonymous0);
            }

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
        }

        public OverflowMenuButton(Context context) {
            super(context, null, R.attr.actionOverflowButtonStyle);
            this.mTempPts = new float[2];
            setClickable(true);
            setFocusable(true);
            setVisibility(0);
            setEnabled(true);
            setOnTouchListener(new AnonymousClass1(this));
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

        protected boolean setFrame(int l, int t, int r, int b) {
            boolean changed = super.setFrame(l, t, r, b);
            Drawable d = getDrawable();
            Drawable bg = getBackground();
            if (!(d == null || bg == null)) {
                int width = getWidth();
                int height = getHeight();
                int halfEdge = Math.max(width, height) / 2;
                int centerX = (width + (getPaddingLeft() - getPaddingRight())) / 2;
                int centerY = (height + (getPaddingTop() - getPaddingBottom())) / 2;
                bg.setHotspotBounds(centerX - halfEdge, centerY - halfEdge, centerX + halfEdge, centerY + halfEdge);
            }
            return changed;
        }
    }

    public class OverflowPopup extends MenuPopupHelper {
        public OverflowPopup(Context context, MenuBuilder menu, View anchorView, boolean overflowOnly) {
            super(context, menu, anchorView, overflowOnly, R.attr.actionOverflowMenuStyle);
            ActionMenuPresenter.this.setPopupGravity(this);
            setPresenterCallback(ActionMenuPresenter.this.mPopupPresenterCallback);
        }

        protected void onDismiss() {
            if (ActionMenuPresenter.this.mMenu != null) {
                ActionMenuPresenter.this.mMenu.close();
            }
            ActionMenuPresenter.this.mOverflowPopup = null;
            super.onDismiss();
        }
    }

    private class PopupPresenterCallback implements Callback {
        private PopupPresenterCallback() {
        }

        public boolean onOpenSubMenu(MenuBuilder subMenu) {
            if (subMenu == null) {
                return false;
            }
            ActionMenuPresenter.this.mOpenSubMenuId = ((SubMenuBuilder) subMenu).getItem().getItemId();
            Callback cb = ActionMenuPresenter.this.getCallback();
            return cb != null ? cb.onOpenSubMenu(subMenu) : false;
        }

        public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
            if (menu instanceof SubMenuBuilder) {
                menu.getRootMenu().close(false);
            }
            Callback cb = ActionMenuPresenter.this.getCallback();
            if (cb != null) {
                cb.onCloseMenu(menu, allMenusAreClosing);
            }
        }
    }

    private static class SavedState implements Parcelable {
        public static final Creator<SavedState> CREATOR = null;
        public int openSubMenuId;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.widget.ActionMenuPresenter.SavedState.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.widget.ActionMenuPresenter.SavedState.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.ActionMenuPresenter.SavedState.<clinit>():void");
        }

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
        super(context, R.layout.action_menu_layout, R.layout.action_menu_item_layout);
        this.mAnimationEnabled = false;
        this.mToolbarAttachOverlay = false;
        this.mActionButtonGroups = new SparseBooleanArray();
        this.mPopupPresenterCallback = new PopupPresenterCallback();
        this.mPreLayoutItems = new SparseArray();
        this.mPostLayoutItems = new SparseArray();
        this.mRunningItemAnimations = new ArrayList();
        this.mItemAnimationPreDrawListener = new OnPreDrawListener() {
            public boolean onPreDraw() {
                ActionMenuPresenter.this.computeMenuItemAnimationInfo(false);
                ((View) ActionMenuPresenter.this.mMenuView).getViewTreeObserver().removeOnPreDrawListener(this);
                if (ActionMenuPresenter.this.getIsToolBarMode()) {
                    ActionMenuPresenter.this.runItemAnimations();
                }
                return true;
            }
        };
        this.mAttachStateChangeListener = new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
            }

            public void onViewDetachedFromWindow(View v) {
                ((View) ActionMenuPresenter.this.mMenuView).getViewTreeObserver().removeOnPreDrawListener(ActionMenuPresenter.this.mItemAnimationPreDrawListener);
                ActionMenuPresenter.this.mPreLayoutItems.clear();
                ActionMenuPresenter.this.mPostLayoutItems.clear();
            }
        };
    }

    public ActionMenuPresenter(Context context, int menuLayout, int itemLayout) {
        super(context, menuLayout, itemLayout);
        this.mAnimationEnabled = false;
        this.mToolbarAttachOverlay = false;
        this.mActionButtonGroups = new SparseBooleanArray();
        this.mPopupPresenterCallback = new PopupPresenterCallback();
        this.mPreLayoutItems = new SparseArray();
        this.mPostLayoutItems = new SparseArray();
        this.mRunningItemAnimations = new ArrayList();
        this.mItemAnimationPreDrawListener = new OnPreDrawListener() {
            public boolean onPreDraw() {
                ActionMenuPresenter.this.computeMenuItemAnimationInfo(false);
                ((View) ActionMenuPresenter.this.mMenuView).getViewTreeObserver().removeOnPreDrawListener(this);
                if (ActionMenuPresenter.this.getIsToolBarMode()) {
                    ActionMenuPresenter.this.runItemAnimations();
                }
                return true;
            }
        };
        this.mAttachStateChangeListener = new OnAttachStateChangeListener() {
            public void onViewAttachedToWindow(View v) {
            }

            public void onViewDetachedFromWindow(View v) {
                ((View) ActionMenuPresenter.this.mMenuView).getViewTreeObserver().removeOnPreDrawListener(ActionMenuPresenter.this.mItemAnimationPreDrawListener);
                ActionMenuPresenter.this.mPreLayoutItems.clear();
                ActionMenuPresenter.this.mPostLayoutItems.clear();
            }
        };
    }

    public void initForMenu(Context context, MenuBuilder menu) {
        super.initForMenu(context, menu);
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
            if (this.mOverflowButton == null) {
                this.mOverflowButton = HwWidgetFactory.getHwOverflowMenuButton(this.mSystemContext, this);
                if (this.mOverflowButton == null) {
                    this.mOverflowButton = new OverflowMenuButton(this.mSystemContext);
                }
                if (this.mPendingOverflowIconSet) {
                    if (this.mOverflowButton instanceof OverflowMenuButton) {
                        ((OverflowMenuButton) this.mOverflowButton).setImageDrawable(this.mPendingOverflowIcon);
                    }
                    this.mPendingOverflowIcon = null;
                    this.mPendingOverflowIconSet = false;
                }
                int spec = MeasureSpec.makeMeasureSpec(0, 0);
                this.mOverflowButton.measure(spec, spec);
            }
            width -= this.mOverflowButton.getMeasuredWidth();
        } else {
            this.mOverflowButton = null;
        }
        this.mActionItemWidthLimit = width;
        this.mMinCellSize = (int) (res.getDisplayMetrics().density * 56.0f);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (!this.mMaxItemsSet) {
            this.mMaxItems = ActionBarPolicy.get(this.mContext).getMaxActionButtons();
            this.mMaxItems = getMaxActionButtons(this.mMaxItems);
        }
        this.mOverflowButton = null;
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
        if (this.mOverflowButton == null) {
            this.mPendingOverflowIconSet = true;
            this.mPendingOverflowIcon = icon;
        } else if (this.mOverflowButton instanceof OverflowMenuButton) {
            ((OverflowMenuButton) this.mOverflowButton).setImageDrawable(icon);
        }
    }

    public Drawable getOverflowIcon() {
        if (this.mOverflowButton != null) {
            if (this.mOverflowButton instanceof OverflowMenuButton) {
                return ((OverflowMenuButton) this.mOverflowButton).getDrawable();
            }
        } else if (this.mPendingOverflowIconSet) {
            return this.mPendingOverflowIcon;
        }
        return null;
    }

    public MenuView getMenuView(ViewGroup root) {
        MenuView oldMenuView = this.mMenuView;
        MenuView result = super.getMenuView(root);
        if (oldMenuView != result) {
            ((ActionMenuView) result).setPresenter(this);
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
            actionView = super.getItemView(item, convertView, parent);
        }
        actionView.setVisibility(item.isActionViewExpanded() ? 8 : 0);
        ActionMenuView menuParent = (ActionMenuView) parent;
        LayoutParams lp = actionView.getLayoutParams();
        if (!menuParent.checkLayoutParams(lp)) {
            actionView.setLayoutParams(menuParent.generateLayoutParams(lp));
        }
        return actionView;
    }

    public void bindItemView(MenuItemImpl item, ItemView itemView) {
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

    public boolean getToolBarAttachOverlay() {
        return getIsToolBarMode() ? this.mToolbarAttachOverlay : false;
    }

    private boolean getIsToolBarMode() {
        if (this.mMenuView != null) {
            View parent = (View) ((View) this.mMenuView).getParent();
            if (parent != null && parent.getId() == R.id.split_action_bar) {
                return true;
            }
        }
        return false;
    }

    private View getAnimateOverlayRootView() {
        if (this.mMenuView != null) {
            View parent = (View) ((View) this.mMenuView).getParent();
            if (parent != null && parent.getId() == R.id.split_action_bar) {
                return (View) parent.getParent();
            }
        }
        return null;
    }

    private void computeMenuItemAnimationInfo(boolean preLayout) {
        ViewGroup menuView = this.mMenuView;
        int count = menuView.getChildCount();
        SparseArray items = preLayout ? this.mPreLayoutItems : this.mPostLayoutItems;
        if (preLayout) {
            this.mPreLayoutItems.clear();
        } else {
            this.mPostLayoutItems.clear();
        }
        for (int i = 0; i < count; i++) {
            View child = menuView.getChildAt(i);
            int id = child.getId();
            if (!((id <= 0 && id != -1) || child.getWidth() == 0 || child.getHeight() == 0)) {
                items.put(id, new MenuItemLayoutInfo(child, preLayout));
            }
        }
    }

    private void runItemAnimations() {
        int i = 0;
        while (true) {
            if (i >= this.mPreLayoutItems.size()) {
                break;
            }
            int j;
            ItemAnimationInfo oldInfo;
            int i2;
            ObjectAnimator anim;
            ItemAnimationInfo info;
            float oldAlpha;
            int id = this.mPreLayoutItems.keyAt(i);
            MenuItemLayoutInfo menuItemLayoutInfoPre = (MenuItemLayoutInfo) this.mPreLayoutItems.get(id);
            int postLayoutIndex = this.mPostLayoutItems.indexOfKey(id);
            if (postLayoutIndex >= 0) {
                Property property;
                float[] fArr;
                MenuItemLayoutInfo menuItemLayoutInfoPost = (MenuItemLayoutInfo) this.mPostLayoutItems.valueAt(postLayoutIndex);
                PropertyValuesHolder pvhX = null;
                PropertyValuesHolder pvhY = null;
                if (menuItemLayoutInfoPre.left != menuItemLayoutInfoPost.left) {
                    property = View.TRANSLATION_X;
                    fArr = new float[2];
                    fArr[0] = (float) (menuItemLayoutInfoPre.left - menuItemLayoutInfoPost.left);
                    fArr[1] = 0.0f;
                    pvhX = PropertyValuesHolder.ofFloat(property, fArr);
                }
                if (menuItemLayoutInfoPre.top != menuItemLayoutInfoPost.top) {
                    property = View.TRANSLATION_Y;
                    fArr = new float[2];
                    fArr[0] = (float) (menuItemLayoutInfoPre.top - menuItemLayoutInfoPost.top);
                    fArr[1] = 0.0f;
                    pvhY = PropertyValuesHolder.ofFloat(property, fArr);
                }
                if (!(pvhX == null && pvhY == null)) {
                    j = 0;
                    while (true) {
                        if (j >= this.mRunningItemAnimations.size()) {
                            break;
                        }
                        oldInfo = (ItemAnimationInfo) this.mRunningItemAnimations.get(j);
                        i2 = oldInfo.id;
                        if (r0 == id && oldInfo.animType == 0) {
                            oldInfo.animator.cancel();
                        }
                        j++;
                    }
                    if (pvhX == null) {
                        anim = ObjectAnimator.ofPropertyValuesHolder(menuItemLayoutInfoPost.view, new PropertyValuesHolder[]{pvhY});
                    } else if (pvhY != null) {
                        anim = ObjectAnimator.ofPropertyValuesHolder(menuItemLayoutInfoPost.view, new PropertyValuesHolder[]{pvhX, pvhY});
                    } else {
                        anim = ObjectAnimator.ofPropertyValuesHolder(menuItemLayoutInfoPost.view, new PropertyValuesHolder[]{pvhX});
                    }
                    anim.setDuration(350);
                    anim.setInterpolator(new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f));
                    anim.start();
                    info = new ItemAnimationInfo(id, menuItemLayoutInfoPost, anim, 0);
                    this.mRunningItemAnimations.add(info);
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
                oldAlpha = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
                j = 0;
                while (true) {
                    if (j >= this.mRunningItemAnimations.size()) {
                        break;
                    }
                    oldInfo = (ItemAnimationInfo) this.mRunningItemAnimations.get(j);
                    i2 = oldInfo.id;
                    if (r0 == id) {
                        i2 = oldInfo.animType;
                        if (r0 == 1) {
                            oldAlpha = oldInfo.menuItemLayoutInfo.view.getAlpha();
                            oldInfo.animator.cancel();
                        }
                    }
                    j++;
                }
                anim = ObjectAnimator.ofFloat(menuItemLayoutInfoPre.view, View.ALPHA, new float[]{oldAlpha, 0.0f});
                if (id == -1) {
                    this.mToolbarAttachOverlay = true;
                } else {
                    ((ActionMenuItemView) menuItemLayoutInfoPre.view).setToolBarAttachOverlay(true);
                }
                ViewGroup container = (ViewGroup) getAnimateOverlayRootView();
                if (container != null) {
                    container.getOverlay().add(menuItemLayoutInfoPre.view);
                    info = new ItemAnimationInfo(id, menuItemLayoutInfoPre, anim, 2);
                    this.mRunningItemAnimations.add(info);
                    anim.addListener(new AnonymousClass4(container, menuItemLayoutInfoPre, id));
                    anim.setDuration(350);
                    anim.setInterpolator(new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f));
                    anim.start();
                }
            }
            i++;
        }
        i = 0;
        while (true) {
            if (i < this.mPostLayoutItems.size()) {
                id = this.mPostLayoutItems.keyAt(i);
                postLayoutIndex = this.mPostLayoutItems.indexOfKey(id);
                if (postLayoutIndex >= 0) {
                    MenuItemLayoutInfo menuItemLayoutInfo = (MenuItemLayoutInfo) this.mPostLayoutItems.valueAt(postLayoutIndex);
                    oldAlpha = 0.0f;
                    j = 0;
                    while (true) {
                        if (j >= this.mRunningItemAnimations.size()) {
                            break;
                        }
                        oldInfo = (ItemAnimationInfo) this.mRunningItemAnimations.get(j);
                        i2 = oldInfo.id;
                        if (r0 == id) {
                            i2 = oldInfo.animType;
                            if (r0 == 2) {
                                oldAlpha = oldInfo.menuItemLayoutInfo.view.getAlpha();
                                oldInfo.animator.cancel();
                            }
                        }
                        j++;
                    }
                    anim = ObjectAnimator.ofFloat(menuItemLayoutInfo.view, View.ALPHA, new float[]{oldAlpha, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL});
                    anim.start();
                    anim.setDuration(350);
                    anim.setInterpolator(new PathInterpolator(0.3f, 0.15f, 0.1f, 0.85f));
                    info = new ItemAnimationInfo(id, menuItemLayoutInfo, anim, 1);
                    this.mRunningItemAnimations.add(info);
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
                i++;
            } else {
                return;
            }
        }
    }

    private void setupItemAnimations() {
        computeMenuItemAnimationInfo(true);
        ((View) this.mMenuView).getViewTreeObserver().addOnPreDrawListener(this.mItemAnimationPreDrawListener);
    }

    public void updateMenuView(boolean cleared) {
        int count;
        if (((ViewGroup) ((View) this.mMenuView).getParent()) != null && this.mAnimationEnabled) {
            setupItemAnimations();
        }
        if (this.mActionButtonPopup == null || !this.mActionButtonPopup.isShowing()) {
            this.mIsReuse = false;
        } else {
            this.mIsReuse = true;
        }
        super.updateMenuView(cleared);
        ((View) this.mMenuView).requestLayout();
        if (this.mMenu != null) {
            ArrayList<MenuItemImpl> actionItems = this.mMenu.getActionItems();
            count = actionItems.size();
            for (int i = 0; i < count; i++) {
                ActionProvider provider = ((MenuItemImpl) actionItems.get(i)).getActionProvider();
                if (provider != null) {
                    provider.setSubUiVisibilityListener(this);
                }
            }
        }
        ArrayList nonActionItems = this.mMenu != null ? this.mMenu.getNonActionItems() : null;
        boolean hasOverflow = false;
        if (this.mReserveOverflow && nonActionItems != null) {
            count = nonActionItems.size();
            hasOverflow = count == 1 ? !((MenuItemImpl) nonActionItems.get(0)).isActionViewExpanded() : count > 0;
        }
        if (hasOverflow) {
            if (this.mOverflowButton == null) {
                this.mOverflowButton = HwWidgetFactory.getHwOverflowMenuButton(this.mSystemContext, this);
                if (this.mOverflowButton == null) {
                    this.mOverflowButton = new OverflowMenuButton(this.mSystemContext);
                }
            }
            ViewGroup parent = (ViewGroup) this.mOverflowButton.getParent();
            if (parent != this.mMenuView) {
                if (parent != null) {
                    parent.removeView(this.mOverflowButton);
                }
                ActionMenuView menuView = this.mMenuView;
                menuView.addView(this.mOverflowButton, menuView.generateOverflowButtonLayoutParams());
            }
        } else if (this.mOverflowButton != null && this.mOverflowButton.getParent() == this.mMenuView) {
            ((ViewGroup) this.mMenuView).removeView(this.mOverflowButton);
        }
        ((ActionMenuView) this.mMenuView).setOverflowReserved(this.mReserveOverflow);
    }

    public boolean filterLeftoverView(ViewGroup parent, int childIndex) {
        if (parent.getChildAt(childIndex) == this.mOverflowButton) {
            return false;
        }
        return super.filterLeftoverView(parent, childIndex);
    }

    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        if (!subMenu.hasVisibleItems()) {
            return false;
        }
        SubMenuBuilder topSubMenu = subMenu;
        while (topSubMenu.getParentMenu() != this.mMenu) {
            topSubMenu = (SubMenuBuilder) topSubMenu.getParentMenu();
        }
        View anchor = findViewForItem(topSubMenu.getItem());
        if (anchor == null) {
            return false;
        }
        this.mOpenSubMenuId = subMenu.getItem().getItemId();
        boolean preserveIconSpacing = false;
        int count = subMenu.size();
        for (int i = 0; i < count; i++) {
            MenuItem childItem = subMenu.getItem(i);
            if (childItem.isVisible() && childItem.getIcon() != null) {
                preserveIconSpacing = true;
                break;
            }
        }
        this.mActionButtonPopup = new ActionButtonSubmenu(this.mContext, subMenu, anchor);
        this.mActionButtonPopup.setForceShowIcon(preserveIconSpacing);
        this.mActionButtonPopup.show();
        super.onSubMenuSelected(subMenu);
        return true;
    }

    private View findViewForItem(MenuItem item) {
        ViewGroup parent = this.mMenuView;
        if (parent == null) {
            return null;
        }
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            if ((child instanceof ItemView) && ((ItemView) child).getItemData() == item) {
                return child;
            }
        }
        return null;
    }

    public boolean showOverflowMenu() {
        if (!this.mReserveOverflow || isOverflowMenuShowing() || this.mMenu == null || this.mMenuView == null || this.mPostedOpenRunnable != null || this.mMenu.getNonActionItems().isEmpty()) {
            return false;
        }
        OverflowPopup popup = new OverflowPopup(this.mContext, this.mMenu, this.mOverflowButton, true);
        MenuPopupWindow menuPopupWindow = popup.getPopup().getMenuPopup();
        if (menuPopupWindow != null) {
            menuPopupWindow.mPopup.setPopupLocation(this.mPopupStartLocation, this.mPopupEndLocation);
        }
        this.mPostedOpenRunnable = new OpenOverflowRunnable(popup);
        ((View) this.mMenuView).post(this.mPostedOpenRunnable);
        super.onSubMenuSelected(null);
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
        ((View) this.mMenuView).removeCallbacks(this.mPostedOpenRunnable);
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
        return this.mOverflowPopup != null ? this.mOverflowPopup.isShowing() : false;
    }

    public boolean isOverflowMenuShowPending() {
        return this.mPostedOpenRunnable == null ? isOverflowMenuShowing() : true;
    }

    public boolean isOverflowReserved() {
        return this.mReserveOverflow;
    }

    public boolean flagActionItems() {
        ArrayList<MenuItemImpl> visibleItems;
        int itemsSize;
        int i;
        if (this.mMenu != null) {
            visibleItems = this.mMenu.getVisibleItems();
            itemsSize = visibleItems.size();
        } else {
            visibleItems = null;
            itemsSize = 0;
        }
        int maxActions = this.mMaxItems;
        int widthLimit = this.mActionItemWidthLimit;
        int querySpec = MeasureSpec.makeMeasureSpec(0, 0);
        ViewGroup parent = (ViewGroup) this.mMenuView;
        int requiredItems = 0;
        int requestedItems = 0;
        int firstActionWidth = 0;
        boolean hasOverflow = false;
        for (i = 0; i < itemsSize; i++) {
            MenuItemImpl item = (MenuItemImpl) visibleItems.get(i);
            if (item.requiresActionButton()) {
                requiredItems++;
            } else if (item.requestsActionButton()) {
                requestedItems++;
            } else {
                hasOverflow = true;
            }
            if (this.mExpandedActionViewsExclusive && item.isActionViewExpanded()) {
                maxActions = 0;
            }
        }
        if (this.mReserveOverflow && (hasOverflow || requiredItems + requestedItems > maxActions)) {
            maxActions--;
        }
        maxActions -= requiredItems;
        SparseBooleanArray seenGroups = this.mActionButtonGroups;
        seenGroups.clear();
        int cellSize = 0;
        int cellsRemaining = 0;
        if (this.mStrictWidthLimit) {
            cellsRemaining = widthLimit / this.mMinCellSize;
            cellSize = this.mMinCellSize + ((widthLimit % this.mMinCellSize) / cellsRemaining);
        }
        for (i = 0; i < itemsSize; i++) {
            item = (MenuItemImpl) visibleItems.get(i);
            View v;
            int measuredWidth;
            int groupId;
            if (item.requiresActionButton()) {
                v = getItemView(item, null, parent);
                if (this.mStrictWidthLimit) {
                    cellsRemaining -= ActionMenuView.measureChildForCells(v, cellSize, cellsRemaining, querySpec, 0);
                } else {
                    v.measure(querySpec, querySpec);
                }
                measuredWidth = v.getMeasuredWidth();
                widthLimit -= measuredWidth;
                if (firstActionWidth == 0) {
                    firstActionWidth = measuredWidth;
                }
                groupId = item.getGroupId();
                if (groupId != 0) {
                    seenGroups.put(groupId, true);
                }
                item.setIsActionButton(true);
            } else if (item.requestsActionButton()) {
                boolean isAction;
                groupId = item.getGroupId();
                boolean inGroup = seenGroups.get(groupId);
                if ((maxActions > 0 || inGroup) && widthLimit > 0) {
                    boolean z = !this.mStrictWidthLimit || cellsRemaining > 0;
                    isAction = z;
                } else {
                    isAction = false;
                }
                if (isAction) {
                    int isAction2;
                    v = getItemView(item, null, parent);
                    if (this.mStrictWidthLimit) {
                        int cells = ActionMenuView.measureChildForCells(v, cellSize, cellsRemaining, querySpec, 0);
                        cellsRemaining -= cells;
                        if (cells == 0) {
                            isAction2 = 0;
                        }
                    } else {
                        v.measure(querySpec, querySpec);
                    }
                    measuredWidth = v.getMeasuredWidth();
                    widthLimit -= measuredWidth;
                    if (firstActionWidth == 0) {
                        firstActionWidth = measuredWidth;
                    }
                    if (this.mStrictWidthLimit) {
                        isAction = isAction2 & (widthLimit >= 0 ? 1 : 0);
                    } else {
                        isAction = isAction2 & (widthLimit + firstActionWidth > 0 ? 1 : 0);
                    }
                }
                if (isAction && groupId != 0) {
                    seenGroups.put(groupId, true);
                } else if (inGroup) {
                    seenGroups.put(groupId, false);
                    for (int j = 0; j < i; j++) {
                        MenuItemImpl areYouMyGroupie = (MenuItemImpl) visibleItems.get(j);
                        if (areYouMyGroupie.getGroupId() == groupId) {
                            if (areYouMyGroupie.isActionButton()) {
                                maxActions++;
                            }
                            areYouMyGroupie.setIsActionButton(false);
                        }
                    }
                }
                if (isAction) {
                    maxActions--;
                }
                item.setIsActionButton(isAction);
            } else {
                item.setIsActionButton(false);
            }
        }
        return true;
    }

    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        dismissPopupMenus();
        super.onCloseMenu(menu, allMenusAreClosing);
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
                onSubMenuSelected((SubMenuBuilder) item.getSubMenu());
            }
        }
    }

    public void onSubUiVisibilityChanged(boolean isVisible) {
        if (isVisible) {
            super.onSubMenuSelected(null);
        } else if (this.mMenu != null) {
            this.mMenu.close(false);
        }
    }

    public void setMenuView(ActionMenuView menuView) {
        if (menuView != this.mMenuView) {
            if (this.mMenuView != null) {
                ((View) this.mMenuView).removeOnAttachStateChangeListener(this.mAttachStateChangeListener);
            }
            this.mMenuView = menuView;
            menuView.initialize(this.mMenu);
            menuView.addOnAttachStateChangeListener(this.mAttachStateChangeListener);
        }
    }

    public OverflowPopup getOverflowPopup() {
        return this.mOverflowPopup;
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

    protected View getOverflowButton() {
        return this.mOverflowButton;
    }

    protected void setPopupGravity(MenuPopupHelper mph) {
        mph.setGravity(Gravity.END);
    }

    protected int getMaxActionButtons(int maxItems) {
        return maxItems;
    }

    public void showOverflowMenuPending() {
    }

    public void setPopupLocation(int start, int end) {
    }
}
