package com.android.server.wifi.MSS;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.os.Message;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.MSS.HwMssArbitrager;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.wificond.NativeMssResult;
import java.util.Collections;
import java.util.List;

public class HisiMssStateMachine extends StateMachine {
    private static final String IFACE = "wlan0";
    private static final int MACHINESTATE_CONN = 5;
    private static final int MACHINESTATE_MIMO = 1;
    private static final int MACHINESTATE_MTOS = 2;
    private static final int MACHINESTATE_SISO = 3;
    private static final int MACHINESTATE_STOM = 4;
    private static final String TAG = "HisiMssStateMachine";
    private static HisiMssStateMachine sMssStateMachine = null;
    private IHwMssBlacklistMgr mBlacklistMgr = null;
    private Context mContext;
    private State mDefaultState = new DefaultState();
    private HwMssHandler mHwMssHandler = null;
    private State mMimoState = new MimoState();
    private State mMimoToSisoState = new MimoToSisoState();
    private int mMssFailureCounter = 0;
    private State mSisoState = new SisoState();
    private State mSisoToMimoState = new SisoToMimoState();
    private State mWiFiConnectedState = new WiFiConnectedState();
    private State mWiFiDisabledState = new WiFiDisabledState();
    private State mWiFiDisconnectedState = new WiFiDisconnectedState();
    private State mWiFiEnabledState = new WiFiEnabledState();
    private WifiInfo mWifiInfo = null;
    private WifiNative mWifiNative = null;

    public static HisiMssStateMachine createHisiMssStateMachine(HwMssHandler hwMSSHandler, Context cxt, WifiNative wifiNative, WifiInfo wifiInfo) {
        HwMssUtils.logD(TAG, false, "createHisiMssStateMachine enter", new Object[0]);
        if (sMssStateMachine == null) {
            sMssStateMachine = new HisiMssStateMachine(hwMSSHandler, cxt, wifiNative, wifiInfo);
        }
        return sMssStateMachine;
    }

    private HisiMssStateMachine(HwMssHandler hwMSSHandler, Context cxt, WifiNative wifiNative, WifiInfo wifiInfo) {
        super(TAG);
        this.mContext = cxt;
        this.mWifiInfo = wifiInfo;
        this.mWifiNative = wifiNative;
        this.mHwMssHandler = hwMSSHandler;
        this.mBlacklistMgr = HisiMssBlackListManager.getInstance(this.mContext);
        addState(this.mDefaultState);
        addState(this.mWiFiEnabledState, this.mDefaultState);
        addState(this.mWiFiDisabledState, this.mDefaultState);
        addState(this.mWiFiConnectedState, this.mWiFiEnabledState);
        addState(this.mMimoState, this.mWiFiConnectedState);
        addState(this.mSisoState, this.mWiFiConnectedState);
        addState(this.mMimoToSisoState, this.mWiFiConnectedState);
        addState(this.mSisoToMimoState, this.mWiFiConnectedState);
        addState(this.mWiFiDisconnectedState, this.mWiFiEnabledState);
        setInitialState(this.mDefaultState);
        start();
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message msg) {
            HwMssUtils.logD(HisiMssStateMachine.TAG, false, "DefaultState processMessage:%{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (i != 4) {
                switch (i) {
                    case 10:
                    case 11:
                        HwMssUtils.logE(HisiMssStateMachine.TAG, false, "DefaultState:unexpect msg:%{public}d", Integer.valueOf(msg.what));
                        break;
                    case 12:
                        HisiMssStateMachine hisiMssStateMachine = HisiMssStateMachine.this;
                        hisiMssStateMachine.transitionTo(hisiMssStateMachine.mWiFiEnabledState);
                        break;
                    case 13:
                        HisiMssStateMachine hisiMssStateMachine2 = HisiMssStateMachine.this;
                        hisiMssStateMachine2.transitionTo(hisiMssStateMachine2.mWiFiDisabledState);
                        break;
                }
            } else {
                HisiMssStateMachine.this.removeMessages(4);
            }
            return true;
        }
    }

    class WiFiEnabledState extends State {
        WiFiEnabledState() {
        }

        public void enter() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "WiFiEnabledState enter", new Object[0]);
            HisiMssStateMachine.this.sendBlacklistToDriver();
        }

        public boolean processMessage(Message msg) {
            HwMssUtils.logD(HisiMssStateMachine.TAG, false, "WiFiEnabledState processMessage:%{public}d", Integer.valueOf(msg.what));
            switch (msg.what) {
                case 10:
                    HisiMssStateMachine hisiMssStateMachine = HisiMssStateMachine.this;
                    hisiMssStateMachine.transitionTo(hisiMssStateMachine.mWiFiConnectedState);
                    break;
                case 11:
                    HisiMssStateMachine hisiMssStateMachine2 = HisiMssStateMachine.this;
                    hisiMssStateMachine2.transitionTo(hisiMssStateMachine2.mWiFiDisconnectedState);
                    break;
                case 12:
                    HwMssUtils.logE(HisiMssStateMachine.TAG, false, "WiFiEnabledState:unexpect msg:%{public}d", Integer.valueOf(msg.what));
                    break;
                case 13:
                    HisiMssStateMachine hisiMssStateMachine3 = HisiMssStateMachine.this;
                    hisiMssStateMachine3.transitionTo(hisiMssStateMachine3.mWiFiDisabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "WiFiEnabledState exit", new Object[0]);
        }
    }

    class WiFiDisabledState extends State {
        WiFiDisabledState() {
        }

        public void enter() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "WiFiDisabledState enter", new Object[0]);
        }

        public boolean processMessage(Message msg) {
            HwMssUtils.logD(HisiMssStateMachine.TAG, false, "WiFiDisabledState processMessage:%{public}d", Integer.valueOf(msg.what));
            switch (msg.what) {
                case 10:
                case 11:
                case 13:
                    HwMssUtils.logE(HisiMssStateMachine.TAG, false, "WiFiDisabledState:unexpect msg:%{public}d", Integer.valueOf(msg.what));
                    break;
                case 12:
                    HisiMssStateMachine hisiMssStateMachine = HisiMssStateMachine.this;
                    hisiMssStateMachine.transitionTo(hisiMssStateMachine.mWiFiEnabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "WiFiDisabledState exit", new Object[0]);
        }
    }

    class WiFiDisconnectedState extends State {
        WiFiDisconnectedState() {
        }

        public void enter() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "WiFiDisconnectedState enter", new Object[0]);
        }

        public boolean processMessage(Message msg) {
            HwMssUtils.logD(HisiMssStateMachine.TAG, false, "WiFiDisconnectedState processMessage:%{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (i == 10) {
                HisiMssStateMachine hisiMssStateMachine = HisiMssStateMachine.this;
                hisiMssStateMachine.transitionTo(hisiMssStateMachine.mWiFiConnectedState);
            } else if (i != 11) {
                return false;
            }
            return true;
        }

        public void exit() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "WiFiDisconnectedState exit", new Object[0]);
        }
    }

    class WiFiConnectedState extends State {
        WiFiConnectedState() {
        }

        public void enter() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "WiFiConnectedState enter", new Object[0]);
            HisiMssStateMachine.this.sendMessageDelayed(5, 5000);
        }

        public boolean processMessage(Message msg) {
            HwMssUtils.logD(HisiMssStateMachine.TAG, false, "WiFiConnectedState processMessage:%{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (!(i == 1 || i == 2)) {
                if (i == 3) {
                    HisiMssStateMachine.this.handleMssResponse(5, (NativeMssResult) msg.obj);
                } else if (i != 4) {
                    if (i != 5) {
                        if (i != 10) {
                            if (i != 11) {
                                return false;
                            }
                            HisiMssStateMachine hisiMssStateMachine = HisiMssStateMachine.this;
                            hisiMssStateMachine.transitionTo(hisiMssStateMachine.mWiFiDisconnectedState);
                        }
                    }
                }
                return true;
            }
            HisiMssStateMachine.this.syncStateFromDriver(5);
            return true;
        }

        public void exit() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "WiFiConnectedState exit", new Object[0]);
        }
    }

    class MimoState extends State {
        MimoState() {
        }

        public void enter() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "MimoState enter", new Object[0]);
            HisiMssStateMachine.this.syncStateToMssHandler(HwMssArbitrager.MssState.MSSMIMO);
        }

        public boolean processMessage(Message msg) {
            HwMssUtils.logD(HisiMssStateMachine.TAG, false, "MimoState processMessage:%{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (i == 1) {
                HisiMssStateMachine.this.sendMssCmdToDriver(1);
                HisiMssStateMachine hisiMssStateMachine = HisiMssStateMachine.this;
                hisiMssStateMachine.transitionTo(hisiMssStateMachine.mMimoToSisoState);
            } else if (i == 2) {
                HisiMssStateMachine.this.syncStateToMssHandler(HwMssArbitrager.MssState.MSSMIMO);
                HisiMssStateMachine.this.syncStateFromDriver(1);
            } else if (i == 3) {
                HisiMssStateMachine.this.handleMssResponse(1, (NativeMssResult) msg.obj);
            } else if (i == 4) {
                HisiMssStateMachine.this.handleMssRspTimeout(1);
            } else if (i != 5) {
                return false;
            } else {
                HisiMssStateMachine.this.syncStateFromDriver(1);
            }
            return true;
        }

        public void exit() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "MimoState exit", new Object[0]);
        }
    }

    class SisoState extends State {
        SisoState() {
        }

        public void enter() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "SisoState enter", new Object[0]);
            HisiMssStateMachine.this.syncStateToMssHandler(HwMssArbitrager.MssState.MSSSISO);
        }

        public boolean processMessage(Message msg) {
            HwMssUtils.logD(HisiMssStateMachine.TAG, false, "SisoState processMessage:%{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (i == 1) {
                HisiMssStateMachine.this.syncStateToMssHandler(HwMssArbitrager.MssState.MSSSISO);
                HisiMssStateMachine.this.syncStateFromDriver(3);
            } else if (i == 2) {
                HisiMssStateMachine.this.sendMssCmdToDriver(2);
                HisiMssStateMachine hisiMssStateMachine = HisiMssStateMachine.this;
                hisiMssStateMachine.transitionTo(hisiMssStateMachine.mSisoToMimoState);
            } else if (i == 3) {
                HisiMssStateMachine.this.handleMssResponse(3, (NativeMssResult) msg.obj);
            } else if (i == 4) {
                HisiMssStateMachine.this.handleMssRspTimeout(3);
            } else if (i != 5) {
                return false;
            } else {
                HisiMssStateMachine.this.syncStateFromDriver(3);
            }
            return true;
        }

        public void exit() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "SisoState exit", new Object[0]);
        }
    }

    class MimoToSisoState extends State {
        MimoToSisoState() {
        }

        public void enter() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "MimoToSisoState enter", new Object[0]);
            HisiMssStateMachine.this.syncStateToMssHandler(HwMssArbitrager.MssState.MSSSWITCHING);
        }

        public boolean processMessage(Message msg) {
            HwMssUtils.logD(HisiMssStateMachine.TAG, false, "MimoToSisoState processMessage:%{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (i == 1) {
                HwMssUtils.logE(HisiMssStateMachine.TAG, false, "receive MSG_MSS_SWITCH_SISO_REQ", new Object[0]);
            } else if (i == 2) {
                HisiMssStateMachine.this.sendMssCmdToDriver(2);
                HisiMssStateMachine hisiMssStateMachine = HisiMssStateMachine.this;
                hisiMssStateMachine.transitionTo(hisiMssStateMachine.mSisoToMimoState);
            } else if (i == 3) {
                HisiMssStateMachine.this.handleMssResponse(2, (NativeMssResult) msg.obj);
            } else if (i == 4) {
                HisiMssStateMachine.this.handleMssRspTimeout(2);
            } else if (i != 5) {
                return false;
            } else {
                HisiMssStateMachine.this.syncStateFromDriver(2);
            }
            return true;
        }

        public void exit() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "MimoToSisoState exit", new Object[0]);
        }
    }

    class SisoToMimoState extends State {
        SisoToMimoState() {
        }

        public void enter() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "SisoToMimoState enter", new Object[0]);
            HisiMssStateMachine.this.syncStateToMssHandler(HwMssArbitrager.MssState.MSSSWITCHING);
        }

        public boolean processMessage(Message msg) {
            HwMssUtils.logD(HisiMssStateMachine.TAG, false, "SisoToMimoState processMessage %{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (i == 1) {
                HisiMssStateMachine.this.sendMssCmdToDriver(1);
                HisiMssStateMachine hisiMssStateMachine = HisiMssStateMachine.this;
                hisiMssStateMachine.transitionTo(hisiMssStateMachine.mMimoToSisoState);
            } else if (i == 2) {
                HwMssUtils.logE(HisiMssStateMachine.TAG, false, "receive MSG_MSS_SWITCH_MIMO_REQ", new Object[0]);
            } else if (i != 3) {
                if (i == 4) {
                    HisiMssStateMachine.this.handleMssRspTimeout(4);
                } else if (i != 5) {
                    return false;
                } else {
                    HisiMssStateMachine.this.syncStateFromDriver(4);
                }
            } else if (msg.obj instanceof NativeMssResult) {
                HisiMssStateMachine.this.handleMssResponse(4, (NativeMssResult) msg.obj);
            }
            return true;
        }

        public void exit() {
            HwMssUtils.logV(HisiMssStateMachine.TAG, false, "SisoToMimoState exit", new Object[0]);
        }
    }

    private void addToBlacklist(NativeMssResult mssStru) {
        if (mssStru == null) {
            HwMssUtils.logE(TAG, false, "valid mssStru", new Object[0]);
            return;
        }
        WifiInfo wifiInfo = this.mWifiInfo;
        String ssid = wifiInfo == null ? "<unknown ssid>" : wifiInfo.getSSID();
        if (mssStru.mssResult == 0) {
            for (int i = 0; i < mssStru.vapNum; i++) {
                NativeMssResult.MssVapInfo info = mssStru.mssVapList[i];
                if (info.mssResult == 0) {
                    this.mBlacklistMgr.addToBlacklist(ssid, HwMssUtils.parseMacBytes(info.userMacAddr), info.actionType);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMssRspTimeout(int machineState) {
        this.mMssFailureCounter++;
        if (this.mMssFailureCounter > 10) {
            handlerMaxMssFailure();
        } else {
            syncStateFromDriver(machineState);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void syncStateFromDriver(int machineState) {
        int mssState = getMssStateFromDriver();
        if (machineState == 1) {
            handleSyncStateInMimoState(mssState);
        } else if (machineState == 2) {
            handleSyncStateInMimoToSisoState(mssState);
        } else if (machineState == 3) {
            handleSyncStateInSisoState(mssState);
        } else if (machineState == 4) {
            handleSyncStateInSisoToMimoState(mssState);
        } else if (machineState == 5) {
            handleSyncStateInConnState(mssState);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMssResponse(int machineState, NativeMssResult mssStru) {
        if (mssStru == null) {
            HwMssUtils.logE(TAG, false, "handleMssResponse mssStru is null", new Object[0]);
            return;
        }
        reportChrToMssHandler(mssStru);
        if (mssStru.vapNum > 0) {
            if (mssStru.mssResult == 1) {
                this.mMssFailureCounter = 0;
            } else {
                addToBlacklist(mssStru);
            }
        }
        if ((mssStru.vapNum == 0 || mssStru.mssResult == 0) && mssStru.mssState == 3) {
            this.mMssFailureCounter++;
        }
        if (this.mMssFailureCounter > 10) {
            handlerMaxMssFailure();
            return;
        }
        if (mssStru.mssState == 2 && machineState != 1) {
            transitionTo(this.mMimoState);
        }
        if (mssStru.mssState == 1 && machineState != 3) {
            transitionTo(this.mSisoState);
        }
        if (mssStru.mssState == 3) {
            sendMssCmdToDriver(2);
            transitionTo(this.mMimoState);
        }
        if (mssStru.mssState == 4) {
            HwMssUtils.logD(TAG, false, "handleMssResponse:STATE_SIMO", new Object[0]);
        }
    }

    private void handleSyncStateInConnState(int mssState) {
        if (mssState == 1) {
            transitionTo(this.mSisoState);
        } else if (mssState == 2) {
            transitionTo(this.mMimoState);
        } else if (mssState == 3) {
            sendMssCmdToDriver(2);
            transitionTo(this.mMimoState);
        } else if (mssState == 4 || mssState == 0) {
            transitionTo(this.mMimoState);
        } else {
            HwMssUtils.logD(TAG, false, "CONNState:mssState:%{public}d", Integer.valueOf(mssState));
        }
    }

    private void handleSyncStateInMimoState(int mssState) {
        if (mssState == 1) {
            transitionTo(this.mSisoState);
        } else if (mssState == 2) {
            HwMssUtils.logD(TAG, false, "MIMOState:now in MIMOState", new Object[0]);
        } else if (mssState == 3 || mssState == 4) {
            sendMssCmdToDriver(2);
            transitionTo(this.mMimoState);
        } else {
            HwMssUtils.logD(TAG, false, "MIMOState:mssState:%{public}d", Integer.valueOf(mssState));
        }
    }

    private void handleSyncStateInMimoToSisoState(int mssState) {
        if (mssState == 1) {
            transitionTo(this.mSisoState);
        } else if (mssState == 2) {
            transitionTo(this.mMimoState);
        } else if (mssState == 3 || mssState == 4) {
            sendMssCmdToDriver(2);
            transitionTo(this.mMimoState);
        } else {
            HwMssUtils.logD(TAG, false, "MTOSState:mssState:%{public}d", Integer.valueOf(mssState));
        }
    }

    private void handleSyncStateInSisoState(int mssState) {
        if (mssState == 1) {
            HwMssUtils.logD(TAG, false, "SISOState:now in SISOState", new Object[0]);
        } else if (mssState == 2) {
            transitionTo(this.mMimoState);
        } else if (mssState == 3 || mssState == 4) {
            sendMssCmdToDriver(2);
            transitionTo(this.mMimoState);
        } else {
            HwMssUtils.logD(TAG, false, "SISOState:mssState:%{public}d", Integer.valueOf(mssState));
        }
    }

    private void handleSyncStateInSisoToMimoState(int mssState) {
        if (mssState == 1) {
            transitionTo(this.mSisoState);
        } else if (mssState == 2) {
            transitionTo(this.mMimoState);
        } else if (mssState == 3 || mssState == 4) {
            sendMssCmdToDriver(2);
            transitionTo(this.mMimoState);
        } else {
            HwMssUtils.logD(TAG, false, "STOMState:mssState:%{public}d", Integer.valueOf(mssState));
        }
    }

    private int getMssStateFromDriver() {
        WifiNative wifiNative = this.mWifiNative;
        if (wifiNative != null) {
            return wifiNative.mHwWifiNativeEx.getWifiAnt(IFACE, 0);
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMssCmdToDriver(int direction) {
        if (this.mWifiNative == null || !(direction == 1 || direction == 2)) {
            HwMssUtils.logD(TAG, false, "unknown direction:%{public}d", Integer.valueOf(direction));
            return;
        }
        int timeout = HwMssUtils.HISI_M2S_RSP_TIMEOUT;
        if (direction == 2) {
            timeout = 30000;
        }
        if (hasMessages(4)) {
            removeMessages(4);
        }
        this.mWifiNative.mHwWifiNativeEx.setWifiAnt(IFACE, 0, direction);
        sendMessageDelayed(4, (long) timeout);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBlacklistToDriver() {
        List<HwMssDatabaseItem> lists = this.mBlacklistMgr.getBlacklist(true);
        Collections.sort(lists);
        List<HisiMssBlackListItem> newlists = HisiMssBlackListItem.parse(lists);
        if (newlists.size() > 0) {
            HwMssUtils.logD(TAG, false, "sendBlacklistToDriver size:%{public}d", Integer.valueOf(newlists.size()));
            for (HisiMssBlackListItem item : newlists) {
                if (item != null) {
                    HwMssUtils.logD(TAG, false, item.toString(), new Object[0]);
                }
            }
            this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 100, HisiMssBlackListItem.toByteArray(newlists));
        }
    }

    private void handlerMaxMssFailure() {
        HwMssUtils.logE(TAG, false, "reach max MSS failure", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void syncStateToMssHandler(HwMssArbitrager.MssState state) {
        HwMssHandler hwMssHandler = this.mHwMssHandler;
        if (hwMssHandler != null) {
            hwMssHandler.callbackSyncMssState(state);
        }
    }

    private void reportChrToMssHandler(NativeMssResult mssStru) {
        HwMssHandler hwMssHandler = this.mHwMssHandler;
        if (hwMssHandler != null) {
            hwMssHandler.callbackReportChr(mssStru);
        }
    }

    public void onMssDrvEvent(NativeMssResult mssStru) {
        if (mssStru != null) {
            HwMssUtils.logD(TAG, false, "%{public}s", mssStru.toString());
            if (hasMessages(4)) {
                removeMessages(4);
            }
            Message msg = obtainMessage(3, mssStru);
            if (msg != null) {
                sendMessage(msg);
            }
        }
    }

    public void doMssSwitch(int direction) {
        if (direction == 1 || direction == 2) {
            int msgwhat = 2;
            if (direction == 1) {
                msgwhat = 1;
            }
            sendMessage(msgwhat);
            return;
        }
        HwMssUtils.logE(TAG, false, "unknown direction:%{public}d", Integer.valueOf(direction));
    }
}
