package com.android.server.hidata.appqoe;

import android.content.Context;
import android.os.Message;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.hidata.arbitration.HwArbitrationDefs;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import com.android.server.hidata.wavemapping.IWaveMappingCallback;

public class HwAppQoeStateMachine extends StateMachine implements IWaveMappingCallback {
    private static final String TAG = (HwArbitrationDefs.BASE_TAG + HwAppQoeStateMachine.class.getSimpleName());
    private static HwAppQoeStateMachine sHwAppQoeStateMachine = null;
    private State mCellMonitorState = new CellMonitorState();
    private Context mContext = null;
    private HwAppStateInfo mCurAppStateInfo = new HwAppStateInfo();
    private State mDataOffMonitorState = new DataOffMonitorState();
    private HwAppQoeContentAware mHwAppQoeContentAware = null;
    private HwAppQoeResourceManager mHwAppQoeResourceManager = null;
    private HwAppQoeSystemStateMonitor mHwAppQoeSystemStateMonitor = null;
    private State mIdleState = new IdleState();
    private int mLastSentAppState = -1;
    private State mWiFiMonitorState = new WiFiMonitorState();

    private HwAppQoeStateMachine(Context context) {
        super("HwAppQoeStateMachine");
        this.mContext = context;
        this.mHwAppQoeResourceManager = HwAppQoeResourceManager.createHwAppQoeResourceManager();
        this.mHwAppQoeSystemStateMonitor = new HwAppQoeSystemStateMonitor(context, getHandler());
        addState(this.mIdleState);
        addState(this.mWiFiMonitorState, this.mIdleState);
        addState(this.mCellMonitorState, this.mIdleState);
        addState(this.mDataOffMonitorState, this.mIdleState);
        setInitialState(this.mIdleState);
        start();
    }

    public HwAppStateInfo getCurAppStateInfo() {
        HwAppQoeUtils.logD(TAG, false, "enter getCurAppStateInfo ", new Object[0]);
        return this.mCurAppStateInfo;
    }

    public static HwAppQoeStateMachine createHwAppQoeStateMachine(Context context) {
        if (sHwAppQoeStateMachine == null) {
            sHwAppQoeStateMachine = new HwAppQoeStateMachine(context);
        }
        return sHwAppQoeStateMachine;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initAppQoeModules() {
        if (this.mContext == null) {
            HwAppQoeUtils.logD(TAG, false, "initAppQoeModules, invalid context.", new Object[0]);
            return;
        }
        HwAppQoeUtils.logD(TAG, false, "initAppQoeModules, process.", new Object[0]);
        this.mHwAppQoeContentAware = HwAppQoeContentAware.createHwAppQoeContentAware(this.mContext, getHandler());
    }

    private class IdleState extends State {
        private IdleState() {
        }

        public void enter() {
            HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "Enter IdleState", new Object[0]);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 100) {
                HwAppStateInfo infoData = (HwAppStateInfo) message.obj;
                HwAppQoeStateMachine.this.updateCurAppStateInfo(infoData);
                HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "IdleState, msg.what:%{public}d,%{public}s", Integer.valueOf(message.what), infoData.toString());
                int networkType = HwAppQoeStateMachine.this.queryCurrentNetwork(infoData.mAppUid);
                if (networkType == 800) {
                    HwAppQoeStateMachine hwAppQoeStateMachine = HwAppQoeStateMachine.this;
                    hwAppQoeStateMachine.transitionTo(hwAppQoeStateMachine.mWiFiMonitorState);
                } else if (networkType == 801) {
                    HwAppQoeStateMachine hwAppQoeStateMachine2 = HwAppQoeStateMachine.this;
                    hwAppQoeStateMachine2.transitionTo(hwAppQoeStateMachine2.mCellMonitorState);
                } else if (networkType == 802) {
                    HwAppQoeStateMachine hwAppQoeStateMachine3 = HwAppQoeStateMachine.this;
                    hwAppQoeStateMachine3.transitionTo(hwAppQoeStateMachine3.mDataOffMonitorState);
                    HwAppQoeStateMachine hwAppQoeStateMachine4 = HwAppQoeStateMachine.this;
                    hwAppQoeStateMachine4.sendMessage(Message.obtain(hwAppQoeStateMachine4.getHandler(), 100, infoData));
                } else {
                    HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "other network type", new Object[0]);
                }
            } else if (i == 111) {
                HwAppQoeStateMachine.this.notifyAppQualityCallback((HwAppStateInfo) message.obj, message.what, false);
            } else if (i == 200) {
                HwAppQoeStateMachine.this.initAppQoeModules();
            } else if (i == 202) {
                HwAppQoeStateMachine.this.mHwAppQoeContentAware.reRegisterAllGameCallbacks();
            } else if (i != 207) {
                HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "IdleState:default case", new Object[0]);
            } else if (HwAppQoeStateMachine.this.mHwAppQoeContentAware != null) {
                HwAppQoeStateMachine.this.mHwAppQoeContentAware.registerFiHiComCallback();
            }
            return true;
        }
    }

    private class DataOffMonitorState extends State {
        private DataOffMonitorState() {
        }

        public void enter() {
            HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "Enter DataOffMonitorState", new Object[0]);
        }

        public boolean processMessage(Message message) {
            HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "data off monitor state:%{public}d", Integer.valueOf(message.what));
            int i = message.what;
            if (i == 3) {
                HwAppQoeStateMachine hwAppQoeStateMachine = HwAppQoeStateMachine.this;
                hwAppQoeStateMachine.transitionTo(hwAppQoeStateMachine.mWiFiMonitorState);
            } else if (i != 7) {
                if (i != 104) {
                    if (i == 201) {
                        HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "DataOffMonitorState MSG_INTERNAL_NETWORK_STATE_CHANGE", new Object[0]);
                        if (((Integer) message.obj).intValue() == 801) {
                            HwAppQoeStateMachine hwAppQoeStateMachine2 = HwAppQoeStateMachine.this;
                            hwAppQoeStateMachine2.transitionTo(hwAppQoeStateMachine2.mCellMonitorState);
                        } else {
                            HwAppQoeStateMachine hwAppQoeStateMachine3 = HwAppQoeStateMachine.this;
                            hwAppQoeStateMachine3.transitionTo(hwAppQoeStateMachine3.mWiFiMonitorState);
                        }
                    } else if (i != 202) {
                        switch (i) {
                            case 100:
                            case 102:
                                HwAppStateInfo infoData = (HwAppStateInfo) message.obj;
                                infoData.mNetworkType = 802;
                                HwAppQoeStateMachine.this.notifyAppStateCallback(infoData, message.what);
                                HwAppQoeStateMachine.this.updateCurAppStateInfo(infoData);
                                break;
                            case 101:
                                break;
                            default:
                                return false;
                        }
                    } else {
                        HwAppQoeStateMachine.this.mHwAppQoeContentAware.reRegisterAllGameCallbacks();
                    }
                }
                HwAppStateInfo infoData2 = (HwAppStateInfo) message.obj;
                infoData2.mNetworkType = 802;
                HwAppQoeStateMachine.this.notifyAppStateCallback(infoData2, message.what);
                if (HwAppQoeStateMachine.this.isMatchCurrentAppStateInfo(infoData2)) {
                    HwAppQoeStateMachine hwAppQoeStateMachine4 = HwAppQoeStateMachine.this;
                    hwAppQoeStateMachine4.transitionTo(hwAppQoeStateMachine4.mIdleState);
                }
            } else {
                HwAppQoeStateMachine hwAppQoeStateMachine5 = HwAppQoeStateMachine.this;
                hwAppQoeStateMachine5.transitionTo(hwAppQoeStateMachine5.mCellMonitorState);
            }
            return true;
        }
    }

    private class WiFiMonitorState extends State {
        private WiFiMonitorState() {
        }

        public void enter() {
            HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "Enter WiFiMonitorState", new Object[0]);
            int event = 100;
            if (HwAppQoeStateMachine.this.mLastSentAppState == 102) {
                event = 102;
            }
            HwAppQoeStateMachine hwAppQoeStateMachine = HwAppQoeStateMachine.this;
            hwAppQoeStateMachine.sendMessage(Message.obtain(hwAppQoeStateMachine.getHandler(), event, HwAppQoeStateMachine.this.mCurAppStateInfo));
        }

        public void exit() {
            HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "WiFiMonitorState is Exit", new Object[0]);
        }

        public boolean processMessage(Message message) {
            HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "wifi monitor state:%{public}d", Integer.valueOf(message.what));
            int i = message.what;
            if (i == 4) {
                HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "WiFiMonitorState MSG_WIFI_STATE_DISCONNECT", new Object[0]);
                HwAppQoeStateMachine hwAppQoeStateMachine = HwAppQoeStateMachine.this;
                hwAppQoeStateMachine.transitionTo(hwAppQoeStateMachine.mDataOffMonitorState);
            } else if (i == 7) {
                HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "WiFiMonitorState MSG_CELL_STATE_CONNECTED", new Object[0]);
                HwAppQoeStateMachine hwAppQoeStateMachine2 = HwAppQoeStateMachine.this;
                hwAppQoeStateMachine2.transitionTo(hwAppQoeStateMachine2.mCellMonitorState);
            } else if (i != 112) {
                if (i != 104) {
                    if (i == 105) {
                        HwAppQoeStateMachine.this.processWifiAppStateMonitor(message);
                    } else if (i == 201) {
                        HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "WiFiMonitorState MSG_INTERNAL_NETWORK_STATE_CHANGE", new Object[0]);
                        if (((Integer) message.obj).intValue() == 801) {
                            HwAppQoeStateMachine hwAppQoeStateMachine3 = HwAppQoeStateMachine.this;
                            hwAppQoeStateMachine3.transitionTo(hwAppQoeStateMachine3.mCellMonitorState);
                        }
                    } else if (i != 202) {
                        switch (i) {
                            case 100:
                            case 102:
                                if (HwAppQoeStateMachine.this.isCurrentScenesInWar(message)) {
                                }
                                break;
                            case 101:
                                break;
                            default:
                                return false;
                        }
                    } else {
                        HwAppQoeStateMachine.this.mHwAppQoeContentAware.reRegisterAllGameCallbacks();
                    }
                }
                HwAppQoeStateMachine.this.processWifiAppStateEnd(message);
            } else {
                HwAppQoeStateMachine.this.sendMessage(107, (HwAppStateInfo) message.obj);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isCurrentScenesInWar(Message message) {
        HwAppQoeUtils.logD(TAG, false, "WiFiMonitorState MSG_APP_STATE_START/UPDATE", new Object[0]);
        HwAppStateInfo infoData = (HwAppStateInfo) message.obj;
        if (infoData.mAppType == 2000 && infoData.mScenesId == 200001 && HwAppQoeGameCallback.isHasGameCacheWarInfo(infoData.mAppUid)) {
            infoData.mScenesId = 200002;
            HwAppQoeUtils.logD(TAG, false, "has the game cashe war info", new Object[0]);
        }
        infoData.mNetworkType = 800;
        if (!this.mCurAppStateInfo.isObjectValueEqual(infoData) && message.what == 100 && this.mCurAppStateInfo.mAppType == 2000) {
            if (this.mCurAppStateInfo.mScenesId == 200002) {
                HwAppQoeUtils.logD(TAG, false, "already in war", new Object[0]);
                return true;
            } else if (infoData.mScenesId == 200002) {
                HwAppQoeUtils.logD(TAG, false, "update in war", new Object[0]);
                message.what = 102;
            }
        }
        notifyAppStateCallback(infoData, message.what);
        updateCurAppStateInfo(infoData);
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processWifiAppStateMonitor(Message message) {
        HwAppQoeUtils.logD(TAG, false, "WiFiMonitorState MSG_APP_STATE_MONITOR", new Object[0]);
        HwAppStateInfo infoData = (HwAppStateInfo) message.obj;
        infoData.mNetworkType = 800;
        notifyAppRttInfoCallback(infoData);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processWifiAppStateEnd(Message message) {
        HwAppQoeUtils.logD(TAG, false, "WiFiMonitorState MSG_APP_STATE_BACKGROUND/END", new Object[0]);
        HwAppStateInfo infoData = (HwAppStateInfo) message.obj;
        infoData.mNetworkType = 800;
        notifyAppStateCallback(infoData, message.what);
        if (isMatchCurrentAppStateInfo(infoData)) {
            transitionTo(this.mIdleState);
        }
    }

    private class CellMonitorState extends State {
        private CellMonitorState() {
        }

        public void enter() {
            HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "Enter CellMonitorState", new Object[0]);
            int event = 100;
            if (HwAppQoeStateMachine.this.mLastSentAppState == 102) {
                event = 102;
            }
            HwAppQoeStateMachine hwAppQoeStateMachine = HwAppQoeStateMachine.this;
            hwAppQoeStateMachine.sendMessage(Message.obtain(hwAppQoeStateMachine.getHandler(), event, HwAppQoeStateMachine.this.mCurAppStateInfo));
        }

        public void exit() {
            HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "CellMonitorState is Exit", new Object[0]);
            HwAppQoeStateMachine.this.removeMessages(107);
        }

        public boolean processMessage(Message message) {
            HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "CellMonitorState, msg.what:%{public}d", Integer.valueOf(message.what));
            int i = message.what;
            if (i == 3) {
                HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "CellMonitorState MSG_WIFI_STATE_CONNECTED", new Object[0]);
                HwAppQoeStateMachine hwAppQoeStateMachine = HwAppQoeStateMachine.this;
                hwAppQoeStateMachine.transitionTo(hwAppQoeStateMachine.mWiFiMonitorState);
            } else if (i != 8) {
                if (i != 104) {
                    if (i == 105) {
                        HwAppQoeStateMachine.this.processCellAppStateMonitor(message);
                    } else if (i == 201) {
                        HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "CellMonitorState MSG_INTERNAL_NETWORK_STATE_CHANGE", new Object[0]);
                        if (((Integer) message.obj).intValue() == 800) {
                            HwAppQoeStateMachine hwAppQoeStateMachine2 = HwAppQoeStateMachine.this;
                            hwAppQoeStateMachine2.transitionTo(hwAppQoeStateMachine2.mWiFiMonitorState);
                        }
                    } else if (i != 202) {
                        switch (i) {
                            case 100:
                            case 102:
                                HwAppQoeStateMachine.this.processCellAppStateStart(message);
                                break;
                            case 101:
                                break;
                            default:
                                return false;
                        }
                    } else {
                        HwAppQoeStateMachine.this.mHwAppQoeContentAware.reRegisterAllGameCallbacks();
                    }
                }
                HwAppQoeStateMachine.this.processCellAppStateEnd(message);
            } else {
                HwAppQoeUtils.logD(HwAppQoeStateMachine.TAG, false, "CellMonitorState MSG_CELL_STATE_DISCONNECT", new Object[0]);
                HwAppQoeStateMachine hwAppQoeStateMachine3 = HwAppQoeStateMachine.this;
                hwAppQoeStateMachine3.transitionTo(hwAppQoeStateMachine3.mDataOffMonitorState);
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processCellAppStateStart(Message message) {
        HwAppQoeUtils.logD(TAG, false, "CellMonitorState MSG_APP_STATE_START/UPDATE", new Object[0]);
        HwAppStateInfo infoData = (HwAppStateInfo) message.obj;
        infoData.mNetworkType = 801;
        notifyAppStateCallback(infoData, message.what);
        updateCurAppStateInfo(infoData);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processCellAppStateMonitor(Message message) {
        HwAppQoeUtils.logD(TAG, false, "CellMonitorState MSG_APP_STATE_MONITOR", new Object[0]);
        HwAppStateInfo infoData = (HwAppStateInfo) message.obj;
        infoData.mNetworkType = 801;
        notifyAppRttInfoCallback(infoData);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void processCellAppStateEnd(Message message) {
        HwAppQoeUtils.logD(TAG, false, "CellMonitorState MSG_APP_STATE_BACKGROUND/END", new Object[0]);
        if (message.obj instanceof HwAppStateInfo) {
            HwAppStateInfo infoData = (HwAppStateInfo) message.obj;
            infoData.mNetworkType = 801;
            notifyAppStateCallback(infoData, message.what);
            if (isMatchCurrentAppStateInfo(infoData)) {
                transitionTo(this.mIdleState);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyAppStateCallback(HwAppStateInfo data, int state) {
        HwAppQoeUtils.logD(TAG, false, "notifyAppStateCallback, state:%{public}d, app:%{public}d, scenes:%{public}d, action:%{public}d", Integer.valueOf(state), Integer.valueOf(data.mAppId), Integer.valueOf(data.mScenesId), Integer.valueOf(data.mAction));
        if (!this.mCurAppStateInfo.isObjectValueEqual(data) || state != this.mLastSentAppState) {
            HwAppQoeManager hwAppQoeManager = HwAppQoeManager.getInstance();
            if (hwAppQoeManager == null) {
                HwAppQoeUtils.logE(TAG, false, "get HwAppQoeManager instance fail", new Object[0]);
                return;
            }
            IHwAppQoeCallback brainCallback = hwAppQoeManager.getAppQoeCallback(true);
            data.mAppState = state;
            HwAppStateInfo tempData = new HwAppStateInfo();
            tempData.copyObjectValue(data);
            if (brainCallback != null) {
                brainCallback.onAppStateCallBack(tempData, state);
            }
            IHwAppQoeCallback wmCallback = hwAppQoeManager.getAppQoeCallback(false);
            if (wmCallback != null) {
                wmCallback.onAppStateCallBack(tempData, state);
            }
            if (tempData.mAppType == 2000 || tempData.mAppType == 3000 || (HwAppQoeUtils.IS_TABLET && tempData.mAction > 0 && (tempData.mAction & 2) == 2)) {
                hwAppQoeManager.notifyGameQoeCallback(tempData, state);
            }
            this.mLastSentAppState = state;
            return;
        }
        HwAppQoeUtils.logD(TAG, false, "notifyAppStateCallback, info sent was same as before", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyAppQualityCallback(HwAppStateInfo data, int state, boolean isWmOnly) {
        HwAppQoeUtils.logD(TAG, false, "notifyAppQualityCallback, %{public}d,%{public}d", Integer.valueOf(state), Integer.valueOf(data.mAppId));
        HwAppQoeManager hwAPPQoEManager = HwAppQoeManager.getInstance();
        if (hwAPPQoEManager == null) {
            HwAppQoeUtils.logE(TAG, false, "get HwAppQoeManager instance fail", new Object[0]);
            return;
        }
        IHwAppQoeCallback brainCallback = hwAPPQoEManager.getAppQoeCallback(true);
        data.setExperience(state);
        HwAppStateInfo tempData = new HwAppStateInfo();
        tempData.copyObjectValue(data);
        if (brainCallback != null && !isWmOnly) {
            brainCallback.onAppQualityCallBack(tempData, state);
        }
        IHwAppQoeCallback wmCallback = hwAPPQoEManager.getAppQoeCallback(false);
        if (wmCallback != null) {
            wmCallback.onAppQualityCallBack(tempData, state);
        }
    }

    private void notifyAppRttInfoCallback(HwAppStateInfo data) {
        HwAppQoeUtils.logD(TAG, false, "notifyAppRttInfoCallback, %{public}s", data.toString());
        HwAppQoeManager hwAppQoeManager = HwAppQoeManager.getInstance();
        if (hwAppQoeManager == null) {
            HwAppQoeUtils.logE(TAG, false, "get HwAppQoeManager instance fail", new Object[0]);
            return;
        }
        IHwAppQoeCallback brainCallback = hwAppQoeManager.getAppQoeCallback(true);
        HwAppStateInfo tempData = new HwAppStateInfo();
        tempData.copyObjectValue(data);
        if (brainCallback != null) {
            brainCallback.onAppRttInfoCallBack(tempData);
        }
        IHwAppQoeCallback wmCallback = hwAppQoeManager.getAppQoeCallback(false);
        if (wmCallback != null) {
            wmCallback.onAppRttInfoCallBack(tempData);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int queryCurrentNetwork(int uid) {
        return HwArbitrationFunction.getCurrentNetwork(this.mContext, uid);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isMatchCurrentAppStateInfo(HwAppStateInfo infoData) {
        if (infoData.mAppUid != this.mCurAppStateInfo.mAppUid) {
            return false;
        }
        this.mCurAppStateInfo = new HwAppStateInfo();
        updateCurAppStateInfo(this.mCurAppStateInfo);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCurAppStateInfo(HwAppStateInfo infoData) {
        this.mCurAppStateInfo.copyObjectValue(infoData);
        this.mHwAppQoeSystemStateMonitor.mCurAppStateInfo.copyObjectValue(infoData);
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingReportCallback(int reportType, String networkName, int networkType) {
        HwAppQoeUtils.logD(TAG, false, "onWaveMappingReportCallback", new Object[0]);
        HwAppStateInfo curAppInfo = getCurAppStateInfo();
        if (curAppInfo != null) {
            HwAppQoeUtils.logD(TAG, false, "onWaveMappingReportCallback, app: %{public}d", Integer.valueOf(curAppInfo.mAppId));
            HwAppQoeManager hwAppQoeManager = HwAppQoeManager.getInstance();
            if (hwAppQoeManager == null) {
                HwAppQoeUtils.logE(TAG, false, "get HwAppQoeManager instance fail", new Object[0]);
                return;
            }
            IHwAppQoeCallback brainCallback = hwAppQoeManager.getAppQoeCallback(true);
            if (brainCallback != null) {
                brainCallback.onAppQualityCallBack(curAppInfo, 107);
            }
        }
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingRespondCallback(int UID, int prefer, int network, boolean isGood, boolean found) {
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingRespond4BackCallback(int UID, int prefer, int network, boolean isGood, boolean found) {
    }
}
