package android.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteProgram;
import android.database.sqlite.SQLiteStatement;
import android.net.NetworkPolicyManager;
import android.net.ProxyInfo;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.OperationCanceledException;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.rms.iaware.AwareConstant.Database.HwUserData;
import android.security.keymaster.KeymasterDefs;
import android.service.notification.NotificationRankerService;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.Collator;
import java.util.HashMap;
import java.util.Locale;

public class DatabaseUtils {
    private static final boolean DEBUG = false;
    private static final char[] DIGITS = null;
    public static final int STATEMENT_ABORT = 6;
    public static final int STATEMENT_ATTACH = 3;
    public static final int STATEMENT_BEGIN = 4;
    public static final int STATEMENT_COMMIT = 5;
    public static final int STATEMENT_DDL = 8;
    public static final int STATEMENT_OTHER = 99;
    public static final int STATEMENT_PRAGMA = 7;
    public static final int STATEMENT_SELECT = 1;
    public static final int STATEMENT_UNPREPARED = 9;
    public static final int STATEMENT_UPDATE = 2;
    private static final String TAG = "DatabaseUtils";
    private static Collator mColl;

    @Deprecated
    public static class InsertHelper {
        public static final int TABLE_INFO_PRAGMA_COLUMNNAME_INDEX = 1;
        public static final int TABLE_INFO_PRAGMA_DEFAULT_INDEX = 4;
        private HashMap<String, Integer> mColumns;
        private final SQLiteDatabase mDb;
        private String mInsertSQL;
        private SQLiteStatement mInsertStatement;
        private SQLiteStatement mPreparedStatement;
        private SQLiteStatement mReplaceStatement;
        private final String mTableName;

        private long insertInternal(android.content.ContentValues r12, boolean r13) {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:18:? in {6, 11, 14, 15, 17, 19, 20} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r11 = this;
            r8 = r11.mDb;
            r8.beginTransactionNonExclusive();
            r5 = r11.getStatement(r13);	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r5.clearBindings();	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r8 = r12.valueSet();	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r2 = r8.iterator();	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
        L_0x0014:
            r8 = r2.hasNext();	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            if (r8 == 0) goto L_0x0062;	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
        L_0x001a:
            r1 = r2.next();	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r1 = (java.util.Map.Entry) r1;	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r4 = r1.getKey();	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r4 = (java.lang.String) r4;	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r3 = r11.getColumnIndex(r4);	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r8 = r1.getValue();	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            android.database.DatabaseUtils.bindObjectToProgram(r5, r3, r8);	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            goto L_0x0014;
        L_0x0032:
            r0 = move-exception;
            r8 = "DatabaseUtils";	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r9 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r9.<init>();	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r10 = "Error inserting ";	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r9 = r9.append(r10);	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r9 = r9.append(r12);	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r10 = " into table  ";	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r9 = r9.append(r10);	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r10 = r11.mTableName;	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r9 = r9.append(r10);	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r9 = r9.toString();	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            android.util.Log.e(r8, r9, r0);	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r8 = -1;
            r10 = r11.mDb;
            r10.endTransaction();
            return r8;
        L_0x0062:
            r6 = r5.executeInsert();	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r8 = r11.mDb;	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r8.setTransactionSuccessful();	 Catch:{ SQLException -> 0x0032, all -> 0x0071 }
            r8 = r11.mDb;
            r8.endTransaction();
            return r6;
        L_0x0071:
            r8 = move-exception;
            r9 = r11.mDb;
            r9.endTransaction();
            throw r8;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.database.DatabaseUtils.InsertHelper.insertInternal(android.content.ContentValues, boolean):long");
        }

        public long execute() {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:15:? in {3, 6, 11, 12, 14, 16, 17} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
            /*
            r5 = this;
            r4 = 0;
            r1 = r5.mPreparedStatement;
            if (r1 != 0) goto L_0x000e;
        L_0x0005:
            r1 = new java.lang.IllegalStateException;
            r2 = "you must prepare this inserter before calling execute";
            r1.<init>(r2);
            throw r1;
        L_0x000e:
            r1 = r5.mPreparedStatement;	 Catch:{ SQLException -> 0x0017, all -> 0x0039 }
            r2 = r1.executeInsert();	 Catch:{ SQLException -> 0x0017, all -> 0x0039 }
            r5.mPreparedStatement = r4;
            return r2;
        L_0x0017:
            r0 = move-exception;
            r1 = "DatabaseUtils";	 Catch:{ SQLException -> 0x0017, all -> 0x0039 }
            r2 = new java.lang.StringBuilder;	 Catch:{ SQLException -> 0x0017, all -> 0x0039 }
            r2.<init>();	 Catch:{ SQLException -> 0x0017, all -> 0x0039 }
            r3 = "Error executing InsertHelper with table ";	 Catch:{ SQLException -> 0x0017, all -> 0x0039 }
            r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0017, all -> 0x0039 }
            r3 = r5.mTableName;	 Catch:{ SQLException -> 0x0017, all -> 0x0039 }
            r2 = r2.append(r3);	 Catch:{ SQLException -> 0x0017, all -> 0x0039 }
            r2 = r2.toString();	 Catch:{ SQLException -> 0x0017, all -> 0x0039 }
            android.util.Log.e(r1, r2, r0);	 Catch:{ SQLException -> 0x0017, all -> 0x0039 }
            r2 = -1;
            r5.mPreparedStatement = r4;
            return r2;
        L_0x0039:
            r1 = move-exception;
            r5.mPreparedStatement = r4;
            throw r1;
            */
            throw new UnsupportedOperationException("Method not decompiled: android.database.DatabaseUtils.InsertHelper.execute():long");
        }

        public InsertHelper(SQLiteDatabase db, String tableName) {
            this.mInsertSQL = null;
            this.mInsertStatement = null;
            this.mReplaceStatement = null;
            this.mPreparedStatement = null;
            this.mDb = db;
            this.mTableName = tableName;
        }

        private void buildSQL() throws SQLException {
            StringBuilder sb = new StringBuilder(KeymasterDefs.KM_ALGORITHM_HMAC);
            sb.append("INSERT INTO ");
            sb.append(this.mTableName);
            sb.append(" (");
            StringBuilder sbv = new StringBuilder(KeymasterDefs.KM_ALGORITHM_HMAC);
            sbv.append("VALUES (");
            int i = TABLE_INFO_PRAGMA_COLUMNNAME_INDEX;
            Cursor cursor = null;
            try {
                cursor = this.mDb.rawQuery("PRAGMA table_info(" + this.mTableName + ")", null);
                this.mColumns = new HashMap(cursor.getCount());
                while (cursor.moveToNext()) {
                    String columnName = cursor.getString(TABLE_INFO_PRAGMA_COLUMNNAME_INDEX);
                    String defaultValue = cursor.getString(TABLE_INFO_PRAGMA_DEFAULT_INDEX);
                    this.mColumns.put(columnName, Integer.valueOf(i));
                    sb.append("'");
                    sb.append(columnName);
                    sb.append("'");
                    if (defaultValue == null) {
                        sbv.append("?");
                    } else {
                        sbv.append("COALESCE(?, ");
                        sbv.append(defaultValue);
                        sbv.append(")");
                    }
                    sb.append(i == cursor.getCount() ? ") " : ", ");
                    sbv.append(i == cursor.getCount() ? ");" : ", ");
                    i += TABLE_INFO_PRAGMA_COLUMNNAME_INDEX;
                }
                sb.append(sbv);
                this.mInsertSQL = sb.toString();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        private SQLiteStatement getStatement(boolean allowReplace) throws SQLException {
            if (allowReplace) {
                if (this.mReplaceStatement == null) {
                    if (this.mInsertSQL == null) {
                        buildSQL();
                    }
                    this.mReplaceStatement = this.mDb.compileStatement("INSERT OR REPLACE" + this.mInsertSQL.substring(DatabaseUtils.STATEMENT_ABORT));
                }
                return this.mReplaceStatement;
            }
            if (this.mInsertStatement == null) {
                if (this.mInsertSQL == null) {
                    buildSQL();
                }
                this.mInsertStatement = this.mDb.compileStatement(this.mInsertSQL);
            }
            return this.mInsertStatement;
        }

        public int getColumnIndex(String key) {
            getStatement(DatabaseUtils.DEBUG);
            Integer index = (Integer) this.mColumns.get(key);
            if (index != null) {
                return index.intValue();
            }
            throw new IllegalArgumentException("column '" + key + "' is invalid");
        }

        public void bind(int index, double value) {
            this.mPreparedStatement.bindDouble(index, value);
        }

        public void bind(int index, float value) {
            this.mPreparedStatement.bindDouble(index, (double) value);
        }

        public void bind(int index, long value) {
            this.mPreparedStatement.bindLong(index, value);
        }

        public void bind(int index, int value) {
            this.mPreparedStatement.bindLong(index, (long) value);
        }

        public void bind(int index, boolean value) {
            this.mPreparedStatement.bindLong(index, (long) (value ? TABLE_INFO_PRAGMA_COLUMNNAME_INDEX : 0));
        }

        public void bindNull(int index) {
            this.mPreparedStatement.bindNull(index);
        }

        public void bind(int index, byte[] value) {
            if (value == null) {
                this.mPreparedStatement.bindNull(index);
            } else {
                this.mPreparedStatement.bindBlob(index, value);
            }
        }

        public void bind(int index, String value) {
            if (value == null) {
                this.mPreparedStatement.bindNull(index);
            } else {
                this.mPreparedStatement.bindString(index, value);
            }
        }

        public long insert(ContentValues values) {
            return insertInternal(values, DatabaseUtils.DEBUG);
        }

        public void prepareForInsert() {
            this.mPreparedStatement = getStatement(DatabaseUtils.DEBUG);
            this.mPreparedStatement.clearBindings();
        }

        public void prepareForReplace() {
            this.mPreparedStatement = getStatement(true);
            this.mPreparedStatement.clearBindings();
        }

        public long replace(ContentValues values) {
            return insertInternal(values, true);
        }

        public void close() {
            if (this.mInsertStatement != null) {
                this.mInsertStatement.close();
                this.mInsertStatement = null;
            }
            if (this.mReplaceStatement != null) {
                this.mReplaceStatement.close();
                this.mReplaceStatement = null;
            }
            this.mInsertSQL = null;
            this.mColumns = null;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.database.DatabaseUtils.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.database.DatabaseUtils.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.database.DatabaseUtils.<clinit>():void");
    }

    public static final void writeExceptionToParcel(Parcel reply, Exception e) {
        int code;
        boolean logException = true;
        if (e instanceof FileNotFoundException) {
            code = STATEMENT_SELECT;
            logException = DEBUG;
        } else if (e instanceof IllegalArgumentException) {
            code = STATEMENT_UPDATE;
        } else if (e instanceof UnsupportedOperationException) {
            code = STATEMENT_ATTACH;
        } else if (e instanceof SQLiteAbortException) {
            code = STATEMENT_BEGIN;
        } else if (e instanceof SQLiteConstraintException) {
            code = STATEMENT_COMMIT;
        } else if (e instanceof SQLiteDatabaseCorruptException) {
            code = STATEMENT_ABORT;
        } else if (e instanceof SQLiteFullException) {
            code = STATEMENT_PRAGMA;
        } else if (e instanceof SQLiteDiskIOException) {
            code = STATEMENT_DDL;
        } else if (e instanceof SQLiteException) {
            code = STATEMENT_UNPREPARED;
        } else if (e instanceof OperationApplicationException) {
            code = 10;
        } else if (e instanceof OperationCanceledException) {
            code = 11;
            logException = DEBUG;
        } else {
            reply.writeException(e);
            Log.e(TAG, "Writing exception to parcel", e);
            return;
        }
        reply.writeInt(code);
        reply.writeString(e.getMessage());
        if (logException) {
            Log.e(TAG, "Writing exception to parcel", e);
        }
    }

    public static final void readExceptionFromParcel(Parcel reply) {
        int code = reply.readExceptionCode();
        if (code != 0) {
            readExceptionFromParcel(reply, reply.readString(), code);
        }
    }

    public static void readExceptionWithFileNotFoundExceptionFromParcel(Parcel reply) throws FileNotFoundException {
        int code = reply.readExceptionCode();
        if (code != 0) {
            String msg = reply.readString();
            if (code == STATEMENT_SELECT) {
                throw new FileNotFoundException(msg);
            }
            readExceptionFromParcel(reply, msg, code);
        }
    }

    public static void readExceptionWithOperationApplicationExceptionFromParcel(Parcel reply) throws OperationApplicationException {
        int code = reply.readExceptionCode();
        if (code != 0) {
            String msg = reply.readString();
            if (code == 10) {
                throw new OperationApplicationException(msg);
            }
            readExceptionFromParcel(reply, msg, code);
        }
    }

    private static final void readExceptionFromParcel(Parcel reply, String msg, int code) {
        switch (code) {
            case STATEMENT_UPDATE /*2*/:
                throw new IllegalArgumentException(msg);
            case STATEMENT_ATTACH /*3*/:
                throw new UnsupportedOperationException(msg);
            case STATEMENT_BEGIN /*4*/:
                throw new SQLiteAbortException(msg);
            case STATEMENT_COMMIT /*5*/:
                throw new SQLiteConstraintException(msg);
            case STATEMENT_ABORT /*6*/:
                throw new SQLiteDatabaseCorruptException(msg);
            case STATEMENT_PRAGMA /*7*/:
                throw new SQLiteFullException(msg);
            case STATEMENT_DDL /*8*/:
                throw new SQLiteDiskIOException(msg);
            case STATEMENT_UNPREPARED /*9*/:
                throw new SQLiteException(msg);
            case NotificationRankerService.REASON_LISTENER_CANCEL_ALL /*11*/:
                throw new OperationCanceledException(msg);
            default:
                reply.readException(code, msg);
        }
    }

    public static void bindObjectToProgram(SQLiteProgram prog, int index, Object value) {
        if (value == null) {
            prog.bindNull(index);
        } else if ((value instanceof Double) || (value instanceof Float)) {
            prog.bindDouble(index, ((Number) value).doubleValue());
        } else if (value instanceof Number) {
            prog.bindLong(index, ((Number) value).longValue());
        } else if (value instanceof Boolean) {
            if (((Boolean) value).booleanValue()) {
                prog.bindLong(index, 1);
            } else {
                prog.bindLong(index, 0);
            }
        } else if (value instanceof byte[]) {
            prog.bindBlob(index, (byte[]) value);
        } else {
            prog.bindString(index, value.toString());
        }
    }

    public static int getTypeOfObject(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof byte[]) {
            return STATEMENT_BEGIN;
        }
        if ((obj instanceof Float) || (obj instanceof Double)) {
            return STATEMENT_UPDATE;
        }
        if ((obj instanceof Long) || (obj instanceof Integer) || (obj instanceof Short) || (obj instanceof Byte)) {
            return STATEMENT_SELECT;
        }
        return STATEMENT_ATTACH;
    }

    public static void cursorFillWindow(Cursor cursor, int position, CursorWindow window) {
        if (position >= 0 && position < cursor.getCount()) {
            int oldPos = cursor.getPosition();
            int numColumns = cursor.getColumnCount();
            window.clear();
            window.setStartPosition(position);
            window.setNumColumns(numColumns);
            if (cursor.moveToPosition(position)) {
                while (window.allocRow()) {
                    int i = 0;
                    while (i < numColumns) {
                        boolean success;
                        switch (cursor.getType(i)) {
                            case TextToSpeech.SUCCESS /*0*/:
                                success = window.putNull(position, i);
                                break;
                            case STATEMENT_SELECT /*1*/:
                                success = window.putLong(cursor.getLong(i), position, i);
                                break;
                            case STATEMENT_UPDATE /*2*/:
                                success = window.putDouble(cursor.getDouble(i), position, i);
                                break;
                            case STATEMENT_BEGIN /*4*/:
                                byte[] value = cursor.getBlob(i);
                                if (value == null) {
                                    success = window.putNull(position, i);
                                    break;
                                } else {
                                    success = window.putBlob(value, position, i);
                                    break;
                                }
                            default:
                                String value2 = cursor.getString(i);
                                if (value2 == null) {
                                    success = window.putNull(position, i);
                                    break;
                                } else {
                                    success = window.putString(value2, position, i);
                                    break;
                                }
                        }
                        if (success) {
                            i += STATEMENT_SELECT;
                        } else {
                            window.freeLastRow();
                        }
                    }
                    position += STATEMENT_SELECT;
                    if (cursor.moveToNext()) {
                    }
                }
            }
            cursor.moveToPosition(oldPos);
        }
    }

    public static void appendEscapedSQLString(StringBuilder sb, String sqlString) {
        sb.append('\'');
        if (sqlString.indexOf(39) != -1) {
            int length = sqlString.length();
            for (int i = 0; i < length; i += STATEMENT_SELECT) {
                char c = sqlString.charAt(i);
                if (c == '\'') {
                    sb.append('\'');
                }
                sb.append(c);
            }
        } else {
            sb.append(sqlString);
        }
        sb.append('\'');
    }

    public static String sqlEscapeString(String value) {
        StringBuilder escaper = new StringBuilder();
        appendEscapedSQLString(escaper, value);
        return escaper.toString();
    }

    public static final void appendValueToSql(StringBuilder sql, Object value) {
        if (value == null) {
            sql.append(WifiEnterpriseConfig.EMPTY_VALUE);
        } else if (!(value instanceof Boolean)) {
            appendEscapedSQLString(sql, value.toString());
        } else if (((Boolean) value).booleanValue()) {
            sql.append('1');
        } else {
            sql.append('0');
        }
    }

    public static String concatenateWhere(String a, String b) {
        if (TextUtils.isEmpty(a)) {
            return b;
        }
        if (TextUtils.isEmpty(b)) {
            return a;
        }
        return "(" + a + ") AND (" + b + ")";
    }

    public static String getCollationKey(String name) {
        byte[] arr = getCollationKeyInBytes(name);
        try {
            return new String(arr, 0, getKeyLen(arr), "ISO8859_1");
        } catch (Exception e) {
            return ProxyInfo.LOCAL_EXCL_LIST;
        }
    }

    public static String getHexCollationKey(String name) {
        byte[] arr = getCollationKeyInBytes(name);
        return new String(encodeHex(arr), 0, getKeyLen(arr) * STATEMENT_UPDATE);
    }

    private static char[] encodeHex(byte[] input) {
        int l = input.length;
        char[] out = new char[(l << STATEMENT_SELECT)];
        int j = 0;
        for (int i = 0; i < l; i += STATEMENT_SELECT) {
            int i2 = j + STATEMENT_SELECT;
            out[j] = DIGITS[(input[i] & NetworkPolicyManager.MASK_ALL_NETWORKS) >>> STATEMENT_BEGIN];
            j = i2 + STATEMENT_SELECT;
            out[i2] = DIGITS[input[i] & 15];
        }
        return out;
    }

    private static int getKeyLen(byte[] arr) {
        if (arr[arr.length - 1] != null) {
            return arr.length;
        }
        return arr.length - 1;
    }

    private static byte[] getCollationKeyInBytes(String name) {
        if (mColl == null) {
            mColl = Collator.getInstance();
            mColl.setStrength(0);
        }
        return mColl.getCollationKey(name).toByteArray();
    }

    public static void dumpCursor(Cursor cursor) {
        dumpCursor(cursor, System.out);
    }

    public static void dumpCursor(Cursor cursor, PrintStream stream) {
        stream.println(">>>>> Dumping cursor " + cursor);
        if (cursor != null) {
            int startPos = cursor.getPosition();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                dumpCurrentRow(cursor, stream);
            }
            cursor.moveToPosition(startPos);
        }
        stream.println("<<<<<");
    }

    public static void dumpCursor(Cursor cursor, StringBuilder sb) {
        sb.append(">>>>> Dumping cursor ").append(cursor).append("\n");
        if (cursor != null) {
            int startPos = cursor.getPosition();
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                dumpCurrentRow(cursor, sb);
            }
            cursor.moveToPosition(startPos);
        }
        sb.append("<<<<<\n");
    }

    public static String dumpCursorToString(Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        dumpCursor(cursor, sb);
        return sb.toString();
    }

    public static void dumpCurrentRow(Cursor cursor) {
        dumpCurrentRow(cursor, System.out);
    }

    public static void dumpCurrentRow(Cursor cursor, PrintStream stream) {
        String[] cols = cursor.getColumnNames();
        stream.println(ProxyInfo.LOCAL_EXCL_LIST + cursor.getPosition() + " {");
        int length = cols.length;
        for (int i = 0; i < length; i += STATEMENT_SELECT) {
            String value;
            try {
                value = cursor.getString(i);
            } catch (SQLiteException e) {
                value = "<unprintable>";
            }
            stream.println("   " + cols[i] + '=' + value);
        }
        stream.println("}");
    }

    public static void dumpCurrentRow(Cursor cursor, StringBuilder sb) {
        String[] cols = cursor.getColumnNames();
        sb.append(ProxyInfo.LOCAL_EXCL_LIST).append(cursor.getPosition()).append(" {\n");
        int length = cols.length;
        for (int i = 0; i < length; i += STATEMENT_SELECT) {
            String value;
            try {
                value = cursor.getString(i);
            } catch (SQLiteException e) {
                value = "<unprintable>";
            }
            sb.append("   ").append(cols[i]).append('=').append(value).append("\n");
        }
        sb.append("}\n");
    }

    public static String dumpCurrentRowToString(Cursor cursor) {
        StringBuilder sb = new StringBuilder();
        dumpCurrentRow(cursor, sb);
        return sb.toString();
    }

    public static void cursorStringToContentValues(Cursor cursor, String field, ContentValues values) {
        cursorStringToContentValues(cursor, field, values, field);
    }

    public static void cursorStringToInsertHelper(Cursor cursor, String field, InsertHelper inserter, int index) {
        inserter.bind(index, cursor.getString(cursor.getColumnIndexOrThrow(field)));
    }

    public static void cursorStringToContentValues(Cursor cursor, String field, ContentValues values, String key) {
        values.put(key, cursor.getString(cursor.getColumnIndexOrThrow(field)));
    }

    public static void cursorIntToContentValues(Cursor cursor, String field, ContentValues values) {
        cursorIntToContentValues(cursor, field, values, field);
    }

    public static void cursorIntToContentValues(Cursor cursor, String field, ContentValues values, String key) {
        int colIndex = cursor.getColumnIndex(field);
        if (cursor.isNull(colIndex)) {
            values.put(key, (Integer) null);
        } else {
            values.put(key, Integer.valueOf(cursor.getInt(colIndex)));
        }
    }

    public static void cursorLongToContentValues(Cursor cursor, String field, ContentValues values) {
        cursorLongToContentValues(cursor, field, values, field);
    }

    public static void cursorLongToContentValues(Cursor cursor, String field, ContentValues values, String key) {
        int colIndex = cursor.getColumnIndex(field);
        if (cursor.isNull(colIndex)) {
            values.put(key, (Long) null);
        } else {
            values.put(key, Long.valueOf(cursor.getLong(colIndex)));
        }
    }

    public static void cursorDoubleToCursorValues(Cursor cursor, String field, ContentValues values) {
        cursorDoubleToContentValues(cursor, field, values, field);
    }

    public static void cursorDoubleToContentValues(Cursor cursor, String field, ContentValues values, String key) {
        int colIndex = cursor.getColumnIndex(field);
        if (cursor.isNull(colIndex)) {
            values.put(key, (Double) null);
        } else {
            values.put(key, Double.valueOf(cursor.getDouble(colIndex)));
        }
    }

    public static void cursorRowToContentValues(Cursor cursor, ContentValues values) {
        AbstractWindowedCursor abstractWindowedCursor = cursor instanceof AbstractWindowedCursor ? (AbstractWindowedCursor) cursor : null;
        String[] columns = cursor.getColumnNames();
        int length = columns.length;
        int i = 0;
        while (i < length) {
            if (abstractWindowedCursor == null || !abstractWindowedCursor.isBlob(i)) {
                values.put(columns[i], cursor.getString(i));
            } else {
                values.put(columns[i], cursor.getBlob(i));
            }
            i += STATEMENT_SELECT;
        }
    }

    public static int cursorPickFillWindowStartPosition(int cursorPosition, int cursorWindowCapacity) {
        return Math.max(cursorPosition - (cursorWindowCapacity / STATEMENT_ATTACH), 0);
    }

    public static long queryNumEntries(SQLiteDatabase db, String table) {
        return queryNumEntries(db, table, null, null);
    }

    public static long queryNumEntries(SQLiteDatabase db, String table, String selection) {
        return queryNumEntries(db, table, selection, null);
    }

    public static long queryNumEntries(SQLiteDatabase db, String table, String selection, String[] selectionArgs) {
        return longForQuery(db, "select count(*) from " + table + (!TextUtils.isEmpty(selection) ? " where " + selection : ProxyInfo.LOCAL_EXCL_LIST), selectionArgs);
    }

    public static boolean queryIsEmpty(SQLiteDatabase db, String table) {
        return longForQuery(db, new StringBuilder().append("select exists(select 1 from ").append(table).append(")").toString(), null) == 0 ? true : DEBUG;
    }

    public static long longForQuery(SQLiteDatabase db, String query, String[] selectionArgs) {
        SQLiteStatement prog = db.compileStatement(query);
        try {
            long longForQuery = longForQuery(prog, selectionArgs);
            return longForQuery;
        } finally {
            prog.close();
        }
    }

    public static long longForQuery(SQLiteStatement prog, String[] selectionArgs) {
        prog.bindAllArgsAsStrings(selectionArgs);
        return prog.simpleQueryForLong();
    }

    public static String stringForQuery(SQLiteDatabase db, String query, String[] selectionArgs) {
        SQLiteStatement prog = db.compileStatement(query);
        try {
            String stringForQuery = stringForQuery(prog, selectionArgs);
            return stringForQuery;
        } finally {
            prog.close();
        }
    }

    public static String stringForQuery(SQLiteStatement prog, String[] selectionArgs) {
        prog.bindAllArgsAsStrings(selectionArgs);
        return prog.simpleQueryForString();
    }

    public static ParcelFileDescriptor blobFileDescriptorForQuery(SQLiteDatabase db, String query, String[] selectionArgs) {
        SQLiteStatement prog = db.compileStatement(query);
        try {
            ParcelFileDescriptor blobFileDescriptorForQuery = blobFileDescriptorForQuery(prog, selectionArgs);
            return blobFileDescriptorForQuery;
        } finally {
            prog.close();
        }
    }

    public static ParcelFileDescriptor blobFileDescriptorForQuery(SQLiteStatement prog, String[] selectionArgs) {
        prog.bindAllArgsAsStrings(selectionArgs);
        return prog.simpleQueryForBlobFileDescriptor();
    }

    public static void cursorStringToContentValuesIfPresent(Cursor cursor, ContentValues values, String column) {
        int index = cursor.getColumnIndex(column);
        if (index != -1 && !cursor.isNull(index)) {
            values.put(column, cursor.getString(index));
        }
    }

    public static void cursorLongToContentValuesIfPresent(Cursor cursor, ContentValues values, String column) {
        int index = cursor.getColumnIndex(column);
        if (index != -1 && !cursor.isNull(index)) {
            values.put(column, Long.valueOf(cursor.getLong(index)));
        }
    }

    public static void cursorShortToContentValuesIfPresent(Cursor cursor, ContentValues values, String column) {
        int index = cursor.getColumnIndex(column);
        if (index != -1 && !cursor.isNull(index)) {
            values.put(column, Short.valueOf(cursor.getShort(index)));
        }
    }

    public static void cursorIntToContentValuesIfPresent(Cursor cursor, ContentValues values, String column) {
        int index = cursor.getColumnIndex(column);
        if (index != -1 && !cursor.isNull(index)) {
            values.put(column, Integer.valueOf(cursor.getInt(index)));
        }
    }

    public static void cursorFloatToContentValuesIfPresent(Cursor cursor, ContentValues values, String column) {
        int index = cursor.getColumnIndex(column);
        if (index != -1 && !cursor.isNull(index)) {
            values.put(column, Float.valueOf(cursor.getFloat(index)));
        }
    }

    public static void cursorDoubleToContentValuesIfPresent(Cursor cursor, ContentValues values, String column) {
        int index = cursor.getColumnIndex(column);
        if (index != -1 && !cursor.isNull(index)) {
            values.put(column, Double.valueOf(cursor.getDouble(index)));
        }
    }

    public static void createDbFromSqlStatements(Context context, String dbName, int dbVersion, String sqlStatements) {
        int i = 0;
        SQLiteDatabase db = context.openOrCreateDatabase(dbName, 0, null);
        String[] statements = TextUtils.split(sqlStatements, ";\n");
        int length = statements.length;
        while (i < length) {
            String statement = statements[i];
            if (!TextUtils.isEmpty(statement)) {
                db.execSQL(statement);
            }
            i += STATEMENT_SELECT;
        }
        db.setVersion(dbVersion);
        db.close();
    }

    public static int getSqlStatementType(String sql) {
        if (sql == null) {
            return STATEMENT_OTHER;
        }
        sql = sql.trim();
        if (sql.length() < STATEMENT_ATTACH) {
            return STATEMENT_OTHER;
        }
        String prefixSql = sql.substring(0, STATEMENT_ATTACH).toUpperCase(Locale.ROOT);
        if (prefixSql.equals("SEL")) {
            return STATEMENT_SELECT;
        }
        if (prefixSql.equals("INS") || prefixSql.equals("UPD") || prefixSql.equals("REP") || prefixSql.equals("DEL")) {
            return STATEMENT_UPDATE;
        }
        if (prefixSql.equals("ATT")) {
            return STATEMENT_ATTACH;
        }
        if (prefixSql.equals("COM") || prefixSql.equals("END")) {
            return STATEMENT_COMMIT;
        }
        if (prefixSql.equals("ROL")) {
            return STATEMENT_ABORT;
        }
        if (prefixSql.equals("BEG")) {
            return STATEMENT_BEGIN;
        }
        if (prefixSql.equals("PRA")) {
            return STATEMENT_PRAGMA;
        }
        if (prefixSql.equals("CRE") || prefixSql.equals("DRO") || prefixSql.equals("ALT")) {
            return STATEMENT_DDL;
        }
        if (prefixSql.equals("ANA") || prefixSql.equals("DET")) {
            return STATEMENT_UNPREPARED;
        }
        return STATEMENT_OTHER;
    }

    public static String[] appendSelectionArgs(String[] originalValues, String[] newValues) {
        if (originalValues == null || originalValues.length == 0) {
            return newValues;
        }
        String[] result = new String[(originalValues.length + newValues.length)];
        System.arraycopy(originalValues, 0, result, 0, originalValues.length);
        System.arraycopy(newValues, 0, result, originalValues.length, newValues.length);
        return result;
    }

    public static int findRowIdColumnIndex(String[] columnNames) {
        int length = columnNames.length;
        for (int i = 0; i < length; i += STATEMENT_SELECT) {
            if (columnNames[i].equals(HwUserData._ID)) {
                return i;
            }
        }
        return -1;
    }
}
