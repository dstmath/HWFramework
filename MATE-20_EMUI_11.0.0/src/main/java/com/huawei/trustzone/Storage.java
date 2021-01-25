package com.huawei.trustzone;

import android.content.Context;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;
import java.io.File;

public class Storage {
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
    private native int _close(int i);

    @HwSystemApi
    private native int _getErr();

    @HwSystemApi
    private native int _getSize(int i);

    @HwSystemApi
    private native int _init();

    @HwSystemApi
    private native int _open(String str, int i);

    @HwSystemApi
    private native int _read(int i, byte[] bArr, int i2);

    @HwSystemApi
    private native int _remove(String str);

    @HwSystemApi
    private native int _seek(int i, int i2, int i3);

    @HwSystemApi
    private native int _sync(int i);

    @HwSystemApi
    private native int _uninit();

    @HwSystemApi
    private native int _write(int i, byte[] bArr, int i2);

    public Storage(Context c) {
        this.mPkgName = c.getApplicationContext().getPackageName();
    }

    public int init() {
        return _init();
    }

    public int uninit() {
        return _uninit();
    }

    public int open(String filePath, int mode) {
        String path = constructPath(filePath, false);
        if (path == null) {
            return -1;
        }
        return _open(path, mode);
    }

    public int open(String filePath, int mode, boolean isPersist) {
        String path = constructPath(filePath, isPersist);
        if (path == null) {
            return -1;
        }
        return _open(path, mode);
    }

    public int close(int fd) {
        return _close(fd);
    }

    public int read(int fd, byte[] buf, int length) {
        return _read(fd, buf, length);
    }

    public int write(int fd, byte[] buf, int length) {
        return _write(fd, buf, length);
    }

    public int getErr() {
        return _getErr();
    }

    public int remove(String path) {
        String realPath = constructPath(path, false);
        if (realPath == null) {
            return -1;
        }
        return _remove(realPath);
    }

    public int remove(String path, boolean isPersist) {
        String realPath = constructPath(path, isPersist);
        if (realPath == null) {
            return -1;
        }
        return _remove(realPath);
    }

    public int seek(int fd, int offset, int whence) {
        return _seek(fd, offset, whence);
    }

    public int getSize(int fd) {
        return _getSize(fd);
    }

    public int sync(int fd) {
        return _sync(fd);
    }

    private String constructPath(String originalPath, boolean isPersist) {
        String dir;
        if (originalPath == null || originalPath.length() == 0) {
            Log.e(TAG, "constructPath,invalid original path");
            return null;
        }
        String str = this.mPkgName;
        if (str == null || str.length() == 0) {
            Log.e(TAG, "constructPath,invalid package name");
            return null;
        }
        if (isPersist) {
            dir = TEE_PERSIST_STORAGE_DIR;
        } else {
            dir = TEE_TEMP_STORAGE_DIR;
        }
        String path = dir + File.separator + originalPath;
        Log.i(TAG, "constructPath:path = " + path);
        return path;
    }

    static {
        try {
            System.loadLibrary("trustzone_storage");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "LoadLibrary occurs error " + e.toString());
        }
    }
}
