package android.database.sqlite;

import android.content.res.Resources;
import android.os.StatFs;
import android.os.SystemProperties;

public final class SQLiteGlobal {
    private static final String TAG = "SQLiteGlobal";
    private static int sDefaultPageSize;
    private static final Object sLock = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.database.sqlite.SQLiteGlobal.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.database.sqlite.SQLiteGlobal.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.database.sqlite.SQLiteGlobal.<clinit>():void");
    }

    private static native int nativeReleaseMemory();

    private SQLiteGlobal() {
    }

    public static int releaseMemory() {
        return nativeReleaseMemory();
    }

    public static int getDefaultPageSize() {
        int i;
        synchronized (sLock) {
            if (sDefaultPageSize == 0) {
                sDefaultPageSize = new StatFs("/data").getBlockSize();
            }
            i = SystemProperties.getInt("debug.sqlite.pagesize", sDefaultPageSize);
        }
        return i;
    }

    public static String getDefaultJournalMode() {
        return SystemProperties.get("debug.sqlite.journalmode", Resources.getSystem().getString(17039436));
    }

    public static int getJournalSizeLimit() {
        return SystemProperties.getInt("debug.sqlite.journalsizelimit", Resources.getSystem().getInteger(17694843));
    }

    public static String getDefaultSyncMode() {
        return SystemProperties.get("debug.sqlite.syncmode", Resources.getSystem().getString(17039437));
    }

    public static String getWALSyncMode() {
        return SystemProperties.get("debug.sqlite.wal.syncmode", Resources.getSystem().getString(17039438));
    }

    public static int getWALAutoCheckpoint() {
        return Math.max(1, SystemProperties.getInt("debug.sqlite.wal.autocheckpoint", Resources.getSystem().getInteger(17694844)));
    }

    public static int getWALConnectionPoolSize() {
        return Math.max(2, SystemProperties.getInt("debug.sqlite.wal.poolsize", Resources.getSystem().getInteger(17694842)));
    }
}
