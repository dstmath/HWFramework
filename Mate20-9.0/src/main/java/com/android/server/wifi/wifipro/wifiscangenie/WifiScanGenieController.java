package com.android.server.wifi.wifipro.wifiscangenie;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.provider.Settings;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.WifiInjector;
import com.android.server.wifi.wifipro.WifiproUtils;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieDataBaseImpl;
import com.android.server.wifipro.WifiProCommonUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WifiScanGenieController {
    private static final int[] COMMON_CHANNELS = {2412, 2437, 2462, 5180, 5220, 5745, 5765, 5785, 5805, 5825};
    private static final String DEV_5G_AVAILABLE_FREQ_FALG = "SCAN_GENIE_DEV_5G_AVAILABLE_FREQ_FALG";
    private static final String DEV_5G_CAPABILITY_FALG = "SCAN_GENIE_DEV_5G_CAPABILITY_FALG";
    private static final int DEV_5G_CAPABILITY_SUPPORTED = 101;
    private static final int DEV_5G_CAPABILITY_UNKNOWN = 100;
    private static final int DEV_5G_CAPABILITY_UNSUPPORTED = 102;
    private static final int INVAILD_ID = -1;
    public static final int INVALID_FREQ_PUNISHED = -100;
    private static final int MAX_RECENT_CONNECTED_CHANNELS_NUM = 4;
    public static final int MSG_5G_CAPABILITY_QUERY = 6;
    public static final int MSG_BOOT_COMPLETED = 5;
    public static final int MSG_CONFIGURED_CHANGED = 1;
    public static final int MSG_INIT_PREFERRED_CHANNELS = 7;
    public static final int MSG_NETWORK_CONNECTED_NOTIFIED = 2;
    public static final int MSG_NETWORK_DISCONNECTED_NOTIFIED = 8;
    public static final int MSG_NETWORK_ROAMING_COMPLETED_NOTIFIED = 4;
    public static final int MSG_SCAN_RESULTS_AVAILABLE = 3;
    private static final String OOB_STARTUP_GUIDE = "com.huawei.hwstartupguide";
    private static final boolean SCANGENIE_ENABLE = true;
    private static final String TAG = "WifiScanGenie_Controller";
    private static WifiScanGenieController mWifiScanGenieController;
    /* access modifiers changed from: private */
    public boolean m11vSupported = false;
    /* access modifiers changed from: private */
    public Object m5GCapabilityLock = new Object();
    /* access modifiers changed from: private */
    public int m5GFreqCapability = 100;
    private BroadcastReceiver mBroadcastReceiver;
    /* access modifiers changed from: private */
    public CellIdChangedListener mCellIdChangedListener;
    /* access modifiers changed from: private */
    public Object mChannelsLock = new Object();
    private List<Integer> mCommonFrequencys = new ArrayList();
    /* access modifiers changed from: private */
    public boolean mConncetedBackGround = false;
    /* access modifiers changed from: private */
    public boolean mConncetedUseSpecifiedChannels = false;
    /* access modifiers changed from: private */
    public ContentResolver mContentResolver = null;
    private Context mContext;
    private String mCurrentBSSID = null;
    /* access modifiers changed from: private */
    public int mCurrentCellId = -1;
    /* access modifiers changed from: private */
    public int mCurrentFrequency;
    private int mCurrentPriority = -1;
    private String mCurrentSSID = null;
    /* access modifiers changed from: private */
    public WifiConfiguration mCurrentWifiConfig;
    /* access modifiers changed from: private */
    public WifiScanGenieDataBaseImpl mDataBaseImpl;
    private IntentFilter mIntentFilter;
    /* access modifiers changed from: private */
    public Object mNetworkInfoLock = new Object();
    /* access modifiers changed from: private */
    public int mPunishedCellId = -1;
    /* access modifiers changed from: private */
    public List<Integer> mRecentConnectedChannels = new ArrayList();
    /* access modifiers changed from: private */
    public TelephonyManager mTelephonyManager;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    /* access modifiers changed from: private */
    public Handler mWifiScanGenieHandler;

    private class CellIdChangedListener extends PhoneStateListener {
        private CellIdChangedListener() {
        }

        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            synchronized (WifiScanGenieController.this.mNetworkInfoLock) {
                int tmpId = WifiProCommonUtils.getCurrentCellId();
                if (!(tmpId == -1 || WifiScanGenieController.this.mCurrentCellId == tmpId)) {
                    Log.d(WifiScanGenieController.TAG, "mCurrentCellId has Changed, try update = " + tmpId);
                    int unused = WifiScanGenieController.this.mCurrentCellId = tmpId;
                    if (WifiScanGenieController.this.mCurrentWifiConfig != null) {
                        WifiScanGenieController.this.handleWiFiConnected(WifiScanGenieController.this.mCurrentWifiConfig, true);
                    }
                }
            }
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            synchronized (WifiScanGenieController.this.mNetworkInfoLock) {
                if (serviceState.getState() != 0) {
                    int unused = WifiScanGenieController.this.mCurrentCellId = -1;
                }
            }
        }
    }

    public static synchronized WifiScanGenieController createWifiScanGenieControllerImpl(Context context) {
        WifiScanGenieController wifiScanGenieController;
        synchronized (WifiScanGenieController.class) {
            if (mWifiScanGenieController == null) {
                mWifiScanGenieController = new WifiScanGenieController(context);
            }
            wifiScanGenieController = mWifiScanGenieController;
        }
        return wifiScanGenieController;
    }

    private WifiScanGenieController(Context context) {
        initController(context);
    }

    private void initController(Context context) {
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        this.mDataBaseImpl = new WifiScanGenieDataBaseImpl(context);
        this.mDataBaseImpl.openDB();
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        initWifiScanGenieControllerHandler();
        registerBroadcastReceiver();
        synchronized (this.m5GCapabilityLock) {
            this.m5GFreqCapability = Settings.Secure.getInt(this.mContentResolver, DEV_5G_CAPABILITY_FALG, 100);
            if (this.m5GFreqCapability == 101 && this.mWifiScanGenieHandler != null) {
                this.mWifiScanGenieHandler.sendMessageDelayed(this.mWifiScanGenieHandler.obtainMessage(7), 1000);
            }
        }
        Log.d(TAG, "WifiScanGenieController init!");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:37:0x005f, code lost:
        r2 = "";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:?, code lost:
        r4 = com.android.server.wifi.wifipro.HwAutoConnectManager.getInstance();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0066, code lost:
        if (r4 == null) goto L_0x0072;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0068, code lost:
        r1 = r4.getCurrentTopUid();
        r2 = r4.getCurrentPackageName();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0078, code lost:
        if (OOB_STARTUP_GUIDE.equals(r2) == false) goto L_0x0083;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x007a, code lost:
        android.util.Log.d(TAG, "getScanfrequencys, OOB_STARTUP_GUIDE matched!");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0082, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x0085, code lost:
        if (r10.mDataBaseImpl == null) goto L_0x00d2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0087, code lost:
        r5 = r10.mDataBaseImpl.queryScanRecordsByCellid(r10.mCurrentCellId);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x008f, code lost:
        if (r5 == null) goto L_0x00cb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0091, code lost:
        r6 = r5.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0099, code lost:
        if (r6.hasNext() == false) goto L_0x00b7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00a7, code lost:
        if (r6.next().getCurrentFrequency() != -100) goto L_0x00b6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00a9, code lost:
        r10.mPunishedCellId = r10.mCurrentCellId;
        android.util.Log.d(TAG, "getScanfrequencys, INVALID_FREQ_PUNISHED");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00b5, code lost:
        return null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x00b7, code lost:
        android.util.Log.d(TAG, "no punished, use the specified channels to scan");
        r6 = fusefrequencys(frequencyDb(r5));
        r10.mConncetedUseSpecifiedChannels = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00ca, code lost:
        return r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00cb, code lost:
        android.util.Log.w(TAG, "queryScanRecordsByCellid is null");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x00d3, code lost:
        return null;
     */
    public List<Integer> getScanfrequencys() {
        synchronized (this.mNetworkInfoLock) {
            this.mCurrentCellId = WifiProCommonUtils.getCurrentCellId();
            if (this.mCurrentCellId != -1) {
                if (this.mCurrentCellId != this.mPunishedCellId) {
                    synchronized (this.m5GCapabilityLock) {
                        if (this.m5GFreqCapability == 100) {
                            Log.d(TAG, "getScanfrequencys, DEV_5G_CAPABILITY_UNKNOWN");
                            this.mWifiScanGenieHandler.sendMessage(this.mWifiScanGenieHandler.obtainMessage(6));
                            return null;
                        } else if (this.m5GFreqCapability == 102) {
                            Log.d(TAG, "getScanfrequencys, DEV_5G_CAPABILITY_UNSUPPORTED");
                            return null;
                        } else if (this.m5GFreqCapability == 101) {
                            Log.d(TAG, "getScanfrequencys, DEV_5G_CAPABILITY_SUPPORTED");
                            if (this.mCommonFrequencys.size() == 0) {
                                return null;
                            }
                        }
                    }
                }
            }
            Log.d(TAG, "getScanfrequencys, INVAILD_ID or unallowed id = " + this.mPunishedCellId);
            return null;
        }
    }

    public void notifyUseFullChannels() {
        synchronized (this.mNetworkInfoLock) {
            if (this.mCurrentWifiConfig == null) {
                this.mConncetedUseSpecifiedChannels = false;
            }
        }
    }

    public void notifyNetworkRoamingCompleted(String bssid) {
        synchronized (this.mNetworkInfoLock) {
            if (bssid != null) {
                try {
                    if (!(this.mCurrentBSSID == null || bssid.equals(this.mCurrentBSSID) || this.mCurrentWifiConfig == null)) {
                        this.mWifiScanGenieHandler.sendMessage(this.mWifiScanGenieHandler.obtainMessage(4, this.mCurrentWifiConfig));
                    }
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public void notifyWifiConnectedBackground() {
        synchronized (this.mNetworkInfoLock) {
            this.mConncetedBackGround = true;
        }
    }

    public List<WifiScanGenieDataBaseImpl.ScanRecord> getWifiScanRecordbyCellid() {
        return this.mDataBaseImpl == null ? null : null;
    }

    public void handleWiFiDisconnected() {
        this.mWifiScanGenieHandler.sendMessage(this.mWifiScanGenieHandler.obtainMessage(8));
    }

    public void handleWiFiConnected(WifiConfiguration currentWifiConfig, boolean cellIdChanged) {
        this.mWifiScanGenieHandler.sendMessage(this.mWifiScanGenieHandler.obtainMessage(2, currentWifiConfig));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:82:0x01b3, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x01cf, code lost:
        return;
     */
    public void handleWifiConnectedMsg(WifiConfiguration currentWifiConfig, boolean cellIdChanged) {
        synchronized (this.mNetworkInfoLock) {
            Log.d(TAG, "handleWifiConnectedMsg, cellIdChanged:" + cellIdChanged);
            if (this.mDataBaseImpl != null) {
                if (currentWifiConfig != null) {
                    if (HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.mContext)) {
                        Log.d(TAG, "handleWifiConnectedMsg, this is mobile ap,ignore it");
                        return;
                    }
                    if (this.mCurrentCellId == -1) {
                        this.mCurrentCellId = WifiProCommonUtils.getCurrentCellId();
                        Log.d(TAG, "handleWifiConnectedMsg, id  = " + this.mCurrentCellId);
                    }
                    if (this.mCurrentCellId != -1) {
                        if (this.mCurrentCellId != this.mPunishedCellId) {
                            this.mCurrentWifiConfig = currentWifiConfig;
                            if (this.mCurrentWifiConfig != null) {
                                this.mCurrentPriority = this.mCurrentWifiConfig.priority;
                                Log.d(TAG, "wifi connect,mCurrentPriority: " + this.mCurrentPriority);
                            }
                            if (!cellIdChanged) {
                                getCurrentAPInfo();
                            }
                            List<WifiScanGenieDataBaseImpl.ScanRecord> scanRecords = this.mDataBaseImpl.queryScanRecordsByCellid(this.mCurrentCellId);
                            if (scanRecords != null) {
                                for (WifiScanGenieDataBaseImpl.ScanRecord scanRecord : scanRecords) {
                                    if (scanRecord.getCurrentFrequency() == -100) {
                                        this.mPunishedCellId = this.mCurrentCellId;
                                        Log.d(TAG, "handleWifiConnectedMsg, INVALID_FREQ_PUNISHED");
                                        return;
                                    }
                                }
                            }
                            List<WifiScanGenieDataBaseImpl.ScanRecord> scanRecordList = this.mDataBaseImpl.queryScanRecordsByBssid(this.mCurrentBSSID);
                            if (scanRecordList == null || scanRecordList.size() <= 0) {
                                addNewRecord();
                            } else {
                                boolean isNewChannel = true;
                                if (scanRecordList.get(0).getCurrentFrequency() == this.mCurrentFrequency) {
                                    isNewChannel = false;
                                }
                                boolean isNewCellId = true;
                                if (this.mCurrentCellId != -1) {
                                    for (WifiScanGenieDataBaseImpl.ScanRecord record : scanRecordList) {
                                        if (record.getCellid() == this.mCurrentCellId) {
                                            isNewCellId = false;
                                        }
                                    }
                                }
                                if (isNewChannel && !cellIdChanged) {
                                    Log.d(TAG, "wifi connect, is isNewChannel ,mCurrentFrequency: " + this.mCurrentFrequency);
                                    this.mDataBaseImpl.updateBssidChannelRecord(this.mCurrentBSSID, this.mCurrentSSID, this.mCurrentFrequency, this.mCurrentPriority);
                                }
                                if (isNewCellId && this.mCurrentCellId != -1) {
                                    Log.d(TAG, "wifi connect, is isNewCellId");
                                    addNewRecord();
                                }
                                if (!isNewChannel && !isNewCellId && !cellIdChanged) {
                                    Log.d(TAG, "wifi connect, only update ssid , priority ,use time ");
                                    this.mDataBaseImpl.updateBssidPriorityRecord(this.mCurrentSSID, this.mCurrentPriority);
                                }
                            }
                            if (ScanResult.is24GHz(this.mCurrentFrequency) && this.mCurrentBSSID != null) {
                                List<ScanResult> scanResults = WifiproUtils.getScanResultsFromWsm();
                                if (scanResults != null) {
                                    for (ScanResult scanResult : scanResults) {
                                        if (this.mCurrentBSSID.equals(scanResult.BSSID) && scanResult.dot11vNetwork) {
                                            Log.d(TAG, "handleWifiConnectedMsg, ssid = " + scanResult.SSID + ", freq = " + scanResult.frequency + ", 11v = " + scanResult.dot11vNetwork);
                                            this.m11vSupported = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Log.d(TAG, "handleWifiConnectedMsg, INVAILD_ID, id  = " + this.mCurrentCellId);
                }
            }
        }
    }

    private boolean belongCommonAndSavedChannels(int freq) {
        if (isCommonChannel(freq)) {
            return true;
        }
        synchronized (this.mChannelsLock) {
            for (int k = 0; k < this.mRecentConnectedChannels.size(); k++) {
                int savedFreq = this.mRecentConnectedChannels.get(k).intValue();
                if (freq == savedFreq && savedFreq != -1) {
                    return true;
                }
            }
            return false;
        }
    }

    /* access modifiers changed from: private */
    public void handleScanResultsAvailable(List<ScanResult> scanResults) {
        if (this.mCurrentBSSID != null && this.mCurrentWifiConfig != null && this.mCurrentSSID != null && !this.mConncetedBackGround && scanResults != null) {
            boolean currentCellIdPunished = false;
            List<Integer> all5gFreqs = new ArrayList<>();
            int j = 0;
            for (int i = 0; i < scanResults.size(); i++) {
                ScanResult nextResult = scanResults.get(i);
                String scanSsid = "\"" + nextResult.SSID + "\"";
                String scanResultEncrypt = nextResult.capabilities;
                boolean sameBssid = this.mCurrentBSSID.equals(nextResult.BSSID);
                boolean sameConfigKey = this.mCurrentSSID.equals(scanSsid) && WifiProCommonUtils.isSameEncryptType(scanResultEncrypt, this.mCurrentWifiConfig.configKey());
                if (!sameBssid && sameConfigKey && nextResult.is5GHz()) {
                    all5gFreqs.add(Integer.valueOf(nextResult.frequency));
                }
            }
            while (true) {
                if (j >= all5gFreqs.size()) {
                    break;
                } else if (!belongCommonAndSavedChannels(all5gFreqs.get(j).intValue())) {
                    currentCellIdPunished = true;
                    break;
                } else {
                    j++;
                }
            }
            if (currentCellIdPunished) {
                Log.d(TAG, "handleScanResultsAvailable, currentCellIdPunished = " + this.mCurrentCellId);
                this.mPunishedCellId = this.mCurrentCellId;
                this.mDataBaseImpl.deleteCellIdRecord(this.mCurrentCellId);
                this.mDataBaseImpl.addNewChannelRecord("", "", -100, this.mCurrentCellId, -1);
            }
        }
    }

    private void addNewRecord() {
        Log.d(TAG, "WifiScanGenie addNewRecord");
        int count = this.mDataBaseImpl.queryTableSize(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_NAME);
        if (count > 2000) {
            this.mDataBaseImpl.deleteLastRecords(WifiScanGenieDataBaseImpl.CHANNEL_TABLE_NAME);
        }
        Log.d(TAG, "add new Record,mCurrentSSID : " + this.mCurrentSSID + ", mCurrentFrequency :" + this.mCurrentFrequency + ", count :" + count);
        this.mDataBaseImpl.addNewChannelRecord(this.mCurrentBSSID, this.mCurrentSSID, this.mCurrentFrequency, this.mCurrentCellId, this.mCurrentPriority);
    }

    private List<Integer> frequencyDb(List<WifiScanGenieDataBaseImpl.ScanRecord> scanRecords) {
        if (scanRecords == null) {
            return null;
        }
        List<Integer> frequencys = new ArrayList<>();
        for (WifiScanGenieDataBaseImpl.ScanRecord scanRecord : scanRecords) {
            if (isValidChannel(scanRecord.getCurrentFrequency()) && !isCommonChannel(scanRecord.getCurrentFrequency())) {
                frequencys.add(Integer.valueOf(scanRecord.getCurrentFrequency()));
                synchronized (this.mChannelsLock) {
                    this.mRecentConnectedChannels.add(Integer.valueOf(scanRecord.getCurrentFrequency()));
                }
                if (frequencys.size() == 4) {
                    return frequencys;
                }
            }
        }
        return frequencys;
    }

    private List<Integer> fusefrequencys(List<Integer> frequencyDb) {
        List<Integer> fusefrequencyList;
        Log.d(TAG, "start fusefrequencys");
        synchronized (this.m5GCapabilityLock) {
            List<Integer> temp = new ArrayList<>(frequencyDb);
            temp.retainAll(this.mCommonFrequencys);
            frequencyDb.removeAll(temp);
            fusefrequencyList = new ArrayList<>();
            fusefrequencyList.addAll(frequencyDb);
            fusefrequencyList.addAll(this.mCommonFrequencys);
        }
        return fusefrequencyList;
    }

    private void getCurrentAPInfo() {
        WifiInfo conInfo = this.mWifiManager.getConnectionInfo();
        if (conInfo == null || conInfo.getSupplicantState() != SupplicantState.COMPLETED) {
            reSetCurrentAPInfo();
            return;
        }
        this.mCurrentBSSID = conInfo.getBSSID();
        this.mCurrentSSID = conInfo.getSSID();
        this.mCurrentFrequency = conInfo.getFrequency();
    }

    /* access modifiers changed from: private */
    public void reSetCurrentAPInfo() {
        Log.w(TAG, "reSetCurrentAPInfo !");
        this.mCurrentBSSID = null;
        this.mCurrentSSID = null;
        this.mCurrentFrequency = -1;
        this.mCurrentPriority = -1;
        this.mCurrentWifiConfig = null;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x008c, code lost:
        return;
     */
    public void parseAvailablePreferredChannels(String strAvailable5GChannels) {
        synchronized (this.m5GCapabilityLock) {
            if (!TextUtils.isEmpty(strAvailable5GChannels)) {
                List<String> available5GChannels = Arrays.asList(strAvailable5GChannels.replace("[", "").replace("]", "").split(", "));
                if (available5GChannels != null) {
                    int AVAI_5G_SIZE = available5GChannels.size();
                    for (int i = 0; i < COMMON_CHANNELS.length; i++) {
                        boolean curr5gAvailable = false;
                        if (ScanResult.is5GHz(COMMON_CHANNELS[i])) {
                            int j = 0;
                            while (true) {
                                if (j >= AVAI_5G_SIZE) {
                                    break;
                                }
                                if (!TextUtils.isEmpty(available5GChannels.get(j))) {
                                    int available5GFreq = -1;
                                    try {
                                        available5GFreq = Integer.parseInt(available5GChannels.get(j));
                                    } catch (NumberFormatException e) {
                                        Log.d(TAG, "NumberFormatException:");
                                    }
                                    if (COMMON_CHANNELS[i] == available5GFreq) {
                                        curr5gAvailable = true;
                                        break;
                                    }
                                }
                                j++;
                            }
                        }
                        if (ScanResult.is24GHz(COMMON_CHANNELS[i]) || curr5gAvailable) {
                            this.mCommonFrequencys.add(Integer.valueOf(COMMON_CHANNELS[i]));
                        }
                    }
                }
            }
        }
    }

    private void initWifiScanGenieControllerHandler() {
        HandlerThread handlerThread = new HandlerThread("WifiScanGenie_handler_thread");
        handlerThread.start();
        this.mWifiScanGenieHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Intent confg_intent = (Intent) msg.obj;
                        WifiConfiguration conn_cfg = (WifiConfiguration) confg_intent.getParcelableExtra("wifiConfiguration");
                        if (conn_cfg != null && !conn_cfg.isTempCreated && confg_intent.getIntExtra("changeReason", -1) == 1) {
                            Log.d(WifiScanGenieController.TAG, "user forget " + conn_cfg.SSID);
                            WifiScanGenieController.this.mDataBaseImpl.deleteSsidRecord(conn_cfg.SSID);
                            break;
                        }
                    case 2:
                    case 4:
                        WifiScanGenieController.this.handleWifiConnectedMsg((WifiConfiguration) msg.obj, false);
                        break;
                    case 3:
                        boolean needParseScanResults = false;
                        synchronized (WifiScanGenieController.this.mNetworkInfoLock) {
                            if (WifiScanGenieController.this.mConncetedUseSpecifiedChannels && !WifiScanGenieController.this.m11vSupported && WifiScanGenieController.this.mCurrentFrequency != -1 && WifiScanGenieController.this.mCurrentCellId != WifiScanGenieController.this.mPunishedCellId && WifiScanGenieController.this.mCurrentCellId != -1 && ScanResult.is24GHz(WifiScanGenieController.this.mCurrentFrequency)) {
                                needParseScanResults = true;
                            }
                        }
                        if (needParseScanResults) {
                            List<ScanResult> scanResults = WifiScanGenieController.this.mWifiManager.getScanResults();
                            synchronized (WifiScanGenieController.this.mNetworkInfoLock) {
                                WifiScanGenieController.this.handleScanResultsAvailable(scanResults);
                            }
                            break;
                        }
                        break;
                    case 5:
                        Log.d(WifiScanGenieController.TAG, "MSG_BOOT_COMPLETED");
                        if (WifiScanGenieController.this.mCellIdChangedListener == null) {
                            CellIdChangedListener unused = WifiScanGenieController.this.mCellIdChangedListener = new CellIdChangedListener();
                            WifiScanGenieController.this.mTelephonyManager.listen(WifiScanGenieController.this.mCellIdChangedListener, 16);
                            break;
                        }
                        break;
                    case 6:
                        Log.d(WifiScanGenieController.TAG, "###MSG_5G_CAPABILITY_QUERY");
                        boolean is5GHzBandSupported = WifiScanGenieController.this.mWifiManager.is5GHzBandSupported();
                        synchronized (WifiScanGenieController.this.m5GCapabilityLock) {
                            Log.d(WifiScanGenieController.TAG, "is5GHzBandSupported = " + is5GHzBandSupported);
                            int i = 102;
                            int unused2 = WifiScanGenieController.this.m5GFreqCapability = is5GHzBandSupported ? 101 : 102;
                            ContentResolver access$1800 = WifiScanGenieController.this.mContentResolver;
                            if (is5GHzBandSupported) {
                                i = 101;
                            }
                            Settings.Secure.putInt(access$1800, WifiScanGenieController.DEV_5G_CAPABILITY_FALG, i);
                        }
                        if (is5GHzBandSupported) {
                            List<Integer> availableChannels = WifiInjector.getInstance().getWifiScanner().getAvailableChannels(2);
                            if (availableChannels != null && !availableChannels.isEmpty()) {
                                Settings.Secure.putString(WifiScanGenieController.this.mContentResolver, WifiScanGenieController.DEV_5G_AVAILABLE_FREQ_FALG, availableChannels.toString());
                                WifiScanGenieController.this.parseAvailablePreferredChannels(availableChannels.toString());
                                break;
                            }
                        }
                        break;
                    case 7:
                        Log.d(WifiScanGenieController.TAG, "###MSG_INIT_PREFERRED_CHANNELS");
                        WifiScanGenieController.this.parseAvailablePreferredChannels(Settings.Secure.getString(WifiScanGenieController.this.mContentResolver, WifiScanGenieController.DEV_5G_AVAILABLE_FREQ_FALG));
                        break;
                    case 8:
                        Log.d(WifiScanGenieController.TAG, "WifiScanGenie handleWiFiDisconnected");
                        if (WifiScanGenieController.this.mDataBaseImpl != null) {
                            synchronized (WifiScanGenieController.this.mNetworkInfoLock) {
                                WifiScanGenieController.this.reSetCurrentAPInfo();
                                boolean unused3 = WifiScanGenieController.this.mConncetedUseSpecifiedChannels = false;
                                boolean unused4 = WifiScanGenieController.this.mConncetedBackGround = false;
                                boolean unused5 = WifiScanGenieController.this.m11vSupported = false;
                                synchronized (WifiScanGenieController.this.mChannelsLock) {
                                    WifiScanGenieController.this.mRecentConnectedChannels.clear();
                                }
                                break;
                            }
                        } else {
                            return;
                        }
                }
            }
        };
    }

    private void registerBroadcastReceiver() {
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(action)) {
                    WifiScanGenieController.this.mWifiScanGenieHandler.sendMessage(WifiScanGenieController.this.mWifiScanGenieHandler.obtainMessage(1, intent));
                } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    WifiScanGenieController.this.mWifiScanGenieHandler.sendMessage(WifiScanGenieController.this.mWifiScanGenieHandler.obtainMessage(5));
                } else if ("android.net.wifi.SCAN_RESULTS".equals(action) && intent.getBooleanExtra("resultsUpdated", false)) {
                    WifiScanGenieController.this.mWifiScanGenieHandler.sendMessage(WifiScanGenieController.this.mWifiScanGenieHandler.obtainMessage(3));
                }
            }
        };
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        this.mIntentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mIntentFilter.addAction("android.net.wifi.SCAN_RESULTS");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
    }

    private boolean isValidChannel(int frequency) {
        return frequency > 0;
    }

    private boolean isCommonChannel(int frequency) {
        for (int i : COMMON_CHANNELS) {
            if (i == frequency) {
                return true;
            }
        }
        return false;
    }
}
