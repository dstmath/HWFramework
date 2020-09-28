package android.vrsystem;

import android.os.FreezeScreenScene;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import android.view.IWindowManager;
import android.view.InputChannel;
import android.view.SurfaceControl;

public class VrTouchInterface {
    private static final String INPUT_CONSUMER_VRAR = "INPUT_CONSUMER_VRAR";
    private static final String TAG = "VrTouchInterface";
    private static Singleton<IWindowManager> sWindowManager = new Singleton<IWindowManager>() {
        /* class android.vrsystem.VrTouchInterface.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public IWindowManager create() {
            IWindowManager service = IWindowManager.Stub.asInterface(ServiceManager.getService(FreezeScreenScene.WINDOW_PARAM));
            if (service == null) {
                Log.e(VrTouchInterface.TAG, "create: Oops! window Missed...");
            }
            return service;
        }
    };
    private boolean mPointerListenerRegistered = false;

    public synchronized InputChannel registerInputEventCallback(IBinder binder) {
        if (Process.myUid() != 1000) {
            throw new SecurityException("System Uid Only");
        } else if (this.mPointerListenerRegistered) {
            return null;
        } else {
            IWindowManager windowManager = (IWindowManager) sWindowManager.get();
            if (windowManager != null) {
                InputChannel inputChannel = new InputChannel();
                try {
                    Log.i(TAG, "registerInputEventListener: createInputConsumer");
                    windowManager.destroyInputConsumer(INPUT_CONSUMER_VRAR, 0);
                    windowManager.createInputConsumer(binder, INPUT_CONSUMER_VRAR, 0, inputChannel);
                    this.mPointerListenerRegistered = true;
                    return inputChannel;
                } catch (RemoteException e) {
                    Log.e(TAG, "registerInputEventCallback:" + e.getMessage());
                }
            }
            return null;
        }
    }

    public synchronized void unregisterInputEventCallback() {
        if (Process.myUid() != 1000) {
            throw new SecurityException("System Uid Only");
        } else if (this.mPointerListenerRegistered) {
            IWindowManager windowManager = (IWindowManager) sWindowManager.get();
            if (windowManager != null) {
                try {
                    Log.i(TAG, "unregisterInputEventCallback: destroyInputConsumer");
                    windowManager.destroyInputConsumer(INPUT_CONSUMER_VRAR, 0);
                } catch (RemoteException e) {
                    Log.e(TAG, "unregisterInputEventCallback:" + e.getMessage());
                }
            }
            this.mPointerListenerRegistered = false;
        }
    }

    public void setDefaultDisplayPowerMode(int powerMode) {
        Log.i(TAG, "setDefaultDisplayPowerMode:" + powerMode);
        IBinder displayToken = SurfaceControl.getPhysicalDisplayToken(0);
        if (displayToken != null) {
            SurfaceControl.setDisplayPowerMode(displayToken, powerMode);
        }
    }
}
