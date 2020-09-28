package com.huawei.kvdb;

import android.text.TextUtils;
import android.util.Log;
import com.huawei.uikit.effect.BuildConfig;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class HwKVConnection {
    private static final int DATABASE_MALFORMED = -1;
    private static final int DATA_SIZE = 1048576;
    private static final int FILE_DELETED = 1;
    private static final int SQLITE_FULL = 13;
    private static final int STEP_CONTINUE = 0;
    private static final int STEP_DONE = 1;
    private static final int STEP_ERROR = 2;
    private static final int SUCCESS = 0;
    private static final String TAG = "HwKVConnection";
    private byte[] mData = null;
    private String mDbName = BuildConfig.FLAVOR;
    private long mHandle = 0;
    private boolean mIsGeneralKV = false;
    private boolean mIsReadOnly = true;
    private String mPath = BuildConfig.FLAVOR;

    private native int nativeBlobBytes(long j);

    private native int nativeBlobClose(long j);

    private native long nativeBlobOpen(long j, long j2);

    private native int nativeBlobRead(long j, byte[] bArr, int i);

    private native int nativeClose(long j);

    private native int nativeFinalize(long j);

    private native long nativeGeneralBlobOpen(long j, String str);

    private native boolean nativeGeneralHasKey(long j, String str);

    private native int nativeGeneralPut(long j, String str, byte[] bArr, int i);

    private native int nativeGeneralRemove(long j, String str);

    private native int nativeGetKeyNum(long j);

    private native long nativeGetLong(long j);

    private native String nativeGetString(long j);

    private native int nativeIsFileDeleted(long j);

    private native long nativeOpen(String str, String str2, boolean z, boolean z2);

    private native long nativePrepare(long j);

    private native int nativePut(long j, long j2, byte[] bArr, int i);

    private native int nativeRemove(long j, long j2);

    private native int nativeStep(long j);

    static {
        System.loadLibrary("kvdb_jni");
    }

    HwKVConnection(String path, String dbName, boolean isReadOnly, boolean isGeneralKV) {
        this.mIsReadOnly = isReadOnly;
        this.mPath = path;
        this.mDbName = dbName;
        this.mIsGeneralKV = isGeneralKV;
    }

    private long callNativeOpen(String path, String dbName, boolean isReadOnly, boolean isGeneralKV) {
        return nativeOpen(path, dbName, isReadOnly, isGeneralKV);
    }

    private int callNativeClose(long handle) {
        return nativeClose(handle);
    }

    private int callNativePut(long handle, long key, byte[] value, int valueSize) {
        return nativePut(handle, key, value, valueSize);
    }

    private int callNativePut(long handle, String key, byte[] value, int valueSize) {
        return nativeGeneralPut(handle, key, value, valueSize);
    }

    private int callNativeRemove(long handle, long key) {
        return nativeRemove(handle, key);
    }

    private int callNativeRemove(long handle, String key) {
        return nativeGeneralRemove(handle, key);
    }

    private long callNativeBlobOpen(long handle, long key) {
        return nativeBlobOpen(handle, key);
    }

    private long callNativeBlobOpen(long handle, String key) {
        return nativeGeneralBlobOpen(handle, key);
    }

    private int callNativeBlobBytes(long blobHandle) {
        return nativeBlobBytes(blobHandle);
    }

    private int callNativeBlobRead(long blobHandle, byte[] value, int valueSize) {
        return nativeBlobRead(blobHandle, value, valueSize);
    }

    private int callNativeBlobClose(long blobHandle) {
        return nativeBlobClose(blobHandle);
    }

    private long callNativePrepare(long handle) {
        return nativePrepare(handle);
    }

    private int callNativeStep(long stmtHandle) {
        return nativeStep(stmtHandle);
    }

    private int callNativeFinalize(long stmtHandle) {
        return nativeFinalize(stmtHandle);
    }

    private long callNativeGetLong(long stmtHandle) {
        return nativeGetLong(stmtHandle);
    }

    private String callNativeGetString(long stmtHandle) {
        return nativeGetString(stmtHandle);
    }

    private int callNativeIsFileDeleted(long handle) {
        return nativeIsFileDeleted(handle);
    }

    private int callNativeGetKeyNum(long handle) {
        return nativeGetKeyNum(handle);
    }

    private void deleteDbFiles() {
        try {
            String db = new File(this.mPath + "/" + this.mDbName).getCanonicalPath();
            String wal = db + "-wal";
            if (!new File(db + "-shm").delete()) {
                Log.e(TAG, "Failure: delete database shm file.");
            }
            if (!new File(wal).delete()) {
                Log.e(TAG, "Failure: delete database wal files.");
            }
            if (!new File(db).delete()) {
                Log.e(TAG, "Failure: delete database file.");
            }
        } catch (IOException e) {
            Log.e(TAG, "Failure: delete dbPath is invalible.");
        }
    }

    private boolean isValid() {
        return !TextUtils.isEmpty(this.mPath) && !TextUtils.isEmpty(this.mDbName);
    }

    /* access modifiers changed from: package-private */
    public boolean open() {
        if (!isValid()) {
            return false;
        }
        this.mData = new byte[1048576];
        this.mHandle = callNativeOpen(this.mPath, this.mDbName, this.mIsReadOnly, this.mIsGeneralKV);
        long j = this.mHandle;
        if (j > 0) {
            return true;
        }
        if (j == -1) {
            deleteDbFiles();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean close() {
        return callNativeClose(this.mHandle) == 0;
    }

    private byte[] getInternal(long blobHandle) throws HwKVDatabaseDeleteException {
        byte[] value;
        if (blobHandle <= 0) {
            if (blobHandle == -1) {
                deleteDbFiles();
            }
            return null;
        }
        int size = callNativeBlobBytes(blobHandle);
        if (size <= 0) {
            return null;
        }
        if (size > 1048576 || this.mData == null) {
            value = new byte[size];
        } else {
            value = this.mData;
        }
        int returnCode = callNativeBlobRead(blobHandle, value, size);
        if (returnCode != 0) {
            if (returnCode == -1) {
                deleteDbFiles();
            }
            return null;
        } else if (size > 1048576) {
            return value;
        } else {
            byte[] copyValue = new byte[size];
            System.arraycopy(value, 0, copyValue, 0, size);
            return copyValue;
        }
    }

    public byte[] get(long key) throws HwKVDatabaseDeleteException {
        if (this.mIsGeneralKV) {
            Log.w(TAG, "prohibit querying by a long key when opening a general key-value database.");
            return null;
        }
        checkIsFileDeleted();
        long blobHandle = 0;
        try {
            blobHandle = callNativeBlobOpen(this.mHandle, key);
            return getInternal(blobHandle);
        } finally {
            if (blobHandle > 0) {
                callNativeBlobClose(blobHandle);
            }
        }
    }

    public byte[] get(String key) throws HwKVDatabaseDeleteException {
        if (!this.mIsGeneralKV) {
            Log.w(TAG, "prohibit querying by a string key when opening a thumbnail key-value database.");
            return null;
        }
        checkIsFileDeleted();
        long blobHandle = 0;
        try {
            blobHandle = callNativeBlobOpen(this.mHandle, key);
            return getInternal(blobHandle);
        } finally {
            if (blobHandle > 0) {
                callNativeBlobClose(blobHandle);
            }
        }
    }

    private boolean handleResult(int result) throws HwKVFullException {
        if (result == 0) {
            return true;
        }
        if (result == -1) {
            deleteDbFiles();
            return false;
        } else if (result != 13) {
            return false;
        } else {
            throw new HwKVFullException();
        }
    }

    private boolean isAllowedToPut(byte[] value, int size) throws HwKVDatabaseDeleteException {
        if (value == null || value.length == 0 || value.length < size || this.mIsReadOnly) {
            return false;
        }
        checkIsFileDeleted();
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean put(String key, byte[] value, int size) throws HwKVDatabaseDeleteException, HwKVFullException {
        if (!this.mIsGeneralKV || !isAllowedToPut(value, size)) {
            return false;
        }
        return handleResult(callNativePut(this.mHandle, key, value, size));
    }

    /* access modifiers changed from: package-private */
    public boolean put(long key, byte[] value, int size) throws HwKVDatabaseDeleteException, HwKVFullException {
        if (this.mIsGeneralKV || !isAllowedToPut(value, size)) {
            return false;
        }
        return handleResult(callNativePut(this.mHandle, key, value, size));
    }

    /* access modifiers changed from: package-private */
    public boolean remove(long key) throws HwKVDatabaseDeleteException, HwKVFullException {
        if (this.mIsReadOnly || this.mIsGeneralKV) {
            return false;
        }
        checkIsFileDeleted();
        return handleResult(callNativeRemove(this.mHandle, key));
    }

    /* access modifiers changed from: package-private */
    public boolean remove(String key) throws HwKVDatabaseDeleteException, HwKVFullException {
        if (this.mIsReadOnly || !this.mIsGeneralKV) {
            return false;
        }
        checkIsFileDeleted();
        return handleResult(callNativeRemove(this.mHandle, key));
    }

    /* access modifiers changed from: package-private */
    public boolean hasKey(long key) throws HwKVDatabaseDeleteException {
        checkIsFileDeleted();
        long blobHandle = 0;
        try {
            blobHandle = callNativeBlobOpen(this.mHandle, key);
            if (blobHandle <= 0) {
                if (blobHandle == -1) {
                    deleteDbFiles();
                }
                return false;
            }
            if (blobHandle > 0) {
                callNativeBlobClose(blobHandle);
            }
            return true;
        } finally {
            if (blobHandle > 0) {
                callNativeBlobClose(blobHandle);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasKey(String key) throws HwKVDatabaseDeleteException {
        checkIsFileDeleted();
        return nativeGeneralHasKey(this.mHandle, key);
    }

    /* access modifiers changed from: package-private */
    public Hashtable<Long, Long> getAllKeys() throws HwKVDatabaseDeleteException {
        int result;
        checkIsFileDeleted();
        long stmt = 0;
        try {
            Hashtable<Long, Long> hashTable = new Hashtable<>();
            stmt = callNativePrepare(this.mHandle);
            if (stmt <= 0) {
                if (stmt == -1) {
                    deleteDbFiles();
                }
                return hashTable;
            }
            while (true) {
                result = callNativeStep(stmt);
                if (result != 0) {
                    break;
                }
                long key = callNativeGetLong(stmt);
                hashTable.put(Long.valueOf(key), Long.valueOf(key));
            }
            if (result == -1) {
                deleteDbFiles();
            }
            if (result == 1) {
                if (stmt > 0) {
                    callNativeFinalize(stmt);
                }
                return hashTable;
            }
            hashTable.clear();
            if (stmt > 0) {
                callNativeFinalize(stmt);
            }
            return hashTable;
        } finally {
            if (stmt > 0) {
                callNativeFinalize(stmt);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public Set<String> getAllKeysGeneral() throws HwKVDatabaseDeleteException {
        int result;
        checkIsFileDeleted();
        long stmt = 0;
        try {
            stmt = callNativePrepare(this.mHandle);
            if (stmt <= 0) {
                if (stmt == -1) {
                    deleteDbFiles();
                }
                return Collections.emptySet();
            }
            Set<String> keys = new HashSet<>();
            while (true) {
                result = callNativeStep(stmt);
                if (result != 0) {
                    break;
                }
                String key = callNativeGetString(stmt);
                if (key != null) {
                    keys.add(key);
                }
            }
            if (result == -1) {
                deleteDbFiles();
            }
            if (result == 1) {
                if (stmt > 0) {
                    callNativeFinalize(stmt);
                }
                return keys;
            }
            Set<String> emptySet = Collections.emptySet();
            if (stmt > 0) {
                callNativeFinalize(stmt);
            }
            return emptySet;
        } finally {
            if (stmt > 0) {
                callNativeFinalize(stmt);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int getKeyNum() throws HwKVDatabaseDeleteException {
        checkIsFileDeleted();
        int result = callNativeGetKeyNum(this.mHandle);
        if (result != -1) {
            return result;
        }
        deleteDbFiles();
        return 0;
    }

    private void checkIsFileDeleted() throws HwKVDatabaseDeleteException {
        if (callNativeIsFileDeleted(this.mHandle) == 1) {
            throw new HwKVDatabaseDeleteException();
        }
    }
}
