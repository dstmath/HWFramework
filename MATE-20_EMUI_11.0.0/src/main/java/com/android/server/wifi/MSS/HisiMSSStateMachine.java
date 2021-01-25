package com.android.server.wifi.MSS;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.os.Message;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.MSS.HwMSSArbitrager;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.wificond.NativeMssResult;
import java.util.Collections;
import java.util.List;

public class HisiMSSStateMachine extends StateMachine {
    private static final String IFACE = "wlan0";
    private static final int MACHINESTATE_CONN = 5;
    private static final int MACHINESTATE_MIMO = 1;
    private static final int MACHINESTATE_MTOS = 2;
    private static final int MACHINESTATE_SISO = 3;
    private static final int MACHINESTATE_STOM = 4;
    private static final String TAG = "HisiMSSStateMachine";
    private static HisiMSSStateMachine mMSStateMachine = null;
    private IHwMSSBlacklistMgr mBlacklistMgr = null;
    private Context mContext;
    private State mDefaultState = new DefaultState();
    private HwMSSHandler mHwMSSHandler = null;
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

    public static HisiMSSStateMachine createHisiMSSStateMachine(HwMSSHandler hwMSSHandler, Context cxt, WifiNative wifiNative, WifiInfo wifiInfo) {
        HwMSSUtils.logd(TAG, false, "createHisiMSSStateMachine enter", new Object[0]);
        if (mMSStateMachine == null) {
            mMSStateMachine = new HisiMSSStateMachine(hwMSSHandler, cxt, wifiNative, wifiInfo);
        }
        return mMSStateMachine;
    }

    private HisiMSSStateMachine(HwMSSHandler hwMSSHandler, Context cxt, WifiNative wifiNative, WifiInfo wifiInfo) {
        super(TAG);
        this.mContext = cxt;
        this.mWifiInfo = wifiInfo;
        this.mWifiNative = wifiNative;
        this.mHwMSSHandler = hwMSSHandler;
        this.mBlacklistMgr = HisiMSSBlackListManager.getInstance(this.mContext);
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
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, false, "DefaultState processMessage:%{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (i != 4) {
                switch (i) {
                    case 10:
                    case 11:
                        HwMSSUtils.loge(HisiMSSStateMachine.TAG, false, "DefaultState:unexpect msg:%{public}d", Integer.valueOf(msg.what));
                        break;
                    case 12:
                        HisiMSSStateMachine hisiMSSStateMachine = HisiMSSStateMachine.this;
                        hisiMSSStateMachine.transitionTo(hisiMSSStateMachine.mWiFiEnabledState);
                        break;
                    case 13:
                        HisiMSSStateMachine hisiMSSStateMachine2 = HisiMSSStateMachine.this;
                        hisiMSSStateMachine2.transitionTo(hisiMSSStateMachine2.mWiFiDisabledState);
                        break;
                }
            } else {
                HisiMSSStateMachine.this.removeMessages(4);
            }
            return true;
        }
    }

    class WiFiEnabledState extends State {
        WiFiEnabledState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "WiFiEnabledState enter", new Object[0]);
            HisiMSSStateMachine.this.sendBlacklistToDriver();
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, false, "WiFiEnabledState processMessage:%{public}d", Integer.valueOf(msg.what));
            switch (msg.what) {
                case 10:
                    HisiMSSStateMachine hisiMSSStateMachine = HisiMSSStateMachine.this;
                    hisiMSSStateMachine.transitionTo(hisiMSSStateMachine.mWiFiConnectedState);
                    break;
                case 11:
                    HisiMSSStateMachine hisiMSSStateMachine2 = HisiMSSStateMachine.this;
                    hisiMSSStateMachine2.transitionTo(hisiMSSStateMachine2.mWiFiDisconnectedState);
                    break;
                case 12:
                    HwMSSUtils.loge(HisiMSSStateMachine.TAG, false, "WiFiEnabledState:unexpect msg:%{public}d", Integer.valueOf(msg.what));
                    break;
                case 13:
                    HisiMSSStateMachine hisiMSSStateMachine3 = HisiMSSStateMachine.this;
                    hisiMSSStateMachine3.transitionTo(hisiMSSStateMachine3.mWiFiDisabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "WiFiEnabledState exit", new Object[0]);
        }
    }

    class WiFiDisabledState extends State {
        WiFiDisabledState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "WiFiDisabledState enter", new Object[0]);
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, false, "WiFiDisabledState processMessage:%{public}d", Integer.valueOf(msg.what));
            switch (msg.what) {
                case 10:
                case 11:
                case 13:
                    HwMSSUtils.loge(HisiMSSStateMachine.TAG, false, "WiFiDisabledState:unexpect msg:%{public}d", Integer.valueOf(msg.what));
                    break;
                case 12:
                    HisiMSSStateMachine hisiMSSStateMachine = HisiMSSStateMachine.this;
                    hisiMSSStateMachine.transitionTo(hisiMSSStateMachine.mWiFiEnabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "WiFiDisabledState exit", new Object[0]);
        }
    }

    class WiFiDisconnectedState extends State {
        WiFiDisconnectedState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "WiFiDisconnectedState enter", new Object[0]);
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, false, "WiFiDisconnectedState processMessage:%{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (i == 10) {
                HisiMSSStateMachine hisiMSSStateMachine = HisiMSSStateMachine.this;
                hisiMSSStateMachine.transitionTo(hisiMSSStateMachine.mWiFiConnectedState);
            } else if (i != 11) {
                return false;
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "WiFiDisconnectedState exit", new Object[0]);
        }
    }

    class WiFiConnectedState extends State {
        WiFiConnectedState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "WiFiConnectedState enter", new Object[0]);
            HisiMSSStateMachine.this.sendMessageDelayed(5, 5000);
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, false, "WiFiConnectedState processMessage:%{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (!(i == 1 || i == 2)) {
                if (i == 3) {
                    HisiMSSStateMachine.this.handleMssResponse(5, (NativeMssResult) msg.obj);
                } else if (i != 4) {
                    if (i != 5) {
                        if (i != 10) {
                            if (i != 11) {
                                return false;
                            }
                            HisiMSSStateMachine hisiMSSStateMachine = HisiMSSStateMachine.this;
                            hisiMSSStateMachine.transitionTo(hisiMSSStateMachine.mWiFiDisconnectedState);
                        }
                    }
                }
                return true;
            }
            HisiMSSStateMachine.this.syncStateFromDriver(5);
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "WiFiConnectedState exit", new Object[0]);
        }
    }

    class MimoState extends State {
        MimoState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "MimoState enter", new Object[0]);
            HisiMSSStateMachine.this.syncStateToMSSHandler(HwMSSArbitrager.MSSState.MSSMIMO);
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, false, "MimoState processMessage:%{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (i == 1) {
                HisiMSSStateMachine.this.sendMSSCmdToDriver(1);
                HisiMSSStateMachine hisiMSSStateMachine = HisiMSSStateMachine.this;
                hisiMSSStateMachine.transitionTo(hisiMSSStateMachine.mMimoToSisoState);
            } else if (i == 2) {
                HisiMSSStateMachine.this.syncStateToMSSHandler(HwMSSArbitrager.MSSState.MSSMIMO);
                HisiMSSStateMachine.this.syncStateFromDriver(1);
            } else if (i == 3) {
                HisiMSSStateMachine.this.handleMssResponse(1, (NativeMssResult) msg.obj);
            } else if (i == 4) {
                HisiMSSStateMachine.this.handleMssRspTimeout(1);
            } else if (i != 5) {
                return false;
            } else {
                HisiMSSStateMachine.this.syncStateFromDriver(1);
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "MimoState exit", new Object[0]);
        }
    }

    class SisoState extends State {
        SisoState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "SisoState enter", new Object[0]);
            HisiMSSStateMachine.this.syncStateToMSSHandler(HwMSSArbitrager.MSSState.MSSSISO);
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, false, "SisoState processMessage:%{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (i == 1) {
                HisiMSSStateMachine.this.syncStateToMSSHandler(HwMSSArbitrager.MSSState.MSSSISO);
                HisiMSSStateMachine.this.syncStateFromDriver(3);
            } else if (i == 2) {
                HisiMSSStateMachine.this.sendMSSCmdToDriver(2);
                HisiMSSStateMachine hisiMSSStateMachine = HisiMSSStateMachine.this;
                hisiMSSStateMachine.transitionTo(hisiMSSStateMachine.mSisoToMimoState);
            } else if (i == 3) {
                HisiMSSStateMachine.this.handleMssResponse(3, (NativeMssResult) msg.obj);
            } else if (i == 4) {
                HisiMSSStateMachine.this.handleMssRspTimeout(3);
            } else if (i != 5) {
                return false;
            } else {
                HisiMSSStateMachine.this.syncStateFromDriver(3);
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "SisoState exit", new Object[0]);
        }
    }

    class MimoToSisoState extends State {
        MimoToSisoState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "MimoToSisoState enter", new Object[0]);
            HisiMSSStateMachine.this.syncStateToMSSHandler(HwMSSArbitrager.MSSState.MSSSWITCHING);
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, false, "MimoToSisoState processMessage:%{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (i == 1) {
                HwMSSUtils.loge(HisiMSSStateMachine.TAG, false, "receive MSG_MSS_SWITCH_SISO_REQ", new Object[0]);
            } else if (i == 2) {
                HisiMSSStateMachine.this.sendMSSCmdToDriver(2);
                HisiMSSStateMachine hisiMSSStateMachine = HisiMSSStateMachine.this;
                hisiMSSStateMachine.transitionTo(hisiMSSStateMachine.mSisoToMimoState);
            } else if (i == 3) {
                HisiMSSStateMachine.this.handleMssResponse(2, (NativeMssResult) msg.obj);
            } else if (i == 4) {
                HisiMSSStateMachine.this.handleMssRspTimeout(2);
            } else if (i != 5) {
                return false;
            } else {
                HisiMSSStateMachine.this.syncStateFromDriver(2);
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "MimoToSisoState exit", new Object[0]);
        }
    }

    class SisoToMimoState extends State {
        SisoToMimoState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "SisoToMimoState enter", new Object[0]);
            HisiMSSStateMachine.this.syncStateToMSSHandler(HwMSSArbitrager.MSSState.MSSSWITCHING);
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, false, "SisoToMimoState processMessage %{public}d", Integer.valueOf(msg.what));
            int i = msg.what;
            if (i == 1) {
                HisiMSSStateMachine.this.sendMSSCmdToDriver(1);
                HisiMSSStateMachine hisiMSSStateMachine = HisiMSSStateMachine.this;
                hisiMSSStateMachine.transitionTo(hisiMSSStateMachine.mMimoToSisoState);
            } else if (i == 2) {
                HwMSSUtils.loge(HisiMSSStateMachine.TAG, false, "receive MSG_MSS_SWITCH_MIMO_REQ", new Object[0]);
            } else if (i == 3) {
                HisiMSSStateMachine.this.handleMssResponse(4, (NativeMssResult) msg.obj);
            } else if (i == 4) {
                HisiMSSStateMachine.this.handleMssRspTimeout(4);
            } else if (i != 5) {
                return false;
            } else {
                HisiMSSStateMachine.this.syncStateFromDriver(4);
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, false, "SisoToMimoState exit", new Object[0]);
        }
    }

    private void addToBlacklist(NativeMssResult mssStru) {
        if (mssStru == null) {
            HwMSSUtils.loge(TAG, false, "valid mssStru", new Object[0]);
            return;
        }
        WifiInfo wifiInfo = this.mWifiInfo;
        String ssid = wifiInfo == null ? "<unknown ssid>" : wifiInfo.getSSID();
        if (mssStru.mssResult == 0) {
            for (int i = 0; i < mssStru.vapNum; i++) {
                NativeMssResult.MssVapInfo info = mssStru.mssVapList[i];
                if (info.mssResult == 0) {
                    this.mBlacklistMgr.addToBlacklist(ssid, HwMSSUtils.parseMacBytes(info.userMacAddr), info.actionType);
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
        int mssState = getMSSStateFromDriver();
        if (machineState == 1) {
            handleSyncStateInMIMOState(mssState);
        } else if (machineState == 2) {
            handleSyncStateInMTOSState(mssState);
        } else if (machineState == 3) {
            handleSyncStateInSISOState(mssState);
        } else if (machineState == 4) {
            handleSyncStateInSTOMState(mssState);
        } else if (machineState == 5) {
            handleSyncStateInCONNState(mssState);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleMssResponse(int machineState, NativeMssResult mssStru) {
        if (mssStru == null) {
            HwMSSUtils.loge(TAG, false, "handleMssResponse mssStru is null", new Object[0]);
            return;
        }
        reportCHRToMSSHandler(mssStru);
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
            sendMSSCmdToDriver(2);
            transitionTo(this.mMimoState);
        }
        if (mssStru.mssState == 4) {
            HwMSSUtils.logd(TAG, false, "handleMssResponse:STATE_SIMO", new Object[0]);
        }
    }

    private void handleSyncStateInCONNState(int mssState) {
        if (1 == mssState) {
            transitionTo(this.mSisoState);
        } else if (2 == mssState) {
            transitionTo(this.mMimoState);
        } else if (3 == mssState) {
            sendMSSCmdToDriver(2);
            transitionTo(this.mMimoState);
        } else if (4 == mssState || mssState == 0) {
            transitionTo(this.mMimoState);
        } else {
            HwMSSUtils.logd(TAG, false, "CONNState:mssState:%{public}d", Integer.valueOf(mssState));
        }
    }

    private void handleSyncStateInMIMOState(int mssState) {
        if (1 == mssState) {
            transitionTo(this.mSisoState);
        } else if (2 == mssState) {
            HwMSSUtils.logd(TAG, false, "MIMOState:now in MIMOState", new Object[0]);
        } else if (3 == mssState || 4 == mssState) {
            sendMSSCmdToDriver(2);
            transitionTo(this.mMimoState);
        } else {
            HwMSSUtils.logd(TAG, false, "MIMOState:mssState:%{public}d", Integer.valueOf(mssState));
        }
    }

    private void handleSyncStateInMTOSState(int mssState) {
        if (1 == mssState) {
            transitionTo(this.mSisoState);
        } else if (2 == mssState) {
            transitionTo(this.mMimoState);
        } else if (3 == mssState || 4 == mssState) {
            sendMSSCmdToDriver(2);
            transitionTo(this.mMimoState);
        } else {
            HwMSSUtils.logd(TAG, false, "MTOSState:mssState:%{public}d", Integer.valueOf(mssState));
        }
    }

    private void handleSyncStateInSISOState(int mssState) {
        if (1 == mssState) {
            HwMSSUtils.logd(TAG, false, "SISOState:now in SISOState", new Object[0]);
        } else if (2 == mssState) {
            transitionTo(this.mMimoState);
        } else if (3 == mssState || 4 == mssState) {
            sendMSSCmdToDriver(2);
            transitionTo(this.mMimoState);
        } else {
            HwMSSUtils.logd(TAG, false, "SISOState:mssState:%{public}d", Integer.valueOf(mssState));
        }
    }

    private void handleSyncStateInSTOMState(int mssState) {
        if (1 == mssState) {
            transitionTo(this.mSisoState);
        } else if (2 == mssState) {
            transitionTo(this.mMimoState);
        } else if (3 == mssState || 4 == mssState) {
            sendMSSCmdToDriver(2);
            transitionTo(this.mMimoState);
        } else {
            HwMSSUtils.logd(TAG, false, "STOMState:mssState:%{public}d", Integer.valueOf(mssState));
        }
    }

    private int getMSSStateFromDriver() {
        WifiNative wifiNative = this.mWifiNative;
        if (wifiNative != null) {
            return wifiNative.mHwWifiNativeEx.getWifiAnt(IFACE, 0);
        }
        return -1;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMSSCmdToDriver(int direction) {
        if (this.mWifiNative == null || !(1 == direction || 2 == direction)) {
            HwMSSUtils.logd(TAG, false, "unknown direction:%{public}d", Integer.valueOf(direction));
            return;
        }
        int timeout = HwMSSUtils.HISI_M2S_RSP_TIMEOUT;
        if (2 == direction) {
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
        List<HwMSSDatabaseItem> lists = this.mBlacklistMgr.getBlacklist(true);
        Collections.sort(lists);
        List<HisiMSSBlacklistItem> newlists = HisiMSSBlacklistItem.parse(lists);
        if (newlists.size() > 0) {
            HwMSSUtils.logd(TAG, false, "sendBlacklistToDriver size:%{public}d", Integer.valueOf(newlists.size()));
            for (HisiMSSBlacklistItem item : newlists) {
                if (item != null) {
                    HwMSSUtils.logd(TAG, false, item.toString(), new Object[0]);
                }
            }
            this.mWifiNative.mHwWifiNativeEx.sendCmdToDriver(IFACE, 100, HisiMSSBlacklistItem.toByteArray(newlists));
        }
    }

    private void handlerMaxMssFailure() {
        HwMSSUtils.loge(TAG, false, "reach max MSS failure", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void syncStateToMSSHandler(HwMSSArbitrager.MSSState state) {
        HwMSSHandler hwMSSHandler = this.mHwMSSHandler;
        if (hwMSSHandler != null) {
            hwMSSHandler.callbackSyncMssState(state);
        }
    }

    private void reportCHRToMSSHandler(NativeMssResult mssStru) {
        HwMSSHandler hwMSSHandler = this.mHwMSSHandler;
        if (hwMSSHandler != null) {
            hwMSSHandler.callbackReportCHR(mssStru);
        }
    }

    public void onMssDrvEvent(NativeMssResult mssStru) {
        if (mssStru != null) {
            HwMSSUtils.logd(TAG, false, "%{public}s", mssStru.toString());
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
        if (1 == direction || 2 == direction) {
            int msgwhat = 2;
            if (1 == direction) {
                msgwhat = 1;
            }
            sendMessage(msgwhat);
            return;
        }
        HwMSSUtils.loge(TAG, false, "unknown direction:%{public}d", Integer.valueOf(direction));
    }
}
