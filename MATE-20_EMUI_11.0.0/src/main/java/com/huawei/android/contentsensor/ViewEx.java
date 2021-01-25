package com.huawei.android.contentsensor;

import android.app.AppGlobals;
import android.app.Application;
import android.view.View;
import android.view.ViewGroup;

public class ViewEx {
    public static Application getInitialApplication() {
        return AppGlobals.getInitialApplication();
    }

    public static View findViewInGroup(ViewGroup viewGroup, int index, int childCount) {
        return viewGroup.dispatchFindView(index, childCount);
    }

    public static CharSequence getViewIterableTextForAccessibility(View view) {
        return view.getIterableTextForAccessibility();
    }
}
