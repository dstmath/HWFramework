package com.android.server.wifi.dc;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.internal.util.State;
import com.android.internal.util.StateMachine;
import com.android.server.wifi.HwHilinkProxyController;
import com.android.server.wifi.HwWifiServiceManager;
import com.android.server.wifi.HwWifiServiceManagerImpl;
import com.android.server.wifi.IHwHilinkCallback;
import com.android.server.wifi.p2p.HwWifiP2pService;
import com.android.server.wifi.p2p.WifiP2pServiceImpl;
import com.huawei.hilink.framework.aidl.ResponseCallbackWrapper;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DcHilinkController extends StateMachine {
    private static final int ADDRESS_LENGTH = 6;
    private static final int DC_ACTION_DELAY_MSEC = 3000;
    private static final int DC_ACTION_RETRY_LIMIT_TIMES = 5;
    private static final int DC_MANAGER_DETECT_INTVAL_MSEC = 3000;
    private static final int DC_P2P_DELETE_DELAY_MSEC = 1000;
    private static final int DISCONNECT_HILINK_DELAY_TIME_MSEC = 100000;
    private static final int DISCOVER_LIMIT_RETRY_TIMES = 3;
    private static final int HILINK_DISCOVER_TIMEOUT_MS = 3000;
    private static final String TAG = "DcHilinkController";
    private static DcHilinkController sDcHilinkController = null;
    private ActiveState mActiveState = new ActiveState();
    private String mBssid = "";
    private Context mContext;
    private int mDcActionRetryTimes = 0;
    private int mDcActionType = 0;
    private DcChr mDcChr;
    private Handler mDcHilinkHandler;
    private DefaultState mDefaultState = new DefaultState();
    private int mDiscoverRetryTimes = 0;
    private DiscoverState mDiscoverState = new DiscoverState();
    private long mElapsedScreenOffTime = 0;
    private IHwHilinkCallback mHwHilinkCallback = new IHwHilinkCallback() {
        /* class com.android.server.wifi.dc.DcHilinkController.AnonymousClass1 */
        private static final int STATE_DTLS_FAILURE = 1;

        @Override // com.android.server.wifi.IHwHilinkCallback
        public void onProxyReadyStateChanged(int state) {
            HwHiLog.d(DcHilinkController.TAG, false, "onProxyReady, state=%{public}d, isEnterDiscoverState=%{public}s", new Object[]{Integer.valueOf(state), String.valueOf(DcHilinkController.this.mIsEnterDiscoverState)});
            if (state != 0 || !DcHilinkController.this.mIsEnterDiscoverState) {
                DcHilinkController dcHilinkController = DcHilinkController.this;
                dcHilinkController.transitionTo(dcHilinkController.mIdleState);
                return;
            }
            DcHilinkController.this.sendMessage(19);
        }

        @Override // com.android.server.wifi.IHwHilinkCallback
        public void onProxyLostStateChanged() {
            HwHiLog.d(DcHilinkController.TAG, false, "onProxyLostStateChanged", new Object[0]);
            DcHilinkController dcHilinkController = DcHilinkController.this;
            dcHilinkController.transitionTo(dcHilinkController.mIdleState);
        }

        @Override // com.android.server.wifi.IHwHilinkCallback
        public void onConnectionStateChanged(int state) {
            if (state == 1) {
                DcHilinkController.this.mDiscoverRetryTimes = 0;
                DcHilinkController.this.sendMessage(21);
            }
        }

        @Override // com.android.server.wifi.IHwHilinkCallback
        public void onConnectSuccessResult(int requestId) {
            HwHiLog.d(DcHilinkController.TAG, false, "onConnectSuccessResult, isDcConfigDetected=%{public}s,discoverRetryTimes=%{public}d", new Object[]{String.valueOf(DcHilinkController.this.mIsDcConfigDetected), Integer.valueOf(DcHilinkController.this.mDiscoverRetryTimes)});
            if (!DcHilinkController.this.mIsDcConfigDetected && DcHilinkController.this.mDiscoverRetryTimes > 0) {
                DcHilinkController.this.mDiscoverRetryTimes = 0;
                DcHilinkController.this.sendMessage(20);
            }
        }

        @Override // com.android.server.wifi.IHwHilinkCallback
        public void onConnectFailedResult(int requestId, int errorCode) {
            HwHiLog.d(DcHilinkController.TAG, false, "onConnectFailedResult, isDcConfigDetected=%{public}s,discoverRetryTimes=%{public}d", new Object[]{String.valueOf(DcHilinkController.this.mIsDcConfigDetected), Integer.valueOf(DcHilinkController.this.mDiscoverRetryTimes)});
            if (!DcHilinkController.this.mIsDcConfigDetected && DcHilinkController.this.mDiscoverRetryTimes > 0) {
                DcHilinkController.this.mDiscoverRetryTimes = 0;
                DcHilinkController.this.sendMessage(21);
            }
        }
    };
    private HwHilinkProxyController mHwHilinkProxyController;
    private IdleState mIdleState = new IdleState();
    private boolean mIsDcAllowedByRssi = false;
    private boolean mIsDcConfigDetected = false;
    private boolean mIsDcConnected = false;
    private boolean mIsEnterDiscoverState = false;
    private HwHilinkProxyController.HwHilinkModuleType mModuleType = HwHilinkProxyController.HwHilinkModuleType.DC;
    private String mP2pMacAddress = "";
    private HwHilinkProxyController.HwHilinkServiceType mServiceType = HwHilinkProxyController.HwHilinkServiceType.DC;
    private WifiManager mWifiManager;

    private DcHilinkController(Context context) {
        super(TAG);
        this.mContext = context;
        this.mDcHilinkHandler = getHandler();
        this.mWifiManager = (WifiManager) this.mContext.getSystemService("wifi");
        this.mDcChr = DcChr.getInstance();
        this.mHwHilinkProxyController = HwHilinkProxyController.getInstance();
        HwHiLog.d(TAG, false, "registerHilinkCallback isRegisterSuccess=%{public}s", new Object[]{Boolean.valueOf(this.mHwHilinkProxyController.isRegisterHilinkCallback(this.mHwHilinkCallback))});
        addState(this.mDefaultState);
        addState(this.mIdleState, this.mDefaultState);
        addState(this.mDiscoverState, this.mDefaultState);
        addState(this.mActiveState, this.mDefaultState);
        setInitialState(this.mIdleState);
        start();
    }

    public static DcHilinkController createDcHilinkController(Context context) {
        if (sDcHilinkController == null) {
            sDcHilinkController = new DcHilinkController(context);
        }
        return sDcHilinkController;
    }

    public static DcHilinkController getInstance() {
        return sDcHilinkController;
    }

    public Handler getDcHilinkHandler() {
        return this.mDcHilinkHandler;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initHilinkControllerParams() {
        HwHiLog.d(TAG, false, "init DcHilinkController config", new Object[0]);
        this.mDiscoverRetryTimes = 0;
        this.mDcActionRetryTimes = 0;
        this.mIsEnterDiscoverState = false;
        if (this.mHwHilinkProxyController.isOpened()) {
            HwHiLog.d(TAG, false, "already open, now close", new Object[0]);
            this.mHwHilinkProxyController.removeReferenceModule(this.mModuleType.getValue());
            this.mHwHilinkProxyController.closeHilinkServiceProxy();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void closeHilinkService() {
        this.mHwHilinkProxyController.clearReferenceModule();
        this.mHwHilinkProxyController.closeHilinkServiceProxy();
    }

    public boolean isWifiAndP2pStateAllowDc() {
        DcMonitor dcMonitor = DcMonitor.getInstance();
        if (dcMonitor == null) {
            HwHiLog.e(TAG, false, "dcMonitor is null", new Object[0]);
            return false;
        }
        boolean isP2pEnabled = false;
        boolean isP2pServiceExist = false;
        HwWifiServiceManager hwWifiServiceManager = HwWifiServiceManagerImpl.getDefault();
        if (hwWifiServiceManager instanceof HwWifiServiceManagerImpl) {
            WifiP2pServiceImpl wifiP2pServiceImpl = ((HwWifiServiceManagerImpl) hwWifiServiceManager).getHwWifiP2pService();
            if (wifiP2pServiceImpl instanceof HwWifiP2pService) {
                isP2pServiceExist = ((HwWifiP2pService) wifiP2pServiceImpl).hasP2pService();
                isP2pEnabled = wifiP2pServiceImpl.isP2pEnabled();
            }
        }
        boolean isWifiConnected = dcMonitor.isWifiConnected();
        HwHiLog.i(TAG, false, "p2pEnabled=%{public}s wifiConnected=%{public}s isP2pServiceExist=%{public}s", new Object[]{String.valueOf(isP2pEnabled), String.valueOf(isWifiConnected), String.valueOf(isP2pServiceExist)});
        if (!isP2pEnabled || !isWifiConnected || isP2pServiceExist) {
            return false;
        }
        return true;
    }

    public boolean isDcAllowed() {
        DcMonitor dcMonitor = DcMonitor.getInstance();
        DcArbitra dcArbitra = DcArbitra.getInstance();
        if (dcMonitor == null || dcArbitra == null) {
            HwHiLog.e(TAG, false, "dcMonitor or dcArbitra is null", new Object[0]);
            return false;
        }
        boolean isWifiAndP2pStateAllowDc = isWifiAndP2pStateAllowDc();
        boolean isHilinkGateway = dcArbitra.isHilinkGateway();
        boolean isGameStarted = dcMonitor.isGameStarted();
        HwHiLog.i(TAG, false, "isDcConnected=%{public}s isDcAllowedByRssi=%{public}s isWifiAndP2pStateAllowDc=%{public}s isHilinkGateway=%{public}s gameStarted=%{public}s", new Object[]{String.valueOf(this.mIsDcConnected), String.valueOf(this.mIsDcAllowedByRssi), String.valueOf(isWifiAndP2pStateAllowDc), String.valueOf(isHilinkGateway), String.valueOf(isGameStarted)});
        if (this.mIsDcConnected || !isWifiAndP2pStateAllowDc || !this.mIsDcAllowedByRssi || !isHilinkGateway || !isGameStarted) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void openHilinkService() {
        HwHiLog.d(TAG, false, "openHilinkService isHilinkServiceOpened=%{public}s", new Object[]{String.valueOf(this.mHwHilinkProxyController.isOpened())});
        this.mHwHilinkProxyController.openHilinkServiceProxy(this.mModuleType.getValue(), this.mContext);
        if (this.mHwHilinkProxyController.isOpened()) {
            sendMessage(19);
        }
    }

    public void handleScreenStateChanged(boolean isScreenOn) {
        HwHiLog.d(TAG, false, "handleScreenStateChanged, isScreenOn=%{public}s", new Object[]{Boolean.valueOf(isScreenOn)});
        if (!isScreenOn) {
            this.mElapsedScreenOffTime = SystemClock.elapsedRealtime();
        } else if (this.mElapsedScreenOffTime != 0 && this.mHwHilinkProxyController.isOpened() && SystemClock.elapsedRealtime() - this.mElapsedScreenOffTime > 100000) {
            HwHiLog.d(TAG, false, "screen off > 100s, reset hilink", new Object[0]);
            sendMessage(18);
            this.mElapsedScreenOffTime = 0;
            DcController.getInstance().getDcControllerHandler().sendEmptyMessage(18);
        }
    }

    public void handleP2pConnected(String p2pGroupInterface) {
        if (TextUtils.isEmpty(p2pGroupInterface)) {
            HwHiLog.d(TAG, false, "p2p interface is empty", new Object[0]);
            return;
        }
        String macAddress = "";
        try {
            NetworkInterface p2pInterface = NetworkInterface.getByName(p2pGroupInterface);
            if (!(p2pInterface == null || p2pInterface.getHardwareAddress() == null)) {
                if (p2pInterface.getHardwareAddress().length == 6) {
                    byte[] macBytes = p2pInterface.getHardwareAddress();
                    StringBuilder builder = new StringBuilder();
                    for (byte macByte : macBytes) {
                        builder.append(String.format(Locale.ENGLISH, "%02X:", Byte.valueOf(macByte)));
                    }
                    if (builder.length() > 0) {
                        builder.deleteCharAt(builder.length() - 1);
                    }
                    HwHiLog.d(TAG, false, "p2p interface deviceAddress %{private}s", new Object[]{builder.toString()});
                    macAddress = builder.toString();
                    this.mP2pMacAddress = macAddress;
                    return;
                }
            }
            HwHiLog.d(TAG, false, "p2pInterface is null", new Object[0]);
        } catch (SocketException e) {
            HwHiLog.e(TAG, false, "SocketException exception when getLocalMacAddress", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void detectDcManager() {
        WifiManager wifiManager = this.mWifiManager;
        if (wifiManager == null) {
            HwHiLog.e(TAG, false, "wifiManager is null", new Object[0]);
            return;
        }
        this.mBssid = wifiManager.getConnectionInfo().getBSSID();
        HwHiLog.d(TAG, false, "prepare to detect hilik ability, mDiscoverRetryTimes: %{public}d", new Object[]{Integer.valueOf(this.mDiscoverRetryTimes)});
        this.mDiscoverRetryTimes++;
        HwHilinkProxyController hwHilinkProxyController = this.mHwHilinkProxyController;
        Objects.requireNonNull(hwHilinkProxyController);
        HwHilinkProxyController.ServiceFoundCallback serviceFoundCallback = new HwHilinkProxyController.ServiceFoundCallback();
        serviceFoundCallback.bssid = this.mBssid;
        serviceFoundCallback.serviceType = this.mServiceType.getValue();
        int ret = this.mHwHilinkProxyController.discover(serviceFoundCallback);
        if (ret != 0) {
            HwHiLog.d(TAG, false, "discover Service failed! ret = %{public}d", new Object[]{Integer.valueOf(ret)});
            sendMessage(21);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int parseErrcode(String payload) {
        try {
            int errCode = new JSONObject(payload).getInt("errcode");
            HwHiLog.d(TAG, false, "errcode: %{public}d", new Object[]{Integer.valueOf(errCode)});
            return errCode;
        } catch (JSONException e) {
            HwHiLog.e(TAG, false, "JSONException when parseErrcode", new Object[0]);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendDcActionFailMessage(int actionType) {
        if (actionType == 1) {
            sendMessage(23);
        } else if (actionType == 2) {
            sendMessage(26);
        } else if (actionType == 3) {
            sendMessage(28);
        } else {
            HwHiLog.d(TAG, false, "sendDcActionFailMessage unhandle actionType", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    public class ResponseCallback extends ResponseCallbackWrapper {
        private static final int ERRORCODE_MAX_REQUEST_NUM_REACHED = 9;
        private static final int ERRORCODE_NO_NETWORK = 3;
        private static final int ERRORCODE_RUNTIME = 4;
        private static final int ERRORCODE_TIMEOUT = 1;

        private ResponseCallback() {
        }

        @Override // com.huawei.hilink.framework.aidl.ResponseCallbackWrapper, com.huawei.hilink.framework.aidl.IResponseCallback
        public void onRecieveError(int errorCode) throws RemoteException {
            HwHiLog.d(DcHilinkController.TAG, false, "response recieve error : %{public}d", new Object[]{Integer.valueOf(errorCode)});
            if (errorCode == 1) {
                DcHilinkController dcHilinkController = DcHilinkController.this;
                dcHilinkController.sendDcActionFailMessage(dcHilinkController.mDcActionType);
            }
        }

        @Override // com.huawei.hilink.framework.aidl.ResponseCallbackWrapper, com.huawei.hilink.framework.aidl.IResponseCallback
        public void onRecieveResponse(int callId, String payload) throws RemoteException {
            HwHiLog.d(DcHilinkController.TAG, false, "response recieve callID : %{public}d, payload: %{private}s", new Object[]{Integer.valueOf(callId), payload});
            if (DcHilinkController.this.parseErrcode(payload) == 0) {
                DcHilinkController.this.mDcActionRetryTimes = 0;
                Handler dcHandler = DcController.getInstance().getDcControllerHandler();
                if (DcHilinkController.this.mDcActionType == 1) {
                    DcHilinkController.this.sendMessage(24);
                    dcHandler.sendMessage(dcHandler.obtainMessage(24, 0, 0, payload));
                } else if (DcHilinkController.this.mDcActionType == 2) {
                    DcHilinkController.this.sendMessage(14);
                    dcHandler.sendEmptyMessage(14);
                } else if (DcHilinkController.this.mDcActionType == 3) {
                    DcHilinkController.this.sendMessage(16);
                } else {
                    HwHiLog.d(DcHilinkController.TAG, false, "onRecieveResponse unhandle actionType", new Object[0]);
                }
            } else {
                DcHilinkController dcHilinkController = DcHilinkController.this;
                dcHilinkController.sendDcActionFailMessage(dcHilinkController.mDcActionType);
            }
        }
    }

    public void sendActionToHilink(int actionType) {
        this.mDcActionType = actionType;
        this.mDcActionRetryTimes++;
        HwHiLog.d(TAG, false, "prepare to send hilik message, mDcActionType=%{public}d mDcActionRetryTimes=%{public}d", new Object[]{Integer.valueOf(this.mDcActionType), Integer.valueOf(this.mDcActionRetryTimes)});
        String payload = buildHilinkPayload(actionType);
        if (TextUtils.isEmpty(payload)) {
            HwHiLog.d(TAG, false, "payload is null", new Object[0]);
            sendDcActionFailMessage(this.mDcActionType);
            return;
        }
        HwHiLog.d(TAG, false, "payload length:%{public}d payload:%{private}s", new Object[]{Integer.valueOf(payload.length()), payload});
        int ret = this.mHwHilinkProxyController.call(1, payload, this.mServiceType.getValue(), new ResponseCallback());
        if (ret != 0) {
            HwHiLog.e(TAG, false, "call failed! ret=%{public}d", new Object[]{Integer.valueOf(ret)});
            sendDcActionFailMessage(this.mDcActionType);
        }
    }

    private String buildHilinkPayload(int actionType) {
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        if (actionType != 1) {
            if (actionType != 2) {
                if (actionType != 3) {
                    return null;
                }
                try {
                    DcConfiguration selectedNetwork = DcArbitra.getInstance().getSelectedDcConfig();
                    if (selectedNetwork != null) {
                        if (selectedNetwork.getInterface() != null) {
                            JSONObject jsonDeviceInfo = new JSONObject();
                            jsonDeviceInfo.put("interface", selectedNetwork.getInterface());
                            jsonDeviceInfo.put("mac", this.mP2pMacAddress);
                            jsonArray.put(jsonDeviceInfo);
                            jsonObject.put("action", "disconnect");
                            jsonObject.put("sn", Settings.Secure.getString(this.mContext.getContentResolver(), "android_id"));
                            jsonObject.put("deviceinfo", jsonArray);
                        }
                    }
                    return null;
                } catch (JSONException e) {
                    HwHiLog.e(TAG, false, "Json Exception when buildHilinkPayload", new Object[0]);
                }
            } else if (!buildHilinkActionDcConnectPayload(jsonObject, jsonArray)) {
                return null;
            }
        } else if (!buildHilinkActionDcGetConfigPayload(jsonObject, jsonArray)) {
            return null;
        }
        return jsonObject.toString();
    }

    private boolean buildHilinkActionDcGetConfigPayload(JSONObject jsonObject, JSONArray jsonArray) {
        JSONObject jsonConnect = new JSONObject();
        String wifiAddr = this.mWifiManager.getConnectionInfo().getMacAddress();
        if (TextUtils.isEmpty(wifiAddr)) {
            return false;
        }
        try {
            jsonConnect.put("mac", wifiAddr.toUpperCase(Locale.ROOT));
            String p2pAddr = DcUtils.wifiAddr2p2pAddr(wifiAddr);
            if (TextUtils.isEmpty(p2pAddr)) {
                return false;
            }
            JSONObject p2pConnect = new JSONObject();
            p2pConnect.put("mac", p2pAddr);
            HwHiLog.d(TAG, false, "%{private}s: snd_mac_addr: %{private}s", new Object[]{wifiAddr, p2pAddr});
            jsonArray.put(jsonConnect);
            jsonArray.put(p2pConnect);
            jsonObject.put("action", "get");
            jsonObject.put("sn", Settings.Secure.getString(this.mContext.getContentResolver(), "android_id"));
            jsonObject.put("deviceinfo", jsonArray);
            return true;
        } catch (JSONException e) {
            HwHiLog.e(TAG, false, "JSONException when parseErrcode", new Object[0]);
        }
    }

    private boolean buildHilinkActionDcConnectPayload(JSONObject jsonObject, JSONArray jsonArray) {
        DcConfiguration dcSelectedNetwork;
        List<DcConfiguration> dcConfigList;
        if (TextUtils.isEmpty(this.mP2pMacAddress) || (dcSelectedNetwork = DcArbitra.getInstance().getSelectedDcConfig()) == null || dcSelectedNetwork.getInterface() == null || (dcConfigList = DcArbitra.getInstance().getDcConfigList()) == null || dcConfigList.size() == 0) {
            return false;
        }
        DcConfiguration wifiDcConfig = null;
        WifiInfo wifiInfo = this.mWifiManager.getConnectionInfo();
        Iterator<DcConfiguration> it = dcConfigList.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            DcConfiguration dcConfig = it.next();
            if (dcConfig.getBssid() != null && dcConfig.getBssid().equalsIgnoreCase(wifiInfo.getBSSID())) {
                wifiDcConfig = dcConfig;
                break;
            }
        }
        if (wifiDcConfig == null || wifiDcConfig.getInterface() == null) {
            return false;
        }
        try {
            JSONObject jsonObjectWifi = new JSONObject();
            JSONObject jsonObjectP2p = new JSONObject();
            jsonObjectWifi.put("interface", wifiDcConfig.getInterface());
            jsonObjectP2p.put("interface", dcSelectedNetwork.getInterface());
            jsonObjectWifi.put("mac", wifiInfo.getMacAddress().toUpperCase(Locale.ENGLISH));
            jsonObjectP2p.put("mac", this.mP2pMacAddress);
            jsonArray.put(jsonObjectWifi);
            jsonArray.put(jsonObjectP2p);
            jsonObject.put("action", "connect");
            jsonObject.put("sn", Settings.Secure.getString(this.mContext.getContentResolver(), "android_id"));
            jsonObject.put("deviceinfo", jsonArray);
            return true;
        } catch (JSONException e) {
            HwHiLog.e(TAG, false, "JSONException when parseErrcode", new Object[0]);
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logStateAndMessage(State state, Message message) {
        HwHiLog.d(TAG, false, "%{public}s: handle message: %{public}s", new Object[]{state.getClass().getSimpleName(), DcUtils.getStateAndMessageString(state, message)});
    }

    class DefaultState extends State {
        DefaultState() {
        }

        public void enter() {
            HwHiLog.d(DcHilinkController.TAG, false, "%{public}s enter.", new Object[]{getName()});
        }

        public boolean processMessage(Message message) {
            DcHilinkController.this.logStateAndMessage(this, message);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public class IdleState extends State {
        IdleState() {
        }

        public void enter() {
            HwHiLog.d(DcHilinkController.TAG, false, "%{public}s enter.", new Object[]{getName()});
            DcHilinkController.this.initHilinkControllerParams();
        }

        public boolean processMessage(Message message) {
            DcHilinkController.this.logStateAndMessage(this, message);
            int i = message.what;
            if (!(i == 0 || i == 6 || i == 18)) {
                if (i == 29) {
                    if (!DcHilinkController.this.isWifiAndP2pStateAllowDc()) {
                        HwHiLog.d(DcHilinkController.TAG, false, "p2p interface exists, delay send msg", new Object[0]);
                        DcHilinkController.this.sendMessageDelayed(36, 1000);
                    }
                    DcHilinkController.this.closeHilinkService();
                } else if (i != 36) {
                    if (i != 33) {
                        if (i != 34) {
                            return true;
                        }
                        DcHilinkController.this.mIsDcAllowedByRssi = false;
                        return true;
                    }
                }
                DcHilinkController.this.mIsDcAllowedByRssi = true;
            }
            DcHilinkController.this.mDiscoverRetryTimes = 0;
            if (DcHilinkController.this.isDcAllowed()) {
                if (DcHilinkController.this.mDcHilinkHandler != null && DcHilinkController.this.mDcHilinkHandler.hasMessages(36)) {
                    DcHilinkController.this.mDcHilinkHandler.removeMessages(36);
                }
                DcHilinkController dcHilinkController = DcHilinkController.this;
                dcHilinkController.transitionTo(dcHilinkController.mDiscoverState);
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public class DiscoverState extends State {
        DiscoverState() {
        }

        public void enter() {
            HwHiLog.d(DcHilinkController.TAG, false, "%{public}s enter.", new Object[]{getName()});
            DcHilinkController.this.mIsEnterDiscoverState = true;
            DcHilinkController.this.openHilinkService();
            DcHilinkController.this.mDcActionRetryTimes = 0;
            DcHilinkController.this.mDiscoverRetryTimes = 0;
        }

        public boolean processMessage(Message message) {
            DcHilinkController.this.logStateAndMessage(this, message);
            int i = message.what;
            if (!(i == 1 || i == 7 || i == 29 || i == 3 || i == 4)) {
                if (i == 33) {
                    DcHilinkController.this.mIsDcAllowedByRssi = true;
                } else if (i != 34) {
                    switch (i) {
                        case 18:
                            DcHilinkController.this.deferMessage(message);
                            DcHilinkController dcHilinkController = DcHilinkController.this;
                            dcHilinkController.transitionTo(dcHilinkController.mIdleState);
                            break;
                        case 19:
                            DcHilinkController.this.detectDcManager();
                            break;
                        case 20:
                            DcHilinkController dcHilinkController2 = DcHilinkController.this;
                            dcHilinkController2.transitionTo(dcHilinkController2.mActiveState);
                            break;
                        case 21:
                            if (DcHilinkController.this.mDiscoverRetryTimes > 3) {
                                DcHilinkController dcHilinkController3 = DcHilinkController.this;
                                dcHilinkController3.transitionTo(dcHilinkController3.mIdleState);
                                break;
                            } else {
                                DcHilinkController.this.sendMessageDelayed(19, 3000);
                                break;
                            }
                        default:
                            return true;
                    }
                } else {
                    DcHilinkController.this.mIsDcAllowedByRssi = false;
                }
                return true;
            }
            DcHilinkController dcHilinkController4 = DcHilinkController.this;
            dcHilinkController4.transitionTo(dcHilinkController4.mIdleState);
            return true;
        }

        public void exit() {
            DcHilinkController.this.mIsEnterDiscoverState = false;
        }
    }

    /* access modifiers changed from: package-private */
    public class ActiveState extends State {
        ActiveState() {
        }

        public void enter() {
            HwHiLog.d(DcHilinkController.TAG, false, "%{public}s enter.", new Object[]{getName()});
            DcHilinkController.this.mIsDcConfigDetected = false;
            DcHilinkController.this.mIsDcConnected = false;
            DcHilinkController.this.sendMessage(22);
        }

        public boolean processMessage(Message message) {
            DcHilinkController.this.logStateAndMessage(this, message);
            int i = message.what;
            if (!(i == 1 || i == 3)) {
                if (i == 7) {
                    handleMsgGameStop();
                } else if (i == 14) {
                    DcHilinkController.this.mIsDcConnected = true;
                } else if (i != 16) {
                    if (i != 18) {
                        if (i == 31) {
                            DcHilinkController dcHilinkController = DcHilinkController.this;
                            dcHilinkController.transitionTo(dcHilinkController.mIdleState);
                        } else if (i == 33) {
                            DcHilinkController.this.mIsDcAllowedByRssi = true;
                        } else if (i != 34) {
                            switch (i) {
                                case 10:
                                case 11:
                                case 12:
                                    break;
                                default:
                                    switch (i) {
                                        case 22:
                                            handleMsgGetDcConfig();
                                            break;
                                        case 23:
                                            handleMsgGetDcConfigFail();
                                            break;
                                        case 24:
                                            DcHilinkController.this.mIsDcConfigDetected = true;
                                            break;
                                        case 25:
                                            DcHilinkController.this.sendActionToHilink(2);
                                            break;
                                        case DcUtils.MSG_DC_CONNECT_FAIL /* 26 */:
                                            handleMsgDcConnectFail();
                                            break;
                                        case DcUtils.MSG_DC_DISCONNECT /* 27 */:
                                            DcHilinkController.this.sendActionToHilink(3);
                                            break;
                                        case DcUtils.MSG_DC_DISCONNECT_FAIL /* 28 */:
                                            handleMsgDcDisconnectFail();
                                            break;
                                        case DcUtils.MSG_WIFI_ROAMING_COMPLETED /* 29 */:
                                            break;
                                        default:
                                            return true;
                                    }
                            }
                        } else {
                            DcHilinkController.this.mIsDcAllowedByRssi = false;
                        }
                    }
                    DcHilinkController.this.deferMessage(message);
                    DcHilinkController dcHilinkController2 = DcHilinkController.this;
                    dcHilinkController2.transitionTo(dcHilinkController2.mIdleState);
                } else {
                    DcMonitor dcMonitor = DcMonitor.getInstance();
                    if (dcMonitor != null && dcMonitor.isGameStarted()) {
                        DcHilinkController.this.sendMessage(6);
                    }
                }
                return true;
            }
            DcHilinkController dcHilinkController3 = DcHilinkController.this;
            dcHilinkController3.transitionTo(dcHilinkController3.mIdleState);
            return true;
        }

        public void exit() {
            DcHilinkController.this.mIsDcConfigDetected = false;
            DcHilinkController.this.mIsDcConnected = false;
        }

        private void handleMsgDcDisconnectFail() {
            if (DcHilinkController.this.mDcActionRetryTimes < 5) {
                DcHilinkController.this.sendMessageDelayed(27, 3000);
                return;
            }
            DcController dcController = DcController.getInstance();
            if (dcController != null) {
                dcController.getDcControllerHandler().sendEmptyMessage(17);
            }
            DcMonitor dcMonitor = DcMonitor.getInstance();
            if (dcMonitor != null && dcMonitor.isGameStarted()) {
                DcHilinkController.this.sendMessage(6);
            }
            DcHilinkController dcHilinkController = DcHilinkController.this;
            dcHilinkController.transitionTo(dcHilinkController.mIdleState);
        }

        private void handleMsgDcConnectFail() {
            DcHilinkController.this.mDcChr.uploadDcHilinkConnectFailCount();
            if (DcHilinkController.this.mDcActionRetryTimes < 5) {
                DcHilinkController.this.sendMessageDelayed(25, 3000);
                return;
            }
            DcController dcController = DcController.getInstance();
            if (dcController != null) {
                dcController.getDcControllerHandler().sendEmptyMessage(15);
            }
            DcHilinkController dcHilinkController = DcHilinkController.this;
            dcHilinkController.transitionTo(dcHilinkController.mIdleState);
        }

        private void handleMsgGetDcConfigFail() {
            DcHilinkController.this.mDcChr.uploadDcGetConfigFailCount();
            if (DcHilinkController.this.mDcActionRetryTimes < 5) {
                DcHilinkController.this.sendMessageDelayed(22, 3000);
                return;
            }
            DcHilinkController dcHilinkController = DcHilinkController.this;
            dcHilinkController.transitionTo(dcHilinkController.mIdleState);
        }

        private void handleMsgGetDcConfig() {
            if (!DcHilinkController.this.mIsDcConfigDetected) {
                HwHiLog.d(DcHilinkController.TAG, false, "start to get DC config", new Object[0]);
                DcHilinkController.this.sendActionToHilink(1);
            }
        }

        private void handleMsgGameStop() {
            if (!DcHilinkController.this.mIsDcConnected) {
                DcHilinkController dcHilinkController = DcHilinkController.this;
                dcHilinkController.transitionTo(dcHilinkController.mIdleState);
            }
        }
    }
}
