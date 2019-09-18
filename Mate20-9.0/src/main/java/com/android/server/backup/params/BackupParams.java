package com.android.server.backup.params;

import android.app.backup.IBackupManagerMonitor;
import android.app.backup.IBackupObserver;
import com.android.server.backup.internal.OnTaskFinishedListener;
import com.android.server.backup.transport.TransportClient;
import java.util.ArrayList;

public class BackupParams {
    public String dirName;
    public ArrayList<String> fullPackages;
    public ArrayList<String> kvPackages;
    public OnTaskFinishedListener listener;
    public IBackupManagerMonitor monitor;
    public boolean nonIncrementalBackup;
    public IBackupObserver observer;
    public TransportClient transportClient;
    public boolean userInitiated;

    public BackupParams(TransportClient transportClient2, String dirName2, ArrayList<String> kvPackages2, ArrayList<String> fullPackages2, IBackupObserver observer2, IBackupManagerMonitor monitor2, OnTaskFinishedListener listener2, boolean userInitiated2, boolean nonIncrementalBackup2) {
        this.transportClient = transportClient2;
        this.dirName = dirName2;
        this.kvPackages = kvPackages2;
        this.fullPackages = fullPackages2;
        this.observer = observer2;
        this.monitor = monitor2;
        this.listener = listener2;
        this.userInitiated = userInitiated2;
        this.nonIncrementalBackup = nonIncrementalBackup2;
    }
}
