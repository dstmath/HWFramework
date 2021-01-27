package ohos.backgroundtaskmgr;

import ohos.app.Context;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IBackgroundTaskManager extends IRemoteBroker {
    public static final int CANCEL_SUSPEND_DELAY = 4;
    public static final String DESCRIPTOR = "ohos.resourceschedule.IBackgroundTaskMgr";
    public static final int GET_REMAINING_DELAY = 5;
    public static final int REQUEST_SUSPEND_DELAY = 3;

    void cancelSuspendDelay(Context context, int i) throws RemoteException;

    int getRemainingDelayTime(Context context, int i) throws RemoteException;

    DelaySuspendInfo requestSuspendDelay(Context context, String str, ExpiredCallback expiredCallback) throws RemoteException;
}
