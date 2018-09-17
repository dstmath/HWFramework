package android.content.pm;

import android.content.Context;
import android.content.pm.IShortcutService.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.UserDictionary.Words;
import java.util.List;

public class ShortcutManager {
    private static final String TAG = "ShortcutManager";
    private final Context mContext;
    private final IShortcutService mService;

    public ShortcutManager(Context context, IShortcutService service) {
        this.mContext = context;
        this.mService = service;
    }

    public ShortcutManager(Context context) {
        this(context, Stub.asInterface(ServiceManager.getService(Words.SHORTCUT)));
    }

    public boolean setDynamicShortcuts(List<ShortcutInfo> shortcutInfoList) {
        try {
            return this.mService.setDynamicShortcuts(this.mContext.getPackageName(), new ParceledListSlice(shortcutInfoList), injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ShortcutInfo> getDynamicShortcuts() {
        try {
            return this.mService.getDynamicShortcuts(this.mContext.getPackageName(), injectMyUserId()).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean addDynamicShortcuts(List<ShortcutInfo> shortcutInfoList) {
        try {
            return this.mService.addDynamicShortcuts(this.mContext.getPackageName(), new ParceledListSlice(shortcutInfoList), injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeDynamicShortcuts(List<String> shortcutIds) {
        try {
            this.mService.removeDynamicShortcuts(this.mContext.getPackageName(), shortcutIds, injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void removeAllDynamicShortcuts() {
        try {
            this.mService.removeAllDynamicShortcuts(this.mContext.getPackageName(), injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<ShortcutInfo> getPinnedShortcuts() {
        try {
            return this.mService.getPinnedShortcuts(this.mContext.getPackageName(), injectMyUserId()).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean updateShortcuts(List<ShortcutInfo> shortcutInfoList) {
        try {
            return this.mService.updateShortcuts(this.mContext.getPackageName(), new ParceledListSlice(shortcutInfoList), injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getMaxDynamicShortcutCount() {
        try {
            return this.mService.getMaxDynamicShortcutCount(this.mContext.getPackageName(), injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getRemainingCallCount() {
        try {
            return this.mService.getRemainingCallCount(this.mContext.getPackageName(), injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public long getRateLimitResetTime() {
        try {
            return this.mService.getRateLimitResetTime(this.mContext.getPackageName(), injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getIconMaxDimensions() {
        try {
            return this.mService.getIconMaxDimensions(this.mContext.getPackageName(), injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    protected int injectMyUserId() {
        return UserHandle.myUserId();
    }
}
