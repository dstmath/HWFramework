package com.android.server.wifi;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.DhcpInfo;
import android.net.IpConfiguration.ProxySettings;
import android.net.NetworkUtils;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.System;
import android.util.Log;
import com.huawei.device.connectivitychrlog.CSubBTStatus;
import com.huawei.device.connectivitychrlog.CSubCellID;
import com.huawei.device.connectivitychrlog.CSubNET_CFG;
import com.huawei.device.connectivitychrlog.LogByte;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class HwCHRWifiRelatedStateMonitor {
    private static final /* synthetic */ int[] -android-net-IpConfiguration$ProxySettingsSwitchesValues = null;
    public static final Uri BTOPP_CONTENT_URI = null;
    public static final String BTOPP_CURRENT_BYTES = "current_bytes";
    public static final String BTOPP_DATA = "_data";
    public static final String BTOPP_STATUS = "status";
    public static final int BTOPP_STATUS_RUNNING = 192;
    public static final String BTOPP_TOTAL_BYTES = "total_bytes";
    private static final int GET_BTSTATUS_WAIT_TIME = 1;
    private static final boolean HWFLOW = false;
    private static final int PROXY_NONE = 0;
    private static final int PROXY_PAC = 3;
    private static final int PROXY_STATIC = 1;
    private static final int PROXY_UNASSIGNED = 2;
    private static final String SETTING_SECURE_VPN_WORK_VALUE = "wifi_network_vpn_state";
    private static final int STATE_SCANING = 8;
    private static final String TAG = "HwCHRWifiRelatedStateMonitor";
    private static HwCHRWifiRelatedStateMonitor instance;
    private BluetoothA2dp mBluetoothA2dp;
    private final ServiceListener mBluetoothA2dpServiceListener;
    private BluetoothHeadset mBluetoothHeadset;
    private final ServiceListener mBluetoothProfileServiceListener;
    private HwCHRWifiCellID mCellID;
    private Context mContext;
    private MonitorHandler mHandler;
    private ReentrantLock mLock;
    private WifiConfiguration mRecentConfig;
    private HandlerThread mThread;
    private int mVPN;
    private WifiManager mWM;

    private static class MonitorHandler extends Handler {
        public MonitorHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
        }
    }

    private class VPNStateMonitor extends ContentObserver {
        public VPNStateMonitor(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            HwCHRWifiRelatedStateMonitor.this.mVPN = System.getInt(HwCHRWifiRelatedStateMonitor.this.mContext.getContentResolver(), HwCHRWifiRelatedStateMonitor.SETTING_SECURE_VPN_WORK_VALUE, HwCHRWifiRelatedStateMonitor.PROXY_NONE);
            Log.d(HwCHRWifiRelatedStateMonitor.TAG, "wifi_network_vpn_state = " + HwCHRWifiRelatedStateMonitor.this.mVPN);
        }
    }

    private static /* synthetic */ int[] -getandroid-net-IpConfiguration$ProxySettingsSwitchesValues() {
        if (-android-net-IpConfiguration$ProxySettingsSwitchesValues != null) {
            return -android-net-IpConfiguration$ProxySettingsSwitchesValues;
        }
        int[] iArr = new int[ProxySettings.values().length];
        try {
            iArr[ProxySettings.NONE.ordinal()] = 4;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ProxySettings.PAC.ordinal()] = PROXY_STATIC;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ProxySettings.STATIC.ordinal()] = PROXY_UNASSIGNED;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ProxySettings.UNASSIGNED.ordinal()] = PROXY_PAC;
        } catch (NoSuchFieldError e4) {
        }
        -android-net-IpConfiguration$ProxySettingsSwitchesValues = iArr;
        return iArr;
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HwCHRWifiRelatedStateMonitor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HwCHRWifiRelatedStateMonitor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwCHRWifiRelatedStateMonitor.<clinit>():void");
    }

    private HwCHRWifiRelatedStateMonitor(Context context) {
        this.mRecentConfig = null;
        this.mVPN = PROXY_NONE;
        this.mWM = null;
        this.mLock = new ReentrantLock();
        this.mBluetoothProfileServiceListener = new ServiceListener() {
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                try {
                    if (HwCHRWifiRelatedStateMonitor.this.mLock.tryLock(1, TimeUnit.SECONDS)) {
                        try {
                            HwCHRWifiRelatedStateMonitor.this.mBluetoothHeadset = (BluetoothHeadset) proxy;
                        } catch (ClassCastException e) {
                            if (HwCHRWifiRelatedStateMonitor.HWFLOW) {
                                Log.d(HwCHRWifiRelatedStateMonitor.TAG, "BluetoothProfileServiceListener assign value failed");
                            }
                            Log.d(HwCHRWifiRelatedStateMonitor.TAG, "  mBluetoothHeadset onServiceConnected ");
                        } finally {
                            HwCHRWifiRelatedStateMonitor.this.mLock.unlock();
                        }
                    }
                } catch (InterruptedException e2) {
                    if (HwCHRWifiRelatedStateMonitor.HWFLOW) {
                        Log.d(HwCHRWifiRelatedStateMonitor.TAG, "BluetoothProfileServiceListener ReentrantLock.tryLock failed");
                    }
                }
                Log.d(HwCHRWifiRelatedStateMonitor.TAG, "  mBluetoothHeadset onServiceConnected ");
            }

            public void onServiceDisconnected(int profile) {
                try {
                    boolean bGetLock = HwCHRWifiRelatedStateMonitor.this.mLock.tryLock(1, TimeUnit.SECONDS);
                    if (bGetLock) {
                        HwCHRWifiRelatedStateMonitor.this.mBluetoothHeadset = null;
                    }
                    if (bGetLock) {
                        HwCHRWifiRelatedStateMonitor.this.mLock.unlock();
                    }
                } catch (ClassCastException e) {
                    if (HwCHRWifiRelatedStateMonitor.HWFLOW) {
                        Log.d(HwCHRWifiRelatedStateMonitor.TAG, "BluetoothProfileServiceListener onServiceDisconnected assign value failed");
                    }
                    if (HwCHRWifiRelatedStateMonitor.PROXY_NONE != null) {
                        HwCHRWifiRelatedStateMonitor.this.mLock.unlock();
                    }
                } catch (InterruptedException e2) {
                    if (HwCHRWifiRelatedStateMonitor.HWFLOW) {
                        Log.d(HwCHRWifiRelatedStateMonitor.TAG, "BluetoothProfileServiceListener onServiceDisconnected ReentrantLock.tryLock failed");
                    }
                    if (HwCHRWifiRelatedStateMonitor.PROXY_NONE != null) {
                        HwCHRWifiRelatedStateMonitor.this.mLock.unlock();
                    }
                } catch (Throwable th) {
                    if (HwCHRWifiRelatedStateMonitor.PROXY_NONE != null) {
                        HwCHRWifiRelatedStateMonitor.this.mLock.unlock();
                    }
                }
                Log.d(HwCHRWifiRelatedStateMonitor.TAG, "  mBluetoothHeadset onServiceDisconnected ");
            }
        };
        this.mBluetoothA2dpServiceListener = new ServiceListener() {
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                try {
                    if (HwCHRWifiRelatedStateMonitor.this.mLock.tryLock(1, TimeUnit.SECONDS)) {
                        try {
                            HwCHRWifiRelatedStateMonitor.this.mBluetoothA2dp = (BluetoothA2dp) proxy;
                        } catch (ClassCastException e) {
                            if (HwCHRWifiRelatedStateMonitor.HWFLOW) {
                                Log.d(HwCHRWifiRelatedStateMonitor.TAG, "BluetoothA2dpServiceListener onServiceDisconnected assign value failed");
                            }
                            Log.d(HwCHRWifiRelatedStateMonitor.TAG, "  mBluetoothA2dp onServiceConnected ");
                        } finally {
                            HwCHRWifiRelatedStateMonitor.this.mLock.unlock();
                        }
                    }
                } catch (InterruptedException e2) {
                    if (HwCHRWifiRelatedStateMonitor.HWFLOW) {
                        Log.d(HwCHRWifiRelatedStateMonitor.TAG, "BluetoothA2dpServiceListener onServiceDisconnected ReentrantLock.tryLock failed");
                    }
                }
                Log.d(HwCHRWifiRelatedStateMonitor.TAG, "  mBluetoothA2dp onServiceConnected ");
            }

            public void onServiceDisconnected(int profile) {
                try {
                    boolean bGetLock = HwCHRWifiRelatedStateMonitor.this.mLock.tryLock(1, TimeUnit.SECONDS);
                    if (bGetLock) {
                        HwCHRWifiRelatedStateMonitor.this.mBluetoothA2dp = null;
                    }
                    if (bGetLock) {
                        HwCHRWifiRelatedStateMonitor.this.mLock.unlock();
                    }
                } catch (InterruptedException e) {
                    if (HwCHRWifiRelatedStateMonitor.HWFLOW) {
                        Log.d(HwCHRWifiRelatedStateMonitor.TAG, "BluetoothA2dpServiceListener onServiceDisconnected ReentrantLock.tryLock failed");
                    }
                    if (HwCHRWifiRelatedStateMonitor.PROXY_NONE != null) {
                        HwCHRWifiRelatedStateMonitor.this.mLock.unlock();
                    }
                } catch (Throwable th) {
                    if (HwCHRWifiRelatedStateMonitor.PROXY_NONE != null) {
                        HwCHRWifiRelatedStateMonitor.this.mLock.unlock();
                    }
                }
                Log.d(HwCHRWifiRelatedStateMonitor.TAG, "  mBluetoothA2dp onServiceDisconnected ");
            }
        };
        this.mContext = context;
        this.mThread = new HandlerThread("WiFiRelatedStateMonitor");
        this.mThread.start();
        this.mHandler = new MonitorHandler(this.mThread.getLooper());
        this.mCellID = HwCHRWifiCellID.make(context, PROXY_PAC);
        registerVPNStateMonitor();
        BluetoothAdapter mLocalBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mLocalBluetoothAdapter != null) {
            mLocalBluetoothAdapter.getProfileProxy(context, this.mBluetoothProfileServiceListener, PROXY_STATIC);
            mLocalBluetoothAdapter.getProfileProxy(context, this.mBluetoothA2dpServiceListener, PROXY_UNASSIGNED);
        }
        this.mWM = (WifiManager) this.mContext.getSystemService("wifi");
    }

    public static HwCHRWifiRelatedStateMonitor make(Context context) {
        if (instance != null) {
            return instance;
        }
        instance = new HwCHRWifiRelatedStateMonitor(context);
        return instance;
    }

    private void registerVPNStateMonitor() {
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor(SETTING_SECURE_VPN_WORK_VALUE), HWFLOW, new VPNStateMonitor(this.mHandler));
    }

    private int getBluetoothState() {
        BluetoothAdapter mLocalBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mLocalBluetoothAdapter != null) {
            return mLocalBluetoothAdapter.getState();
        }
        return 10;
    }

    private int getBluetoothConnectionState() {
        BluetoothAdapter mLocalBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mLocalBluetoothAdapter == null) {
            return PROXY_NONE;
        }
        int stat = mLocalBluetoothAdapter.getConnectionState();
        if (stat == 0 && mLocalBluetoothAdapter.isDiscovering()) {
            return STATE_SCANING;
        }
        return stat;
    }

    public void updateWIFIConfiguraion(WifiConfiguration cfg) {
        synchronized (this) {
            this.mRecentConfig = cfg;
        }
    }

    public int getProxyStatus() {
        if (this.mRecentConfig == null) {
            return PROXY_NONE;
        }
        switch (-getandroid-net-IpConfiguration$ProxySettingsSwitchesValues()[this.mRecentConfig.getProxySettings().ordinal()]) {
            case PROXY_STATIC /*1*/:
                return PROXY_PAC;
            case PROXY_UNASSIGNED /*2*/:
                return PROXY_STATIC;
            case PROXY_PAC /*3*/:
                return PROXY_UNASSIGNED;
            default:
                return PROXY_NONE;
        }
    }

    public String getProxyInfo() {
        if (this.mRecentConfig == null || this.mRecentConfig.getHttpProxy() == null) {
            return "";
        }
        return this.mRecentConfig.getHttpProxy().toString();
    }

    public int getBTOppStatus() {
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(BTOPP_CONTENT_URI, null, null, null, null);
            if (cursor == null) {
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e3) {
                        Log.d(TAG, "Exception e3: " + e3);
                    }
                }
                return PROXY_NONE;
            }
            int totalBytesIndex = cursor.getColumnIndexOrThrow(BTOPP_TOTAL_BYTES);
            int currentBytesIndex = cursor.getColumnIndexOrThrow(BTOPP_CURRENT_BYTES);
            int StatusIndex = cursor.getColumnIndexOrThrow(BTOPP_STATUS);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                long total = cursor.getLong(totalBytesIndex);
                long current = cursor.getLong(currentBytesIndex);
                int status = cursor.getInt(StatusIndex);
                if (total <= 0 || current <= 0 || status != BTOPP_STATUS_RUNNING) {
                    cursor.moveToNext();
                } else {
                    if (cursor != null) {
                        try {
                            cursor.close();
                        } catch (Exception e32) {
                            Log.d(TAG, "Exception e3: " + e32);
                        }
                    }
                    return PROXY_STATIC;
                }
            }
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e322) {
                    Log.d(TAG, "Exception e3: " + e322);
                }
            }
            return PROXY_NONE;
        } catch (RuntimeException e1) {
            Log.d(TAG, "RuntimeException e1: " + e1);
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e3222) {
                    Log.d(TAG, "Exception e3: " + e3222);
                }
            }
        } catch (Exception e2) {
            Log.d(TAG, "Exception e2:" + e2);
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e32222) {
                    Log.d(TAG, "Exception e3: " + e32222);
                }
            }
        } catch (Throwable th) {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Exception e322222) {
                    Log.d(TAG, "Exception e3: " + e322222);
                }
            }
        }
    }

    public CSubBTStatus getBTStateCHR() {
        int i = PROXY_NONE;
        CSubBTStatus status = new CSubBTStatus();
        int result = PROXY_NONE;
        int count = PROXY_NONE;
        try {
            boolean bGetLocked = this.mLock.tryLock(1, TimeUnit.SECONDS);
            if (bGetLocked) {
                if (this.mBluetoothHeadset != null) {
                    count = this.mBluetoothHeadset.getConnectedDevices().size() + PROXY_NONE;
                    LogByte logByte = status.ucisAudioOn;
                    if (this.mBluetoothHeadset.isAudioOn()) {
                        i = PROXY_STATIC;
                    }
                    logByte.setValue(i);
                }
                if (this.mBluetoothA2dp != null) {
                    count += this.mBluetoothA2dp.getConnectedDevices().size();
                    for (int i2 = PROXY_NONE; i2 < this.mBluetoothA2dp.getConnectedDevices().size(); i2 += PROXY_STATIC) {
                        if (this.mBluetoothA2dp.isA2dpPlaying((BluetoothDevice) this.mBluetoothA2dp.getConnectedDevices().get(i2))) {
                            result = PROXY_STATIC;
                        }
                    }
                }
            }
            if (bGetLocked) {
                this.mLock.unlock();
            }
        } catch (RuntimeException e) {
            if (HWFLOW) {
                Log.d(TAG, "RuntimeException e" + e);
            }
            if (PROXY_NONE != null) {
                this.mLock.unlock();
            }
        } catch (Exception e2) {
            if (HWFLOW) {
                Log.d(TAG, "Exception e" + e2);
            }
            if (PROXY_NONE != null) {
                this.mLock.unlock();
            }
        } catch (Throwable th) {
            if (PROXY_NONE != null) {
                this.mLock.unlock();
            }
        }
        int btopp_status = getBTOppStatus();
        status.ucisA2DPPlaying.setValue(result);
        status.ucConnectedDevicesCnt.setValue((byte) count);
        status.ucBTState.setValue(getBluetoothState());
        status.ucBTConnState.setValue(getBluetoothConnectionState());
        status.ucisOPP.setValue(btopp_status);
        return status;
    }

    public CSubNET_CFG getSSIDSetting() {
        CSubNET_CFG result = new CSubNET_CFG();
        DhcpInfo dhcpInfo = this.mWM.getDhcpInfo();
        synchronized (this) {
            result.strProxySettingInfo.setValue(getProxyInfo());
            result.ucProxySettings.setValue(getProxyStatus());
            if (dhcpInfo != null) {
                result.iIpMask.setValue(NetworkUtils.netmaskIntToPrefixLength(dhcpInfo.netmask));
            }
        }
        result.ucVPN.setValue(this.mVPN);
        return result;
    }

    public CSubCellID getCellIDCHR() {
        return this.mCellID.getCellIDCHR();
    }

    public String getCountryCode() {
        if (this.mWM == null) {
            return "";
        }
        Log.d(TAG, "getCountryCode()=" + this.mWM.getCountryCode());
        return this.mWM.getCountryCode();
    }
}
