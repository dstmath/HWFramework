package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.Location;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import android.util.HwLog;
import android.util.IMonitor;
import com.android.server.HwServiceFactory;
import com.android.server.LocationManagerServiceUtil;
import com.android.server.appactcontrol.AppActConstant;
import com.android.server.hidata.wavemapping.modelservice.ModelBaseService;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.huawei.cust.HwCfgFilePolicy;
import com.huawei.cust.HwCustUtils;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

public class HwGnssLocationProvider implements IHwGnssLocationProvider {
    private static final String ACTION_COTA_PARA_LOADED = "com.huawei.settingsprovider.cota_para_loaded";
    private static final int AGPS_TYPE_SUPL = 1;
    private static final int ASSISTED_GPS_DISABLED = 0;
    private static final int ASSISTED_GPS_ENABLED = 1;
    private static final boolean AUTO_ACC_ENABLE = SystemProperties.getBoolean("ro.config.hw_auto_acc_enable", false);
    private static final int CALL_HIBERNATE = 53;
    private static final String DEFAULT_SUPL_ADDRESS = "supl.default.com";
    private static final String DEFAULT_SUPL_PORT = "7275";
    private static final int DEL_AID_DATA = 1;
    private static final int DFT_APK_INFO_EVENT = 910009006;
    private static final int DFT_CHIP_ASSERT_EVENT = 910009014;
    private static final int DFT_GNSS_ERROR_EVENT = 910000007;
    private static final int DFT_GNSS_IDLE_ERROR = 111;
    private static final boolean GNSS_ADAPT_CARD = SystemProperties.getBoolean("ro.odm.config.gnss_adapt_card", false);
    private static final String GNSS_NAVIGATING_FLAG = "hw_higeo_gnss_Navigating";
    private static final int GNSS_REPORT_STATUS = 200;
    private static final int GNSS_REQ_CHANGE_START = 1;
    private static final int GNSS_REQ_CHANGE_STOP = 2;
    private static final String GOOGLE_SUPL_ADDRESS = "supl.google.com";
    private static final String GPS_DAEMON_RALOAD_TRIGGER = "vendor.gps_daemon_reload";
    private static final String LOCATION_MAP_BAIDU_PACKAGE = "com.baidu.BaiduMap";
    private static final String LOCATION_MAP_DIDI_GSUI_PACKAGE = "com.sdu.didi.gsui";
    private static final String LOCATION_MAP_DIDI_PSNGER_PACKAGE = "com.sdu.didi.psnger";
    private static final String LOCATION_MAP_FLP_PACKAGE = "com.amap.android.ams";
    private static final String LOCATION_MAP_GAODE_PACKAGE = "com.autonavi.minimap";
    private static final String LOCATION_MAP_GOOGLE_PACKAGE = "com.google.android.apps.maps";
    private static final String LOCATION_MAP_WAZE_PACKAGE = "com.waze";
    private static final Object LOCK = new Object();
    private static final String MAPS_LOCATION_FLAG = "hw_higeo_maps_location";
    private static final int MAX_IDLE_GPS_RECORD = 6;
    private static final String PERMISSION_INJECT_LOCATION = "com.huawei.android.permission.INJECT_LOCATION";
    private static final int QUICK_TTFF_THREHOLD_ACC = 200;
    private static final int REPORT_IDLE_DELAY = 3000;
    private static final int RESET_SUPL_SERVER = 2;
    private static final int RTK_LOCATION = 8;
    private static final int START_NAVIGATING = 52;
    private static final String TAG = "HwGnssLocationProvider";
    private static final int UPDATE_LOWPOWER_MODE = 54;
    private static final int UPDATE_NETWORK_STATE = 0;
    private static SecureRandom random = new SecureRandom();
    private static volatile HwGnssLocationProvider sInstance;
    private BroadcastHelper mBroadcastHelper = new BroadcastHelper();
    private Context mContext;
    HwCustGpsLocationProvider mCust;
    private IDeviceIdleController mDeviceIdleController;
    private long mEnterIdleTime;
    private GnssLocationProvider mGnssLocationProvider;
    private IHwCmccGpsFeature mHwCmccGpsFeature;
    private IHwGpsLocationCustFeature mHwGpsLocationCustFeature;
    private IHwGpsLocationManager mHwGpsLocationManager;
    private HwHigeoService mHwHigeoService;
    private Properties mHwProperties;
    private boolean mIsGpsFirstFixed;
    private int mIsLastExistMapLocation;
    private boolean mIsLastIdle = false;
    private String mIsLocalDBEnabled;
    private PowerManager mPowerManager;
    private int mPreferAccuracy;
    private GpsLocationProviderUtils mUtils;

    /* access modifiers changed from: package-private */
    public enum MapNavigatingType {
        NOT_MAP,
        WHITE_MAP,
        BLACK_MAP,
        RTK_MAP
    }

    public static HwGnssLocationProvider getInstance(Context context, GnssLocationProvider gnssLocationProvider, Looper looper) {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new HwGnssLocationProvider(context, gnssLocationProvider, looper);
                }
            }
        }
        return sInstance;
    }

    public HwGnssLocationProvider(Context context, GnssLocationProvider gnssLocationProvider, Looper looper) {
        this.mGnssLocationProvider = gnssLocationProvider;
        this.mCust = (HwCustGpsLocationProvider) HwCustUtils.createObj(HwCustGpsLocationProvider.class, new Object[]{this.mGnssLocationProvider});
        this.mUtils = EasyInvokeFactory.getInvokeUtils(GpsLocationProviderUtils.class);
        this.mContext = context;
        this.mBroadcastHelper.init();
        this.mHwProperties = this.mUtils.getProperties(this.mGnssLocationProvider);
        Properties properties = this.mHwProperties;
        if (properties != null) {
            this.mIsLocalDBEnabled = properties.getProperty("LOCAL_DB");
            if (this.mIsLocalDBEnabled == null) {
                this.mIsLocalDBEnabled = AppActConstant.VALUE_TRUE;
            }
        }
        this.mHwGpsLocationManager = HwGpsLocationManager.getInstance(this.mContext);
        this.mDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mHwCmccGpsFeature = HwServiceFactory.getHwCmccGpsFeature(this.mContext, gnssLocationProvider);
        this.mHwGpsLocationCustFeature = HwServiceFactory.getHwGpsLocationCustFeature();
        Settings.Secure.putInt(this.mContext.getContentResolver(), MAPS_LOCATION_FLAG, 0);
        this.mHwHigeoService = new HwHigeoService(context);
    }

    private class BroadcastHelper {
        HwLbsConfigManager configManager = HwLbsConfigManager.getInstance(HwGnssLocationProvider.this.mContext);
        BroadcastReceiver innerBroadcastReciever;
        String suplHost;
        String suplPort;

        public BroadcastHelper() {
            this.innerBroadcastReciever = new BroadcastReceiver(HwGnssLocationProvider.this) {
                /* class com.android.server.location.HwGnssLocationProvider.BroadcastHelper.AnonymousClass1 */

                @Override // android.content.BroadcastReceiver
                public void onReceive(Context context, Intent intent) {
                    if (context == null || intent == null) {
                        LBSLog.i(HwGnssLocationProvider.TAG, false, "context or intent is null,return.", new Object[0]);
                        return;
                    }
                    String action = intent.getAction();
                    if (SmartDualCardConsts.SYSTEM_STATE_NAME_SIM_STATE_CHANGED.equals(action)) {
                        String state = intent.getStringExtra("ss");
                        LBSLog.i(HwGnssLocationProvider.TAG, false, " onReceive action state = %{public}s", state);
                        if ("LOADED".equals(state)) {
                            BroadcastHelper.this.checkAndSetAGpsParameter(HwGnssLocationProvider.DEFAULT_SUPL_ADDRESS);
                            if (HwGnssLocationProvider.GNSS_ADAPT_CARD) {
                                LBSLog.i(HwGnssLocationProvider.TAG, false, "sim changed,set prop of gps daemon to 1", new Object[0]);
                                SystemProperties.set(HwGnssLocationProvider.GPS_DAEMON_RALOAD_TRIGGER, "1");
                            }
                        }
                    }
                    if (HwGnssLocationProvider.ACTION_COTA_PARA_LOADED.equals(action) && !HwGnssLocationProvider.GNSS_ADAPT_CARD) {
                        LBSLog.i(HwGnssLocationProvider.TAG, false, "vc changed set prop of gps daemon to 1", new Object[0]);
                        SystemProperties.set(HwGnssLocationProvider.GPS_DAEMON_RALOAD_TRIGGER, "1");
                    }
                    if ("action_inject_location".equals(action)) {
                        HwGnssLocationProvider.this.mUtils.sendMessage(HwGnssLocationProvider.this.mGnssLocationProvider, HwGnssLocationProvider.this.mUtils.getUpdateLocationMassage(HwGnssLocationProvider.this.mGnssLocationProvider), 0, intent.getParcelableExtra("key_location"));
                    }
                }
            };
        }

        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SIM_STATE_CHANGED);
            intentFilter.addAction(HwGnssLocationProvider.ACTION_COTA_PARA_LOADED);
            HwGnssLocationProvider.this.mContext.registerReceiver(this.innerBroadcastReciever, intentFilter);
            IntentFilter localLocationIntentFilter = new IntentFilter();
            localLocationIntentFilter.addAction("action_inject_location");
            HwGnssLocationProvider.this.mContext.registerReceiver(this.innerBroadcastReciever, localLocationIntentFilter, HwGnssLocationProvider.PERMISSION_INJECT_LOCATION, null);
        }

        private void setCaictParameter() {
            String suplHostCloud = this.configManager.getStringForParam(LbsConfigContent.CONFIG_SUPL_SERVER);
            String suplPortCloud = this.configManager.getStringForParam(LbsConfigContent.CONFIG_SUPL_PORT);
            if (!TextUtils.isEmpty(suplHostCloud) && !TextUtils.isEmpty(suplPortCloud)) {
                this.suplHost = suplHostCloud;
                this.suplPort = suplPortCloud;
                LBSLog.e(HwGnssLocationProvider.TAG, false, "cloud supl_server: %{public}s, supl_port: %{public}s", this.suplHost, this.suplPort);
            }
        }

        private void setHwServiceParameter() {
            String hwSuplHost = this.configManager.getStringForParam(LbsConfigContent.CONFIG_SUPL_HWSERVER);
            String hwSuplPort = this.configManager.getStringForParam(LbsConfigContent.CONFIG_SUPL_HWPORT);
            if (!TextUtils.isEmpty(hwSuplHost) && !TextUtils.isEmpty(hwSuplPort)) {
                this.suplHost = hwSuplHost;
                this.suplPort = hwSuplPort;
                LBSLog.e(HwGnssLocationProvider.TAG, false, " Hwsupl_server: %{public}s, hwsupl_port: %{public}s", this.suplHost, this.suplPort);
            }
        }

        private void setCardParameter() {
            Map agpsConfigMap = HwCfgFilePolicy.getFileConfig("xml/agps_config.xml");
            String suplPortCust = "";
            String suplHostCust = "";
            if (agpsConfigMap != null) {
                try {
                    Map portHostMap = (Map) agpsConfigMap.get("portHostKey");
                    if (portHostMap != null) {
                        suplPortCust = (String) portHostMap.get(LbsConfigContent.CONFIG_SUPL_PORT);
                        suplHostCust = (String) portHostMap.get("supl_host");
                    }
                } catch (ClassCastException e) {
                    LBSLog.e(HwGnssLocationProvider.TAG, false, "ClassCastException occured .", new Object[0]);
                }
                if (!TextUtils.isEmpty(suplHostCust) && !TextUtils.isEmpty(suplPortCust)) {
                    this.suplHost = suplHostCust;
                    this.suplPort = suplPortCust;
                    LBSLog.e(HwGnssLocationProvider.TAG, false, "cust supl_server: %{public}s, supl_port: %{public}s", this.suplHost, this.suplPort);
                }
            }
        }

        private void setNoGmsParameter() {
            if (!this.configManager.isEnableForParam(LbsConfigContent.CONFIG_ENABLE_GOOGLE_SUPL_SERVER) && HwGnssLocationProvider.GOOGLE_SUPL_ADDRESS.equals(this.suplHost)) {
                this.suplHost = HwGnssLocationProvider.DEFAULT_SUPL_ADDRESS;
                this.suplPort = HwGnssLocationProvider.DEFAULT_SUPL_PORT;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void checkAndSetAGpsParameter(String inSuplHost) {
            this.suplHost = inSuplHost;
            this.suplPort = "";
            setCaictParameter();
            setHwServiceParameter();
            setCardParameter();
            setNoGmsParameter();
            if (!this.suplHost.equals(inSuplHost)) {
                HwGnssLocationProvider.this.mUtils.setSuplServerHost(HwGnssLocationProvider.this.mGnssLocationProvider, this.suplHost);
                try {
                    int suplPortInt = Integer.parseInt(this.suplPort);
                    HwGnssLocationProvider.this.mUtils.setSuplServerPort(HwGnssLocationProvider.this.mGnssLocationProvider, suplPortInt);
                    HwGnssLocationProvider.this.mUtils.native_set_agps_server(HwGnssLocationProvider.this.mGnssLocationProvider, 1, this.suplHost, suplPortInt);
                } catch (NumberFormatException e) {
                    LBSLog.e(HwGnssLocationProvider.TAG, false, "Unable to parse supl host , %{public}s", e.getMessage());
                }
            }
        }
    }

    public boolean isLocalDBEnabled() {
        return true;
    }

    public void initDefaultApnObserver(Handler handler) {
        this.mUtils.setDefaultApnObserver(this.mGnssLocationProvider, new ContentObserver(handler) {
            /* class com.android.server.location.HwGnssLocationProvider.AnonymousClass1 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange) {
                HwGnssLocationProvider.this.mUtils.setDefaultApn(HwGnssLocationProvider.this.mGnssLocationProvider, HwGnssLocationProvider.this.mUtils.getDefaultApn(HwGnssLocationProvider.this.mGnssLocationProvider));
            }
        });
    }

    public void reportContextStatus(int status) {
    }

    public int getPreferredAccuracy() {
        if (!AUTO_ACC_ENABLE) {
            return 0;
        }
        int accuracySet = 0;
        if (existLocationMap()) {
            accuracySet = 200;
        }
        LBSLog.i(TAG, false, "getPreferredAccuracy: %{public}d", Integer.valueOf(accuracySet));
        this.mPreferAccuracy = accuracySet;
        return accuracySet;
    }

    public boolean shouldRestartNavi() {
        handleGnssNavigatingStateChange(true);
        if (!this.mUtils.getGnssStarted(this.mGnssLocationProvider) || !AUTO_ACC_ENABLE) {
            return false;
        }
        if ((this.mPreferAccuracy == 200 || !existLocationMap()) && (this.mPreferAccuracy != 200 || existLocationMap())) {
            return false;
        }
        this.mUtils.stopNavigating(this.mGnssLocationProvider);
        this.mUtils.startNavigating(this.mGnssLocationProvider);
        return true;
    }

    private boolean existLocationMap() {
        WorkSource workSource = this.mUtils.getWorkSource(this.mGnssLocationProvider);
        if (workSource != null) {
            int numName = workSource.size();
            for (int i = 0; i < numName; i++) {
                if (workSource.getName(i).equals(LOCATION_MAP_GAODE_PACKAGE) || workSource.getName(i).equals(LOCATION_MAP_FLP_PACKAGE) || workSource.getName(i).equals(LOCATION_MAP_BAIDU_PACKAGE)) {
                    LBSLog.i(TAG, false, "existLocationMap: true", new Object[0]);
                    return true;
                }
            }
        }
        LBSLog.i(TAG, false, "existLocationMap: false", new Object[0]);
        return false;
    }

    public void handleGnssRequirementsChange(int reson) {
        if (reson == 1) {
            handleMapLocation(existLocationMapsForHigeo());
        } else if (reson == 2) {
            handleMapLocation(MapNavigatingType.NOT_MAP.ordinal());
        } else {
            LBSLog.i(TAG, false, "handleGnssRequirementsChange reson not clear.", new Object[0]);
        }
    }

    private void handleMapLocation(int mapNavigatingType) {
        int i = this.mIsLastExistMapLocation;
        if (i != mapNavigatingType) {
            this.mIsLastExistMapLocation = mapNavigatingType;
            Settings.Secure.putInt(this.mContext.getContentResolver(), MAPS_LOCATION_FLAG, mapNavigatingType);
            LBSLog.i(TAG, false, "handleGnssRequirementsChange,existLocationMap = %{public}d", Integer.valueOf(mapNavigatingType));
            return;
        }
        LBSLog.i(TAG, false, "existLocationMap state is not change,ignor! isLastExistMapLocation: %{public}d", Integer.valueOf(i));
    }

    public void handleGnssNavigatingStateChange(boolean isStart) {
        LBSLog.i(TAG, false, "handleGnssNavigateState,start : %{public}b", Boolean.valueOf(isStart));
        HwLog.dubaie("DUBAI_TAG_NAVIGATING_STATE", "state=" + (isStart ? 1 : 0));
        if (isStart) {
            handleGnssRequirementsChange(1);
        } else {
            handleGnssRequirementsChange(2);
        }
    }

    public void startNavigatingEx() {
        boolean agpsEnabled = true;
        if (Settings.Global.getInt(this.mContext.getContentResolver(), "assisted_gps_enabled", 1) == 0) {
            agpsEnabled = false;
        }
        BroadcastHelper broadcastHelper = this.mBroadcastHelper;
        if (broadcastHelper != null) {
            broadcastHelper.checkAndSetAGpsParameter(DEFAULT_SUPL_ADDRESS);
        }
        if (this.mHwGpsLocationCustFeature != null) {
            this.mUtils.setPositionMode(this.mGnssLocationProvider, this.mHwGpsLocationCustFeature.setPostionMode(this.mContext, this.mUtils.getPositionMode(this.mGnssLocationProvider), agpsEnabled));
        }
        if (this.mHwCmccGpsFeature != null) {
            this.mUtils.setPositionMode(this.mGnssLocationProvider, this.mHwCmccGpsFeature.setPostionModeAndAgpsServer(this.mUtils.getPositionMode(this.mGnssLocationProvider), agpsEnabled));
        }
        if (this.mCust != null) {
            this.mUtils.setPositionMode(this.mGnssLocationProvider, this.mCust.setPostionMode(this.mUtils.getPositionMode(this.mGnssLocationProvider)));
        }
        this.mIsGpsFirstFixed = false;
    }

    public boolean sendExtraCommandEx(String command) {
        HwCustGpsLocationProvider hwCustGpsLocationProvider = this.mCust;
        if (hwCustGpsLocationProvider == null) {
            return false;
        }
        boolean isResult = hwCustGpsLocationProvider.sendPostionModeCommand(false, command);
        this.mUtils.setPositionMode(this.mGnssLocationProvider, this.mCust.setPostionMode(this.mUtils.getPositionMode(this.mGnssLocationProvider)));
        return isResult;
    }

    public boolean sendExtraCommandEx(String command, Bundle extras) {
        boolean isResult = sendExtraCommandEx(command);
        if (!"download_aiding_data".equals(command) || extras == null) {
            return isResult;
        }
        try {
            LBSLog.i(TAG, false, "call native_inject_psds_data", new Object[0]);
            if (extras.getString("ePoData") == null) {
                return false;
            }
            byte[] data = extras.getString("ePoData").getBytes("ISO-8859-1");
            this.mUtils.native_inject_psds_data(this.mGnssLocationProvider, data, data.length);
            return true;
        } catch (UnsupportedEncodingException e) {
            LBSLog.e(TAG, false, " extras.getString throw UnsupportedEncodingException", new Object[0]);
            return false;
        }
    }

    public void handleUpdateNetworkStateHook(NetworkInfo info) {
        boolean isRoaming = false;
        if (info != null) {
            isRoaming = info.isRoaming();
        }
        HwCustGpsLocationProvider hwCustGpsLocationProvider = this.mCust;
        if (hwCustGpsLocationProvider != null) {
            hwCustGpsLocationProvider.setRoaming(isRoaming);
        }
    }

    private int existLocationMapsForHigeo() {
        WorkSource workSource = this.mUtils.getWorkSource(this.mGnssLocationProvider);
        if (workSource != null) {
            int numName = workSource.size();
            for (int i = 0; i < numName; i++) {
                if ("com.google.android.apps.maps".equals(workSource.getName(i))) {
                    LBSLog.i(TAG, false, "existLocationMapsForHigeo: black map", new Object[0]);
                    return MapNavigatingType.BLACK_MAP.ordinal();
                } else if (LOCATION_MAP_GAODE_PACKAGE.equals(workSource.getName(i)) || LOCATION_MAP_FLP_PACKAGE.equals(workSource.getName(i)) || LOCATION_MAP_WAZE_PACKAGE.equals(workSource.getName(i)) || LOCATION_MAP_DIDI_GSUI_PACKAGE.equals(workSource.getName(i)) || LOCATION_MAP_DIDI_PSNGER_PACKAGE.equals(workSource.getName(i))) {
                    LBSLog.i(TAG, false, "existLocationMapsForHigeo: white map", new Object[0]);
                    return MapNavigatingType.WHITE_MAP.ordinal();
                } else if (LOCATION_MAP_BAIDU_PACKAGE.equals(workSource.getName(i))) {
                    LBSLog.i(TAG, false, "existLocationMapsForHigeo: rtk map", new Object[0]);
                    return MapNavigatingType.RTK_MAP.ordinal();
                }
            }
        }
        return MapNavigatingType.NOT_MAP.ordinal();
    }

    public String getSvType(int svidWithFlag) {
        String result = ModelBaseService.UNKONW_IDENTIFY_RET;
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
        return String.format("%-7s", result);
    }

    public void hwHandleMessage(Message msg) {
        int message = msg.what;
        if (message == 0) {
            handleUpdateNetworkStateHook((NetworkInfo) msg.obj);
        } else if (message == 1) {
            IHwCmccGpsFeature iHwCmccGpsFeature = this.mHwCmccGpsFeature;
            if (iHwCmccGpsFeature != null) {
                iHwCmccGpsFeature.setDelAidData();
            }
        } else if (message == 2) {
            this.mBroadcastHelper.checkAndSetAGpsParameter((String) msg.obj);
        } else if (message != 200) {
            switch (message) {
                case 52:
                    if (!this.mUtils.getGnssRealStoped(this.mGnssLocationProvider)) {
                        this.mUtils.startNavigating(this.mGnssLocationProvider);
                        return;
                    }
                    return;
                case 53:
                    this.mUtils.hibernate(this.mGnssLocationProvider);
                    return;
                case 54:
                    this.mUtils.updateLowPowerMode(this.mGnssLocationProvider);
                    return;
                default:
                    sendGeofenceCallback(msg);
                    return;
            }
        } else {
            LocationManagerServiceUtil util = LocationManagerServiceUtil.getDefault();
            if (util != null) {
                util.updateNavigatingStatus(((Boolean) msg.obj).booleanValue());
            }
        }
    }

    private boolean isValidMessage(Message message) {
        HwHigeoService hwHigeoService;
        if (message == null || message.obj == null || (hwHigeoService = this.mHwHigeoService) == null || hwHigeoService.getGeofenceHardware() == null) {
            return false;
        }
        return true;
    }

    private void sendGeofenceCallback(Message message) {
        Bundle data;
        if (isValidMessage(message)) {
            switch (message.what) {
                case IDisplayEngineService.DE_ACTION_GAME_MOVIE /* 56 */:
                    if ((message.obj instanceof Bundle) && (data = (Bundle) message.obj) != null) {
                        data.putInt("geofenceId", message.arg1);
                        int transition = data.getInt("transition");
                        long transitionTimestamp = data.getLong("timestamp");
                        if (transition == 0 && transitionTimestamp == 0) {
                            data.putInt("type", 55);
                        } else {
                            data.putInt("type", 56);
                        }
                        this.mHwHigeoService.getGeofenceHardware().sendGeofenceEventCallback(data);
                        return;
                    }
                    return;
                case IDisplayEngineService.DE_ACTION_FULLSCREEN_CUVA /* 57 */:
                    Bundle status = new Bundle();
                    status.putInt("type", 57);
                    status.putInt("status", message.arg1);
                    if (message.obj instanceof Location) {
                        Location location = (Location) message.obj;
                        if (location != null) {
                            status.putParcelable("location", location);
                        } else {
                            return;
                        }
                    }
                    this.mHwHigeoService.getGeofenceHardware().sendGeofenceEventCallback(status);
                    return;
                case IDisplayEngineService.DE_ACTION_MAX /* 58 */:
                    LBSLog.i(TAG, String.valueOf(58));
                    new Bundle();
                    if (message.obj instanceof Bundle) {
                        this.mHwHigeoService.getGeofenceHardware().sendGeofenceEventCallback((Bundle) message.obj);
                        return;
                    }
                    return;
                case 59:
                    Bundle result = new Bundle();
                    result.putInt("type", message.what);
                    result.putInt("geofenceId", message.arg1);
                    if (message.obj instanceof Integer) {
                        result.putInt("status", ((Integer) message.obj).intValue());
                    }
                    this.mHwHigeoService.getGeofenceHardware().sendGeofenceEventCallback(result);
                    return;
                default:
                    return;
            }
        }
    }

    public boolean checkLowPowerMode() {
        boolean isDeviceIdle = this.mPowerManager.isDeviceIdleMode();
        boolean isNeedReport = false;
        boolean isInteractive = false;
        if (isDeviceIdle != this.mIsLastIdle) {
            this.mIsLastIdle = isDeviceIdle;
            isInteractive = this.mPowerManager.isInteractive();
            if (isDeviceIdle) {
                this.mEnterIdleTime = System.currentTimeMillis();
                LBSLog.i(TAG, false, "checkLowPowerMode isInteractive %{public}b", Boolean.valueOf(isInteractive));
                if (isInteractive) {
                    isNeedReport = true;
                }
            }
            HwGpsLogServices.getInstance(this.mContext).idleChange(isDeviceIdle, isInteractive);
        }
        boolean isGnssStarted = false;
        boolean isExitIdle = false;
        boolean isShouldEnterIdle = false;
        if (isDeviceIdle) {
            isGnssStarted = this.mUtils.getGnssStarted(this.mGnssLocationProvider);
            LocationManagerServiceUtil util = LocationManagerServiceUtil.getDefault();
            if (isGnssStarted && util != null) {
                isShouldEnterIdle = util.shouldEnterIdle();
            }
            LBSLog.i(TAG, false, "checkLowPowerMode isGnssStarted: %{public}b, shouldEnterIdle: %{public}b", Boolean.valueOf(isGnssStarted), Boolean.valueOf(isShouldEnterIdle));
            if (isGnssStarted) {
                try {
                    if (this.mDeviceIdleController != null && !isShouldEnterIdle) {
                        this.mDeviceIdleController.exitIdle("gps interaction");
                        isNeedReport = true;
                        isExitIdle = true;
                    }
                } catch (RemoteException e) {
                    LBSLog.e(TAG, false, "exitIdle fail!", new Object[0]);
                }
            }
        }
        if (isNeedReport) {
            Timer timer = new Timer();
            timer.schedule(new ReportIdleTask(timer, isGnssStarted, isInteractive), 3000);
        }
        return !isExitIdle;
    }

    class ReportIdleTask extends TimerTask {
        boolean isGnssStarted;
        boolean isInteractive;
        Timer timer;

        ReportIdleTask(Timer timer2, boolean isGnssStarted2, boolean isInteractive2) {
            this.isGnssStarted = isGnssStarted2;
            this.isInteractive = isInteractive2;
            this.timer = timer2;
        }

        @Override // java.util.TimerTask, java.lang.Runnable
        public void run() {
            HwGnssLocationProvider.this.handleReportIdle(this.isGnssStarted, this.isInteractive);
            this.timer.cancel();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleReportIdle(boolean isGnssStarted, boolean isInteractive) {
        uploadGnssErrorEvent(isInteractive, isGnssStarted, this.mUtils.getGnssStarted(this.mGnssLocationProvider));
    }

    private void uploadGnssErrorEvent(boolean isInteractive, boolean isPreGnssStarted, boolean isCurGnssStarted) {
        String str;
        IMonitor.EventStream gnssErrorStream = IMonitor.openEventStream((int) DFT_GNSS_ERROR_EVENT);
        if (gnssErrorStream == null) {
            LBSLog.e(TAG, false, "gnssErrorStream is null", new Object[0]);
            return;
        }
        gnssErrorStream.setParam(1, 111);
        WorkSource workSource = this.mUtils.getWorkSource(this.mGnssLocationProvider);
        if (workSource != null && !workSource.isEmpty()) {
            int i = 0;
            int size = workSource.size();
            while (i < 6 && i < size) {
                IMonitor.EventStream apkInfoStream = IMonitor.openEventStream((int) DFT_APK_INFO_EVENT);
                if (apkInfoStream == null) {
                    LBSLog.e(TAG, false, "apkInfoStream is null", new Object[0]);
                    return;
                }
                apkInfoStream.setParam(0, workSource.getName(i));
                gnssErrorStream.fillArrayParam(3, apkInfoStream);
                IMonitor.closeEventStream(apkInfoStream);
                i++;
            }
        }
        IMonitor.EventStream assertStream = IMonitor.openEventStream((int) DFT_CHIP_ASSERT_EVENT);
        if (assertStream == null) {
            LBSLog.e(TAG, false, "assertStream is null", new Object[0]);
            return;
        }
        StringBuilder sb = new StringBuilder();
        String str2 = "1,";
        sb.append(isInteractive ? str2 : "0,");
        if (isPreGnssStarted) {
            str = str2;
        } else {
            str = "0,";
        }
        sb.append(str);
        if (!isCurGnssStarted) {
            str2 = "0,";
        }
        sb.append(str2);
        sb.append(this.mEnterIdleTime);
        assertStream.setParam(1, sb.toString());
        gnssErrorStream.setParam(13, assertStream);
        IMonitor.closeEventStream(assertStream);
        IMonitor.sendEvent(gnssErrorStream);
        IMonitor.closeEventStream(gnssErrorStream);
    }

    public void handleReportLocationEx(boolean hasLatLong, Location location) {
        int sourceType = location.getExtras().getInt("SourceType");
        Bundle locBundle = location.getExtras();
        if ((sourceType & 8) == 8) {
            locBundle.putFloat("HDACC", location.getAccuracy());
            location.setAccuracy(new Integer(random.nextInt(200) + 300).floatValue() / 100.0f);
        } else {
            locBundle.putFloat("HDACC", 0.0f);
        }
        location.setExtras(locBundle);
        if (!this.mIsGpsFirstFixed && hasLatLong && !"quickgps".equals(location.getProvider())) {
            this.mIsGpsFirstFixed = true;
            this.mHwGpsLocationManager.setGpsTime(location.getTime(), location.getElapsedRealtimeNanos());
            IHwCmccGpsFeature iHwCmccGpsFeature = this.mHwCmccGpsFeature;
            if (iHwCmccGpsFeature != null) {
                iHwCmccGpsFeature.syncTime(location.getTime());
            }
        }
    }

    public void loadPropertiesFromResourceEx(Context context, Properties properties) {
    }
}
