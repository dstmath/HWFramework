package com.android.server.wm;

import android.os.RemoteException;
import com.huawei.android.app.servertransaction.PauseActivityItemEx;

public class ClientLifecycleManagerEx {
    private ClientLifecycleManager mClientLifecycleManager;

    public ClientLifecycleManager getClientLifecycleManager() {
        return this.mClientLifecycleManager;
    }

    public void setClientLifecycleManager(ClientLifecycleManager clientLifecycleManager) {
        this.mClientLifecycleManager = clientLifecycleManager;
    }

    public void scheduleTransaction(ActivityRecordEx activityRecordEx, PauseActivityItemEx stateRequest) {
        try {
            this.mClientLifecycleManager.scheduleTransaction(activityRecordEx.getActivityRecord().app.mThread, activityRecordEx.getActivityRecord().appToken, stateRequest.getPauseActivityItem());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
