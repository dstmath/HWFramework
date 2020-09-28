package com.huawei.android.app;

import android.app.DialogFragment;
import android.app.FragmentManager;

public class DialogFragmentEx {
    public static void showAllowingStateLoss(DialogFragment fragment, FragmentManager manager, String tag) {
        fragment.showAllowingStateLoss(manager, tag);
    }
}
