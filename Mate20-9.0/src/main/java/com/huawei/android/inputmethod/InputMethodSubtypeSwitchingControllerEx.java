package com.huawei.android.inputmethod;

import android.content.Context;
import android.view.inputmethod.InputMethodInfo;
import com.android.internal.inputmethod.InputMethodSubtypeSwitchingController;
import com.android.internal.inputmethod.InputMethodUtils;
import com.huawei.android.inputmethod.InputMethodUtilsEx;
import java.util.ArrayList;
import java.util.List;

public class InputMethodSubtypeSwitchingControllerEx {
    InputMethodSubtypeSwitchingController mController;

    public static class ImeSubtypeListItemEx {
        InputMethodSubtypeSwitchingController.ImeSubtypeListItem mItem;

        public ImeSubtypeListItemEx(InputMethodSubtypeSwitchingController.ImeSubtypeListItem item) {
            this.mItem = item;
        }

        public InputMethodInfo getImi() {
            if (this.mItem != null) {
                return this.mItem.mImi;
            }
            return null;
        }

        public int getSubtypeId() {
            if (this.mItem != null) {
                return this.mItem.mSubtypeId;
            }
            return -1;
        }

        public CharSequence getImeName() {
            if (this.mItem != null) {
                return this.mItem.mImeName;
            }
            return null;
        }

        public CharSequence getSubtypeName() {
            if (this.mItem != null) {
                return this.mItem.mSubtypeName;
            }
            return null;
        }
    }

    private InputMethodSubtypeSwitchingControllerEx(InputMethodUtils.InputMethodSettings settings, Context context) {
        this.mController = InputMethodSubtypeSwitchingController.createInstanceLocked(settings, context);
    }

    public static InputMethodSubtypeSwitchingControllerEx createInstanceLocked(InputMethodUtilsEx.InputMethodSettingsEx settings, Context context) {
        return new InputMethodSubtypeSwitchingControllerEx(settings.mSettings, context);
    }

    public List<ImeSubtypeListItemEx> getSortedInputMethodAndSubtypeListLocked(boolean includingAuxiliarySubtypes, boolean isScreenLocked) {
        List<ImeSubtypeListItemEx> list = new ArrayList<>();
        List<InputMethodSubtypeSwitchingController.ImeSubtypeListItem> imList = new ArrayList<>();
        if (this.mController != null) {
            imList = this.mController.getSortedInputMethodAndSubtypeListLocked(includingAuxiliarySubtypes, isScreenLocked);
        }
        int N = imList.size();
        for (int i = 0; i < N; i++) {
            list.add(new ImeSubtypeListItemEx(imList.get(i)));
        }
        return list;
    }
}
