package com.huawei.android.gameassist;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Singleton;
import android.util.Slog;
import com.huawei.android.gameassist.IGameAssistManager;

public class HwGameAssistManager {
    private static final Singleton<IGameAssistManager> IHwGameAssistManagerSingleton = new Singleton<IGameAssistManager>() {
        /* class com.huawei.android.gameassist.HwGameAssistManager.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // android.util.Singleton
        public IGameAssistManager create() {
            return IGameAssistManager.Stub.asInterface(ServiceManager.getService("game_assist"));
        }
    };
    private static final String TAG = "HwGameAssistManager";

    public static IGameAssistManager getService() {
        return IHwGameAssistManagerSingleton.get();
    }

    public static void notifyKeyEvent() {
        IGameAssistManager gameService = getService();
        if (gameService != null) {
            try {
                gameService.notifyKeyEvent();
            } catch (RemoteException e) {
                Slog.e(TAG, "notifyKeyEvent failed: catch RemoteException!");
            }
        }
    }
}
