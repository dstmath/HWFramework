package com.android.server.mtm.iaware.brjob.controller;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.rms.iaware.AwareLog;
import com.android.internal.annotations.GuardedBy;
import com.android.server.mtm.iaware.brjob.AwareJobSchedulerConstants;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobSchedulerService;
import com.android.server.mtm.iaware.brjob.scheduler.AwareJobStatus;
import com.android.server.mtm.iaware.brjob.scheduler.AwareStateChangedListener;
import com.huawei.android.bluetooth.BluetoothAdapterExt;
import com.huawei.android.content.ContextEx;
import com.huawei.android.os.UserHandleEx;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BluetoothStatusController extends AwareStateController {
    private static final String CONDITION_NAME = "BluetoothStatus";
    private static final Object CREATE_LOCK = new Object();
    private static final String TAG = "BluetoothStatusController";
    private static BluetoothStatusController sSingleton;
    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        /* class com.android.server.mtm.iaware.brjob.controller.BluetoothStatusController.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED".equals(intent.getAction())) {
                int state = intent.getIntExtra("android.bluetooth.adapter.extra.CONNECTION_STATE", -1);
                if (BluetoothStatusController.this.debug) {
                    AwareLog.i(BluetoothStatusController.TAG, "iaware_brjob bluetooth receiver, state:" + state);
                }
                if (state == 0) {
                    BluetoothStatusController.this.updateTrackedJobs(AwareJobSchedulerConstants.BLUETOOTH_STATUS_DISCONNECTED);
                } else if (state == 2) {
                    BluetoothStatusController.this.updateTrackedJobs(AwareJobSchedulerConstants.BLUETOOTH_STATUS_CONNECTED);
                }
            }
        }
    };
    private String mBluetoothState = "UNKNOWN";
    @GuardedBy({"mLock"})
    private final ArrayList<AwareJobStatus> mTrackedJobs = new ArrayList<>();

    private BluetoothStatusController(AwareStateChangedListener stateChangedListener, Context context, Object lock) {
        super(stateChangedListener, context, lock);
        if (this.mContext != null) {
            ContextEx.registerReceiverAsUser(this.mContext, this.mBluetoothReceiver, UserHandleEx.SYSTEM, new IntentFilter("android.bluetooth.adapter.action.CONNECTION_STATE_CHANGED"), (String) null, (Handler) null);
        }
    }

    public static BluetoothStatusController get(AwareJobSchedulerService jms) {
        BluetoothStatusController bluetoothStatusController;
        synchronized (CREATE_LOCK) {
            if (sSingleton == null) {
                sSingleton = new BluetoothStatusController(jms, jms.getContext(), jms.getLock());
            }
            bluetoothStatusController = sSingleton;
        }
        return bluetoothStatusController;
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStartTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("BluetoothStatus")) {
            String filterValue = job.getActionFilterValue("BluetoothStatus");
            this.mBluetoothState = getBluetoothState();
            if (this.mBluetoothState.equals(filterValue)) {
                job.setSatisfied("BluetoothStatus", true);
            } else {
                job.setSatisfied("BluetoothStatus", false);
            }
            addJobLocked(this.mTrackedJobs, job);
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void maybeStopTrackingJobLocked(AwareJobStatus job) {
        if (job != null && job.hasConstraint("BluetoothStatus")) {
            if (this.debug) {
                AwareLog.i(TAG, "iaware_brjob stop tracking begin");
            }
            this.mTrackedJobs.remove(job);
        }
    }

    @Override // com.android.server.mtm.iaware.brjob.controller.AwareStateController
    public void dump(PrintWriter pw) {
        if (pw != null) {
            pw.println("    BluetoothStatusController tracked job num: " + this.mTrackedJobs.size());
            pw.println("        now bluetooth state is " + getBluetoothState());
        }
    }

    private String getBluetoothState() {
        BluetoothAdapter blueAdapter = BluetoothAdapter.getDefaultAdapter();
        String bluetoothState = "UNKNOWN";
        if (blueAdapter == null) {
            return bluetoothState;
        }
        if (blueAdapter.isEnabled()) {
            int connectState = BluetoothAdapterExt.getConnectionState(blueAdapter);
            if (connectState == 0) {
                bluetoothState = AwareJobSchedulerConstants.BLUETOOTH_STATUS_DISCONNECTED;
            } else if (connectState == 2) {
                bluetoothState = AwareJobSchedulerConstants.BLUETOOTH_STATUS_CONNECTED;
            }
        } else {
            bluetoothState = AwareJobSchedulerConstants.BLUETOOTH_STATUS_DISCONNECTED;
        }
        if (this.debug) {
            AwareLog.i(TAG, "iaware_brjob bluetooth state:" + bluetoothState);
        }
        return bluetoothState;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTrackedJobs(String btState) {
        synchronized (this.mLock) {
            boolean changeToSatisfied = false;
            if (!this.mBluetoothState.equals(btState)) {
                this.mBluetoothState = btState;
                List<AwareJobStatus> changedJobs = new ArrayList<>();
                Iterator<AwareJobStatus> it = this.mTrackedJobs.iterator();
                while (it.hasNext()) {
                    AwareJobStatus job = it.next();
                    String filterValue = job.getActionFilterValue("BluetoothStatus");
                    if (this.mBluetoothState.equals(filterValue) && !job.isSatisfied("BluetoothStatus")) {
                        job.setSatisfied("BluetoothStatus", true);
                        changeToSatisfied = true;
                        changedJobs.add(job);
                    } else if (!this.mBluetoothState.equals(filterValue) && job.isSatisfied("BluetoothStatus")) {
                        job.setSatisfied("BluetoothStatus", false);
                    }
                }
                if (changeToSatisfied) {
                    if (this.debug) {
                        AwareLog.i(TAG, "iaware_brjob onControllerStateChanged");
                    }
                    this.mStateChangedListener.onControllerStateChanged(changedJobs);
                }
            }
        }
    }
}
