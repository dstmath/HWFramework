package com.android.server.media.projection;

import android.app.AppOpsManager;
import android.content.Context;
import android.media.MediaRouter;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionCallback;
import android.media.projection.IMediaProjectionManager;
import android.media.projection.IMediaProjectionWatcherCallback;
import android.media.projection.MediaProjectionInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.util.DumpUtils;
import com.android.server.HwServiceExFactory;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.huawei.server.media.projection.IHwMediaProjectionManagerServiceEx;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;

public final class MediaProjectionManagerService extends SystemService implements IHwMediaProjectionManagerServiceInner, Watchdog.Monitor {
    private static final String TAG = "MediaProjectionManagerService";
    public static boolean sHasStartedInSystemserver = false;
    /* access modifiers changed from: private */
    public final AppOpsManager mAppOps;
    /* access modifiers changed from: private */
    public final CallbackDelegate mCallbackDelegate;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final Map<IBinder, IBinder.DeathRecipient> mDeathEaters;
    /* access modifiers changed from: private */
    public final IHwMediaProjectionManagerServiceEx mHwMPMSEx;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public MediaRouter.RouteInfo mMediaRouteInfo;
    private final MediaRouter mMediaRouter;
    private final MediaRouterCallback mMediaRouterCallback;
    /* access modifiers changed from: private */
    public MediaProjection mProjectionGrant;
    private IBinder mProjectionToken;

    private final class BinderService extends IMediaProjectionManager.Stub {
        private BinderService() {
        }

        public boolean hasProjectionPermission(int uid, String packageName) {
            long token = Binder.clearCallingIdentity();
            boolean z = false;
            try {
                if (checkPermission(packageName, "android.permission.CAPTURE_VIDEO_OUTPUT") || MediaProjectionManagerService.this.mAppOps.noteOpNoThrow(46, uid, packageName) == 0) {
                    z = true;
                }
                return z | false;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public IMediaProjection createProjection(int uid, String packageName, int type, boolean isPermanentGrant) {
            if (MediaProjectionManagerService.this.mContext.checkCallingPermission("android.permission.MANAGE_MEDIA_PROJECTION") != 0) {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to grant projection permission");
            } else if (packageName == null || packageName.isEmpty()) {
                throw new IllegalArgumentException("package name must not be empty");
            } else {
                long callingToken = Binder.clearCallingIdentity();
                try {
                    MediaProjection projection = new MediaProjection(type, uid, packageName);
                    if (isPermanentGrant) {
                        MediaProjectionManagerService.this.mAppOps.setMode(46, projection.uid, projection.packageName, 0);
                    }
                    return projection;
                } finally {
                    Binder.restoreCallingIdentity(callingToken);
                }
            }
        }

        public boolean isValidMediaProjection(IMediaProjection projection) {
            return MediaProjectionManagerService.this.isValidMediaProjection(projection.asBinder());
        }

        public MediaProjectionInfo getActiveProjectionInfo() {
            if (MediaProjectionManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.MANAGE_MEDIA_PROJECTION") == 0) {
                long token = Binder.clearCallingIdentity();
                try {
                    return MediaProjectionManagerService.this.getActiveProjectionInfo();
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to add projection callbacks");
            }
        }

        public void stopActiveProjection() {
            if (MediaProjectionManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.MANAGE_MEDIA_PROJECTION") == 0) {
                long token = Binder.clearCallingIdentity();
                try {
                    if (MediaProjectionManagerService.this.mProjectionGrant != null) {
                        MediaProjectionManagerService.this.mProjectionGrant.stop();
                    }
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to add projection callbacks");
            }
        }

        public void addCallback(IMediaProjectionWatcherCallback callback) {
            if (MediaProjectionManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.MANAGE_MEDIA_PROJECTION") == 0) {
                long token = Binder.clearCallingIdentity();
                try {
                    MediaProjectionManagerService.this.addCallback(callback);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to add projection callbacks");
            }
        }

        public void removeCallback(IMediaProjectionWatcherCallback callback) {
            if (MediaProjectionManagerService.this.mContext.checkCallingPermission("android.permission.MANAGE_MEDIA_PROJECTION") == 0) {
                long token = Binder.clearCallingIdentity();
                try {
                    MediaProjectionManagerService.this.removeCallback(callback);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to remove projection callbacks");
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(MediaProjectionManagerService.this.mContext, MediaProjectionManagerService.TAG, pw)) {
                long token = Binder.clearCallingIdentity();
                try {
                    MediaProjectionManagerService.this.dump(pw);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }

        private boolean checkPermission(String packageName, String permission) {
            return MediaProjectionManagerService.this.mContext.getPackageManager().checkPermission(permission, packageName) == 0;
        }
    }

    private static class CallbackDelegate {
        private Map<IBinder, IMediaProjectionCallback> mClientCallbacks = new ArrayMap();
        private Handler mHandler = new Handler(Looper.getMainLooper(), null, true);
        private Object mLock = new Object();
        private Map<IBinder, IMediaProjectionWatcherCallback> mWatcherCallbacks = new ArrayMap();

        public void add(IMediaProjectionCallback callback) {
            synchronized (this.mLock) {
                this.mClientCallbacks.put(callback.asBinder(), callback);
            }
        }

        public void add(IMediaProjectionWatcherCallback callback) {
            synchronized (this.mLock) {
                this.mWatcherCallbacks.put(callback.asBinder(), callback);
            }
        }

        public void remove(IMediaProjectionCallback callback) {
            synchronized (this.mLock) {
                this.mClientCallbacks.remove(callback.asBinder());
            }
        }

        public void remove(IMediaProjectionWatcherCallback callback) {
            synchronized (this.mLock) {
                this.mWatcherCallbacks.remove(callback.asBinder());
            }
        }

        public void dispatchStart(MediaProjection projection) {
            if (projection == null) {
                Slog.e(MediaProjectionManagerService.TAG, "Tried to dispatch start notification for a null media projection. Ignoring!");
                return;
            }
            synchronized (this.mLock) {
                for (IMediaProjectionWatcherCallback callback : this.mWatcherCallbacks.values()) {
                    this.mHandler.post(new WatcherStartCallback(projection.getProjectionInfo(), callback));
                }
            }
        }

        public void dispatchStop(MediaProjection projection) {
            if (projection == null) {
                Slog.e(MediaProjectionManagerService.TAG, "Tried to dispatch stop notification for a null media projection. Ignoring!");
                return;
            }
            synchronized (this.mLock) {
                for (IMediaProjectionCallback callback : this.mClientCallbacks.values()) {
                    this.mHandler.post(new ClientStopCallback(callback));
                }
                for (IMediaProjectionWatcherCallback callback2 : this.mWatcherCallbacks.values()) {
                    this.mHandler.post(new WatcherStopCallback(projection.getProjectionInfo(), callback2));
                }
            }
        }
    }

    private static final class ClientStopCallback implements Runnable {
        private IMediaProjectionCallback mCallback;

        public ClientStopCallback(IMediaProjectionCallback callback) {
            this.mCallback = callback;
        }

        public void run() {
            try {
                this.mCallback.onStop();
            } catch (RemoteException e) {
                Slog.w(MediaProjectionManagerService.TAG, "Failed to notify media projection has stopped", e);
            }
        }
    }

    private final class MediaProjection extends IMediaProjection.Stub {
        private IMediaProjectionCallback mCallback;
        private IBinder.DeathRecipient mDeathEater;
        private IBinder mToken;
        private int mType;
        public final String packageName;
        public final int uid;
        public final UserHandle userHandle;

        public MediaProjection(int type, int uid2, String packageName2) {
            this.mType = type;
            this.uid = uid2;
            this.packageName = packageName2;
            this.userHandle = new UserHandle(UserHandle.getUserId(uid2));
        }

        public boolean canProjectVideo() {
            return this.mType == 1 || this.mType == 0;
        }

        public boolean canProjectSecureVideo() {
            return false;
        }

        public boolean canProjectAudio() {
            return this.mType == 1 || this.mType == 2;
        }

        public int applyVirtualDisplayFlags(int flags) {
            if (this.mType == 0) {
                return (flags & -9) | 18;
            }
            if (this.mType == 1) {
                return (flags & -18) | 10;
            }
            if (this.mType == 2) {
                return (flags & -9) | 19;
            }
            throw new RuntimeException("Unknown MediaProjection type");
        }

        public void start(final IMediaProjectionCallback callback) {
            if (callback != null) {
                synchronized (MediaProjectionManagerService.this.mLock) {
                    if (MediaProjectionManagerService.this.isValidMediaProjection(asBinder())) {
                        throw new IllegalStateException("Cannot start already started MediaProjection");
                    } else if (MediaProjectionManagerService.this.mHwMPMSEx.shouldPreventMediaProjection(Binder.getCallingUid())) {
                        Slog.d(MediaProjectionManagerService.TAG, "SecurityProfile denied the start request of MediaProjection");
                    } else {
                        this.mCallback = callback;
                        registerCallback(this.mCallback);
                        try {
                            this.mToken = callback.asBinder();
                            this.mDeathEater = new IBinder.DeathRecipient() {
                                public void binderDied() {
                                    MediaProjectionManagerService.this.mCallbackDelegate.remove(callback);
                                    MediaProjection.this.stop();
                                }
                            };
                            this.mToken.linkToDeath(this.mDeathEater, 0);
                            MediaProjectionManagerService.this.startProjectionLocked(this);
                        } catch (RemoteException e) {
                            Slog.w(MediaProjectionManagerService.TAG, "MediaProjectionCallbacks must be valid, aborting MediaProjection", e);
                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("callback must not be null");
            }
        }

        public void stop() {
            synchronized (MediaProjectionManagerService.this.mLock) {
                if (!MediaProjectionManagerService.this.isValidMediaProjection(asBinder())) {
                    Slog.w(MediaProjectionManagerService.TAG, "Attempted to stop inactive MediaProjection (uid=" + Binder.getCallingUid() + ", pid=" + Binder.getCallingPid() + ")");
                    return;
                }
                MediaProjectionManagerService.this.stopProjectionLocked(this);
                this.mToken.unlinkToDeath(this.mDeathEater, 0);
                this.mToken = null;
                unregisterCallback(this.mCallback);
                this.mCallback = null;
            }
        }

        public void registerCallback(IMediaProjectionCallback callback) {
            if (callback != null) {
                MediaProjectionManagerService.this.mCallbackDelegate.add(callback);
                return;
            }
            throw new IllegalArgumentException("callback must not be null");
        }

        public void unregisterCallback(IMediaProjectionCallback callback) {
            if (callback != null) {
                MediaProjectionManagerService.this.mCallbackDelegate.remove(callback);
                return;
            }
            throw new IllegalArgumentException("callback must not be null");
        }

        public MediaProjectionInfo getProjectionInfo() {
            return new MediaProjectionInfo(this.packageName, this.userHandle);
        }

        public void dump(PrintWriter pw) {
            pw.println("(" + this.packageName + ", uid=" + this.uid + "): " + MediaProjectionManagerService.typeToString(this.mType));
        }
    }

    private class MediaRouterCallback extends MediaRouter.SimpleCallback {
        private MediaRouterCallback() {
        }

        public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
            synchronized (MediaProjectionManagerService.this.mLock) {
                if ((type & 4) != 0) {
                    try {
                        MediaRouter.RouteInfo unused = MediaProjectionManagerService.this.mMediaRouteInfo = info;
                        if (MediaProjectionManagerService.this.mProjectionGrant != null) {
                            MediaProjectionManagerService.this.mProjectionGrant.stop();
                        }
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            }
        }

        public void onRouteUnselected(MediaRouter route, int type, MediaRouter.RouteInfo info) {
            if (MediaProjectionManagerService.this.mMediaRouteInfo == info) {
                MediaRouter.RouteInfo unused = MediaProjectionManagerService.this.mMediaRouteInfo = null;
            }
        }
    }

    private static final class WatcherStartCallback implements Runnable {
        private IMediaProjectionWatcherCallback mCallback;
        private MediaProjectionInfo mInfo;

        public WatcherStartCallback(MediaProjectionInfo info, IMediaProjectionWatcherCallback callback) {
            this.mInfo = info;
            this.mCallback = callback;
        }

        public void run() {
            try {
                Slog.d(MediaProjectionManagerService.TAG, "notify media projection has started");
                this.mCallback.onStart(this.mInfo);
            } catch (RemoteException e) {
                Slog.w(MediaProjectionManagerService.TAG, "Failed to notify media projection has stopped", e);
            }
        }
    }

    private static final class WatcherStopCallback implements Runnable {
        private IMediaProjectionWatcherCallback mCallback;
        private MediaProjectionInfo mInfo;

        public WatcherStopCallback(MediaProjectionInfo info, IMediaProjectionWatcherCallback callback) {
            this.mInfo = info;
            this.mCallback = callback;
        }

        public void run() {
            try {
                Slog.d(MediaProjectionManagerService.TAG, "notify media projection has stopped");
                this.mCallback.onStop(this.mInfo);
            } catch (RemoteException e) {
                Slog.w(MediaProjectionManagerService.TAG, "Failed to notify media projection has stopped", e);
            }
        }
    }

    public MediaProjectionManagerService(Context context) {
        super(context);
        this.mHwMPMSEx = HwServiceExFactory.getHwMediaProjectionManagerServiceEx(this, context);
        this.mContext = context;
        this.mDeathEaters = new ArrayMap();
        this.mCallbackDelegate = new CallbackDelegate();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mMediaRouter = (MediaRouter) this.mContext.getSystemService("media_router");
        this.mMediaRouterCallback = new MediaRouterCallback();
        Watchdog.getInstance().addMonitor(this);
    }

    /* JADX WARNING: type inference failed for: r1v0, types: [com.android.server.media.projection.MediaProjectionManagerService$BinderService, android.os.IBinder] */
    public void onStart() {
        publishBinderService("media_projection", new BinderService(), false);
        this.mMediaRouter.addCallback(4, this.mMediaRouterCallback, 8);
    }

    public void onSwitchUser(int userId) {
        this.mMediaRouter.rebindAsUser(userId);
        synchronized (this.mLock) {
            if (this.mProjectionGrant != null) {
                this.mProjectionGrant.stop();
            }
        }
    }

    public void monitor() {
        synchronized (this.mLock) {
        }
    }

    /* access modifiers changed from: private */
    public void startProjectionLocked(MediaProjection projection) {
        if (this.mProjectionGrant != null) {
            this.mProjectionGrant.stop();
        }
        if (this.mMediaRouteInfo != null) {
            this.mMediaRouter.getFallbackRoute().select();
        }
        this.mProjectionToken = projection.asBinder();
        this.mProjectionGrant = projection;
        dispatchStart(projection);
    }

    /* access modifiers changed from: private */
    public void stopProjectionLocked(MediaProjection projection) {
        this.mProjectionToken = null;
        this.mProjectionGrant = null;
        dispatchStop(projection);
    }

    /* access modifiers changed from: private */
    public void addCallback(final IMediaProjectionWatcherCallback callback) {
        IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
            public void binderDied() {
                MediaProjectionManagerService.this.removeCallback(callback);
            }
        };
        synchronized (this.mLock) {
            this.mCallbackDelegate.add(callback);
            linkDeathRecipientLocked(callback, deathRecipient);
        }
    }

    /* access modifiers changed from: private */
    public void removeCallback(IMediaProjectionWatcherCallback callback) {
        synchronized (this.mLock) {
            unlinkDeathRecipientLocked(callback);
            this.mCallbackDelegate.remove(callback);
        }
    }

    private void linkDeathRecipientLocked(IMediaProjectionWatcherCallback callback, IBinder.DeathRecipient deathRecipient) {
        try {
            IBinder token = callback.asBinder();
            token.linkToDeath(deathRecipient, 0);
            this.mDeathEaters.put(token, deathRecipient);
        } catch (RemoteException e) {
            Slog.e(TAG, "Unable to link to death for media projection monitoring callback", e);
        }
    }

    private void unlinkDeathRecipientLocked(IMediaProjectionWatcherCallback callback) {
        IBinder token = callback.asBinder();
        IBinder.DeathRecipient deathRecipient = this.mDeathEaters.remove(token);
        if (deathRecipient != null) {
            token.unlinkToDeath(deathRecipient, 0);
        }
    }

    private void dispatchStart(MediaProjection projection) {
        this.mCallbackDelegate.dispatchStart(projection);
    }

    private void dispatchStop(MediaProjection projection) {
        this.mCallbackDelegate.dispatchStop(projection);
    }

    /* access modifiers changed from: private */
    public boolean isValidMediaProjection(IBinder token) {
        synchronized (this.mLock) {
            if (this.mProjectionToken == null) {
                return false;
            }
            boolean equals = this.mProjectionToken.equals(token);
            return equals;
        }
    }

    /* access modifiers changed from: private */
    public MediaProjectionInfo getActiveProjectionInfo() {
        synchronized (this.mLock) {
            if (this.mProjectionGrant == null) {
                return null;
            }
            MediaProjectionInfo projectionInfo = this.mProjectionGrant.getProjectionInfo();
            return projectionInfo;
        }
    }

    /* access modifiers changed from: private */
    public void dump(PrintWriter pw) {
        pw.println("MEDIA PROJECTION MANAGER (dumpsys media_projection)");
        synchronized (this.mLock) {
            pw.println("Media Projection: ");
            if (this.mProjectionGrant != null) {
                this.mProjectionGrant.dump(pw);
            } else {
                pw.println("null");
            }
        }
    }

    /* access modifiers changed from: private */
    public static String typeToString(int type) {
        switch (type) {
            case 0:
                return "TYPE_SCREEN_CAPTURE";
            case 1:
                return "TYPE_MIRRORING";
            case 2:
                return "TYPE_PRESENTATION";
            default:
                return Integer.toString(type);
        }
    }
}
