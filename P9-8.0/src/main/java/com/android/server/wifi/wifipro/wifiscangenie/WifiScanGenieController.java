package com.android.server.wifi.wifipro.wifiscangenie;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import com.android.server.wifi.wifipro.wifiscangenie.WifiScanGenieDataBaseImpl.ScanRecord;
import java.util.ArrayList;
import java.util.List;

public class WifiScanGenieController {
    public static final int CHANNEL_11_FREQ = 2462;
    public static final int CHANNEL_1_FREQ = 2412;
    public static final int CHANNEL_6_FREQ = 2437;
    private static final int INVAILD_ID = -1;
    public static final int MSG_CONFIGURED_CHANGED = 1;
    public static final int MSG_NETWORK_STATE_CHANGED = 2;
    private static final boolean SCANGENIE_ENABLE = true;
    public static final int Scan = -1;
    private static final String TAG = "WifiScanGenie_Controller";
    private static WifiScanGenieController mWifiScanGenieController;
    private BroadcastReceiver mBroadcastReceiver;
    private CellIdChangedListener mCellIdChangedListener;
    private List<Integer> mCommonFrequencys;
    private Context mContext;
    private String mCurrentBSSID;
    private int mCurrentCellId;
    private int mCurrentFrequency;
    private int mCurrentPriority;
    private String mCurrentSSID;
    private WifiConfiguration mCurrentWifiConfig;
    private WifiScanGenieDataBaseImpl mDataBaseImpl;
    private IntentFilter mIntentFilter;
    private boolean mIsWiFiConnceted;
    private TelephonyManager mTelephonyManager;
    private WifiManager mWifiManager;
    private Handler mWifiScanGenieHandler;

    private class CellIdChangedListener extends PhoneStateListener {
        /* synthetic */ CellIdChangedListener(WifiScanGenieController this$0, CellIdChangedListener -this1) {
            this();
        }

        private CellIdChangedListener() {
        }

        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);
            if (WifiScanGenieController.this.mTelephonyManager != null) {
                Log.d(WifiScanGenieController.TAG, "cellid has changed");
                switch (WifiScanGenieController.this.mTelephonyManager.getCurrentPhoneType()) {
                    case 1:
                        if (location instanceof GsmCellLocation) {
                            try {
                                WifiScanGenieController.this.mCurrentCellId = ((GsmCellLocation) location).getCid();
                                break;
                            } catch (Exception e) {
                                Log.e(WifiScanGenieController.TAG, "GsmCellLocation Type Cast Exception :" + e.getMessage());
                                break;
                            }
                        }
                        break;
                    case 2:
                        if (location instanceof CdmaCellLocation) {
                            try {
                                WifiScanGenieController.this.mCurrentCellId = ((CdmaCellLocation) location).getBaseStationId();
                                break;
                            } catch (Exception e2) {
                                Log.e(WifiScanGenieController.TAG, "CdmaCellLocation Type Cast Exception :" + e2.getMessage());
                                break;
                            }
                        }
                        break;
                    default:
                        WifiScanGenieController.this.mCurrentCellId = -1;
                        break;
                }
                if (WifiScanGenieController.this.mIsWiFiConnceted && WifiScanGenieController.this.mCurrentWifiConfig != null) {
                    Log.d(WifiScanGenieController.TAG, "mCurrentCellId has Changed,try update");
                    WifiScanGenieController.this.handleWiFiConnected(WifiScanGenieController.this.mCurrentWifiConfig, true);
                }
            }
        }

        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            if (serviceState.getState() != 0) {
                WifiScanGenieController.this.mCurrentCellId = -1;
            }
        }
    }

    public static WifiScanGenieController createWifiScanGenieControllerImpl(Context context) {
        if (mWifiScanGenieController == null) {
            mWifiScanGenieController = new WifiScanGenieController(context);
        }
        return mWifiScanGenieController;
    }

    private WifiScanGenieController(Context context) {
        initController(context);
    }

    private void initController(Context context) {
        this.mContext = context;
        this.mDataBaseImpl = new WifiScanGenieDataBaseImpl(context);
        this.mDataBaseImpl.openDB();
        this.mWifiManager = (WifiManager) context.getSystemService("wifi");
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mCommonFrequencys = new ArrayList();
        this.mCommonFrequencys.add(Integer.valueOf(CHANNEL_1_FREQ));
        this.mCommonFrequencys.add(Integer.valueOf(CHANNEL_6_FREQ));
        this.mCommonFrequencys.add(Integer.valueOf(CHANNEL_11_FREQ));
        initWifiScanGenieControllerHandler();
        registerBroadcastReceiver();
        Log.d(TAG, "WifiScanGenieController init!");
    }

    public void handWiFiScanGenieScan(int type) {
    }

    public List<Integer> getScanfrequencys() {
        if (this.mDataBaseImpl != null) {
            Log.d(TAG, "queryScanRecordsByCellid");
            List<ScanRecord> scanRecords = this.mDataBaseImpl.queryScanRecordsByCellid(this.mCurrentCellId);
            if (scanRecords != null) {
                Log.d(TAG, "queryScanRecordsByCellid scanRecords");
                return fusefrequencys(frequencyDb(scanRecords));
            }
            Log.w(TAG, "queryScanRecordsByCellid is null");
        }
        return null;
    }

    public List<ScanRecord> getWifiScanRecordbyCellid() {
        return this.mDataBaseImpl == null ? null : null;
    }

    public void handleWiFiDisconnected() {
        Log.d(TAG, "WifiScanGenie handleWiFiDisconnected");
        if (this.mDataBaseImpl != null) {
            reSetCurrentAPInfo();
        }
    }

    public void handleWiFiConnected(WifiConfiguration currentWifiConfig, boolean cellIdChanged) {
        Log.d(TAG, "WifiScanGenie handleWiFiConnected, cellIdChanged:" + cellIdChanged);
        if (this.mDataBaseImpl != null) {
            if (HwFrameworkFactory.getHwInnerWifiManager().getHwMeteredHint(this.mContext)) {
                Log.d(TAG, "this is mobile ap,ignor it");
            }
            this.mCurrentWifiConfig = currentWifiConfig;
            if (this.mCurrentWifiConfig != null) {
                this.mCurrentPriority = this.mCurrentWifiConfig.priority;
                Log.d(TAG, "wifi connect,mCurrentPriority: " + this.mCurrentPriority);
            }
            if (!cellIdChanged) {
                getCurrentAPInfo();
            }
            List<ScanRecord> scanRecordList = this.mDataBaseImpl.queryScanRecordsByBssid(this.mCurrentBSSID);
            if (scanRecordList == null || scanRecordList.size() <= 0) {
                addNewRecord();
            } else {
                boolean isNewChannel = true;
                if (((ScanRecord) scanRecordList.get(0)).getCurrentFrequency() == this.mCurrentFrequency) {
                    isNewChannel = false;
                }
                boolean isNewCellId = true;
                if (this.mCurrentCellId != -1) {
                    for (ScanRecord record : scanRecordList) {
                        if (record.getCellid() == this.mCurrentCellId) {
                            isNewCellId = false;
                        }
                    }
                }
                if (isNewChannel && (cellIdChanged ^ 1) != 0) {
                    Log.d(TAG, "wifi connect, is isNewChannel ,mCurrentFrequency: " + this.mCurrentFrequency);
                    this.mDataBaseImpl.updateBssidChannelRecord(this.mCurrentBSSID, this.mCurrentSSID, this.mCurrentFrequency, this.mCurrentPriority);
                }
                if (isNewCellId && this.mCurrentCellId != -1) {
                    Log.d(TAG, "wifi connect, is isNewCellId");
                    addNewRecord();
                }
                if (!(isNewChannel || (isNewCellId ^ 1) == 0 || (cellIdChanged ^ 1) == 0)) {
                    Log.d(TAG, "wifi connect, only update ssid , priority ,use time ");
                    this.mDataBaseImpl.updateBssidPriorityRecord(this.mCurrentSSID, this.mCurrentPriority);
                }
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

    private List<Integer> frequencyDb(List<ScanRecord> scanRecords) {
        if (scanRecords == null) {
            return null;
        }
        List<Integer> frequencys = new ArrayList();
        for (ScanRecord scanRecord : scanRecords) {
            if (isValidChannel(scanRecord.getCurrentFrequency()) && (isCommonChannel(scanRecord.getCurrentFrequency()) ^ 1) != 0) {
                frequencys.add(Integer.valueOf(scanRecord.getCurrentFrequency()));
                if (frequencys.size() == 2) {
                    return frequencys;
                }
            }
        }
        return frequencys;
    }

    private List<Integer> fusefrequencys(List<Integer> frequencyDb) {
        Log.d(TAG, "start fusefrequencys");
        List<Integer> temp = new ArrayList(frequencyDb);
        temp.retainAll(this.mCommonFrequencys);
        frequencyDb.removeAll(temp);
        List<Integer> fusefrequencyList = new ArrayList();
        fusefrequencyList.addAll(frequencyDb);
        fusefrequencyList.addAll(this.mCommonFrequencys);
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

    private void reSetCurrentAPInfo() {
        Log.w(TAG, "reSetCurrentAPInfo !");
        this.mCurrentBSSID = null;
        this.mCurrentSSID = null;
        this.mCurrentWifiConfig = null;
        this.mCurrentFrequency = -1;
        this.mCurrentPriority = -1;
        this.mCurrentWifiConfig = null;
    }

    private void initWifiScanGenieControllerHandler() {
        HandlerThread handlerThread = new HandlerThread("wifipro_sample_handler_thread");
        handlerThread.start();
        this.mWifiScanGenieHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        Intent confg_intent = msg.obj;
                        WifiConfiguration conn_cfg = (WifiConfiguration) confg_intent.getParcelableExtra("wifiConfiguration");
                        if (conn_cfg != null && (conn_cfg.isTempCreated ^ 1) != 0 && confg_intent.getIntExtra("changeReason", -1) == 1) {
                            Log.d(WifiScanGenieController.TAG, "user forget " + conn_cfg.SSID);
                            WifiScanGenieController.this.mDataBaseImpl.deleteSsidRecord(conn_cfg.SSID);
                            return;
                        }
                        return;
                    case 2:
                        NetworkInfo info = (NetworkInfo) msg.obj.getParcelableExtra("networkInfo");
                        if (info == null || !info.isConnected()) {
                            WifiScanGenieController.this.mIsWiFiConnceted = false;
                            return;
                        }
                        Log.d(WifiScanGenieController.TAG, "wifi has connected");
                        WifiScanGenieController.this.mIsWiFiConnceted = true;
                        return;
                    default:
                        return;
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
                } else if ("android.net.wifi.STATE_CHANGE".equals(action)) {
                    WifiScanGenieController.this.mWifiScanGenieHandler.sendMessage(WifiScanGenieController.this.mWifiScanGenieHandler.obtainMessage(2, intent));
                } else if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
                    Log.d(WifiScanGenieController.TAG, "**receive ACTION_BOOT_COMPLETED concurrently********");
                    if (WifiScanGenieController.this.mCellIdChangedListener == null) {
                        WifiScanGenieController.this.mCellIdChangedListener = new CellIdChangedListener(WifiScanGenieController.this, null);
                        WifiScanGenieController.this.mTelephonyManager.listen(WifiScanGenieController.this.mCellIdChangedListener, 16);
                    }
                }
            }
        };
        this.mIntentFilter = new IntentFilter();
        this.mIntentFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
        this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
        this.mIntentFilter.addAction("android.intent.action.BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mBroadcastReceiver, this.mIntentFilter);
    }

    private boolean isValidChannel(int frequency) {
        return frequency > 0;
    }

    private boolean isCommonChannel(int frequency) {
        return frequency == CHANNEL_1_FREQ || frequency == CHANNEL_6_FREQ || frequency == CHANNEL_11_FREQ;
    }
}
