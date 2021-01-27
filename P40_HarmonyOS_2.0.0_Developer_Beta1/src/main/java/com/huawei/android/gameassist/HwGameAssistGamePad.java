package com.huawei.android.gameassist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.util.Singleton;
import android.util.Slog;
import android.view.InputEvent;
import com.huawei.android.gameassist.IGamePadAIDL;

public class HwGameAssistGamePad {
    private static final Singleton<IGamePadAIDL> IHwGameAssistGamePadSingleton = new Singleton<IGamePadAIDL>() {
        /* class com.huawei.android.gameassist.HwGameAssistGamePad.AnonymousClass2 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IGamePadAIDL create() {
            if (HwGameAssistGamePad.sService == null) {
                HwGameAssistGamePad.bindService();
            }
            return HwGameAssistGamePad.sService;
        }
    };
    private static final int KEY_MAX_DELAY_TIME = 5;
    public static final int NO_SEND = 2;
    public static final int SEND_FAIL = 1;
    public static final int SEND_SUCCESS = 0;
    private static final int SERVICE_CONNECT_TIME_OUT = 3000;
    private static final String TAG = "HwGameAssistGamePad";
    private static ServiceConnection mConnection = new ServiceConnection() {
        /* class com.huawei.android.gameassist.HwGameAssistGamePad.AnonymousClass1 */

        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName className, IBinder service) {
            HwGameAssistGamePad.sService = IGamePadAIDL.Stub.asInterface(service);
            Slog.d(HwGameAssistGamePad.TAG, "onServiceConnected " + HwGameAssistGamePad.sService);
            HwGameAssistGamePad.sSumDelayEvent = 0;
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName className) {
            Slog.d(HwGameAssistGamePad.TAG, "onServiceDisconnected " + HwGameAssistGamePad.sService);
            HwGameAssistGamePad.sService = null;
        }
    };
    public static Context mContext = null;
    static long sBindTime;
    static IGamePadAIDL sService = null;
    static long sSumDelayEvent = 0;

    public static IGamePadAIDL getService() {
        IHwGameAssistGamePadSingleton.get();
        return sService;
    }

    public static void bindService() {
        if (sService == null) {
            sBindTime = SystemClock.uptimeMillis();
            Intent intent = new Intent();
            Slog.d(TAG, "bindService");
            intent.setAction("com.huawei.gameassistant.equipservice");
            intent.setPackage("com.huawei.gameassistant");
            Context context = mContext;
            if (context != null) {
                try {
                    context.bindServiceAsUser(intent, mConnection, 1, UserHandle.SYSTEM);
                } catch (Exception e) {
                    Slog.e(TAG, "bindServiceAsUser failed: equipservice!");
                }
            } else {
                Slog.w(TAG, "mContext == null");
            }
        }
    }

    public static void unbindService() {
        if (sService != null) {
            Slog.d(TAG, "unbindService");
            mContext.unbindService(mConnection);
            sService = null;
        }
    }

    public static int notifyInputEvent(InputEvent event) {
        IGamePadAIDL gameService = getService();
        if (gameService != null) {
            try {
                long now = SystemClock.uptimeMillis();
                Trace.traceBegin(4, "gamepad");
                gameService.notifyInputEvent(event);
                Trace.traceEnd(4);
                if (SystemClock.uptimeMillis() - now <= 5) {
                    return 0;
                }
                sSumDelayEvent++;
                Slog.w(TAG, "GamePad notifyKeyEvent delay " + (SystemClock.uptimeMillis() - now) + " ms  " + sSumDelayEvent);
                return 0;
            } catch (RemoteException e) {
                Slog.e(TAG, "notifyKeyEvent failed: catch RemoteException!");
                unbindService();
                sService = null;
                return 1;
            }
        } else if (SystemClock.uptimeMillis() - sBindTime > 3000) {
            Slog.d(TAG, "Service is null" + gameService);
            return 1;
        } else {
            Slog.d(TAG, "Service is connecting...");
            return 2;
        }
    }
}
