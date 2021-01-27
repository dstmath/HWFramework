package android.content.pm;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.IShortcutService;
import android.hsm.HwSystemManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import java.util.List;

public class ShortcutManager {
    private static final int PERMISSION_EDIT_SHORTCUT = 16777216;
    private static final String TAG = "ShortcutManager";
    private final Context mContext;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final IShortcutService mService;

    public ShortcutManager(Context context, IShortcutService service) {
        this.mContext = context;
        this.mService = service;
    }

    public ShortcutManager(Context context) {
        this(context, IShortcutService.Stub.asInterface(ServiceManager.getService("shortcut")));
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

    public List<ShortcutInfo> getManifestShortcuts() {
        try {
            return this.mService.getManifestShortcuts(this.mContext.getPackageName(), injectMyUserId()).getList();
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

    public void disableShortcuts(List<String> shortcutIds) {
        try {
            this.mService.disableShortcuts(this.mContext.getPackageName(), shortcutIds, null, 0, injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void disableShortcuts(List<String> shortcutIds, int disabledMessageResId) {
        try {
            this.mService.disableShortcuts(this.mContext.getPackageName(), shortcutIds, null, disabledMessageResId, injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void disableShortcuts(List<String> shortcutIds, String disabledMessage) {
        disableShortcuts(shortcutIds, (CharSequence) disabledMessage);
    }

    public void disableShortcuts(List<String> shortcutIds, CharSequence disabledMessage) {
        try {
            this.mService.disableShortcuts(this.mContext.getPackageName(), shortcutIds, disabledMessage, 0, injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void enableShortcuts(List<String> shortcutIds) {
        try {
            this.mService.enableShortcuts(this.mContext.getPackageName(), shortcutIds, injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getMaxShortcutCountForActivity() {
        return getMaxShortcutCountPerActivity();
    }

    public int getMaxShortcutCountPerActivity() {
        try {
            return this.mService.getMaxShortcutCountPerActivity(this.mContext.getPackageName(), injectMyUserId());
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

    public boolean isRateLimitingActive() {
        try {
            return this.mService.getRemainingCallCount(this.mContext.getPackageName(), injectMyUserId()) == 0;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getIconMaxWidth() {
        try {
            return this.mService.getIconMaxDimensions(this.mContext.getPackageName(), injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int getIconMaxHeight() {
        try {
            return this.mService.getIconMaxDimensions(this.mContext.getPackageName(), injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void reportShortcutUsed(String shortcutId) {
        try {
            this.mService.reportShortcutUsed(this.mContext.getPackageName(), shortcutId, injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean isRequestPinShortcutSupported() {
        try {
            return this.mService.isRequestPinItemSupported(injectMyUserId(), 1);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public boolean requestPinShortcut(ShortcutInfo shortcut, IntentSender resultIntent) {
        try {
            Slog.i(TAG, "start requestPinShortcut: " + shortcut.getPackage() + ", shortcutId: " + shortcut.getId());
            if (HwSystemManager.allowOp(16777216)) {
                return this.mService.requestPinShortcut(this.mContext.getPackageName(), shortcut, resultIntent, injectMyUserId());
            }
            Slog.i(TAG, "package: " + shortcut.getPackage() + ", shortcutId: " + shortcut.getId() + " not have PERMISSION_EDIT_SHORTCUT");
            return false;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Intent createShortcutResultIntent(ShortcutInfo shortcut) {
        try {
            return this.mService.createShortcutResultIntent(this.mContext.getPackageName(), shortcut, injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void onApplicationActive(String packageName, int userId) {
        try {
            this.mService.onApplicationActive(packageName, userId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public int injectMyUserId() {
        return this.mContext.getUserId();
    }

    @SystemApi
    public List<ShareShortcutInfo> getShareTargets(IntentFilter filter) {
        try {
            return this.mService.getShareTargets(this.mContext.getPackageName(), filter, injectMyUserId()).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public static final class ShareShortcutInfo implements Parcelable {
        public static final Parcelable.Creator<ShareShortcutInfo> CREATOR = new Parcelable.Creator<ShareShortcutInfo>() {
            /* class android.content.pm.ShortcutManager.ShareShortcutInfo.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public ShareShortcutInfo createFromParcel(Parcel in) {
                return new ShareShortcutInfo(in);
            }

            @Override // android.os.Parcelable.Creator
            public ShareShortcutInfo[] newArray(int size) {
                return new ShareShortcutInfo[size];
            }
        };
        private final ShortcutInfo mShortcutInfo;
        private final ComponentName mTargetComponent;

        public ShareShortcutInfo(ShortcutInfo shortcutInfo, ComponentName targetComponent) {
            if (shortcutInfo == null) {
                throw new NullPointerException("shortcut info is null");
            } else if (targetComponent != null) {
                this.mShortcutInfo = shortcutInfo;
                this.mTargetComponent = targetComponent;
            } else {
                throw new NullPointerException("target component is null");
            }
        }

        private ShareShortcutInfo(Parcel in) {
            this.mShortcutInfo = (ShortcutInfo) in.readParcelable(ShortcutInfo.class.getClassLoader());
            this.mTargetComponent = (ComponentName) in.readParcelable(ComponentName.class.getClassLoader());
        }

        public ShortcutInfo getShortcutInfo() {
            return this.mShortcutInfo;
        }

        public ComponentName getTargetComponent() {
            return this.mTargetComponent;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.mShortcutInfo, flags);
            dest.writeParcelable(this.mTargetComponent, flags);
        }
    }

    @SystemApi
    public boolean hasShareTargets(String packageName) {
        try {
            return this.mService.hasShareTargets(this.mContext.getPackageName(), packageName, injectMyUserId());
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
