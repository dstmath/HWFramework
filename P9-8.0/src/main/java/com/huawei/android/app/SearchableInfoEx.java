package com.huawei.android.app;

import android.app.SearchableInfo;
import android.content.Context;

public class SearchableInfoEx {
    public static Context getActivityContext(SearchableInfo searchableInfo, Context context) {
        return searchableInfo.getActivityContext(context);
    }

    public static int getLabelId(SearchableInfo searchableInfo) {
        return searchableInfo.getLabelId();
    }

    public static int getIconId(SearchableInfo searchableInfo) {
        return searchableInfo.getIconId();
    }

    public static String getLayoutType(SearchableInfo searchableInfo) {
        return searchableInfo.getLayoutType();
    }

    public static String getSearchSuggestOthersIntent(SearchableInfo searchableInfo) {
        return searchableInfo.getSearchSuggestOthersIntent();
    }
}
