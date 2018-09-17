package com.android.server.clipboard;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.app.KeyguardManager;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.Context;
import android.content.IClipboard.Stub;
import android.content.IOnPrimaryClipChangedListener;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.hdm.HwDeviceManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.Parcel;
import android.os.Process;
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
import com.android.server.clipboard.HostClipboardMonitor.HostClipboardCallback;
import java.util.HashSet;
import java.util.List;

public class ClipboardService extends SystemService {
    private static final boolean IS_EMULATOR = SystemProperties.getBoolean("ro.kernel.qemu", false);
    private static final String TAG = "ClipboardService";
    private final IActivityManager mAm = ActivityManager.getService();
    private final AppOpsManager mAppOps = ((AppOpsManager) getContext().getSystemService("appops"));
    private final SparseArray<PerUserClipboard> mClipboards = new SparseArray();
    private HostClipboardMonitor mHostClipboardMonitor = null;
    private Thread mHostMonitorThread = null;
    private final IBinder mPermissionOwner;
    private final PackageManager mPm = getContext().getPackageManager();
    private final IUserManager mUm = ((IUserManager) ServiceManager.getService("user"));

    private class ClipboardImpl extends Stub {
        /* synthetic */ ClipboardImpl(ClipboardService this$0, ClipboardImpl -this1) {
            this();
        }

        private ClipboardImpl() {
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            try {
                return super.onTransact(code, data, reply, flags);
            } catch (RuntimeException e) {
                if (!(e instanceof SecurityException)) {
                    Slog.wtf("clipboard", "Exception: ", e);
                }
                throw e;
            }
        }

        /* JADX WARNING: Missing block: B:24:0x006f, code:
            return;
     */
        /* JADX WARNING: Missing block: B:54:0x0112, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void setPrimaryClip(ClipData clip, String callingPackage) {
            Throwable th;
            synchronized (this) {
                if (clip != null) {
                    try {
                        if (clip.getItemCount() <= 0) {
                            throw new IllegalArgumentException("No items");
                        }
                    } catch (RemoteException e) {
                        Slog.e(ClipboardService.TAG, "Remote Exception calling UserManager: " + e);
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
                if (!(clip.getItemAt(0).getText() == null || ClipboardService.this.mHostClipboardMonitor == null)) {
                    ClipboardService.this.mHostClipboardMonitor.setHostClipboard(clip.getItemAt(0).getText().toString());
                }
                int callingUid = Binder.getCallingUid();
                if (ClipboardService.this.clipboardAccessAllowed(30, callingPackage, callingUid)) {
                    ClipboardService.this.checkDataOwnerLocked(clip, callingUid);
                    int userId = UserHandle.getUserId(callingUid);
                    PerUserClipboard clipboard = ClipboardService.this.getClipboard(userId);
                    ClipboardService.this.revokeUris(clipboard);
                    ClipboardService.this.setPrimaryClipInternal(clipboard, clip);
                    List<UserInfo> related = ClipboardService.this.getRelatedProfiles(userId);
                    if (related != null) {
                        int size = related.size();
                        if (size > 1) {
                            int i;
                            boolean canCopy = false;
                            canCopy = ClipboardService.this.mUm.getUserRestrictions(userId).getBoolean("no_cross_profile_copy_paste") ^ 1;
                            if (canCopy) {
                                ClipData clip2 = new ClipData(clip);
                                try {
                                    for (i = clip2.getItemCount() - 1; i >= 0; i--) {
                                        clip2.setItemAt(i, new Item(clip2.getItemAt(i)));
                                    }
                                    clip2.fixUrisLight(userId);
                                    clip = clip2;
                                } catch (Throwable th3) {
                                    th = th3;
                                    clip = clip2;
                                    throw th;
                                }
                            }
                            clip = null;
                            for (i = 0; i < size; i++) {
                                int id = ((UserInfo) related.get(i)).id;
                                if (id != userId) {
                                    ClipboardService.this.setPrimaryClipInternal(ClipboardService.this.getClipboard(id), clip);
                                }
                            }
                        }
                    }
                } else {
                    final Context context = ClipboardService.this.getContext();
                    if (HwDeviceManager.disallowOp(23) && context != null) {
                        UiThread.getHandler().post(new Runnable() {
                            public void run() {
                                Toast toast = Toast.makeText(context, context.getResources().getString(33685904), 1);
                                toast.getWindowParams().type = 2006;
                                toast.show();
                            }
                        });
                    }
                }
            }
        }

        public ClipData getPrimaryClip(String pkg) {
            synchronized (this) {
                if (!ClipboardService.this.clipboardAccessAllowed(29, pkg, Binder.getCallingUid()) || ClipboardService.this.isDeviceLocked()) {
                    return null;
                }
                ClipboardService.this.addActiveOwnerLocked(Binder.getCallingUid(), pkg);
                ClipData clipData = ClipboardService.this.getClipboard().primaryClip;
                return clipData;
            }
        }

        /* JADX WARNING: Missing block: B:8:0x0019, code:
            return null;
     */
        /* JADX WARNING: Missing block: B:14:0x002b, code:
            return r1;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public ClipDescription getPrimaryClipDescription(String callingPackage) {
            ClipDescription clipDescription = null;
            synchronized (this) {
                if (!ClipboardService.this.clipboardAccessAllowed(29, callingPackage, Binder.getCallingUid()) || ClipboardService.this.isDeviceLocked()) {
                } else {
                    PerUserClipboard clipboard = ClipboardService.this.getClipboard();
                    if (clipboard.primaryClip != null) {
                        clipDescription = clipboard.primaryClip.getDescription();
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:8:0x0019, code:
            return false;
     */
        /* JADX WARNING: Missing block: B:14:0x0026, code:
            return r0;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean hasPrimaryClip(String callingPackage) {
            boolean z = false;
            synchronized (this) {
                if (!ClipboardService.this.clipboardAccessAllowed(29, callingPackage, Binder.getCallingUid()) || ClipboardService.this.isDeviceLocked()) {
                } else if (ClipboardService.this.getClipboard().primaryClip != null) {
                    z = true;
                }
            }
        }

        public void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener listener, String callingPackage) {
            synchronized (this) {
                ClipboardService.this.getClipboard().primaryClipListeners.register(listener, new ListenerInfo(Binder.getCallingUid(), callingPackage));
            }
        }

        public void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener listener) {
            synchronized (this) {
                ClipboardService.this.getClipboard().primaryClipListeners.unregister(listener);
            }
        }

        /* JADX WARNING: Missing block: B:8:0x0019, code:
            return false;
     */
        /* JADX WARNING: Missing block: B:18:0x0039, code:
            return r2;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean hasClipboardText(String callingPackage) {
            boolean z = false;
            synchronized (this) {
                if (!ClipboardService.this.clipboardAccessAllowed(29, callingPackage, Binder.getCallingUid()) || ClipboardService.this.isDeviceLocked()) {
                } else {
                    PerUserClipboard clipboard = ClipboardService.this.getClipboard();
                    if (clipboard.primaryClip != null) {
                        CharSequence text = clipboard.primaryClip.getItemAt(0).getText();
                        if (text != null && text.length() > 0) {
                            z = true;
                        }
                    } else {
                        return false;
                    }
                }
            }
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
        final HashSet<String> activePermissionOwners = new HashSet();
        ClipData primaryClip;
        final RemoteCallbackList<IOnPrimaryClipChangedListener> primaryClipListeners = new RemoteCallbackList();
        final int userId;

        PerUserClipboard(int userId) {
            this.userId = userId;
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
            this.mHostClipboardMonitor = new HostClipboardMonitor(new HostClipboardCallback() {
                public void onHostClipboardUpdated(String contents) {
                    ClipData clip = new ClipData("host clipboard", new String[]{"text/plain"}, new Item(contents));
                    synchronized (ClipboardService.this.mClipboards) {
                        ClipboardService.this.setPrimaryClipInternal(ClipboardService.this.getClipboard(0), clip);
                    }
                }
            });
            this.mHostMonitorThread = new Thread(this.mHostClipboardMonitor);
            this.mHostMonitorThread.start();
        }
    }

    public void onStart() {
        publishBinderService("clipboard", new ClipboardImpl(this, null));
    }

    public void onCleanupUser(int userId) {
        synchronized (this.mClipboards) {
            this.mClipboards.remove(userId);
        }
    }

    private PerUserClipboard getClipboard() {
        return getClipboard(UserHandle.getCallingUserId());
    }

    private PerUserClipboard getClipboard(int userId) {
        PerUserClipboard puc;
        synchronized (this.mClipboards) {
            puc = (PerUserClipboard) this.mClipboards.get(userId);
            if (puc == null) {
                puc = new PerUserClipboard(userId);
                this.mClipboards.put(userId, puc);
            }
        }
        return puc;
    }

    List<UserInfo> getRelatedProfiles(int userId) {
        long origId = Binder.clearCallingIdentity();
        try {
            List<UserInfo> related = this.mUm.getProfiles(userId, true);
            Binder.restoreCallingIdentity(origId);
            return related;
        } catch (RemoteException e) {
            Slog.e(TAG, "Remote Exception calling UserManager: " + e);
            Binder.restoreCallingIdentity(origId);
            return null;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(origId);
            throw th;
        }
    }

    void setPrimaryClipInternal(PerUserClipboard clipboard, ClipData clip) {
        clipboard.activePermissionOwners.clear();
        if (clip != null || clipboard.primaryClip != null) {
            clipboard.primaryClip = clip;
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
                        ((IOnPrimaryClipChangedListener) clipboard.primaryClipListeners.getBroadcastItem(i)).dispatchPrimaryClipChanged();
                    }
                } catch (RemoteException e) {
                } catch (Throwable th) {
                    clipboard.primaryClipListeners.finishBroadcast();
                    Binder.restoreCallingIdentity(ident);
                }
            }
            clipboard.primaryClipListeners.finishBroadcast();
            Binder.restoreCallingIdentity(ident);
        }
    }

    private boolean isDeviceLocked() {
        int callingUserId = UserHandle.getCallingUserId();
        long token = Binder.clearCallingIdentity();
        try {
            KeyguardManager keyguardManager = (KeyguardManager) getContext().getSystemService(KeyguardManager.class);
            boolean isDeviceLocked = keyguardManager != null ? keyguardManager.isDeviceLocked(callingUserId) : false;
            Binder.restoreCallingIdentity(token);
            return isDeviceLocked;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    private final void checkUriOwnerLocked(Uri uri, int uid) {
        if ("content".equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mAm.checkGrantUriPermission(uid, null, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(uid)));
            } catch (RemoteException e) {
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }
    }

    private final void checkItemOwnerLocked(Item item, int uid) {
        if (item.getUri() != null) {
            checkUriOwnerLocked(item.getUri(), uid);
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            checkUriOwnerLocked(intent.getData(), uid);
        }
    }

    private final void checkDataOwnerLocked(ClipData data, int uid) {
        int N = data.getItemCount();
        for (int i = 0; i < N; i++) {
            checkItemOwnerLocked(data.getItemAt(i), uid);
        }
    }

    private final void grantUriLocked(Uri uri, String pkg, int userId) {
        long ident = Binder.clearCallingIdentity();
        try {
            int sourceUserId = ContentProvider.getUserIdFromUri(uri, userId);
            this.mAm.grantUriPermissionFromOwner(this.mPermissionOwner, Process.myUid(), pkg, ContentProvider.getUriWithoutUserId(uri), 1, sourceUserId, userId);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private final void grantItemLocked(Item item, String pkg, int userId) {
        if (item.getUri() != null) {
            grantUriLocked(item.getUri(), pkg, userId);
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            grantUriLocked(intent.getData(), pkg, userId);
        }
    }

    private final void addActiveOwnerLocked(int uid, String pkg) {
        IPackageManager pm = AppGlobals.getPackageManager();
        int targetUserHandle = UserHandle.getCallingUserId();
        long oldIdentity = Binder.clearCallingIdentity();
        try {
            PackageInfo pi = pm.getPackageInfo(pkg, 0, targetUserHandle);
            if (pi == null) {
                throw new IllegalArgumentException("Unknown package " + pkg);
            } else if (UserHandle.isSameApp(pi.applicationInfo.uid, uid)) {
                PerUserClipboard clipboard = getClipboard();
                if (clipboard.primaryClip != null && (clipboard.activePermissionOwners.contains(pkg) ^ 1) != 0) {
                    int N = clipboard.primaryClip.getItemCount();
                    for (int i = 0; i < N; i++) {
                        grantItemLocked(clipboard.primaryClip.getItemAt(i), pkg, UserHandle.getUserId(uid));
                    }
                    clipboard.activePermissionOwners.add(pkg);
                }
            } else {
                throw new SecurityException("Calling uid " + uid + " does not own package " + pkg);
            }
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(oldIdentity);
        }
    }

    private final void revokeUriLocked(Uri uri) {
        int userId = ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(Binder.getCallingUid()));
        long ident = Binder.clearCallingIdentity();
        try {
            this.mAm.revokeUriPermissionFromOwner(this.mPermissionOwner, ContentProvider.getUriWithoutUserId(uri), 3, userId);
        } catch (RemoteException e) {
        } finally {
            Binder.restoreCallingIdentity(ident);
        }
    }

    private final void revokeItemLocked(Item item) {
        if (item.getUri() != null) {
            revokeUriLocked(item.getUri());
        }
        Intent intent = item.getIntent();
        if (intent != null && intent.getData() != null) {
            revokeUriLocked(intent.getData());
        }
    }

    private final void revokeUris(PerUserClipboard clipboard) {
        if (clipboard.primaryClip != null) {
            int N = clipboard.primaryClip.getItemCount();
            for (int i = 0; i < N; i++) {
                revokeItemLocked(clipboard.primaryClip.getItemAt(i));
            }
        }
    }

    private boolean clipboardAccessAllowed(int op, String callingPackage, int callingUid) {
        if (this.mAppOps.checkOp(op, callingUid, callingPackage) != 0) {
            return false;
        }
        if (HwDeviceManager.disallowOp(23)) {
            Slog.i(TAG, "Clipboard is not allowed by MDM!");
            return false;
        }
        try {
            if (AppGlobals.getPackageManager().isInstantApp(callingPackage, UserHandle.getUserId(callingUid))) {
                return this.mAm.isAppForeground(callingUid);
            }
            return true;
        } catch (RemoteException e) {
            Slog.e("clipboard", "Failed to get Instant App status for package " + callingPackage, e);
            return false;
        }
    }
}
