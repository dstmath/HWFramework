package com.android.server.wifi;

import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import java.text.DateFormat;
import java.util.Date;

public class HwWifiRoamingEngine extends StateMachine {
    public static final String ACTION_11v_ROAMING_NETWORK_FOUND = "com.huawei.wifi.action.11v_ROAMING_NETWORK_FOUND";
    private static final int CMD_11v_ROAMING_TIMEOUT = 108;
    private static final int CMD_DISCONNECT_POOR_LINK = 105;
    private static final int CMD_NETWORK_CONNECTED_RCVD = 101;
    private static final int CMD_NETWORK_DISCONNECTED_RCVD = 102;
    private static final int CMD_NEW_RSSI_RCVD = 104;
    private static final int CMD_QUERY_11v_ROAMING_NETWORK = 103;
    private static final int CMD_REQUEST_ROAMING_NETWORK = 109;
    private static final int CMD_ROAMING_COMPLETED_RCVD = 107;
    private static final int CMD_ROAMING_STARTED_RCVD = 106;
    private static final int[] DELAYED_MS_TABLE = null;
    private static final int POOR_LINK_MONITOR_MS = 6000;
    private static final int QUERY_11v_ROAMING_NETWORK_DELAYED_MS = 5000;
    private static final int QUERY_REASON_LOW_RSSI = 16;
    private static final int QUERY_REASON_PREFERRED_BSS = 19;
    private static final int ROAMING_11v_NETWORK_TIMEOUT_MS = 8000;
    private static final String ROAMING_ENGINE_PROP = "ro.config.hw_RoaEngine_enable";
    private static final int SIGNAL_LEVEL_0 = 0;
    private static final int SIGNAL_LEVEL_2 = 2;
    private static final int SIGNAL_LEVEL_4 = 4;
    private static final String TAG = "HwWifiRoamingEngine";
    private static HwWifiRoamingEngine mHwWifiRoamingEngine;
    private State mConnectedMonitorState;
    private Context mContext;
    private State mDefaultState;
    private State mDisconnectedMonitorState;
    private boolean mInitialized;
    private WifiManager mWifiManager;
    private WifiNative mWifiNative;
    private WifiStateMachine mWifiStateMachine;

    class ConnectedMonitorState extends State {
        private int m11vRoamingFailedCounter;
        private boolean m11vRoamingOnGoing;
        private long mLast11vRoamingFailedTs;
        private int mLastSignalLevel;
        private boolean mRoamingOnGoing;

        ConnectedMonitorState() {
        }

        public void enter() {
            this.mRoamingOnGoing = false;
            this.m11vRoamingOnGoing = false;
            this.m11vRoamingFailedCounter = HwWifiRoamingEngine.SIGNAL_LEVEL_0;
            this.mLast11vRoamingFailedTs = 0;
            WifiInfo wifiInfo = HwWifiRoamingEngine.this.mWifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                this.mLastSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(wifiInfo.getRssi());
                HwWifiRoamingEngine.this.LOGD("ConnectedMonitorState, network = " + wifiInfo.getSSID() + ", 802.11v = " + is11vNetworkConnected() + ", 2.4GHz = " + wifiInfo.is24GHz() + ", current level = " + this.mLastSignalLevel);
                if (!is11vNetworkConnected()) {
                    return;
                }
                if (wifiInfo.is24GHz() || this.mLastSignalLevel <= HwWifiRoamingEngine.SIGNAL_LEVEL_2) {
                    HwWifiRoamingEngine.this.sendMessageDelayed(HwWifiRoamingEngine.CMD_QUERY_11v_ROAMING_NETWORK, 5000);
                }
            }
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwWifiRoamingEngine.CMD_NETWORK_DISCONNECTED_RCVD /*102*/:
                    if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_QUERY_11v_ROAMING_NETWORK)) {
                        HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_QUERY_11v_ROAMING_NETWORK);
                    }
                    if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT)) {
                        HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT);
                    }
                    if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK)) {
                        HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK);
                    }
                    HwWifiRoamingEngine.this.transitionTo(HwWifiRoamingEngine.this.mDisconnectedMonitorState);
                    break;
                case HwWifiRoamingEngine.CMD_QUERY_11v_ROAMING_NETWORK /*103*/:
                    query11vRoamingNetowrk(HwWifiRoamingEngine.QUERY_REASON_LOW_RSSI);
                    break;
                case HwWifiRoamingEngine.CMD_NEW_RSSI_RCVD /*104*/:
                    handleNewRssiRcvd(message.arg1);
                    break;
                case HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK /*105*/:
                    break;
                case HwWifiRoamingEngine.CMD_ROAMING_STARTED_RCVD /*106*/:
                    if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK)) {
                        HwWifiRoamingEngine.this.LOGD("CMD_DISCONNECT_POOR_LINK remove due to roaming received.");
                        HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK);
                    }
                    this.mRoamingOnGoing = true;
                    break;
                case HwWifiRoamingEngine.CMD_ROAMING_COMPLETED_RCVD /*107*/:
                    if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT)) {
                        HwWifiRoamingEngine.this.LOGD("CMD_11v_ROAMING_TIMEOUT remove due to roaming completed received.");
                        HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT);
                    }
                    this.mRoamingOnGoing = false;
                    this.m11vRoamingOnGoing = false;
                    this.m11vRoamingFailedCounter = HwWifiRoamingEngine.SIGNAL_LEVEL_0;
                    this.mLast11vRoamingFailedTs = 0;
                    break;
                case HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT /*108*/:
                    if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_QUERY_11v_ROAMING_NETWORK)) {
                        HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_QUERY_11v_ROAMING_NETWORK);
                    }
                    this.m11vRoamingOnGoing = false;
                    this.m11vRoamingFailedCounter++;
                    this.mLast11vRoamingFailedTs = System.currentTimeMillis();
                    HwWifiRoamingEngine.this.LOGD("CMD_11v_ROAMING_TIMEOUT received, counter = " + this.m11vRoamingFailedCounter + ", ts = " + DateFormat.getDateTimeInstance().format(new Date(this.mLast11vRoamingFailedTs)));
                    break;
                case HwWifiRoamingEngine.CMD_REQUEST_ROAMING_NETWORK /*109*/:
                    if (is11vNetworkConnected()) {
                        if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_QUERY_11v_ROAMING_NETWORK)) {
                            HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_QUERY_11v_ROAMING_NETWORK);
                        }
                        query11vRoamingNetowrk(HwWifiRoamingEngine.QUERY_REASON_LOW_RSSI);
                        break;
                    }
                    break;
                default:
                    return false;
            }
            return true;
        }

        private void handleNewRssiRcvd(int newRssi) {
            int currentSignalLevel = HwFrameworkFactory.getHwInnerWifiManager().calculateSignalLevelHW(newRssi);
            if (currentSignalLevel >= 0 && currentSignalLevel != this.mLastSignalLevel) {
                HwWifiRoamingEngine.this.LOGD("handleNewRssiRcvd, signal level changed: " + this.mLastSignalLevel + " --> " + currentSignalLevel + ", 802.11v = " + is11vNetworkConnected());
                if (currentSignalLevel == 0 && !HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK)) {
                    HwWifiRoamingEngine.this.sendMessageDelayed(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK, 6000);
                } else if (currentSignalLevel > 0 && HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK)) {
                    HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_DISCONNECT_POOR_LINK);
                }
                if (is11vNetworkConnected() && !this.m11vRoamingOnGoing && currentSignalLevel <= HwWifiRoamingEngine.SIGNAL_LEVEL_2) {
                    if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_QUERY_11v_ROAMING_NETWORK)) {
                        HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_QUERY_11v_ROAMING_NETWORK);
                    }
                    HwWifiRoamingEngine.this.LOGD("to delay " + HwWifiRoamingEngine.DELAYED_MS_TABLE[currentSignalLevel] + " ms to request roaming 802.11v network.");
                    HwWifiRoamingEngine.this.sendMessageDelayed(HwWifiRoamingEngine.CMD_QUERY_11v_ROAMING_NETWORK, (long) HwWifiRoamingEngine.DELAYED_MS_TABLE[currentSignalLevel]);
                }
            }
            this.mLastSignalLevel = currentSignalLevel;
        }

        private void query11vRoamingNetowrk(int reason) {
            HwWifiRoamingEngine.this.LOGD("query11vRoamingNetowrk, mRoamingOnGoing = " + this.mRoamingOnGoing + ", m11vRoamingOnGoing = " + this.m11vRoamingOnGoing);
            if (!this.mRoamingOnGoing && !this.m11vRoamingOnGoing) {
                HwWifiRoamingEngine.this.mWifiNative.query11vRoamingNetwork(reason);
                this.m11vRoamingOnGoing = true;
                if (HwWifiRoamingEngine.this.hasMessages(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT)) {
                    HwWifiRoamingEngine.this.removeMessages(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT);
                }
                HwWifiRoamingEngine.this.sendMessageDelayed(HwWifiRoamingEngine.CMD_11v_ROAMING_TIMEOUT, 8000);
            }
        }

        private boolean is11vNetworkConnected() {
            ScanResult currentScanResult = HwWifiRoamingEngine.this.mWifiStateMachine.getCurrentScanResult();
            if (currentScanResult == null || !currentScanResult.dot11vNetwork) {
                return false;
            }
            return true;
        }
    }

    static class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            return true;
        }
    }

    class DisconnectedMonitorState extends State {
        DisconnectedMonitorState() {
        }

        public void enter() {
            HwWifiRoamingEngine.this.LOGD("InitialState::DisconnectedMonitorState, enter()");
        }

        public boolean processMessage(Message message) {
            switch (message.what) {
                case HwWifiRoamingEngine.CMD_NETWORK_CONNECTED_RCVD /*101*/:
                    HwWifiRoamingEngine.this.transitionTo(HwWifiRoamingEngine.this.mConnectedMonitorState);
                    return true;
                default:
                    return false;
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.wifi.HwWifiRoamingEngine.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.wifi.HwWifiRoamingEngine.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.HwWifiRoamingEngine.<clinit>():void");
    }

    public static synchronized HwWifiRoamingEngine getInstance(Context context, WifiStateMachine wsm) {
        HwWifiRoamingEngine hwWifiRoamingEngine;
        synchronized (HwWifiRoamingEngine.class) {
            if (mHwWifiRoamingEngine == null) {
                mHwWifiRoamingEngine = new HwWifiRoamingEngine(context, wsm);
            }
            hwWifiRoamingEngine = mHwWifiRoamingEngine;
        }
        return hwWifiRoamingEngine;
    }

    public static synchronized HwWifiRoamingEngine getInstance() {
        HwWifiRoamingEngine hwWifiRoamingEngine;
        synchronized (HwWifiRoamingEngine.class) {
            hwWifiRoamingEngine = mHwWifiRoamingEngine;
        }
        return hwWifiRoamingEngine;
    }

    private HwWifiRoamingEngine(Context context, WifiStateMachine wsm) {
        super(TAG);
        this.mInitialized = false;
        this.mDefaultState = new DefaultState();
        this.mConnectedMonitorState = new ConnectedMonitorState();
        this.mDisconnectedMonitorState = new DisconnectedMonitorState();
        this.mContext = context;
        this.mWifiStateMachine = wsm;
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mWifiNative = WifiNative.getWlanNativeInterface();
        addState(this.mDefaultState);
        addState(this.mConnectedMonitorState, this.mDefaultState);
        addState(this.mDisconnectedMonitorState, this.mDefaultState);
        setInitialState(this.mDisconnectedMonitorState);
        start();
    }

    public synchronized void setup() {
        if (!SystemProperties.getBoolean(ROAMING_ENGINE_PROP, false) && !this.mInitialized) {
            this.mInitialized = true;
            LOGD("setup DONE!");
            registerReceivers();
        }
    }

    public void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.RSSI_CHANGED");
        intentFilter.addAction(ACTION_11v_ROAMING_NETWORK_FOUND);
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.net.wifi.STATE_CHANGE".equals(intent.getAction())) {
                    NetworkInfo info = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                    if (info != null && info.getDetailedState() == DetailedState.DISCONNECTED) {
                        HwWifiRoamingEngine.this.sendMessage(HwWifiRoamingEngine.CMD_NETWORK_DISCONNECTED_RCVD);
                    } else if (info != null && info.getDetailedState() == DetailedState.CONNECTED) {
                        HwWifiRoamingEngine.this.sendMessage(HwWifiRoamingEngine.CMD_NETWORK_CONNECTED_RCVD);
                    }
                } else if ("android.net.wifi.RSSI_CHANGED".equals(intent.getAction())) {
                    int newRssi = intent.getIntExtra("newRssi", -127);
                    if (newRssi != -127) {
                        HwWifiRoamingEngine.this.sendMessage(HwWifiRoamingEngine.CMD_NEW_RSSI_RCVD, newRssi, HwWifiRoamingEngine.SIGNAL_LEVEL_0);
                    }
                }
            }
        }, intentFilter);
    }

    public synchronized void notifyWifiRoamingStarted() {
        LOGD("ENTER: notifyWifiRoamingStarted()");
        if (this.mInitialized) {
            sendMessage(CMD_ROAMING_STARTED_RCVD);
        }
    }

    public synchronized void notifyWifiRoamingCompleted() {
        LOGD("ENTER: notifyWifiRoamingCompleted()");
        if (this.mInitialized) {
            sendMessage(CMD_ROAMING_COMPLETED_RCVD);
        }
    }

    public synchronized void requestRoamingByNoInternet() {
        LOGD("ENTER: requestRoamingByNoInternet()");
        if (this.mInitialized) {
            sendMessage(CMD_REQUEST_ROAMING_NETWORK);
        }
    }

    public void LOGD(String msg) {
        Log.d(TAG, msg);
    }
}
