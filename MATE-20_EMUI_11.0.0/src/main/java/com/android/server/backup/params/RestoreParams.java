package com.android.server.backup.params;

import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IRestoreObserver;
import android.content.pm.PackageInfo;
import com.android.server.backup.internal.OnTaskFinishedListener;
import com.android.server.backup.transport.TransportClient;

public class RestoreParams {
    public final String[] filterSet;
    public final boolean isSystemRestore;
    public final OnTaskFinishedListener listener;
    public final IBackupManagerMonitor monitor;
    public final IRestoreObserver observer;
    public final PackageInfo packageInfo;
    public final int pmToken;
    public final long token;
    public final TransportClient transportClient;

    public static RestoreParams createForSinglePackage(TransportClient transportClient2, IRestoreObserver observer2, IBackupManagerMonitor monitor2, long token2, PackageInfo packageInfo2, OnTaskFinishedListener listener2) {
        return new RestoreParams(transportClient2, observer2, monitor2, token2, packageInfo2, 0, false, null, listener2);
    }

    public static RestoreParams createForRestoreAtInstall(TransportClient transportClient2, IRestoreObserver observer2, IBackupManagerMonitor monitor2, long token2, String packageName, int pmToken2, OnTaskFinishedListener listener2) {
        return new RestoreParams(transportClient2, observer2, monitor2, token2, null, pmToken2, false, new String[]{packageName}, listener2);
    }

    public static RestoreParams createForRestoreAll(TransportClient transportClient2, IRestoreObserver observer2, IBackupManagerMonitor monitor2, long token2, OnTaskFinishedListener listener2) {
        return new RestoreParams(transportClient2, observer2, monitor2, token2, null, 0, true, null, listener2);
    }

    public static RestoreParams createForRestorePackages(TransportClient transportClient2, IRestoreObserver observer2, IBackupManagerMonitor monitor2, long token2, String[] filterSet2, boolean isSystemRestore2, OnTaskFinishedListener listener2) {
        return new RestoreParams(transportClient2, observer2, monitor2, token2, null, 0, isSystemRestore2, filterSet2, listener2);
    }

    private RestoreParams(TransportClient transportClient2, IRestoreObserver observer2, IBackupManagerMonitor monitor2, long token2, PackageInfo packageInfo2, int pmToken2, boolean isSystemRestore2, String[] filterSet2, OnTaskFinishedListener listener2) {
        this.transportClient = transportClient2;
        this.observer = observer2;
        this.monitor = monitor2;
        this.token = token2;
        this.packageInfo = packageInfo2;
        this.pmToken = pmToken2;
        this.isSystemRestore = isSystemRestore2;
        this.filterSet = filterSet2;
        this.listener = listener2;
    }
}
