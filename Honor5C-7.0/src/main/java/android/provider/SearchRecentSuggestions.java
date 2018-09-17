package android.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.VoicemailContract.Voicemails;
import android.text.TextUtils;
import android.util.Log;
import java.util.concurrent.Semaphore;

public class SearchRecentSuggestions {
    private static final String LOG_TAG = "SearchSuggestions";
    private static final int MAX_HISTORY_COUNT = 250;
    public static final String[] QUERIES_PROJECTION_1LINE = null;
    public static final String[] QUERIES_PROJECTION_2LINE = null;
    public static final int QUERIES_PROJECTION_DATE_INDEX = 1;
    public static final int QUERIES_PROJECTION_DISPLAY1_INDEX = 3;
    public static final int QUERIES_PROJECTION_DISPLAY2_INDEX = 4;
    public static final int QUERIES_PROJECTION_QUERY_INDEX = 2;
    private static final Semaphore sWritesInProgress = null;
    private final String mAuthority;
    private final Context mContext;
    private final Uri mSuggestionsUri;
    private final boolean mTwoLineDisplay;

    /* renamed from: android.provider.SearchRecentSuggestions.1 */
    class AnonymousClass1 extends Thread {
        final /* synthetic */ String val$line2;
        final /* synthetic */ String val$queryString;

        AnonymousClass1(String $anonymous0, String val$queryString, String val$line2) {
            this.val$queryString = val$queryString;
            this.val$line2 = val$line2;
            super($anonymous0);
        }

        public void run() {
            SearchRecentSuggestions.this.saveRecentQueryBlocking(this.val$queryString, this.val$line2);
            SearchRecentSuggestions.sWritesInProgress.release();
        }
    }

    private static class SuggestionColumns implements BaseColumns {
        public static final String DATE = "date";
        public static final String DISPLAY1 = "display1";
        public static final String DISPLAY2 = "display2";
        public static final String QUERY = "query";

        private SuggestionColumns() {
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.SearchRecentSuggestions.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.SearchRecentSuggestions.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.SearchRecentSuggestions.<clinit>():void");
    }

    public SearchRecentSuggestions(Context context, String authority, int mode) {
        boolean z = false;
        if (TextUtils.isEmpty(authority) || (mode & QUERIES_PROJECTION_DATE_INDEX) == 0) {
            throw new IllegalArgumentException();
        }
        if ((mode & QUERIES_PROJECTION_QUERY_INDEX) != 0) {
            z = true;
        }
        this.mTwoLineDisplay = z;
        this.mContext = context;
        this.mAuthority = new String(authority);
        this.mSuggestionsUri = Uri.parse("content://" + this.mAuthority + "/suggestions");
    }

    public void saveRecentQuery(String queryString, String line2) {
        if (!TextUtils.isEmpty(queryString)) {
            if (this.mTwoLineDisplay || TextUtils.isEmpty(line2)) {
                new AnonymousClass1("saveRecentQuery", queryString, line2).start();
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
            values.put(Voicemails.DATE, Long.valueOf(now));
            cr.insert(this.mSuggestionsUri, values);
        } catch (RuntimeException e) {
            Log.e(LOG_TAG, "saveRecentQuery", e);
        }
        truncateHistory(cr, MAX_HISTORY_COUNT);
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
