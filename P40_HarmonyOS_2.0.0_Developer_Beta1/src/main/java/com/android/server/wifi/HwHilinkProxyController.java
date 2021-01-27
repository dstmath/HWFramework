package com.android.server.wifi;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.huawei.hilink.framework.aidl.CallRequest;
import com.huawei.hilink.framework.aidl.ConnectRequest;
import com.huawei.hilink.framework.aidl.ConnectResultCallbackWrapper;
import com.huawei.hilink.framework.aidl.DiscoverRequest;
import com.huawei.hilink.framework.aidl.HilinkServiceProxy;
import com.huawei.hilink.framework.aidl.HilinkServiceProxyState;
import com.huawei.hilink.framework.aidl.ResponseCallbackWrapper;
import com.huawei.hilink.framework.aidl.ServiceFoundCallbackWrapper;
import com.huawei.hilink.framework.aidl.ServiceRecord;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HwHilinkProxyController {
    private static final int DEFAULT_REMOTE_HILINK_PORT = -1;
    public static final int DEFAULT_REQUEST_ID = 1;
    private static final int ERRORCODE_CONNECT_FAILED = -4;
    private static final int ERRORCODE_OK = 0;
    private static final int ERRORCODE_OPEN_FAILED = -3;
    private static final int ERRORCODE_PARA_ERROR = -1;
    private static final int ERRORCODE_RESULT_ERROR = -2;
    private static final int HILINK_CALLBACKLIST_MAX_SIZE = 10;
    private static final String HILINK_ENTERPRISE_ROUTER = "enterprise";
    private static final String HILINK_FAMILY_MASTER_ROUTER = "homeCenter";
    private static final int RADIX_HEX = 16;
    private static final String TAG = "HwHilinkProxyController";
    private static HwHilinkProxyController sHwHilinkProxyController;
    private ArrayList<IHwHilinkCallback> mHilinkCallbackList = new ArrayList<>();
    private HilinkServiceProxy mHilinkServiceProxy;
    private boolean mIsConnectInterfaceCalled = false;
    private boolean mIsEnterpriseRouter = false;
    private boolean mIsHilinkAddrFound = false;
    private boolean mIsMasterRouterConnected = false;
    private boolean mIsSlaveRouterConnected = false;
    private final Object mLock = new Object();
    private Set<String> mReferenceSet = new HashSet();
    private String mRemoteHilinkIp = "";
    private int mRemoteHilinkPort = -1;

    private HwHilinkProxyController() {
    }

    public static synchronized HwHilinkProxyController getInstance() {
        HwHilinkProxyController hwHilinkProxyController;
        synchronized (HwHilinkProxyController.class) {
            if (sHwHilinkProxyController == null) {
                sHwHilinkProxyController = new HwHilinkProxyController();
            }
            hwHilinkProxyController = sHwHilinkProxyController;
        }
        return hwHilinkProxyController;
    }

    /* access modifiers changed from: private */
    public class HwHilinkServiceProxyState implements HilinkServiceProxyState {
        private static final int STATE_DTLS_FAILURE = 1;

        private HwHilinkServiceProxyState() {
        }

        @Override // com.huawei.hilink.framework.aidl.HilinkServiceProxyState
        public void onProxyReady(int state) {
            boolean z = true;
            HwHiLog.d(HwHilinkProxyController.TAG, false, "onProxyReady, state=%{public}d", new Object[]{Integer.valueOf(state)});
            resetParams();
            HwHilinkProxyController hwHilinkProxyController = HwHilinkProxyController.this;
            if (state != 0) {
                z = false;
            }
            hwHilinkProxyController.mIsMasterRouterConnected = z;
            if (state != 0) {
                synchronized (HwHilinkProxyController.this.mLock) {
                    if (HwHilinkProxyController.this.mHilinkServiceProxy != null) {
                        HwHilinkProxyController.this.mHilinkServiceProxy.close();
                        HwHilinkProxyController.this.mHilinkServiceProxy = null;
                        HwHilinkProxyController.this.mReferenceSet.clear();
                    }
                }
            }
            synchronized (HwHilinkProxyController.this.mLock) {
                Iterator it = HwHilinkProxyController.this.mHilinkCallbackList.iterator();
                while (it.hasNext()) {
                    ((IHwHilinkCallback) it.next()).onProxyReadyStateChanged(state);
                }
            }
        }

        private void resetParams() {
            HwHilinkProxyController.this.mIsMasterRouterConnected = false;
            HwHilinkProxyController.this.mIsSlaveRouterConnected = false;
            HwHilinkProxyController.this.mIsHilinkAddrFound = false;
            HwHilinkProxyController.this.mIsConnectInterfaceCalled = false;
            HwHilinkProxyController.this.mIsEnterpriseRouter = false;
            HwHilinkProxyController.this.mRemoteHilinkIp = "";
            HwHilinkProxyController.this.mRemoteHilinkPort = -1;
        }

        @Override // com.huawei.hilink.framework.aidl.HilinkServiceProxyState
        public void onProxyLost() {
            HwHiLog.d(HwHilinkProxyController.TAG, false, "onProxyLost enter", new Object[0]);
            resetParams();
            synchronized (HwHilinkProxyController.this.mLock) {
                HwHilinkProxyController.this.mHilinkServiceProxy = null;
                HwHilinkProxyController.this.mReferenceSet.clear();
            }
            synchronized (HwHilinkProxyController.this.mLock) {
                Iterator it = HwHilinkProxyController.this.mHilinkCallbackList.iterator();
                while (it.hasNext()) {
                    ((IHwHilinkCallback) it.next()).onProxyLostStateChanged();
                }
            }
        }

        @Override // com.huawei.hilink.framework.aidl.HilinkServiceProxyState
        public void onConnectionState(int state) {
            HwHiLog.d(HwHilinkProxyController.TAG, false, "onConnectionState state=%{public}d", new Object[]{Integer.valueOf(state)});
            if (state == 1) {
                synchronized (HwHilinkProxyController.this.mLock) {
                    if (HwHilinkProxyController.this.mHilinkServiceProxy != null) {
                        HwHilinkProxyController.this.mHilinkServiceProxy.close();
                        HwHilinkProxyController.this.mHilinkServiceProxy = null;
                        HwHilinkProxyController.this.mReferenceSet.clear();
                    }
                    Iterator it = HwHilinkProxyController.this.mHilinkCallbackList.iterator();
                    while (it.hasNext()) {
                        ((IHwHilinkCallback) it.next()).onConnectionStateChanged(state);
                    }
                }
            }
        }
    }

    public enum HwHilinkServiceType {
        DC("dcMngr"),
        HIGAME("appawareMngr"),
        WIFICHR("routerphone"),
        UNKNOWN("unknown");
        
        private String mServiceType;

        private HwHilinkServiceType(String serviceType) {
            this.mServiceType = serviceType;
        }

        public String getValue() {
            return this.mServiceType;
        }
    }

    public enum HwHilinkModuleType {
        DC("DC"),
        HIGAME("HIGAME"),
        WIFICHR("WIFICHR"),
        UNKONWN("UNKNOWN");
        
        private String mModuleType;

        private HwHilinkModuleType(String moduleType) {
            this.mModuleType = moduleType;
        }

        public String getValue() {
            return this.mModuleType;
        }
    }

    public boolean isOpened() {
        return this.mIsMasterRouterConnected;
    }

    public boolean isEnterpriseRouter() {
        return this.mIsEnterpriseRouter;
    }

    public boolean isRegisterHilinkCallback(IHwHilinkCallback callback) {
        synchronized (this.mLock) {
            if (callback == null) {
                HwHiLog.e(TAG, false, "Callback null", new Object[0]);
                return false;
            } else if (this.mHilinkCallbackList.size() >= 10) {
                HwHiLog.e(TAG, false, "mHilinkCallbackList size full", new Object[0]);
                return false;
            } else if (this.mHilinkCallbackList.contains(callback)) {
                HwHiLog.d(TAG, false, "Callback has in list, do not register again", new Object[0]);
                return true;
            } else {
                this.mHilinkCallbackList.add(callback);
                return true;
            }
        }
    }

    public void removeReferenceModule(String module) {
        synchronized (this.mLock) {
            this.mReferenceSet.remove(module);
            HwHiLog.d(TAG, false, "remove module=%{public}s, referenceCnt=%{public}d", new Object[]{module, Integer.valueOf(this.mReferenceSet.size())});
        }
    }

    public void clearReferenceModule() {
        synchronized (this.mLock) {
            this.mReferenceSet.clear();
        }
    }

    public void closeHilinkServiceProxy() {
        synchronized (this.mLock) {
            HwHiLog.d(TAG, false, "closeHilinkService, referenceCnt=%{public}d", new Object[]{Integer.valueOf(this.mReferenceSet.size())});
            if (this.mHilinkServiceProxy != null && this.mReferenceSet.size() == 0) {
                this.mHilinkServiceProxy.close();
                this.mHilinkServiceProxy = null;
            }
        }
    }

    public void openHilinkServiceProxy(String moduleType, Context context) {
        synchronized (this.mLock) {
            this.mReferenceSet.add(moduleType);
            HwHiLog.d(TAG, false, "add module=%{public}s, referenceCnt=%{public}d", new Object[]{moduleType, Integer.valueOf(this.mReferenceSet.size())});
            if (this.mHilinkServiceProxy == null) {
                HwHiLog.d(TAG, false, "real open hilink service", new Object[0]);
                this.mHilinkServiceProxy = new HilinkServiceProxy(context, new HwHilinkServiceProxyState());
            }
        }
    }

    private boolean isConnectedRouterMacAddr(String macAddr, String macGap, String bssid) {
        long macAddrLong;
        if (!TextUtils.isEmpty(macAddr) && !TextUtils.isEmpty(macGap)) {
            if (!TextUtils.isEmpty(bssid)) {
                try {
                    long macGapLong = Long.parseLong(macGap);
                    try {
                        macAddrLong = Long.parseLong(macAddr.replace(":", ""), 16);
                    } catch (NumberFormatException e) {
                        HwHiLog.e(TAG, false, "Long prase gap or addr or bssid string exception happened", new Object[0]);
                    }
                    try {
                        long bssidLong = Long.parseLong(bssid.replace(":", ""), 16);
                        HwHiLog.d(TAG, false, "realGap=%{public}d, macGapL=%{public}d", new Object[]{Long.valueOf(bssidLong - macAddrLong), Long.valueOf(macGapLong)});
                        return 0 <= bssidLong - macAddrLong && bssidLong - macAddrLong < macGapLong;
                    } catch (NumberFormatException e2) {
                        HwHiLog.e(TAG, false, "Long prase gap or addr or bssid string exception happened", new Object[0]);
                    }
                } catch (NumberFormatException e3) {
                    HwHiLog.e(TAG, false, "Long prase gap or addr or bssid string exception happened", new Object[0]);
                }
            }
        }
        HwHiLog.e(TAG, false, "macAddr or macGap or bssid is empty, macGap=%{public}s", new Object[]{macGap});
        return false;
    }

    private String getJsonObjValue(JSONObject jsonObject, String key) {
        if (jsonObject == null || TextUtils.isEmpty(key)) {
            HwHiLog.e(TAG, false, "getJsonObjValue para is invalid", new Object[0]);
            return "";
        }
        try {
            return jsonObject.getString(key);
        } catch (JSONException e) {
            HwHiLog.e(TAG, false, "JSONException when getJsonObjValue", new Object[0]);
            return "";
        }
    }

    private JSONObject getPayloadJsonObj(JSONObject jsonObject, String key) {
        if (jsonObject == null || TextUtils.isEmpty(key)) {
            HwHiLog.e(TAG, false, "getPayloadJsonObj para is invalid", new Object[0]);
            return null;
        }
        try {
            return jsonObject.getJSONObject(key);
        } catch (JSONException e) {
            HwHiLog.e(TAG, false, "JSONException when parse key=%{public}s", new Object[]{key});
            return null;
        }
    }

    private boolean isJsonKeyValueMatch(JSONArray jsonArray, String key, String value) {
        if (jsonArray == null || TextUtils.isEmpty(key) || value == null) {
            HwHiLog.e(TAG, false, "jsonArray or key is invalid", new Object[0]);
            return false;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                if (getJsonObjValue(jsonArray.getJSONObject(i), key).equalsIgnoreCase(value)) {
                    return true;
                }
            } catch (JSONException e) {
                HwHiLog.e(TAG, false, "JSONException when isHomeCenterValueFound", new Object[0]);
            }
        }
        return false;
    }

    private JSONArray getPayloadJsonArray(JSONObject jsonObject, String key) {
        if (jsonObject == null || TextUtils.isEmpty(key)) {
            HwHiLog.e(TAG, false, "getPayloadJsonArray para is invalid", new Object[0]);
            return null;
        }
        try {
            return jsonObject.getJSONArray(key);
        } catch (JSONException e) {
            HwHiLog.e(TAG, false, "JSONException when parse key=%{public}s", new Object[]{key});
            return null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNeedConnectCoap(ServiceRecord serviceRecord, String bssid) {
        if (serviceRecord == null || TextUtils.isEmpty(bssid)) {
            return false;
        }
        if (this.mIsHilinkAddrFound) {
            HwHiLog.d(TAG, false, "2_0_version hilinkip already found", new Object[0]);
            return false;
        }
        try {
            JSONObject payloadJsonObj = new JSONObject(serviceRecord.getPayload());
            JSONObject devInfoJsonObj = getPayloadJsonObj(payloadJsonObj, "devInfo");
            JSONArray servicesJsonArray = getPayloadJsonArray(payloadJsonObj, "services");
            boolean isFamilyMasterRouter = isJsonKeyValueMatch(servicesJsonArray, "sid", HILINK_FAMILY_MASTER_ROUTER);
            this.mIsEnterpriseRouter = isJsonKeyValueMatch(servicesJsonArray, "st", HILINK_ENTERPRISE_ROUTER);
            HwHiLog.d(TAG, false, "isEnterpriseRouter=%{public}s", new Object[]{String.valueOf(this.mIsEnterpriseRouter)});
            String mac = getJsonObjValue(devInfoJsonObj, "mac");
            if (TextUtils.isEmpty(mac) || !isConnectedRouterMacAddr(mac, getJsonObjValue(devInfoJsonObj, "macNum"), bssid)) {
                if (isFamilyMasterRouter) {
                    this.mRemoteHilinkIp = serviceRecord.getRemoteIP();
                    this.mRemoteHilinkPort = serviceRecord.getRemotePort();
                    HwHiLog.d(TAG, false, "1_0_version master remoteIp found", new Object[0]);
                }
                return false;
            }
            this.mRemoteHilinkIp = serviceRecord.getRemoteIP();
            this.mRemoteHilinkPort = serviceRecord.getRemotePort();
            this.mIsHilinkAddrFound = true;
            if (!isFamilyMasterRouter) {
                HwHiLog.d(TAG, false, "2_0_version slave remoteIp found", new Object[0]);
                return true;
            }
            HwHiLog.d(TAG, false, "2_0_version master remoteIp found", new Object[0]);
            return false;
        } catch (JSONException e) {
            HwHiLog.e(TAG, false, "JSONException when getPayload", new Object[0]);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public class ConnectCallback extends ConnectResultCallbackWrapper {
        private ConnectCallback() {
        }

        @Override // com.huawei.hilink.framework.aidl.ConnectResultCallbackWrapper, com.huawei.hilink.framework.aidl.IConnectResultCallback
        public void onConnectSuccess(int requestId) throws RemoteException {
            HwHiLog.d(HwHilinkProxyController.TAG, false, "onConnectSuccess: requestId=%{public}d", new Object[]{Integer.valueOf(requestId)});
            HwHilinkProxyController.this.mIsSlaveRouterConnected = true;
            HwHilinkProxyController.this.mIsConnectInterfaceCalled = false;
            synchronized (HwHilinkProxyController.this.mLock) {
                Iterator it = HwHilinkProxyController.this.mHilinkCallbackList.iterator();
                while (it.hasNext()) {
                    ((IHwHilinkCallback) it.next()).onConnectSuccessResult(requestId);
                }
            }
        }

        @Override // com.huawei.hilink.framework.aidl.ConnectResultCallbackWrapper, com.huawei.hilink.framework.aidl.IConnectResultCallback
        public void onConnectFailed(int requestId, int errorCode) throws RemoteException {
            HwHiLog.e(HwHilinkProxyController.TAG, false, "onConnectFailed error=%{public}d requestId=%{public}d", new Object[]{Integer.valueOf(errorCode), Integer.valueOf(requestId)});
            HwHilinkProxyController.this.mIsSlaveRouterConnected = false;
            HwHilinkProxyController.this.mIsConnectInterfaceCalled = false;
            synchronized (HwHilinkProxyController.this.mLock) {
                Iterator it = HwHilinkProxyController.this.mHilinkCallbackList.iterator();
                while (it.hasNext()) {
                    ((IHwHilinkCallback) it.next()).onConnectFailedResult(requestId, errorCode);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int connect(int requestId, String serviceType) {
        HwHiLog.d(TAG, false, "connect serviceType=%{public}s isConnectedCalled=%{public}s,slaveConnected=%{public}s, requestId=%{public}d", new Object[]{serviceType, String.valueOf(this.mIsConnectInterfaceCalled), String.valueOf(this.mIsSlaveRouterConnected), Integer.valueOf(requestId)});
        if (this.mIsConnectInterfaceCalled || this.mIsSlaveRouterConnected) {
            return 0;
        }
        if (TextUtils.isEmpty(this.mRemoteHilinkIp) || this.mRemoteHilinkPort < 0 || this.mHilinkServiceProxy == null || requestId == 0 || TextUtils.isEmpty(serviceType)) {
            HwHiLog.d(TAG, false, "connect para is invalid", new Object[0]);
            return -1;
        }
        ConnectRequest.Builder builder = new ConnectRequest.Builder();
        builder.setRequestID(requestId);
        builder.setRemoteIP(this.mRemoteHilinkIp).setRemotePort(this.mRemoteHilinkPort);
        if (!this.mIsEnterpriseRouter) {
            builder.setGatewayType(1);
        } else {
            builder.setGatewayType(2);
        }
        builder.setServiceType(serviceType);
        ConnectRequest request = builder.build();
        this.mIsConnectInterfaceCalled = true;
        HwHiLog.d(TAG, false, "ip=%{private}s, port=%{private}d", new Object[]{this.mRemoteHilinkIp, Integer.valueOf(this.mRemoteHilinkPort)});
        int ret = this.mHilinkServiceProxy.connect(request, new ConnectCallback());
        if (ret == 0) {
            return 0;
        }
        HwHiLog.e(TAG, false, "connect failed ret=%{public}d", new Object[]{Integer.valueOf(ret)});
        return -2;
    }

    public class ServiceFoundCallback extends ServiceFoundCallbackWrapper {
        private static final int ERRORCODE_TIMEOUT = 1;
        public String bssid = "";
        public int requestId = -1;
        public String serviceType = "";

        public ServiceFoundCallback() {
        }

        @Override // com.huawei.hilink.framework.aidl.ServiceFoundCallbackWrapper, com.huawei.hilink.framework.aidl.IServiceFoundCallback
        public void onFoundError(int errorCode) throws RemoteException {
            HwHiLog.e(HwHilinkProxyController.TAG, false, "service found error=%{public}d", new Object[]{Integer.valueOf(errorCode)});
            if (errorCode != 1) {
                synchronized (HwHilinkProxyController.this.mLock) {
                    Iterator it = HwHilinkProxyController.this.mHilinkCallbackList.iterator();
                    while (it.hasNext()) {
                        ((IHwHilinkCallback) it.next()).onConnectFailedResult(this.requestId, errorCode);
                    }
                }
                return;
            }
            if (HwHilinkProxyController.this.mIsSlaveRouterConnected || (HwHilinkProxyController.this.mIsHilinkAddrFound && HwHilinkProxyController.this.mIsMasterRouterConnected)) {
                HwHiLog.d(HwHilinkProxyController.TAG, false, "found timeout but msg already send", new Object[0]);
            } else {
                HwHiLog.d(HwHilinkProxyController.TAG, false, "1_0_ver found timeout but default master connected", new Object[0]);
                if (HwHilinkProxyController.this.mIsEnterpriseRouter) {
                    HwHiLog.d(HwHilinkProxyController.TAG, false, "EnterpriseRouter 1_0_ver, no need re-invoke callback", new Object[0]);
                    HwHilinkProxyController.this.mIsHilinkAddrFound = false;
                    return;
                }
                synchronized (HwHilinkProxyController.this.mLock) {
                    Iterator it2 = HwHilinkProxyController.this.mHilinkCallbackList.iterator();
                    while (it2.hasNext()) {
                        ((IHwHilinkCallback) it2.next()).onConnectSuccessResult(this.requestId);
                    }
                }
            }
            HwHilinkProxyController.this.mIsHilinkAddrFound = false;
        }

        @Override // com.huawei.hilink.framework.aidl.ServiceFoundCallbackWrapper, com.huawei.hilink.framework.aidl.IServiceFoundCallback
        public void onFoundService(ServiceRecord serviceRecord) throws RemoteException {
            HwHiLog.d(HwHilinkProxyController.TAG, false, "onFoundService, serviceType=%{public}s", new Object[]{this.serviceType});
            if (serviceRecord == null) {
                HwHiLog.d(HwHilinkProxyController.TAG, false, "service Record is null", new Object[0]);
                synchronized (HwHilinkProxyController.this.mLock) {
                    Iterator it = HwHilinkProxyController.this.mHilinkCallbackList.iterator();
                    while (it.hasNext()) {
                        ((IHwHilinkCallback) it.next()).onConnectFailedResult(this.requestId, -1);
                    }
                }
            } else if (HwHilinkProxyController.this.mIsSlaveRouterConnected) {
                HwHiLog.d(HwHilinkProxyController.TAG, false, "2_0_ver slave already connected", new Object[0]);
                synchronized (HwHilinkProxyController.this.mLock) {
                    Iterator it2 = HwHilinkProxyController.this.mHilinkCallbackList.iterator();
                    while (it2.hasNext()) {
                        ((IHwHilinkCallback) it2.next()).onConnectSuccessResult(this.requestId);
                    }
                }
            } else {
                HwHiLog.d(HwHilinkProxyController.TAG, false, "found payload: %{private}s", new Object[]{serviceRecord.getPayload()});
                if (HwHilinkProxyController.this.isNeedConnectCoap(serviceRecord, this.bssid)) {
                    if (HwHilinkProxyController.this.connect(this.requestId, this.serviceType) != 0) {
                        HwHiLog.d(HwHilinkProxyController.TAG, false, "connect Service failed", new Object[0]);
                    }
                } else if (HwHilinkProxyController.this.mIsHilinkAddrFound && HwHilinkProxyController.this.mIsMasterRouterConnected && !HwHilinkProxyController.this.mIsConnectInterfaceCalled) {
                    HwHiLog.d(HwHilinkProxyController.TAG, false, "2_0_ver but master default connected", new Object[0]);
                    synchronized (HwHilinkProxyController.this.mLock) {
                        Iterator it3 = HwHilinkProxyController.this.mHilinkCallbackList.iterator();
                        while (it3.hasNext()) {
                            ((IHwHilinkCallback) it3.next()).onConnectSuccessResult(this.requestId);
                        }
                    }
                } else if (HwHilinkProxyController.this.mIsEnterpriseRouter) {
                    HwHiLog.d(HwHilinkProxyController.TAG, false, "EnterpriseRouter default 1_0_ver", new Object[0]);
                    synchronized (HwHilinkProxyController.this.mLock) {
                        Iterator it4 = HwHilinkProxyController.this.mHilinkCallbackList.iterator();
                        while (it4.hasNext()) {
                            ((IHwHilinkCallback) it4.next()).onConnectSuccessResult(this.requestId);
                        }
                    }
                } else {
                    HwHiLog.d(HwHilinkProxyController.TAG, false, "not 2_0_ver, wait for discover timeout", new Object[0]);
                }
            }
        }
    }

    public int discover(ServiceFoundCallback serviceFoundCallback) {
        int ret;
        if (serviceFoundCallback == null) {
            return -1;
        }
        if (!this.mIsMasterRouterConnected) {
            HwHiLog.d(TAG, false, "hilink open failed, don't discover", new Object[0]);
            return -3;
        }
        HwHiLog.d(TAG, false, "discover serviceType=%{public}s", new Object[]{serviceFoundCallback.serviceType});
        DiscoverRequest.Builder builder = new DiscoverRequest.Builder();
        builder.setServiceType("st=" + serviceFoundCallback.serviceType);
        DiscoverRequest request = builder.build();
        HilinkServiceProxy hilinkServiceProxy = this.mHilinkServiceProxy;
        if (hilinkServiceProxy == null || request == null || (ret = hilinkServiceProxy.discover(request, serviceFoundCallback)) == 0) {
            return 0;
        }
        HwHiLog.e(TAG, false, "discover failed ret=%{public}d", new Object[]{Integer.valueOf(ret)});
        return -2;
    }

    public int call(int requestId, String payload, String serviceType, ResponseCallbackWrapper responseCallback) {
        HilinkServiceProxy hilinkServiceProxy;
        int ret;
        if (TextUtils.isEmpty(this.mRemoteHilinkIp) || this.mRemoteHilinkPort < 0 || requestId == 0 || responseCallback == null || TextUtils.isEmpty(serviceType)) {
            HwHiLog.d(TAG, false, "call para is invalid", new Object[0]);
            return -1;
        } else if (this.mIsMasterRouterConnected || this.mIsSlaveRouterConnected) {
            HwHiLog.d(TAG, false, "call serviceType=%{public}s, ip=%{private}s, port=%{private}d,requestId=%{public}d", new Object[]{serviceType, this.mRemoteHilinkIp, Integer.valueOf(this.mRemoteHilinkPort), Integer.valueOf(requestId)});
            CallRequest.Builder builder = new CallRequest.Builder();
            builder.setRequestID(requestId);
            builder.setServiceID(serviceType);
            builder.setMethod(1);
            builder.setRemoteIP(this.mRemoteHilinkIp).setRemotePort(this.mRemoteHilinkPort);
            builder.setPayload(payload);
            CallRequest request = builder.build();
            if (request == null || (hilinkServiceProxy = this.mHilinkServiceProxy) == null || (ret = hilinkServiceProxy.call(request, responseCallback)) == 0) {
                return 0;
            }
            HwHiLog.e(TAG, false, "call failed ret=%{public}d", new Object[]{Integer.valueOf(ret)});
            return -2;
        } else {
            HwHiLog.d(TAG, false, "coap create failed, don't call", new Object[0]);
            return -4;
        }
    }
}
