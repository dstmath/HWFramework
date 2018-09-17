package com.android.server.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.SystemProperties;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.HwServiceFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import libcore.io.IoUtils;

public class HwQuickTTFFMonitor {
    private static final float ACC_IN_WHITE_LIST = 50.0f;
    private static final boolean DBG = true;
    private static final boolean DEBUG = true;
    private static final boolean IS_DEBUG_VERSION;
    public static final int LIST_TYPE_DISABLE = 4;
    public static final int LIST_TYPE_WHITE = 3;
    private static final int LOCATION_HAS_HWQUICKFIX = 256;
    private static final String QUICK_GPS = "quickgps";
    private static final int QUICK_GPS_LOCATION = 2;
    private static final int QUICK_GPS_REQUEST_START = 1;
    private static final int QUICK_GPS_REQUEST_STOP = 3;
    private static final int QUICK_GPS_SESSION_START = 0;
    private static final int QUICK_GPS_SESSION_STOP = 4;
    private static final int QUICK_IS_ENABLE = 1;
    private static final String QUICK_START = "request_quick_ttff";
    private static final String QUICK_STOP = "stop_quick_ttff";
    private static final String TAG = "HwQuickTTFFMonitor-V2.0.0";
    private static final String VERSION = "V2.0.0";
    private static final int WAIT_TIME = 30000;
    private static HwQuickTTFFMonitor mHwQuickTTFFMonitorManager;
    private static final Object mLock = new Object();
    private String DEBUG_PROPERTIES_FILE;
    private String Enable_Quickttff;
    private boolean isNavigating = false;
    private boolean isPermission = true;
    private boolean isQuickLocation = false;
    private boolean isQuickTTFFEnable = false;
    private boolean isReceive = false;
    private boolean isSatisfiedRequest = false;
    private boolean isSendStartCommand = false;
    private HashMap<String, String> mAppMonitorMap;
    private Context mContext;
    private int mEnableQuickttff;
    LocationProviderInterface mGnssProvider;
    private IHwGpsLogServices mHwLocationGpsLogServices;
    private Properties mProperties;
    private Timer mQuickTTFFtimer;
    private ConcurrentHashMap<String, Long> mRemoveTimeMap;
    private ArraySet<String> m_QuickttffDisableList = new ArraySet();
    private ArraySet<String> m_accWhiteList = new ArraySet();
    private boolean m_running = false;

    class LocalTimerTask extends TimerTask {
        LocalTimerTask() {
        }

        public void run() {
            if (HwQuickTTFFMonitor.this.mQuickTTFFtimer != null) {
                HwQuickTTFFMonitor.this.mQuickTTFFtimer.cancel();
                HwQuickTTFFMonitor.this.mQuickTTFFtimer = null;
                Log.i(HwQuickTTFFMonitor.TAG, "mQuickTTFFtimer.cancel");
            }
            if (!HwQuickTTFFMonitor.this.isReceive && HwQuickTTFFMonitor.this.m_running) {
                Log.e(HwQuickTTFFMonitor.TAG, " Not receive location :stop quickttff");
                HwQuickTTFFMonitor.this.clearAppMonitorMap();
                HwQuickTTFFMonitor.this.sendStopCommand();
            }
        }
    }

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
    }

    private HwQuickTTFFMonitor(Context context, LocationProviderInterface provider) {
        this.mContext = context;
        this.mGnssProvider = provider;
        this.mHwLocationGpsLogServices = HwServiceFactory.getHwGpsLogServices(this.mContext);
    }

    public static HwQuickTTFFMonitor getInstance(Context context, LocationProviderInterface provider) {
        synchronized (mLock) {
            if (mHwQuickTTFFMonitorManager == null) {
                Log.i(TAG, "mHwQuickTTFFMonitorManager create.");
                mHwQuickTTFFMonitorManager = new HwQuickTTFFMonitor(context, provider);
            }
        }
        return mHwQuickTTFFMonitorManager;
    }

    public static HwQuickTTFFMonitor getMonitor() {
        return mHwQuickTTFFMonitorManager;
    }

    public void startMonitor() {
        if (isBetaUser()) {
            this.DEBUG_PROPERTIES_FILE = "/odm/etc/higeo_beta.conf";
            Log.i(TAG, "This is beta user");
        } else {
            this.DEBUG_PROPERTIES_FILE = "/odm/etc/higeo.conf";
            Log.i(TAG, "This is not beta user");
        }
        quickTTFFEnable(this.DEBUG_PROPERTIES_FILE);
        if (this.isQuickTTFFEnable) {
            Log.i(TAG, "startMonitor");
            this.mAppMonitorMap = new HashMap();
            this.mRemoveTimeMap = new ConcurrentHashMap();
            this.m_running = false;
            this.isSendStartCommand = false;
            return;
        }
        Log.e(TAG, " QuickTTFF is  not enable" + this.Enable_Quickttff);
    }

    public void requestHwQuickTTFF(LocationRequest request, String packageName, String requestProvider, String id) {
        this.isSatisfiedRequest = false;
        if (this.isQuickTTFFEnable) {
            if ("gps".equals(request.getProvider()) && ("gps".equals(requestProvider) ^ 1) == 0 && (this.isPermission ^ 1) == 0) {
                String permissionStatus = String.valueOf(this.m_QuickttffDisableList.contains(packageName) ^ 1);
                String requestAccuracy = "null";
                if (this.m_accWhiteList.contains(packageName)) {
                    requestAccuracy = String.valueOf(ACC_IN_WHITE_LIST);
                } else {
                    requestAccuracy = "null";
                }
                this.mHwLocationGpsLogServices.setQuickGpsParam(1, packageName + "," + permissionStatus + "," + requestAccuracy);
                if (isQuickttffDisableListApp(packageName)) {
                    Log.i(TAG, packageName + " is in black list  can not start Quickttff");
                    return;
                }
                if (this.mRemoveTimeMap.size() > 0) {
                    for (Entry<String, Long> entry : this.mRemoveTimeMap.entrySet()) {
                        String packageNameRemove = (String) entry.getKey();
                        if (System.currentTimeMillis() - ((Long) entry.getValue()).longValue() > 2000) {
                            this.mRemoveTimeMap.remove(packageNameRemove);
                        }
                    }
                    if (this.mRemoveTimeMap.containsKey(packageName)) {
                        Log.i(TAG, packageName + " can not start quickttff in 2s");
                        return;
                    }
                }
                synchronized (mLock) {
                    this.mAppMonitorMap.put(id, packageName);
                    Log.i(TAG, "requestHwQuickTTFF:" + packageName);
                }
                this.isSatisfiedRequest = true;
                if (!this.m_running && this.isNavigating) {
                    Log.i(TAG, "start HwQuickTTFF  when isNavigating");
                    sendStartCommand();
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:14:0x0050, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeHwQuickTTFF(String packageName, String id) {
        if (this.isQuickTTFFEnable && this.mAppMonitorMap.containsKey(id)) {
            synchronized (mLock) {
                this.mAppMonitorMap.remove(id);
                this.mRemoveTimeMap.put(packageName, Long.valueOf(System.currentTimeMillis()));
                this.mHwLocationGpsLogServices.setQuickGpsParam(3, packageName);
                Log.i(TAG, "removeHwQuickTTFF:" + packageName);
                if (this.mAppMonitorMap.size() > 0 || !this.m_running) {
                } else {
                    Log.i(TAG, "removeHwQuickTTFF HwQuickTTFF STOP");
                    sendStopCommand();
                }
            }
        }
    }

    /* JADX WARNING: Missing block: B:19:0x0025, code:
            android.util.Log.i(TAG, "removeAllHwQuickTTFF HwQuickTTFF STOP");
     */
    /* JADX WARNING: Missing block: B:20:0x0034, code:
            if (r6.mAppMonitorMap.size() <= 0) goto L_0x0063;
     */
    /* JADX WARNING: Missing block: B:21:0x0036, code:
            r2 = r6.mAppMonitorMap.entrySet().iterator();
     */
    /* JADX WARNING: Missing block: B:23:0x0044, code:
            if (r2.hasNext() == false) goto L_0x0063;
     */
    /* JADX WARNING: Missing block: B:24:0x0046, code:
            r6.mRemoveTimeMap.put((java.lang.String) ((java.util.Map.Entry) r2.next()).getValue(), java.lang.Long.valueOf(java.lang.System.currentTimeMillis()));
     */
    /* JADX WARNING: Missing block: B:28:0x0063, code:
            r6.mAppMonitorMap.clear();
            sendStopCommand();
     */
    /* JADX WARNING: Missing block: B:29:0x006b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeAllHwQuickTTFF(String provider) {
        if (this.isQuickTTFFEnable && !this.isQuickLocation && ("gps".equals(provider) ^ 1) == 0) {
            synchronized (mLock) {
                if (this.m_running) {
                    this.m_running = false;
                    this.isSendStartCommand = false;
                }
            }
        }
    }

    public boolean isLocationReportToApp(String packageName, String provider, Location location) {
        boolean isReportLocation = false;
        float acc = location.getAccuracy();
        if (!this.isQuickLocation || ("gps".equals(provider) ^ 1) != 0) {
            return true;
        }
        if (this.mAppMonitorMap.containsValue(packageName)) {
            if (isAccSatisfied(packageName, acc)) {
                isReportLocation = true;
            } else {
                isReportLocation = false;
            }
        }
        Log.i(TAG, "isReportQuickTTFFLocation:" + isReportLocation + ", pkgname=" + packageName);
        return isReportLocation;
    }

    public void setQuickTTFFLocation(Location location, boolean passive) {
        if (this.isQuickTTFFEnable && !passive) {
            if ((QUICK_GPS.equals(location.getProvider()) || "gps".equals(location.getProvider())) && this.m_running) {
                this.mHwLocationGpsLogServices.setQuickGpsParam(2, String.valueOf(location.getAccuracy()));
            }
            if (QUICK_GPS.equals(location.getProvider())) {
                this.isQuickLocation = true;
                location.setProvider("gps");
                Bundle extras = new Bundle();
                extras.putBoolean("QUICKGPS", true);
                location.setExtras(extras);
                Log.i(TAG, "is a QuickTTFFLocation");
                if (!this.isReceive) {
                    this.isReceive = true;
                    Log.i(TAG, "have receive QuickTTFFLocation");
                    if (this.mQuickTTFFtimer != null) {
                        this.mQuickTTFFtimer.cancel();
                        this.mQuickTTFFtimer = null;
                        Log.i(TAG, "mQuickTTFFtimer.cancel");
                    }
                }
            } else if ("gps".equals(location.getProvider())) {
                this.isQuickLocation = false;
            }
        }
    }

    public boolean isQuickLocationEnable() {
        return this.isQuickLocation;
    }

    public boolean isRunning() {
        return this.m_running;
    }

    public void setPermission(boolean permission) {
        this.isPermission = permission;
    }

    public void setNavigating(boolean Navigating) {
        this.isNavigating = Navigating;
    }

    private boolean isQuickttffDisableListApp(String appName) {
        if (this.m_QuickttffDisableList == null) {
            return false;
        }
        if (this.m_QuickttffDisableList.contains(appName)) {
            Log.i(TAG, appName + "is not WhiteApp");
            return true;
        }
        Log.i(TAG, appName + "is  WhiteApp");
        return false;
    }

    private boolean isAccSatisfied(String appName, float acc) {
        if (this.m_QuickttffDisableList == null || !this.m_accWhiteList.contains(appName)) {
            return true;
        }
        Log.i(TAG, appName + "is inAccList ");
        if (ACC_IN_WHITE_LIST >= acc) {
            Log.d(TAG, "Satisfied with Acc50.0" + acc);
            return true;
        }
        Log.d(TAG, " Not Satisfied with Acc50.0" + acc);
        return false;
    }

    public void updateAccWhiteList(List<String> accWhiteList) {
        Log.d(TAG, "AccWhiteList " + accWhiteList.size());
        for (String packageName : accWhiteList) {
            Log.d(TAG, "AccWhiteList " + packageName);
        }
        this.m_accWhiteList.clear();
        this.m_accWhiteList.addAll(accWhiteList);
        Log.d(TAG, "accWhiteList" + accWhiteList);
    }

    public void updateDisableList(List<String> QuickttffDisableList) {
        Log.d(TAG, "DisableList " + QuickttffDisableList.size());
        for (String packageName : QuickttffDisableList) {
            Log.d(TAG, "DisableList " + packageName);
        }
        this.m_QuickttffDisableList.clear();
        this.m_QuickttffDisableList.addAll(QuickttffDisableList);
        Log.d(TAG, " QuickttffDisableList " + QuickttffDisableList);
    }

    public void quickTTFFEnable(String filename) {
        Throwable th;
        this.mProperties = new Properties();
        try {
            FileInputStream stream = null;
            try {
                FileInputStream stream2 = new FileInputStream(new File(filename));
                try {
                    this.mProperties.load(stream2);
                    IoUtils.closeQuietly(stream2);
                } catch (Throwable th2) {
                    th = th2;
                    stream = stream2;
                    IoUtils.closeQuietly(stream);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                IoUtils.closeQuietly(stream);
                throw th;
            }
        } catch (IOException e) {
            Log.d(TAG, "Could not open higeo configuration file " + filename);
            this.isQuickTTFFEnable = false;
        }
        this.Enable_Quickttff = this.mProperties.getProperty("higeo_enable_quickttff");
        if (!(this.Enable_Quickttff == null || ("".equals(this.Enable_Quickttff) ^ 1) == 0)) {
            try {
                this.mEnableQuickttff = Integer.parseInt(this.Enable_Quickttff);
            } catch (NumberFormatException e2) {
                Log.e(TAG, "unable to parse Enable_Quickttff: " + this.Enable_Quickttff);
                this.isQuickTTFFEnable = false;
            }
        }
        if (this.mEnableQuickttff == 1) {
            this.isQuickTTFFEnable = true;
            Log.e(TAG, " QuickTTFF is enable" + this.Enable_Quickttff);
            return;
        }
        this.isQuickTTFFEnable = false;
        Log.e(TAG, " QuickTTFF is  not enable" + this.Enable_Quickttff);
    }

    private boolean isBetaUser() {
        return IS_DEBUG_VERSION;
    }

    public boolean isReport(Location location) {
        if (this.m_running || !QUICK_GPS.equals(location.getProvider())) {
            return true;
        }
        return false;
    }

    public void clearAppMonitorMap() {
        this.mAppMonitorMap.clear();
        Log.i(TAG, "clearAppMonitorMap");
    }

    public void sendStartCommand() {
        if (this.m_running) {
            Log.i(TAG, "HwQuickTTFF has be started before.");
            return;
        }
        if (!this.isSendStartCommand && this.isSatisfiedRequest) {
            this.isReceive = false;
            timerTask();
            this.m_running = true;
            this.isSendStartCommand = true;
            Log.i(TAG, "HwQuickTTFF Start");
            this.mHwLocationGpsLogServices.setQuickGpsParam(0, QUICK_START);
            this.mGnssProvider.sendExtraCommand(QUICK_START, null);
            Log.e(TAG, "isSendStartCommand" + this.isSendStartCommand);
        }
    }

    public void sendStopCommand() {
        this.m_running = false;
        this.isSendStartCommand = false;
        this.isQuickLocation = false;
        this.isReceive = false;
        Log.i(TAG, "HwQuickTTFF STOP");
        this.mHwLocationGpsLogServices.setQuickGpsParam(4, "ALL");
        this.mGnssProvider.sendExtraCommand(QUICK_STOP, null);
        if (this.mQuickTTFFtimer != null) {
            this.mQuickTTFFtimer.cancel();
            this.mQuickTTFFtimer = null;
            Log.i(TAG, "mQuickTTFFtimer.cancel");
        }
    }

    public void timerTask() {
        this.mQuickTTFFtimer = new Timer();
        this.mQuickTTFFtimer.schedule(new LocalTimerTask(), 30000);
    }
}
