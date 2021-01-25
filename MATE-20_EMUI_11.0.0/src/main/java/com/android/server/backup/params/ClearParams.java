package com.android.server.backup.params;

import android.content.pm.PackageInfo;
import com.android.server.backup.internal.OnTaskFinishedListener;
import com.android.server.backup.transport.TransportClient;

public class ClearParams {
    public OnTaskFinishedListener listener;
    public PackageInfo packageInfo;
    public TransportClient transportClient;

    public ClearParams(TransportClient transportClient2, PackageInfo packageInfo2, OnTaskFinishedListener listener2) {
        this.transportClient = transportClient2;
        this.packageInfo = packageInfo2;
        this.listener = listener2;
    }
}
