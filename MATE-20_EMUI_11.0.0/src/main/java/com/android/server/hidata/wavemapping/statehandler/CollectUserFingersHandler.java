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
import com.android.server.hidata.arbitration.HwArbitrationFunction;
import com.android.server.hidata.arbitration.HwArbitrationHistoryQoeManager;
import com.android.server.hidata.wavemapping.HwWaveMappingManager;
import com.android.server.hidata.wavemapping.IWaveMappingCallback;
import com.android.server.hidata.wavemapping.chr.QueryHistAppQoeService;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.android.server.hidata.wavemapping.cons.ContextManager;
import com.android.server.hidata.wavemapping.cons.ParamManager;
import com.android.server.hidata.wavemapping.cons.WMStateCons;
import com.android.server.hidata.wavemapping.dao.FastBack2LteChrDao;
import com.android.server.hidata.wavemapping.dao.HisQoeChrDao;
import com.android.server.hidata.wavemapping.dao.LocationDao;
import com.android.server.hidata.wavemapping.dao.SpaceUserDao;
import com.android.server.hidata.wavemapping.entity.HwWmpAppInfo;
import com.android.server.hidata.wavemapping.entity.ParameterInfo;
import com.android.server.hidata.wavemapping.entity.RecognizeResult;
import com.android.server.hidata.wavemapping.entity.SpaceExpInfo;
import com.android.server.hidata.wavemapping.util.LogUtil;
import com.android.server.hidata.wavemapping.util.NetUtil;
import com.android.server.intellicom.common.SmartDualCardConsts;
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
    private static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss.SSS";
    private static final int DEFAULT_BAD_RATIO_INDEX = 9;
    private static final int DEFAULT_COUNT = 2;
    private static final int DEFAULT_DAYS_INDEX = 8;
    private static final int DEFAULT_DURATION_INDEX = 3;
    private static final int DEFAULT_GOOD_INDEX = 4;
    private static final int DEFAULT_NETWORK_FREQUENT_INDEX = 2;
    private static final int DEFAULT_NETWORK_ID_INDEX = 1;
    private static final int DEFAULT_POOR_INDEX = 5;
    private static final int DEFAULT_QOE_SIZE1 = 4;
    private static final int DEFAULT_QOE_SIZE2 = 9;
    private static final float DEFAULT_RATIO = 2.0f;
    private static final int DEFAULT_RX_INDEX = 6;
    private static final int DEFAULT_TX_INDEX = 7;
    private static final String DEFAULT_VALUE = "0";
    private static final float INVALID_RATIO = -1.0f;
    private static final String KEY_4G = "4G";
    private static final String KEY_APP_QOE_HAS_PREFERENCE = "isAppQoeHasPref";
    private static final String KEY_AVG_SIGNAL = "avg_signal";
    private static final String KEY_CELL_FREQUENT = "cellFreq";
    private static final String KEY_CELL_ID = "cellId";
    private static final String KEY_CELL_NUM = "cell_num";
    private static final String KEY_CELL_RAT = "cellRAT";
    private static final String KEY_CELL_RSSI = "cellRssi";
    private static final String KEY_CELL_STATE = "cellState";
    private static final String KEY_DATA_STATE = "dataState";
    private static final String KEY_DEFAULT_NETWORK_FREQUENCY = "defaultNwFreq";
    private static final String KEY_DEFAULT_NETWORK_ID = "defaultNwId";
    private static final String KEY_DEFAULT_NETWORK_NAME = "defaultNwName";
    private static final String KEY_DEFAULT_TYPE = "defaultType";
    private static final String KEY_DISABLED = "DISABLED";
    private static final String KEY_DURATION_CONNECTED = "duration_connected";
    private static final String KEY_ENABLED = "ENABLED";
    private static final String KEY_FREQUENCY = "FREQ";
    private static final String KEY_ID = "ID";
    private static final String KEY_NAME = "NAME";
    private static final String KEY_NETWORK_NAME = "networkname";
    private static final String KEY_NETWORK_TYPE = "networktype";
    private static final String KEY_PREFERRED_MODE = "preferredMode";
    private static final String KEY_PREFER_NETWORK_NAME = "prefNetworkName";
    private static final String KEY_PREFER_NETWORK_TYPE = "prefNetworkType";
    private static final String KEY_SIGNAL = "SIGNAL";
    private static final String KEY_TOTAL_DURATION = "total_duration";
    private static final String KEY_USER_HAS_PREFERENCE = "isUserHasPref";
    private static final String KEY_USER_PREFERENCE_ENTRY = "user_pref_entery";
    private static final String KEY_USER_PREFERENCE_OPT_IN = "user_pref_opt_in";
    private static final String KEY_USER_PREFERENCE_OPT_OUT = "user_pref_opt_out";
    private static final String KEY_USER_PREFERENCE_STAY = "user_pref_stay";
    private static final String KEY_VALID = "VALID";
    private static final String KEY_WIFI_AP = "wifiAp";
    private static final String KEY_WIFI_CHANNEL = "wifiCh";
    private static final String KEY_WIFI_MAC = "wifiMAC";
    private static final String KEY_WIFI_RSSI = "wifiRssi";
    private static final String KEY_WIFI_STATE = "wifiState";
    private static final int MAP_DEFAULT_CAPACITY = 16;
    private static final int SECOND_UNIT = 60000;
    private static final String TAG = ("WMapping." + CollectUserFingersHandler.class.getSimpleName());
    private static final Integer[] VALID_PREFERRED_MODES = {8, 9, 10, 12, 15, 17, 19, 20, 22, 58, 61, 63};
    private static Bundle lastCellState = new Bundle();
    private static Bundle lastWifiState = new Bundle();
    private static CollectUserFingersHandler mCollectUserFingersHandler = null;
    private int back4gRestartCnt = 0;
    private String freqLoc = Constant.NAME_FREQLOCATION_OTHER;
    private Runnable getRegStateAfterPeriodHandler = new Runnable() {
        /* class com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.AnonymousClass3 */

        @Override // java.lang.Runnable
        public void run() {
            Bundle cellInfo = NetUtil.getMobileDataState(CollectUserFingersHandler.this.mContext);
            if (cellInfo == null) {
                LogUtil.e(false, "cellInfo is null", new Object[0]);
                return;
            }
            LogUtil.i(false, "getRegStateAfterPeriodHandler", new Object[0]);
            String nwName = cellInfo.getString(CollectUserFingersHandler.KEY_CELL_RAT);
            if (nwName != null && CollectUserFingersHandler.KEY_4G.equals(nwName)) {
                CollectUserFingersHandler.this.mFastBack2LteChrDao.addSuccessBack();
                CollectUserFingersHandler.this.mFastBack2LteChrDao.endSession();
            }
            CollectUserFingersHandler.this.mFastBack2LteChrDao.insertRecordByLoc();
        }
    };
    private Handler handler = new Handler();
    private boolean isBack4gBegin = true;
    private boolean isChrUserPrefAutoOn = false;
    private boolean isChrUserPrefDetermineOn = false;
    private boolean isChrUserPrefManualOn = false;
    private boolean isDestNetworkCellular = false;
    private boolean isUserOperated = false;
    private boolean isUserPref = false;
    private int locBatch = 0;
    private long mChrUserPrefAutoSwitchTime = 0;
    private long mChrUserPrefManualSwitchTime = 0;
    private String mChrUserPrefOriginalNwFreq = "UNKNOWN";
    private String mChrUserPrefOriginalNwId = "UNKNOWN";
    private String mChrUserPrefOriginalNwName = "UNKNOWN";
    private int mChrUserPrefOriginalNwType = 8;
    private String mChrUserPrefSwitchNwFreq = "UNKNOWN";
    private String mChrUserPrefSwitchNwId = "UNKNOWN";
    private String mChrUserPrefSwitchNwName = "UNKNOWN";
    private int mChrUserPrefSwitchNwType = 8;
    private long mChrUserPrefSwitchTime = 0;
    LogCollectManager mCollectManger = null;
    private Context mContext;
    private String mDestNetworkFreq = "UNKNOWN";
    private String mDestNetworkId = "UNKNOWN";
    private String mDestNetworkName = "UNKNOWN";
    private int mDestNetworkType = 8;
    private FastBack2LteChrDao mFastBack2LteChrDao;
    private HwWmpFastBackLteManager mFastBackLteMgr = null;
    private HisQoeChrDao mHisQoeChrDao;
    private QueryHistAppQoeService mHisQoeChrService;
    private HwArbitrationHistoryQoeManager mHistoryQoeMgr = null;
    private LocationDao mLocationDao;
    private Handler mMachineHandler;
    private BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {
        /* class com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                LogUtil.w(false, " action is null", new Object[0]);
                return;
            }
            char c = 65535;
            int hashCode = action.hashCode();
            if (hashCode != -229777127) {
                if (hashCode != 233521600) {
                    if (hashCode == 1067187475 && action.equals("com.android.server.hidata.arbitration.HwArbitrationStateMachine")) {
                        c = 1;
                    }
                } else if (action.equals("android.net.wifi.supplicant.STATE_CHANGE")) {
                    c = 0;
                }
            } else if (action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_SIM_STATE_CHANGED)) {
                c = 2;
            }
            if (c != 0) {
                if (c == 1) {
                    LogUtil.i(false, "MP-LINK state changes", new Object[0]);
                    Message msg = Message.obtain(CollectUserFingersHandler.this.mMachineHandler, 91);
                    msg.arg1 = 4;
                    msg.arg2 = 8;
                    CollectUserFingersHandler.this.mMachineHandler.sendMessage(msg);
                } else if (c == 2) {
                    CollectUserFingersHandler.this.setCurrScrbId();
                    LogUtil.i(false, "SIM state changes", new Object[0]);
                }
            } else if (intent.getParcelableExtra("newState") != null && (intent.getParcelableExtra("newState") instanceof SupplicantState)) {
                SupplicantState mCurrentsupplicantState = (SupplicantState) intent.getParcelableExtra("newState");
                LogUtil.i(false, "SUPPLICANT_STATE_CHANGED_ACTION mCurrentsupplicantState = %{public}s", mCurrentsupplicantState);
                if (mCurrentsupplicantState == SupplicantState.COMPLETED) {
                    CollectUserFingersHandler.this.mMachineHandler.sendEmptyMessage(92);
                }
            }
        }
    };
    private String mPrefNetworkName = "UNKNOWN";
    private int mPrefNetworkType = 8;
    private String mSourceNetworkFreq = "UNKNOWN";
    private String mSourceNetworkId = "UNKNOWN";
    private String mSourceNetworkName = "UNKNOWN";
    private int mSourceNetworkType = 8;
    private HashMap<String, SpaceExpInfo> mSpaceExperience = new HashMap<>(16);
    private StringBuilder mSpaceId = new StringBuilder("0");
    private StringBuilder mSpaceIdMainAp = new StringBuilder("0");
    private HashMap<String, SpaceExpInfo> mSpaceSessionDuration = new HashMap<>(16);
    private SpaceUserDao mSpaceUserDao;
    private Map<String, String> mapCollectFileName = new HashMap(16);
    private HashMap<Integer, Integer> netCodes = new HashMap<>(16);
    private long outOf4gBegin = 0;
    private ParameterInfo param;
    private Runnable periodicOutOf4gHandler = new Runnable() {
        /* class com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.AnonymousClass2 */

        @Override // java.lang.Runnable
        public void run() {
            LogUtil.i(false, "periodically check no 4G coverage", new Object[0]);
            CollectUserFingersHandler.this.checkOutOf4gCoverage(false);
        }
    };
    private HashMap<String, HwWmpAppInfo> saveAppInfo = new HashMap<>(16);

    public CollectUserFingersHandler(Handler handler2) {
        LogUtil.i(false, " ,new CollectUserFingersHandler ", new Object[0]);
        try {
            this.mContext = ContextManager.getInstance().getContext();
            Map<String, String> map = this.mapCollectFileName;
            map.put("wifipro", Constant.getRawDataPath() + "network" + Constant.RAW_FILE_WIFIPRO_EXTENSION);
            this.param = ParamManager.getInstance().getParameterInfo();
            this.mFastBackLteMgr = HwWmpFastBackLteManager.getInstance();
            this.mSpaceUserDao = new SpaceUserDao();
            this.mLocationDao = new LocationDao();
            this.mHisQoeChrDao = new HisQoeChrDao();
            this.mHisQoeChrService = QueryHistAppQoeService.getInstance();
            this.mFastBack2LteChrDao = new FastBack2LteChrDao();
            this.mMachineHandler = handler2;
            this.mHistoryQoeMgr = HwArbitrationHistoryQoeManager.getInstance(handler2);
            this.mCollectManger = new LogCollectManager(this.mContext);
            startCollect();
        } catch (Exception e) {
            LogUtil.e(false, "CollectUserFingersHandler init failed by Exception", new Object[0]);
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
        clearUserPrefChr();
        if (!this.mFastBack2LteChrDao.getLocation().equals(location)) {
            this.mFastBack2LteChrDao.resetSession();
            this.mFastBack2LteChrDao.insertRecordByLoc();
            this.mFastBack2LteChrDao.setLocation(location);
            if (!this.mFastBack2LteChrDao.getCountersByLocation()) {
                this.mFastBack2LteChrDao.insertRecordByLoc();
            }
        }
        if (!this.mHisQoeChrDao.getLocation().equals(location)) {
            this.mHisQoeChrDao.insertRecordByLoc();
            this.mHisQoeChrDao.setLocation(location);
            this.mHisQoeChrDao.getCountersByLocation();
        }
        this.freqLoc = location;
        this.mSpaceUserDao.setFreqLocation(location);
    }

    public void setModelVer(int modelAllAp, int modelMainAp) {
        LogUtil.i(false, " set Model VERSION: %{public}d_%{public}d", Integer.valueOf(modelAllAp), Integer.valueOf(modelMainAp));
        this.mSpaceUserDao.setModelVer(modelAllAp, modelMainAp);
    }

    public void setCurrScrbId() {
        this.mSpaceUserDao.setSubscribeId(NetUtil.getMobileDataScrbId(this.mContext, this.mCollectManger));
    }

    public void setBatch(int batch) {
        this.locBatch = batch;
    }

    public final void startCollect() {
        try {
            LogUtil.i(false, " startCollect ", new Object[0]);
            setCurrScrbId();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
            filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_SIM_STATE_CHANGED);
            filter.addAction("com.android.server.hidata.arbitration.HwArbitrationStateMachine");
            this.mContext.registerReceiver(this.mNetworkReceiver, filter, "com.huawei.hidata.permission.MPLINK_START_CHECK", null);
            int networkType = NetUtil.getNetworkTypeInfo(this.mContext);
            if (networkType == 1) {
                setNewStartTime(new HwWmpAppInfo(1));
                LogUtil.d(false, " Wifi active ", new Object[0]);
                backupWifiInfo();
            } else {
                resetTime(Constant.USERDB_APP_NAME_WIFI);
            }
            if (networkType == 0) {
                setNewStartTime(new HwWmpAppInfo(0));
                LogUtil.d(false, " Mobile active ", new Object[0]);
                backupCellInfo();
            } else {
                resetTime(Constant.USERDB_APP_NAME_MOBILE);
            }
            regNetworkCallback();
            checkOutOf4gCoverage(false);
        } catch (Exception e) {
            LogUtil.e(false, "startCollect failed by Exception", new Object[0]);
        }
    }

    private void regNetworkCallback() {
        final ConnectivityManager connectivityManager = (ConnectivityManager) this.mContext.getSystemService("connectivity");
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            /* class com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.AnonymousClass4 */

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onAvailable(Network network) {
                super.onAvailable(network);
                CollectUserFingersHandler.this.availableCallback(network, connectivityManager);
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLosing(Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                LogUtil.i(false, "NetworkCallback: onLosing", new Object[0]);
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLost(Network network) {
                CollectUserFingersHandler.this.lostCallback(network);
                super.onLost(network);
            }
        };
        ConnectivityManager.NetworkCallback networkCallbackDef = new ConnectivityManager.NetworkCallback() {
            /* class com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.AnonymousClass5 */

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onAvailable(Network network) {
                super.onAvailable(network);
                CollectUserFingersHandler.this.availableCallbackDef(network, connectivityManager);
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLosing(Network network, int maxMsToLive) {
                super.onLosing(network, maxMsToLive);
                LogUtil.i(false, "NetworkCallbackD: onLosing", new Object[0]);
            }

            @Override // android.net.ConnectivityManager.NetworkCallback
            public void onLost(Network network) {
                LogUtil.i(false, "NetworkCallbackD: onLost=%{public}d", Integer.valueOf(network.hashCode()));
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void availableCallback(Network network, ConnectivityManager connectivityManager) {
        LogUtil.i(false, "NetworkCallback: onAvailable = %{public}d", Integer.valueOf(network.hashCode()));
        NetworkInfo netInfo = connectivityManager.getNetworkInfo(network);
        if (netInfo != null) {
            LogUtil.v(false, "NetworkCallback: networksInfo is %{public}s", netInfo.toString());
            if (!netInfo.isConnected()) {
                return;
            }
            if (netInfo.getType() == 1 || netInfo.getType() == 0) {
                this.netCodes.put(Integer.valueOf(netInfo.getType()), Integer.valueOf(network.hashCode()));
                LogUtil.i(false, "send START to SM: type=%{public}d", Integer.valueOf(netInfo.getType()));
                Message msg = Message.obtain(this.mMachineHandler, 91);
                msg.arg1 = 1;
                msg.arg2 = netInfo.getType();
                this.mMachineHandler.sendMessage(msg);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void availableCallbackDef(Network network, ConnectivityManager connectivityManager) {
        LogUtil.i(false, "NetworkCallbackD: onAvailable=%{public}d", Integer.valueOf(network.hashCode()));
        NetworkInfo netInfo = connectivityManager.getNetworkInfo(network);
        if (netInfo != null) {
            LogUtil.v(false, "NetworkCallbackD: networksInfo is %{public}s", netInfo.toString());
            if (netInfo.getType() == 1) {
                if (netInfo.isConnected()) {
                    LogUtil.i(false, "NetworkCallbackD: wifi connected", new Object[0]);
                } else {
                    LogUtil.i(false, "NetworkCallbackD: wifi connecting", new Object[0]);
                }
            }
            if (netInfo.getType() == 0) {
                if (netInfo.isConnected()) {
                    LogUtil.i(false, "NetworkCallbackD: mobile connected", new Object[0]);
                } else {
                    LogUtil.i(false, "NetworkCallbackD: mobile connecting", new Object[0]);
                }
            }
            Message msg = Message.obtain(this.mMachineHandler, 91);
            msg.arg1 = 3;
            msg.arg2 = 8;
            this.mMachineHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void lostCallback(Network network) {
        LogUtil.i(false, "NetworkCallback: onLost = %{public}d", Integer.valueOf(network.hashCode()));
        LogUtil.i(false, "onLost: netCodes = %{public}s", this.netCodes.toString());
        for (Map.Entry<Integer, Integer> entry : this.netCodes.entrySet()) {
            int type = entry.getKey().intValue();
            if (entry.getValue().intValue() == network.hashCode()) {
                LogUtil.i(false, "send END to SM: type=%{public}d", Integer.valueOf(type));
                Message msg = Message.obtain(this.mMachineHandler, 91);
                msg.arg1 = 2;
                msg.arg2 = type;
                this.mMachineHandler.sendMessage(msg);
            }
        }
    }

    public void updateSourceNetwork() {
        try {
            Bundle connectivity = NetUtil.getConnectedNetworkState(this.mContext);
            if (connectivity == null) {
                LogUtil.e(false, "connectivity is null", new Object[0]);
                return;
            }
            String networkId = "UNKNOWN";
            String networkName = "UNKNOWN";
            String networkFreq = "UNKNOWN";
            int defaultType = connectivity.getInt(KEY_DEFAULT_TYPE);
            if (!this.isDestNetworkCellular || defaultType != 0) {
                if (defaultType == 1 || defaultType == 0) {
                    networkId = connectivity.getString(KEY_DEFAULT_NETWORK_ID, "UNKNOWN");
                    networkName = connectivity.getString(KEY_DEFAULT_NETWORK_NAME, "UNKNOWN");
                    networkFreq = connectivity.getString(KEY_DEFAULT_NETWORK_FREQUENCY, "UNKNOWN");
                }
                this.mSourceNetworkId = networkId;
                this.mSourceNetworkName = networkName;
                this.mSourceNetworkFreq = networkFreq;
                this.mSourceNetworkType = defaultType;
                LogUtil.v(false, " updateSourceNetwork: mSourceNetworkId: %{public}s mSourceNetworkName %{public}s", this.mSourceNetworkId, this.mSourceNetworkName);
                return;
            }
            LogUtil.i(false, "Disable WIFI without connection ", new Object[0]);
        } catch (Exception e) {
            LogUtil.e(false, "updateSourceNetwork failed by Exception", new Object[0]);
        }
    }

    public void updateUserAction(String action, String apkName) {
        try {
            this.isDestNetworkCellular = "ACTION_ENABLE_WIFI_FALSE".equals(action);
            if (!this.isUserOperated) {
                this.isUserOperated = true;
                this.mMachineHandler.sendEmptyMessage(115);
                clearSavedPreference();
            }
            this.mDestNetworkId = "UNKNOWN";
            this.mDestNetworkName = "UNKNOWN";
            this.mDestNetworkFreq = "UNKNOWN";
            this.mDestNetworkType = 8;
            LogUtil.d(false, " updateUserAction: action: %{public}s apkName:%{public}s isDestNetworkCellular:%{public}s", action, apkName, String.valueOf(this.isDestNetworkCellular));
        } catch (Exception e) {
            LogUtil.e(false, "updateUserAction failed by Exception", new Object[0]);
        }
    }

    private void updateUserPreference() {
        SpaceExpInfo sourceNetworkSpaceInfo = null;
        SpaceExpInfo destNetworkSpaceInfo = null;
        try {
            if (this.mSourceNetworkName.equals(this.mDestNetworkName)) {
                this.isUserOperated = false;
                LogUtil.i(false, "mSourceNetworkName = mDestNetworkName, isUserOperated = false", new Object[0]);
            }
            if (this.isUserOperated) {
                if (!"UNKNOWN".equals(this.mSourceNetworkId) || !"UNKNOWN".equals(this.mDestNetworkId)) {
                    if (this.mSpaceExperience.containsKey(this.mSourceNetworkId)) {
                        sourceNetworkSpaceInfo = this.mSpaceExperience.get(this.mSourceNetworkId);
                    }
                    if (sourceNetworkSpaceInfo == null) {
                        sourceNetworkSpaceInfo = new SpaceExpInfo(this.mSpaceId, this.mSpaceIdMainAp, this.mSourceNetworkId, this.mSourceNetworkName, this.mSourceNetworkFreq, this.mSourceNetworkType);
                    }
                    sourceNetworkSpaceInfo.accUserPrefOptOut();
                    this.mSpaceExperience.put(this.mSourceNetworkId, sourceNetworkSpaceInfo);
                    LogUtil.i(false, "sourceNetworkSpaceInfo: %{public}s", sourceNetworkSpaceInfo.toString());
                    if (this.mSpaceExperience.containsKey(this.mDestNetworkId)) {
                        destNetworkSpaceInfo = this.mSpaceExperience.get(this.mDestNetworkId);
                    }
                    if (destNetworkSpaceInfo == null) {
                        destNetworkSpaceInfo = new SpaceExpInfo(this.mSpaceId, this.mSpaceIdMainAp, this.mDestNetworkId, this.mDestNetworkName, this.mDestNetworkFreq, this.mDestNetworkType);
                    }
                    destNetworkSpaceInfo.accUserPrefOptIn();
                    this.mSpaceExperience.put(this.mDestNetworkId, destNetworkSpaceInfo);
                    LogUtil.i(false, "destNetworkSpaceInfo: %{public}s", destNetworkSpaceInfo.toString());
                }
            } else if (this.isChrUserPrefDetermineOn && this.isChrUserPrefAutoOn) {
                if (this.mSpaceExperience.containsKey(this.mChrUserPrefOriginalNwId)) {
                    sourceNetworkSpaceInfo = this.mSpaceExperience.get(this.mChrUserPrefOriginalNwId);
                }
                if (sourceNetworkSpaceInfo == null) {
                    sourceNetworkSpaceInfo = new SpaceExpInfo(this.mSpaceId, this.mSpaceIdMainAp, this.mChrUserPrefOriginalNwId, this.mChrUserPrefOriginalNwName, this.mChrUserPrefOriginalNwFreq, this.mChrUserPrefOriginalNwType);
                }
                sourceNetworkSpaceInfo.accUserPrefOptOut();
                this.mSpaceExperience.put(this.mChrUserPrefOriginalNwId, sourceNetworkSpaceInfo);
                LogUtil.i(false, "WM Auto Switch from Network: %{public}s", sourceNetworkSpaceInfo.toString());
                if (this.mSpaceExperience.containsKey(this.mChrUserPrefSwitchNwId)) {
                    destNetworkSpaceInfo = this.mSpaceExperience.get(this.mChrUserPrefSwitchNwId);
                }
                if (destNetworkSpaceInfo == null) {
                    destNetworkSpaceInfo = new SpaceExpInfo(this.mSpaceId, this.mSpaceIdMainAp, this.mChrUserPrefSwitchNwId, this.mChrUserPrefSwitchNwName, this.mChrUserPrefSwitchNwFreq, this.mChrUserPrefSwitchNwType);
                }
                destNetworkSpaceInfo.accUserPrefOptIn();
                this.mSpaceExperience.put(this.mChrUserPrefSwitchNwId, destNetworkSpaceInfo);
                LogUtil.i(false, " -> %{public}s", destNetworkSpaceInfo.toString());
            } else if (this.mSpaceSessionDuration.size() != 0) {
                SpaceExpInfo maxEntry = null;
                SpaceExpInfo spaceExpEntry = null;
                for (Map.Entry<String, SpaceExpInfo> entry : this.mSpaceSessionDuration.entrySet()) {
                    SpaceExpInfo value = entry.getValue();
                    LogUtil.i(false, " mSpaceSessionDuration:%{public}s", value.toString());
                    if (maxEntry == null || maxEntry.getDuration() < value.getDuration()) {
                        maxEntry = value;
                    }
                }
                if (maxEntry != null && maxEntry.getDuration() > 0 && !maxEntry.getNetworkId().equals("UNKNOWN")) {
                    if (this.mSpaceExperience.containsKey(maxEntry.getNetworkId())) {
                        spaceExpEntry = this.mSpaceExperience.get(maxEntry.getNetworkId());
                    }
                    if (spaceExpEntry == null) {
                        spaceExpEntry = new SpaceExpInfo(this.mSpaceId, this.mSpaceIdMainAp, maxEntry.getNetworkId(), maxEntry.getNetworkName(), maxEntry.getNetworkFreq(), maxEntry.getNetworkType());
                    }
                    spaceExpEntry.accUserPrefStay();
                    this.mSpaceExperience.put(maxEntry.getNetworkId(), spaceExpEntry);
                    LogUtil.i(false, "No manual operation, USER_PREF_STAY Network: %{public}s", spaceExpEntry.toString());
                }
            }
            LogUtil.i(false, " updateUserPreference record size: %{public}d", Integer.valueOf(this.mSpaceExperience.size()));
        } catch (Exception e) {
            LogUtil.e(false, "updateUserPreference failed by Exception", new Object[0]);
        }
    }

    private void clearUserPreference() {
        this.isUserOperated = false;
        this.mSourceNetworkId = "UNKNOWN";
        this.mSourceNetworkName = "UNKNOWN";
        this.mSourceNetworkFreq = "UNKNOWN";
        this.mSourceNetworkType = 8;
        this.mDestNetworkId = "UNKNOWN";
        this.mDestNetworkName = "UNKNOWN";
        this.mDestNetworkFreq = "UNKNOWN";
        this.mDestNetworkType = 8;
        this.isDestNetworkCellular = false;
        this.isUserPref = false;
        this.mPrefNetworkName = "UNKNOWN";
        this.mPrefNetworkType = 8;
    }

    public void startAppCollect(HwWmpAppInfo appInfo) {
        LogUtil.i(false, " startAppCollect ", new Object[0]);
        try {
            String app = appInfo.getAppName();
            if (app == null) {
                LogUtil.i(false, " not handle this APP:%{public}d", Integer.valueOf(appInfo.getScenceId()));
                return;
            }
            setNewStartTime(appInfo);
            LogUtil.d(false, " startAppCollect, scenes:%{public}d, app:%{public}s, appNwType:%{public}d", Integer.valueOf(appInfo.getScenceId()), app, Integer.valueOf(appInfo.getConMgrNetworkType()));
        } catch (Exception e) {
            LogUtil.e(false, "startAppCollect failed by Exception", new Object[0]);
        }
    }

    public void endAppCollect(HwWmpAppInfo appInfo) {
        int signalVal;
        LogUtil.i(false, " endAppCollect ", new Object[0]);
        try {
            String app = appInfo.getAppName();
            if (app == null) {
                LogUtil.i(false, " APP is null: scenesId=%{public}d", Integer.valueOf(appInfo.getScenceId()));
            } else if (this.saveAppInfo.isEmpty()) {
                LogUtil.w(false, " no saved APP", new Object[0]);
            } else if (!this.saveAppInfo.containsKey(app)) {
                LogUtil.d(false, " no this APP: scenesId=%{public}d", Integer.valueOf(appInfo.getScenceId()));
            } else {
                int network = appInfo.getConMgrNetworkType();
                Bundle state = getSpecifiedDataState(network);
                if (state == null) {
                    LogUtil.e(false, "state is null", new Object[0]);
                } else if (!state.getBoolean(KEY_VALID)) {
                    LogUtil.w(false, " network info is null", new Object[0]);
                } else {
                    String nwName = state.getString(KEY_NAME);
                    String nwId = state.getString(KEY_ID);
                    String nwFreq = state.getString(KEY_FREQUENCY);
                    String signal = state.getString(KEY_SIGNAL);
                    if (signal == null || "".equalsIgnoreCase(signal) || signal.equalsIgnoreCase("UNKNOWN")) {
                        signalVal = 0;
                    } else {
                        signalVal = Integer.parseInt(signal.trim());
                    }
                    LogUtil.d(false, " endAppCollect, scenes:%{public}d, app:%{public}s", Integer.valueOf(appInfo.getScenceId()), app);
                    LogUtil.v(false, " appNwType:%{public}d, nwId:%{public}s, signal:%{public}d", Integer.valueOf(network), nwId, Integer.valueOf(signalVal));
                    updateDurationByNwId(app, nwId, nwName, nwFreq, network, signalVal);
                    resetTime(appInfo.getAppName());
                    LogUtil.i(false, " endAppCollect, app:%{public}s, nwType:%{public}d", app, Integer.valueOf(appInfo.getConMgrNetworkType()));
                }
            }
        } catch (Exception e) {
            LogUtil.e(false, "endAppCollect failed by Exception", new Object[0]);
        }
    }

    public void updateAppNetwork(String app, int newAppNw) {
        int signalVal;
        LogUtil.i(false, " updateAppNetwork ", new Object[0]);
        if (app == null) {
            try {
                LogUtil.i(false, " APP is null", new Object[0]);
            } catch (Exception e) {
                LogUtil.e(false, "updateAppNetwork failed by Exception", new Object[0]);
            }
        } else if (this.saveAppInfo.isEmpty()) {
            LogUtil.w(false, " no saved APP", new Object[0]);
        } else if (!this.saveAppInfo.containsKey(app)) {
            LogUtil.d(false, " no this APP in containsKey", new Object[0]);
        } else {
            int oldAppNw = this.saveAppInfo.get(app).getConMgrNetworkType();
            if (newAppNw == oldAppNw) {
                try {
                    LogUtil.d(false, " new == old, not to update", new Object[0]);
                } catch (Exception e2) {
                    LogUtil.e(false, "updateAppNetwork failed by Exception", new Object[0]);
                }
            } else {
                Bundle state = getSpecifiedDataState(oldAppNw);
                if (state == null) {
                    LogUtil.e(false, "state is null", new Object[0]);
                } else if (!state.getBoolean(KEY_VALID)) {
                    LogUtil.w(false, " old network info is null", new Object[0]);
                    updateStartTime(app, newAppNw);
                } else {
                    String nwName = state.getString(KEY_NAME);
                    String nwId = state.getString(KEY_ID);
                    String nwFreq = state.getString(KEY_FREQUENCY);
                    String signal = state.getString(KEY_SIGNAL);
                    if (signal == null || "".equalsIgnoreCase(signal) || signal.equalsIgnoreCase("UNKNOWN")) {
                        signalVal = 0;
                    } else {
                        signalVal = Integer.parseInt(signal.trim());
                    }
                    LogUtil.d(false, " updateAppNetwork, scenes:%{public}d, app:%{public}s", Integer.valueOf(this.saveAppInfo.get(app).getScenceId()), app);
                    LogUtil.v(false, " app:%{public}s, old appNwType:%{public}d, nwId:%{public}s, new appNwType:%{public}d, signal:%{public}d", app, Integer.valueOf(oldAppNw), nwId, Integer.valueOf(newAppNw), Integer.valueOf(signalVal));
                    updateDurationByNwId(app, nwId, nwName, nwFreq, oldAppNw, signalVal);
                    updateStartTime(app, newAppNw);
                }
            }
        }
    }

    public void updateAppQoE(String app, int levelQoe) {
        String signal;
        LogUtil.i(false, " updateAppQoE ", new Object[0]);
        SpaceExpInfo spaceInfo = null;
        if (app == null) {
            try {
                LogUtil.i(false, " APP is null", new Object[0]);
            } catch (Exception e) {
                LogUtil.e(false, "updateAppQoE failed by Exception", new Object[0]);
            }
        } else if (this.saveAppInfo.isEmpty()) {
            LogUtil.w(false, " no saved APP", new Object[0]);
        } else if (!this.saveAppInfo.containsKey(app)) {
            LogUtil.d(false, " no this APP", new Object[0]);
        } else {
            int network = this.saveAppInfo.get(app).getConMgrNetworkType();
            Bundle state = getSpecifiedDataState(network);
            if (state == null) {
                LogUtil.e(false, "state is null", new Object[0]);
            } else if (!state.getBoolean(KEY_VALID)) {
                LogUtil.w(false, " network info is null", new Object[0]);
            } else {
                String nwName = state.getString(KEY_NAME);
                String nwId = state.getString(KEY_ID);
                String nwFreq = state.getString(KEY_FREQUENCY);
                String signal2 = state.getString(KEY_SIGNAL);
                int signalVal = 0;
                if (this.mSpaceExperience.containsKey(nwId)) {
                    spaceInfo = this.mSpaceExperience.get(nwId);
                }
                if (spaceInfo == null) {
                    signal = signal2;
                    spaceInfo = new SpaceExpInfo(this.mSpaceId, this.mSpaceIdMainAp, nwId, nwName, nwFreq, network);
                } else {
                    signal = signal2;
                }
                if (levelQoe == 1) {
                    spaceInfo.accAppPoor(app);
                }
                if (levelQoe == 2) {
                    spaceInfo.accAppGood(app);
                }
                if (signal != null && !"".equalsIgnoreCase(signal) && !signal.equalsIgnoreCase("UNKNOWN")) {
                    int signalVal2 = Integer.parseInt(signal.trim());
                    spaceInfo.accSignalValue(signalVal2);
                    signalVal = signalVal2;
                }
                LogUtil.d(false, " updateAppQoE, app:%{public}s, level:%{public}d, poor count=%{public}d, good count=%{public}d, signal:%{public}d", app, Integer.valueOf(levelQoe), Integer.valueOf(spaceInfo.getAppQoePoor(app)), Integer.valueOf(spaceInfo.getAppQoeGood(app)), Integer.valueOf(signalVal));
                LogUtil.v(false, "nwId:%{public}s", nwId);
                this.mSpaceExperience.put(nwId, spaceInfo);
                LogUtil.i(false, " updateAppQoE record size: %{public}d spaceInfo:%{public}s", Integer.valueOf(this.mSpaceExperience.size()), spaceInfo.toString());
            }
        }
    }

    public void updateWifiDurationForAp(boolean isChanged) {
        LogUtil.i(false, " updateWifiDurationForAp ", new Object[0]);
        try {
            Bundle wifiInfo = NetUtil.getWifiStateString(this.mContext);
            if (wifiInfo != null) {
                if (lastWifiState != null) {
                    String newBssid = wifiInfo.getString(KEY_WIFI_MAC, "UNKNOWN");
                    String newSsid = wifiInfo.getString(KEY_WIFI_AP, "UNKNOWN");
                    String newState = wifiInfo.getString(KEY_WIFI_STATE, "UNKNOWN");
                    if (newBssid == null) {
                        newBssid = "UNKNOWN";
                    }
                    String oldBssid = lastWifiState.getString(KEY_WIFI_MAC, "UNKNOWN");
                    String oldSsid = lastWifiState.getString(KEY_WIFI_AP, "UNKNOWN");
                    String oldFreq = lastWifiState.getString(KEY_WIFI_CHANNEL, "UNKNOWN");
                    String signal = lastWifiState.getString(KEY_WIFI_RSSI, "0");
                    if (oldBssid == null) {
                        oldBssid = "UNKNOWN";
                    }
                    if (!newSsid.equals(oldSsid) || !newBssid.equals(oldBssid)) {
                        LogUtil.d(false, " wifi id changed", new Object[0]);
                        LogUtil.i(false, " new=%{private}s - %{private}s , old=%{private}s - %{private}s", newSsid, newBssid, oldSsid, oldBssid);
                    } else {
                        LogUtil.i(false, " wifi id NOT changed:%{private}s", newSsid);
                        if (isChanged) {
                            return;
                        }
                    }
                    backupWifiInfo();
                    int signalVal = 0;
                    if (signal != null && !"".equalsIgnoreCase(signal) && !signal.equalsIgnoreCase("UNKNOWN")) {
                        signalVal = Integer.parseInt(signal.trim());
                    }
                    updateRecord(oldBssid, oldSsid, oldFreq, signalVal);
                    if (!KEY_ENABLED.equals(newState) || newBssid.contains("UNKNOWN")) {
                        LogUtil.d(false, " current wifi == null", new Object[0]);
                        resetTime(Constant.USERDB_APP_NAME_WIFI);
                    }
                    return;
                }
            }
            LogUtil.e(false, "wifiInfo or lastWifiState is null", new Object[0]);
        } catch (Exception e) {
            LogUtil.e(false, "updateWifiDurationByAp failed by Exception", new Object[0]);
        }
    }

    private void updateRecord(String oldBssid, String oldSsid, String oldFreq, int signalVal) {
        LogUtil.i(false, " updateWifiDurationForAp, save to :%{private}s", oldSsid);
        if (!this.saveAppInfo.isEmpty()) {
            LogUtil.i(false, " saveAppInfo.size=%{public}d", Integer.valueOf(this.saveAppInfo.size()));
            for (Map.Entry<String, HwWmpAppInfo> entry : this.saveAppInfo.entrySet()) {
                String app = entry.getKey();
                HwWmpAppInfo info = entry.getValue();
                int nwType = info.getConMgrNetworkType();
                LogUtil.d(false, " saveAppInfo, app: %{public}s, network=%{public}d, startTime=%{public}s", info.getAppName(), Integer.valueOf(nwType), String.valueOf(info.getStartTime()));
                if (nwType != 0) {
                    updateDurationByNwId(app, oldBssid, oldSsid, oldFreq, nwType, signalVal);
                }
            }
        }
    }

    private void updateWifiDurationEnd() {
        String oldBssid;
        LogUtil.i(false, " updateWifiDurationEnd ", new Object[0]);
        try {
            if (lastWifiState == null) {
                LogUtil.e(false, "lastWifiState is null", new Object[0]);
                return;
            }
            String oldBssid2 = lastWifiState.getString(KEY_WIFI_MAC, "UNKNOWN");
            String oldSsid = lastWifiState.getString(KEY_WIFI_AP, "UNKNOWN");
            String oldFreq = lastWifiState.getString(KEY_WIFI_CHANNEL, "UNKNOWN");
            String signal = lastWifiState.getString(KEY_WIFI_RSSI, "0");
            if (oldBssid2 == null) {
                oldBssid = "UNKNOWN";
            } else {
                oldBssid = oldBssid2;
            }
            int signalVal = (signal == null || "".equalsIgnoreCase(signal) || signal.equalsIgnoreCase("UNKNOWN")) ? 0 : Integer.parseInt(signal.trim());
            LogUtil.i(false, " updateWifiDurationEnd, save to : %{private}s", oldSsid);
            if (!this.saveAppInfo.isEmpty()) {
                LogUtil.i(false, "                   , saveAppInfo.size=%{public}d", Integer.valueOf(this.saveAppInfo.size()));
                for (Map.Entry<String, HwWmpAppInfo> entry : this.saveAppInfo.entrySet()) {
                    String app = entry.getKey();
                    HwWmpAppInfo info = entry.getValue();
                    int nwType = info.getConMgrNetworkType();
                    LogUtil.d(false, " saveAppInfo, app: %{public}s, network=%{public}d, startTime=%{public}s", info.getAppName(), Integer.valueOf(nwType), String.valueOf(info.getStartTime()));
                    if (nwType != 0) {
                        updateDurationByNwId(app, oldBssid, oldSsid, oldFreq, nwType, signalVal);
                    }
                }
            }
            resetTime(Constant.USERDB_APP_NAME_WIFI);
        } catch (RuntimeException e) {
            LogUtil.e(false, " updateWifiDurationEnd, RuntimeException: %{public}s", e.getMessage());
        } catch (Exception e2) {
            LogUtil.e(false, "updateWifiDurationEnd failed by Exception", new Object[0]);
        }
    }

    private void backupWifiInfo() {
        LogUtil.i(false, " backupWifiInfo ", new Object[0]);
        try {
            Bundle wifiInfo = NetUtil.getWifiStateString(this.mContext);
            if (wifiInfo == null) {
                LogUtil.e(false, "wifiInfo is null", new Object[0]);
                return;
            }
            String newState = wifiInfo.getString(KEY_WIFI_STATE, "UNKNOWN");
            String newBssid = wifiInfo.getString(KEY_WIFI_MAC, "UNKNOWN");
            if (KEY_ENABLED.equalsIgnoreCase(newState) && newBssid != null && !newBssid.contains("UNKNOWN")) {
                lastWifiState = wifiInfo.deepCopy();
                if (lastWifiState == null) {
                    LogUtil.e(false, "lastWifiState is null", new Object[0]);
                } else {
                    LogUtil.i(false, " wifi ENABLED, backup info: ssid=%{private}s, %{public}s", lastWifiState.getString(KEY_WIFI_AP), lastWifiState.getString(KEY_WIFI_STATE));
                }
            }
        } catch (RuntimeException e) {
            LogUtil.e(false, " backupWifiInfo, RuntimeException: %{public}s", e.getMessage());
        } catch (Exception e2) {
            LogUtil.e(false, "backupWifiInfo failed by Exception", new Object[0]);
        }
    }

    public void updateMobileDurationForCell(boolean isChanged) {
        String newId;
        int i;
        LogUtil.i(false, " updateMobileDurationForCell ", new Object[0]);
        try {
            Bundle cellInfo = NetUtil.getMobileDataState(this.mContext);
            if (cellInfo != null) {
                if (lastCellState != null) {
                    String newRat = cellInfo.getString(KEY_CELL_RAT, "UNKNOWN");
                    String newId2 = cellInfo.getString(KEY_CELL_ID, "UNKNOWN");
                    if (newId2 == null) {
                        newId = "UNKNOWN";
                    } else {
                        newId = newId2;
                    }
                    String oldRat = lastCellState.getString(KEY_CELL_RAT, "UNKNOWN");
                    String oldId = lastCellState.getString(KEY_CELL_ID, "UNKNOWN");
                    String oldFreq = lastCellState.getString(KEY_CELL_FREQUENT, "UNKNOWN");
                    String signal = cellInfo.getString(KEY_CELL_RSSI, "0");
                    if (oldId == null) {
                        oldId = "UNKNOWN";
                    }
                    if (isChanged) {
                        LogUtil.i(false, " check cell ID change ", new Object[0]);
                        if (newId.equals(oldId) && newRat.equals(oldRat)) {
                            LogUtil.d(false, " cell id not changed ", new Object[0]);
                            return;
                        }
                    }
                    backupCellInfo();
                    int signalVal = (signal == null || signal.isEmpty() || signal.equalsIgnoreCase("UNKNOWN")) ? 0 : Integer.parseInt(signal.trim());
                    LogUtil.i(false, "updateMobileDurationForCell, save to cellId: ", new Object[0]);
                    if (!this.saveAppInfo.isEmpty()) {
                        int i2 = 1;
                        LogUtil.i(false, "saveAppInfo.size=%{public}d", Integer.valueOf(this.saveAppInfo.size()));
                        for (Map.Entry<String, HwWmpAppInfo> entry : this.saveAppInfo.entrySet()) {
                            String app = entry.getKey();
                            HwWmpAppInfo info = entry.getValue();
                            int nwType = info.getConMgrNetworkType();
                            Object[] objArr = new Object[3];
                            objArr[0] = info.getAppName();
                            objArr[i2] = Integer.valueOf(nwType);
                            objArr[2] = String.valueOf(info.getStartTime());
                            LogUtil.d(false, " saveAppInfo, app: %{public}s, network=%{public}d, startTime=%{public}s", objArr);
                            if (nwType != i2) {
                                i = i2;
                                updateDurationByNwId(app, oldId, oldRat, oldFreq, nwType, signalVal);
                            } else {
                                i = i2;
                            }
                            i2 = i;
                        }
                    }
                    return;
                }
            }
            LogUtil.e(false, "cellInfo or lastCellState is null", new Object[0]);
        } catch (Exception e) {
            LogUtil.e(false, "updateMobileDurationForCell failed by Exception", new Object[0]);
        }
    }

    private void updateMobileDurationEnd() {
        String oldId;
        LogUtil.i(false, " updateMobileDurationEnd ", new Object[0]);
        try {
            Bundle cellInfo = NetUtil.getMobileDataState(this.mContext);
            if (cellInfo != null) {
                if (lastCellState != null) {
                    String oldRat = lastCellState.getString(KEY_CELL_RAT, "UNKNOWN");
                    String oldId2 = lastCellState.getString(KEY_CELL_ID, "UNKNOWN");
                    String oldFreq = lastCellState.getString(KEY_CELL_FREQUENT, "UNKNOWN");
                    String signal = cellInfo.getString(KEY_CELL_RSSI, "0");
                    if (oldId2 == null) {
                        oldId = "UNKNOWN";
                    } else {
                        oldId = oldId2;
                    }
                    int signalVal = (signal == null || "".equalsIgnoreCase(signal) || signal.equalsIgnoreCase("UNKNOWN")) ? 0 : Integer.parseInt(signal.trim());
                    LogUtil.i(false, "updateMobileDurationEnd, save to cellId: ", new Object[0]);
                    if (!this.saveAppInfo.isEmpty()) {
                        LogUtil.i(false, "                   , saveAppInfo.size=%{public}d", Integer.valueOf(this.saveAppInfo.size()));
                        for (Map.Entry<String, HwWmpAppInfo> entry : this.saveAppInfo.entrySet()) {
                            String app = entry.getKey();
                            HwWmpAppInfo info = entry.getValue();
                            int nwType = info.getConMgrNetworkType();
                            LogUtil.d(false, " saveAppInfo, app: %{public}s, network=%{public}d, startTime=%{public}s", info.getAppName(), Integer.valueOf(nwType), String.valueOf(info.getStartTime()));
                            if (nwType != 1) {
                                updateDurationByNwId(app, oldId, oldRat, oldFreq, nwType, signalVal);
                            }
                        }
                    }
                    resetTime(Constant.USERDB_APP_NAME_MOBILE);
                    return;
                }
            }
            LogUtil.e(false, "cellInfo or lastCellState is null", new Object[0]);
        } catch (Exception e) {
            LogUtil.e(false, "updateMobileDurationEnd failed by Exception", new Object[0]);
        }
    }

    private void backupCellInfo() {
        LogUtil.i(false, " backupCellInfo ", new Object[0]);
        try {
            Bundle cellInfo = NetUtil.getMobileDataState(this.mContext);
            if (cellInfo == null) {
                LogUtil.e(false, "cellInfo is null", new Object[0]);
                return;
            }
            String cellState = cellInfo.getString(KEY_CELL_STATE, "UNKNOWN");
            String cellId = cellInfo.getString(KEY_CELL_ID, "UNKNOWN");
            if (KEY_ENABLED.equalsIgnoreCase(cellState) && cellId != null && !cellId.contains("UNKNOWN")) {
                lastCellState = cellInfo.deepCopy();
                if (lastCellState == null) {
                    LogUtil.e(false, "lastCellState is null", new Object[0]);
                } else {
                    LogUtil.i(false, " cell ENABLED, backup info: cellRat=%{public}s, %{public}s", lastCellState.getString(KEY_CELL_RAT), lastCellState.getString(KEY_CELL_STATE));
                }
            }
        } catch (RuntimeException e) {
            LogUtil.e(false, " backupCellInfo, RuntimeException: %{public}s", e.getMessage());
        } catch (Exception e2) {
            LogUtil.e(false, "backupCellInfo failed by Exception", new Object[0]);
        }
    }

    private void updateDurationByNwId(String app, String newId, String newNwName, String newNwFreq, int nwType, int signal) {
        String newId2;
        SpaceExpInfo sessionSpaceInfo;
        LogUtil.i(false, " updateDurationByNwId ", new Object[0]);
        try {
            if (this.saveAppInfo.isEmpty()) {
                try {
                    LogUtil.w(false, " no saved APP", new Object[0]);
                } catch (Exception e) {
                    LogUtil.e(false, "updateDurationByNwId failed by Exception", new Object[0]);
                }
            } else if (this.saveAppInfo.get(app) == null) {
                LogUtil.d(false, " updateDurationByNwId, no saved app ", new Object[0]);
            } else {
                if (newId != null && newNwName != null) {
                    if (newNwFreq != null) {
                        if (this.freqLoc.equals(Constant.NAME_FREQLOCATION_OTHER)) {
                            newId2 = newNwFreq;
                        } else {
                            newId2 = newId;
                        }
                        SpaceExpInfo spaceInfo = null;
                        try {
                            if (this.mSpaceExperience.containsKey(newId2)) {
                                spaceInfo = this.mSpaceExperience.get(newId2);
                                LogUtil.i(false, " get old records:%{public}s", spaceInfo.toString());
                            }
                            if (this.mSpaceSessionDuration.containsKey(newId2)) {
                                sessionSpaceInfo = this.mSpaceSessionDuration.get(newId2);
                            } else {
                                sessionSpaceInfo = null;
                            }
                            if (spaceInfo == null) {
                                spaceInfo = new SpaceExpInfo(this.mSpaceId, this.mSpaceIdMainAp, newId2, newNwName, newNwFreq, nwType);
                            }
                            if (sessionSpaceInfo == null) {
                                sessionSpaceInfo = new SpaceExpInfo(this.mSpaceId, this.mSpaceIdMainAp, newId2, newNwName, newNwFreq, nwType);
                            }
                            long duration = System.currentTimeMillis() - this.saveAppInfo.get(app).getStartTime();
                            if (duration > 0) {
                                spaceInfo.accDuration(app, duration);
                                sessionSpaceInfo.accDuration(app, duration);
                                LogUtil.i(false, " session duration: +%{public}s", String.valueOf(duration));
                            }
                            if (Constant.USERDB_APP_NAME_WIFI.equals(app) || Constant.USERDB_APP_NAME_MOBILE.equals(app)) {
                                long[] dataTraffic = NetUtil.getTraffic(this.saveAppInfo.get(app).getStartTime(), System.currentTimeMillis(), nwType, this.mContext);
                                spaceInfo.accDataTraffic(dataTraffic[0], dataTraffic[1]);
                            }
                            try {
                                spaceInfo.accSignalValue(signal);
                                this.mSpaceExperience.put(newId2, spaceInfo);
                                this.mSpaceSessionDuration.put(newId2, sessionSpaceInfo);
                                LogUtil.i(false, " updateDurationByNwId record size: %{public}d, NW: %{public}s, spaceInfo:%{public}s", Integer.valueOf(this.mSpaceExperience.size()), newNwName, spaceInfo.toString());
                                this.saveAppInfo.get(app).setStartTime(System.currentTimeMillis());
                                return;
                            } catch (Exception e2) {
                            }
                        } catch (Exception e3) {
                            LogUtil.e(false, "updateDurationByNwId failed by Exception", new Object[0]);
                        }
                    }
                }
                try {
                    LogUtil.d(false, " updateDurationByNwId, network==null ", new Object[0]);
                } catch (Exception e4) {
                    LogUtil.e(false, "updateDurationByNwId failed by Exception", new Object[0]);
                }
            }
        } catch (Exception e5) {
            LogUtil.e(false, "updateDurationByNwId failed by Exception", new Object[0]);
        }
    }

    private void saveSpaceExptoDatabaseNew(RecognizeResult spaceIds) {
        try {
            if (this.mSpaceExperience.size() != 0) {
                LogUtil.i(false, " loadSpaceExpfromDatabase", new Object[0]);
                int num = 0;
                for (Map.Entry<String, SpaceExpInfo> entry : mergeSumOfMaps(this.mSpaceExperience, this.mSpaceUserDao.findAllByTwoSpaces(spaceIds.getRgResult(), spaceIds.getMainApRgResult())).entrySet()) {
                    SpaceExpInfo val = entry.getValue();
                    if (val != null) {
                        LogUtil.d(false, " saveSpaceExptoDatabaseNew:", new Object[0]);
                        LogUtil.i(false, "                            Records %{public}d: %{public}s", Integer.valueOf(num), val.toString());
                        this.mSpaceUserDao.insertBase(val);
                        this.mSpaceUserDao.insertApp(val);
                        num++;
                    }
                }
                return;
            }
            LogUtil.d(false, " mSpaceExperience size=0", new Object[0]);
        } catch (Exception e) {
            LogUtil.e(false, "saveSpaceExptoDatabaseNew failed by Exception", new Object[0]);
        }
    }

    private HashMap<String, SpaceExpInfo> mergeSumOfMaps(HashMap<String, SpaceExpInfo>... maps) {
        SpaceExpInfo value;
        HashMap<String, SpaceExpInfo> resultMap = new HashMap<>(16);
        for (HashMap<String, SpaceExpInfo> map : maps) {
            for (Map.Entry<String, SpaceExpInfo> entry : map.entrySet()) {
                String key = entry.getKey();
                LogUtil.v(false, " mergeSumOfMaps: nwId=%{public}s", key);
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
        LogUtil.i(false, " assignSpaceExp2Space ", new Object[0]);
        if (spaceIds == null) {
            try {
                LogUtil.i(false, " no spaceIds", new Object[0]);
            } catch (Exception e) {
                LogUtil.e(false, "assignSpaceExp2Space failed by Exception", new Object[0]);
            }
        } else {
            int spaceIdOfAllAp = spaceIds.getRgResult();
            int spaceIdOfMainAp = spaceIds.getMainApRgResult();
            LogUtil.d(false, " assignSpaceExp2Space: changed=%{public}s, spaceIdOfAllAp=%{public}d, spaceIdOfMainAp=%{public}d, model ver=%{public}d_%{public}d", String.valueOf(allApSpaceChanged), Integer.valueOf(spaceIdOfAllAp), Integer.valueOf(spaceIdOfMainAp), Integer.valueOf(spaceIds.getAllApModelName()), Integer.valueOf(spaceIds.getMainApModelName()));
            updateWifiDurationForAp(false);
            updateMobileDurationForCell(false);
            if (allApSpaceChanged) {
                updateUserPrefChr();
                updateUserPreference();
                clearUserPreference();
                clearUserPrefChr();
                this.mSpaceSessionDuration.clear();
            }
            setSpaceId(spaceIdOfAllAp, spaceIdOfMainAp);
            saveSpaceExptoDatabaseNew(spaceIds);
            this.mSpaceExperience.clear();
            setSpaceId(0, 0);
        }
    }

    private void setSpaceId(int spaceIdOfAll, int spaceIdOfMain) {
        this.mSpaceId.setLength(0);
        this.mSpaceId.trimToSize();
        this.mSpaceId.append(spaceIdOfAll);
        this.mSpaceIdMainAp.setLength(0);
        this.mSpaceIdMainAp.trimToSize();
        this.mSpaceIdMainAp.append(spaceIdOfMain);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v3, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v4, types: [boolean] */
    /* JADX WARN: Type inference failed for: r3v6 */
    /* JADX WARN: Type inference failed for: r3v8 */
    /* JADX WARN: Type inference failed for: r3v13 */
    /* JADX WARN: Type inference failed for: r3v14 */
    /* JADX WARN: Type inference failed for: r3v15 */
    /* JADX WARN: Type inference failed for: r3v19 */
    /* JADX WARN: Type inference failed for: r3v21 */
    /* JADX WARN: Type inference failed for: r3v22 */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00b7: APUT  
      (r8v8 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x00b0: INVOKE  (r15v2 java.lang.Integer) = (r23v0 'spaceIdOfAllAp' int A[D('spaceIdOfAllAp' int)]) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00d0: APUT  
      (r6v10 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x00cb: INVOKE  (r8v6 java.lang.Integer) = (r23v0 'spaceIdOfAllAp' int A[D('spaceIdOfAllAp' int)]) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0068, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00d9, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00da, code lost:
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x00f3, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x00f4, code lost:
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00f6, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00f7, code lost:
        r3 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0101, code lost:
        r3 = 0;
        com.android.server.hidata.wavemapping.util.LogUtil.e(false, "getUserPrefNetwork failed by Exception", new java.lang.Object[0]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x010a, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x010b, code lost:
        r3 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x010c, code lost:
        r3 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0057, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0058, code lost:
        r3 = false;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:21:0x007c, B:27:0x008d] */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:21:0x007c, B:36:0x00b7] */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:21:0x007c, B:41:0x00d0] */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:3:0x004a, B:10:0x005b] */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:3:0x004a, B:14:0x0062] */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0085 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:21:0x007c] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x00fe A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:3:0x004a] */
    /* JADX WARNING: Unknown variable types count: 1 */
    public Bundle getUserPrefNetwork(int spaceIdOfAllAp) {
        ?? r3;
        RuntimeException e;
        LogUtil.i(false, " getUserPrefNetwork, allAp Space ", new Object[0]);
        Bundle output = new Bundle();
        output.putBoolean(KEY_USER_HAS_PREFERENCE, false);
        output.putInt(KEY_PREFER_NETWORK_TYPE, 8);
        output.putString(KEY_PREFER_NETWORK_NAME, "UNKNOWN");
        LogUtil.d(false, " getUserPrefNetwork: spaceIdOfAllAp=%{public}d", Integer.valueOf(spaceIdOfAllAp));
        if (spaceIdOfAllAp == 0) {
            try {
                LogUtil.d(false, " spaceIdOfAllAp is unknown", new Object[0]);
                this.mLocationDao.accChrUserPrefUnknownSpaceByFreqLoc(this.freqLoc);
                return output;
            } catch (RuntimeException e2) {
                e = e2;
                r3 = 0;
                Object[] objArr = new Object[1];
                objArr[r3] = e.getMessage();
                LogUtil.e(r3, "getUserPrefNetwork RuntimeException,e:%{public}s", objArr);
                int i = r3;
                LogUtil.d(i, " User has no preference", new Object[i]);
                return output;
            } catch (Exception e3) {
            }
        } else if (this.isUserOperated) {
            boolean z = false;
            LogUtil.d(false, " isUserOperated = true", new Object[0]);
            return output;
        } else {
            Bundle calResult = calculateUserPreference(this.mSpaceUserDao.findUserPrefByAllApSpaces(spaceIdOfAllAp), true);
            if (calResult == null) {
                r3 = 0;
                try {
                    LogUtil.e(false, "calResult is null", new Object[0]);
                    return output;
                } catch (RuntimeException e4) {
                    e = e4;
                } catch (Exception e5) {
                }
            } else {
                if (calResult.getBoolean(KEY_USER_HAS_PREFERENCE, false)) {
                    String preferNet = calResult.getString(KEY_PREFER_NETWORK_NAME, "UNKNOWN");
                    int preferType = calResult.getInt(KEY_PREFER_NETWORK_TYPE, 8);
                    if (!"UNKNOWN".equals(preferNet)) {
                        if (preferType != 8) {
                            Object[] objArr2 = new Object[2];
                            objArr2[0] = Integer.valueOf(spaceIdOfAllAp);
                            objArr2[1] = preferNet;
                            LogUtil.i(false, "getUserPrefNetwork, found preferred network: space=%{public}d, name=%{public}s", objArr2);
                            this.mSpaceUserDao.setUserPrefEnteryFlag(preferNet, preferType, spaceIdOfAllAp, 1);
                        }
                    }
                    Object[] objArr3 = new Object[2];
                    objArr3[0] = Integer.valueOf(spaceIdOfAllAp);
                    objArr3[1] = preferNet;
                    LogUtil.i(false, "getUserPrefNetwork, found preferred CELL network: space=%{public}d, name=%{public}s", objArr3);
                } else {
                    LogUtil.i(false, "getUserPrefNetwork, preferred network NOT found: space=%{public}d", Integer.valueOf(spaceIdOfAllAp));
                    this.mSpaceUserDao.clearUserPrefEnteryFlag(spaceIdOfAllAp);
                }
                return calResult;
            }
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r2v1, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r2v3 */
    /* JADX WARN: Type inference failed for: r2v34 */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00ae: APUT  
      (r0v36 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (r5v12 'preferNet' java.lang.String A[D('preferNet' java.lang.String)])
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x00f3: APUT  
      (r12v6 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (r14v1 'preferNet' java.lang.String A[D('preferNet' java.lang.String)])
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x011c: APUT  
      (r2v20 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (r14v1 'preferNet' java.lang.String A[D('preferNet' java.lang.String)])
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0170: APUT  
      (r2v6 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x016c: INVOKE  (r4v6 java.lang.String) = 
      (wrap: boolean : 0x0168: INVOKE  (r4v5 boolean) = 
      (r12v2 'calResult' android.os.Bundle A[D('calResult' android.os.Bundle)])
      (wrap: java.lang.String : ?: SGET   com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.KEY_USER_HAS_PREFERENCE java.lang.String)
      false
     type: VIRTUAL call: android.os.Bundle.getBoolean(java.lang.String, boolean):boolean)
     type: STATIC call: java.lang.String.valueOf(boolean):java.lang.String)
     */
    /* JADX WARNING: Code restructure failed: missing block: B:60:0x0130, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0131, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0155, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:69:0x0156, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:70:0x0158, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:0x0159, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0179, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:79:0x017a, code lost:
        r2 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x017f, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x0180, code lost:
        r2 = false;
     */
    /* JADX WARNING: Exception block dominator not found, dom blocks: [B:26:0x0083, B:56:0x011c] */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0089 A[Catch:{ RuntimeException -> 0x008c, Exception -> 0x0089 }, ExcHandler: Exception (e java.lang.Exception), Splitter:B:26:0x0083] */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x017c A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:12:0x0050] */
    public Bundle getUserPrefNetworkByMainAp(int spaceIdOfMainAp) {
        int i;
        RuntimeException e;
        ArrayList<Integer> spaceAllList;
        String str;
        char c;
        boolean z = false;
        boolean z2 = false;
        LogUtil.i(false, " getUserPrefNetworkByMainAp ", new Object[0]);
        Bundle output = new Bundle();
        new Bundle();
        output.putBoolean(KEY_USER_HAS_PREFERENCE, false);
        String str2 = KEY_PREFER_NETWORK_TYPE;
        output.putInt(str2, 8);
        output.putString(KEY_PREFER_NETWORK_NAME, "UNKNOWN");
        int i2 = 1;
        try {
            if (this.isUserOperated) {
                LogUtil.d(false, " isUserOperated = true", new Object[0]);
                return output;
            }
            try {
                ArrayList<Integer> spaceAllList2 = this.mSpaceUserDao.findAllApSpaceIdByMainApSpace(spaceIdOfMainAp, 1);
                String preferNet = null;
                int listnum = spaceAllList2.size();
                int preferType = 8;
                Bundle calResult = output;
                int n = 0;
                while (n < listnum) {
                    try {
                        int spaceIdOfAllAp = spaceAllList2.get(n).intValue();
                        Object[] objArr = new Object[i2];
                        objArr[0] = Integer.valueOf(spaceIdOfAllAp);
                        LogUtil.i(false, " loopID:%{public}d", objArr);
                        if (spaceIdOfAllAp == 0) {
                            spaceAllList = spaceAllList2;
                            str = str2;
                            c = '\b';
                        } else {
                            Bundle calResult2 = calculateUserPreference(this.mSpaceUserDao.findUserPrefByAllApSpaces(spaceIdOfAllAp), false);
                            if (calResult2 == null) {
                                try {
                                    LogUtil.e(false, "calResult is null", new Object[0]);
                                    return output;
                                } catch (RuntimeException e2) {
                                    e = e2;
                                    z = false;
                                    Object[] objArr2 = new Object[1];
                                    objArr2[z ? 1 : 0] = e.getMessage();
                                    LogUtil.e(z, "getUserPrefNetworkByMainAp2 RuntimeException,e:%{public}s", objArr2);
                                    i = z;
                                    LogUtil.d(i, " User has no preference", new Object[i]);
                                    return output;
                                } catch (Exception e3) {
                                }
                            } else if (!calResult2.getBoolean(KEY_USER_HAS_PREFERENCE, false)) {
                                LogUtil.i(false, "getUserPrefNetworkByMainAp, NOT found preferred network: id=%{public}d", Integer.valueOf(spaceIdOfAllAp));
                                this.mSpaceUserDao.clearUserPrefEnteryFlag(spaceIdOfAllAp);
                                return output;
                            } else if (preferNet == null) {
                                try {
                                    String preferNet2 = calResult2.getString(KEY_PREFER_NETWORK_NAME, "UNKNOWN");
                                    int preferType2 = calResult2.getInt(str2, 8);
                                    spaceAllList = spaceAllList2;
                                    Object[] objArr3 = new Object[2];
                                    z = false;
                                    try {
                                        objArr3[0] = preferNet2;
                                        objArr3[1] = Integer.valueOf(spaceIdOfAllAp);
                                        LogUtil.i(false, "getUserPrefNetworkByMainAp, found preferred network:%{public}s, id=%{public}d", objArr3);
                                        this.mSpaceUserDao.setUserPrefEnteryFlag(preferNet2, preferType2, spaceIdOfAllAp, 1);
                                        preferNet = preferNet2;
                                        str = str2;
                                        preferType = preferType2;
                                        c = '\b';
                                        calResult = calResult2;
                                    } catch (RuntimeException e4) {
                                        e = e4;
                                        Object[] objArr22 = new Object[1];
                                        objArr22[z ? 1 : 0] = e.getMessage();
                                        LogUtil.e(z, "getUserPrefNetworkByMainAp2 RuntimeException,e:%{public}s", objArr22);
                                        i = z;
                                        LogUtil.d(i, " User has no preference", new Object[i]);
                                        return output;
                                    } catch (Exception e32) {
                                    }
                                } catch (RuntimeException e5) {
                                    e = e5;
                                    z = false;
                                    Object[] objArr222 = new Object[1];
                                    objArr222[z ? 1 : 0] = e.getMessage();
                                    LogUtil.e(z, "getUserPrefNetworkByMainAp2 RuntimeException,e:%{public}s", objArr222);
                                    i = z;
                                    LogUtil.d(i, " User has no preference", new Object[i]);
                                    return output;
                                } catch (Exception e322) {
                                }
                            } else {
                                spaceAllList = spaceAllList2;
                                if (preferNet.equals(calResult2.getString(KEY_PREFER_NETWORK_NAME, "UNKNOWN"))) {
                                    c = '\b';
                                    if (preferType == calResult2.getInt(str2, 8) && !"UNKNOWN".equals(preferNet)) {
                                        Object[] objArr4 = new Object[2];
                                        str = str2;
                                        try {
                                            objArr4[0] = preferNet;
                                            objArr4[1] = Integer.valueOf(spaceIdOfAllAp);
                                            LogUtil.i(false, " found the same network:%{public}s, id=%{public}d", objArr4);
                                            this.mSpaceUserDao.setUserPrefEnteryFlag(preferNet, preferType, spaceIdOfAllAp, 1);
                                            preferType = preferType;
                                            calResult = calResult2;
                                        } catch (RuntimeException e6) {
                                            e = e6;
                                            z = false;
                                            Object[] objArr2222 = new Object[1];
                                            objArr2222[z ? 1 : 0] = e.getMessage();
                                            LogUtil.e(z, "getUserPrefNetworkByMainAp2 RuntimeException,e:%{public}s", objArr2222);
                                            i = z;
                                            LogUtil.d(i, " User has no preference", new Object[i]);
                                            return output;
                                        } catch (Exception e3222) {
                                        }
                                    }
                                }
                                Object[] objArr5 = new Object[2];
                                objArr5[0] = preferNet;
                                objArr5[1] = Integer.valueOf(spaceIdOfAllAp);
                                LogUtil.d(false, "getUserPrefNetworkByMainAp, not the same network:%{public}s, id=%{public}d", objArr5);
                                this.mMachineHandler.sendEmptyMessage(116);
                                return output;
                            }
                        }
                        n++;
                        str2 = str;
                        spaceAllList2 = spaceAllList;
                        z2 = false;
                        i2 = 1;
                    } catch (RuntimeException e7) {
                        e = e7;
                        boolean z3 = z2;
                        z = z3;
                        Object[] objArr22222 = new Object[1];
                        objArr22222[z ? 1 : 0] = e.getMessage();
                        LogUtil.e(z, "getUserPrefNetworkByMainAp2 RuntimeException,e:%{public}s", objArr22222);
                        i = z;
                        LogUtil.d(i, " User has no preference", new Object[i]);
                        return output;
                    } catch (Exception e8) {
                    }
                }
                Object[] objArr6 = new Object[2];
                objArr6[0] = String.valueOf(calResult.getBoolean(KEY_USER_HAS_PREFERENCE, false));
                objArr6[1] = preferNet;
                LogUtil.d(false, "getUserPrefNetworkByMainAp, results:%{public}s, net=%{public}s", objArr6);
                return calResult;
            } catch (RuntimeException e9) {
                e = e9;
                Object[] objArr222222 = new Object[1];
                objArr222222[z ? 1 : 0] = e.getMessage();
                LogUtil.e(z, "getUserPrefNetworkByMainAp2 RuntimeException,e:%{public}s", objArr222222);
                i = z;
                LogUtil.d(i, " User has no preference", new Object[i]);
                return output;
            } catch (Exception e10) {
                i = 0;
                LogUtil.e(false, "getUserPrefNetworkByMainAp failed by Exception", new Object[0]);
                LogUtil.d(i, " User has no preference", new Object[i]);
                return output;
            }
        } catch (RuntimeException e11) {
            e = e11;
            Object[] objArr2222222 = new Object[1];
            objArr2222222[z ? 1 : 0] = e.getMessage();
            LogUtil.e(z, "getUserPrefNetworkByMainAp2 RuntimeException,e:%{public}s", objArr2222222);
            i = z;
            LogUtil.d(i, " User has no preference", new Object[i]);
            return output;
        } catch (Exception e12) {
            i = 0;
            LogUtil.e(false, "getUserPrefNetworkByMainAp failed by Exception", new Object[0]);
            LogUtil.d(i, " User has no preference", new Object[i]);
            return output;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v1, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v2, types: [boolean] */
    /* JADX WARN: Type inference failed for: r4v3 */
    /* JADX WARN: Type inference failed for: r4v8 */
    /* JADX WARN: Type inference failed for: r4v11 */
    /* JADX WARN: Type inference failed for: r4v19 */
    /* JADX WARN: Type inference failed for: r4v22 */
    /* JADX WARN: Type inference failed for: r4v28 */
    /* JADX WARN: Type inference failed for: r4v31 */
    /* JADX WARN: Type inference failed for: r4v36 */
    /* JADX WARN: Type inference failed for: r4v37 */
    /* JADX WARN: Type inference failed for: r4v38 */
    /* JADX WARN: Type inference failed for: r4v39 */
    /* JADX WARN: Type inference failed for: r4v41 */
    /* JADX WARN: Type inference failed for: r4v44 */
    /* JADX WARN: Type inference failed for: r4v50 */
    /* JADX WARN: Type inference failed for: r4v54 */
    /* JADX WARN: Type inference failed for: r4v55 */
    /* JADX WARN: Type inference failed for: r4v56 */
    /* JADX WARN: Type inference failed for: r4v57 */
    /* JADX WARN: Type inference failed for: r4v58 */
    /* JADX WARN: Type inference failed for: r4v59 */
    /* JADX WARN: Type inference failed for: r4v60 */
    /* JADX WARN: Type inference failed for: r4v61 */
    /* JADX WARN: Type inference failed for: r4v62 */
    /* JADX WARN: Type inference failed for: r4v63 */
    /* JADX WARN: Type inference failed for: r4v64 */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x01ae: APUT  
      (r13v15 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x01a7: INVOKE  (r6v24 java.lang.String) = (r17v2 'durationThreshold' double A[D('durationThreshold' double)]) type: STATIC call: java.lang.String.valueOf(double):java.lang.String)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0296: APUT  
      (r7v15 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.String : 0x028f: INVOKE  (r0v14 java.lang.String) = (r13v3 'totalDuration' long A[D('totalDuration' long)]) type: STATIC call: java.lang.String.valueOf(long):java.lang.String)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x02e1: APUT  
      (r5v16 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x02dc: INVOKE  (r4v43 java.lang.Integer) = (r2v34 'otherNetworkCount' int A[D('otherNetworkCount' int)]) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0330: APUT  
      (r5v21 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x032b: INVOKE  (r0v46 java.lang.Integer) = 
      (wrap: int : 0x0327: INVOKE  (r0v45 int) = 
      (r6v8 'maxEntry' android.os.Bundle A[D('maxEntry' android.os.Bundle)])
      (wrap: java.lang.String : ?: SGET   com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.KEY_NETWORK_TYPE java.lang.String)
     type: VIRTUAL call: android.os.Bundle.getInt(java.lang.String):int)
     type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x03d4: APUT  
      (r5v6 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x03cf: INVOKE  (r11v4 java.lang.Integer) = (r2v16 'otherNetworkCount' int A[D('otherNetworkCount' int)]) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    /* JADX WARNING: Code restructure failed: missing block: B:117:0x033d, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:118:0x033e, code lost:
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:125:0x0363, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:126:0x0364, code lost:
        r3 = com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.KEY_USER_HAS_PREFERENCE;
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:128:0x036e, code lost:
        r3 = com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.KEY_USER_HAS_PREFERENCE;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:169:0x0419, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:170:0x041a, code lost:
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:173:0x042a, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:174:0x042b, code lost:
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:176:0x0436, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:177:0x0437, code lost:
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:206:0x04cf, code lost:
        r3 = com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.KEY_USER_HAS_PREFERENCE;
        r7 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:207:0x04e7, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:208:0x04e8, code lost:
        r3 = com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.KEY_USER_HAS_PREFERENCE;
        r4 = false;
        r21 = 0;
        r7 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x011a, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x011b, code lost:
        r3 = com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.KEY_USER_HAS_PREFERENCE;
        r7 = r5;
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x012c, code lost:
        r3 = com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.KEY_USER_HAS_PREFERENCE;
        r7 = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:64:0x01f7, code lost:
        r0 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x01f8, code lost:
        r3 = com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.KEY_USER_HAS_PREFERENCE;
        r7 = r5;
        r4 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x0217, code lost:
        r3 = com.android.server.hidata.wavemapping.statehandler.CollectUserFingersHandler.KEY_USER_HAS_PREFERENCE;
        r7 = r5;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:119:0x0345 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:111:0x030b] */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x036d A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:100:0x02cc] */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x03b1 A[SYNTHETIC, Splitter:B:140:0x03b1] */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x0422 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:156:0x03f0] */
    /* JADX WARNING: Removed duplicated region for block: B:178:0x0440 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:147:0x03cd] */
    /* JADX WARNING: Removed duplicated region for block: B:205:0x04ce A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:1:0x0022] */
    /* JADX WARNING: Removed duplicated region for block: B:214:0x051f  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x012b A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:23:0x00b5] */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x0216 A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:11:0x005b] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private Bundle calculateUserPreference(HashMap<String, Bundle> currentSpaceExp, boolean needRecord) {
        Bundle result;
        int i;
        String str;
        ?? r4;
        RuntimeException e;
        long cellDuration;
        String str2;
        int cellUserPrefOptOut;
        int cellUserPrefStay;
        int cellUserPrefOptIn;
        long totalDuration;
        Bundle maxEntry;
        int inoutCount;
        int totalCount;
        int i2;
        int i3;
        String str3;
        String str4;
        String str5;
        long cellDuration2;
        int cellUserPrefOptIn2;
        String str6 = KEY_PREFER_NETWORK_TYPE;
        Bundle result2 = new Bundle();
        double durationThreshold = this.param.getUserPrefDurationRatio();
        try {
            result2.putBoolean(KEY_USER_HAS_PREFERENCE, false);
            result2.putInt(str6, 8);
            result2.putString(KEY_PREFER_NETWORK_NAME, "UNKNOWN");
            long totalDuration2 = 0;
            int inoutCount2 = 0;
            int stayCount = 0;
            long cellDuration3 = 0;
            if (currentSpaceExp.size() != 0) {
                try {
                    Iterator<Map.Entry<String, Bundle>> it = currentSpaceExp.entrySet().iterator();
                    int cellUserPrefOptOut2 = 0;
                    Bundle maxEntry2 = null;
                    cellUserPrefStay = 0;
                    cellUserPrefOptIn = 0;
                    while (it.hasNext()) {
                        try {
                            Bundle value = it.next().getValue();
                            if (value == null) {
                                it = it;
                            } else {
                                totalDuration2 += value.getLong(KEY_DURATION_CONNECTED);
                                inoutCount2 = inoutCount2 + value.getInt(KEY_USER_PREFERENCE_OPT_IN) + value.getInt(KEY_USER_PREFERENCE_OPT_OUT);
                                stayCount += value.getInt(KEY_USER_PREFERENCE_STAY);
                                if (value.getInt(KEY_NETWORK_TYPE) == 0) {
                                    cellDuration3 += value.getLong(KEY_DURATION_CONNECTED);
                                    cellUserPrefOptOut2 += value.getInt(KEY_USER_PREFERENCE_OPT_OUT);
                                    cellUserPrefOptIn += value.getInt(KEY_USER_PREFERENCE_OPT_IN);
                                    cellUserPrefStay += value.getInt(KEY_USER_PREFERENCE_STAY);
                                }
                                try {
                                    LogUtil.d(false, " Entry: NetworkName:%{public}s Type:%{public}d IN:%{public}d OUT:%{public}d STAY:%{public}d duration_connected:%{public}s", value.getString(KEY_NETWORK_NAME), Integer.valueOf(value.getInt(KEY_NETWORK_TYPE)), Integer.valueOf(value.getInt(KEY_USER_PREFERENCE_OPT_IN)), Integer.valueOf(value.getInt(KEY_USER_PREFERENCE_OPT_OUT)), Integer.valueOf(value.getInt(KEY_USER_PREFERENCE_STAY)), String.valueOf(value.getLong(KEY_DURATION_CONNECTED)));
                                    if (maxEntry2 == null || maxEntry2.getLong(KEY_DURATION_CONNECTED) < value.getLong(KEY_DURATION_CONNECTED)) {
                                        maxEntry2 = value;
                                    }
                                    it = it;
                                    cellUserPrefOptOut2 = cellUserPrefOptOut2;
                                    cellUserPrefOptIn = cellUserPrefOptIn;
                                    str6 = str6;
                                } catch (RuntimeException e2) {
                                    e = e2;
                                    str = KEY_USER_HAS_PREFERENCE;
                                    result = result2;
                                    r4 = 0;
                                    Object[] objArr = new Object[1];
                                    objArr[r4] = e.getMessage();
                                    LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr);
                                    i = r4;
                                    if (!result.getBoolean(str, i)) {
                                    }
                                    return result;
                                } catch (Exception e3) {
                                }
                            }
                        } catch (RuntimeException e4) {
                            e = e4;
                            str = KEY_USER_HAS_PREFERENCE;
                            result = result2;
                            r4 = 0;
                            Object[] objArr2 = new Object[1];
                            objArr2[r4] = e.getMessage();
                            LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr2);
                            i = r4;
                            if (!result.getBoolean(str, i)) {
                            }
                            return result;
                        } catch (Exception e5) {
                            str = KEY_USER_HAS_PREFERENCE;
                            result = result2;
                            i = 0;
                            LogUtil.e(false, "calculateUserPreference failed by Exception", new Object[0]);
                            if (!result.getBoolean(str, i)) {
                            }
                            return result;
                        }
                        try {
                        } catch (RuntimeException e6) {
                            e = e6;
                            str = KEY_USER_HAS_PREFERENCE;
                            result = result2;
                            r4 = 0;
                            Object[] objArr22 = new Object[1];
                            objArr22[r4] = e.getMessage();
                            LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr22);
                            i = r4;
                            if (!result.getBoolean(str, i)) {
                            }
                            return result;
                        } catch (Exception e7) {
                        }
                    }
                    str2 = str6;
                    if (maxEntry2 != null) {
                        if (maxEntry2.getInt(KEY_USER_PREFERENCE_ENTRY, 0) > 0) {
                            durationThreshold = this.param.getUserPrefDurationRatioLeave();
                            try {
                                Object[] objArr3 = new Object[1];
                                cellUserPrefOptOut = cellUserPrefOptOut2;
                                try {
                                    objArr3[0] = String.valueOf(durationThreshold);
                                    LogUtil.d(false, " durationThreshold = %{public}s", objArr3);
                                    totalDuration = totalDuration2;
                                    inoutCount = inoutCount2;
                                    cellDuration = cellDuration3;
                                    maxEntry = maxEntry2;
                                } catch (RuntimeException e8) {
                                    e = e8;
                                    str = KEY_USER_HAS_PREFERENCE;
                                    result = result2;
                                    r4 = 0;
                                    Object[] objArr222 = new Object[1];
                                    objArr222[r4] = e.getMessage();
                                    LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr222);
                                    i = r4;
                                    if (!result.getBoolean(str, i)) {
                                    }
                                    return result;
                                } catch (Exception e9) {
                                    str = KEY_USER_HAS_PREFERENCE;
                                    result = result2;
                                    i = 0;
                                    LogUtil.e(false, "calculateUserPreference failed by Exception", new Object[0]);
                                    if (!result.getBoolean(str, i)) {
                                    }
                                    return result;
                                }
                            } catch (RuntimeException e10) {
                                e = e10;
                                str = KEY_USER_HAS_PREFERENCE;
                                result = result2;
                                r4 = 0;
                                Object[] objArr2222 = new Object[1];
                                objArr2222[r4] = e.getMessage();
                                LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr2222);
                                i = r4;
                                if (!result.getBoolean(str, i)) {
                                }
                                return result;
                            } catch (Exception e11) {
                                str = KEY_USER_HAS_PREFERENCE;
                                result = result2;
                                i = 0;
                                LogUtil.e(false, "calculateUserPreference failed by Exception", new Object[0]);
                                if (!result.getBoolean(str, i)) {
                                }
                                return result;
                            }
                        }
                    }
                    cellUserPrefOptOut = cellUserPrefOptOut2;
                    totalDuration = totalDuration2;
                    inoutCount = inoutCount2;
                    cellDuration = cellDuration3;
                    maxEntry = maxEntry2;
                } catch (RuntimeException e12) {
                    e = e12;
                    str = KEY_USER_HAS_PREFERENCE;
                    result = result2;
                    r4 = 0;
                    Object[] objArr22222 = new Object[1];
                    objArr22222[r4] = e.getMessage();
                    LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr22222);
                    i = r4;
                    if (!result.getBoolean(str, i)) {
                    }
                    return result;
                } catch (Exception e13) {
                    str = KEY_USER_HAS_PREFERENCE;
                    result = result2;
                    i = 0;
                    LogUtil.e(false, "calculateUserPreference failed by Exception", new Object[0]);
                    if (!result.getBoolean(str, i)) {
                    }
                    return result;
                }
            } else {
                str2 = str6;
                if (needRecord) {
                    this.mLocationDao.accChrUserPrefUnknownDbByFreqLoc(this.freqLoc);
                }
                cellUserPrefOptOut = 0;
                maxEntry = null;
                inoutCount = 0;
                cellDuration = 0;
                cellUserPrefStay = 0;
                cellUserPrefOptIn = 0;
                totalDuration = 0;
            }
            try {
                totalCount = Math.round(((float) inoutCount) / 2.0f) + stayCount;
            } catch (RuntimeException e14) {
                e = e14;
                str = KEY_USER_HAS_PREFERENCE;
                result = result2;
                r4 = 0;
                Object[] objArr222222 = new Object[1];
                objArr222222[r4] = e.getMessage();
                LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr222222);
                i = r4;
                if (!result.getBoolean(str, i)) {
                }
                return result;
            } catch (Exception e15) {
                str = KEY_USER_HAS_PREFERENCE;
                result = result2;
                i = 0;
                LogUtil.e(false, "calculateUserPreference failed by Exception", new Object[0]);
                if (!result.getBoolean(str, i)) {
                }
                return result;
            }
            try {
                Object[] objArr4 = new Object[2];
                try {
                    objArr4[0] = String.valueOf(totalDuration);
                    objArr4[1] = Integer.valueOf(totalCount);
                    LogUtil.d(false, "totalDuration: %{public}s totalCount:%{public}d", objArr4);
                    if (!(maxEntry == null || totalDuration == 0)) {
                        try {
                            if (totalDuration > this.param.getUserPrefStartDuration()) {
                                if (!maxEntry.getString(KEY_NETWORK_NAME, "UNKNOWN").equalsIgnoreCase("UNKNOWN")) {
                                    str5 = "UNKNOWN";
                                    result = result2;
                                    if (((double) maxEntry.getLong(KEY_DURATION_CONNECTED)) / ((double) totalDuration) >= durationThreshold) {
                                        try {
                                            int otherNetworkCount = (totalCount - maxEntry.getInt(KEY_USER_PREFERENCE_OPT_OUT)) - maxEntry.getInt(KEY_USER_PREFERENCE_STAY);
                                            Object[] objArr5 = new Object[2];
                                            objArr5[0] = Integer.valueOf(otherNetworkCount);
                                            objArr5[1] = maxEntry.toString();
                                            LogUtil.d(false, "otherNetworkCount: %{public}d maxEntry: %{public}s", objArr5);
                                            if (totalCount <= this.param.getUserPrefStartTimes() || otherNetworkCount <= 0) {
                                                str3 = KEY_PREFER_NETWORK_NAME;
                                                str = KEY_USER_HAS_PREFERENCE;
                                                str4 = str2;
                                            } else if (((float) maxEntry.getInt(KEY_USER_PREFERENCE_OPT_IN)) / ((float) otherNetworkCount) >= this.param.getUserPrefFreqRatio()) {
                                                str = KEY_USER_HAS_PREFERENCE;
                                                try {
                                                    result.putBoolean(str, true);
                                                    result.putInt(str2, maxEntry.getInt(KEY_NETWORK_TYPE));
                                                    result.putString(KEY_PREFER_NETWORK_NAME, maxEntry.getString(KEY_NETWORK_NAME));
                                                    Object[] objArr6 = new Object[2];
                                                    objArr6[0] = Integer.valueOf(maxEntry.getInt(KEY_NETWORK_TYPE));
                                                    objArr6[1] = maxEntry.getString(KEY_NETWORK_NAME);
                                                    LogUtil.d(false, " isUserHasPref: true prefNetworkType:%{public}d prefNetworkName:%{public}s", objArr6);
                                                    return result;
                                                } catch (RuntimeException e16) {
                                                    e = e16;
                                                    r4 = 0;
                                                    Object[] objArr2222222 = new Object[1];
                                                    objArr2222222[r4] = e.getMessage();
                                                    LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr2222222);
                                                    i = r4;
                                                    if (!result.getBoolean(str, i)) {
                                                    }
                                                    return result;
                                                } catch (Exception e17) {
                                                }
                                            } else {
                                                str3 = KEY_PREFER_NETWORK_NAME;
                                                str = KEY_USER_HAS_PREFERENCE;
                                                str4 = str2;
                                            }
                                        } catch (RuntimeException e18) {
                                            e = e18;
                                            str = KEY_USER_HAS_PREFERENCE;
                                            r4 = 0;
                                            Object[] objArr22222222 = new Object[1];
                                            objArr22222222[r4] = e.getMessage();
                                            LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr22222222);
                                            i = r4;
                                            if (!result.getBoolean(str, i)) {
                                            }
                                            return result;
                                        } catch (Exception e19) {
                                        }
                                    } else {
                                        str3 = KEY_PREFER_NETWORK_NAME;
                                        str = KEY_USER_HAS_PREFERENCE;
                                        str4 = str2;
                                    }
                                } else {
                                    str5 = "UNKNOWN";
                                    result = result2;
                                    str3 = KEY_PREFER_NETWORK_NAME;
                                    str = KEY_USER_HAS_PREFERENCE;
                                    str4 = str2;
                                }
                                if (totalDuration != 0) {
                                    try {
                                        if (totalDuration > this.param.getUserPrefStartDuration()) {
                                            cellDuration2 = cellDuration;
                                            if (((double) cellDuration2) / ((double) totalDuration) >= durationThreshold) {
                                                int otherNetworkCount2 = (totalCount - cellUserPrefOptOut) - cellUserPrefStay;
                                                try {
                                                    Object[] objArr7 = new Object[2];
                                                    objArr7[0] = Integer.valueOf(otherNetworkCount2);
                                                    objArr7[1] = String.valueOf(cellDuration2);
                                                    LogUtil.d(false, "otherNetworkCount: %{public}d cellDuration %{public}s", objArr7);
                                                    if (totalCount <= this.param.getUserPrefStartTimes() || otherNetworkCount2 <= 0) {
                                                        cellUserPrefOptIn2 = cellUserPrefOptIn;
                                                    } else {
                                                        cellUserPrefOptIn2 = cellUserPrefOptIn;
                                                        try {
                                                            if (((float) cellUserPrefOptIn2) / ((float) otherNetworkCount2) >= this.param.getUserPrefFreqRatio()) {
                                                                result.putBoolean(str, true);
                                                                result.putInt(str4, 0);
                                                                result.putString(str3, str5);
                                                                LogUtil.d(false, " User Prefer Cellular", new Object[0]);
                                                                return result;
                                                            }
                                                        } catch (RuntimeException e20) {
                                                            e = e20;
                                                            r4 = 0;
                                                            Object[] objArr222222222 = new Object[1];
                                                            objArr222222222[r4] = e.getMessage();
                                                            LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr222222222);
                                                            i = r4;
                                                            if (!result.getBoolean(str, i)) {
                                                            }
                                                            return result;
                                                        } catch (Exception e21) {
                                                        }
                                                    }
                                                } catch (RuntimeException e22) {
                                                    e = e22;
                                                    r4 = 0;
                                                    Object[] objArr2222222222 = new Object[1];
                                                    objArr2222222222[r4] = e.getMessage();
                                                    LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr2222222222);
                                                    i = r4;
                                                    if (!result.getBoolean(str, i)) {
                                                    }
                                                    return result;
                                                } catch (Exception e23) {
                                                }
                                            } else {
                                                cellUserPrefOptIn2 = cellUserPrefOptIn;
                                            }
                                            i = 0;
                                            if (!result.getBoolean(str, i)) {
                                                LogUtil.i(i, " NO user prefer", new Object[i]);
                                            }
                                            return result;
                                        }
                                    } catch (RuntimeException e24) {
                                        e = e24;
                                        r4 = 0;
                                        Object[] objArr22222222222 = new Object[1];
                                        objArr22222222222[r4] = e.getMessage();
                                        LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr22222222222);
                                        i = r4;
                                        if (!result.getBoolean(str, i)) {
                                        }
                                        return result;
                                    } catch (Exception e25) {
                                        i = 0;
                                        LogUtil.e(false, "calculateUserPreference failed by Exception", new Object[0]);
                                        if (!result.getBoolean(str, i)) {
                                        }
                                        return result;
                                    }
                                }
                                cellUserPrefOptIn2 = cellUserPrefOptIn;
                                cellDuration2 = cellDuration;
                                i = 0;
                                if (!result.getBoolean(str, i)) {
                                }
                                return result;
                            }
                        } catch (RuntimeException e26) {
                            e = e26;
                            result = result2;
                            str = KEY_USER_HAS_PREFERENCE;
                            r4 = 0;
                            Object[] objArr222222222222 = new Object[1];
                            objArr222222222222[r4] = e.getMessage();
                            LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr222222222222);
                            i = r4;
                            if (!result.getBoolean(str, i)) {
                            }
                            return result;
                        } catch (Exception e27) {
                            result = result2;
                            str = KEY_USER_HAS_PREFERENCE;
                            i = 0;
                            LogUtil.e(false, "calculateUserPreference failed by Exception", new Object[0]);
                            if (!result.getBoolean(str, i)) {
                            }
                            return result;
                        }
                    }
                    str5 = "UNKNOWN";
                    result = result2;
                    str3 = KEY_PREFER_NETWORK_NAME;
                    str = KEY_USER_HAS_PREFERENCE;
                    str4 = str2;
                    if (totalDuration != 0) {
                    }
                    cellUserPrefOptIn2 = cellUserPrefOptIn;
                    cellDuration2 = cellDuration;
                    i = 0;
                } catch (RuntimeException e28) {
                    e = e28;
                    result = result2;
                    i2 = cellUserPrefOptIn;
                    str = KEY_USER_HAS_PREFERENCE;
                    r4 = 0;
                    Object[] objArr2222222222222 = new Object[1];
                    objArr2222222222222[r4] = e.getMessage();
                    LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr2222222222222);
                    i = r4;
                    if (!result.getBoolean(str, i)) {
                    }
                    return result;
                } catch (Exception e29) {
                    result = result2;
                    i3 = cellUserPrefOptIn;
                    str = KEY_USER_HAS_PREFERENCE;
                    i = 0;
                    LogUtil.e(false, "calculateUserPreference failed by Exception", new Object[0]);
                    if (!result.getBoolean(str, i)) {
                    }
                    return result;
                }
            } catch (RuntimeException e30) {
                e = e30;
                str = KEY_USER_HAS_PREFERENCE;
                result = result2;
                i2 = cellUserPrefOptIn;
                r4 = 0;
                Object[] objArr22222222222222 = new Object[1];
                objArr22222222222222[r4] = e.getMessage();
                LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr22222222222222);
                i = r4;
                if (!result.getBoolean(str, i)) {
                }
                return result;
            } catch (Exception e31) {
                str = KEY_USER_HAS_PREFERENCE;
                result = result2;
                i3 = cellUserPrefOptIn;
                i = 0;
                LogUtil.e(false, "calculateUserPreference failed by Exception", new Object[0]);
                if (!result.getBoolean(str, i)) {
                }
                return result;
            }
        } catch (RuntimeException e32) {
            e = e32;
            str = KEY_USER_HAS_PREFERENCE;
            long j = 0;
            result = result2;
            boolean z = false;
            r4 = z;
            Object[] objArr222222222222222 = new Object[1];
            objArr222222222222222[r4] = e.getMessage();
            LogUtil.e(r4, "calculateUserPreference RuntimeException,e:%{public}s", objArr222222222222222);
            i = r4;
            if (!result.getBoolean(str, i)) {
            }
            return result;
        } catch (Exception e33) {
        }
        if (!result.getBoolean(str, i)) {
        }
        return result;
    }

    public void recognizeActions(RecognizeResult spaceIds, boolean isChanged) {
        LogUtil.i(false, " recognizeActions ", new Object[0]);
        if (spaceIds == null) {
            try {
                LogUtil.i(false, " no spaceIds", new Object[0]);
            } catch (Exception e) {
                LogUtil.e(false, "assignSpaceExp2Space failed by Exception", new Object[0]);
            }
        } else {
            int spaceIdOfAllAp = spaceIds.getRgResult();
            int spaceIdOfMainAp = spaceIds.getMainApRgResult();
            LogUtil.d(false, " recognizeActions: spaceIdOfAllAp=%{public}d, spaceIdOfMainAp=%{public}d", Integer.valueOf(spaceIdOfAllAp), Integer.valueOf(spaceIdOfMainAp));
            if (isChanged) {
                determineUserPreference(spaceIdOfAllAp, spaceIdOfMainAp);
            }
        }
    }

    private void determineUserPreference(int spaceIdOfAllAp, int spaceIdOfMainAp) {
        IWaveMappingCallback brainCallback;
        Bundle userPrefResult;
        LogUtil.i(false, " determineUserPreference ", new Object[0]);
        boolean isMainSpace = false;
        try {
            HwWaveMappingManager hwWaveMappingManager = HwWaveMappingManager.getInstance();
            if (hwWaveMappingManager != null && (brainCallback = hwWaveMappingManager.getWaveMappingCallback()) != null) {
                if (spaceIdOfAllAp == 0) {
                    if (spaceIdOfMainAp == 0) {
                        LogUtil.i(false, "No preference due to unknown space", new Object[0]);
                        brainCallback.onWaveMappingReportCallback(0, this.mPrefNetworkName, this.mPrefNetworkType);
                        clearSavedPreference();
                        this.mLocationDao.accChrUserPrefUnknownSpaceByFreqLoc(this.freqLoc);
                        return;
                    }
                    isMainSpace = true;
                    LogUtil.i(false, "Search preference by mainAp space", new Object[0]);
                }
                if (isMainSpace) {
                    userPrefResult = getUserPrefNetworkByMainAp(spaceIdOfMainAp);
                } else {
                    try {
                        userPrefResult = getUserPrefNetwork(spaceIdOfAllAp);
                    } catch (Exception e) {
                        LogUtil.e(false, "determineUserPreference failed by Exception", new Object[0]);
                    }
                }
                if (userPrefResult == null) {
                    LogUtil.e(false, "userPrefResult is null", new Object[0]);
                    return;
                }
                this.isUserPref = userPrefResult.getBoolean(KEY_USER_HAS_PREFERENCE);
                this.mPrefNetworkName = userPrefResult.getString(KEY_PREFER_NETWORK_NAME);
                this.mPrefNetworkType = userPrefResult.getInt(KEY_PREFER_NETWORK_TYPE);
                Bundle connectivity = NetUtil.getConnectedNetworkState(this.mContext);
                if (connectivity == null) {
                    LogUtil.e(false, "connectivity is null", new Object[0]);
                    return;
                }
                int defaultType = connectivity.getInt(KEY_DEFAULT_TYPE, 8);
                String currNwName = connectivity.getString(KEY_DEFAULT_NETWORK_NAME, "UNKNOWN");
                String currNwId = connectivity.getString(KEY_DEFAULT_NETWORK_ID, "UNKNOWN");
                String currNwFreq = connectivity.getString(KEY_DEFAULT_NETWORK_FREQUENCY, "UNKNOWN");
                if (this.isUserPref) {
                    try {
                        if (this.mPrefNetworkType == 1 && defaultType == 1) {
                            LogUtil.i(false, "%{public}s", "W2W: perfered WiFi name=" + this.mPrefNetworkName + ", current WiFi name=" + currNwName + ", space=" + spaceIdOfAllAp);
                            HashMap<Integer, String> preference = new HashMap<>(16);
                            preference.put(0, this.mPrefNetworkName);
                            this.mHistoryQoeMgr.savePreferListForWifi(preference);
                            if (!currNwName.equals(this.mPrefNetworkName)) {
                                brainCallback.onWaveMappingReportCallback(1, this.mPrefNetworkName, this.mPrefNetworkType);
                                this.mChrUserPrefSwitchTime = System.currentTimeMillis();
                                this.mChrUserPrefOriginalNwName = currNwName;
                                this.mChrUserPrefOriginalNwType = defaultType;
                                this.mChrUserPrefOriginalNwId = currNwId;
                                this.mChrUserPrefOriginalNwFreq = currNwFreq;
                                this.mChrUserPrefSwitchNwName = this.mPrefNetworkName;
                                this.mChrUserPrefSwitchNwType = this.mPrefNetworkType;
                                this.isChrUserPrefDetermineOn = true;
                            }
                            return;
                        }
                    } catch (Exception e2) {
                        isMainSpace = isMainSpace;
                        LogUtil.e(false, "determineUserPreference failed by Exception", new Object[0]);
                    }
                }
                if (this.isUserPref && this.mPrefNetworkType == 0 && defaultType == 1) {
                    clearSavedPreference();
                    Bundle cellInfo = NetUtil.getMobileDataState(this.mContext);
                    if (cellInfo == null) {
                        LogUtil.e(false, "cellInfo is null", new Object[0]);
                        return;
                    }
                    String cellName = cellInfo.getString(KEY_CELL_RAT, "UNKNOWN");
                    LogUtil.i(false, "%{public}s", "W2C: perfered Cell name=" + this.mPrefNetworkName + ", current Cell name=" + cellName + ", space=" + spaceIdOfAllAp);
                    if (cellName.equals(this.mPrefNetworkName)) {
                        brainCallback.onWaveMappingReportCallback(1, this.mPrefNetworkName, this.mPrefNetworkType);
                        this.mChrUserPrefSwitchTime = System.currentTimeMillis();
                        this.mChrUserPrefOriginalNwName = currNwName;
                        this.mChrUserPrefOriginalNwType = defaultType;
                        this.mChrUserPrefOriginalNwId = currNwId;
                        this.mChrUserPrefOriginalNwFreq = currNwFreq;
                        this.mChrUserPrefSwitchNwName = this.mPrefNetworkName;
                        this.mChrUserPrefSwitchNwType = this.mPrefNetworkType;
                        this.isChrUserPrefDetermineOn = true;
                    }
                } else if (this.isUserPref && this.mPrefNetworkType == 1 && defaultType == 0) {
                    LogUtil.i(false, "%{public}s", "C2W: perfered WiFi name=" + this.mPrefNetworkName + ", current Cell name=" + currNwName + ", space=" + spaceIdOfAllAp);
                    brainCallback.onWaveMappingReportCallback(1, this.mPrefNetworkName, this.mPrefNetworkType);
                    HashMap<Integer, String> preference2 = new HashMap<>(16);
                    preference2.put(0, this.mPrefNetworkName);
                    this.mHistoryQoeMgr.savePreferListForWifi(preference2);
                } else {
                    LogUtil.i(false, "No preference", new Object[0]);
                    brainCallback.onWaveMappingReportCallback(0, this.mPrefNetworkName, this.mPrefNetworkType);
                    clearSavedPreference();
                }
            }
        } catch (Exception e3) {
            LogUtil.e(false, "determineUserPreference failed by Exception", new Object[0]);
        }
    }

    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x005d: APUT  
      (r4v5 java.lang.Object[])
      (0 ??[int, short, byte, char])
      (wrap: java.lang.Integer : 0x0056: INVOKE  (r2v22 java.lang.Integer) = (r15v1 'spaceIdOfMainAp' int A[D('spaceIdOfMainAp' int)]) type: STATIC call: java.lang.Integer.valueOf(int):java.lang.Integer)
     */
    public boolean determine4gCoverage(RecognizeResult spaceIds) {
        boolean foundRecord;
        int spaceIdOfAllAp;
        int spaceIdOfMainAp;
        boolean z;
        Bundle record4g;
        HwWmpFastBackLte mBack = new HwWmpFastBackLte();
        new Bundle();
        double duration4gRatio = 0.0d;
        LogUtil.i(false, "determine4gCoverage ", new Object[0]);
        if (!this.param.getBack4gEnabled()) {
            LogUtil.d(false, "Fast Back 4G Feature - Disabled", new Object[0]);
        }
        if (spaceIds == null) {
            try {
                LogUtil.i(false, "no spaceIds", new Object[0]);
                return false;
            } catch (Exception e) {
                foundRecord = false;
                LogUtil.e(false, "determine4gCoverage failed by Exception", new Object[0]);
                return foundRecord;
            }
        } else {
            try {
                spaceIdOfAllAp = spaceIds.getRgResult();
                spaceIdOfMainAp = spaceIds.getMainApRgResult();
                if (spaceIdOfAllAp != 0) {
                    foundRecord = false;
                    z = false;
                    try {
                        record4g = judgeRecord4g(spaceIdOfAllAp, spaceIdOfMainAp);
                    } catch (Exception e2) {
                        LogUtil.e(false, "determine4gCoverage failed by Exception", new Object[0]);
                        return foundRecord;
                    }
                } else if (spaceIdOfMainAp == 0) {
                    processNoSpaceId();
                    return false;
                } else {
                    foundRecord = false;
                    try {
                        Object[] objArr = new Object[1];
                        z = false;
                        try {
                            objArr[0] = Integer.valueOf(spaceIdOfMainAp);
                            LogUtil.d(false, "determine4gCoverage, space ID: mainAp=%{public}d", objArr);
                            record4g = this.mSpaceUserDao.find4gCoverageByMainApSpace(spaceIdOfMainAp);
                        } catch (Exception e3) {
                            LogUtil.e(false, "determine4gCoverage failed by Exception", new Object[0]);
                            return foundRecord;
                        }
                    } catch (Exception e4) {
                        LogUtil.e(false, "determine4gCoverage failed by Exception", new Object[0]);
                        return foundRecord;
                    }
                }
            } catch (Exception e5) {
                foundRecord = false;
                LogUtil.e(false, "determine4gCoverage failed by Exception", new Object[0]);
                return foundRecord;
            }
            try {
                if (this.mFastBack2LteChrDao.sessionSpace(spaceIdOfAllAp, spaceIdOfMainAp)) {
                    try {
                        this.mFastBack2LteChrDao.startSession();
                        this.mFastBack2LteChrDao.addQueryCnt();
                    } catch (Exception e6) {
                        LogUtil.e(false, "determine4gCoverage failed by Exception", new Object[0]);
                        return foundRecord;
                    }
                }
                if (record4g == null) {
                    LogUtil.e(false, "record4g is null", new Object[0]);
                    return false;
                } else if (record4g.containsKey(KEY_CELL_NUM)) {
                    long duration = record4g.getLong(KEY_TOTAL_DURATION);
                    int cellNum = record4g.getInt(KEY_CELL_NUM);
                    try {
                        int avgSignal = record4g.getInt(KEY_AVG_SIGNAL);
                        long durationOut4g = record4g.getLong("durationOut4g");
                        if (duration + durationOut4g != 0) {
                            duration4gRatio = (((double) duration) * 1.0d) / ((double) (duration + durationOut4g));
                        }
                        try {
                            LogUtil.d(false, "found 4g record: duration=%{public}s ,cell num=%{public}d ,avg signal=%{public}d ,duration4gRatio=%{public}s", String.valueOf(duration), Integer.valueOf(cellNum), Integer.valueOf(avgSignal), String.valueOf(duration4gRatio));
                            this.mFastBack2LteChrDao.setCells4G(cellNum);
                            if (((long) this.param.getBack4gThresholdDurationMin()) >= duration || this.param.getBack4gThresholdSignalMin() >= avgSignal || this.param.getBack4gThresholdDuration4gRatio() >= duration4gRatio) {
                                process4gCellNotFound();
                                return foundRecord;
                            }
                            LogUtil.d(false, "in 4G coverage, send to booster", new Object[0]);
                            mBack.mSubId = 0;
                            mBack.setRat(KEY_4G);
                            processChr(mBack);
                            return true;
                        } catch (Exception e7) {
                            LogUtil.e(false, "determine4gCoverage failed by Exception", new Object[0]);
                            return foundRecord;
                        }
                    } catch (Exception e8) {
                        LogUtil.e(false, "determine4gCoverage failed by Exception", new Object[0]);
                        return foundRecord;
                    }
                } else {
                    try {
                        processNo4gCell();
                        return foundRecord;
                    } catch (Exception e9) {
                        LogUtil.e(false, "determine4gCoverage failed by Exception", new Object[0]);
                        return foundRecord;
                    }
                }
            } catch (Exception e10) {
                LogUtil.e(false, "determine4gCoverage failed by Exception", new Object[0]);
                return foundRecord;
            }
        }
    }

    private void processNoSpaceId() {
        LogUtil.i(false, "determine4gCoverage: no space ID", new Object[0]);
        if (this.mFastBack2LteChrDao.sessionSpace(0, 0)) {
            this.mFastBack2LteChrDao.startSession();
            this.mFastBack2LteChrDao.addQueryCnt();
        }
        this.mFastBack2LteChrDao.addUnknownSpace();
        this.mFastBack2LteChrDao.endSession();
        this.mFastBack2LteChrDao.insertRecordByLoc();
    }

    private Bundle judgeRecord4g(int spaceIdOfAllAp, int spaceIdOfMainAp) {
        if (spaceIdOfMainAp == 0) {
            LogUtil.d(false, "determine4gCoverage: space ID: allAp=%{public}d", Integer.valueOf(spaceIdOfAllAp));
            return this.mSpaceUserDao.find4gCoverageByAllApSpace(spaceIdOfAllAp);
        }
        LogUtil.d(false, "determine4gCoverage, space ID: allAp=%{public}d ,mainAp=%{public}d", Integer.valueOf(spaceIdOfAllAp), Integer.valueOf(spaceIdOfMainAp));
        return this.mSpaceUserDao.find4gCoverageByBothSpace(spaceIdOfAllAp, spaceIdOfMainAp);
    }

    private void processChr(HwWmpFastBackLte mBack) {
        this.mFastBack2LteChrDao.addInLteCnt();
        HwWmpFastBackLteManager hwWmpFastBackLteManager = this.mFastBackLteMgr;
        if (hwWmpFastBackLteManager != null) {
            hwWmpFastBackLteManager.sendDataToBooster(mBack);
            this.mFastBack2LteChrDao.addFastBack();
            this.mFastBack2LteChrDao.waitSession();
            this.handler.removeCallbacks(this.getRegStateAfterPeriodHandler);
            this.handler.postDelayed(this.getRegStateAfterPeriodHandler, (long) this.param.getReGetPsRegStatus());
            return;
        }
        this.mFastBack2LteChrDao.endSession();
    }

    private void process4gCellNotFound() {
        LogUtil.i(false, "4g cell NOT found ", new Object[0]);
        this.mFastBack2LteChrDao.addOutLteCnt();
        this.mFastBack2LteChrDao.endSession();
        this.mFastBack2LteChrDao.insertRecordByLoc();
    }

    private void processNo4gCell() {
        LogUtil.i(false, "NO 4g cell", new Object[0]);
        this.mFastBack2LteChrDao.setCells4G(0);
        this.mFastBack2LteChrDao.addOutLteCnt();
        this.mFastBack2LteChrDao.endSession();
        this.mFastBack2LteChrDao.insertRecordByLoc();
    }

    public void clearSavedPreference() {
        LogUtil.i(false, "clearSavedPreference", new Object[0]);
        this.mHistoryQoeMgr.savePreferListForWifi(new HashMap<>(16));
    }

    public void queryAppQoeByTargetNw(RecognizeResult spaceIds, int fullId, int uid, int network, IWaveMappingCallback callback, int direction) {
        int mArbitrationNet;
        String currWifiName;
        String currCellName;
        int spaceIdOfMainAp;
        Bundle wifiInfo;
        Bundle cellInfo;
        String currWifiName2;
        boolean isFound;
        boolean isFound2;
        boolean isGood;
        int tx;
        int days;
        int rx;
        Object obj;
        String nwName;
        String currWifiName3;
        String nwName2;
        int tx2;
        String currWifiName4 = "UNKNOWN";
        LogUtil.i(false, " queryAppQoeByTargetNw: appName=%{public}d", Integer.valueOf(fullId));
        String appName = Constant.USERDB_APP_NAME_PREFIX + fullId;
        float sourceNwBadRatio = 2.0f;
        float targetNwBadRatio = 2.0f;
        if (callback == null) {
            try {
                LogUtil.d(false, " no callback", new Object[0]);
            } catch (Exception e) {
                mArbitrationNet = 802;
                LogUtil.e(false, "assignSpaceExp2Space failed by Exception", new Object[0]);
                respondCallBack(uid, mArbitrationNet, true, false, callback, direction);
            }
        } else {
            try {
                int mArbitrationNet2 = judgeArbitrationNet(network);
                if (spaceIds == null) {
                    try {
                        LogUtil.i(false, " no spaceIds", new Object[0]);
                        respondCallBack(uid, mArbitrationNet2, true, false, callback, direction);
                    } catch (Exception e2) {
                        mArbitrationNet = mArbitrationNet2;
                        LogUtil.e(false, "assignSpaceExp2Space failed by Exception", new Object[0]);
                        respondCallBack(uid, mArbitrationNet, true, false, callback, direction);
                    }
                } else {
                    try {
                        LogUtil.i(false, " queryAppQoeByTargetNw: spaces=%{public}s", spaceIds.toString());
                        if (!Constant.getSavedQoeAppList().containsKey(Integer.valueOf(fullId))) {
                            LogUtil.i(false, " NOT monitor app", new Object[0]);
                            respondCallBack(uid, mArbitrationNet2, true, false, callback, direction);
                            return;
                        }
                        this.mHisQoeChrDao.accQueryCnt();
                        int spaceIdOfAllAp = spaceIds.getRgResult();
                        int spaceIdOfMainAp2 = spaceIds.getMainApRgResult();
                        this.mHisQoeChrService.resetRecordByApp(fullId);
                        this.mHisQoeChrService.setSpaceInfoOfAll(spaceIds.getRgResult(), spaceIds.getAllApModelName());
                        this.mHisQoeChrService.setSpaceInfoOfMain(spaceIds.getMainApRgResult(), spaceIds.getMainApModelName());
                        this.mHisQoeChrService.setSpaceInfoOfCell(0, 0);
                        if (spaceIdOfAllAp == 0 && spaceIdOfMainAp2 == 0) {
                            LogUtil.i(false, "no space found", new Object[0]);
                            respondCallBack(uid, mArbitrationNet2, true, false, callback, direction);
                            this.mHisQoeChrDao.accUnknownSpace();
                            this.mHisQoeChrService.saveRecordByApp(fullId);
                            return;
                        }
                        HashMap<String, List> historyQoe = calculateAppQoeFromDatabase(appName, spaceIdOfAllAp, spaceIdOfMainAp2);
                        boolean isGood2 = false;
                        boolean isFound3 = false;
                        Bundle wifiInfo2 = NetUtil.getWifiStateString(this.mContext);
                        if (wifiInfo2 == null) {
                            LogUtil.e(false, "wifiInfo is null", new Object[0]);
                            return;
                        }
                        String currWifiName5 = wifiInfo2.getString(KEY_WIFI_AP, currWifiName4);
                        if (currWifiName5 == null) {
                            currWifiName = currWifiName4;
                        } else {
                            currWifiName = currWifiName5;
                        }
                        Bundle cellInfo2 = NetUtil.getMobileDataState(this.mContext);
                        if (cellInfo2 == null) {
                            LogUtil.e(false, "cellInfo is null", new Object[0]);
                            return;
                        }
                        String currCellName2 = cellInfo2.getString(KEY_CELL_RAT, currWifiName4);
                        if (currCellName2 == null) {
                            currCellName2 = currWifiName4;
                        }
                        if (!historyQoe.isEmpty()) {
                            try {
                                LogUtil.i(false, "historyQoe size=%{public}d", Integer.valueOf(historyQoe.size()));
                                Iterator<Map.Entry<String, List>> it = historyQoe.entrySet().iterator();
                                while (it.hasNext()) {
                                    Map.Entry<String, List> entry = it.next();
                                    String nwName3 = entry.getKey();
                                    List value = entry.getValue();
                                    int nwType = ((Integer) value.get(0)).intValue();
                                    int nwId = ((Integer) value.get(1)).intValue();
                                    int nwFreq = ((Integer) value.get(2)).intValue();
                                    int dur = ((Integer) value.get(3)).intValue();
                                    int good = ((Integer) value.get(4)).intValue();
                                    int poor = ((Integer) value.get(5)).intValue();
                                    int rx2 = ((Integer) value.get(6)).intValue();
                                    try {
                                        tx = ((Integer) value.get(7)).intValue();
                                        days = ((Integer) value.get(8)).intValue();
                                        mArbitrationNet = mArbitrationNet2;
                                    } catch (Exception e3) {
                                        mArbitrationNet = mArbitrationNet2;
                                        LogUtil.e(false, "assignSpaceExp2Space failed by Exception", new Object[0]);
                                        respondCallBack(uid, mArbitrationNet, true, false, callback, direction);
                                    }
                                    try {
                                        float badRatio = ((Float) value.get(9)).floatValue();
                                        String currCellName3 = currCellName2;
                                        if (nwType == 1) {
                                            nwName2 = nwName3;
                                            if (!nwName2.equals(currWifiName) || currWifiName.equals(currWifiName4)) {
                                                obj = currWifiName4;
                                                nwName = currWifiName;
                                                rx = rx2;
                                                tx2 = tx;
                                                currWifiName3 = "  found source network, bad ratio=%{public}f";
                                            } else {
                                                nwName = currWifiName;
                                                obj = currWifiName4;
                                                tx2 = tx;
                                                LogUtil.v(false, "  found Wifi network:%{public}s", nwName2);
                                                if (network == nwType) {
                                                    targetNwBadRatio = badRatio;
                                                    LogUtil.i(false, "  found target network, bad ratio=%{public}f", Float.valueOf(badRatio));
                                                    this.mHisQoeChrService.setNetInfo(nwId, nwName2, nwFreq, nwType);
                                                    this.mHisQoeChrService.setTimeRecords(days, dur);
                                                    this.mHisQoeChrService.setQualityRecords(good, poor);
                                                    this.mHisQoeChrService.setDataRecords(rx2, tx2);
                                                    rx = rx2;
                                                    currWifiName3 = "  found source network, bad ratio=%{public}f";
                                                } else {
                                                    sourceNwBadRatio = badRatio;
                                                    rx = rx2;
                                                    currWifiName3 = "  found source network, bad ratio=%{public}f";
                                                    LogUtil.i(false, currWifiName3, Float.valueOf(badRatio));
                                                }
                                            }
                                        } else {
                                            obj = currWifiName4;
                                            rx = rx2;
                                            tx2 = tx;
                                            nwName2 = nwName3;
                                            nwName = currWifiName;
                                            currWifiName3 = "  found source network, bad ratio=%{public}f";
                                        }
                                        if (nwType == 0) {
                                            if (!nwName2.equals(currCellName3)) {
                                                currCellName3 = currCellName3;
                                            } else if (!currCellName3.equals(obj)) {
                                                currCellName3 = currCellName3;
                                                obj = obj;
                                                LogUtil.v(false, "  found CELL network:%{public}s", nwName2);
                                                if (network == nwType) {
                                                    targetNwBadRatio = badRatio;
                                                    LogUtil.i(false, "  found target network, bad ratio=%{public}f", Float.valueOf(badRatio));
                                                    this.mHisQoeChrService.setNetInfo(nwId, nwName2, nwFreq, nwType);
                                                    this.mHisQoeChrService.setTimeRecords(days, dur);
                                                    this.mHisQoeChrService.setQualityRecords(good, poor);
                                                    this.mHisQoeChrService.setDataRecords(rx, tx2);
                                                } else {
                                                    sourceNwBadRatio = badRatio;
                                                    LogUtil.i(false, currWifiName3, Float.valueOf(badRatio));
                                                }
                                            } else {
                                                currCellName3 = currCellName3;
                                                obj = obj;
                                            }
                                        }
                                        currWifiName = nwName;
                                        it = it;
                                        isGood2 = isGood2;
                                        isFound3 = isFound3;
                                        cellInfo2 = cellInfo2;
                                        wifiInfo2 = wifiInfo2;
                                        spaceIdOfMainAp2 = spaceIdOfMainAp2;
                                        appName = appName;
                                        mArbitrationNet2 = mArbitrationNet;
                                        currCellName2 = currCellName3;
                                        currWifiName4 = obj;
                                    } catch (Exception e4) {
                                        LogUtil.e(false, "assignSpaceExp2Space failed by Exception", new Object[0]);
                                        respondCallBack(uid, mArbitrationNet, true, false, callback, direction);
                                    }
                                }
                                mArbitrationNet = mArbitrationNet2;
                                currCellName = currCellName2;
                                cellInfo = cellInfo2;
                                currWifiName2 = currWifiName;
                                wifiInfo = wifiInfo2;
                                spaceIdOfMainAp = spaceIdOfMainAp2;
                                if (targetNwBadRatio != 2.0f) {
                                    float threshold = Constant.getSavedQoeAppList().get(Integer.valueOf(fullId)).floatValue();
                                    float margin = this.param.getAppThresholdTargetRationMargin();
                                    if (targetNwBadRatio < threshold) {
                                        isGood = true;
                                        LogUtil.d(false, "target network is GOOD enough, targetNwBadRatio:%{public}f", Float.valueOf(targetNwBadRatio));
                                    } else if (sourceNwBadRatio == 2.0f || targetNwBadRatio + margin >= sourceNwBadRatio) {
                                        LogUtil.d(false, "target network is Bad", new Object[0]);
                                        isGood = isGood2;
                                    } else {
                                        isGood = true;
                                        LogUtil.d(false, "target network(%{public}f) is BETTER than source network(%{public}f)", Float.valueOf(targetNwBadRatio), Float.valueOf(sourceNwBadRatio));
                                    }
                                    isFound = true;
                                    isFound2 = isGood;
                                } else {
                                    isFound2 = isGood2;
                                    isFound = isFound3;
                                }
                            } catch (Exception e5) {
                                mArbitrationNet = mArbitrationNet2;
                                LogUtil.e(false, "assignSpaceExp2Space failed by Exception", new Object[0]);
                                respondCallBack(uid, mArbitrationNet, true, false, callback, direction);
                            }
                        } else {
                            mArbitrationNet = mArbitrationNet2;
                            currCellName = currCellName2;
                            cellInfo = cellInfo2;
                            currWifiName2 = currWifiName;
                            wifiInfo = wifiInfo2;
                            spaceIdOfMainAp = spaceIdOfMainAp2;
                            try {
                                LogUtil.i(false, "historyQoe is empty", new Object[0]);
                                isFound2 = false;
                                isFound = false;
                            } catch (Exception e6) {
                                LogUtil.e(false, "assignSpaceExp2Space failed by Exception", new Object[0]);
                                respondCallBack(uid, mArbitrationNet, true, false, callback, direction);
                            }
                        }
                        try {
                            respondCallBack(uid, mArbitrationNet, isFound2, isFound, callback, direction);
                            try {
                                processHisQoeChrDao(isFound, isFound2, fullId);
                            } catch (Exception e7) {
                                mArbitrationNet = mArbitrationNet;
                                LogUtil.e(false, "assignSpaceExp2Space failed by Exception", new Object[0]);
                                respondCallBack(uid, mArbitrationNet, true, false, callback, direction);
                            }
                        } catch (Exception e8) {
                            mArbitrationNet = mArbitrationNet;
                            LogUtil.e(false, "assignSpaceExp2Space failed by Exception", new Object[0]);
                            respondCallBack(uid, mArbitrationNet, true, false, callback, direction);
                        }
                    } catch (Exception e9) {
                        mArbitrationNet = mArbitrationNet2;
                        LogUtil.e(false, "assignSpaceExp2Space failed by Exception", new Object[0]);
                        respondCallBack(uid, mArbitrationNet, true, false, callback, direction);
                    }
                }
            } catch (Exception e10) {
                mArbitrationNet = 802;
                LogUtil.e(false, "assignSpaceExp2Space failed by Exception", new Object[0]);
                respondCallBack(uid, mArbitrationNet, true, false, callback, direction);
            }
        }
    }

    private int judgeArbitrationNet(int network) {
        if (network == 1) {
            return 800;
        }
        if (network == 0) {
            return 801;
        }
        return 802;
    }

    private void processHisQoeChrDao(boolean isFound, boolean isGood, int fullId) {
        if (!isFound) {
            this.mHisQoeChrDao.accUnknownDb();
        } else if (isGood) {
            this.mHisQoeChrDao.accGoodCnt();
        } else {
            this.mHisQoeChrDao.accPoorCnt();
        }
        this.mHisQoeChrDao.insertRecordByLoc();
        this.mHisQoeChrService.saveRecordByApp(fullId);
    }

    private void respondCallBack(int uid, int net, boolean isGood, boolean isFound, IWaveMappingCallback callback, int direction) {
        if (callback == null) {
            return;
        }
        if (direction == 1) {
            callback.onWaveMappingRespondCallback(uid, 0, net, isGood, isFound);
        } else if (direction == 2) {
            callback.onWaveMappingRespond4BackCallback(uid, 0, net, isGood, isFound);
        }
    }

    public void determineAppQoePreference(RecognizeResult spaceIds, HwWmpAppInfo appInfo) {
        int fullId = appInfo.getAppFullId();
        if (spaceIds == null) {
            LogUtil.i(false, " no spaceIds", new Object[0]);
            return;
        }
        LogUtil.i(false, " determineAppQoePreference: spaces=%{public}s", spaceIds.toString());
        if (!Constant.getSavedQoeAppList().containsKey(Integer.valueOf(fullId))) {
            LogUtil.i(false, " NOT monitor app", new Object[0]);
            return;
        }
        int spaceIdOfAllAp = spaceIds.getRgResult();
        HwWaveMappingManager hwWaveMappingManager = HwWaveMappingManager.getInstance();
        if (hwWaveMappingManager != null) {
            IWaveMappingCallback appQoeCallback = hwWaveMappingManager.getAppQoeCallback();
            IWaveMappingCallback hiStreamCallback = hwWaveMappingManager.getHiStreamCallback();
            Bundle connectivity = NetUtil.getConnectedNetworkState(this.mContext);
            if (connectivity == null) {
                LogUtil.e(false, "connectivity is null", new Object[0]);
                return;
            }
            int currentNetworkType = connectivity.getInt(KEY_DEFAULT_TYPE, 8);
            Bundle appQoePrefResult = calculateAppQoePrefNetwork(fullId, spaceIdOfAllAp, connectivity.getString(KEY_DEFAULT_NETWORK_NAME, "UNKNOWN"));
            if (appQoePrefResult == null) {
                LogUtil.e(false, "appQoePrefResult is null", new Object[0]);
                return;
            }
            boolean isHasPref = appQoePrefResult.getBoolean(KEY_APP_QOE_HAS_PREFERENCE, false);
            int prefNetworkType = appQoePrefResult.getInt(KEY_PREFER_NETWORK_TYPE, 8);
            String prefNetworkName = appQoePrefResult.getString(KEY_PREFER_NETWORK_NAME, "UNKNOWN");
            if (isHasPref && currentNetworkType == 1 && prefNetworkType == 0) {
                if (appQoeCallback != null) {
                    appQoeCallback.onWaveMappingReportCallback(3, prefNetworkName, prefNetworkType);
                }
                if (hiStreamCallback != null) {
                    hiStreamCallback.onWaveMappingReportCallback(3, prefNetworkName, prefNetworkType);
                }
            }
        }
    }

    private Bundle calculateAppQoePrefNetwork(int fullId, int spaceIdOfAllAp, String currentNetworkName) {
        float f;
        Bundle result = new Bundle();
        int maxNetworkType = 8;
        float currentNetworkRatio = INVALID_RATIO;
        long totalDuration = 0;
        String app = Constant.USERDB_APP_NAME_PREFIX + fullId;
        try {
            result.putBoolean(KEY_APP_QOE_HAS_PREFERENCE, false);
            result.putInt(KEY_PREFER_NETWORK_TYPE, 8);
            result.putString(KEY_PREFER_NETWORK_NAME, "UNKNOWN");
            HashMap<String, List> valueQoe = this.mSpaceUserDao.findAppQoeGroupByAllSpace(app, spaceIdOfAllAp);
            if (valueQoe.size() > 0) {
                long currentNetworkDuration = 0;
                long maxNetworkDuration = 0;
                long cellDuration = 0;
                float maxNetworkRatio = -1.0f;
                String maxNetworkName = null;
                for (Map.Entry<String, List> entry : valueQoe.entrySet()) {
                    try {
                        String nwName = entry.getKey();
                        List qoeRaws = entry.getValue();
                        if (qoeRaws == null) {
                            try {
                                LogUtil.w(false, " qoe record invalid, qoeRaws=null", new Object[0]);
                                return result;
                            } catch (Exception e) {
                                f = maxNetworkRatio;
                                LogUtil.e(false, "calculateAppQoePrefNetwork failed by Exception", new Object[0]);
                                return result;
                            }
                        } else if (qoeRaws.size() < 4) {
                            try {
                                LogUtil.w(false, " qoe record invalid, qoeRaws size=%{public}d", Integer.valueOf(qoeRaws.size()));
                                return result;
                            } catch (Exception e2) {
                                f = maxNetworkRatio;
                                LogUtil.e(false, "calculateAppQoePrefNetwork failed by Exception", new Object[0]);
                                return result;
                            }
                        } else {
                            try {
                                int type = ((Integer) qoeRaws.get(0)).intValue();
                                long duration = ((Long) qoeRaws.get(3)).longValue();
                                int good = ((Integer) qoeRaws.get(4)).intValue();
                                int poor = ((Integer) qoeRaws.get(5)).intValue();
                                totalDuration += duration;
                                if (type == 0) {
                                    cellDuration += duration;
                                }
                                try {
                                    float ratio = judgeRatio(duration, good, poor, nwName);
                                    if (nwName.equals(currentNetworkName)) {
                                        currentNetworkDuration = duration;
                                        currentNetworkRatio = ratio;
                                    }
                                    if (maxNetworkName != null) {
                                        if (nwName.equals("UNKNOWN") || maxNetworkDuration >= duration) {
                                            maxNetworkType = maxNetworkType;
                                            app = app;
                                        }
                                    }
                                    maxNetworkDuration = duration;
                                    maxNetworkName = nwName;
                                    maxNetworkRatio = ratio;
                                    maxNetworkType = type;
                                    app = app;
                                } catch (Exception e3) {
                                    f = maxNetworkRatio;
                                    LogUtil.e(false, "calculateAppQoePrefNetwork failed by Exception", new Object[0]);
                                    return result;
                                }
                            } catch (Exception e4) {
                                f = maxNetworkRatio;
                                LogUtil.e(false, "calculateAppQoePrefNetwork failed by Exception", new Object[0]);
                                return result;
                            }
                        }
                    } catch (Exception e5) {
                        f = maxNetworkRatio;
                        LogUtil.e(false, "calculateAppQoePrefNetwork failed by Exception", new Object[0]);
                        return result;
                    }
                }
                if (currentNetworkName != null && maxNetworkName != null && !currentNetworkName.equals("UNKNOWN") && totalDuration > this.param.getUserPrefStartDuration() && !currentNetworkName.equals(maxNetworkName)) {
                    float threshold = Constant.getSavedQoeAppList().get(Integer.valueOf(fullId)).floatValue();
                    float margin = this.param.getAppThresholdTargetRationMargin();
                    LogUtil.d(false, "currentNetworkRatio:%{public}f maxNetworkRatio:%{public}f margin:%{public}f", Float.valueOf(currentNetworkRatio), Float.valueOf(maxNetworkRatio), Float.valueOf(margin));
                    if (currentNetworkRatio >= threshold && maxNetworkRatio >= 0.0f && maxNetworkRatio + margin < currentNetworkRatio) {
                        result.putBoolean(KEY_APP_QOE_HAS_PREFERENCE, true);
                        result.putInt(KEY_PREFER_NETWORK_TYPE, maxNetworkType);
                        result.putString(KEY_PREFER_NETWORK_NAME, maxNetworkName);
                        LogUtil.d(false, "isAppQoeHasPref = true, prefNetworkType:%{public}d prefNetworkName:%{public}s", Integer.valueOf(maxNetworkType), maxNetworkName);
                    }
                }
            }
        } catch (Exception e6) {
            f = -1.0f;
            LogUtil.e(false, "calculateAppQoePrefNetwork failed by Exception", new Object[0]);
            return result;
        }
        return result;
    }

    private float judgeRatio(long duration, int good, int poor, String nwName) {
        float ratio;
        if (duration <= ((long) this.param.getAppThresholdDurationMin())) {
            LogUtil.i(false, " too less duration = %{public}s, network=%{public}s", String.valueOf(duration), nwName);
            return INVALID_RATIO;
        } else if (poor > this.param.getAppThresholdPoorCntMin()) {
            if (good > this.param.getAppThresholdGoodCntMin()) {
                ratio = (((float) poor) * 1.0f) / ((((float) good) * 1.0f) + (((float) poor) * 1.0f));
            } else {
                ratio = (((float) poor) * 5000.0f) / (((float) duration) * 1.0f);
            }
            LogUtil.i(false, " have poor, network=%{public}s, poor ratio=%{public}f", nwName, Float.valueOf(ratio));
            return ratio;
        } else {
            LogUtil.i(false, " too less poor=%{public}d, network=%{public}s", Integer.valueOf(poor), nwName);
            return 0.0f;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v2, resolved type: int */
    /* JADX DEBUG: Multi-variable search result rejected for r9v8, resolved type: int */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v0 */
    /* JADX WARN: Type inference failed for: r3v5 */
    /* JADX WARN: Type inference failed for: r9v9 */
    /* JADX WARN: Type inference failed for: r9v13 */
    private HashMap<String, List> calculateAppQoeFromDatabase(String app, int spaceIdOfAllAp, int spaceIdOfMainAp) {
        HashMap<String, List> valueQoe;
        int i;
        float ratio;
        CollectUserFingersHandler collectUserFingersHandler = this;
        HashMap<String, List> resultsQoe = new HashMap<>(16);
        int i2 = 0;
        try {
            try {
                HashMap<String, List> valueQoe2 = collectUserFingersHandler.mSpaceUserDao.findAppQoeGroupBySpace(app, spaceIdOfAllAp, spaceIdOfMainAp);
                for (Map.Entry<String, List> entry : valueQoe2.entrySet()) {
                    String nwName = entry.getKey();
                    List qoeRaws = entry.getValue();
                    if (qoeRaws == null) {
                        LogUtil.w(i2, " qoe record invalid, qoeRaws=null", new Object[i2]);
                        return resultsQoe;
                    } else if (qoeRaws.size() < 9) {
                        Object[] objArr = new Object[1];
                        objArr[i2] = Integer.valueOf(qoeRaws.size());
                        LogUtil.w(i2, " qoe record invalid, qoeRaws size=%{public}d", objArr);
                        return resultsQoe;
                    } else {
                        ((Integer) qoeRaws.get(i2)).intValue();
                        int duration = ((Integer) qoeRaws.get(3)).intValue();
                        int good = ((Integer) qoeRaws.get(4)).intValue();
                        int poor = ((Integer) qoeRaws.get(5)).intValue();
                        float ratio2 = 2.0f;
                        if (duration > collectUserFingersHandler.param.getAppThresholdDurationMin()) {
                            if (poor > collectUserFingersHandler.param.getAppThresholdPoorCntMin()) {
                                if (good > collectUserFingersHandler.param.getAppThresholdGoodCntMin()) {
                                    valueQoe = valueQoe2;
                                    ratio = (((float) poor) * 1.0f) / ((((float) good) * 1.0f) + (((float) poor) * 1.0f));
                                } else {
                                    valueQoe = valueQoe2;
                                    ratio = (((float) poor) * 5000.0f) / (((float) duration) * 1.0f);
                                }
                                LogUtil.i(false, " have poor, network=%{public}s, poor ratio=%{public}f", nwName, Float.valueOf(ratio));
                                ratio2 = ratio;
                                i = 0;
                            } else {
                                valueQoe = valueQoe2;
                                i = 0;
                                LogUtil.i(false, " too less poor=%{public}d, network=%{public}s", Integer.valueOf(poor), nwName);
                                ratio2 = 0.0f;
                            }
                            LogUtil.d(i, " add history Qoe", new Object[i]);
                        } else {
                            valueQoe = valueQoe2;
                            LogUtil.i(false, " too less duration = %{public}d, network=%{public}s", Integer.valueOf(duration), nwName);
                        }
                        qoeRaws.add(Float.valueOf(ratio2));
                        resultsQoe.put(nwName, qoeRaws);
                        i2 = 0;
                        collectUserFingersHandler = this;
                        valueQoe2 = valueQoe;
                    }
                }
                LogUtil.d(false, " record size:%{public}d", Integer.valueOf(resultsQoe.size()));
            } catch (Exception e) {
                LogUtil.e(false, "calculateAppQoeFromDatabase failed by Exception", new Object[0]);
                return resultsQoe;
            }
        } catch (Exception e2) {
            LogUtil.e(false, "calculateAppQoeFromDatabase failed by Exception", new Object[0]);
            return resultsQoe;
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
            LogUtil.d(false, " setNewStartTime - new, %{public}s", newInfo.toString());
            return;
        }
        LogUtil.w(false, " getAppName == null", new Object[0]);
    }

    private void updateStartTime(String app, int net) {
        if (this.saveAppInfo.isEmpty() || !this.saveAppInfo.containsKey(app)) {
            LogUtil.d(false, " updateStartTime, no this app:%{public}s", app);
            return;
        }
        this.saveAppInfo.get(app).setStartTime(System.currentTimeMillis());
        this.saveAppInfo.get(app).setConMgrNetworkType(net);
        LogUtil.i(false, " updateStartTime, update app:%{public}s, appNwType:%{public}d", app, Integer.valueOf(net));
    }

    private void resetTime(String appName) {
        if (this.saveAppInfo.isEmpty()) {
            LogUtil.i(false, " no saved APP", new Object[0]);
            return;
        }
        if (this.saveAppInfo.containsKey(appName)) {
            this.saveAppInfo.remove(appName);
        }
        LogUtil.i(false, " resetTime, app:%{public}s", appName);
    }

    public void checkUserPrefAutoSwitch() {
        Bundle connectivity = NetUtil.getConnectedNetworkState(this.mContext);
        if (connectivity == null) {
            LogUtil.e(false, "connectivity is null", new Object[0]);
            return;
        }
        int defaultType = connectivity.getInt(KEY_DEFAULT_TYPE);
        if (this.isChrUserPrefDetermineOn && !this.isChrUserPrefAutoOn && !this.isUserOperated && this.mChrUserPrefSwitchNwName.equals(connectivity.getString(KEY_DEFAULT_NETWORK_NAME)) && defaultType == this.mChrUserPrefSwitchNwType) {
            this.isChrUserPrefAutoOn = true;
            this.mChrUserPrefAutoSwitchTime = System.currentTimeMillis();
            this.mChrUserPrefSwitchNwId = connectivity.getString(KEY_DEFAULT_NETWORK_ID);
            this.mChrUserPrefSwitchNwFreq = connectivity.getString(KEY_DEFAULT_NETWORK_FREQUENCY);
        }
    }

    private void checkDefaultConnectivityState() {
        Bundle connectivity = NetUtil.getConnectedNetworkState(this.mContext);
        if (connectivity == null) {
            LogUtil.e(false, "connectivity is null", new Object[0]);
            return;
        }
        int defaultType = connectivity.getInt(KEY_DEFAULT_TYPE);
        if (this.isChrUserPrefDetermineOn) {
            if (!this.isChrUserPrefAutoOn && !this.isUserOperated && this.mChrUserPrefSwitchNwName.equals(connectivity.getString(KEY_DEFAULT_NETWORK_NAME)) && defaultType == this.mChrUserPrefSwitchNwType) {
                this.isChrUserPrefAutoOn = true;
                this.mChrUserPrefAutoSwitchTime = System.currentTimeMillis();
                this.mChrUserPrefSwitchNwId = connectivity.getString(KEY_DEFAULT_NETWORK_ID);
                this.mChrUserPrefSwitchNwFreq = connectivity.getString(KEY_DEFAULT_NETWORK_FREQUENCY);
            }
            if (!this.isChrUserPrefManualOn && this.isUserOperated && this.mDestNetworkId.equals("UNKNOWN") && !this.isChrUserPrefAutoOn && this.mChrUserPrefSwitchNwName.equals(connectivity.getString(KEY_DEFAULT_NETWORK_NAME)) && defaultType == this.mChrUserPrefSwitchNwType) {
                this.isChrUserPrefManualOn = true;
                this.mChrUserPrefManualSwitchTime = System.currentTimeMillis();
            }
            if (!this.isChrUserPrefManualOn && this.isUserOperated && this.mDestNetworkId.equals("UNKNOWN") && this.isChrUserPrefAutoOn && this.mChrUserPrefOriginalNwName.equals(connectivity.getString(KEY_DEFAULT_NETWORK_NAME)) && defaultType == this.mChrUserPrefOriginalNwType) {
                this.isChrUserPrefManualOn = true;
                this.mChrUserPrefManualSwitchTime = System.currentTimeMillis();
            }
        }
        if (defaultType == 8 || !this.mDestNetworkId.equals("UNKNOWN")) {
            LogUtil.i(false, "Not setup mDestNetworkId, defaultType=%{public}d, DestNetworkId=%{public}s", Integer.valueOf(defaultType), this.mDestNetworkId);
        } else if (!this.isDestNetworkCellular && defaultType == 1) {
            this.mDestNetworkId = connectivity.getString(KEY_DEFAULT_NETWORK_ID);
            this.mDestNetworkName = connectivity.getString(KEY_DEFAULT_NETWORK_NAME);
            this.mDestNetworkFreq = connectivity.getString(KEY_DEFAULT_NETWORK_FREQUENCY);
            this.mDestNetworkType = 1;
            LogUtil.d(false, "CONNECTIVITY_ACTION WIFI connected: mDestNetworkName:%{public}s", this.mDestNetworkName);
            LogUtil.v(false, "                                    mDestNetworkId:%{public}s", this.mDestNetworkId);
        } else if (this.isDestNetworkCellular && defaultType == 0) {
            this.mDestNetworkId = connectivity.getString(KEY_DEFAULT_NETWORK_ID);
            this.mDestNetworkName = connectivity.getString(KEY_DEFAULT_NETWORK_NAME);
            this.mDestNetworkFreq = connectivity.getString(KEY_DEFAULT_NETWORK_FREQUENCY);
            this.mDestNetworkType = 0;
            LogUtil.d(false, "CONNECTIVITY_ACTION MOBILE connected: mDestNetworkName:%{public}s", this.mDestNetworkName);
            LogUtil.v(false, "                                      mDestNetworkId:%{public}s", this.mDestNetworkId);
        }
        checkAppCurrentNetwork();
    }

    public void checkAllConnectivityState(int connectedState, int netType) {
        LogUtil.i(false, "checkAllConnectivityState", new Object[0]);
        if (connectedState == 3) {
            try {
                LogUtil.i(false, "checkDefaultConnectivityState", new Object[0]);
                checkDefaultConnectivityState();
            } catch (Exception e) {
                LogUtil.e(false, "checkAllConnectivityState failed by Exception", new Object[0]);
            }
        } else if (connectedState == 4) {
            LogUtil.i(false, "checkAppCurrentNetwork", new Object[0]);
            checkAppCurrentNetwork();
        } else if (connectedState == 1) {
            if (netType == 1) {
                LogUtil.i(false, "checkConnectivityState, wifi start", new Object[0]);
                HwWmpAppInfo wifiApp = new HwWmpAppInfo(1);
                LogUtil.v(false, "checkConnectivityState: wifiApp=%{public}s", wifiApp.toString());
                setNewStartTime(wifiApp);
                backupWifiInfo();
            }
            if (netType == 0) {
                LogUtil.i(false, "checkConnectivityState, mobile start", new Object[0]);
                HwWmpAppInfo mobileApp = new HwWmpAppInfo(0);
                LogUtil.v(false, "checkConnectivityState: mobileApp=%{public}s", mobileApp.toString());
                setNewStartTime(mobileApp);
            }
        } else if (connectedState == 2) {
            if (netType == 1) {
                LogUtil.i(false, "checkConnectivityState, wifi end", new Object[0]);
                updateWifiDurationEnd();
            }
            if (netType == 0) {
                LogUtil.i(false, "checkConnectivityState, mobile end", new Object[0]);
                updateMobileDurationEnd();
            }
        }
    }

    private void checkAppCurrentNetwork() {
        LogUtil.i(false, "checkAppCurrentNetwork ", new Object[0]);
        try {
            if (!this.saveAppInfo.isEmpty()) {
                LogUtil.i(false, "              ,saveAppInfo size=%{public}d", Integer.valueOf(this.saveAppInfo.size()));
                for (Map.Entry<String, HwWmpAppInfo> entry : this.saveAppInfo.entrySet()) {
                    String app = entry.getKey();
                    HwWmpAppInfo info = entry.getValue();
                    int nwType = info.getConMgrNetworkType();
                    int uid = info.getAppUid();
                    if (info.isNormalApp()) {
                        int arbitrationNwType = HwArbitrationFunction.getCurrentNetwork(this.mContext, uid);
                        int currConMgrNwType = 8;
                        if (arbitrationNwType == 800) {
                            currConMgrNwType = 1;
                        } else if (arbitrationNwType == 801) {
                            currConMgrNwType = 0;
                        }
                        if (currConMgrNwType != nwType) {
                            updateAppNetwork(app, currConMgrNwType);
                            LogUtil.d(false, "network change, app=%{public}s, from %{public}d to %{public}d", app, Integer.valueOf(nwType), Integer.valueOf(currConMgrNwType));
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e(false, "checkAppCurrentNetwork failed by Exception", new Object[0]);
        }
    }

    public boolean checkOutOf4gCoverage(boolean isRestarted) {
        boolean isOutOf4g;
        Bundle cellInfo = NetUtil.getMobileDataState(this.mContext);
        boolean isCallIdle = NetUtil.isMobileCallStateIdle(this.mContext);
        if (!this.param.getBack4gEnabled()) {
            LogUtil.d(false, "Fast Back 4G Feature - Disabled", new Object[0]);
            return false;
        } else if (!isCallIdle) {
            LogUtil.d(false, "checkOutOf4gCoverage: call exists", new Object[0]);
            resetOut4gBeginTime();
            this.handler.removeCallbacks(this.periodicOutOf4gHandler);
            return false;
        } else if (cellInfo == null || !cellInfo.containsKey(KEY_CELL_RAT)) {
            LogUtil.d(false, "checkOutOf4gCoverage: null cell RAT, last outof4Gflag=%{public}s", String.valueOf(false));
            resetOut4gBeginTime();
            this.handler.removeCallbacks(this.periodicOutOf4gHandler);
            return false;
        } else {
            String cellRat = cellInfo.getString(KEY_CELL_RAT, "UNKNOWN");
            String mState = cellInfo.getString(KEY_CELL_STATE, KEY_DISABLED);
            int dataState = cellInfo.getInt(KEY_DATA_STATE);
            int preferredMode = cellInfo.getInt(KEY_PREFERRED_MODE);
            if (!Arrays.asList(VALID_PREFERRED_MODES).contains(Integer.valueOf(preferredMode))) {
                LogUtil.i(false, "NW preferred mode:%{public}d NOT supported", Integer.valueOf(preferredMode));
                isOutOf4g = false;
                this.handler.removeCallbacks(this.periodicOutOf4gHandler);
            } else if (KEY_DISABLED.equals(mState) || dataState != 2) {
                isOutOf4g = false;
                this.handler.removeCallbacks(this.periodicOutOf4gHandler);
                LogUtil.d(false, "checkOutOf4gCoverage: Data Disabled(%{public}s) or Not Connect(%{public}d), then stop searching 4G process", mState, Integer.valueOf(dataState));
            } else if (cellRat.equals(KEY_4G)) {
                isOutOf4g = false;
                processBackTo4g();
                this.mFastBack2LteChrDao.endSession();
            } else {
                isOutOf4g = true;
                prepareBackTo4g(isRestarted);
            }
            LogUtil.d(false, "checkOutOf4gCoverage: out of 4G=%{public}s, cellRat=%{public}s", String.valueOf(isOutOf4g), cellRat);
            return isOutOf4g;
        }
    }

    private void processBackTo4g() {
        resetOut4gBeginTime();
        this.handler.removeCallbacks(this.periodicOutOf4gHandler);
        if (!this.isBack4gBegin) {
            LogUtil.i(false, " once back to 4G", new Object[0]);
            this.isBack4gBegin = true;
            this.back4gRestartCnt = 0;
            this.mMachineHandler.sendEmptyMessage(WMStateCons.MSG_BACK_4G_COVERAGE);
            this.mFastBack2LteChrDao.addSuccessBack();
            this.mFastBack2LteChrDao.insertRecordByLoc();
        }
    }

    private void prepareBackTo4g(boolean isRestarted) {
        this.isBack4gBegin = false;
        int cell4gNum = 0;
        Bundle record4g = this.mSpaceUserDao.find4gCoverageByCurrLoc();
        if (record4g.containsKey(KEY_CELL_NUM)) {
            cell4gNum = record4g.getInt(KEY_CELL_NUM);
        }
        if (cell4gNum == 0) {
            LogUtil.d(false, "checkOutOf4gCoverage: no 4G cell in this freq location, not trigger searching 4G process", new Object[0]);
        } else if (this.outOf4gBegin == 0) {
            this.outOf4gBegin = System.currentTimeMillis();
            this.back4gRestartCnt = 0;
            LogUtil.i(false, "checkOutOf4gCoverage: 1st out to 4G, begin time=%{public}s", String.valueOf(this.outOf4gBegin));
            this.mFastBack2LteChrDao.startSession();
            this.mFastBack2LteChrDao.sessionSpace(-1, -1);
            this.mFastBack2LteChrDao.addLowRatCnt();
            this.handler.postDelayed(this.periodicOutOf4gHandler, (long) this.param.getBack4gThresholdOut4gInterval());
        } else if (isRestarted) {
            LogUtil.i(false, "checkOutOf4gCoverage: isRestarted(%{public}d)", Integer.valueOf(this.back4gRestartCnt));
            if (this.back4gRestartCnt < this.param.getBack4gThresholdRestartLimit()) {
                this.handler.postDelayed(this.periodicOutOf4gHandler, (long) (this.param.getBack4gThresholdOut4gInterval() * 2));
                this.back4gRestartCnt++;
            }
        } else {
            long durationOutOf4G = System.currentTimeMillis() - this.outOf4gBegin;
            LogUtil.i(false, "checkOutOf4gCoverage: already out to 4G, duration=%{public}s, TH=%{public}d", String.valueOf(durationOutOf4G), Integer.valueOf(this.param.getBack4gThresholdOut4gInterval()));
            if (((long) this.param.getBack4gThresholdOut4gInterval()) < durationOutOf4G) {
                this.handler.removeCallbacks(this.periodicOutOf4gHandler);
                this.mMachineHandler.sendEmptyMessage(WMStateCons.MSG_CHECK_4G_COVERAGE);
                return;
            }
            this.handler.postDelayed(this.periodicOutOf4gHandler, (long) this.param.getBack4gThresholdOut4gInterval());
        }
    }

    public void resetOut4gBeginTime() {
        this.outOf4gBegin = 0;
    }

    private Bundle getSpecifiedDataState(int networkType) {
        Bundle output = new Bundle();
        boolean isValid = false;
        String nwName = "UNKNOWN";
        String nwId = "UNKNOWN";
        String nwFreq = "UNKNOWN";
        String signal = "UNKNOWN";
        if (networkType == 1) {
            Bundle wifiInfo = NetUtil.getWifiStateString(this.mContext);
            if (wifiInfo == null) {
                LogUtil.e(false, "wifiInfo is null", new Object[0]);
                return output;
            }
            nwName = wifiInfo.getString(KEY_WIFI_AP, "UNKNOWN");
            nwId = wifiInfo.getString(KEY_WIFI_MAC, "UNKNOWN");
            nwFreq = wifiInfo.getString(KEY_WIFI_CHANNEL, "UNKNOWN");
            signal = wifiInfo.getString(KEY_WIFI_RSSI, "0");
            if (!(nwName == null || nwId == null || nwFreq == null)) {
                isValid = true;
                backupWifiInfo();
            }
        }
        if (networkType == 0) {
            Bundle cellInfo = NetUtil.getMobileDataState(this.mContext);
            if (cellInfo == null) {
                LogUtil.e(false, "cellInfo is null", new Object[0]);
                return output;
            }
            nwName = cellInfo.getString(KEY_CELL_RAT, "UNKNOWN");
            nwId = cellInfo.getString(KEY_CELL_ID, "UNKNOWN");
            nwFreq = cellInfo.getString(KEY_CELL_FREQUENT, "UNKNOWN");
            signal = cellInfo.getString(KEY_CELL_RSSI, "UNKNOWN");
            if (!(nwName == null || nwId == null || nwFreq == null)) {
                isValid = true;
                backupCellInfo();
            }
        }
        output.putBoolean(KEY_VALID, isValid);
        output.putString(KEY_ID, nwId);
        output.putString(KEY_NAME, nwName);
        output.putString(KEY_FREQUENCY, nwFreq);
        output.putString(KEY_SIGNAL, signal);
        return output;
    }

    public FastBack2LteChrDao getBack2LteChrDao() {
        return this.mFastBack2LteChrDao;
    }

    private void updateUserPrefChr() {
        if (this.isChrUserPrefDetermineOn) {
            LogUtil.i(false, "updateUserPrefChr: DetermineSwitch, %{public}s -> %{public}s Time:%{public}s", this.mChrUserPrefOriginalNwName, this.mChrUserPrefSwitchNwName, String.valueOf(this.mChrUserPrefSwitchTime));
            LogUtil.i(false, "updateUserPrefChr: Auto Switch Time %{public}s Manual Switch Time:%{public}s", String.valueOf(this.mChrUserPrefAutoSwitchTime), String.valueOf(this.mChrUserPrefManualSwitchTime));
            this.mLocationDao.accChrUserPrefTotalSwitchByFreqLoc(this.freqLoc);
            if (this.isChrUserPrefAutoOn) {
                if (this.isChrUserPrefManualOn) {
                    long j = this.mChrUserPrefManualSwitchTime;
                    long j2 = this.mChrUserPrefAutoSwitchTime;
                    if (j > j2 && j - j2 < 60000) {
                        LogUtil.i(false, "updateUserPrefChr: AutoFail + 1", new Object[0]);
                        this.mLocationDao.accChrUserPrefAutoFailByFreqLoc(this.freqLoc);
                    }
                }
                if (!this.isChrUserPrefManualOn) {
                    LogUtil.i(false, "updateUserPrefChr: AutoSucc + 1", new Object[0]);
                    this.mLocationDao.accChrUserPrefAutoSuccByFreqLoc(this.freqLoc);
                }
            } else if (this.isChrUserPrefManualOn) {
                LogUtil.i(false, "updateUserPrefChr: ManualSucc + 1", new Object[0]);
                this.mLocationDao.accChrUserPrefManualSuccByFreqLoc(this.freqLoc);
            } else {
                LogUtil.i(false, "updateUserPrefChr: NoSwitchFail + 1", new Object[0]);
                this.mLocationDao.accChrUserPrefNoSwitchFailByFreqLoc(this.freqLoc);
            }
        }
    }

    private void clearUserPrefChr() {
        this.isChrUserPrefDetermineOn = false;
        this.mChrUserPrefOriginalNwName = "UNKNOWN";
        this.mChrUserPrefOriginalNwType = 8;
        this.mChrUserPrefOriginalNwId = "UNKNOWN";
        this.mChrUserPrefOriginalNwFreq = "UNKNOWN";
        this.mChrUserPrefSwitchNwName = "UNKNOWN";
        this.mChrUserPrefSwitchNwType = 8;
        this.mChrUserPrefSwitchNwId = "UNKNOWN";
        this.mChrUserPrefSwitchNwFreq = "UNKNOWN";
        this.mChrUserPrefSwitchTime = 0;
        this.isChrUserPrefAutoOn = false;
        this.mChrUserPrefAutoSwitchTime = 0;
        this.isChrUserPrefManualOn = false;
        this.mChrUserPrefManualSwitchTime = 0;
    }
}
