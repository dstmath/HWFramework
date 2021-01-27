package com.android.server.connectivity.usbp2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.util.SharedLog;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.INetworkManagementService;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;
import com.android.internal.util.Preconditions;
import com.android.server.HwServiceFactory;
import com.android.server.connectivity.tethering.TetheringConfiguration;
import com.android.server.connectivity.usbp2p.UsbP2pManager;
import com.android.server.intellicom.common.SmartDualCardConsts;
import com.android.server.net.BaseNetworkObserver;
import com.huawei.aod.AodThemeConst;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class UsbP2pManager extends BaseNetworkObserver {
    public static final int BASE_CMD = 593920;
    private static final int CMD_BINDER_DIED = 593925;
    private static final int CMD_P2P_REGISTERED = 593923;
    private static final int CMD_P2P_REQUESTED = 593921;
    private static final int CMD_P2P_STATE_CHANGE = 593924;
    private static final int CMD_P2P_UNREQUESTED = 593922;
    private static final int CMD_USB_UNAVAILABLE = 593926;
    private static final int INITIAL_CAPACITY = 10;
    private static final int MAX_REQUESTS_PER_UID = 100;
    private static final String TAG = "UsbP2pManager";
    private static final int USB_P2P_TYPE_LISTEN = 2;
    private static final int USB_P2P_TYPE_REQUEST = 1;
    private static final Object USB_STATE_LOCK = new Object();
    private UsbP2pCommands mCommands;
    private TetheringConfiguration mConfig;
    private Handler mHandler;
    private boolean mIsOtherIfaceTethered;
    private boolean mIsRndisEnabled;
    private boolean mIsUsbConfigured;
    private boolean mIsUsbConnected;
    private boolean mIsUsbTethered;
    private final AtomicInteger mNextRequestId;
    private final Map<Integer, UsbP2pRequestInfo> mRequests;
    private BroadcastReceiver mStateReceiver;
    private final SparseIntArray mUidToRequestCount;
    private UsbP2pStateMachine mUsbP2pStateMachine;

    private UsbP2pManager() {
        this.mUidToRequestCount = new SparseIntArray();
        this.mRequests = new ConcurrentHashMap(10);
        this.mNextRequestId = new AtomicInteger();
        this.mIsRndisEnabled = false;
        this.mIsUsbConnected = false;
        this.mIsUsbConfigured = false;
        this.mIsUsbTethered = false;
        this.mIsOtherIfaceTethered = false;
    }

    private static final class Singleton {
        private static final UsbP2pManager INSTANCE = new UsbP2pManager();

        private Singleton() {
        }
    }

    public static UsbP2pManager getInstance() {
        return Singleton.INSTANCE;
    }

    public void init(Context context, INetworkManagementService nms) {
        if (!Stream.of(context, nms).anyMatch($$Lambda$wLIh0GiBW9398cTP8uaTH8KoGwo.INSTANCE)) {
            this.mConfig = new TetheringConfiguration(context, new SharedLog(TAG), -1);
            this.mCommands = new UsbP2pCommands(nms, context, this.mConfig);
            HandlerThread handlerThread = new HandlerThread(TAG);
            handlerThread.start();
            this.mHandler = new UsbP2pHandler(handlerThread.getLooper());
            this.mUsbP2pStateMachine = new UsbP2pStateMachine(handlerThread, this.mCommands, this);
            this.mUsbP2pStateMachine.start();
            this.mStateReceiver = new StateReceiver();
            try {
                nms.registerObserver(this);
            } catch (RemoteException e) {
                loge("Register network management service catch remote exception.");
            }
            registerBroadcast(context);
            return;
        }
        throw new IllegalStateException("Error in construct UsbP2pManager, params null.");
    }

    public int getUsbP2pState() {
        return this.mUsbP2pStateMachine.getState();
    }

    public int requestForUsbP2p(int requestId, Messenger messenger, IBinder binder) {
        UsbP2pRequestInfo requestInfo;
        Preconditions.checkNotNull(messenger);
        Preconditions.checkNotNull(binder);
        int responseId = requestId == -1 ? getNextRequestId() : requestId;
        if (this.mRequests.containsKey(Integer.valueOf(responseId))) {
            requestInfo = this.mRequests.get(Integer.valueOf(responseId));
        } else {
            requestInfo = new UsbP2pRequestInfo(responseId, binder, messenger, 1);
        }
        Preconditions.checkArgument(requestInfo.uid == Binder.getCallingUid());
        requestInfo.addType(1);
        requestInfo.enforceRequestCountLimit();
        log("requestForUsbP2p, info = " + requestInfo);
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(CMD_P2P_REQUESTED, requestInfo));
        return responseId;
    }

    public int listenForUsbP2p(int listenId, Messenger messenger, IBinder binder) {
        UsbP2pRequestInfo requestInfo;
        Preconditions.checkNotNull(messenger);
        Preconditions.checkNotNull(binder);
        int responseId = listenId == -1 ? getNextRequestId() : listenId;
        if (this.mRequests.containsKey(Integer.valueOf(responseId))) {
            requestInfo = this.mRequests.get(Integer.valueOf(responseId));
        } else {
            requestInfo = new UsbP2pRequestInfo(responseId, binder, messenger, 2);
        }
        requestInfo.addType(2);
        requestInfo.enforceRequestCountLimit();
        log("listenForUsbP2p, info = " + requestInfo);
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(CMD_P2P_REGISTERED, requestInfo));
        return responseId;
    }

    public void releaseUsbP2pRequest(int requestId) {
        UsbP2pRequestInfo requestInfo = this.mRequests.get(Integer.valueOf(requestId));
        log("releaseUsbP2pRequest, info = " + requestInfo);
        if (requestInfo != null) {
            Handler handler = this.mHandler;
            handler.sendMessage(handler.obtainMessage(CMD_P2P_UNREQUESTED, requestInfo));
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean isConflictWithUsbP2p(int actionType) {
        char c;
        String currentState = this.mUsbP2pStateMachine.getCurrentState().getName();
        boolean isConflict = false;
        switch (currentState.hashCode()) {
            case -1891880349:
                if (currentState.equals("P2pState")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -574916355:
                if (currentState.equals("IdleState")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 132755015:
                if (currentState.equals("JointState")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 688485469:
                if (currentState.equals("StoppingState")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 1043090449:
                if (currentState.equals("StartingState")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 1151761279:
                if (currentState.equals("TetherState")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        if (c == 0 || c == 1) {
            return true;
        }
        if (c == 2) {
            if (actionType == 2) {
                isConflict = true;
            }
            return isConflict;
        } else if (c == 3 || c == 4 || c == 5) {
            return false;
        } else {
            return false;
        }
    }

    public void interfaceLinkStateChanged(String iface, boolean isUp) {
        interfaceStatusChanged(iface, isUp);
    }

    public void interfaceStatusChanged(String iface, boolean isUp) {
        if (this.mConfig.isUsb(iface)) {
            log("interfaceStatusChanged " + iface + " , up " + isUp);
        }
    }

    public void interfaceAdded(String iface) {
        if (this.mConfig.isUsb(iface)) {
            log("interfaceAdded " + iface);
            sendIfaceChanged(true, iface);
        }
    }

    public void interfaceRemoved(String iface) {
        if (this.mConfig.isUsb(iface)) {
            log("interfaceRemove " + iface);
            sendIfaceChanged(false, iface);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isRndisEnabled() {
        boolean z;
        synchronized (USB_STATE_LOCK) {
            z = this.mIsRndisEnabled;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isUsbConfigured() {
        boolean z;
        synchronized (USB_STATE_LOCK) {
            z = this.mIsUsbConfigured;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public boolean isOtherIfaceTethered() {
        boolean z;
        synchronized (USB_STATE_LOCK) {
            z = this.mIsOtherIfaceTethered;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void notifyStateChange(int newState) {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(CMD_P2P_STATE_CHANGE, newState, 0));
    }

    private class StateReceiver extends BroadcastReceiver {
        private StateReceiver() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (!TextUtils.isEmpty(action)) {
                    UsbP2pManager.this.log("State receiver onReceive action : " + action);
                    char c = 65535;
                    int hashCode = action.hashCode();
                    if (hashCode != -1754841973) {
                        if (hashCode == -494529457 && action.equals("android.hardware.usb.action.USB_STATE")) {
                            c = 0;
                        }
                    } else if (action.equals(SmartDualCardConsts.SYSTEM_STATE_NAME_TETHER_STATE_CHANGED)) {
                        c = 1;
                    }
                    if (c == 0) {
                        UsbP2pManager.this.handleUsbAction(intent);
                    } else if (c == 1) {
                        UsbP2pManager.this.handleTetherStateChange(intent);
                    }
                }
            }
        }
    }

    private void registerBroadcast(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_STATE");
        filter.addAction(SmartDualCardConsts.SYSTEM_STATE_NAME_TETHER_STATE_CHANGED);
        context.registerReceiver(this.mStateReceiver, filter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUsbAction(Intent intent) {
        boolean z = false;
        boolean isUsbConnected = intent.getBooleanExtra("connected", false);
        boolean isUsbConfigured = intent.getBooleanExtra("configured", false);
        boolean isRndisEnabled = intent.getBooleanExtra("rndis", false);
        log(String.format(Locale.ROOT, "handleUsbAction connected:%s configured:%s rndis:%s", Boolean.valueOf(isUsbConnected), Boolean.valueOf(isUsbConfigured), Boolean.valueOf(isRndisEnabled)));
        synchronized (USB_STATE_LOCK) {
            boolean isOldConnected = this.mIsUsbConnected;
            this.mIsUsbConnected = isUsbConnected;
            if (isOldConnected != this.mIsUsbConnected) {
                sendUsbStateChanged(this.mIsUsbConnected);
            }
            this.mIsUsbConnected = isUsbConnected;
            if (isUsbConfigured && isRndisEnabled) {
                z = true;
            }
            this.mIsRndisEnabled = z;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTetherStateChange(Intent intent) {
        List<String> tetheredList = intent.getStringArrayListExtra("tetherArray");
        synchronized (USB_STATE_LOCK) {
            if (tetheredList != null) {
                boolean isUsbTethered = false;
                boolean isWifiTethered = false;
                boolean isBluetoothTethered = false;
                boolean isWifiP2pTethered = false;
                for (String iface : tetheredList) {
                    if (this.mConfig.isUsb(iface)) {
                        isUsbTethered = true;
                    } else if (this.mConfig.isWifi(iface)) {
                        isWifiTethered = true;
                    } else if (this.mConfig.isBluetooth(iface)) {
                        isBluetoothTethered = true;
                    } else if (HwServiceFactory.getHwConnectivityManager().isP2pTether(iface)) {
                        isWifiP2pTethered = true;
                    }
                }
                if (isUsbTethered != this.mIsUsbTethered) {
                    this.mIsUsbTethered = isUsbTethered;
                    sendTetheredStateChanged(this.mIsUsbTethered);
                }
                this.mIsOtherIfaceTethered = isWifiTethered | isBluetoothTethered | isWifiP2pTethered;
            }
        }
        log("handleTetherStateChange, usb [" + this.mIsUsbTethered + "], others [" + this.mIsOtherIfaceTethered + "]");
    }

    private class UsbP2pHandler extends Handler {
        UsbP2pHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbP2pManager.CMD_P2P_REQUESTED /* 593921 */:
                    if (msg.obj instanceof UsbP2pRequestInfo) {
                        UsbP2pManager.this.handleP2pRequested((UsbP2pRequestInfo) msg.obj);
                        return;
                    }
                    return;
                case UsbP2pManager.CMD_P2P_UNREQUESTED /* 593922 */:
                case UsbP2pManager.CMD_BINDER_DIED /* 593925 */:
                    if (msg.obj instanceof UsbP2pRequestInfo) {
                        UsbP2pManager.this.handleP2pUnrequested((UsbP2pRequestInfo) msg.obj);
                        return;
                    }
                    return;
                case UsbP2pManager.CMD_P2P_REGISTERED /* 593923 */:
                    if (msg.obj instanceof UsbP2pRequestInfo) {
                        UsbP2pManager.this.handleP2pRegistered((UsbP2pRequestInfo) msg.obj);
                        return;
                    }
                    return;
                case UsbP2pManager.CMD_P2P_STATE_CHANGE /* 593924 */:
                    UsbP2pManager.this.handleP2pStateChanged(msg.arg1);
                    return;
                case UsbP2pManager.CMD_USB_UNAVAILABLE /* 593926 */:
                    UsbP2pManager.this.handleUsbUnavailable();
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pStateChanged(int state) {
        log("handleP2pStateChanged, state = " + state);
        this.mRequests.forEach(new BiConsumer(state) {
            /* class com.android.server.connectivity.usbp2p.$$Lambda$UsbP2pManager$50GDZQnAeLE3iqkzr86ViKwGwQ */
            private final /* synthetic */ int f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                UsbP2pManager.this.lambda$handleP2pStateChanged$0$UsbP2pManager(this.f$1, (Integer) obj, (UsbP2pManager.UsbP2pRequestInfo) obj2);
            }
        });
    }

    public /* synthetic */ void lambda$handleP2pStateChanged$0$UsbP2pManager(int state, Integer requestId, UsbP2pRequestInfo requestInfo) {
        notifyUsbP2pCallback(requestInfo, 2, state);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pRequested(UsbP2pRequestInfo requestInfo) {
        synchronized (USB_STATE_LOCK) {
            if (!this.mIsUsbConnected) {
                loge("handleP2pRequested, release for " + requestInfo + " when usb not connected.");
                return;
            }
        }
        log("handleP2pRequested, requestInfo = " + requestInfo);
        this.mRequests.put(Integer.valueOf(requestInfo.requestId), requestInfo);
        int currentState = getUsbP2pState();
        if (currentState != 0) {
            notifyUsbP2pCallback(requestInfo, 2, currentState);
        }
        if (isNeedP2pStart()) {
            this.mUsbP2pStateMachine.sendMessage(594021);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pRegistered(UsbP2pRequestInfo requestInfo) {
        synchronized (USB_STATE_LOCK) {
            if (!this.mIsUsbConnected) {
                loge("handleP2pRequested, release for " + requestInfo + " when usb not connected.");
                return;
            }
        }
        log("handleP2pRegistered, requestInfo = " + requestInfo);
        this.mRequests.put(Integer.valueOf(requestInfo.requestId), requestInfo);
        int currentState = getUsbP2pState();
        if (currentState != 0) {
            notifyUsbP2pCallback(requestInfo, 2, currentState);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleP2pUnrequested(UsbP2pRequestInfo requestInfo) {
        UsbP2pRequestInfo releaseInfo = this.mRequests.get(Integer.valueOf(requestInfo.requestId));
        if (releaseInfo == null) {
            loge("handleP2pUnrequested return for info is null.");
            return;
        }
        log("RELEASE " + releaseInfo);
        releaseInfo.unlinkDeathRecipient();
        releaseInfo.reduceRequestCount();
        this.mRequests.remove(Integer.valueOf(releaseInfo.requestId));
        if (releaseInfo.isRequest() && isNeedP2pStop()) {
            this.mUsbP2pStateMachine.sendMessage(594022);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleUsbUnavailable() {
        log("handleUsbUnavailable, release all request.");
        if (getUsbP2pState() != 0) {
            this.mRequests.forEach(new BiConsumer() {
                /* class com.android.server.connectivity.usbp2p.$$Lambda$UsbP2pManager$MS2lfTCfM8hXAx5o6iPT7tGRCIc */

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    UsbP2pManager.this.lambda$handleUsbUnavailable$1$UsbP2pManager((Integer) obj, (UsbP2pManager.UsbP2pRequestInfo) obj2);
                }
            });
        } else {
            this.mRequests.forEach(new BiConsumer() {
                /* class com.android.server.connectivity.usbp2p.$$Lambda$UsbP2pManager$vhoE6EN88KRrxXMv_Ldkq2IMOu0 */

                @Override // java.util.function.BiConsumer
                public final void accept(Object obj, Object obj2) {
                    UsbP2pManager.this.lambda$handleUsbUnavailable$2$UsbP2pManager((Integer) obj, (UsbP2pManager.UsbP2pRequestInfo) obj2);
                }
            });
        }
        this.mRequests.values().forEach($$Lambda$l1gfiXhlZxt682CCAsd2Ts5B__M.INSTANCE);
        synchronized (this.mUidToRequestCount) {
            this.mUidToRequestCount.clear();
            this.mRequests.clear();
        }
        this.mUsbP2pStateMachine.sendMessage(594022);
    }

    public /* synthetic */ void lambda$handleUsbUnavailable$1$UsbP2pManager(Integer requestId, UsbP2pRequestInfo requestInfo) {
        notifyUsbP2pCallback(requestInfo, 1, 0);
    }

    public /* synthetic */ void lambda$handleUsbUnavailable$2$UsbP2pManager(Integer requestId, UsbP2pRequestInfo requestInfo) {
        notifyUsbP2pCallback(requestInfo, 1, -1);
    }

    private void notifyUsbP2pCallback(UsbP2pRequestInfo requestInfo, int callbackType, int currentState) {
        Message message = Message.obtain();
        message.what = callbackType;
        message.arg1 = currentState;
        message.arg2 = requestInfo.requestId;
        try {
            requestInfo.messenger.send(message);
        } catch (RemoteException e) {
            loge("RemoteException caught trying to send a callback to " + requestInfo);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0042 A[ADDED_TO_REGION] */
    private boolean isNeedP2pStart() {
        char c;
        String currentState = this.mUsbP2pStateMachine.getCurrentState().getName();
        int hashCode = currentState.hashCode();
        if (hashCode != -1891880349) {
            if (hashCode != 132755015) {
                if (hashCode == 1043090449 && currentState.equals("StartingState")) {
                    c = 2;
                    if (c != 0 || c == 1 || c == 2) {
                        return false;
                    }
                    return this.mRequests.values().stream().anyMatch($$Lambda$9y4rklj21DjMtYk4DIqT2aD83rE.INSTANCE);
                }
            } else if (currentState.equals("JointState")) {
                c = 0;
                if (c != 0) {
                }
                return false;
            }
        } else if (currentState.equals("P2pState")) {
            c = 1;
            if (c != 0) {
            }
            return false;
        }
        c = 65535;
        if (c != 0) {
        }
        return false;
    }

    private boolean isNeedP2pStop() {
        return this.mRequests.values().stream().noneMatch($$Lambda$9y4rklj21DjMtYk4DIqT2aD83rE.INSTANCE);
    }

    private void sendIfaceChanged(boolean isUp, String ifaceName) {
        if (isUp) {
            this.mUsbP2pStateMachine.sendMessage(594025, ifaceName);
        } else if (!this.mCommands.isUsbRndisEnabled()) {
            this.mHandler.sendEmptyMessage(CMD_USB_UNAVAILABLE);
        }
    }

    private void sendUsbStateChanged(boolean isPlugIn) {
        if (isPlugIn) {
            this.mUsbP2pStateMachine.sendMessage(594026);
        } else {
            this.mHandler.sendEmptyMessage(CMD_USB_UNAVAILABLE);
        }
    }

    private void sendTetheredStateChanged(boolean isOn) {
        this.mUsbP2pStateMachine.sendMessage(isOn ? 594023 : 594024);
    }

    private int getNextRequestId() {
        return this.mNextRequestId.incrementAndGet();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void log(String msg) {
        Log.i(TAG, msg);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void loge(String msg) {
        Log.e(TAG, msg);
    }

    /* access modifiers changed from: private */
    public class UsbP2pRequestInfo implements IBinder.DeathRecipient {
        private final IBinder binder;
        private final Messenger messenger;
        private final int pid = Binder.getCallingPid();
        private final int requestId;
        private int type;
        private final int uid = Binder.getCallingUid();

        UsbP2pRequestInfo(int requestId2, IBinder binder2, Messenger messenger2, int type2) {
            this.requestId = requestId2;
            this.binder = binder2;
            this.messenger = messenger2;
            this.type = type2;
            try {
                binder2.linkToDeath(this, 0);
            } catch (RemoteException e) {
                binderDied();
            }
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            UsbP2pManager usbP2pManager = UsbP2pManager.this;
            usbP2pManager.log("UsbP2pRequest binderDied(" + this.uid + "," + this.pid + "," + this.type + ")");
            UsbP2pManager.this.mHandler.sendMessage(UsbP2pManager.this.mHandler.obtainMessage(UsbP2pManager.CMD_BINDER_DIED, this));
        }

        @Override // java.lang.Object
        public String toString() {
            return "UsbP2pRequestInfo { requestId:" + this.requestId + ", uid/pid:" + this.uid + AodThemeConst.SPLASH + this.pid + ", type:" + this.type + "}";
        }

        /* access modifiers changed from: package-private */
        public void addType(int newType) {
            this.type |= newType;
        }

        /* access modifiers changed from: package-private */
        public boolean isRequest() {
            return (this.type & 1) == 1;
        }

        /* access modifiers changed from: package-private */
        public boolean isListen() {
            return (this.type & 2) == 2;
        }

        /* access modifiers changed from: package-private */
        public void unlinkDeathRecipient() {
            IBinder iBinder = this.binder;
            if (iBinder != null) {
                iBinder.unlinkToDeath(this, 0);
            }
        }

        /* access modifiers changed from: package-private */
        public void enforceRequestCountLimit() {
            synchronized (UsbP2pManager.this.mUidToRequestCount) {
                int requestsNum = UsbP2pManager.this.mUidToRequestCount.get(this.uid) + 1;
                if (requestsNum <= 100) {
                    UsbP2pManager.this.mUidToRequestCount.put(this.uid, requestsNum);
                } else {
                    throw new ServiceSpecificException(1);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void reduceRequestCount() {
            synchronized (UsbP2pManager.this.mUidToRequestCount) {
                int requestCount = UsbP2pManager.this.mUidToRequestCount.get(this.uid);
                if (requestCount < 1) {
                    UsbP2pManager usbP2pManager = UsbP2pManager.this;
                    usbP2pManager.loge("Too small request count for uid " + this.uid + ", ignore.");
                } else if (requestCount == 1) {
                    UsbP2pManager.this.mUidToRequestCount.removeAt(UsbP2pManager.this.mUidToRequestCount.indexOfKey(this.uid));
                } else {
                    UsbP2pManager.this.mUidToRequestCount.put(this.uid, requestCount - 1);
                }
            }
        }
    }
}
