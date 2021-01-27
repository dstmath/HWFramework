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

public class HwMssArbitrager {
    private static final String ACTION_PKG_PREFIX = "package:";
    private static final String BRCM_CHIP_4359 = "bcm4359";
    private static final int MAX_FREQ_24G = 2484;
    private static final int MIN_BSSID_LENGTH = 8;
    private static final int MIN_FREQ_24G = 2412;
    private static final long MIN_MSS_TIME_SPAN = 60000;
    private static final int MSS_SWITCH_24G = 1;
    private static final int MSS_SWITCH_50G = 2;
    private static final String PRODUCT_VTR_PREFIX = "VTR";
    public static final String SMART_MODE_STATUS = "SmartModeStatus";
    private static final String TAG = "MssArbitrager";
    private static HwMssArbitrager sInstance;
    private boolean isP2pConnectState = false;
    private boolean isWiFiConnectState = false;
    private MssState mAbsCurrentState = MssState.ABSMIMO;
    private String[] mAllowMssApkList = null;
    private BroadcastReceiver mBcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.MSS.HwMssArbitrager.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                HwMssUtils.log(3, false, HwMssArbitrager.TAG, "action:%{public}s", action);
                if ("android.intent.action.BATTERY_CHANGED".equals(action)) {
                    handleBatteyChangedAction(intent);
                } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    Object object = intent.getParcelableExtra("networkInfo");
                    if (object instanceof NetworkInfo) {
                        NetworkInfo networkInfo = (NetworkInfo) object;
                        HwMssUtils.log(3, false, HwMssArbitrager.TAG, "Received WIFI_P2P_CONNECTION_CHANGED_ACTION: networkInfo=%{public}s", networkInfo.toString());
                        HwMssArbitrager.this.isP2pConnectState = networkInfo.isConnected();
                        HwMssUtils.log(3, false, HwMssArbitrager.TAG, "mP2pConnectState: %{public}s", String.valueOf(HwMssArbitrager.this.isP2pConnectState));
                    }
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    handleStateChangeAction(context, intent);
                } else if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
                    int apState = intent.getIntExtra("wifi_state", 11);
                    if (apState == 11) {
                        HwMssArbitrager.this.mMssSwitchTimestamp = 0;
                        HwMssArbitrager.this.mWiFiApMode = false;
                        HwHiLog.d(HwMssArbitrager.TAG, false, "ap close status %{public}s", new Object[]{String.valueOf(HwMssArbitrager.this.mWiFiApMode)});
                    } else if (apState == 13) {
                        HwMssArbitrager.this.mMssSwitchTimestamp = 0;
                        HwMssArbitrager.this.mWiFiApMode = true;
                        HwHiLog.d(HwMssArbitrager.TAG, false, "ap enable status %{public}s", new Object[]{String.valueOf(HwMssArbitrager.this.mWiFiApMode)});
                    } else {
                        HwMssUtils.log(3, false, HwMssArbitrager.TAG, "no handle network action of state change", new Object[0]);
                    }
                } else {
                    HwHiLog.d(HwMssArbitrager.TAG, false, "No processing type", new Object[0]);
                }
            }
        }

        private void handleStateChangeAction(Context context, Intent intent) {
            Object object = intent.getParcelableExtra("networkInfo");
            if (object instanceof NetworkInfo) {
                int i = AnonymousClass3.$SwitchMap$android$net$NetworkInfo$DetailedState[((NetworkInfo) object).getDetailedState().ordinal()];
                if (i == 1) {
                    HwMssArbitrager.this.mMssSwitchTimestamp = 0;
                    HwMssUtils.log(3, false, HwMssArbitrager.TAG, "connect enter", new Object[0]);
                    HwMssArbitrager.this.isWiFiConnectState = true;
                    synchronized (HwMssArbitrager.this.mSyncLock) {
                        if (HwMssArbitrager.this.mWifiManager == null) {
                            HwMssArbitrager.this.mWifiManager = (WifiManager) HwMssArbitrager.this.mContext.getSystemService("wifi");
                        }
                        HwMssArbitrager.this.mConnectWifiInfo = HwMssArbitrager.this.mWifiManager.getConnectionInfo();
                    }
                    if (HwMssArbitrager.this.mConnectWifiInfo != null) {
                        HwMssUtils.log(3, false, HwMssArbitrager.TAG, "ssid:%{public}s, bssid:%{private}s, freq:%{public}d", HwMssArbitrager.this.mConnectWifiInfo.getSSID(), HwMssArbitrager.this.mConnectWifiInfo.getBSSID(), Integer.valueOf(HwMssArbitrager.this.mConnectWifiInfo.getFrequency()));
                        HwMssArbitrager hwMssArbitrager = HwMssArbitrager.this;
                        hwMssArbitrager.mCurrentBssid = hwMssArbitrager.mConnectWifiInfo.getBSSID();
                    }
                } else if (i == 2) {
                    HwMssArbitrager.this.mMssSwitchTimestamp = 0;
                    HwMssArbitrager.this.isWiFiConnectState = false;
                    HwMssArbitrager.this.mCurrentBssid = null;
                }
            }
        }

        private void handleBatteyChangedAction(Intent intent) {
            HwMssArbitrager.this.mPluggedType = intent.getIntExtra("plugged", 0);
            HwMssUtils.log(3, false, HwMssArbitrager.TAG, "mPluggedType:%{public}d", Integer.valueOf(HwMssArbitrager.this.mPluggedType));
            if (SystemProperties.getInt("runtime.hwmss.debug", 0) == 0 && HwMssArbitrager.this.isChargePluggedin() && !HwMssArbitrager.this.isP2pConnected() && !HwMssArbitrager.this.mWiFiApMode) {
                for (IHwMssObserver observer : HwMssArbitrager.this.mMssObservers) {
                    observer.onMssSwitchRequest(2);
                }
            }
        }
    };
    private WifiInfo mConnectWifiInfo = null;
    private Context mContext;
    private String mCurrentBssid = null;
    private boolean mGameForeground = false;
    private String[] mHt40PkgList = null;
    private HwWifiCHRService mHwWifiChrService;
    private String[] mIncompatibleBssidList = null;
    private List<String> mInstalledPkgNameList = new ArrayList();
    private boolean mIsSisoFixFlag = false;
    private IHwMssBlacklistMgr mMssBlackMgrt = null;
    private MssState mMssCurrentState = MssState.MSSMIMO;
    private String[] mMssLimitList = null;
    private String[] mMssLimitSwitchApkList = null;
    private List<IHwMssObserver> mMssObservers = new ArrayList();
    private int mMssSwitchCapa = 0;
    private long mMssSwitchTimestamp = 0;
    private BroadcastReceiver mPkgChangedReceiver = new BroadcastReceiver() {
        /* class com.android.server.wifi.MSS.HwMssArbitrager.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                HwMssUtils.log(3, false, HwMssArbitrager.TAG, "action:%{public}s", action);
                String packageName = intent.getDataString();
                if (packageName != null && packageName.startsWith(HwMssArbitrager.ACTION_PKG_PREFIX)) {
                    packageName = packageName.substring(HwMssArbitrager.ACTION_PKG_PREFIX.length());
                }
                if (!TextUtils.isEmpty(packageName)) {
                    if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                        HwMssUtils.log(3, false, HwMssArbitrager.TAG, "packageName:%{public}s", packageName);
                        for (String pkgName : HwMssArbitrager.this.mInstalledPkgNameList) {
                            if (pkgName.equals(packageName)) {
                                return;
                            }
                        }
                        HwMssArbitrager.this.mInstalledPkgNameList.add(packageName);
                        HwMssArbitrager.this.checkMssLimitPackage(packageName);
                    } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                        HwMssUtils.log(3, false, HwMssArbitrager.TAG, "packageName:%{public}s", packageName);
                        for (int idx = HwMssArbitrager.this.mInstalledPkgNameList.size() - 1; idx >= 0; idx--) {
                            if (((String) HwMssArbitrager.this.mInstalledPkgNameList.get(idx)).equals(packageName)) {
                                HwMssUtils.log(3, false, HwMssArbitrager.TAG, "match:packageName:%{public}s", packageName);
                                HwMssArbitrager.this.mInstalledPkgNameList.remove(idx);
                                return;
                            }
                        }
                    } else {
                        HwMssUtils.log(3, false, HwMssArbitrager.TAG, "no handle network action of state change", new Object[0]);
                    }
                }
            }
        }
    };
    private int mPluggedType = 0;
    private PowerKit mPowerKit = null;
    private final Object mSyncLock = new Object();
    private boolean mWiFiApMode = false;
    private WifiManager mWifiManager = null;

    public interface IHwMssObserver {
        void onHt40Request();

        void onMssSwitchRequest(int i);
    }

    public enum MssState {
        MSSUNKNOWN,
        MSSMIMO,
        MSSSISO,
        ABSMIMO,
        ABSMRC,
        ABSSWITCHING,
        MSSSWITCHING
    }

    public enum MssTrigType {
        CLONE_TRIG,
        COMMON_TRIG
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.wifi.MSS.HwMssArbitrager$3  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass3 {
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

    private HwMssArbitrager(Context cxt) {
        this.mContext = cxt;
        if (HwMssUtils.is1103() || HwMssUtils.is1105()) {
            this.mMssBlackMgrt = HisiMssBlackListManager.getInstance(this.mContext);
        } else {
            this.mMssBlackMgrt = HwMssBlackListManager.getInstance(this.mContext);
        }
        getMssSwitchCapa();
        getWhiteList();
        getHt40WhiteList();
        getAllInstalledPkg();
        getAllLimitSwitchApk();
        getAllowMssApkList();
        registerBcastReceiver();
        registerPkgChangedReceiver();
        getIncompatibleBssidList();
    }

    public static synchronized HwMssArbitrager getInstance(Context cxt) {
        HwMssArbitrager hwMssArbitrager;
        synchronized (HwMssArbitrager.class) {
            if (sInstance == null) {
                sInstance = new HwMssArbitrager(cxt);
            }
            hwMssArbitrager = sInstance;
        }
        return hwMssArbitrager;
    }

    public boolean setAbsCurrentState(MssState state) {
        HwMssUtils.log(1, false, TAG, "setAbsCurrentState:%{public}d", state);
        if (state != MssState.ABSMIMO && state != MssState.ABSMRC && state != MssState.ABSSWITCHING) {
            return false;
        }
        this.mAbsCurrentState = state;
        HwMssUtils.switchToast(this.mContext, this.mAbsCurrentState);
        return true;
    }

    public void setMssCurrentState(MssState state) {
        HwMssUtils.log(1, false, TAG, "setMssCurrentState:%{public}s", state);
        if (state == MssState.MSSMIMO || state == MssState.MSSSISO || state == MssState.MSSUNKNOWN || state == MssState.MSSSWITCHING) {
            this.mMssCurrentState = state;
            this.mHwWifiChrService = HwWifiCHRServiceImpl.getInstance();
            HwWifiCHRService hwWifiCHRService = this.mHwWifiChrService;
            if (hwWifiCHRService != null) {
                hwWifiCHRService.updateMSSState(this.mMssCurrentState.toString());
            }
            HwMssUtils.switchToast(this.mContext, this.mMssCurrentState);
        }
    }

    public void setGameForeground(boolean enable) {
        HwMssUtils.log(1, false, TAG, "setGameForeground:%{public}s", String.valueOf(enable));
        this.mGameForeground = enable;
    }

    public boolean isGameForeground() {
        return this.mGameForeground;
    }

    public MssState getMssCurrentState() {
        return this.mMssCurrentState;
    }

    public MssState getAbsCurrentState() {
        return this.mAbsCurrentState;
    }

    public void registerMssObserver(IHwMssObserver observer) {
        for (IHwMssObserver item : this.mMssObservers) {
            if (item == observer) {
                return;
            }
        }
        this.mMssObservers.add(observer);
    }

    public void unregisterMssObserver(IHwMssObserver observer) {
        for (int i = this.mMssObservers.size() - 1; i >= 0; i--) {
            if (this.mMssObservers.get(i) == observer) {
                this.mMssObservers.remove(i);
            }
        }
    }

    public boolean isSupportHt40() {
        return this.mMssSwitchCapa > 0;
    }

    public boolean isAbsAllowed(int direction, int freq, MssTrigType reason, long now) {
        if (direction == 1 && isChargePluggedin() && reason != MssTrigType.CLONE_TRIG && SystemProperties.getInt("runtime.hwmss.debug", 0) == 0) {
            HwMssUtils.log(1, false, TAG, "isMssAllowed isChargePluggedin:false", new Object[0]);
            return false;
        } else if (!isChipSwitchSupport(freq)) {
            HwMssUtils.log(1, false, TAG, "isMssAllowed isChipSwitchSupport:false", new Object[0]);
            return false;
        } else {
            if (!this.mWiFiApMode) {
                if (!isWiFiConnected()) {
                    HwMssUtils.log(1, false, TAG, "isMssAllowed isWiFiConnected: false", new Object[0]);
                    return false;
                } else if (SystemProperties.getInt("runtime.hwmss.blktest", 0) == 0 && direction == 1 && isInMssBlacklist()) {
                    HwMssUtils.log(1, false, TAG, "isMssAllowed isInMssBlacklist true", new Object[0]);
                    return false;
                }
            }
            if (isP2pConnected()) {
                HwMssUtils.log(1, false, TAG, "isMssAllowed isP2pConnected true", new Object[0]);
                return false;
            } else if (direction == 1 && isLimitApkonFront()) {
                HwMssUtils.log(1, false, TAG, "isLimitApkonFront true", new Object[0]);
                return false;
            } else if (direction != 1 || !matchMssLimitList() || reason == MssTrigType.CLONE_TRIG) {
                this.mMssSwitchTimestamp = now;
                return true;
            } else {
                HwMssUtils.log(1, false, TAG, "isMssAllowed: matchMssLimitList true", new Object[0]);
                return false;
            }
        }
    }

    public boolean isMssAllowed(int direction, int freq, MssTrigType reason) {
        long now = SystemClock.elapsedRealtime();
        if (direction == 2 && this.mIsSisoFixFlag) {
            HwMssUtils.log(1, false, TAG, "isMssAllowed false: Mobile Clone", new Object[0]);
            return false;
        } else if (SystemProperties.getInt("runtime.hwmss.blktest", 0) == 0 && direction == 1 && now - this.mMssSwitchTimestamp < MIN_MSS_TIME_SPAN) {
            HwMssUtils.log(1, false, TAG, "isMssAllowed false: time limit", new Object[0]);
            return false;
        } else if (SystemProperties.getInt("persist.hwmss.switch", 0) == 1) {
            HwMssUtils.log(1, false, TAG, "doMssSwitch persist.hwmss.switch:false", new Object[0]);
            return false;
        } else if (SystemProperties.getInt("runtime.hwmss.bssid", 0) == 0 && !this.mWiFiApMode && isIncompatibleBssid(this.mCurrentBssid)) {
            HwMssUtils.log(1, false, TAG, "isMssAllowed isIncompatibleBssid true", new Object[0]);
            return false;
        } else if (isGameForeground()) {
            HwMssUtils.log(1, false, TAG, "isMssAllowed isGameForeground true", new Object[0]);
            return false;
        } else if (this.mAbsCurrentState == MssState.ABSMIMO || (this.mAbsCurrentState == MssState.ABSMRC && ScanResult.is5GHz(freq))) {
            return isAbsAllowed(direction, freq, reason, now);
        } else {
            HwMssUtils.log(1, false, TAG, "isMssAllowed false: ABSState %{public}d", this.mAbsCurrentState);
            return false;
        }
    }

    public boolean isMssSwitchBandSupport() {
        if ((this.mMssSwitchCapa & 1) > 0) {
            return HwMssUtils.isAllowSwitch();
        }
        return false;
    }

    public boolean isMatchHt40List() {
        if (this.mHt40PkgList == null || this.mInstalledPkgNameList.size() == 0) {
            return false;
        }
        for (int i = 0; i < this.mHt40PkgList.length; i++) {
            for (int j = this.mInstalledPkgNameList.size() - 1; j >= 0; j--) {
                String pkgname = this.mInstalledPkgNameList.get(j);
                if (pkgname != null && pkgname.contains(this.mHt40PkgList[i])) {
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

    private void getAllInstalledPkg() {
        List<PackageInfo> pkgInfoList = this.mContext.getPackageManager().getInstalledPackages(0);
        this.mInstalledPkgNameList.clear();
        HwMssUtils.log(3, false, TAG, "getAllInstalledPkg:", new Object[0]);
        for (PackageInfo info : pkgInfoList) {
            this.mInstalledPkgNameList.add(info.packageName);
            HwMssUtils.log(3, false, TAG, info.packageName, new Object[0]);
        }
    }

    private void getWhiteList() {
        this.mMssLimitList = new String[]{"com.magicandroidapps.iperf", "net.he.networktools", "org.zwanoo.android.speedtest", "com.example.wptp.testapp", "com.veriwave.waveagent"};
    }

    private void getHt40WhiteList() {
        this.mHt40PkgList = new String[]{"com.example.wptp.testapp", "com.veriwave.waveagent"};
    }

    private void getAllowMssApkList() {
        this.mAllowMssApkList = new String[]{"com.ixia.ixchariot"};
    }

    private void getAllLimitSwitchApk() {
        this.mMssLimitSwitchApkList = new String[]{"com.dewmobile.kuaiya"};
    }

    private void getIncompatibleBssidList() {
        this.mIncompatibleBssidList = new String[]{"b0:d5:9d", "c8:d5:fe", "70:b0:35"};
    }

    private boolean matchMssLimitList() {
        if (this.mMssLimitList == null || this.mInstalledPkgNameList.size() == 0) {
            return false;
        }
        for (int i = 0; i < this.mMssLimitList.length; i++) {
            for (int j = this.mInstalledPkgNameList.size() - 1; j >= 0; j--) {
                String pkgname = this.mInstalledPkgNameList.get(j);
                if (pkgname != null && pkgname.contains(this.mMssLimitList[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean matchAllowMssApkList() {
        if (this.mAllowMssApkList == null || this.mInstalledPkgNameList.size() == 0) {
            return false;
        }
        for (int i = 0; i < this.mAllowMssApkList.length; i++) {
            for (int j = this.mInstalledPkgNameList.size() - 1; j >= 0; j--) {
                String apkname = this.mInstalledPkgNameList.get(j);
                if (apkname != null && apkname.contains(this.mAllowMssApkList[i])) {
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

    public boolean isP2pConnected() {
        return this.isP2pConnectState;
    }

    public boolean isWiFiConnected() {
        return this.isWiFiConnectState;
    }

    private boolean isLimitApkonFront() {
        if (this.mPowerKit == null) {
            this.mPowerKit = PowerKit.getInstance();
        }
        PowerKit powerKit = this.mPowerKit;
        if (powerKit != null) {
            try {
                String pktName = powerKit.getTopFrontApp(this.mContext);
                int i = 0;
                while (true) {
                    String[] strArr = this.mMssLimitSwitchApkList;
                    if (i >= strArr.length) {
                        break;
                    } else if (strArr[i].equals(pktName)) {
                        HwMssUtils.log(1, false, TAG, "found limit APK: %{public}s", pktName);
                        return true;
                    } else {
                        i++;
                    }
                }
            } catch (RemoteException e) {
                HwMssUtils.log(1, false, TAG, "get top front app fail", new Object[0]);
                return false;
            }
        }
        return false;
    }

    public boolean isInMssBlacklist() {
        String ssid = "";
        String bssid = "";
        synchronized (this.mSyncLock) {
            if (this.mConnectWifiInfo != null) {
                ssid = this.mConnectWifiInfo.getSSID();
                bssid = this.mConnectWifiInfo.getBSSID();
            }
        }
        if (((HwMssUtils.is1103() || HwMssUtils.is1105()) && !TextUtils.isEmpty(bssid) && this.mMssBlackMgrt.isInBlacklistByBssid(bssid)) || TextUtils.isEmpty(ssid)) {
            return true;
        }
        return this.mMssBlackMgrt.isInBlacklist(ssid);
    }

    private void getMssSwitchCapa() {
        this.mMssSwitchCapa = 0;
        String chipName = SystemProperties.get("ro.connectivity.sub_chiptype", "");
        if (BRCM_CHIP_4359.equals(chipName)) {
            if (SystemProperties.get("ro.product.name", "").startsWith(PRODUCT_VTR_PREFIX)) {
                this.mMssSwitchCapa = 1;
            } else {
                this.mMssSwitchCapa = 3;
            }
        } else if (HwMssUtils.HISI_CHIP_1103.equals(chipName) || HwMssUtils.HISI_CHIP_1105.equals(chipName)) {
            this.mMssSwitchCapa = 3;
        }
    }

    private boolean isChipSwitchSupport(int freq) {
        int i = this.mMssSwitchCapa;
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

    private void checkMssLimitList(String packageName) {
        if (this.mMssObservers.size() != 0) {
            int i = 0;
            while (true) {
                String[] strArr = this.mMssLimitList;
                if (i >= strArr.length) {
                    return;
                }
                if (strArr[i].equals(packageName)) {
                    for (IHwMssObserver observer : this.mMssObservers) {
                        observer.onMssSwitchRequest(2);
                    }
                    return;
                }
                i++;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkMssLimitPackage(String packageName) {
        if (this.mMssObservers.size() != 0) {
            if (this.mAbsCurrentState == MssState.ABSMIMO && this.mMssCurrentState == MssState.MSSSISO) {
                checkMssLimitList(packageName);
            }
            int i = 0;
            while (true) {
                String[] strArr = this.mHt40PkgList;
                if (i >= strArr.length) {
                    return;
                }
                if (strArr[i].equals(packageName)) {
                    for (IHwMssObserver observer : this.mMssObservers) {
                        observer.onHt40Request();
                    }
                    return;
                }
                i++;
            }
        }
    }

    private boolean isIncompatibleBssid(String bssid) {
        if (bssid == null || bssid.length() < 8) {
            HwMssUtils.log(1, false, TAG, "found incompatible vendor", new Object[0]);
            return true;
        }
        int i = 0;
        while (true) {
            String[] strArr = this.mIncompatibleBssidList;
            if (i >= strArr.length) {
                return false;
            }
            if (strArr[i].equals(bssid.substring(0, 8))) {
                HwMssUtils.log(1, false, TAG, "found incompatible vendor: %{private}s", bssid.substring(0, 8));
                return true;
            }
            i++;
        }
    }

    public void setSisoFixFlag(boolean value) {
        this.mIsSisoFixFlag = value;
    }

    public boolean getSisoFixFlag() {
        return this.mIsSisoFixFlag;
    }
}
