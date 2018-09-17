package huawei.com.android.internal.app;

import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import com.android.internal.view.menu.MenuBuilder;
import com.android.internal.view.menu.MenuBuilder.Callback;
import huawei.com.android.internal.widget.ActionModeView;
import java.lang.ref.WeakReference;

public class DefaultActionModeImpl extends ActionMode implements Callback {
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
            boolean onCreateActionMode = this.mCallback.onCreateActionMode(this, this.mMenu);
            return onCreateActionMode;
        } finally {
            this.mMenu.startDispatchingItemsChanged();
        }
    }

    public void finish() {
        if (!this.mFinished) {
            if (this.mCallback != null) {
                this.mCallback.onDestroyActionMode(this);
                this.mCallback = null;
            }
            if (this.mActionModeCallback != null) {
                this.mActionModeCallback.onActionModeFinish(this);
            }
            ActionModeView amv = (ActionModeView) this.mActionModeView.get();
            if (amv != null) {
                amv.closeMode();
            }
            this.mFinished = true;
        }
    }

    public View getCustomView() {
        throw new UnsupportedOperationException("getCustomView not supported");
    }

    public Menu getMenu() {
        return this.mMenu;
    }

    public MenuInflater getMenuInflater() {
        return new MenuInflater(this.mContext);
    }

    public CharSequence getSubtitle() {
        throw new UnsupportedOperationException("getSubtitle not supported");
    }

    public CharSequence getTitle() {
        throw new UnsupportedOperationException("getTitle not supported");
    }

    public void invalidate() {
        this.mMenu.stopDispatchingItemsChanged();
        try {
            this.mCallback.onPrepareActionMode(this, this.mMenu);
        } finally {
            this.mMenu.startDispatchingItemsChanged();
        }
    }

    public boolean onMenuItemSelected(MenuBuilder paramMenuBuilder, MenuItem menuItem) {
        if (this.mCallback != null) {
            return this.mCallback.onActionItemClicked(this, menuItem);
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
        this.mActionModeView = new WeakReference(actionModeView);
    }

    public void setCustomView(View view) {
        throw new UnsupportedOperationException("setCustomView not supported");
    }

    public void setSubtitle(int resId) {
        throw new UnsupportedOperationException("setSubTitle not supported");
    }

    public void setSubtitle(CharSequence cs) {
        throw new UnsupportedOperationException("setSubTitle not supported");
    }

    public void setTitle(int resId) {
        throw new UnsupportedOperationException("setTitle not supported");
    }

    public void setTitle(CharSequence cs) {
        throw new UnsupportedOperationException("setTitle not supported");
    }
}
