package android.content.pm;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.IPackageInstallerCallback.Stub;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.FileBridge.FileBridgeOutputStream;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.system.ErrnoException;
import android.system.Os;
import android.util.ExceptionUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PackageInstaller {
    public static final String ACTION_CONFIRM_PERMISSIONS = "android.content.pm.action.CONFIRM_PERMISSIONS";
    public static final String ACTION_SESSION_COMMITTED = "android.content.pm.action.SESSION_COMMITTED";
    public static final String ACTION_SESSION_DETAILS = "android.content.pm.action.SESSION_DETAILS";
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
    private final ArrayList<SessionCallbackDelegate> mDelegates = new ArrayList();
    private final IPackageInstaller mInstaller;
    private final String mInstallerPackageName;
    private final int mUserId;

    public static class Session implements Closeable {
        private IPackageInstallerSession mSession;

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
                    return new AutoCloseOutputStream(this.mSession.openWrite(name, offsetBytes, lengthBytes));
                }
                return new FileBridgeOutputStream(this.mSession.openWrite(name, offsetBytes, lengthBytes));
            } catch (RuntimeException e) {
                ExceptionUtils.maybeUnwrapIOException(e);
                throw e;
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }

        public void fsync(OutputStream out) throws IOException {
            if (PackageInstaller.ENABLE_REVOCABLE_FD) {
                if (out instanceof AutoCloseOutputStream) {
                    try {
                        Os.fsync(((AutoCloseOutputStream) out).getFD());
                        return;
                    } catch (ErrnoException e) {
                        throw e.rethrowAsIOException();
                    }
                }
                throw new IllegalArgumentException("Unrecognized stream");
            } else if (out instanceof FileBridgeOutputStream) {
                ((FileBridgeOutputStream) out).fsync();
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
                return new AutoCloseInputStream(this.mSession.openRead(name));
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
                this.mSession.commit(statusReceiver);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }

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
    }

    public static abstract class SessionCallback {
        public abstract void onActiveChanged(int i, boolean z);

        public abstract void onBadgingChanged(int i);

        public abstract void onCreated(int i);

        public abstract void onFinished(int i, boolean z);

        public abstract void onProgressChanged(int i, float f);
    }

    private static class SessionCallbackDelegate extends Stub implements Callback {
        private static final int MSG_SESSION_ACTIVE_CHANGED = 3;
        private static final int MSG_SESSION_BADGING_CHANGED = 2;
        private static final int MSG_SESSION_CREATED = 1;
        private static final int MSG_SESSION_FINISHED = 5;
        private static final int MSG_SESSION_PROGRESS_CHANGED = 4;
        final SessionCallback mCallback;
        final Handler mHandler;

        public SessionCallbackDelegate(SessionCallback callback, Looper looper) {
            this.mCallback = callback;
            this.mHandler = new Handler(looper, (Callback) this);
        }

        public boolean handleMessage(Message msg) {
            boolean z = false;
            int sessionId = msg.arg1;
            switch (msg.what) {
                case 1:
                    this.mCallback.onCreated(sessionId);
                    return true;
                case 2:
                    this.mCallback.onBadgingChanged(sessionId);
                    return true;
                case 3:
                    this.mCallback.onActiveChanged(sessionId, msg.arg2 != 0);
                    return true;
                case 4:
                    this.mCallback.onProgressChanged(sessionId, ((Float) msg.obj).floatValue());
                    return true;
                case 5:
                    SessionCallback sessionCallback = this.mCallback;
                    if (msg.arg2 != 0) {
                        z = true;
                    }
                    sessionCallback.onFinished(sessionId, z);
                    return true;
                default:
                    return false;
            }
        }

        public void onSessionCreated(int sessionId) {
            this.mHandler.obtainMessage(1, sessionId, 0).sendToTarget();
        }

        public void onSessionBadgingChanged(int sessionId) {
            this.mHandler.obtainMessage(2, sessionId, 0).sendToTarget();
        }

        public void onSessionActiveChanged(int sessionId, boolean active) {
            this.mHandler.obtainMessage(3, sessionId, active ? 1 : 0).sendToTarget();
        }

        public void onSessionProgressChanged(int sessionId, float progress) {
            this.mHandler.obtainMessage(4, sessionId, 0, Float.valueOf(progress)).sendToTarget();
        }

        public void onSessionFinished(int sessionId, boolean success) {
            this.mHandler.obtainMessage(5, sessionId, success ? 1 : 0).sendToTarget();
        }
    }

    public static class SessionInfo implements Parcelable {
        public static final Creator<SessionInfo> CREATOR = new Creator<SessionInfo>() {
            public SessionInfo createFromParcel(Parcel p) {
                return new SessionInfo(p);
            }

            public SessionInfo[] newArray(int size) {
                return new SessionInfo[size];
            }
        };
        public boolean active;
        public Bitmap appIcon;
        public CharSequence appLabel;
        public String appPackageName;
        public int installReason;
        public String installerPackageName;
        public int mode;
        public float progress;
        public String resolvedBaseCodePath;
        public boolean sealed;
        public int sessionId;
        public long sizeBytes;

        public SessionInfo(Parcel source) {
            boolean z = true;
            this.sessionId = source.readInt();
            this.installerPackageName = source.readString();
            this.resolvedBaseCodePath = source.readString();
            this.progress = source.readFloat();
            this.sealed = source.readInt() != 0;
            if (source.readInt() == 0) {
                z = false;
            }
            this.active = z;
            this.mode = source.readInt();
            this.installReason = source.readInt();
            this.sizeBytes = source.readLong();
            this.appPackageName = source.readString();
            this.appIcon = (Bitmap) source.readParcelable(null);
            this.appLabel = source.readString();
        }

        public int getSessionId() {
            return this.sessionId;
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

        @Deprecated
        public Intent getDetailsIntent() {
            return createDetailsIntent();
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
            String charSequence;
            int i = 1;
            dest.writeInt(this.sessionId);
            dest.writeString(this.installerPackageName);
            dest.writeString(this.resolvedBaseCodePath);
            dest.writeFloat(this.progress);
            dest.writeInt(this.sealed ? 1 : 0);
            if (!this.active) {
                i = 0;
            }
            dest.writeInt(i);
            dest.writeInt(this.mode);
            dest.writeInt(this.installReason);
            dest.writeLong(this.sizeBytes);
            dest.writeString(this.appPackageName);
            dest.writeParcelable(this.appIcon, flags);
            if (this.appLabel != null) {
                charSequence = this.appLabel.toString();
            } else {
                charSequence = null;
            }
            dest.writeString(charSequence);
        }
    }

    public static class SessionParams implements Parcelable {
        public static final Creator<SessionParams> CREATOR = new Creator<SessionParams>() {
            public SessionParams createFromParcel(Parcel p) {
                return new SessionParams(p);
            }

            public SessionParams[] newArray(int size) {
                return new SessionParams[size];
            }
        };
        public static final int MODE_FULL_INSTALL = 1;
        public static final int MODE_INHERIT_EXISTING = 2;
        public static final int MODE_INVALID = -1;
        public static final int UID_UNKNOWN = -1;
        public String abiOverride;
        public Bitmap appIcon;
        public long appIconLastModified = -1;
        public String appLabel;
        public String appPackageName;
        public String[] grantedRuntimePermissions;
        public int hdbArgIndex = 0;
        public String[] hdbArgs;
        public String hdbEncode = null;
        public int installFlags;
        public int installLocation = 1;
        public int installReason = 0;
        public int mode = -1;
        public int originatingUid = -1;
        public Uri originatingUri;
        public Uri referrerUri;
        public long sizeBytes = -1;
        public String volumeUuid;

        public SessionParams(int mode) {
            this.mode = mode;
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
            this.hdbEncode = source.readString();
            this.hdbArgIndex = source.readInt();
            this.hdbArgs = source.readStringArray();
        }

        public void setInstallLocation(int installLocation) {
            this.installLocation = installLocation;
        }

        public void setSize(long sizeBytes) {
            this.sizeBytes = sizeBytes;
        }

        public void setAppPackageName(String appPackageName) {
            this.appPackageName = appPackageName;
        }

        public void setAppIcon(Bitmap appIcon) {
            this.appIcon = appIcon;
        }

        public void setAppLabel(CharSequence appLabel) {
            String str = null;
            if (appLabel != null) {
                str = appLabel.toString();
            }
            this.appLabel = str;
        }

        public void setOriginatingUri(Uri originatingUri) {
            this.originatingUri = originatingUri;
        }

        public void setOriginatingUid(int originatingUid) {
            this.originatingUid = originatingUid;
        }

        public void setReferrerUri(Uri referrerUri) {
            this.referrerUri = referrerUri;
        }

        public void setGrantedRuntimePermissions(String[] permissions) {
            this.installFlags |= 256;
            this.grantedRuntimePermissions = permissions;
        }

        public void setInstallFlagsInternal() {
            this.installFlags |= 16;
            this.installFlags &= -9;
        }

        public void setAllowDowngrade(boolean allowDowngrade) {
            if (allowDowngrade) {
                this.installFlags |= 128;
            } else {
                this.installFlags &= -129;
            }
        }

        public void setInstallFlagsExternal() {
            this.installFlags |= 8;
            this.installFlags &= -17;
        }

        public void setInstallFlagsForcePermissionPrompt() {
            this.installFlags |= 1024;
        }

        public void setDontKillApp(boolean dontKillApp) {
            if (dontKillApp) {
                this.installFlags |= 4096;
            } else {
                this.installFlags &= -4097;
            }
        }

        public void setInstallAsInstantApp(boolean isInstantApp) {
            if (isInstantApp) {
                this.installFlags |= 2048;
                this.installFlags &= -16385;
                return;
            }
            this.installFlags &= -2049;
            this.installFlags |= 16384;
        }

        public void setInstallReason(int installReason) {
            this.installReason = installReason;
        }

        public void setAllocateAggressive(boolean allocateAggressive) {
            if (allocateAggressive) {
                this.installFlags |= 32768;
            } else {
                this.installFlags &= -32769;
            }
        }

        public void dump(IndentingPrintWriter pw) {
            pw.printPair(PowerManager.EXTRA_POWER_SAVE_MODE, Integer.valueOf(this.mode));
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
            pw.printPair("grantedRuntimePermissions", this.grantedRuntimePermissions);
            pw.println();
        }

        public int describeContents() {
            return 0;
        }

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
            dest.writeString(this.hdbEncode);
            dest.writeInt(this.hdbArgIndex);
            dest.writeStringArray(this.hdbArgs);
        }
    }

    public PackageInstaller(IPackageInstaller installer, String installerPackageName, int userId) {
        this.mInstaller = installer;
        this.mInstallerPackageName = installerPackageName;
        this.mUserId = userId;
    }

    public int createSession(SessionParams params) throws IOException {
        try {
            return this.mInstaller.createSession(params, this.mInstallerPackageName, this.mUserId);
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
        } catch (RuntimeException e) {
            ExceptionUtils.maybeUnwrapIOException(e);
            throw e;
        } catch (RemoteException e2) {
            throw e2.rethrowFromSystemServer();
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
        }
        val = null;
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

    public void uninstall(String packageName, IntentSender statusReceiver) {
        uninstall(new VersionedPackage(packageName, -1), statusReceiver);
    }

    public void uninstall(VersionedPackage versionedPackage, IntentSender statusReceiver) {
        Preconditions.checkNotNull(versionedPackage, "versionedPackage cannot be null");
        try {
            this.mInstaller.uninstall(versionedPackage, this.mInstallerPackageName, 0, statusReceiver, this.mUserId);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public void setPermissionsResult(int sessionId, boolean accepted) {
        try {
            this.mInstaller.setPermissionsResult(sessionId, accepted);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
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
            SessionCallbackDelegate delegate = new SessionCallbackDelegate(callback, handler.getLooper());
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
                SessionCallbackDelegate delegate = (SessionCallbackDelegate) i.next();
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
}
