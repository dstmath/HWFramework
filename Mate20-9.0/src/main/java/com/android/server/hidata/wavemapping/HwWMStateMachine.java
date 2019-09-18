package com.android.server.hidata.wavemapping;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.PowerManager;
import android.util.Log;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.hidata.HwHidataJniAdapter;
import com.android.server.hidata.appqoe.HwAPPQoEManager;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.cons.WMStateCons;
import com.android.server.hidata.wavemapping.dao.EnterpriseApDAO;
import com.android.server.hidata.wavemapping.dao.LocationDAO;
import com.android.server.hidata.wavemapping.dao.MobileApDAO;
import com.android.server.hidata.wavemapping.dao.RegularPlaceDAO;
import com.android.server.hidata.wavemapping.dataprovider.BehaviorReceiver;
import com.android.server.hidata.wavemapping.dataprovider.FrequentLocation;
import com.android.server.hidata.wavemapping.dataprovider.HwWmpCallbackImpl;
import com.android.server.hidata.wavemapping.dataprovider.WifiDataProvider;
import com.android.server.hidata.wavemapping.entity.ApInfo;
import com.android.server.hidata.wavemapping.entity.ClusterResult;
import com.android.server.hidata.wavemapping.entity.HwWmpAppInfo;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.RecognizeResult;
import com.android.server.hidata.wavemapping.entity.RegularPlaceInfo;
import com.android.server.hidata.wavemapping.entity.UiInfo;
import com.android.server.hidata.wavemapping.modelservice.ModelService5;
import com.android.server.hidata.wavemapping.service.ActiveCollectDecision;
import com.android.server.hidata.wavemapping.service.RecognizeService;
import com.android.server.hidata.wavemapping.service.UiService;
import com.android.server.hidata.wavemapping.statehandler.CollectFingersHandler;
import com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler;
import com.android.server.hidata.wavemapping.util.CellStateMonitor;
import com.android.server.hidata.wavemapping.util.CheckTempertureUtil;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import com.android.server.rms.iaware.feature.DevSchedFeatureRT;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwWMStateMachine extends StateMachine {
    private static final long POWER_CONNECT_DELAY = 300000;
    public static final String TAG = ("WMapping." + HwWMStateMachine.class.getSimpleName());
    private static HwWMStateMachine hwWMStateMachine;
    /* access modifiers changed from: private */
    public int SUBSTATE_ACTIVITY = 1;
    /* access modifiers changed from: private */
    public int SUBSTATE_LASTRECG = 0;
    private CheckTempertureUtil checkTempertureUtil;
    /* access modifiers changed from: private */
    public CollectFingersHandler collectFingersHandler;
    /* access modifiers changed from: private */
    public RecognizeResult curSpaceIds = new RecognizeResult();
    /* access modifiers changed from: private */
    public RegularPlaceInfo cur_mainApPlaceInfo = null;
    /* access modifiers changed from: private */
    public RegularPlaceInfo cur_place = null;
    private HandlerThread handlerThread;
    /* access modifiers changed from: private */
    public boolean initFinish = false;
    private boolean isUsbConnected = false;
    /* access modifiers changed from: private */
    public RecognizeResult lastSpaceIds = new RecognizeResult();
    /* access modifiers changed from: private */
    public long lastTrainingTime = 0;
    private HwAPPQoEManager mAPPQoEManager = null;
    /* access modifiers changed from: private */
    public ActiveCollectDecision mActiveCollectHandler = null;
    /* access modifiers changed from: private */
    public CellStateMonitor mCellStateMonitor = null;
    /* access modifiers changed from: private */
    public CollectTrainingState mCollectTrainingState = new CollectTrainingState();
    /* access modifiers changed from: private */
    public CollectUserFingersHandler mCollectUserFingersHandler = null;
    /* access modifiers changed from: private */
    public Context mCtx;
    /* access modifiers changed from: private */
    public State mDefaultState = new DefaultState();
    /* access modifiers changed from: private */
    public FrequentLocation mFrequentLocation;
    private FrequentLocationState mFrequentLocationState = new FrequentLocationState();
    private Handler mHandler;
    private HwWmpCallbackImpl mHwWmpCallbackImpl = null;
    /* access modifiers changed from: private */
    public LocatingState mLocatingState = new LocatingState();
    /* access modifiers changed from: private */
    public LocationDAO mLocationDAO;
    /* access modifiers changed from: private */
    public PositionState mPositionState = new PositionState();
    /* access modifiers changed from: private */
    public PowerManager mPowerManager = null;
    private RecognitionState mRecognitionState = new RecognitionState();
    private long mScanEndTime = 0;
    private long mUpdateDBOtherTime = 0;
    /* access modifiers changed from: private */
    public WifiManager mWifiManager;
    /* access modifiers changed from: private */
    public BehaviorReceiver mbehaviorReceiver;
    public ModelService5 modelService = null;
    private ParameterInfo param;
    private RegularPlaceDAO regularPlaceDAO;
    private HashMap<String, RegularPlaceInfo> rgLocations;
    /* access modifiers changed from: private */
    public long timePowerConnect = 0;
    private String toastInfo;
    /* access modifiers changed from: private */
    public UiInfo uiInfo;
    /* access modifiers changed from: private */
    public UiService uiService;
    /* access modifiers changed from: private */
    public WifiDataProvider wifiDataProvider;
    /* access modifiers changed from: private */
    public List<ScanResult> wifiList = null;

    class CollectTrainingState extends State {
        public final String TAG = ("WMapping." + CollectTrainingState.class.getSimpleName());
        private RegularPlaceInfo regularPlaceInfo;

        CollectTrainingState() {
        }

        public void enter() {
            LogUtil.d("enter mCollectTrainingState");
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 64) {
                LogUtil.d("MSG_WIFI_UPDATE_SCAN_RESULT002, wifi scan complete");
                HwWMStateMachine.this.mActiveCollectHandler.startBgScan();
                long checkTrainingDuration = System.currentTimeMillis() - HwWMStateMachine.this.lastTrainingTime;
                if (checkTrainingDuration < 43200000) {
                    LogUtil.d("checkDataSatisfiedToTraining, checkTrainingDuration < LIMIT_TRAINING_INTERVAL,checkTrainingDuration:" + checkTrainingDuration);
                } else if (HwWMStateMachine.this.collectFingersHandler.checkDataSatisfiedToTraining(HwWMStateMachine.this.cur_place)) {
                    HwWMStateMachine.this.sendMessage(80);
                }
            } else if (i == 110) {
                LogUtil.d("MSG_AR_MOVE, user go to other space");
                HwWMStateMachine.this.mActiveCollectHandler.stopBgScan();
                HwWMStateMachine.this.mActiveCollectHandler.stopStallScan();
                HwWMStateMachine.this.mActiveCollectHandler.stopOut4gRecgScan();
                return false;
            } else if (i == 141 || i == 211) {
                LogUtil.d("Into MSG_APP_DATA_STALL");
                if (HwWMStateMachine.this.mActiveCollectHandler != null) {
                    HwWMStateMachine.this.mActiveCollectHandler.startStallScan();
                }
            } else {
                switch (i) {
                    case 80:
                        long duration = System.currentTimeMillis() - HwWMStateMachine.this.timePowerConnect;
                        LogUtil.d("MSG_FINGER_NUM_SATISFIED, training criteria is satisfied, check power connection: " + HwWMStateMachine.this.timePowerConnect + " , duration: " + duration);
                        if (0 != HwWMStateMachine.this.timePowerConnect && duration > 300000) {
                            HwWMStateMachine.this.trainModels(HwWMStateMachine.this.cur_place.getPlace());
                            break;
                        }
                    case WMStateCons.MSG_BUILDMODEL_COMPLETED /*81*/:
                        LogUtil.d("MSG_BUILDMODEL_COMPLETED, model complete");
                        if (HwWMStateMachine.this.cur_place != null && HwWMStateMachine.this.cur_place.getPlace() != null) {
                            LogUtil.d("    current location = " + HwWMStateMachine.this.cur_place.getPlace());
                            if (!HwWMStateMachine.this.getBuildModelResult()) {
                                HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mCollectTrainingState);
                                break;
                            } else {
                                HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mLocatingState);
                                break;
                            }
                        } else {
                            LogUtil.e(" current location == null");
                            HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mDefaultState);
                            break;
                        }
                        break;
                    default:
                        return false;
                }
            }
            return true;
        }

        public void exit() {
            LogUtil.d("exit mCollectTrainingState");
            HwWMStateMachine.this.mActiveCollectHandler.stopBgScan();
            HwWMStateMachine.this.mActiveCollectHandler.stopStallScan();
            if (HwWMStateMachine.this.getHandler().hasMessages(64)) {
                HwWMStateMachine.this.removeMessages(64);
            }
        }
    }

    class DefaultState extends State {
        public final String TAG = ("WMapping." + DefaultState.class.getSimpleName());

        DefaultState() {
        }

        public void enter() {
            LogUtil.d("enter DefaultState");
        }

        public boolean processMessage(Message message) {
            if (!HwWMStateMachine.this.initFinish) {
                LogUtil.w(" StateMachine NOT initial");
                return true;
            }
            int i = message.what;
            if (i == 20) {
                try {
                    LogUtil.i("Into MSG_ADD_FREQ_LOCATION_TOOL");
                    HwWMStateMachine.this.addFreqLocByTool(message.getData());
                } catch (Exception e) {
                    LogUtil.e("processMessage:" + e);
                }
            } else if (i == 30) {
                LogUtil.d("Into MSG_IN_FREQ_LOCATION");
                try {
                    Bundle bundle = message.getData();
                    if (bundle != null) {
                        if (bundle.get("LOCATION") != null) {
                            String curLocation = bundle.get("LOCATION").toString();
                            LogUtil.d("MSG_IN_FREQ_LOCATION,curLocation=" + curLocation);
                            if (HwWMStateMachine.this.cur_place == null || HwWMStateMachine.this.cur_place.getPlace() == null) {
                                LogUtil.d(" cur_place == null");
                                HwWMStateMachine.this.getCur_place(curLocation);
                                if (HwWMStateMachine.this.cur_place == null || HwWMStateMachine.this.cur_place.getPlace() == null) {
                                    LogUtil.e(" getPlace error");
                                    HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mDefaultState);
                                } else if (!HwWMStateMachine.this.cur_place.getPlace().equals(curLocation)) {
                                    LogUtil.e(" getPlace:" + HwWMStateMachine.this.cur_place.toString() + " not the same as input location:" + curLocation);
                                    HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mDefaultState);
                                }
                            } else if (HwWMStateMachine.this.cur_place.getPlace().equals(curLocation)) {
                                LogUtil.d(" the same location - KEEP current state, current " + HwWMStateMachine.this.cur_place.toString());
                            }
                            LogUtil.i(" cur_place.getPlace()=" + HwWMStateMachine.this.cur_place.getPlace());
                            CollectFingersHandler unused = HwWMStateMachine.this.collectFingersHandler = new CollectFingersHandler(HwWMStateMachine.this.mCtx, HwWMStateMachine.this.cur_place.getPlace(), HwWMStateMachine.this.getHandler());
                            HwWMStateMachine.this.mCollectUserFingersHandler.assignSpaceExp2Space(HwWMStateMachine.this.lastSpaceIds, true);
                            HwWMStateMachine.this.mCollectUserFingersHandler.setFreqLocation(HwWMStateMachine.this.cur_place.getPlace());
                            HwWMStateMachine.this.mbehaviorReceiver.saveBatch();
                            HwWMStateMachine.this.collectFingersHandler.mainApInfoToDb();
                            if (HwWMStateMachine.this.cur_place.getState() == 4) {
                                HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mLocatingState);
                            } else {
                                HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mCollectTrainingState);
                            }
                        }
                    }
                    LogUtil.w(" no bundle location");
                } catch (Exception e2) {
                    LogUtil.e("WMStateCons.MSG_IN_FREQ_LOCATION:" + e2.getMessage());
                }
            } else if (i == 115) {
                LogUtil.d("Into MSG_USER_DATA_ACTION");
                if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                    HwWMStateMachine.this.mCollectUserFingersHandler.updateSourceNetwork();
                }
            } else if (i == 120) {
                LogUtil.d("into MSG_QUERY_HISQOE, query saved history App QoE");
                HwWMStateMachine.this.queryHistoryQoe(message.getData(), (IWaveMappingCallback) message.obj);
            } else if (i == 130) {
                HwWMStateMachine.this.updateUserSpaceDbForOtherLoc(true);
                LogUtil.i("into MSG_SYS_SHUTDOWN, go back Default");
                HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mDefaultState);
            } else if (i == 210) {
                LogUtil.d("Into MSG_APP_QOE_EVENT");
                Bundle bundle2 = message.getData();
                if (bundle2 == null) {
                    LogUtil.e(" no bundle messages");
                } else if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                    HwWMStateMachine.this.mCollectUserFingersHandler.updateAppQoE(bundle2.getString("APPNAME"), bundle2.getInt("QOE"));
                }
            } else if (i != 220) {
                switch (i) {
                    case WMStateCons.MSG_CONNECTIVITY_CHANGE /*91*/:
                        LogUtil.d("Into MSG_CONNECTIVITY_CHANGE");
                        if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                            HwWMStateMachine.this.mCollectUserFingersHandler.checkAllConnectivityState(message.arg1, message.arg2);
                            break;
                        }
                        break;
                    case WMStateCons.MSG_SUPPLICANT_COMPLETE /*92*/:
                        LogUtil.i("Into MSG_SUPPLICANT_COMPLETE");
                        if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                            HwWMStateMachine.this.mCollectUserFingersHandler.checkUserPrefAutoSwitch();
                            HwWMStateMachine.this.mCollectUserFingersHandler.updateWifiDurationForAp(true);
                            break;
                        }
                        break;
                    case WMStateCons.MSG_CELL_CHANGE /*93*/:
                    case WMStateCons.MSG_CELL_IN_SERVICE /*95*/:
                    case WMStateCons.MSG_CELL_OUT_OF_SERVICE /*96*/:
                        LogUtil.i("Into MSG_CELL_CHANGE");
                        if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                            HwWMStateMachine.this.mCollectUserFingersHandler.updateMobileDurationForCell(true);
                            break;
                        }
                        break;
                    case WMStateCons.MSG_SIM_STATE_CHANGE /*94*/:
                        LogUtil.i("Into MSG_SIM_STATE_CHANGE");
                        if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                            HwWMStateMachine.this.mCollectUserFingersHandler.assignSpaceExp2Space(HwWMStateMachine.this.lastSpaceIds, true);
                            HwWMStateMachine.this.mCollectUserFingersHandler.setCurrScrbId();
                            break;
                        }
                        break;
                    default:
                        switch (i) {
                            case 110:
                                LogUtil.d("into MSG_AR_MOVE");
                                int unused2 = HwWMStateMachine.this.SUBSTATE_ACTIVITY = 2;
                                HwWMStateMachine.this.mActiveCollectHandler.stopOut4gRecgScan();
                                break;
                            case 111:
                                LogUtil.d("into MSG_AR_STATION");
                                int unused3 = HwWMStateMachine.this.SUBSTATE_ACTIVITY = 1;
                                break;
                            case 112:
                                LogUtil.d("into MSG_POWER_CONNECTED");
                                long unused4 = HwWMStateMachine.this.timePowerConnect = System.currentTimeMillis();
                                break;
                            case 113:
                                LogUtil.d("into MSG_POWER_DISCONNECTED");
                                long unused5 = HwWMStateMachine.this.timePowerConnect = 0;
                                HwWMStateMachine.this.setStopTrainModel();
                                break;
                            default:
                                switch (i) {
                                    case 200:
                                    case 201:
                                    case 202:
                                        HwWMStateMachine.this.appStateHandle(message.what, (HwWmpAppInfo) message.obj);
                                        break;
                                    default:
                                        LogUtil.i("into default, msg:" + message.what);
                                        break;
                                }
                        }
                }
            } else {
                LogUtil.d("into MSG_HIGH_TEMPERATRUE");
                HwWMStateMachine.this.setStopTrainModel();
            }
            return true;
        }

        public void exit() {
            LogUtil.d("exit DefaultState");
            HwWMStateMachine.this.mbehaviorReceiver.saveBatch();
            if (HwWMStateMachine.this.collectFingersHandler != null) {
                HwWMStateMachine.this.collectFingersHandler.mainApInfoToDb();
            }
        }
    }

    class FrequentLocationState extends State {
        public final String TAG = ("WMapping." + FrequentLocationState.class.getSimpleName());

        FrequentLocationState() {
        }

        public void enter() {
            LogUtil.d("enter mFrequentLocationState");
            HwWMStateMachine.this.wifiDataProvider.start();
            if (HwWMStateMachine.this.cur_place != null) {
                LogUtil.i("cur_place=" + HwWMStateMachine.this.cur_place.getPlace());
                HwWMStateMachine.this.collectFingersHandler.startCollect();
                HwWMStateMachine.this.mCellStateMonitor.startMonitor();
                return;
            }
            LogUtil.e(" empty current location, back to DefaultState");
            HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mDefaultState);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 50) {
                LogUtil.d("In to Other Location, MSG_OUT_FREQ_LOCATION");
                if (HwWMStateMachine.this.uiInfo != null) {
                    HwWMStateMachine.this.uiInfo.setFinger_batch_num(0);
                    HwWMStateMachine.this.uiInfo.setStage(0);
                    HwWMStateMachine.this.uiInfo.setSsid(Constant.NAME_FREQLOCATION_OTHER);
                }
                HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mDefaultState);
            } else if (i == 55) {
                LogUtil.i("In to Other Location, MSG_LEAVE_FREQ_LOCATION_TOOL");
                if (!(HwWMStateMachine.this.cur_place == null || HwWMStateMachine.this.cur_place.getPlace() == null)) {
                    int currLoc = 1;
                    if (HwWMStateMachine.this.cur_place.getPlace().equals(Constant.NAME_FREQLOCATION_HOME)) {
                        currLoc = 0;
                    }
                    HwWMStateMachine.this.mFrequentLocation.updateWaveMapping(currLoc, 1);
                }
            } else if (i == 60) {
                LogUtil.d("Into MSG_TOOL_FORCE_TRAINING, force training by tool");
                if (HwWMStateMachine.this.cur_place == null || HwWMStateMachine.this.cur_place.getPlace() == null) {
                    LogUtil.e(" current location == null");
                    HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mDefaultState);
                } else {
                    LogUtil.d("  force training by tool, current location = " + HwWMStateMachine.this.cur_place.getPlace());
                    HwWMStateMachine.this.trainModels(HwWMStateMachine.this.cur_place.getPlace());
                    HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mCollectTrainingState);
                }
            } else if (i == 62) {
                LogUtil.d("MSG_WIFI_UPDATE_SCAN_RESULT01, wifi scan complete");
                try {
                    List unused = HwWMStateMachine.this.wifiList = HwWMStateMachine.this.mWifiManager.getScanResults();
                    if (HwWMStateMachine.this.wifiList == null) {
                        LogUtil.d("MSG_WIFI_UPDATE_SCAN_RESULT, wifiList=null");
                    } else if (HwWMStateMachine.this.cur_place == null) {
                        LogUtil.d("MSG_WIFI_UPDATE_SCAN_RESULT, cur_place=null");
                    } else {
                        LogUtil.i("MSG_WIFI_UPDATE_SCAN_RESULT, cur_place=" + HwWMStateMachine.this.cur_place.toString() + ", wifiList.size=" + HwWMStateMachine.this.wifiList.size());
                        HwWMStateMachine.this.collectFingersHandler.processWifiData(HwWMStateMachine.this.wifiList, HwWMStateMachine.this.cur_place);
                    }
                } catch (Exception e) {
                    LogUtil.e("MSG_WIFI_UPDATE_SCAN_RESULT,e" + e.getMessage());
                }
            } else if (i != 141) {
                return false;
            } else {
                LogUtil.d("Into MSG_BACK_4G_COVERAGE, stop active scan timer");
                HwWMStateMachine.this.mActiveCollectHandler.stopOut4gRecgScan();
            }
            return true;
        }

        public void exit() {
            LogUtil.d("exit mFrequentLocationState");
            if (HwWMStateMachine.this.collectFingersHandler != null) {
                HwWMStateMachine.this.collectFingersHandler.mainApInfoToDb();
            }
            if (!HwWMStateMachine.this.collectFingersHandler.stopCollect()) {
                LogUtil.e(" Stop Collection Failure");
            }
            RegularPlaceInfo unused = HwWMStateMachine.this.cur_place = null;
            HwWMStateMachine.this.mCollectUserFingersHandler.assignSpaceExp2Space(HwWMStateMachine.this.lastSpaceIds, true);
            HwWMStateMachine.this.mCollectUserFingersHandler.setFreqLocation(Constant.NAME_FREQLOCATION_OTHER);
            HwWMStateMachine.this.mCollectUserFingersHandler.resetOut4GBeginTime();
            HwWMStateMachine.this.wifiDataProvider.stop();
            HwWMStateMachine.this.mbehaviorReceiver.saveBatch();
            if (HwWMStateMachine.this.getHandler().hasMessages(62)) {
                HwWMStateMachine.this.removeMessages(62);
            }
        }
    }

    class LocatingState extends State {
        public final String TAG = ("WMapping." + LocatingState.class.getSimpleName());

        LocatingState() {
        }

        public void enter() {
            LogUtil.d("enter mLocatingState");
            HwWMStateMachine.this.setCur_preLable(HwWMStateMachine.this.recognizeByMainAp());
            HwWMStateMachine.this.mCollectUserFingersHandler.assignSpaceExp2Space(HwWMStateMachine.this.lastSpaceIds, true);
            HwWMStateMachine.this.mCollectUserFingersHandler.recognizeActions(HwWMStateMachine.this.curSpaceIds, true);
            if (1 == HwWMStateMachine.this.SUBSTATE_LASTRECG) {
                HwWMStateMachine.this.mLocationDAO.accCHRSpaceLeavebyFreqLoc(HwWMStateMachine.this.cur_place.getPlace());
            }
            HwWMStateMachine.this.lastSpaceIds.copyResults(HwWMStateMachine.this.curSpaceIds);
            LogUtil.i(" save last results:" + HwWMStateMachine.this.lastSpaceIds.toString());
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 111) {
                if (i == 120) {
                    LogUtil.d("into MSG_QUERY_HISQOE, query saved history App QoE");
                    Bundle bundle = message.getData();
                    if (bundle == null || !bundle.containsKey("FULLID")) {
                        LogUtil.w(" no bundle");
                        IWaveMappingCallback cb = (IWaveMappingCallback) message.obj;
                        if (cb != null) {
                            IWaveMappingCallback iWaveMappingCallback = cb;
                            iWaveMappingCallback.onWaveMappingRespondCallback(0, 0, 0, true, false);
                            iWaveMappingCallback.onWaveMappingRespond4BackCallback(0, 0, 0, true, false);
                        }
                    } else {
                        HwWMStateMachine.this.mCollectUserFingersHandler.queryAppQoebyTargetNw(HwWMStateMachine.this.curSpaceIds, bundle.getInt("FULLID"), bundle.getInt("UID"), bundle.getInt("NW"), (IWaveMappingCallback) message.obj, bundle.getInt("DIRECT"));
                    }
                } else if (i != 140) {
                    switch (i) {
                        case 115:
                            LogUtil.d("Into MSG_USER_DATA_ACTION");
                            HwWMStateMachine.this.mCollectUserFingersHandler.recognizeActions(HwWMStateMachine.this.curSpaceIds, true);
                            return false;
                        case 116:
                            LogUtil.d("into MSG_USER_NEED_FURTHER_SPACE, trigger scan to get allAp space");
                            HwWMStateMachine.this.mActiveCollectHandler.startFurtherSpaceScan();
                            break;
                        default:
                            return false;
                    }
                } else {
                    boolean isScreenOff = true;
                    if (HwWMStateMachine.this.mPowerManager != null) {
                        isScreenOff = !HwWMStateMachine.this.mPowerManager.isInteractive();
                    }
                    LogUtil.d("into MSG_CHECK_4G_COVERAGE, check 4G coverage by mainAp result: ACT_STATE=" + HwWMStateMachine.this.SUBSTATE_ACTIVITY + ", isScreenOff=" + isScreenOff);
                    try {
                        if (2 != HwWMStateMachine.this.SUBSTATE_ACTIVITY) {
                            if (!isScreenOff) {
                                if (1 == HwWMStateMachine.this.SUBSTATE_ACTIVITY) {
                                    if (!HwWMStateMachine.this.mCollectUserFingersHandler.determine4gCoverage(HwWMStateMachine.this.curSpaceIds)) {
                                        HwWMStateMachine.this.mActiveCollectHandler.startOut4gRecgScan();
                                    }
                                    HwWMStateMachine.this.mCollectUserFingersHandler.checkOutOf4GCoverage(true);
                                }
                            }
                        }
                        HwWMStateMachine.this.mActiveCollectHandler.stopOut4gRecgScan();
                    } catch (Exception e) {
                        LogUtil.e("WMStateCons.MSG_RECOGNITIONSTATE_WIFI_UPDATE_SCAN_RESULT,LocatingState,e:" + e.getMessage());
                    }
                }
                return true;
            }
            LogUtil.d("into MSG_AR_STATION, stand in a certain space");
            if (2 == HwWMStateMachine.this.SUBSTATE_ACTIVITY) {
                HwWMStateMachine.this.mCollectUserFingersHandler.checkOutOf4GCoverage(false);
                HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mLocatingState);
            }
            return false;
        }

        public void exit() {
            LogUtil.d("exit mLocatingState");
            int unused = HwWMStateMachine.this.SUBSTATE_LASTRECG = 1;
        }
    }

    class PositionState extends State {
        public final String TAG = ("WMapping." + PositionState.class.getSimpleName());

        PositionState() {
        }

        public void enter() {
            LogUtil.d("enter mPositionState");
            HwWMStateMachine.this.mCollectUserFingersHandler.recognizeActions(HwWMStateMachine.this.curSpaceIds, HwWMStateMachine.this.curSpaceIds.getRgResult() != HwWMStateMachine.this.lastSpaceIds.getRgResult());
            HwWMStateMachine.this.mActiveCollectHandler.stopOut4gRecgScan();
            HwWMStateMachine.this.mCollectUserFingersHandler.checkOutOf4GCoverage(false);
            HwWMStateMachine.this.mLocationDAO.accCHRSpaceChangebyFreqLoc(HwWMStateMachine.this.cur_place.getPlace());
            HwWMStateMachine.this.lastSpaceIds.copyResults(HwWMStateMachine.this.curSpaceIds);
            LogUtil.d(" save last results:" + HwWMStateMachine.this.lastSpaceIds.toString());
        }

        public boolean processMessage(Message message) {
            Message message2 = message;
            int i = message2.what;
            if (i != 110) {
                if (i == 120) {
                    LogUtil.d("into MSG_QUERY_HISQOE, query saved history App QoE");
                    Bundle bundle = message.getData();
                    if (bundle == null || !bundle.containsKey("FULLID")) {
                        LogUtil.w(" no bundle");
                        IWaveMappingCallback cb = (IWaveMappingCallback) message2.obj;
                        if (cb != null) {
                            IWaveMappingCallback iWaveMappingCallback = cb;
                            iWaveMappingCallback.onWaveMappingRespondCallback(0, 0, 0, true, false);
                            iWaveMappingCallback.onWaveMappingRespond4BackCallback(0, 0, 0, true, false);
                        }
                    } else {
                        HwWMStateMachine.this.mCollectUserFingersHandler.queryAppQoebyTargetNw(HwWMStateMachine.this.curSpaceIds, bundle.getInt("FULLID"), bundle.getInt("UID"), bundle.getInt("NW"), (IWaveMappingCallback) message2.obj, bundle.getInt("DIRECT"));
                    }
                } else if (i == 140) {
                    boolean isScreenOff = true;
                    if (HwWMStateMachine.this.mPowerManager != null) {
                        isScreenOff = !HwWMStateMachine.this.mPowerManager.isInteractive();
                    }
                    LogUtil.d("into MSG_CHECK_4G_COVERAGE, check 4G coverage by current results: isScreenOff=" + isScreenOff);
                    if (!isScreenOff && HwWMStateMachine.this.mCollectUserFingersHandler.determine4gCoverage(HwWMStateMachine.this.lastSpaceIds)) {
                        HwWMStateMachine.this.mCollectUserFingersHandler.checkOutOf4GCoverage(true);
                    }
                } else if (i != 200) {
                    return false;
                } else {
                    LogUtil.d("Into MSG_APP_STATE_START");
                    HwWmpAppInfo mAppInfo = (HwWmpAppInfo) message2.obj;
                    if (mAppInfo == null) {
                        LogUtil.e(" no app messages");
                    } else {
                        if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                            HwWMStateMachine.this.mCollectUserFingersHandler.determineAppQoePreference(HwWMStateMachine.this.curSpaceIds, mAppInfo);
                        }
                        return false;
                    }
                }
                return true;
            }
            LogUtil.d("into MSG_AR_MOVE, user go to other space");
            HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mLocatingState);
            return false;
        }

        public void exit() {
            LogUtil.d("exit mPositionState");
            if (HwWMStateMachine.this.curSpaceIds.getRgResult() != 0) {
                HwWMStateMachine.this.mCollectUserFingersHandler.assignSpaceExp2Space(HwWMStateMachine.this.lastSpaceIds, HwWMStateMachine.this.curSpaceIds.getRgResult() != HwWMStateMachine.this.lastSpaceIds.getRgResult());
            }
            int unused = HwWMStateMachine.this.SUBSTATE_LASTRECG = 1;
        }
    }

    class RecognitionState extends State {
        public final String TAG = ("WMapping." + RecognitionState.class.getSimpleName());
        private RecognizeService mRecognizeService;

        RecognitionState() {
        }

        public void enter() {
            LogUtil.d("enter mRecognitionState");
            if (HwWMStateMachine.this.modelService != null) {
                this.mRecognizeService = new RecognizeService(HwWMStateMachine.this.modelService);
                return;
            }
            LogUtil.d("enter mLocatingState,null == modelService");
            HwWMStateMachine.this.modelService = ModelService5.getInstance(HwWMStateMachine.this.getHandler());
            this.mRecognizeService = new RecognizeService(HwWMStateMachine.this.modelService);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 63) {
                LogUtil.d("MSG_WIFI_UPDATE_SCAN_RESULT, wifi scan complete");
                try {
                    if (this.mRecognizeService == null) {
                        LogUtil.e(" null == recognizeService");
                    } else {
                        RecognizeResult recognizeResult = this.mRecognizeService.identifyLocation(HwWMStateMachine.this.cur_place, HwWMStateMachine.this.cur_mainApPlaceInfo, HwWMStateMachine.this.wifiList);
                        if (recognizeResult.getRgResult() <= 0) {
                            LogUtil.d(" recognizeResult failure, result = unknonw");
                            HwWMStateMachine.this.setCur_preLable(recognizeResult);
                            UiService.getUiInfo().setToast("RecognitionState.");
                            UiService unused = HwWMStateMachine.this.uiService;
                            UiService.sendMsgToUi();
                            HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mLocatingState);
                        } else {
                            LogUtil.d(" found the space:" + recognizeResult.toString());
                            if (HwWMStateMachine.this.curSpaceIds.cmpResults(recognizeResult)) {
                                LogUtil.i(" new space");
                                HwWMStateMachine.this.setCur_preLable(recognizeResult);
                                HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mPositionState);
                            }
                            UiService.getUiInfo().setToast("RecognitionState.");
                            UiInfo uiInfo = UiService.getUiInfo();
                            uiInfo.setPreLabel(recognizeResult.getRgResult() + "_" + recognizeResult.getMainApRgResult());
                            UiService unused2 = HwWMStateMachine.this.uiService;
                            UiService.sendMsgToUi();
                        }
                    }
                } catch (Exception e) {
                    LogUtil.e("WMStateCons.MSG_RECOGNITIONSTATE_WIFI_UPDATE_SCAN_RESULT,LocatingState,e:" + e.getMessage());
                }
            } else if (i == 93 || i == 95) {
                LogUtil.i("Into MSG_CELL_CHANGE");
                HwWMStateMachine.this.check4gCoverage();
            } else if (i != 100) {
                return false;
            } else {
                LogUtil.d("into MSG_MODEL_UNQUALIFIED, space model too old");
                if (HwWMStateMachine.this.cur_place == null || HwWMStateMachine.this.cur_place.getPlace() == null) {
                    LogUtil.e(" current location == null");
                    HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mDefaultState);
                } else {
                    LogUtil.d("    current location = " + HwWMStateMachine.this.cur_place.getPlace());
                    HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mCollectTrainingState);
                }
            }
            return true;
        }

        public void exit() {
            LogUtil.d("exit mRecognitionState");
            HwWMStateMachine.this.mActiveCollectHandler.stopOut4gRecgScan();
            if (HwWMStateMachine.this.getHandler().hasMessages(63)) {
                HwWMStateMachine.this.removeMessages(63);
            }
        }
    }

    public static HwWMStateMachine getInstance(Context context) {
        if (hwWMStateMachine == null) {
            hwWMStateMachine = new HwWMStateMachine(context);
        }
        return hwWMStateMachine;
    }

    private HwWMStateMachine(Context context) {
        super("HwWMStateMachine");
        LogUtil.d("HwWMStateMachine  start..");
        this.mCtx = context;
        Context context2 = this.mCtx;
        Context context3 = this.mCtx;
        this.mWifiManager = (WifiManager) context2.getSystemService(DevSchedFeatureRT.WIFI_FEATURE);
        addState(this.mDefaultState);
        addState(this.mFrequentLocationState, this.mDefaultState);
        addState(this.mCollectTrainingState, this.mFrequentLocationState);
        addState(this.mRecognitionState, this.mFrequentLocationState);
        addState(this.mPositionState, this.mRecognitionState);
        addState(this.mLocatingState, this.mRecognitionState);
        setInitialState(this.mDefaultState);
        start();
    }

    public Handler getStateMachineHandler() {
        return getHandler();
    }

    public void init() {
        LogUtil.d("init begin.");
        try {
            Context context = this.mCtx;
            Context context2 = this.mCtx;
            this.mPowerManager = (PowerManager) context.getSystemService("power");
            this.param = ParamManager.getInstance().getParameterInfo();
            this.uiService = new UiService(getHandler());
            UiService uiService2 = this.uiService;
            this.uiInfo = UiService.getUiInfo();
            this.modelService = ModelService5.getInstance(getHandler());
            this.wifiDataProvider = WifiDataProvider.getInstance(this.mCtx, getHandler());
            this.mbehaviorReceiver = new BehaviorReceiver(getHandler());
            this.mActiveCollectHandler = new ActiveCollectDecision(this.mCtx, this.mbehaviorReceiver);
            this.mCellStateMonitor = new CellStateMonitor(this.mCtx, getHandler());
            this.regularPlaceDAO = new RegularPlaceDAO();
            if (this.rgLocations == null || this.rgLocations.size() == 0) {
                this.rgLocations = this.regularPlaceDAO.findAllLocations();
                for (Map.Entry entry : this.rgLocations.entrySet()) {
                    String str = (String) entry.getKey();
                    LogUtil.d("rgLocations val:" + ((RegularPlaceInfo) entry.getValue()).toString());
                }
            }
            if (!cleanExpireMobileApEnterpriseAps()) {
                Log.e(TAG, " cleanExpireMobileApEnterpriseAps failure.");
            }
            this.mCollectUserFingersHandler = CollectUserFingersHandler.getInstance(getHandler());
            if (this.mCollectUserFingersHandler != null) {
                this.mCollectUserFingersHandler.startCollect();
            }
            this.mHwWmpCallbackImpl = HwWmpCallbackImpl.getInstance(getHandler());
            this.mAPPQoEManager = HwAPPQoEManager.createHwAPPQoEManager(this.mCtx);
            this.mAPPQoEManager.registerAppQoECallback(this.mHwWmpCallbackImpl, false);
            this.mFrequentLocation = FrequentLocation.getInstance(getHandler());
            if (this.mFrequentLocation != null && !this.mFrequentLocation.isConnected()) {
                this.mFrequentLocation.connectService(this.mCtx);
            }
            this.initFinish = true;
            if (this.mFrequentLocation != null) {
                this.mFrequentLocation.queryFrequentLocationState();
            }
            this.mLocationDAO = new LocationDAO();
            this.mbehaviorReceiver.uploadBenefitCHR();
            this.mbehaviorReceiver.uploadSpaceUserCHR();
            this.checkTempertureUtil = CheckTempertureUtil.getInstance(getHandler());
        } catch (Exception e) {
            LogUtil.e("init exception." + e.getMessage());
        }
    }

    public void handleShutDown() {
        sendMessage(130);
    }

    private boolean cleanExpireMobileApEnterpriseAps() {
        try {
            MobileApDAO mobileApDAO = new MobileApDAO();
            EnterpriseApDAO enterpriseApDAO = new EnterpriseApDAO();
            List<ApInfo> mobileAps = mobileApDAO.findAllAps();
            TimeUtil timeUtil = new TimeUtil();
            String updataDate = timeUtil.getSomeDay(new Date(), -30);
            if (mobileAps.size() > 0) {
                for (ApInfo apInfo : mobileAps) {
                    String lastDate = timeUtil.changeDateFormat(apInfo.getUptime());
                    if (lastDate == null || updataDate.compareTo(lastDate) > 0) {
                        mobileApDAO.remove(apInfo.getSsid(), apInfo.getMac());
                    }
                }
            }
            List<ApInfo> enterpriseAps = enterpriseApDAO.findAllAps();
            if (enterpriseAps.size() > 0) {
                for (ApInfo apInfo2 : enterpriseAps) {
                    String lastDate2 = timeUtil.changeDateFormat(apInfo2.getUptime());
                    if (lastDate2 == null || updataDate.compareTo(lastDate2) > 0) {
                        enterpriseApDAO.remove(apInfo2.getSsid());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.e("cleanExpireMobileApEnterpriseAps:" + e);
            return false;
        }
    }

    public void getCur_place(String freqLoc) {
        if (freqLoc == null) {
            LogUtil.d("getCur_place: null == curLocation");
            return;
        }
        RegularPlaceInfo placeInfo = this.regularPlaceDAO.findBySsid(freqLoc, false);
        if (placeInfo == null) {
            RegularPlaceInfo regularPlaceInfo = new RegularPlaceInfo(freqLoc, 3, 1, 0, 0, 0, 0, "", false);
            placeInfo = regularPlaceInfo;
            if (!this.regularPlaceDAO.insert(placeInfo)) {
                LogUtil.e(" insert into common current place Failure.");
            }
        }
        this.cur_place = placeInfo;
        LogUtil.d("getCur_place, cur_place:" + this.cur_place.toString());
        RegularPlaceInfo mainApPlaceInfo = this.regularPlaceDAO.findBySsid(freqLoc, true);
        if (mainApPlaceInfo == null) {
            RegularPlaceInfo regularPlaceInfo2 = new RegularPlaceInfo(freqLoc, 3, 1, 0, 0, 0, 0, "", true);
            mainApPlaceInfo = regularPlaceInfo2;
            if (!this.regularPlaceDAO.insert(mainApPlaceInfo)) {
                LogUtil.e(" insert into mainAp current place Failure.");
            }
        }
        this.cur_mainApPlaceInfo = mainApPlaceInfo;
        LogUtil.d("getCur_place,mainApPlaceInfo:" + this.cur_mainApPlaceInfo.toString());
        if (this.mCollectUserFingersHandler != null) {
            this.mCollectUserFingersHandler.setModelVer(this.cur_place.getModelName(), this.cur_mainApPlaceInfo.getModelName());
        }
    }

    /* access modifiers changed from: private */
    public void setCur_preLable(RecognizeResult newResults) {
        this.curSpaceIds = new RecognizeResult();
        if (newResults == null) {
            LogUtil.w("setCur_preLable failure: null");
        } else {
            this.curSpaceIds.copyResults(newResults);
        }
        if (this.curSpaceIds.getRgResult() < 0) {
            this.curSpaceIds.setRgResult(0);
        }
        if (this.curSpaceIds.getMainApRgResult() < 0) {
            this.curSpaceIds.setMainApRgResult(0);
        }
        LogUtil.d(" setCur_preLable: mainAp=" + this.curSpaceIds.getMainApRgResult() + ", allAp=" + this.curSpaceIds.getRgResult());
        UiInfo uiInfo2 = UiService.getUiInfo();
        uiInfo2.setPreLabel(this.curSpaceIds.getRgResult() + "_" + this.curSpaceIds.getMainApRgResult());
    }

    /* access modifiers changed from: private */
    public RecognizeResult recognizeByMainAp() {
        LogUtil.i("recognizeByMainAp");
        RecognizeResult recognizeResult = new RecognizeResult();
        recognizeResult.setAllApModelName(this.cur_place.getModelName());
        recognizeResult.setMainApModelName(this.cur_mainApPlaceInfo.getModelName());
        try {
            RecognizeResult mainResult = new RecognizeService(this.modelService).identifyLocationByMainAp(this.cur_mainApPlaceInfo);
            if (mainResult == null) {
                LogUtil.d(" mainAp recognizeResult failure, recognizeResult = null");
            } else if (mainResult.getMainApRgResult() == 0) {
                LogUtil.d(" mainAp recognizeResult failure, result == unknonw");
            } else {
                LogUtil.d(" found the mainAp space:" + mainResult.toString());
                recognizeResult.setMainApRgResult(mainResult.getMainApRgResult());
                recognizeResult.setMainApModelName(mainResult.getMainApModelName());
            }
        } catch (Exception e) {
            LogUtil.e("WMStateCons.MSG_RECOGNITIONSTATE_WIFI_UPDATE_SCAN_RESULT,LocatingState,e:" + e.getMessage());
        }
        return recognizeResult;
    }

    /* access modifiers changed from: private */
    public void updateUserSpaceDbForOtherLoc(boolean notCheckPeriod) {
        LogUtil.i("updateUserSpaceDbForOtherLoc");
        boolean isOtherLoc = false;
        if (this.cur_place == null) {
            isOtherLoc = true;
        } else if (this.cur_place.getPlace() == null) {
            isOtherLoc = true;
        } else if (this.cur_place.getPlace().equals(Constant.NAME_FREQLOCATION_OTHER)) {
            isOtherLoc = true;
        }
        if (isOtherLoc) {
            long now = System.currentTimeMillis();
            if ((now - this.mUpdateDBOtherTime > 86400000 || notCheckPeriod) && this.mCollectUserFingersHandler != null) {
                this.mCollectUserFingersHandler.assignSpaceExp2Space(new RecognizeResult(), true);
            }
            this.mUpdateDBOtherTime = now;
        }
    }

    /* access modifiers changed from: private */
    public void setStopTrainModel() {
        HwHidataJniAdapter hwHidataJniAdapter = HwHidataJniAdapter.getInstance();
        if (hwHidataJniAdapter != null) {
            hwHidataJniAdapter.nativeSetIsStop(true);
        }
    }

    /* access modifiers changed from: private */
    public void addFreqLocByTool(Bundle inputLloc) {
        if (inputLloc == null || inputLloc.get("LOCATION") == null) {
            LogUtil.w(" no bundle location");
            return;
        }
        String location = inputLloc.get("LOCATION").toString();
        int freqLoc = -1;
        if (location.equals(Constant.NAME_FREQLOCATION_HOME)) {
            freqLoc = 0;
        } else if (location.equals(Constant.NAME_FREQLOCATION_OFFICE)) {
            freqLoc = 1;
        }
        LogUtil.d("MSG_ADD_FREQ_LOCATION_TOOL,curLocation=" + location);
        this.mFrequentLocation.updateWaveMapping(freqLoc, 0);
    }

    /* access modifiers changed from: private */
    public void appStateHandle(int appState, HwWmpAppInfo appInfo) {
        if (appInfo == null) {
            LogUtil.e(" no app messages");
            return;
        }
        if (this.mCollectUserFingersHandler != null) {
            switch (appState) {
                case 200:
                    LogUtil.d("Into MSG_APP_STATE_START");
                    this.mCollectUserFingersHandler.startAppCollect(appInfo);
                    break;
                case 201:
                    LogUtil.d("Into MSG_APP_STATE_END");
                    this.mCollectUserFingersHandler.endAppCollect(appInfo);
                    break;
                case 202:
                    LogUtil.d("Into MSG_APP_STATE_NWUPDATE");
                    this.mCollectUserFingersHandler.updateAppNetwork(appInfo.getAppName(), appInfo.getConMgrNetworkType());
                    break;
                default:
                    LogUtil.d("Not Handle");
                    break;
            }
        }
    }

    /* access modifiers changed from: private */
    public void queryHistoryQoe(Bundle appInfo, IWaveMappingCallback cb) {
        if (appInfo == null || !appInfo.containsKey("FULLID")) {
            LogUtil.w(" no bundle");
            if (cb != null) {
                cb.onWaveMappingRespondCallback(0, 0, 0, true, false);
                cb.onWaveMappingRespond4BackCallback(0, 0, 0, true, false);
            }
            return;
        }
        int i = appInfo.getInt("FULLID");
        int UID = appInfo.getInt("UID");
        int i2 = appInfo.getInt("NW");
        int ArbNet = appInfo.getInt("ArbNW");
        int direction = appInfo.getInt("DIRECT");
        if (cb != null) {
            if (1 == direction) {
                cb.onWaveMappingRespondCallback(UID, 0, ArbNet, true, false);
            } else if (2 == direction) {
                cb.onWaveMappingRespond4BackCallback(UID, 0, ArbNet, true, false);
            }
        }
    }

    public void trainModels(String freqLoc) {
        if (this.modelService == null) {
            LogUtil.e("trainModels null == modelService");
        } else if (freqLoc == null || freqLoc.equals("")) {
            LogUtil.e("trainModels null == freqLoc");
        } else if (this.checkTempertureUtil == null) {
            LogUtil.e("trainModels null == checkTempertureUtil");
        } else if (this.checkTempertureUtil.exceedMaxTemperture()) {
            LogUtil.d("trainModels exceedMaxTemperture == true");
        } else {
            Handler modelServiceHandler = this.modelService.getmHandler();
            if (modelServiceHandler == null) {
                LogUtil.e("trainModels null == modelServiceHandler");
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString(Constant.NAME_FREQLACATION, freqLoc);
            Message buildModelMsg = Message.obtain(modelServiceHandler, 2);
            buildModelMsg.setData(bundle);
            modelServiceHandler.sendMessage(buildModelMsg);
            this.lastTrainingTime = System.currentTimeMillis();
        }
    }

    public boolean getBuildModelResult() {
        if (this.modelService == null) {
            LogUtil.e("getBuildModelResult null == modelService");
            return false;
        }
        ClusterResult result = this.modelService.getClusterResult();
        if (result == null || result.getPlace() == null || result.getPlace().equals("")) {
            return false;
        }
        LogUtil.d(" ,startTraining result:" + result.toString());
        UiInfo uiInfo2 = UiService.getUiInfo();
        uiInfo2.setCluster_num(result.getCluster_num() + "_" + result.getMainAp_cluster_num());
        UiService uiService2 = this.uiService;
        UiService.sendMsgToUi();
        if (result.getCluster_num() <= 0) {
            LogUtil.d(" getCluster_num ,still unqualified, return to CollectTrainingState");
            return false;
        } else if (this.cur_place == null) {
            LogUtil.d(" cur_place == null, return to CollectTrainingState");
            return false;
        } else {
            getCur_place(this.cur_place.getPlace());
            UiService.getUiInfo().setStage(4);
            UiService uiService3 = this.uiService;
            UiService.sendMsgToUi();
            LogUtil.d(" update current location success, train model OK, location :" + result.getPlace());
            return true;
        }
    }

    /* access modifiers changed from: private */
    public void check4gCoverage() {
        try {
            if (this.mCollectUserFingersHandler != null && this.mCollectUserFingersHandler.updateMobileDurationForCell(true)) {
                this.mCollectUserFingersHandler.checkOutOf4GCoverage(true);
            }
        } catch (Exception e) {
            LogUtil.e("check4gCoverage,e:" + e.getMessage());
        }
    }
}
