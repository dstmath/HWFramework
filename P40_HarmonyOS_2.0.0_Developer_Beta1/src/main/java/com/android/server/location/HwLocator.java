package com.android.server.location;

import android.location.ILocationListener;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Binder;
import android.os.SystemProperties;
import com.android.server.LocationManagerService;
import com.android.server.LocationManagerServiceUtil;
import java.util.HashMap;

public class HwLocator {
    private static final boolean IS_DUAL_FWK;
    private static final Object LOCK = new Object();
    private static final int MAX_SYSTEM_UID = 1000;
    private static final String TAG = "HwLocator";
    private static volatile HwLocator instance;
    private LocationManagerServiceUtil util = LocationManagerServiceUtil.getDefault();

    static {
        boolean z = false;
        if (SystemProperties.getBoolean("ro.build.harmoney.enable", false) || SystemProperties.getBoolean("hw_sc.build.os.enable", false)) {
            z = true;
        }
        IS_DUAL_FWK = z;
    }

    private HwLocator() {
    }

    public static HwLocator getInstance() {
        HwLocator hwLocator;
        synchronized (LOCK) {
            if (instance == null) {
                instance = new HwLocator();
            }
            hwLocator = instance;
        }
        return hwLocator;
    }

    public boolean requestLocating(LocationRequest request, ILocationListener listener, String info) throws IllegalArgumentException {
        LocationManagerServiceUtil locationManagerServiceUtil = this.util;
        if (locationManagerServiceUtil == null) {
            return false;
        }
        synchronized (locationManagerServiceUtil.getmLock()) {
            RequestInfo requsetInfo = new RequestInfo(info);
            if (!requsetInfo.isRequestInfoValid()) {
                LBSLog.d(TAG, false, "not Z location request", new Object[0]);
                return false;
            } else if (listener == null || request == null) {
                throw new IllegalArgumentException("request or listener can not be null");
            } else {
                LBSLog.i(TAG, false, "end Z location request from %{public}s, pid:%{public}d, uid:%{public}d", requsetInfo.getPackageName(), Integer.valueOf(requsetInfo.getPid()), Integer.valueOf(requsetInfo.getUid()));
                LocationManagerService.Receiver receiver = this.util.getReceiverLocked(listener, requsetInfo.getPid(), requsetInfo.getUid(), requsetInfo.getPackageName());
                if (receiver != null) {
                    LBSLog.d(TAG, false, receiver.toString(), new Object[0]);
                }
                this.util.requestLocationUpdatesLocked(request, receiver, requsetInfo.getUid(), requsetInfo.getPackageName());
                return true;
            }
        }
    }

    public boolean finishLocating(ILocationListener listener, String info) throws IllegalArgumentException {
        LocationManagerServiceUtil locationManagerServiceUtil = this.util;
        if (locationManagerServiceUtil == null) {
            return false;
        }
        synchronized (locationManagerServiceUtil.getmLock()) {
            RequestInfo requsetInfo = new RequestInfo(info);
            if (!requsetInfo.isRequestInfoValid()) {
                LBSLog.d(TAG, false, "not Z location request", new Object[0]);
                return false;
            } else if (listener != null) {
                LBSLog.i(TAG, false, "end Z location request from %{public}s, pid:%{public}d, uid:%{public}d", requsetInfo.getPackageName(), Integer.valueOf(requsetInfo.getPid()), Integer.valueOf(requsetInfo.getUid()));
                this.util.removeUpdatesLocked(this.util.getReceiverLocked(listener, requsetInfo.getPid(), requsetInfo.getUid(), requsetInfo.getPackageName()));
                return true;
            } else {
                throw new IllegalArgumentException("listener can not be null");
            }
        }
    }

    public boolean isGetLastLocationValid(String info) {
        if (new RequestInfo(info).isRequestInfoValid()) {
            return true;
        }
        LBSLog.d(TAG, false, "not Z location request", new Object[0]);
        return false;
    }

    public Location getLastLocation(LocationRequest request) {
        Location location;
        synchronized (this.util.getmLock()) {
            HashMap<String, Location> lastLocations = this.util.getLastLocation();
            location = null;
            if (!(lastLocations == null || request == null)) {
                location = lastLocations.get(request.getProvider());
            }
            if (location != null) {
                LBSLog.d(TAG, false, "get cached location:%{public}s", location.toString());
            }
        }
        return location;
    }

    private class RequestInfo {
        private static final String ZIDANE_LOCATION_PREFIX = "zlocation";
        private static final String ZIDANE_REQUEST_INFO_DIVISION = ":";
        private static final int ZIDANE_REQUEST_INFO_FORMATE_SIZE = 4;
        private boolean isValid = false;
        private String packageName = "";
        private int pid = 0;
        private int uid = 0;

        RequestInfo(String info) {
            if (info == null || !info.startsWith(ZIDANE_LOCATION_PREFIX) || !HwLocator.IS_DUAL_FWK) {
                return;
            }
            if (Binder.getCallingUid() > 1000) {
                LBSLog.w(HwLocator.TAG, false, "calling not from zos, uid = %{public}d", Integer.valueOf(Binder.getCallingUid()));
                return;
            }
            String[] str = info.split(":");
            if (str.length == 4) {
                this.pid = Integer.valueOf(str[1]).intValue();
                this.uid = Integer.valueOf(str[2]).intValue();
                this.packageName = str[3];
                this.isValid = true;
            }
        }

        public boolean isRequestInfoValid() {
            return this.isValid;
        }

        public int getUid() {
            return this.uid;
        }

        public int getPid() {
            return this.pid;
        }

        public String getPackageName() {
            return this.packageName;
        }
    }
}
