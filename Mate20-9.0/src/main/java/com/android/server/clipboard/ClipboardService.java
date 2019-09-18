package com.android.server.clipboard;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.KeyguardManager;
import android.common.HwFrameworkFactory;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.Context;
import android.content.IClipboard;
import android.content.IOnPrimaryClipChangedListener;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.hdm.HwDeviceManager;
import android.hwclipboarddelayread.HwClipboardReadDelayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Slog;
import android.util.SparseArray;
import android.widget.Toast;
import com.android.server.SystemService;
import com.android.server.UiThread;
import com.android.server.clipboard.HostClipboardMonitor;
import com.huawei.android.content.IOnPrimaryClipGetedListener;
import com.huawei.android.server.clipboard.IHwClipboardServiceManager;
import huawei.android.security.IHwBehaviorCollectManager;
import java.util.HashSet;
import java.util.List;

public class ClipboardService extends SystemService {
    private static final boolean IS_EMULATOR = SystemProperties.getBoolean("ro.kernel.qemu", false);
    private static final String TAG = "ClipboardService";
    private final IActivityManager mAm = ActivityManager.getService();
    private final AppOpsManager mAppOps = ((AppOpsManager) getContext().getSystemService("appops"));
    /* access modifiers changed from: private */
    public final SparseArray<PerUserClipboard> mClipboards = new SparseArray<>();
    private HostClipboardMonitor mHostClipboardMonitor = null;
    private Thread mHostMonitorThread = null;
    private final IBinder mPermissionOwner;
    private final PackageManager mPm = getContext().getPackageManager();
    private final IUserManager mUm = ServiceManager.getService("user");

    private class ClipboardImpl extends IClipboard.Stub {
        HwInnerClipboardService mHwInnerServicel;

        public class HwInnerClipboardService extends IHwClipboardServiceManager.Stub {
            public HwInnerClipboardService() {
            }

            public void addPrimaryClipGetedListener(IOnPrimaryClipGetedListener listener, String callingPackage) {
                synchronized (this) {
                    HwClipboardReadDelayer.addPrimaryClipGetedListener(listener, callingPackage);
                }
            }

            public void removePrimaryClipGetedListener(IOnPrimaryClipGetedListener listener) {
                synchronized (this) {
                    HwClipboardReadDelayer.removePrimaryClipGetedListener(listener);
                }
            }

            public void setGetWaitTime(int waitTime) {
                HwClipboardReadDelayer.setGetWaitTime(waitTime);
            }
        }

        private ClipboardImpl() {
            this.mHwInnerServicel = new HwInnerClipboardService();
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            try {
                return ClipboardService.super.onTransact(code, data, reply, flags);
            } catch (RuntimeException e) {
                if (!(e instanceof SecurityException)) {
                    Slog.wtf("clipboard", "Exception: ", e);
                }
                throw e;
            }
        }

        public void setPrimaryClip(ClipData clip, String callingPackage) {
            synchronized (this) {
                if (clip != null) {
                    try {
                        if (clip.getItemCount() > 0) {
                            int callingUid = Binder.getCallingUid();
                            if (ClipboardService.this.clipboardAccessAllowed(30, callingPackage, callingUid)) {
                                if (HwDeviceManager.disallowOp(23)) {
                                    final Context context = ClipboardService.this.getContext();
                                    UiThread.getHandler().post(new Runnable() {
                                        public void run() {
                                            Toast toast = Toast.makeText(context, context.getResources().getString(33685904), 1);
                                            toast.getWindowParams().type = 2006;
                                            toast.show();
                                        }
                                    });
                                    return;
                                }
                                ClipboardService.this.checkDataOwnerLocked(clip, callingUid);
                                ClipboardService.this.setPrimaryClipInternal(clip, callingUid);
                                HwClipboardReadDelayer.setPrimaryClipNotify();
                                return;
                            }
                            return;
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
                throw new IllegalArgumentException("No items");
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0021, code lost:
            return;
         */
        public void clearPrimaryClip(String callingPackage) {
            synchronized (this) {
                int callingUid = Binder.getCallingUid();
                if (ClipboardService.this.clipboardAccessAllowed(30, callingPackage, callingUid)) {
                    if (!HwDeviceManager.disallowOp(23)) {
                        ClipboardService.this.setPrimaryClipInternal(null, callingUid);
                    }
                }
            }
        }

        public ClipData getPrimaryClip(String pkg) {
            HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.CLIPBOARD_GETPRIMARYCLIP);
            HwClipboardReadDelayer.getPrimaryClipNotify();
            synchronized (this) {
                if (ClipboardService.this.clipboardAccessAllowed(29, pkg, Binder.getCallingUid()) && !ClipboardService.this.isDeviceLocked()) {
                    if (!HwDeviceManager.disallowOp(23)) {
                        ClipboardService.this.addActiveOwnerLocked(Binder.getCallingUid(), pkg);
                        ClipData clipData = ClipboardService.this.getClipboard().primaryClip;
                        return clipData;
                    }
                }
                return null;
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0033, code lost:
            return r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0035, code lost:
            return null;
         */
        public ClipDescription getPrimaryClipDescription(String callingPackage) {
            synchronized (this) {
                ClipDescription clipDescription = null;
                if (ClipboardService.this.clipboardAccessAllowed(29, callingPackage, Binder.getCallingUid()) && !ClipboardService.this.isDeviceLocked()) {
                    if (!HwDeviceManager.disallowOp(23)) {
                        PerUserClipboard clipboard = ClipboardService.this.getClipboard();
                        if (clipboard.primaryClip != null) {
                            clipDescription = clipboard.primaryClip.getDescription();
                        }
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x002e, code lost:
            return r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x0030, code lost:
            return false;
         */
        public boolean hasPrimaryClip(String callingPackage) {
            synchronized (this) {
                boolean z = false;
                if (ClipboardService.this.clipboardAccessAllowed(29, callingPackage, Binder.getCallingUid()) && !ClipboardService.this.isDeviceLocked()) {
                    if (!HwDeviceManager.disallowOp(23)) {
                        if (ClipboardService.this.getClipboard().primaryClip != null) {
                            z = true;
                        }
                    }
                }
            }
        }

        public void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener listener, String callingPackage) {
            HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.CLIPBOARD_ADDPRIMARYCLIPCHANGEDLISTENER);
            synchronized (this) {
                ClipboardService.this.getClipboard().primaryClipListeners.register(listener, new ListenerInfo(Binder.getCallingUid(), callingPackage));
            }
        }

        public void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener listener) {
            synchronized (this) {
                ClipboardService.this.getClipboard().primaryClipListeners.unregister(listener);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:17:0x0040, code lost:
            return r1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0044, code lost:
            return false;
         */
        public boolean hasClipboardText(String callingPackage) {
            synchronized (this) {
                boolean z = false;
                if (ClipboardService.this.clipboardAccessAllowed(29, callingPackage, Binder.getCallingUid()) && !ClipboardService.this.isDeviceLocked()) {
                    if (!HwDeviceManager.disallowOp(23)) {
                        PerUserClipboard clipboard = ClipboardService.this.getClipboard();
                        if (clipboard.primaryClip == null) {
                            return false;
                        }
                        CharSequence text = clipboard.primaryClip.getItemAt(0).getText();
                        if (text != null && text.length() > 0) {
                            z = true;
                        }
                    }
                }
            }
        }

        /* JADX WARNING: type inference failed for: r0v0, types: [com.android.server.clipboard.ClipboardService$ClipboardImpl$HwInnerClipboardService, android.os.IBinder] */
        public IBinder getHwInnerService() {
            return this.mHwInnerServicel;
        }
    }

    private class ListenerInfo {
        final String mPackageName;
        final int mUid;

        ListenerInfo(int uid, String packageName) {
            this.mUid = uid;
            this.mPackageName = packageName;
        }
    }

    private class PerUserClipboard {
        final HashSet<String> activePermissionOwners = new HashSet<>();
        ClipData primaryClip;
        final RemoteCallbackList<IOnPrimaryClipChangedListener> primaryClipListeners = new RemoteCallbackList<>();
        int primaryClipUid = 9999;
        final int userId;

        PerUserClipboard(int userId2) {
            this.userId = userId2;
        }
    }

    public ClipboardService(Context context) {
        super(context);
        IBinder permOwner = null;
        try {
            permOwner = this.mAm.newUriPermissionOwner("clipboard");
        } catch (RemoteException e) {
            Slog.w("clipboard", "AM dead", e);
        }
        this.mPermissionOwner = permOwner;
        if (IS_EMULATOR) {
            this.mHostClipboardMonitor = new HostClipboardMonitor(new HostClipboardMonitor.HostClipboardCallback() {
                public void onHostClipboardUpdated(String contents) {
                    ClipData clip = new ClipData("host clipboard", new String[]{"text/plain"}, new ClipData.Item(contents));
                    synchronized (ClipboardService.this.mClipboards) {
                        ClipboardService.this.setPrimaryClipInternal(ClipboardService.this.getClipboard(0), clip, 1000);
                    }
                }
            });
            this.mHostMonitorThread = new Thread(this.mHostClipboardMonitor);
            this.mHostMonitorThread.start();
        }
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.clipboard.ClipboardService$ClipboardImpl, android.os.IBinder] */
    public void onStart() {
        publishBinderService("clipboard", new ClipboardImpl());
    }

    public void onCleanupUser(int userId) {
        synchronized (this.mClipboards) {
            this.mClipboards.remove(userId);
        }
    }

    /* access modifiers changed from: private */
    public PerUserClipboard getClipboard() {
        return getClipboard(UserHandle.getCallingUserId());
    }

    /* access modifiers changed from: private */
    public PerUserClipboard getClipboard(int userId) {
        PerUserClipboard puc;
        synchronized (this.mClipboards) {
            puc = this.mClipboards.get(userId);
            if (puc == null) {
                puc = new PerUserClipboard(userId);
                this.mClipboards.put(userId, puc);
            }
        }
        return puc;
    }

    /* access modifiers changed from: package-private */
    public List<UserInfo> getRelatedProfiles(int userId) {
        long origId = Binder.clearCallingIdentity();
        try {
            return this.mUm.getProfiles(userId, true);
        } catch (RemoteException e) {
            Slog.e(TAG, "Remote Exception calling UserManager: " + e);
            return null;
        } finally {
            Binder.restoreCallingIdentity(origId);
        }
    }

    private boolean hasRestriction(String restriction, int userId) {
        try {
            return this.mUm.hasUserRestriction(restriction, userId);
        } catch (RemoteException e) {
            Slog.e(TAG, "Remote Exception calling UserManager.getUserRestrictions: ", e);
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public void setPrimaryClipInternal(ClipData clip, int callingUid) {
        ClipData clip2;
        if (this.mHostClipboardMonitor != null) {
            if (clip == null) {
                this.mHostClipboardMonitor.setHostClipboard(BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
            } else if (clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (text != null) {
                    this.mHostClipboardMonitor.setHostClipboard(text.toString());
                }
            }
        }
        int userId = UserHandle.getUserId(callingUid);
        setPrimaryClipInternal(getClipboard(userId), clip, callingUid);
        List<UserInfo> related = getRelatedProfiles(userId);
        if (related != null) {
            int size = related.size();
            if (size > 1) {
                if (!(!hasRestriction("no_cross_profile_copy_paste", userId))) {
                    clip2 = null;
                } else {
                    clip2 = new ClipData(clip);
                    for (int i = clip2.getItemCount() - 1; i >= 0; i--) {
                        clip2.setItemAt(i, new ClipData.Item(clip2.getItemAt(i)));
                    }
                    clip2.fixUrisLight(userId);
                }
                for (int i2 = 0; i2 < size; i2++) {
                    int id = related.get(i2).id;
                    if (id != userId && (!hasRestriction("no_sharing_into_profile", id))) {
                        setPrimaryClipInternal(getClipboard(id), clip2, callingUid);
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPrimaryClipInternal(PerUserClipboard clipboard, ClipData clip, int callingUid) {
        revokeUris(clipboard);
        clipboard.activePermissionOwners.clear();
        if (clip != null || clipboard.primaryClip != null) {
            clipboard.primaryClip = clip;
            if (clip != null) {
                clipboard.primaryClipUid = callingUid;
            } else {
                clipboard.primaryClipUid = 9999;
            }
            if (clip != null) {
                ClipDescription description = clip.getDescription();
                if (description != null) {
                    description.setTimestamp(System.currentTimeMillis());
                }
            }
            long ident = Binder.clearCallingIdentity();
            int n = clipboard.primaryClipListeners.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ListenerInfo li = (ListenerInfo) clipboard.primaryClipListeners.getBroadcastCookie(i);
                    if (clipboardAccessAllowed(29, li.mPackageName, li.mUid)) {
                        clipboard.primaryClipListeners.getBroadcastItem(i).dispatchPrimaryClipChanged();
                    }
                } catch (RemoteException e) {
                } catch (Throwable th) {
                    clipboard.primaryClipListeners.finishBroadcast();
                    Binder.restoreCallingIdentity(ident);
                    throw th;
                }
            }
            clipboard.primaryClipListeners.finishBroadcast();
            Binder.restoreCallingIdentity(ident);
        }
    }

    /* access modifiers changed from: private */
    public boolean isDeviceLocked() {
        int callingUserId = UserHandle.getCallingUserId();
        long token = Binder.clearCallingIdentity();
        try {
            KeyguardManager keyguardManager = (KeyguardManager) getContext().getSystemService(KeyguardManager.class);
            return keyguardManager != null && keyguardManager.isDeviceLocked(callingUserId);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private final void checkUriOwnerLocked(Uri uri, int sourceUid) {
        if (uri != null && "content".equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mAm.checkGrantUriPermission(sourceUid, null, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)));
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
            Binder.restoreCallingIdentity(ident);
        }
    }

    private final void checkItemOwnerLocked(ClipData.Item item, int uid) {
        if (item.getUri() != null) {
            checkUriOwnerLocked(item.getUri(), uid);
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            checkUriOwnerLocked(intent.getData(), uid);
        }
    }

    /* access modifiers changed from: private */
    public final void checkDataOwnerLocked(ClipData data, int uid) {
        int N = data.getItemCount();
        for (int i = 0; i < N; i++) {
            checkItemOwnerLocked(data.getItemAt(i), uid);
        }
    }

    private final void grantUriLocked(Uri uri, int sourceUid, String targetPkg, int targetUserId) {
        if (uri != null && "content".equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mAm.grantUriPermissionFromOwner(this.mPermissionOwner, sourceUid, targetPkg, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)), targetUserId);
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
            Binder.restoreCallingIdentity(ident);
        }
    }

    private final void grantItemLocked(ClipData.Item item, int sourceUid, String targetPkg, int targetUserId) {
        if (item.getUri() != null) {
            grantUriLocked(item.getUri(), sourceUid, targetPkg, targetUserId);
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            grantUriLocked(intent.getData(), sourceUid, targetPkg, targetUserId);
        }
    }

    /* access modifiers changed from: private */
    public final void addActiveOwnerLocked(int uid, String pkg) {
        IPackageManager pm = AppGlobals.getPackageManager();
        int targetUserHandle = UserHandle.getCallingUserId();
        long oldIdentity = Binder.clearCallingIdentity();
        try {
            PackageInfo pi = pm.getPackageInfo(pkg, 0, targetUserHandle);
            if (pi == null) {
                throw new IllegalArgumentException("Unknown package " + pkg);
            } else if (UserHandle.isSameApp(pi.applicationInfo.uid, uid)) {
                Binder.restoreCallingIdentity(oldIdentity);
                PerUserClipboard clipboard = getClipboard();
                if (clipboard.primaryClip != null && !clipboard.activePermissionOwners.contains(pkg)) {
                    int N = clipboard.primaryClip.getItemCount();
                    for (int i = 0; i < N; i++) {
                        grantItemLocked(clipboard.primaryClip.getItemAt(i), clipboard.primaryClipUid, pkg, UserHandle.getUserId(uid));
                    }
                    clipboard.activePermissionOwners.add(pkg);
                }
            } else {
                throw new SecurityException("Calling uid " + uid + " does not own package " + pkg);
            }
        } catch (RemoteException e) {
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(oldIdentity);
            throw th;
        }
    }

    private final void revokeUriLocked(Uri uri, int sourceUid) {
        if (uri != null && "content".equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mAm.revokeUriPermissionFromOwner(this.mPermissionOwner, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)));
            } catch (RemoteException e) {
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(ident);
                throw th;
            }
            Binder.restoreCallingIdentity(ident);
        }
    }

    private final void revokeItemLocked(ClipData.Item item, int sourceUid) {
        if (item.getUri() != null) {
            revokeUriLocked(item.getUri(), sourceUid);
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            revokeUriLocked(intent.getData(), sourceUid);
        }
    }

    private final void revokeUris(PerUserClipboard clipboard) {
        if (clipboard.primaryClip != null) {
            int N = clipboard.primaryClip.getItemCount();
            for (int i = 0; i < N; i++) {
                revokeItemLocked(clipboard.primaryClip.getItemAt(i), clipboard.primaryClipUid);
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean clipboardAccessAllowed(int op, String callingPackage, int callingUid) {
        if (this.mAppOps.noteOp(op, callingUid, callingPackage) != 0) {
            return false;
        }
        try {
            if (!AppGlobals.getPackageManager().isInstantApp(callingPackage, UserHandle.getUserId(callingUid))) {
                return true;
            }
            return this.mAm.isAppForeground(callingUid);
        } catch (RemoteException e) {
            Slog.e("clipboard", "Failed to get Instant App status for package " + callingPackage, e);
            return false;
        }
    }
}
