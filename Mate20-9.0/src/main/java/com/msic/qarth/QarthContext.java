package com.msic.qarth;

import android.content.Context;
import java.io.File;

public class QarthContext {
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
        return "{packageName: " + this.packageName + ", qarthFile: " + this.qarthFile.getAbsolutePath() + "}";
    }
}
