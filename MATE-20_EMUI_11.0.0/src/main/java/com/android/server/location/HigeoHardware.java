package com.android.server.location;

import android.content.Context;
import android.hardware.gnss.V1_0.GnssLocation;
import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;
import com.android.server.wm.HwWmConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import vendor.huawei.hardware.fusd.V1_0.NlpLocation;
import vendor.huawei.hardware.fusd.V1_0.WlanScanInfo;
import vendor.huawei.hardware.fusd.V1_1.FlpCellInfo;
import vendor.huawei.hardware.fusd.V1_1.FlpWifiInfo;
import vendor.huawei.hardware.fusd.V1_1.IFusdLbs;
import vendor.huawei.hardware.fusd.V1_1.IFusdLbsCallback;
import vendor.huawei.hardware.fusd.V1_2.IOfflineDbCallback;
import vendor.huawei.hardware.fusd.V1_2.IOfflineDbInterface;
import vendor.huawei.hardware.fusd.V1_2.NeighborCell;
import vendor.huawei.hardware.fusd.V1_2.WifiScanResult;
import vendor.huawei.hardware.fusd.V1_2.cellPair;
import vendor.huawei.hardware.fusd.V1_2.offlineDb;
import vendor.huawei.hardware.fusd.V1_2.requestCellDb;
import vendor.huawei.hardware.fusd.V1_2.requestWifiDb;

public class HigeoHardware {
    private static final int DEFAULT_SIZE = 1;
    private static final int EXTREME_MODE = 4;
    private static final int HMS_IS_ENABLE = 1;
    private static final int HMS_NOT_ENABLE = 0;
    private static final int HMS_TAG = 2;
    private static final int HOST_QIANXUN_TAG = 1;
    private static final short LOCATION_HAS_ACCURACY = 16;
    private static final short LOCATION_HAS_ALTITUDE = 2;
    private static final short LOCATION_HAS_BEARING = 8;
    private static final short LOCATION_HAS_SPEED = 4;
    private static final short LOCATION_VALUES_ALL_VALID = 31;
    private static final int NORMAL_MODE = 2;
    private static final int OFF_MODE = 3;
    private static final int RTK_IS_ENABLE = 1;
    private static final int RTK_NOT_ENABLE = 0;
    private static final int SAVE_MODE = 1;
    private static final int SEND_OFFLINE_DB_SUCC = 0;
    private static final int SUPER_MODE = 1;
    private static final String TAG = "HigeoHardware";
    private static final int UNSAVE_MODE = 0;
    private static final int UX_DEFAULT_VALUE = 0;
    private static final Object WATCHER_LOCK = new Object();
    private static HigeoHardware sHigeoHardware;
    private HigeoCallback mCallback;
    private IFusdLbs mIFusdLbs;
    private vendor.huawei.hardware.fusd.V1_2.IFusdLbs mIFusdLbsV2;
    private vendor.huawei.hardware.fusd.V1_5.IFusdLbs mIFusdLbsV5;
    private IOfflineDbInterface mOfflineDbInterface;

    public static HigeoHardware getInstance(Context context, HigeoCallback callback) {
        HigeoHardware higeoHardware;
        synchronized (WATCHER_LOCK) {
            if (sHigeoHardware == null) {
                sHigeoHardware = new HigeoHardware(context, callback);
            }
            higeoHardware = sHigeoHardware;
        }
        return higeoHardware;
    }

    private HigeoHardware(Context context, HigeoCallback callback) {
        getIFusdLbsService();
        this.mCallback = callback;
        FusedLbsServiceConnect.getInstance().registerServiceDiedNotify(new IFusedLbsServiceDied() {
            /* class com.android.server.location.HigeoHardware.AnonymousClass1 */

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceDied() {
                if (HigeoHardware.this.mCallback != null) {
                    LBSLog.i(HigeoHardware.TAG, false, "stop wifi scan", new Object[0]);
                    HigeoHardware.this.mCallback.wifiCommandCallback(0);
                }
            }

            @Override // com.android.server.location.IFusedLbsServiceDied
            public void onFusedLbsServiceConnect() {
                HigeoHardware.this.getIFusdLbsService();
            }
        });
        LBSLog.i(TAG, false, "HigeoHardware init completed.", new Object[0]);
    }

    private void registerLbsServiceCallback() {
        if (this.mIFusdLbs == null) {
            this.mIFusdLbs = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_3();
            if (this.mIFusdLbs == null) {
                this.mIFusdLbs = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_2();
            }
            IFusdLbs iFusdLbs = this.mIFusdLbs;
            if (iFusdLbs != null) {
                try {
                    iFusdLbs.registerLbsServiceCallbackV1_1(new HigeEventCallback());
                } catch (RemoteException e) {
                    LBSLog.e(TAG, false, "register callback error", new Object[0]);
                } catch (NoSuchElementException e2) {
                    LBSLog.e(TAG, false, "No Such Element Exception register callback", new Object[0]);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void getIFusdLbsService() {
        registerLbsServiceCallback();
        if (this.mIFusdLbsV5 == null) {
            this.mIFusdLbsV5 = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_5();
            if (this.mIFusdLbsV5 == null) {
                this.mIFusdLbsV2 = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_3();
            }
            if (this.mIFusdLbsV2 == null) {
                this.mIFusdLbsV2 = FusedLbsServiceConnect.getInstance().getIFusdLbsServiceV1_2();
            }
            if (this.mIFusdLbsV2 != null) {
                try {
                    this.mOfflineDbInterface = this.mIFusdLbsV2.getOfflineDbInterface();
                    if (this.mOfflineDbInterface != null) {
                        LBSLog.d(TAG, false, "getIFusdLbsService mOfflineDbInterface not null.", new Object[0]);
                        this.mOfflineDbInterface.setOfflineDbCallback(new OfflineDbCallback());
                    } else {
                        LBSLog.w(TAG, false, "getIFusdLbsService mOfflineDbInterface is null.", new Object[0]);
                    }
                } catch (RemoteException e) {
                    LBSLog.e(TAG, false, "register callback error:", new Object[0]);
                } catch (NoSuchElementException e2) {
                    LBSLog.e(TAG, false, "No Such Element Exception set offlineDbCallback", new Object[0]);
                }
            }
        }
    }

    public boolean sendQuickLocation(Bundle bundle) {
        if (bundle == null) {
            LBSLog.e(TAG, false, "sendQuickLocation bundle == null", new Object[0]);
            return false;
        }
        Location quickLocation = (Location) bundle.getParcelable("quickLocation");
        if (quickLocation == null) {
            LBSLog.e(TAG, false, "sendQuickLocation quickLocation == null", new Object[0]);
            return false;
        }
        NlpLocation nlpLocation = new NlpLocation();
        nlpLocation.timestamp = quickLocation.getTime();
        nlpLocation.longitude = quickLocation.getLongitude();
        nlpLocation.latitude = quickLocation.getLatitude();
        float f = -1.0f;
        nlpLocation.accuracy = quickLocation.hasAccuracy() ? quickLocation.getAccuracy() : -1.0f;
        if (quickLocation.hasSpeed()) {
            f = quickLocation.getSpeed();
        }
        nlpLocation.speed = f;
        nlpLocation.bearing = quickLocation.hasBearing() ? quickLocation.getBearing() : 1000.0f;
        nlpLocation.source = bundle.getInt("Source");
        nlpLocation.matchedWifi = bundle.getInt("MatchedWifi");
        nlpLocation.scannedWifi = bundle.getInt("ScannedWifi");
        nlpLocation.usedWifi = bundle.getInt("UsedWifi");
        nlpLocation.wlanScanage = bundle.getInt("WlanScanage");
        nlpLocation.isValid = bundle.getInt("IsValid");
        LBSLog.i(TAG, false, "synchronized sendQuickLocation enter", new Object[0]);
        getIFusdLbsService();
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            if (this.mIFusdLbsV2 != null) {
                try {
                    this.mIFusdLbsV2.sendQuickTtffLocation(bundle.getInt("NlpSource"), nlpLocation);
                } catch (RemoteException e) {
                    LBSLog.e(TAG, false, "sendQuickLocation remote exception", new Object[0]);
                    result = false;
                } catch (NoSuchElementException e2) {
                    LBSLog.e(TAG, false, "No Such Element Exception sendQuickLocation", new Object[0]);
                }
            }
        }
        LBSLog.i(TAG, "synchronized sendQuickLocation exit");
        return result;
    }

    public boolean sendHereWifiLocation(Bundle bundle) {
        if (bundle == null) {
            LBSLog.e(TAG, false, "sendHereWifiLocation bundle == null", new Object[0]);
            return false;
        }
        Location wifiLocation = (Location) bundle.getParcelable("wifiLocation");
        if (wifiLocation == null) {
            LBSLog.e(TAG, false, "sendHereWifiLocation wifiLocation == null", new Object[0]);
            return false;
        }
        NlpLocation nlpLocation = new NlpLocation();
        nlpLocation.timestamp = wifiLocation.getTime();
        nlpLocation.longitude = wifiLocation.getLongitude();
        nlpLocation.latitude = wifiLocation.getLatitude();
        float f = -1.0f;
        nlpLocation.accuracy = wifiLocation.hasAccuracy() ? wifiLocation.getAccuracy() : -1.0f;
        if (wifiLocation.hasSpeed()) {
            f = wifiLocation.getSpeed();
        }
        nlpLocation.speed = f;
        nlpLocation.bearing = wifiLocation.hasBearing() ? wifiLocation.getBearing() : 1000.0f;
        nlpLocation.source = bundle.getInt("Source");
        nlpLocation.matchedWifi = bundle.getInt("MatchedWifi");
        nlpLocation.scannedWifi = bundle.getInt("ScannedWifi");
        nlpLocation.usedWifi = bundle.getInt("UsedWifi");
        nlpLocation.wlanScanage = bundle.getInt("WlanScanage");
        nlpLocation.isValid = bundle.getInt("IsValid");
        getIFusdLbsService();
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            if (this.mIFusdLbsV2 != null) {
                try {
                    this.mIFusdLbsV2.sendHereWifiLocation(nlpLocation);
                } catch (RemoteException e) {
                    LBSLog.e(TAG, false, "sendHereWifiLocation remote exception", new Object[0]);
                    result = false;
                } catch (NoSuchElementException e2) {
                    LBSLog.e(TAG, false, "No Such Element Exception sendHereWifiLocation", new Object[0]);
                }
            }
        }
        LBSLog.e(TAG, false, "sendHereWifiLocation true", new Object[0]);
        return result;
    }

    public boolean injectLocation(int source, Location location) {
        LBSLog.i(TAG, false, "injectLocation source = %{public}d", Integer.valueOf(source));
        if (location == null) {
            LBSLog.w(TAG, false, "injectLocation location is null.", new Object[0]);
            return false;
        }
        GnssLocation gnssLocation = new GnssLocation();
        gnssLocation.latitudeDegrees = location.getLatitude();
        gnssLocation.longitudeDegrees = location.getLongitude();
        gnssLocation.speedMetersPerSec = location.getSpeed();
        gnssLocation.bearingDegrees = location.getBearing();
        gnssLocation.timestamp = location.getTime();
        gnssLocation.bearingAccuracyDegrees = location.getAccuracy();
        short flag = LOCATION_VALUES_ALL_VALID;
        if (!location.hasAccuracy()) {
            flag = (short) (31 - 16);
        }
        if (!location.hasAltitude()) {
            flag = (short) (flag - 2);
        }
        if (!location.hasBearing()) {
            flag = (short) (flag - 8);
        }
        if (!location.hasSpeed()) {
            flag = (short) (flag - 4);
        }
        gnssLocation.gnssLocationFlags = flag;
        getIFusdLbsService();
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            try {
                if (this.mIFusdLbsV2 != null) {
                    this.mIFusdLbsV2.injectLocation(source, gnssLocation);
                }
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "injectLocation exception", new Object[0]);
                result = false;
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception injectLocation", new Object[0]);
                result = false;
            }
        }
        return result;
    }

    public boolean sendBatteryState(int state) {
        LBSLog.i(TAG, false, "sendBatteryState state = %{public}d", Integer.valueOf(state));
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            try {
                if (this.mIFusdLbsV2 != null) {
                    this.mIFusdLbsV2.sendBatteryState(state);
                }
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "sendBatteryState exception", new Object[0]);
                result = false;
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception sendBatteryState", new Object[0]);
                result = false;
            }
        }
        return result;
    }

    public boolean sendScreenState(int state) {
        LBSLog.i(TAG, false, "sendScreenState state = %{public}d", Integer.valueOf(state));
        getIFusdLbsService();
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            try {
                if (this.mIFusdLbsV2 != null) {
                    this.mIFusdLbsV2.sendScreenState(state);
                }
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "sendScreenState exception", new Object[0]);
                result = false;
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception sendScreenState", new Object[0]);
                result = false;
            }
        }
        return result;
    }

    public boolean sendNetworkRoamingState(int state) {
        LBSLog.i(TAG, false, "sendNetworkRoamingState state = %{public}d", Integer.valueOf(state));
        getIFusdLbsService();
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            try {
                if (this.mIFusdLbsV2 != null) {
                    this.mIFusdLbsV2.sendNetworkRoamingState(state);
                }
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "sendNetworkRoamingState exception", new Object[0]);
                result = false;
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception sendNetworkRoamingState", new Object[0]);
                result = false;
            }
        }
        return result;
    }

    public boolean sendMapNavigatingState(int state) {
        LBSLog.i(TAG, false, "sendMapNavigatingState state = %{public}d", Integer.valueOf(state));
        getIFusdLbsService();
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            try {
                if (this.mIFusdLbsV2 != null) {
                    this.mIFusdLbsV2.sendMapNavigatingState(state);
                }
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "sendMapNavigatingState exception", new Object[0]);
                result = false;
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception sendMapNavigatingState", new Object[0]);
                result = false;
            }
        }
        return result;
    }

    public boolean sendCleanUpHifence(int status) {
        LBSLog.i(TAG, false, "sendCleanUpHifence state = %{public}d", Integer.valueOf(status));
        getIFusdLbsService();
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            try {
                if (this.mIFusdLbsV2 != null) {
                    this.mIFusdLbsV2.cleanUpHifence(status);
                }
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "sendCleanUpHifence exception", new Object[0]);
                result = false;
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception sendCleanUpHifence", new Object[0]);
                result = false;
            }
        }
        return result;
    }

    public boolean sendRtkGnssMode(int status) {
        LBSLog.i(TAG, false, "sendRtkGnssMode state = %{public}d", Integer.valueOf(status));
        getIFusdLbsService();
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            try {
                if (this.mIFusdLbsV5 != null) {
                    this.mIFusdLbsV5.sendHDGnssMode(status);
                }
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "Exception sendHDGnssMode error", new Object[0]);
                result = false;
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception sendHDGnssMode", new Object[0]);
                result = false;
            }
        }
        return result;
    }

    public boolean sendMccCode(int mccCode) {
        LBSLog.i(TAG, false, "sendMccCode state = %{public}d", Integer.valueOf(mccCode));
        getIFusdLbsService();
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            try {
                if (this.mIFusdLbsV5 != null) {
                    this.mIFusdLbsV5.sendMccCode(mccCode);
                }
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "Exception sendMccCode error", new Object[0]);
                result = false;
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception sendMccCode", new Object[0]);
                result = false;
            }
        }
        return result;
    }

    public boolean sendCityCode(int cityCode) {
        LBSLog.i(TAG, false, "sendCityCode = %{public}d", Integer.valueOf(cityCode));
        getIFusdLbsService();
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            try {
                if (this.mIFusdLbsV5 != null) {
                    this.mIFusdLbsV5.sendCityCode(cityCode);
                }
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "Exception sendCityCode error", new Object[0]);
                result = false;
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception sendCityCode", new Object[0]);
                result = false;
            }
        }
        return result;
    }

    public boolean sendPowerInfoToHdGnss(int type, int[] data) {
        LBSLog.i(TAG, false, "sendPowerInfoToHdGnss type = %{public}d", Integer.valueOf(type));
        if (data == null || data.length == 0) {
            return false;
        }
        getIFusdLbsService();
        ArrayList<Integer> modeList = new ArrayList<>();
        for (int i : data) {
            modeList.add(Integer.valueOf(i));
        }
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            try {
                if (this.mIFusdLbsV5 != null) {
                    this.mIFusdLbsV5.sendHigeoExtraInfo(type, modeList);
                }
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "Exception sendPowerInfoToHdGnss error", new Object[0]);
                result = false;
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception sendPowerInfoToHdGnss", new Object[0]);
                result = false;
            }
        }
        return result;
    }

    public boolean sendLocationSourceStatus(int type, int status) {
        LBSLog.i(TAG, false, "sendLocationSourceStatus type = %{public}d, status = %{public}d", Integer.valueOf(type), Integer.valueOf(status));
        getIFusdLbsService();
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            try {
                if (this.mIFusdLbsV2 != null) {
                    this.mIFusdLbsV2.sendLocationSourceStatus(type, status);
                }
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "sendLocationSourceStatus exception", new Object[0]);
                result = false;
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception sendLocationSourceStatus", new Object[0]);
                result = false;
            }
        }
        return result;
    }

    public boolean sendMapMatchingResult(Bundle bundle) {
        if (bundle == null) {
            LBSLog.e(TAG, false, "sendMapMatchingResult bundle == null", new Object[0]);
            return false;
        }
        getIFusdLbsService();
        boolean result = true;
        synchronized (WATCHER_LOCK) {
            try {
                this.mIFusdLbs.sendMapMatchingResult(bundle.getLong("timetag"), bundle.getDouble("heading"), bundle.getDouble("offsetLong"), bundle.getDouble("offsetLat"), bundle.getInt("rerouted"));
            } catch (RemoteException e) {
                LBSLog.e(TAG, false, "sendMapMatchingResult exception", new Object[0]);
                result = false;
            } catch (NoSuchElementException e2) {
                LBSLog.e(TAG, false, "No Such Element Exception sendMapMatchingResult", new Object[0]);
                result = false;
            }
        }
        return result;
    }

    public boolean sendOfflineDb(Bundle bundle) {
        boolean result;
        offlineDb celldb = new offlineDb();
        int type = bundle.getInt("type");
        int totalTimes = bundle.getInt("totalTimes");
        int times = bundle.getInt("times");
        celldb.type = type;
        celldb.totalTimes = totalTimes;
        celldb.times = times;
        byte[] data = null;
        boolean result2 = false;
        try {
            data = bundle.getByteArray("data");
        } catch (ArrayIndexOutOfBoundsException e) {
            LBSLog.w(TAG, false, "ArrayIndexOutOfBoundsException", new Object[0]);
        }
        if (data != null) {
            for (byte b : data) {
                celldb.dbs.add(Byte.valueOf(b));
            }
        } else {
            LBSLog.i(TAG, false, "data is null", new Object[0]);
        }
        if (this.mOfflineDbInterface == null) {
            return false;
        }
        synchronized (WATCHER_LOCK) {
            try {
                if (this.mOfflineDbInterface.sendOfflineDb(celldb) == 0) {
                    result2 = true;
                }
            } catch (RemoteException e2) {
                LBSLog.e(TAG, false, "sendOfflineDb exception", new Object[0]);
                result2 = false;
            } catch (NoSuchElementException e3) {
                LBSLog.e(TAG, false, "No Such Element Exception sendOfflineDb", new Object[0]);
                result = false;
            }
            result = result2;
        }
        return result;
    }

    /* access modifiers changed from: package-private */
    public class HigeEventCallback extends IFusdLbsCallback.Stub {
        HigeEventCallback() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbsCallback
        public void wifiCommandCallback(int command) throws RemoteException {
            if (HigeoHardware.this.mCallback != null) {
                LBSLog.i(HigeoHardware.TAG, false, "WifiFixCommand command = %{public}d", Integer.valueOf(command));
                HigeoHardware.this.mCallback.wifiCommandCallback(command);
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbsCallback
        public void gpsStateCallback(int state) throws RemoteException {
            if (HigeoHardware.this.mCallback != null) {
                LBSLog.i(HigeoHardware.TAG, false, "gpsStateCallback enter, state = %{public}d", Integer.valueOf(state));
                HigeoHardware.this.mCallback.gpsStateCallback(state);
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbsCallback
        public void arStatusCallback(long timeStamp, int eventType, int activity, int arSource) {
            if (HigeoHardware.this.mCallback != null) {
                LBSLog.i(HigeoHardware.TAG, false, "arStatusCallback activity = %{public}d, eventType = %{public}d, arSource = %{public}d", Integer.valueOf(activity), Integer.valueOf(eventType), Integer.valueOf(arSource));
                HigeoHardware.this.mCallback.arStatusCallback(timeStamp, eventType, activity, arSource);
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbsCallback
        public void quickTtffCommandCallback(int command) {
            if (HigeoHardware.this.mCallback != null) {
                LBSLog.i(HigeoHardware.TAG, false, "quickTtffCommandCallback command = %{public}d", Integer.valueOf(command));
                HigeoHardware.this.mCallback.quickTtffCommandCallback(command);
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbsCallback
        public void wlanScanCommandCallback(int command, WlanScanInfo scanInfo) throws RemoteException {
            if (HigeoHardware.this.mCallback != null) {
                HigeoHardware.this.mCallback.wlanScanCommandCallback(command, scanInfo.reportInterval, scanInfo.amount);
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_0.IFusdLbsCallback
        public void setChrDataCallback(int eventId, int subEventId, String content) throws RemoteException {
            if (HigeoHardware.this.mCallback != null) {
                HigeoHardware.this.mCallback.setChrDataCallback(eventId, subEventId, content);
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IFusdLbsCallback
        public void onSendExcept2Lbs(int type) {
            if (HigeoHardware.this.mCallback != null) {
                LBSLog.i(HigeoHardware.TAG, false, "sendExcept2Lbs, type = %{public}d", Integer.valueOf(type));
                HigeoHardware.this.mCallback.onSendExcept2Lbs(type);
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_1.IFusdLbsCallback
        public void onRequestNLPLocation(ArrayList<FlpWifiInfo> arrayList, ArrayList<FlpCellInfo> arrayList2) throws RemoteException {
        }
    }

    /* access modifiers changed from: package-private */
    public class OfflineDbCallback extends IOfflineDbCallback.Stub {
        OfflineDbCallback() {
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IOfflineDbCallback
        public void onRequestCellDb(requestCellDb database) {
            if (database == null) {
                LBSLog.w(HigeoHardware.TAG, false, "onRequestCellDb db is null.", new Object[0]);
            } else if (HigeoHardware.this.mCallback != null) {
                HigeoHardware.this.mCallback.onRequestCellDb(HigeoHardware.this.getCellDbInfo(database));
            }
        }

        @Override // vendor.huawei.hardware.fusd.V1_2.IOfflineDbCallback
        public void onRequestWifiDb(requestWifiDb arg0) throws RemoteException {
        }
    }

    private Bundle getWifiResultFromBundle(requestCellDb database) {
        Bundle bundle = new Bundle();
        bundle.putInt(HwWmConstants.FLAG_STR, database.type);
        bundle.putInt("size", database.requestSize);
        bundle.putParcelable("location", getLocation(database.location));
        ArrayList<Map<String, Object>> wifiResults = new ArrayList<>();
        if (database.result != null) {
            for (int i = 0; i < database.result.size(); i++) {
                WifiScanResult wifiScanResult = database.result.get(i);
                if (wifiScanResult != null) {
                    Map<String, Object> wifiResult = new HashMap<>();
                    wifiResult.put("bootTimeHigh", Integer.valueOf(wifiScanResult.bootTimeHigh));
                    wifiResult.put("bootTimeLow", Integer.valueOf(wifiScanResult.bootTimeLow));
                    wifiResult.put("freQuency", Integer.valueOf(wifiScanResult.frequency));
                    wifiResult.put("rssi", Integer.valueOf(wifiScanResult.rssi));
                    wifiResult.put("mac", wifiScanResult.mac.toString());
                    wifiResults.add(wifiResult);
                }
            }
        }
        bundle.putSerializable("wifiResult", wifiResults);
        return bundle;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Bundle getCellDbInfo(requestCellDb database) {
        Bundle bundle = getWifiResultFromBundle(database);
        ArrayList<Map<String, Object>> cellResults = new ArrayList<>();
        ArrayList<Map<String, Object>> negbCellResults = new ArrayList<>();
        if (database.pairs != null) {
            for (int i = 0; i < database.pairs.size(); i++) {
                cellPair pair = database.pairs.get(i);
                if (pair != null) {
                    if (pair.cell != null) {
                        Map<String, Object> cellResult = new HashMap<>();
                        cellResult.put("bootTimeHigh", Integer.valueOf(pair.cell.bootTimeHigh));
                        cellResult.put("bootTimeLow", Integer.valueOf(pair.cell.bootTimeLow));
                        cellResult.put("cellId", Integer.valueOf(pair.cell.cellId));
                        cellResult.put("channelNum", Integer.valueOf(pair.cell.channelNum));
                        cellResult.put("lac", Integer.valueOf(pair.cell.lac));
                        cellResult.put("mcc", Short.valueOf(pair.cell.mcc));
                        cellResult.put("mnc", Short.valueOf(pair.cell.mnc));
                        cellResult.put("rat", Short.valueOf(pair.cell.rat));
                        cellResult.put("rssi", Short.valueOf(pair.cell.rssi));
                        cellResults.add(cellResult);
                    }
                    if (pair.negbCells != null) {
                        for (int j = 0; j < pair.negbCells.size(); j++) {
                            NeighborCell neighborCell = pair.negbCells.get(j);
                            if (neighborCell != null) {
                                Map<String, Object> neighborResult = new HashMap<>();
                                neighborResult.put("channelNum", Integer.valueOf(neighborCell.channelNum));
                                neighborResult.put("physicalId", Integer.valueOf(neighborCell.physicalId));
                                neighborResult.put("mcc", Short.valueOf(neighborCell.mcc));
                                neighborResult.put("mnc", Short.valueOf(neighborCell.mnc));
                                neighborResult.put("rat", Short.valueOf(neighborCell.rat));
                                neighborResult.put("rssi", Short.valueOf(neighborCell.rssi));
                                negbCellResults.add(neighborResult);
                            }
                        }
                    }
                }
            }
        }
        bundle.putSerializable("cellResults", cellResults);
        bundle.putSerializable("negbCellResults", negbCellResults);
        return bundle;
    }

    private Location getLocation(GnssLocation lastLocation) {
        Location loc = new Location("gnss");
        loc.setAltitude(lastLocation.altitudeMeters);
        loc.setLatitude(lastLocation.latitudeDegrees);
        loc.setLongitude(lastLocation.longitudeDegrees);
        loc.setSpeed(lastLocation.speedMetersPerSec);
        loc.setBearing(lastLocation.bearingDegrees);
        loc.setAccuracy(lastLocation.horizontalAccuracyMeters);
        loc.setTime(lastLocation.timestamp);
        return loc;
    }
}
