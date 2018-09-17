package android.database.sqlite;

import android.database.DatabaseUtils;
import android.os.CancellationSignal;
import android.speech.SpeechRecognizer;
import android.telecom.AudioState;
import java.util.Arrays;

public abstract class SQLiteProgram extends SQLiteClosable {
    private static final String[] EMPTY_STRING_ARRAY = null;
    private final Object[] mBindArgs;
    private final String[] mColumnNames;
    private final SQLiteDatabase mDatabase;
    private final int mNumParameters;
    private final boolean mReadOnly;
    private final String mSql;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.database.sqlite.SQLiteProgram.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.database.sqlite.SQLiteProgram.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.database.sqlite.SQLiteProgram.<clinit>():void");
    }

    SQLiteProgram(SQLiteDatabase db, String sql, Object[] bindArgs, CancellationSignal cancellationSignalForPrepare) {
        this.mDatabase = db;
        if (sql != null) {
            this.mSql = sql.trim();
        } else {
            this.mSql = null;
        }
        int n = DatabaseUtils.getSqlStatementType(this.mSql);
        switch (n) {
            case AudioState.ROUTE_WIRED_HEADSET /*4*/:
            case AudioState.ROUTE_WIRED_OR_EARPIECE /*5*/:
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT /*6*/:
                this.mReadOnly = false;
                this.mColumnNames = EMPTY_STRING_ARRAY;
                this.mNumParameters = 0;
                break;
            default:
                boolean assumeReadOnly = n == 1;
                SQLiteStatementInfo info = new SQLiteStatementInfo();
                db.getThreadSession().prepare(this.mSql, db.getThreadDefaultConnectionFlags(assumeReadOnly), cancellationSignalForPrepare, info);
                this.mReadOnly = info.readOnly;
                this.mColumnNames = info.columnNames;
                this.mNumParameters = info.numParameters;
                break;
        }
        if (bindArgs != null && bindArgs.length > this.mNumParameters) {
            throw new IllegalArgumentException("Too many bind arguments.  " + bindArgs.length + " arguments were provided but the statement needs " + this.mNumParameters + " arguments.");
        } else if (this.mNumParameters != 0) {
            this.mBindArgs = new Object[this.mNumParameters];
            if (bindArgs != null) {
                System.arraycopy(bindArgs, 0, this.mBindArgs, 0, bindArgs.length);
            }
        } else {
            this.mBindArgs = null;
        }
    }

    final SQLiteDatabase getDatabase() {
        return this.mDatabase;
    }

    final String getSql() {
        return this.mSql;
    }

    final Object[] getBindArgs() {
        return this.mBindArgs;
    }

    final String[] getColumnNames() {
        return this.mColumnNames;
    }

    protected final SQLiteSession getSession() {
        return this.mDatabase.getThreadSession();
    }

    protected final int getConnectionFlags() {
        return this.mDatabase.getThreadDefaultConnectionFlags(this.mReadOnly);
    }

    protected final void onCorruption() {
        this.mDatabase.onCorruption();
    }

    @Deprecated
    public final int getUniqueId() {
        return -1;
    }

    public void bindNull(int index) {
        bind(index, null);
    }

    public void bindLong(int index, long value) {
        bind(index, Long.valueOf(value));
    }

    public void bindDouble(int index, double value) {
        bind(index, Double.valueOf(value));
    }

    public void bindString(int index, String value) {
        if (value == null) {
            throw new IllegalArgumentException("the bind value at index " + index + " is null");
        }
        bind(index, value);
    }

    public void bindBlob(int index, byte[] value) {
        if (value == null) {
            throw new IllegalArgumentException("the bind value at index " + index + " is null");
        }
        bind(index, value);
    }

    public void clearBindings() {
        if (this.mBindArgs != null) {
            Arrays.fill(this.mBindArgs, null);
        }
    }

    public void bindAllArgsAsStrings(String[] bindArgs) {
        if (bindArgs != null) {
            for (int i = bindArgs.length; i != 0; i--) {
                bindString(i, bindArgs[i - 1]);
            }
        }
    }

    protected void onAllReferencesReleased() {
        clearBindings();
    }

    private void bind(int index, Object value) {
        if (index < 1 || index > this.mNumParameters) {
            throw new IllegalArgumentException("Cannot bind argument at index " + index + " because the index is out of range.  " + "The statement has " + this.mNumParameters + " parameters.");
        }
        this.mBindArgs[index - 1] = value;
    }
}
