package com.android.internal.telephony.dataconnection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkFactory;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.telephony.Rlog;
import android.util.LocalLog;
import android.util.Log;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.HwVSimDcSwitchAsyncChannel;
import com.android.internal.telephony.vsim.HwVSimLog;
import java.util.HashMap;

public class HwVSimDctController extends Handler {
    private static final int EVENT_DATA_ATTACHED = 500;
    private static final int EVENT_DATA_DETACHED = 600;
    private static final int EVENT_EXECUTE_ALL_REQUESTS = 102;
    private static final int EVENT_EXECUTE_REQUEST = 101;
    private static final int EVENT_PROCESS_REQUESTS = 100;
    private static final int EVENT_RELEASE_ALL_REQUESTS = 104;
    private static final int EVENT_RELEASE_REQUEST = 103;
    /* access modifiers changed from: private */
    public static boolean HWDBG = (Log.HWLog || (Log.HWModuleLog && Log.isLoggable(LOG_TAG, 3)));
    private static final boolean HWLOGW_E = true;
    private static final String LOG_TAG = "VSimDctController";
    private static final Object mLock = new Object();
    private static HwVSimDctController sDctController;
    /* access modifiers changed from: private */
    public HwVSimDcSwitchAsyncChannel mDcSwitchAsyncChannel;
    private Handler mDcSwitchStateHandler;
    private HwVSimDcSwitchStateMachine mDcSwitchStateMachine;
    private NetworkFactory mNetworkFactory;
    private Messenger mNetworkFactoryMessenger;
    private NetworkCapabilities mNetworkFilter;
    /* access modifiers changed from: private */
    public Phone mPhone;
    private HashMap<Integer, HwVSimDcSwitchAsyncChannel.RequestInfo> mRequestInfos = new HashMap<>();
    private Handler mRspHandler = new Handler() {
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 500) {
                HwVSimDctController.logd("EVENT_PHONE2_DATA_ATTACH.");
                HwVSimDctController.this.mDcSwitchAsyncChannel.notifyDataAttached();
            } else if (i == HwVSimDctController.EVENT_DATA_DETACHED) {
                HwVSimDctController.logd("EVENT_PHONE2_DATA_DETACH.");
                HwVSimDctController.this.mDcSwitchAsyncChannel.notifyDataDetached();
            }
        }
    };

    private class VSimNetworkFactory extends NetworkFactory {
        private static final int MAX_LOG_LINES_PER_REQUEST = 50;

        public VSimNetworkFactory(Looper l, Context c, String TAG, Phone phone, NetworkCapabilities nc) {
            super(l, c, TAG, nc);
        }

        /* access modifiers changed from: protected */
        public void needNetworkFor(NetworkRequest networkRequest, int score) {
            log("Cellular needs Network for " + networkRequest);
            DcTracker dcTracker = HwVSimDctController.this.mPhone.mDcTracker;
            String apn = HwVSimDctController.this.apnForNetworkRequest(networkRequest);
            if (dcTracker.isApnSupported(apn)) {
                int unused = HwVSimDctController.this.requestNetwork(networkRequest, dcTracker.getApnPriority(apn), new LocalLog(50));
                return;
            }
            log("Unsupported APN");
        }

        /* access modifiers changed from: protected */
        public void releaseNetworkFor(NetworkRequest networkRequest) {
            log("Cellular releasing Network for " + networkRequest);
            if (HwVSimDctController.this.mPhone.mDcTracker.isApnSupported(HwVSimDctController.this.apnForNetworkRequest(networkRequest))) {
                int unused = HwVSimDctController.this.releaseNetwork(networkRequest);
            } else {
                log("Unsupported APN");
            }
        }

        /* access modifiers changed from: protected */
        public void log(String s) {
            if (HwVSimDctController.HWDBG) {
                Rlog.d(HwVSimDctController.LOG_TAG, "[TNF " + HwVSimDctController.this.mPhone.getSubId() + "]" + s);
            }
        }
    }

    public static HwVSimDctController makeDctController(Phone phone, Looper looper) {
        HwVSimDctController hwVSimDctController;
        synchronized (mLock) {
            if (sDctController == null) {
                sDctController = new HwVSimDctController(phone, looper);
                logd("makeDctController: X sDctController=" + sDctController);
                hwVSimDctController = sDctController;
            } else {
                throw new RuntimeException("VSimDctController already created");
            }
        }
        return hwVSimDctController;
    }

    public static HwVSimDctController getInstance() {
        HwVSimDctController hwVSimDctController;
        synchronized (mLock) {
            if (sDctController != null) {
                hwVSimDctController = sDctController;
            } else {
                throw new RuntimeException("DctController.getInstance can't be called before makeDCTController()");
            }
        }
        return hwVSimDctController;
    }

    private HwVSimDctController(Phone phone, Looper looper) {
        super(looper);
        logd("DctController()");
        this.mPhone = phone;
        Phone phone2 = this.mPhone;
        this.mDcSwitchStateMachine = new HwVSimDcSwitchStateMachine(this, phone2, "DcSwitchStateMachine-" + 2, 2);
        this.mDcSwitchStateMachine.start();
        this.mDcSwitchAsyncChannel = new HwVSimDcSwitchAsyncChannel(this.mDcSwitchStateMachine, 2);
        this.mDcSwitchStateHandler = new Handler();
        if (this.mDcSwitchAsyncChannel.fullyConnectSync(this.mPhone.getContext(), this.mDcSwitchStateHandler, this.mDcSwitchStateMachine.getHandler()) == 0) {
            logd("DctController: Connect success: " + 2);
        } else {
            loge("DctController: Could not connect to " + 2);
        }
        this.mPhone.getServiceStateTracker().registerForDataConnectionAttached(this.mRspHandler, 500, null);
        this.mPhone.getServiceStateTracker().registerForDataConnectionDetached(this.mRspHandler, EVENT_DATA_DETACHED, null);
        this.mNetworkFilter = new NetworkCapabilities();
        this.mNetworkFilter.addTransportType(0);
        this.mNetworkFilter.addCapability(1);
        this.mNetworkFilter.addCapability(2);
        this.mNetworkFilter.addCapability(12);
        VSimNetworkFactory vSimNetworkFactory = new VSimNetworkFactory(getLooper(), this.mPhone.getContext(), "VSimNetworkFactory", this.mPhone, this.mNetworkFilter);
        this.mNetworkFactory = vSimNetworkFactory;
        this.mNetworkFactory.setScoreFilter(50);
        this.mNetworkFactoryMessenger = new Messenger(this.mNetworkFactory);
        ((ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity")).registerNetworkFactory(this.mNetworkFactoryMessenger, "Telephony");
    }

    public void dispose() {
        logd("DctController.dispose");
        ((ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity")).unregisterNetworkFactory(this.mNetworkFactoryMessenger);
        this.mNetworkFactoryMessenger = null;
    }

    public void handleMessage(Message msg) {
        logd("handleMessage msg=" + msg);
        switch (msg.what) {
            case 100:
                onProcessRequest();
                return;
            case EVENT_EXECUTE_REQUEST /*101*/:
                onExecuteRequest((HwVSimDcSwitchAsyncChannel.RequestInfo) msg.obj);
                return;
            case EVENT_EXECUTE_ALL_REQUESTS /*102*/:
                onExecuteAllRequests(msg.arg1);
                return;
            case EVENT_RELEASE_REQUEST /*103*/:
                onReleaseRequest((HwVSimDcSwitchAsyncChannel.RequestInfo) msg.obj);
                return;
            case EVENT_RELEASE_ALL_REQUESTS /*104*/:
                onReleaseAllRequests(msg.arg1);
                return;
            default:
                loge("Un-handled message [" + msg.what + "]");
                return;
        }
    }

    /* access modifiers changed from: private */
    public static void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private static void loge(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    /* access modifiers changed from: private */
    public int requestNetwork(NetworkRequest request, int priority, LocalLog l) {
        logd("requestNetwork request=" + request + ", priority=" + priority);
        StringBuilder sb = new StringBuilder();
        sb.append("Dctc.requestNetwork, priority=");
        sb.append(priority);
        l.log(sb.toString());
        this.mRequestInfos.put(Integer.valueOf(request.requestId), new HwVSimDcSwitchAsyncChannel.RequestInfo(request, priority, l));
        processRequests();
        return 1;
    }

    /* access modifiers changed from: private */
    public int releaseNetwork(NetworkRequest request) {
        HwVSimDcSwitchAsyncChannel.RequestInfo requestInfo = this.mRequestInfos.get(Integer.valueOf(request.requestId));
        logd("releaseNetwork request=" + request + ", requestInfo=" + requestInfo);
        if (requestInfo != null) {
            requestInfo.log("DctController.releaseNetwork");
        }
        this.mRequestInfos.remove(Integer.valueOf(request.requestId));
        releaseRequest(requestInfo);
        processRequests();
        return 1;
    }

    /* access modifiers changed from: package-private */
    public void processRequests() {
        logd("processRequests");
        sendMessage(obtainMessage(100));
    }

    /* access modifiers changed from: package-private */
    public void executeRequest(HwVSimDcSwitchAsyncChannel.RequestInfo request) {
        logd("executeRequest, request= " + request);
        sendMessage(obtainMessage(EVENT_EXECUTE_REQUEST, request));
    }

    /* access modifiers changed from: package-private */
    public void executeAllRequests(int phoneId) {
        logd("executeAllRequests, phone:" + phoneId);
        sendMessage(obtainMessage(EVENT_EXECUTE_ALL_REQUESTS, phoneId, 0));
    }

    /* access modifiers changed from: package-private */
    public void releaseRequest(HwVSimDcSwitchAsyncChannel.RequestInfo request) {
        logd("releaseRequest, request= " + request);
        sendMessage(obtainMessage(EVENT_RELEASE_REQUEST, request));
    }

    /* access modifiers changed from: package-private */
    public void releaseAllRequests(int phoneId) {
        logd("releaseAllRequests, phone:" + phoneId);
        sendMessage(obtainMessage(EVENT_RELEASE_ALL_REQUESTS, phoneId, 0));
    }

    private void onProcessRequest() {
        for (Integer num : this.mRequestInfos.keySet()) {
            HwVSimDcSwitchAsyncChannel.RequestInfo requestInfo = this.mRequestInfos.get(num);
            if (!requestInfo.executed) {
                this.mDcSwitchAsyncChannel.connect(requestInfo);
            }
        }
    }

    private void onExecuteRequest(HwVSimDcSwitchAsyncChannel.RequestInfo requestInfo) {
        if (!requestInfo.executed && this.mRequestInfos.containsKey(Integer.valueOf(requestInfo.request.requestId))) {
            logd("onExecuteRequest request=" + requestInfo);
            requestInfo.log("DctController.onExecuteRequest - executed=" + requestInfo.executed);
            requestInfo.executed = true;
            String apn = apnForNetworkRequest(requestInfo.request);
            ApnContext apnContext = (ApnContext) this.mPhone.mDcTracker.mApnContexts.get(apn);
            logd("DcTracker.incApnRefCount on " + apn + " found " + apnContext);
            if (apnContext != null) {
                apnContext.requestNetwork(requestInfo.getNetworkRequest(), requestInfo.getLog());
            }
        }
    }

    private void onExecuteAllRequests(int phoneId) {
        logd("onExecuteAllRequests phoneId=" + phoneId);
        for (Integer num : this.mRequestInfos.keySet()) {
            onExecuteRequest(this.mRequestInfos.get(num));
        }
    }

    private void onReleaseRequest(HwVSimDcSwitchAsyncChannel.RequestInfo requestInfo) {
        logd("onReleaseRequest request=" + requestInfo);
        if (requestInfo != null) {
            requestInfo.log("DctController.onReleaseRequest");
            if (requestInfo.executed) {
                String apn = apnForNetworkRequest(requestInfo.request);
                ApnContext apnContext = (ApnContext) this.mPhone.mDcTracker.mApnContexts.get(apn);
                logd("DcTracker.decApnRefCount on " + apn + " found " + apnContext);
                if (apnContext != null) {
                    apnContext.releaseNetwork(requestInfo.getNetworkRequest(), requestInfo.getLog());
                }
                requestInfo.executed = false;
            }
        }
    }

    private void onReleaseAllRequests(int phoneId) {
        logd("onReleaseAllRequests phoneId=" + phoneId);
        for (Integer num : this.mRequestInfos.keySet()) {
            onReleaseRequest(this.mRequestInfos.get(num));
        }
    }

    /* access modifiers changed from: private */
    public String apnForNetworkRequest(NetworkRequest nr) {
        NetworkCapabilities nc = nr.networkCapabilities;
        if (nc.getTransportTypes().length > 0 && !nc.hasTransport(0)) {
            return null;
        }
        int type = -1;
        String name = null;
        boolean error = false;
        if (nc.hasCapability(12)) {
            name = "default";
            type = 0;
        }
        if (nc.hasCapability(1)) {
            if (name != null) {
                error = true;
            }
            name = "supl";
            type = 3;
        }
        if (nc.hasCapability(2)) {
            if (name != null) {
                error = true;
            }
            name = "dun";
            type = 4;
        }
        if (error) {
            loge("Multiple apn types specified in request - result is unspecified!");
        }
        if (type != -1 && name != null) {
            return name;
        }
        loge("Unsupported NetworkRequest in Telephony: nr=" + nr);
        return null;
    }
}
