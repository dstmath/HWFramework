package ohos.workschedulerservice.controller;

import java.io.PrintWriter;
import java.util.Iterator;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.IntentFilter;
import ohos.app.Context;
import ohos.event.commonevent.CommonEventData;
import ohos.event.commonevent.CommonEventManager;
import ohos.event.commonevent.CommonEventSubscribeInfo;
import ohos.event.commonevent.CommonEventSubscriber;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.net.NetCapabilities;
import ohos.net.NetHandle;
import ohos.net.NetManager;
import ohos.net.NetStatusCallback;
import ohos.rpc.RemoteException;
import ohos.utils.LightweightSet;
import ohos.workschedulerservice.WorkQueueManager;

public final class NetworkStateListener extends StateListener {
    private static final HiLogLabel LOG_LABEL = new HiLogLabel(3, 218109696, TAG);
    private static final String TAG = "NetworkStateListener";
    private Context context;
    private boolean listenerEnable;
    private final Object lock = new Object();
    private NetManager netManager;
    private final NetStatusCallback netStatusCallback = new NetStatusCallback() {
        /* class ohos.workschedulerservice.controller.NetworkStateListener.AnonymousClass1 */

        @Override // ohos.net.NetStatusCallback
        public void onCapabilitiesChanged(NetHandle netHandle, NetCapabilities netCapabilities) {
            if (NetworkStateListener.this.isNetworkTypeChanged(netCapabilities)) {
                NetworkStateListener.this.updateTrackedWorks();
            }
            HiLog.debug(NetworkStateListener.LOG_LABEL, "onCapabilitiesChanged", new Object[0]);
        }

        @Override // ohos.net.NetStatusCallback
        public void onLost(NetHandle netHandle) {
            HiLog.info(NetworkStateListener.LOG_LABEL, "onLost  ", new Object[0]);
            synchronized (NetworkStateListener.this.lock) {
                if (NetworkStateListener.this.networkType != 0) {
                    NetworkStateListener.this.networkType = 0;
                    NetworkStateListener.this.updateTrackedWorks();
                }
            }
        }
    };
    private NetworkStatusEventSubscriber networkStatusEventSubscriber;
    private int networkType;
    private final LightweightSet<WorkStatus> trackedTasks = new LightweightSet<>();

    public NetworkStateListener(WorkQueueManager workQueueManager) {
        super(workQueueManager);
    }

    public void init() {
        if (this.netManager == null && this.workQueueMgr != null) {
            this.context = new Ability();
            this.netManager = NetManager.getInstance(this.context);
            if (this.netManager.addDefaultNetStatusCallback(this.netStatusCallback)) {
                this.listenerEnable = true;
                HiLog.info(LOG_LABEL, "init NetStatusCallback success", new Object[0]);
                return;
            }
            registerNetworkStatus();
            HiLog.info(LOG_LABEL, "callback failed, init NetStatusCommonEvent instead", new Object[0]);
        }
    }

    private void updateListenerStatus() {
        if (this.trackedTasks.isEmpty()) {
            if (this.listenerEnable) {
                this.listenerEnable = !this.netManager.removeNetStatusCallback(this.netStatusCallback);
            }
        } else if (!this.listenerEnable) {
            this.listenerEnable = this.netManager.addDefaultNetStatusCallback(this.netStatusCallback);
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void tryStartSignWork(WorkStatus workStatus) {
        synchronized (this.lock) {
            if (workStatus.hasConnectivityCondition()) {
                this.trackedTasks.add(workStatus);
                workStatus.changeNetWorkSatisfiedCondition(this.networkType);
                updateListenerStatus();
            }
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void tryStopSignWork(WorkStatus workStatus) {
        synchronized (this.lock) {
            this.trackedTasks.remove(workStatus);
            updateListenerStatus();
        }
    }

    @Override // ohos.workschedulerservice.controller.StateListener
    public void dumpStateListenerStatus(PrintWriter printWriter, String str) {
        if (printWriter == null || str == null) {
            HiLog.error(LOG_LABEL, "error dump PrintWriter or prefix input", new Object[0]);
            return;
        }
        printWriter.println();
        printWriter.println("NetworkStateListener:");
        synchronized (this.lock) {
            printWriter.println(str + "networkType format:" + Integer.toBinaryString(this.networkType));
        }
    }

    private int convertFromCapability(NetCapabilities netCapabilities) {
        if (netCapabilities == null) {
            HiLog.error(LOG_LABEL, "convertFromCapability networkCapabilities is null ", new Object[0]);
            return 0;
        } else if (!netCapabilities.hasCapability(16)) {
            return 0;
        } else {
            int i = netCapabilities.hasBearer(3) ? 32 : 0;
            if (netCapabilities.hasCapability(6)) {
                i |= 16;
            }
            if (netCapabilities.hasBearer(1)) {
                i |= 4;
            }
            if (netCapabilities.hasBearer(2)) {
                i |= 8;
            }
            if (netCapabilities.hasBearer(0)) {
                i |= 2;
            }
            return i != 0 ? i | 1 : i;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isNetworkTypeChanged(NetCapabilities netCapabilities) {
        boolean z;
        if (netCapabilities == null) {
            netCapabilities = this.netManager.getNetCapabilities(this.netManager.getDefaultNet());
        }
        int convertFromCapability = convertFromCapability(netCapabilities);
        synchronized (this.lock) {
            if (convertFromCapability != this.networkType) {
                this.networkType = convertFromCapability;
                z = true;
            } else {
                z = false;
            }
        }
        return z;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTrackedWorks() {
        HiLog.debug(LOG_LABEL, "updateTrackedJobs", new Object[0]);
        synchronized (this.lock) {
            Iterator<WorkStatus> it = this.trackedTasks.iterator();
            while (it.hasNext()) {
                WorkStatus next = it.next();
                if (next.changeNetWorkSatisfiedCondition(this.networkType)) {
                    this.workQueueMgr.onDeviceStateChanged(next, 4);
                }
            }
        }
    }

    private void registerNetworkStatus() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("usual.event.CONNECTIVITY_CHANGE");
        this.networkStatusEventSubscriber = new NetworkStatusEventSubscriber(new CommonEventSubscribeInfo(intentFilter));
        try {
            CommonEventManager.subscribeCommonEvent(this.networkStatusEventSubscriber);
        } catch (RemoteException unused) {
            HiLog.info(LOG_LABEL, "subscribeCommonEvent occur exception.", new Object[0]);
        }
    }

    public final class NetworkStatusEventSubscriber extends CommonEventSubscriber {
        NetworkStatusEventSubscriber(CommonEventSubscribeInfo commonEventSubscribeInfo) {
            super(commonEventSubscribeInfo);
        }

        public void onReceiveEvent(CommonEventData commonEventData) {
            Intent intent;
            if (commonEventData != null && (intent = commonEventData.getIntent()) != null) {
                HiLog.info(NetworkStateListener.LOG_LABEL, "NetworkStatusListener onStorageStatusChanged action%{public}s.", intent.getAction());
                if (NetworkStateListener.this.isNetworkTypeChanged(null)) {
                    NetworkStateListener.this.updateTrackedWorks();
                }
            }
        }
    }
}
