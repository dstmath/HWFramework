package com.msic.qarth;

import android.content.Context;
import com.huawei.android.os.storage.StorageManagerExt;
import java.io.File;
import java.io.IOException;

public class QarthContext {
    private static final String TAG = "QarthContext";
    public Context context;
    public String cpuAbi;
    public String packageName;
    public ClassLoader patchClassLoader;
    public PatchFile patchFile;
    public ClassLoader qarthClassLoader;
    public File qarthFile;
    public String qarthVersion;
    public RecordProcessUtil recordProcessUtil;

    public String toString() {
        String canonicalPath = StorageManagerExt.INVALID_KEY_DESC;
        try {
            if (this.qarthFile != null) {
                canonicalPath = this.qarthFile.getCanonicalPath();
            }
        } catch (IOException e) {
            QarthLog.e(TAG, "get patch file path exception");
        }
        return "{packageName: " + this.packageName + ", qarthFile: " + canonicalPath + "}";
    }
}
