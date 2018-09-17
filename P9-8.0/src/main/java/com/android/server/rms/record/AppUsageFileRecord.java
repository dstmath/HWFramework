package com.android.server.rms.record;

import android.content.Context;
import android.util.Log;
import com.android.server.rms.io.IOFileRotator;
import java.io.File;
import java.io.IOException;

public class AppUsageFileRecord {
    private static final String APPUSAGE_FILE_PREFIX = "app_usage";
    private static final String APPUSAGE_PATH = "data/system/usagetime";
    private static final int MAX_FILE_SIZE = 131072;
    private static final String TAG = "RMS.AppUsageFileRecord";
    private IOFileRotator mAppUsageRotator = null;
    private AppUsageTime mAppUsageTime = null;
    private final Context mContext;
    private final boolean mEnabled;

    public AppUsageFileRecord(Context context, int time, boolean enable) {
        this.mAppUsageTime = AppUsageTime.getInstance(context, time);
        this.mContext = context;
        this.mEnabled = enable;
        if (this.mEnabled) {
            this.mAppUsageRotator = new IOFileRotator(new File(APPUSAGE_PATH), APPUSAGE_FILE_PREFIX, 1, 1, 131072);
        }
    }

    public void loadUsageInfo() {
        if (this.mEnabled) {
            this.mAppUsageRotator.removeFilesWhenOverFlow();
            try {
                this.mAppUsageRotator.readMatching(this.mAppUsageTime, Long.MIN_VALUE, Long.MAX_VALUE);
            } catch (IOException e) {
                Log.e(TAG, "loadUsageInfo exception ", e);
                this.mAppUsageRotator.deleteAll();
            } catch (OutOfMemoryError e2) {
                this.mAppUsageRotator.deleteAll();
                Log.e(TAG, "LoadUsageInfo outof memory ", e2);
            }
        }
    }

    public void saveUsageInfo() {
        if (this.mEnabled) {
            int totalBytes = this.mAppUsageTime.getTotalBytes();
            if (totalBytes != 0) {
                if (totalBytes > 131072) {
                    Log.e(TAG, "AppUsage file is so big that we have to clear it, bytes is " + this.mAppUsageTime.getTotalBytes());
                    this.mAppUsageTime.clear();
                    this.mAppUsageRotator.deleteAll();
                    return;
                }
                try {
                    this.mAppUsageRotator.rewriteActive(this.mAppUsageTime, 1);
                    this.mAppUsageRotator.removeFilesWhenOverFlow();
                } catch (IOException e) {
                    Log.e(TAG, "saveUsageInfo exception ", e);
                    this.mAppUsageRotator.deleteAll();
                } catch (OutOfMemoryError e2) {
                    Log.e(TAG, "saveUsageInfo outof memory ", e2);
                    this.mAppUsageRotator.deleteAll();
                }
            }
        }
    }

    public void updateUsageInfo(String pkg) {
        this.mAppUsageTime.setHistoryTime(pkg, ResourceUtils.getAppTime(this.mContext, pkg));
    }

    public int getUsageTimeforUpload(String pkg) {
        return this.mAppUsageTime.getUsageTimeforUpload(pkg);
    }
}
