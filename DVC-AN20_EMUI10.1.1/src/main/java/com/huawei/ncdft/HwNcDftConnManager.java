package com.huawei.ncdft;

import android.content.Context;
import android.location.Location;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemProperties;
import java.util.List;

public class HwNcDftConnManager {
    private static final int BETA_USER = 3;
    private static final int COMMERCIAL_USER = 1;
    private static final int TEST_USER = 4;
    private final HwNcDftConnImpl mNcDftImpl;

    public HwNcDftConnManager(Context context) {
        this.mNcDftImpl = HwNcDftConnImpl.getInstance(context);
    }

    public synchronized int reportToDft(int domain, int event, List<String> list) {
        return this.mNcDftImpl.reportToDft(domain, event, list);
    }

    public synchronized int reportToDft(int domain, int event, Bundle data) {
        return this.mNcDftImpl.reportToDft(domain, event, data);
    }

    public synchronized String getFromDft(int domain, List<String> list) {
        return this.mNcDftImpl.getFromDft(domain, list);
    }

    public synchronized void reportNetworkInfo(int domain, int event, NetworkInfo info) {
        this.mNcDftImpl.reportNetworkInfo(domain, event, info);
    }

    public synchronized void reportGnssLocation(int domain, int event, Location location, long time, String provider) {
        this.mNcDftImpl.reportGnssLocation(domain, event, location, time, provider);
    }

    public synchronized void reportGnssSvStatus(int domain, int event, int svCount, int[] svs, float[] snrs, float[] svElevations, float[] svAzimuths) {
        this.mNcDftImpl.reportGnssSvStatus(domain, event, svCount, svs, snrs, svElevations, svAzimuths);
    }

    public static boolean isCommercialUser() {
        int userType = SystemProperties.getInt("ro.logsystem.usertype", 1);
        if (userType == 3 || userType == 4) {
            return false;
        }
        return true;
    }
}
