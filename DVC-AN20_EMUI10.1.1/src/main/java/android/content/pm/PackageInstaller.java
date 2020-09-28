package android.content.pm;

import android.annotation.SystemApi;
import android.annotation.UnsupportedAppUsage;
import android.app.AppGlobals;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.IPackageInstallerCallback;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.FileBridge;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.os.ParcelableException;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.system.ErrnoException;
import android.system.Os;
import android.util.ArraySet;
import android.util.ExceptionUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.function.pooled.PooledLambda;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

public class PackageInstaller {
    public static final String ACTION_CONFIRM_INSTALL = "android.content.pm.action.CONFIRM_INSTALL";
    public static final String ACTION_SESSION_COMMITTED = "android.content.pm.action.SESSION_COMMITTED";
    public static final String ACTION_SESSION_DETAILS = "android.content.pm.action.SESSION_DETAILS";
    public static final String ACTION_SESSION_UPDATED = "android.content.pm.action.SESSION_UPDATED";
    public static final boolean ENABLE_REVOCABLE_FD = SystemProperties.getBoolean("fw.revocable_fd", false);
    public static final String EXTRA_CALLBACK = "android.content.pm.extra.CALLBACK";
    public static final String EXTRA_LEGACY_BUNDLE = "android.content.pm.extra.LEGACY_BUNDLE";
    public static final String EXTRA_LEGACY_STATUS = "android.content.pm.extra.LEGACY_STATUS";
    public static final String EXTRA_OTHER_PACKAGE_NAME = "android.content.pm.extra.OTHER_PACKAGE_NAME";
    public static final String EXTRA_PACKAGE_NAME = "android.content.pm.extra.PACKAGE_NAME";
    @Deprecated
    public static final String EXTRA_PACKAGE_NAMES = "android.content.pm.extra.PACKAGE_NAMES";
    public static final String EXTRA_SESSION = "android.content.pm.extra.SESSION";
    public static final String EXTRA_SESSION_ID = "android.content.pm.extra.SESSION_ID";
    public static final String EXTRA_STATUS = "android.content.pm.extra.STATUS";
    public static final String EXTRA_STATUS_MESSAGE = "android.content.pm.extra.STATUS_MESSAGE";
    public static final String EXTRA_STORAGE_PATH = "android.content.pm.extra.STORAGE_PATH";
    public static final int STATUS_FAILURE = 1;
    public static final int STATUS_FAILURE_ABORTED = 3;
    public static final int STATUS_FAILURE_BLOCKED = 2;
    public static final int STATUS_FAILURE_CONFLICT = 5;
    public static final int STATUS_FAILURE_INCOMPATIBLE = 7;
    public static final int STATUS_FAILURE_INVALID = 4;
    public static final int STATUS_FAILURE_STORAGE = 6;
    public static final int STATUS_PENDING_USER_ACTION = -1;
    public static final int STATUS_SUCCESS = 0;
    private static final String TAG = "PackageInstaller";
    private final ArrayList<SessionCallbackDelegate> mDelegates = new ArrayList<>();
    private final IPackageInstaller mInstaller;
    private final String mInstallerPackageName;
    private final int mUserId;

    public static abstract class SessionCallback {
        public abstract void onActiveChanged(int i, boolean z);

        public abstract void onBadgingChanged(int i);

        public abstract void onCreated(int i);

        public abstract void onFinished(int i, boolean z);

        public abstract void onProgressChanged(int i, float f);
    }

    public PackageInstaller(IPackageInstaller installer, String installerPackageName, int userId) {
        this.mInstaller = installer;
        this.mInstallerPackageName = installerPackageName;
        this.mUserId = userId;
    }

    public int createSession(SessionParams params) throws IOException {
        String installerPackage;
        try {
            if (params.installerPackageName == null) {
                installerPackage = this.mInstallerPackageName;
            } else {
                installerPackage = params.installerPackageName;
            }
            return this.mInstaller.createSession(params, installerPackage, this.mUserId);
        } catch (RuntimeException e) {
            ExceptionUtils.maybeUnwrapIOException(e);
            throw e;
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
        }
    }

    public Session openSession(int sessionId) throws IOException {
        try {
            return new Session(this.mInstaller.openSession(sessionId));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (RuntimeException e2) {
            ExceptionUtils.maybeUnwrapIOException(e2);
            throw e2;
        }
    }

    public void updateSessionAppIcon(int sessionId, Bitmap appIcon) {
        try {
            this.mInstaller.updateSessionAppIcon(sessionId, appIcon);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void updateSessionAppLabel(int sessionId, CharSequence appLabel) {
        String val;
        if (appLabel != null) {
            try {
                val = appLabel.toString();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        } else {
            val = null;
        }
        this.mInstaller.updateSessionAppLabel(sessionId, val);
    }

    public void abandonSession(int sessionId) {
        try {
            this.mInstaller.abandonSession(sessionId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public SessionInfo getSessionInfo(int sessionId) {
        try {
            return this.mInstaller.getSessionInfo(sessionId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<SessionInfo> getAllSessions() {
        try {
            return this.mInstaller.getAllSessions(this.mUserId).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<SessionInfo> getMySessions() {
        try {
            return this.mInstaller.getMySessions(this.mInstallerPackageName, this.mUserId).getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public List<SessionInfo> getStagedSessions() {
        try {
            return this.mInstaller.getStagedSessions().getList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public SessionInfo getActiveStagedSession() {
        for (SessionInfo s : getStagedSessions()) {
            if (!s.isStagedSessionApplied() && !s.isStagedSessionFailed() && s.getParentSessionId() == -1 && s.isCommitted()) {
                return s;
            }
        }
        return null;
    }

    public void uninstall(String packageName, IntentSender statusReceiver) {
        uninstall(packageName, 0, statusReceiver);
    }

    public void uninstall(String packageName, int flags, IntentSender statusReceiver) {
        uninstall(new VersionedPackage(packageName, -1), flags, statusReceiver);
    }

    public void uninstall(VersionedPackage versionedPackage, IntentSender statusReceiver) {
        uninstall(versionedPackage, 0, statusReceiver);
    }

    public void uninstall(VersionedPackage versionedPackage, int flags, IntentSender statusReceiver) {
        Preconditions.checkNotNull(versionedPackage, "versionedPackage cannot be null");
        try {
            this.mInstaller.uninstall(versionedPackage, this.mInstallerPackageName, flags, statusReceiver, this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void installExistingPackage(String packageName, int installReason, IntentSender statusReceiver) {
        Preconditions.checkNotNull(packageName, "packageName cannot be null");
        try {
            this.mInstaller.installExistingPackage(packageName, 4194304, installReason, statusReceiver, this.mUserId, null);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @SystemApi
    public void setPermissionsResult(int sessionId, boolean accepted) {
        try {
            this.mInstaller.setPermissionsResult(sessionId, accepted);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /* access modifiers changed from: package-private */
    public static class SessionCallbackDelegate extends IPackageInstallerCallback.Stub {
        private static final int MSG_SESSION_ACTIVE_CHANGED = 3;
        private static final int MSG_SESSION_BADGING_CHANGED = 2;
        private static final int MSG_SESSION_CREATED = 1;
        private static final int MSG_SESSION_FINISHED = 5;
        private static final int MSG_SESSION_PROGRESS_CHANGED = 4;
        final SessionCallback mCallback;
        final Executor mExecutor;

        SessionCallbackDelegate(SessionCallback callback, Executor executor) {
            this.mCallback = callback;
            this.mExecutor = executor;
        }

        @Override // android.content.pm.IPackageInstallerCallback
        public void onSessionCreated(int sessionId) {
            this.mExecutor.execute(PooledLambda.obtainRunnable($$Lambda$ciir_QAmv6RwJro4I58t77dPnxU.INSTANCE, this.mCallback, Integer.valueOf(sessionId)).recycleOnUse());
        }

        @Override // android.content.pm.IPackageInstallerCallback
        public void onSessionBadgingChanged(int sessionId) {
            this.mExecutor.execute(PooledLambda.obtainRunnable($$Lambda$B12dZLpdwpXn89QSesmkaZjD72Q.INSTANCE, this.mCallback, Integer.valueOf(sessionId)).recycleOnUse());
        }

        @Override // android.content.pm.IPackageInstallerCallback
        public void onSessionActiveChanged(int sessionId, boolean active) {
            this.mExecutor.execute(PooledLambda.obtainRunnable($$Lambda$T1UQAuePWRRmVQ1KzTyMAktZUPM.INSTANCE, this.mCallback, Integer.valueOf(sessionId), Boolean.valueOf(active)).recycleOnUse());
        }

        @Override // android.content.pm.IPackageInstallerCallback
        public void onSessionProgressChanged(int sessionId, float progress) {
            this.mExecutor.execute(PooledLambda.obtainRunnable($$Lambda$n3uXeb1vYRmq_BWTfosEqUUr9g.INSTANCE, this.mCallback, Integer.valueOf(sessionId), Float.valueOf(progress)).recycleOnUse());
        }

        @Override // android.content.pm.IPackageInstallerCallback
        public void onSessionFinished(int sessionId, boolean success) {
            this.mExecutor.execute(PooledLambda.obtainRunnable($$Lambda$zO9HBUVgPeroyDQPLJEMNMvSqc.INSTANCE, this.mCallback, Integer.valueOf(sessionId), Boolean.valueOf(success)).recycleOnUse());
        }
    }

    @Deprecated
    public void addSessionCallback(SessionCallback callback) {
        registerSessionCallback(callback);
    }

    public void registerSessionCallback(SessionCallback callback) {
        registerSessionCallback(callback, new Handler());
    }

    @Deprecated
    public void addSessionCallback(SessionCallback callback, Handler handler) {
        registerSessionCallback(callback, handler);
    }

    public void registerSessionCallback(SessionCallback callback, Handler handler) {
        synchronized (this.mDelegates) {
            SessionCallbackDelegate delegate = new SessionCallbackDelegate(callback, new HandlerExecutor(handler));
            try {
                this.mInstaller.registerCallback(delegate, this.mUserId);
                this.mDelegates.add(delegate);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    @Deprecated
    public void removeSessionCallback(SessionCallback callback) {
        unregisterSessionCallback(callback);
    }

    public void unregisterSessionCallback(SessionCallback callback) {
        synchronized (this.mDelegates) {
            Iterator<SessionCallbackDelegate> i = this.mDelegates.iterator();
            while (i.hasNext()) {
                SessionCallbackDelegate delegate = i.next();
                if (delegate.mCallback == callback) {
                    try {
                        this.mInstaller.unregisterCallback(delegate);
                        i.remove();
                    } catch (RemoteException e) {
                        throw e.rethrowFromSystemServer();
                    }
                }
            }
        }
    }

    public static class Session implements Closeable {
        protected final IPackageInstallerSession mSession;

        public Session(IPackageInstallerSession session) {
            this.mSession = session;
        }

        @Deprecated
        public void setProgress(float progress) {
            setStagingProgress(progress);
        }

        public void setStagingProgress(float progress) {
            try {
                this.mSession.setClientProgress(progress);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        @UnsupportedAppUsage
        public void addProgress(float progress) {
            try {
                this.mSession.addClientProgress(progress);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public OutputStream openWrite(String name, long offsetBytes, long lengthBytes) throws IOException {
            try {
                if (PackageInstaller.ENABLE_REVOCABLE_FD) {
                    return new ParcelFileDescriptor.AutoCloseOutputStream(this.mSession.openWrite(name, offsetBytes, lengthBytes));
                }
                return new FileBridge.FileBridgeOutputStream(this.mSession.openWrite(name, offsetBytes, lengthBytes));
            } catch (RuntimeException e) {
                ExceptionUtils.maybeUnwrapIOException(e);
                throw e;
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }

        public void write(String name, long offsetBytes, long lengthBytes, ParcelFileDescriptor fd) throws IOException {
            try {
                this.mSession.write(name, offsetBytes, lengthBytes, fd);
            } catch (RuntimeException e) {
                ExceptionUtils.maybeUnwrapIOException(e);
                throw e;
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }

        public void fsync(OutputStream out) throws IOException {
            if (PackageInstaller.ENABLE_REVOCABLE_FD) {
                if (out instanceof ParcelFileDescriptor.AutoCloseOutputStream) {
                    try {
                        Os.fsync(((ParcelFileDescriptor.AutoCloseOutputStream) out).getFD());
                    } catch (ErrnoException e) {
                        throw e.rethrowAsIOException();
                    }
                } else {
                    throw new IllegalArgumentException("Unrecognized stream");
                }
            } else if (out instanceof FileBridge.FileBridgeOutputStream) {
                ((FileBridge.FileBridgeOutputStream) out).fsync();
            } else {
                throw new IllegalArgumentException("Unrecognized stream");
            }
        }

        public String[] getNames() throws IOException {
            try {
                return this.mSession.getNames();
            } catch (RuntimeException e) {
                ExceptionUtils.maybeUnwrapIOException(e);
                throw e;
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }

        public InputStream openRead(String name) throws IOException {
            try {
                return new ParcelFileDescriptor.AutoCloseInputStream(this.mSession.openRead(name));
            } catch (RuntimeException e) {
                ExceptionUtils.maybeUnwrapIOException(e);
                throw e;
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }

        public void removeSplit(String splitName) throws IOException {
            try {
                this.mSession.removeSplit(splitName);
            } catch (RuntimeException e) {
                ExceptionUtils.maybeUnwrapIOException(e);
                throw e;
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }

        public void commit(IntentSender statusReceiver) {
            try {
                this.mSession.commit(statusReceiver, false);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        @SystemApi
        public void commitTransferred(IntentSender statusReceiver) {
            try {
                this.mSession.commit(statusReceiver, true);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void transfer(String packageName) throws PackageManager.NameNotFoundException {
            Preconditions.checkNotNull(packageName);
            try {
                this.mSession.transfer(packageName);
            } catch (ParcelableException e) {
                e.maybeRethrow(PackageManager.NameNotFoundException.class);
                throw new RuntimeException(e);
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable
        public void close() {
            try {
                this.mSession.close();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void abandon() {
            try {
                this.mSession.abandon();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public boolean isMultiPackage() {
            try {
                return this.mSession.isMultiPackage();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public boolean isStaged() {
            try {
                return this.mSession.isStaged();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public int getParentSessionId() {
            try {
                return this.mSession.getParentSessionId();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public int[] getChildSessionIds() {
            try {
                return this.mSession.getChildSessionIds();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

        public void addChildSessionId(int sessionId) {
            try {
                this.mSession.addChildSessionId(sessionId);
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
        }

        public void removeChildSessionId(int sessionId) {
            try {
                this.mSession.removeChildSessionId(sessionId);
            } catch (RemoteException e) {
                e.rethrowFromSystemServer();
            }
        }
    }

    public static class SessionParams implements Parcelable {
        public static final Parcelable.Creator<SessionParams> CREATOR = new Parcelable.Creator<SessionParams>() {
            /* class android.content.pm.PackageInstaller.SessionParams.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SessionParams createFromParcel(Parcel p) {
                return new SessionParams(p);
            }

            @Override // android.os.Parcelable.Creator
            public SessionParams[] newArray(int size) {
                return new SessionParams[size];
            }
        };
        public static final int MODE_FULL_INSTALL = 1;
        public static final int MODE_INHERIT_EXISTING = 2;
        public static final int MODE_INVALID = -1;
        public static final Set<String> RESTRICTED_PERMISSIONS_ALL = new ArraySet();
        public static final int UID_UNKNOWN = -1;
        public String abiOverride;
        public List<String> addSplitList;
        @UnsupportedAppUsage
        public Bitmap appIcon;
        public long appIconLastModified = -1;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public String appLabel;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public String appPackageName;
        public String[] grantedRuntimePermissions;
        public int hdbArgIndex = 0;
        public String[] hdbArgs;
        public String hdbEncode = null;
        public int hwInstallFlags;
        @UnsupportedAppUsage
        public int installFlags = 4194304;
        public int installLocation = 1;
        public int installReason = 0;
        public String installerPackageName;
        public boolean isHep;
        public boolean isMultiPackage;
        public boolean isStaged;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public int mode = -1;
        @UnsupportedAppUsage
        public int originatingUid = -1;
        public Uri originatingUri;
        public Uri referrerUri;
        public List<String> removeSplitList;
        public long requiredInstalledVersionCode = -1;
        @UnsupportedAppUsage
        public long sizeBytes = -1;
        public String volumeUuid;
        public List<String> whitelistedRestrictedPermissions;

        public SessionParams(int mode2) {
            this.mode = mode2;
        }

        public SessionParams(Parcel source) {
            this.mode = source.readInt();
            this.installFlags = source.readInt();
            this.installLocation = source.readInt();
            this.installReason = source.readInt();
            this.sizeBytes = source.readLong();
            this.appPackageName = source.readString();
            this.appIcon = (Bitmap) source.readParcelable(null);
            this.appLabel = source.readString();
            this.originatingUri = (Uri) source.readParcelable(null);
            this.originatingUid = source.readInt();
            this.referrerUri = (Uri) source.readParcelable(null);
            this.abiOverride = source.readString();
            this.volumeUuid = source.readString();
            this.grantedRuntimePermissions = source.readStringArray();
            this.whitelistedRestrictedPermissions = source.createStringArrayList();
            this.installerPackageName = source.readString();
            this.isMultiPackage = source.readBoolean();
            this.isStaged = source.readBoolean();
            this.isHep = source.readBoolean();
            this.requiredInstalledVersionCode = source.readLong();
            this.hdbEncode = source.readString();
            this.hdbArgIndex = source.readInt();
            this.hdbArgs = source.readStringArray();
            this.hwInstallFlags = source.readInt();
        }

        public SessionParams copy() {
            SessionParams ret = new SessionParams(this.mode);
            ret.installFlags = this.installFlags;
            ret.installLocation = this.installLocation;
            ret.installReason = this.installReason;
            ret.sizeBytes = this.sizeBytes;
            ret.appPackageName = this.appPackageName;
            ret.appIcon = this.appIcon;
            ret.appLabel = this.appLabel;
            ret.originatingUri = this.originatingUri;
            ret.originatingUid = this.originatingUid;
            ret.referrerUri = this.referrerUri;
            ret.abiOverride = this.abiOverride;
            ret.volumeUuid = this.volumeUuid;
            ret.grantedRuntimePermissions = this.grantedRuntimePermissions;
            ret.whitelistedRestrictedPermissions = this.whitelistedRestrictedPermissions;
            ret.installerPackageName = this.installerPackageName;
            ret.isMultiPackage = this.isMultiPackage;
            ret.isStaged = this.isStaged;
            ret.isHep = this.isHep;
            ret.requiredInstalledVersionCode = this.requiredInstalledVersionCode;
            ret.hwInstallFlags = this.hwInstallFlags;
            return ret;
        }

        public boolean areHiddenOptionsSet() {
            int i = this.installFlags;
            return ((1169536 & i) == i && this.abiOverride == null && this.volumeUuid == null) ? false : true;
        }

        public void setInstallLocation(int installLocation2) {
            this.installLocation = installLocation2;
        }

        public void setSize(long sizeBytes2) {
            this.sizeBytes = sizeBytes2;
        }

        public void setAppPackageName(String appPackageName2) {
            this.appPackageName = appPackageName2;
        }

        public void setAppIcon(Bitmap appIcon2) {
            this.appIcon = appIcon2;
        }

        public void setAppLabel(CharSequence appLabel2) {
            this.appLabel = appLabel2 != null ? appLabel2.toString() : null;
        }

        public void setOriginatingUri(Uri originatingUri2) {
            this.originatingUri = originatingUri2;
        }

        public void setOriginatingUid(int originatingUid2) {
            this.originatingUid = originatingUid2;
        }

        public void setReferrerUri(Uri referrerUri2) {
            this.referrerUri = referrerUri2;
        }

        @SystemApi
        public void setGrantedRuntimePermissions(String[] permissions) {
            this.installFlags |= 256;
            this.grantedRuntimePermissions = permissions;
        }

        public void setWhitelistedRestrictedPermissions(Set<String> permissions) {
            ArrayList arrayList = null;
            if (permissions == RESTRICTED_PERMISSIONS_ALL) {
                this.installFlags |= 4194304;
                this.whitelistedRestrictedPermissions = null;
                return;
            }
            this.installFlags &= -4194305;
            if (permissions != null) {
                arrayList = new ArrayList(permissions);
            }
            this.whitelistedRestrictedPermissions = arrayList;
        }

        @SystemApi
        public void setEnableRollback(boolean enable) {
            if (enable) {
                this.installFlags |= 262144;
            } else {
                this.installFlags &= -262145;
            }
        }

        @SystemApi
        @Deprecated
        public void setAllowDowngrade(boolean allowDowngrade) {
            setRequestDowngrade(allowDowngrade);
        }

        @SystemApi
        public void setRequestDowngrade(boolean requestDowngrade) {
            if (requestDowngrade) {
                this.installFlags |= 128;
            } else {
                this.installFlags &= -129;
            }
        }

        public void setRequiredInstalledVersionCode(long versionCode) {
            this.requiredInstalledVersionCode = versionCode;
        }

        public void setInstallFlagsForcePermissionPrompt() {
            this.installFlags |= 1024;
        }

        @SystemApi
        public void setDontKillApp(boolean dontKillApp) {
            if (dontKillApp) {
                this.installFlags |= 4096;
            } else {
                this.installFlags &= -4097;
            }
        }

        @SystemApi
        public void setInstallAsInstantApp(boolean isInstantApp) {
            if (isInstantApp) {
                this.installFlags |= 2048;
                this.installFlags &= -16385;
                return;
            }
            this.installFlags &= -2049;
            this.installFlags |= 16384;
        }

        @SystemApi
        public void setInstallAsVirtualPreload() {
            this.installFlags |= 65536;
        }

        public void setInstallReason(int installReason2) {
            this.installReason = installReason2;
        }

        @SystemApi
        public void setAllocateAggressive(boolean allocateAggressive) {
            if (allocateAggressive) {
                this.installFlags |= 32768;
            } else {
                this.installFlags &= -32769;
            }
        }

        public void setInstallerPackageName(String installerPackageName2) {
            this.installerPackageName = installerPackageName2;
        }

        public void setMultiPackage() {
            this.isMultiPackage = true;
        }

        @SystemApi
        public void setStaged() {
            this.isStaged = true;
        }

        public void setHep() {
            this.isHep = true;
        }

        @SystemApi
        public void setInstallAsApex() {
            this.installFlags |= 131072;
        }

        public boolean getEnableRollback() {
            return (this.installFlags & 262144) != 0;
        }

        public void dump(IndentingPrintWriter pw) {
            pw.printPair("mode", Integer.valueOf(this.mode));
            pw.printHexPair("installFlags", this.installFlags);
            pw.printPair("installLocation", Integer.valueOf(this.installLocation));
            pw.printPair("sizeBytes", Long.valueOf(this.sizeBytes));
            pw.printPair("appPackageName", this.appPackageName);
            pw.printPair("appIcon", Boolean.valueOf(this.appIcon != null));
            pw.printPair("appLabel", this.appLabel);
            pw.printPair("originatingUri", this.originatingUri);
            pw.printPair("originatingUid", Integer.valueOf(this.originatingUid));
            pw.printPair("referrerUri", this.referrerUri);
            pw.printPair("abiOverride", this.abiOverride);
            pw.printPair("volumeUuid", this.volumeUuid);
            pw.printPair("grantedRuntimePermissions", (Object[]) this.grantedRuntimePermissions);
            pw.printPair("whitelistedRestrictedPermissions", this.whitelistedRestrictedPermissions);
            pw.printPair("installerPackageName", this.installerPackageName);
            pw.printPair("isMultiPackage", Boolean.valueOf(this.isMultiPackage));
            pw.printPair("isStaged", Boolean.valueOf(this.isStaged));
            pw.printPair("isHep", Boolean.valueOf(this.isHep));
            pw.printPair("requiredInstalledVersionCode", Long.valueOf(this.requiredInstalledVersionCode));
            pw.printPair("hwInstallFlags", Integer.valueOf(this.hwInstallFlags));
            pw.println();
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mode);
            dest.writeInt(this.installFlags);
            dest.writeInt(this.installLocation);
            dest.writeInt(this.installReason);
            dest.writeLong(this.sizeBytes);
            dest.writeString(this.appPackageName);
            dest.writeParcelable(this.appIcon, flags);
            dest.writeString(this.appLabel);
            dest.writeParcelable(this.originatingUri, flags);
            dest.writeInt(this.originatingUid);
            dest.writeParcelable(this.referrerUri, flags);
            dest.writeString(this.abiOverride);
            dest.writeString(this.volumeUuid);
            dest.writeStringArray(this.grantedRuntimePermissions);
            dest.writeStringList(this.whitelistedRestrictedPermissions);
            dest.writeString(this.installerPackageName);
            dest.writeBoolean(this.isMultiPackage);
            dest.writeBoolean(this.isStaged);
            dest.writeBoolean(this.isHep);
            dest.writeLong(this.requiredInstalledVersionCode);
            dest.writeString(this.hdbEncode);
            dest.writeInt(this.hdbArgIndex);
            dest.writeStringArray(this.hdbArgs);
            dest.writeInt(this.hwInstallFlags);
        }
    }

    public static class SessionInfo implements Parcelable {
        public static final Parcelable.Creator<SessionInfo> CREATOR = new Parcelable.Creator<SessionInfo>() {
            /* class android.content.pm.PackageInstaller.SessionInfo.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public SessionInfo createFromParcel(Parcel p) {
                return new SessionInfo(p);
            }

            @Override // android.os.Parcelable.Creator
            public SessionInfo[] newArray(int size) {
                return new SessionInfo[size];
            }
        };
        public static final int INVALID_ID = -1;
        private static final int[] NO_SESSIONS = new int[0];
        public static final int STAGED_SESSION_ACTIVATION_FAILED = 2;
        public static final int STAGED_SESSION_NO_ERROR = 0;
        public static final int STAGED_SESSION_UNKNOWN = 3;
        public static final int STAGED_SESSION_VERIFICATION_FAILED = 1;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public boolean active;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public Bitmap appIcon;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public CharSequence appLabel;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public String appPackageName;
        public int[] childSessionIds = NO_SESSIONS;
        public String[] grantedRuntimePermissions;
        public int installFlags;
        public int installLocation;
        public int installReason;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public String installerPackageName;
        public boolean isCommitted;
        public boolean isMultiPackage;
        public boolean isStaged;
        public boolean isStagedSessionApplied;
        public boolean isStagedSessionFailed;
        public boolean isStagedSessionReady;
        private int mStagedSessionErrorCode;
        private String mStagedSessionErrorMessage;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public int mode;
        public int originatingUid;
        public Uri originatingUri;
        public int parentSessionId = -1;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public float progress;
        public Uri referrerUri;
        @UnsupportedAppUsage
        public String resolvedBaseCodePath;
        @UnsupportedAppUsage
        public boolean sealed;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public int sessionId;
        @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
        public long sizeBytes;
        public long updatedMillis;
        public int userId;
        public List<String> whitelistedRestrictedPermissions;

        @Retention(RetentionPolicy.SOURCE)
        public @interface StagedSessionErrorCode {
        }

        @UnsupportedAppUsage
        public SessionInfo() {
        }

        public SessionInfo(Parcel source) {
            this.sessionId = source.readInt();
            this.userId = source.readInt();
            this.installerPackageName = source.readString();
            this.resolvedBaseCodePath = source.readString();
            this.progress = source.readFloat();
            boolean z = true;
            this.sealed = source.readInt() != 0;
            this.active = source.readInt() == 0 ? false : z;
            this.mode = source.readInt();
            this.installReason = source.readInt();
            this.sizeBytes = source.readLong();
            this.appPackageName = source.readString();
            this.appIcon = (Bitmap) source.readParcelable(null);
            this.appLabel = source.readString();
            this.installLocation = source.readInt();
            this.originatingUri = (Uri) source.readParcelable(null);
            this.originatingUid = source.readInt();
            this.referrerUri = (Uri) source.readParcelable(null);
            this.grantedRuntimePermissions = source.readStringArray();
            this.whitelistedRestrictedPermissions = source.createStringArrayList();
            this.installFlags = source.readInt();
            this.isMultiPackage = source.readBoolean();
            this.isStaged = source.readBoolean();
            this.parentSessionId = source.readInt();
            this.childSessionIds = source.createIntArray();
            if (this.childSessionIds == null) {
                this.childSessionIds = NO_SESSIONS;
            }
            this.isStagedSessionApplied = source.readBoolean();
            this.isStagedSessionReady = source.readBoolean();
            this.isStagedSessionFailed = source.readBoolean();
            this.mStagedSessionErrorCode = source.readInt();
            this.mStagedSessionErrorMessage = source.readString();
            this.isCommitted = source.readBoolean();
        }

        public int getSessionId() {
            return this.sessionId;
        }

        public UserHandle getUser() {
            return new UserHandle(this.userId);
        }

        public String getInstallerPackageName() {
            return this.installerPackageName;
        }

        public float getProgress() {
            return this.progress;
        }

        public boolean isActive() {
            return this.active;
        }

        public boolean isSealed() {
            return this.sealed;
        }

        public int getInstallReason() {
            return this.installReason;
        }

        @Deprecated
        public boolean isOpen() {
            return isActive();
        }

        public String getAppPackageName() {
            return this.appPackageName;
        }

        public Bitmap getAppIcon() {
            if (this.appIcon == null) {
                try {
                    IPackageInstaller packageInstaller = AppGlobals.getPackageManager().getPackageInstaller();
                    if (packageInstaller != null) {
                        SessionInfo info = packageInstaller.getSessionInfo(this.sessionId);
                        this.appIcon = info != null ? info.appIcon : null;
                    }
                } catch (RemoteException e) {
                    throw e.rethrowFromSystemServer();
                }
            }
            return this.appIcon;
        }

        public CharSequence getAppLabel() {
            return this.appLabel;
        }

        public Intent createDetailsIntent() {
            Intent intent = new Intent(PackageInstaller.ACTION_SESSION_DETAILS);
            intent.putExtra(PackageInstaller.EXTRA_SESSION_ID, this.sessionId);
            intent.setPackage(this.installerPackageName);
            intent.setFlags(268435456);
            return intent;
        }

        public int getMode() {
            return this.mode;
        }

        public int getInstallLocation() {
            return this.installLocation;
        }

        public long getSize() {
            return this.sizeBytes;
        }

        public Uri getOriginatingUri() {
            return this.originatingUri;
        }

        public int getOriginatingUid() {
            return this.originatingUid;
        }

        public Uri getReferrerUri() {
            return this.referrerUri;
        }

        @SystemApi
        public String[] getGrantedRuntimePermissions() {
            return this.grantedRuntimePermissions;
        }

        @SystemApi
        public Set<String> getWhitelistedRestrictedPermissions() {
            if ((this.installFlags & 4194304) != 0) {
                return SessionParams.RESTRICTED_PERMISSIONS_ALL;
            }
            List<String> list = this.whitelistedRestrictedPermissions;
            if (list != null) {
                return new ArraySet(list);
            }
            return Collections.emptySet();
        }

        @SystemApi
        @Deprecated
        public boolean getAllowDowngrade() {
            return getRequestDowngrade();
        }

        @SystemApi
        public boolean getRequestDowngrade() {
            return (this.installFlags & 128) != 0;
        }

        @SystemApi
        public boolean getDontKillApp() {
            return (this.installFlags & 4096) != 0;
        }

        @SystemApi
        public boolean getInstallAsInstantApp(boolean isInstantApp) {
            return (this.installFlags & 2048) != 0;
        }

        @SystemApi
        public boolean getInstallAsFullApp(boolean isInstantApp) {
            return (this.installFlags & 16384) != 0;
        }

        @SystemApi
        public boolean getInstallAsVirtualPreload() {
            return (this.installFlags & 65536) != 0;
        }

        @SystemApi
        public boolean getEnableRollback() {
            return (this.installFlags & 262144) != 0;
        }

        @SystemApi
        public boolean getAllocateAggressive() {
            return (this.installFlags & 32768) != 0;
        }

        @Deprecated
        public Intent getDetailsIntent() {
            return createDetailsIntent();
        }

        public boolean isMultiPackage() {
            return this.isMultiPackage;
        }

        public boolean isStaged() {
            return this.isStaged;
        }

        public int getParentSessionId() {
            return this.parentSessionId;
        }

        public int[] getChildSessionIds() {
            return this.childSessionIds;
        }

        private void checkSessionIsStaged() {
            if (!this.isStaged) {
                throw new IllegalStateException("Session is not marked as staged.");
            }
        }

        public boolean isStagedSessionApplied() {
            checkSessionIsStaged();
            return this.isStagedSessionApplied;
        }

        public boolean isStagedSessionReady() {
            checkSessionIsStaged();
            return this.isStagedSessionReady;
        }

        public boolean isStagedSessionFailed() {
            checkSessionIsStaged();
            return this.isStagedSessionFailed;
        }

        public int getStagedSessionErrorCode() {
            checkSessionIsStaged();
            return this.mStagedSessionErrorCode;
        }

        public String getStagedSessionErrorMessage() {
            checkSessionIsStaged();
            return this.mStagedSessionErrorMessage;
        }

        public void setStagedSessionErrorCode(int errorCode, String errorMessage) {
            this.mStagedSessionErrorCode = errorCode;
            this.mStagedSessionErrorMessage = errorMessage;
        }

        public boolean isCommitted() {
            return this.isCommitted;
        }

        public long getUpdatedMillis() {
            return this.updatedMillis;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.sessionId);
            dest.writeInt(this.userId);
            dest.writeString(this.installerPackageName);
            dest.writeString(this.resolvedBaseCodePath);
            dest.writeFloat(this.progress);
            dest.writeInt(this.sealed ? 1 : 0);
            dest.writeInt(this.active ? 1 : 0);
            dest.writeInt(this.mode);
            dest.writeInt(this.installReason);
            dest.writeLong(this.sizeBytes);
            dest.writeString(this.appPackageName);
            dest.writeParcelable(this.appIcon, flags);
            CharSequence charSequence = this.appLabel;
            dest.writeString(charSequence != null ? charSequence.toString() : null);
            dest.writeInt(this.installLocation);
            dest.writeParcelable(this.originatingUri, flags);
            dest.writeInt(this.originatingUid);
            dest.writeParcelable(this.referrerUri, flags);
            dest.writeStringArray(this.grantedRuntimePermissions);
            dest.writeStringList(this.whitelistedRestrictedPermissions);
            dest.writeInt(this.installFlags);
            dest.writeBoolean(this.isMultiPackage);
            dest.writeBoolean(this.isStaged);
            dest.writeInt(this.parentSessionId);
            dest.writeIntArray(this.childSessionIds);
            dest.writeBoolean(this.isStagedSessionApplied);
            dest.writeBoolean(this.isStagedSessionReady);
            dest.writeBoolean(this.isStagedSessionFailed);
            dest.writeInt(this.mStagedSessionErrorCode);
            dest.writeString(this.mStagedSessionErrorMessage);
            dest.writeBoolean(this.isCommitted);
        }
    }
}
