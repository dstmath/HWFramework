package com.android.server.clipboard;

import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.IActivityManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipData.Item;
import android.content.ClipDescription;
import android.content.ContentProvider;
import android.content.Context;
import android.content.IClipboard.Stub;
import android.content.IOnPrimaryClipChangedListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IUserManager;
import android.os.Parcel;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Slog;
import android.util.SparseArray;
import java.util.HashSet;
import java.util.List;

public class ClipboardService extends Stub {
    private static final String TAG = "ClipboardService";
    private final IActivityManager mAm;
    private final AppOpsManager mAppOps;
    private SparseArray<PerUserClipboard> mClipboards;
    private final Context mContext;
    private final IBinder mPermissionOwner;
    private final PackageManager mPm;
    private final IUserManager mUm;

    private class ListenerInfo {
        final String mPackageName;
        final int mUid;

        ListenerInfo(int uid, String packageName) {
            this.mUid = uid;
            this.mPackageName = packageName;
        }
    }

    private class PerUserClipboard {
        final HashSet<String> activePermissionOwners;
        ClipData primaryClip;
        final RemoteCallbackList<IOnPrimaryClipChangedListener> primaryClipListeners;
        final int userId;

        PerUserClipboard(int userId) {
            this.primaryClipListeners = new RemoteCallbackList();
            this.activePermissionOwners = new HashSet();
            this.userId = userId;
        }
    }

    java.util.List<android.content.pm.UserInfo> getRelatedProfiles(int r8) {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:12:? in {3, 8, 9, 11, 13, 14} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
*/
        /*
        r7 = this;
        r2 = android.os.Binder.clearCallingIdentity();
        r4 = r7.mUm;	 Catch:{ RemoteException -> 0x000f, all -> 0x002f }
        r5 = 1;	 Catch:{ RemoteException -> 0x000f, all -> 0x002f }
        r1 = r4.getProfiles(r8, r5);	 Catch:{ RemoteException -> 0x000f, all -> 0x002f }
        android.os.Binder.restoreCallingIdentity(r2);
        return r1;
    L_0x000f:
        r0 = move-exception;
        r4 = "ClipboardService";	 Catch:{ RemoteException -> 0x000f, all -> 0x002f }
        r5 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x000f, all -> 0x002f }
        r5.<init>();	 Catch:{ RemoteException -> 0x000f, all -> 0x002f }
        r6 = "Remote Exception calling UserManager: ";	 Catch:{ RemoteException -> 0x000f, all -> 0x002f }
        r5 = r5.append(r6);	 Catch:{ RemoteException -> 0x000f, all -> 0x002f }
        r5 = r5.append(r0);	 Catch:{ RemoteException -> 0x000f, all -> 0x002f }
        r5 = r5.toString();	 Catch:{ RemoteException -> 0x000f, all -> 0x002f }
        android.util.Slog.e(r4, r5);	 Catch:{ RemoteException -> 0x000f, all -> 0x002f }
        r4 = 0;
        android.os.Binder.restoreCallingIdentity(r2);
        return r4;
    L_0x002f:
        r4 = move-exception;
        android.os.Binder.restoreCallingIdentity(r2);
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.clipboard.ClipboardService.getRelatedProfiles(int):java.util.List<android.content.pm.UserInfo>");
    }

    public ClipboardService(Context context) {
        this.mClipboards = new SparseArray();
        this.mContext = context;
        this.mAm = ActivityManagerNative.getDefault();
        this.mPm = context.getPackageManager();
        this.mUm = (IUserManager) ServiceManager.getService("user");
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        IBinder permOwner = null;
        try {
            permOwner = this.mAm.newUriPermissionOwner("clipboard");
        } catch (RemoteException e) {
            Slog.w("clipboard", "AM dead", e);
        }
        this.mPermissionOwner = permOwner;
        IntentFilter userFilter = new IntentFilter();
        userFilter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                    ClipboardService.this.removeClipboard(intent.getIntExtra("android.intent.extra.user_handle", 0));
                }
            }
        }, userFilter);
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

    private void removeClipboard(int userId) {
        synchronized (this.mClipboards) {
            this.mClipboards.remove(userId);
        }
    }

    public void setPrimaryClip(ClipData clip, String callingPackage) {
        synchronized (this) {
            if (clip != null) {
                if (clip.getItemCount() <= 0) {
                    throw new IllegalArgumentException("No items");
                }
            }
            int callingUid = Binder.getCallingUid();
            if (this.mAppOps.noteOp(30, callingUid, callingPackage) != 0) {
                return;
            }
            checkDataOwnerLocked(clip, callingUid);
            int userId = UserHandle.getUserId(callingUid);
            PerUserClipboard clipboard = getClipboard(userId);
            revokeUris(clipboard);
            setPrimaryClipInternal(clipboard, clip);
            List<UserInfo> related = getRelatedProfiles(userId);
            if (related != null) {
                int size = related.size();
                if (size > 1) {
                    boolean canCopy = false;
                    try {
                        canCopy = !this.mUm.getUserRestrictions(userId).getBoolean("no_cross_profile_copy_paste");
                    } catch (RemoteException e) {
                        Slog.e(TAG, "Remote Exception calling UserManager: " + e);
                    }
                    if (canCopy) {
                        clip.fixUrisLight(userId);
                    } else {
                        clip = null;
                    }
                    for (int i = 0; i < size; i++) {
                        int id = ((UserInfo) related.get(i)).id;
                        if (id != userId) {
                            setPrimaryClipInternal(getClipboard(id), clip);
                        }
                    }
                }
            }
        }
    }

    void setPrimaryClipInternal(PerUserClipboard clipboard, ClipData clip) {
        clipboard.activePermissionOwners.clear();
        if (clip != null || clipboard.primaryClip != null) {
            clipboard.primaryClip = clip;
            long ident = Binder.clearCallingIdentity();
            int n = clipboard.primaryClipListeners.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    ListenerInfo li = (ListenerInfo) clipboard.primaryClipListeners.getBroadcastCookie(i);
                    if (this.mAppOps.checkOpNoThrow(29, li.mUid, li.mPackageName) == 0) {
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

    public ClipData getPrimaryClip(String pkg) {
        synchronized (this) {
            if (this.mAppOps.noteOp(29, Binder.getCallingUid(), pkg) != 0) {
                return null;
            }
            addActiveOwnerLocked(Binder.getCallingUid(), pkg);
            ClipData clipData = getClipboard().primaryClip;
            return clipData;
        }
    }

    public ClipDescription getPrimaryClipDescription(String callingPackage) {
        ClipDescription clipDescription = null;
        synchronized (this) {
            if (this.mAppOps.checkOp(29, Binder.getCallingUid(), callingPackage) != 0) {
                return null;
            }
            PerUserClipboard clipboard = getClipboard();
            if (clipboard.primaryClip != null) {
                clipDescription = clipboard.primaryClip.getDescription();
            }
            return clipDescription;
        }
    }

    public boolean hasPrimaryClip(String callingPackage) {
        boolean z = false;
        synchronized (this) {
            if (this.mAppOps.checkOp(29, Binder.getCallingUid(), callingPackage) != 0) {
                return false;
            }
            if (getClipboard().primaryClip != null) {
                z = true;
            }
            return z;
        }
    }

    public void addPrimaryClipChangedListener(IOnPrimaryClipChangedListener listener, String callingPackage) {
        synchronized (this) {
            getClipboard().primaryClipListeners.register(listener, new ListenerInfo(Binder.getCallingUid(), callingPackage));
        }
    }

    public void removePrimaryClipChangedListener(IOnPrimaryClipChangedListener listener) {
        synchronized (this) {
            getClipboard().primaryClipListeners.unregister(listener);
        }
    }

    public boolean hasClipboardText(String callingPackage) {
        boolean z = false;
        synchronized (this) {
            if (this.mAppOps.checkOp(29, Binder.getCallingUid(), callingPackage) != 0) {
                return false;
            }
            PerUserClipboard clipboard = getClipboard();
            if (clipboard.primaryClip != null) {
                CharSequence text = clipboard.primaryClip.getItemAt(0).getText();
                if (text != null && text.length() > 0) {
                    z = true;
                }
                return z;
            }
            return false;
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
                if (clipboard.primaryClip != null && !clipboard.activePermissionOwners.contains(pkg)) {
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
}
