package com.huawei.android.view;

import android.os.FreezeScreenScene;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import android.view.IWindowManager;
import com.huawei.annotation.HwSystemApi;

public class InputConsumerEx {
    private static final String TAG = "InputConsumerEx";
    private static final Singleton<IWindowManager> sWindowManager = new Singleton<IWindowManager>() {
        /* class com.huawei.android.view.InputConsumerEx.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public IWindowManager create() {
            return IWindowManager.Stub.asInterface(ServiceManager.getService(FreezeScreenScene.WINDOW_PARAM));
        }
    };

    @HwSystemApi
    public static void createInputConsumer(IBinder token, String name, int displayId, InputChannelEx inputChannelEx) {
        IWindowManager mWindowManager = (IWindowManager) sWindowManager.get();
        if (mWindowManager != null) {
            try {
                mWindowManager.createInputConsumer(token, name, displayId, inputChannelEx.getInputChannel());
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException in createInputConsumer");
            }
        }
    }

    @HwSystemApi
    public static boolean destroyInputConsumer(String name, int displayId) {
        IWindowManager mWindowManager = (IWindowManager) sWindowManager.get();
        if (mWindowManager == null) {
            return false;
        }
        try {
            return mWindowManager.destroyInputConsumer(name, displayId);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException in destroyInputConsumer");
            return false;
        }
    }
}
