package android.provider;

import android.content.ActivityNotFoundException;
import android.content.ClipDescription;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BrowserContract.History;
import android.rms.iaware.AwareConstant.Database.HwUserData;
import android.security.keymaster.KeymasterDefs;
import android.webkit.WebIconDatabase.IconListener;

public class Browser {
    public static final Uri BOOKMARKS_URI = null;
    public static final String EXTRA_APPLICATION_ID = "com.android.browser.application_id";
    public static final String EXTRA_CREATE_NEW_TAB = "create_new_tab";
    public static final String EXTRA_HEADERS = "com.android.browser.headers";
    public static final String EXTRA_SHARE_FAVICON = "share_favicon";
    public static final String EXTRA_SHARE_SCREENSHOT = "share_screenshot";
    public static final String[] HISTORY_PROJECTION = null;
    public static final int HISTORY_PROJECTION_BOOKMARK_INDEX = 4;
    public static final int HISTORY_PROJECTION_DATE_INDEX = 3;
    public static final int HISTORY_PROJECTION_FAVICON_INDEX = 6;
    public static final int HISTORY_PROJECTION_ID_INDEX = 0;
    public static final int HISTORY_PROJECTION_THUMBNAIL_INDEX = 7;
    public static final int HISTORY_PROJECTION_TITLE_INDEX = 5;
    public static final int HISTORY_PROJECTION_TOUCH_ICON_INDEX = 8;
    public static final int HISTORY_PROJECTION_URL_INDEX = 1;
    public static final int HISTORY_PROJECTION_VISITS_INDEX = 2;
    public static final String INITIAL_ZOOM_LEVEL = "browser.initialZoomLevel";
    private static final String LOGTAG = "browser";
    private static final int MAX_HISTORY_COUNT = 250;
    public static final String[] SEARCHES_PROJECTION = null;
    public static final int SEARCHES_PROJECTION_DATE_INDEX = 2;
    public static final int SEARCHES_PROJECTION_SEARCH_INDEX = 1;
    public static final Uri SEARCHES_URI = null;
    public static final String[] TRUNCATE_HISTORY_PROJECTION = null;
    public static final int TRUNCATE_HISTORY_PROJECTION_ID_INDEX = 0;
    public static final int TRUNCATE_N_OLDEST = 5;

    public static class BookmarkColumns implements BaseColumns {
        public static final String BOOKMARK = "bookmark";
        public static final String CREATED = "created";
        public static final String DATE = "date";
        public static final String FAVICON = "favicon";
        public static final String THUMBNAIL = "thumbnail";
        public static final String TITLE = "title";
        public static final String TOUCH_ICON = "touch_icon";
        public static final String URL = "url";
        public static final String USER_ENTERED = "user_entered";
        public static final String VISITS = "visits";
    }

    public static class SearchColumns implements BaseColumns {
        public static final String DATE = "date";
        public static final String SEARCH = "search";
        @Deprecated
        public static final String URL = "url";
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.provider.Browser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.provider.Browser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.provider.Browser.<clinit>():void");
    }

    public static final void saveBookmark(Context c, String title, String url) {
    }

    public static final void sendString(Context context, String string) {
        sendString(context, string, context.getString(17040305));
    }

    public static final void sendString(Context c, String stringToSend, String chooserDialogTitle) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType(ClipDescription.MIMETYPE_TEXT_PLAIN);
        send.putExtra(Intent.EXTRA_TEXT, stringToSend);
        try {
            Intent i = Intent.createChooser(send, chooserDialogTitle);
            i.setFlags(KeymasterDefs.KM_ENUM);
            c.startActivity(i);
        } catch (ActivityNotFoundException e) {
        }
    }

    public static final Cursor getAllBookmarks(ContentResolver cr) throws IllegalStateException {
        String[] strArr = new String[SEARCHES_PROJECTION_SEARCH_INDEX];
        strArr[TRUNCATE_HISTORY_PROJECTION_ID_INDEX] = ImageMappingColumns.URL;
        return new MatrixCursor(strArr, TRUNCATE_HISTORY_PROJECTION_ID_INDEX);
    }

    public static final Cursor getAllVisitedUrls(ContentResolver cr) throws IllegalStateException {
        String[] strArr = new String[SEARCHES_PROJECTION_SEARCH_INDEX];
        strArr[TRUNCATE_HISTORY_PROJECTION_ID_INDEX] = ImageMappingColumns.URL;
        return new MatrixCursor(strArr, TRUNCATE_HISTORY_PROJECTION_ID_INDEX);
    }

    private static final void addOrUrlEquals(StringBuilder sb) {
        sb.append(" OR url = ");
    }

    private static final Cursor getVisitedLike(ContentResolver cr, String url) {
        StringBuilder whereClause;
        boolean secure = false;
        String compareString = url;
        if (url.startsWith("http://")) {
            compareString = url.substring(HISTORY_PROJECTION_THUMBNAIL_INDEX);
        } else if (url.startsWith("https://")) {
            compareString = url.substring(HISTORY_PROJECTION_TOUCH_ICON_INDEX);
            secure = true;
        }
        if (compareString.startsWith("www.")) {
            compareString = compareString.substring(HISTORY_PROJECTION_BOOKMARK_INDEX);
        }
        if (secure) {
            whereClause = new StringBuilder("url = ");
            DatabaseUtils.appendEscapedSQLString(whereClause, "https://" + compareString);
            addOrUrlEquals(whereClause);
            DatabaseUtils.appendEscapedSQLString(whereClause, "https://www." + compareString);
        } else {
            whereClause = new StringBuilder("url = ");
            DatabaseUtils.appendEscapedSQLString(whereClause, compareString);
            addOrUrlEquals(whereClause);
            String wwwString = "www." + compareString;
            DatabaseUtils.appendEscapedSQLString(whereClause, wwwString);
            addOrUrlEquals(whereClause);
            DatabaseUtils.appendEscapedSQLString(whereClause, "http://" + compareString);
            addOrUrlEquals(whereClause);
            DatabaseUtils.appendEscapedSQLString(whereClause, "http://" + wwwString);
        }
        Uri uri = History.CONTENT_URI;
        String[] strArr = new String[SEARCHES_PROJECTION_DATE_INDEX];
        strArr[TRUNCATE_HISTORY_PROJECTION_ID_INDEX] = HwUserData._ID;
        strArr[SEARCHES_PROJECTION_SEARCH_INDEX] = HistoryColumns.VISITS;
        return cr.query(uri, strArr, whereClause.toString(), null, null);
    }

    public static final void updateVisitedHistory(ContentResolver cr, String url, boolean real) {
    }

    @Deprecated
    public static final String[] getVisitedHistory(ContentResolver cr) {
        return new String[TRUNCATE_HISTORY_PROJECTION_ID_INDEX];
    }

    public static final void truncateHistory(ContentResolver cr) {
    }

    public static final boolean canClearHistory(ContentResolver cr) {
        return false;
    }

    public static final void clearHistory(ContentResolver cr) {
    }

    public static final void deleteHistoryTimeFrame(ContentResolver cr, long begin, long end) {
    }

    public static final void deleteFromHistory(ContentResolver cr, String url) {
    }

    public static final void addSearchUrl(ContentResolver cr, String search) {
    }

    public static final void clearSearches(ContentResolver cr) {
    }

    public static final void requestAllIcons(ContentResolver cr, String where, IconListener listener) {
    }
}
