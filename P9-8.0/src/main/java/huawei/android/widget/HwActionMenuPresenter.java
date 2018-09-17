package huawei.android.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ActionMenuPresenter;
import android.widget.ForwardingListener;
import android.widget.ListPopupWindow;
import com.android.internal.view.menu.MenuItemImpl;
import com.android.internal.view.menu.MenuPopup;
import com.android.internal.view.menu.MenuPopupHelper;
import com.android.internal.view.menu.SubMenuBuilder;

public class HwActionMenuPresenter extends ActionMenuPresenter {
    private OnPreDrawListener mOverflowMenuPreDrawListener = new OnPreDrawListener() {
        public boolean onPreDraw() {
            View over = HwActionMenuPresenter.this.getOverflowButton();
            if (over != null) {
                over.getViewTreeObserver().removeOnPreDrawListener(this);
            }
            if (HwActionMenuPresenter.this.mShowOverflowMenuPending) {
                HwActionMenuPresenter.this.mShowOverflowMenuPending = false;
                HwActionMenuPresenter.this.showOverflowMenu();
            }
            return true;
        }
    };
    private boolean mShowOverflowMenuPending = false;

    private static class SavedState implements Parcelable {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        public int openSubMenuId;
        public int overflowMenuShownInt;

        SavedState() {
        }

        SavedState(Parcel in) {
            this.openSubMenuId = in.readInt();
            this.overflowMenuShownInt = in.readInt();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.openSubMenuId);
            dest.writeInt(this.overflowMenuShownInt);
        }
    }

    public HwActionMenuPresenter(Context context) {
        super(context);
    }

    public HwActionMenuPresenter(Context context, int menuLayout, int itemLayout) {
        super(context, menuLayout, itemLayout);
    }

    public boolean isPopupMenuShowing() {
        return getActionButtonPopup() != null ? getActionButtonPopup().isShowing() : false;
    }

    public Parcelable onSaveInstanceState() {
        SavedState state = new SavedState();
        state.openSubMenuId = getOpenSubMenuId();
        state.overflowMenuShownInt = isOverflowMenuShowing() ? 1 : 0;
        return state;
    }

    public void onRestoreInstanceState(Parcelable state) {
        SavedState saved = (SavedState) state;
        if (saved.openSubMenuId > 0 && this.mMenu != null) {
            MenuItem item = this.mMenu.findItem(saved.openSubMenuId);
            if (item != null) {
                onSubMenuSelected((SubMenuBuilder) item.getSubMenu());
            }
        }
        if (saved.overflowMenuShownInt > 0) {
            showOverflowMenuPending();
        }
    }

    public void showOverflowMenuPending() {
        this.mShowOverflowMenuPending = true;
    }

    public void updateMenuView(boolean cleared) {
        super.updateMenuView(cleared);
        View over = getOverflowButton();
        if (over != null) {
            ViewTreeObserver vto = over.getViewTreeObserver();
            vto.removeOnPreDrawListener(this.mOverflowMenuPreDrawListener);
            vto.addOnPreDrawListener(this.mOverflowMenuPreDrawListener);
        }
    }

    public View getItemView(final MenuItemImpl item, View convertView, ViewGroup parent) {
        View actionView = super.getItemView(item, convertView, parent);
        if (item.hasSubMenu()) {
            actionView.setOnTouchListener(new ForwardingListener(actionView) {
                public ListPopupWindow getPopup() {
                    if (HwActionMenuPresenter.this.getActionButtonPopup() != null) {
                        MenuPopup mp = HwActionMenuPresenter.this.getActionButtonPopup().getPopup();
                        if (mp != null) {
                            return mp.getMenuPopup();
                        }
                    }
                    return null;
                }

                protected boolean onForwardingStarted() {
                    return HwActionMenuPresenter.this.onSubMenuSelected((SubMenuBuilder) item.getSubMenu());
                }

                protected boolean onForwardingStopped() {
                    return HwActionMenuPresenter.this.dismissPopupMenus();
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
        } else {
            actionView.setOnTouchListener(null);
        }
        return actionView;
    }

    public void setOverflowIcon(Drawable icon) {
        if (!(getOverflowButton() instanceof HwOverflowMenuButton)) {
            super.setOverflowIcon(icon);
        }
    }

    public Drawable getOverflowIcon() {
        if (getOverflowButton() instanceof HwOverflowMenuButton) {
            return null;
        }
        return super.getOverflowIcon();
    }

    protected void setPopupGravity(MenuPopupHelper mph) {
        mph.setGravity(0);
    }

    protected int getMaxActionButtons(int maxItems) {
        return 5;
    }

    public void setPopupLocation(int start, int end) {
        this.mPopupStartLocation = start;
        this.mPopupEndLocation = end;
    }
}
