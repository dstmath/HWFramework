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
    /* access modifiers changed from: private */
    public State mMimoToSisoState = new MimoToSisoState();
    private int mMssFailureCounter = 0;
    private State mSisoState = new SisoState();
    /* access modifiers changed from: private */
    public State mSisoToMimoState = new SisoToMimoState();
    /* access modifiers changed from: private */
    public State mWiFiConnectedState = new WiFiConnectedState();
    /* access modifiers changed from: private */
    public State mWiFiDisabledState = new WiFiDisabledState();
    /* access modifiers changed from: private */
    public State mWiFiDisconnectedState = new WiFiDisconnectedState();
    /* access modifiers changed from: private */
    public State mWiFiEnabledState = new WiFiEnabledState();
    private WifiInfo mWifiInfo = null;
    private WifiNative mWifiNative = null;

    class DefaultState extends State {
        DefaultState() {
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, "DefaultState processMessage:" + msg.what);
            int i = msg.what;
            if (i != 4) {
                switch (i) {
                    case 10:
                    case 11:
                        HwMSSUtils.loge(HisiMSSStateMachine.TAG, "DefaultState:unexpect msg:" + msg.what);
                        break;
                    case 12:
                        HisiMSSStateMachine.this.transitionTo(HisiMSSStateMachine.this.mWiFiEnabledState);
                        break;
                    case 13:
                        HisiMSSStateMachine.this.transitionTo(HisiMSSStateMachine.this.mWiFiDisabledState);
                        break;
                }
            } else {
                HisiMSSStateMachine.this.removeMessages(4);
            }
            return true;
        }
    }

    class MimoState extends State {
        MimoState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "MimoState enter");
            HisiMSSStateMachine.this.syncStateToMSSHandler(HwMSSArbitrager.MSSState.MSSMIMO);
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, "MimoState processMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                    HisiMSSStateMachine.this.sendMSSCmdToDriver(1);
                    HisiMSSStateMachine.this.transitionTo(HisiMSSStateMachine.this.mMimoToSisoState);
                    break;
                case 2:
                    HisiMSSStateMachine.this.syncStateToMSSHandler(HwMSSArbitrager.MSSState.MSSMIMO);
                    HisiMSSStateMachine.this.syncStateFromDriver(1);
                    break;
                case 3:
                    HisiMSSStateMachine.this.handleMssResponse(1, (NativeMssResult) msg.obj);
                    break;
                case 4:
                    HisiMSSStateMachine.this.handleMssRspTimeout(1);
                    break;
                case 5:
                    HisiMSSStateMachine.this.syncStateFromDriver(1);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "MimoState exit");
        }
    }

    class MimoToSisoState extends State {
        MimoToSisoState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "MimoToSisoState enter");
            HisiMSSStateMachine.this.syncStateToMSSHandler(HwMSSArbitrager.MSSState.MSSSWITCHING);
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, "MimoToSisoState processMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                    HwMSSUtils.loge(HisiMSSStateMachine.TAG, "receive MSG_MSS_SWITCH_SISO_REQ");
                    break;
                case 2:
                    HisiMSSStateMachine.this.sendMSSCmdToDriver(2);
                    HisiMSSStateMachine.this.transitionTo(HisiMSSStateMachine.this.mSisoToMimoState);
                    break;
                case 3:
                    HisiMSSStateMachine.this.handleMssResponse(2, (NativeMssResult) msg.obj);
                    break;
                case 4:
                    HisiMSSStateMachine.this.handleMssRspTimeout(2);
                    break;
                case 5:
                    HisiMSSStateMachine.this.syncStateFromDriver(2);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "MimoToSisoState exit");
        }
    }

    class SisoState extends State {
        SisoState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "SisoState enter");
            HisiMSSStateMachine.this.syncStateToMSSHandler(HwMSSArbitrager.MSSState.MSSSISO);
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, "SisoState processMessage:" + msg.what);
            switch (msg.what) {
                case 1:
                    HisiMSSStateMachine.this.syncStateToMSSHandler(HwMSSArbitrager.MSSState.MSSSISO);
                    HisiMSSStateMachine.this.syncStateFromDriver(3);
                    break;
                case 2:
                    HisiMSSStateMachine.this.sendMSSCmdToDriver(2);
                    HisiMSSStateMachine.this.transitionTo(HisiMSSStateMachine.this.mSisoToMimoState);
                    break;
                case 3:
                    HisiMSSStateMachine.this.handleMssResponse(3, (NativeMssResult) msg.obj);
                    break;
                case 4:
                    HisiMSSStateMachine.this.handleMssRspTimeout(3);
                    break;
                case 5:
                    HisiMSSStateMachine.this.syncStateFromDriver(3);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "SisoState exit");
        }
    }

    class SisoToMimoState extends State {
        SisoToMimoState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "SisoToMimoState enter");
            HisiMSSStateMachine.this.syncStateToMSSHandler(HwMSSArbitrager.MSSState.MSSSWITCHING);
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, "SisoToMimoState processMessage" + msg.what);
            switch (msg.what) {
                case 1:
                    HisiMSSStateMachine.this.sendMSSCmdToDriver(1);
                    HisiMSSStateMachine.this.transitionTo(HisiMSSStateMachine.this.mMimoToSisoState);
                    break;
                case 2:
                    HwMSSUtils.loge(HisiMSSStateMachine.TAG, "receive MSG_MSS_SWITCH_MIMO_REQ");
                    break;
                case 3:
                    HisiMSSStateMachine.this.handleMssResponse(4, (NativeMssResult) msg.obj);
                    break;
                case 4:
                    HisiMSSStateMachine.this.handleMssRspTimeout(4);
                    break;
                case 5:
                    HisiMSSStateMachine.this.syncStateFromDriver(4);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "SisoToMimoState exit");
        }
    }

    class WiFiConnectedState extends State {
        WiFiConnectedState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "WiFiConnectedState enter");
            HisiMSSStateMachine.this.sendMessageDelayed(5, 5000);
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, "WiFiConnectedState processMessage:" + msg.what);
            int i = msg.what;
            switch (i) {
                case 1:
                case 2:
                case 5:
                    HisiMSSStateMachine.this.syncStateFromDriver(5);
                    break;
                case 3:
                    HisiMSSStateMachine.this.handleMssResponse(5, (NativeMssResult) msg.obj);
                    break;
                case 4:
                    break;
                default:
                    switch (i) {
                        case 10:
                            break;
                        case 11:
                            HisiMSSStateMachine.this.transitionTo(HisiMSSStateMachine.this.mWiFiDisconnectedState);
                            break;
                        default:
                            return false;
                    }
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "WiFiConnectedState exit");
        }
    }

    class WiFiDisabledState extends State {
        WiFiDisabledState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "WiFiDisabledState enter");
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, "WiFiDisabledState processMessage:" + msg.what);
            switch (msg.what) {
                case 10:
                case 11:
                case 13:
                    HwMSSUtils.loge(HisiMSSStateMachine.TAG, "WiFiDisabledState:unexpect msg:" + msg.what);
                    break;
                case 12:
                    HisiMSSStateMachine.this.transitionTo(HisiMSSStateMachine.this.mWiFiEnabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "WiFiDisabledState exit");
        }
    }

    class WiFiDisconnectedState extends State {
        WiFiDisconnectedState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "WiFiDisconnectedState enter");
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, "WiFiDisconnectedState processMessage:" + msg.what);
            switch (msg.what) {
                case 10:
                    HisiMSSStateMachine.this.transitionTo(HisiMSSStateMachine.this.mWiFiConnectedState);
                    break;
                case 11:
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "WiFiDisconnectedState exit");
        }
    }

    class WiFiEnabledState extends State {
        WiFiEnabledState() {
        }

        public void enter() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "WiFiEnabledState enter");
            HisiMSSStateMachine.this.sendBlacklistToDriver();
        }

        public boolean processMessage(Message msg) {
            HwMSSUtils.logd(HisiMSSStateMachine.TAG, "WiFiEnabledState processMessage:" + msg.what);
            switch (msg.what) {
                case 10:
                    HisiMSSStateMachine.this.transitionTo(HisiMSSStateMachine.this.mWiFiConnectedState);
                    break;
                case 11:
                    HisiMSSStateMachine.this.transitionTo(HisiMSSStateMachine.this.mWiFiDisconnectedState);
                    break;
                case 12:
                    HwMSSUtils.loge(HisiMSSStateMachine.TAG, "WiFiEnabledState:unexpect msg:" + msg.what);
                    break;
                case 13:
                    HisiMSSStateMachine.this.transitionTo(HisiMSSStateMachine.this.mWiFiDisabledState);
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void exit() {
            HwMSSUtils.logv(HisiMSSStateMachine.TAG, "WiFiEnabledState exit");
        }
    }

    public static HisiMSSStateMachine createHisiMSSStateMachine(HwMSSHandler hwMSSHandler, Context cxt, WifiNative wifiNative, WifiInfo wifiInfo) {
        HwMSSUtils.logd(TAG, "createHisiMSSStateMachine enter");
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

    private void addToBlacklist(NativeMssResult mssStru) {
        if (mssStru == null) {
            HwMSSUtils.loge(TAG, "valid mssStru");
            return;
        }
        String ssid = this.mWifiInfo == null ? "<unknown ssid>" : this.mWifiInfo.getSSID();
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
    public void handleMssRspTimeout(int machineState) {
        this.mMssFailureCounter++;
        if (this.mMssFailureCounter > 10) {
            handlerMaxMssFailure();
        } else {
            syncStateFromDriver(machineState);
        }
    }

    /* access modifiers changed from: private */
    public void syncStateFromDriver(int machineState) {
        int mssState = getMSSStateFromDriver();
        switch (machineState) {
            case 1:
                handleSyncStateInMIMOState(mssState);
                return;
            case 2:
                handleSyncStateInMTOSState(mssState);
                return;
            case 3:
                handleSyncStateInSISOState(mssState);
                return;
            case 4:
                handleSyncStateInSTOMState(mssState);
                return;
            case 5:
                handleSyncStateInCONNState(mssState);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: private */
    public void handleMssResponse(int machineState, NativeMssResult mssStru) {
        if (mssStru == null) {
            HwMSSUtils.loge(TAG, "handleMssResponse mssStru is null");
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
            HwMSSUtils.logd(TAG, "handleMssResponse:STATE_SIMO");
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
            HwMSSUtils.logd(TAG, "CONNState:mssState:" + mssState);
        }
    }

    private void handleSyncStateInMIMOState(int mssState) {
        if (1 == mssState) {
            transitionTo(this.mSisoState);
        } else if (2 == mssState) {
            HwMSSUtils.logd(TAG, "MIMOState:now in MIMOState");
        } else if (3 == mssState || 4 == mssState) {
            sendMSSCmdToDriver(2);
            transitionTo(this.mMimoState);
        } else {
            HwMSSUtils.logd(TAG, "MIMOState:mssState:" + mssState);
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
            HwMSSUtils.logd(TAG, "MTOSState:mssState:" + mssState);
        }
    }

    private void handleSyncStateInSISOState(int mssState) {
        if (1 == mssState) {
            HwMSSUtils.logd(TAG, "SISOState:now in SISOState");
        } else if (2 == mssState) {
            transitionTo(this.mMimoState);
        } else if (3 == mssState || 4 == mssState) {
            sendMSSCmdToDriver(2);
            transitionTo(this.mMimoState);
        } else {
            HwMSSUtils.logd(TAG, "SISOState:mssState:" + mssState);
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
            HwMSSUtils.logd(TAG, "STOMState:mssState:" + mssState);
        }
    }

    private int getMSSStateFromDriver() {
        if (this.mWifiNative != null) {
            return this.mWifiNative.getWifiAnt(IFACE, 0);
        }
        return -1;
    }

    /* access modifiers changed from: private */
    public void sendMSSCmdToDriver(int direction) {
        if (this.mWifiNative == null || !(1 == direction || 2 == direction)) {
            HwMSSUtils.logd(TAG, "unknown direction:" + direction);
            return;
        }
        int timeout = 300000;
        if (2 == direction) {
            timeout = 30000;
        }
        if (hasMessages(4)) {
            removeMessages(4);
        }
        this.mWifiNative.setWifiAnt(IFACE, 0, direction);
        sendMessageDelayed(4, (long) timeout);
    }

    /* access modifiers changed from: private */
    public void sendBlacklistToDriver() {
        List<HwMSSDatabaseItem> lists = this.mBlacklistMgr.getBlacklist(true);
        Collections.sort(lists);
        List<HisiMSSBlacklistItem> newlists = HisiMSSBlacklistItem.parse(lists);
        if (newlists.size() > 0) {
            HwMSSUtils.logd(TAG, "sendBlacklistToDriver size:" + newlists.size());
            for (HisiMSSBlacklistItem item : newlists) {
                if (item != null) {
                    HwMSSUtils.logd(TAG, item.toString());
                }
            }
            this.mWifiNative.sendCmdToDriver(IFACE, 100, HisiMSSBlacklistItem.toByteArray(newlists));
        }
    }

    private void handlerMaxMssFailure() {
        HwMSSUtils.loge(TAG, "reach max MSS failure");
    }

    /* access modifiers changed from: private */
    public void syncStateToMSSHandler(HwMSSArbitrager.MSSState state) {
        if (this.mHwMSSHandler != null) {
            this.mHwMSSHandler.callbackSyncMssState(state);
        }
    }

    private void reportCHRToMSSHandler(NativeMssResult mssStru) {
        if (this.mHwMSSHandler != null) {
            this.mHwMSSHandler.callbackReportCHR(mssStru);
        }
    }

    public void onMssDrvEvent(NativeMssResult mssStru) {
        if (mssStru != null) {
            HwMSSUtils.logd(TAG, mssStru.toString());
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
        HwMSSUtils.loge(TAG, "unknown direction:" + direction);
    }
}
