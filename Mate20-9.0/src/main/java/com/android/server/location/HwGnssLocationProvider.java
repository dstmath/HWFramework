package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.ILocationManager;
import android.location.Location;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.IMonitor;
import android.util.Log;
import com.android.server.LocationManagerServiceUtil;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.huawei.cust.HwCfgFilePolicy;
import com.huawei.cust.HwCustUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.Map;
import java.util.Properties;

public class HwGnssLocationProvider extends GnssLocationProvider {
    private static final int AGPS_TYPE_SUPL = 1;
    private static final int DFT_APK_INFO_EVENT = 910009006;
    private static final int DFT_CHIP_ASSERT_EVENT = 910009014;
    private static final int DFT_GNSS_ERROR_EVENT = 910000007;
    private static final int DFT_GNSS_IDLE_ERROR = 111;
    private static final String GNSS_NAVIGATING_FLAG = "hw_higeo_gnss_Navigating";
    private static final int GNSS_REQ_CHANGE_START = 1;
    private static final int GNSS_REQ_CHANGE_STOP = 2;
    private static final String LOCATION_MAP_BAIDU_PACKAGE = "com.baidu.BaiduMap";
    private static final String LOCATION_MAP_FLP_PACKAGE = "com.amap.android.ams";
    private static final String LOCATION_MAP_GAODE_PACKAGE = "com.autonavi.minimap";
    private static final String LOCATION_MAP_GOOGLE_PACKAGE = "com.google.android.apps.maps";
    private static final String LOCATION_MAP_WAZE_PACKAGE = "com.waze";
    private static final String MAPS_LOCATION_FLAG = "hw_higeo_maps_location";
    private static final int MAX_IDLE_GPS_RECORD = 6;
    private static final int REPORT_IDLE_DELAY = 3000;
    private static final String TAG = "HwGnssLocationProvider";
    private boolean AUTO_ACC_ENABLE = SystemProperties.getBoolean("ro.config.hw_auto_acc_enable", false);
    private BroadcastHelper mBroadcastHelper = new BroadcastHelper();
    /* access modifiers changed from: private */
    public Context mContext;
    HwCustGpsLocationProvider mCust = ((HwCustGpsLocationProvider) HwCustUtils.createObj(HwCustGpsLocationProvider.class, new Object[]{this}));
    private IDeviceIdleController mDeviceIdleController;
    private long mEnterIdleTime;
    private boolean mGpsFirstFixed;
    private Handler mHandler;
    private Properties mHwProperties;
    private int mIsLastExistMapLocation;
    private String mIsLocalDBEnabled;
    private boolean mLastIdle = false;
    PowerManager mPowerManager;
    private int mPreferAccuracy;
    /* access modifiers changed from: private */
    public GpsLocationProviderUtils mUtils = EasyInvokeFactory.getInvokeUtils(GpsLocationProviderUtils.class);

    private class BroadcastHelper {
        BroadcastReceiver innerBroadcastReciever;

        public BroadcastHelper() {
            this.innerBroadcastReciever = new BroadcastReceiver(HwGnssLocationProvider.this) {
                public void onReceive(Context context, Intent intent) {
                    if (context == null || intent == null) {
                        Log.d(HwGnssLocationProvider.TAG, "context or intent is null,return.");
                        return;
                    }
                    if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                        String state = intent.getStringExtra("ss");
                        boolean GNSS_ADAPT_CARD = SystemProperties.getBoolean("ro.odm.config.gnss_adapt_card", false);
                        Log.d(HwGnssLocationProvider.TAG, " onReceive action state = " + state);
                        if ("LOADED".equals(state)) {
                            BroadcastHelper.this.checkAndSetAGpsParameter();
                            if (GNSS_ADAPT_CARD) {
                                Log.d(HwGnssLocationProvider.TAG, "set prop of gps daemon to 1");
                                SystemProperties.set("vendor.gps_daemon_reload", "1");
                            }
                        }
                        if (AwareJobSchedulerConstants.SIM_STATUS_ABSENT.equals(state) && GNSS_ADAPT_CARD) {
                            Log.d(HwGnssLocationProvider.TAG, "set prop of gps daemon to 0");
                            SystemProperties.set("vendor.gps_daemon_reload", "0");
                        }
                    }
                }
            };
        }

        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
            HwGnssLocationProvider.this.mContext.registerReceiver(this.innerBroadcastReciever, intentFilter);
        }

        /* access modifiers changed from: private */
        public void checkAndSetAGpsParameter() {
            Map agpsConfigMap = HwCfgFilePolicy.getFileConfig("xml/agps_config.xml");
            String suplPort = "";
            String suplHost = "";
            if (agpsConfigMap != null) {
                try {
                    Map portHostMap = (Map) agpsConfigMap.get("portHostKey");
                    if (portHostMap != null) {
                        suplPort = (String) portHostMap.get("supl_port");
                        suplHost = (String) portHostMap.get("supl_host");
                    }
                    if (!TextUtils.isEmpty(suplPort) && !TextUtils.isEmpty(suplHost)) {
                        HwGnssLocationProvider.this.mUtils.setSuplServerHost(HwGnssLocationProvider.this, suplHost);
                        HwGnssLocationProvider.this.mUtils.setSuplServerPort(HwGnssLocationProvider.this, Integer.parseInt(suplPort));
                        Log.d(HwGnssLocationProvider.TAG, "checkAGpsServer mSuplServerHost = " + HwGnssLocationProvider.this.mUtils.getSuplServerHost(HwGnssLocationProvider.this) + " mSuplServerPort = " + HwGnssLocationProvider.this.mUtils.getSuplServerPort(HwGnssLocationProvider.this));
                        HwGnssLocationProvider.this.mUtils.native_set_agps_server(HwGnssLocationProvider.this, 1, suplHost, HwGnssLocationProvider.this.mUtils.getSuplServerPort(HwGnssLocationProvider.this));
                    }
                } catch (ClassCastException e) {
                    Log.e(HwGnssLocationProvider.TAG, "ClassCastException occured", e);
                } catch (NumberFormatException e2) {
                    Log.e(HwGnssLocationProvider.TAG, "Unable to parse supl_port: " + suplPort, e2);
                }
            }
        }
    }

    enum MapNavigatingType {
        NOT_MAP,
        WHITE_MAP,
        BLACK_MAP
    }

    public HwGnssLocationProvider(Context context, ILocationManager ilocationManager, Looper looper) {
        super(context, ilocationManager, looper);
        this.mContext = context;
        this.mBroadcastHelper.init();
        this.mHwProperties = this.mUtils.getProperties(this);
        if (this.mHwProperties != null) {
            this.mIsLocalDBEnabled = this.mHwProperties.getProperty("LOCAL_DB");
            if (this.mIsLocalDBEnabled == null) {
                this.mIsLocalDBEnabled = "true";
            }
        }
        this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
        this.mHandler = this.mUtils.getHandler(this);
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
    }

    public boolean isLocalDBEnabled() {
        return "true".equals(this.mIsLocalDBEnabled);
    }

    public void initDefaultApnObserver(Handler handler) {
        this.mUtils.setDefaultApnObserver(this, new ContentObserver(handler) {
            public void onChange(boolean selfChange) {
                HwGnssLocationProvider.this.mUtils.setDefaultApn(HwGnssLocationProvider.this, HwGnssLocationProvider.this.mUtils.getDefaultApn(HwGnssLocationProvider.this));
            }
        });
    }

    public void reportContextStatus(int status) {
    }

    public int getPreferred_accuracy() {
        if (!this.AUTO_ACC_ENABLE) {
            return 0;
        }
        int accuracySet = 0;
        if (existLocationMap()) {
            accuracySet = 200;
        }
        Log.d(TAG, "getPreferred_accuracy:" + accuracySet);
        this.mPreferAccuracy = accuracySet;
        return accuracySet;
    }

    public boolean shouldReStartNavi() {
        if (!this.AUTO_ACC_ENABLE) {
            return false;
        }
        if ((this.mPreferAccuracy == 200 || !existLocationMap()) && (this.mPreferAccuracy != 200 || existLocationMap())) {
            return false;
        }
        return true;
    }

    private boolean existLocationMap() {
        WorkSource workSource = getWorkSource();
        if (workSource != null) {
            int numName = workSource.size();
            for (int i = 0; i < numName; i++) {
                if (workSource.getName(i).equals(LOCATION_MAP_GAODE_PACKAGE) || workSource.getName(i).equals(LOCATION_MAP_FLP_PACKAGE) || workSource.getName(i).equals(LOCATION_MAP_BAIDU_PACKAGE)) {
                    Log.d(TAG, "existLocationMap:true");
                    return true;
                }
            }
        }
        Log.d(TAG, "existLocationMap:false");
        return false;
    }

    public void handleGnssRequirementsChange(int reson) {
        if (reson == 1) {
            handleMapLocation(existLocationMapsForHigeo());
        } else if (reson == 2) {
            handleMapLocation(MapNavigatingType.NOT_MAP.ordinal());
        } else {
            Log.d(TAG, "handleGnssRequirementsChange reson not clear.");
        }
    }

    private void handleMapLocation(int mapNavigatingType) {
        int newState = mapNavigatingType;
        if (this.mIsLastExistMapLocation != newState) {
            this.mIsLastExistMapLocation = newState;
            Settings.Secure.putInt(this.mContext.getContentResolver(), MAPS_LOCATION_FLAG, newState);
            Log.d(TAG, "handleGnssRequirementsChange,existLocationMap = " + newState);
            return;
        }
        Log.d(TAG, "existLocationMap state is not change,ignor! isLastExistMapLocation:" + this.mIsLastExistMapLocation);
    }

    public void handleGnssNavigatingStateChange(boolean start) {
        Log.d(TAG, "handleGnssNavigateState,start : " + start);
    }

    public void startNavigatingPreparedHook() {
        if (this.mCust != null) {
            this.mUtils.setPositionMode(this, this.mCust.setPostionMode(this.mUtils.getPositionMode(this)));
        }
        this.mGpsFirstFixed = false;
    }

    public boolean sendExtraCommandHook(String command, boolean result) {
        boolean hwResult = result;
        if (this.mCust == null) {
            return hwResult;
        }
        boolean hwResult2 = this.mCust.sendPostionModeCommand(result, command);
        this.mUtils.setPositionMode(this, this.mCust.setPostionMode(this.mUtils.getPositionMode(this)));
        return hwResult2;
    }

    public void handleUpdateNetworkStateHook(NetworkInfo info) {
        boolean isRoaming = false;
        if (info != null) {
            isRoaming = info.isRoaming();
        }
        if (this.mCust != null) {
            this.mCust.setRoaming(isRoaming);
        }
    }

    private int existLocationMapsForHigeo() {
        WorkSource workSource = getWorkSource();
        if (workSource != null) {
            int numName = workSource.size();
            int i = 0;
            while (i < numName) {
                if ("com.google.android.apps.maps".equals(workSource.getName(i))) {
                    Log.d(TAG, "existLocationMapsForHigeo: black map");
                    return MapNavigatingType.BLACK_MAP.ordinal();
                } else if (LOCATION_MAP_GAODE_PACKAGE.equals(workSource.getName(i)) || LOCATION_MAP_FLP_PACKAGE.equals(workSource.getName(i)) || LOCATION_MAP_WAZE_PACKAGE.equals(workSource.getName(i)) || LOCATION_MAP_BAIDU_PACKAGE.equals(workSource.getName(i))) {
                    Log.d(TAG, "existLocationMapsForHigeo: white map");
                    return MapNavigatingType.WHITE_MAP.ordinal();
                } else {
                    i++;
                }
            }
        }
        return MapNavigatingType.NOT_MAP.ordinal();
    }

    /* access modifiers changed from: protected */
    public String getSvType(int svidWithFlag) {
        String result = "unknown";
        switch ((svidWithFlag >> 4) & 15) {
            case 1:
                result = "gps";
                break;
            case 2:
                result = "sbas";
                break;
            case 3:
                result = "glonass";
                break;
            case 4:
                result = "qzss";
                break;
            case 5:
                result = "beidou";
                break;
            case 6:
                result = "galileo";
                break;
        }
        return String.format("%-7s", new Object[]{result});
    }

    /* access modifiers changed from: protected */
    public boolean hwCheckLowPowerMode() {
        boolean isDeviceIdle = this.mPowerManager.isDeviceIdleMode();
        boolean needReport = false;
        boolean isInteractive = false;
        if (isDeviceIdle != this.mLastIdle) {
            this.mLastIdle = isDeviceIdle;
            isInteractive = this.mPowerManager.isInteractive();
            if (isDeviceIdle) {
                this.mEnterIdleTime = System.currentTimeMillis();
                Log.d(TAG, "hwCheckLowPowerMode isInteractive " + isInteractive);
                if (isInteractive) {
                    needReport = true;
                }
            }
            HwGpsLogServices.getInstance(this.mContext).idleChange(isDeviceIdle, isInteractive);
        }
        boolean isGnssStarted = false;
        boolean isExitIdle = false;
        boolean isGpsOrFusedStartedBySystem = false;
        if (isDeviceIdle) {
            isGnssStarted = this.mUtils.getGnssStarted(this);
            LocationManagerServiceUtil util = LocationManagerServiceUtil.getDefault();
            if (isGnssStarted && util != null) {
                isGpsOrFusedStartedBySystem = util.isGpsOrFusedStartedBySystem();
            }
            Log.d(TAG, "hwCheckLowPowerMode isGnssStarted: " + isGnssStarted + ", isGpsOrFusedStartedBySystem: " + isGpsOrFusedStartedBySystem);
            if (isGnssStarted) {
                try {
                    if (this.mDeviceIdleController != null && !isGpsOrFusedStartedBySystem) {
                        this.mDeviceIdleController.exitIdle("gps interaction");
                        isExitIdle = true;
                        needReport = true;
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "exitIdle fail!");
                }
            }
        }
        if (needReport) {
            this.mHandler.sendMessageDelayed(Message.obtain(this.mHandler, 57, isGnssStarted ? 1 : 0, isInteractive ? 1 : 0), 3000);
        }
        if (!isExitIdle) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleReportIdle(boolean isGnssStarted, boolean isInteractive) {
        uploadGnssErrorEvent(isInteractive, isGnssStarted, this.mUtils.getGnssStarted(this));
    }

    private void uploadGnssErrorEvent(boolean isInteractive, boolean isPreGnssStarted, boolean isCurGnssStarted) {
        IMonitor.EventStream gnssErrorStream = IMonitor.openEventStream(DFT_GNSS_ERROR_EVENT);
        if (gnssErrorStream == null) {
            Log.e(TAG, "gnssErrorStream is null");
            return;
        }
        gnssErrorStream.setParam(1, 111);
        WorkSource workSource = getWorkSource();
        if (workSource != null && !workSource.isEmpty()) {
            int i = 0;
            int size = workSource.size();
            while (i < 6 && i < size) {
                IMonitor.EventStream apkInfoStream = IMonitor.openEventStream(DFT_APK_INFO_EVENT);
                if (apkInfoStream == null) {
                    Log.e(TAG, "apkInfoStream is null");
                    return;
                }
                apkInfoStream.setParam(0, workSource.getName(i));
                gnssErrorStream.fillArrayParam(3, apkInfoStream);
                IMonitor.closeEventStream(apkInfoStream);
                i++;
            }
        }
        IMonitor.EventStream assertStream = IMonitor.openEventStream(DFT_CHIP_ASSERT_EVENT);
        if (assertStream == null) {
            Log.e(TAG, "assertStream is null");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(isInteractive ? "1," : "0,");
        sb.append(isPreGnssStarted ? "1," : "0,");
        sb.append(isCurGnssStarted ? "1," : "0,");
        sb.append(this.mEnterIdleTime);
        assertStream.setParam(1, sb.toString());
        gnssErrorStream.setParam(13, assertStream);
        IMonitor.closeEventStream(assertStream);
        IMonitor.sendEvent(gnssErrorStream);
        IMonitor.closeEventStream(gnssErrorStream);
    }

    /* access modifiers changed from: protected */
    public void hwLoadPropertiesFromResource(Context context, Properties properties) {
        String[] configValues = context.getResources().getStringArray(17236011);
        int length = configValues.length;
        boolean z = false;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            }
            String item = configValues[i];
            Log.d(TAG, "hwLoadPropertiesFromResource: " + item);
            int index = item.indexOf("=");
            if (index <= 0 || index + 1 >= item.length()) {
                Log.w(TAG, "malformed contents: " + item);
            } else {
                String key = item.substring(0, index);
                String value = item.substring(index + 1);
                if ("SUPL_ES".equals(key)) {
                    properties.setProperty(key.trim().toUpperCase(), value);
                    break;
                }
            }
            i++;
        }
        String suplESProperty = properties.getProperty("SUPL_ES");
        if (suplESProperty != null) {
            try {
                GpsLocationProviderUtils gpsLocationProviderUtils = this.mUtils;
                if (Integer.parseInt(suplESProperty) == 1) {
                    z = true;
                }
                gpsLocationProviderUtils.setSuplEsEnabled(this, z);
            } catch (NumberFormatException e) {
                Log.e(TAG, "unable to parse SUPL_ES when sim changed " + suplESProperty);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void hwHandleReportLocation(boolean hasLatLong, Location location) {
        if (!this.mGpsFirstFixed && hasLatLong && !location.getProvider().equals("quickgps")) {
            this.mGpsFirstFixed = true;
            this.mHwGpsLocationManager.setGpsTime(location.getTime(), location.getElapsedRealtimeNanos());
        }
    }
}
