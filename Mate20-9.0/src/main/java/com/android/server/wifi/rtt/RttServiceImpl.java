package com.android.server.wifi.rtt;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.wifi.V1_0.RttResult;
import android.location.LocationManager;
import android.net.MacAddress;
import android.net.wifi.aware.IWifiAwareMacAddressProvider;
import android.net.wifi.aware.IWifiAwareManager;
import android.net.wifi.rtt.IRttCallback;
import android.net.wifi.rtt.IWifiRttManager;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.ResponderConfig;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ShellCallback;
import android.os.ShellCommand;
import android.os.UserHandle;
import android.os.WorkSource;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseIntArray;
import com.android.internal.util.WakeupMessage;
import com.android.server.wifi.Clock;
import com.android.server.wifi.FrameworkFacade;
import com.android.server.wifi.rtt.RttServiceImpl;
import com.android.server.wifi.util.NativeUtil;
import com.android.server.wifi.util.WifiPermissionsUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class RttServiceImpl extends IWifiRttManager.Stub {
    private static final int CONTROL_PARAM_OVERRIDE_ASSUME_NO_PRIVILEGE_DEFAULT = 0;
    private static final String CONTROL_PARAM_OVERRIDE_ASSUME_NO_PRIVILEGE_NAME = "override_assume_no_privilege";
    private static final int CONVERSION_US_TO_MS = 1000;
    private static final long DEFAULT_BACKGROUND_PROCESS_EXEC_GAP_MS = 1800000;
    private static final long HAL_RANGING_TIMEOUT_MS = 5000;
    static final String HAL_RANGING_TIMEOUT_TAG = "RttServiceImpl HAL Ranging Timeout";
    static final int MAX_QUEUED_PER_UID = 20;
    private static final String TAG = "RttServiceImpl";
    private static final boolean VDBG = false;
    /* access modifiers changed from: private */
    public ActivityManager mActivityManager;
    /* access modifiers changed from: private */
    public IWifiAwareManager mAwareBinder;
    /* access modifiers changed from: private */
    public long mBackgroundProcessExecGapMs;
    /* access modifiers changed from: private */
    public Clock mClock;
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public boolean mDbg = false;
    private FrameworkFacade mFrameworkFacade;
    /* access modifiers changed from: private */
    public LocationManager mLocationManager;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public RttMetrics mRttMetrics;
    private RttNative mRttNative;
    /* access modifiers changed from: private */
    public RttServiceSynchronized mRttServiceSynchronized;
    private final RttShellCommand mShellCommand;
    /* access modifiers changed from: private */
    public WifiPermissionsUtil mWifiPermissionsUtil;

    private static class RttRequestInfo {
        public IBinder binder;
        public IRttCallback callback;
        public String callingPackage;
        public int cmdId;
        public boolean dispatchedToNative;
        public IBinder.DeathRecipient dr;
        public boolean isCalledFromPrivilegedContext;
        public boolean peerHandlesTranslated;
        public RangingRequest request;
        public int uid;
        public WorkSource workSource;

        private RttRequestInfo() {
            this.cmdId = 0;
            this.dispatchedToNative = false;
            this.peerHandlesTranslated = false;
        }

        public String toString() {
            return "RttRequestInfo: uid=" + this.uid + ", workSource=" + this.workSource + ", binder=" + this.binder + ", dr=" + this.dr + ", callingPackage=" + this.callingPackage + ", request=" + this.request.toString() + ", callback=" + this.callback + ", cmdId=" + this.cmdId + ", peerHandlesTranslated=" + this.peerHandlesTranslated + ", isCalledFromPrivilegedContext=" + this.isCalledFromPrivilegedContext;
        }
    }

    private static class RttRequesterInfo {
        public long lastRangingExecuted;

        private RttRequesterInfo() {
        }

        public String toString() {
            return "RttRequesterInfo: lastRangingExecuted=" + this.lastRangingExecuted;
        }
    }

    private class RttServiceSynchronized {
        public Handler mHandler;
        private int mNextCommandId = RttServiceImpl.CONVERSION_US_TO_MS;
        private WakeupMessage mRangingTimeoutMessage = null;
        private RttNative mRttNative;
        private List<RttRequestInfo> mRttRequestQueue = new LinkedList();
        private Map<Integer, RttRequesterInfo> mRttRequesterInfo = new HashMap();

        RttServiceSynchronized(Looper looper, RttNative rttNative) {
            this.mRttNative = rttNative;
            this.mHandler = new Handler(looper);
            this.mRangingTimeoutMessage = new WakeupMessage(RttServiceImpl.this.mContext, this.mHandler, RttServiceImpl.HAL_RANGING_TIMEOUT_TAG, new Runnable() {
                public final void run() {
                    RttServiceImpl.RttServiceSynchronized.this.timeoutRangingRequest();
                }
            });
        }

        private void cancelRanging(RttRequestInfo rri) {
            ArrayList<byte[]> macAddresses = new ArrayList<>();
            for (ResponderConfig peer : rri.request.mRttPeers) {
                macAddresses.add(peer.macAddress.toByteArray());
            }
            this.mRttNative.rangeCancel(rri.cmdId, macAddresses);
        }

        /* access modifiers changed from: private */
        public void cleanUpOnDisable() {
            for (RttRequestInfo rri : this.mRttRequestQueue) {
                try {
                    if (rri.dispatchedToNative) {
                        cancelRanging(rri);
                    }
                    RttServiceImpl.this.mRttMetrics.recordOverallStatus(3);
                    rri.callback.onRangingFailure(2);
                } catch (RemoteException e) {
                    Log.e(RttServiceImpl.TAG, "RttServiceSynchronized.startRanging: disabled, callback failed -- " + e);
                }
                rri.binder.unlinkToDeath(rri.dr, 0);
            }
            this.mRttRequestQueue.clear();
            this.mRangingTimeoutMessage.cancel();
        }

        /* access modifiers changed from: private */
        public void cleanUpClientRequests(int uid, WorkSource workSource) {
            boolean dispatchedRequestAborted = false;
            ListIterator<RttRequestInfo> it = this.mRttRequestQueue.listIterator();
            while (true) {
                boolean match = true;
                if (!it.hasNext()) {
                    break;
                }
                RttRequestInfo rri = it.next();
                if (rri.uid != uid) {
                    match = false;
                }
                if (!(rri.workSource == null || workSource == null)) {
                    rri.workSource.remove(workSource);
                    if (rri.workSource.isEmpty()) {
                        match = true;
                    }
                }
                if (match) {
                    if (!rri.dispatchedToNative) {
                        it.remove();
                        rri.binder.unlinkToDeath(rri.dr, 0);
                    } else {
                        dispatchedRequestAborted = true;
                        Log.d(RttServiceImpl.TAG, "Client death - cancelling RTT operation in progress: cmdId=" + rri.cmdId);
                        this.mRangingTimeoutMessage.cancel();
                        cancelRanging(rri);
                    }
                }
            }
            if (dispatchedRequestAborted) {
                executeNextRangingRequestIfPossible(true);
            }
        }

        /* access modifiers changed from: private */
        public void timeoutRangingRequest() {
            if (this.mRttRequestQueue.size() == 0) {
                Log.w(RttServiceImpl.TAG, "RttServiceSynchronized.timeoutRangingRequest: but nothing in queue!?");
                return;
            }
            RttRequestInfo rri = this.mRttRequestQueue.get(0);
            if (!rri.dispatchedToNative) {
                Log.w(RttServiceImpl.TAG, "RttServiceSynchronized.timeoutRangingRequest: command not dispatched to native!?");
                return;
            }
            cancelRanging(rri);
            try {
                RttServiceImpl.this.mRttMetrics.recordOverallStatus(4);
                rri.callback.onRangingFailure(1);
            } catch (RemoteException e) {
                Log.e(RttServiceImpl.TAG, "RttServiceSynchronized.timeoutRangingRequest: callback failed: " + e);
            }
            executeNextRangingRequestIfPossible(true);
        }

        /* access modifiers changed from: private */
        public void queueRangingRequest(int uid, WorkSource workSource, IBinder binder, IBinder.DeathRecipient dr, String callingPackage, RangingRequest request, IRttCallback callback, boolean isCalledFromPrivilegedContext) {
            RttServiceImpl.this.mRttMetrics.recordRequest(workSource, request);
            if (isRequestorSpamming(workSource)) {
                Log.w(RttServiceImpl.TAG, "Work source " + workSource + " is spamming, dropping request: " + request);
                binder.unlinkToDeath(dr, 0);
                try {
                    RttServiceImpl.this.mRttMetrics.recordOverallStatus(5);
                    callback.onRangingFailure(1);
                } catch (RemoteException e) {
                    Log.e(RttServiceImpl.TAG, "RttServiceSynchronized.queueRangingRequest: spamming, callback failed -- " + e);
                }
                return;
            }
            RttRequestInfo newRequest = new RttRequestInfo();
            newRequest.uid = uid;
            newRequest.workSource = workSource;
            newRequest.binder = binder;
            newRequest.dr = dr;
            newRequest.callingPackage = callingPackage;
            newRequest.request = request;
            newRequest.callback = callback;
            newRequest.isCalledFromPrivilegedContext = isCalledFromPrivilegedContext;
            this.mRttRequestQueue.add(newRequest);
            executeNextRangingRequestIfPossible(false);
        }

        private boolean isRequestorSpamming(WorkSource ws) {
            SparseIntArray counts = new SparseIntArray();
            Iterator<RttRequestInfo> it = this.mRttRequestQueue.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                RttRequestInfo rri = it.next();
                for (int i = 0; i < rri.workSource.size(); i++) {
                    int uid = rri.workSource.get(i);
                    counts.put(uid, counts.get(uid) + 1);
                }
                ArrayList<WorkSource.WorkChain> workChains = rri.workSource.getWorkChains();
                if (workChains != null) {
                    for (int i2 = 0; i2 < workChains.size(); i2++) {
                        int uid2 = workChains.get(i2).getAttributionUid();
                        counts.put(uid2, counts.get(uid2) + 1);
                    }
                }
            }
            for (int i3 = 0; i3 < ws.size(); i3++) {
                if (counts.get(ws.get(i3)) < 20) {
                    return false;
                }
            }
            ArrayList<WorkSource.WorkChain> workChains2 = ws.getWorkChains();
            if (workChains2 != null) {
                for (int i4 = 0; i4 < workChains2.size(); i4++) {
                    if (counts.get(workChains2.get(i4).getAttributionUid()) < 20) {
                        return false;
                    }
                }
            }
            if (RttServiceImpl.this.mDbg) {
                Log.v(RttServiceImpl.TAG, "isRequestorSpamming: ws=" + ws + ", someone is spamming: " + counts);
            }
            return true;
        }

        /* access modifiers changed from: private */
        public void executeNextRangingRequestIfPossible(boolean popFirst) {
            if (popFirst) {
                if (this.mRttRequestQueue.size() == 0) {
                    Log.w(RttServiceImpl.TAG, "executeNextRangingRequestIfPossible: pop requested - but empty queue!? Ignoring pop.");
                } else {
                    RttRequestInfo topOfQueueRequest = this.mRttRequestQueue.remove(0);
                    topOfQueueRequest.binder.unlinkToDeath(topOfQueueRequest.dr, 0);
                }
            }
            if (this.mRttRequestQueue.size() != 0) {
                RttRequestInfo nextRequest = this.mRttRequestQueue.get(0);
                if (!nextRequest.peerHandlesTranslated && !nextRequest.dispatchedToNative) {
                    startRanging(nextRequest);
                }
            }
        }

        private void startRanging(RttRequestInfo nextRequest) {
            if (!RttServiceImpl.this.isAvailable()) {
                Log.d(RttServiceImpl.TAG, "RttServiceSynchronized.startRanging: disabled");
                try {
                    RttServiceImpl.this.mRttMetrics.recordOverallStatus(3);
                    nextRequest.callback.onRangingFailure(2);
                } catch (RemoteException e) {
                    Log.e(RttServiceImpl.TAG, "RttServiceSynchronized.startRanging: disabled, callback failed -- " + e);
                    executeNextRangingRequestIfPossible(true);
                    return;
                }
            }
            if (!processAwarePeerHandles(nextRequest)) {
                if (!preExecThrottleCheck(nextRequest.workSource)) {
                    Log.w(RttServiceImpl.TAG, "RttServiceSynchronized.startRanging: execution throttled - nextRequest=" + nextRequest + ", mRttRequesterInfo=" + this.mRttRequesterInfo);
                    try {
                        RttServiceImpl.this.mRttMetrics.recordOverallStatus(5);
                        nextRequest.callback.onRangingFailure(1);
                    } catch (RemoteException e2) {
                        Log.e(RttServiceImpl.TAG, "RttServiceSynchronized.startRanging: throttled, callback failed -- " + e2);
                    }
                    executeNextRangingRequestIfPossible(true);
                    return;
                }
                int i = this.mNextCommandId;
                this.mNextCommandId = i + 1;
                nextRequest.cmdId = i;
                if (this.mRttNative.rangeRequest(nextRequest.cmdId, nextRequest.request, nextRequest.isCalledFromPrivilegedContext)) {
                    this.mRangingTimeoutMessage.schedule(RttServiceImpl.this.mClock.getElapsedSinceBootMillis() + RttServiceImpl.HAL_RANGING_TIMEOUT_MS);
                } else {
                    Log.w(RttServiceImpl.TAG, "RttServiceSynchronized.startRanging: native rangeRequest call failed");
                    try {
                        RttServiceImpl.this.mRttMetrics.recordOverallStatus(6);
                        nextRequest.callback.onRangingFailure(1);
                    } catch (RemoteException e3) {
                        Log.e(RttServiceImpl.TAG, "RttServiceSynchronized.startRanging: HAL request failed, callback failed -- " + e3);
                    }
                    executeNextRangingRequestIfPossible(true);
                }
                nextRequest.dispatchedToNative = true;
            }
        }

        private boolean preExecThrottleCheck(WorkSource ws) {
            boolean allUidsInBackground = true;
            int i = 0;
            while (true) {
                if (i >= ws.size()) {
                    break;
                } else if (RttServiceImpl.this.mActivityManager.getUidImportance(ws.get(i)) <= 125) {
                    allUidsInBackground = false;
                    break;
                } else {
                    i++;
                }
            }
            ArrayList<WorkSource.WorkChain> workChains = ws.getWorkChains();
            if (allUidsInBackground && workChains != null) {
                int i2 = 0;
                while (true) {
                    if (i2 >= workChains.size()) {
                        break;
                    } else if (RttServiceImpl.this.mActivityManager.getUidImportance(workChains.get(i2).getAttributionUid()) <= 125) {
                        allUidsInBackground = false;
                        break;
                    } else {
                        i2++;
                    }
                }
            }
            boolean allowExecution = false;
            long mostRecentExecutionPermitted = RttServiceImpl.this.mClock.getElapsedSinceBootMillis() - RttServiceImpl.this.mBackgroundProcessExecGapMs;
            if (allUidsInBackground) {
                int i3 = 0;
                while (true) {
                    if (i3 >= ws.size()) {
                        break;
                    }
                    RttRequesterInfo info = this.mRttRequesterInfo.get(Integer.valueOf(ws.get(i3)));
                    if (info == null || info.lastRangingExecuted < mostRecentExecutionPermitted) {
                        allowExecution = true;
                    } else {
                        i3++;
                    }
                }
                allowExecution = true;
                int i4 = 1;
                int i5 = workChains != null ? 1 : 0;
                if (allowExecution) {
                    i4 = 0;
                }
                if ((i4 & i5) != 0) {
                    int i6 = 0;
                    while (true) {
                        if (i6 >= workChains.size()) {
                            break;
                        }
                        RttRequesterInfo info2 = this.mRttRequesterInfo.get(Integer.valueOf(workChains.get(i6).getAttributionUid()));
                        if (info2 == null || info2.lastRangingExecuted < mostRecentExecutionPermitted) {
                            allowExecution = true;
                        } else {
                            i6++;
                        }
                    }
                    allowExecution = true;
                }
            } else {
                allowExecution = true;
            }
            if (allowExecution) {
                for (int i7 = 0; i7 < ws.size(); i7++) {
                    RttRequesterInfo info3 = this.mRttRequesterInfo.get(Integer.valueOf(ws.get(i7)));
                    if (info3 == null) {
                        info3 = new RttRequesterInfo();
                        this.mRttRequesterInfo.put(Integer.valueOf(ws.get(i7)), info3);
                    }
                    info3.lastRangingExecuted = RttServiceImpl.this.mClock.getElapsedSinceBootMillis();
                }
                if (workChains != null) {
                    for (int i8 = 0; i8 < workChains.size(); i8++) {
                        WorkSource.WorkChain wc = workChains.get(i8);
                        RttRequesterInfo info4 = this.mRttRequesterInfo.get(Integer.valueOf(wc.getAttributionUid()));
                        if (info4 == null) {
                            info4 = new RttRequesterInfo();
                            this.mRttRequesterInfo.put(Integer.valueOf(wc.getAttributionUid()), info4);
                        }
                        info4.lastRangingExecuted = RttServiceImpl.this.mClock.getElapsedSinceBootMillis();
                    }
                }
            }
            return allowExecution;
        }

        private boolean processAwarePeerHandles(final RttRequestInfo request) {
            List<Integer> peerIdsNeedingTranslation = new ArrayList<>();
            for (ResponderConfig rttPeer : request.request.mRttPeers) {
                if (rttPeer.peerHandle != null && rttPeer.macAddress == null) {
                    peerIdsNeedingTranslation.add(Integer.valueOf(rttPeer.peerHandle.peerId));
                }
            }
            if (peerIdsNeedingTranslation.size() == 0) {
                return false;
            }
            if (request.peerHandlesTranslated) {
                Log.w(RttServiceImpl.TAG, "processAwarePeerHandles: request=" + request + ": PeerHandles translated - but information still missing!?");
                try {
                    RttServiceImpl.this.mRttMetrics.recordOverallStatus(7);
                    request.callback.onRangingFailure(1);
                } catch (RemoteException e) {
                    Log.e(RttServiceImpl.TAG, "processAwarePeerHandles: onRangingResults failure -- " + e);
                }
                executeNextRangingRequestIfPossible(true);
                return true;
            }
            request.peerHandlesTranslated = true;
            try {
                RttServiceImpl.this.mAwareBinder.requestMacAddresses(request.uid, peerIdsNeedingTranslation, new IWifiAwareMacAddressProvider.Stub() {
                    public void macAddress(Map peerIdToMacMap) {
                        RttServiceSynchronized.this.mHandler.post(
                        /*  JADX ERROR: Method code generation error
                            jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x000b: INVOKE  (wrap: android.os.Handler
                              0x0002: IGET  (r0v1 android.os.Handler) = (wrap: com.android.server.wifi.rtt.RttServiceImpl$RttServiceSynchronized
                              0x0000: IGET  (r0v0 com.android.server.wifi.rtt.RttServiceImpl$RttServiceSynchronized) = (r3v0 'this' com.android.server.wifi.rtt.RttServiceImpl$RttServiceSynchronized$1 A[THIS]) com.android.server.wifi.rtt.RttServiceImpl.RttServiceSynchronized.1.this$1 com.android.server.wifi.rtt.RttServiceImpl$RttServiceSynchronized) com.android.server.wifi.rtt.RttServiceImpl.RttServiceSynchronized.mHandler android.os.Handler), (wrap: com.android.server.wifi.rtt.-$$Lambda$RttServiceImpl$RttServiceSynchronized$1$X3EitWNHg38OS5b_JDpRvNEeXDk
                              0x0008: CONSTRUCTOR  (r2v0 com.android.server.wifi.rtt.-$$Lambda$RttServiceImpl$RttServiceSynchronized$1$X3EitWNHg38OS5b_JDpRvNEeXDk) = (r3v0 'this' com.android.server.wifi.rtt.RttServiceImpl$RttServiceSynchronized$1 A[THIS]), (wrap: com.android.server.wifi.rtt.RttServiceImpl$RttRequestInfo
                              0x0004: IGET  (r1v0 com.android.server.wifi.rtt.RttServiceImpl$RttRequestInfo) = (r3v0 'this' com.android.server.wifi.rtt.RttServiceImpl$RttServiceSynchronized$1 A[THIS]) com.android.server.wifi.rtt.RttServiceImpl.RttServiceSynchronized.1.val$request com.android.server.wifi.rtt.RttServiceImpl$RttRequestInfo), (r4v0 'peerIdToMacMap' java.util.Map) com.android.server.wifi.rtt.-$$Lambda$RttServiceImpl$RttServiceSynchronized$1$X3EitWNHg38OS5b_JDpRvNEeXDk.<init>(com.android.server.wifi.rtt.RttServiceImpl$RttServiceSynchronized$1, com.android.server.wifi.rtt.RttServiceImpl$RttRequestInfo, java.util.Map):void CONSTRUCTOR) android.os.Handler.post(java.lang.Runnable):boolean type: VIRTUAL in method: com.android.server.wifi.rtt.RttServiceImpl.RttServiceSynchronized.1.macAddress(java.util.Map):void, dex: wifi-service_classes.dex
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                            	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                            	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                            	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                            	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                            	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                            	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                            	at jadx.core.codegen.InsnGen.inlineAnonymousConstructor(InsnGen.java:665)
                            	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:596)
                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                            	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                            	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                            	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                            	at jadx.core.codegen.RegionGen.makeRegionIndent(RegionGen.java:98)
                            	at jadx.core.codegen.RegionGen.makeTryCatch(RegionGen.java:311)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:68)
                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                            	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                            	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                            	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                            	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                            	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                            	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                            	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                            	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                            	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                            	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                            	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                            	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                            	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                            	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                            	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                            	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                            	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                            Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0008: CONSTRUCTOR  (r2v0 com.android.server.wifi.rtt.-$$Lambda$RttServiceImpl$RttServiceSynchronized$1$X3EitWNHg38OS5b_JDpRvNEeXDk) = (r3v0 'this' com.android.server.wifi.rtt.RttServiceImpl$RttServiceSynchronized$1 A[THIS]), (wrap: com.android.server.wifi.rtt.RttServiceImpl$RttRequestInfo
                              0x0004: IGET  (r1v0 com.android.server.wifi.rtt.RttServiceImpl$RttRequestInfo) = (r3v0 'this' com.android.server.wifi.rtt.RttServiceImpl$RttServiceSynchronized$1 A[THIS]) com.android.server.wifi.rtt.RttServiceImpl.RttServiceSynchronized.1.val$request com.android.server.wifi.rtt.RttServiceImpl$RttRequestInfo), (r4v0 'peerIdToMacMap' java.util.Map) com.android.server.wifi.rtt.-$$Lambda$RttServiceImpl$RttServiceSynchronized$1$X3EitWNHg38OS5b_JDpRvNEeXDk.<init>(com.android.server.wifi.rtt.RttServiceImpl$RttServiceSynchronized$1, com.android.server.wifi.rtt.RttServiceImpl$RttRequestInfo, java.util.Map):void CONSTRUCTOR in method: com.android.server.wifi.rtt.RttServiceImpl.RttServiceSynchronized.1.macAddress(java.util.Map):void, dex: wifi-service_classes.dex
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                            	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                            	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                            	at jadx.core.codegen.InsnGen.makeInvoke(InsnGen.java:717)
                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:357)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                            	... 52 more
                            Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: com.android.server.wifi.rtt.-$$Lambda$RttServiceImpl$RttServiceSynchronized$1$X3EitWNHg38OS5b_JDpRvNEeXDk, state: NOT_LOADED
                            	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                            	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                            	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                            	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                            	... 57 more
                            */
                        /*
                            this = this;
                            com.android.server.wifi.rtt.RttServiceImpl$RttServiceSynchronized r0 = com.android.server.wifi.rtt.RttServiceImpl.RttServiceSynchronized.this
                            android.os.Handler r0 = r0.mHandler
                            com.android.server.wifi.rtt.RttServiceImpl$RttRequestInfo r1 = r8
                            com.android.server.wifi.rtt.-$$Lambda$RttServiceImpl$RttServiceSynchronized$1$X3EitWNHg38OS5b_JDpRvNEeXDk r2 = new com.android.server.wifi.rtt.-$$Lambda$RttServiceImpl$RttServiceSynchronized$1$X3EitWNHg38OS5b_JDpRvNEeXDk
                            r2.<init>(r3, r1, r4)
                            r0.post(r2)
                            return
                        */
                        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.rtt.RttServiceImpl.RttServiceSynchronized.AnonymousClass1.macAddress(java.util.Map):void");
                    }
                });
                return true;
            } catch (RemoteException e1) {
                Log.e(RttServiceImpl.TAG, "processAwarePeerHandles: exception while calling requestMacAddresses -- " + e1 + ", aborting request=" + request);
                try {
                    RttServiceImpl.this.mRttMetrics.recordOverallStatus(7);
                    request.callback.onRangingFailure(1);
                } catch (RemoteException e2) {
                    Log.e(RttServiceImpl.TAG, "processAwarePeerHandles: onRangingResults failure -- " + e2);
                }
                executeNextRangingRequestIfPossible(true);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public void processReceivedAwarePeerMacAddresses(RttRequestInfo request, Map<Integer, byte[]> peerIdToMacMap) {
            RttRequestInfo rttRequestInfo = request;
            RangingRequest.Builder newRequestBuilder = new RangingRequest.Builder();
            for (ResponderConfig rttPeer : rttRequestInfo.request.mRttPeers) {
                if (rttPeer.peerHandle == null || rttPeer.macAddress != null) {
                    Map<Integer, byte[]> map = peerIdToMacMap;
                    newRequestBuilder.addResponder(rttPeer);
                } else {
                    ResponderConfig responderConfig = new ResponderConfig(MacAddress.fromBytes(peerIdToMacMap.get(Integer.valueOf(rttPeer.peerHandle.peerId))), rttPeer.peerHandle, rttPeer.responderType, rttPeer.supports80211mc, rttPeer.channelWidth, rttPeer.frequency, rttPeer.centerFreq0, rttPeer.centerFreq1, rttPeer.preamble);
                    newRequestBuilder.addResponder(responderConfig);
                }
            }
            Map<Integer, byte[]> map2 = peerIdToMacMap;
            rttRequestInfo.request = newRequestBuilder.build();
            startRanging(request);
        }

        /* access modifiers changed from: private */
        public void onRangingResults(int cmdId, List<RttResult> results) {
            if (this.mRttRequestQueue.size() == 0) {
                Log.e(RttServiceImpl.TAG, "RttServiceSynchronized.onRangingResults: no current RTT request pending!?");
                return;
            }
            this.mRangingTimeoutMessage.cancel();
            boolean permissionGranted = false;
            RttRequestInfo topOfQueueRequest = this.mRttRequestQueue.get(0);
            if (topOfQueueRequest.cmdId != cmdId) {
                Log.e(RttServiceImpl.TAG, "RttServiceSynchronized.onRangingResults: cmdId=" + cmdId + ", does not match pending RTT request cmdId=" + topOfQueueRequest.cmdId);
                return;
            }
            if (RttServiceImpl.this.mWifiPermissionsUtil.checkCallersLocationPermission(topOfQueueRequest.callingPackage, topOfQueueRequest.uid) && RttServiceImpl.this.mLocationManager.isLocationEnabled()) {
                permissionGranted = true;
            }
            if (permissionGranted) {
                try {
                    List<RangingResult> finalResults = postProcessResults(topOfQueueRequest.request, results, topOfQueueRequest.isCalledFromPrivilegedContext);
                    RttServiceImpl.this.mRttMetrics.recordOverallStatus(1);
                    RttServiceImpl.this.mRttMetrics.recordResult(topOfQueueRequest.request, results);
                    topOfQueueRequest.callback.onRangingResults(finalResults);
                } catch (RemoteException e) {
                    Log.e(RttServiceImpl.TAG, "RttServiceSynchronized.onRangingResults: callback exception -- " + e);
                }
            } else {
                Log.w(RttServiceImpl.TAG, "RttServiceSynchronized.onRangingResults: location permission revoked - not forwarding results");
                RttServiceImpl.this.mRttMetrics.recordOverallStatus(8);
                topOfQueueRequest.callback.onRangingFailure(1);
            }
            executeNextRangingRequestIfPossible(true);
        }

        private List<RangingResult> postProcessResults(RangingRequest request, List<RttResult> results, boolean isCalledFromPrivilegedContext) {
            RttServiceSynchronized rttServiceSynchronized = this;
            RangingRequest rangingRequest = request;
            Map<MacAddress, RttResult> resultEntries = new HashMap<>();
            for (RttResult result : results) {
                resultEntries.put(MacAddress.fromBytes(result.addr), result);
            }
            List<RangingResult> finalResults = new ArrayList<>(rangingRequest.mRttPeers.size());
            for (ResponderConfig peer : rangingRequest.mRttPeers) {
                RttResult resultForRequest = resultEntries.get(peer.macAddress);
                if (resultForRequest == null) {
                    if (RttServiceImpl.this.mDbg) {
                        Log.v(RttServiceImpl.TAG, "postProcessResults: missing=" + peer.macAddress);
                    }
                    int errorCode = 1;
                    if (!isCalledFromPrivilegedContext && !peer.supports80211mc) {
                        errorCode = 2;
                    }
                    if (peer.peerHandle == null) {
                        RangingResult rangingResult = r8;
                        RangingResult rangingResult2 = new RangingResult(errorCode, peer.macAddress, 0, 0, 0, 0, 0, null, null, 0);
                        finalResults.add(rangingResult);
                    } else {
                        RangingResult rangingResult3 = r8;
                        RangingResult rangingResult4 = new RangingResult(errorCode, peer.peerHandle, 0, 0, 0, 0, 0, null, null, 0);
                        finalResults.add(rangingResult3);
                    }
                } else {
                    int status = resultForRequest.status == 0 ? 0 : 1;
                    byte[] lci = null;
                    byte[] lcr = null;
                    if (isCalledFromPrivilegedContext) {
                        lci = NativeUtil.byteArrayFromArrayList(resultForRequest.lci.data);
                        lcr = NativeUtil.byteArrayFromArrayList(resultForRequest.lcr.data);
                    }
                    byte[] lcr2 = lcr;
                    if (resultForRequest.successNumber <= 1 && resultForRequest.distanceSdInMm != 0) {
                        if (RttServiceImpl.this.mDbg) {
                            Log.w(RttServiceImpl.TAG, "postProcessResults: non-zero distance stdev with 0||1 num samples!? result=" + resultForRequest);
                        }
                        resultForRequest.distanceSdInMm = 0;
                    }
                    if (peer.peerHandle == null) {
                        RangingResult rangingResult5 = new RangingResult(status, peer.macAddress, resultForRequest.distanceInMm, resultForRequest.distanceSdInMm, resultForRequest.rssi / -2, resultForRequest.numberPerBurstPeer, resultForRequest.successNumber, lci, lcr2, resultForRequest.timeStampInUs / 1000);
                        finalResults.add(rangingResult5);
                    } else {
                        RangingResult rangingResult6 = new RangingResult(status, peer.peerHandle, resultForRequest.distanceInMm, resultForRequest.distanceSdInMm, resultForRequest.rssi / -2, resultForRequest.numberPerBurstPeer, resultForRequest.successNumber, lci, lcr2, resultForRequest.timeStampInUs / 1000);
                        finalResults.add(rangingResult6);
                    }
                }
                rttServiceSynchronized = this;
                RangingRequest rangingRequest2 = request;
            }
            return finalResults;
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("  mNextCommandId: " + this.mNextCommandId);
            pw.println("  mRttRequesterInfo: " + this.mRttRequesterInfo);
            pw.println("  mRttRequestQueue: " + this.mRttRequestQueue);
            pw.println("  mRangingTimeoutMessage: " + this.mRangingTimeoutMessage);
            RttServiceImpl.this.mRttMetrics.dump(fd, pw, args);
            this.mRttNative.dump(fd, pw, args);
        }
    }

    private class RttShellCommand extends ShellCommand {
        private Map<String, Integer> mControlParams;

        private RttShellCommand() {
            this.mControlParams = new HashMap();
        }

        public int onCommand(String cmd) {
            int uid = Binder.getCallingUid();
            if (uid == 0) {
                PrintWriter pw = getErrPrintWriter();
                try {
                    if ("reset".equals(cmd)) {
                        reset();
                        return 0;
                    } else if ("get".equals(cmd)) {
                        String name = getNextArgRequired();
                        if (!this.mControlParams.containsKey(name)) {
                            pw.println("Unknown parameter name -- '" + name + "'");
                            return -1;
                        }
                        getOutPrintWriter().println(this.mControlParams.get(name));
                        return 0;
                    } else if ("set".equals(cmd)) {
                        String name2 = getNextArgRequired();
                        String valueStr = getNextArgRequired();
                        if (!this.mControlParams.containsKey(name2)) {
                            pw.println("Unknown parameter name -- '" + name2 + "'");
                            return -1;
                        }
                        try {
                            this.mControlParams.put(name2, Integer.valueOf(valueStr));
                            return 0;
                        } catch (NumberFormatException e) {
                            pw.println("Can't convert value to integer -- '" + valueStr + "'");
                            return -1;
                        }
                    } else {
                        handleDefaultCommands(cmd);
                        return -1;
                    }
                } catch (Exception e2) {
                    pw.println("Exception: " + e2);
                }
            } else {
                throw new SecurityException("Uid " + uid + " does not have access to wifirtt commands");
            }
        }

        public void onHelp() {
            PrintWriter pw = getOutPrintWriter();
            pw.println("Wi-Fi RTT (wifirt) commands:");
            pw.println("  help");
            pw.println("    Print this help text.");
            pw.println("  reset");
            pw.println("    Reset parameters to default values.");
            pw.println("  get <name>");
            pw.println("    Get the value of the control parameter.");
            pw.println("  set <name> <value>");
            pw.println("    Set the value of the control parameter.");
            pw.println("  Control parameters:");
            Iterator<String> it = this.mControlParams.keySet().iterator();
            while (it.hasNext()) {
                pw.println("    " + it.next());
            }
            pw.println();
        }

        public int getControlParam(String name) {
            if (this.mControlParams.containsKey(name)) {
                return this.mControlParams.get(name).intValue();
            }
            Log.wtf(RttServiceImpl.TAG, "getControlParam for unknown variable: " + name);
            return 0;
        }

        public void reset() {
            this.mControlParams.put(RttServiceImpl.CONTROL_PARAM_OVERRIDE_ASSUME_NO_PRIVILEGE_NAME, 0);
        }
    }

    public RttServiceImpl(Context context) {
        this.mContext = context;
        this.mShellCommand = new RttShellCommand();
        this.mShellCommand.reset();
    }

    public void start(Looper looper, Clock clock, IWifiAwareManager awareBinder, RttNative rttNative, RttMetrics rttMetrics, WifiPermissionsUtil wifiPermissionsUtil, final FrameworkFacade frameworkFacade) {
        this.mClock = clock;
        this.mAwareBinder = awareBinder;
        this.mRttNative = rttNative;
        this.mRttMetrics = rttMetrics;
        this.mWifiPermissionsUtil = wifiPermissionsUtil;
        this.mFrameworkFacade = frameworkFacade;
        this.mRttServiceSynchronized = new RttServiceSynchronized(looper, rttNative);
        this.mActivityManager = (ActivityManager) this.mContext.getSystemService("activity");
        this.mPowerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        this.mLocationManager = (LocationManager) this.mContext.getSystemService("location");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.os.action.DEVICE_IDLE_MODE_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (RttServiceImpl.this.mDbg) {
                    Log.v(RttServiceImpl.TAG, "BroadcastReceiver: action=" + action);
                }
                if (!"android.os.action.DEVICE_IDLE_MODE_CHANGED".equals(action)) {
                    return;
                }
                if (RttServiceImpl.this.mPowerManager.isDeviceIdleMode()) {
                    RttServiceImpl.this.disable();
                } else {
                    RttServiceImpl.this.enableIfPossible();
                }
            }
        }, intentFilter);
        frameworkFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_verbose_logging_enabled"), true, new ContentObserver(this.mRttServiceSynchronized.mHandler) {
            public void onChange(boolean selfChange) {
                RttServiceImpl.this.enableVerboseLogging(frameworkFacade.getIntegerSetting(RttServiceImpl.this.mContext, "wifi_verbose_logging_enabled", 0));
            }
        });
        enableVerboseLogging(frameworkFacade.getIntegerSetting(this.mContext, "wifi_verbose_logging_enabled", 0));
        frameworkFacade.registerContentObserver(this.mContext, Settings.Global.getUriFor("wifi_rtt_background_exec_gap_ms"), true, new ContentObserver(this.mRttServiceSynchronized.mHandler) {
            public void onChange(boolean selfChange) {
                RttServiceImpl.this.updateBackgroundThrottlingInterval(frameworkFacade);
            }
        });
        updateBackgroundThrottlingInterval(frameworkFacade);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.location.MODE_CHANGED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (RttServiceImpl.this.mDbg) {
                    Log.v(RttServiceImpl.TAG, "onReceive: MODE_CHANGED_ACTION: intent=" + intent);
                }
                if (RttServiceImpl.this.mLocationManager.isLocationEnabled()) {
                    RttServiceImpl.this.enableIfPossible();
                } else {
                    RttServiceImpl.this.disable();
                }
            }
        }, intentFilter2);
        this.mRttServiceSynchronized.mHandler.post(new Runnable(rttNative) {
            private final /* synthetic */ RttNative f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                this.f$1.start(RttServiceImpl.this.mRttServiceSynchronized.mHandler);
            }
        });
    }

    /* access modifiers changed from: private */
    public void enableVerboseLogging(int verbose) {
        if (verbose > 0) {
            this.mDbg = true;
        } else {
            this.mDbg = false;
        }
        this.mRttNative.mDbg = this.mDbg;
        this.mRttMetrics.mDbg = this.mDbg;
    }

    /* access modifiers changed from: private */
    public void updateBackgroundThrottlingInterval(FrameworkFacade frameworkFacade) {
        this.mBackgroundProcessExecGapMs = frameworkFacade.getLongSetting(this.mContext, "wifi_rtt_background_exec_gap_ms", DEFAULT_BACKGROUND_PROCESS_EXEC_GAP_MS);
    }

    public int getMockableCallingUid() {
        return getCallingUid();
    }

    public void enableIfPossible() {
        if (isAvailable()) {
            sendRttStateChangedBroadcast(true);
            this.mRttServiceSynchronized.mHandler.post(new Runnable() {
                public final void run() {
                    RttServiceImpl.this.mRttServiceSynchronized.executeNextRangingRequestIfPossible(false);
                }
            });
        }
    }

    public void disable() {
        sendRttStateChangedBroadcast(false);
        this.mRttServiceSynchronized.mHandler.post(new Runnable() {
            public final void run() {
                RttServiceImpl.this.mRttServiceSynchronized.cleanUpOnDisable();
            }
        });
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [android.os.Binder] */
    /* JADX WARNING: Multi-variable type inference failed */
    public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
        this.mShellCommand.exec(this, in, out, err, args, callback, resultReceiver);
    }

    public boolean isAvailable() {
        return this.mRttNative.isReady() && !this.mPowerManager.isDeviceIdleMode() && this.mLocationManager.isLocationEnabled();
    }

    public void startRanging(IBinder binder, String callingPackage, WorkSource workSource, RangingRequest request, IRttCallback callback) throws RemoteException {
        final IBinder iBinder = binder;
        RangingRequest rangingRequest = request;
        IRttCallback iRttCallback = callback;
        if (iBinder == null) {
            throw new IllegalArgumentException("Binder must not be null");
        } else if (rangingRequest == null || rangingRequest.mRttPeers == null || rangingRequest.mRttPeers.size() == 0) {
            throw new IllegalArgumentException("Request must not be null or empty");
        } else {
            for (ResponderConfig responder : rangingRequest.mRttPeers) {
                if (responder == null) {
                    throw new IllegalArgumentException("Request must not contain null Responders");
                }
            }
            if (iRttCallback != null) {
                rangingRequest.enforceValidity(this.mAwareBinder != null);
                if (!isAvailable()) {
                    try {
                        this.mRttMetrics.recordOverallStatus(3);
                        iRttCallback.onRangingFailure(2);
                    } catch (RemoteException e) {
                        Log.e(TAG, "startRanging: disabled, callback failed -- " + e);
                    }
                    return;
                }
                final int uid = getMockableCallingUid();
                enforceAccessPermission();
                enforceChangePermission();
                String str = callingPackage;
                this.mWifiPermissionsUtil.enforceFineLocationPermission(str, uid);
                if (workSource != null) {
                    enforceLocationHardware();
                    workSource.clearNames();
                }
                boolean isCalledFromPrivilegedContext = checkLocationHardware() && this.mShellCommand.getControlParam(CONTROL_PARAM_OVERRIDE_ASSUME_NO_PRIVILEGE_NAME) == 0;
                AnonymousClass5 r8 = new IBinder.DeathRecipient() {
                    public void binderDied() {
                        if (RttServiceImpl.this.mDbg) {
                            Log.v(RttServiceImpl.TAG, "binderDied: uid=" + uid);
                        }
                        iBinder.unlinkToDeath(this, 0);
                        RttServiceImpl.this.mRttServiceSynchronized.mHandler.post(new Runnable(uid) {
                            private final /* synthetic */ int f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void run() {
                                RttServiceImpl.this.mRttServiceSynchronized.cleanUpClientRequests(this.f$1, null);
                            }
                        });
                    }
                };
                try {
                    iBinder.linkToDeath(r8, 0);
                    Handler handler = this.mRttServiceSynchronized.mHandler;
                    $$Lambda$RttServiceImpl$3Addfr11wJKJqRbBre_6uYT6vT0 r11 = r1;
                    AnonymousClass5 r16 = r8;
                    $$Lambda$RttServiceImpl$3Addfr11wJKJqRbBre_6uYT6vT0 r1 = new Runnable(workSource, uid, iBinder, r8, str, rangingRequest, iRttCallback, isCalledFromPrivilegedContext) {
                        private final /* synthetic */ WorkSource f$1;
                        private final /* synthetic */ int f$2;
                        private final /* synthetic */ IBinder f$3;
                        private final /* synthetic */ IBinder.DeathRecipient f$4;
                        private final /* synthetic */ String f$5;
                        private final /* synthetic */ RangingRequest f$6;
                        private final /* synthetic */ IRttCallback f$7;
                        private final /* synthetic */ boolean f$8;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                            this.f$4 = r5;
                            this.f$5 = r6;
                            this.f$6 = r7;
                            this.f$7 = r8;
                            this.f$8 = r9;
                        }

                        public final void run() {
                            RttServiceImpl.lambda$startRanging$3(RttServiceImpl.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7, this.f$8);
                        }
                    };
                    handler.post(r11);
                } catch (RemoteException e2) {
                    AnonymousClass5 r162 = r8;
                    RemoteException remoteException = e2;
                    Log.e(TAG, "Error on linkToDeath - " + e2);
                }
            } else {
                throw new IllegalArgumentException("Callback must not be null");
            }
        }
    }

    public static /* synthetic */ void lambda$startRanging$3(RttServiceImpl rttServiceImpl, WorkSource workSource, int uid, IBinder binder, IBinder.DeathRecipient dr, String callingPackage, RangingRequest request, IRttCallback callback, boolean isCalledFromPrivilegedContext) {
        int i;
        WorkSource sourceToUse = workSource;
        if (workSource == null || workSource.isEmpty()) {
            i = uid;
            sourceToUse = new WorkSource(i);
        } else {
            i = uid;
        }
        rttServiceImpl.mRttServiceSynchronized.queueRangingRequest(i, sourceToUse, binder, dr, callingPackage, request, callback, isCalledFromPrivilegedContext);
    }

    public void cancelRanging(WorkSource workSource) throws RemoteException {
        enforceLocationHardware();
        if (workSource != null) {
            workSource.clearNames();
        }
        if (workSource == null || workSource.isEmpty()) {
            Log.e(TAG, "cancelRanging: invalid work-source -- " + workSource);
            return;
        }
        this.mRttServiceSynchronized.mHandler.post(new Runnable(workSource) {
            private final /* synthetic */ WorkSource f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                RttServiceImpl.this.mRttServiceSynchronized.cleanUpClientRequests(0, this.f$1);
            }
        });
    }

    public void onRangingResults(int cmdId, List<RttResult> results) {
        this.mRttServiceSynchronized.mHandler.post(new Runnable(cmdId, results) {
            private final /* synthetic */ int f$1;
            private final /* synthetic */ List f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                RttServiceImpl.this.mRttServiceSynchronized.onRangingResults(this.f$1, this.f$2);
            }
        });
    }

    private void enforceAccessPermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_WIFI_STATE", TAG);
    }

    private void enforceChangePermission() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.CHANGE_WIFI_STATE", TAG);
    }

    private void enforceLocationHardware() {
        this.mContext.enforceCallingOrSelfPermission("android.permission.LOCATION_HARDWARE", TAG);
    }

    private boolean checkLocationHardware() {
        return this.mContext.checkCallingOrSelfPermission("android.permission.LOCATION_HARDWARE") == 0;
    }

    private void sendRttStateChangedBroadcast(boolean enabled) {
        Intent intent = new Intent("android.net.wifi.rtt.action.WIFI_RTT_STATE_CHANGED");
        intent.addFlags(1073741824);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
            pw.println("Permission Denial: can't dump RttService from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            return;
        }
        pw.println("Wi-Fi RTT Service");
        this.mRttServiceSynchronized.dump(fd, pw, args);
    }
}
