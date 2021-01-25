package com.android.server.hidata.appqoe;

import android.content.Context;
import android.os.Message;
import android.os.PowerManager;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import com.android.server.hidata.wavemapping.HwWaveMappingManager;
import com.android.server.hidata.wavemapping.IWaveMappingCallback;

public class HwAPPQoEStateMachine extends StateMachine implements IWaveMappingCallback {
    public static final String STATE_MACHINE_NAME_CELL = "CellMonitorState";
    public static final String STATE_MACHINE_NAME_DATA_OFF = "DataOffMonitorState";
    public static final String STATE_MACHINE_NAME_IDLE = "IdleState";
    public static final String STATE_MACHINE_NAME_WIFI = "WiFiMonitorState";
    private static String TAG = "HiData_HwAPPQoEStateMachine";
    private static HwAPPQoEStateMachine mHwAPPQoEStateMachine = null;
    private HwAPPStateInfo curAPPStateInfo = new HwAPPStateInfo();
    private int lastAPPStateSent = -1;
    private State mCellMonitorState = new CellMonitorState();
    private Context mContext = null;
    private State mDataOffMonitorState = new DataOffMonitorState();
    private HwAPPQoEContentAware mHwAPPQoEContentAware = null;
    private HwAPPQoESystemStateMonitor mHwAPPQoESystemStateMonitor = null;
    private HwAPPQoEResourceManger mHwAPPQoeResourceManger = null;
    private State mIdleState = new IdleState();
    private State mWiFiMonitorState = new WiFiMonitorState();

    private HwAPPQoEStateMachine(Context context) {
        super("HwAPPQoEStateMachine");
        this.mContext = context;
        this.mHwAPPQoeResourceManger = HwAPPQoEResourceManger.createHwAPPQoEResourceManger();
        this.mHwAPPQoESystemStateMonitor = new HwAPPQoESystemStateMonitor(context, getHandler());
        addState(this.mIdleState);
        addState(this.mWiFiMonitorState, this.mIdleState);
        addState(this.mCellMonitorState, this.mIdleState);
        addState(this.mDataOffMonitorState, this.mIdleState);
        setInitialState(this.mIdleState);
        start();
    }

    public HwAPPStateInfo getCurAPPStateInfo() {
        HwAPPQoEUtils.logD(TAG, false, "APP QOE State machine Enter getCurAPPStateInfo ", new Object[0]);
        return this.curAPPStateInfo;
    }

    public static HwAPPQoEStateMachine createHwAPPQoEStateMachine(Context context) {
        if (mHwAPPQoEStateMachine == null) {
            mHwAPPQoEStateMachine = new HwAPPQoEStateMachine(context);
        }
        return mHwAPPQoEStateMachine;
    }

    public void initAPPQoEModules() {
        if (this.mContext == null) {
            HwAPPQoEUtils.logD(TAG, false, "initAPPQoEModules, invalid context.", new Object[0]);
            return;
        }
        HwAPPQoEUtils.logD(TAG, false, "initAPPQoEModules, process.", new Object[0]);
        this.mHwAPPQoEContentAware = HwAPPQoEContentAware.createHwAPPQoEContentAware(this.mContext, getHandler());
        HwWaveMappingManager mWaveMappingManager = HwWaveMappingManager.getInstance();
        if (mWaveMappingManager != null) {
            mWaveMappingManager.registerWaveMappingCallback(this, 0);
        }
    }

    public class IdleState extends State {
        public IdleState() {
        }

        public void enter() {
            HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "enter idle state", new Object[0]);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 100) {
                HwAPPStateInfo infoData = (HwAPPStateInfo) message.obj;
                HwAPPQoEStateMachine.this.updateCurAPPStateInfo(infoData);
                HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "IdleState, msg.what:%{public}d,%{public}s", Integer.valueOf(message.what), infoData.toString());
                int networkType = HwAPPQoEStateMachine.this.quryCurrentNetwork(infoData.mAppUID);
                if (800 == networkType) {
                    HwAPPQoEStateMachine hwAPPQoEStateMachine = HwAPPQoEStateMachine.this;
                    hwAPPQoEStateMachine.transitionTo(hwAPPQoEStateMachine.mWiFiMonitorState);
                } else if (801 == networkType) {
                    HwAPPQoEStateMachine hwAPPQoEStateMachine2 = HwAPPQoEStateMachine.this;
                    hwAPPQoEStateMachine2.transitionTo(hwAPPQoEStateMachine2.mCellMonitorState);
                } else if (802 == networkType) {
                    HwAPPQoEStateMachine hwAPPQoEStateMachine3 = HwAPPQoEStateMachine.this;
                    hwAPPQoEStateMachine3.transitionTo(hwAPPQoEStateMachine3.mDataOffMonitorState);
                    HwAPPQoEStateMachine hwAPPQoEStateMachine4 = HwAPPQoEStateMachine.this;
                    hwAPPQoEStateMachine4.sendMessage(Message.obtain(hwAPPQoEStateMachine4.getHandler(), 100, infoData));
                }
            } else if (i == 111) {
                HwAPPQoEStateMachine.this.notifyAPPQualityCallback((HwAPPStateInfo) message.obj, message.what, false);
            } else if (i == 200) {
                HwAPPQoEStateMachine.this.initAPPQoEModules();
            } else if (i == 202) {
                HwAPPQoEStateMachine.this.mHwAPPQoEContentAware.reRegisterAllGameCallbacks();
            } else if (i != 207) {
                HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "IdleState:default case", new Object[0]);
            } else if (HwAPPQoEStateMachine.this.mHwAPPQoEContentAware != null) {
                HwAPPQoEStateMachine.this.mHwAPPQoEContentAware.registerFIHiComCallback();
            }
            return true;
        }
    }

    public class DataOffMonitorState extends State {
        public DataOffMonitorState() {
        }

        public void enter() {
            HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "Enter DataOffMonitorState", new Object[0]);
        }

        public boolean processMessage(Message message) {
            HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "dataoff monitor state:%{public}d", Integer.valueOf(message.what));
            int i = message.what;
            if (i == 3) {
                HwAPPQoEStateMachine hwAPPQoEStateMachine = HwAPPQoEStateMachine.this;
                hwAPPQoEStateMachine.transitionTo(hwAPPQoEStateMachine.mWiFiMonitorState);
            } else if (i != 7) {
                if (i != 104) {
                    if (i == 201) {
                        HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "DataOffMonitorState MSG_INTERNAL_NETWORK_STATE_CHANGE", new Object[0]);
                        if (((Integer) message.obj).intValue() == 801) {
                            HwAPPQoEStateMachine hwAPPQoEStateMachine2 = HwAPPQoEStateMachine.this;
                            hwAPPQoEStateMachine2.transitionTo(hwAPPQoEStateMachine2.mCellMonitorState);
                        } else {
                            HwAPPQoEStateMachine hwAPPQoEStateMachine3 = HwAPPQoEStateMachine.this;
                            hwAPPQoEStateMachine3.transitionTo(hwAPPQoEStateMachine3.mWiFiMonitorState);
                        }
                    } else if (i != 202) {
                        switch (i) {
                            case 100:
                            case 102:
                                HwAPPStateInfo infoData = (HwAPPStateInfo) message.obj;
                                infoData.mNetworkType = 802;
                                HwAPPQoEStateMachine.this.notifyAPPStateCallback(infoData, message.what);
                                HwAPPQoEStateMachine.this.updateCurAPPStateInfo(infoData);
                                break;
                            case 101:
                                break;
                            default:
                                return false;
                        }
                    } else {
                        HwAPPQoEStateMachine.this.mHwAPPQoEContentAware.reRegisterAllGameCallbacks();
                    }
                }
                HwAPPStateInfo infoData2 = (HwAPPStateInfo) message.obj;
                infoData2.mNetworkType = 802;
                HwAPPQoEStateMachine.this.notifyAPPStateCallback(infoData2, message.what);
                if (true == HwAPPQoEStateMachine.this.isStopInfoMatchStartInfo(infoData2)) {
                    HwAPPQoEStateMachine hwAPPQoEStateMachine4 = HwAPPQoEStateMachine.this;
                    hwAPPQoEStateMachine4.transitionTo(hwAPPQoEStateMachine4.mIdleState);
                }
            } else {
                HwAPPQoEStateMachine hwAPPQoEStateMachine5 = HwAPPQoEStateMachine.this;
                hwAPPQoEStateMachine5.transitionTo(hwAPPQoEStateMachine5.mCellMonitorState);
            }
            return true;
        }
    }

    public class WiFiMonitorState extends State {
        public WiFiMonitorState() {
        }

        public void enter() {
            HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "Enter WiFiMonitorState", new Object[0]);
            int event = 100;
            if (102 == HwAPPQoEStateMachine.this.lastAPPStateSent) {
                event = 102;
            }
            HwAPPQoEStateMachine hwAPPQoEStateMachine = HwAPPQoEStateMachine.this;
            hwAPPQoEStateMachine.sendMessage(Message.obtain(hwAPPQoEStateMachine.getHandler(), event, HwAPPQoEStateMachine.this.curAPPStateInfo));
        }

        public void exit() {
            HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "WiFiMonitorState is Exit", new Object[0]);
        }

        public boolean processMessage(Message message) {
            HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "wifi monitor state:%{public}d", Integer.valueOf(message.what));
            int i = message.what;
            if (i == 4) {
                HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "WiFiMonitorState MSG_WIFI_STATE_DISCONNECT", new Object[0]);
                HwAPPQoEStateMachine hwAPPQoEStateMachine = HwAPPQoEStateMachine.this;
                hwAPPQoEStateMachine.transitionTo(hwAPPQoEStateMachine.mDataOffMonitorState);
            } else if (i == 7) {
                HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "WiFiMonitorState MSG_CELL_STATE_CONNECTED", new Object[0]);
                HwAPPQoEStateMachine hwAPPQoEStateMachine2 = HwAPPQoEStateMachine.this;
                hwAPPQoEStateMachine2.transitionTo(hwAPPQoEStateMachine2.mCellMonitorState);
            } else if (i != 112) {
                if (i != 104) {
                    if (i == 105) {
                        HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "WiFiMonitorState MSG_APP_STATE_MONITOR", new Object[0]);
                        HwAPPStateInfo infoData = (HwAPPStateInfo) message.obj;
                        infoData.mNetworkType = 800;
                        HwAPPQoEStateMachine.this.notifyAPPRttInfoCallback(infoData);
                    } else if (i == 201) {
                        HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "WiFiMonitorState MSG_INTERNAL_NETWORK_STATE_CHANGE", new Object[0]);
                        if (((Integer) message.obj).intValue() == 801) {
                            HwAPPQoEStateMachine hwAPPQoEStateMachine3 = HwAPPQoEStateMachine.this;
                            hwAPPQoEStateMachine3.transitionTo(hwAPPQoEStateMachine3.mCellMonitorState);
                        }
                    } else if (i != 202) {
                        switch (i) {
                            case 100:
                            case 102:
                                HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "WiFiMonitorState MSG_APP_STATE_START/UPDATE", new Object[0]);
                                HwAPPStateInfo infoData2 = (HwAPPStateInfo) message.obj;
                                if (infoData2.mAppType == 2000 && infoData2.mScenceId == 200001 && HwAPPQoEGameCallback.isHasGameCacheWarInfo(infoData2.mAppUID)) {
                                    infoData2.mScenceId = 200002;
                                    HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "has the game cashe war info", new Object[0]);
                                }
                                infoData2.mNetworkType = 800;
                                if (!HwAPPQoEStateMachine.this.curAPPStateInfo.isObjectValueEqual(infoData2) && message.what == 100 && HwAPPQoEStateMachine.this.curAPPStateInfo.mAppType == 2000) {
                                    if (HwAPPQoEStateMachine.this.curAPPStateInfo.mScenceId == 200002) {
                                        HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "alread in war", new Object[0]);
                                        break;
                                    } else if (infoData2.mScenceId == 200002) {
                                        HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "update in war", new Object[0]);
                                        message.what = 102;
                                    }
                                }
                                HwAPPQoEStateMachine.this.notifyAPPStateCallback(infoData2, message.what);
                                HwAPPQoEStateMachine.this.updateCurAPPStateInfo(infoData2);
                                break;
                            case 101:
                                break;
                            default:
                                return false;
                        }
                    } else {
                        HwAPPQoEStateMachine.this.mHwAPPQoEContentAware.reRegisterAllGameCallbacks();
                    }
                }
                HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "WiFiMonitorState MSG_APP_STATE_BACKGROUND/END", new Object[0]);
                HwAPPStateInfo infoData3 = (HwAPPStateInfo) message.obj;
                infoData3.mNetworkType = 800;
                HwAPPQoEStateMachine.this.notifyAPPStateCallback(infoData3, message.what);
                if (true == HwAPPQoEStateMachine.this.isStopInfoMatchStartInfo(infoData3)) {
                    HwAPPQoEStateMachine hwAPPQoEStateMachine4 = HwAPPQoEStateMachine.this;
                    hwAPPQoEStateMachine4.transitionTo(hwAPPQoEStateMachine4.mIdleState);
                }
            } else {
                HwAPPQoEStateMachine.this.sendMessage(107, (HwAPPStateInfo) message.obj);
            }
            return true;
        }
    }

    public class CellMonitorState extends State {
        public CellMonitorState() {
        }

        public void enter() {
            HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "Enter CellMonitorState", new Object[0]);
            int event = 100;
            if (102 == HwAPPQoEStateMachine.this.lastAPPStateSent) {
                event = 102;
            }
            HwAPPQoEStateMachine hwAPPQoEStateMachine = HwAPPQoEStateMachine.this;
            hwAPPQoEStateMachine.sendMessage(Message.obtain(hwAPPQoEStateMachine.getHandler(), event, HwAPPQoEStateMachine.this.curAPPStateInfo));
        }

        public void exit() {
            HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "CellMonitorState is Exit", new Object[0]);
            HwAPPQoEStateMachine.this.removeMessages(107);
        }

        public boolean processMessage(Message message) {
            HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "CellMonitorState, msg.what:%{public}d", Integer.valueOf(message.what));
            int i = message.what;
            if (i == 3) {
                HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "CellMonitorState MSG_WIFI_STATE_CONNECTED", new Object[0]);
                HwAPPQoEStateMachine hwAPPQoEStateMachine = HwAPPQoEStateMachine.this;
                hwAPPQoEStateMachine.transitionTo(hwAPPQoEStateMachine.mWiFiMonitorState);
            } else if (i == 8) {
                HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "CellMonitorState MSG_CELL_STATE_DISCONNECT", new Object[0]);
                HwAPPQoEStateMachine hwAPPQoEStateMachine2 = HwAPPQoEStateMachine.this;
                hwAPPQoEStateMachine2.transitionTo(hwAPPQoEStateMachine2.mDataOffMonitorState);
            } else if (i != 205) {
                if (i != 104) {
                    if (i == 105) {
                        HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "CellMonitorState MSG_APP_STATE_MONITOR", new Object[0]);
                        HwAPPStateInfo infoData = (HwAPPStateInfo) message.obj;
                        infoData.mNetworkType = 801;
                        HwAPPQoEStateMachine.this.notifyAPPRttInfoCallback(infoData);
                    } else if (i == 201) {
                        HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "CellMonitorState MSG_INTERNAL_NETWORK_STATE_CHANGE", new Object[0]);
                        if (((Integer) message.obj).intValue() == 800) {
                            HwAPPQoEStateMachine hwAPPQoEStateMachine3 = HwAPPQoEStateMachine.this;
                            hwAPPQoEStateMachine3.transitionTo(hwAPPQoEStateMachine3.mWiFiMonitorState);
                        }
                    } else if (i != 202) {
                        switch (i) {
                            case 100:
                            case 102:
                                HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "CellMonitorState MSG_APP_STATE_START/UPDATE", new Object[0]);
                                HwAPPStateInfo infoData2 = (HwAPPStateInfo) message.obj;
                                infoData2.mNetworkType = 801;
                                HwAPPQoEStateMachine.this.notifyAPPStateCallback(infoData2, message.what);
                                HwAPPQoEStateMachine.this.updateCurAPPStateInfo(infoData2);
                                break;
                            case 101:
                                break;
                            default:
                                return false;
                        }
                    } else {
                        HwAPPQoEStateMachine.this.mHwAPPQoEContentAware.reRegisterAllGameCallbacks();
                    }
                }
                HwAPPQoEUtils.logD(HwAPPQoEStateMachine.TAG, false, "CellMonitorState MSG_APP_STATE_BACKGROUND/END", new Object[0]);
                HwAPPStateInfo infoData3 = (HwAPPStateInfo) message.obj;
                infoData3.mNetworkType = 801;
                HwAPPQoEStateMachine.this.notifyAPPStateCallback(infoData3, message.what);
                if (true == HwAPPQoEStateMachine.this.isStopInfoMatchStartInfo(infoData3)) {
                    HwAPPQoEStateMachine hwAPPQoEStateMachine4 = HwAPPQoEStateMachine.this;
                    hwAPPQoEStateMachine4.transitionTo(hwAPPQoEStateMachine4.mIdleState);
                }
            }
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyAPPStateCallback(HwAPPStateInfo data, int state) {
        HwAPPQoEUtils.logD(TAG, false, "notifyAPPStateCallback, state:%{public}d, app:%{public}d, scence:%{public}d, action:%{public}d", Integer.valueOf(state), Integer.valueOf(data.mAppId), Integer.valueOf(data.mScenceId), Integer.valueOf(data.mAction));
        if (!this.curAPPStateInfo.isObjectValueEqual(data) || state != this.lastAPPStateSent) {
            HwAPPQoEManager hwAPPQoEManager = HwAPPQoEManager.getInstance();
            if (hwAPPQoEManager == null) {
                HwAPPQoEUtils.logE(TAG, false, "get HwAPPQoEManager instance fail", new Object[0]);
                return;
            }
            IHwAPPQoECallback brainCallback = hwAPPQoEManager.getAPPQoECallback(true);
            data.mAppState = state;
            HwAPPStateInfo tempData = new HwAPPStateInfo();
            tempData.copyObjectValue(data);
            if (brainCallback != null) {
                brainCallback.onAPPStateCallBack(tempData, state);
            }
            IHwAPPQoECallback wmCallback = hwAPPQoEManager.getAPPQoECallback(false);
            if (wmCallback != null) {
                wmCallback.onAPPStateCallBack(tempData, state);
            }
            if (tempData.mAppType == 2000 || tempData.mAppType == 3000 || (HwAPPQoEUtils.IS_TABLET && tempData.mAction > 0 && (tempData.mAction & 2) == 2)) {
                hwAPPQoEManager.notifyGameQoeCallback(tempData, state);
            }
            this.lastAPPStateSent = state;
            return;
        }
        HwAPPQoEUtils.logD(TAG, false, "notifyAPPStateCallback: info sent was same as before", new Object[0]);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyAPPQualityCallback(HwAPPStateInfo data, int state, boolean isWmOnly) {
        HwAPPQoEUtils.logD(TAG, false, "notifyAPPQualityCallback:%{public}d,%{public}d", Integer.valueOf(state), Integer.valueOf(data.mAppId));
        HwAPPQoEManager hwAPPQoEManager = HwAPPQoEManager.getInstance();
        if (hwAPPQoEManager == null) {
            HwAPPQoEUtils.logE(TAG, false, "get HwAPPQoEManager instance fail", new Object[0]);
            return;
        }
        IHwAPPQoECallback brainCallback = hwAPPQoEManager.getAPPQoECallback(true);
        data.setExperience(state);
        HwAPPStateInfo tempData = new HwAPPStateInfo();
        tempData.copyObjectValue(data);
        if (brainCallback != null && !isWmOnly) {
            brainCallback.onAPPQualityCallBack(tempData, state);
        }
        IHwAPPQoECallback wmCallback = hwAPPQoEManager.getAPPQoECallback(false);
        if (wmCallback != null) {
            wmCallback.onAPPQualityCallBack(tempData, state);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyAPPRttInfoCallback(HwAPPStateInfo data) {
        HwAPPQoEUtils.logD(TAG, false, "notifyAPPRttInfoCallback:%{public}s", data.toString());
        HwAPPQoEManager hwAPPQoEManager = HwAPPQoEManager.getInstance();
        if (hwAPPQoEManager == null) {
            HwAPPQoEUtils.logE(TAG, false, "get HwAPPQoEManager instance fail", new Object[0]);
            return;
        }
        IHwAPPQoECallback brainCallback = hwAPPQoEManager.getAPPQoECallback(true);
        HwAPPStateInfo tempData = new HwAPPStateInfo();
        tempData.copyObjectValue(data);
        if (brainCallback != null) {
            brainCallback.onAPPRttInfoCallBack(tempData);
        }
        IHwAPPQoECallback wmCallback = hwAPPQoEManager.getAPPQoECallback(false);
        if (wmCallback != null) {
            wmCallback.onAPPRttInfoCallBack(tempData);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int quryCurrentNetwork(int uid) {
        return HwArbitrationFunction.getCurrentNetwork(this.mContext, uid);
    }

    public boolean isStopInfoMatchStartInfo(HwAPPStateInfo infoData) {
        if (infoData.mAppUID != this.curAPPStateInfo.mAppUID) {
            return false;
        }
        this.curAPPStateInfo = new HwAPPStateInfo();
        updateCurAPPStateInfo(this.curAPPStateInfo);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateCurAPPStateInfo(HwAPPStateInfo infoData) {
        this.curAPPStateInfo.copyObjectValue(infoData);
        this.mHwAPPQoESystemStateMonitor.curAPPStateInfo.copyObjectValue(infoData);
    }

    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
        if (pm == null || !pm.isScreenOn()) {
            return false;
        }
        return true;
    }

    @Override // com.android.server.hidata.wavemapping.IWaveMappingCallback
    public void onWaveMappingReportCallback(int reportType, String networkName, int networkType) {
        HwAPPQoEUtils.logD(TAG, false, "onWaveMappingReportCallback", new Object[0]);
        HwAPPStateInfo curAppInfo = getCurAPPStateInfo();
        if (curAppInfo != null) {
            HwAPPQoEUtils.logD(TAG, false, "onWaveMappingReportCallback, app: %{public}d", Integer.valueOf(curAppInfo.mAppId));
            HwAPPQoEManager hwAPPQoEManager = HwAPPQoEManager.getInstance();
            if (hwAPPQoEManager == null) {
                HwAPPQoEUtils.logE(TAG, false, "get HwAPPQoEManager instance fail", new Object[0]);
                return;
            }
            IHwAPPQoECallback brainCallback = hwAPPQoEManager.getAPPQoECallback(true);
            if (brainCallback != null) {
                brainCallback.onAPPQualityCallBack(curAppInfo, 107);
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
