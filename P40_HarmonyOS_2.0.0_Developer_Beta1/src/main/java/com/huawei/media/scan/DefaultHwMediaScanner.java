package com.huawei.media.scan;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import com.huawei.annotation.HwSystemApi;
import java.io.File;
import java.nio.file.FileVisitor;
import java.nio.file.Path;

@HwSystemApi
public class DefaultHwMediaScanner {
    private static DefaultHwMediaScanner sHwMediaMediaScanner = new DefaultHwMediaScanner();

    public static DefaultHwMediaScanner getDefault() {
        if (sHwMediaMediaScanner == null) {
            sHwMediaMediaScanner = new DefaultHwMediaScanner();
        }
        return sHwMediaMediaScanner;
    }

    public void initHwMediaScanner(Context context, MediaServiceProxy proxy) {
    }

    public boolean onHandleIntent(Context context, Intent intent) {
        return false;
    }

    public boolean isEnableMultiThread() {
        return false;
    }

    public void preScan(String volumeName) {
    }

    public void postScan(String volumeName) {
    }

    public void ensureDefaultRingtonesEx(Context context) {
    }

    public ModernScannerEx createModernScannerEx(Context context, FileVisitor<Path> visitor, File root, ContentValues contentValues) {
        return new ModernScannerEx(context, visitor, root, contentValues);
    }
}
