package com.huawei.android.hardware.input;

import android.content.Context;
import android.hardware.input.IInputManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Singleton;
import com.android.internal.os.SomeArgs;
import com.huawei.android.hardware.input.IHwInputManager;
import com.huawei.android.hardware.input.IHwTHPEventListener;
import java.util.HashMap;

public class HwInputManager {
    private static final Singleton<IHwInputManager> IInputManagerSingleton = new Singleton<IHwInputManager>() {
        /* access modifiers changed from: protected */
        public IHwInputManager create() {
            try {
                return IHwInputManager.Stub.asInterface(IInputManager.Stub.asInterface(ServiceManager.getService("input")).getHwInnerService());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    };
    private static final String TAG = "HwInputManager";
    private static HwInputManager sInstance;
    private final Context mContext;
    final HashMap<HwTHPEventListener, HwTHPEventListenerDelegate> mDelegates = new HashMap<>();
    private final IBinder mToken;

    public interface HwTHPEventListener {
        void onHwTHPEvent(int i);
    }

    private class HwTHPEventListenerDelegate extends IHwTHPEventListener.Stub implements Handler.Callback {
        private static final int MSG_THP_EVENT_REPORT = 1;
        private Handler mCallBackHandler;
        final HwTHPEventListener mCallback;

        public HwTHPEventListenerDelegate(HwTHPEventListener callback, Handler handler) {
            this.mCallback = callback;
            if (handler != null) {
                this.mCallBackHandler = new Handler(handler.getLooper(), this);
            } else {
                this.mCallBackHandler = new Handler(Looper.myLooper(), this);
            }
        }

        public boolean handleMessage(Message msg) {
            SomeArgs args = (SomeArgs) msg.obj;
            if (msg.what != 1) {
                args.recycle();
                return false;
            }
            this.mCallback.onHwTHPEvent(((Integer) args.arg1).intValue());
            args.recycle();
            return true;
        }

        public void onHwTHPEvent(int event) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(event);
            this.mCallBackHandler.obtainMessage(1, args).sendToTarget();
        }
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

    private HwInputManager(Context context) {
        this.mContext = context;
        this.mToken = new Binder();
    }

    public static IHwInputManager getService() {
        return (IHwInputManager) IInputManagerSingleton.get();
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

    public static void setInputEventStrategy(boolean isStartInputEventControl) {
        try {
            getService().setInputEventStrategy(isStartInputEventControl);
        } catch (RemoteException e) {
            Log.e(TAG, "setInputEventStrategy failed: catch RemoteException!");
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
}
