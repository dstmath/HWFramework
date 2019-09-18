package com.android.server.location;

import android.content.Context;
import android.location.ILocationManager;
import android.location.Location;
import android.os.Bundle;
import android.os.WorkSource;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwLocalLocationProvider implements LocationProviderInterface, IHwLocalLocationProvider {
    static final byte[] C1 = {98, 94, -52, 117, -82, 28, -44, 66, 28, 61, -110, -119, -75, 70, 2, 85};
    public static final String KEY_LOC_SOURCE = "key_loc_source";
    public static final String KEY_LOC_TABLEID = "key_loc_tableID";
    public static final byte LOCATION_TYPE_ACCURACY_PRIORITY = -1;
    public static final byte LOCATION_TYPE_TIME_PRIORITY = 1;
    public static final byte LOCATION_TYPE_UNKNOEWN = 0;
    private static final ProviderProperties PROPERTIES;
    private static final String TAG = "HwLocalLocationProvider";
    private static boolean isEnable;
    private static HwLocalLocationProvider mLocalLocationProvider;
    private final ILocationManager mILocationManager;
    private HwLocalLocationManager mLocalLocationManager;

    static {
        ProviderProperties providerProperties = new ProviderProperties(true, false, true, false, false, false, false, 1, 2);
        PROPERTIES = providerProperties;
    }

    private HwLocalLocationProvider(Context context, ILocationManager iLocationManager) {
        this.mILocationManager = iLocationManager;
        isEnable = true;
        this.mLocalLocationManager = new HwLocalLocationManager(context, iLocationManager);
    }

    public static synchronized HwLocalLocationProvider getInstance(Context context, ILocationManager iLocationManager) {
        HwLocalLocationProvider hwLocalLocationProvider;
        synchronized (HwLocalLocationProvider.class) {
            if (mLocalLocationProvider == null) {
                mLocalLocationProvider = new HwLocalLocationProvider(context, iLocationManager);
            }
            hwLocalLocationProvider = mLocalLocationProvider;
        }
        return hwLocalLocationProvider;
    }

    public void requestLocation() {
        if (isEnable) {
            this.mLocalLocationManager.requestLocation();
        }
    }

    public byte getLocationRequestType(String appname) {
        if (isEnable) {
            return this.mLocalLocationManager.getLocationRequestType(appname);
        }
        return 0;
    }

    public void updataLocationDB(Location loc) {
        if (isEnable && loc != null) {
            this.mLocalLocationManager.updataLocationDB(loc);
        }
    }

    public String getName() {
        return HwLocalLocationManager.LOCAL_PROVIDER;
    }

    public void enable() {
        isEnable = true;
    }

    public void disable() {
        isEnable = false;
        this.mLocalLocationManager.closedb();
    }

    public boolean isEnabled() {
        return isEnable;
    }

    public boolean isBetterLocation(Location location, Location currentBestLocation) {
        return this.mLocalLocationManager.isBetterLocation(location, currentBestLocation);
    }

    public boolean isValidLocation(Location loc) {
        return this.mLocalLocationManager.isValidLocation(loc);
    }

    public void setRequest(ProviderRequest request, WorkSource source) {
    }

    public ProviderProperties getProperties() {
        return PROPERTIES;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

    public int getStatus(Bundle extras) {
        return 0;
    }

    public long getStatusUpdateTime() {
        return 0;
    }

    public boolean sendExtraCommand(String command, Bundle extras) {
        return false;
    }
}
