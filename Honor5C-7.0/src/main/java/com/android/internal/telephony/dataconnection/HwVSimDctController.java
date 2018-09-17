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
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.dataconnection.HwVSimDcSwitchAsyncChannel.RequestInfo;
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
    private static boolean HWDBG = false;
    private static final boolean HWLOGW_E = true;
    private static final String LOG_TAG = "VSimDctController";
    private static final Object mLock = null;
    private static HwVSimDctController sDctController;
    private HwVSimDcSwitchAsyncChannel mDcSwitchAsyncChannel;
    private Handler mDcSwitchStateHandler;
    private HwVSimDcSwitchStateMachine mDcSwitchStateMachine;
    private NetworkFactory mNetworkFactory;
    private Messenger mNetworkFactoryMessenger;
    private NetworkCapabilities mNetworkFilter;
    private Phone mPhone;
    private HashMap<Integer, RequestInfo> mRequestInfos;
    private Handler mRspHandler;

    private class VSimNetworkFactory extends NetworkFactory {
        private static final int MAX_LOG_LINES_PER_REQUEST = 50;

        public VSimNetworkFactory(Looper l, Context c, String TAG, Phone phone, NetworkCapabilities nc) {
            super(l, c, TAG, nc);
            log("NetworkCapabilities: " + nc);
        }

        protected void needNetworkFor(NetworkRequest networkRequest, int score) {
            log("Cellular needs Network for " + networkRequest);
            DcTracker dcTracker = HwVSimDctController.this.mPhone.mDcTracker;
            String apn = HwVSimDctController.this.apnForNetworkRequest(networkRequest);
            if (dcTracker.isApnSupported(apn)) {
                HwVSimDctController.this.requestNetwork(networkRequest, dcTracker.getApnPriority(apn), new LocalLog(MAX_LOG_LINES_PER_REQUEST));
                return;
            }
            String str = "Unsupported APN";
            log("Unsupported APN");
        }

        protected void releaseNetworkFor(NetworkRequest networkRequest) {
            log("Cellular releasing Network for " + networkRequest);
            if (HwVSimDctController.this.mPhone.mDcTracker.isApnSupported(HwVSimDctController.this.apnForNetworkRequest(networkRequest))) {
                HwVSimDctController.this.releaseNetwork(networkRequest);
            } else {
                log("Unsupported APN");
            }
        }

        protected void log(String s) {
            if (HwVSimDctController.HWDBG) {
                Rlog.d(HwVSimDctController.LOG_TAG, "[TNF " + HwVSimDctController.this.mPhone.getSubId() + "]" + s);
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.telephony.dataconnection.HwVSimDctController.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.telephony.dataconnection.HwVSimDctController.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.dataconnection.HwVSimDctController.<clinit>():void");
    }

    public static HwVSimDctController makeDctController(Phone phone, Looper looper) {
        HwVSimDctController hwVSimDctController;
        synchronized (mLock) {
            if (sDctController != null) {
                throw new RuntimeException("VSimDctController already created");
            }
            sDctController = new HwVSimDctController(phone, looper);
            logd("makeDctController: X sDctController=" + sDctController);
            hwVSimDctController = sDctController;
        }
        return hwVSimDctController;
    }

    public static HwVSimDctController getInstance() {
        HwVSimDctController hwVSimDctController;
        synchronized (mLock) {
            if (sDctController == null) {
                throw new RuntimeException("DctController.getInstance can't be called before makeDCTController()");
            }
            hwVSimDctController = sDctController;
        }
        return hwVSimDctController;
    }

    private HwVSimDctController(Phone phone, Looper looper) {
        super(looper);
        this.mRequestInfos = new HashMap();
        this.mRspHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwVSimDctController.EVENT_DATA_ATTACHED /*500*/:
                        HwVSimDctController.logd("EVENT_PHONE2_DATA_ATTACH.");
                        HwVSimDctController.this.mDcSwitchAsyncChannel.notifyDataAttached();
                    case HwVSimDctController.EVENT_DATA_DETACHED /*600*/:
                        HwVSimDctController.logd("EVENT_PHONE2_DATA_DETACH.");
                        HwVSimDctController.this.mDcSwitchAsyncChannel.notifyDataDetached();
                    default:
                }
            }
        };
        logd("DctController()");
        this.mPhone = phone;
        this.mDcSwitchStateMachine = new HwVSimDcSwitchStateMachine(this, this.mPhone, "DcSwitchStateMachine-" + 2, 2);
        this.mDcSwitchStateMachine.start();
        this.mDcSwitchAsyncChannel = new HwVSimDcSwitchAsyncChannel(this.mDcSwitchStateMachine, 2);
        this.mDcSwitchStateHandler = new Handler();
        if (this.mDcSwitchAsyncChannel.fullyConnectSync(this.mPhone.getContext(), this.mDcSwitchStateHandler, this.mDcSwitchStateMachine.getHandler()) == 0) {
            logd("DctController: Connect success: " + 2);
        } else {
            loge("DctController: Could not connect to " + 2);
        }
        this.mPhone.getServiceStateTracker().registerForDataConnectionAttached(this.mRspHandler, EVENT_DATA_ATTACHED, null);
        this.mPhone.getServiceStateTracker().registerForDataConnectionDetached(this.mRspHandler, EVENT_DATA_DETACHED, null);
        ConnectivityManager cm = (ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity");
        this.mNetworkFilter = new NetworkCapabilities();
        this.mNetworkFilter.addTransportType(0);
        this.mNetworkFilter.addCapability(1);
        this.mNetworkFilter.addCapability(2);
        this.mNetworkFilter.addCapability(12);
        this.mNetworkFactory = new VSimNetworkFactory(getLooper(), this.mPhone.getContext(), "VSimNetworkFactory", this.mPhone, this.mNetworkFilter);
        this.mNetworkFactory.setScoreFilter(50);
        this.mNetworkFactoryMessenger = new Messenger(this.mNetworkFactory);
        cm.registerNetworkFactory(this.mNetworkFactoryMessenger, "Telephony");
    }

    public void dispose() {
        logd("DctController.dispose");
        ((ConnectivityManager) this.mPhone.getContext().getSystemService("connectivity")).unregisterNetworkFactory(this.mNetworkFactoryMessenger);
        this.mNetworkFactoryMessenger = null;
    }

    public void handleMessage(Message msg) {
        logd("handleMessage msg=" + msg);
        switch (msg.what) {
            case EVENT_PROCESS_REQUESTS /*100*/:
                onProcessRequest();
            case EVENT_EXECUTE_REQUEST /*101*/:
                onExecuteRequest((RequestInfo) msg.obj);
            case EVENT_EXECUTE_ALL_REQUESTS /*102*/:
                onExecuteAllRequests(msg.arg1);
            case EVENT_RELEASE_REQUEST /*103*/:
                onReleaseRequest((RequestInfo) msg.obj);
            case EVENT_RELEASE_ALL_REQUESTS /*104*/:
                onReleaseAllRequests(msg.arg1);
            default:
                loge("Un-handled message [" + msg.what + "]");
        }
    }

    private static void logd(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private static void loge(String s) {
        HwVSimLog.VSimLogD(LOG_TAG, s);
    }

    private int requestNetwork(NetworkRequest request, int priority, LocalLog l) {
        logd("requestNetwork request=" + request + ", priority=" + priority);
        l.log("Dctc.requestNetwork, priority=" + priority);
        this.mRequestInfos.put(Integer.valueOf(request.requestId), new RequestInfo(request, priority, l));
        processRequests();
        return 1;
    }

    private int releaseNetwork(NetworkRequest request) {
        RequestInfo requestInfo = (RequestInfo) this.mRequestInfos.get(Integer.valueOf(request.requestId));
        logd("releaseNetwork request=" + request + ", requestInfo=" + requestInfo);
        if (requestInfo != null) {
            requestInfo.log("DctController.releaseNetwork");
        }
        this.mRequestInfos.remove(Integer.valueOf(request.requestId));
        releaseRequest(requestInfo);
        processRequests();
        return 1;
    }

    void processRequests() {
        logd("processRequests");
        sendMessage(obtainMessage(EVENT_PROCESS_REQUESTS));
    }

    void executeRequest(RequestInfo request) {
        logd("executeRequest, request= " + request);
        sendMessage(obtainMessage(EVENT_EXECUTE_REQUEST, request));
    }

    void executeAllRequests(int phoneId) {
        logd("executeAllRequests, phone:" + phoneId);
        sendMessage(obtainMessage(EVENT_EXECUTE_ALL_REQUESTS, phoneId, 0));
    }

    void releaseRequest(RequestInfo request) {
        logd("releaseRequest, request= " + request);
        sendMessage(obtainMessage(EVENT_RELEASE_REQUEST, request));
    }

    void releaseAllRequests(int phoneId) {
        logd("releaseAllRequests, phone:" + phoneId);
        sendMessage(obtainMessage(EVENT_RELEASE_ALL_REQUESTS, phoneId, 0));
    }

    private void onProcessRequest() {
        for (Object obj : this.mRequestInfos.keySet()) {
            RequestInfo requestInfo = (RequestInfo) this.mRequestInfos.get(obj);
            if (!requestInfo.executed) {
                this.mDcSwitchAsyncChannel.connect(requestInfo);
            }
        }
    }

    private void onExecuteRequest(RequestInfo requestInfo) {
        if (!requestInfo.executed && this.mRequestInfos.containsKey(Integer.valueOf(requestInfo.request.requestId))) {
            logd("onExecuteRequest request=" + requestInfo);
            requestInfo.log("DctController.onExecuteRequest - executed=" + requestInfo.executed);
            requestInfo.executed = HWLOGW_E;
            String apn = apnForNetworkRequest(requestInfo.request);
            ApnContext apnContext = (ApnContext) this.mPhone.mDcTracker.mApnContexts.get(apn);
            logd("DcTracker.incApnRefCount on " + apn + " found " + apnContext);
            if (apnContext != null) {
                apnContext.incRefCount(requestInfo.getLog());
            }
        }
    }

    private void onExecuteAllRequests(int phoneId) {
        logd("onExecuteAllRequests phoneId=" + phoneId);
        for (Object obj : this.mRequestInfos.keySet()) {
            onExecuteRequest((RequestInfo) this.mRequestInfos.get(obj));
        }
    }

    private void onReleaseRequest(RequestInfo requestInfo) {
        logd("onReleaseRequest request=" + requestInfo);
        if (requestInfo != null) {
            requestInfo.log("DctController.onReleaseRequest");
            if (requestInfo.executed) {
                String apn = apnForNetworkRequest(requestInfo.request);
                ApnContext apnContext = (ApnContext) this.mPhone.mDcTracker.mApnContexts.get(apn);
                logd("DcTracker.decApnRefCount on " + apn + " found " + apnContext);
                if (apnContext != null) {
                    apnContext.decRefCount(requestInfo.getLog());
                }
                requestInfo.executed = false;
            }
        }
    }

    private void onReleaseAllRequests(int phoneId) {
        logd("onReleaseAllRequests phoneId=" + phoneId);
        for (Object obj : this.mRequestInfos.keySet()) {
            onReleaseRequest((RequestInfo) this.mRequestInfos.get(obj));
        }
    }

    private String apnForNetworkRequest(NetworkRequest nr) {
        NetworkCapabilities nc = nr.networkCapabilities;
        if (nc.getTransportTypes().length > 0 && !nc.hasTransport(0)) {
            return null;
        }
        int type = -1;
        String str = null;
        boolean error = false;
        if (nc.hasCapability(12)) {
            str = "default";
            type = 0;
        }
        if (nc.hasCapability(1)) {
            if (str != null) {
                error = HWLOGW_E;
            }
            str = "supl";
            type = 3;
        }
        if (nc.hasCapability(2)) {
            if (str != null) {
                error = HWLOGW_E;
            }
            str = "dun";
            type = 4;
        }
        if (error) {
            loge("Multiple apn types specified in request - result is unspecified!");
        }
        if (type != -1 && str != null) {
            return str;
        }
        loge("Unsupported NetworkRequest in Telephony: nr=" + nr);
        return null;
    }
}
