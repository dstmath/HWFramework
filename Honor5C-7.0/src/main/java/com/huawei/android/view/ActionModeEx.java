package com.huawei.android.view;

import android.view.ActionMode;
import huawei.com.android.internal.app.EditActionModeImpl;

public class ActionModeEx {
    public static void setImageResource(ActionMode actionMode, int resIdOK, int resIdCancel) {
        EditActionModeImpl eaml = getEditActionModeImpl(actionMode);
        if (eaml != null) {
            eaml.setImageResource(resIdOK, resIdCancel);
        }
    }

    public static void setActionVisible(ActionMode actionMode, boolean OKVis, boolean cancelVis) {
        EditActionModeImpl eaml = getEditActionModeImpl(actionMode);
        if (eaml != null) {
            eaml.setActionVisible(OKVis, cancelVis);
        }
    }

    public static EditActionModeImpl getEditActionModeImpl(ActionMode actionMode) {
        if (actionMode == null || !(actionMode instanceof EditActionModeImpl)) {
            return null;
        }
        return (EditActionModeImpl) actionMode;
    }

    public static void setContentDescription(ActionMode actionMode, CharSequence okContentDescription, CharSequence cancelContentDescription) {
        EditActionModeImpl eaml = getEditActionModeImpl(actionMode);
        if (eaml != null) {
            eaml.setContentDescription(okContentDescription, cancelContentDescription);
        }
    }
}
