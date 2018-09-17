package com.android.server.location;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class HwLocalLocationDBHelper extends SQLiteOpenHelper {
    static final byte[] C3 = null;
    private static final String TAG = "HwLocalLocationProvider";
    private SQLiteDatabase mSqLiteDatabase;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.location.HwLocalLocationDBHelper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.location.HwLocalLocationDBHelper.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.location.HwLocalLocationDBHelper.<clinit>():void");
    }

    public HwLocalLocationDBHelper(Context context) {
        super(context, HwLocalLocationManager.LOCATION_DB_NAME, null, 2);
    }

    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(HwLocalLocationManager.CREATE_CELLID_TABLE);
            db.execSQL(HwLocalLocationManager.CREATE_BSSID_TABLE);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
        Log.i(TAG, "Path = " + db.getPath());
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS cell_fix_info");
            db.execSQL("DROP TABLE IF EXISTS bssid_fix_info");
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
        onCreate(db);
    }

    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    public long insert(String table, ContentValues values) {
        this.mSqLiteDatabase = getWritableDatabase();
        return this.mSqLiteDatabase.insert(table, null, values);
    }

    public int delete(String table, String whereClause, String[] whereArgs) {
        this.mSqLiteDatabase = getWritableDatabase();
        return this.mSqLiteDatabase.delete(table, whereClause, whereArgs);
    }

    public void execSQL(String sql) {
        this.mSqLiteDatabase = getWritableDatabase();
        try {
            this.mSqLiteDatabase.execSQL(sql);
        } catch (SQLException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        this.mSqLiteDatabase = getWritableDatabase();
        return this.mSqLiteDatabase.update(table, values, whereClause, whereArgs);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        this.mSqLiteDatabase = getReadableDatabase();
        return this.mSqLiteDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs) {
        this.mSqLiteDatabase = getReadableDatabase();
        return this.mSqLiteDatabase.query(table, columns, selection, selectionArgs, null, null, null, null);
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        this.mSqLiteDatabase = getReadableDatabase();
        return this.mSqLiteDatabase.rawQuery(sql, selectionArgs);
    }

    public void closedb() {
        if (this.mSqLiteDatabase != null && this.mSqLiteDatabase.isOpen()) {
            this.mSqLiteDatabase.close();
        }
    }

    public void beginTransaction() {
        this.mSqLiteDatabase = getWritableDatabase();
        this.mSqLiteDatabase.beginTransaction();
    }

    public void setTransactionSuccessful() {
        this.mSqLiteDatabase = getWritableDatabase();
        this.mSqLiteDatabase.setTransactionSuccessful();
    }

    public void endTransaction() {
        this.mSqLiteDatabase = getWritableDatabase();
        this.mSqLiteDatabase.endTransaction();
    }
}
