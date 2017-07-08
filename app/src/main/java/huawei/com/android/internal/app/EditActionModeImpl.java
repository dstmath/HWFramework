package huawei.com.android.internal.app;

import android.content.Context;
import android.view.ActionMode.Callback;
import android.view.View;
import huawei.com.android.internal.widget.HwActionBarContextView;
import java.lang.ref.WeakReference;

public class EditActionModeImpl extends DefaultActionModeImpl {
    private WeakReference<View> mCustomView;

    public EditActionModeImpl(Context context, Callback callback) {
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

    public void setTitle(CharSequence cs) {
        HwActionBarContextView habc = (HwActionBarContextView) this.mActionModeView.get();
        if (habc != null) {
            habc.setTitle(cs);
        }
    }

    public void setSubtitle(CharSequence cs) {
        HwActionBarContextView habc = (HwActionBarContextView) this.mActionModeView.get();
        if (habc != null) {
            habc.setSubtitle(cs);
        }
    }

    public CharSequence getTitle() {
        HwActionBarContextView habc = (HwActionBarContextView) this.mActionModeView.get();
        if (habc != null) {
            return habc.getTitle();
        }
        return null;
    }

    public CharSequence getSubtitle() {
        HwActionBarContextView habc = (HwActionBarContextView) this.mActionModeView.get();
        if (habc != null) {
            return habc.getSubtitle();
        }
        return null;
    }

    public void setCustomView(View view) {
        HwActionBarContextView habc = (HwActionBarContextView) this.mActionModeView.get();
        if (habc != null) {
            habc.setCustomView(view);
        }
        this.mCustomView = new WeakReference(view);
    }

    public View getCustomView() {
        return this.mCustomView != null ? (View) this.mCustomView.get() : null;
    }
}
