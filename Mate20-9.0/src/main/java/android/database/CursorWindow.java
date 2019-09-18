package android.database;

import android.content.res.Resources;
import android.database.sqlite.SQLiteClosable;
import android.os.Binder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Process;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.SparseIntArray;
import com.android.internal.R;
import dalvik.system.CloseGuard;

public class CursorWindow extends SQLiteClosable implements Parcelable {
    public static final Parcelable.Creator<CursorWindow> CREATOR = new Parcelable.Creator<CursorWindow>() {
        public CursorWindow createFromParcel(Parcel source) {
            return new CursorWindow(source);
        }

        public CursorWindow[] newArray(int size) {
            return new CursorWindow[size];
        }
    };
    private static final int MAX_EACH_THRESHOLDE = 30;
    private static final int MAX_THRESHOLDE = 100;
    private static final String STATS_TAG = "CursorWindowStats";
    private static CursorResourceWrapper sCursorMonitor = null;
    private static int sCursorWindowSize = -1;
    private static final LongSparseArray<Integer> sWindowToPidMap = new LongSparseArray<>();
    private final CloseGuard mCloseGuard;
    private final String mName;
    private int mStartPos;
    public long mWindowPtr;

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
        this(name, (long) getCursorWindowSize());
    }

    public CursorWindow(String name, long windowSizeBytes) {
        this.mCloseGuard = CloseGuard.get();
        this.mStartPos = 0;
        this.mName = (name == null || name.length() == 0) ? "<unnamed>" : name;
        this.mWindowPtr = nativeCreate(this.mName, (int) windowSizeBytes);
        if (this.mWindowPtr != 0) {
            this.mCloseGuard.open("close");
            recordNewWindow(Binder.getCallingPid(), this.mWindowPtr);
            return;
        }
        throw new CursorWindowAllocationException("Cursor window allocation of " + windowSizeBytes + " bytes failed. " + printStats());
    }

    @Deprecated
    public CursorWindow(boolean localWindow) {
        this((String) null);
    }

    private CursorWindow(Parcel source) {
        this.mCloseGuard = CloseGuard.get();
        this.mStartPos = source.readInt();
        this.mWindowPtr = nativeCreateFromParcel(source);
        if (this.mWindowPtr != 0) {
            this.mName = nativeGetName(this.mWindowPtr);
            this.mCloseGuard.open("close");
            return;
        }
        throw new CursorWindowAllocationException("Cursor window could not be created from binder.");
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
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
            return nativeGetNumRows(this.mWindowPtr);
        } finally {
            releaseReference();
        }
    }

    public boolean setNumColumns(int columnNum) {
        acquireReference();
        try {
            return nativeSetNumColumns(this.mWindowPtr, columnNum);
        } finally {
            releaseReference();
        }
    }

    public boolean allocRow() {
        acquireReference();
        try {
            return nativeAllocRow(this.mWindowPtr);
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
        return type == 4 || type == 0;
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
        return type == 3 || type == 0;
    }

    public int getType(int row, int column) {
        acquireReference();
        try {
            return nativeGetType(this.mWindowPtr, row - this.mStartPos, column);
        } finally {
            releaseReference();
        }
    }

    public byte[] getBlob(int row, int column) {
        acquireReference();
        try {
            return nativeGetBlob(this.mWindowPtr, row - this.mStartPos, column);
        } finally {
            releaseReference();
        }
    }

    public String getString(int row, int column) {
        acquireReference();
        try {
            return nativeGetString(this.mWindowPtr, row - this.mStartPos, column);
        } finally {
            releaseReference();
        }
    }

    public void copyStringToBuffer(int row, int column, CharArrayBuffer buffer) {
        if (buffer != null) {
            acquireReference();
            try {
                nativeCopyStringToBuffer(this.mWindowPtr, row - this.mStartPos, column, buffer);
            } finally {
                releaseReference();
            }
        } else {
            throw new IllegalArgumentException("CharArrayBuffer should not be null");
        }
    }

    public long getLong(int row, int column) {
        acquireReference();
        try {
            return nativeGetLong(this.mWindowPtr, row - this.mStartPos, column);
        } finally {
            releaseReference();
        }
    }

    public double getDouble(int row, int column) {
        acquireReference();
        try {
            return nativeGetDouble(this.mWindowPtr, row - this.mStartPos, column);
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
            return nativePutBlob(this.mWindowPtr, value, row - this.mStartPos, column);
        } finally {
            releaseReference();
        }
    }

    public boolean putString(String value, int row, int column) {
        acquireReference();
        try {
            return nativePutString(this.mWindowPtr, value, row - this.mStartPos, column);
        } finally {
            releaseReference();
        }
    }

    public boolean putLong(long value, int row, int column) {
        acquireReference();
        try {
            return nativePutLong(this.mWindowPtr, value, row - this.mStartPos, column);
        } finally {
            releaseReference();
        }
    }

    public boolean putDouble(double value, int row, int column) {
        acquireReference();
        try {
            return nativePutDouble(this.mWindowPtr, value, row - this.mStartPos, column);
        } finally {
            releaseReference();
        }
    }

    public boolean putNull(int row, int column) {
        acquireReference();
        try {
            return nativePutNull(this.mWindowPtr, row - this.mStartPos, column);
        } finally {
            releaseReference();
        }
    }

    public static CursorWindow newFromParcel(Parcel p) {
        return CREATOR.createFromParcel(p);
    }

    public int describeContents() {
        return 0;
    }

    /* JADX INFO: finally extract failed */
    public void writeToParcel(Parcel dest, int flags) {
        acquireReference();
        try {
            dest.writeInt(this.mStartPos);
            nativeWriteToParcel(this.mWindowPtr, dest);
            releaseReference();
            if ((flags & 1) != 0) {
                releaseReference();
            }
        } catch (Throwable th) {
            releaseReference();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void onAllReferencesReleased() {
        dispose();
    }

    private void recordNewWindow(int pid, long window) {
        synchronized (sWindowToPidMap) {
            if (sCursorMonitor != null && sWindowToPidMap.size() > 100) {
                int usage = getStatsByPidLocked(pid);
                if (usage > 30) {
                    if (!sCursorMonitor.acquireLocked(pid, Binder.getCallingUid(), usage)) {
                        dispose();
                        throw new CursorWindowAllocationException("Created too many Cursor windows." + printStats());
                    }
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
            if (sWindowToPidMap.size() != 0) {
                sWindowToPidMap.delete(window);
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003b, code lost:
        r4 = r3.size();
        r5 = 0;
        r2 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0041, code lost:
        if (r2 >= r4) goto L_0x008b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0043, code lost:
        r0.append(" (# cursors opened by ");
        r7 = r3.keyAt(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x004c, code lost:
        if (r7 != r1) goto L_0x0055;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004e, code lost:
        r0.append("this proc=");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0055, code lost:
        r0.append("pid " + r7 + "=");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x006f, code lost:
        r0.append(r8 + ")");
        r5 = r5 + r8;
        r2 = r2 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0091, code lost:
        if (r0.length() <= 980) goto L_0x0098;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0093, code lost:
        r2 = r0.substring(0, 980);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0098, code lost:
        r2 = r0.toString();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x00b0, code lost:
        return "# Open Cursors=" + r5 + r2;
     */
    private String printStats() {
        StringBuilder buff = new StringBuilder();
        int myPid = Process.myPid();
        SparseIntArray pidCounts = new SparseIntArray();
        synchronized (sWindowToPidMap) {
            int size = sWindowToPidMap.size();
            if (size == 0) {
                return "";
            }
            for (int indx = 0; indx < size; indx++) {
                int pid = sWindowToPidMap.valueAt(indx).intValue();
                pidCounts.put(pid, pidCounts.get(pid) + 1);
            }
        }
    }

    private static int getCursorWindowSize() {
        if (sCursorWindowSize < 0) {
            sCursorWindowSize = Resources.getSystem().getInteger(R.integer.config_cursorWindowSize) * 1024;
        }
        return sCursorWindowSize;
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
            if (sWindowToPidMap.valueAt(indx).intValue() == targetPid) {
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
