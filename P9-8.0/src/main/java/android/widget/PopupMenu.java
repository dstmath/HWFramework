package android.widget;

import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnTouchListener;
import com.android.internal.R;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuBuilder.Callback;
import com.android.internal.view.menu.MenuPopupHelper;
import com.android.internal.view.menu.ShowableListMenu;

public class PopupMenu {
    private final View mAnchor;
    private final Context mContext;
    private OnTouchListener mDragListener;
    private final MenuBuilder mMenu;
    private OnMenuItemClickListener mMenuItemClickListener;
    private OnDismissListener mOnDismissListener;
    private final MenuPopupHelper mPopup;

    public interface OnDismissListener {
        void onDismiss(PopupMenu popupMenu);
    }

    public interface OnMenuItemClickListener {
        boolean onMenuItemClick(MenuItem menuItem);
    }

    public PopupMenu(Context context, View anchor) {
        this(context, anchor, 0);
    }

    public PopupMenu(Context context, View anchor, int gravity) {
        this(context, anchor, gravity, R.attr.popupMenuStyle, 0);
    }

    public PopupMenu(Context context, View anchor, int gravity, int popupStyleAttr, int popupStyleRes) {
        this.mContext = context;
        this.mAnchor = anchor;
        this.mMenu = new MenuBuilder(context);
        this.mMenu.setCallback(new Callback() {
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                if (PopupMenu.this.mMenuItemClickListener != null) {
                    return PopupMenu.this.mMenuItemClickListener.onMenuItemClick(item);
                }
                return false;
            }

            public void onMenuModeChange(MenuBuilder menu) {
            }
        });
        this.mPopup = new MenuPopupHelper(context, this.mMenu, anchor, false, popupStyleAttr, popupStyleRes);
        this.mPopup.setGravity(gravity);
        this.mPopup.setOnDismissListener(new android.widget.PopupWindow.OnDismissListener() {
            public void onDismiss() {
                if (PopupMenu.this.mOnDismissListener != null) {
                    PopupMenu.this.mOnDismissListener.onDismiss(PopupMenu.this);
                }
            }
        });
    }

    public void setGravity(int gravity) {
        this.mPopup.setGravity(gravity);
    }

    public int getGravity() {
        return this.mPopup.getGravity();
    }

    public OnTouchListener getDragToOpenListener() {
        if (this.mDragListener == null) {
            this.mDragListener = new ForwardingListener(this.mAnchor) {
                protected boolean onForwardingStarted() {
                    PopupMenu.this.show();
                    return true;
                }

                protected boolean onForwardingStopped() {
                    PopupMenu.this.dismiss();
                    return true;
                }

                public ShowableListMenu getPopup() {
                    return PopupMenu.this.mPopup.getPopup();
                }
            };
        }
        return this.mDragListener;
    }

    public Menu getMenu() {
        return this.mMenu;
    }

    public MenuInflater getMenuInflater() {
        return new MenuInflater(this.mContext);
    }

    public void inflate(int menuRes) {
        getMenuInflater().inflate(menuRes, this.mMenu);
    }

    public void show() {
        this.mPopup.show();
    }

    public void dismiss() {
        this.mPopup.dismiss();
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        this.mMenuItemClickListener = listener;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.mOnDismissListener = listener;
    }

    public ListView getMenuListView() {
        if (this.mPopup.isShowing()) {
            return this.mPopup.getPopup().getListView();
        }
        return null;
    }
}
