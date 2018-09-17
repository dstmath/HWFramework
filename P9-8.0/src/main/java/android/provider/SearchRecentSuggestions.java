package android.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import java.util.concurrent.Semaphore;

public class SearchRecentSuggestions {
    private static final String LOG_TAG = "SearchSuggestions";
    private static final int MAX_HISTORY_COUNT = 250;
    public static final String[] QUERIES_PROJECTION_1LINE = new String[]{"_id", "date", SuggestionColumns.QUERY, SuggestionColumns.DISPLAY1};
    public static final String[] QUERIES_PROJECTION_2LINE = new String[]{"_id", "date", SuggestionColumns.QUERY, SuggestionColumns.DISPLAY1, SuggestionColumns.DISPLAY2};
    public static final int QUERIES_PROJECTION_DATE_INDEX = 1;
    public static final int QUERIES_PROJECTION_DISPLAY1_INDEX = 3;
    public static final int QUERIES_PROJECTION_DISPLAY2_INDEX = 4;
    public static final int QUERIES_PROJECTION_QUERY_INDEX = 2;
    private static final Semaphore sWritesInProgress = new Semaphore(0);
    private final String mAuthority;
    private final Context mContext;
    private final Uri mSuggestionsUri;
    private final boolean mTwoLineDisplay;

    private static class SuggestionColumns implements BaseColumns {
        public static final String DATE = "date";
        public static final String DISPLAY1 = "display1";
        public static final String DISPLAY2 = "display2";
        public static final String QUERY = "query";

        private SuggestionColumns() {
        }
    }

    public SearchRecentSuggestions(Context context, String authority, int mode) {
        boolean z = false;
        if (TextUtils.isEmpty(authority) || (mode & 1) == 0) {
            throw new IllegalArgumentException();
        }
        if ((mode & 2) != 0) {
            z = true;
        }
        this.mTwoLineDisplay = z;
        this.mContext = context;
        this.mAuthority = new String(authority);
        this.mSuggestionsUri = Uri.parse("content://" + this.mAuthority + "/suggestions");
    }

    public void saveRecentQuery(final String queryString, final String line2) {
        if (!TextUtils.isEmpty(queryString)) {
            if (this.mTwoLineDisplay || (TextUtils.isEmpty(line2) ^ 1) == 0) {
                new Thread("saveRecentQuery") {
                    public void run() {
                        SearchRecentSuggestions.this.saveRecentQueryBlocking(queryString, line2);
                        SearchRecentSuggestions.sWritesInProgress.release();
                    }
                }.start();
                return;
            }
            throw new IllegalArgumentException();
        }
    }

    void waitForSave() {
        do {
            sWritesInProgress.acquireUninterruptibly();
        } while (sWritesInProgress.availablePermits() > 0);
    }

    private void saveRecentQueryBlocking(String queryString, String line2) {
        ContentResolver cr = this.mContext.getContentResolver();
        long now = System.currentTimeMillis();
        try {
            ContentValues values = new ContentValues();
            values.put(SuggestionColumns.DISPLAY1, queryString);
            if (this.mTwoLineDisplay) {
                values.put(SuggestionColumns.DISPLAY2, line2);
            }
            values.put(SuggestionColumns.QUERY, queryString);
            values.put("date", Long.valueOf(now));
            cr.insert(this.mSuggestionsUri, values);
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "saveRecentQuery", e);
        }
        truncateHistory(cr, 250);
    }

    public void clearHistory() {
        truncateHistory(this.mContext.getContentResolver(), 0);
    }

    protected void truncateHistory(ContentResolver cr, int maxEntries) {
        if (maxEntries < 0) {
            throw new IllegalArgumentException();
        }
        String selection = null;
        if (maxEntries > 0) {
            try {
                selection = "_id IN (SELECT _id FROM suggestions ORDER BY date DESC LIMIT -1 OFFSET " + String.valueOf(maxEntries) + ")";
            } catch (RuntimeException e) {
                Log.e(LOG_TAG, "truncateHistory", e);
                return;
            }
        }
        cr.delete(this.mSuggestionsUri, selection, null);
    }
}
