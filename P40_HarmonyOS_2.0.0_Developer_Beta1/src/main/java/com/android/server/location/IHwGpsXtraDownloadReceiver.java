package com.android.server.location;

import android.content.Context;
import android.net.NetworkInfo;

public interface IHwGpsXtraDownloadReceiver {
    boolean handleUpdateNetworkState(NetworkInfo networkInfo, boolean z);

    void init(Context context);

    void sendXtraDownloadComplete();

    void setNtpTime(long j, long j2);
}
