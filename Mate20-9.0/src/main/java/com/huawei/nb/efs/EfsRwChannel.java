package com.huawei.nb.efs;

import android.util.Log;
import java.nio.ByteBuffer;

public class EfsRwChannel {
    public static final int CURRENT_POS = -1;
    public static final int OPEN_CREATE = 4;
    public static final int OPEN_DIRECTIO = 16;
    public static final int OPEN_NOLOCK = 256;
    public static final int OPEN_READONLY = 1;
    public static final int OPEN_READWRITE = 2;
    public static final int READ_TRANSACTION = 0;
    private static final String TAG = "EfsRwChannel";
    public static final int TRANS_DEFERRED = 0;
    public static final int TRANS_EXCLUSIVE = 2;
    public static final int TRANS_IMMEDIATE = 1;
    public static final int WRITE_TRANSACTION = 1;
    private long mChannelPtr;

    private static native void nativeClose(long j);

    private static native void nativeEndTransaction(long j, boolean z);

    private static native int nativeGetFileSize(long j);

    private static native byte[] nativeGetKey(long j, ByteBuffer byteBuffer);

    private static native long nativeOpen(String str, int i);

    private static native int nativeRead(long j, int i, ByteBuffer byteBuffer, int i2);

    private static native void nativeSetCipher(long j, Object obj, String str);

    private static native void nativeSetKey(long j, byte[] bArr);

    private static native void nativeStartTransaction(long j, int i, int i2);

    private static native void nativeTruncateFile(long j, int i);

    private static native int nativeWrite(long j, int i, byte[] bArr, int i2, int i3);

    static {
        try {
            System.loadLibrary("efs_jni");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "load library libefs_jni.so failed!");
            throw e;
        }
    }

    private void openInner(String path, int openFlags) {
        this.mChannelPtr = nativeOpen(path, openFlags);
    }

    public static EfsRwChannel open(String path, int openFlags, byte[] key) throws EfsException {
        EfsRwChannel chnl = new EfsRwChannel();
        chnl.openInner(path, openFlags);
        if (!(key == null || key.length == 0)) {
            chnl.setKey(key);
        }
        return chnl;
    }

    public void setKey(byte[] key) throws EfsException {
        nativeSetKey(this.mChannelPtr, key);
    }

    public void setCipher(String desc) throws EfsException {
        if (desc == null) {
            throw new EfsException("Invalid arguments, cipher desc is null");
        }
        nativeSetCipher(this.mChannelPtr, null, desc);
    }

    public void setCipher(Object context, String desc) throws EfsException {
        if (desc == null) {
            throw new EfsException("Invalid arguments, cipher desc is null");
        }
        nativeSetCipher(this.mChannelPtr, context, desc);
    }

    public byte[] getKey(ByteBuffer buffer) throws EfsException {
        if (buffer == null) {
            return new byte[0];
        }
        return nativeGetKey(this.mChannelPtr, buffer);
    }

    public void startTransaction(int type, int mode) throws EfsException {
        nativeStartTransaction(this.mChannelPtr, type, mode);
    }

    public void endTransaction(boolean bCommit) throws EfsException {
        nativeEndTransaction(this.mChannelPtr, bCommit);
    }

    public int getFileSize() throws EfsException {
        return nativeGetFileSize(this.mChannelPtr);
    }

    public void truncateFile(int size) throws EfsException {
        nativeTruncateFile(this.mChannelPtr, size);
    }

    public ByteBuffer read() throws EfsException {
        startTransaction(0, 0);
        int fileSize = getFileSize();
        ByteBuffer buffer = ByteBuffer.allocateDirect(fileSize);
        read(0, buffer, fileSize);
        endTransaction(true);
        return buffer;
    }

    public int write(int pos, byte[] bytes, int offset, int size) throws EfsException {
        return nativeWrite(this.mChannelPtr, pos, bytes, offset, size);
    }

    public int write(byte[] bytes, int offset, int size) throws EfsException {
        return nativeWrite(this.mChannelPtr, -1, bytes, offset, size);
    }

    public void close() throws EfsException {
        nativeClose(this.mChannelPtr);
    }

    private int read(int pos, ByteBuffer buffer, int size) throws EfsException {
        if (buffer == null) {
            throw new EfsException("Invalid arguments, buffer is null");
        } else if (buffer.capacity() >= size) {
            return nativeRead(this.mChannelPtr, pos, buffer, size);
        } else {
            throw new EfsException("Invalid arguments, byte buffer capacity < size");
        }
    }
}
