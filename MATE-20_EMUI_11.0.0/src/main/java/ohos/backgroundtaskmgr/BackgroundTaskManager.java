package ohos.backgroundtaskmgr;

import ohos.app.Context;
import ohos.rpc.RemoteException;

public class BackgroundTaskManager {
    private final Context mContext;

    public BackgroundTaskManager(Context context) {
        this.mContext = context;
    }

    public DelaySuspendInfo requestSuspendDelay(String str, ExpiredCallback expiredCallback) throws RemoteException {
        return BackgroundTaskManagerProxy.getProxy().requestSuspendDelay(this.mContext, str, expiredCallback);
    }

    public void cancelSuspendDelay(int i) throws RemoteException {
        BackgroundTaskManagerProxy.getProxy().cancelSuspendDelay(this.mContext, i);
    }

    public int getRemainingDelayTime(int i) throws RemoteException {
        return BackgroundTaskManagerProxy.getProxy().getRemainingDelayTime(this.mContext, i);
    }
}
