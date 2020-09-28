package huawei.com.android.internal.app;

import android.content.Context;
import android.view.ActionMode;
import android.view.View;
import huawei.com.android.internal.widget.HwActionBarContextView;
import java.lang.ref.WeakReference;

public class EditActionModeImpl extends DefaultActionModeImpl {
    private WeakReference<View> mCustomView;

    public EditActionModeImpl(Context context, ActionMode.Callback callback) {
        super(context, callback);
    }

    public void setImageResource(int resIdOK, int resIdCancel) {
        HwActionBarContextView habc = (HwActionBarContextView) this.mActionModeView.get();
        if (habc != null) {
            habc.setImageResource(resIdOK, resIdCancel);
        }
    }

    public void setActionVisible(boolean OKVis, boolean cancelVis) {
        HwActionBarContextView habc = (HwActionBarContextView) this.mActionModeView.get();
        if (habc != null) {
            habc.setActionVisible(OKVis, cancelVis);
        }
    }

    public void setContentDescription(CharSequence okContentDescription, CharSequence cancelContentDescription) {
        HwActionBarContextView habc = (HwActionBarContextView) this.mActionModeView.get();
        if (habc != null) {
            habc.setContentDescription(okContentDescription, cancelContentDescription);
        }
    }

    @Override // huawei.com.android.internal.app.DefaultActionModeImpl, android.view.ActionMode
    public void setTitle(CharSequence cs) {
        HwActionBarContextView habc = (HwActionBarContextView) this.mActionModeView.get();
        if (habc != null) {
            habc.setTitle(cs);
        }
    }

    @Override // huawei.com.android.internal.app.DefaultActionModeImpl, android.view.ActionMode
    public void setSubtitle(CharSequence cs) {
        HwActionBarContextView habc = (HwActionBarContextView) this.mActionModeView.get();
        if (habc != null) {
            habc.setSubtitle(cs);
        }
    }

    @Override // huawei.com.android.internal.app.DefaultActionModeImpl
    public CharSequence getTitle() {
        HwActionBarContextView habc = (HwActionBarContextView) this.mActionModeView.get();
        if (habc != null) {
            return habc.getTitle();
        }
        return null;
    }

    @Override // huawei.com.android.internal.app.DefaultActionModeImpl
    public CharSequence getSubtitle() {
        HwActionBarContextView habc = (HwActionBarContextView) this.mActionModeView.get();
        if (habc != null) {
            return habc.getSubtitle();
        }
        return null;
    }

    @Override // huawei.com.android.internal.app.DefaultActionModeImpl
    public void setCustomView(View view) {
        HwActionBarContextView habc = (HwActionBarContextView) this.mActionModeView.get();
        if (habc != null) {
            habc.setCustomView(view);
        }
        this.mCustomView = new WeakReference<>(view);
    }

    @Override // huawei.com.android.internal.app.DefaultActionModeImpl
    public View getCustomView() {
        WeakReference<View> weakReference = this.mCustomView;
        if (weakReference != null) {
            return weakReference.get();
        }
        return null;
    }
}
