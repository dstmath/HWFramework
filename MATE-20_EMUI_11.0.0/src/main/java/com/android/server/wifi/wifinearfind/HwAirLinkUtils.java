package com.android.server.wifi.wifinearfind;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.airlink.IAirLinkAidlCallBack;
import com.huawei.airlink.IAirLinkAidlInterface;

public class HwAirLinkUtils {
    private static final String AIRLINK_PACKAGE = "com.huawei.airlink";
    private static final String AIRLINK_SERVICE_ACTION = "com.huawei.airlink.AirLinkService";
    private static final Object LOCK_OBJECT = new Object();
    private static final String TAG = HwAirLinkUtils.class.getSimpleName();
    private static volatile HwAirLinkUtils sInstance;
    private AirLinkCallback mAirLinkCallback = new AirLinkCallback();
    private IAirLinkAidlInterface mAirLinkService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.android.server.wifi.wifinearfind.HwAirLinkUtils.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(HwAirLinkUtils.TAG, "onServiceConnected, enter onServiceConnected");
            HwAirLinkUtils.this.mAirLinkService = IAirLinkAidlInterface.Stub.asInterface(service);
            HwAirLinkUtils.this.registerCallback();
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Log.d(HwAirLinkUtils.TAG, "onServiceDisconnected, enter onServiceDisconnected");
            HwAirLinkUtils.this.mAirLinkService = null;
        }
    };

    private HwAirLinkUtils() {
    }

    public static HwAirLinkUtils getInstance() {
        if (sInstance == null) {
            synchronized (LOCK_OBJECT) {
                if (sInstance == null) {
                    sInstance = new HwAirLinkUtils();
                }
            }
        }
        return sInstance;
    }

    public boolean bindAirLinkService(Context context) {
        if (context == null) {
            Log.e(TAG, "bindAirLinkService, context is null");
            return false;
        }
        Intent intent = new Intent();
        intent.setAction(AIRLINK_SERVICE_ACTION);
        intent.setPackage(AIRLINK_PACKAGE);
        try {
            boolean result = context.bindService(intent, this.mServiceConnection, 1);
            String str = TAG;
            Log.d(str, "bindAirLinkService, bind result = " + result);
            return result;
        } catch (SecurityException e) {
            Log.e(TAG, "bindAirLinkService, bind failed");
            return false;
        }
    }

    public void unBindAirLinkService(Context context) {
        ServiceConnection serviceConnection = this.mServiceConnection;
        if (serviceConnection == null || context == null) {
            Log.d(TAG, "unBindAirLinkService, mServiceConnection is null");
        } else {
            context.unbindService(serviceConnection);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void registerCallback() {
        IAirLinkAidlInterface iAirLinkAidlInterface = this.mAirLinkService;
        if (iAirLinkAidlInterface == null) {
            Log.e(TAG, "registerCallback, mAirLinkService is null");
            return;
        }
        try {
            boolean result = iAirLinkAidlInterface.registerCallback(this.mAirLinkCallback);
            String str = TAG;
            Log.d(str, "registerCallback, register result = " + result);
        } catch (RemoteException e) {
            Log.e(TAG, "registerCallback, register failed");
        }
    }

    private void unRegisterCallback() {
        IAirLinkAidlInterface iAirLinkAidlInterface = this.mAirLinkService;
        if (iAirLinkAidlInterface == null) {
            Log.e(TAG, "unRegisterCallback, mAirLinkService is null");
            return;
        }
        try {
            boolean result = iAirLinkAidlInterface.unRegisterCallback();
            String str = TAG;
            Log.d(str, "unRegisterCallback, unRegister result = " + result);
        } catch (RemoteException e) {
            Log.e(TAG, "unRegisterCallback, unRegister failed");
        }
    }

    public void sendDeviceFoundOperation(String event, Bundle bundle) {
        IAirLinkAidlInterface iAirLinkAidlInterface = this.mAirLinkService;
        if (iAirLinkAidlInterface == null) {
            Log.e(TAG, "sendDeviceFoundOperation, mAirLinkService is null");
            return;
        }
        try {
            iAirLinkAidlInterface.commonOperation(event, bundle);
        } catch (RemoteException e) {
            Log.e(TAG, "sendDeviceFoundOperation, send failed");
        }
    }

    /* access modifiers changed from: private */
    public class AirLinkCallback extends IAirLinkAidlCallBack.Stub {
        private AirLinkCallback() {
        }

        @Override // com.huawei.airlink.IAirLinkAidlCallBack
        public void onEventReceived(String event, Bundle bundle) {
            if (TextUtils.isEmpty(event) || bundle == null) {
                Log.e(HwAirLinkUtils.TAG, "onEventReceived, event or bundle is null");
                return;
            }
            String str = HwAirLinkUtils.TAG;
            Log.d(str, "onEventReceived, event is " + event);
        }
    }
}
