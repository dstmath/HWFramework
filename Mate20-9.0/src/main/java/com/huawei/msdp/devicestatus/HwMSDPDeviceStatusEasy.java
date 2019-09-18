package com.huawei.msdp.devicestatus;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.msdp.devicestatus.IMSDPDeviceStatusChangedCallBack;
import com.huawei.msdp.devicestatus.IMSDPDeviceStatusService;
import java.util.HashSet;
import java.util.Set;

public class HwMSDPDeviceStatusEasy {
    private static final String AIDL_MESSAGE_SERVICE_CLASS = "com.huawei.msdp.devicestatus.HwMSDPDeviceStatusService";
    private static final String AIDL_MESSAGE_SERVICE_PACKAGE = "com.huawei.msdp";
    /* access modifiers changed from: private */
    public static final String TAG = HwMSDPDeviceStatusEasy.class.getSimpleName();
    private Intent bindIntent;
    private final IMSDPDeviceStatusChangedCallBack.Stub deviceStatusChangedCallBack = new IMSDPDeviceStatusChangedCallBack.Stub() {
        public void onDeviceStatusChanged(HwMSDPDeviceStatusChangeEvent event) throws RemoteException {
            if (HwMSDPDeviceStatusEasy.this.mCallBack != null) {
                HwMSDPDeviceStatusEasy.this.mCallBack.onDeviceStatusChanged(event);
            }
        }
    };
    /* access modifiers changed from: private */
    public final Set<EnableEvent> enableEvents = new HashSet();
    /* access modifiers changed from: private */
    public HwMSDPDeviceStatusChangedCallBack mCallBack = null;
    private final ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(HwMSDPDeviceStatusEasy.TAG, "onServiceConnected");
            IMSDPDeviceStatusService unused = HwMSDPDeviceStatusEasy.this.mService = IMSDPDeviceStatusService.Stub.asInterface(service);
            HwMSDPDeviceStatusEasy.this.processRegister();
            HwMSDPDeviceStatusEasy.this.link2Death();
            HwMSDPDeviceStatusEasy.this.processRegisterCallback();
        }

        public void onServiceDisconnected(ComponentName name) {
            IMSDPDeviceStatusService unused = HwMSDPDeviceStatusEasy.this.mService = null;
            Log.d(HwMSDPDeviceStatusEasy.TAG, "onServiceDisconnected");
        }
    };
    private Context mContext;
    /* access modifiers changed from: private */
    public IMSDPDeviceStatusService mService = null;
    private String packageName;
    private final IBinder.DeathRecipient recipient = new IBinder.DeathRecipient() {
        public void binderDied() {
            synchronized (HwMSDPDeviceStatusEasy.this.enableEvents) {
                for (EnableEvent enableEvent : HwMSDPDeviceStatusEasy.this.enableEvents) {
                    enableEvent.isEnable = false;
                }
                HwMSDPDeviceStatusEasy.this.bindService();
            }
        }
    };

    private static class EnableEvent {
        String deviceStatus;
        int eventType;
        boolean isEnable = false;
        long reportLatencyNs;

        EnableEvent(String deviceStatus2, int eventType2, long reportLatencyNs2) {
            this.deviceStatus = deviceStatus2;
            this.eventType = eventType2;
            this.reportLatencyNs = reportLatencyNs2;
        }

        EnableEvent(String deviceStatus2, int eventType2) {
            this.deviceStatus = deviceStatus2;
            this.eventType = eventType2;
        }

        public boolean equals(Object o) {
            boolean z = true;
            if (this == o) {
                return true;
            }
            if (!(o instanceof EnableEvent)) {
                return false;
            }
            EnableEvent that = (EnableEvent) o;
            if (this.eventType != that.eventType || !this.deviceStatus.equals(that.deviceStatus)) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (31 * this.deviceStatus.hashCode()) + this.eventType;
        }
    }

    /* access modifiers changed from: private */
    public void processRegisterCallback() {
        if (this.mService != null && this.mCallBack != null && this.packageName != null) {
            try {
                this.mService.registerDeviceStatusCallBack(this.packageName, this.deviceStatusChangedCallBack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void freeRegisterCallback() {
        if (this.mService != null && this.mCallBack != null && this.packageName != null) {
            try {
                this.mService.freeDeviceStatusService(this.packageName, this.deviceStatusChangedCallBack);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: private */
    public void link2Death() {
        if (this.mService != null) {
            try {
                this.mService.asBinder().linkToDeath(this.recipient, 0);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    private void unlink2Death() {
        Log.d(TAG, "unlink2Death");
        if (this.mService != null) {
            this.mService.asBinder().unlinkToDeath(this.recipient, 0);
        }
    }

    /* access modifiers changed from: private */
    public void processRegister() {
        synchronized (this.enableEvents) {
            if (this.mService != null) {
                for (EnableEvent enableEvent : this.enableEvents) {
                    if (!enableEvent.isEnable) {
                        try {
                            if (this.mService.enableDeviceStatusService(this.packageName, enableEvent.deviceStatus, enableEvent.eventType, enableEvent.reportLatencyNs)) {
                                enableEvent.isEnable = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                bindService();
            }
        }
    }

    public HwMSDPDeviceStatusEasy(Context context, HwMSDPDeviceStatusChangedCallBack callBack) {
        this.mContext = context;
        if (context != null) {
            this.packageName = context.getPackageName();
        }
        this.mCallBack = callBack;
    }

    /* access modifiers changed from: private */
    public synchronized void bindService() {
        if (this.mContext != null) {
            this.bindIntent = new Intent();
            this.bindIntent.setClassName(AIDL_MESSAGE_SERVICE_PACKAGE, AIDL_MESSAGE_SERVICE_CLASS);
            this.mContext.bindService(this.bindIntent, this.mConnection, 1);
        }
    }

    public boolean registerEvent(String deviceStatus, int eventType, long reportLatencyNs) {
        boolean result;
        synchronized (this.enableEvents) {
            result = this.enableEvents.add(new EnableEvent(deviceStatus, eventType, reportLatencyNs));
            processRegister();
        }
        return result;
    }

    public boolean unregisterEvent(String deviceStatus, int eventType) {
        boolean result;
        synchronized (this.enableEvents) {
            this.enableEvents.remove(new EnableEvent(deviceStatus, eventType));
            result = false;
            try {
                if (this.mService != null) {
                    if (this.enableEvents.isEmpty()) {
                        result = this.mService.disableDeviceStatusService(this.packageName, deviceStatus, eventType);
                        freeRegisterCallback();
                        unlink2Death();
                        if (!(this.mContext == null || this.mService == null)) {
                            Log.d(TAG, "unbindService");
                            this.mContext.unbindService(this.mConnection);
                            this.mService = null;
                        }
                    } else {
                        result = this.mService.disableDeviceStatusService(this.packageName, deviceStatus, eventType);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "unregisterEvent Exception");
                return false;
            }
        }
        return result;
    }

    public String[] getSupports() {
        try {
            return this.mService.getSupportDeviceStatus();
        } catch (Exception e) {
            Log.e(TAG, "getSupports Exception");
            return new String[0];
        }
    }

    public HwMSDPDeviceStatusChangeEvent getCurrentDeviceStatus() {
        if (this.mService != null) {
            try {
                return this.mService.getCurrentDeviceStatus(this.packageName);
            } catch (Exception e) {
                Log.d(TAG, "getCurrentDeviceStatus error");
            }
        }
        return new HwMSDPDeviceStatusChangeEvent(new HwMSDPDeviceStatusEvent[0]);
    }

    public void onDestory() {
        for (EnableEvent enableEvent : (EnableEvent[]) this.enableEvents.toArray(new EnableEvent[0])) {
            unregisterEvent(enableEvent.deviceStatus, enableEvent.eventType);
        }
    }
}
