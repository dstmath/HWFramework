package com.android.server.hidata.wavemapping;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.hidata.appqoe.HwAppQoeManager;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.cons.WMStateCons;
import com.android.server.hidata.wavemapping.dao.EnterpriseApDao;
import com.android.server.hidata.wavemapping.dao.LocationDao;
import com.android.server.hidata.wavemapping.dao.MobileApDao;
import com.android.server.hidata.wavemapping.dao.RegularPlaceDao;
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
import com.android.server.hidata.wavemapping.util.CheckTemperatureUtil;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.TimeUtil;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwWMStateMachine extends StateMachine {
    private static final int INVALID_VALUE = -1;
    private static final String KEY_ARB_NETWORK = "ArbNW";
    private static final String KEY_DIRECT = "DIRECT";
    private static final String KEY_FULL_ID = "FULLID";
    private static final String KEY_LOCATION = "LOCATION";
    private static final String KEY_NETWORK = "NW";
    private static final String KEY_UID = "UID";
    private static final long POWER_CONNECT_DELAY = 300000;
    public static final String TAG = ("WMapping." + HwWMStateMachine.class.getSimpleName());
    private static final Object lock = new Object();
    private static HwWMStateMachine stateMachine;
    private CollectFingersHandler collectFingersHandler;
    private RegularPlaceInfo curMainApPlaceInfo = null;
    private RegularPlaceInfo curPlace = null;
    private RecognizeResult curSpaceIds = new RecognizeResult();
    private boolean isInitFinished = false;
    private RecognizeResult lastSpaceIds = new RecognizeResult();
    private ActiveCollectDecision mActiveCollectHandler = null;
    private HwAppQoeManager mAppQoeManager = null;
    private BehaviorReceiver mBehaviorReceiver;
    private CellStateMonitor mCellStateMonitor = null;
    private CheckTemperatureUtil mCheckTemperatureUtil = null;
    private CollectTrainingState mCollectTrainingState = new CollectTrainingState();
    private CollectUserFingersHandler mCollectUserFingersHandler = null;
    private Context mCtx;
    private State mDefaultState = new DefaultState();
    private FrequentLocation mFrequentLocation;
    private FrequentLocationState mFrequentLocationState = new FrequentLocationState();
    private HwWmpCallbackImpl mHwWmpCallbackImpl = null;
    private long mLastTrainingTime = 0;
    private LocatingState mLocatingState = new LocatingState();
    private LocationDao mLocationDao;
    private PositionState mPositionState = new PositionState();
    private long mPowerConnectTime = 0;
    private RecognitionState mRecognitionState = new RecognitionState();
    private long mUpdateDbOtherTime = 0;
    private WifiManager mWifiManager;
    private ModelService5 modelService = null;
    private ParameterInfo param;
    private RegularPlaceDao regularPlaceDao;
    private HashMap<String, RegularPlaceInfo> rgLocations;
    private int substateActivity = 1;
    private int substateLastrecg = 0;
    private UiInfo uiInfo;
    private UiService uiService;
    private WifiDataProvider wifiDataProvider;
    private List<ScanResult> wifiList = null;

    private HwWMStateMachine(Context context) {
        super("HwWMStateMachine");
        LogUtil.d(false, "HwWMStateMachine start..", new Object[0]);
        this.mCtx = context;
        this.mWifiManager = (WifiManager) this.mCtx.getSystemService("wifi");
        addState(this.mDefaultState);
        addState(this.mFrequentLocationState, this.mDefaultState);
        addState(this.mCollectTrainingState, this.mFrequentLocationState);
        addState(this.mRecognitionState, this.mFrequentLocationState);
        addState(this.mPositionState, this.mRecognitionState);
        addState(this.mLocatingState, this.mRecognitionState);
        setInitialState(this.mDefaultState);
        start();
    }

    public static HwWMStateMachine getInstance(Context context) {
        if (stateMachine == null) {
            synchronized (lock) {
                if (stateMachine == null) {
                    stateMachine = new HwWMStateMachine(context);
                }
            }
        }
        return stateMachine;
    }

    public Handler getStateMachineHandler() {
        return getHandler();
    }

    public void init() {
        LogUtil.d(false, "init begin.", new Object[0]);
        try {
            this.param = ParamManager.getInstance().getParameterInfo();
            this.uiService = new UiService(getHandler());
            UiService uiService2 = this.uiService;
            this.uiInfo = UiService.getUiInfo();
            this.modelService = ModelService5.getInstance(this.mCtx, getHandler());
            this.wifiDataProvider = WifiDataProvider.getInstance(this.mCtx, getHandler());
            this.mBehaviorReceiver = new BehaviorReceiver(getHandler());
            this.mActiveCollectHandler = new ActiveCollectDecision(this.mCtx, this.mBehaviorReceiver);
            this.mCellStateMonitor = new CellStateMonitor(this.mCtx, getHandler());
            this.regularPlaceDao = new RegularPlaceDao();
            if (this.rgLocations == null || this.rgLocations.size() == 0) {
                this.rgLocations = this.regularPlaceDao.findAllLocations();
                for (Map.Entry<String, RegularPlaceInfo> entry : this.rgLocations.entrySet()) {
                    entry.getKey();
                    LogUtil.d(false, "rgLocations val:%{private}s", entry.getValue().toString());
                }
            }
            if (!cleanExpireMobileApEnterpriseAps()) {
                HwHiLog.e(TAG, false, " cleanExpireMobileApEnterpriseAps failure.", new Object[0]);
            }
            this.mCollectUserFingersHandler = CollectUserFingersHandler.getInstance(getHandler());
            this.mHwWmpCallbackImpl = HwWmpCallbackImpl.getInstance(getHandler());
            this.mAppQoeManager = HwAppQoeManager.createHwAppQoeManager(this.mCtx);
            this.mAppQoeManager.registerAppQoeCallback(this.mHwWmpCallbackImpl, false);
            this.mFrequentLocation = FrequentLocation.getInstance(getHandler());
            this.isInitFinished = true;
            if (this.mFrequentLocation != null) {
                this.mFrequentLocation.queryFrequentLocationState();
            }
            this.mLocationDao = new LocationDao();
            this.mCheckTemperatureUtil = CheckTemperatureUtil.getInstance(getHandler());
        } catch (Exception e) {
            LogUtil.e(false, "init failed by Exception", new Object[0]);
        }
    }

    public void handleShutDown() {
        sendMessage(130);
    }

    private boolean cleanExpireMobileApEnterpriseAps() {
        try {
            MobileApDao mobileApDao = new MobileApDao();
            EnterpriseApDao enterpriseApDao = new EnterpriseApDao();
            List<ApInfo> mobileAps = mobileApDao.findAllAps();
            TimeUtil timeUtil = new TimeUtil();
            String updataDate = timeUtil.getSomeDay(new Date(), -30);
            if (mobileAps.size() > 0) {
                for (ApInfo apInfo : mobileAps) {
                    String lastDate = timeUtil.changeDateFormat(apInfo.getUptime());
                    if (lastDate == null || updataDate.compareTo(lastDate) > 0) {
                        mobileApDao.remove(apInfo.getSsid(), apInfo.getMac());
                    }
                }
            }
            List<ApInfo> enterpriseAps = enterpriseApDao.findAllAps();
            if (enterpriseAps.size() <= 0) {
                return true;
            }
            for (ApInfo apInfo2 : enterpriseAps) {
                String lastDate2 = timeUtil.changeDateFormat(apInfo2.getUptime());
                if (lastDate2 == null || updataDate.compareTo(lastDate2) > 0) {
                    enterpriseApDao.remove(apInfo2.getSsid());
                }
            }
            return true;
        } catch (Exception e) {
            LogUtil.e(false, "cleanExpireMobileApEnterpriseAps failed by Exception", new Object[0]);
            return false;
        }
    }

    public void getCurrentPlace(String freqLoc) {
        if (freqLoc == null) {
            LogUtil.d(false, "getCurrentPlace: curLocation == null", new Object[0]);
            return;
        }
        RegularPlaceInfo placeInfo = this.regularPlaceDao.findBySsid(freqLoc, false);
        if (placeInfo == null) {
            placeInfo = new RegularPlaceInfo(freqLoc, 3, 1, 0, 0, 0, 0, "", false);
            if (!this.regularPlaceDao.insert(placeInfo)) {
                LogUtil.e(false, " insert into common current place Failure.", new Object[0]);
            }
        }
        this.curPlace = placeInfo;
        LogUtil.d(false, "getCurrentPlace, curPlace:%{private}s", this.curPlace.toString());
        RegularPlaceInfo mainApPlaceInfo = this.regularPlaceDao.findBySsid(freqLoc, true);
        if (mainApPlaceInfo == null) {
            mainApPlaceInfo = new RegularPlaceInfo(freqLoc, 3, 1, 0, 0, 0, 0, "", true);
            if (!this.regularPlaceDao.insert(mainApPlaceInfo)) {
                LogUtil.e(false, " insert into mainAp current place Failure.", new Object[0]);
            }
        }
        this.curMainApPlaceInfo = mainApPlaceInfo;
        LogUtil.d(false, "getCurrentPlace,mainApPlaceInfo:%{private}s", this.curMainApPlaceInfo.toString());
        CollectUserFingersHandler collectUserFingersHandler = this.mCollectUserFingersHandler;
        if (collectUserFingersHandler != null) {
            collectUserFingersHandler.setModelVer(this.curPlace.getModelName(), this.curMainApPlaceInfo.getModelName());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setCurrentPreLable(RecognizeResult newResults) {
        this.curSpaceIds = new RecognizeResult();
        if (newResults == null) {
            LogUtil.w(false, "setCurrentPreLable failure: null", new Object[0]);
        } else {
            this.curSpaceIds.copyResults(newResults);
        }
        if (this.curSpaceIds.getRgResult() < 0) {
            this.curSpaceIds.setRgResult(0);
        }
        if (this.curSpaceIds.getMainApRgResult() < 0) {
            this.curSpaceIds.setMainApRgResult(0);
        }
        LogUtil.d(false, " setCurrentPreLable: mainAp=%{public}d, allAp=%{public}d", Integer.valueOf(this.curSpaceIds.getMainApRgResult()), Integer.valueOf(this.curSpaceIds.getRgResult()));
        UiInfo uiInfo2 = UiService.getUiInfo();
        uiInfo2.setPreLabel(this.curSpaceIds.getRgResult() + "_" + this.curSpaceIds.getMainApRgResult());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private RecognizeResult recognizeByMainAp() {
        LogUtil.i(false, "recognizeByMainAp", new Object[0]);
        RecognizeResult recognizeResult = new RecognizeResult();
        recognizeResult.setAllApModelName(this.curPlace.getModelName());
        recognizeResult.setMainApModelName(this.curMainApPlaceInfo.getModelName());
        try {
            RecognizeResult mainResult = new RecognizeService(this.modelService).identifyLocationByMainAp(this.curMainApPlaceInfo);
            if (mainResult == null) {
                LogUtil.d(false, " mainAp recognizeResult failure, recognizeResult = null", new Object[0]);
            } else if (mainResult.getMainApRgResult() == 0) {
                LogUtil.d(false, " mainAp recognizeResult failure, result == unknonw", new Object[0]);
            } else {
                LogUtil.d(false, " found the mainAp space:%{public}s", mainResult.toString());
                recognizeResult.setMainApRgResult(mainResult.getMainApRgResult());
                recognizeResult.setMainApModelName(mainResult.getMainApModelName());
            }
        } catch (Exception e) {
            LogUtil.e(false, "recognizeByMainAp failed by Exception", new Object[0]);
        }
        return recognizeResult;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateUserSpaceDbForOtherLoc(boolean isCheckPeriod) {
        LogUtil.i(false, "updateUserSpaceDbForOtherLoc", new Object[0]);
        boolean isOtherLoc = false;
        RegularPlaceInfo regularPlaceInfo = this.curPlace;
        if (regularPlaceInfo == null) {
            isOtherLoc = true;
        } else if (regularPlaceInfo.getPlace() == null) {
            isOtherLoc = true;
        } else if (Constant.NAME_FREQLOCATION_OTHER.equals(this.curPlace.getPlace())) {
            isOtherLoc = true;
        }
        if (isOtherLoc) {
            long now = System.currentTimeMillis();
            if ((now - this.mUpdateDbOtherTime > Constant.MILLISEC_ONE_DAY || isCheckPeriod) && this.mCollectUserFingersHandler != null) {
                this.mCollectUserFingersHandler.assignSpaceExp2Space(new RecognizeResult(), true);
            }
            this.mUpdateDbOtherTime = now;
        }
    }

    class DefaultState extends State {
        public final String TAG = ("WMapping." + DefaultState.class.getSimpleName());

        DefaultState() {
        }

        public void enter() {
            LogUtil.d(false, "enter DefaultState", new Object[0]);
        }

        public boolean processMessage(Message message) {
            if (!HwWMStateMachine.this.isInitFinished) {
                LogUtil.w(false, "StateMachine NOT initial", new Object[0]);
                return true;
            }
            int i = message.what;
            if (i == 20) {
                processAddFreqLocation(message);
            } else if (i == 30) {
                processInFreqLocation(message);
            } else if (i == 110) {
                processArMove();
            } else if (i == 113) {
                processArStation();
            } else if (i == 115) {
                processUserDataAction();
            } else if (i == 120) {
                processQueryHisQoe(message);
            } else if (i == 130) {
                processSystemShutDown();
            } else if (i == 210) {
                processAppQoeEvent(message);
            } else if (i == 150) {
                processPowerConnected();
            } else if (i != 151) {
                switch (i) {
                    case WMStateCons.MSG_CONNECTIVITY_CHANGE /* 91 */:
                        processConnectivityChanged(message);
                        break;
                    case WMStateCons.MSG_SUPPLICANT_COMPLETE /* 92 */:
                        processSupplicantCompleted();
                        break;
                    case WMStateCons.MSG_CELL_CHANGE /* 93 */:
                    case WMStateCons.MSG_CELL_IN_SERVICE /* 95 */:
                        processCellChanged();
                        break;
                    case WMStateCons.MSG_SIM_STATE_CHANGE /* 94 */:
                        processSimStateChanged();
                        break;
                    case WMStateCons.MSG_CELL_OUT_OF_SERVICE /* 96 */:
                        processCellOutOfService();
                        break;
                    default:
                        switch (i) {
                            case 200:
                                processAppStateStart(message);
                                break;
                            case 201:
                                processAppStateEnd(message);
                                break;
                            case 202:
                                processAppStateUpdate(message);
                                break;
                            default:
                                LogUtil.i(false, "into default, msg:%{public}d", Integer.valueOf(message.what));
                                break;
                        }
                }
            } else {
                processPowerDisconnected();
            }
            return true;
        }

        private void processSystemShutDown() {
            HwWMStateMachine.this.updateUserSpaceDbForOtherLoc(true);
            LogUtil.i(false, "into MSG_SYS_SHUTDOWN, go back Default", new Object[0]);
            HwWMStateMachine hwWMStateMachine = HwWMStateMachine.this;
            hwWMStateMachine.transitionTo(hwWMStateMachine.mDefaultState);
        }

        private void processPowerDisconnected() {
            LogUtil.d(false, "into MSG_POWER_DISCONNECTED", new Object[0]);
            HwWMStateMachine.this.mPowerConnectTime = 0;
        }

        private void processPowerConnected() {
            LogUtil.d(false, "into MSG_POWER_CONNECTED", new Object[0]);
            HwWMStateMachine.this.mPowerConnectTime = SystemClock.elapsedRealtime();
        }

        private void processArMove() {
            LogUtil.d(false, "into MSG_AR_MOVE", new Object[0]);
            HwWMStateMachine.this.substateActivity = 2;
            HwWMStateMachine.this.mActiveCollectHandler.stopOut4gRecgScan();
        }

        private void processArStation() {
            LogUtil.d(false, "into MSG_AR_STATION", new Object[0]);
            HwWMStateMachine.this.substateActivity = 1;
        }

        private void processQueryHisQoe(Message message) {
            LogUtil.d(false, "into MSG_QUERY_HISQOE, query saved history App QoE", new Object[0]);
            HwWMStateMachine.this.queryHistoryQoe(message.getData(), (IWaveMappingCallback) message.obj);
        }

        private void processAppQoeEvent(Message message) {
            LogUtil.d(false, "Into MSG_APP_QOE_EVENT", new Object[0]);
            Bundle bundle = message.getData();
            if (bundle == null) {
                LogUtil.e(false, " no bundle messages", new Object[0]);
            } else if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                HwWMStateMachine.this.mCollectUserFingersHandler.updateAppQoE(bundle.getString("APPNAME"), bundle.getInt("QOE"));
            }
        }

        private void processAppStateUpdate(Message message) {
            LogUtil.d(false, "Into MSG_APP_STATE_NWUPDATE", new Object[0]);
            HwWmpAppInfo mAppInfo = (HwWmpAppInfo) message.obj;
            if (mAppInfo == null) {
                LogUtil.e(false, " no app messages", new Object[0]);
            } else if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                HwWMStateMachine.this.mCollectUserFingersHandler.updateAppNetwork(mAppInfo.getAppName(), mAppInfo.getConMgrNetworkType());
            }
        }

        private void processAppStateEnd(Message message) {
            LogUtil.d(false, "Into MSG_APP_STATE_END", new Object[0]);
            HwWmpAppInfo mAppInfo = (HwWmpAppInfo) message.obj;
            if (mAppInfo == null) {
                LogUtil.e(false, " no app messages", new Object[0]);
                return;
            }
            if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                HwWMStateMachine.this.mCollectUserFingersHandler.endAppCollect(mAppInfo);
            }
            HwWMStateMachine.this.updateUserSpaceDbForOtherLoc(false);
        }

        private void processAppStateStart(Message message) {
            LogUtil.d(false, "Into MSG_APP_STATE_START", new Object[0]);
            HwWmpAppInfo mAppInfo = (HwWmpAppInfo) message.obj;
            if (mAppInfo == null) {
                LogUtil.e(false, " no app messages", new Object[0]);
            } else if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                HwWMStateMachine.this.mCollectUserFingersHandler.startAppCollect(mAppInfo);
            }
        }

        private void processInFreqLocation(Message message) {
            LogUtil.d(false, "Into MSG_IN_FREQ_LOCATION", new Object[0]);
            try {
                Bundle bundle = message.getData();
                if (bundle != null) {
                    if (bundle.get(HwWMStateMachine.KEY_LOCATION) != null) {
                        String curLocation = bundle.get(HwWMStateMachine.KEY_LOCATION).toString();
                        LogUtil.d(false, "MSG_IN_FREQ_LOCATION,curLocation=%{private}s", curLocation);
                        if (HwWMStateMachine.this.curPlace == null || HwWMStateMachine.this.curPlace.getPlace() == null) {
                            LogUtil.d(false, "curPlace == null", new Object[0]);
                            HwWMStateMachine.this.getCurrentPlace(curLocation);
                            if (HwWMStateMachine.this.curPlace != null) {
                                if (HwWMStateMachine.this.curPlace.getPlace() != null) {
                                    if (!HwWMStateMachine.this.curPlace.getPlace().equals(curLocation)) {
                                        LogUtil.e(false, " getPlace:%{private}s not the same as input location:%{private}s", HwWMStateMachine.this.curPlace.toString(), curLocation);
                                        HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mDefaultState);
                                        return;
                                    }
                                }
                            }
                            HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mDefaultState);
                            return;
                        } else if (curLocation.equals(HwWMStateMachine.this.curPlace.getPlace())) {
                            LogUtil.d(false, "the same location - KEEP current state, current %{private}s", HwWMStateMachine.this.curPlace.toString());
                            return;
                        }
                        processCurrentPlaceState();
                        return;
                    }
                }
                LogUtil.w(false, " no bundle location", new Object[0]);
            } catch (Exception e) {
                LogUtil.e(false, "MSG_IN_FREQ_LOCATION failed by Exception", new Object[0]);
            }
        }

        private void processAddFreqLocation(Message message) {
            LogUtil.i(false, "Into MSG_ADD_FREQ_LOCATION_TOOL", new Object[0]);
            try {
                Bundle bundle = message.getData();
                if (bundle != null) {
                    if (bundle.get(HwWMStateMachine.KEY_LOCATION) != null) {
                        addFreqLoc(bundle.get(HwWMStateMachine.KEY_LOCATION).toString());
                        return;
                    }
                }
                LogUtil.w(false, "no bundle location", new Object[0]);
            } catch (Exception e) {
                LogUtil.e(false, "DefaultState processMessage failed by Exception", new Object[0]);
            }
        }

        private void processCurrentPlaceState() {
            LogUtil.i(false, " curPlace.getPlace()=%{private}s", HwWMStateMachine.this.curPlace.getPlace());
            HwWMStateMachine hwWMStateMachine = HwWMStateMachine.this;
            hwWMStateMachine.collectFingersHandler = new CollectFingersHandler(hwWMStateMachine.mCtx, HwWMStateMachine.this.curPlace.getPlace(), HwWMStateMachine.this.getHandler());
            HwWMStateMachine.this.mCollectUserFingersHandler.assignSpaceExp2Space(HwWMStateMachine.this.lastSpaceIds, true);
            HwWMStateMachine.this.mCollectUserFingersHandler.setFreqLocation(HwWMStateMachine.this.curPlace.getPlace());
            HwWMStateMachine.this.mBehaviorReceiver.saveBatch();
            HwWMStateMachine.this.collectFingersHandler.mainApInfoToDb();
            if (HwWMStateMachine.this.curPlace.getState() == 4) {
                HwWMStateMachine hwWMStateMachine2 = HwWMStateMachine.this;
                hwWMStateMachine2.transitionTo(hwWMStateMachine2.mLocatingState);
                return;
            }
            HwWMStateMachine hwWMStateMachine3 = HwWMStateMachine.this;
            hwWMStateMachine3.transitionTo(hwWMStateMachine3.mCollectTrainingState);
        }

        private void processSimStateChanged() {
            LogUtil.i(false, "Into MSG_SIM_STATE_CHANGE", new Object[0]);
            if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                HwWMStateMachine.this.mCollectUserFingersHandler.assignSpaceExp2Space(HwWMStateMachine.this.lastSpaceIds, true);
                HwWMStateMachine.this.mCollectUserFingersHandler.setCurrScrbId();
            }
        }

        private void processCellOutOfService() {
            LogUtil.i(false, "Into MSG_CELL_OUT_OF_SERVICE", new Object[0]);
            if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                HwWMStateMachine.this.mCollectUserFingersHandler.updateMobileDurationForCell(true);
            }
        }

        private void processCellChanged() {
            LogUtil.i(false, "Into MSG_CELL_CHANGE", new Object[0]);
            if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                HwWMStateMachine.this.mCollectUserFingersHandler.updateMobileDurationForCell(true);
                HwWMStateMachine.this.mCollectUserFingersHandler.checkOutOf4gCoverage(false);
            }
        }

        private void processSupplicantCompleted() {
            LogUtil.i(false, "Into MSG_SUPPLICANT_COMPLETE", new Object[0]);
            if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                HwWMStateMachine.this.mCollectUserFingersHandler.checkUserPrefAutoSwitch();
                HwWMStateMachine.this.mCollectUserFingersHandler.updateWifiDurationForAp(true);
            }
        }

        private void processUserDataAction() {
            LogUtil.d(false, "Into MSG_USER_DATA_ACTION", new Object[0]);
            if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                HwWMStateMachine.this.mCollectUserFingersHandler.updateSourceNetwork();
            }
        }

        private void processConnectivityChanged(Message message) {
            LogUtil.d(false, "Into MSG_CONNECTIVITY_CHANGE", new Object[0]);
            if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                HwWMStateMachine.this.mCollectUserFingersHandler.checkAllConnectivityState(message.arg1, message.arg2);
            }
        }

        private void addFreqLoc(String curLocation) {
            int freqLoc = -1;
            if (curLocation.equals(Constant.NAME_FREQLOCATION_HOME)) {
                freqLoc = 0;
            } else if (curLocation.equals(Constant.NAME_FREQLOCATION_OFFICE)) {
                freqLoc = 1;
            }
            LogUtil.d(false, "MSG_ADD_FREQ_LOCATION_TOOL,curLocation=%{private}s", curLocation);
            HwWMStateMachine.this.mFrequentLocation.updateWaveMapping(freqLoc, 0);
        }

        public void exit() {
            LogUtil.d(false, "exit DefaultState", new Object[0]);
            HwWMStateMachine.this.mBehaviorReceiver.saveBatch();
            if (HwWMStateMachine.this.collectFingersHandler != null) {
                HwWMStateMachine.this.collectFingersHandler.mainApInfoToDb();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void queryHistoryQoe(Bundle appInfo, IWaveMappingCallback cb) {
        if (appInfo == null || !appInfo.containsKey(KEY_FULL_ID)) {
            LogUtil.w(false, " no bundle", new Object[0]);
            if (cb != null) {
                cb.onWaveMappingRespondCallback(0, 0, 0, true, false);
                cb.onWaveMappingRespond4BackCallback(0, 0, 0, true, false);
                return;
            }
            return;
        }
        appInfo.getInt(KEY_FULL_ID);
        int uid = appInfo.getInt(KEY_UID);
        appInfo.getInt(KEY_NETWORK);
        int arbNet = appInfo.getInt(KEY_ARB_NETWORK);
        int direction = appInfo.getInt(KEY_DIRECT);
        if (cb == null) {
            return;
        }
        if (direction == 1) {
            cb.onWaveMappingRespondCallback(uid, 0, arbNet, true, false);
        } else if (direction == 2) {
            cb.onWaveMappingRespond4BackCallback(uid, 0, arbNet, true, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isSatisfiedPowerCondition() {
        if (this.mPowerConnectTime == 0) {
            LogUtil.d(false, "power not connect", new Object[0]);
            return false;
        } else if (SystemClock.elapsedRealtime() - this.mPowerConnectTime < 300000) {
            LogUtil.d(false, "need after power connect 5 min", new Object[0]);
            return false;
        } else {
            CheckTemperatureUtil checkTemperatureUtil = this.mCheckTemperatureUtil;
            if (checkTemperatureUtil == null || !checkTemperatureUtil.isExceedMaxTemperature()) {
                return true;
            }
            LogUtil.d(false, "trainModels isExceedMaxTemperature == true", new Object[0]);
            return false;
        }
    }

    class FrequentLocationState extends State {
        public final String TAG = ("WMapping." + FrequentLocationState.class.getSimpleName());

        FrequentLocationState() {
        }

        public void enter() {
            LogUtil.d(false, "enter mFrequentLocationState", new Object[0]);
            HwWMStateMachine.this.wifiDataProvider.start();
            if (HwWMStateMachine.this.curPlace != null) {
                LogUtil.i(false, "curPlace=%{public}s", HwWMStateMachine.this.curPlace.getPlace());
                HwWMStateMachine.this.collectFingersHandler.startCollect();
                HwWMStateMachine.this.mCellStateMonitor.startMonitor();
                return;
            }
            LogUtil.e(false, " empty current location, back to DefaultState", new Object[0]);
            HwWMStateMachine hwWMStateMachine = HwWMStateMachine.this;
            hwWMStateMachine.transitionTo(hwWMStateMachine.mDefaultState);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 50) {
                processOutFreqLoc();
            } else if (i == 55) {
                processLeaveFreqLoc();
            } else if (i == 60) {
                LogUtil.d(false, "Into MSG_TOOL_FORCE_TRAINING, force training by tool", new Object[0]);
                if (HwWMStateMachine.this.curPlace == null || HwWMStateMachine.this.curPlace.getPlace() == null) {
                    LogUtil.e(false, "current location == null", new Object[0]);
                    HwWMStateMachine hwWMStateMachine = HwWMStateMachine.this;
                    hwWMStateMachine.transitionTo(hwWMStateMachine.mDefaultState);
                } else {
                    LogUtil.d(false, "force training by tool, current location = %{private}s", HwWMStateMachine.this.curPlace.getPlace());
                    HwWMStateMachine hwWMStateMachine2 = HwWMStateMachine.this;
                    hwWMStateMachine2.trainModels(hwWMStateMachine2.curPlace.getPlace());
                    HwWMStateMachine hwWMStateMachine3 = HwWMStateMachine.this;
                    hwWMStateMachine3.transitionTo(hwWMStateMachine3.mCollectTrainingState);
                }
            } else if (i == 62) {
                LogUtil.d(false, "MSG_WIFI_UPDATE_SCAN_RESULT01, wifi scan complete", new Object[0]);
                try {
                    HwWMStateMachine.this.wifiList = HwWMStateMachine.this.mWifiManager.getScanResults();
                    if (HwWMStateMachine.this.wifiList == null) {
                        LogUtil.d(false, "MSG_WIFI_UPDATE_SCAN_RESULT, wifiList=null", new Object[0]);
                    } else if (HwWMStateMachine.this.curPlace == null) {
                        LogUtil.d(false, "MSG_WIFI_UPDATE_SCAN_RESULT, curPlace=null", new Object[0]);
                    } else {
                        LogUtil.i(false, "MSG_WIFI_UPDATE_SCAN_RESULT, curPlace=%{private}s, wifiList.size=%{public}d", HwWMStateMachine.this.curPlace.toString(), Integer.valueOf(HwWMStateMachine.this.wifiList.size()));
                        HwWMStateMachine.this.collectFingersHandler.isProcessWifiData(HwWMStateMachine.this.wifiList, HwWMStateMachine.this.curPlace);
                    }
                } catch (Exception e) {
                    LogUtil.e(false, "MSG_WIFI_UPDATE_SCAN_RESULT failed by Exception", new Object[0]);
                }
            } else if (i != 141) {
                return false;
            } else {
                LogUtil.d(false, "Into MSG_BACK_4G_COVERAGE, stop active scan timer", new Object[0]);
                HwWMStateMachine.this.mActiveCollectHandler.stopOut4gRecgScan();
            }
            return true;
        }

        private void processLeaveFreqLoc() {
            LogUtil.i(false, "In to Other Location, MSG_LEAVE_FREQ_LOCATION_TOOL", new Object[0]);
            if (HwWMStateMachine.this.curPlace != null && HwWMStateMachine.this.curPlace.getPlace() != null) {
                int currLoc = 1;
                if (HwWMStateMachine.this.curPlace.getPlace().equals(Constant.NAME_FREQLOCATION_HOME)) {
                    currLoc = 0;
                }
                HwWMStateMachine.this.mFrequentLocation.updateWaveMapping(currLoc, 1);
            }
        }

        private void processOutFreqLoc() {
            LogUtil.d(false, "In to Other Location, MSG_OUT_FREQ_LOCATION", new Object[0]);
            if (HwWMStateMachine.this.uiInfo != null) {
                HwWMStateMachine.this.uiInfo.setFingerBatchNumber(0);
                HwWMStateMachine.this.uiInfo.setStage(0);
                HwWMStateMachine.this.uiInfo.setSsid(Constant.NAME_FREQLOCATION_OTHER);
            }
            HwWMStateMachine hwWMStateMachine = HwWMStateMachine.this;
            hwWMStateMachine.transitionTo(hwWMStateMachine.mDefaultState);
        }

        public void exit() {
            LogUtil.d(false, "exit mFrequentLocationState", new Object[0]);
            if (HwWMStateMachine.this.collectFingersHandler != null) {
                HwWMStateMachine.this.collectFingersHandler.mainApInfoToDb();
            }
            if (!HwWMStateMachine.this.collectFingersHandler.isStopCollect()) {
                LogUtil.e(false, " Stop Collection Failure", new Object[0]);
            }
            HwWMStateMachine.this.curPlace = null;
            HwWMStateMachine.this.mCollectUserFingersHandler.assignSpaceExp2Space(HwWMStateMachine.this.lastSpaceIds, true);
            HwWMStateMachine.this.mCollectUserFingersHandler.setFreqLocation(Constant.NAME_FREQLOCATION_OTHER);
            HwWMStateMachine.this.mCollectUserFingersHandler.resetOut4gBeginTime();
            HwWMStateMachine.this.wifiDataProvider.stop();
            HwWMStateMachine.this.mBehaviorReceiver.saveBatch();
            if (HwWMStateMachine.this.getHandler().hasMessages(62)) {
                HwWMStateMachine.this.removeMessages(62);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class CollectTrainingState extends State {
        public final String TAG = ("WMapping." + CollectTrainingState.class.getSimpleName());
        private RegularPlaceInfo regularPlaceInfo;

        CollectTrainingState() {
        }

        public void enter() {
            LogUtil.d(false, "enter mCollectTrainingState", new Object[0]);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 64) {
                LogUtil.d(false, "MSG_WIFI_UPDATE_SCAN_RESULT002, wifi scan complete", new Object[0]);
                HwWMStateMachine.this.mActiveCollectHandler.isStartBgScan();
                long trainingDuration = SystemClock.elapsedRealtime() - HwWMStateMachine.this.mLastTrainingTime;
                if (HwWMStateMachine.this.mLastTrainingTime != 0 && trainingDuration < 43200000) {
                    LogUtil.d(false, "checkTrainingDuration, after 12h", new Object[0]);
                    return true;
                } else if (!HwWMStateMachine.this.collectFingersHandler.isCheckDataSatisfiedToTraining(HwWMStateMachine.this.curPlace)) {
                    return true;
                } else {
                    HwWMStateMachine.this.sendMessage(80);
                    return true;
                }
            } else if (i == 110) {
                LogUtil.d(false, "MSG_AR_MOVE, user go to other space", new Object[0]);
                HwWMStateMachine.this.mActiveCollectHandler.stopBgScan();
                HwWMStateMachine.this.mActiveCollectHandler.stopStallScan();
                HwWMStateMachine.this.mActiveCollectHandler.stopOut4gRecgScan();
                return false;
            } else if (i == 141 || i == 211) {
                LogUtil.d(false, "Into MSG_APP_DATA_STALL", new Object[0]);
                if (HwWMStateMachine.this.mActiveCollectHandler == null) {
                    return true;
                }
                HwWMStateMachine.this.mActiveCollectHandler.isStartStallScan();
                return true;
            } else if (i == 80) {
                LogUtil.d(false, "MSG_FINGER_NUM_SATISFIED, training criteria is satisfied", new Object[0]);
                if (!HwWMStateMachine.this.isSatisfiedPowerCondition()) {
                    return true;
                }
                HwWMStateMachine hwWMStateMachine = HwWMStateMachine.this;
                hwWMStateMachine.trainModels(hwWMStateMachine.curPlace.getPlace());
                return true;
            } else if (i != 81) {
                return false;
            } else {
                buildModelCompleted();
                return true;
            }
        }

        private void buildModelCompleted() {
            LogUtil.d(false, "MSG_BUILDMODEL_COMPLETED, model complete", new Object[0]);
            if (HwWMStateMachine.this.curPlace == null || HwWMStateMachine.this.curPlace.getPlace() == null) {
                LogUtil.e(false, " current location == null", new Object[0]);
                HwWMStateMachine hwWMStateMachine = HwWMStateMachine.this;
                hwWMStateMachine.transitionTo(hwWMStateMachine.mDefaultState);
                return;
            }
            LogUtil.d(false, "    current location = %{private}s", HwWMStateMachine.this.curPlace.getPlace());
            if (HwWMStateMachine.this.getBuildModelResult()) {
                HwWMStateMachine hwWMStateMachine2 = HwWMStateMachine.this;
                hwWMStateMachine2.transitionTo(hwWMStateMachine2.mLocatingState);
                return;
            }
            HwWMStateMachine hwWMStateMachine3 = HwWMStateMachine.this;
            hwWMStateMachine3.transitionTo(hwWMStateMachine3.mCollectTrainingState);
        }

        public void exit() {
            LogUtil.d(false, "exit mCollectTrainingState", new Object[0]);
            HwWMStateMachine.this.mActiveCollectHandler.stopBgScan();
            HwWMStateMachine.this.mActiveCollectHandler.stopStallScan();
            if (HwWMStateMachine.this.getHandler().hasMessages(64)) {
                HwWMStateMachine.this.removeMessages(64);
            }
        }
    }

    public void trainModels(String freqLoc) {
        if (this.modelService == null) {
            LogUtil.e(false, "trainModels, modelService == null", new Object[0]);
        } else if (TextUtils.isEmpty(freqLoc)) {
            LogUtil.e(false, "trainModels, freqLoc == null", new Object[0]);
        } else {
            Handler modelServiceHandler = this.modelService.getHandler();
            if (modelServiceHandler == null) {
                LogUtil.e(false, "trainModels, modelServiceHandler == null", new Object[0]);
                return;
            }
            Bundle bundle = new Bundle();
            bundle.putString(Constant.NAME_FREQLACATION, freqLoc);
            Message buildModelMsg = Message.obtain(modelServiceHandler, 2);
            buildModelMsg.setData(bundle);
            modelServiceHandler.sendMessage(buildModelMsg);
            this.mLastTrainingTime = SystemClock.elapsedRealtime();
        }
    }

    public boolean getBuildModelResult() {
        ModelService5 modelService5 = this.modelService;
        if (modelService5 == null) {
            LogUtil.e(false, "getBuildModelResult, modelService == null", new Object[0]);
            return false;
        }
        ClusterResult result = modelService5.getClusterResult();
        if (result == null || TextUtils.isEmpty(result.getPlace())) {
            return false;
        }
        LogUtil.d(false, " ,startTraining result:%{private}s", result.toString());
        UiInfo uiInfo2 = UiService.getUiInfo();
        uiInfo2.setClusterNumber(result.getClusterNum() + "_" + result.getMainApClusterNum());
        UiService uiService2 = this.uiService;
        UiService.sendMsgToUi();
        if (result.getClusterNum() <= 0) {
            LogUtil.d(false, " getClusterNumber ,still unqualified, return to CollectTrainingState", new Object[0]);
            return false;
        }
        RegularPlaceInfo regularPlaceInfo = this.curPlace;
        if (regularPlaceInfo == null) {
            LogUtil.d(false, " curPlace == null, return to CollectTrainingState", new Object[0]);
            return false;
        }
        getCurrentPlace(regularPlaceInfo.getPlace());
        UiService.getUiInfo().setStage(4);
        UiService uiService3 = this.uiService;
        UiService.sendMsgToUi();
        LogUtil.d(false, " update current location success, train model OK, location :%{private}s", result.getPlace());
        return true;
    }

    class RecognitionState extends State {
        public final String TAG = ("WMapping." + RecognitionState.class.getSimpleName());
        private RecognizeService mRecognizeService;

        RecognitionState() {
        }

        public void enter() {
            LogUtil.d(false, "enter mRecognitionState", new Object[0]);
            if (HwWMStateMachine.this.modelService != null) {
                this.mRecognizeService = new RecognizeService(HwWMStateMachine.this.modelService);
                return;
            }
            LogUtil.d(false, "enter mLocatingState, modelService == null", new Object[0]);
            HwWMStateMachine hwWMStateMachine = HwWMStateMachine.this;
            hwWMStateMachine.modelService = ModelService5.getInstance(hwWMStateMachine.mCtx, HwWMStateMachine.this.getHandler());
            this.mRecognizeService = new RecognizeService(HwWMStateMachine.this.modelService);
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i == 63) {
                LogUtil.d(false, "MSG_WIFI_UPDATE_SCAN_RESULT, wifi scan complete", new Object[0]);
                try {
                    if (this.mRecognizeService == null) {
                        LogUtil.e(false, "recognizeService == null", new Object[0]);
                    } else {
                        RecognizeResult recognizeResult = this.mRecognizeService.identifyLocation(HwWMStateMachine.this.curPlace, HwWMStateMachine.this.curMainApPlaceInfo, HwWMStateMachine.this.wifiList);
                        if (recognizeResult.getRgResult() == 0) {
                            LogUtil.d(false, " recognizeResult failure, result = unknonw", new Object[0]);
                            HwWMStateMachine.this.setCurrentPreLable(recognizeResult);
                            UiService.getUiInfo().setToast("RecognitionState.");
                            UiService unused = HwWMStateMachine.this.uiService;
                            UiService.sendMsgToUi();
                            HwWMStateMachine.this.transitionTo(HwWMStateMachine.this.mLocatingState);
                        } else {
                            LogUtil.d(false, " found the space:%{public}s", recognizeResult.toString());
                            if (HwWMStateMachine.this.curSpaceIds.cmpResults(recognizeResult)) {
                                LogUtil.i(false, " new space", new Object[0]);
                                HwWMStateMachine.this.setCurrentPreLable(recognizeResult);
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
                    LogUtil.e(false, "RecognitionState processMessage failed by Exception", new Object[0]);
                }
            } else if (i != 100) {
                return false;
            } else {
                LogUtil.d(false, "into MSG_MODEL_UNQUALIFIED, space model too old", new Object[0]);
                if (HwWMStateMachine.this.curPlace == null || HwWMStateMachine.this.curPlace.getPlace() == null) {
                    LogUtil.e(false, " current location == null", new Object[0]);
                    HwWMStateMachine hwWMStateMachine = HwWMStateMachine.this;
                    hwWMStateMachine.transitionTo(hwWMStateMachine.mDefaultState);
                } else {
                    LogUtil.d(false, "    current location = %{private}s", HwWMStateMachine.this.curPlace.getPlace());
                    HwWMStateMachine hwWMStateMachine2 = HwWMStateMachine.this;
                    hwWMStateMachine2.transitionTo(hwWMStateMachine2.mCollectTrainingState);
                }
            }
            return true;
        }

        public void exit() {
            LogUtil.d(false, "exit mRecognitionState", new Object[0]);
            HwWMStateMachine.this.mActiveCollectHandler.stopOut4gRecgScan();
            if (HwWMStateMachine.this.getHandler().hasMessages(63)) {
                HwWMStateMachine.this.removeMessages(63);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class PositionState extends State {
        public final String TAG = ("WMapping." + PositionState.class.getSimpleName());

        PositionState() {
        }

        public void enter() {
            LogUtil.d(false, "enter mPositionState", new Object[0]);
            HwWMStateMachine.this.mCollectUserFingersHandler.recognizeActions(HwWMStateMachine.this.curSpaceIds, HwWMStateMachine.this.curSpaceIds.getRgResult() != HwWMStateMachine.this.lastSpaceIds.getRgResult());
            HwWMStateMachine.this.mActiveCollectHandler.stopOut4gRecgScan();
            HwWMStateMachine.this.mCollectUserFingersHandler.checkOutOf4gCoverage(false);
            HwWMStateMachine.this.mLocationDao.accChrSpaceChangeByFreqLoc(HwWMStateMachine.this.curPlace.getPlace());
            HwWMStateMachine.this.lastSpaceIds.copyResults(HwWMStateMachine.this.curSpaceIds);
            LogUtil.d(false, " save last results:%{public}s", HwWMStateMachine.this.lastSpaceIds.toString());
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 110) {
                if (i == 120) {
                    LogUtil.d(false, "into MSG_QUERY_HISQOE, query saved history App QoE", new Object[0]);
                    Bundle bundle = message.getData();
                    if (bundle == null || !bundle.containsKey(HwWMStateMachine.KEY_FULL_ID)) {
                        LogUtil.w(false, " no bundle", new Object[0]);
                        IWaveMappingCallback cb = (IWaveMappingCallback) message.obj;
                        if (cb != null) {
                            cb.onWaveMappingRespondCallback(0, 0, 0, true, false);
                            cb.onWaveMappingRespond4BackCallback(0, 0, 0, true, false);
                        }
                    } else {
                        int fullId = bundle.getInt(HwWMStateMachine.KEY_FULL_ID);
                        int uid = bundle.getInt(HwWMStateMachine.KEY_UID);
                        int net = bundle.getInt(HwWMStateMachine.KEY_NETWORK);
                        int direct = bundle.getInt(HwWMStateMachine.KEY_DIRECT);
                        HwWMStateMachine.this.mCollectUserFingersHandler.queryAppQoeByTargetNw(HwWMStateMachine.this.curSpaceIds, fullId, uid, net, (IWaveMappingCallback) message.obj, direct);
                    }
                } else if (i == 140) {
                    LogUtil.d(false, "into MSG_CHECK_4G_COVERAGE, check 4G coverage by current results", new Object[0]);
                    if (HwWMStateMachine.this.mCollectUserFingersHandler.determine4gCoverage(HwWMStateMachine.this.lastSpaceIds)) {
                        HwWMStateMachine.this.mCollectUserFingersHandler.checkOutOf4gCoverage(true);
                    }
                } else if (i != 200) {
                    return false;
                } else {
                    LogUtil.d(false, "Into MSG_APP_STATE_START", new Object[0]);
                    HwWmpAppInfo mAppInfo = (HwWmpAppInfo) message.obj;
                    if (mAppInfo == null) {
                        LogUtil.e(false, " no app messages", new Object[0]);
                    } else {
                        if (HwWMStateMachine.this.mCollectUserFingersHandler != null) {
                            HwWMStateMachine.this.mCollectUserFingersHandler.determineAppQoePreference(HwWMStateMachine.this.curSpaceIds, mAppInfo);
                        }
                        return false;
                    }
                }
                return true;
            }
            LogUtil.d(false, "into MSG_AR_MOVE, user go to other space", new Object[0]);
            HwWMStateMachine hwWMStateMachine = HwWMStateMachine.this;
            hwWMStateMachine.transitionTo(hwWMStateMachine.mLocatingState);
            return false;
        }

        public void exit() {
            boolean isSpaceChanged = false;
            LogUtil.d(false, "exit mPositionState", new Object[0]);
            if (HwWMStateMachine.this.curSpaceIds.getRgResult() != 0) {
                if (HwWMStateMachine.this.curSpaceIds.getRgResult() != HwWMStateMachine.this.lastSpaceIds.getRgResult()) {
                    isSpaceChanged = true;
                }
                HwWMStateMachine.this.mCollectUserFingersHandler.assignSpaceExp2Space(HwWMStateMachine.this.lastSpaceIds, isSpaceChanged);
            }
            HwWMStateMachine.this.substateLastrecg = 1;
        }
    }

    /* access modifiers changed from: package-private */
    public class LocatingState extends State {
        public final String TAG = ("WMapping." + LocatingState.class.getSimpleName());

        LocatingState() {
        }

        public void enter() {
            LogUtil.d(false, "enter mLocatingState", new Object[0]);
            HwWMStateMachine.this.setCurrentPreLable(HwWMStateMachine.this.recognizeByMainAp());
            HwWMStateMachine.this.mCollectUserFingersHandler.assignSpaceExp2Space(HwWMStateMachine.this.lastSpaceIds, true);
            HwWMStateMachine.this.mCollectUserFingersHandler.recognizeActions(HwWMStateMachine.this.curSpaceIds, true);
            if (HwWMStateMachine.this.substateLastrecg == 1) {
                HwWMStateMachine.this.mLocationDao.accChrSpaceLeaveByFreqLoc(HwWMStateMachine.this.curPlace.getPlace());
            }
            HwWMStateMachine.this.lastSpaceIds.copyResults(HwWMStateMachine.this.curSpaceIds);
            LogUtil.i(false, " save last results:%{public}s", HwWMStateMachine.this.lastSpaceIds.toString());
        }

        public boolean processMessage(Message message) {
            int i = message.what;
            if (i != 113) {
                if (i == 120) {
                    LogUtil.d(false, "into MSG_QUERY_HISQOE, query saved history App QoE", new Object[0]);
                    Bundle bundle = message.getData();
                    if (bundle == null || !bundle.containsKey(HwWMStateMachine.KEY_FULL_ID)) {
                        LogUtil.w(false, " no bundle", new Object[0]);
                        IWaveMappingCallback cb = (IWaveMappingCallback) message.obj;
                        if (cb != null) {
                            cb.onWaveMappingRespondCallback(0, 0, 0, true, false);
                            cb.onWaveMappingRespond4BackCallback(0, 0, 0, true, false);
                        }
                    } else {
                        int fullId = bundle.getInt(HwWMStateMachine.KEY_FULL_ID);
                        int uid = bundle.getInt(HwWMStateMachine.KEY_UID);
                        int net = bundle.getInt(HwWMStateMachine.KEY_NETWORK);
                        int direct = bundle.getInt(HwWMStateMachine.KEY_DIRECT);
                        HwWMStateMachine.this.mCollectUserFingersHandler.queryAppQoeByTargetNw(HwWMStateMachine.this.curSpaceIds, fullId, uid, net, (IWaveMappingCallback) message.obj, direct);
                    }
                } else if (i == 140) {
                    check4gCoverage();
                } else if (i == 115) {
                    LogUtil.d(false, "Into MSG_USER_DATA_ACTION", new Object[0]);
                    HwWMStateMachine.this.mCollectUserFingersHandler.recognizeActions(HwWMStateMachine.this.curSpaceIds, true);
                    return false;
                } else if (i != 116) {
                    return false;
                } else {
                    LogUtil.d(false, "into MSG_USER_NEED_FURTHER_SPACE, trigger scan to get allAp space", new Object[0]);
                    HwWMStateMachine.this.mActiveCollectHandler.isStartFurtherSpaceScan();
                }
                return true;
            }
            LogUtil.d(false, "into MSG_AR_STATION, stand in a certain space", new Object[0]);
            HwWMStateMachine.this.mCollectUserFingersHandler.checkOutOf4gCoverage(false);
            if (HwWMStateMachine.this.substateActivity == 2) {
                HwWMStateMachine hwWMStateMachine = HwWMStateMachine.this;
                hwWMStateMachine.transitionTo(hwWMStateMachine.mLocatingState);
            }
            return false;
        }

        private void check4gCoverage() {
            LogUtil.d(false, "into MSG_CHECK_4G_COVERAGE, check 4G coverage by mainAp result: ACT_STATE=%{public}d", Integer.valueOf(HwWMStateMachine.this.substateActivity));
            try {
                if (HwWMStateMachine.this.substateActivity == 2) {
                    HwWMStateMachine.this.mActiveCollectHandler.stopOut4gRecgScan();
                } else if (HwWMStateMachine.this.substateActivity == 1) {
                    if (!HwWMStateMachine.this.mCollectUserFingersHandler.determine4gCoverage(HwWMStateMachine.this.curSpaceIds)) {
                        HwWMStateMachine.this.mActiveCollectHandler.isStartOut4gRecgScan();
                    }
                    HwWMStateMachine.this.mCollectUserFingersHandler.checkOutOf4gCoverage(true);
                }
            } catch (Exception e) {
                LogUtil.e(false, "MSG_CHECK_4G_COVERAGE failed by Exception", new Object[0]);
            }
        }

        public void exit() {
            LogUtil.d(false, "exit mLocatingState", new Object[0]);
            HwWMStateMachine.this.substateLastrecg = 1;
        }
    }
}
