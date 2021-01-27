package android.app;

import android.common.HwFrameworkFactory;
import android.database.Cursor;
import android.util.Log;
import com.huawei.indexsearch.IIndexSearchManager;

public class HwSearchManager {
    private static final String TAG = "HwSearchManager";

    public static Cursor getHwSuggestions(SearchManager searchManager, SearchableInfo searchable, String query) {
        IIndexSearchManager indexSearchManager;
        if (searchManager == null || searchable == null || query == null || (indexSearchManager = searchManager.getHwIndexSearchManager()) == null || !indexSearchManager.isSupportHwGlobalSearch() || !searchable.getUseFullTextSearch()) {
            return null;
        }
        for (int times = 10; times > 0; times--) {
            try {
                if (indexSearchManager.hasConnected()) {
                    Log.d(TAG, "getHwSuggestions call IndexSearchManager.search().");
                    return indexSearchManager.search(searchable.getSuggestPackage(), query, searchable.getFullTextSearchFields());
                }
                Log.d(TAG, "getHwSuggestions IndexSearchManager not connected.");
                Thread.sleep((long) 10);
            } catch (InterruptedException e) {
                Log.i(TAG, "Thread sleep was interrupted.");
            } catch (RuntimeException e2) {
                Log.i(TAG, "getHwSuggestions has RuntimeException.");
            }
        }
        return null;
    }

    public static void trigerGlobalSearchService(SearchManager searchManager, boolean enabled) {
        IIndexSearchManager indexSearchManager = null;
        if (enabled) {
            Log.i(TAG, "trigerGlobalSearchService enable.");
            indexSearchManager = HwFrameworkFactory.getIndexSearchManager();
            if (indexSearchManager != null) {
                indexSearchManager.connect();
            }
        } else {
            Log.i(TAG, "trigerGlobalSearchService disable.");
            if (searchManager != null) {
                indexSearchManager = searchManager.getHwIndexSearchManager();
            }
            if (indexSearchManager != null) {
                indexSearchManager.destroy();
                indexSearchManager = null;
            }
        }
        if (searchManager != null) {
            searchManager.setHwIndexSearchManager(indexSearchManager);
        }
    }
}
