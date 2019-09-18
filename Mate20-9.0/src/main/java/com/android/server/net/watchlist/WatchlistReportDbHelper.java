package com.android.server.net.watchlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import com.android.internal.util.HexDump;
import com.android.server.net.watchlist.WatchlistLoggingHandler;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class WatchlistReportDbHelper extends SQLiteOpenHelper {
    private static final String CREATE_TABLE_MODEL = "CREATE TABLE records(app_digest BLOB,cnc_domain TEXT,timestamp INTEGER DEFAULT 0 )";
    private static final String[] DIGEST_DOMAIN_PROJECTION = {"app_digest", "cnc_domain"};
    private static final int IDLE_CONNECTION_TIMEOUT_MS = 30000;
    private static final int INDEX_CNC_DOMAIN = 1;
    private static final int INDEX_DIGEST = 0;
    private static final int INDEX_TIMESTAMP = 2;
    private static final String NAME = "watchlist_report.db";
    private static final String TAG = "WatchlistReportDbHelper";
    private static final int VERSION = 2;
    private static WatchlistReportDbHelper sInstance;

    public static class AggregatedResult {
        final HashMap<String, String> appDigestCNCList;
        final Set<String> appDigestList;
        final String cncDomainVisited;

        public AggregatedResult(Set<String> appDigestList2, String cncDomainVisited2, HashMap<String, String> appDigestCNCList2) {
            this.appDigestList = appDigestList2;
            this.cncDomainVisited = cncDomainVisited2;
            this.appDigestCNCList = appDigestCNCList2;
        }
    }

    private static class WhiteListReportContract {
        private static final String APP_DIGEST = "app_digest";
        private static final String CNC_DOMAIN = "cnc_domain";
        private static final String TABLE = "records";
        private static final String TIMESTAMP = "timestamp";

        private WhiteListReportContract() {
        }
    }

    static File getSystemWatchlistDbFile() {
        return new File(Environment.getDataSystemDirectory(), NAME);
    }

    private WatchlistReportDbHelper(Context context) {
        super(context, getSystemWatchlistDbFile().getAbsolutePath(), null, 2);
        setIdleConnectionTimeout(30000);
    }

    public static synchronized WatchlistReportDbHelper getInstance(Context context) {
        synchronized (WatchlistReportDbHelper.class) {
            if (sInstance != null) {
                WatchlistReportDbHelper watchlistReportDbHelper = sInstance;
                return watchlistReportDbHelper;
            }
            sInstance = new WatchlistReportDbHelper(context);
            WatchlistReportDbHelper watchlistReportDbHelper2 = sInstance;
            return watchlistReportDbHelper2;
        }
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MODEL);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS records");
        onCreate(db);
    }

    public boolean insertNewRecord(byte[] appDigest, String cncDomain, long timestamp) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("app_digest", appDigest);
        values.put("cnc_domain", cncDomain);
        values.put(WatchlistLoggingHandler.WatchlistEventKeys.TIMESTAMP, Long.valueOf(timestamp));
        return db.insert("records", null, values) != -1;
    }

    public AggregatedResult getAggregatedRecords(long untilTimestamp) {
        String cncDomainVisited = null;
        Cursor c = null;
        try {
            c = getReadableDatabase().query(true, "records", DIGEST_DOMAIN_PROJECTION, "timestamp < ?", new String[]{Long.toString(untilTimestamp)}, null, null, null, null);
            if (c == null) {
                if (c != null) {
                    c.close();
                }
                return null;
            }
            HashSet<String> appDigestList = new HashSet<>();
            HashMap<String, String> appDigestCNCList = new HashMap<>();
            while (c.moveToNext()) {
                String digestHexStr = HexDump.toHexString(c.getBlob(0));
                String cncDomain = c.getString(1);
                appDigestList.add(digestHexStr);
                if (cncDomainVisited != null) {
                    cncDomainVisited = cncDomain;
                }
                appDigestCNCList.put(digestHexStr, cncDomain);
            }
            return new AggregatedResult(appDigestList, cncDomainVisited, appDigestCNCList);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public boolean cleanup(long untilTimestamp) {
        SQLiteDatabase db = getWritableDatabase();
        StringBuilder sb = new StringBuilder();
        sb.append("timestamp< ");
        sb.append(untilTimestamp);
        return db.delete("records", sb.toString(), null) != 0;
    }
}
