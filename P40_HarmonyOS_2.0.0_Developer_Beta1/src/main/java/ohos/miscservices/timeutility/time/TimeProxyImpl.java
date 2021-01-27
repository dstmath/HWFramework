package ohos.miscservices.timeutility.time;

import android.app.IAlarmManager;
import android.os.ServiceManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class TimeProxyImpl {
    private static final String ALARM_SERVICE = "alarm";
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "TimeProxyImpl");
    private IAlarmManager mService;

    public TimeProxyImpl() {
        tryInit();
    }

    private boolean tryInit() {
        this.mService = IAlarmManager.Stub.asInterface(ServiceManager.getService(ALARM_SERVICE));
        return this.mService != null;
    }

    public void setTime(long j) throws RemoteException {
        if (this.mService != null || tryInit()) {
            try {
                this.mService.setTime(j);
            } catch (android.os.RemoteException unused) {
                HiLog.error(TAG, "time service IPC error !", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "Can not get time service proxy!", new Object[0]);
            throw new RemoteException();
        }
    }

    public void setTimeZone(String str) throws RemoteException {
        if (this.mService != null || tryInit()) {
            try {
                this.mService.setTimeZone(str);
            } catch (android.os.RemoteException | IllegalArgumentException unused) {
                HiLog.error(TAG, "timezone service IPC error !", new Object[0]);
                throw new RemoteException();
            }
        } else {
            HiLog.error(TAG, "Can not get service proxy!", new Object[0]);
            throw new RemoteException();
        }
    }
}
