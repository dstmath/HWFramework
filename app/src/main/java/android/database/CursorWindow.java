package android.database;

import android.content.res.Resources;
import android.database.sqlite.SQLiteClosable;
import android.net.ProxyInfo;
import android.os.Binder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.Process;
import android.provider.DocumentsContract.Document;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseIntArray;
import dalvik.system.CloseGuard;

public class CursorWindow extends SQLiteClosable implements Parcelable {
    public static final Creator<CursorWindow> CREATOR = null;
    private static final int MAX_EACH_THRESHOLDE = 30;
    private static final int MAX_THRESHOLDE = 100;
    private static final String STATS_TAG = "CursorWindowStats";
    private static CursorResourceWrapper sCursorMonitor;
    private static int sCursorWindowSize;
    private static final LongSparseArray<Integer> sWindowToPidMap = null;
    private final CloseGuard mCloseGuard;
    private final String mName;
    private int mStartPos;
    public long mWindowPtr;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.database.CursorWindow.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.database.CursorWindow.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.database.CursorWindow.<clinit>():void");
    }

    private static native boolean nativeAllocRow(long j);

    private static native void nativeClear(long j);

    private static native void nativeCopyStringToBuffer(long j, int i, int i2, CharArrayBuffer charArrayBuffer);

    private static native long nativeCreate(String str, int i);

    private static native long nativeCreateFromParcel(Parcel parcel);

    private static native void nativeDispose(long j);

    private static native void nativeFreeLastRow(long j);

    private static native byte[] nativeGetBlob(long j, int i, int i2);

    private static native double nativeGetDouble(long j, int i, int i2);

    private static native long nativeGetLong(long j, int i, int i2);

    private static native String nativeGetName(long j);

    private static native int nativeGetNumRows(long j);

    private static native String nativeGetString(long j, int i, int i2);

    private static native int nativeGetType(long j, int i, int i2);

    private static native boolean nativePutBlob(long j, byte[] bArr, int i, int i2);

    private static native boolean nativePutDouble(long j, double d, int i, int i2);

    private static native boolean nativePutLong(long j, long j2, int i, int i2);

    private static native boolean nativePutNull(long j, int i, int i2);

    private static native boolean nativePutString(long j, String str, int i, int i2);

    private static native boolean nativeSetNumColumns(long j, int i);

    private static native void nativeWriteToParcel(long j, Parcel parcel);

    public CursorWindow(String name) {
        this.mCloseGuard = CloseGuard.get();
        this.mStartPos = 0;
        if (name == null || name.length() == 0) {
            name = "<unnamed>";
        }
        this.mName = name;
        if (sCursorWindowSize < 0) {
            sCursorWindowSize = Resources.getSystem().getInteger(17694848) * Document.FLAG_SUPPORTS_REMOVE;
        }
        this.mWindowPtr = nativeCreate(this.mName, sCursorWindowSize);
        if (this.mWindowPtr == 0) {
            throw new CursorWindowAllocationException("Cursor window allocation of " + (sCursorWindowSize / Document.FLAG_SUPPORTS_REMOVE) + " kb failed. " + printStats());
        }
        this.mCloseGuard.open("close");
        recordNewWindow(Binder.getCallingPid(), this.mWindowPtr);
    }

    @Deprecated
    public CursorWindow(boolean localWindow) {
        this((String) null);
    }

    private CursorWindow(Parcel source) {
        this.mCloseGuard = CloseGuard.get();
        this.mStartPos = source.readInt();
        this.mWindowPtr = nativeCreateFromParcel(source);
        if (this.mWindowPtr == 0) {
            throw new CursorWindowAllocationException("Cursor window could not be created from binder.");
        }
        this.mName = nativeGetName(this.mWindowPtr);
        this.mCloseGuard.open("close");
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mCloseGuard != null) {
                this.mCloseGuard.warnIfOpen();
            }
            dispose();
        } finally {
            super.finalize();
        }
    }

    private void dispose() {
        if (this.mCloseGuard != null) {
            this.mCloseGuard.close();
        }
        if (this.mWindowPtr != 0) {
            recordClosingOfWindow(this.mWindowPtr);
            nativeDispose(this.mWindowPtr);
            this.mWindowPtr = 0;
        }
    }

    public String getName() {
        return this.mName;
    }

    public void clear() {
        acquireReference();
        try {
            this.mStartPos = 0;
            nativeClear(this.mWindowPtr);
        } finally {
            releaseReference();
        }
    }

    public int getStartPosition() {
        return this.mStartPos;
    }

    public void setStartPosition(int pos) {
        this.mStartPos = pos;
    }

    public int getNumRows() {
        acquireReference();
        try {
            int nativeGetNumRows = nativeGetNumRows(this.mWindowPtr);
            return nativeGetNumRows;
        } finally {
            releaseReference();
        }
    }

    public boolean setNumColumns(int columnNum) {
        acquireReference();
        try {
            boolean nativeSetNumColumns = nativeSetNumColumns(this.mWindowPtr, columnNum);
            return nativeSetNumColumns;
        } finally {
            releaseReference();
        }
    }

    public boolean allocRow() {
        acquireReference();
        try {
            boolean nativeAllocRow = nativeAllocRow(this.mWindowPtr);
            return nativeAllocRow;
        } finally {
            releaseReference();
        }
    }

    public void freeLastRow() {
        acquireReference();
        try {
            nativeFreeLastRow(this.mWindowPtr);
        } finally {
            releaseReference();
        }
    }

    @Deprecated
    public boolean isNull(int row, int column) {
        return getType(row, column) == 0;
    }

    @Deprecated
    public boolean isBlob(int row, int column) {
        int type = getType(row, column);
        if (type == 4 || type == 0) {
            return true;
        }
        return false;
    }

    @Deprecated
    public boolean isLong(int row, int column) {
        return getType(row, column) == 1;
    }

    @Deprecated
    public boolean isFloat(int row, int column) {
        return getType(row, column) == 2;
    }

    @Deprecated
    public boolean isString(int row, int column) {
        int type = getType(row, column);
        if (type == 3 || type == 0) {
            return true;
        }
        return false;
    }

    public int getType(int row, int column) {
        acquireReference();
        try {
            int nativeGetType = nativeGetType(this.mWindowPtr, row - this.mStartPos, column);
            return nativeGetType;
        } finally {
            releaseReference();
        }
    }

    public byte[] getBlob(int row, int column) {
        acquireReference();
        try {
            byte[] nativeGetBlob = nativeGetBlob(this.mWindowPtr, row - this.mStartPos, column);
            return nativeGetBlob;
        } finally {
            releaseReference();
        }
    }

    public String getString(int row, int column) {
        acquireReference();
        try {
            String nativeGetString = nativeGetString(this.mWindowPtr, row - this.mStartPos, column);
            return nativeGetString;
        } finally {
            releaseReference();
        }
    }

    public void copyStringToBuffer(int row, int column, CharArrayBuffer buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("CharArrayBuffer should not be null");
        }
        acquireReference();
        try {
            nativeCopyStringToBuffer(this.mWindowPtr, row - this.mStartPos, column, buffer);
        } finally {
            releaseReference();
        }
    }

    public long getLong(int row, int column) {
        acquireReference();
        try {
            long nativeGetLong = nativeGetLong(this.mWindowPtr, row - this.mStartPos, column);
            return nativeGetLong;
        } finally {
            releaseReference();
        }
    }

    public double getDouble(int row, int column) {
        acquireReference();
        try {
            double nativeGetDouble = nativeGetDouble(this.mWindowPtr, row - this.mStartPos, column);
            return nativeGetDouble;
        } finally {
            releaseReference();
        }
    }

    public short getShort(int row, int column) {
        return (short) ((int) getLong(row, column));
    }

    public int getInt(int row, int column) {
        return (int) getLong(row, column);
    }

    public float getFloat(int row, int column) {
        return (float) getDouble(row, column);
    }

    public boolean putBlob(byte[] value, int row, int column) {
        acquireReference();
        try {
            boolean nativePutBlob = nativePutBlob(this.mWindowPtr, value, row - this.mStartPos, column);
            return nativePutBlob;
        } finally {
            releaseReference();
        }
    }

    public boolean putString(String value, int row, int column) {
        acquireReference();
        try {
            boolean nativePutString = nativePutString(this.mWindowPtr, value, row - this.mStartPos, column);
            return nativePutString;
        } finally {
            releaseReference();
        }
    }

    public boolean putLong(long value, int row, int column) {
        acquireReference();
        try {
            boolean nativePutLong = nativePutLong(this.mWindowPtr, value, row - this.mStartPos, column);
            return nativePutLong;
        } finally {
            releaseReference();
        }
    }

    public boolean putDouble(double value, int row, int column) {
        acquireReference();
        try {
            boolean nativePutDouble = nativePutDouble(this.mWindowPtr, value, row - this.mStartPos, column);
            return nativePutDouble;
        } finally {
            releaseReference();
        }
    }

    public boolean putNull(int row, int column) {
        acquireReference();
        try {
            boolean nativePutNull = nativePutNull(this.mWindowPtr, row - this.mStartPos, column);
            return nativePutNull;
        } finally {
            releaseReference();
        }
    }

    public static CursorWindow newFromParcel(Parcel p) {
        return (CursorWindow) CREATOR.createFromParcel(p);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        acquireReference();
        try {
            dest.writeInt(this.mStartPos);
            nativeWriteToParcel(this.mWindowPtr, dest);
            if ((flags & 1) != 0) {
                releaseReference();
            }
        } finally {
            releaseReference();
        }
    }

    protected void onAllReferencesReleased() {
        dispose();
    }

    private void recordNewWindow(int pid, long window) {
        synchronized (sWindowToPidMap) {
            if (sCursorMonitor != null && sWindowToPidMap.size() > MAX_THRESHOLDE) {
                int usage = getStatsByPidLocked(pid);
                if (usage > MAX_EACH_THRESHOLDE && !sCursorMonitor.acquireLocked(pid, Binder.getCallingUid(), usage)) {
                    dispose();
                    throw new CursorWindowAllocationException("Created too many Cursor windows." + printStats());
                }
            }
            sWindowToPidMap.put(window, Integer.valueOf(pid));
            if (Log.isLoggable(STATS_TAG, 2)) {
                Log.i(STATS_TAG, "Created a new Cursor. " + printStats());
            }
        }
    }

    private void recordClosingOfWindow(long window) {
        synchronized (sWindowToPidMap) {
            if (sWindowToPidMap.size() == 0) {
                return;
            }
            sWindowToPidMap.delete(window);
        }
    }

    private String printStats() {
        StringBuilder buff = new StringBuilder();
        int myPid = Process.myPid();
        int total = 0;
        SparseIntArray pidCounts = new SparseIntArray();
        synchronized (sWindowToPidMap) {
            int size = sWindowToPidMap.size();
            if (size == 0) {
                String str = ProxyInfo.LOCAL_EXCL_LIST;
                return str;
            }
            for (int indx = 0; indx < size; indx++) {
                int pid = ((Integer) sWindowToPidMap.valueAt(indx)).intValue();
                pidCounts.put(pid, pidCounts.get(pid) + 1);
            }
            int numPids = pidCounts.size();
            for (int i = 0; i < numPids; i++) {
                buff.append(" (# cursors opened by ");
                pid = pidCounts.keyAt(i);
                if (pid == myPid) {
                    buff.append("this proc=");
                } else {
                    buff.append("pid ").append(pid).append("=");
                }
                int num = pidCounts.get(pid);
                buff.append(num).append(")");
                total += num;
            }
            return "# Open Cursors=" + total + (buff.length() > 980 ? buff.substring(0, 980) : buff.toString());
        }
    }

    public String toString() {
        return getName() + " {" + Long.toHexString(this.mWindowPtr) + "}";
    }

    private int getStatsByPidLocked(int targetPid) {
        if (targetPid == Process.myPid()) {
            return 0;
        }
        int usage = 0;
        int size = sWindowToPidMap.size();
        for (int indx = 0; indx < size; indx++) {
            if (((Integer) sWindowToPidMap.valueAt(indx)).intValue() == targetPid) {
                usage++;
            }
        }
        return usage;
    }

    public static void setCursorResource(CursorResourceWrapper crw) {
        synchronized (sWindowToPidMap) {
            sCursorMonitor = crw;
        }
    }
}
