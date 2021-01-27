package com.huawei.trustzone;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;
import java.io.File;

public class Storage {
    private static final String LIBRARY_NAME = "trustzone_storage";
    public static final int SEEK_CUR = 1;
    public static final int SEEK_END = 2;
    public static final int SEEK_SET = 0;
    private static final String TAG = "TrustZoneStorage";
    public static final int TEE_DATA_FLAG_ACCESS_READ = 1;
    public static final int TEE_DATA_FLAG_ACCESS_WRITE = 2;
    public static final int TEE_DATA_FLAG_ACCESS_WRITE_META = 4;
    public static final int TEE_DATA_FLAG_CREATE = 512;
    public static final int TEE_DATA_FLAG_EXCLUSIVE = 1024;
    public static final int TEE_DATA_FLAG_SHARE_READ = 16;
    public static final int TEE_DATA_FLAG_SHARE_WRITE = 32;
    private static final String TEE_PERSIST_STORAGE_DIR = "sec_storage";
    private static final String TEE_TEMP_STORAGE_DIR = "sec_storage_data";
    private String mPkgName = null;

    @HwSystemApi
    private native int storageClose(int i);

    @HwSystemApi
    private native int storageGetErr();

    @HwSystemApi
    private native int storageGetSize(int i);

    @HwSystemApi
    private native int storageInit();

    @HwSystemApi
    private native int storageOpen(String str, int i);

    @HwSystemApi
    private native int storageRead(int i, byte[] bArr, int i2);

    @HwSystemApi
    private native int storageRemove(String str);

    @HwSystemApi
    private native int storageSeek(int i, int i2, int i3);

    @HwSystemApi
    private native int storageSync(int i);

    @HwSystemApi
    private native int storageUninit();

    @HwSystemApi
    private native int storageWrite(int i, byte[] bArr, int i2);

    static {
        try {
            System.loadLibrary(LIBRARY_NAME);
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "LoadLibrary occurs error!");
        }
    }

    public Storage(Context context) {
        if (context == null) {
            Log.e(TAG, "Storage: The context is null!");
        } else {
            this.mPkgName = context.getApplicationContext().getPackageName();
        }
    }

    public int init() {
        return storageInit();
    }

    public int uninit() {
        return storageUninit();
    }

    public int open(String filePath, int mode) {
        String path = constructPath(filePath, false);
        if (path == null) {
            return -1;
        }
        return storageOpen(path, mode);
    }

    public int open(String filePath, int mode, boolean isPersist) {
        String path = constructPath(filePath, isPersist);
        if (path == null) {
            return -1;
        }
        return storageOpen(path, mode);
    }

    public int close(int fileDescriptor) {
        return storageClose(fileDescriptor);
    }

    public int read(int fileDescriptor, byte[] buffer, int length) {
        return storageRead(fileDescriptor, buffer, length);
    }

    public int write(int fileDescriptor, byte[] buffer, int length) {
        return storageWrite(fileDescriptor, buffer, length);
    }

    public int getErr() {
        return storageGetErr();
    }

    public int remove(String path) {
        String realPath = constructPath(path, false);
        if (realPath == null) {
            return -1;
        }
        return storageRemove(realPath);
    }

    public int remove(String path, boolean isPersist) {
        String realPath = constructPath(path, isPersist);
        if (realPath == null) {
            return -1;
        }
        return storageRemove(realPath);
    }

    public int seek(int fileDescriptor, int offset, int whence) {
        return storageSeek(fileDescriptor, offset, whence);
    }

    public int getSize(int fileDescriptor) {
        return storageGetSize(fileDescriptor);
    }

    public int sync(int fileDescriptor) {
        return storageSync(fileDescriptor);
    }

    private String constructPath(String originalPath, boolean isPersist) {
        String dir;
        if (TextUtils.isEmpty(originalPath)) {
            Log.e(TAG, "constructPath: Invalid original path.");
            return null;
        } else if (TextUtils.isEmpty(this.mPkgName)) {
            Log.e(TAG, "constructPath: Invalid package name.");
            return null;
        } else {
            if (isPersist) {
                dir = TEE_PERSIST_STORAGE_DIR;
            } else {
                dir = TEE_TEMP_STORAGE_DIR;
            }
            String path = dir + File.separator + originalPath;
            Log.i(TAG, "constructPath: path = " + path);
            return path;
        }
    }
}
