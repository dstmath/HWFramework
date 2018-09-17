package com.android.server.media.projection;

import android.app.AppOpsManager;
import android.content.Context;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.media.MediaRouter.SimpleCallback;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionCallback;
import android.media.projection.IMediaProjectionManager.Stub;
import android.media.projection.IMediaProjectionWatcherCallback;
import android.media.projection.MediaProjectionInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Slog;
import com.android.internal.util.DumpUtils;
import com.android.server.HwServiceFactory;
import com.android.server.SystemService;
import com.android.server.Watchdog;
import com.android.server.Watchdog.Monitor;
import com.android.server.security.securityprofile.ISecurityProfileController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Map;

public final class MediaProjectionManagerService extends SystemService implements Monitor {
    private static final String TAG = "MediaProjectionManagerService";
    private final AppOpsManager mAppOps;
    private final CallbackDelegate mCallbackDelegate;
    private final Context mContext;
    private final Map<IBinder, DeathRecipient> mDeathEaters;
    private final Object mLock = new Object();
    private RouteInfo mMediaRouteInfo;
    private final MediaRouter mMediaRouter;
    private final MediaRouterCallback mMediaRouterCallback;
    private MediaProjection mProjectionGrant;
    private IBinder mProjectionToken;

    private final class BinderService extends Stub {
        /* synthetic */ BinderService(MediaProjectionManagerService this$0, BinderService -this1) {
            this();
        }

        private BinderService() {
        }

        public boolean hasProjectionPermission(int uid, String packageName) {
            long token = Binder.clearCallingIdentity();
            try {
                boolean hasPermission;
                if (checkPermission(packageName, "android.permission.CAPTURE_VIDEO_OUTPUT")) {
                    hasPermission = true;
                } else if (MediaProjectionManagerService.this.mAppOps.noteOpNoThrow(46, uid, packageName) == 0) {
                    hasPermission = true;
                } else {
                    hasPermission = false;
                }
                Binder.restoreCallingIdentity(token);
                return hasPermission;
            } catch (Throwable th) {
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
                    Binder.restoreCallingIdentity(callingToken);
                    return projection;
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(callingToken);
                }
            }
        }

        public boolean isValidMediaProjection(IMediaProjection projection) {
            return MediaProjectionManagerService.this.isValidMediaProjection(projection.asBinder());
        }

        public MediaProjectionInfo getActiveProjectionInfo() {
            if (MediaProjectionManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.MANAGE_MEDIA_PROJECTION") != 0) {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to add projection callbacks");
            }
            long token = Binder.clearCallingIdentity();
            try {
                MediaProjectionInfo -wrap0 = MediaProjectionManagerService.this.getActiveProjectionInfo();
                return -wrap0;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void stopActiveProjection() {
            if (MediaProjectionManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.MANAGE_MEDIA_PROJECTION") != 0) {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to add projection callbacks");
            }
            long token = Binder.clearCallingIdentity();
            try {
                if (MediaProjectionManagerService.this.mProjectionGrant != null) {
                    MediaProjectionManagerService.this.mProjectionGrant.stop();
                }
                Binder.restoreCallingIdentity(token);
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void addCallback(IMediaProjectionWatcherCallback callback) {
            if (MediaProjectionManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.MANAGE_MEDIA_PROJECTION") != 0) {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to add projection callbacks");
            }
            long token = Binder.clearCallingIdentity();
            try {
                MediaProjectionManagerService.this.addCallback(callback);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void removeCallback(IMediaProjectionWatcherCallback callback) {
            if (MediaProjectionManagerService.this.mContext.checkCallingPermission("android.permission.MANAGE_MEDIA_PROJECTION") != 0) {
                throw new SecurityException("Requires MANAGE_MEDIA_PROJECTION in order to remove projection callbacks");
            }
            long token = Binder.clearCallingIdentity();
            try {
                MediaProjectionManagerService.this.removeCallback(callback);
            } finally {
                Binder.restoreCallingIdentity(token);
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
        private DeathRecipient mDeathEater;
        private IBinder mToken;
        private int mType;
        public final String packageName;
        public final int uid;
        public final UserHandle userHandle;

        public MediaProjection(int type, int uid, String packageName) {
            this.mType = type;
            this.uid = uid;
            this.packageName = packageName;
            this.userHandle = new UserHandle(UserHandle.getUserId(uid));
        }

        public boolean canProjectVideo() {
            return this.mType == 1 || this.mType == 0;
        }

        public boolean canProjectSecureVideo() {
            return false;
        }

        public boolean canProjectAudio() {
            if (this.mType == 1 || this.mType == 2) {
                return true;
            }
            return false;
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
            if (callback == null) {
                throw new IllegalArgumentException("callback must not be null");
            }
            synchronized (MediaProjectionManagerService.this.mLock) {
                if (MediaProjectionManagerService.this.isValidMediaProjection(asBinder())) {
                    throw new IllegalStateException("Cannot start already started MediaProjection");
                }
                ISecurityProfileController spc = HwServiceFactory.getSecurityProfileController();
                if (spc == null || !spc.shouldPreventMediaProjection(Binder.getCallingUid())) {
                    this.mCallback = callback;
                    registerCallback(this.mCallback);
                    try {
                        this.mToken = callback.asBinder();
                        this.mDeathEater = new DeathRecipient() {
                            public void binderDied() {
                                MediaProjectionManagerService.this.mCallbackDelegate.remove(callback);
                                MediaProjection.this.stop();
                            }
                        };
                        this.mToken.linkToDeath(this.mDeathEater, 0);
                        MediaProjectionManagerService.this.startProjectionLocked(this);
                        return;
                    } catch (RemoteException e) {
                        Slog.w(MediaProjectionManagerService.TAG, "MediaProjectionCallbacks must be valid, aborting MediaProjection", e);
                        return;
                    }
                }
                Slog.d(MediaProjectionManagerService.TAG, "SecurityProfile denied the start request of MediaProjection");
            }
        }

        public void stop() {
            synchronized (MediaProjectionManagerService.this.mLock) {
                if (MediaProjectionManagerService.this.isValidMediaProjection(asBinder())) {
                    MediaProjectionManagerService.this.stopProjectionLocked(this);
                    this.mToken.unlinkToDeath(this.mDeathEater, 0);
                    this.mToken = null;
                    unregisterCallback(this.mCallback);
                    this.mCallback = null;
                    return;
                }
                Slog.w(MediaProjectionManagerService.TAG, "Attempted to stop inactive MediaProjection (uid=" + Binder.getCallingUid() + ", " + "pid=" + Binder.getCallingPid() + ")");
            }
        }

        public void registerCallback(IMediaProjectionCallback callback) {
            if (callback == null) {
                throw new IllegalArgumentException("callback must not be null");
            }
            MediaProjectionManagerService.this.mCallbackDelegate.add(callback);
        }

        public void unregisterCallback(IMediaProjectionCallback callback) {
            if (callback == null) {
                throw new IllegalArgumentException("callback must not be null");
            }
            MediaProjectionManagerService.this.mCallbackDelegate.remove(callback);
        }

        public MediaProjectionInfo getProjectionInfo() {
            return new MediaProjectionInfo(this.packageName, this.userHandle);
        }

        public void dump(PrintWriter pw) {
            pw.println("(" + this.packageName + ", uid=" + this.uid + "): " + MediaProjectionManagerService.typeToString(this.mType));
        }
    }

    private class MediaRouterCallback extends SimpleCallback {
        /* synthetic */ MediaRouterCallback(MediaProjectionManagerService this$0, MediaRouterCallback -this1) {
            this();
        }

        private MediaRouterCallback() {
        }

        public void onRouteSelected(MediaRouter router, int type, RouteInfo info) {
            synchronized (MediaProjectionManagerService.this.mLock) {
                if ((type & 4) != 0) {
                    MediaProjectionManagerService.this.mMediaRouteInfo = info;
                    if (MediaProjectionManagerService.this.mProjectionGrant != null) {
                        MediaProjectionManagerService.this.mProjectionGrant.stop();
                    }
                }
            }
        }

        public void onRouteUnselected(MediaRouter route, int type, RouteInfo info) {
            if (MediaProjectionManagerService.this.mMediaRouteInfo == info) {
                MediaProjectionManagerService.this.mMediaRouteInfo = null;
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
                this.mCallback.onStop(this.mInfo);
            } catch (RemoteException e) {
                Slog.w(MediaProjectionManagerService.TAG, "Failed to notify media projection has stopped", e);
            }
        }
    }

    public MediaProjectionManagerService(Context context) {
        super(context);
        this.mContext = context;
        this.mDeathEaters = new ArrayMap();
        this.mCallbackDelegate = new CallbackDelegate();
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService("appops");
        this.mMediaRouter = (MediaRouter) this.mContext.getSystemService("media_router");
        this.mMediaRouterCallback = new MediaRouterCallback(this, null);
        Watchdog.getInstance().addMonitor(this);
    }

    public void onStart() {
        publishBinderService("media_projection", new BinderService(this, null), false);
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

    private void startProjectionLocked(MediaProjection projection) {
        if (this.mProjectionGrant != null) {
            this.mProjectionGrant.stop();
        }
        if (this.mMediaRouteInfo != null) {
            this.mMediaRouter.getDefaultRoute().select();
        }
        this.mProjectionToken = projection.asBinder();
        this.mProjectionGrant = projection;
        dispatchStart(projection);
    }

    private void stopProjectionLocked(MediaProjection projection) {
        this.mProjectionToken = null;
        this.mProjectionGrant = null;
        dispatchStop(projection);
    }

    private void addCallback(final IMediaProjectionWatcherCallback callback) {
        DeathRecipient deathRecipient = new DeathRecipient() {
            public void binderDied() {
                MediaProjectionManagerService.this.removeCallback(callback);
            }
        };
        synchronized (this.mLock) {
            this.mCallbackDelegate.add(callback);
            linkDeathRecipientLocked(callback, deathRecipient);
        }
    }

    private void removeCallback(IMediaProjectionWatcherCallback callback) {
        synchronized (this.mLock) {
            unlinkDeathRecipientLocked(callback);
            this.mCallbackDelegate.remove(callback);
        }
    }

    private void linkDeathRecipientLocked(IMediaProjectionWatcherCallback callback, DeathRecipient deathRecipient) {
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
        DeathRecipient deathRecipient = (DeathRecipient) this.mDeathEaters.remove(token);
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

    private boolean isValidMediaProjection(IBinder token) {
        synchronized (this.mLock) {
            if (this.mProjectionToken != null) {
                boolean equals = this.mProjectionToken.equals(token);
                return equals;
            }
            return false;
        }
    }

    private MediaProjectionInfo getActiveProjectionInfo() {
        synchronized (this.mLock) {
            if (this.mProjectionGrant == null) {
                return null;
            }
            MediaProjectionInfo projectionInfo = this.mProjectionGrant.getProjectionInfo();
            return projectionInfo;
        }
    }

    private void dump(PrintWriter pw) {
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

    private static String typeToString(int type) {
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
