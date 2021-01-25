package com.huawei.android.os.storage;

import android.os.storage.ExternalStorageFileImpl;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class ExternalStorageFileImplEx {
    private ExternalStorageFileImpl mExternalStorageFileImpl;

    public ExternalStorageFileImplEx(String path) {
        this.mExternalStorageFileImpl = new ExternalStorageFileImpl(path);
    }

    public boolean exists() {
        return this.mExternalStorageFileImpl.exists();
    }

    public boolean isDirectory() {
        return this.mExternalStorageFileImpl.isDirectory();
    }

    public boolean isFile() {
        return this.mExternalStorageFileImpl.isFile();
    }

    public boolean delete() {
        return this.mExternalStorageFileImpl.delete();
    }

    public long lastModified() {
        return this.mExternalStorageFileImpl.lastModified();
    }

    public long length() {
        return this.mExternalStorageFileImpl.length();
    }
}
