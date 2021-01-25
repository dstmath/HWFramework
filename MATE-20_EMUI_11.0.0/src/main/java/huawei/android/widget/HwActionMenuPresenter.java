package huawei.android.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ActionMenuPresenter;
import android.widget.ForwardingListener;
import android.widget.ListPopupWindow;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPopup;
import com.android.internal.view.menu.MenuPopupHelper;
import com.android.internal.view.menu.SubMenuBuilder;
import huawei.android.widget.utils.ReflectUtil;

public class HwActionMenuPresenter extends ActionMenuPresenter {
    private static final boolean IS_DEBUG = false;
    private static final int MENU_NUM_LIMIT = 3;
    private static final String TAG = "HwActionMenuPresenter";
    private boolean mIsShowOverflowMenuPending = false;
    private ViewTreeObserver.OnPreDrawListener mOverflowMenuPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        /* class huawei.android.widget.HwActionMenuPresenter.AnonymousClass1 */

        @Override // android.view.ViewTreeObserver.OnPreDrawListener
        public boolean onPreDraw() {
            View over = HwActionMenuPresenter.this.getOverflowButton();
            if (over != null) {
                over.getViewTreeObserver().removeOnPreDrawListener(this);
            }
            if (!HwActionMenuPresenter.this.mIsShowOverflowMenuPending) {
                return true;
            }
            HwActionMenuPresenter.this.mIsShowOverflowMenuPending = false;
            HwActionMenuPresenter.this.showOverflowMenu();
            return true;
        }
    };

    public HwActionMenuPresenter(Context context) {
        super(context);
    }

    public HwActionMenuPresenter(Context context, int menuLayout, int itemLayout) {
        super(context, menuLayout, itemLayout);
    }

    public boolean isPopupMenuShowing() {
        return getParentActionButtonPopup() != null && getParentActionButtonPopup().isShowing();
    }

    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState();
        state.mOpenSubMenuId = getParentOpenSubMenuId();
        state.mOverflowMenuShownInt = isOverflowMenuShowing() ? 1 : 0;
        return state;
    }

    public void onRestoreInstanceState(Parcelable state) {
        MenuItem item;
        if (state instanceof SavedState) {
            SavedState saved = (SavedState) state;
            if (this.mMenu != null && saved.mOpenSubMenuId > 0 && (item = this.mMenu.findItem(saved.mOpenSubMenuId)) != null && (item.getSubMenu() instanceof SubMenuBuilder)) {
                onSubMenuSelected((SubMenuBuilder) item.getSubMenu());
            }
            if (saved.mOverflowMenuShownInt > 0) {
                showOverflowMenuPending();
            }
        }
    }

    public void showOverflowMenuPending() {
        this.mIsShowOverflowMenuPending = true;
    }

    public void updateMenuView(boolean isCleared) {
        HwActionMenuPresenter.super.updateMenuView(isCleared);
        View over = getOverflowButton();
        if (over != null) {
            ViewTreeObserver observer = over.getViewTreeObserver();
            observer.removeOnPreDrawListener(this.mOverflowMenuPreDrawListener);
            observer.addOnPreDrawListener(this.mOverflowMenuPreDrawListener);
        }
    }

    private int getParentOpenSubMenuId() {
        Object obj = ReflectUtil.getObject(this, "mOpenSubMenuId", ActionMenuPresenter.class);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ActionMenuPresenter.ActionButtonSubmenu getParentActionButtonPopup() {
        Object object = ReflectUtil.getObject(this, "mActionButtonPopup", ActionMenuPresenter.class);
        if (object instanceof ActionMenuPresenter.ActionButtonSubmenu) {
            return (ActionMenuPresenter.ActionButtonSubmenu) object;
        }
        return null;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: android.view.View */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r1v3, types: [android.view.View$OnTouchListener, huawei.android.widget.HwActionMenuPresenter$3] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public View getItemView(final MenuItemImpl item, View convertView, ViewGroup parent) {
        View actionView = HwActionMenuPresenter.super.getItemView(item, convertView, parent);
        actionView.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            /* class huawei.android.widget.HwActionMenuPresenter.AnonymousClass2 */

            @Override // android.view.View.AccessibilityDelegate
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                info.setSelected(false);
                info.removeAction(4);
            }
        });
        if (item.hasSubMenu()) {
            actionView.setOnTouchListener(new ForwardingListener(actionView) {
                /* class huawei.android.widget.HwActionMenuPresenter.AnonymousClass3 */

                public ListPopupWindow getPopup() {
                    MenuPopup popup;
                    if (HwActionMenuPresenter.this.getParentActionButtonPopup() == null || (popup = HwActionMenuPresenter.this.getParentActionButtonPopup().getPopup()) == null) {
                        return null;
                    }
                    return popup.getMenuPopup();
                }

                /* access modifiers changed from: protected */
                public boolean onForwardingStarted() {
                    SubMenu subMenu = item.getSubMenu();
                    if (subMenu instanceof SubMenuBuilder) {
                        return HwActionMenuPresenter.this.onSubMenuSelected((SubMenuBuilder) subMenu);
                    }
                    return false;
                }

                /* access modifiers changed from: protected */
                public boolean onForwardingStopped() {
                    return HwActionMenuPresenter.this.dismissPopupMenus();
                }

                public boolean onTouch(View view, MotionEvent event) {
                    this.mForwarding = this.mForwarding && getPopup() != null;
                    return HwActionMenuPresenter.super.onTouch(view, event);
                }
            });
        } else {
            actionView.setOnTouchListener(null);
        }
        return actionView;
    }

    public Drawable getOverflowIcon() {
        if (getOverflowButton() instanceof HwOverflowMenuButton) {
            return null;
        }
        return HwActionMenuPresenter.super.getOverflowIcon();
    }

    public void setOverflowIcon(Drawable icon) {
        if (!(getOverflowButton() instanceof HwOverflowMenuButton)) {
            HwActionMenuPresenter.super.setOverflowIcon(icon);
        }
    }

    /* access modifiers changed from: protected */
    public void setPopupGravity(MenuPopupHelper menuPopupHelper) {
        menuPopupHelper.setGravity(0);
    }

    /* access modifiers changed from: protected */
    public int getMaxActionButtons(int maxItems) {
        return 3;
    }

    public void setPopupLocation(int start, int end) {
        this.mPopupStartLocation = start;
        this.mPopupEndLocation = end;
    }

    /* access modifiers changed from: protected */
    public void setMenuPopup(MenuPopupHelper menuPopupHelper, int start, int end) {
        HwActionMenuPresenter.super.setMenuPopup(menuPopupHelper, start, end);
    }

    /* access modifiers changed from: private */
    public static class SavedState implements Parcelable {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            /* class huawei.android.widget.HwActionMenuPresenter.SavedState.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override // android.os.Parcelable.Creator
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int mOpenSubMenuId;
        int mOverflowMenuShownInt;

        SavedState() {
        }

        SavedState(Parcel in) {
            this.mOpenSubMenuId = in.readInt();
            this.mOverflowMenuShownInt = in.readInt();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mOpenSubMenuId);
            dest.writeInt(this.mOverflowMenuShownInt);
        }
    }
}
