package android.content.pm;

import android.content.Context;
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
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.StrictMode;
import android.provider.ContactsContract.Intents;
import android.provider.DocumentsContract.Document;
import android.renderscript.Mesh.TriangleMeshBuilder;
import android.security.keymaster.KeymasterDefs;
import android.util.ExceptionUtils;
import com.android.internal.util.IndentingPrintWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PackageInstaller {
    public static final String ACTION_CONFIRM_PERMISSIONS = "android.content.pm.action.CONFIRM_PERMISSIONS";
    public static final String ACTION_SESSION_DETAILS = "android.content.pm.action.SESSION_DETAILS";
    public static final String EXTRA_CALLBACK = "android.content.pm.extra.CALLBACK";
    public static final String EXTRA_LEGACY_BUNDLE = "android.content.pm.extra.LEGACY_BUNDLE";
    public static final String EXTRA_LEGACY_STATUS = "android.content.pm.extra.LEGACY_STATUS";
    public static final String EXTRA_OTHER_PACKAGE_NAME = "android.content.pm.extra.OTHER_PACKAGE_NAME";
    public static final String EXTRA_PACKAGE_NAME = "android.content.pm.extra.PACKAGE_NAME";
    @Deprecated
    public static final String EXTRA_PACKAGE_NAMES = "android.content.pm.extra.PACKAGE_NAMES";
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
    private final Context mContext;
    private final ArrayList<SessionCallbackDelegate> mDelegates;
    private final IPackageInstaller mInstaller;
    private final String mInstallerPackageName;
    private final PackageManager mPm;
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
                return new FileBridgeOutputStream(this.mSession.openWrite(name, offsetBytes, lengthBytes));
            } catch (RuntimeException e) {
                ExceptionUtils.maybeUnwrapIOException(e);
                throw e;
            } catch (RemoteException e2) {
                throw e2.rethrowFromSystemServer();
            }
        }

        public void fsync(OutputStream out) throws IOException {
            if (out instanceof FileBridgeOutputStream) {
                ((FileBridgeOutputStream) out).fsync();
                return;
            }
            throw new IllegalArgumentException("Unrecognized stream");
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
                case MSG_SESSION_CREATED /*1*/:
                    this.mCallback.onCreated(sessionId);
                    return true;
                case MSG_SESSION_BADGING_CHANGED /*2*/:
                    this.mCallback.onBadgingChanged(sessionId);
                    return true;
                case MSG_SESSION_ACTIVE_CHANGED /*3*/:
                    this.mCallback.onActiveChanged(sessionId, msg.arg2 != 0);
                    return true;
                case MSG_SESSION_PROGRESS_CHANGED /*4*/:
                    this.mCallback.onProgressChanged(sessionId, ((Float) msg.obj).floatValue());
                    return true;
                case MSG_SESSION_FINISHED /*5*/:
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
            this.mHandler.obtainMessage(MSG_SESSION_CREATED, sessionId, PackageInstaller.STATUS_SUCCESS).sendToTarget();
        }

        public void onSessionBadgingChanged(int sessionId) {
            this.mHandler.obtainMessage(MSG_SESSION_BADGING_CHANGED, sessionId, PackageInstaller.STATUS_SUCCESS).sendToTarget();
        }

        public void onSessionActiveChanged(int sessionId, boolean active) {
            this.mHandler.obtainMessage(MSG_SESSION_ACTIVE_CHANGED, sessionId, active ? MSG_SESSION_CREATED : PackageInstaller.STATUS_SUCCESS).sendToTarget();
        }

        public void onSessionProgressChanged(int sessionId, float progress) {
            this.mHandler.obtainMessage(MSG_SESSION_PROGRESS_CHANGED, sessionId, PackageInstaller.STATUS_SUCCESS, Float.valueOf(progress)).sendToTarget();
        }

        public void onSessionFinished(int sessionId, boolean success) {
            this.mHandler.obtainMessage(MSG_SESSION_FINISHED, sessionId, success ? MSG_SESSION_CREATED : PackageInstaller.STATUS_SUCCESS).sendToTarget();
        }
    }

    public static class SessionInfo implements Parcelable {
        public static final Creator<SessionInfo> CREATOR = null;
        public boolean active;
        public Bitmap appIcon;
        public CharSequence appLabel;
        public String appPackageName;
        public String installerPackageName;
        public int mode;
        public float progress;
        public String resolvedBaseCodePath;
        public boolean sealed;
        public int sessionId;
        public long sizeBytes;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.PackageInstaller.SessionInfo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.PackageInstaller.SessionInfo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageInstaller.SessionInfo.<clinit>():void");
        }

        public SessionInfo(Parcel source) {
            boolean z;
            boolean z2 = true;
            this.sessionId = source.readInt();
            this.installerPackageName = source.readString();
            this.resolvedBaseCodePath = source.readString();
            this.progress = source.readFloat();
            if (source.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.sealed = z;
            if (source.readInt() == 0) {
                z2 = false;
            }
            this.active = z2;
            this.mode = source.readInt();
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
            intent.setFlags(KeymasterDefs.KM_ENUM);
            return intent;
        }

        @Deprecated
        public Intent getDetailsIntent() {
            return createDetailsIntent();
        }

        public int describeContents() {
            return PackageInstaller.STATUS_SUCCESS;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            String charSequence;
            int i2 = PackageInstaller.STATUS_FAILURE;
            dest.writeInt(this.sessionId);
            dest.writeString(this.installerPackageName);
            dest.writeString(this.resolvedBaseCodePath);
            dest.writeFloat(this.progress);
            if (this.sealed) {
                i = PackageInstaller.STATUS_FAILURE;
            } else {
                i = PackageInstaller.STATUS_SUCCESS;
            }
            dest.writeInt(i);
            if (!this.active) {
                i2 = PackageInstaller.STATUS_SUCCESS;
            }
            dest.writeInt(i2);
            dest.writeInt(this.mode);
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
        public static final Creator<SessionParams> CREATOR = null;
        public static final int MODE_FULL_INSTALL = 1;
        public static final int MODE_INHERIT_EXISTING = 2;
        public static final int MODE_INVALID = -1;
        public static final int UID_UNKNOWN = -1;
        public String abiOverride;
        public Bitmap appIcon;
        public long appIconLastModified;
        public String appLabel;
        public String appPackageName;
        public String[] grantedRuntimePermissions;
        public int installFlags;
        public int installLocation;
        public int mode;
        public int originatingUid;
        public Uri originatingUri;
        public Uri referrerUri;
        public long sizeBytes;
        public String volumeUuid;

        /* renamed from: android.content.pm.PackageInstaller.SessionParams.1 */
        static class AnonymousClass1 implements Creator<SessionParams> {
            AnonymousClass1() {
            }

            public /* bridge */ /* synthetic */ Object m34createFromParcel(Parcel p) {
                return createFromParcel(p);
            }

            public SessionParams createFromParcel(Parcel p) {
                return new SessionParams(p);
            }

            public /* bridge */ /* synthetic */ Object[] m35newArray(int size) {
                return newArray(size);
            }

            public SessionParams[] newArray(int size) {
                return new SessionParams[size];
            }
        }

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.pm.PackageInstaller.SessionParams.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.pm.PackageInstaller.SessionParams.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageInstaller.SessionParams.<clinit>():void");
        }

        public SessionParams(int mode) {
            this.mode = UID_UNKNOWN;
            this.installLocation = MODE_FULL_INSTALL;
            this.sizeBytes = -1;
            this.appIconLastModified = -1;
            this.originatingUid = UID_UNKNOWN;
            this.mode = mode;
        }

        public SessionParams(Parcel source) {
            this.mode = UID_UNKNOWN;
            this.installLocation = MODE_FULL_INSTALL;
            this.sizeBytes = -1;
            this.appIconLastModified = -1;
            this.originatingUid = UID_UNKNOWN;
            this.mode = source.readInt();
            this.installFlags = source.readInt();
            this.installLocation = source.readInt();
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
            this.installFlags |= TriangleMeshBuilder.TEXTURE_0;
            this.grantedRuntimePermissions = permissions;
        }

        public void setInstallFlagsInternal() {
            this.installFlags |= 16;
            this.installFlags &= -9;
        }

        public void setAllowDowngrade(boolean allowDowngrade) {
            if (allowDowngrade) {
                this.installFlags |= KeymasterDefs.KM_ALGORITHM_HMAC;
            } else {
                this.installFlags &= -129;
            }
        }

        public void setInstallFlagsExternal() {
            this.installFlags |= 8;
            this.installFlags &= -17;
        }

        public void setInstallFlagsForcePermissionPrompt() {
            this.installFlags |= Document.FLAG_SUPPORTS_REMOVE;
        }

        public void setDontKillApp(boolean dontKillApp) {
            if (dontKillApp) {
                this.installFlags |= StrictMode.DETECT_VM_REGISTRATION_LEAKS;
            } else {
                this.installFlags &= -4097;
            }
        }

        public void dump(IndentingPrintWriter pw) {
            pw.printPair(Intents.EXTRA_MODE, Integer.valueOf(this.mode));
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
            return PackageInstaller.STATUS_SUCCESS;
        }

        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.mode);
            dest.writeInt(this.installFlags);
            dest.writeInt(this.installLocation);
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
        }
    }

    public PackageInstaller(Context context, PackageManager pm, IPackageInstaller installer, String installerPackageName, int userId) {
        this.mDelegates = new ArrayList();
        this.mContext = context;
        this.mPm = pm;
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
        String charSequence;
        if (appLabel != null) {
            try {
                charSequence = appLabel.toString();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        charSequence = null;
        this.mInstaller.updateSessionAppLabel(sessionId, charSequence);
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
        try {
            this.mInstaller.uninstall(packageName, this.mInstallerPackageName, STATUS_SUCCESS, statusReceiver, this.mUserId);
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
