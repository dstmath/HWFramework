package com.huawei.reliability.faultnotify;

import android.app.Application;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.zrhung.IFaultEventService;

public class AppFaultManager {
    private static final String FAULT_NOTIFY_SERVER_NAME = "hwFaultNotifyService";
    private static final String TAG = "AppFaultManager";
    private static AppFaultManager appFaultManager = new AppFaultManager();

    private AppFaultManager() {
    }

    public static AppFaultManager getInstance() {
        return appFaultManager;
    }

    public boolean addListener(FaultEventListener faultEventListener, int faultType) {
        try {
            IBinder binder = ServiceManager.getService(FAULT_NOTIFY_SERVER_NAME);
            if (binder != null) {
                return IFaultEventService.Stub.asInterface(binder).registerCallback(Application.getProcessName(), faultEventListener, faultType);
            }
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to register service");
            return false;
        }
    }

    public void removeListener() {
        try {
            IBinder binder = ServiceManager.getService(FAULT_NOTIFY_SERVER_NAME);
            if (binder != null) {
                IFaultEventService.Stub.asInterface(binder).unRegisterCallback(Application.getProcessName());
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to unRegister service");
        }
    }
}
