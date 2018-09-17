package com.android.server.wifi.MSS;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.huawei.pgmng.plug.PGSdk;
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
        private static final /* synthetic */ int[] -android-net-NetworkInfo$DetailedStateSwitchesValues = null;
        final /* synthetic */ int[] $SWITCH_TABLE$android$net$NetworkInfo$DetailedState;

        private static /* synthetic */ int[] -getandroid-net-NetworkInfo$DetailedStateSwitchesValues() {
            if (-android-net-NetworkInfo$DetailedStateSwitchesValues != null) {
                return -android-net-NetworkInfo$DetailedStateSwitchesValues;
            }
            int[] iArr = new int[DetailedState.values().length];
            try {
                iArr[DetailedState.AUTHENTICATING.ordinal()] = 3;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[DetailedState.BLOCKED.ordinal()] = 4;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[DetailedState.CAPTIVE_PORTAL_CHECK.ordinal()] = 5;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[DetailedState.CONNECTED.ordinal()] = 1;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[DetailedState.CONNECTING.ordinal()] = 6;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[DetailedState.DISCONNECTED.ordinal()] = 2;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[DetailedState.DISCONNECTING.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[DetailedState.FAILED.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[DetailedState.IDLE.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[DetailedState.OBTAINING_IPADDR.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[DetailedState.SCANNING.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[DetailedState.SUSPENDED.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                iArr[DetailedState.VERIFYING_POOR_LINK.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
            -android-net-NetworkInfo$DetailedStateSwitchesValues = iArr;
            return iArr;
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            HwMSSUtils.log(3, HwMSSArbitrager.TAG, "action:" + action);
            if (!"android.intent.action.BATTERY_CHANGED".equals(action)) {
                NetworkInfo networkInfo;
                if (!"android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                    if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                        networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                        if (networkInfo != null) {
                            switch (AnonymousClass2.-getandroid-net-NetworkInfo$DetailedStateSwitchesValues()[networkInfo.getDetailedState().ordinal()]) {
                                case 1:
                                    HwMSSUtils.log(3, HwMSSArbitrager.TAG, "connect enter");
                                    HwMSSArbitrager.this.mWiFiConnectState = true;
                                    synchronized (HwMSSArbitrager.this.mSyncLock) {
                                        if (intent.hasExtra("wifiInfo")) {
                                            HwMSSArbitrager.this.mConnectWifiInfo = (WifiInfo) intent.getParcelableExtra("wifiInfo");
                                        } else {
                                            HwMSSUtils.log(3, HwMSSArbitrager.TAG, "connect is null");
                                            HwMSSArbitrager.this.mConnectWifiInfo = null;
                                        }
                                    }
                                    if (HwMSSArbitrager.this.mConnectWifiInfo != null) {
                                        HwMSSUtils.log(3, HwMSSArbitrager.TAG, "ssid:" + HwMSSArbitrager.this.mConnectWifiInfo.getSSID() + ",bssid:" + HwMSSArbitrager.this.mConnectWifiInfo.getBSSID() + ",freq:" + HwMSSArbitrager.this.mConnectWifiInfo.getFrequency());
                                        HwMSSArbitrager.this.mCurrentBssid = HwMSSArbitrager.this.mConnectWifiInfo.getBSSID();
                                        break;
                                    }
                                    break;
                                case 2:
                                    HwMSSArbitrager.this.mWiFiConnectState = false;
                                    HwMSSArbitrager.this.mCurrentBssid = null;
                                    break;
                            }
                        }
                        return;
                    }
                }
                networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                if (networkInfo != null) {
                    HwMSSUtils.log(3, HwMSSArbitrager.TAG, "Received WIFI_P2P_CONNECTION_CHANGED_ACTION: networkInfo=" + networkInfo);
                    HwMSSArbitrager.this.mP2pConnectState = networkInfo.isConnected();
                    HwMSSUtils.log(3, HwMSSArbitrager.TAG, "mP2pConnectState: " + HwMSSArbitrager.this.mP2pConnectState);
                }
            } else {
                HwMSSArbitrager.this.mPluggedType = intent.getIntExtra("plugged", 0);
                HwMSSUtils.log(3, HwMSSArbitrager.TAG, "mPluggedType:" + HwMSSArbitrager.this.mPluggedType);
                if (SystemProperties.getInt("runtime.hwmss.debug", 0) == 0 && HwMSSArbitrager.this.isChargePluggedin()) {
                    for (IHwMSSObserver observer : HwMSSArbitrager.this.mMSSObservers) {
                        observer.onMSSSwitchRequest(2);
                    }
                }
            }
        }
    };
    private WifiInfo mConnectWifiInfo = null;
    private Context mContext;
    private String mCurrentBssid = null;
    private String[] mHT40PkgList = null;
    private String[] mIncompatibleBssidList = null;
    private List<String> mInstalledPkgNameList = new ArrayList();
    private HwMSSBlackListManager mMSSBlackMgrt = null;
    private MSSState mMSSCurrentState = MSSState.MSSMIMO;
    private String[] mMSSLimitList = null;
    private String[] mMSSLimitSwitchAPKList = null;
    private List<IHwMSSObserver> mMSSObservers = new ArrayList();
    private int mMSSSwitchCapa = 0;
    private long mMssSwitchTimestamp = 0;
    private boolean mP2pConnectState = false;
    private PGSdk mPGSdk = null;
    private BroadcastReceiver mPkgChangedReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                HwMSSUtils.log(3, HwMSSArbitrager.TAG, "action:" + action);
                String packageName = intent.getDataString();
                if (packageName != null && packageName.startsWith(HwMSSArbitrager.ACTION_PKG_PREFIX)) {
                    packageName = packageName.substring(HwMSSArbitrager.ACTION_PKG_PREFIX.length());
                }
                if (!TextUtils.isEmpty(packageName)) {
                    if ("android.intent.action.PACKAGE_ADDED".equals(action)) {
                        HwMSSUtils.log(3, HwMSSArbitrager.TAG, "packageName:" + packageName);
                        for (String pkgName : HwMSSArbitrager.this.mInstalledPkgNameList) {
                            if (pkgName.equals(packageName)) {
                                return;
                            }
                        }
                        HwMSSArbitrager.this.mInstalledPkgNameList.add(packageName);
                        HwMSSArbitrager.this.checkMssLimitPackage(packageName);
                    } else if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                        HwMSSUtils.log(3, HwMSSArbitrager.TAG, "packageName:" + packageName);
                        for (int idx = HwMSSArbitrager.this.mInstalledPkgNameList.size() - 1; idx >= 0; idx--) {
                            if (((String) HwMSSArbitrager.this.mInstalledPkgNameList.get(idx)).equals(packageName)) {
                                HwMSSUtils.log(3, HwMSSArbitrager.TAG, "match:packageName:" + packageName);
                                HwMSSArbitrager.this.mInstalledPkgNameList.remove(idx);
                                break;
                            }
                        }
                    }
                }
            }
        }
    };
    private int mPluggedType = 0;
    private Object mSyncLock = new Object();
    private boolean mWiFiConnectState = false;

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
        ABSSWITCHING
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
        HwMSSUtils.log(1, TAG, "setABSCurrentState:" + state);
        if (state != MSSState.ABSMIMO && state != MSSState.ABSMRC && state != MSSState.ABSSWITCHING) {
            return false;
        }
        this.mABSCurrentState = state;
        HwMSSUtils.switchToast(this.mContext, this.mABSCurrentState);
        return true;
    }

    public boolean setMSSCurrentState(MSSState state) {
        HwMSSUtils.log(1, TAG, "setMSSCurrentState:" + state);
        if (state != MSSState.MSSMIMO && state != MSSState.MSSSISO && state != MSSState.MSSUNKNOWN) {
            return false;
        }
        this.mMSSCurrentState = state;
        HwMSSUtils.switchToast(this.mContext, this.mMSSCurrentState);
        return true;
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
        this.mMSSBlackMgrt = HwMSSBlackListManager.getInstance(this.mContext);
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

    public boolean isMSSAllowed(int direction, int freq) {
        long now = SystemClock.elapsedRealtime();
        if (now - this.mMssSwitchTimestamp < MIN_MSS_TIME_SPAN) {
            HwMSSUtils.log(1, TAG, "isMSSAllowed false: time limit");
            return false;
        } else if (1 == SystemProperties.getInt("persist.hwmss.switch", 0)) {
            HwMSSUtils.log(1, TAG, "doMssSwitch persist.hwmss.switch:false");
            return false;
        } else if (SystemProperties.getInt("runtime.hwmss.bssid", 0) == 0 && isIncompatibleBssid(this.mCurrentBssid)) {
            HwMSSUtils.log(1, TAG, "isMSSAllowed isIncompatibleBssid true");
            return false;
        } else if (this.mABSCurrentState != MSSState.ABSMIMO && (this.mABSCurrentState != MSSState.ABSMRC || !ScanResult.is5GHz(freq))) {
            HwMSSUtils.log(1, TAG, "isMSSAllowed false: ABSState " + this.mABSCurrentState);
            return false;
        } else if (direction == 1 && isChargePluggedin() && SystemProperties.getInt("runtime.hwmss.debug", 0) == 0) {
            HwMSSUtils.log(1, TAG, "isMSSAllowed isChargePluggedin:false");
            return false;
        } else if (!isChipSwitchSupport(freq)) {
            HwMSSUtils.log(1, TAG, "isMSSAllowed isChipSwitchSupport:false");
            return false;
        } else if (!isWiFiConnected()) {
            HwMSSUtils.log(1, TAG, "isMSSAllowed isWiFiConnected: false");
            return false;
        } else if (isP2PConnected()) {
            HwMSSUtils.log(1, TAG, "isMSSAllowed isP2PConnected true");
            return false;
        } else if (isInMSSBlacklist()) {
            HwMSSUtils.log(1, TAG, "isMSSAllowed isInMSSBlacklist true");
            return false;
        } else if (direction == 1 && isMobileHotspot() && isLimitAPKonFront()) {
            HwMSSUtils.log(1, TAG, "isMSSAllowed isMobileHotspot true");
            return false;
        } else if (direction == 1 && matchMssLimitList()) {
            HwMSSUtils.log(1, TAG, "isMSSAllowed: matchMssLimitList true");
            return false;
        } else {
            this.mMssSwitchTimestamp = now;
            return true;
        }
    }

    public boolean isMSSSwitchBandSupport() {
        if ((this.mMSSSwitchCapa & 1) > 0) {
            return true;
        }
        return false;
    }

    public boolean matchHT40List() {
        String pkgname = "";
        if (this.mHT40PkgList == null || this.mInstalledPkgNameList.size() == 0) {
            return false;
        }
        int i = 0;
        while (i < this.mHT40PkgList.length) {
            for (int j = this.mInstalledPkgNameList.size() - 1; j >= 0; j--) {
                pkgname = (String) this.mInstalledPkgNameList.get(j);
                if (pkgname != null && pkgname.contains(this.mHT40PkgList[i])) {
                    return true;
                }
            }
            i++;
        }
        return false;
    }

    public int readSaveMode() {
        return System.getIntForUser(this.mContext.getContentResolver(), SMART_MODE_STATUS, 1, 0);
    }

    private void registerBcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.net.wifi.STATE_CHANGE");
        filter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
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
        HwMSSUtils.log(3, TAG, "getAllInstalledPkg:");
        for (PackageInfo info : pkgInfoList) {
            this.mInstalledPkgNameList.add(info.packageName);
            HwMSSUtils.log(3, TAG, info.packageName);
        }
    }

    private void getWhiteList() {
        this.mMSSLimitList = new String[]{"com.magicandroidapps.iperf", "net.he.networktools", "org.zwanoo.android.speedtest", "com.example.wptp.testapp", "com.veriwave.waveagent"};
    }

    public void getAllowMSSApkList() {
        this.mAllowMSSApkList = new String[]{"com.ixia.ixchariot"};
    }

    private void getHT40WhiteList() {
        this.mHT40PkgList = new String[]{"com.example.wptp.testapp", "com.veriwave.waveagent"};
    }

    private void getAllLimitSwitchAPK() {
        this.mMSSLimitSwitchAPKList = new String[]{"com.hicloud.android.clone", "com.dewmobile.kuaiya"};
    }

    private void getIncompatibleBssidList() {
        this.mIncompatibleBssidList = new String[]{"b0:d5:9d", "c8:d5:fe", "70:b0:35"};
    }

    private boolean matchMssLimitList() {
        String pkgname = "";
        if (this.mMSSLimitList == null || this.mInstalledPkgNameList.size() == 0) {
            return false;
        }
        int i = 0;
        while (i < this.mMSSLimitList.length) {
            for (int j = this.mInstalledPkgNameList.size() - 1; j >= 0; j--) {
                pkgname = (String) this.mInstalledPkgNameList.get(j);
                if (pkgname != null && pkgname.contains(this.mMSSLimitList[i])) {
                    return true;
                }
            }
            i++;
        }
        return false;
    }

    public boolean matchAllowMSSApkList() {
        String apkname = "";
        if (this.mAllowMSSApkList == null || this.mInstalledPkgNameList.size() == 0) {
            return false;
        }
        int i = 0;
        while (i < this.mAllowMSSApkList.length) {
            for (int j = this.mInstalledPkgNameList.size() - 1; j >= 0; j--) {
                apkname = (String) this.mInstalledPkgNameList.get(j);
                if (apkname != null && apkname.contains(this.mAllowMSSApkList[i])) {
                    return true;
                }
            }
            i++;
        }
        return false;
    }

    private boolean isChargePluggedin() {
        if (this.mPluggedType == 2 || this.mPluggedType == 5) {
            return true;
        }
        return false;
    }

    private boolean isP2PConnected() {
        return this.mP2pConnectState;
    }

    public boolean isWiFiConnected() {
        return this.mWiFiConnectState;
    }

    private boolean isMobileHotspot() {
        return HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.mContext);
    }

    private boolean isLimitAPKonFront() {
        if (this.mPGSdk == null) {
            this.mPGSdk = PGSdk.getInstance();
        }
        if (this.mPGSdk != null) {
            try {
                String pktName = this.mPGSdk.getTopFrontApp(this.mContext);
                for (String equals : this.mMSSLimitSwitchAPKList) {
                    if (equals.equals(pktName)) {
                        HwMSSUtils.log(1, TAG, "found limit APK: " + pktName);
                        return true;
                    }
                }
            } catch (RemoteException e) {
                HwMSSUtils.log(1, TAG, "get top front app fail");
                return false;
            }
        }
        return false;
    }

    private boolean isInMSSBlacklist() {
        String ssid = "";
        synchronized (this.mSyncLock) {
            if (this.mConnectWifiInfo != null) {
                ssid = this.mConnectWifiInfo.getSSID();
            }
        }
        if (TextUtils.isEmpty(ssid)) {
            return true;
        }
        return this.mMSSBlackMgrt.isInBlacklist(ssid);
    }

    private void getMSSSwitchCapa() {
        this.mMSSSwitchCapa = 0;
        if (!BRCM_CHIP_4359.equals(SystemProperties.get("ro.connectivity.sub_chiptype", ""))) {
            return;
        }
        if (SystemProperties.get("ro.product.name", "").startsWith(PRODUCT_VTR_PREFIX)) {
            this.mMSSSwitchCapa = 1;
        } else {
            this.mMSSSwitchCapa = 3;
        }
    }

    /* JADX WARNING: Missing block: B:14:0x001a, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isChipSwitchSupport(int freq) {
        if (this.mMSSSwitchCapa == 3) {
            return true;
        }
        if (this.mMSSSwitchCapa != 1) {
            return false;
        }
        synchronized (this.mSyncLock) {
            if (freq < 2412 || freq > MAX_FREQ_24G) {
            } else {
                return true;
            }
        }
    }

    private void checkMssLimitPackage(String packageName) {
        if (this.mMSSObservers.size() != 0) {
            int i;
            if (this.mABSCurrentState == MSSState.ABSMIMO && this.mMSSCurrentState == MSSState.MSSSISO) {
                i = 0;
                while (i < this.mMSSLimitList.length) {
                    if (this.mMSSLimitList[i].equals(packageName)) {
                        for (IHwMSSObserver observer : this.mMSSObservers) {
                            observer.onMSSSwitchRequest(2);
                        }
                    } else {
                        i++;
                    }
                }
            }
            for (String equals : this.mHT40PkgList) {
                if (equals.equals(packageName)) {
                    for (IHwMSSObserver observer2 : this.mMSSObservers) {
                        observer2.onHT40Request();
                    }
                }
            }
        }
    }

    private boolean isIncompatibleBssid(String bssid) {
        if (bssid == null || bssid.length() < 8) {
            HwMSSUtils.log(1, TAG, "found incompatible vendor");
            return true;
        }
        for (String equals : this.mIncompatibleBssidList) {
            if (equals.equals(bssid.substring(0, 8))) {
                HwMSSUtils.log(1, TAG, "found incompatible vendor: " + bssid.substring(0, 8));
                return true;
            }
        }
        return false;
    }
}
