package com.android.server.wm;

import android.app.IAssistDataReceiver;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.am.AssistDataRequester;

class AssistDataReceiverProxy implements AssistDataRequester.AssistDataRequesterCallbacks, IBinder.DeathRecipient {
    private static final String TAG = "ActivityTaskManager";
    private String mCallerPackage;
    private IAssistDataReceiver mReceiver;

    public AssistDataReceiverProxy(IAssistDataReceiver receiver, String callerPackage) {
        this.mReceiver = receiver;
        this.mCallerPackage = callerPackage;
        linkToDeath();
    }

    public boolean canHandleReceivedAssistDataLocked() {
        return true;
    }

    public void onAssistDataReceivedLocked(Bundle data, int activityIndex, int activityCount) {
        IAssistDataReceiver iAssistDataReceiver = this.mReceiver;
        if (iAssistDataReceiver != null) {
            try {
                iAssistDataReceiver.onHandleAssistData(data);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to proxy assist data to receiver in package=" + this.mCallerPackage, e);
            }
        }
    }

    public void onAssistScreenshotReceivedLocked(Bitmap screenshot) {
        IAssistDataReceiver iAssistDataReceiver = this.mReceiver;
        if (iAssistDataReceiver != null) {
            try {
                iAssistDataReceiver.onHandleAssistScreenshot(screenshot);
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to proxy assist screenshot to receiver in package=" + this.mCallerPackage, e);
            }
        }
    }

    public void onAssistRequestCompleted() {
        unlinkToDeath();
    }

    @Override // android.os.IBinder.DeathRecipient
    public void binderDied() {
        unlinkToDeath();
    }

    private void linkToDeath() {
        try {
            this.mReceiver.asBinder().linkToDeath(this, 0);
        } catch (RemoteException e) {
            Log.w(TAG, "Could not link to client death", e);
        }
    }

    private void unlinkToDeath() {
        IAssistDataReceiver iAssistDataReceiver = this.mReceiver;
        if (iAssistDataReceiver != null) {
            iAssistDataReceiver.asBinder().unlinkToDeath(this, 0);
        }
        this.mReceiver = null;
    }
}
