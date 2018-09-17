package com.android.server.pm;

import android.app.ActivityManager;
import android.app.AppGlobals;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PackageDeleteObserver;
import android.app.PackageInstallObserver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.IPackageInstaller.Stub;
import android.content.pm.IPackageInstallerCallback;
import android.content.pm.IPackageInstallerSession;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller.SessionInfo;
import android.content.pm.PackageInstaller.SessionParams;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.pm.VersionedPackage;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.system.ErrnoException;
import android.system.Os;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AtomicFile;
import android.util.ExceptionUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.PackageHelper;
import com.android.internal.notification.SystemNotificationChannels;
import com.android.internal.util.FastXmlSerializer;
import com.android.internal.util.ImageUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.XmlUtils;
import com.android.server.IoThread;
import com.android.server.am.HwBroadcastRadarUtil;
import com.android.server.power.IHwShutdownThread;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PackageInstallerService extends Stub {
    private static final String ATTR_ABI_OVERRIDE = "abiOverride";
    @Deprecated
    private static final String ATTR_APP_ICON = "appIcon";
    private static final String ATTR_APP_LABEL = "appLabel";
    private static final String ATTR_APP_PACKAGE_NAME = "appPackageName";
    private static final String ATTR_CREATED_MILLIS = "createdMillis";
    private static final String ATTR_INSTALLER_PACKAGE_NAME = "installerPackageName";
    private static final String ATTR_INSTALLER_UID = "installerUid";
    private static final String ATTR_INSTALL_FLAGS = "installFlags";
    private static final String ATTR_INSTALL_LOCATION = "installLocation";
    private static final String ATTR_INSTALL_REASON = "installRason";
    private static final String ATTR_MODE = "mode";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_ORIGINATING_UID = "originatingUid";
    private static final String ATTR_ORIGINATING_URI = "originatingUri";
    private static final String ATTR_PREPARED = "prepared";
    private static final String ATTR_REFERRER_URI = "referrerUri";
    private static final String ATTR_SEALED = "sealed";
    private static final String ATTR_SESSION_ID = "sessionId";
    private static final String ATTR_SESSION_STAGE_CID = "sessionStageCid";
    private static final String ATTR_SESSION_STAGE_DIR = "sessionStageDir";
    private static final String ATTR_SIZE_BYTES = "sizeBytes";
    private static final String ATTR_USER_ID = "userId";
    private static final String ATTR_VOLUME_UUID = "volumeUuid";
    private static final boolean LOGD = false;
    private static final long MAX_ACTIVE_SESSIONS = 1024;
    private static final long MAX_AGE_MILLIS = 259200000;
    private static final long MAX_HISTORICAL_SESSIONS = 1048576;
    private static final String TAG = "PackageInstaller";
    private static final String TAG_GRANTED_RUNTIME_PERMISSION = "granted-runtime-permission";
    private static final String TAG_SESSION = "session";
    private static final String TAG_SESSIONS = "sessions";
    private static final FilenameFilter sStageFilter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return PackageInstallerService.isStageName(name);
        }
    };
    @GuardedBy("mSessions")
    private final SparseBooleanArray mAllocatedSessions = new SparseBooleanArray();
    private AppOpsManager mAppOps;
    private final Callbacks mCallbacks;
    private final Context mContext;
    @GuardedBy("mSessions")
    private final List<String> mHistoricalSessions = new ArrayList();
    @GuardedBy("mSessions")
    private final SparseIntArray mHistoricalSessionsByInstaller = new SparseIntArray();
    private final Handler mInstallHandler;
    private final HandlerThread mInstallThread;
    private final InternalCallback mInternalCallback = new InternalCallback();
    @GuardedBy("mSessions")
    private final SparseBooleanArray mLegacySessions = new SparseBooleanArray();
    private final PackageManagerService mPm;
    private final Random mRandom = new SecureRandom();
    @GuardedBy("mSessions")
    private final SparseArray<PackageInstallerSession> mSessions = new SparseArray();
    private final File mSessionsDir;
    private final AtomicFile mSessionsFile;

    private static class Callbacks extends Handler {
        private static final int MSG_SESSION_ACTIVE_CHANGED = 3;
        private static final int MSG_SESSION_BADGING_CHANGED = 2;
        private static final int MSG_SESSION_CREATED = 1;
        private static final int MSG_SESSION_FINISHED = 5;
        private static final int MSG_SESSION_PROGRESS_CHANGED = 4;
        private final RemoteCallbackList<IPackageInstallerCallback> mCallbacks = new RemoteCallbackList();

        public Callbacks(Looper looper) {
            super(looper);
        }

        public void register(IPackageInstallerCallback callback, int userId) {
            this.mCallbacks.register(callback, new UserHandle(userId));
        }

        public void unregister(IPackageInstallerCallback callback) {
            this.mCallbacks.unregister(callback);
        }

        public void handleMessage(Message msg) {
            int userId = msg.arg2;
            int n = this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                IPackageInstallerCallback callback = (IPackageInstallerCallback) this.mCallbacks.getBroadcastItem(i);
                if (userId == ((UserHandle) this.mCallbacks.getBroadcastCookie(i)).getIdentifier()) {
                    try {
                        invokeCallback(callback, msg);
                    } catch (RemoteException e) {
                    }
                }
            }
            this.mCallbacks.finishBroadcast();
        }

        private void invokeCallback(IPackageInstallerCallback callback, Message msg) throws RemoteException {
            int sessionId = msg.arg1;
            switch (msg.what) {
                case 1:
                    callback.onSessionCreated(sessionId);
                    return;
                case 2:
                    callback.onSessionBadgingChanged(sessionId);
                    return;
                case 3:
                    callback.onSessionActiveChanged(sessionId, ((Boolean) msg.obj).booleanValue());
                    return;
                case 4:
                    callback.onSessionProgressChanged(sessionId, ((Float) msg.obj).floatValue());
                    return;
                case 5:
                    callback.onSessionFinished(sessionId, ((Boolean) msg.obj).booleanValue());
                    return;
                default:
                    return;
            }
        }

        private void notifySessionCreated(int sessionId, int userId) {
            obtainMessage(1, sessionId, userId).sendToTarget();
        }

        private void notifySessionBadgingChanged(int sessionId, int userId) {
            obtainMessage(2, sessionId, userId).sendToTarget();
        }

        private void notifySessionActiveChanged(int sessionId, int userId, boolean active) {
            obtainMessage(3, sessionId, userId, Boolean.valueOf(active)).sendToTarget();
        }

        private void notifySessionProgressChanged(int sessionId, int userId, float progress) {
            obtainMessage(4, sessionId, userId, Float.valueOf(progress)).sendToTarget();
        }

        public void notifySessionFinished(int sessionId, int userId, boolean success) {
            obtainMessage(5, sessionId, userId, Boolean.valueOf(success)).sendToTarget();
        }
    }

    class InternalCallback {
        InternalCallback() {
        }

        public void onSessionBadgingChanged(PackageInstallerSession session) {
            PackageInstallerService.this.mCallbacks.notifySessionBadgingChanged(session.sessionId, session.userId);
            PackageInstallerService.this.writeSessionsAsync();
        }

        public void onSessionActiveChanged(PackageInstallerSession session, boolean active) {
            PackageInstallerService.this.mCallbacks.notifySessionActiveChanged(session.sessionId, session.userId, active);
        }

        public void onSessionProgressChanged(PackageInstallerSession session, float progress) {
            PackageInstallerService.this.mCallbacks.notifySessionProgressChanged(session.sessionId, session.userId, progress);
        }

        public void onSessionFinished(final PackageInstallerSession session, boolean success) {
            PackageInstallerService.this.mCallbacks.notifySessionFinished(session.sessionId, session.userId, success);
            PackageInstallerService.this.mInstallHandler.post(new Runnable() {
                public void run() {
                    synchronized (PackageInstallerService.this.mSessions) {
                        PackageInstallerService.this.mSessions.remove(session.sessionId);
                        PackageInstallerService.this.addHistoricalSessionLocked(session);
                        File appIconFile = PackageInstallerService.this.buildAppIconFile(session.sessionId);
                        if (appIconFile.exists()) {
                            appIconFile.delete();
                        }
                        PackageInstallerService.this.writeSessionsLocked();
                    }
                }
            });
        }

        public void onSessionPrepared(PackageInstallerSession session) {
            PackageInstallerService.this.writeSessionsAsync();
        }

        public void onSessionSealedBlocking(PackageInstallerSession session) {
            synchronized (PackageInstallerService.this.mSessions) {
                PackageInstallerService.this.writeSessionsLocked();
            }
        }
    }

    static class PackageDeleteObserverAdapter extends PackageDeleteObserver {
        private final Context mContext;
        private final Notification mNotification;
        private final String mPackageName;
        private final IntentSender mTarget;

        public PackageDeleteObserverAdapter(Context context, IntentSender target, String packageName, boolean showNotification, int userId) {
            this.mContext = context;
            this.mTarget = target;
            this.mPackageName = packageName;
            if (showNotification) {
                this.mNotification = PackageInstallerService.buildSuccessNotification(this.mContext, this.mContext.getResources().getString(17040534), packageName, userId);
            } else {
                this.mNotification = null;
            }
        }

        public void onUserActionRequired(Intent intent) {
            Intent fillIn = new Intent();
            fillIn.putExtra("android.content.pm.extra.PACKAGE_NAME", this.mPackageName);
            fillIn.putExtra("android.content.pm.extra.STATUS", -1);
            fillIn.putExtra("android.intent.extra.INTENT", intent);
            try {
                this.mTarget.sendIntent(this.mContext, 0, fillIn, null, null);
            } catch (SendIntentException e) {
            }
        }

        public void onPackageDeleted(String basePackageName, int returnCode, String msg) {
            if (1 == returnCode && this.mNotification != null) {
                ((NotificationManager) this.mContext.getSystemService("notification")).notify(basePackageName, 21, this.mNotification);
            }
            Intent fillIn = new Intent();
            fillIn.putExtra("android.content.pm.extra.PACKAGE_NAME", this.mPackageName);
            fillIn.putExtra("android.content.pm.extra.STATUS", PackageManager.deleteStatusToPublicStatus(returnCode));
            fillIn.putExtra("android.content.pm.extra.STATUS_MESSAGE", PackageManager.deleteStatusToString(returnCode, msg));
            fillIn.putExtra("android.content.pm.extra.LEGACY_STATUS", returnCode);
            try {
                this.mTarget.sendIntent(this.mContext, 0, fillIn, null, null);
            } catch (SendIntentException e) {
            }
        }
    }

    static class PackageInstallObserverAdapter extends PackageInstallObserver {
        private final Context mContext;
        private final int mSessionId;
        private final boolean mShowNotification;
        private final IntentSender mTarget;
        private final int mUserId;

        public PackageInstallObserverAdapter(Context context, IntentSender target, int sessionId, boolean showNotification, int userId) {
            this.mContext = context;
            this.mTarget = target;
            this.mSessionId = sessionId;
            this.mShowNotification = showNotification;
            this.mUserId = userId;
        }

        public void onUserActionRequired(Intent intent) {
            Intent fillIn = new Intent();
            fillIn.putExtra("android.content.pm.extra.SESSION_ID", this.mSessionId);
            fillIn.putExtra("android.content.pm.extra.STATUS", -1);
            fillIn.putExtra("android.intent.extra.INTENT", intent);
            try {
                this.mTarget.sendIntent(this.mContext, 0, fillIn, null, null);
            } catch (SendIntentException e) {
            }
        }

        public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) {
            if (1 == returnCode && this.mShowNotification) {
                int i;
                boolean update = extras != null ? extras.getBoolean("android.intent.extra.REPLACING") : false;
                Context context = this.mContext;
                Resources resources = this.mContext.getResources();
                if (update) {
                    i = 17040536;
                } else {
                    i = 17040535;
                }
                Notification notification = PackageInstallerService.buildSuccessNotification(context, resources.getString(i), basePackageName, this.mUserId);
                if (notification != null) {
                    ((NotificationManager) this.mContext.getSystemService("notification")).notify(basePackageName, 21, notification);
                }
            }
            Intent fillIn = new Intent();
            fillIn.putExtra("android.content.pm.extra.PACKAGE_NAME", basePackageName);
            fillIn.putExtra("android.content.pm.extra.SESSION_ID", this.mSessionId);
            fillIn.putExtra("android.content.pm.extra.STATUS", PackageManager.installStatusToPublicStatus(returnCode));
            fillIn.putExtra("android.content.pm.extra.STATUS_MESSAGE", PackageManager.installStatusToString(returnCode, msg));
            fillIn.putExtra("android.content.pm.extra.LEGACY_STATUS", returnCode);
            if (extras != null) {
                String existing = extras.getString("android.content.pm.extra.FAILURE_EXISTING_PACKAGE");
                if (!TextUtils.isEmpty(existing)) {
                    fillIn.putExtra("android.content.pm.extra.OTHER_PACKAGE_NAME", existing);
                }
            }
            try {
                this.mTarget.sendIntent(this.mContext, 0, fillIn, null, null);
            } catch (SendIntentException e) {
            }
        }
    }

    public PackageInstallerService(Context context, PackageManagerService pm) {
        this.mContext = context;
        this.mPm = pm;
        this.mInstallThread = new HandlerThread(TAG);
        this.mInstallThread.start();
        this.mInstallHandler = new Handler(this.mInstallThread.getLooper());
        this.mCallbacks = new Callbacks(this.mInstallThread.getLooper());
        this.mSessionsFile = new AtomicFile(new File(Environment.getDataSystemDirectory(), "install_sessions.xml"));
        this.mSessionsDir = new File(Environment.getDataSystemDirectory(), "install_sessions");
        this.mSessionsDir.mkdirs();
    }

    public void systemReady() {
        this.mAppOps = (AppOpsManager) this.mContext.getSystemService(AppOpsManager.class);
        synchronized (this.mSessions) {
            readSessionsLocked();
            reconcileStagesLocked(StorageManager.UUID_PRIVATE_INTERNAL, false);
            reconcileStagesLocked(StorageManager.UUID_PRIVATE_INTERNAL, true);
            ArraySet<File> unclaimedIcons = newArraySet(this.mSessionsDir.listFiles());
            for (int i = 0; i < this.mSessions.size(); i++) {
                unclaimedIcons.remove(buildAppIconFile(((PackageInstallerSession) this.mSessions.valueAt(i)).sessionId));
            }
            for (File icon : unclaimedIcons) {
                Slog.w(TAG, "Deleting orphan icon " + icon);
                icon.delete();
            }
        }
    }

    private void reconcileStagesLocked(String volumeUuid, boolean isEphemeral) {
        ArraySet<File> unclaimedStages = newArraySet(buildStagingDir(volumeUuid, isEphemeral).listFiles(sStageFilter));
        for (int i = 0; i < this.mSessions.size(); i++) {
            unclaimedStages.remove(((PackageInstallerSession) this.mSessions.valueAt(i)).stageDir);
        }
        for (File stage : unclaimedStages) {
            Slog.w(TAG, "Deleting orphan stage " + stage);
            synchronized (this.mPm.mInstallLock) {
                this.mPm.removeCodePathLI(stage);
            }
        }
    }

    public void onPrivateVolumeMounted(String volumeUuid) {
        synchronized (this.mSessions) {
            reconcileStagesLocked(volumeUuid, false);
        }
    }

    public void onSecureContainersAvailable() {
        synchronized (this.mSessions) {
            String cid;
            ArraySet<String> unclaimed = new ArraySet();
            for (String cid2 : PackageHelper.getSecureContainerList()) {
                if (isStageName(cid2)) {
                    unclaimed.add(cid2);
                }
            }
            for (int i = 0; i < this.mSessions.size(); i++) {
                cid2 = ((PackageInstallerSession) this.mSessions.valueAt(i)).stageCid;
                if (unclaimed.remove(cid2)) {
                    PackageHelper.mountSdDir(cid2, PackageManagerService.getEncryptKey(), 1000);
                }
            }
            for (String cid22 : unclaimed) {
                Slog.w(TAG, "Deleting orphan container " + cid22);
                PackageHelper.destroySdDir(cid22);
            }
        }
    }

    public static boolean isStageName(String name) {
        return ((name.startsWith("vmdl") ? name.endsWith(".tmp") : false) || (name.startsWith("smdl") ? name.endsWith(".tmp") : false)) ? true : name.startsWith("smdl2tmp");
    }

    @Deprecated
    public File allocateStageDirLegacy(String volumeUuid, boolean isEphemeral) throws IOException {
        File stageDir;
        synchronized (this.mSessions) {
            try {
                int sessionId = allocateSessionIdLocked();
                this.mLegacySessions.put(sessionId, true);
                stageDir = buildStageDir(volumeUuid, sessionId, isEphemeral);
                prepareStageDir(stageDir);
            } catch (IllegalStateException e) {
                throw new IOException(e);
            }
        }
        return stageDir;
    }

    @Deprecated
    public String allocateExternalStageCidLegacy() {
        String str;
        synchronized (this.mSessions) {
            int sessionId = allocateSessionIdLocked();
            this.mLegacySessions.put(sessionId, true);
            str = "smdl" + sessionId + ".tmp";
        }
        return str;
    }

    /* JADX WARNING: Removed duplicated region for block: B:19:0x007d A:{Splitter: B:1:0x0007, ExcHandler: java.io.IOException (r3_0 'e' java.lang.Exception), PHI: r4 } */
    /* JADX WARNING: Missing block: B:19:0x007d, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:21:?, code:
            android.util.Slog.wtf(TAG, "Failed reading install sessions", r3);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readSessionsLocked() {
        this.mSessions.clear();
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = this.mSessionsFile.openRead();
            XmlPullParser in = Xml.newPullParser();
            in.setInput(autoCloseable, StandardCharsets.UTF_8.name());
            loop0:
            while (true) {
                int type = in.next();
                if (type == 1) {
                    break loop0;
                } else if (type == 2) {
                    if (TAG_SESSION.equals(in.getName())) {
                        boolean valid;
                        PackageInstallerSession session = readSessionLocked(in);
                        if (System.currentTimeMillis() - session.createdMillis >= MAX_AGE_MILLIS) {
                            Slog.w(TAG, "Abandoning old session first created at " + session.createdMillis);
                            valid = false;
                        } else {
                            valid = true;
                        }
                        if (valid) {
                            this.mSessions.put(session.sessionId, session);
                        } else {
                            addHistoricalSessionLocked(session);
                        }
                        this.mAllocatedSessions.put(session.sessionId, true);
                    } else {
                        continue;
                    }
                }
            }
        } catch (FileNotFoundException e) {
        } catch (Exception e2) {
        } finally {
            IoUtils.closeQuietly(autoCloseable);
        }
    }

    private void addHistoricalSessionLocked(PackageInstallerSession session) {
        CharArrayWriter writer = new CharArrayWriter();
        session.dump(new IndentingPrintWriter(writer, "    "));
        this.mHistoricalSessions.add(writer.toString());
        this.mHistoricalSessionsByInstaller.put(session.installerUid, this.mHistoricalSessionsByInstaller.get(session.installerUid) + 1);
    }

    private PackageInstallerSession readSessionLocked(XmlPullParser in) throws IOException, XmlPullParserException {
        int sessionId = XmlUtils.readIntAttribute(in, ATTR_SESSION_ID);
        int userId = XmlUtils.readIntAttribute(in, ATTR_USER_ID);
        String installerPackageName = XmlUtils.readStringAttribute(in, ATTR_INSTALLER_PACKAGE_NAME);
        int installerUid = XmlUtils.readIntAttribute(in, ATTR_INSTALLER_UID, this.mPm.getPackageUid(installerPackageName, 8192, userId));
        long createdMillis = XmlUtils.readLongAttribute(in, ATTR_CREATED_MILLIS);
        String stageDirRaw = XmlUtils.readStringAttribute(in, ATTR_SESSION_STAGE_DIR);
        File stageDir = stageDirRaw != null ? new File(stageDirRaw) : null;
        String stageCid = XmlUtils.readStringAttribute(in, ATTR_SESSION_STAGE_CID);
        boolean prepared = XmlUtils.readBooleanAttribute(in, ATTR_PREPARED, true);
        boolean sealed = XmlUtils.readBooleanAttribute(in, ATTR_SEALED);
        SessionParams params = new SessionParams(-1);
        params.mode = XmlUtils.readIntAttribute(in, ATTR_MODE);
        params.installFlags = XmlUtils.readIntAttribute(in, ATTR_INSTALL_FLAGS);
        params.installLocation = XmlUtils.readIntAttribute(in, ATTR_INSTALL_LOCATION);
        params.sizeBytes = XmlUtils.readLongAttribute(in, ATTR_SIZE_BYTES);
        params.appPackageName = XmlUtils.readStringAttribute(in, ATTR_APP_PACKAGE_NAME);
        params.appIcon = XmlUtils.readBitmapAttribute(in, ATTR_APP_ICON);
        params.appLabel = XmlUtils.readStringAttribute(in, ATTR_APP_LABEL);
        params.originatingUri = XmlUtils.readUriAttribute(in, ATTR_ORIGINATING_URI);
        params.originatingUid = XmlUtils.readIntAttribute(in, ATTR_ORIGINATING_UID, -1);
        params.referrerUri = XmlUtils.readUriAttribute(in, ATTR_REFERRER_URI);
        params.abiOverride = XmlUtils.readStringAttribute(in, ATTR_ABI_OVERRIDE);
        params.volumeUuid = XmlUtils.readStringAttribute(in, ATTR_VOLUME_UUID);
        params.grantedRuntimePermissions = readGrantedRuntimePermissions(in);
        params.installReason = XmlUtils.readIntAttribute(in, ATTR_INSTALL_REASON);
        File appIconFile = buildAppIconFile(sessionId);
        if (appIconFile.exists()) {
            params.appIcon = BitmapFactory.decodeFile(appIconFile.getAbsolutePath());
            params.appIconLastModified = appIconFile.lastModified();
        }
        return new PackageInstallerSession(this.mInternalCallback, this.mContext, this.mPm, this.mInstallThread.getLooper(), sessionId, userId, installerPackageName, installerUid, params, createdMillis, stageDir, stageCid, prepared, sealed);
    }

    private void writeSessionsLocked() {
        try {
            FileOutputStream fos = this.mSessionsFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, StandardCharsets.UTF_8.name());
            out.startDocument(null, Boolean.valueOf(true));
            out.startTag(null, TAG_SESSIONS);
            int size = this.mSessions.size();
            for (int i = 0; i < size; i++) {
                writeSessionLocked(out, (PackageInstallerSession) this.mSessions.valueAt(i));
            }
            out.endTag(null, TAG_SESSIONS);
            out.endDocument();
            this.mSessionsFile.finishWrite(fos);
        } catch (IOException e) {
            if (null != null) {
                this.mSessionsFile.failWrite(null);
            }
        }
    }

    private void writeSessionLocked(XmlSerializer out, PackageInstallerSession session) throws IOException {
        IOException e;
        Object os;
        Throwable th;
        SessionParams params = session.params;
        out.startTag(null, TAG_SESSION);
        XmlUtils.writeIntAttribute(out, ATTR_SESSION_ID, session.sessionId);
        XmlUtils.writeIntAttribute(out, ATTR_USER_ID, session.userId);
        XmlUtils.writeStringAttribute(out, ATTR_INSTALLER_PACKAGE_NAME, session.installerPackageName);
        XmlUtils.writeIntAttribute(out, ATTR_INSTALLER_UID, session.installerUid);
        XmlUtils.writeLongAttribute(out, ATTR_CREATED_MILLIS, session.createdMillis);
        if (session.stageDir != null) {
            XmlUtils.writeStringAttribute(out, ATTR_SESSION_STAGE_DIR, session.stageDir.getAbsolutePath());
        }
        if (session.stageCid != null) {
            XmlUtils.writeStringAttribute(out, ATTR_SESSION_STAGE_CID, session.stageCid);
        }
        XmlUtils.writeBooleanAttribute(out, ATTR_PREPARED, session.isPrepared());
        XmlUtils.writeBooleanAttribute(out, ATTR_SEALED, session.isSealed());
        XmlUtils.writeIntAttribute(out, ATTR_MODE, params.mode);
        XmlUtils.writeIntAttribute(out, ATTR_INSTALL_FLAGS, params.installFlags);
        XmlUtils.writeIntAttribute(out, ATTR_INSTALL_LOCATION, params.installLocation);
        XmlUtils.writeLongAttribute(out, ATTR_SIZE_BYTES, params.sizeBytes);
        XmlUtils.writeStringAttribute(out, ATTR_APP_PACKAGE_NAME, params.appPackageName);
        XmlUtils.writeStringAttribute(out, ATTR_APP_LABEL, params.appLabel);
        XmlUtils.writeUriAttribute(out, ATTR_ORIGINATING_URI, params.originatingUri);
        XmlUtils.writeIntAttribute(out, ATTR_ORIGINATING_UID, params.originatingUid);
        XmlUtils.writeUriAttribute(out, ATTR_REFERRER_URI, params.referrerUri);
        XmlUtils.writeStringAttribute(out, ATTR_ABI_OVERRIDE, params.abiOverride);
        XmlUtils.writeStringAttribute(out, ATTR_VOLUME_UUID, params.volumeUuid);
        XmlUtils.writeIntAttribute(out, ATTR_INSTALL_REASON, params.installReason);
        File appIconFile = buildAppIconFile(session.sessionId);
        if (params.appIcon == null && appIconFile.exists()) {
            appIconFile.delete();
        } else if (!(params.appIcon == null || appIconFile.lastModified() == params.appIconLastModified)) {
            AutoCloseable os2 = null;
            try {
                FileOutputStream os3 = new FileOutputStream(appIconFile);
                try {
                    params.appIcon.compress(CompressFormat.PNG, 90, os3);
                    IoUtils.closeQuietly(os3);
                    FileOutputStream fileOutputStream = os3;
                } catch (IOException e2) {
                    e = e2;
                    os2 = os3;
                    try {
                        Slog.w(TAG, "Failed to write icon " + appIconFile + ": " + e.getMessage());
                        IoUtils.closeQuietly(os2);
                        params.appIconLastModified = appIconFile.lastModified();
                        writeGrantedRuntimePermissions(out, params.grantedRuntimePermissions);
                        out.endTag(null, TAG_SESSION);
                    } catch (Throwable th2) {
                        th = th2;
                        IoUtils.closeQuietly(os2);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    os2 = os3;
                    IoUtils.closeQuietly(os2);
                    throw th;
                }
            } catch (IOException e3) {
                e = e3;
                Slog.w(TAG, "Failed to write icon " + appIconFile + ": " + e.getMessage());
                IoUtils.closeQuietly(os2);
                params.appIconLastModified = appIconFile.lastModified();
                writeGrantedRuntimePermissions(out, params.grantedRuntimePermissions);
                out.endTag(null, TAG_SESSION);
            }
            params.appIconLastModified = appIconFile.lastModified();
        }
        writeGrantedRuntimePermissions(out, params.grantedRuntimePermissions);
        out.endTag(null, TAG_SESSION);
    }

    private static void writeGrantedRuntimePermissions(XmlSerializer out, String[] grantedRuntimePermissions) throws IOException {
        if (grantedRuntimePermissions != null) {
            for (String permission : grantedRuntimePermissions) {
                out.startTag(null, TAG_GRANTED_RUNTIME_PERMISSION);
                XmlUtils.writeStringAttribute(out, ATTR_NAME, permission);
                out.endTag(null, TAG_GRANTED_RUNTIME_PERMISSION);
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003d  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x003c A:{RETURN} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static String[] readGrantedRuntimePermissions(XmlPullParser in) throws IOException, XmlPullParserException {
        List permissions = null;
        int outerDepth = in.getDepth();
        while (true) {
            int type = in.next();
            if (type == 1 || (type == 3 && in.getDepth() <= outerDepth)) {
                if (permissions != null) {
                    return null;
                }
                String[] permissionsArray = new String[permissions.size()];
                permissions.toArray(permissionsArray);
                return permissionsArray;
            } else if (!(type == 3 || type == 4 || !TAG_GRANTED_RUNTIME_PERMISSION.equals(in.getName()))) {
                String permission = XmlUtils.readStringAttribute(in, ATTR_NAME);
                if (permissions == null) {
                    permissions = new ArrayList();
                }
                permissions.add(permission);
            }
        }
        if (permissions != null) {
        }
    }

    private File buildAppIconFile(int sessionId) {
        return new File(this.mSessionsDir, "app_icon." + sessionId + ".png");
    }

    private void writeSessionsAsync() {
        IoThread.getHandler().post(new Runnable() {
            public void run() {
                synchronized (PackageInstallerService.this.mSessions) {
                    PackageInstallerService.this.writeSessionsLocked();
                }
            }
        });
    }

    public int createSession(SessionParams params, String installerPackageName, int userId) {
        try {
            return createSessionInternal(params, installerPackageName, userId);
        } catch (IOException e) {
            throw ExceptionUtils.wrap(e);
        }
    }

    private int createSessionInternal(SessionParams params, String installerPackageName, int userId) throws IOException {
        int callingUid = Binder.getCallingUid();
        this.mPm.enforceCrossUserPermission(callingUid, userId, true, true, "createSession");
        if (this.mPm.isUserRestricted(userId, "no_install_apps")) {
            throw new SecurityException("User restriction prevents installing");
        }
        if (callingUid == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || callingUid == 0) {
            params.installFlags |= 32;
        } else {
            this.mAppOps.checkPackage(callingUid, installerPackageName);
            params.installFlags &= -33;
            params.installFlags &= -65;
            params.installFlags |= 2;
        }
        if ((params.installFlags & 256) != 0 && this.mContext.checkCallingOrSelfPermission("android.permission.INSTALL_GRANT_RUNTIME_PERMISSIONS") == -1) {
            throw new SecurityException("You need the android.permission.INSTALL_GRANT_RUNTIME_PERMISSIONS permission to use the PackageManager.INSTALL_GRANT_RUNTIME_PERMISSIONS flag");
        } else if ((params.installFlags & 1) == 0 && (params.installFlags & 8) == 0) {
            if (params.appIcon != null) {
                int iconSize = ((ActivityManager) this.mContext.getSystemService("activity")).getLauncherLargeIconSize();
                if (params.appIcon.getWidth() > iconSize * 2 || params.appIcon.getHeight() > iconSize * 2) {
                    params.appIcon = Bitmap.createScaledBitmap(params.appIcon, iconSize, iconSize, true);
                }
            }
            switch (params.mode) {
                case 1:
                case 2:
                    int sessionId;
                    if ((params.installFlags & 16) != 0) {
                        if (!PackageHelper.fitsOnInternal(this.mContext, params)) {
                            throw new IOException("No suitable internal storage available");
                        }
                    } else if ((params.installFlags & 8) != 0) {
                        if (!PackageHelper.fitsOnExternal(this.mContext, params)) {
                            throw new IOException("No suitable external storage available");
                        }
                    } else if ((params.installFlags & 512) != 0) {
                        params.setInstallFlagsInternal();
                    } else {
                        params.setInstallFlagsInternal();
                        long ident = Binder.clearCallingIdentity();
                        try {
                            params.volumeUuid = PackageHelper.resolveInstallVolume(this.mContext, params);
                        } finally {
                            Binder.restoreCallingIdentity(ident);
                        }
                    }
                    synchronized (this.mSessions) {
                        if (((long) getSessionCount(this.mSessions, callingUid)) >= MAX_ACTIVE_SESSIONS) {
                            throw new IllegalStateException("Too many active sessions for UID " + callingUid);
                        } else if (((long) this.mHistoricalSessionsByInstaller.get(callingUid)) >= MAX_HISTORICAL_SESSIONS) {
                            throw new IllegalStateException("Too many historical sessions for UID " + callingUid);
                        } else {
                            sessionId = allocateSessionIdLocked();
                        }
                    }
                    long createdMillis = System.currentTimeMillis();
                    File stageDir = null;
                    String stageCid = null;
                    if ((params.installFlags & 16) != 0) {
                        stageDir = buildStageDir(params.volumeUuid, sessionId, (params.installFlags & 2048) != 0);
                    } else {
                        stageCid = buildExternalStageCid(sessionId);
                    }
                    PackageInstallerSession session = new PackageInstallerSession(this.mInternalCallback, this.mContext, this.mPm, this.mInstallThread.getLooper(), sessionId, userId, installerPackageName, callingUid, params, createdMillis, stageDir, stageCid, false, false);
                    synchronized (this.mSessions) {
                        this.mSessions.put(sessionId, session);
                    }
                    this.mCallbacks.notifySessionCreated(session.sessionId, session.userId);
                    writeSessionsAsync();
                    return sessionId;
                default:
                    throw new IllegalArgumentException("Invalid install mode: " + params.mode);
            }
        } else {
            throw new IllegalArgumentException("New installs into ASEC containers no longer supported");
        }
    }

    public void updateSessionAppIcon(int sessionId, Bitmap appIcon) {
        synchronized (this.mSessions) {
            PackageInstallerSession session = (PackageInstallerSession) this.mSessions.get(sessionId);
            if (session == null || (isCallingUidOwner(session) ^ 1) != 0) {
                throw new SecurityException("Caller has no access to session " + sessionId);
            }
            if (appIcon != null) {
                int iconSize = ((ActivityManager) this.mContext.getSystemService("activity")).getLauncherLargeIconSize();
                if (appIcon.getWidth() > iconSize * 2 || appIcon.getHeight() > iconSize * 2) {
                    appIcon = Bitmap.createScaledBitmap(appIcon, iconSize, iconSize, true);
                }
            }
            session.params.appIcon = appIcon;
            session.params.appIconLastModified = -1;
            this.mInternalCallback.onSessionBadgingChanged(session);
        }
    }

    public void updateSessionAppLabel(int sessionId, String appLabel) {
        synchronized (this.mSessions) {
            PackageInstallerSession session = (PackageInstallerSession) this.mSessions.get(sessionId);
            if (session == null || (isCallingUidOwner(session) ^ 1) != 0) {
                throw new SecurityException("Caller has no access to session " + sessionId);
            }
            session.params.appLabel = appLabel;
            this.mInternalCallback.onSessionBadgingChanged(session);
        }
    }

    public void abandonSession(int sessionId) {
        synchronized (this.mSessions) {
            PackageInstallerSession session = (PackageInstallerSession) this.mSessions.get(sessionId);
            if (session == null || (isCallingUidOwner(session) ^ 1) != 0) {
                throw new SecurityException("Caller has no access to session " + sessionId);
            }
            session.abandon();
        }
    }

    public IPackageInstallerSession openSession(int sessionId) {
        try {
            return openSessionInternal(sessionId);
        } catch (IOException e) {
            throw ExceptionUtils.wrap(e);
        }
    }

    private IPackageInstallerSession openSessionInternal(int sessionId) throws IOException {
        PackageInstallerSession session;
        synchronized (this.mSessions) {
            session = (PackageInstallerSession) this.mSessions.get(sessionId);
            if (session == null || (isCallingUidOwner(session) ^ 1) != 0) {
                throw new SecurityException("Caller has no access to session " + sessionId);
            }
            session.open();
        }
        return session;
    }

    private int allocateSessionIdLocked() {
        int n = 0;
        while (true) {
            int sessionId = this.mRandom.nextInt(2147483646) + 1;
            if (this.mAllocatedSessions.get(sessionId, false)) {
                int n2 = n + 1;
                if (n < 32) {
                    n = n2;
                } else {
                    throw new IllegalStateException("Failed to allocate session ID");
                }
            }
            this.mAllocatedSessions.put(sessionId, true);
            return sessionId;
        }
    }

    private File buildStagingDir(String volumeUuid, boolean isEphemeral) {
        return Environment.getDataAppDirectory(volumeUuid);
    }

    private File buildStageDir(String volumeUuid, int sessionId, boolean isEphemeral) {
        return new File(buildStagingDir(volumeUuid, isEphemeral), "vmdl" + sessionId + ".tmp");
    }

    static void prepareStageDir(File stageDir) throws IOException {
        if (stageDir.exists()) {
            throw new IOException("Session dir already exists: " + stageDir);
        }
        try {
            Os.mkdir(stageDir.getAbsolutePath(), 493);
            Os.chmod(stageDir.getAbsolutePath(), 493);
            if (!SELinux.restorecon(stageDir)) {
                throw new IOException("Failed to restorecon session dir: " + stageDir);
            }
        } catch (ErrnoException e) {
            throw new IOException("Failed to prepare session dir: " + stageDir, e);
        }
    }

    private String buildExternalStageCid(int sessionId) {
        return "smdl" + sessionId + ".tmp";
    }

    static void prepareExternalStageCid(String stageCid, long sizeBytes) throws IOException {
        if (PackageHelper.createSdDir(sizeBytes, stageCid, PackageManagerService.getEncryptKey(), 1000, true) == null) {
            throw new IOException("Failed to create session cid: " + stageCid);
        }
    }

    public SessionInfo getSessionInfo(int sessionId) {
        SessionInfo sessionInfo = null;
        synchronized (this.mSessions) {
            PackageInstallerSession session = (PackageInstallerSession) this.mSessions.get(sessionId);
            if (session != null) {
                sessionInfo = session.generateInfo();
            }
        }
        return sessionInfo;
    }

    public ParceledListSlice<SessionInfo> getAllSessions(int userId) {
        this.mPm.enforceCrossUserPermission(Binder.getCallingUid(), userId, true, false, "getAllSessions");
        List<SessionInfo> result = new ArrayList();
        synchronized (this.mSessions) {
            for (int i = 0; i < this.mSessions.size(); i++) {
                PackageInstallerSession session = (PackageInstallerSession) this.mSessions.valueAt(i);
                if (session.userId == userId) {
                    result.add(session.generateInfo());
                }
            }
        }
        return new ParceledListSlice(result);
    }

    public ParceledListSlice<SessionInfo> getMySessions(String installerPackageName, int userId) {
        this.mPm.enforceCrossUserPermission(Binder.getCallingUid(), userId, true, false, "getMySessions");
        this.mAppOps.checkPackage(Binder.getCallingUid(), installerPackageName);
        List<SessionInfo> result = new ArrayList();
        synchronized (this.mSessions) {
            for (int i = 0; i < this.mSessions.size(); i++) {
                PackageInstallerSession session = (PackageInstallerSession) this.mSessions.valueAt(i);
                if (Objects.equals(session.installerPackageName, installerPackageName) && session.userId == userId) {
                    result.add(session.generateInfo());
                }
            }
        }
        return new ParceledListSlice(result);
    }

    public void uninstall(VersionedPackage versionedPackage, String callerPackageName, int flags, IntentSender statusReceiver, int userId) throws RemoteException {
        int callingUid = Binder.getCallingUid();
        this.mPm.enforceCrossUserPermission(callingUid, userId, true, true, "uninstall");
        if (!(callingUid == IHwShutdownThread.SHUTDOWN_ANIMATION_WAIT_TIME || callingUid == 0)) {
            this.mAppOps.checkPackage(callingUid, callerPackageName);
        }
        DevicePolicyManager dpm = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        boolean isDeviceOwner = dpm != null ? dpm.isDeviceOwnerAppOnCallingUser(callerPackageName) : false;
        PackageDeleteObserverAdapter adapter = new PackageDeleteObserverAdapter(this.mContext, statusReceiver, versionedPackage.getPackageName(), isDeviceOwner, userId);
        if (this.mContext.checkCallingOrSelfPermission("android.permission.DELETE_PACKAGES") == 0) {
            this.mPm.deletePackageVersioned(versionedPackage, adapter.getBinder(), userId, flags);
        } else if (isDeviceOwner) {
            long ident = Binder.clearCallingIdentity();
            try {
                this.mPm.deletePackageVersioned(versionedPackage, adapter.getBinder(), userId, flags);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        } else {
            Intent intent = new Intent("android.intent.action.UNINSTALL_PACKAGE");
            intent.setData(Uri.fromParts(HwBroadcastRadarUtil.KEY_PACKAGE, versionedPackage.getPackageName(), null));
            intent.putExtra("android.content.pm.extra.CALLBACK", adapter.getBinder().asBinder());
            adapter.onUserActionRequired(intent);
        }
    }

    public void setPermissionsResult(int sessionId, boolean accepted) {
        this.mContext.enforceCallingOrSelfPermission("android.permission.INSTALL_PACKAGES", TAG);
        synchronized (this.mSessions) {
            PackageInstallerSession session = (PackageInstallerSession) this.mSessions.get(sessionId);
            if (session != null) {
                session.setPermissionsResult(accepted);
            }
        }
    }

    public void registerCallback(IPackageInstallerCallback callback, int userId) {
        this.mPm.enforceCrossUserPermission(Binder.getCallingUid(), userId, true, false, "registerCallback");
        this.mCallbacks.register(callback, userId);
    }

    public void unregisterCallback(IPackageInstallerCallback callback) {
        this.mCallbacks.unregister(callback);
    }

    private static int getSessionCount(SparseArray<PackageInstallerSession> sessions, int installerUid) {
        int count = 0;
        int size = sessions.size();
        for (int i = 0; i < size; i++) {
            if (((PackageInstallerSession) sessions.valueAt(i)).installerUid == installerUid) {
                count++;
            }
        }
        return count;
    }

    private boolean isCallingUidOwner(PackageInstallerSession session) {
        boolean z = true;
        int callingUid = Binder.getCallingUid();
        if (callingUid == 0) {
            return true;
        }
        if (session == null || callingUid != session.installerUid) {
            z = false;
        }
        return z;
    }

    private static Notification buildSuccessNotification(Context context, String contentText, String basePackageName, int userId) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = AppGlobals.getPackageManager().getPackageInfo(basePackageName, 67108864, userId);
        } catch (RemoteException e) {
        }
        if (packageInfo == null || packageInfo.applicationInfo == null) {
            Slog.w(TAG, "Notification not built for package: " + basePackageName);
            return null;
        }
        PackageManager pm = context.getPackageManager();
        return new Builder(context, SystemNotificationChannels.DEVICE_ADMIN).setSmallIcon(17302298).setColor(context.getResources().getColor(17170769)).setContentTitle(packageInfo.applicationInfo.loadLabel(pm)).setContentText(contentText).setStyle(new BigTextStyle().bigText(contentText)).setLargeIcon(ImageUtils.buildScaledBitmap(packageInfo.applicationInfo.loadIcon(pm), context.getResources().getDimensionPixelSize(17104901), context.getResources().getDimensionPixelSize(17104902))).build();
    }

    public static <E> ArraySet<E> newArraySet(E... elements) {
        ArraySet<E> set = new ArraySet();
        if (elements != null) {
            set.ensureCapacity(elements.length);
            Collections.addAll(set, elements);
        }
        return set;
    }

    void dump(IndentingPrintWriter pw) {
        synchronized (this.mSessions) {
            int i;
            pw.println("Active install sessions:");
            pw.increaseIndent();
            int N = this.mSessions.size();
            for (i = 0; i < N; i++) {
                ((PackageInstallerSession) this.mSessions.valueAt(i)).dump(pw);
                pw.println();
            }
            pw.println();
            pw.decreaseIndent();
            pw.println("Historical install sessions:");
            pw.increaseIndent();
            N = this.mHistoricalSessions.size();
            for (i = 0; i < N; i++) {
                pw.print((String) this.mHistoricalSessions.get(i));
                pw.println();
            }
            pw.println();
            pw.decreaseIndent();
            pw.println("Legacy install sessions:");
            pw.increaseIndent();
            pw.println(this.mLegacySessions.toString());
            pw.decreaseIndent();
        }
    }
}
