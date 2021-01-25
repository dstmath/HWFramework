package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.WorkSource;
import com.android.internal.location.ProviderProperties;
import com.android.internal.location.ProviderRequest;
import com.android.server.LocationManagerService;
import com.android.server.LocationManagerServiceUtil;
import com.android.server.location.AbstractLocationProvider;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class HwLocalLocationProvider extends AbstractLocationProvider implements IHwLocalLocationProvider {
    static final byte[] C1 = {98, 94, -52, 117, -82, 28, -44, 66, 28, 61, -110, -119, -75, 70, 2, 85};
    public static final String KEY_LOC_SOURCE = "key_loc_source";
    public static final String KEY_LOC_TABLEID = "key_loc_tableID";
    public static final byte LOCATION_TYPE_ACCURACY_PRIORITY = -1;
    public static final byte LOCATION_TYPE_TIME_PRIORITY = 1;
    public static final byte LOCATION_TYPE_UNKNOEWN = 0;
    private static final ProviderProperties PROPERTIES = new ProviderProperties(true, false, true, false, false, false, false, 1, 2);
    private static final String TAG = "HwLocalLocationProvider";
    private static boolean isEnable;
    private static HwLocalLocationProvider sHwLocalLocationProvider;
    private HwLocalLocationManager mLocalLocationManager;

    private HwLocalLocationProvider(Context context, AbstractLocationProvider.LocationProviderManager locationProviderManager) {
        super(context, locationProviderManager);
        setProperties(PROPERTIES);
        isEnable = true;
        this.mLocalLocationManager = new HwLocalLocationManager(context);
    }

    public static boolean isLocalDBEnabled() {
        return true;
    }

    public void enableLocalLocationProviders(LocationManagerService.LocationProvider localLocationProvider) {
        LocationManagerServiceUtil util = LocationManagerServiceUtil.getDefault();
        if (util == null) {
            LBSLog.e("HwLocalLocationProvider", false, "LocationManagerServiceUtil is null enable provider fail", new Object[0]);
            return;
        }
        enable();
        util.addProviderLocked(localLocationProvider);
        util.getRealProviders().add(localLocationProvider);
        LBSLog.e("HwLocalLocationProvider", false, "LocalLocationProvider is enabled", new Object[0]);
    }

    public static synchronized HwLocalLocationProvider getInstance(Context context, AbstractLocationProvider.LocationProviderManager locationProviderManager) {
        HwLocalLocationProvider hwLocalLocationProvider;
        synchronized (HwLocalLocationProvider.class) {
            if (sHwLocalLocationProvider == null) {
                sHwLocalLocationProvider = new HwLocalLocationProvider(context, locationProviderManager);
            }
            hwLocalLocationProvider = sHwLocalLocationProvider;
        }
        return hwLocalLocationProvider;
    }

    public void requestLocation() {
        if (isEnable) {
            this.mLocalLocationManager.requestLocation();
        }
    }

    public byte getLocationRequestType(String appName) {
        if (isEnable) {
            return this.mLocalLocationManager.getLocationRequestType(appName);
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

    public void sendExtraCommand(String command, Bundle extras) {
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
    }

    public void setRequest(ProviderRequest request, WorkSource source) {
    }
}
