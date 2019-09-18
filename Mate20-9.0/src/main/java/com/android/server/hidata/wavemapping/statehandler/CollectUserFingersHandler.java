package com.android.server.hidata.wavemapping.statehandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.SupplicantState;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.android.server.gesture.GestureNavConst;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import com.android.server.hidata.arbitration.HwArbitrationHistoryQoeManager;
import com.android.server.hidata.wavemapping.HwWaveMappingManager;
import com.android.server.hidata.wavemapping.IWaveMappingCallback;
import com.android.server.hidata.wavemapping.chr.QueryHistAppQoeService;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ContextManager;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.dao.FastBack2LteChrDAO;
import com.android.server.hidata.wavemapping.dao.HisQoEChrDAO;
import com.android.server.hidata.wavemapping.dao.LocationDAO;
import com.android.server.hidata.wavemapping.dao.SpaceUserDAO;
import com.android.server.hidata.wavemapping.entity.HwWmpAppInfo;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.RecognizeResult;
import com.android.server.hidata.wavemapping.entity.SpaceExpInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.NetUtil;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import com.android.server.security.trustcircle.utils.LogHelper;
import com.huawei.lcagent.client.LogCollectManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CollectUserFingersHandler {
    private static final String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss.SSS";
    private static final int EVENT_UPDATE_SOURCE_NETWORK = 0;
    private static final String TAG = ("WMapping." + CollectUserFingersHandler.class.getSimpleName());
    private static final Integer[] VALIDPREFERREDMODE = {8, 9, 10, 12, 15, 17, 19, 20, 22, 58, 61, 63};
    private static Bundle lastCellState = new Bundle();
    private static Bundle lastWifiState = new Bundle();
    private static CollectUserFingersHandler mCollectUserFingersHandler = null;
    private boolean back4g_begin = true;
    private int back4g_restartCnt = 0;
    private boolean connectedMobile = false;
    private boolean connectedWifi = false;
    private String freqLoc = Constant.NAME_FREQLOCATION_OTHER;
    private Runnable getRegStateAfterPeriodHandler = new Runnable() {
        public void run() {
            Bundle cellInfo = NetUtil.getMobileDataState(CollectUserFingersHandler.this.mContext);
            LogUtil.i("getRegStateAfterPeriodHandler");
            String nwName = cellInfo.getString("cellRAT");
            if (nwName != null && nwName.equals("4G")) {
                CollectUserFingersHandler.this.mFastBack2LteChrDAO.addsuccessBack();
                CollectUserFingersHandler.this.mFastBack2LteChrDAO.endSession();
            }
            CollectUserFingersHandler.this.mFastBack2LteChrDAO.insertRecordByLoc();
        }
    };
    private Handler handler = new Handler();
    private boolean isDestNetworkCellular = false;
    private int locBatch = 0;
    private boolean mCHRQoEPrefAutoSwitch = false;
    private boolean mCHRQoEPrefDetermineSwitch = false;
    private boolean mCHRQoEPrefManualSwitch = false;
    private boolean mCHRUserPrefAutoSwitch = false;
    private long mCHRUserPrefAutoSwitchTime = 0;
    private boolean mCHRUserPrefDetermineSwitch = false;
    private boolean mCHRUserPrefManualSwitch = false;
    private long mCHRUserPrefManualSwitchTime = 0;
    private String mCHRUserPrefOriginalNWFreq = "UNKNOWN";
    private String mCHRUserPrefOriginalNWId = "UNKNOWN";
    private String mCHRUserPrefOriginalNWName = "UNKNOWN";
    private int mCHRUserPrefOriginalNWType = 8;
    private String mCHRUserPrefSwitchNWFreq = "UNKNOWN";
    private String mCHRUserPrefSwitchNWId = "UNKNOWN";
    private String mCHRUserPrefSwitchNWName = "UNKNOWN";
    private int mCHRUserPrefSwitchNWType = 8;
    private long mCHRUserPrefSwitchTime = 0;
    LogCollectManager mCollectManger = null;
    private CollectPowerHandler mCollectPowerHandler = null;
    /* access modifiers changed from: private */
    public Context mContext;
    private String mDestNetworkFreq = "UNKNOWN";
    private String mDestNetworkId = "UNKNOWN";
    private String mDestNetworkName = "UNKNOWN";
    private int mDestNetworkType = 8;
    /* access modifiers changed from: private */
    public FastBack2LteChrDAO mFastBack2LteChrDAO;
    private HwWmpFastBackLteManager mFastBackLteMgr = null;
    private HisQoEChrDAO mHisQoEChrDAO;
    private QueryHistAppQoeService mHisQoEChrService;
    private HwArbitrationHistoryQoeManager mHistoryQoeMgr = null;
    private LocationDAO mLocationDAO;
    /* access modifiers changed from: private */
    public Handler mMachineHandler;
    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                LogUtil.w(" action is null");
                return;
            }
            char c = 65535;
            int hashCode = action.hashCode();
            if (hashCode != -229777127) {
                if (hashCode != 233521600) {
                    if (hashCode != 1067187475) {
                        if (hashCode == 1272267123 && action.equals(HwArbitrationDEFS.ACTION_HiData_DATA_ROVE_IN)) {
                            c = 3;
                        }
                    } else if (action.equals("com.android.server.hidata.arbitration.HwArbitrationStateMachine")) {
                        c = 1;
                    }
                } else if (action.equals("android.net.wifi.supplicant.STATE_CHANGE")) {
                    c = 0;
                }
            } else if (action.equals(CollectUserFingersHandler.ACTION_SIM_STATE_CHANGED)) {
                c = 2;
            }
            switch (c) {
                case 0:
                    if (intent.getParcelableExtra("newState") != null && (intent.getParcelableExtra("newState") instanceof SupplicantState)) {
                        SupplicantState mCurrentsupplicantState = (SupplicantState) intent.getParcelableExtra("newState");
                        if (mCurrentsupplicantState != null) {
                            LogUtil.i("SUPPLICANT_STATE_CHANGED_ACTION mCurrentsupplicantState = " + mCurrentsupplicantState);
                            if (mCurrentsupplicantState == SupplicantState.COMPLETED) {
                                CollectUserFingersHandler.this.mMachineHandler.sendEmptyMessage(92);
                                break;
                            }
                        }
                    }
                    break;
                case 1:
                    LogUtil.i("MP-LINK state changes");
                    Message msg = Message.obtain(CollectUserFingersHandler.this.mMachineHandler, 91);
                    msg.arg1 = 4;
                    int network = intent.getIntExtra("MPLinkSuccessNetworkKey", 802);
                    if (network == 800) {
                        msg.arg2 = 1;
                    } else if (network == 801) {
                        msg.arg2 = 0;
                    } else {
                        msg.arg2 = 8;
                    }
                    CollectUserFingersHandler.this.mMachineHandler.sendMessage(msg);
                    break;
                case 2:
                    CollectUserFingersHandler.this.setCurrScrbId();
                    LogUtil.i("SIM state changes");
                    break;
                case 3:
                    CollectUserFingersHandler.this.setCHRQoEPrefManualSwitch();
                    break;
            }
        }
    };
    private String mPrefNetworkName = "UNKNOWN";
    private int mPrefNetworkType = 8;
    private String mSourceNetworkFreq = "UNKNOWN";
    private String mSourceNetworkId = "UNKNOWN";
    private String mSourceNetworkName = "UNKNOWN";
    private int mSourceNetworkType = 8;
    private HashMap<String, SpaceExpInfo> mSpaceExperience = new HashMap<>();
    private HashMap<String, SpaceExpInfo> mSpaceSessionDuration = new HashMap<>();
    private SpaceUserDAO mSpaceUserDAO;
    private StringBuilder mSpaceid = new StringBuilder("0");
    private StringBuilder mSpaceid_mainAp = new StringBuilder("0");
    private boolean mUserHasPref = false;
    private boolean mUserOperation = false;
    private Map<String, String> mapCollectFileName = new HashMap();
    /* access modifiers changed from: private */
    public HashMap<Integer, Integer> netCodes = new HashMap<>();
    private String oldBssid = "UNKNOWN";
    private String oldCellId = "UNKNOWN";
    private String oldRat = "UNKNOWN";
    private String oldSsid = "UNKNOWN";
    private long outof4G_begin = 0;
    private ParameterInfo param;
    private Runnable periodicOutof4GHandler = new Runnable() {
        public void run() {
            LogUtil.i("periodically check no 4G coverage");
            CollectUserFingersHandler.this.checkOutOf4GCoverage(false);
        }
    };
    private HashMap<String, HwWmpAppInfo> saveAppInfo = new HashMap<>();

    public CollectUserFingersHandler(Handler handler2) {
        LogUtil.i(" ,new CollectUserFingersHandler ");
        try {
            this.mContext = ContextManager.getInstance().getContext();
            Map<String, String> map = this.mapCollectFileName;
            map.put("wifipro", Constant.getRawDataPath() + "network" + Constant.RAW_FILE_WIFIPRO_EXTENSION);
            this.param = ParamManager.getInstance().getParameterInfo();
            this.mFastBackLteMgr = HwWmpFastBackLteManager.getInstance();
            this.mSpaceUserDAO = SpaceUserDAO.getInstance();
            this.mLocationDAO = new LocationDAO();
            this.mHisQoEChrDAO = new HisQoEChrDAO();
            this.mHisQoEChrService = QueryHistAppQoeService.getInstance();
            this.mFastBack2LteChrDAO = new FastBack2LteChrDAO();
            this.mMachineHandler = handler2;
            this.mHistoryQoeMgr = HwArbitrationHistoryQoeManager.getInstance(handler2);
            this.mCollectManger = new LogCollectManager(this.mContext);
            this.mCollectPowerHandler = CollectPowerHandler.getInstance(handler2);
        } catch (Exception e) {
            LogUtil.e(TAG + " CollectUserFingersHandler " + e.getMessage());
        }
    }

    public static synchronized CollectUserFingersHandler getInstance(Handler handler2) {
        CollectUserFingersHandler collectUserFingersHandler;
        synchronized (CollectUserFingersHandler.class) {
            if (mCollectUserFingersHandler == null) {
                mCollectUserFingersHandler = new CollectUserFingersHandler(handler2);
            }
            collectUserFingersHandler = mCollectUserFingersHandler;
        }
        return collectUserFingersHandler;
    }

    public static synchronized CollectUserFingersHandler getInstance() {
        CollectUserFingersHandler collectUserFingersHandler;
        synchronized (CollectUserFingersHandler.class) {
            collectUserFingersHandler = mCollectUserFingersHandler;
        }
        return collectUserFingersHandler;
    }

    public void setFreqLocation(String location) {
        clearUserPrefCHR();
        clearQoEPrefCHR();
        if (!this.mFastBack2LteChrDAO.getLocation().equals(location)) {
            this.mFastBack2LteChrDAO.resetSession();
            this.mFastBack2LteChrDAO.insertRecordByLoc();
            this.mFastBack2LteChrDAO.setLocation(location);
            if (!this.mFastBack2LteChrDAO.getCountersByLocation()) {
                this.mFastBack2LteChrDAO.insertRecordByLoc();
            }
        }
        if (!this.mHisQoEChrDAO.getLocation().equals(location)) {
            this.mHisQoEChrDAO.insertRecordByLoc();
            this.mHisQoEChrDAO.setLocation(location);
            this.mHisQoEChrDAO.getCountersByLocation();
        }
        this.freqLoc = location;
        this.mSpaceUserDAO.setFreqLocation(location);
    }

    public void setModelVer(int model_allAp, int model_mainAp) {
        LogUtil.i(" set Model version: " + model_allAp + "_" + model_mainAp);
        this.mSpaceUserDAO.setModelVer(model_allAp, model_mainAp);
    }

    public void setCurrScrbId() {
        this.mSpaceUserDAO.setScrbId(NetUtil.getMobileDataScrbId(this.mContext, this.mCollectManger));
    }

    public void setBatch(int batch) {
        this.locBatch = batch;
    }

    public final void startCollect() {
        try {
            LogUtil.i(" startCollect ");
            setCurrScrbId();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
            filter.addAction(ACTION_SIM_STATE_CHANGED);
            filter.addAction("com.android.server.hidata.arbitration.HwArbitrationStateMachine");
            filter.addAction(HwArbitrationDEFS.ACTION_HiData_DATA_ROVE_IN);
            this.mContext.registerReceiver(this.mNetworkReceiver, filter, "com.huawei.hidata.permission.MPLINK_START_CHECK", null);
            int networkType = NetUtil.getNetworkTypeInfo(this.mContext);
            if (1 == networkType) {
                setNewStartTime(new HwWmpAppInfo(1));
                LogUtil.d(" Wifi active ");
                backupWifiInfo();
            } else {
                resetTime(Constant.USERDB_APP_NAME_WIFI);
            }
            if (networkType == 0) {
                setNewStartTime(new HwWmpAppInfo(0));
                LogUtil.d(" Mobile active ");
                backupCellInfo();
            } else {
                resetTime(Constant.USERDB_APP_NAME_MOBILE);
            }
            regNetworkCallback();
            checkOutOf4GCoverage(false);
        } catch (Exception e) {
            LogUtil.e(" startCollect " + e.getMessage());
        }
    }

    private void regNetworkCallback() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        if (connectivityManager != null) {
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    LogUtil.i("NetworkCallback: onAvailable = " + network.hashCode());
                    NetworkInfo netInfo = connectivityManager.getNetworkInfo(network);
                    if (netInfo != null) {
                        LogUtil.v("NetworkCallback: networksInfo is " + netInfo.toString());
                        if (!netInfo.isConnected()) {
                            return;
                        }
                        if (netInfo.getType() == 1 || netInfo.getType() == 0) {
                            CollectUserFingersHandler.this.netCodes.put(Integer.valueOf(netInfo.getType()), Integer.valueOf(network.hashCode()));
                            LogUtil.i("send START to SM: type=" + netInfo.getType());
                            Message msg = Message.obtain(CollectUserFingersHandler.this.mMachineHandler, 91);
                            msg.arg1 = 1;
                            msg.arg2 = netInfo.getType();
                            CollectUserFingersHandler.this.mMachineHandler.sendMessage(msg);
                        }
                    }
                }

                public void onLosing(Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    LogUtil.i("NetworkCallback: onLosing");
                }

                public void onLost(Network network) {
                    LogUtil.i("NetworkCallback: onLost = " + network.hashCode());
                    LogUtil.i("onLost: netCodes = " + CollectUserFingersHandler.this.netCodes.toString());
                    for (Map.Entry<Integer, Integer> entry : CollectUserFingersHandler.this.netCodes.entrySet()) {
                        int type = entry.getKey().intValue();
                        if (entry.getValue().intValue() == network.hashCode()) {
                            LogUtil.i("send END to SM: type=" + type);
                            Message msg = Message.obtain(CollectUserFingersHandler.this.mMachineHandler, 91);
                            msg.arg1 = 2;
                            msg.arg2 = type;
                            CollectUserFingersHandler.this.mMachineHandler.sendMessage(msg);
                        }
                    }
                    super.onLost(network);
                }
            };
            ConnectivityManager.NetworkCallback networkCallbackDef = new ConnectivityManager.NetworkCallback() {
                public void onAvailable(Network network) {
                    super.onAvailable(network);
                    LogUtil.i("NetworkCallbackD: onAvailable=" + network.hashCode());
                    NetworkInfo netInfo = connectivityManager.getNetworkInfo(network);
                    if (netInfo != null) {
                        LogUtil.v("NetworkCallbackD: networksInfo is " + netInfo.toString());
                        if (netInfo.getType() == 1) {
                            if (netInfo.isConnected()) {
                                LogUtil.i("NetworkCallbackD: wifi connected");
                            } else {
                                LogUtil.i("NetworkCallbackD: wifi connecting");
                            }
                        }
                        if (netInfo.getType() == 0) {
                            if (netInfo.isConnected()) {
                                LogUtil.i("NetworkCallbackD: mobile connected");
                            } else {
                                LogUtil.i("NetworkCallbackD: mobile connecting");
                            }
                        }
                        Message msg = Message.obtain(CollectUserFingersHandler.this.mMachineHandler, 91);
                        msg.arg1 = 3;
                        msg.arg2 = 8;
                        CollectUserFingersHandler.this.mMachineHandler.sendMessage(msg);
                    }
                }

                public void onLosing(Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                    LogUtil.i("NetworkCallbackD: onLosing");
                }

                public void onLost(Network network) {
                    LogUtil.i("NetworkCallbackD: onLost=" + network.hashCode());
                    Message msg = Message.obtain(CollectUserFingersHandler.this.mMachineHandler, 91);
                    msg.arg1 = 3;
                    msg.arg2 = 8;
                    CollectUserFingersHandler.this.mMachineHandler.sendMessage(msg);
                    super.onLost(network);
                }
            };
            connectivityManager.registerNetworkCallback(new NetworkRequest.Builder().build(), networkCallback);
            connectivityManager.registerDefaultNetworkCallback(networkCallbackDef);
        }
    }

    public void updateSourceNetwork() {
        try {
            Bundle connectivity = NetUtil.getConnectedNetworkState(this.mContext);
            String networkId = "UNKNOWN";
            String networkName = "UNKNOWN";
            String networkFreq = "UNKNOWN";
            int defaultType = connectivity.getInt("defaultType");
            if (!this.isDestNetworkCellular || defaultType != 0) {
                if (1 == defaultType || defaultType == 0) {
                    networkId = connectivity.getString("defaultNwId", "UNKNOWN");
                    networkName = connectivity.getString("defaultNwName", "UNKNOWN");
                    networkFreq = connectivity.getString("defaultNwFreq", "UNKNOWN");
                }
                this.mSourceNetworkId = networkId;
                this.mSourceNetworkName = networkName;
                this.mSourceNetworkFreq = networkFreq;
                this.mSourceNetworkType = defaultType;
                LogUtil.v(" updateSourceNetwork: mSourceNetworkId: " + this.mSourceNetworkId + " mSourceNetworkName" + this.mSourceNetworkName);
                return;
            }
            LogUtil.i("Disable WIFI without connection ");
        } catch (Exception e) {
            LogUtil.e(" updateSourceNetwork " + e.getMessage());
        }
    }

    public void updateUserAction(String action, String apkname) {
        try {
            this.isDestNetworkCellular = action.equals("ACTION_ENABLE_WIFI_FALSE");
            if (!this.mUserOperation) {
                this.mUserOperation = true;
                this.mMachineHandler.sendEmptyMessage(115);
            }
            if (!action.equals("ACTION_ENABLE_WIFI_TRUE")) {
                clearSavedPreference();
            }
            this.mDestNetworkId = "UNKNOWN";
            this.mDestNetworkName = "UNKNOWN";
            this.mDestNetworkFreq = "UNKNOWN";
            this.mDestNetworkType = 8;
            LogUtil.d(" updateUserAction: action: " + action + " apkname:" + apkname + " isDestNetworkCellular:" + this.isDestNetworkCellular);
        } catch (Exception e) {
            LogUtil.e(" updateUserAction " + e.getMessage());
        }
    }

    private void updateUserPreference() {
        SpaceExpInfo sourceNetworkSpaceInfo = null;
        SpaceExpInfo destNetworkSpaceInfo = null;
        try {
            if (this.mSourceNetworkName.equals(this.mDestNetworkName)) {
                this.mUserOperation = false;
                LogUtil.i("mSourceNetworkName = mDestNetworkName, mUserOperation = false");
            }
            if (this.mUserOperation) {
                if (!this.mSourceNetworkId.equals("UNKNOWN") || !this.mDestNetworkId.equals("UNKNOWN")) {
                    if (this.mSpaceExperience.containsKey(this.mSourceNetworkId)) {
                        sourceNetworkSpaceInfo = this.mSpaceExperience.get(this.mSourceNetworkId);
                    }
                    if (sourceNetworkSpaceInfo == null) {
                        SpaceExpInfo spaceExpInfo = new SpaceExpInfo(this.mSpaceid, this.mSpaceid_mainAp, this.mSourceNetworkId, this.mSourceNetworkName, this.mSourceNetworkFreq, this.mSourceNetworkType);
                        sourceNetworkSpaceInfo = spaceExpInfo;
                    }
                    sourceNetworkSpaceInfo.accUserPrefOptOut();
                    this.mSpaceExperience.put(this.mSourceNetworkId, sourceNetworkSpaceInfo);
                    LogUtil.i("sourceNetworkSpaceInfo: " + sourceNetworkSpaceInfo.toString());
                    if (this.mSpaceExperience.containsKey(this.mDestNetworkId)) {
                        destNetworkSpaceInfo = this.mSpaceExperience.get(this.mDestNetworkId);
                    }
                    if (destNetworkSpaceInfo == null) {
                        SpaceExpInfo spaceExpInfo2 = new SpaceExpInfo(this.mSpaceid, this.mSpaceid_mainAp, this.mDestNetworkId, this.mDestNetworkName, this.mDestNetworkFreq, this.mDestNetworkType);
                        destNetworkSpaceInfo = spaceExpInfo2;
                    }
                    destNetworkSpaceInfo.accUserPrefOptIn();
                    this.mSpaceExperience.put(this.mDestNetworkId, destNetworkSpaceInfo);
                    LogUtil.i("destNetworkSpaceInfo: " + destNetworkSpaceInfo.toString());
                }
            } else if (this.mCHRUserPrefDetermineSwitch && this.mCHRUserPrefAutoSwitch) {
                if (this.mSpaceExperience.containsKey(this.mCHRUserPrefOriginalNWId)) {
                    sourceNetworkSpaceInfo = this.mSpaceExperience.get(this.mCHRUserPrefOriginalNWId);
                }
                if (sourceNetworkSpaceInfo == null) {
                    SpaceExpInfo spaceExpInfo3 = new SpaceExpInfo(this.mSpaceid, this.mSpaceid_mainAp, this.mCHRUserPrefOriginalNWId, this.mCHRUserPrefOriginalNWName, this.mCHRUserPrefOriginalNWFreq, this.mCHRUserPrefOriginalNWType);
                    sourceNetworkSpaceInfo = spaceExpInfo3;
                }
                sourceNetworkSpaceInfo.accUserPrefOptOut();
                this.mSpaceExperience.put(this.mCHRUserPrefOriginalNWId, sourceNetworkSpaceInfo);
                LogUtil.i("WM Auto Switch from Network: " + sourceNetworkSpaceInfo.toString());
                if (this.mSpaceExperience.containsKey(this.mCHRUserPrefSwitchNWId)) {
                    destNetworkSpaceInfo = this.mSpaceExperience.get(this.mCHRUserPrefSwitchNWId);
                }
                if (destNetworkSpaceInfo == null) {
                    SpaceExpInfo spaceExpInfo4 = new SpaceExpInfo(this.mSpaceid, this.mSpaceid_mainAp, this.mCHRUserPrefSwitchNWId, this.mCHRUserPrefSwitchNWName, this.mCHRUserPrefSwitchNWFreq, this.mCHRUserPrefSwitchNWType);
                    destNetworkSpaceInfo = spaceExpInfo4;
                }
                destNetworkSpaceInfo.accUserPrefOptIn();
                this.mSpaceExperience.put(this.mCHRUserPrefSwitchNWId, destNetworkSpaceInfo);
                LogUtil.i(" -> " + destNetworkSpaceInfo.toString());
            } else if (this.mSpaceSessionDuration.size() != 0) {
                SpaceExpInfo maxEntry = null;
                SpaceExpInfo spaceExpEntry = null;
                for (Map.Entry<String, SpaceExpInfo> entry : this.mSpaceSessionDuration.entrySet()) {
                    SpaceExpInfo value = entry.getValue();
                    LogUtil.i(" mSpaceSessionDuration:" + value.toString());
                    if (maxEntry == null || maxEntry.getDuration() < value.getDuration()) {
                        maxEntry = value;
                    }
                }
                if (maxEntry != null && maxEntry.getDuration() > 0 && !maxEntry.getNetworkId().equals("UNKNOWN")) {
                    if (this.mSpaceExperience.containsKey(maxEntry.getNetworkId())) {
                        spaceExpEntry = this.mSpaceExperience.get(maxEntry.getNetworkId());
                    }
                    if (spaceExpEntry == null) {
                        SpaceExpInfo spaceExpInfo5 = new SpaceExpInfo(this.mSpaceid, this.mSpaceid_mainAp, maxEntry.getNetworkId(), maxEntry.getNetworkName(), maxEntry.getNetworkFreq(), maxEntry.getNetworkType());
                        spaceExpEntry = spaceExpInfo5;
                    }
                    spaceExpEntry.accUserPrefStay();
                    this.mSpaceExperience.put(maxEntry.getNetworkId(), spaceExpEntry);
                    LogUtil.i("No manual operation, USER_PREF_STAY Network: " + spaceExpEntry.toString());
                }
            }
            LogUtil.i(" updateUserPreference record size: " + this.mSpaceExperience.size());
        } catch (Exception e) {
            LogUtil.e(" updateUserPreference " + e.getMessage());
        }
    }

    private void clearUserPreference() {
        this.mUserOperation = false;
        this.mSourceNetworkId = "UNKNOWN";
        this.mSourceNetworkName = "UNKNOWN";
        this.mSourceNetworkFreq = "UNKNOWN";
        this.mSourceNetworkType = 8;
        this.mDestNetworkId = "UNKNOWN";
        this.mDestNetworkName = "UNKNOWN";
        this.mDestNetworkFreq = "UNKNOWN";
        this.mDestNetworkType = 8;
        this.isDestNetworkCellular = false;
        this.mUserHasPref = false;
        this.mPrefNetworkName = "UNKNOWN";
        this.mPrefNetworkType = 8;
    }

    public void startAppCollect(HwWmpAppInfo appInfo) {
        LogUtil.i(" startAppCollect ");
        try {
            String app = appInfo.getAppName();
            if (app == null) {
                LogUtil.i(" not handle this APP:" + appInfo.getScenceId());
                return;
            }
            setNewStartTime(appInfo);
            LogUtil.d(" startAppCollect, scenes:" + appInfo.getScenceId() + ", app:" + app + ", appNwType:" + appInfo.getConMgrNetworkType());
        } catch (Exception e) {
            LogUtil.e(" startAppCollect " + e.getMessage());
        }
    }

    public void endAppCollect(HwWmpAppInfo appInfo) {
        LogUtil.i(" endAppCollect ");
        try {
            String app = appInfo.getAppName();
            if (app == null) {
                LogUtil.i(" APP is null: scenesId=" + appInfo.getScenceId());
            } else if (this.saveAppInfo.isEmpty()) {
                LogUtil.w(" no saved APP");
            } else if (!this.saveAppInfo.containsKey(app)) {
                LogUtil.d(" no this APP: scenesId=" + appInfo.getScenceId());
            } else {
                int network = appInfo.getConMgrNetworkType();
                Bundle state = getSpecifiedDataState(network);
                if (!state.getBoolean("VALID")) {
                    LogUtil.w(" network info is null");
                    return;
                }
                String nwName = state.getString("NAME");
                String nwId = state.getString("ID");
                String nwFreq = state.getString("FREQ");
                String signal = state.getString("SIGNAL");
                int signalVal = 0;
                if (signal != null && !signal.equalsIgnoreCase("") && !signal.equalsIgnoreCase("UNKNOWN")) {
                    signalVal = Integer.parseInt(signal.trim());
                }
                int signalVal2 = signalVal;
                LogUtil.d(" endAppCollect, scenes:" + appInfo.getScenceId() + ", app:" + app);
                LogUtil.v("              , appNwType:" + network + ", nwId:" + nwId + ", signal:" + signalVal2);
                updateDurationbyNwId(app, nwId, nwName, nwFreq, network, signalVal2);
                resetTime(appInfo.getAppName());
                LogUtil.i(" endAppCollect, app:" + app + ", nwType:" + appInfo.getConMgrNetworkType());
            }
        } catch (Exception e) {
            LogUtil.e(" endAppCollect " + e.getMessage());
        }
    }

    public void updateAppNetwork(String app, int newAppNw) {
        LogUtil.i(" updateAppNetwork ");
        if (app == null) {
            try {
                LogUtil.i(" APP is null");
            } catch (Exception e) {
                LogUtil.e(" updateAppNetwork " + e.getMessage());
            }
        } else if (this.saveAppInfo.isEmpty()) {
            LogUtil.w(" no saved APP");
        } else if (!this.saveAppInfo.containsKey(app)) {
            LogUtil.d(" no this APP in containsKey");
        } else {
            int oldAppNw = this.saveAppInfo.get(app).getConMgrNetworkType();
            if (newAppNw == oldAppNw) {
                LogUtil.d(" new == old, not to update");
                return;
            }
            Bundle state = getSpecifiedDataState(oldAppNw);
            if (!state.getBoolean("VALID")) {
                LogUtil.w(" old network info is null");
                updateStartTime(app, newAppNw);
                return;
            }
            String nwName = state.getString("NAME");
            String nwId = state.getString("ID");
            String nwFreq = state.getString("FREQ");
            String signal = state.getString("SIGNAL");
            int signalVal = 0;
            if (signal != null && !signal.equalsIgnoreCase("") && !signal.equalsIgnoreCase("UNKNOWN")) {
                signalVal = Integer.parseInt(signal.trim());
            }
            int signalVal2 = signalVal;
            LogUtil.d(" updateAppNetwork, scenes:" + this.saveAppInfo.get(app).getScenceId() + ", app:" + app);
            LogUtil.v("                 , app:" + app + ", old appNwType:" + oldAppNw + ", nwId:" + nwId + ", new appNwType:" + newAppNw + ", signal:" + signalVal2);
            updateDurationbyNwId(app, nwId, nwName, nwFreq, oldAppNw, signalVal2);
            updateStartTime(app, newAppNw);
        }
    }

    public void updateAppQoE(String app, int levelQoE) {
        int signalVal;
        String str = app;
        int i = levelQoE;
        LogUtil.i(" updateAppQoE ");
        SpaceExpInfo spaceInfo = null;
        if (str == null) {
            try {
                LogUtil.i(" APP is null");
            } catch (Exception e) {
                LogUtil.e(" updateAppQoE " + e.getMessage());
            }
        } else if (this.saveAppInfo.isEmpty()) {
            LogUtil.w(" no saved APP");
        } else if (!this.saveAppInfo.containsKey(str)) {
            LogUtil.d(" no this APP");
        } else {
            int network = this.saveAppInfo.get(str).getConMgrNetworkType();
            Bundle state = getSpecifiedDataState(network);
            if (!state.getBoolean("VALID")) {
                LogUtil.w(" network info is null");
                return;
            }
            String nwName = state.getString("NAME");
            String nwId = state.getString("ID");
            String nwFreq = state.getString("FREQ");
            String signal = state.getString("SIGNAL");
            if (this.mSpaceExperience.containsKey(nwId)) {
                spaceInfo = this.mSpaceExperience.get(nwId);
            }
            if (spaceInfo == null) {
                SpaceExpInfo spaceExpInfo = new SpaceExpInfo(this.mSpaceid, this.mSpaceid_mainAp, nwId, nwName, nwFreq, network);
                spaceInfo = spaceExpInfo;
            }
            if (i == 1) {
                spaceInfo.accAppPoor(str);
            }
            if (i == 2) {
                spaceInfo.accAppGood(str);
            }
            if (signal == null || signal.equalsIgnoreCase("") || signal.equalsIgnoreCase("UNKNOWN")) {
                signalVal = 0;
            } else {
                signalVal = Integer.parseInt(signal.trim());
                spaceInfo.accSignalValue(signalVal);
            }
            LogUtil.d(" updateAppQoE, app:" + str + ", level:" + i + ", poor count=" + spaceInfo.getAppQoePoor(str) + ", good count=" + spaceInfo.getAppQoeGood(str) + ", signal:" + signalVal);
            StringBuilder sb = new StringBuilder();
            sb.append("              nwId:");
            sb.append(nwId);
            LogUtil.v(sb.toString());
            this.mSpaceExperience.put(nwId, spaceInfo);
            StringBuilder sb2 = new StringBuilder();
            sb2.append(" updateAppQoE record size: ");
            sb2.append(this.mSpaceExperience.size());
            sb2.append("spaceInfo:");
            sb2.append(spaceInfo.toString());
            LogUtil.i(sb2.toString());
        }
    }

    public void updateWifiDurationForAp(boolean checkChanges) {
        LogUtil.i(" updateWifiDurationForAp ");
        try {
            Bundle wifiInfo = NetUtil.getWifiStateString(this.mContext);
            String newBssid = wifiInfo.getString("wifiMAC", "UNKNOWN");
            String newSsid = wifiInfo.getString("wifiAp", "UNKNOWN");
            String newState = wifiInfo.getString("wifiState", "UNKNOWN");
            if (newBssid == null) {
                newBssid = "UNKNOWN";
            }
            String newBssid2 = newBssid;
            String oldBssid2 = lastWifiState.getString("wifiMAC", "UNKNOWN");
            String oldSsid2 = lastWifiState.getString("wifiAp", "UNKNOWN");
            String oldFreq = lastWifiState.getString("wifiCh", "UNKNOWN");
            String signal = lastWifiState.getString("wifiRssi", "0");
            if (oldBssid2 == null) {
                oldBssid2 = "UNKNOWN";
            }
            String oldBssid3 = oldBssid2;
            if (!newSsid.equals(oldSsid2) || !newBssid2.equals(oldBssid3)) {
                LogUtil.d(" wifi id changed");
                LogUtil.i("                , new=" + newSsid + LogHelper.SEPARATOR + newBssid2 + " , old=" + oldSsid2 + LogHelper.SEPARATOR + oldBssid3);
            } else {
                LogUtil.i(" wifi id NOT changed:" + newSsid);
                if (checkChanges) {
                    return;
                }
            }
            backupWifiInfo();
            int signalVal = 0;
            if (signal != null && !signal.equalsIgnoreCase("") && !signal.equalsIgnoreCase("UNKNOWN")) {
                signalVal = Integer.parseInt(signal.trim());
            }
            int signalVal2 = signalVal;
            LogUtil.i(" updateWifiDurationForAp, save to :" + oldSsid2);
            if (!this.saveAppInfo.isEmpty()) {
                LogUtil.i("                   , saveAppInfo.size=" + this.saveAppInfo.size());
                Iterator<Map.Entry<String, HwWmpAppInfo>> it = this.saveAppInfo.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry next = it.next();
                    String app = (String) next.getKey();
                    HwWmpAppInfo info = (HwWmpAppInfo) next.getValue();
                    int nwType = info.getConMgrNetworkType();
                    StringBuilder sb = new StringBuilder();
                    Bundle wifiInfo2 = wifiInfo;
                    sb.append(" saveAppInfo, app: ");
                    sb.append(info.getAppName());
                    sb.append(", network=");
                    sb.append(nwType);
                    sb.append(", startTime=");
                    Map.Entry entry = next;
                    Iterator<Map.Entry<String, HwWmpAppInfo>> it2 = it;
                    sb.append(info.getStartTime());
                    LogUtil.d(sb.toString());
                    if (nwType != 0) {
                        HwWmpAppInfo hwWmpAppInfo = info;
                        updateDurationbyNwId(app, oldBssid3, oldSsid2, oldFreq, nwType, signalVal2);
                    }
                    wifiInfo = wifiInfo2;
                    it = it2;
                }
            }
            if (!newState.equals("ENABLED") || newBssid2.contains("UNKNOWN")) {
                LogUtil.d(" current wifi == null");
                resetTime(Constant.USERDB_APP_NAME_WIFI);
            }
        } catch (Exception e) {
            LogUtil.e(" updateWifiDurationByAp " + e.getMessage());
        }
    }

    private void updateWifiDurationEnd() {
        LogUtil.i(" updateWifiDurationEnd ");
        try {
            String oldBssid2 = lastWifiState.getString("wifiMAC", "UNKNOWN");
            String oldSsid2 = lastWifiState.getString("wifiAp", "UNKNOWN");
            String oldFreq = lastWifiState.getString("wifiCh", "UNKNOWN");
            String signal = lastWifiState.getString("wifiRssi", "0");
            if (oldBssid2 == null) {
                oldBssid2 = "UNKNOWN";
            }
            int signalVal = 0;
            if (signal != null && !signal.equalsIgnoreCase("") && !signal.equalsIgnoreCase("UNKNOWN")) {
                signalVal = Integer.parseInt(signal.trim());
            }
            int signalVal2 = signalVal;
            LogUtil.i(" updateWifiDurationEnd, save to : " + oldSsid2);
            if (!this.saveAppInfo.isEmpty()) {
                LogUtil.i("                   , saveAppInfo.size=" + this.saveAppInfo.size());
                for (Map.Entry next : this.saveAppInfo.entrySet()) {
                    String app = (String) next.getKey();
                    HwWmpAppInfo info = (HwWmpAppInfo) next.getValue();
                    int nwType = info.getConMgrNetworkType();
                    LogUtil.d(" saveAppInfo, app: " + info.getAppName() + ", network=" + nwType + ", startTime=" + info.getStartTime());
                    if (nwType != 0) {
                        updateDurationbyNwId(app, oldBssid2, oldSsid2, oldFreq, nwType, signalVal2);
                    }
                }
            }
            resetTime(Constant.USERDB_APP_NAME_WIFI);
        } catch (RuntimeException e) {
            LogUtil.e(" updateWifiDurationEnd, RuntimeException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e(" updateWifiDurationEnd " + e2.getMessage());
        }
    }

    private void backupWifiInfo() {
        LogUtil.i(" backupWifiInfo ");
        try {
            Bundle wifiInfo = NetUtil.getWifiStateString(this.mContext);
            String newState = wifiInfo.getString("wifiState", "UNKNOWN");
            String newBssid = wifiInfo.getString("wifiMAC", "UNKNOWN");
            if (newState.equalsIgnoreCase("ENABLED") && newBssid != null && !newBssid.contains("UNKNOWN")) {
                lastWifiState = wifiInfo.deepCopy();
                LogUtil.i(" wifi ENABLED, backup info: ssid=" + lastWifiState.getString("wifiAp") + ", " + lastWifiState.getString("wifiState"));
            }
        } catch (RuntimeException e) {
            LogUtil.e(" backupWifiInfo, RuntimeException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e(" backupWifiInfo, e: " + e2.getMessage());
        }
    }

    public boolean updateMobileDurationForCell(boolean checkChanges) {
        LogUtil.i(" updateMobileDurationForCell ");
        try {
            Bundle cellInfo = NetUtil.getMobileDataState(this.mContext);
            String newRat = cellInfo.getString("cellRAT", "UNKNOWN");
            String newId = cellInfo.getString("cellId", "UNKNOWN");
            if (newId == null) {
                newId = "UNKNOWN";
            }
            String newId2 = newId;
            String oldRat2 = lastCellState.getString("cellRAT", "UNKNOWN");
            String oldId = lastCellState.getString("cellId", "UNKNOWN");
            String oldFreq = lastCellState.getString("cellFreq", "UNKNOWN");
            String signal = cellInfo.getString("cellRssi", "0");
            if (oldId == null) {
                oldId = "UNKNOWN";
            }
            String oldId2 = oldId;
            if (checkChanges) {
                LogUtil.i(" check cell ID change ");
                if (newId2.equals(oldId2) && newRat.equals(oldRat2)) {
                    LogUtil.d(" cell id not changed ");
                    return false;
                }
            }
            backupCellInfo();
            int signalVal = 0;
            if (signal != null && !signal.equalsIgnoreCase("") && !signal.equalsIgnoreCase("UNKNOWN")) {
                signalVal = Integer.parseInt(signal.trim());
            }
            int signalVal2 = signalVal;
            LogUtil.i("updateMobileDurationForCell, save to cellId: ");
            if (!this.saveAppInfo.isEmpty()) {
                LogUtil.i("                   , saveAppInfo.size=" + this.saveAppInfo.size());
                Iterator<Map.Entry<String, HwWmpAppInfo>> it = this.saveAppInfo.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry next = it.next();
                    String app = (String) next.getKey();
                    HwWmpAppInfo info = (HwWmpAppInfo) next.getValue();
                    int nwType = info.getConMgrNetworkType();
                    StringBuilder sb = new StringBuilder();
                    sb.append(" saveAppInfo, app: ");
                    sb.append(info.getAppName());
                    sb.append(", network=");
                    sb.append(nwType);
                    sb.append(", startTime=");
                    Map.Entry entry = next;
                    Iterator<Map.Entry<String, HwWmpAppInfo>> it2 = it;
                    sb.append(info.getStartTime());
                    LogUtil.d(sb.toString());
                    if (1 != nwType) {
                        HwWmpAppInfo hwWmpAppInfo = info;
                        updateDurationbyNwId(app, oldId2, oldRat2, oldFreq, nwType, signalVal2);
                    }
                    it = it2;
                }
            }
        } catch (Exception e) {
            LogUtil.e(" updateMobileDurationForCell " + e.getMessage());
        }
        return true;
    }

    private void updateMobileDurationEnd() {
        LogUtil.i(" updateMobileDurationEnd ");
        try {
            Bundle cellInfo = NetUtil.getMobileDataState(this.mContext);
            String oldRat2 = lastCellState.getString("cellRAT", "UNKNOWN");
            String oldId = lastCellState.getString("cellId", "UNKNOWN");
            String oldFreq = lastCellState.getString("cellFreq", "UNKNOWN");
            String signal = cellInfo.getString("cellRssi", "0");
            if (oldId == null) {
                oldId = "UNKNOWN";
            }
            int signalVal = 0;
            if (signal != null && !signal.equalsIgnoreCase("") && !signal.equalsIgnoreCase("UNKNOWN")) {
                signalVal = Integer.parseInt(signal.trim());
            }
            LogUtil.i("updateMobileDurationEnd, save to cellId: ");
            if (!this.saveAppInfo.isEmpty()) {
                LogUtil.i("                   , saveAppInfo.size=" + this.saveAppInfo.size());
                for (Map.Entry next : this.saveAppInfo.entrySet()) {
                    String app = (String) next.getKey();
                    HwWmpAppInfo info = (HwWmpAppInfo) next.getValue();
                    int nwType = info.getConMgrNetworkType();
                    LogUtil.d(" saveAppInfo, app: " + info.getAppName() + ", network=" + nwType + ", startTime=" + info.getStartTime());
                    if (1 != nwType) {
                        updateDurationbyNwId(app, oldId, oldRat2, oldFreq, nwType, signalVal);
                    }
                }
            }
            resetTime(Constant.USERDB_APP_NAME_MOBILE);
        } catch (Exception e) {
            LogUtil.e(" updateMobileDurationEnd " + e.getMessage());
        }
    }

    private void backupCellInfo() {
        LogUtil.i(" backupCellInfo ");
        try {
            Bundle cellInfo = NetUtil.getMobileDataState(this.mContext);
            String cellState = cellInfo.getString("cellState", "UNKNOWN");
            String cellId = cellInfo.getString("cellId", "UNKNOWN");
            if (cellState.equalsIgnoreCase("ENABLED") && cellId != null && !cellId.contains("UNKNOWN")) {
                lastCellState = cellInfo.deepCopy();
                LogUtil.i(" cell ENABLED, backup info: cellRat=" + lastCellState.getString("cellRAT") + ", " + lastCellState.getString("cellState"));
            }
        } catch (RuntimeException e) {
            LogUtil.e(" backupCellInfo, RuntimeException: " + e.getMessage());
        } catch (Exception e2) {
            LogUtil.e(" backupCellInfo, e: " + e2.getMessage());
        }
    }

    private void updateDurationbyNwId(String app, String newId, String newNwName, String newNwFreq, int nw_type, int signal) {
        String str = app;
        String str2 = newNwName;
        LogUtil.i(" updateDurationbyNwId ");
        try {
            if (this.saveAppInfo.isEmpty()) {
                try {
                    LogUtil.w(" no saved APP");
                } catch (Exception e) {
                    e = e;
                    String str3 = newId;
                    int i = signal;
                    LogUtil.e(" updateDurationbyNwId " + e.getMessage());
                }
            } else if (this.saveAppInfo.get(str) == null) {
                LogUtil.d(" updateDurationbyNwId, no saved app ");
            } else if (newId == null || str2 == null || newNwFreq == null) {
                int i2 = signal;
                try {
                    LogUtil.d(" updateDurationbyNwId, network==null ");
                } catch (Exception e2) {
                    e = e2;
                    LogUtil.e(" updateDurationbyNwId " + e.getMessage());
                }
            } else {
                String newId2 = this.freqLoc.equals(Constant.NAME_FREQLOCATION_OTHER) ? newNwFreq : newId;
                SpaceExpInfo spaceInfo = null;
                SpaceExpInfo sessionSpaceInfo = null;
                try {
                    if (this.mSpaceExperience.containsKey(newId2)) {
                        spaceInfo = this.mSpaceExperience.get(newId2);
                    }
                    if (this.mSpaceSessionDuration.containsKey(newId2)) {
                        sessionSpaceInfo = this.mSpaceSessionDuration.get(newId2);
                    }
                    SpaceExpInfo sessionSpaceInfo2 = sessionSpaceInfo;
                    if (spaceInfo == null) {
                        SpaceExpInfo spaceExpInfo = new SpaceExpInfo(this.mSpaceid, this.mSpaceid_mainAp, newId2, str2, newNwFreq, nw_type);
                        spaceInfo = spaceExpInfo;
                    }
                    if (sessionSpaceInfo2 == null) {
                        SpaceExpInfo spaceExpInfo2 = new SpaceExpInfo(this.mSpaceid, this.mSpaceid_mainAp, newId2, str2, newNwFreq, nw_type);
                        sessionSpaceInfo2 = spaceExpInfo2;
                    }
                    long duration = System.currentTimeMillis() - this.saveAppInfo.get(str).getStartTime();
                    if (duration > 0) {
                        spaceInfo.accDuration(str, duration);
                        sessionSpaceInfo2.accDuration(str, duration);
                        LogUtil.i(" session duration: +" + duration);
                    }
                    if (Constant.USERDB_APP_NAME_WIFI.equals(str) || Constant.USERDB_APP_NAME_MOBILE.equals(str)) {
                        long[] dataTraffic = NetUtil.getTraffic(this.saveAppInfo.get(str).getStartTime(), System.currentTimeMillis(), nw_type, this.mContext);
                        spaceInfo.accDataTraffic(dataTraffic[0], dataTraffic[1]);
                        this.mCollectPowerHandler.requestPowerData(this.saveAppInfo.get(str).getStartTime(), newId2, str2, newNwFreq, nw_type);
                    }
                    try {
                        spaceInfo.accSignalValue(signal);
                        this.mSpaceExperience.put(newId2, spaceInfo);
                        this.mSpaceSessionDuration.put(newId2, sessionSpaceInfo2);
                        LogUtil.i(" updateDurationbyNwId record size: " + this.mSpaceExperience.size() + ", NW: " + str2 + ", spaceInfo:" + spaceInfo.toString());
                        this.saveAppInfo.get(str).setStartTime(System.currentTimeMillis());
                    } catch (Exception e3) {
                        e = e3;
                    }
                } catch (Exception e4) {
                    e = e4;
                    int i3 = signal;
                    LogUtil.e(" updateDurationbyNwId " + e.getMessage());
                }
            }
        } catch (Exception e5) {
            e = e5;
            int i4 = signal;
            LogUtil.e(" updateDurationbyNwId " + e.getMessage());
        }
    }

    private void saveSpaceExptoDatabaseNew(RecognizeResult spaceIds) {
        try {
            if (this.mSpaceExperience.size() != 0) {
                LogUtil.i(" loadSpaceExpfromDatabase");
                int num = 0;
                for (Map.Entry<String, SpaceExpInfo> entry : mergeSumOfMaps(this.mSpaceExperience, this.mSpaceUserDAO.findAllByTwoSpaces(spaceIds.getRgResult(), spaceIds.getMainApRgResult())).entrySet()) {
                    SpaceExpInfo val = (SpaceExpInfo) entry.getValue();
                    if (val != null) {
                        LogUtil.d(" saveSpaceExptoDatabaseNew:");
                        LogUtil.i("                            Records" + num + ": " + val.toString());
                        this.mSpaceUserDAO.insertBase(val);
                        this.mSpaceUserDAO.insertApp(val);
                        num++;
                    }
                }
                return;
            }
            LogUtil.d(" mSpaceExperience size=0");
        } catch (Exception e) {
            LogUtil.e(" saveSpaceExptoDatabaseNew " + e.getMessage());
        }
    }

    private HashMap<String, SpaceExpInfo> mergeSumOfMaps(HashMap<String, SpaceExpInfo>... maps) {
        SpaceExpInfo value;
        HashMap<String, SpaceExpInfo> resultMap = new HashMap<>();
        for (HashMap<String, SpaceExpInfo> map : maps) {
            for (Map.Entry<String, SpaceExpInfo> entry : map.entrySet()) {
                String key = entry.getKey();
                LogUtil.v(" mergeSumOfMaps: nwId=" + key);
                if (resultMap.containsKey(key)) {
                    value = entry.getValue();
                    value.mergeAllRecords(resultMap.get(key));
                } else {
                    value = entry.getValue();
                }
                resultMap.put(key, value);
            }
        }
        return resultMap;
    }

    public void assignSpaceExp2Space(RecognizeResult spaceIds, boolean allApSpaceChanged) {
        LogUtil.i(" assignSpaceExp2Space ");
        if (spaceIds == null) {
            try {
                LogUtil.i(" no spaceIds");
            } catch (Exception e) {
                LogUtil.e(" assignSpaceExp2Space " + e.getMessage());
            }
        } else {
            int spaceId_allAp = spaceIds.getRgResult();
            int spaceId_mainAp = spaceIds.getMainApRgResult();
            LogUtil.d(" assignSpaceExp2Space: changed=" + allApSpaceChanged + ", spaceId_allAp=" + spaceId_allAp + ", spaceId_mainAp=" + spaceId_mainAp + ", model ver=" + spaceIds.getAllApModelName() + "_" + spaceIds.getMainApModelName());
            updateWifiDurationForAp(false);
            updateMobileDurationForCell(false);
            if (allApSpaceChanged) {
                updateQoEPrefCHR();
                updateUserPrefCHR();
                updateUserPreference();
                clearUserPreference();
                clearUserPrefCHR();
                clearQoEPrefCHR();
                this.mSpaceSessionDuration.clear();
            }
            setSpaceId(spaceId_allAp, spaceId_mainAp);
            this.mCollectPowerHandler.setSpaceId(spaceId_allAp, spaceId_mainAp);
            saveSpaceExptoDatabaseNew(spaceIds);
            this.mSpaceExperience.clear();
            setSpaceId(0, 0);
        }
    }

    private void setSpaceId(int spaceId_all, int spaceId_main) {
        this.mSpaceid.setLength(0);
        this.mSpaceid.trimToSize();
        this.mSpaceid.append(spaceId_all);
        this.mSpaceid_mainAp.setLength(0);
        this.mSpaceid_mainAp.trimToSize();
        this.mSpaceid_mainAp.append(spaceId_main);
    }

    public Bundle getUserPrefNetwork(int spaceId_allAp) {
        int i = spaceId_allAp;
        LogUtil.i(" getUserPrefNetwork, allAp Space ");
        Bundle output = new Bundle();
        output.putBoolean("isUserHasPref", false);
        output.putInt("prefNetworkType", 8);
        output.putString("prefNetworkName", "UNKNOWN");
        Bundle bundle = output;
        LogUtil.d(" getUserPrefNetwork: spaceId_allAp=" + i);
        if (i == 0) {
            try {
                LogUtil.d(" spaceId_allAp is unknown");
                this.mLocationDAO.accCHRUserPrefUnknownSpacebyFreqLoc(this.freqLoc);
                return output;
            } catch (RuntimeException e) {
                e = e;
                LogUtil.e("getUserPrefNetwork RuntimeException,e:" + e.getMessage());
                LogUtil.d(" User has no preference");
                return output;
            } catch (Exception e2) {
                e = e2;
                LogUtil.e(" getUserPrefNetwork, e:" + e.getMessage());
                LogUtil.d(" User has no preference");
                return output;
            }
        } else {
            try {
                if (this.mUserOperation) {
                    LogUtil.d(" mUserOperation = true");
                    return output;
                }
                HashMap<String, Bundle> currentSpaceExp = this.mSpaceUserDAO.findUserPrefByAllApSpaces(i);
                Bundle calResult = calculateUserPreference(currentSpaceExp, true);
                HashMap<String, Bundle> hashMap = currentSpaceExp;
                if (calResult.getBoolean("isUserHasPref", false)) {
                    String preferNet = calResult.getString("prefNetworkName", "UNKNOWN");
                    try {
                        int preferType = calResult.getInt("prefNetworkType", 8);
                        if (!preferNet.equals("UNKNOWN")) {
                            if (8 != preferType) {
                                StringBuilder sb = new StringBuilder();
                                try {
                                    sb.append("getUserPrefNetwork, found preferred network: space=");
                                    sb.append(i);
                                    sb.append(", name=");
                                    sb.append(preferNet);
                                    LogUtil.i(sb.toString());
                                    this.mSpaceUserDAO.setUserPrefEnteryFlag(preferNet, preferType, i, 1);
                                } catch (RuntimeException e3) {
                                    e = e3;
                                    LogUtil.e("getUserPrefNetwork RuntimeException,e:" + e.getMessage());
                                    LogUtil.d(" User has no preference");
                                    return output;
                                } catch (Exception e4) {
                                    e = e4;
                                    LogUtil.e(" getUserPrefNetwork, e:" + e.getMessage());
                                    LogUtil.d(" User has no preference");
                                    return output;
                                }
                            }
                        }
                        LogUtil.i("getUserPrefNetwork, found preferred CELL network: space=" + i + ", name=" + preferNet);
                    } catch (RuntimeException e5) {
                        e = e5;
                        LogUtil.e("getUserPrefNetwork RuntimeException,e:" + e.getMessage());
                        LogUtil.d(" User has no preference");
                        return output;
                    } catch (Exception e6) {
                        e = e6;
                        LogUtil.e(" getUserPrefNetwork, e:" + e.getMessage());
                        LogUtil.d(" User has no preference");
                        return output;
                    }
                } else {
                    LogUtil.i("getUserPrefNetwork, preferred network NOT found: space=" + i);
                    this.mSpaceUserDAO.clearUserPrefEnteryFlag(i);
                }
                return calResult;
            } catch (RuntimeException e7) {
                e = e7;
                LogUtil.e("getUserPrefNetwork RuntimeException,e:" + e.getMessage());
                LogUtil.d(" User has no preference");
                return output;
            } catch (Exception e8) {
                e = e8;
                LogUtil.e(" getUserPrefNetwork, e:" + e.getMessage());
                LogUtil.d(" User has no preference");
                return output;
            }
        }
    }

    public Bundle getUserPrefNetworkbyMainAp(int spaceId_mainAp) {
        LogUtil.i(" getUserPrefNetworkbyMainAp ");
        Bundle output = new Bundle();
        new Bundle();
        output.putBoolean("isUserHasPref", false);
        output.putInt("prefNetworkType", 8);
        output.putString("prefNetworkName", "UNKNOWN");
        Bundle calResult = output;
        try {
            if (this.mUserOperation) {
                LogUtil.d(" mUserOperation = true");
                return output;
            }
            ArrayList<Integer> spaceAllList = this.mSpaceUserDAO.findAllApSpaceIdByMainApSpace(spaceId_mainAp, 1);
            String preferNet = null;
            int listnum = spaceAllList.size();
            int preferType = 8;
            Bundle calResult2 = calResult;
            int n = 0;
            while (n < listnum) {
                try {
                    int spaceId_allAp = spaceAllList.get(n).intValue();
                    LogUtil.i(" loopID:" + spaceId_allAp);
                    if (spaceId_allAp != 0) {
                        calResult2 = calculateUserPreference(this.mSpaceUserDAO.findUserPrefByAllApSpaces(spaceId_allAp), false);
                        if (!calResult2.getBoolean("isUserHasPref", false)) {
                            LogUtil.i("getUserPrefNetworkbyMainAp, NOT found preferred network: id=" + spaceId_allAp);
                            this.mSpaceUserDAO.clearUserPrefEnteryFlag(spaceId_allAp);
                            return output;
                        } else if (preferNet == null) {
                            preferNet = calResult2.getString("prefNetworkName", "UNKNOWN");
                            preferType = calResult2.getInt("prefNetworkType", 8);
                            LogUtil.i("getUserPrefNetworkbyMainAp, found preferred network:" + preferNet + ", id=" + spaceId_allAp);
                            this.mSpaceUserDAO.setUserPrefEnteryFlag(preferNet, preferType, spaceId_allAp, 1);
                        } else if (!preferNet.equals(calResult2.getString("prefNetworkName", "UNKNOWN")) || preferType != calResult2.getInt("prefNetworkType", 8) || preferNet.equals("UNKNOWN")) {
                            LogUtil.d("getUserPrefNetworkbyMainAp, not the same network:" + preferNet + ", id=" + spaceId_allAp);
                            this.mMachineHandler.sendEmptyMessage(116);
                            return output;
                        } else {
                            LogUtil.i(" found the same network:" + preferNet + ", id=" + spaceId_allAp);
                            this.mSpaceUserDAO.setUserPrefEnteryFlag(preferNet, preferType, spaceId_allAp, 1);
                        }
                    }
                    n++;
                } catch (RuntimeException e) {
                    e = e;
                    LogUtil.e("getUserPrefNetworkbyMainAp2 RuntimeException,e:" + e.getMessage());
                    LogUtil.d(" User has no preference");
                    return output;
                } catch (Exception e2) {
                    e = e2;
                    LogUtil.e(" getUserPrefNetworkbyMainAp2, e:" + e.getMessage());
                    LogUtil.d(" User has no preference");
                    return output;
                }
            }
            LogUtil.d("getUserPrefNetworkbyMainAp, results:" + calResult2.getBoolean("isUserHasPref", false) + ", net=" + preferNet);
            return calResult2;
        } catch (RuntimeException e3) {
            e = e3;
            Bundle bundle = calResult;
            LogUtil.e("getUserPrefNetworkbyMainAp2 RuntimeException,e:" + e.getMessage());
            LogUtil.d(" User has no preference");
            return output;
        } catch (Exception e4) {
            e = e4;
            Bundle bundle2 = calResult;
            LogUtil.e(" getUserPrefNetworkbyMainAp2, e:" + e.getMessage());
            LogUtil.d(" User has no preference");
            return output;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:107:0x032b  */
    private Bundle calculateUserPreference(HashMap<String, Bundle> currentSpaceExp, boolean needRecord) {
        double duration_threshold;
        int stayCount;
        int inoutCount;
        long totalDuration;
        Bundle result = new Bundle();
        Bundle maxEntry = null;
        long totalDuration2 = 0;
        int inoutCount2 = 0;
        int stayCount2 = 0;
        long cellDuration = 0;
        int cellUserPrefOptOut = 0;
        int cellUserPrefOptIn = 0;
        int cellUserPrefStay = 0;
        double duration_threshold2 = this.param.getUserPrefDurationRatio();
        try {
            result.putBoolean("isUserHasPref", false);
            result.putInt("prefNetworkType", 8);
            result.putString("prefNetworkName", "UNKNOWN");
            if (currentSpaceExp.size() != 0) {
                Iterator<Map.Entry<String, Bundle>> it = currentSpaceExp.entrySet().iterator();
                while (it.hasNext()) {
                    Iterator<Map.Entry<String, Bundle>> it2 = it;
                    double duration_threshold3 = duration_threshold2;
                    Bundle value = it.next().getValue();
                    try {
                        totalDuration = totalDuration2 + value.getLong("duration_connected");
                    } catch (RuntimeException e) {
                        e = e;
                    } catch (Exception e2) {
                        e = e2;
                        LogUtil.e(" calculateUserPreference, e:" + e.getMessage());
                        if (!result.getBoolean("isUserHasPref", false)) {
                        }
                        return result;
                    }
                    try {
                        inoutCount2 = value.getInt("user_pref_opt_in") + inoutCount2 + value.getInt("user_pref_opt_out");
                        stayCount2 += value.getInt("user_pref_stay");
                        if (value.getInt("networktype") == 0) {
                            cellDuration += value.getLong("duration_connected");
                            cellUserPrefOptOut += value.getInt("user_pref_opt_out");
                            cellUserPrefOptIn += value.getInt("user_pref_opt_in");
                            cellUserPrefStay += value.getInt("user_pref_stay");
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append(" Entry: Networkname:");
                        sb.append(value.getString("networkname"));
                        sb.append(" Type:");
                        sb.append(value.getInt("networktype"));
                        sb.append(" IN:");
                        sb.append(value.getInt("user_pref_opt_in"));
                        sb.append(" OUT:");
                        sb.append(value.getInt("user_pref_opt_out"));
                        sb.append(" STAY:");
                        sb.append(value.getInt("user_pref_stay"));
                        sb.append(" duration_connected:");
                        long totalDuration3 = totalDuration;
                        try {
                            sb.append(value.getLong("duration_connected"));
                            LogUtil.d(sb.toString());
                            if (maxEntry == null || maxEntry.getLong("duration_connected") < value.getLong("duration_connected")) {
                                maxEntry = value;
                            }
                            it = it2;
                            duration_threshold2 = duration_threshold3;
                            totalDuration2 = totalDuration3;
                        } catch (RuntimeException e3) {
                            e = e3;
                            long j = totalDuration3;
                            LogUtil.e("calculateUserPreference RuntimeException,e:" + e.getMessage());
                            if (!result.getBoolean("isUserHasPref", false)) {
                            }
                            return result;
                        } catch (Exception e4) {
                            e = e4;
                            long j2 = totalDuration3;
                            LogUtil.e(" calculateUserPreference, e:" + e.getMessage());
                            if (!result.getBoolean("isUserHasPref", false)) {
                            }
                            return result;
                        }
                    } catch (RuntimeException e5) {
                        e = e5;
                        long j3 = totalDuration;
                        LogUtil.e("calculateUserPreference RuntimeException,e:" + e.getMessage());
                        if (!result.getBoolean("isUserHasPref", false)) {
                        }
                        return result;
                    } catch (Exception e6) {
                        e = e6;
                        long j4 = totalDuration;
                        LogUtil.e(" calculateUserPreference, e:" + e.getMessage());
                        if (!result.getBoolean("isUserHasPref", false)) {
                        }
                        return result;
                    }
                }
                duration_threshold = duration_threshold2;
                if (maxEntry != null) {
                    if (maxEntry.getInt("user_pref_entery", 0) > 0) {
                        double duration_threshold4 = this.param.getUserPrefDurationRatio_Leave();
                        try {
                            LogUtil.d(" duration_threshold = " + duration_threshold4);
                            duration_threshold = duration_threshold4;
                        } catch (RuntimeException e7) {
                            e = e7;
                            double d = duration_threshold4;
                            LogUtil.e("calculateUserPreference RuntimeException,e:" + e.getMessage());
                            if (!result.getBoolean("isUserHasPref", false)) {
                            }
                            return result;
                        } catch (Exception e8) {
                            e = e8;
                            double d2 = duration_threshold4;
                            LogUtil.e(" calculateUserPreference, e:" + e.getMessage());
                            if (!result.getBoolean("isUserHasPref", false)) {
                            }
                            return result;
                        }
                    }
                }
            } else {
                duration_threshold = duration_threshold2;
                if (needRecord) {
                    this.mLocationDAO.accCHRUserPrefUnknownDBbyFreqLoc(this.freqLoc);
                }
            }
            try {
                int totalCount = Math.round(((float) inoutCount2) / 2.0f) + stayCount2;
                LogUtil.d("totalDuration: " + totalDuration2 + " totalCount:" + totalCount);
                if (maxEntry == null || totalDuration2 <= this.param.getUserPrefStartDuration() || maxEntry.getString("networkname", "UNKNOWN").equalsIgnoreCase("UNKNOWN")) {
                    inoutCount = inoutCount2;
                    stayCount = stayCount2;
                } else {
                    inoutCount = inoutCount2;
                    stayCount = stayCount2;
                    if (((double) maxEntry.getLong("duration_connected")) / ((double) totalDuration2) >= duration_threshold) {
                        try {
                            int otherNetworkCount = (totalCount - maxEntry.getInt("user_pref_opt_out")) - maxEntry.getInt("user_pref_stay");
                            LogUtil.d("otherNetworkCount: " + otherNetworkCount + " maxEntry: " + maxEntry.toString());
                            if (totalCount > this.param.getUserPrefStartTimes() && otherNetworkCount > 0 && ((float) maxEntry.getInt("user_pref_opt_in")) / ((float) otherNetworkCount) >= this.param.getUserPrefFreqRatio()) {
                                result.putBoolean("isUserHasPref", true);
                                result.putInt("prefNetworkType", maxEntry.getInt("networktype"));
                                result.putString("prefNetworkName", maxEntry.getString("networkname"));
                                LogUtil.d(" isUserHasPref: true prefNetworkType:" + maxEntry.getInt("networktype") + " prefNetworkName:" + maxEntry.getString("networkname"));
                                return result;
                            }
                        } catch (RuntimeException e9) {
                            e = e9;
                            int i = inoutCount;
                            int i2 = stayCount;
                            LogUtil.e("calculateUserPreference RuntimeException,e:" + e.getMessage());
                            if (!result.getBoolean("isUserHasPref", false)) {
                            }
                            return result;
                        } catch (Exception e10) {
                            e = e10;
                            int i3 = inoutCount;
                            int i4 = stayCount;
                            LogUtil.e(" calculateUserPreference, e:" + e.getMessage());
                            if (!result.getBoolean("isUserHasPref", false)) {
                            }
                            return result;
                        }
                    }
                }
                if (totalDuration2 > this.param.getUserPrefStartDuration() && ((double) cellDuration) / ((double) totalDuration2) >= duration_threshold) {
                    int otherNetworkCount2 = (totalCount - cellUserPrefOptOut) - cellUserPrefStay;
                    LogUtil.d("otherNetworkCount: " + otherNetworkCount2 + " cellDuration" + cellDuration);
                    if (totalCount > this.param.getUserPrefStartTimes() && otherNetworkCount2 > 0 && ((float) cellUserPrefOptIn) / ((float) otherNetworkCount2) >= this.param.getUserPrefFreqRatio()) {
                        result.putBoolean("isUserHasPref", true);
                        result.putInt("prefNetworkType", 0);
                        result.putString("prefNetworkName", "UNKNOWN");
                        LogUtil.d(" User Prefer Cellular");
                        return result;
                    }
                }
                int i5 = inoutCount;
                int i6 = stayCount;
            } catch (RuntimeException e11) {
                e = e11;
                int i7 = inoutCount2;
                int i8 = stayCount2;
                LogUtil.e("calculateUserPreference RuntimeException,e:" + e.getMessage());
                if (!result.getBoolean("isUserHasPref", false)) {
                }
                return result;
            } catch (Exception e12) {
                e = e12;
                int i9 = inoutCount2;
                int i10 = stayCount2;
                LogUtil.e(" calculateUserPreference, e:" + e.getMessage());
                if (!result.getBoolean("isUserHasPref", false)) {
                }
                return result;
            }
        } catch (RuntimeException e13) {
            e = e13;
            double d3 = duration_threshold2;
            LogUtil.e("calculateUserPreference RuntimeException,e:" + e.getMessage());
            if (!result.getBoolean("isUserHasPref", false)) {
            }
            return result;
        } catch (Exception e14) {
            e = e14;
            double d4 = duration_threshold2;
            LogUtil.e(" calculateUserPreference, e:" + e.getMessage());
            if (!result.getBoolean("isUserHasPref", false)) {
            }
            return result;
        }
        if (!result.getBoolean("isUserHasPref", false)) {
            LogUtil.i(" NO user prefer");
        }
        return result;
    }

    public void recognizeActions(RecognizeResult spaceIds, boolean allApSpaceChanged) {
        LogUtil.i(" recognizeActions ");
        if (spaceIds == null) {
            try {
                LogUtil.i(" no spaceIds");
            } catch (Exception e) {
                LogUtil.e(" assignSpaceExp2Space " + e.getMessage());
            }
        } else {
            int spaceId_allAp = spaceIds.getRgResult();
            int spaceId_mainAp = spaceIds.getMainApRgResult();
            LogUtil.d(" recognizeActions: spaceId_allAp=" + spaceId_allAp + ", spaceId_mainAp=" + spaceId_mainAp);
            if (allApSpaceChanged) {
                determineUserPreference(spaceId_allAp, spaceId_mainAp);
            }
        }
    }

    private void determineUserPreference(int spaceId_allAp, int spaceId_mainAp) {
        Bundle userPrefResult;
        String cellName;
        int i = spaceId_allAp;
        int i2 = spaceId_mainAp;
        LogUtil.i(" determineUserPreference ");
        boolean use_mainSpace = false;
        try {
            HwWaveMappingManager hwWaveMappingManager = HwWaveMappingManager.getInstance();
            if (hwWaveMappingManager != null) {
                IWaveMappingCallback brainCallback = hwWaveMappingManager.getWaveMappingCallback();
                if (brainCallback != null) {
                    if (i == 0) {
                        if (i2 == 0) {
                            LogUtil.i("No preference due to unknown space");
                            brainCallback.onWaveMappingReportCallback(0, this.mPrefNetworkName, this.mPrefNetworkType);
                            clearSavedPreference();
                            this.mLocationDAO.accCHRUserPrefUnknownSpacebyFreqLoc(this.freqLoc);
                            return;
                        }
                        use_mainSpace = true;
                        LogUtil.i("Search preference by mainAp space");
                    }
                    if (use_mainSpace) {
                        userPrefResult = getUserPrefNetworkbyMainAp(i2);
                    } else {
                        try {
                            userPrefResult = getUserPrefNetwork(spaceId_allAp);
                        } catch (Exception e) {
                            e = e;
                            boolean z = use_mainSpace;
                            LogUtil.e(" determineUserPreference " + e.getMessage());
                        }
                    }
                    this.mUserHasPref = userPrefResult.getBoolean("isUserHasPref");
                    this.mPrefNetworkName = userPrefResult.getString("prefNetworkName");
                    this.mPrefNetworkType = userPrefResult.getInt("prefNetworkType");
                    Bundle connectivity = NetUtil.getConnectedNetworkState(this.mContext);
                    int defaultType = connectivity.getInt("defaultType", 8);
                    String currNwName = connectivity.getString("defaultNwName", "UNKNOWN");
                    String currNwId = connectivity.getString("defaultNwId", "UNKNOWN");
                    String currNwFreq = connectivity.getString("defaultNwFreq", "UNKNOWN");
                    if (this.mUserHasPref && this.mPrefNetworkType == 1 && defaultType == 1) {
                        LogUtil.i("W2W: perfered WiFi name=" + this.mPrefNetworkName + ", current WiFi name=" + currNwName + ", space=" + i);
                        HashMap<Integer, String> preference = new HashMap<>();
                        preference.put(0, this.mPrefNetworkName);
                        this.mHistoryQoeMgr.savePreferListForWifi(preference);
                        if (!currNwName.equals(this.mPrefNetworkName)) {
                            brainCallback.onWaveMappingReportCallback(1, this.mPrefNetworkName, this.mPrefNetworkType);
                            boolean z2 = use_mainSpace;
                            try {
                                this.mCHRUserPrefSwitchTime = System.currentTimeMillis();
                                this.mCHRUserPrefOriginalNWName = currNwName;
                                this.mCHRUserPrefOriginalNWType = defaultType;
                                this.mCHRUserPrefOriginalNWId = currNwId;
                                this.mCHRUserPrefOriginalNWFreq = currNwFreq;
                                this.mCHRUserPrefSwitchNWName = this.mPrefNetworkName;
                                this.mCHRUserPrefSwitchNWType = this.mPrefNetworkType;
                                this.mCHRUserPrefDetermineSwitch = true;
                            } catch (Exception e2) {
                                e = e2;
                                LogUtil.e(" determineUserPreference " + e.getMessage());
                            }
                        }
                    }
                    if (this.mUserHasPref && this.mPrefNetworkType == 0 && defaultType == 1) {
                        clearSavedPreference();
                        LogUtil.i("W2C: perfered Cell name=" + this.mPrefNetworkName + ", current Cell name=" + cellName + ", space=" + i);
                        if (cellName.equals(this.mPrefNetworkName)) {
                            brainCallback.onWaveMappingReportCallback(1, this.mPrefNetworkName, this.mPrefNetworkType);
                        }
                    }
                    if (this.mUserHasPref && this.mPrefNetworkType == 1 && defaultType == 0) {
                        LogUtil.i("C2W: perfered WiFi name=" + this.mPrefNetworkName + ", current Cell name=" + currNwName + ", space=" + i);
                        brainCallback.onWaveMappingReportCallback(1, this.mPrefNetworkName, this.mPrefNetworkType);
                        HashMap<Integer, String> preference2 = new HashMap<>();
                        preference2.put(0, this.mPrefNetworkName);
                        this.mHistoryQoeMgr.savePreferListForWifi(preference2);
                        this.mCHRUserPrefSwitchTime = System.currentTimeMillis();
                        this.mCHRUserPrefOriginalNWName = currNwName;
                        this.mCHRUserPrefOriginalNWType = defaultType;
                        this.mCHRUserPrefOriginalNWId = currNwId;
                        this.mCHRUserPrefOriginalNWFreq = currNwFreq;
                        this.mCHRUserPrefSwitchNWName = this.mPrefNetworkName;
                        this.mCHRUserPrefSwitchNWType = this.mPrefNetworkType;
                        this.mCHRUserPrefDetermineSwitch = true;
                    } else {
                        LogUtil.i("No preference");
                        brainCallback.onWaveMappingReportCallback(0, this.mPrefNetworkName, this.mPrefNetworkType);
                        clearSavedPreference();
                    }
                }
            }
        } catch (Exception e3) {
            e = e3;
            LogUtil.e(" determineUserPreference " + e.getMessage());
        }
    }

    public boolean determine4gCoverage(RecognizeResult spaceIds) {
        Bundle record4g;
        int cellNum;
        int avgSignal;
        double duration4gRatio;
        boolean foundRecord;
        boolean foundRecord2 = false;
        HwWmpFastBackLte mBack = new HwWmpFastBackLte();
        Bundle record4g2 = new Bundle();
        LogUtil.i(" determine4gCoverage ");
        if (!this.param.getBack4GEnabled()) {
            LogUtil.d("Fast Back 4G Feature - Disabled");
        }
        if (spaceIds == null) {
            try {
                LogUtil.i(" no spaceIds");
                return false;
            } catch (Exception e) {
                e = e;
                HwWmpFastBackLte hwWmpFastBackLte = mBack;
                LogUtil.e(" determine4gCoverage " + e.getMessage());
                return foundRecord2;
            }
        } else {
            try {
                int spaceId_allAp = spaceIds.getRgResult();
                int spaceId_mainAp = spaceIds.getMainApRgResult();
                if (spaceId_allAp != 0) {
                    Bundle record4g3 = record4g2;
                    if (spaceId_mainAp == 0) {
                        LogUtil.d(" determine4gCoverage: space ID: allAp=" + spaceId_allAp);
                        record4g = this.mSpaceUserDAO.find4gCoverageByAllApSpace(spaceId_allAp);
                    } else {
                        try {
                            LogUtil.d(" determine4gCoverage, space ID: allAp=" + spaceId_allAp + " ,mainAp=" + spaceId_mainAp);
                            record4g = this.mSpaceUserDAO.find4gCoverageByBothSpace(spaceId_allAp, spaceId_mainAp);
                        } catch (Exception e2) {
                            e = e2;
                            HwWmpFastBackLte hwWmpFastBackLte2 = mBack;
                            Bundle bundle = record4g3;
                            LogUtil.e(" determine4gCoverage " + e.getMessage());
                            return foundRecord2;
                        }
                    }
                } else if (spaceId_mainAp == 0) {
                    try {
                        LogUtil.i(" determine4gCoverage: no space ID");
                        Bundle record4g4 = record4g2;
                        try {
                            if (this.mFastBack2LteChrDAO.sessionSpace(0, 0)) {
                                this.mFastBack2LteChrDAO.startSession();
                                this.mFastBack2LteChrDAO.addQueryCnt();
                            }
                            this.mFastBack2LteChrDAO.addunknownSpace();
                            this.mFastBack2LteChrDAO.endSession();
                            this.mFastBack2LteChrDAO.insertRecordByLoc();
                            return false;
                        } catch (Exception e3) {
                            e = e3;
                            HwWmpFastBackLte hwWmpFastBackLte3 = mBack;
                            Bundle bundle2 = record4g4;
                            LogUtil.e(" determine4gCoverage " + e.getMessage());
                            return foundRecord2;
                        }
                    } catch (Exception e4) {
                        e = e4;
                        Bundle bundle3 = record4g2;
                        HwWmpFastBackLte hwWmpFastBackLte4 = mBack;
                        LogUtil.e(" determine4gCoverage " + e.getMessage());
                        return foundRecord2;
                    }
                } else {
                    LogUtil.d(" determine4gCoverage, space ID: mainAp=" + spaceId_mainAp);
                    record4g = this.mSpaceUserDAO.find4gCoverageByMainApSpace(spaceId_mainAp);
                }
                Bundle record4g5 = record4g;
                try {
                    if (this.mFastBack2LteChrDAO.sessionSpace(spaceId_allAp, spaceId_mainAp)) {
                        this.mFastBack2LteChrDAO.startSession();
                        this.mFastBack2LteChrDAO.addQueryCnt();
                    }
                    if (record4g5.containsKey("cell_num")) {
                        long duration = record4g5.getLong("total_duration");
                        int i = spaceId_allAp;
                        try {
                            LogUtil.d(" found 4g record: duration=" + duration + " ,cell num=" + cellNum + " ,avg signal=" + avgSignal + " ,duration4gRatio=" + duration4gRatio);
                            this.mFastBack2LteChrDAO.setcells4G(cellNum);
                            if (((long) this.param.getBack4GTH_duration_min()) >= duration || this.param.getBack4GTH_signal_min() >= avgSignal || this.param.getBack4GTH_duration_4gRatio() >= duration4gRatio) {
                                try {
                                    LogUtil.i(" 4g cell NOT found ");
                                    this.mFastBack2LteChrDAO.addoutLteCnt();
                                    this.mFastBack2LteChrDAO.endSession();
                                    this.mFastBack2LteChrDAO.insertRecordByLoc();
                                } catch (Exception e5) {
                                    e = e5;
                                }
                                return foundRecord2;
                            }
                            LogUtil.d(" in 4G coverage, send to booster");
                            foundRecord2 = true;
                            try {
                                mBack.mSubId = 0;
                                mBack.setRat("4G");
                                this.mFastBack2LteChrDAO.addinLteCnt();
                                if (this.mFastBackLteMgr != null) {
                                    this.mFastBackLteMgr.SendDataToBooster(mBack);
                                    this.mFastBack2LteChrDAO.addfastBack();
                                    this.mFastBack2LteChrDAO.waitSession();
                                    this.handler.removeCallbacks(this.getRegStateAfterPeriodHandler);
                                    foundRecord = true;
                                    HwWmpFastBackLte hwWmpFastBackLte5 = mBack;
                                    try {
                                        this.handler.postDelayed(this.getRegStateAfterPeriodHandler, (long) this.param.getReGetPsRegStatus());
                                    } catch (Exception e6) {
                                        e = e6;
                                        foundRecord2 = true;
                                        LogUtil.e(" determine4gCoverage " + e.getMessage());
                                        return foundRecord2;
                                    }
                                } else {
                                    foundRecord = true;
                                    HwWmpFastBackLte hwWmpFastBackLte6 = mBack;
                                    this.mFastBack2LteChrDAO.endSession();
                                }
                                foundRecord2 = foundRecord;
                            } catch (Exception e7) {
                                e = e7;
                                HwWmpFastBackLte hwWmpFastBackLte7 = mBack;
                                LogUtil.e(" determine4gCoverage " + e.getMessage());
                                return foundRecord2;
                            }
                            return foundRecord2;
                        } catch (Exception e8) {
                            e = e8;
                            HwWmpFastBackLte hwWmpFastBackLte8 = mBack;
                            LogUtil.e(" determine4gCoverage " + e.getMessage());
                            return foundRecord2;
                        }
                    } else {
                        int i2 = spaceId_allAp;
                        try {
                            LogUtil.i(" NO 4g cell");
                            this.mFastBack2LteChrDAO.setcells4G(0);
                            this.mFastBack2LteChrDAO.addoutLteCnt();
                            this.mFastBack2LteChrDAO.endSession();
                            this.mFastBack2LteChrDAO.insertRecordByLoc();
                        } catch (Exception e9) {
                            e = e9;
                            LogUtil.e(" determine4gCoverage " + e.getMessage());
                            return foundRecord2;
                        }
                        return foundRecord2;
                    }
                } catch (Exception e10) {
                    e = e10;
                    HwWmpFastBackLte hwWmpFastBackLte9 = mBack;
                    LogUtil.e(" determine4gCoverage " + e.getMessage());
                    return foundRecord2;
                }
            } catch (Exception e11) {
                e = e11;
                HwWmpFastBackLte hwWmpFastBackLte10 = mBack;
                Bundle bundle4 = record4g2;
                LogUtil.e(" determine4gCoverage " + e.getMessage());
                return foundRecord2;
            }
        }
    }

    public void clearSavedPreference() {
        LogUtil.i("clearSavedPreference");
        this.mHistoryQoeMgr.savePreferListForWifi(new HashMap<>());
    }

    public void queryAppQoebyTargetNw(RecognizeResult spaceIds, int fullId, int UID, int Network, IWaveMappingCallback callback, int direction) {
        int mArbitrationNet;
        String currWifiName;
        String currWifiName2;
        int spaceId_mainAp;
        HashMap<String, List> historyQoe;
        Bundle wifiInfo;
        boolean isFound;
        Bundle cellInfo;
        boolean isGood;
        String currWifiName3;
        int spaceId_mainAp2;
        String nwName;
        String currCellName;
        int i = fullId;
        int i2 = Network;
        LogUtil.i(" queryAppQoebyTargetNw: appName=" + i);
        String appName = Constant.USERDB_APP_NAME_PREFIX + i;
        float sourceNwBadRatio = 2.0f;
        float targetNwBadRatio = 2.0f;
        int mArbitrationNet2 = 802;
        if (callback == null) {
            try {
                LogUtil.d(" no callback");
            } catch (Exception e) {
                e = e;
                mArbitrationNet = 802;
                LogUtil.e(" queryAppQoebyTargetNw " + e.getMessage());
                respondCallBack(UID, mArbitrationNet, true, false, callback, direction);
            }
        } else {
            if (1 == i2) {
                mArbitrationNet2 = 800;
            } else if (i2 == 0) {
                mArbitrationNet2 = 801;
            }
            mArbitrationNet = mArbitrationNet2;
            if (spaceIds == null) {
                try {
                    LogUtil.i(" no spaceIds");
                    respondCallBack(UID, mArbitrationNet, true, false, callback, direction);
                } catch (Exception e2) {
                    e = e2;
                    LogUtil.e(" queryAppQoebyTargetNw " + e.getMessage());
                    respondCallBack(UID, mArbitrationNet, true, false, callback, direction);
                }
            } else {
                LogUtil.i(" queryAppQoebyTargetNw: spaces=" + spaceIds.toString());
                if (!Constant.getSavedQoeAppList().containsKey(Integer.valueOf(fullId))) {
                    LogUtil.i(" NOT monitor app");
                    respondCallBack(UID, mArbitrationNet, true, false, callback, direction);
                    return;
                }
                this.mHisQoEChrDAO.accQueryCnt();
                int spaceId_allAp = spaceIds.getRgResult();
                int spaceId_mainAp3 = spaceIds.getMainApRgResult();
                this.mHisQoEChrService.resetRecordByApp(i);
                this.mHisQoEChrService.setSpaceInfo(spaceIds.getRgResult(), spaceIds.getAllApModelName(), spaceIds.getMainApRgResult(), spaceIds.getMainApModelName(), 0, 0);
                if (spaceId_allAp == 0) {
                    LogUtil.i(" no space found");
                    int i3 = spaceId_mainAp3;
                    int spaceId_mainAp4 = spaceId_allAp;
                    respondCallBack(UID, mArbitrationNet, true, false, callback, direction);
                    this.mHisQoEChrDAO.accUnknownSpace();
                    this.mHisQoEChrService.saveRecordByApp(i);
                    return;
                }
                int spaceId_allAp2 = spaceId_allAp;
                int spaceId_mainAp5 = spaceId_mainAp3;
                HashMap<String, List> historyQoe2 = calculateAppQoeFromDatabase(appName, spaceId_allAp2, spaceId_mainAp5);
                boolean isFound2 = false;
                Bundle wifiInfo2 = NetUtil.getWifiStateString(this.mContext);
                String currWifiName4 = wifiInfo2.getString("wifiAp", "UNKNOWN");
                if (currWifiName4 == null) {
                    currWifiName = "UNKNOWN";
                } else {
                    currWifiName = currWifiName4;
                }
                Bundle cellInfo2 = NetUtil.getMobileDataState(this.mContext);
                int i4 = spaceId_allAp2;
                boolean isGood2 = false;
                String currCellName2 = cellInfo2.getString("cellRAT", "UNKNOWN");
                if (currCellName2 == null) {
                    currCellName2 = "UNKNOWN";
                }
                if (!historyQoe2.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    cellInfo = cellInfo2;
                    sb.append("              ,historyQoe size=");
                    sb.append(historyQoe2.size());
                    LogUtil.i(sb.toString());
                    Iterator<Map.Entry<String, List>> it = historyQoe2.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, List> entry = it.next();
                        String nwName2 = entry.getKey();
                        Iterator<Map.Entry<String, List>> it2 = it;
                        Map.Entry<String, List> entry2 = entry;
                        List value = entry.getValue();
                        int nwType = ((Integer) value.get(0)).intValue();
                        boolean isFound3 = isFound2;
                        int nwId = ((Integer) value.get(1)).intValue();
                        Bundle wifiInfo3 = wifiInfo2;
                        int nwFreq = ((Integer) value.get(2)).intValue();
                        HashMap<String, List> historyQoe3 = historyQoe2;
                        long dur = ((Long) value.get(3)).longValue();
                        int good = ((Integer) value.get(4)).intValue();
                        int poor = ((Integer) value.get(5)).intValue();
                        int rx = ((Integer) value.get(6)).intValue();
                        int tx = ((Integer) value.get(7)).intValue();
                        int days = ((Integer) value.get(8)).intValue();
                        float badRatio = ((Float) value.get(9)).floatValue();
                        List list = value;
                        if (1 == nwType) {
                            nwName = nwName2;
                            if (nwName.equals(currWifiName)) {
                                spaceId_mainAp2 = spaceId_mainAp5;
                                if (currWifiName.equals("UNKNOWN") == 0) {
                                    StringBuilder sb2 = new StringBuilder();
                                    currWifiName3 = currWifiName;
                                    sb2.append("  found Wifi network:");
                                    sb2.append(nwName);
                                    LogUtil.v(sb2.toString());
                                    if (i2 == nwType) {
                                        float targetNwBadRatio2 = badRatio;
                                        try {
                                            LogUtil.i("  found target network, bad ratio=" + badRatio);
                                            this.mHisQoEChrService.setNetInfo(nwId, nwName, nwFreq, nwType);
                                            float targetNwBadRatio3 = targetNwBadRatio2;
                                            try {
                                                this.mHisQoEChrService.setRecords(days, (int) (dur / 1000), good, poor, rx, tx);
                                                targetNwBadRatio = targetNwBadRatio3;
                                            } catch (Exception e3) {
                                                e = e3;
                                                float f = targetNwBadRatio3;
                                                LogUtil.e(" queryAppQoebyTargetNw " + e.getMessage());
                                                respondCallBack(UID, mArbitrationNet, true, false, callback, direction);
                                            }
                                        } catch (Exception e4) {
                                            e = e4;
                                            float f2 = targetNwBadRatio2;
                                            LogUtil.e(" queryAppQoebyTargetNw " + e.getMessage());
                                            respondCallBack(UID, mArbitrationNet, true, false, callback, direction);
                                        }
                                    } else {
                                        sourceNwBadRatio = badRatio;
                                        LogUtil.i("  found source network, bad ratio=" + badRatio);
                                    }
                                } else {
                                    currWifiName3 = currWifiName;
                                }
                            } else {
                                currWifiName3 = currWifiName;
                                spaceId_mainAp2 = spaceId_mainAp5;
                            }
                        } else {
                            currWifiName3 = currWifiName;
                            spaceId_mainAp2 = spaceId_mainAp5;
                            nwName = nwName2;
                        }
                        if (nwType != 0 || !nwName.equals(currCellName2) || currCellName2.equals("UNKNOWN")) {
                            currCellName = currCellName2;
                        } else {
                            LogUtil.v("  found CELL network:" + nwName);
                            if (i2 == nwType) {
                                targetNwBadRatio = badRatio;
                                LogUtil.i("  found target network, bad ratio=" + badRatio);
                                this.mHisQoEChrService.setNetInfo(nwId, nwName, nwFreq, nwType);
                                currCellName = currCellName2;
                                int i5 = nwType;
                                this.mHisQoEChrService.setRecords(days, (int) (dur / 1000), good, poor, rx, tx);
                            } else {
                                currCellName = currCellName2;
                                int i6 = nwType;
                                sourceNwBadRatio = badRatio;
                                LogUtil.i("  found source network, bad ratio=" + badRatio);
                            }
                        }
                        it = it2;
                        isFound2 = isFound3;
                        wifiInfo2 = wifiInfo3;
                        historyQoe2 = historyQoe3;
                        spaceId_mainAp5 = spaceId_mainAp2;
                        currWifiName = currWifiName3;
                        currCellName2 = currCellName;
                    }
                    isFound = isFound2;
                    currWifiName2 = currWifiName;
                    wifiInfo = wifiInfo2;
                    historyQoe = historyQoe2;
                    spaceId_mainAp = spaceId_mainAp5;
                    if (targetNwBadRatio != 2.0f) {
                        float threshold = Constant.getSavedQoeAppList().get(Integer.valueOf(fullId)).floatValue();
                        float margin = this.param.getAppTH_Target_Ration_margin();
                        if (targetNwBadRatio < threshold) {
                            isGood = true;
                            LogUtil.d("  target network is GOOD enough, targetNwBadRatio:" + targetNwBadRatio);
                        } else if (sourceNwBadRatio == 2.0f || targetNwBadRatio + margin >= sourceNwBadRatio) {
                            LogUtil.d("  target network is Bad");
                            isGood = false;
                        } else {
                            isGood = true;
                            LogUtil.d("  target network(" + targetNwBadRatio + ") is BETTER than source network(" + sourceNwBadRatio + ")");
                        }
                        isGood2 = isGood;
                        isFound = true;
                    }
                } else {
                    cellInfo = cellInfo2;
                    isFound = false;
                    currWifiName2 = currWifiName;
                    wifiInfo = wifiInfo2;
                    historyQoe = historyQoe2;
                    spaceId_mainAp = spaceId_mainAp5;
                    LogUtil.i("  historyQoe is empty");
                }
                Bundle bundle = cellInfo;
                String str = currWifiName2;
                Bundle bundle2 = wifiInfo;
                HashMap<String, List> hashMap = historyQoe;
                int i7 = spaceId_mainAp;
                respondCallBack(UID, mArbitrationNet, isGood2, isFound, callback, direction);
                if (!isFound) {
                    this.mHisQoEChrDAO.accUnknownDB();
                } else if (isGood2) {
                    this.mHisQoEChrDAO.accGoodCnt();
                } else {
                    this.mHisQoEChrDAO.accPoorCnt();
                }
                this.mHisQoEChrDAO.insertRecordByLoc();
                this.mHisQoEChrService.saveRecordByApp(i);
            }
        }
    }

    private void respondCallBack(int UID, int net, boolean isGood, boolean found, IWaveMappingCallback callback, int direction) {
        if (callback == null) {
            return;
        }
        if (1 == direction) {
            callback.onWaveMappingRespondCallback(UID, 0, net, isGood, found);
        } else if (2 == direction) {
            callback.onWaveMappingRespond4BackCallback(UID, 0, net, isGood, found);
        }
    }

    public void determineAppQoePreference(RecognizeResult spaceIds, HwWmpAppInfo appInfo) {
        int fullid = appInfo.getAppFullId();
        if (spaceIds == null) {
            LogUtil.i(" no spaceIds");
            return;
        }
        LogUtil.i(" determineAppQoePreference: spaces=" + spaceIds.toString());
        if (!Constant.getSavedQoeAppList().containsKey(Integer.valueOf(fullid))) {
            LogUtil.i(" NOT monitor app");
            return;
        }
        int spaceId_allAp = spaceIds.getRgResult();
        updateQoEPrefCHR();
        clearQoEPrefCHR();
        HwWaveMappingManager hwWaveMappingManager = HwWaveMappingManager.getInstance();
        if (hwWaveMappingManager != null) {
            IWaveMappingCallback appQoeCallback = hwWaveMappingManager.getAppQoeCallback();
            IWaveMappingCallback hiStreamCallback = hwWaveMappingManager.getHiStreamCallback();
            Bundle connectivity = NetUtil.getConnectedNetworkState(this.mContext);
            int currentNetworkType = connectivity.getInt("defaultType", 8);
            Bundle appQoePrefResult = calculateAppQoePrefNetwork(fullid, spaceId_allAp, connectivity.getString("defaultNwName", "UNKNOWN"));
            boolean isHasPref = appQoePrefResult.getBoolean("isAppQoeHasPref", false);
            int prefNetworkType = appQoePrefResult.getInt("prefNetworkType", 8);
            String prefNetworkName = appQoePrefResult.getString("prefNetworkName", "UNKNOWN");
            if (isHasPref && currentNetworkType == 1 && prefNetworkType == 0) {
                if (appQoeCallback != null) {
                    this.mCHRQoEPrefDetermineSwitch = true;
                    appQoeCallback.onWaveMappingReportCallback(3, prefNetworkName, prefNetworkType);
                }
                if (hiStreamCallback != null) {
                    this.mCHRQoEPrefDetermineSwitch = true;
                    hiStreamCallback.onWaveMappingReportCallback(3, prefNetworkName, prefNetworkType);
                }
            }
        }
    }

    private Bundle calculateAppQoePrefNetwork(int fullId, int spaceId_allAp, String currentNetworkName) {
        Bundle result;
        float margin;
        Bundle result2;
        String nwName;
        float ratio;
        int i = spaceId_allAp;
        String str = currentNetworkName;
        Bundle result3 = new Bundle();
        String maxNetworkName = null;
        int maxNetworkType = 8;
        float maxNetworkRatio = -1.0f;
        StringBuilder sb = new StringBuilder();
        long totalDuration = 0;
        sb.append(Constant.USERDB_APP_NAME_PREFIX);
        sb.append(fullId);
        String app = sb.toString();
        long cellDuration = 0;
        try {
            result3.putBoolean("isAppQoeHasPref", false);
            result3.putInt("prefNetworkType", 8);
            result3.putString("prefNetworkName", "UNKNOWN");
            if (i == 0) {
                try {
                    LogUtil.i("No QoE preference due to unknown space");
                    this.mLocationDAO.accCHRUserPrefResCntbyFreqLoc(this.freqLoc);
                    return result3;
                } catch (Exception e) {
                    e = e;
                    result = result3;
                    String str2 = app;
                    LogUtil.e(" calculateAppQoePrefNetwork " + e.getMessage());
                    return result;
                }
            } else {
                HashMap<String, List> valueQoe = this.mSpaceUserDAO.findAppQoEgroupByAllSpace(app, i);
                if (valueQoe.size() > 0) {
                    Iterator<Map.Entry<String, List>> it = valueQoe.entrySet().iterator();
                    long currentNetworkDuration = 0;
                    long maxNetworkDuration = 0;
                    float currentNetworkRatio = -1.0f;
                    while (it.hasNext()) {
                        try {
                            try {
                                Map.Entry<String, List> entry = it.next();
                                String nwName2 = entry.getKey();
                                List qoeRaw = entry.getValue();
                                if (qoeRaw == null) {
                                    try {
                                        LogUtil.w(" qoe record invalid, qoeRaw=null");
                                        return result3;
                                    } catch (Exception e2) {
                                        e = e2;
                                        result = result3;
                                        String str3 = app;
                                        LogUtil.e(" calculateAppQoePrefNetwork " + e.getMessage());
                                        return result;
                                    }
                                } else {
                                    HashMap<String, List> valueQoe2 = valueQoe;
                                    if (qoeRaw.size() < 9) {
                                        LogUtil.w(" qoe record invalid, qoeRaw size=" + qoeRaw.size());
                                        return result3;
                                    }
                                    int type = ((Integer) qoeRaw.get(0)).intValue();
                                    long duration = ((Long) qoeRaw.get(3)).longValue();
                                    int good = ((Integer) qoeRaw.get(4)).intValue();
                                    String app2 = app;
                                    try {
                                        int poor = ((Integer) qoeRaw.get(5)).intValue();
                                        float ratio2 = -1.0f;
                                        List list = qoeRaw;
                                        Iterator<Map.Entry<String, List>> it2 = it;
                                        long duration2 = duration;
                                        totalDuration += duration2;
                                        if (type == 0) {
                                            cellDuration += duration2;
                                        }
                                        Map.Entry<String, List> entry2 = entry;
                                        int maxNetworkType2 = maxNetworkType;
                                        if (duration2 > ((long) this.param.getAppTH_duration_min())) {
                                            try {
                                                if (poor > this.param.getAppTH_poorCnt_min()) {
                                                    if (good > this.param.getAppTH_goodCnt_min()) {
                                                        result2 = result3;
                                                        int i2 = good;
                                                        ratio = (((float) poor) * 1.0f) / ((((float) good) * 1.0f) + (((float) poor) * 1.0f));
                                                    } else {
                                                        result2 = result3;
                                                        ratio = (((float) poor) * 5000.0f) / (((float) duration2) * 1.0f);
                                                    }
                                                    try {
                                                        StringBuilder sb2 = new StringBuilder();
                                                        sb2.append(" have poor, network=");
                                                        nwName = nwName2;
                                                        sb2.append(nwName);
                                                        sb2.append(", poor ratio=");
                                                        sb2.append(ratio);
                                                        LogUtil.i(sb2.toString());
                                                        ratio2 = ratio;
                                                    } catch (Exception e3) {
                                                        e = e3;
                                                        float f = currentNetworkRatio;
                                                        long j = maxNetworkDuration;
                                                        long maxNetworkDuration2 = currentNetworkDuration;
                                                        int i3 = maxNetworkType2;
                                                        result = result2;
                                                        LogUtil.e(" calculateAppQoePrefNetwork " + e.getMessage());
                                                        return result;
                                                    }
                                                } else {
                                                    result2 = result3;
                                                    nwName = nwName2;
                                                    LogUtil.i(" too less poor=" + poor + ", network=" + nwName);
                                                    ratio2 = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                                                }
                                            } catch (Exception e4) {
                                                e = e4;
                                                result = result3;
                                                float f2 = currentNetworkRatio;
                                                long j2 = maxNetworkDuration;
                                                long maxNetworkDuration3 = currentNetworkDuration;
                                                int i4 = maxNetworkType2;
                                                LogUtil.e(" calculateAppQoePrefNetwork " + e.getMessage());
                                                return result;
                                            }
                                        } else {
                                            int i5 = good;
                                            result2 = result3;
                                            nwName = nwName2;
                                            LogUtil.i(" too less duration = " + duration2 + ", network=" + nwName);
                                        }
                                        if (nwName.equals(str)) {
                                            currentNetworkRatio = ratio2;
                                            currentNetworkDuration = duration2;
                                        }
                                        if (maxNetworkName == null || (!nwName.equals("UNKNOWN") && maxNetworkDuration < duration2)) {
                                            maxNetworkName = nwName;
                                            maxNetworkDuration = duration2;
                                            maxNetworkRatio = ratio2;
                                            maxNetworkType = type;
                                        } else {
                                            maxNetworkType = maxNetworkType2;
                                        }
                                        valueQoe = valueQoe2;
                                        app = app2;
                                        it = it2;
                                        result3 = result2;
                                        int i6 = spaceId_allAp;
                                    } catch (Exception e5) {
                                        e = e5;
                                        int i7 = maxNetworkType;
                                        result = result3;
                                        float f3 = currentNetworkRatio;
                                        long j3 = maxNetworkDuration;
                                        long maxNetworkDuration4 = currentNetworkDuration;
                                        LogUtil.e(" calculateAppQoePrefNetwork " + e.getMessage());
                                        return result;
                                    }
                                }
                            } catch (Exception e6) {
                                e = e6;
                                String str4 = app;
                                int i8 = maxNetworkType;
                                result = result3;
                                float f4 = currentNetworkRatio;
                                long j4 = maxNetworkDuration;
                                long maxNetworkDuration5 = currentNetworkDuration;
                                LogUtil.e(" calculateAppQoePrefNetwork " + e.getMessage());
                                return result;
                            }
                        } catch (Exception e7) {
                            e = e7;
                            result = result3;
                            String str5 = app;
                            float f5 = currentNetworkRatio;
                            long j5 = maxNetworkDuration;
                            long maxNetworkDuration6 = currentNetworkDuration;
                            LogUtil.e(" calculateAppQoePrefNetwork " + e.getMessage());
                            return result;
                        }
                    }
                    HashMap<String, List> hashMap = valueQoe;
                    Bundle result4 = result3;
                    String str6 = app;
                    int maxNetworkType3 = maxNetworkType;
                    if (!(str == null || maxNetworkName == null)) {
                        try {
                            if (!str.equals("UNKNOWN") && totalDuration > this.param.getUserPrefStartDuration() && !str.equals(maxNetworkName)) {
                                float threshold = Constant.getSavedQoeAppList().get(Integer.valueOf(fullId)).floatValue();
                                LogUtil.d("currentNetworkRatio:" + currentNetworkRatio + " maxNetworkRatio:" + maxNetworkRatio + " margin:" + margin);
                                if (currentNetworkRatio < threshold || maxNetworkRatio < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO || maxNetworkRatio + margin >= currentNetworkRatio) {
                                    result = result4;
                                    float f6 = currentNetworkRatio;
                                    long j6 = maxNetworkDuration;
                                    long maxNetworkDuration7 = currentNetworkDuration;
                                    return result;
                                }
                                result = result4;
                                try {
                                    result.putBoolean("isAppQoeHasPref", true);
                                    try {
                                        result.putInt("prefNetworkType", maxNetworkType3);
                                        result.putString("prefNetworkName", maxNetworkName);
                                        LogUtil.d("isAppQoeHasPref = true, prefNetworkType:" + maxNetworkType + " prefNetworkName:" + maxNetworkName);
                                        float f62 = currentNetworkRatio;
                                        long j62 = maxNetworkDuration;
                                        long maxNetworkDuration72 = currentNetworkDuration;
                                    } catch (Exception e8) {
                                        e = e8;
                                    }
                                } catch (Exception e9) {
                                    e = e9;
                                    int i9 = maxNetworkType3;
                                    float f7 = currentNetworkRatio;
                                    long j7 = maxNetworkDuration;
                                    long maxNetworkDuration8 = currentNetworkDuration;
                                    LogUtil.e(" calculateAppQoePrefNetwork " + e.getMessage());
                                    return result;
                                }
                                return result;
                            }
                        } catch (Exception e10) {
                            e = e10;
                            int i10 = maxNetworkType3;
                            result = result4;
                            float f8 = currentNetworkRatio;
                            long j8 = maxNetworkDuration;
                            long maxNetworkDuration9 = currentNetworkDuration;
                            LogUtil.e(" calculateAppQoePrefNetwork " + e.getMessage());
                            return result;
                        }
                    }
                    result = result4;
                    float f622 = currentNetworkRatio;
                    long j622 = maxNetworkDuration;
                    long maxNetworkDuration722 = currentNetworkDuration;
                    return result;
                }
                HashMap<String, List> hashMap2 = valueQoe;
                result = result3;
                String str7 = app;
                try {
                    this.mLocationDAO.accCHRUserPrefQueryCntbyFreqLoc(this.freqLoc);
                } catch (Exception e11) {
                    e = e11;
                }
                return result;
            }
        } catch (Exception e12) {
            e = e12;
            result = result3;
            String str8 = app;
            LogUtil.e(" calculateAppQoePrefNetwork " + e.getMessage());
            return result;
        }
    }

    private HashMap<String, List> calculateAppQoeFromDatabase(String app, int spaceId_allAp, int spaceId_mainAp) {
        HashMap<String, List> valueQoe;
        float ratio;
        HashMap<String, List> resultsQoe = new HashMap<>();
        try {
            HashMap<String, List> valueQoe2 = this.mSpaceUserDAO.findAppQoEgroupByAllSpace(app, spaceId_allAp);
            for (Map.Entry<String, List> entry : valueQoe2.entrySet()) {
                String nwName = entry.getKey();
                List qoeRaw = entry.getValue();
                if (qoeRaw == null) {
                    LogUtil.w(" qoe record invalid, qoeRaw=null");
                    return resultsQoe;
                } else if (qoeRaw.size() < 9) {
                    LogUtil.w(" qoe record invalid, qoeRaw size=" + qoeRaw.size());
                    return resultsQoe;
                } else {
                    int intValue = ((Integer) qoeRaw.get(0)).intValue();
                    long duration = ((Long) qoeRaw.get(3)).longValue();
                    int good = ((Integer) qoeRaw.get(4)).intValue();
                    int poor = ((Integer) qoeRaw.get(5)).intValue();
                    float ratio2 = 2.0f;
                    if (duration > ((long) this.param.getAppTH_duration_min())) {
                        if (poor > this.param.getAppTH_poorCnt_min()) {
                            if (good > this.param.getAppTH_goodCnt_min()) {
                                valueQoe = valueQoe2;
                                ratio = (((float) poor) * 1.0f) / ((((float) good) * 1.0f) + (((float) poor) * 1.0f));
                            } else {
                                valueQoe = valueQoe2;
                                ratio = (((float) poor) * 5000.0f) / (((float) duration) * 1.0f);
                            }
                            LogUtil.i(" have poor, network=" + nwName + ", poor ratio=" + ratio);
                            ratio2 = ratio;
                        } else {
                            valueQoe = valueQoe2;
                            LogUtil.i(" too less poor=" + poor + ", network=" + nwName);
                            ratio2 = 0.0f;
                        }
                        LogUtil.d(" add history Qoe");
                    } else {
                        valueQoe = valueQoe2;
                        LogUtil.i(" too less duration = " + duration + ", network=" + nwName);
                    }
                    qoeRaw.add(Float.valueOf(ratio2));
                    resultsQoe.put(nwName, qoeRaw);
                    valueQoe2 = valueQoe;
                    String str = app;
                    int i = spaceId_allAp;
                }
            }
            LogUtil.d(" record size:" + resultsQoe.size());
        } catch (Exception e) {
            LogUtil.e("calculateAppQoeFromDatabase " + e.getMessage());
        }
        return resultsQoe;
    }

    private String getTime(long time) {
        return new SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(new Date(time));
    }

    private void setNewStartTime(HwWmpAppInfo info) {
        HwWmpAppInfo newInfo = new HwWmpAppInfo(8);
        newInfo.copyObjectValue(info);
        newInfo.setStartTime(System.currentTimeMillis());
        if (newInfo.getAppName() != null) {
            this.saveAppInfo.put(newInfo.getAppName(), newInfo);
            LogUtil.d(" setNewStartTime - new, " + newInfo.toString());
            return;
        }
        LogUtil.w(" getAppName == null");
    }

    private void updateStartTime(String app, int net) {
        if (this.saveAppInfo.isEmpty() || !this.saveAppInfo.containsKey(app)) {
            LogUtil.d(" updateStartTime, no this app:" + app);
            return;
        }
        this.saveAppInfo.get(app).setStartTime(System.currentTimeMillis());
        this.saveAppInfo.get(app).setConMgrNetworkType(net);
        LogUtil.i(" updateStartTime, update app:" + app + ", appNwType:" + net);
    }

    private void resetTime(String appName) {
        if (this.saveAppInfo.isEmpty()) {
            LogUtil.i(" no saved APP");
            return;
        }
        if (this.saveAppInfo.containsKey(appName)) {
            this.saveAppInfo.remove(appName);
        }
        LogUtil.i(" resetTime, app:" + appName);
    }

    public void checkUserPrefAutoSwitch() {
        Bundle connectivity = NetUtil.getConnectedNetworkState(this.mContext);
        int defaultType = connectivity.getInt("defaultType");
        if (this.mCHRUserPrefDetermineSwitch && !this.mCHRUserPrefAutoSwitch) {
            if ((!this.mUserOperation || !this.mDestNetworkId.equals("UNKNOWN")) && this.mCHRUserPrefSwitchNWName.equals(connectivity.getString("defaultNwName")) && defaultType == this.mCHRUserPrefSwitchNWType) {
                this.mCHRUserPrefAutoSwitch = true;
                this.mCHRUserPrefAutoSwitchTime = System.currentTimeMillis();
                this.mCHRUserPrefSwitchNWId = connectivity.getString("defaultNwId");
                this.mCHRUserPrefSwitchNWFreq = connectivity.getString("defaultNwFreq");
            }
        }
    }

    private void checkDefaultConnectivityState() {
        Bundle connectivity = NetUtil.getConnectedNetworkState(this.mContext);
        int defaultType = connectivity.getInt("defaultType");
        if (this.mCHRUserPrefDetermineSwitch) {
            if (!this.mCHRUserPrefAutoSwitch && ((!this.mUserOperation || !this.mDestNetworkId.equals("UNKNOWN")) && this.mCHRUserPrefSwitchNWName.equals(connectivity.getString("defaultNwName")) && defaultType == this.mCHRUserPrefSwitchNWType)) {
                this.mCHRUserPrefAutoSwitch = true;
                this.mCHRUserPrefAutoSwitchTime = System.currentTimeMillis();
                this.mCHRUserPrefSwitchNWId = connectivity.getString("defaultNwId");
                this.mCHRUserPrefSwitchNWFreq = connectivity.getString("defaultNwFreq");
            }
            if (!this.mCHRUserPrefManualSwitch && this.mUserOperation && this.mDestNetworkId.equals("UNKNOWN") && !this.mCHRUserPrefAutoSwitch && this.mCHRUserPrefSwitchNWName.equals(connectivity.getString("defaultNwName")) && defaultType == this.mCHRUserPrefSwitchNWType) {
                this.mCHRUserPrefManualSwitch = true;
                this.mCHRUserPrefManualSwitchTime = System.currentTimeMillis();
            }
            if (!this.mCHRUserPrefManualSwitch && this.mUserOperation && this.mDestNetworkId.equals("UNKNOWN") && this.mCHRUserPrefAutoSwitch && this.mCHRUserPrefOriginalNWName.equals(connectivity.getString("defaultNwName")) && defaultType == this.mCHRUserPrefOriginalNWType) {
                this.mCHRUserPrefManualSwitch = true;
                this.mCHRUserPrefManualSwitchTime = System.currentTimeMillis();
            }
        }
        if (defaultType == 8 || !this.mDestNetworkId.equals("UNKNOWN")) {
            LogUtil.i("Not setup mDestNetworkId, defaultType=" + defaultType + ", DestNetworkId=" + this.mDestNetworkId);
        } else if (!this.isDestNetworkCellular && 1 == defaultType) {
            this.mDestNetworkId = connectivity.getString("defaultNwId");
            this.mDestNetworkName = connectivity.getString("defaultNwName");
            this.mDestNetworkFreq = connectivity.getString("defaultNwFreq");
            this.mDestNetworkType = 1;
            LogUtil.d("CONNECTIVITY_ACTION WIFI connected: mDestNetworkName:" + this.mDestNetworkName);
            LogUtil.v("                                    mDestNetworkId:" + this.mDestNetworkId);
        } else if (this.isDestNetworkCellular && defaultType == 0) {
            this.mDestNetworkId = connectivity.getString("defaultNwId");
            this.mDestNetworkName = connectivity.getString("defaultNwName");
            this.mDestNetworkFreq = connectivity.getString("defaultNwFreq");
            this.mDestNetworkType = 0;
            LogUtil.d("CONNECTIVITY_ACTION MOBILE connected: mDestNetworkName:" + this.mDestNetworkName);
            LogUtil.v("                                      mDestNetworkId:" + this.mDestNetworkId);
        }
        checkAppCurrentNetwork();
    }

    public void checkAllConnectivityState(int connectedState, int netType) {
        LogUtil.i("checkAllConnectivityState");
        if (3 == connectedState) {
            try {
                LogUtil.i("checkDefaultConnectivityState");
                checkDefaultConnectivityState();
            } catch (Exception e) {
                LogUtil.e("checkAllConnectivityState " + e.getMessage());
            }
        } else if (4 == connectedState) {
            LogUtil.i("checkAppCurrentNetwork");
            if (this.mCHRQoEPrefDetermineSwitch && netType == 0) {
                this.mCHRQoEPrefAutoSwitch = true;
            }
            checkAppCurrentNetwork();
        } else if (1 == connectedState) {
            if (1 == netType) {
                LogUtil.i("checkConnectivityState, wifi start");
                HwWmpAppInfo wifiApp = new HwWmpAppInfo(1);
                LogUtil.v("checkConnectivityState: wifiApp=" + wifiApp.toString());
                setNewStartTime(wifiApp);
                backupWifiInfo();
            }
            if (netType == 0) {
                LogUtil.i("checkConnectivityState, mobile start");
                HwWmpAppInfo mobileApp = new HwWmpAppInfo(0);
                LogUtil.v("checkConnectivityState: mobileApp=" + mobileApp.toString());
                setNewStartTime(mobileApp);
            }
        } else if (2 == connectedState) {
            if (1 == netType) {
                LogUtil.i("checkConnectivityState, wifi end");
                updateWifiDurationEnd();
            }
            if (netType == 0) {
                LogUtil.i("checkConnectivityState, mobile end");
                updateMobileDurationEnd();
            }
        }
    }

    private void checkAppCurrentNetwork() {
        LogUtil.i("checkAppCurrentNetwork ");
        try {
            if (!this.saveAppInfo.isEmpty()) {
                LogUtil.i("              ,saveAppInfo size=" + this.saveAppInfo.size());
                for (Map.Entry<String, HwWmpAppInfo> entry : this.saveAppInfo.entrySet()) {
                    String app = entry.getKey();
                    HwWmpAppInfo info = entry.getValue();
                    int nwType = info.getConMgrNetworkType();
                    int uid = info.getAppUid();
                    if (info.isNormalApp()) {
                        int arbitrationNwType = HwArbitrationFunction.getCurrentNetwork(this.mContext, uid);
                        int currConMgrNwType = 8;
                        if (800 == arbitrationNwType) {
                            currConMgrNwType = 1;
                        } else if (801 == arbitrationNwType) {
                            currConMgrNwType = 0;
                        }
                        if (currConMgrNwType != nwType) {
                            updateAppNetwork(app, currConMgrNwType);
                            LogUtil.d("  network change, app=" + app + ", from " + nwType + " to " + currConMgrNwType);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e("checkAllConnectivityState " + e.getMessage());
        }
    }

    public boolean checkOutOf4GCoverage(boolean restart) {
        boolean outof4G;
        boolean outof4G2;
        long duration_outof4G;
        Bundle cellInfo = NetUtil.getMobileDataState(this.mContext);
        boolean callIdle = NetUtil.isMobileCallStateIdle(this.mContext);
        if (!this.param.getBack4GEnabled()) {
            LogUtil.d("Fast Back 4G Feature - Disabled");
            return false;
        } else if (!callIdle) {
            LogUtil.d("checkOutOf4GCoverage: call exists");
            resetOut4GBeginTime();
            this.handler.removeCallbacks(this.periodicOutof4GHandler);
            return false;
        } else if (cellInfo == null || !cellInfo.containsKey("cellRAT")) {
            LogUtil.d("checkOutOf4GCoverage: null cell RAT, last outof4Gflag=" + false);
            resetOut4GBeginTime();
            this.handler.removeCallbacks(this.periodicOutof4GHandler);
            return false;
        } else {
            String RAT = cellInfo.getString("cellRAT", "UNKNOWN");
            String mState = cellInfo.getString("cellState", "DISABLED");
            int dState = cellInfo.getInt("dataState");
            if (!Arrays.asList(VALIDPREFERREDMODE).contains(Integer.valueOf(cellInfo.getInt("preferredMode")))) {
                LogUtil.i("NW preferred mode:" + preferredMode + " NOT supported");
                outof4G = false;
                this.handler.removeCallbacks(this.periodicOutof4GHandler);
            } else {
                if (mState.equals("DISABLED")) {
                } else if (2 != dState) {
                    Bundle bundle = cellInfo;
                } else if (RAT.equals("4G")) {
                    outof4G = false;
                    resetOut4GBeginTime();
                    this.handler.removeCallbacks(this.periodicOutof4GHandler);
                    if (!this.back4g_begin) {
                        LogUtil.i(" once back to 4G");
                        this.back4g_begin = true;
                        this.back4g_restartCnt = 0;
                        this.mMachineHandler.sendEmptyMessage(141);
                        this.mFastBack2LteChrDAO.addsuccessBack();
                        this.mFastBack2LteChrDAO.insertRecordByLoc();
                    }
                    this.mFastBack2LteChrDAO.endSession();
                } else {
                    this.back4g_begin = false;
                    int cell4gNum = 0;
                    Bundle record4g = this.mSpaceUserDAO.find4gCoverageByCurrLoc();
                    if (record4g.containsKey("cell_num")) {
                        cell4gNum = record4g.getInt("cell_num");
                    }
                    if (cell4gNum == 0) {
                        LogUtil.d("checkOutOf4GCoverage: no 4G cell in this freq location, not trigger searching 4G process");
                    } else {
                        int i = cell4gNum;
                        if (0 == this.outof4G_begin) {
                            this.outof4G_begin = System.currentTimeMillis();
                            this.back4g_restartCnt = 0;
                            LogUtil.i("checkOutOf4GCoverage: 1st out to 4G, begin time=" + this.outof4G_begin);
                            this.mFastBack2LteChrDAO.startSession();
                            this.mFastBack2LteChrDAO.sessionSpace(-1, -1);
                            this.mFastBack2LteChrDAO.addlowRatCnt();
                            this.handler.postDelayed(this.periodicOutof4GHandler, (long) this.param.getBack4GTH_out4G_interval());
                        } else if (restart) {
                            LogUtil.i("checkOutOf4GCoverage: restart(" + this.back4g_restartCnt + ")");
                            if (this.back4g_restartCnt < this.param.getBack4GTH_restart_limit()) {
                                this.handler.postDelayed(this.periodicOutof4GHandler, (long) (this.param.getBack4GTH_out4G_interval() * 2));
                                this.back4g_restartCnt++;
                            }
                        } else {
                            LogUtil.i("checkOutOf4GCoverage: already out to 4G, duration=" + duration_outof4G + ", TH=" + this.param.getBack4GTH_out4G_interval());
                            if (((long) this.param.getBack4GTH_out4G_interval()) < duration_outof4G) {
                                this.handler.removeCallbacks(this.periodicOutof4GHandler);
                                this.mMachineHandler.sendEmptyMessage(140);
                            } else {
                                outof4G2 = true;
                                Bundle bundle2 = cellInfo;
                                this.handler.postDelayed(this.periodicOutof4GHandler, (long) this.param.getBack4GTH_out4G_interval());
                                outof4G = outof4G2;
                                LogUtil.d("checkOutOf4GCoverage: out of 4G=" + outof4G + ", RAT=" + RAT);
                                return outof4G;
                            }
                        }
                    }
                    outof4G2 = true;
                    Bundle bundle3 = cellInfo;
                    outof4G = outof4G2;
                    LogUtil.d("checkOutOf4GCoverage: out of 4G=" + outof4G + ", RAT=" + RAT);
                    return outof4G;
                }
                outof4G = false;
                this.handler.removeCallbacks(this.periodicOutof4GHandler);
                LogUtil.d("checkOutOf4GCoverage: Data Disabled(" + mState + ") or Not Connect(" + dState + "), then stop searching 4G process");
                LogUtil.d("checkOutOf4GCoverage: out of 4G=" + outof4G + ", RAT=" + RAT);
                return outof4G;
            }
            LogUtil.d("checkOutOf4GCoverage: out of 4G=" + outof4G + ", RAT=" + RAT);
            return outof4G;
        }
    }

    public void resetOut4GBeginTime() {
        this.outof4G_begin = 0;
    }

    private Bundle getSpecifiedDataState(int networkType) {
        Bundle output = new Bundle();
        boolean valid = false;
        String nwName = "UNKNOWN";
        String nwId = "UNKNOWN";
        String nwFreq = "UNKNOWN";
        String signal = "UNKNOWN";
        if (1 == networkType) {
            Bundle wifiInfo = NetUtil.getWifiStateString(this.mContext);
            nwName = wifiInfo.getString("wifiAp", "UNKNOWN");
            nwId = wifiInfo.getString("wifiMAC", "UNKNOWN");
            nwFreq = wifiInfo.getString("wifiCh", "UNKNOWN");
            signal = wifiInfo.getString("wifiRssi", "0");
            if (!(nwName == null || nwId == null || nwFreq == null)) {
                valid = true;
                backupWifiInfo();
            }
        }
        if (networkType == 0) {
            Bundle cellInfo = NetUtil.getMobileDataState(this.mContext);
            nwName = cellInfo.getString("cellRAT", "UNKNOWN");
            nwId = cellInfo.getString("cellId", "UNKNOWN");
            nwFreq = cellInfo.getString("cellFreq", "UNKNOWN");
            signal = cellInfo.getString("cellRssi", "UNKNOWN");
            if (!(nwName == null || nwId == null || nwFreq == null)) {
                valid = true;
                backupCellInfo();
            }
        }
        output.putBoolean("VALID", valid);
        output.putString("ID", nwId);
        output.putString("NAME", nwName);
        output.putString("FREQ", nwFreq);
        output.putString("SIGNAL", signal);
        return output;
    }

    public FastBack2LteChrDAO getBack2LteChrDAO() {
        return this.mFastBack2LteChrDAO;
    }

    private void updateUserPrefCHR() {
        if (this.mCHRUserPrefDetermineSwitch) {
            LogUtil.i("updateUserPrefCHR: DetermineSwitch, " + this.mCHRUserPrefOriginalNWName + " ->" + this.mCHRUserPrefSwitchNWName + " Time:" + this.mCHRUserPrefSwitchTime);
            StringBuilder sb = new StringBuilder();
            sb.append("updateUserPrefCHR: Auto Switch Time ");
            sb.append(this.mCHRUserPrefAutoSwitchTime);
            sb.append(" Manual Switch Time:");
            sb.append(this.mCHRUserPrefManualSwitchTime);
            LogUtil.i(sb.toString());
            this.mLocationDAO.accCHRUserPrefTotalSwitchbyFreqLoc(this.freqLoc);
            if (this.mCHRUserPrefAutoSwitch) {
                if (this.mCHRUserPrefManualSwitch && this.mCHRUserPrefManualSwitchTime > this.mCHRUserPrefAutoSwitchTime && this.mCHRUserPrefManualSwitchTime - this.mCHRUserPrefAutoSwitchTime < AppHibernateCst.DELAY_ONE_MINS) {
                    LogUtil.i("updateUserPrefCHR: AutoFail + 1");
                    this.mLocationDAO.accCHRUserPrefAutoFailbyFreqLoc(this.freqLoc);
                }
                if (!this.mCHRUserPrefManualSwitch) {
                    LogUtil.i("updateUserPrefCHR: AutoSucc + 1");
                    this.mLocationDAO.accCHRUserPrefAutoSuccbyFreqLoc(this.freqLoc);
                }
            }
        }
    }

    private void updateQoEPrefCHR() {
        if (this.mCHRQoEPrefDetermineSwitch) {
            LogUtil.i("updateQoEPrefCHR: W2C -> mCHRQoEPrefAutoSwitch:" + this.mCHRQoEPrefAutoSwitch + " mCHRQoEPrefManualSwitch:" + this.mCHRQoEPrefManualSwitch);
            this.mLocationDAO.accCHRUserPrefTotalSwitchbyFreqLoc(this.freqLoc);
            if (this.mCHRQoEPrefAutoSwitch && !this.mCHRQoEPrefManualSwitch) {
                LogUtil.i("updateQoEPrefCHR: W2C Succ + 1");
                this.mLocationDAO.accCHRUserPrefManualSuccbyFreqLoc(this.freqLoc);
            } else if (this.mCHRQoEPrefAutoSwitch && this.mCHRQoEPrefManualSwitch) {
                LogUtil.i("updateQoEPrefCHR: W2C Fail - User Switch back + 1");
                this.mLocationDAO.accCHRUserPrefNoSwitchFailbyFreqLoc(this.freqLoc);
            }
        }
    }

    private void clearUserPrefCHR() {
        this.mCHRUserPrefDetermineSwitch = false;
        this.mCHRUserPrefOriginalNWName = "UNKNOWN";
        this.mCHRUserPrefOriginalNWType = 8;
        this.mCHRUserPrefOriginalNWId = "UNKNOWN";
        this.mCHRUserPrefOriginalNWFreq = "UNKNOWN";
        this.mCHRUserPrefSwitchNWName = "UNKNOWN";
        this.mCHRUserPrefSwitchNWType = 8;
        this.mCHRUserPrefSwitchNWId = "UNKNOWN";
        this.mCHRUserPrefSwitchNWFreq = "UNKNOWN";
        this.mCHRUserPrefSwitchTime = 0;
        this.mCHRUserPrefAutoSwitch = false;
        this.mCHRUserPrefAutoSwitchTime = 0;
        this.mCHRUserPrefManualSwitch = false;
        this.mCHRUserPrefManualSwitchTime = 0;
    }

    private void clearQoEPrefCHR() {
        this.mCHRQoEPrefDetermineSwitch = false;
        this.mCHRQoEPrefAutoSwitch = false;
        this.mCHRQoEPrefManualSwitch = false;
    }

    public void setCHRQoEPrefManualSwitch() {
        if (this.mCHRQoEPrefDetermineSwitch && this.mCHRQoEPrefAutoSwitch) {
            this.mCHRQoEPrefManualSwitch = true;
        }
    }
}
