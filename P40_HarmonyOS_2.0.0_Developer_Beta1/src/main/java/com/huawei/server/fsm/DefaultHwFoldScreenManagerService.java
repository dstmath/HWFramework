package com.huawei.server.fsm;

import android.content.Context;
import android.os.IBinder;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.huawei.android.fsm.IHwFoldScreenManager;
import com.huawei.android.fsm.IHwFoldScreenManagerEx;

public class DefaultHwFoldScreenManagerService extends SystemService implements Watchdog.Monitor {
    protected DefaultHwFoldScreenManagerService(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    public void addBinderService(String name, IHwFoldScreenManagerEx serviceEx) {
        IHwFoldScreenManager manager = serviceEx.getIHwFoldScreenManager();
        IBinder binder = null;
        if (manager instanceof IBinder) {
            binder = (IBinder) manager;
        }
        publishBinderService(name, binder);
    }

    /* access modifiers changed from: protected */
    public <T> void addLocalService(Class<T> type, T service) {
        publishLocalService(type, service);
    }

    public void onStart() {
    }

    public void onBootPhase(int phase) {
    }

    public void monitor() {
    }

    public void onSwitchUser(int userHandle) {
    }
}
