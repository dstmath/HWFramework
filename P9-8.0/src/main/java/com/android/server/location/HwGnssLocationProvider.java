package com.android.server.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.location.ILocationManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemProperties;
import android.os.WorkSource;
import android.provider.Settings.Secure;
import android.util.Log;
import com.android.server.HwNetworkPropertyChecker;
import com.android.server.devicepolicy.StorageUtils;
import com.android.server.wifipro.WifiProCommonUtils;
import com.huawei.utils.reflect.EasyInvokeFactory;
import java.util.Properties;

public class HwGnssLocationProvider extends GnssLocationProvider {
    private static final int AGPS_TYPE_SUPL = 1;
    private static final String GNSS_NAVIGATING_FLAG = "hw_higeo_gnss_Navigating";
    private static final int GNSS_REQ_CHANGE_START = 1;
    private static final int GNSS_REQ_CHANGE_STOP = 2;
    private static final String LOCATION_MAP_BAIDU_PACKAGE = "com.baidu.BaiduMap";
    private static final String LOCATION_MAP_FLP_PACKAGE = "com.amap.android.ams";
    private static final String LOCATION_MAP_GAODE_PACKAGE = "com.autonavi.minimap";
    private static final String LOCATION_MAP_GOOGLE_PACKAGE = "com.google.android.apps.maps";
    private static final String MAPS_LOCATION_FLAG = "hw_higeo_maps_location";
    private static final String TAG = "HwGnssLocationProvider";
    private boolean AUTO_ACC_Enable = SystemProperties.getBoolean("ro.config.hw_auto_acc_enable", false);
    private boolean isLastExistMapLocation;
    private String isLocalDBEnabled;
    private BroadcastHelper mBroadcastHelper = new BroadcastHelper();
    private Context mContext;
    private Properties mHwProperties;
    private HwNetworkPropertyChecker mNetworkChecker;
    private int mPreferAccuracy;
    private GpsLocationProviderUtils utils;

    private class BroadcastHelper {
        BroadcastReceiver innerBroadcastReciever = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action != null && action.equals("android.intent.action.ACTION_AGPS_SERVERS")) {
                    BroadcastHelper.this.checkAGpsServer(intent);
                }
            }
        };

        public void init() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.ACTION_AGPS_SERVERS");
            HwGnssLocationProvider.this.mContext.registerReceiver(this.innerBroadcastReciever, intentFilter);
        }

        private void checkAGpsServer(Intent intent) {
            String supl_host = intent.getStringExtra("supl_host");
            String supl_port = intent.getStringExtra("supl_port");
            if (supl_host != null) {
                HwGnssLocationProvider.this.utils.setSuplServerHost(HwGnssLocationProvider.this, supl_host);
                try {
                    HwGnssLocationProvider.this.utils.setSuplServerPort(HwGnssLocationProvider.this, Integer.parseInt(supl_port));
                } catch (NumberFormatException e) {
                    Log.e(HwGnssLocationProvider.TAG, "unable to parse SUPL_PORT: " + supl_port);
                }
                Log.d("HwGpsLocationProvider", "checkAGpsServer mSuplServerHost = " + HwGnssLocationProvider.this.utils.getSuplServerHost(HwGnssLocationProvider.this) + " mSuplServerPort = " + HwGnssLocationProvider.this.utils.getSuplServerPort(HwGnssLocationProvider.this));
                Log.d("HwGpsLocationProvider", "isEnabled = " + HwGnssLocationProvider.this.isEnabled());
                HwGnssLocationProvider.this.utils.native_set_agps_server(HwGnssLocationProvider.this, 1, supl_host, HwGnssLocationProvider.this.utils.getSuplServerPort(HwGnssLocationProvider.this));
            }
        }
    }

    public HwGnssLocationProvider(Context context, ILocationManager ilocationManager, Looper looper) {
        super(context, ilocationManager, looper);
        this.mNetworkChecker = new HwNetworkPropertyChecker(context, (WifiManager) context.getSystemService("wifi"), null, true, null, true);
        this.utils = (GpsLocationProviderUtils) EasyInvokeFactory.getInvokeUtils(GpsLocationProviderUtils.class);
        this.mContext = context;
        this.mBroadcastHelper.init();
        this.mHwProperties = this.utils.getProperties(this);
        if (this.mHwProperties != null) {
            this.isLocalDBEnabled = this.mHwProperties.getProperty("LOCAL_DB");
            if (this.isLocalDBEnabled == null) {
                this.isLocalDBEnabled = StorageUtils.SDCARD_ROMOUNTED_STATE;
            }
        }
    }

    protected boolean isHttpReachable() {
        return WifiProCommonUtils.unreachableRespCode(this.mNetworkChecker.isCaptivePortal(true)) ^ 1;
    }

    public boolean isLocalDBEnabled() {
        return StorageUtils.SDCARD_ROMOUNTED_STATE.equals(this.isLocalDBEnabled);
    }

    public void initDefaultApnObserver(Handler handler) {
        this.utils.setDefaultApnObserver(this, new ContentObserver(handler) {
            public void onChange(boolean selfChange) {
                HwGnssLocationProvider.this.utils.setDefaultApn(HwGnssLocationProvider.this, HwGnssLocationProvider.this.utils.getDefaultApn(HwGnssLocationProvider.this));
            }
        });
    }

    public void reportContextStatus(int status) {
        Log.d(TAG, "reportContextStatus status = " + status);
        Intent intent = new Intent("huawei.android.location.DRIVER_STATUS");
        intent.putExtra("status", status);
        this.mContext.sendBroadcast(intent);
    }

    public int getPreferred_accuracy() {
        if (!this.AUTO_ACC_Enable) {
            return 0;
        }
        int accuracy_set = 0;
        if (existLocationMap()) {
            accuracy_set = 200;
        }
        Log.d(TAG, "getPreferred_accuracy:" + accuracy_set);
        this.mPreferAccuracy = accuracy_set;
        return accuracy_set;
    }

    public boolean shouldReStartNavi() {
        if (!this.AUTO_ACC_Enable) {
            return false;
        }
        if ((this.mPreferAccuracy == 200 || !existLocationMap()) && (this.mPreferAccuracy != 200 || (existLocationMap() ^ 1) == 0)) {
            return false;
        }
        return true;
    }

    private boolean existLocationMap() {
        WorkSource workSource = getWorkSource();
        if (workSource != null) {
            int num_uName = workSource.size();
            int i = 0;
            while (i < num_uName) {
                if (workSource.getName(i).equals(LOCATION_MAP_GAODE_PACKAGE) || workSource.getName(i).equals(LOCATION_MAP_FLP_PACKAGE) || workSource.getName(i).equals(LOCATION_MAP_BAIDU_PACKAGE)) {
                    Log.d(TAG, "existLocationMap:true");
                    return true;
                }
                i++;
            }
        }
        Log.d(TAG, "existLocationMap:false");
        return false;
    }

    public void handleGnssRequirementsChange(int reson) {
        if (reson == 1) {
            handleGnssRequirementsChange(existLocationMapsForHigeo());
        } else if (reson == 2) {
            handleGnssRequirementsChange(false);
        }
    }

    private void handleGnssRequirementsChange(boolean mapNavigating) {
        boolean new_state = mapNavigating;
        if (this.isLastExistMapLocation != mapNavigating) {
            this.isLastExistMapLocation = mapNavigating;
            Secure.putInt(this.mContext.getContentResolver(), MAPS_LOCATION_FLAG, mapNavigating ? 1 : 0);
            Log.d(TAG, "handleGnssRequirementsChange,existLocationMap = " + mapNavigating);
            return;
        }
        Log.d(TAG, "existLocationMap state is not change,ignor! isLastExistMapLocation:" + this.isLastExistMapLocation);
    }

    public void handleGnssNavigatingStateChange(boolean start) {
        Log.d(TAG, "handleGnssNavigateState,start : " + start);
    }

    private boolean existLocationMapsForHigeo() {
        WorkSource workSource = getWorkSource();
        if (workSource != null) {
            int num_uName = workSource.size();
            int i = 0;
            while (i < num_uName) {
                if (workSource.getName(i).equals(LOCATION_MAP_GAODE_PACKAGE) || workSource.getName(i).equals(LOCATION_MAP_FLP_PACKAGE) || workSource.getName(i).equals("com.google.android.apps.maps") || workSource.getName(i).equals(LOCATION_MAP_BAIDU_PACKAGE)) {
                    Log.d(TAG, "existLocationMapsForHigeo:true");
                    return true;
                }
                i++;
            }
        }
        return false;
    }
}
