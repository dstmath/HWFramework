package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import java.util.Properties;

public interface IHwGnssLocationProvider {
    public static final boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 3)));
    public static final int GEOFENCE_CALLBACK_ADD_RESULT = 58;
    public static final int GEOFENCE_CALLBACK_FENCE_STATUS = 57;
    public static final int GEOFENCE_CALLBACK_GET_CURRENT_LOCATION = 55;
    public static final int GEOFENCE_CALLBACK_REMOVE_RESULT = 59;
    public static final int GEOFENCE_CALLBACK_TRANSITION = 56;
    public static final boolean GPS_DBG = true;
    public static final String TAG = "GnssLocationProvider";

    boolean checkLowPowerMode();

    int getPreferredAccuracy();

    String getSvType(int i);

    void handleGnssNavigatingStateChange(boolean z);

    void handleReportLocationEx(boolean z, Location location);

    void hwHandleMessage(Message message);

    boolean isLocalDBEnabled();

    void loadPropertiesFromResourceEx(Context context, Properties properties);

    boolean sendExtraCommandEx(String str);

    boolean sendExtraCommandEx(String str, Bundle bundle);

    boolean shouldRestartNavi();

    void startNavigatingEx();
}
