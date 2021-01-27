package com.android.server.swing;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.swing.HwAwarenessServiceConnector;
import com.android.server.swing.notification.HwSwingEventAvailabler;
import com.android.server.swing.notification.HwSwingFaceTrackingProcessor;
import com.android.server.swing.notification.HwSwingHideNotificationAvailabler;
import com.android.server.swing.notification.HwSwingMultiEyeGazeProcessor;
import com.android.server.swing.notification.IHwSwingEventDispatcher;
import com.huawei.systemserver.swing.IHwSwingEventNotifier;

public class HwSwingSystemUIHub implements IHwSwingEventDispatcher, HwAwarenessServiceConnector.Listener {
    private static final String ACTION_SYSTEMUI_SWING_SERVICE = "com.android.systemui.swing.HwSwingService";
    private static final int NOTIFICATION_STATE_HIDE = 0;
    private static final int NOTIFICATION_STATE_SHOW = 1;
    private static final int NOTIFICATION_STATE_UNKNOW = -1;
    private static final String PKG_SYSTEMUI = "com.android.systemui";
    private static final String TAG = "HwSwingSystemUIHub";
    private HwAwarenessServiceConnector mAwarenessServiceConnector;
    private Context mContext;
    private HwSwingFaceTrackingProcessor mFaceTrackingProcessor;
    private IHwSwingEventNotifier mHwSwingEventNotifier;
    private boolean mIsConnect;
    private HwSwingMultiEyeGazeProcessor mMultiEyeGazeProcessor;
    private int mNotificationState = -1;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        /* class com.android.server.swing.HwSwingSystemUIHub.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(HwSwingSystemUIHub.TAG, "onServiceConnected");
            HwSwingSystemUIHub.this.mIsConnect = true;
            HwSwingSystemUIHub.this.mHwSwingEventNotifier = IHwSwingEventNotifier.Stub.asInterface(service);
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName name) {
            Log.i(HwSwingSystemUIHub.TAG, "onServiceDisconnected");
            HwSwingSystemUIHub.this.mIsConnect = false;
            HwSwingSystemUIHub.this.mHwSwingEventNotifier = null;
        }
    };

    public HwSwingSystemUIHub(Context context) {
        this.mContext = context;
    }

    public void start() {
        this.mAwarenessServiceConnector = new HwAwarenessServiceConnector(this.mContext);
        this.mAwarenessServiceConnector.addListener(this);
        HwSwingEventAvailabler eventAvailabler = new HwSwingHideNotificationAvailabler(this.mContext);
        this.mMultiEyeGazeProcessor = new HwSwingMultiEyeGazeProcessor(this, eventAvailabler);
        this.mAwarenessServiceConnector.addListener(this.mMultiEyeGazeProcessor);
        this.mFaceTrackingProcessor = new HwSwingFaceTrackingProcessor(this, eventAvailabler);
        this.mAwarenessServiceConnector.addListener(this.mFaceTrackingProcessor);
        this.mAwarenessServiceConnector.connectService();
        bindSystemUISwingEventService(this.mContext);
    }

    private void bindSystemUISwingEventService(Context context) {
        if (context != null) {
            Intent intent = new Intent(ACTION_SYSTEMUI_SWING_SERVICE);
            intent.setPackage("com.android.systemui");
            context.bindService(intent, this.mServiceConnection, 1);
        }
    }

    @Override // com.android.server.swing.HwAwarenessServiceConnector.Listener
    public void onServiceConnectedStateChanged(boolean isConnected) {
        Log.i(TAG, "onServiceConnectedStateChanged: isConnected=" + isConnected);
        if (!isConnected) {
            setHideNotificationState(-1);
        }
    }

    @Override // com.android.server.swing.notification.IHwSwingEventDispatcher
    public void dispatchMultiEyeGazeChange(int multiEyeGaze) {
        Log.i(TAG, "dispatchMultiEyeGazeChange: isMultiEye=" + multiEyeGaze);
        HwAwarenessServiceConnector hwAwarenessServiceConnector = this.mAwarenessServiceConnector;
        if (hwAwarenessServiceConnector == null || !hwAwarenessServiceConnector.isConnected()) {
            Log.w(TAG, "dispatchMultiEyeGazeChange: awareness service is disconnected");
            return;
        }
        int who = -2;
        HwSwingFaceTrackingProcessor hwSwingFaceTrackingProcessor = this.mFaceTrackingProcessor;
        if (hwSwingFaceTrackingProcessor != null) {
            who = hwSwingFaceTrackingProcessor.getWho();
        }
        setHideNotificationState(calcHideNotificationState(multiEyeGaze, who));
    }

    @Override // com.android.server.swing.notification.IHwSwingEventDispatcher
    public void dispatchFaceTrackingChange(int who) {
        Log.i(TAG, "dispatchFaceTrackingChange: who=" + who);
        HwAwarenessServiceConnector hwAwarenessServiceConnector = this.mAwarenessServiceConnector;
        if (hwAwarenessServiceConnector == null || !hwAwarenessServiceConnector.isConnected()) {
            Log.w(TAG, "dispatchFaceTrackingChange: awareness service is disconnected");
            return;
        }
        int multiEyeGaze = -1;
        HwSwingMultiEyeGazeProcessor hwSwingMultiEyeGazeProcessor = this.mMultiEyeGazeProcessor;
        if (hwSwingMultiEyeGazeProcessor != null) {
            multiEyeGaze = hwSwingMultiEyeGazeProcessor.getMultiEyeGazeStatus();
        }
        setHideNotificationState(calcHideNotificationState(multiEyeGaze, who));
    }

    private int calcHideNotificationState(int multiEyeGaze, int who) {
        if (multiEyeGaze == -1 && who == -2) {
            return -1;
        }
        if (multiEyeGaze == 1 || who == 1) {
            return 0;
        }
        return 1;
    }

    private void setHideNotificationState(int notificationState) {
        if (notificationState != this.mNotificationState) {
            this.mNotificationState = notificationState;
            Log.i(TAG, "setHideNotificationState: notificationState=" + notificationState + ";isConnect=" + this.mIsConnect);
            try {
                if (this.mHwSwingEventNotifier != null) {
                    this.mHwSwingEventNotifier.swingNotificationState(notificationState);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "set notification state failed");
            } catch (IllegalStateException e2) {
                Log.e(TAG, "set notification state: illegal state");
            } catch (IllegalArgumentException e3) {
                Log.e(TAG, "set notification state: illegal argument");
            }
        }
    }
}
