package com.android.server.storage;

import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.UserInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserManager;
import android.os.storage.VolumeInfo;
import android.util.Log;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AppCollector {
    private static String TAG = "AppCollector";
    private final BackgroundHandler mBackgroundHandler;
    private CompletableFuture<List<PackageStats>> mStats;

    private class BackgroundHandler extends Handler {
        static final int MSG_START_LOADING_SIZES = 0;
        private final PackageManager mPm;
        private final StorageStatsManager mStorageStatsManager;
        private final UserManager mUm;
        private final VolumeInfo mVolume;

        BackgroundHandler(Looper looper, VolumeInfo volume, PackageManager pm, UserManager um, StorageStatsManager storageStatsManager) {
            super(looper);
            this.mVolume = volume;
            this.mPm = pm;
            this.mUm = um;
            this.mStorageStatsManager = storageStatsManager;
        }

        /* JADX WARNING: Removed duplicated region for block: B:11:0x0081 A:{Splitter: B:9:0x0050, ExcHandler: android.content.pm.PackageManager.NameNotFoundException (r5_0 'e' java.lang.Exception)} */
        /* JADX WARNING: Missing block: B:11:0x0081, code:
            r5 = move-exception;
     */
        /* JADX WARNING: Missing block: B:12:0x0082, code:
            android.util.Log.e(com.android.server.storage.AppCollector.-get0(), "An exception occurred while fetching app size", r5);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    List<PackageStats> stats = new ArrayList();
                    List<UserInfo> users = this.mUm.getUsers();
                    int userSize = users.size();
                    for (int userCount = 0; userCount < userSize; userCount++) {
                        UserInfo user = (UserInfo) users.get(userCount);
                        List<ApplicationInfo> apps = this.mPm.getInstalledApplicationsAsUser(512, user.id);
                        int size = apps.size();
                        for (int appCount = 0; appCount < size; appCount++) {
                            ApplicationInfo app = (ApplicationInfo) apps.get(appCount);
                            if (Objects.equals(app.volumeUuid, this.mVolume.getFsUuid())) {
                                try {
                                    StorageStats storageStats = this.mStorageStatsManager.queryStatsForPackage(app.storageUuid, app.packageName, user.getUserHandle());
                                    PackageStats packageStats = new PackageStats(app.packageName, user.id);
                                    packageStats.cacheSize = storageStats.getCacheBytes();
                                    packageStats.codeSize = storageStats.getCodeBytes();
                                    packageStats.dataSize = storageStats.getDataBytes();
                                    stats.add(packageStats);
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                    AppCollector.this.mStats.complete(stats);
                    return;
                default:
                    return;
            }
        }
    }

    public AppCollector(Context context, VolumeInfo volume) {
        Preconditions.checkNotNull(volume);
        this.mBackgroundHandler = new BackgroundHandler(BackgroundThread.get().getLooper(), volume, context.getPackageManager(), (UserManager) context.getSystemService("user"), (StorageStatsManager) context.getSystemService("storagestats"));
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x002e A:{Splitter: B:7:0x0014, ExcHandler: java.lang.InterruptedException (r1_0 'e' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:14:0x002e, code:
            r1 = move-exception;
     */
    /* JADX WARNING: Missing block: B:15:0x002f, code:
            android.util.Log.e(TAG, "An exception occurred while getting app storage", r1);
     */
    /* JADX WARNING: Missing block: B:18:?, code:
            return r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<PackageStats> getPackageStats(long timeoutMillis) {
        synchronized (this) {
            if (this.mStats == null) {
                this.mStats = new CompletableFuture();
                this.mBackgroundHandler.sendEmptyMessage(0);
            }
        }
        List<PackageStats> value = null;
        try {
            return (List) this.mStats.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
        } catch (TimeoutException e2) {
            Log.e(TAG, "AppCollector timed out");
            return value;
        }
    }
}
