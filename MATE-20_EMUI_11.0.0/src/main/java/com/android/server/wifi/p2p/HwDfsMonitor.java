package com.android.server.wifi.p2p;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.MacAddress;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.IWifiActionListener;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.IMonitor;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.MSS.HwMSSUtils;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class HwDfsMonitor {
    private static final int BAND_WIDTH_160MHZ = 160;
    private static final int BAND_WIDTH_INDEX = 2;
    private static final int BAND_WIDTH_WEIGTH = 10;
    private static final int CAC_RESULT_BACK_80M = 7;
    private static final int CAC_RESULT_BAND_MASK = 255;
    private static final int CAC_RESULT_CAC_DETECT = 3;
    private static final int CAC_RESULT_CAC_START = 1;
    private static final int CAC_RESULT_CHANNEL_INDEX = 8;
    private static final int CAC_RESULT_CHANNEL_MASK = 65280;
    private static final int CAC_RESULT_INS_DETECT = 4;
    private static final int CAC_RESULT_STATUS_INDEX = 16;
    private static final int CAC_RESULT_STATUS_MASK = 16711680;
    private static final int CHR_EID_HWSHARE_160M = 909002087;
    private static final int CMD_CLOSE_GO_CAC = 133;
    private static final int CMD_GET_GO_CAC_STATUS = 134;
    private static final long CURRENT_TIME_MASK_HIGH = -4294967296L;
    private static final long CURRENT_TIME_MASK_LOW = 4294967295L;
    private static final long DATA_TRAFFIC_SPEED_UNIT = 1024;
    private static final int FLASH_TYPE_DEFAULT = 7;
    private static final int GET_GO_CAC_CMD_LENGTH = 2;
    private static final int GPS_SATELLITE_FIXED_MAX = 30;
    private static final int GPS_SATELLITE_FIXED_MIN = 10;
    private static final int GPS_SATELLITE_FOUND = 35;
    private static final int GPS_SATELLITE_THRESHHOLD = 5;
    private static final int INVALID_VALUE = -1;
    private static final boolean IS_CHINA_AREA = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    private static final int LINK_SPEED_COUNT_MAX = 10;
    private static final int LINK_SPEED_COUNT_MIN = 1;
    private static final int LINK_SPEED_UPDATE_TIME_OUT_MSEC = 1000;
    private static final int LOCAL_DFS_INFO_NEIGHBOR_BSSID_NUM = 12;
    private static final int LOCAL_DFS_INFO_NEIGHBOR_BSSID_SAME_NUM = 2;
    private static final int LOCAL_DFS_INFO_NUM = 2;
    private static final long LOCAL_DFS_INFO_UNUSABLE_TIME = 86400000;
    private static final int LOCATION_UPDATE_DISTANCE = 1;
    private static final int LOCATION_UPDATE_INTEVAL = 1000;
    private static final int MSG_P2P_LINKSPEED_POLL = 3;
    private static final int MSG_REGISTER_LOCATION_UPDATE = 0;
    private static final int MSG_REMOVE_LOCATION_UPDATE = 2;
    private static final int MSG_REQUEST_LOCATION_UPDATE = 1;
    private static final int MS_TO_S = 1000;
    private static final int REMOVE_LOCATION_UPDATE_TIME_OUT_MSEC = 5000;
    private static final int STATUS_NOK = 0;
    private static final int STATUS_OK = 1;
    private static final int STATUS_UNKNOWN = 2;
    private static final String TAG = "HwDfsMonitor";
    private static final long UNUSABLE_LOCAL_DFS_PERIOD_ONE = 43200000;
    private static final long UNUSABLE_LOCAL_DFS_PERIOD_TWO = 7200000;
    private static final long USABLE_LOCAL_DFS_PERIOD_ONE = 3600000;
    private static final long USABLE_LOCAL_DFS_PERIOD_TWO = 1800000;
    private static final String WIFI_INTERFACE = SystemProperties.get("wifi.interface", "wlan0");
    private static volatile HwDfsMonitor sHwDfsMonitor = null;
    private AlarmManager mAlarmManager;
    private int mAvarageLinkSpeed = 0;
    private Context mContext;
    private int mDfsAcceptReason = -1;
    private DfsBroadcastReceiver mDfsBroadcastReceiver;
    private Handler mDfsHandler;
    private int mDfsRejectReason = -1;
    private DfsStatus mDfsStatus = new DfsStatus();
    private GnssStatus.Callback mGnssStatusCallback = new GnssStatus.Callback() {
        /* class com.android.server.wifi.p2p.HwDfsMonitor.AnonymousClass2 */

        @Override // android.location.GnssStatus.Callback
        public void onSatelliteStatusChanged(GnssStatus status) {
            HwDfsMonitor.this.calculateGpsSatellite(status);
        }
    };
    private int mGpsSatelliteFoundCount = -1;
    private int mGpsSatelliteUsedCount = -1;
    private boolean mIsBootCompleted = false;
    private boolean mIsGnssCallbackRegistered = false;
    private int mLinkSpeedCount = 0;
    private HashMap<Integer, LocalDfsInfo> mLocalDfsInfo = new HashMap<>(2);
    private LocationListener mLocationListener = new LocationListener() {
        /* class com.android.server.wifi.p2p.HwDfsMonitor.AnonymousClass1 */

        @Override // android.location.LocationListener
        public void onLocationChanged(Location location) {
            HwHiLog.d(HwDfsMonitor.TAG, false, "location changed", new Object[0]);
        }

        @Override // android.location.LocationListener
        public void onStatusChanged(String provider, int status, Bundle extras) {
            HwHiLog.d(HwDfsMonitor.TAG, false, "status changed", new Object[0]);
        }

        @Override // android.location.LocationListener
        public void onProviderEnabled(String provider) {
            HwHiLog.d(HwDfsMonitor.TAG, false, "provider enabled", new Object[0]);
        }

        @Override // android.location.LocationListener
        public void onProviderDisabled(String provider) {
            HwHiLog.d(HwDfsMonitor.TAG, false, "provider disabled", new Object[0]);
        }
    };
    private LocationManager mLocationManager;
    private List<String> mLocationNeighborBssids = new ArrayList(12);
    private long mP2pFinishTime = 0;
    private long mP2pStartRxBytes = 0;
    private long mP2pStartTime = 0;
    private long mP2pStartTxBytes = 0;
    private int mUse160State = DfsUseState.HWSHARE_80M.getValue();
    private int mWifiBandWidthBefore = 0;
    private int mWifiChannelBefore = 0;
    private WifiManager mWifiManager;
    private int mWifiMode = WifiMode.NO_CHANGE.getValue();
    private WifiP2pGroup mWifiP2pGroup;

    static /* synthetic */ int access$608(HwDfsMonitor x0) {
        int i = x0.mLinkSpeedCount;
        x0.mLinkSpeedCount = i + 1;
        return i;
    }

    static /* synthetic */ int access$712(HwDfsMonitor x0, int x1) {
        int i = x0.mAvarageLinkSpeed + x1;
        x0.mAvarageLinkSpeed = i;
        return i;
    }

    private HwDfsMonitor(Context context) {
        this.mContext = context;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mAlarmManager = (AlarmManager) this.mContext.getSystemService("alarm");
        initDfsHandler();
        registerDfsBroadcastReceiver();
    }

    public static HwDfsMonitor createHwDfsMonitor(Context context) {
        if (sHwDfsMonitor == null) {
            synchronized (HwDfsMonitor.class) {
                if (sHwDfsMonitor == null) {
                    sHwDfsMonitor = new HwDfsMonitor(context);
                }
            }
        }
        return sHwDfsMonitor;
    }

    /* access modifiers changed from: private */
    public enum WifiMode {
        NO_CHANGE(0),
        DISCONNECT_AND_NO_RECONNECT(1),
        DISCONNECT_CAN_RECONNECT(2);
        
        private int mWifiMode;

        private WifiMode(int wifiMode) {
            this.mWifiMode = wifiMode;
        }

        public int getValue() {
            return this.mWifiMode;
        }
    }

    /* access modifiers changed from: private */
    public enum DfsUseState {
        HWSHARE_80M(0),
        FRAMEWORK_80M(1),
        DRIVER_160M(2),
        DRIVER_VAP_80M(3),
        DRIVER_DFS_80M(4),
        HWSHARE_24G(5);
        
        private int mDfsUseState;

        private DfsUseState(int dfsUseState) {
            this.mDfsUseState = dfsUseState;
        }

        public int getValue() {
            return this.mDfsUseState;
        }
    }

    /* access modifiers changed from: private */
    public enum DfsAcceptReason {
        HISTORY_CAC_ACCEPT(0),
        HISTORY_CLOUD_ACCEPT(1),
        SCAN_RSULT(2),
        INDOOR(3),
        LATEST_CLOUD_ACCEPT(4);
        
        private int mDfsAcceptReason;

        private DfsAcceptReason(int dfsAcceptReason) {
            this.mDfsAcceptReason = dfsAcceptReason;
        }

        public int getValue() {
            return this.mDfsAcceptReason;
        }
    }

    /* access modifiers changed from: private */
    public enum DfsRejectReason {
        HISTORY_CAC_REJECT(0),
        HISTORY_CLOUD_REJECT(1),
        COUNTRY_INVALID(2),
        LATEST_CLOUD_REJECT(3),
        UNKNOWN(4);
        
        private int mDfsRejectReason;

        private DfsRejectReason(int dfsRejectReason) {
            this.mDfsRejectReason = dfsRejectReason;
        }

        public int getValue() {
            return this.mDfsRejectReason;
        }
    }

    private void initDfsHandler() {
        HwHiLog.i(TAG, false, "initDfsHandler init", new Object[0]);
        this.mDfsHandler = new Handler(this.mContext.getMainLooper()) {
            /* class com.android.server.wifi.p2p.HwDfsMonitor.AnonymousClass3 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                int i = msg.what;
                if (i == 0) {
                    HwDfsMonitor.this.locationStatusChanged();
                } else if (i != 1) {
                    if (i == 2) {
                        HwHiLog.i(HwDfsMonitor.TAG, false, "removeUpdates", new Object[0]);
                        if (HwDfsMonitor.this.mLocationManager != null) {
                            HwDfsMonitor.this.mLocationManager.removeUpdates(HwDfsMonitor.this.mLocationListener);
                        }
                    } else if (i == 3 && HwDfsMonitor.this.mWifiP2pGroup != null) {
                        String interfaceName = HwDfsMonitor.this.mWifiP2pGroup.getInterface();
                        WifiInjector wifiInjector = WifiInjector.getInstance();
                        if (wifiInjector != null && HwDfsMonitor.this.mLinkSpeedCount < 10) {
                            HwDfsMonitor.access$712(HwDfsMonitor.this, wifiInjector.getWifiP2pNative().mHwWifiP2pNativeEx.getP2pLinkSpeed(interfaceName));
                            HwDfsMonitor.access$608(HwDfsMonitor.this);
                            sendEmptyMessageDelayed(3, 1000);
                        }
                    }
                } else if (HwDfsMonitor.this.mLocationManager != null && HwDfsMonitor.this.mLocationManager.isProviderEnabled("gps")) {
                    HwHiLog.i(HwDfsMonitor.TAG, false, "requestLocationUpdates", new Object[0]);
                    HwDfsMonitor.this.mLocationManager.requestLocationUpdates("gps", 1000, 1.0f, HwDfsMonitor.this.mLocationListener);
                    sendEmptyMessageDelayed(2, 5000);
                }
            }
        };
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void locationStatusChanged() {
        if (this.mLocationManager == null) {
            this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
            if (this.mLocationManager == null) {
                HwHiLog.e(TAG, false, "initLocation:mLocationManager is null", new Object[0]);
                return;
            }
        }
        if (this.mLocationManager.isProviderEnabled("gps")) {
            HwHiLog.d(TAG, false, "initLocation:Location is open, IsGnssCallbackRegistered = %{public}s", new Object[]{String.valueOf(this.mIsGnssCallbackRegistered)});
            if (!this.mIsGnssCallbackRegistered) {
                this.mIsGnssCallbackRegistered = true;
                this.mLocationManager.registerGnssStatusCallback(this.mGnssStatusCallback);
                return;
            }
            return;
        }
        HwHiLog.i(TAG, false, "initLocation:Location is not open", new Object[0]);
        if (this.mIsGnssCallbackRegistered) {
            this.mIsGnssCallbackRegistered = false;
            this.mLocationManager.unregisterGnssStatusCallback(this.mGnssStatusCallback);
        }
        this.mGpsSatelliteFoundCount = -1;
        this.mGpsSatelliteUsedCount = -1;
    }

    private void registerDfsBroadcastReceiver() {
        this.mDfsBroadcastReceiver = new DfsBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BOOT_COMPLETED");
        filter.addAction("android.location.PROVIDERS_CHANGED");
        filter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        this.mContext.registerReceiver(this.mDfsBroadcastReceiver, filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pConnectionChange(Intent intent) {
        NetworkInfo network = (NetworkInfo) intent.getParcelableExtra("networkInfo");
        if (network == null || !network.isConnected()) {
            this.mWifiP2pGroup = null;
            return;
        }
        this.mWifiP2pGroup = (WifiP2pGroup) intent.getParcelableExtra("p2pGroupInfo");
        this.mP2pStartTime = SystemClock.elapsedRealtime();
        this.mP2pStartTxBytes = TrafficStats.getTxBytes(WIFI_INTERFACE);
        this.mP2pStartRxBytes = TrafficStats.getRxBytes(WIFI_INTERFACE);
        updateWifiMode();
        this.mLinkSpeedCount = 0;
        this.mAvarageLinkSpeed = 0;
        Handler handler = this.mDfsHandler;
        if (handler != null) {
            handler.sendEmptyMessageDelayed(3, 1000);
        }
    }

    /* access modifiers changed from: private */
    public class DfsBroadcastReceiver extends BroadcastReceiver {
        private DfsBroadcastReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    HwDfsMonitor.this.mIsBootCompleted = true;
                    if (HwDfsMonitor.this.mDfsHandler != null) {
                        HwDfsMonitor.this.mDfsHandler.sendEmptyMessage(0);
                    }
                } else if ("android.location.PROVIDERS_CHANGED".equals(action)) {
                    if (HwDfsMonitor.this.mIsBootCompleted && HwDfsMonitor.this.mDfsHandler != null) {
                        HwDfsMonitor.this.mDfsHandler.sendEmptyMessage(0);
                    }
                } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    HwDfsMonitor.this.handleP2pConnectionChange(intent);
                } else {
                    HwHiLog.i(HwDfsMonitor.TAG, false, "receive other broadcast", new Object[0]);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public class DfsStatus {
        private int mBandWidth;
        private int mFrequency;
        private IWifiActionListener mListener;
        private int mStatue;

        private DfsStatus() {
            this.mFrequency = 0;
            this.mBandWidth = 0;
            this.mStatue = 2;
            this.mListener = null;
        }

        public void setFrequency(int frequency) {
            this.mFrequency = frequency;
        }

        public int getFrequency() {
            return this.mFrequency;
        }

        public void setBandWidth(int bandWidth) {
            this.mBandWidth = bandWidth;
        }

        public int getBandWidth() {
            return this.mBandWidth;
        }

        public void setStatue(int statue) {
            this.mStatue = statue;
        }

        public int getStatue() {
            return this.mStatue;
        }

        public void setListener(IWifiActionListener listener) {
            this.mListener = listener;
        }

        public IWifiActionListener getListener() {
            return this.mListener;
        }
    }

    /* access modifiers changed from: private */
    public class LocalDfsInfo {
        private int mEndChannel;
        private List<String> mNeighborBssidList;
        private int mStartChannel;
        private int mStatus;
        private long mTimeStamp;

        private LocalDfsInfo() {
            this.mNeighborBssidList = new ArrayList(12);
            this.mStartChannel = 0;
            this.mEndChannel = 0;
            this.mStatus = 2;
            this.mTimeStamp = 0;
        }

        public void setNeighborBssidList(List<String> bssidList) {
            this.mNeighborBssidList.clear();
            this.mNeighborBssidList.addAll(bssidList);
        }

        public List<String> getNeighborBssidList() {
            return this.mNeighborBssidList;
        }

        public void setStartChannel(int startChannel) {
            this.mStartChannel = startChannel;
        }

        public int getStartChannel() {
            return this.mStartChannel;
        }

        public void setEndChannel(int endChannel) {
            this.mEndChannel = endChannel;
        }

        public int getEndChannel() {
            return this.mEndChannel;
        }

        public void setStatus(int status) {
            this.mStatus = status;
        }

        public int getStatus() {
            return this.mStatus;
        }

        public void setTimeStamp(long timeStamp) {
            this.mTimeStamp = timeStamp;
        }

        public long getTimeStamp() {
            return this.mTimeStamp;
        }
    }

    private boolean isDfsUsableFromScanResult() {
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        if (scanResults == null || scanResults.size() == 0) {
            HwHiLog.e(TAG, false, "isDfsUsableFromScanResult:scanResults is empty", new Object[0]);
            return false;
        }
        boolean isUsableCh52 = false;
        boolean isUsableCh56 = false;
        boolean isUsableCh60 = false;
        boolean isUsableCh64 = false;
        for (ScanResult scanResult : scanResults) {
            if ((scanResult.channelWidth == 2 && scanResult.frequency >= 5260 && scanResult.frequency <= 5320) || (scanResult.channelWidth == 3 && scanResult.frequency >= 5180 && scanResult.frequency <= 5320)) {
                HwHiLog.i(TAG, false, "isDfsUsableFromScanResult:bandWidth covers DFS channel", new Object[0]);
                return true;
            }
            int i = scanResult.frequency;
            if (i == 5260) {
                isUsableCh52 = true;
                isUsableCh56 = scanResult.channelWidth == 1 ? true : isUsableCh56;
            } else if (i == 5280) {
                isUsableCh56 = true;
                isUsableCh52 = scanResult.channelWidth == 1 ? true : isUsableCh52;
            } else if (i == 5300) {
                isUsableCh60 = true;
                isUsableCh64 = scanResult.channelWidth == 1 ? true : isUsableCh64;
            } else if (i == 5320) {
                isUsableCh64 = true;
                isUsableCh60 = scanResult.channelWidth == 1 ? true : isUsableCh60;
            }
            if (isUsableCh52 && isUsableCh56 && isUsableCh60 && isUsableCh64) {
                HwHiLog.i(TAG, false, "isDfsUsableFromScanResult:every DFS channel is usable", new Object[0]);
                return true;
            }
        }
        return false;
    }

    private boolean isInLocalDfsTime(long currentTime, long okStatusTime, long nokStatusTime) {
        if ((currentTime <= UNUSABLE_LOCAL_DFS_PERIOD_ONE + nokStatusTime || currentTime >= USABLE_LOCAL_DFS_PERIOD_ONE + okStatusTime) && (currentTime <= UNUSABLE_LOCAL_DFS_PERIOD_TWO + nokStatusTime || currentTime >= USABLE_LOCAL_DFS_PERIOD_TWO + okStatusTime)) {
            return false;
        }
        HwHiLog.i(TAG, false, "DFS status is ok according to LocalDfsInfo", new Object[0]);
        return true;
    }

    private boolean isSameLocation(List<String> neighborBssids) {
        if (this.mWifiManager == null || neighborBssids == null || neighborBssids.size() == 0) {
            HwHiLog.e(TAG, false, "isSameLocation:neighborBssids is empty", new Object[0]);
            return false;
        }
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        if (scanResults == null || scanResults.size() == 0) {
            HwHiLog.e(TAG, false, "isSameLocation:scanResults is empty", new Object[0]);
            return false;
        }
        int neighborBssidFoundNum = 0;
        HwHiLog.i(TAG, false, "isSameLocation neighborBssids=%{public}d", new Object[]{Integer.valueOf(neighborBssids.size())});
        for (String neighborBssid : neighborBssids) {
            boolean isNeighborBssidFound = false;
            for (ScanResult scanResult : scanResults) {
                if (scanResult.BSSID != null && scanResult.BSSID.equals(neighborBssid)) {
                    neighborBssidFoundNum++;
                    isNeighborBssidFound = true;
                }
                if (neighborBssidFoundNum < 2) {
                    if (isNeighborBssidFound) {
                        break;
                    }
                } else {
                    HwHiLog.i(TAG, false, "isSameLocation sameBssidNum=%{public}d", new Object[]{Integer.valueOf(neighborBssidFoundNum)});
                    return true;
                }
            }
        }
        HwHiLog.i(TAG, false, "isSameLocation sameBssidNum=%{public}d", new Object[]{Integer.valueOf(neighborBssidFoundNum)});
        return false;
    }

    private int getDfsStatusFromLocalDfsInfo() {
        long okStatusTime;
        boolean isOkStatusUsable;
        LocalDfsInfo okLocalDfsInfo;
        long nokStatusTime;
        boolean isBadStatusUsable;
        boolean isBadStatusExist;
        boolean isBadStatusUsable2;
        HashMap<Integer, LocalDfsInfo> hashMap = this.mLocalDfsInfo;
        if (hashMap == null || hashMap.isEmpty()) {
            HwHiLog.e(TAG, false, "LocalDfsInfo is null", new Object[0]);
            return 2;
        }
        long currentTime = SystemClock.elapsedRealtime();
        if (this.mLocalDfsInfo.containsKey(1)) {
            LocalDfsInfo okLocalDfsInfo2 = this.mLocalDfsInfo.get(1);
            long okStatusTime2 = okLocalDfsInfo2.getTimeStamp();
            if (!isSameLocation(okLocalDfsInfo2.getNeighborBssidList()) || okStatusTime2 + LOCAL_DFS_INFO_UNUSABLE_TIME <= currentTime) {
                isOkStatusUsable = false;
            } else {
                isOkStatusUsable = true;
            }
            okLocalDfsInfo = 1;
            okStatusTime = okStatusTime2;
        } else {
            okLocalDfsInfo = null;
            isOkStatusUsable = false;
            okStatusTime = 0;
        }
        if (this.mLocalDfsInfo.containsKey(0)) {
            LocalDfsInfo nokLocalDfsInfo = this.mLocalDfsInfo.get(0);
            long nokStatusTime2 = nokLocalDfsInfo.getTimeStamp();
            if (!isSameLocation(nokLocalDfsInfo.getNeighborBssidList()) || LOCAL_DFS_INFO_UNUSABLE_TIME + nokStatusTime2 <= currentTime) {
                isBadStatusUsable2 = false;
            } else {
                isBadStatusUsable2 = true;
            }
            isBadStatusExist = true;
            isBadStatusUsable = isBadStatusUsable2;
            nokStatusTime = nokStatusTime2;
        } else {
            isBadStatusExist = false;
            isBadStatusUsable = false;
            nokStatusTime = 0;
        }
        if ((okLocalDfsInfo != null || isBadStatusExist) && (isOkStatusUsable || isBadStatusUsable)) {
            if ((okLocalDfsInfo != null && isOkStatusUsable && (!isBadStatusExist || (isBadStatusExist && !isBadStatusUsable))) || (okLocalDfsInfo != null && isOkStatusUsable && isBadStatusExist && isBadStatusUsable && isInLocalDfsTime(currentTime, okStatusTime, nokStatusTime))) {
                return 1;
            }
            return 0;
        }
        HwHiLog.i(TAG, false, "LocalDfsInfo is unusable", new Object[0]);
        return 2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void calculateGpsSatellite(GnssStatus status) {
        Handler handler;
        this.mGpsSatelliteFoundCount = 0;
        this.mGpsSatelliteUsedCount = 0;
        for (int index = 0; index < status.getSatelliteCount(); index++) {
            int constellationType = status.getConstellationType(index);
            if (constellationType == 3 || constellationType == 5 || constellationType == 2 || constellationType == 1 || constellationType == 4) {
                this.mGpsSatelliteFoundCount++;
                if (status.usedInFix(index)) {
                    this.mGpsSatelliteUsedCount++;
                }
            }
        }
        this.mLocationNeighborBssids.clear();
        this.mLocationNeighborBssids.addAll(updateNeighborBssidList());
        HwHiLog.i(TAG, false, "mGpsSatelliteFoundCount=%{public}d mGpsSatelliteUsedCount=%{public}d", new Object[]{Integer.valueOf(this.mGpsSatelliteFoundCount), Integer.valueOf(this.mGpsSatelliteUsedCount)});
        if (isOutdoor() && (handler = this.mDfsHandler) != null && handler.hasMessages(2)) {
            this.mDfsHandler.removeMessages(2);
            this.mDfsHandler.sendEmptyMessage(2);
        }
    }

    private boolean isOutdoor() {
        int i = this.mGpsSatelliteFoundCount;
        if (i <= 35 || i >= this.mGpsSatelliteUsedCount + 5) {
            return false;
        }
        HwHiLog.i(TAG, false, "now is Outdoor", new Object[0]);
        return true;
    }

    private boolean isIndoor() {
        int i;
        int i2 = this.mGpsSatelliteFoundCount;
        if (i2 < 0 || (i = this.mGpsSatelliteUsedCount) < 0 || (i > 10 && i2 <= i + 5 && (i2 <= 35 || i >= 30))) {
            return false;
        }
        HwHiLog.i(TAG, false, "is Indoor", new Object[0]);
        return true;
    }

    private void notifyListenerDfsStatus(IWifiActionListener listener, boolean isSuccessful, int dfsStatus) {
        if (listener == null) {
            return;
        }
        if (isSuccessful) {
            try {
                listener.onSuccess();
            } catch (RemoteException e) {
                HwHiLog.e(TAG, false, "Exceptions happen when notufy listener", new Object[0]);
            }
        } else {
            listener.onFailure(dfsStatus);
        }
    }

    public boolean requestDfsStatus(int frequency, int bandWidth, IWifiActionListener listener) {
        WifiManager wifiManager;
        HwHiLog.i(TAG, false, "requestDfsStatus: freq=%{public}d BW=%{public}d", new Object[]{Integer.valueOf(frequency), Integer.valueOf(bandWidth)});
        this.mDfsAcceptReason = -1;
        this.mDfsRejectReason = -1;
        this.mDfsStatus.setStatue(2);
        boolean isChipSupported = HwMSSUtils.is1105();
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (!isChipSupported || (wifiManager = this.mWifiManager) == null || wifiInjector == null) {
            HwHiLog.i(TAG, false, "requestDfsStatus: Chip do not support", new Object[0]);
            this.mDfsStatus.setStatue(0);
            notifyListenerDfsStatus(listener, false, 0);
            return false;
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            this.mWifiChannelBefore = WifiCommonUtils.convertFrequencyToChannelNumber(wifiInfo.getFrequency());
            this.mWifiBandWidthBefore = getBandWidth(wifiInfo.getBSSID());
        }
        String countryCode = wifiInjector.getWifiCountryCode().getCountryCodeSentToDriver();
        HwHiLog.i(TAG, false, "requestDfsStatus countryCode=%{private}s", new Object[]{countryCode});
        if (!IS_CHINA_AREA || !"CN".equals(countryCode)) {
            HwHiLog.i(TAG, false, "requestDfsStatus: area is invalid", new Object[0]);
            this.mDfsStatus.setStatue(0);
            notifyListenerDfsStatus(listener, false, 0);
            this.mDfsRejectReason = DfsRejectReason.COUNTRY_INVALID.getValue();
            return false;
        } else if (bandWidth != BAND_WIDTH_160MHZ || frequency < 5180 || frequency > 5320) {
            HwHiLog.i(TAG, false, "requestDfsStatus: frequency or bandWidth is invalid", new Object[0]);
            this.mDfsStatus.setStatue(0);
            notifyListenerDfsStatus(listener, false, 0);
            return false;
        } else {
            boolean isGpsSameDataValid = this.mGpsSatelliteFoundCount > 0 && isSameLocation(this.mLocationNeighborBssids);
            if (getDfsStatusFromLocalDfsInfo() == 2 && !isGpsSameDataValid) {
                HwHiLog.i(TAG, false, "requestDfsStatus: LocalDfsInfo and GpsInfo are invalid", new Object[0]);
                Handler handler = this.mDfsHandler;
                if (handler != null) {
                    handler.sendEmptyMessage(1);
                }
            }
            this.mDfsStatus.setFrequency(frequency);
            this.mDfsStatus.setBandWidth(bandWidth);
            this.mDfsStatus.setListener(listener);
            return true;
        }
    }

    public boolean isDfsUsable(int frequency) {
        int i;
        if (ScanResult.is24GHz(frequency)) {
            i = DfsUseState.HWSHARE_24G.getValue();
        } else {
            i = DfsUseState.HWSHARE_80M.getValue();
        }
        this.mUse160State = i;
        if (frequency < 5180 || frequency > 5320) {
            HwHiLog.e(TAG, false, "isDfsUsable: frequency is invalid", new Object[0]);
            this.mUse160State = ScanResult.is5GHz(frequency) ? DfsUseState.FRAMEWORK_80M.getValue() : this.mUse160State;
            return false;
        }
        int dfsStatus = this.mDfsStatus.getStatue();
        HwHiLog.i(TAG, false, "isDfsUsable: dfsStatus=%{public}d", new Object[]{Integer.valueOf(dfsStatus)});
        if (dfsStatus == 0) {
            this.mUse160State = DfsUseState.FRAMEWORK_80M.getValue();
            return false;
        }
        int dfsStatusFromLocalDfsInfo = getDfsStatusFromLocalDfsInfo();
        HwHiLog.i(TAG, false, "isDfsUsable: dfsStatusFromLocalDfsInfo=%{public}d", new Object[]{Integer.valueOf(dfsStatusFromLocalDfsInfo)});
        if (dfsStatusFromLocalDfsInfo != 2) {
            this.mDfsStatus.setStatue(dfsStatusFromLocalDfsInfo);
        }
        if (this.mDfsStatus.getStatue() == 0) {
            notifyListenerDfsStatus(this.mDfsStatus.getListener(), false, 0);
            this.mUse160State = DfsUseState.FRAMEWORK_80M.getValue();
            this.mDfsRejectReason = DfsRejectReason.HISTORY_CAC_REJECT.getValue();
            return false;
        }
        if (this.mDfsStatus.getStatue() == 1) {
            this.mDfsAcceptReason = DfsAcceptReason.HISTORY_CAC_ACCEPT.getValue();
        } else if (isDfsUsableFromScanResult()) {
            this.mDfsStatus.setStatue(1);
            this.mDfsAcceptReason = DfsAcceptReason.SCAN_RSULT.getValue();
        } else if (isIndoor()) {
            this.mDfsStatus.setStatue(1);
            this.mDfsAcceptReason = DfsAcceptReason.INDOOR.getValue();
        } else {
            HwHiLog.i(TAG, false, "isDfsUsable: can not sure", new Object[0]);
        }
        if (this.mDfsStatus.getStatue() == 1) {
            notifyListenerDfsStatus(this.mDfsStatus.getListener(), true, 1);
            return true;
        }
        this.mUse160State = DfsUseState.FRAMEWORK_80M.getValue();
        this.mDfsRejectReason = DfsRejectReason.UNKNOWN.getValue();
        return false;
    }

    private int[] getLocalDfsInfo() {
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (wifiInjector == null) {
            HwHiLog.e(TAG, false, "getLocalDfsInfo called when wifiInjector is null", new Object[0]);
            return new int[0];
        }
        byte[] buff = {0};
        int[] goCacResult = new int[2];
        for (int index = 0; index < 2; index++) {
            goCacResult[index] = wifiInjector.getWifiNative().mHwWifiNativeEx.sendCmdToDriver(WIFI_INTERFACE, CMD_GET_GO_CAC_STATUS + index, buff);
            HwHiLog.i(TAG, false, "goCacReslt=%{public}d", new Object[]{Integer.valueOf(goCacResult[index])});
        }
        return goCacResult;
    }

    private boolean isLocalAddr(String address) {
        if (TextUtils.isEmpty(address)) {
            HwHiLog.e(TAG, false, "isLocalAddr: address is empty", new Object[0]);
            return false;
        }
        try {
            if ((MacAddress.byteAddrFromStringAddr(address)[0] & 2) != 0) {
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            HwHiLog.e(TAG, false, "isLocalAddr: exception happens", new Object[0]);
        }
    }

    private List<String> updateNeighborBssidList() {
        List<ScanResult> scanResults = this.mWifiManager.getScanResults();
        if (scanResults == null || scanResults.size() == 0) {
            HwHiLog.e(TAG, false, "updateNeighborBssidList:scanResults is empty", new Object[0]);
            return Collections.emptyList();
        }
        List<String> neighborBssidList = new ArrayList<>(12);
        int neighborBssidNum = 0;
        for (ScanResult scanResult : scanResults) {
            if (!isLocalAddr(scanResult.BSSID)) {
                neighborBssidList.add(scanResult.BSSID);
                neighborBssidNum++;
                if (neighborBssidNum == 12) {
                    break;
                }
            }
        }
        return neighborBssidList;
    }

    private void processLocalDfsInfo(int[] goCacResult) {
        int dfsStatus;
        int i;
        if (goCacResult.length != 2) {
            HwHiLog.e(TAG, false, "updateDfsStatus: goCacResult is invalid", new Object[0]);
            return;
        }
        int cacStatusCmdResult = goCacResult[0];
        if (cacStatusCmdResult <= 0) {
            HwHiLog.e(TAG, false, "updateDfsStatus: cacStatusCmdResult=%{public}d is invalid", new Object[]{Integer.valueOf(cacStatusCmdResult)});
            return;
        }
        int channel = (CAC_RESULT_CHANNEL_MASK & cacStatusCmdResult) >> 8;
        if (channel < 36 || channel > 64) {
            HwHiLog.e(TAG, false, "updateDfsStatus: channel=%{public}d is invalid", new Object[]{Integer.valueOf(channel)});
            return;
        }
        int cacResult = (CAC_RESULT_STATUS_MASK & cacStatusCmdResult) >> 16;
        if (cacResult < 1 || cacResult > 7) {
            HwHiLog.i(TAG, false, "updateDfsStatus: dfsStatus is unknown, do not save it", new Object[0]);
            return;
        }
        if (cacResult == 3 || cacResult == 4) {
            dfsStatus = 0;
            this.mUse160State = DfsUseState.DRIVER_DFS_80M.getValue();
        } else {
            dfsStatus = 1;
            if (cacResult == 7) {
                i = DfsUseState.DRIVER_VAP_80M.getValue();
            } else {
                i = DfsUseState.DRIVER_160M.getValue();
            }
            this.mUse160State = i;
        }
        LocalDfsInfo newLocalDfsInfo = new LocalDfsInfo();
        newLocalDfsInfo.setStartChannel(36);
        newLocalDfsInfo.setEndChannel(64);
        newLocalDfsInfo.setNeighborBssidList(updateNeighborBssidList());
        HwHiLog.i(TAG, false, "updateDfsStatus: channel=%{public}d cacResult=%{public}d", new Object[]{Integer.valueOf(channel), Integer.valueOf(cacResult)});
        newLocalDfsInfo.setStatus(dfsStatus);
        long currentTime = System.currentTimeMillis();
        long occurTime = (((currentTime / 1000) & CURRENT_TIME_MASK_HIGH) + (((long) goCacResult[1]) & CURRENT_TIME_MASK_LOW)) * 1000;
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (currentTime <= occurTime || (elapsedRealtime + occurTime) - currentTime <= 0 || currentTime - occurTime >= LOCAL_DFS_INFO_UNUSABLE_TIME) {
            newLocalDfsInfo.setTimeStamp(-86400000);
        } else {
            newLocalDfsInfo.setTimeStamp((elapsedRealtime + occurTime) - currentTime);
        }
        this.mLocalDfsInfo.put(Integer.valueOf(dfsStatus), newLocalDfsInfo);
    }

    public void updateDfsStatus(int transferResult, int transferRate) {
        HwHiLog.i(TAG, false, "updateDfsStatus is called", new Object[0]);
        processLocalDfsInfo(getLocalDfsInfo());
        updateChrInfo(transferResult, transferRate);
        this.mUse160State = DfsUseState.HWSHARE_80M.getValue();
        this.mWifiMode = WifiMode.NO_CHANGE.getValue();
        this.mDfsAcceptReason = -1;
        this.mDfsRejectReason = -1;
    }

    public void closeGoCac(int state) {
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (wifiInjector == null) {
            HwHiLog.e(TAG, false, "closeGoCac called when wifiInjector is null", new Object[0]);
            return;
        }
        HwHiLog.d(TAG, false, "closeGoCac: cacState = %{public}d", new Object[]{Integer.valueOf(state)});
        if (wifiInjector.getWifiNative().mHwWifiNativeEx.sendCmdToDriver(WIFI_INTERFACE, (int) CMD_CLOSE_GO_CAC, new byte[]{(byte) state}) < 0) {
            HwHiLog.e(TAG, false, "close GO CAC command error", new Object[0]);
        }
    }

    private void updateWifiMode() {
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (wifiInjector == null) {
            HwHiLog.e(TAG, false, "updateWifiMode called when wifiInjector is null", new Object[0]);
            return;
        }
        int wifiMode = wifiInjector.getClientModeImpl().getWifiMode();
        if ((wifiMode & 1) == 0) {
            this.mWifiMode = WifiMode.NO_CHANGE.getValue();
        } else if ((wifiMode & 2) != 0) {
            this.mWifiMode = WifiMode.DISCONNECT_AND_NO_RECONNECT.getValue();
        } else {
            this.mWifiMode = WifiMode.DISCONNECT_CAN_RECONNECT.getValue();
        }
    }

    private int getBandWidth(String bssid) {
        WifiManager wifiManager;
        if (TextUtils.isEmpty(bssid) || (wifiManager = this.mWifiManager) == null) {
            return 0;
        }
        List<ScanResult> scanResults = wifiManager.getScanResults();
        if (scanResults == null || scanResults.size() == 0) {
            HwHiLog.e(TAG, false, "isDfsUsableFromScanResult:scanResults is empty", new Object[0]);
            return 0;
        }
        for (ScanResult scanResult : scanResults) {
            if (bssid.equalsIgnoreCase(scanResult.BSSID)) {
                return (2 << scanResult.channelWidth) * 10;
            }
        }
        return 0;
    }

    private int getP2pLinkSpeed() {
        HwHiLog.d(TAG, false, "getP2pLinkSpeed: mAvarageLinkSpeed=%{public}d mLinkSpeedCount=%{public}d", new Object[]{Integer.valueOf(this.mAvarageLinkSpeed), Integer.valueOf(this.mLinkSpeedCount)});
        int i = this.mLinkSpeedCount;
        if (i > 1) {
            return this.mAvarageLinkSpeed / i;
        }
        WifiInjector wifiInjector = WifiInjector.getInstance();
        if (wifiInjector == null || this.mWifiP2pGroup == null) {
            return 0;
        }
        return wifiInjector.getWifiP2pNative().mHwWifiP2pNativeEx.getP2pLinkSpeed(this.mWifiP2pGroup.getInterface());
    }

    private void updateChrInfo(int transferResult, int transferRate) {
        int trafficSpeed;
        WifiInfo wifiInfo;
        IMonitor.EventStream chrData = IMonitor.openEventStream((int) CHR_EID_HWSHARE_160M);
        if (chrData == null) {
            HwHiLog.e(TAG, false, "updateChrInfo: EID_HWSHARE_160M is null", new Object[0]);
            return;
        }
        int wifiChLoad = 0;
        int wifiChannel = 0;
        int wifiBandWidth = 0;
        WifiManager wifiManager = this.mWifiManager;
        if (!(wifiManager == null || (wifiInfo = wifiManager.getConnectionInfo()) == null)) {
            wifiChLoad = wifiInfo.getChload();
            wifiChannel = WifiCommonUtils.convertFrequencyToChannelNumber(wifiInfo.getFrequency());
            wifiBandWidth = getBandWidth(wifiInfo.getBSSID());
        }
        this.mP2pFinishTime = SystemClock.elapsedRealtime();
        int p2pLife = ((int) (this.mP2pFinishTime - this.mP2pStartTime)) / 1000;
        long txBytes = TrafficStats.getTxBytes(WIFI_INTERFACE);
        long rxBytes = TrafficStats.getRxBytes(WIFI_INTERFACE);
        long j = this.mP2pFinishTime;
        long j2 = this.mP2pStartTime;
        if (j > j2) {
            trafficSpeed = (int) (((((txBytes + rxBytes) - this.mP2pStartTxBytes) - this.mP2pStartRxBytes) * 1000) / ((j - j2) * DATA_TRAFFIC_SPEED_UNIT));
        } else {
            trafficSpeed = 0;
        }
        WifiP2pGroup wifiP2pGroup = this.mWifiP2pGroup;
        int p2pChannel = wifiP2pGroup != null ? WifiCommonUtils.convertFrequencyToChannelNumber(wifiP2pGroup.getFrequency()) : 0;
        int linkSpeed = getP2pLinkSpeed();
        chrData.setParam("P2PLIFE", p2pLife).setParam("THRU", transferRate).setParam("RESULT", transferResult & CAC_RESULT_BAND_MASK).setParam("ISCLOSESTA", this.mWifiMode).setParam("IS160", this.mUse160State).setParam("STACH1", this.mWifiChannelBefore).setParam("STABW1", this.mWifiBandWidthBefore).setParam("STACH2", wifiChannel).setParam("STABW2", wifiBandWidth).setParam("P2PCH", p2pChannel).setParam("GCSTACH", (CAC_RESULT_CHANNEL_MASK & transferResult) >> 8).setParam("DFSACC", this.mDfsAcceptReason).setParam("DFSREJ", this.mDfsRejectReason).setParam("FLASHTYPE", 7).setParam("P2PRSSI", 0).setParam("WIFIRATE", linkSpeed).setParam("WIFILOAD", wifiChLoad).setParam("STATRAFFIC", trafficSpeed);
        HwHiLog.d(TAG, false, "p2pLife=%{public}d ISCLOSESTA=%{public}d IS160=%{public}d STACH1=%{public}d STABW1=%{public}d STACH2=%{public}d STABW2=%{public}d P2PCH=%{public}d linkSpeed=%{public}d DFSACC=%{public}d DFSREJ=%{public}d WIFILOAD=%{public}d STATRAFFIC=%{public}d", new Object[]{Integer.valueOf(p2pLife), Integer.valueOf(this.mWifiMode), Integer.valueOf(this.mUse160State), Integer.valueOf(this.mWifiChannelBefore), Integer.valueOf(this.mWifiBandWidthBefore), Integer.valueOf(wifiChannel), Integer.valueOf(wifiBandWidth), Integer.valueOf(p2pChannel), Integer.valueOf(linkSpeed), Integer.valueOf(this.mDfsAcceptReason), Integer.valueOf(this.mDfsRejectReason), Integer.valueOf(wifiChLoad), Integer.valueOf(trafficSpeed)});
        IMonitor.sendEvent(chrData);
        IMonitor.closeEventStream(chrData);
    }
}
