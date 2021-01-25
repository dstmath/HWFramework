package huawei.com.android.internal.app;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.android.internal.view.menu.MenuBuilder;
import huawei.com.android.internal.widget.ActionModeView;
import java.lang.ref.WeakReference;

public class DefaultActionModeImpl extends ActionMode implements MenuBuilder.Callback {
    private ActionModeCallback mActionModeCallback;
    protected WeakReference<ActionModeView> mActionModeView;
    private ActionMode.Callback mCallback;
    protected Context mContext;
    private boolean mFinished;
    private MenuBuilder mMenu;

    public interface ActionModeCallback {
        void onActionModeFinish(ActionMode actionMode);
    }

    public DefaultActionModeImpl(Context context, ActionMode.Callback callback) {
        this.mContext = context;
        this.mCallback = callback;
        this.mMenu = new MenuBuilder(context).setDefaultShowAsAction(1);
        this.mMenu.setCallback(this);
    }

    public boolean dispatchOnCreate() {
        this.mMenu.stopDispatchingItemsChanged();
        try {
            return this.mCallback.onCreateActionMode(this, this.mMenu);
        } finally {
            this.mMenu.startDispatchingItemsChanged();
        }
    }

    @Override // android.view.ActionMode
    public void finish() {
        if (!this.mFinished) {
            ActionMode.Callback callback = this.mCallback;
            if (callback != null) {
                callback.onDestroyActionMode(this);
                this.mCallback = null;
            }
            ActionModeCallback actionModeCallback = this.mActionModeCallback;
            if (actionModeCallback != null) {
                actionModeCallback.onActionModeFinish(this);
            }
            ActionModeView amv = this.mActionModeView.get();
            if (amv != null) {
                amv.closeMode();
            }
            this.mFinished = true;
        }
    }

    @Override // android.view.ActionMode
    public View getCustomView() {
        throw new UnsupportedOperationException("getCustomView not supported");
    }

    @Override // android.view.ActionMode
    public Menu getMenu() {
        return this.mMenu;
    }

    @Override // android.view.ActionMode
    public MenuInflater getMenuInflater() {
        return new MenuInflater(this.mContext);
    }

    @Override // android.view.ActionMode
    public CharSequence getSubtitle() {
        throw new UnsupportedOperationException("getSubtitle not supported");
    }

    @Override // android.view.ActionMode
    public CharSequence getTitle() {
        throw new UnsupportedOperationException("getTitle not supported");
    }

    @Override // android.view.ActionMode
    public void invalidate() {
        this.mMenu.stopDispatchingItemsChanged();
        try {
            this.mCallback.onPrepareActionMode(this, this.mMenu);
        } finally {
            this.mMenu.startDispatchingItemsChanged();
        }
    }

    public boolean onMenuItemSelected(MenuBuilder paramMenuBuilder, MenuItem menuItem) {
        ActionMode.Callback callback = this.mCallback;
        if (callback != null) {
            return callback.onActionItemClicked(this, menuItem);
        }
        return false;
    }

    public void onMenuModeChange(MenuBuilder menuBuilder) {
        if (this.mCallback == null) {
            invalidate();
        }
    }

    public void setActionModeCallback(ActionModeCallback callback) {
        this.mActionModeCallback = callback;
    }

    public void setActionModeView(ActionModeView actionModeView) {
        this.mActionModeView = new WeakReference<>(actionModeView);
    }

    @Override // android.view.ActionMode
    public void setCustomView(View view) {
        throw new UnsupportedOperationException("setCustomView not supported");
    }

    @Override // android.view.ActionMode
    public void setSubtitle(int resId) {
        throw new UnsupportedOperationException("setSubTitle not supported");
    }

    @Override // android.view.ActionMode
    public void setSubtitle(CharSequence cs) {
        throw new UnsupportedOperationException("setSubTitle not supported");
    }

    @Override // android.view.ActionMode
    public void setTitle(int resId) {
        throw new UnsupportedOperationException("setTitle not supported");
    }

    @Override // android.view.ActionMode
    public void setTitle(CharSequence cs) {
        throw new UnsupportedOperationException("setTitle not supported");
    }
}
