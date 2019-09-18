package com.huawei.android.app;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.database.Cursor;
import android.os.Bundle;
import java.util.List;

public class SearchManagerEx {
    public static final int GET_SUGGEST_COLUMN_TEXT_3 = 3;
    public static final int GET_SUGGEST_COLUMN_TEXT_4 = 4;

    public static final String getSuggestColumnText(int type) {
        switch (type) {
            case 3:
                return "suggest_text_3";
            case 4:
                return "suggest_text_4";
            default:
                return null;
        }
    }

    public static Cursor getSuggestions(SearchManager searchManager, SearchableInfo searchable, String query, int limit) {
        return searchManager.getSuggestions(searchable, query, limit);
    }

    public static void trigerGlobalSearchService(SearchManager searchManager, boolean enabled) {
        searchManager.trigerGlobalSearchService(enabled);
    }

    public static List<SearchableInfo> getOnlineSearchablesInGlobalSearch(SearchManager searchManager) {
        return searchManager.getOnlineSearchablesInGlobalSearch();
    }

    public static boolean launchLegacyAssist(SearchManager searchManager, String hint, int userHandle, Bundle args) {
        return searchManager.launchLegacyAssist(hint, userHandle, args);
    }
}
