package com.android.server.location;

import android.location.Location;
import android.net.NetworkInfo;
import android.os.Bundle;
import com.android.internal.location.ProviderRequest;

public interface IHwGpsLogServices {
    public static final int RECORD_REMORE_LISTENER = 0;
    public static final int TYPE_PUT = 1;
    public static final int TYPE_REMOVE = 0;

    void addBatchingStatus();

    void addGeofenceStatus();

    void initGps(boolean z, byte b);

    void injectExtraParam(String str);

    void injectTimeParam(int i, long j, int i2);

    void netWorkLocation(String str, ProviderRequest providerRequest);

    void openGpsSwitchFail(int i);

    void permissionErr(String str);

    void processGnssHalDriverEvent(String str);

    void recordGnssStatusStatistics(int i, int i2, String str, Bundle bundle);

    void setLocationSettingsOffErr(String str);

    void setQuickGpsParam(int i, String str);

    void startGps(boolean z, int i);

    void stopGps(boolean z);

    void updateAgpsState(int i, int i2);

    void updateApkName(String str, String str2, String str3, String str4, String str5);

    void updateGpsRunState(int i);

    void updateLocation(Location location, long j, String str);

    void updateNLPStatus(int i);

    void updateNetworkState(NetworkInfo networkInfo);

    void updateNtpDloadStatus(boolean z);

    void updateNtpServerInfo(String str);

    void updateSetPosMode(boolean z);

    void updateSetPosMode(boolean z, int i);

    void updateSvStatus(int i, int[] iArr, float[] fArr, float[] fArr2, float[] fArr3);

    void updateXtraDloadStatus(boolean z);
}
