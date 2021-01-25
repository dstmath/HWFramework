package com.android.server.wifi.MSS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.HwWifiCHRService;
import com.android.server.wifi.HwWifiCHRServiceImpl;
import com.huawei.android.pgmng.plug.PowerKit;
import java.util.ArrayList;
import java.util.List;

public class HwMSSArbitrager {
    private static final String ACTION_PKG_PREFIX = "package:";
    private static final String BRCM_CHIP_4359 = "bcm4359";
    private static final int MAX_FREQ_24G = 2484;
    private static final int MIN_FREQ_24G = 2412;
    private static final long MIN_MSS_TIME_SPAN = 60000;
    private static final int MSS_SWITCH_24G = 1;
    private static final int MSS_SWITCH_50G = 2;
    private static final String PRODUCT_VTR_PREFIX = "VTR";
    public static final String SMART_MODE_STATUS = "SmartModeStatus";
    private static final String TAG = "MSSArbitrager";
    private static HwMSSArbitrager mInstance;
    private MSSState mABSCurrentState = MSSState.ABSMIMO;
    private String[] mAllowMSSApkList = null;
    private BroadcastReceiver mBcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.MSS.HwMSSArbitrager.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                HwMSSUtils.log(3, false, HwMSSArbitrager.TAG, "action:%{public}s", action);
                if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                    HwMSSArbitrager.this.mPluggedType = intent.getIntExtra("plugged", 0);
                    HwMSSUtils.log(3, false, HwMSSArbitrager.TAG, "mPluggedType:%{public}d", Integer.valueOf(HwMSSArbitrager.this.mPluggedType));
                    if (SystemProperties.getInt("runtime.hwmss.debug", 0) == 0 && HwMSSArbitrager.this.isChargePluggedin() && !HwMSSArbitrager.this.isP2PConnected() && !HwMSSArbitrager.this.mWiFiApMode) {
                        for (IHwMSSObserver observer : HwMSSArbitrager.this.mMSSObservers) {
                            observer.onMSSSwitchRequest(2);
                        }
                    }
                } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo != null) {
                        HwMSSUtils.log(3, false, HwMSSArbitrager.TAG, "Received WIFI_P2P_CONNECTION_CHANGED_ACTION: networkInfo=%{public}s", networkInfo.toString());
                        HwMSSArbitrager.this.mP2pConnectState = networkInfo.isConnected();
                        HwMSSUtils.log(3, false, HwMSSArbitrager.TAG, "mP2pConnectState: %{public}s", String.valueOf(HwMSSArbitrager.this.mP2pConnectState));
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    NetworkInfo networkInfo2 = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (networkInfo2 != null) {
                        int i = AnonymousClass3.$SwitchMap$android$net$NetworkInfo$DetailedState[networkInfo2.getDetailedState().ordinal()];
                        if (i == 1) {
                            HwMSSArbitrager.this.mMssSwitchTimestamp = 0;
                            HwMSSUtils.log(3, false, HwMSSArbitrager.TAG, "connect enter", new Object[0]);
                            HwMSSArbitrager.this.mWiFiConnectState = true;
                            synchronized (HwMSSArbitrager.this.mSyncLock) {
                                if (HwMSSArbitrager.this.mWifiManager == null) {
                                    HwMSSArbitrager.this.mWifiManager = (WifiManager) HwMSSArbitrager.this.mContext.getSystemService("wifi");
                                }
                                HwMSSArbitrager.this.mConnectWifiInfo = HwMSSArbitrager.this.mWifiManager.getConnectionInfo();
                            }
                            if (HwMSSArbitrager.this.mConnectWifiInfo != null) {
                                HwMSSUtils.log(3, false, HwMSSArbitrager.TAG, "ssid:%{public}s, bssid:%{private}s, freq:%{public}d", HwMSSArbitrager.this.mConnectWifiInfo.getSSID(), HwMSSArbitrager.this.mConnectWifiInfo.getBSSID(), Integer.valueOf(HwMSSArbitrager.this.mConnectWifiInfo.getFrequency()));
                                HwMSSArbitrager hwMSSArbitrager = HwMSSArbitrager.this;
                                hwMSSArbitrager.mCurrentBssid = hwMSSArbitrager.mConnectWifiInfo.getBSSID();
                            }
                        } else if (i == 2) {
                            HwMSSArbitrager.this.mMssSwitchTimestamp = 0;
                            HwMSSArbitrager.this.mWiFiConnectState = false;
                            HwMSSArbitrager.this.mCurrentBssid = null;
                        }
                    }
                } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                    int apState = intent.getIntExtra("wifi_state", 11);
                    if (apState == 11) {
                        HwMSSArbitrager.this.mMssSwitchTimestamp = 0;
                        HwMSSArbitrager.this.mWiFiApMode = false;
                        HwHiLog.d(HwMSSArbitrager.TAG, false, "ap close status %{public}s", new Object[]{String.valueOf(HwMSSArbitrager.this.mWiFiApMode)});
                    } else if (apState == 13) {
                        HwMSSArbitrager.this.mMssSwitchTimestamp = 0;
                        HwMSSArbitrager.this.mWiFiApMode = true;
                        HwHiLog.d(HwMSSArbitrager.TAG, false, "ap enable status %{public}s", new Object[]{String.valueOf(HwMSSArbitrager.this.mWiFiApMode)});
                    }
                }
            }
        }
    };
    private WifiInfo mConnectWifiInfo = null;
    private Context mContext;
    private String mCurrentBssid = null;
    private boolean mGameForeground = false;
    private String[] mHT40PkgList = null;
    private HwWifiCHRService mHwWifiCHRService;
    private String[] mIncompatibleBssidList = null;
    private List<String> mInstalledPkgNameList = new ArrayList();
    private IHwMSSBlacklistMgr mMSSBlackMgrt = null;
    private MSSState mMSSCurrentState = MSSState.MSSMIMO;
    private String[] mMSSLimitList = null;
    private String[] mMSSLimitSwitchAPKList = null;
    private List<IHwMSSObserver> mMSSObservers = new ArrayList();
    private int mMSSSwitchCapa = 0;
    private long mMssSwitchTimestamp = 0;
    private boolean mP2pConnectState = false;
    private BroadcastReceiver mPkgChangedReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.MSS.HwMSSArbitrager.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                HwMSSUtils.log(3, false, HwMSSArbitrager.TAG, "action:%{public}s", action);
                String packageName = intent.getDataString();
                if (packageName != null && packageName.startsWith(HwMSSArbitrager.ACTION_PKG_PREFIX)) {
                    packageName = packageName.substring(HwMSSArbitrager.ACTION_PKG_PREFIX.length());
                }
                if (!TextUtils.isEmpty(packageName)) {
                    if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                        HwMSSUtils.log(3, false, HwMSSArbitrager.TAG, "packageName:%{public}s", packageName);
                        for (String pkgName : HwMSSArbitrager.this.mInstalledPkgNameList) {
                            if (pkgName.equals(packageName)) {
                                return;
                            }
                        }
                        HwMSSArbitrager.this.mInstalledPkgNameList.add(packageName);
                        HwMSSArbitrager.this.checkMssLimitPackage(packageName);
                    } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                        HwMSSUtils.log(3, false, HwMSSArbitrager.TAG, "packageName:%{public}s", packageName);
                        for (int idx = HwMSSArbitrager.this.mInstalledPkgNameList.size() - 1; idx >= 0; idx--) {
                            if (((String) HwMSSArbitrager.this.mInstalledPkgNameList.get(idx)).equals(packageName)) {
                                HwMSSUtils.log(3, false, HwMSSArbitrager.TAG, "match:packageName:%{public}s", packageName);
                                HwMSSArbitrager.this.mInstalledPkgNameList.remove(idx);
                                return;
                            }
                        }
                    }
                }
            }
        }
    };
    private int mPluggedType = 0;
    private PowerKit mPowerKit = null;
    private final Object mSyncLock = new Object();
    private boolean mWiFiApMode = false;
    private boolean mWiFiConnectState = false;
    private WifiManager mWifiManager = null;
    private boolean sisoFixFlag = false;

    public interface IHwMSSObserver {
        void onHT40Request();

        void onMSSSwitchRequest(int i);
    }

    public enum MSSState {
        MSSUNKNOWN,
        MSSMIMO,
        MSSSISO,
        ABSMIMO,
        ABSMRC,
        ABSSWITCHING,
        MSSSWITCHING
    }

    public enum MSS_TRIG_TYPE {
        CLONE_TRIG,
        COMMON_TRIG
    }

    public static synchronized HwMSSArbitrager getInstance(Context cxt) {
        HwMSSArbitrager hwMSSArbitrager;
        synchronized (HwMSSArbitrager.class) {
            if (mInstance == null) {
                mInstance = new HwMSSArbitrager(cxt);
            }
            hwMSSArbitrager = mInstance;
        }
        return hwMSSArbitrager;
    }

    public boolean setABSCurrentState(MSSState state) {
        HwMSSUtils.log(1, false, TAG, "setABSCurrentState:%{public}d", state);
        if (state != MSSState.ABSMIMO && state != MSSState.ABSMRC && state != MSSState.ABSSWITCHING) {
            return false;
        }
        this.mABSCurrentState = state;
        HwMSSUtils.switchToast(this.mContext, this.mABSCurrentState);
        return true;
    }

    public boolean setMSSCurrentState(MSSState state) {
        HwMSSUtils.log(1, false, TAG, "setMSSCurrentState:%{public}s", state);
        if (state != MSSState.MSSMIMO && state != MSSState.MSSSISO && state != MSSState.MSSUNKNOWN && state != MSSState.MSSSWITCHING) {
            return false;
        }
        this.mMSSCurrentState = state;
        this.mHwWifiCHRService = HwWifiCHRServiceImpl.getInstance();
        HwWifiCHRService hwWifiCHRService = this.mHwWifiCHRService;
        if (hwWifiCHRService != null) {
            hwWifiCHRService.updateMSSState(this.mMSSCurrentState.toString());
        }
        HwMSSUtils.switchToast(this.mContext, this.mMSSCurrentState);
        return true;
    }

    public void setGameForeground(boolean enable) {
        HwMSSUtils.log(1, false, TAG, "setGameForeground:%{public}s", String.valueOf(enable));
        this.mGameForeground = enable;
    }

    public boolean isGameForeground() {
        return this.mGameForeground;
    }

    public MSSState getMSSCurrentState() {
        return this.mMSSCurrentState;
    }

    public MSSState getABSCurrentState() {
        return this.mABSCurrentState;
    }

    public void registerMSSObserver(IHwMSSObserver observer) {
        for (IHwMSSObserver item : this.mMSSObservers) {
            if (item == observer) {
                return;
            }
        }
        this.mMSSObservers.add(observer);
    }

    public void unregisterMSSObserver(IHwMSSObserver observer) {
        for (int i = this.mMSSObservers.size() - 1; i >= 0; i--) {
            if (this.mMSSObservers.get(i) == observer) {
                this.mMSSObservers.remove(i);
            }
        }
    }

    public boolean isSupportHT40() {
        return this.mMSSSwitchCapa > 0;
    }

    private HwMSSArbitrager(Context cxt) {
        this.mContext = cxt;
        if (HwMSSUtils.is1103() || HwMSSUtils.is1105()) {
            this.mMSSBlackMgrt = HisiMSSBlackListManager.getInstance(this.mContext);
        } else {
            this.mMSSBlackMgrt = HwMSSBlackListManager.getInstance(this.mContext);
        }
        getMSSSwitchCapa();
        getWhiteList();
        getHT40WhiteList();
        getAllInstalledPkg();
        getAllLimitSwitchAPK();
        getAllowMSSApkList();
        registerBcastReceiver();
        registerPkgChangedReceiver();
        getIncompatibleBssidList();
    }

    public boolean isMSSAllowed(int direction, int freq, MSS_TRIG_TYPE reason) {
        long now = SystemClock.elapsedRealtime();
        if (direction == 2 && true == this.sisoFixFlag) {
            HwMSSUtils.log(1, false, TAG, "isMSSAllowed false: Mobile Clone", new Object[0]);
            return false;
        } else if (SystemProperties.getInt("runtime.hwmss.blktest", 0) == 0 && direction == 1 && now - this.mMssSwitchTimestamp < MIN_MSS_TIME_SPAN) {
            HwMSSUtils.log(1, false, TAG, "isMSSAllowed false: time limit", new Object[0]);
            return false;
        } else if (1 == SystemProperties.getInt("persist.hwmss.switch", 0)) {
            HwMSSUtils.log(1, false, TAG, "doMssSwitch persist.hwmss.switch:false", new Object[0]);
            return false;
        } else if (SystemProperties.getInt("runtime.hwmss.bssid", 0) == 0 && !this.mWiFiApMode && isIncompatibleBssid(this.mCurrentBssid)) {
            HwMSSUtils.log(1, false, TAG, "isMSSAllowed isIncompatibleBssid true", new Object[0]);
            return false;
        } else if (isGameForeground()) {
            HwMSSUtils.log(1, false, TAG, "isMSSAllowed isGameForeground true", new Object[0]);
            return false;
        } else if (this.mABSCurrentState != MSSState.ABSMIMO && (this.mABSCurrentState != MSSState.ABSMRC || !ScanResult.is5GHz(freq))) {
            HwMSSUtils.log(1, false, TAG, "isMSSAllowed false: ABSState %{public}d", this.mABSCurrentState);
            return false;
        } else if (direction == 1 && isChargePluggedin() && reason != MSS_TRIG_TYPE.CLONE_TRIG && SystemProperties.getInt("runtime.hwmss.debug", 0) == 0) {
            HwMSSUtils.log(1, false, TAG, "isMSSAllowed isChargePluggedin:false", new Object[0]);
            return false;
        } else if (!isChipSwitchSupport(freq)) {
            HwMSSUtils.log(1, false, TAG, "isMSSAllowed isChipSwitchSupport:false", new Object[0]);
            return false;
        } else {
            if (!this.mWiFiApMode) {
                if (!isWiFiConnected()) {
                    HwMSSUtils.log(1, false, TAG, "isMSSAllowed isWiFiConnected: false", new Object[0]);
                    return false;
                } else if (SystemProperties.getInt("runtime.hwmss.blktest", 0) == 0 && direction == 1 && isInMSSBlacklist()) {
                    HwMSSUtils.log(1, false, TAG, "isMSSAllowed isInMSSBlacklist true", new Object[0]);
                    return false;
                }
            }
            if (isP2PConnected()) {
                HwMSSUtils.log(1, false, TAG, "isMSSAllowed isP2PConnected true", new Object[0]);
                return false;
            } else if (direction == 1 && isLimitAPKonFront()) {
                HwMSSUtils.log(1, false, TAG, "isLimitAPKonFront true", new Object[0]);
                return false;
            } else if (direction != 1 || !matchMssLimitList() || reason == MSS_TRIG_TYPE.CLONE_TRIG) {
                this.mMssSwitchTimestamp = now;
                return true;
            } else {
                HwMSSUtils.log(1, false, TAG, "isMSSAllowed: matchMssLimitList true", new Object[0]);
                return false;
            }
        }
    }

    public boolean isMSSSwitchBandSupport() {
        if ((this.mMSSSwitchCapa & 1) > 0) {
            return HwMSSUtils.isAllowSwitch();
        }
        return false;
    }

    public boolean matchHT40List() {
        if (this.mHT40PkgList == null || this.mInstalledPkgNameList.size() == 0) {
            return false;
        }
        for (int i = 0; i < this.mHT40PkgList.length; i++) {
            for (int j = this.mInstalledPkgNameList.size() - 1; j >= 0; j--) {
                String pkgname = this.mInstalledPkgNameList.get(j);
                if (pkgname != null && pkgname.contains(this.mHT40PkgList[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    public int readSaveMode() {
        return Settings.System.getIntForUser(this.mContext.getContentResolver(), SMART_MODE_STATUS, 1, 0);
    }

    private void registerBcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addCategory("android.net.wifi.STATE_CHANGE@hwBrExpand@WifiNetStatus=WIFICON|WifiNetStatus=WIFIDSCON");
        filter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        this.mContext.registerReceiver(this.mBcastReceiver, filter);
    }

    private void registerPkgChangedReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addDataScheme("package");
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        this.mContext.registerReceiver(this.mPkgChangedReceiver, filter);
    }

    /* renamed from: com.android.server.wifi.MSS.HwMSSArbitrager$3  reason: invalid class name */
    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$android$net$NetworkInfo$DetailedState = new int[NetworkInfo.DetailedState.values().length];

        static {
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$android$net$NetworkInfo$DetailedState[NetworkInfo.DetailedState.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    private void getAllInstalledPkg() {
        List<PackageInfo> pkgInfoList = this.mContext.getPackageManager().getInstalledPackages(0);
        this.mInstalledPkgNameList.clear();
        HwMSSUtils.log(3, false, TAG, "getAllInstalledPkg:", new Object[0]);
        for (PackageInfo info : pkgInfoList) {
            this.mInstalledPkgNameList.add(info.packageName);
            HwMSSUtils.log(3, false, TAG, info.packageName, new Object[0]);
        }
    }

    private void getWhiteList() {
        this.mMSSLimitList = new String[]{"com.magicandroidapps.iperf", "net.he.networktools", "org.zwanoo.android.speedtest", "com.example.wptp.testapp", "com.veriwave.waveagent"};
    }

    private void getHT40WhiteList() {
        this.mHT40PkgList = new String[]{"com.example.wptp.testapp", "com.veriwave.waveagent"};
    }

    private void getAllowMSSApkList() {
        this.mAllowMSSApkList = new String[]{"com.ixia.ixchariot"};
    }

    private void getAllLimitSwitchAPK() {
        this.mMSSLimitSwitchAPKList = new String[]{"com.dewmobile.kuaiya"};
    }

    private void getIncompatibleBssidList() {
        this.mIncompatibleBssidList = new String[]{"b0:d5:9d", "c8:d5:fe", "70:b0:35"};
    }

    private boolean matchMssLimitList() {
        if (this.mMSSLimitList == null || this.mInstalledPkgNameList.size() == 0) {
            return false;
        }
        for (int i = 0; i < this.mMSSLimitList.length; i++) {
            for (int j = this.mInstalledPkgNameList.size() - 1; j >= 0; j--) {
                String pkgname = this.mInstalledPkgNameList.get(j);
                if (pkgname != null && pkgname.contains(this.mMSSLimitList[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean matchAllowMSSApkList() {
        if (this.mAllowMSSApkList == null || this.mInstalledPkgNameList.size() == 0) {
            return false;
        }
        for (int i = 0; i < this.mAllowMSSApkList.length; i++) {
            for (int j = this.mInstalledPkgNameList.size() - 1; j >= 0; j--) {
                String apkname = this.mInstalledPkgNameList.get(j);
                if (apkname != null && apkname.contains(this.mAllowMSSApkList[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isChargePluggedin() {
        int i = this.mPluggedType;
        if (i == 2 || i == 5) {
            return true;
        }
        return false;
    }

    public boolean isP2PConnected() {
        return this.mP2pConnectState;
    }

    public boolean isWiFiConnected() {
        return this.mWiFiConnectState;
    }

    private boolean isLimitAPKonFront() {
        if (this.mPowerKit == null) {
            this.mPowerKit = PowerKit.getInstance();
        }
        PowerKit powerKit = this.mPowerKit;
        if (powerKit != null) {
            try {
                String pktName = powerKit.getTopFrontApp(this.mContext);
                int i = 0;
                while (true) {
                    String[] strArr = this.mMSSLimitSwitchAPKList;
                    if (i >= strArr.length) {
                        break;
                    } else if (strArr[i].equals(pktName)) {
                        HwMSSUtils.log(1, false, TAG, "found limit APK: %{public}s", pktName);
                        return true;
                    } else {
                        i++;
                    }
                }
            } catch (RemoteException e) {
                HwMSSUtils.log(1, false, TAG, "get top front app fail", new Object[0]);
                return false;
            }
        }
        return false;
    }

    public boolean isInMSSBlacklist() {
        String ssid = "";
        String bssid = "";
        synchronized (this.mSyncLock) {
            if (this.mConnectWifiInfo != null) {
                ssid = this.mConnectWifiInfo.getSSID();
                bssid = this.mConnectWifiInfo.getBSSID();
            }
        }
        if (((HwMSSUtils.is1103() || HwMSSUtils.is1105()) && !TextUtils.isEmpty(bssid) && this.mMSSBlackMgrt.isInBlacklistByBssid(bssid)) || TextUtils.isEmpty(ssid)) {
            return true;
        }
        return this.mMSSBlackMgrt.isInBlacklist(ssid);
    }

    private void getMSSSwitchCapa() {
        this.mMSSSwitchCapa = 0;
        String chipName = SystemProperties.get("ro.connectivity.sub_chiptype", "");
        if (BRCM_CHIP_4359.equals(chipName)) {
            if (SystemProperties.get("ro.product.name", "").startsWith(PRODUCT_VTR_PREFIX)) {
                this.mMSSSwitchCapa = 1;
            } else {
                this.mMSSSwitchCapa = 3;
            }
        } else if (HwMSSUtils.HISI_CHIP_1103.equals(chipName) || HwMSSUtils.HISI_CHIP_1105.equals(chipName)) {
            this.mMSSSwitchCapa = 3;
        }
    }

    private boolean isChipSwitchSupport(int freq) {
        int i = this.mMSSSwitchCapa;
        if (i == 3) {
            return true;
        }
        if (i != 1) {
            return false;
        }
        synchronized (this.mSyncLock) {
            if (freq < MIN_FREQ_24G || freq > MAX_FREQ_24G) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkMssLimitPackage(String packageName) {
        if (this.mMSSObservers.size() != 0) {
            if (this.mABSCurrentState == MSSState.ABSMIMO && this.mMSSCurrentState == MSSState.MSSSISO) {
                int i = 0;
                while (true) {
                    String[] strArr = this.mMSSLimitList;
                    if (i >= strArr.length) {
                        break;
                    } else if (strArr[i].equals(packageName)) {
                        for (IHwMSSObserver observer : this.mMSSObservers) {
                            observer.onMSSSwitchRequest(2);
                        }
                    } else {
                        i++;
                    }
                }
            }
            int i2 = 0;
            while (true) {
                String[] strArr2 = this.mHT40PkgList;
                if (i2 >= strArr2.length) {
                    return;
                }
                if (strArr2[i2].equals(packageName)) {
                    for (IHwMSSObserver observer2 : this.mMSSObservers) {
                        observer2.onHT40Request();
                    }
                    return;
                }
                i2++;
            }
        }
    }

    private boolean isIncompatibleBssid(String bssid) {
        if (bssid == null || bssid.length() < 8) {
            HwMSSUtils.log(1, false, TAG, "found incompatible vendor", new Object[0]);
            return true;
        }
        int i = 0;
        while (true) {
            String[] strArr = this.mIncompatibleBssidList;
            if (i >= strArr.length) {
                return false;
            }
            if (strArr[i].equals(bssid.substring(0, 8))) {
                HwMSSUtils.log(1, false, TAG, "found incompatible vendor: %{private}s", bssid.substring(0, 8));
                return true;
            }
            i++;
        }
    }

    public void setSisoFixFlag(boolean value) {
        this.sisoFixFlag = value;
    }

    public boolean getSisoFixFlag() {
        return this.sisoFixFlag;
    }
}
