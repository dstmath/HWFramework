package com.huawei.android.hardware.input;

import android.content.Context;
import android.hardware.input.IInputManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import android.view.InputEvent;
import com.android.internal.os.SomeArgs;
import com.huawei.android.hardware.input.IHwInputManager;
import com.huawei.android.hardware.input.IHwTHPEventListener;
import java.util.HashMap;

public class HwInputManager {
    public static final int COMMAND_RESULT_FAILED = -1;
    public static final int COMMAND_RESULT_NOT_READY = -2;
    private static final Singleton<IHwInputManager> I_INPUT_MANAGER_SINGLETON = new Singleton<IHwInputManager>() {
        /* class com.huawei.android.hardware.input.HwInputManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IHwInputManager create() {
            return HwInputManager.getInputService();
        }
    };
    private static final String TAG = "HwInputManager";
    private static HwInputManager sInstance;
    private final Context mContext;
    private final HashMap<HwTHPEventListener, HwTHPEventListenerDelegate> mDelegates = new HashMap<>();
    private final IBinder mToken;

    public interface HwTHPEventListener {
        void onHwTHPEvent(int i);

        void onHwTpEvent(int i, int i2, String str);
    }

    private HwInputManager(Context context) {
        this.mContext = context;
        this.mToken = new Binder();
    }

    public static HwInputManager getInstance(Context context) {
        HwInputManager hwInputManager;
        synchronized (HwInputManager.class) {
            if (sInstance == null) {
                sInstance = new HwInputManager(context);
            }
            hwInputManager = sInstance;
        }
        return hwInputManager;
    }

    public static IHwInputManager getService() {
        return I_INPUT_MANAGER_SINGLETON.get();
    }

    private class HwTHPEventListenerDelegate extends IHwTHPEventListener.Stub implements Handler.Callback {
        private static final int MSG_THP_EVENT_REPORT = 1;
        private static final int MSG_TP_EVENT_REPORT = 2;
        private Handler mCallBackHandler;
        final HwTHPEventListener mCallback;

        HwTHPEventListenerDelegate(HwTHPEventListener callback, Handler handler) {
            this.mCallback = callback;
            if (handler != null) {
                this.mCallBackHandler = new Handler(handler.getLooper(), this);
            } else {
                this.mCallBackHandler = new Handler(Looper.myLooper(), this);
            }
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            SomeArgs args = (SomeArgs) msg.obj;
            int i = msg.what;
            if (i == 1) {
                this.mCallback.onHwTHPEvent(((Integer) args.arg1).intValue());
                args.recycle();
                return true;
            } else if (i != 2) {
                args.recycle();
                return false;
            } else {
                this.mCallback.onHwTpEvent(((Integer) args.arg1).intValue(), ((Integer) args.arg2).intValue(), (String) args.arg3);
                args.recycle();
                return true;
            }
        }

        @Override // com.huawei.android.hardware.input.IHwTHPEventListener
        public void onHwTHPEvent(int event) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(event);
            this.mCallBackHandler.obtainMessage(1, args).sendToTarget();
        }

        @Override // com.huawei.android.hardware.input.IHwTHPEventListener
        public void onHwTpEvent(int eventClass, int eventCode, String extraInfo) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(eventClass);
            args.arg2 = Integer.valueOf(eventCode);
            args.arg3 = extraInfo;
            this.mCallBackHandler.obtainMessage(2, args).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public static IHwInputManager getInputService() {
        IInputManager ims;
        IBinder innerService;
        try {
            IBinder service = ServiceManager.getService("input");
            if (service == null || (ims = IInputManager.Stub.asInterface(service)) == null || (innerService = ims.getHwInnerService()) == null) {
                return null;
            }
            return IHwInputManager.Stub.asInterface(innerService);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static String runHwTHPCommand(String command, String parameter) {
        if (command != null) {
            try {
                return getService().runHwTHPCommand(command, parameter);
            } catch (RemoteException e) {
                Log.e(TAG, "runHwTHPCommand failed: catch RemoteException!");
                return null;
            }
        } else {
            throw new IllegalArgumentException("command must not be null");
        }
    }

    public static int setTouchscreenFeatureConfig(int feature, String config) {
        try {
            return getService().setTouchscreenFeatureConfig(feature, config);
        } catch (RemoteException e) {
            Log.e(TAG, "setTouchscreenFeatureConfig Exception");
            return -2;
        }
    }

    public void registerListener(HwTHPEventListener listener, Handler handler) {
        if (listener != null) {
            synchronized (this.mDelegates) {
                if (this.mDelegates.get(listener) != null) {
                    Log.w(TAG, "listener has already been registered");
                    return;
                }
                HwTHPEventListenerDelegate delegate = new HwTHPEventListenerDelegate(listener, handler);
                try {
                    getService().registerListener(delegate, this.mToken);
                    this.mDelegates.put(listener, delegate);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        } else {
            throw new IllegalArgumentException("listener must not be null");
        }
    }

    public void unregisterListener(HwTHPEventListener listener) {
        if (listener != null) {
            synchronized (this.mDelegates) {
                HwTHPEventListenerDelegate delegate = this.mDelegates.get(listener);
                if (delegate == null) {
                    Log.w(TAG, "listener has not been registered,return");
                    return;
                }
                try {
                    getService().unregisterListener(delegate, this.mToken);
                    this.mDelegates.remove(listener);
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
        } else {
            throw new IllegalArgumentException("listener must not be null");
        }
    }

    public static void setInputEventStrategy(boolean isStartInputEventControl) {
        try {
            getService().setInputEventStrategy(isStartInputEventControl);
        } catch (RemoteException e) {
            Log.e(TAG, "setInputEventStrategy failed: catch RemoteException!");
        }
    }

    public static String runSideTouchCommand(String command, String parameter) {
        try {
            IHwInputManager service = getService();
            if (service != null) {
                return service.runSideTouchCommand(command, parameter);
            }
            Log.w(TAG, "input service is not ready");
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "notifyVolumeMode failed: catch RemoteException!");
            return null;
        }
    }

    public static int[] sendTPCommand(int type, Bundle bundle) {
        try {
            IHwInputManager service = getService();
            if (service != null) {
                return service.setTPCommand(type, bundle);
            }
            Log.w(TAG, "input service is not ready");
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "sendTPCommand failed: catch RemoteException!");
            return null;
        }
    }

    public static boolean injectInputEventByDisplayId(InputEvent inputEvent, int mode, int displayId) {
        try {
            IHwInputManager service = getService();
            if (service != null) {
                return service.injectInputEventByDisplayId(inputEvent, mode, displayId);
            }
            Log.w(TAG, "input service is not ready");
            return false;
        } catch (RemoteException e) {
            Log.e(TAG, "injectInputEvent failed: catch RemoteException!");
            return false;
        }
    }

    public static void fadeMousePointer() {
        try {
            IHwInputManager service = getService();
            if (service != null) {
                service.fadeMousePointer();
            } else {
                Log.w(TAG, "input service is not ready");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "fadeMousePointer failed: catch RemoteException!");
        }
    }

    public static void setMousePosition(float xPosition, float yPosition) {
        try {
            IHwInputManager service = getService();
            if (service != null) {
                service.setMousePosition(xPosition, yPosition);
            } else {
                Log.w(TAG, "input service is not ready");
            }
        } catch (RemoteException e) {
            Log.e(TAG, "fadeMousePointer failed: catch RemoteException!");
        }
    }
}
