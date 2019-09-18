package com.huawei.android.preference;

import android.preference.PreferenceFragment;
import android.widget.ListView;

public class PreferenceFragmentEx {
    public static ListView getListView(PreferenceFragment fragment) {
        return fragment.getListView();
    }
}
