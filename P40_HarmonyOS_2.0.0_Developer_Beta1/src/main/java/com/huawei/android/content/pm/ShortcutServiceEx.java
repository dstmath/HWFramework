package com.huawei.android.content.pm;

import android.content.pm.IShortcutService;
import android.os.RemoteException;
import android.os.ServiceManager;

public class ShortcutServiceEx {
    public static void restoreShortcuts(int userId) throws RemoteException {
        IShortcutService.Stub.asInterface(ServiceManager.getService("shortcut")).restoreShortcuts(userId);
    }
}
