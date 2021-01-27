package com.android.server.clipboard;

import android.app.ActivityManagerInternal;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IUriGrantsManager;
import android.app.KeyguardManager;
import android.app.UriGrantsManager;
import android.common.HwFrameworkFactory;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ComponentName;
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
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.view.autofill.AutofillManagerInternal;
import android.widget.Toast;
import com.android.server.LocalServices;
import com.android.server.SystemService;
import com.android.server.UiThread;
import com.android.server.clipboard.HostClipboardMonitor;
import com.android.server.contentcapture.ContentCaptureManagerInternal;
import com.android.server.uri.UriGrantsManagerInternal;
import com.android.server.wm.WindowManagerInternal;
import com.huawei.android.content.IOnPrimaryClipGetedListener;
import com.huawei.android.server.clipboard.IHwClipboardServiceManager;
import huawei.android.security.IHwBehaviorCollectManager;
import java.util.HashSet;
import java.util.List;

public class ClipboardService extends SystemService {
    private static final boolean IS_EMULATOR = SystemProperties.getBoolean("ro.kernel.qemu", false);
    private static final String TAG = "ClipboardService";
    private final ActivityManagerInternal mAmInternal = ((ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class));
    private final AppOpsManager mAppOps = ((AppOpsManager) getContext().getSystemService("appops"));
    private final AutofillManagerInternal mAutofillInternal = ((AutofillManagerInternal) LocalServices.getService(AutofillManagerInternal.class));
    private final SparseArray<PerUserClipboard> mClipboards = new SparseArray<>();
    private final ContentCaptureManagerInternal mContentCaptureInternal = ((ContentCaptureManagerInternal) LocalServices.getService(ContentCaptureManagerInternal.class));
    private HostClipboardMonitor mHostClipboardMonitor = null;
    private Thread mHostMonitorThread = null;
    private final IBinder mPermissionOwner = this.mUgmInternal.newUriPermissionOwner("clipboard");
    private final PackageManager mPm = getContext().getPackageManager();
    private final IUriGrantsManager mUgm = UriGrantsManager.getService();
    private final UriGrantsManagerInternal mUgmInternal = ((UriGrantsManagerInternal) LocalServices.getService(UriGrantsManagerInternal.class));
    private final IUserManager mUm = ServiceManager.getService("user");
    private final WindowManagerInternal mWm = ((WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class));

    public ClipboardService(Context context) {
        super(context);
        if (IS_EMULATOR) {
            this.mHostClipboardMonitor = new HostClipboardMonitor(new HostClipboardMonitor.HostClipboardCallback() {
                /* class com.android.server.clipboard.ClipboardService.AnonymousClass1 */

                @Override // com.android.server.clipboard.HostClipboardMonitor.HostClipboardCallback
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

    /* JADX DEBUG: Multi-variable search result rejected for r2v0, resolved type: com.android.server.clipboard.ClipboardService */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.clipboard.ClipboardService$ClipboardImpl, android.os.IBinder] */
    /* JADX WARNING: Unknown variable types count: 1 */
    @Override // com.android.server.SystemService
    public void onStart() {
        publishBinderService("clipboard", new ClipboardImpl());
    }

    @Override // com.android.server.SystemService
    public void onCleanupUser(int userId) {
        synchronized (this.mClipboards) {
            this.mClipboards.remove(userId);
        }
    }

    /* access modifiers changed from: private */
    public class ListenerInfo {
        final String mPackageName;
        final int mUid;

        ListenerInfo(int uid, String packageName) {
            this.mUid = uid;
            this.mPackageName = packageName;
        }
    }

    /* access modifiers changed from: private */
    public class PerUserClipboard {
        final HashSet<String> activePermissionOwners = new HashSet<>();
        ClipData primaryClip;
        final RemoteCallbackList<IOnPrimaryClipChangedListener> primaryClipListeners = new RemoteCallbackList<>();
        int primaryClipUid = 9999;
        final int userId;

        PerUserClipboard(int userId2) {
            this.userId = userId2;
        }
    }

    private boolean isInternalSysWindowAppWithWindowFocus(String callingPackage) {
        if (this.mPm.checkPermission("android.permission.INTERNAL_SYSTEM_WINDOW", callingPackage) != 0 || !this.mWm.isUidFocused(Binder.getCallingUid())) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getIntendingUserId(String packageName, int userId) {
        int callingUserId = UserHandle.getUserId(Binder.getCallingUid());
        if (!UserManager.supportsMultipleUsers() || callingUserId == userId) {
            return callingUserId;
        }
        return this.mAmInternal.handleIncomingUser(Binder.getCallingPid(), Binder.getCallingUid(), userId, false, 2, "checkClipboardServiceCallingUser", packageName);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getIntendingUid(String packageName, int userId) {
        return UserHandle.getUid(getIntendingUserId(packageName, userId), UserHandle.getAppId(Binder.getCallingUid()));
    }

    private class ClipboardImpl extends IClipboard.Stub {
        HwInnerClipboardService mHwInnerServicel;

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

        public void setPrimaryClip(ClipData clip, String callingPackage, int userId) {
            synchronized (this) {
                if (clip != null) {
                    if (clip.getItemCount() > 0) {
                        int intendingUid = ClipboardService.this.getIntendingUid(callingPackage, userId);
                        if (ClipboardService.this.clipboardAccessAllowed(30, callingPackage, intendingUid, UserHandle.getUserId(intendingUid))) {
                            if (HwDeviceManager.disallowOp(23)) {
                                final Context context = ClipboardService.this.getContext();
                                UiThread.getHandler().post(new Runnable() {
                                    /* class com.android.server.clipboard.ClipboardService.ClipboardImpl.AnonymousClass1 */

                                    @Override // java.lang.Runnable
                                    public void run() {
                                        Context context = context;
                                        Toast toast = Toast.makeText(context, context.getResources().getString(33685904), 1);
                                        toast.getWindowParams().type = 2006;
                                        toast.show();
                                    }
                                });
                                return;
                            }
                            ClipboardService.this.checkDataOwnerLocked(clip, intendingUid);
                            try {
                                ClipboardService.this.setPrimaryClipInternal(clip, intendingUid);
                            } catch (RuntimeException e) {
                                Slog.e(ClipboardService.TAG, "callingPackage = " + callingPackage + ", intendingUid = " + intendingUid);
                            }
                            HwClipboardReadDelayer.setPrimaryClipNotify();
                            return;
                        }
                        return;
                    }
                }
                throw new IllegalArgumentException("No items");
            }
        }

        public void clearPrimaryClip(String callingPackage, int userId) {
            synchronized (this) {
                int intendingUid = ClipboardService.this.getIntendingUid(callingPackage, userId);
                if (ClipboardService.this.clipboardAccessAllowed(30, callingPackage, intendingUid, UserHandle.getUserId(intendingUid))) {
                    if (!HwDeviceManager.disallowOp(23)) {
                        ClipboardService.this.setPrimaryClipInternal(null, intendingUid);
                    }
                }
            }
        }

        public ClipData getPrimaryClip(String pkg, int userId) {
            HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.CLIPBOARD_GETPRIMARYCLIP);
            HwClipboardReadDelayer.getPrimaryClipNotify();
            synchronized (this) {
                int intendingUid = ClipboardService.this.getIntendingUid(pkg, userId);
                int intendingUserId = UserHandle.getUserId(intendingUid);
                boolean isAccessAlowed = ClipboardService.this.clipboardAccessAllowed(29, pkg, intendingUid, intendingUserId);
                Slog.i(ClipboardService.TAG, "getPrimaryClip pkg=" + pkg + ", intendingUid=" + intendingUid + ", isAccessAlowed=" + isAccessAlowed);
                if (isAccessAlowed && !ClipboardService.this.isDeviceLocked(intendingUserId)) {
                    if (!HwDeviceManager.disallowOp(23)) {
                        ClipboardService.this.addActiveOwnerLocked(intendingUid, pkg);
                        return ClipboardService.this.getClipboard(intendingUserId).primaryClip;
                    }
                }
                return null;
            }
        }

        public ClipDescription getPrimaryClipDescription(String callingPackage, int userId) {
            synchronized (this) {
                int intendingUid = ClipboardService.this.getIntendingUid(callingPackage, userId);
                int intendingUserId = UserHandle.getUserId(intendingUid);
                ClipDescription clipDescription = null;
                if (ClipboardService.this.clipboardAccessAllowed(29, callingPackage, intendingUid, intendingUserId) && !ClipboardService.this.isDeviceLocked(intendingUserId)) {
                    if (!HwDeviceManager.disallowOp(23)) {
                        PerUserClipboard clipboard = ClipboardService.this.getClipboard(intendingUserId);
                        if (clipboard.primaryClip != null) {
                            clipDescription = clipboard.primaryClip.getDescription();
                        }
                        return clipDescription;
                    }
                }
                return null;
            }
        }

        public boolean hasPrimaryClip(String callingPackage, int userId) {
            synchronized (this) {
                int intendingUid = ClipboardService.this.getIntendingUid(callingPackage, userId);
                int intendingUserId = UserHandle.getUserId(intendingUid);
                boolean z = false;
                if (ClipboardService.this.clipboardAccessAllowed(29, callingPackage, intendingUid, intendingUserId) && !ClipboardService.this.isDeviceLocked(intendingUserId)) {
                    if (!HwDeviceManager.disallowOp(23)) {
                        if (ClipboardService.this.getClipboard(intendingUserId).primaryClip != null) {
                            z = true;
                        }
                        return z;
                    }
                }
                return false;
            }
        }

        public void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener listener, String callingPackage, int userId) {
            if (listener == null) {
                Slog.e(ClipboardService.TAG, "addPrimaryClipChangedListener with listener is null");
                return;
            }
            HwFrameworkFactory.getHwBehaviorCollectManager().sendBehavior(IHwBehaviorCollectManager.BehaviorId.CLIPBOARD_ADDPRIMARYCLIPCHANGEDLISTENER);
            synchronized (this) {
                int intendingUid = ClipboardService.this.getIntendingUid(callingPackage, userId);
                ClipboardService.this.getClipboard(UserHandle.getUserId(intendingUid)).primaryClipListeners.register(listener, new ListenerInfo(intendingUid, callingPackage));
            }
        }

        public void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener listener, String callingPackage, int userId) {
            if (listener == null) {
                Slog.e(ClipboardService.TAG, "removePrimaryClipChangedListener with listener is null");
                return;
            }
            synchronized (this) {
                ClipboardService.this.getClipboard(ClipboardService.this.getIntendingUserId(callingPackage, userId)).primaryClipListeners.unregister(listener);
            }
        }

        public boolean hasClipboardText(String callingPackage, int userId) {
            synchronized (this) {
                int intendingUid = ClipboardService.this.getIntendingUid(callingPackage, userId);
                int intendingUserId = UserHandle.getUserId(intendingUid);
                boolean z = false;
                if (ClipboardService.this.clipboardAccessAllowed(29, callingPackage, intendingUid, intendingUserId) && !ClipboardService.this.isDeviceLocked(intendingUserId)) {
                    if (!HwDeviceManager.disallowOp(23)) {
                        PerUserClipboard clipboard = ClipboardService.this.getClipboard(intendingUserId);
                        if (clipboard.primaryClip == null) {
                            return false;
                        }
                        CharSequence text = clipboard.primaryClip.getItemAt(0).getText();
                        if (text != null && text.length() > 0) {
                            z = true;
                        }
                        return z;
                    }
                }
                return false;
            }
        }

        /* JADX WARN: Type inference failed for: r0v0, types: [com.android.server.clipboard.ClipboardService$ClipboardImpl$HwInnerClipboardService, android.os.IBinder] */
        /* JADX WARNING: Unknown variable types count: 1 */
        public IBinder getHwInnerService() {
            return this.mHwInnerServicel;
        }

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
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private PerUserClipboard getClipboard(int userId) {
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
    public void setPrimaryClipInternal(ClipData clip, int uid) {
        int size;
        CharSequence text;
        HostClipboardMonitor hostClipboardMonitor = this.mHostClipboardMonitor;
        if (hostClipboardMonitor != null) {
            if (clip == null) {
                hostClipboardMonitor.setHostClipboard("");
            } else if (clip.getItemCount() > 0 && (text = clip.getItemAt(0).getText()) != null) {
                this.mHostClipboardMonitor.setHostClipboard(text.toString());
            }
        }
        int userId = UserHandle.getUserId(uid);
        setPrimaryClipInternal(getClipboard(userId), clip, uid);
        List<UserInfo> related = getRelatedProfiles(userId);
        if (related != null && (size = related.size()) > 1) {
            if (!(!hasRestriction("no_cross_profile_copy_paste", userId))) {
                clip = null;
            } else if (clip != null) {
                clip = new ClipData(clip);
                for (int i = clip.getItemCount() - 1; i >= 0; i--) {
                    clip.setItemAt(i, new ClipData.Item(clip.getItemAt(i)));
                }
                clip.fixUrisLight(userId);
            }
            for (int i2 = 0; i2 < size; i2++) {
                int id = related.get(i2).id;
                if (id != userId && (!hasRestriction("no_sharing_into_profile", id))) {
                    setPrimaryClipInternal(getClipboard(id), clip, uid);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setPrimaryClipInternal(PerUserClipboard clipboard, ClipData clip, int uid) {
        ClipDescription description;
        revokeUris(clipboard);
        clipboard.activePermissionOwners.clear();
        if (!(clip == null && clipboard.primaryClip == null)) {
            clipboard.primaryClip = clip;
            if (clip != null) {
                clipboard.primaryClipUid = uid;
            } else {
                clipboard.primaryClipUid = 9999;
            }
            if (!(clip == null || (description = clip.getDescription()) == null)) {
                description.setTimestamp(System.currentTimeMillis());
            }
            long ident = Binder.clearCallingIdentity();
            int n = clipboard.primaryClipListeners.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ListenerInfo li = (ListenerInfo) clipboard.primaryClipListeners.getBroadcastCookie(i);
                    if (clipboardAccessAllowed(29, li.mPackageName, li.mUid, UserHandle.getUserId(li.mUid))) {
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
    /* access modifiers changed from: public */
    private boolean isDeviceLocked(int userId) {
        long token = Binder.clearCallingIdentity();
        try {
            KeyguardManager keyguardManager = (KeyguardManager) getContext().getSystemService(KeyguardManager.class);
            return keyguardManager != null && keyguardManager.isDeviceLocked(userId);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private final void checkUriOwnerLocked(Uri uri, int sourceUid) {
        if (uri != null && "content".equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mUgmInternal.checkGrantUriPermission(sourceUid, (String) null, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)));
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
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
    /* access modifiers changed from: public */
    private final void checkDataOwnerLocked(ClipData data, int uid) {
        int N = data.getItemCount();
        for (int i = 0; i < N; i++) {
            checkItemOwnerLocked(data.getItemAt(i), uid);
        }
    }

    private final void grantUriLocked(Uri uri, int sourceUid, String targetPkg, int targetUserId) {
        if (uri != null && "content".equals(uri.getScheme())) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mUgm.grantUriPermissionFromOwner(this.mPermissionOwner, sourceUid, targetPkg, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)), targetUserId);
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
    /* access modifiers changed from: public */
    private final void addActiveOwnerLocked(int uid, String pkg) {
        IPackageManager pm = AppGlobals.getPackageManager();
        int targetUserHandle = UserHandle.getCallingUserId();
        long oldIdentity = Binder.clearCallingIdentity();
        try {
            PackageInfo pi = pm.getPackageInfo(pkg, 0, targetUserHandle);
            if (pi == null) {
                throw new IllegalArgumentException("Unknown package " + pkg);
            } else if (UserHandle.isSameApp(pi.applicationInfo.uid, uid)) {
                Binder.restoreCallingIdentity(oldIdentity);
                PerUserClipboard clipboard = getClipboard(UserHandle.getUserId(uid));
                if (!(clipboard.primaryClip == null || clipboard.activePermissionOwners.contains(pkg))) {
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
                this.mUgmInternal.revokeUriPermissionFromOwner(this.mPermissionOwner, ContentProvider.getUriWithoutUserId(uri), 1, ContentProvider.getUserIdFromUri(uri, UserHandle.getUserId(sourceUid)));
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
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
    /* access modifiers changed from: public */
    private boolean clipboardAccessAllowed(int op, String callingPackage, int uid, int userId) {
        AutofillManagerInternal autofillManagerInternal;
        ContentCaptureManagerInternal contentCaptureManagerInternal;
        boolean allowed = false;
        try {
            if (this.mAppOps.noteOp(op, uid, callingPackage) != 0) {
                return false;
            }
            if (this.mPm.checkPermission("android.permission.READ_CLIPBOARD_IN_BACKGROUND", callingPackage) == 0) {
                return true;
            }
            String defaultIme = Settings.Secure.getStringForUser(getContext().getContentResolver(), "default_input_method", userId);
            if (!TextUtils.isEmpty(defaultIme) && ComponentName.unflattenFromString(defaultIme).getPackageName().equals(callingPackage)) {
                return true;
            }
            if (op == 29) {
                boolean isFocusedWindow = this.mWm.isUidFocused(uid);
                if (isFocusedWindow || isInternalSysWindowAppWithWindowFocus(callingPackage)) {
                    allowed = true;
                }
                Slog.i(TAG, "callingPackage:" + callingPackage + ", uid =" + uid + ", isFocusedWindow =" + isFocusedWindow);
                if (!allowed && (contentCaptureManagerInternal = this.mContentCaptureInternal) != null) {
                    allowed = contentCaptureManagerInternal.isContentCaptureServiceForUser(uid, userId);
                    Slog.i(TAG, "isContentCaptureServiceForUser:" + allowed);
                }
                if (!allowed && (autofillManagerInternal = this.mAutofillInternal) != null) {
                    allowed = autofillManagerInternal.isAugmentedAutofillServiceForUser(uid, userId);
                    Slog.i(TAG, "isAugmentedAutofillServiceForUser:" + allowed);
                }
                if (!allowed) {
                    Slog.e(TAG, "Denying clipboard access to " + callingPackage + ", application is not in focus neither is a system service for user " + userId);
                }
                return allowed;
            } else if (op == 30) {
                return true;
            } else {
                throw new IllegalArgumentException("Unknown clipboard appop " + op);
            }
        } catch (SecurityException e) {
            Slog.e(TAG, "Calling uid " + uid + " does not own package " + callingPackage);
            return false;
        }
    }
}
