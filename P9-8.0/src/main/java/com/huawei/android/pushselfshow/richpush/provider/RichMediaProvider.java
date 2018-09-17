package com.huawei.android.pushselfshow.richpush.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import com.huawei.android.pushagent.a.a.c;
import com.huawei.android.pushselfshow.utils.a.b;
import com.huawei.systemmanager.rainbow.comm.request.util.RainbowRequestBasic.CheckVersionField;

public class RichMediaProvider extends ContentProvider {
    private static final UriMatcher b = new UriMatcher(-1);
    b a = null;

    public static class a {
        public static final Uri a = Uri.parse("content://com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider/support_porvider");
        public static final Uri b = Uri.parse("content://com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider/insert_bmp");
        public static final Uri c = Uri.parse("content://com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider/update_bmp");
        public static final Uri d = Uri.parse("content://com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider/query_bmp");
        public static final Uri e = Uri.parse("content://com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider/insert_msg");
        public static final Uri f = Uri.parse("content://com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider/query_msg");
        public static final Uri g = Uri.parse("content://com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider/delete_msg");
    }

    static {
        b.addURI("com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider", "support_porvider", 1);
        b.addURI("com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider", "insert_bmp", 2);
        b.addURI("com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider", "update_bmp", 3);
        b.addURI("com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider", "query_bmp", 4);
        b.addURI("com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider", "insert_msg", 5);
        b.addURI("com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider", "query_msg", 6);
        b.addURI("com.huawei.android.pushselfshow.richpush.provider.RichMediaProvider", "delete_msg", 7);
    }

    private Uri a(SQLiteDatabase sQLiteDatabase, String str, ContentValues contentValues, Uri uri) {
        Uri uri2 = null;
        c.a("PushSelfShowLog_RichMediaProvider", "enter insertToDb, table is:" + str);
        if (sQLiteDatabase != null) {
            Cursor cursor = null;
            try {
                cursor = sQLiteDatabase.query(str, null, null, null, null, null, null);
                if (cursor != null) {
                    if (cursor.getCount() < CheckVersionField.CHECK_VERSION_MAX_UPDATE_DAY) {
                        long insert = sQLiteDatabase.insert(str, null, contentValues);
                        if ((insert <= 0 ? 1 : null) == null) {
                            uri2 = ContentUris.withAppendedId(uri, insert);
                            getContext().getContentResolver().notifyChange(uri2, null);
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    sQLiteDatabase.close();
                    c.a("PushSelfShowLog_RichMediaProvider", "resultUri is:" + uri2);
                    return uri2;
                }
                c.d("PushSelfShowLog_RichMediaProvider", "cursor is null");
                if (cursor != null) {
                    cursor.close();
                }
                sQLiteDatabase.close();
                return null;
            } catch (Throwable e) {
                c.d("PushSelfShowLog_RichMediaProvider", e.toString(), e);
                if (cursor != null) {
                    cursor.close();
                }
                sQLiteDatabase.close();
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                sQLiteDatabase.close();
                throw th;
            }
        }
        c.d("PushSelfShowLog_RichMediaProvider", "db is null");
        return null;
    }

    /* JADX WARNING: Missing block: B:2:0x0003, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean a(String str) {
        if (str == null || str.length() == 0 || !str.contains("'")) {
            return false;
        }
        c.d("PushSelfShowLog_RichMediaProvider", str + " can be reject, should check sql");
        return true;
    }

    private boolean a(String[] -l_2_R) {
        if (-l_2_R == null || -l_2_R.length == 0) {
            return false;
        }
        for (String a : -l_2_R) {
            if (a(a)) {
                return true;
            }
        }
        return false;
    }

    public int delete(Uri uri, String str, String[] strArr) {
        int match = b.match(uri);
        c.a("PushSelfShowLog_RichMediaProvider", "uri is:" + uri + ",match result: " + match);
        if (this.a != null) {
            switch (match) {
                case 7:
                    SQLiteDatabase writableDatabase = this.a.getWritableDatabase();
                    if (writableDatabase != null) {
                        int i = 0;
                        try {
                            i = writableDatabase.delete("pushmsg", "_id = ?", strArr);
                            getContext().getContentResolver().notifyChange(uri, null);
                        } catch (Throwable e) {
                            c.d("PushSelfShowLog_RichMediaProvider", e.toString(), e);
                        } finally {
                            writableDatabase.close();
                        }
                        return i;
                    }
                    c.d("PushSelfShowLog_RichMediaProvider", "db is null");
                    return 0;
                default:
                    c.d("PushSelfShowLog_RichMediaProvider", "uri not match!");
                    return 0;
            }
        }
        c.d("PushSelfShowLog_RichMediaProvider", "dbHelper is null");
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        int match = b.match(uri);
        c.a("PushSelfShowLog_RichMediaProvider", "uri is:" + uri + ",match result: " + match);
        if (this.a != null) {
            switch (match) {
                case 2:
                    return a(this.a.getWritableDatabase(), "notify", contentValues, uri);
                case 5:
                    return a(this.a.getWritableDatabase(), "pushmsg", contentValues, uri);
                default:
                    c.d("PushSelfShowLog_RichMediaProvider", "uri not match!");
                    return null;
            }
        }
        c.d("PushSelfShowLog_RichMediaProvider", "dbHelper is null");
        return null;
    }

    public boolean onCreate() {
        c.a("PushSelfShowLog_RichMediaProvider", "onCreate");
        this.a = b.a(getContext());
        return true;
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        if (a(str) || a(strArr)) {
            c.d("PushSelfShowLog_RichMediaProvider", "in query selection:" + str + " or projection is invalied");
            return null;
        }
        int match = b.match(uri);
        c.a("PushSelfShowLog_RichMediaProvider", "uri is:" + uri + ",match result: " + match);
        if (this.a != null) {
            SQLiteDatabase readableDatabase = this.a.getReadableDatabase();
            if (readableDatabase != null) {
                switch (match) {
                    case 1:
                        Cursor matrixCursor = new MatrixCursor(new String[]{"isSupport"});
                        matrixCursor.addRow(new Integer[]{Integer.valueOf(1)});
                        return matrixCursor;
                    case 4:
                        try {
                            return readableDatabase.query("notify", new String[]{"bmp"}, "url = ?", strArr2, null, null, str2, null);
                        } catch (Throwable e) {
                            c.d("PushSelfShowLog_RichMediaProvider", e.toString(), e);
                            break;
                        }
                    case 6:
                        try {
                            return readableDatabase.rawQuery("SELECT pushmsg._id,pushmsg.msg,pushmsg.token,pushmsg.url,notify.bmp  FROM pushmsg LEFT OUTER JOIN notify ON pushmsg.url = notify.url and pushmsg.url = ? order by pushmsg._id desc limit 1000;", strArr2);
                        } catch (Throwable e2) {
                            c.d("PushSelfShowLog_RichMediaProvider", e2.toString(), e2);
                            break;
                        }
                    default:
                        c.d("PushSelfShowLog_RichMediaProvider", "uri not match!");
                        break;
                }
                return null;
            }
            c.d("PushSelfShowLog_RichMediaProvider", "db is null");
            return null;
        }
        c.d("PushSelfShowLog_RichMediaProvider", "dbHelper is null");
        return null;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        int match = b.match(uri);
        c.a("PushSelfShowLog_RichMediaProvider", "uri is:" + uri + ",match result: " + match);
        if (this.a != null) {
            switch (match) {
                case 3:
                    SQLiteDatabase writableDatabase = this.a.getWritableDatabase();
                    if (writableDatabase != null) {
                        int i = 0;
                        try {
                            i = writableDatabase.update("notify", contentValues, "url = ?", strArr);
                            getContext().getContentResolver().notifyChange(uri, null);
                        } catch (Throwable e) {
                            c.d("PushSelfShowLog_RichMediaProvider", e.toString(), e);
                        } finally {
                            writableDatabase.close();
                        }
                        return i;
                    }
                    c.d("PushSelfShowLog_RichMediaProvider", "db is null");
                    return 0;
                default:
                    c.d("PushSelfShowLog_RichMediaProvider", "uri not match!");
                    return 0;
            }
        }
        c.d("PushSelfShowLog_RichMediaProvider", "dbHelper is null");
        return 0;
    }
}
