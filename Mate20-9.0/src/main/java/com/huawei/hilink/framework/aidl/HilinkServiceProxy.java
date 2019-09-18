package com.huawei.hilink.framework.aidl;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.hilink.framework.aidl.IHilinkService;
import java.io.Closeable;

public class HilinkServiceProxy implements Closeable {
    public static final int ERRORCODE_BAD_REQUEST = 400;
    public static final int ERRORCODE_INITIALIZATION_FAILURE = 12;
    public static final int ERRORCODE_MAX_SERVICE_NUM_REACHED = 10;
    public static final int ERRORCODE_METHOD_NOT_ALLOWED = 405;
    public static final int ERRORCODE_NOT_FOUND = 404;
    public static final int ERRORCODE_NO_NETWORK = 3;
    public static final int ERRORCODE_NULLPOINTER = 2;
    public static final int ERRORCODE_OK = 0;
    public static final int ERRORCODE_PERMISSION_DENIED = 11;
    public static final int ERRORCODE_REQUEST_ILLEGAL = 5;
    public static final int ERRORCODE_REQUEST_NOLONGER_EXIST = 8;
    public static final int ERRORCODE_RUNTIME = 4;
    public static final int ERRORCODE_SERVICE_ALREADY_EXIST = 7;
    public static final int ERRORCODE_TASK_QUEUE_FULL = 6;
    private static final String TAG = "hilinkService";
    private ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(HilinkServiceProxy.TAG, "onServiceConnected");
            HilinkServiceProxy.this.hilinkServiceBinder = IHilinkService.Stub.asInterface(service);
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.i(HilinkServiceProxy.TAG, "onServiceDisconnected");
            HilinkServiceProxy.this.hilinkServiceBinder = null;
        }
    };
    private Context context;
    /* access modifiers changed from: private */
    public IHilinkService hilinkServiceBinder;

    public HilinkServiceProxy(Context context2) {
        if (context2 != null) {
            this.context = context2;
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.huawei.hilink.framework", "com.huawei.hilink.framework.HilinkService"));
            context2.bindService(intent, this.conn, 1);
        }
    }

    public int discover(DiscoverRequest request, ServiceFoundCallbackWrapper callback) {
        if (this.hilinkServiceBinder == null) {
            return 12;
        }
        try {
            return this.hilinkServiceBinder.discover(request, callback);
        } catch (RemoteException e) {
            return 4;
        }
    }

    public int call(CallRequest request, ResponseCallbackWrapper callback) {
        if (this.hilinkServiceBinder == null) {
            return 12;
        }
        try {
            return this.hilinkServiceBinder.call(request, callback);
        } catch (RemoteException e) {
            return 4;
        }
    }

    public int publish(String serviceType, String serviceID, RequestHandlerWrapper requestHandler) {
        if (this.hilinkServiceBinder == null) {
            return 12;
        }
        try {
            return this.hilinkServiceBinder.publishKeepOnline(serviceType, serviceID, requestHandler);
        } catch (RemoteException e) {
            return 4;
        }
    }

    public int publish(String serviceType, String serviceID, PendingIntent pendingIntent) {
        if (this.hilinkServiceBinder == null) {
            return 12;
        }
        try {
            return this.hilinkServiceBinder.publishCanbeOffline(serviceType, serviceID, pendingIntent);
        } catch (RemoteException e) {
            return 4;
        }
    }

    public static CallRequest getCallRequest(Intent intent) {
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                return (CallRequest) bundle.get("CallRequest");
            }
        }
        return null;
    }

    public void unpublish(String serviceID) {
        if (this.hilinkServiceBinder != null) {
            try {
                this.hilinkServiceBinder.unpublish(serviceID);
            } catch (RemoteException e) {
                Log.e(TAG, "unpublish failed");
            }
        }
    }

    public int sendResponse(int errorCode, String payload, int requestID) {
        if (this.hilinkServiceBinder == null) {
            return 12;
        }
        try {
            return this.hilinkServiceBinder.sendResponse(errorCode, payload, requestID);
        } catch (RemoteException e) {
            return 4;
        }
    }

    public void close() {
        if (this.context != null) {
            this.context.unbindService(this.conn);
            this.hilinkServiceBinder = null;
        }
    }
}
