package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationRequest;
import android.net.NetworkInfo;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.location.ProviderRequest;
import com.android.server.location.gnsschrlog.GnssConnectivityLogManager;

public class HwGpsLogServices implements IHwGpsLogServices {
    private static final boolean DEBUG;
    private static final String TAG = "HwGnssLogServices";
    private static final boolean VERBOSE;
    static HwGpsLogServices mHwGpsLogServices;
    private Context mContext;
    private boolean mGeolocationCollectEnable;
    HwGnssDataCollector mGnssDataCollector;
    HwGpsSessionRecorder mHwGpsSessionRecorder;
    HwOldGpsLogServices mHwOldGpsLogServices;
    private boolean mNewGpsChrEnable;
    private HandlerThread mThread;

    static {
        DEBUG = Log.isLoggable(TAG, 3);
        VERBOSE = Log.isLoggable(TAG, 2);
    }

    private HwGpsLogServices(Context context) {
        this.mNewGpsChrEnable = SystemProperties.getBoolean("ro.config.hw_nc_chr_gps", true);
        this.mGeolocationCollectEnable = true;
        this.mContext = context;
        GnssConnectivityLogManager.init(this.mContext);
        Log.d(TAG, "enter HwGpsLogServices");
        this.mThread = new HandlerThread("HwGpsLogServices");
        this.mThread.start();
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder = new HwGpsSessionRecorder(this.mThread, this.mContext);
        } else {
            this.mHwOldGpsLogServices = new HwOldGpsLogServices(this.mThread, this.mContext);
        }
        this.mGnssDataCollector = new HwGnssDataCollector(this.mThread, this.mContext);
    }

    public static synchronized HwGpsLogServices getInstance(Context context) {
        HwGpsLogServices hwGpsLogServices;
        synchronized (HwGpsLogServices.class) {
            if (mHwGpsLogServices == null) {
                mHwGpsLogServices = new HwGpsLogServices(context);
            }
            hwGpsLogServices = mHwGpsLogServices;
        }
        return hwGpsLogServices;
    }

    public static synchronized HwGpsLogServices getGpsLogService() {
        HwGpsLogServices hwGpsLogServices;
        synchronized (HwGpsLogServices.class) {
            hwGpsLogServices = mHwGpsLogServices;
        }
        return hwGpsLogServices;
    }

    public void netWorkLocation(String provider, ProviderRequest providerRequest) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.netWorkLocation(provider, providerRequest);
        } else {
            this.mHwOldGpsLogServices.netWorkLocation(provider, providerRequest);
        }
    }

    public void openGpsSwitchFail(int open) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.openGpsSwitchFail(open);
        } else {
            this.mHwOldGpsLogServices.openGpsSwitchFail(open);
        }
    }

    public void initGps(boolean isEnable, byte EngineCapabilities) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.initGps(isEnable, EngineCapabilities);
        } else {
            this.mHwOldGpsLogServices.initGps(isEnable, EngineCapabilities);
        }
    }

    public void updateXtraDloadStatus(boolean status) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.updateXtraDloadStatus(status);
        } else {
            this.mHwOldGpsLogServices.updateXtraDloadStatus(status);
        }
    }

    public void updateNtpDloadStatus(boolean status) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.updateNtpDloadStatus(status);
        } else {
            this.mHwOldGpsLogServices.updateNtpDloadStatus(status);
        }
    }

    public void updateSetPosMode(boolean status) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.updateSetPosMode(status);
        } else {
            this.mHwOldGpsLogServices.updateSetPosMode(status);
        }
    }

    public void updateApkName(LocationRequest request, String name) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.updateApkName(request, name);
        } else {
            this.mHwOldGpsLogServices.updateApkName(name);
        }
    }

    public void startGps(boolean isEnable, int PositionMode) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.startGps(isEnable, PositionMode);
        } else {
            this.mHwOldGpsLogServices.startGps(isEnable, PositionMode);
        }
    }

    public void updateNetworkState(NetworkInfo info) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.updateNetworkState(info);
        } else {
            this.mHwOldGpsLogServices.updateNetworkState(info);
        }
    }

    public void updateAgpsState(int type, int state) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.updateAgpsState(type, state);
        } else {
            this.mHwOldGpsLogServices.updateAgpsState(type, state);
        }
    }

    public void stopGps(boolean status) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.stopGps(status);
        } else {
            this.mHwOldGpsLogServices.stopGps(status);
        }
    }

    public void permissionErr() {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.permissionErr();
        } else {
            this.mHwOldGpsLogServices.permissionErr();
        }
    }

    public void addGeofenceStatus() {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.addGeofenceStatus();
        } else {
            this.mHwOldGpsLogServices.addGeofenceStatus();
        }
    }

    public void addBatchingStatus() {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.addBatchingStatus();
        } else {
            this.mHwOldGpsLogServices.addBatchingStatus();
        }
    }

    public void updateGpsRunState(int status) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.updateGpsRunState(status);
        } else {
            this.mHwOldGpsLogServices.updateGpsRunState(status);
        }
    }

    public void updateLocation(Location location, long time, String provider) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.updateLocation(location, time, provider);
            if (this.mGeolocationCollectEnable) {
                this.mGnssDataCollector.updateLocation(location, time, provider);
                return;
            }
            return;
        }
        this.mHwOldGpsLogServices.updateLocation(location, time, provider);
    }

    public void updateSvStatus(int svCount, int[] svs, float[] snrs, float[] svElevations, float[] svAzimuths) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.updateSvStatus(svCount, svs, snrs, svElevations, svAzimuths);
        } else {
            Log.d(TAG, "updateSvStatus api has changed in android N (Huawei Emui 5.0), old chr version no longger supported in android N(Huawe Emui 5.0");
        }
    }

    public void reportErrorNtpTime(long currentNtpTime, long realTime) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.reportErrorNtpTime(currentNtpTime, realTime);
        } else {
            this.mHwOldGpsLogServices.reportErrorNtpTime(currentNtpTime, realTime);
        }
    }

    public void reportBinderError() {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.reportBinderError();
        }
    }

    public void processGnssHalDriverEvent(String strJsonExceptionBody) {
        if (this.mNewGpsChrEnable) {
            this.mHwGpsSessionRecorder.processGnssHalDriverEvent(strJsonExceptionBody);
        } else {
            this.mHwOldGpsLogServices.processGnssHalDriverEvent(strJsonExceptionBody);
        }
    }

    public void updateModemData(byte aPosMode, byte aAidingDataStatus, byte aAidingDataReqFlg, byte[] aCurNetStatus, byte aAGPSResult, byte aSUPLStatus, byte aTimeFlg, byte aAddrFlg, byte[] aServerAdder, long aSUPLStatusCode, long aAgpsStartTime, long aAtlOpenTime, long aConnSvrTime, long aAgpsEndTime, short aServerIpPort) {
    }

    public void updateNtpServerInfo(String address) {
        if (address != null) {
            this.mGnssDataCollector.updateNtpServerInfo(address);
            this.mHwGpsSessionRecorder.updateNtpServerInfo(address);
        }
    }

    public void injectExtraParam(String extraParam) {
        this.mHwGpsSessionRecorder.injectExtraParam(extraParam);
    }
}
