package com.android.server.emcom.grabservice;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

public class NotificationListener extends NotificationListenerService {
    private static final String TAG = "GrabService";
    private boolean mIsRegister = false;

    public void onNotificationPosted(StatusBarNotification sbn) {
        if (sbn != null) {
            if ("com.tencent.mm".equals(sbn.getPackageName())) {
                Notification localNotification = sbn.getNotification();
                if (localNotification != null) {
                    sendWechatPacketReceiveMsg(localNotification);
                }
            }
        }
    }

    public void onNotificationRemoved(StatusBarNotification arg0) {
    }

    private void sendWechatPacketReceiveMsg(Notification localWechatMessage) {
        Handler handler = AutoGrabService.getHandler();
        if (handler == null) {
            Log.w(TAG, "AutoGrabService handler is null.");
        } else {
            Message.obtain(handler, 6, localWechatMessage).sendToTarget();
        }
    }

    public int onStartCommand(Intent paramIntent, int paramInt1, int paramInt2) {
        if (!this.mIsRegister) {
            try {
                registerAsSystemService(getApplicationContext(), new ComponentName(getApplicationContext().getPackageName(), getClass().getCanonicalName()), -1);
                this.mIsRegister = true;
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to register notification listener");
            }
        }
        return 1;
    }

    public void onDestroy() {
        try {
            unregisterAsSystemService();
            this.mIsRegister = false;
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to unregister notification listener");
        }
        super.onDestroy();
    }
}
