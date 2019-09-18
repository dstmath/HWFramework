package com.android.server.pm;

import android.app.admin.DevicePolicyManagerInternal;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver2;
import android.content.pm.IPackageInstallerSession;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.dex.DexMetadataHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.biometrics.fingerprint.V2_1.RequestStatus;
import android.os.Binder;
import android.os.Bundle;
import android.os.FileBridge;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ParcelableException;
import android.os.RemoteException;
import android.os.RevocableFileDescriptor;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.system.ErrnoException;
import android.system.Int64Ref;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.ExceptionUtils;
import android.util.MathUtils;
import android.util.Slog;
import android.util.apk.ApkSignatureVerifier;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.content.PackageHelper;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.LocalServices;
import com.android.server.pm.Installer;
import com.android.server.pm.PackageInstallerService;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class PackageInstallerSession extends IPackageInstallerSession.Stub {
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
    private static final boolean LOGD = true;
    private static final int MSG_COMMIT = 1;
    private static final int MSG_EARLY_BIND = 0;
    private static final int MSG_ON_PACKAGE_INSTALLED = 2;
    private static final String PROPERTY_NAME_INHERIT_NATIVE = "pi.inherit_native_on_dont_kill";
    private static final String REMOVE_SPLIT_MARKER_EXTENSION = ".removed";
    private static final String TAG = "PackageInstaller";
    private static final String TAG_GRANTED_RUNTIME_PERMISSION = "granted-runtime-permission";
    static final String TAG_SESSION = "session";
    private static final FileFilter sAddedFilter = new FileFilter() {
        public boolean accept(File file) {
            if (!file.isDirectory() && !file.getName().endsWith(PackageInstallerSession.REMOVE_SPLIT_MARKER_EXTENSION) && !DexMetadataHelper.isDexMetadataFile(file)) {
                return true;
            }
            return false;
        }
    };
    private static final FileFilter sRemovedFilter = new FileFilter() {
        public boolean accept(File file) {
            if (!file.isDirectory() && file.getName().endsWith(PackageInstallerSession.REMOVE_SPLIT_MARKER_EXTENSION)) {
                return true;
            }
            return false;
        }
    };
    final long createdMillis;
    final int defaultContainerGid;
    private final AtomicInteger mActiveCount = new AtomicInteger();
    @GuardedBy("mLock")
    private final ArrayList<FileBridge> mBridges;
    private final PackageInstallerService.InternalCallback mCallback;
    @GuardedBy("mLock")
    private float mClientProgress = 0.0f;
    @GuardedBy("mLock")
    private boolean mCommitted;
    private final Context mContext;
    @GuardedBy("mLock")
    private boolean mDestroyed;
    @GuardedBy("mLock")
    private final ArrayList<RevocableFileDescriptor> mFds;
    @GuardedBy("mLock")
    private String mFinalMessage;
    @GuardedBy("mLock")
    private int mFinalStatus;
    private final Handler mHandler;
    private final Handler.Callback mHandlerCallback;
    private boolean mHwPermissionsAccepted;
    @GuardedBy("mLock")
    private File mInheritedFilesBase;
    @GuardedBy("mLock")
    private String mInstallerPackageName;
    @GuardedBy("mLock")
    private int mInstallerUid;
    @GuardedBy("mLock")
    private float mInternalProgress = 0.0f;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    private final int mOriginalInstallerUid;
    @GuardedBy("mLock")
    private String mPackageName;
    @GuardedBy("mLock")
    private boolean mPermissionsManuallyAccepted;
    private final PackageManagerService mPm;
    @GuardedBy("mLock")
    private boolean mPrepared;
    @GuardedBy("mLock")
    private float mProgress = 0.0f;
    @GuardedBy("mLock")
    private boolean mRelinquished;
    @GuardedBy("mLock")
    private IPackageInstallObserver2 mRemoteObserver;
    @GuardedBy("mLock")
    private float mReportedProgress = -1.0f;
    @GuardedBy("mLock")
    private File mResolvedBaseFile;
    @GuardedBy("mLock")
    private final List<File> mResolvedInheritedFiles;
    @GuardedBy("mLock")
    private final List<String> mResolvedInstructionSets;
    @GuardedBy("mLock")
    private final List<String> mResolvedNativeLibPaths;
    @GuardedBy("mLock")
    private File mResolvedStageDir;
    @GuardedBy("mLock")
    private final List<File> mResolvedStagedFiles;
    @GuardedBy("mLock")
    private boolean mSealed;
    @GuardedBy("mLock")
    private PackageParser.SigningDetails mSigningDetails;
    @GuardedBy("mLock")
    private long mVersionCode;
    final PackageInstaller.SessionParams params;
    final int sessionId;
    final String stageCid;
    final File stageDir;
    final int userId;

    /* access modifiers changed from: private */
    public void earlyBindToDefContainer() {
        this.mPm.earlyBindToDefContainer();
    }

    @GuardedBy("mLock")
    private boolean isInstallerDeviceOwnerOrAffiliatedProfileOwnerLocked() {
        DevicePolicyManagerInternal dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class);
        return dpmi != null && dpmi.isActiveAdminWithPolicy(this.mInstallerUid, -1) && dpmi.isUserAffiliatedWithDevice(this.userId);
    }

    @GuardedBy("mLock")
    private boolean needToAskForPermissionsLocked() {
        boolean z = false;
        if (this.mPermissionsManuallyAccepted) {
            return false;
        }
        boolean isInstallPermissionGranted = this.mPm.checkUidPermission("android.permission.INSTALL_PACKAGES", this.mInstallerUid) == 0;
        boolean isSelfUpdatePermissionGranted = this.mPm.checkUidPermission("android.permission.INSTALL_SELF_UPDATES", this.mInstallerUid) == 0;
        boolean isUpdatePermissionGranted = this.mPm.checkUidPermission("android.permission.INSTALL_PACKAGE_UPDATES", this.mInstallerUid) == 0;
        int targetPackageUid = this.mPm.getPackageUid(this.mPackageName, 0, this.userId);
        boolean isPermissionGranted = isInstallPermissionGranted || (isUpdatePermissionGranted && targetPackageUid != -1) || (isSelfUpdatePermissionGranted && targetPackageUid == this.mInstallerUid);
        boolean isInstallerRoot = this.mInstallerUid == 0;
        boolean isInstallerSystem = this.mInstallerUid == 1000;
        if (((this.params.installFlags & 1024) != 0) || (!isPermissionGranted && !isInstallerRoot && !isInstallerSystem && !isInstallerDeviceOwnerOrAffiliatedProfileOwnerLocked())) {
            z = true;
        }
        return z;
    }

    public PackageInstallerSession(PackageInstallerService.InternalCallback callback, Context context, PackageManagerService pm, Looper looper, int sessionId2, int userId2, String installerPackageName, int installerUid, PackageInstaller.SessionParams params2, long createdMillis2, File stageDir2, String stageCid2, boolean prepared, boolean sealed) {
        long identity;
        int i = installerUid;
        PackageInstaller.SessionParams sessionParams = params2;
        File file = stageDir2;
        String str = stageCid2;
        boolean z = false;
        this.mPrepared = false;
        this.mSealed = false;
        this.mCommitted = false;
        boolean z2 = true;
        this.mHwPermissionsAccepted = true;
        this.mRelinquished = false;
        this.mDestroyed = false;
        this.mPermissionsManuallyAccepted = false;
        this.mFds = new ArrayList<>();
        this.mBridges = new ArrayList<>();
        this.mResolvedStagedFiles = new ArrayList();
        this.mResolvedInheritedFiles = new ArrayList();
        this.mResolvedInstructionSets = new ArrayList();
        this.mResolvedNativeLibPaths = new ArrayList();
        this.mHandlerCallback = new Handler.Callback() {
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        PackageInstallerSession.this.earlyBindToDefContainer();
                        break;
                    case 1:
                        synchronized (PackageInstallerSession.this.mLock) {
                            try {
                                PackageInstallerSession.this.commitLocked();
                            } catch (PackageManagerException e) {
                                String completeMsg = ExceptionUtils.getCompleteMessage(e);
                                Slog.e(PackageInstallerSession.TAG, "Commit of session " + PackageInstallerSession.this.sessionId + " failed: " + completeMsg);
                                PackageInstallerSession.this.destroyInternal();
                                PackageInstallerSession.this.dispatchSessionFinished(e.error, completeMsg, null);
                            }
                        }
                        break;
                    case 2:
                        SomeArgs args = (SomeArgs) msg.obj;
                        String packageName = (String) args.arg1;
                        String message = (String) args.arg2;
                        Bundle extras = (Bundle) args.arg3;
                        IPackageInstallObserver2 observer = (IPackageInstallObserver2) args.arg4;
                        int returnCode = args.argi1;
                        args.recycle();
                        try {
                            observer.onPackageInstalled(packageName, returnCode, message, extras);
                            break;
                        } catch (RemoteException e2) {
                            break;
                        }
                }
                return true;
            }
        };
        this.mCallback = callback;
        this.mContext = context;
        this.mPm = pm;
        this.mHandler = new Handler(looper, this.mHandlerCallback);
        this.sessionId = sessionId2;
        this.userId = userId2;
        this.mOriginalInstallerUid = i;
        this.mInstallerPackageName = installerPackageName;
        this.mInstallerUid = i;
        this.params = sessionParams;
        this.createdMillis = createdMillis2;
        this.stageDir = file;
        this.stageCid = str;
        if ((file != null ? false : z2) != (str == null ? true : z)) {
            this.mPrepared = prepared;
            if (sealed) {
                synchronized (this.mLock) {
                    try {
                        sealAndValidateLocked();
                    } catch (PackageManagerException | IOException e) {
                        Exception exc = e;
                        destroyInternal();
                        throw new IllegalArgumentException(e);
                    } catch (Throwable th) {
                        throw th;
                    }
                }
            }
            boolean isAdb = (sessionParams.installFlags & 32) != 0;
            if (isAdb) {
                this.mHwPermissionsAccepted = SystemProperties.getBoolean("ro.config.hwRemoveADBMonitor", false) || HwAdbManager.autoPermitInstall();
            }
            long identity2 = Binder.clearCallingIdentity();
            try {
                boolean z3 = isAdb;
                try {
                    this.defaultContainerGid = UserHandle.getSharedAppGid(this.mPm.getPackageUid(PackageManagerService.DEFAULT_CONTAINER_PACKAGE, DumpState.DUMP_DEXOPT, 0));
                    Binder.restoreCallingIdentity(identity2);
                    if ((sessionParams.installFlags & 2048) != 0) {
                        this.mHandler.sendMessage(this.mHandler.obtainMessage(0));
                    }
                } catch (Throwable th2) {
                    th = th2;
                    identity = identity2;
                    Binder.restoreCallingIdentity(identity);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                boolean z4 = isAdb;
                identity = identity2;
                Binder.restoreCallingIdentity(identity);
                throw th;
            }
        } else {
            boolean z5 = prepared;
            throw new IllegalArgumentException("Exactly one of stageDir or stageCid stage must be set");
        }
    }

    public PackageInstaller.SessionInfo generateInfo() {
        return generateInfo(true);
    }

    public PackageInstaller.SessionInfo generateInfo(boolean includeIcon) {
        PackageInstaller.SessionInfo info = new PackageInstaller.SessionInfo();
        synchronized (this.mLock) {
            info.sessionId = this.sessionId;
            info.installerPackageName = this.mInstallerPackageName;
            info.resolvedBaseCodePath = this.mResolvedBaseFile != null ? this.mResolvedBaseFile.getAbsolutePath() : null;
            info.progress = this.mProgress;
            info.sealed = this.mSealed;
            info.active = this.mActiveCount.get() > 0;
            info.mode = this.params.mode;
            info.installReason = this.params.installReason;
            info.sizeBytes = this.params.sizeBytes;
            info.appPackageName = this.params.appPackageName;
            if (includeIcon) {
                info.appIcon = this.params.appIcon;
            }
            info.appLabel = this.params.appLabel;
            info.installLocation = this.params.installLocation;
            info.originatingUri = this.params.originatingUri;
            info.originatingUid = this.params.originatingUid;
            info.referrerUri = this.params.referrerUri;
            info.grantedRuntimePermissions = this.params.grantedRuntimePermissions;
            info.installFlags = this.params.installFlags;
        }
        return info;
    }

    public boolean isPrepared() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mPrepared;
        }
        return z;
    }

    public boolean isSealed() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mSealed;
        }
        return z;
    }

    @GuardedBy("mLock")
    private void assertPreparedAndNotSealedLocked(String cookie) {
        assertPreparedAndNotCommittedOrDestroyedLocked(cookie);
        if (this.mSealed) {
            throw new SecurityException(cookie + " not allowed after sealing");
        }
    }

    @GuardedBy("mLock")
    private void assertPreparedAndNotCommittedOrDestroyedLocked(String cookie) {
        assertPreparedAndNotDestroyedLocked(cookie);
        if (this.mCommitted) {
            throw new SecurityException(cookie + " not allowed after commit");
        }
    }

    @GuardedBy("mLock")
    private void assertPreparedAndNotDestroyedLocked(String cookie) {
        if (!this.mPrepared) {
            throw new IllegalStateException(cookie + " before prepared");
        } else if (this.mDestroyed) {
            throw new SecurityException(cookie + " not allowed after destruction");
        }
    }

    @GuardedBy("mLock")
    private File resolveStageDirLocked() throws IOException {
        if (this.mResolvedStageDir == null) {
            if (this.stageDir != null) {
                this.mResolvedStageDir = this.stageDir;
            } else {
                throw new IOException("Missing stageDir");
            }
        }
        return this.mResolvedStageDir;
    }

    public void setClientProgress(float progress) {
        synchronized (this.mLock) {
            assertCallerIsOwnerOrRootLocked();
            boolean forcePublish = this.mClientProgress == 0.0f;
            this.mClientProgress = progress;
            computeProgressLocked(forcePublish);
        }
    }

    public void addClientProgress(float progress) {
        synchronized (this.mLock) {
            assertCallerIsOwnerOrRootLocked();
            setClientProgress(this.mClientProgress + progress);
        }
    }

    @GuardedBy("mLock")
    private void computeProgressLocked(boolean forcePublish) {
        this.mProgress = MathUtils.constrain(this.mClientProgress * 0.8f, 0.0f, 0.8f) + MathUtils.constrain(this.mInternalProgress * 0.2f, 0.0f, 0.2f);
        if (forcePublish || ((double) Math.abs(this.mProgress - this.mReportedProgress)) >= 0.01d) {
            this.mReportedProgress = this.mProgress;
            this.mCallback.onSessionProgressChanged(this, this.mProgress);
        }
    }

    public String[] getNames() {
        String[] list;
        synchronized (this.mLock) {
            assertCallerIsOwnerOrRootLocked();
            assertPreparedAndNotCommittedOrDestroyedLocked("getNames");
            try {
                list = resolveStageDirLocked().list();
            } catch (IOException e) {
                throw ExceptionUtils.wrap(e);
            }
        }
        return list;
    }

    public void removeSplit(String splitName) {
        if (!TextUtils.isEmpty(this.params.appPackageName)) {
            synchronized (this.mLock) {
                assertCallerIsOwnerOrRootLocked();
                assertPreparedAndNotCommittedOrDestroyedLocked("removeSplit");
                try {
                    createRemoveSplitMarkerLocked(splitName);
                } catch (IOException e) {
                    throw ExceptionUtils.wrap(e);
                }
            }
            return;
        }
        throw new IllegalStateException("Must specify package name to remove a split");
    }

    private void createRemoveSplitMarkerLocked(String splitName) throws IOException {
        try {
            String markerName = splitName + REMOVE_SPLIT_MARKER_EXTENSION;
            if (FileUtils.isValidExtFilename(markerName)) {
                File target = new File(resolveStageDirLocked(), markerName);
                target.createNewFile();
                Os.chmod(target.getAbsolutePath(), 0);
                return;
            }
            throw new IllegalArgumentException("Invalid marker: " + markerName);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    public ParcelFileDescriptor openWrite(String name, long offsetBytes, long lengthBytes) {
        try {
            return doWriteInternal(name, offsetBytes, lengthBytes, null);
        } catch (IOException e) {
            throw ExceptionUtils.wrap(e);
        }
    }

    public void write(String name, long offsetBytes, long lengthBytes, ParcelFileDescriptor fd) {
        try {
            doWriteInternal(name, offsetBytes, lengthBytes, fd);
        } catch (IOException e) {
            throw ExceptionUtils.wrap(e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:61:0x00f2 A[SYNTHETIC, Splitter:B:61:0x00f2] */
    private ParcelFileDescriptor doWriteInternal(String name, long offsetBytes, long lengthBytes, ParcelFileDescriptor incomingFd) throws IOException {
        FileBridge bridge;
        RevocableFileDescriptor fd;
        File stageDir2;
        FileDescriptor targetFd;
        String str = name;
        long j = offsetBytes;
        long identity = lengthBytes;
        synchronized (this.mLock) {
            assertCallerIsOwnerOrRootLocked();
            assertPreparedAndNotSealedLocked("openWrite");
            if (PackageInstaller.ENABLE_REVOCABLE_FD) {
                fd = new RevocableFileDescriptor();
                bridge = null;
                this.mFds.add(fd);
            } else {
                fd = null;
                bridge = new FileBridge();
                this.mBridges.add(bridge);
            }
            stageDir2 = resolveStageDirLocked();
        }
        RevocableFileDescriptor fd2 = fd;
        FileBridge bridge2 = bridge;
        if (FileUtils.isValidExtFilename(name)) {
            long identity2 = Binder.clearCallingIdentity();
            try {
                File target = new File(stageDir2, str);
                Binder.restoreCallingIdentity(identity2);
                FileDescriptor targetFd2 = Os.open(target.getAbsolutePath(), OsConstants.O_CREAT | OsConstants.O_WRONLY, 420);
                Os.chmod(target.getAbsolutePath(), 420);
                if (stageDir2 != null && identity > 0) {
                    ((StorageManager) this.mContext.getSystemService(StorageManager.class)).allocateBytes(targetFd2, identity, PackageHelper.translateAllocateFlags(this.params.installFlags));
                }
                if (j > 0) {
                    Os.lseek(targetFd2, j, OsConstants.SEEK_SET);
                }
                if (incomingFd != null) {
                    int callingUid = Binder.getCallingUid();
                    if (callingUid != 0) {
                        if (callingUid != 2000) {
                            throw new SecurityException("Reverse mode only supported from shell");
                        }
                    }
                    try {
                        Int64Ref last = new Int64Ref(0);
                        targetFd = targetFd2;
                        File file = target;
                        long j2 = identity2;
                        try {
                            FileUtils.copy(incomingFd.getFileDescriptor(), targetFd2, new FileUtils.ProgressListener(last) {
                                private final /* synthetic */ Int64Ref f$1;

                                {
                                    this.f$1 = r2;
                                }

                                public final void onProgress(long j) {
                                    PackageInstallerSession.lambda$doWriteInternal$0(PackageInstallerSession.this, this.f$1, j);
                                }
                            }, null, identity);
                            IoUtils.closeQuietly(targetFd);
                            IoUtils.closeQuietly(incomingFd);
                            synchronized (this.mLock) {
                                try {
                                    if (PackageInstaller.ENABLE_REVOCABLE_FD) {
                                        this.mFds.remove(fd2);
                                    } else {
                                        this.mBridges.remove(bridge2);
                                    }
                                } catch (Throwable th) {
                                    th = th;
                                    throw th;
                                }
                            }
                            return null;
                        } catch (Throwable th2) {
                            th = th2;
                            IoUtils.closeQuietly(targetFd);
                            IoUtils.closeQuietly(incomingFd);
                            synchronized (this.mLock) {
                                try {
                                    if (PackageInstaller.ENABLE_REVOCABLE_FD) {
                                        this.mFds.remove(fd2);
                                    } else {
                                        this.mBridges.remove(bridge2);
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        targetFd = targetFd2;
                        File file2 = target;
                        long j3 = identity2;
                        IoUtils.closeQuietly(targetFd);
                        IoUtils.closeQuietly(incomingFd);
                        synchronized (this.mLock) {
                        }
                        throw th;
                    }
                } else {
                    FileDescriptor targetFd3 = targetFd2;
                    File file3 = target;
                    long j4 = identity2;
                    if (PackageInstaller.ENABLE_REVOCABLE_FD) {
                        fd2.init(this.mContext, targetFd3);
                        return fd2.getRevocableFileDescriptor();
                    }
                    bridge2.setTargetFile(targetFd3);
                    bridge2.start();
                    return new ParcelFileDescriptor(bridge2.getClientSocket());
                }
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            } catch (Throwable th5) {
                Binder.restoreCallingIdentity(identity2);
                throw th5;
            }
        } else {
            throw new IllegalArgumentException("Invalid name: " + str);
        }
    }

    public static /* synthetic */ void lambda$doWriteInternal$0(PackageInstallerSession packageInstallerSession, Int64Ref last, long progress) {
        if (packageInstallerSession.params.sizeBytes > 0) {
            last.value = progress;
            packageInstallerSession.addClientProgress(((float) (progress - last.value)) / ((float) packageInstallerSession.params.sizeBytes));
        }
    }

    public ParcelFileDescriptor openRead(String name) {
        ParcelFileDescriptor openReadInternalLocked;
        synchronized (this.mLock) {
            assertCallerIsOwnerOrRootLocked();
            assertPreparedAndNotCommittedOrDestroyedLocked("openRead");
            try {
                openReadInternalLocked = openReadInternalLocked(name);
            } catch (IOException e) {
                throw ExceptionUtils.wrap(e);
            }
        }
        return openReadInternalLocked;
    }

    private ParcelFileDescriptor openReadInternalLocked(String name) throws IOException {
        try {
            if (FileUtils.isValidExtFilename(name)) {
                return new ParcelFileDescriptor(Os.open(new File(resolveStageDirLocked(), name).getAbsolutePath(), OsConstants.O_RDONLY, 0));
            }
            throw new IllegalArgumentException("Invalid name: " + name);
        } catch (ErrnoException e) {
            throw e.rethrowAsIOException();
        }
    }

    @GuardedBy("mLock")
    private void assertCallerIsOwnerOrRootLocked() {
        int callingUid = Binder.getCallingUid();
        if (callingUid != 0 && callingUid != this.mInstallerUid) {
            throw new SecurityException("Session does not belong to uid " + callingUid);
        }
    }

    @GuardedBy("mLock")
    private void assertNoWriteFileTransfersOpenLocked() {
        Iterator<RevocableFileDescriptor> it = this.mFds.iterator();
        while (it.hasNext()) {
            if (!it.next().isRevoked()) {
                throw new SecurityException("Files still open");
            }
        }
        Iterator<FileBridge> it2 = this.mBridges.iterator();
        while (it2.hasNext()) {
            if (!it2.next().isClosed()) {
                throw new SecurityException("Files still open");
            }
        }
    }

    public void commit(IntentSender statusReceiver, boolean forTransfer) {
        boolean wasSealed;
        Preconditions.checkNotNull(statusReceiver);
        synchronized (this.mLock) {
            assertCallerIsOwnerOrRootLocked();
            assertPreparedAndNotDestroyedLocked("commit");
            PackageInstallerService.PackageInstallObserverAdapter packageInstallObserverAdapter = new PackageInstallerService.PackageInstallObserverAdapter(this.mContext, statusReceiver, this.sessionId, isInstallerDeviceOwnerOrAffiliatedProfileOwnerLocked(), this.userId);
            this.mRemoteObserver = packageInstallObserverAdapter.getBinder();
            if (forTransfer) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.INSTALL_PACKAGES", null);
                if (this.mInstallerUid == this.mOriginalInstallerUid) {
                    throw new IllegalArgumentException("Session has not been transferred");
                }
            } else if (this.mInstallerUid != this.mOriginalInstallerUid) {
                throw new IllegalArgumentException("Session has been transferred");
            }
            wasSealed = this.mSealed;
            if (!this.mSealed) {
                try {
                    sealAndValidateLocked();
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                } catch (PackageManagerException e2) {
                    destroyInternal();
                    dispatchSessionFinished(e2.error, ExceptionUtils.getCompleteMessage(e2), null);
                    return;
                }
            }
            this.mClientProgress = 1.0f;
            computeProgressLocked(true);
            this.mActiveCount.incrementAndGet();
            this.mCommitted = true;
            this.mHandler.obtainMessage(1).sendToTarget();
        }
        if (!wasSealed) {
            this.mCallback.onSessionSealedBlocking(this);
        }
    }

    @GuardedBy("mLock")
    private void sealAndValidateLocked() throws PackageManagerException, IOException {
        assertNoWriteFileTransfersOpenLocked();
        assertPreparedAndNotDestroyedLocked("sealing of session");
        PackageInfo pkgInfo = this.mPm.getPackageInfo(this.params.appPackageName, 67108928, this.userId);
        resolveStageDirLocked();
        this.mSealed = true;
        try {
            validateInstallLocked(pkgInfo);
        } catch (PackageManagerException e) {
            throw e;
        } catch (Throwable e2) {
            throw new PackageManagerException(e2);
        }
    }

    public void transfer(String packageName) {
        Preconditions.checkNotNull(packageName);
        ApplicationInfo newOwnerAppInfo = this.mPm.getApplicationInfo(packageName, 0, this.userId);
        if (newOwnerAppInfo == null) {
            throw new ParcelableException(new PackageManager.NameNotFoundException(packageName));
        } else if (this.mPm.checkUidPermission("android.permission.INSTALL_PACKAGES", newOwnerAppInfo.uid) != 0) {
            throw new SecurityException("Destination package " + packageName + " does not have the " + "android.permission.INSTALL_PACKAGES" + " permission");
        } else if (this.params.areHiddenOptionsSet()) {
            synchronized (this.mLock) {
                assertCallerIsOwnerOrRootLocked();
                assertPreparedAndNotSealedLocked("transfer");
                try {
                    sealAndValidateLocked();
                    if (this.mPackageName.equals(this.mInstallerPackageName)) {
                        this.mInstallerPackageName = packageName;
                        this.mInstallerUid = newOwnerAppInfo.uid;
                    } else {
                        throw new SecurityException("Can only transfer sessions that update the original installer");
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                } catch (PackageManagerException e2) {
                    destroyInternal();
                    dispatchSessionFinished(e2.error, ExceptionUtils.getCompleteMessage(e2), null);
                    throw new IllegalArgumentException("Package is not valid", e2);
                }
            }
            this.mCallback.onSessionSealedBlocking(this);
        } else {
            throw new SecurityException("Can only transfer sessions that use public options");
        }
    }

    /* access modifiers changed from: private */
    @GuardedBy("mLock")
    public void commitLocked() throws PackageManagerException {
        UserHandle userHandle;
        if (this.mDestroyed) {
            throw new PackageManagerException(RequestStatus.SYS_ETIMEDOUT, "Session destroyed");
        } else if (this.mSealed) {
            Preconditions.checkNotNull(this.mPackageName);
            Preconditions.checkNotNull(this.mSigningDetails);
            Preconditions.checkNotNull(this.mResolvedBaseFile);
            if (this.params.hdbEncode != null) {
                if (!HwAdbManager.startHdbVerification(this.params.hdbArgs, this.params.hdbArgIndex, this.params.hdbEncode)) {
                    throw new PackageManagerException(RequestStatus.SYS_ETIMEDOUT, "Failure [INSTALL_HDB_VERIFY_FAILED]");
                }
            } else if (!this.mHwPermissionsAccepted && HwAdbManager.startPackageInstallerForConfirm(this.mContext, this.sessionId)) {
                Slog.d(TAG, "start PackageInstallerActivity success, close current install!");
                closeInternal(false);
                return;
            }
            if (needToAskForPermissionsLocked()) {
                Intent intent = new Intent("android.content.pm.action.CONFIRM_PERMISSIONS");
                intent.setPackage(this.mContext.getPackageManager().getPermissionControllerPackageName());
                intent.putExtra("android.content.pm.extra.SESSION_ID", this.sessionId);
                try {
                    this.mRemoteObserver.onUserActionRequired(intent);
                } catch (RemoteException e) {
                }
                closeInternal(false);
                return;
            }
            if (this.params.mode == 2) {
                try {
                    List<File> fromFiles = this.mResolvedInheritedFiles;
                    File toDir = resolveStageDirLocked();
                    Slog.d(TAG, "Inherited files: " + this.mResolvedInheritedFiles);
                    if (!this.mResolvedInheritedFiles.isEmpty()) {
                        if (this.mInheritedFilesBase == null) {
                            throw new IllegalStateException("mInheritedFilesBase == null");
                        }
                    }
                    if (isLinkPossible(fromFiles, toDir)) {
                        if (!this.mResolvedInstructionSets.isEmpty()) {
                            createOatDirs(this.mResolvedInstructionSets, new File(toDir, "oat"));
                        }
                        if (!this.mResolvedNativeLibPaths.isEmpty()) {
                            for (String libPath : this.mResolvedNativeLibPaths) {
                                int splitIndex = libPath.lastIndexOf(47);
                                if (splitIndex >= 0) {
                                    if (splitIndex < libPath.length() - 1) {
                                        File libDir = new File(toDir, libPath.substring(1, splitIndex));
                                        if (!libDir.exists()) {
                                            NativeLibraryHelper.createNativeLibrarySubdir(libDir);
                                        }
                                        NativeLibraryHelper.createNativeLibrarySubdir(new File(libDir, libPath.substring(splitIndex + 1)));
                                    }
                                }
                                Slog.e(TAG, "Skipping native library creation for linking due to invalid path: " + libPath);
                            }
                        }
                        linkFiles(fromFiles, toDir, this.mInheritedFilesBase);
                    } else {
                        copyFiles(fromFiles, toDir);
                    }
                } catch (IOException e2) {
                    throw new PackageManagerException(-4, "Failed to inherit existing install", e2);
                }
            }
            this.mInternalProgress = 0.5f;
            computeProgressLocked(true);
            extractNativeLibraries(this.mResolvedStageDir, this.params.abiOverride, mayInheritNativeLibs());
            IPackageInstallObserver2 localObserver = new IPackageInstallObserver2.Stub() {
                public void onUserActionRequired(Intent intent) {
                    throw new IllegalStateException();
                }

                public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) {
                    PackageInstallerSession.this.destroyInternal();
                    PackageInstallerSession.this.dispatchSessionFinished(returnCode, msg, extras);
                }
            };
            if ((this.params.installFlags & 64) != 0) {
                userHandle = UserHandle.ALL;
            } else {
                userHandle = new UserHandle(this.userId);
            }
            UserHandle user = userHandle;
            this.mRelinquished = true;
            this.mPm.installStage(this.mPackageName, this.stageDir, localObserver, this.params, this.mInstallerPackageName, this.mInstallerUid, user, this.mSigningDetails);
        } else {
            throw new PackageManagerException(RequestStatus.SYS_ETIMEDOUT, "Session not sealed");
        }
    }

    private static void maybeRenameFile(File from, File to) throws PackageManagerException {
        if (!from.equals(to) && !from.renameTo(to)) {
            throw new PackageManagerException(RequestStatus.SYS_ETIMEDOUT, "Could not rename file " + from + " to " + to);
        }
    }

    private boolean mayInheritNativeLibs() {
        return SystemProperties.getBoolean(PROPERTY_NAME_INHERIT_NATIVE, true) && this.params.mode == 2 && (this.params.installFlags & 1) != 0;
    }

    static int compareAPIMajorVersion(long versionA, long versionB) {
        return Long.compare(versionA / 10000000, versionB / 10000000);
    }

    @GuardedBy("mLock")
    private void validateInstallLocked(PackageInfo pkgInfo) throws PackageManagerException {
        File[] removedFiles;
        ApplicationInfo appInfo;
        File[] libDirs;
        List<File> libDirsToInherit;
        File[] removedFiles2;
        File[] fileArr;
        List<File> libDirsToInherit2;
        PackageInfo pkgInfo2;
        int i;
        File addedFile;
        String targetName;
        String str;
        PackageInfo pkgInfo3 = pkgInfo;
        File file = null;
        this.mPackageName = null;
        this.mVersionCode = -1;
        this.mSigningDetails = PackageParser.SigningDetails.UNKNOWN;
        this.mResolvedBaseFile = null;
        this.mResolvedStagedFiles.clear();
        this.mResolvedInheritedFiles.clear();
        try {
            resolveStageDirLocked();
            File[] removedFiles3 = this.mResolvedStageDir.listFiles(sRemovedFilter);
            List<String> removeSplitList = new ArrayList<>();
            if (!ArrayUtils.isEmpty(removedFiles3)) {
                for (File removedFile : removedFiles3) {
                    String fileName = removedFile.getName();
                    removeSplitList.add(fileName.substring(0, fileName.length() - REMOVE_SPLIT_MARKER_EXTENSION.length()));
                }
            }
            File[] addedFiles = this.mResolvedStageDir.listFiles(sAddedFilter);
            if (!ArrayUtils.isEmpty(addedFiles) || removeSplitList.size() != 0) {
                ArraySet<String> stagedSplits = new ArraySet<>();
                int length = addedFiles.length;
                PackageInfo pluginPkgInfo = null;
                int i2 = 0;
                while (i2 < length) {
                    File addedFile2 = addedFiles[i2];
                    try {
                        PackageParser.ApkLite apk = PackageParser.parseApkLite(addedFile2, 32);
                        if (!apk.isPlugin || this.mPackageName == null) {
                            i = length;
                            addedFile = addedFile2;
                        } else {
                            i = length;
                            File addedFile3 = addedFile2;
                            int versionStatus = compareAPIMajorVersion(apk.getLongVersionCode(), this.mVersionCode);
                            if (versionStatus > 0) {
                                stagedSplits.clear();
                                this.mResolvedStagedFiles.forEach($$Lambda$PackageInstallerSession$W9UfdQnk8WsOPyckE_zXYvseVVs.INSTANCE);
                                this.mResolvedStagedFiles.clear();
                                this.mVersionCode = apk.getLongVersionCode();
                                this.mResolvedBaseFile = file;
                                addedFile = addedFile3;
                            } else if (versionStatus < 0) {
                                if (("split:" + apk.splitName) == null) {
                                    str = "base";
                                } else {
                                    str = apk.splitName + " version:" + apk.getLongVersionCode() + " is incompatible with current version: " + this.mVersionCode;
                                }
                                Slog.w(TAG, str);
                                addedFile3.delete();
                                i2++;
                                length = i;
                                file = null;
                            } else {
                                addedFile = addedFile3;
                            }
                        }
                        if (stagedSplits.add(apk.splitName)) {
                            if (this.mPackageName == null) {
                                this.mPackageName = apk.packageName;
                                this.mVersionCode = apk.getLongVersionCode();
                            }
                            if (this.mSigningDetails == PackageParser.SigningDetails.UNKNOWN) {
                                this.mSigningDetails = apk.signingDetails;
                            }
                            assertApkConsistentLocked(String.valueOf(addedFile), apk);
                            if (apk.splitName == null) {
                                targetName = "base.apk";
                                if (apk.isPlugin && apk.packageName != null && this.params.mode == 1) {
                                    pluginPkgInfo = this.mPm.getPackageInfo(apk.packageName, 67108928, this.userId);
                                    Slog.d(TAG, "Plugin packageName: " + apk.packageName + " pluginPkgInfo: " + pluginPkgInfo);
                                    if (pluginPkgInfo != null) {
                                        this.params.mode = 2;
                                        Slog.d(TAG, "Plugin Base, change mode to inherit!");
                                    }
                                }
                            } else {
                                targetName = "split_" + apk.splitName + ".apk";
                            }
                            String targetName2 = targetName;
                            if (FileUtils.isValidExtFilename(targetName2)) {
                                File targetFile = new File(this.mResolvedStageDir, targetName2);
                                maybeRenameFile(addedFile, targetFile);
                                if (apk.splitName == null) {
                                    this.mResolvedBaseFile = targetFile;
                                }
                                this.mResolvedStagedFiles.add(targetFile);
                                File dexMetadataFile = DexMetadataHelper.findDexMetadataForFile(addedFile);
                                if (dexMetadataFile == null) {
                                    continue;
                                } else if (FileUtils.isValidExtFilename(dexMetadataFile.getName())) {
                                    File targetDexMetadataFile = new File(this.mResolvedStageDir, DexMetadataHelper.buildDexMetadataPathForApk(targetName2));
                                    this.mResolvedStagedFiles.add(targetDexMetadataFile);
                                    maybeRenameFile(dexMetadataFile, targetDexMetadataFile);
                                } else {
                                    throw new PackageManagerException(-2, "Invalid filename: " + dexMetadataFile);
                                }
                                i2++;
                                length = i;
                                file = null;
                            } else {
                                throw new PackageManagerException(-2, "Invalid filename: " + targetName2);
                            }
                        } else {
                            throw new PackageManagerException(-2, "Split " + apk.splitName + " was defined multiple times");
                        }
                    } catch (PackageParser.PackageParserException e) {
                        File file2 = addedFile2;
                        PackageParser.PackageParserException packageParserException = e;
                        throw PackageManagerException.from(e);
                    }
                }
                if (removeSplitList.size() > 0) {
                    if (pkgInfo3 != null) {
                        for (String splitName : removeSplitList) {
                            if (!ArrayUtils.contains(pkgInfo3.splitNames, splitName)) {
                                throw new PackageManagerException(-2, "Split not found: " + splitName);
                            }
                        }
                        if (this.mPackageName == null) {
                            this.mPackageName = pkgInfo3.packageName;
                            this.mVersionCode = pkgInfo.getLongVersionCode();
                        }
                        if (this.mSigningDetails == PackageParser.SigningDetails.UNKNOWN) {
                            try {
                                this.mSigningDetails = ApkSignatureVerifier.plsCertsNoVerifyOnlyCerts(pkgInfo3.applicationInfo.sourceDir, 1);
                            } catch (PackageParser.PackageParserException e2) {
                                throw new PackageManagerException(-2, "Couldn't obtain signatures from base APK");
                            }
                        }
                    } else {
                        throw new PackageManagerException(-2, "Missing existing base package for " + this.mPackageName);
                    }
                }
                if (this.params.mode != 1) {
                    if (pkgInfo3 == null) {
                        pkgInfo3 = pluginPkgInfo;
                    }
                    if (pkgInfo3 == null || pkgInfo3.applicationInfo == null) {
                        File[] fileArr2 = removedFiles3;
                        PackageInfo packageInfo = pluginPkgInfo;
                        throw new PackageManagerException(-2, "Missing existing base package for " + this.mPackageName);
                    }
                    ApplicationInfo appInfo2 = pkgInfo3.applicationInfo;
                    try {
                        PackageParser.PackageLite existing = PackageParser.parsePackageLite(new File(appInfo2.getCodePath()), 0);
                        PackageParser.ApkLite existingBase = PackageParser.parseApkLite(new File(appInfo2.getBaseCodePath()), 32);
                        if (existingBase.isPlugin) {
                            PackageInfo packageInfo2 = pluginPkgInfo;
                            int versionStatus2 = compareAPIMajorVersion(existingBase.getLongVersionCode(), this.mVersionCode);
                            if (versionStatus2 > 0) {
                                throw new PackageManagerException(-2, "Plugin install version:" + this.mVersionCode + " is incompatible with install version:" + existingBase.getLongVersionCode());
                            } else if (versionStatus2 < 0) {
                                Slog.w(TAG, "existingBase version:" + existingBase.getLongVersionCode() + " is incompatible with install version: " + this.mVersionCode + " will drop!");
                                if (this.mResolvedBaseFile == null) {
                                    throw new PackageManagerException(-2, "Plugin install must include a base package");
                                }
                                return;
                            }
                        }
                        assertApkConsistentLocked("Existing base", existingBase);
                        if (this.mResolvedBaseFile == null) {
                            this.mResolvedBaseFile = new File(appInfo2.getBaseCodePath());
                            this.mResolvedInheritedFiles.add(this.mResolvedBaseFile);
                            File baseDexMetadataFile = DexMetadataHelper.findDexMetadataForFile(this.mResolvedBaseFile);
                            if (baseDexMetadataFile != null) {
                                this.mResolvedInheritedFiles.add(baseDexMetadataFile);
                            }
                        }
                        if (!ArrayUtils.isEmpty(existing.splitNames)) {
                            for (int i3 = 0; i3 < existing.splitNames.length; i3++) {
                                String splitName2 = existing.splitNames[i3];
                                File splitFile = new File(existing.splitCodePaths[i3]);
                                boolean splitRemoved = removeSplitList.contains(splitName2);
                                if (!stagedSplits.contains(splitName2) && !splitRemoved) {
                                    this.mResolvedInheritedFiles.add(splitFile);
                                    File splitDexMetadataFile = DexMetadataHelper.findDexMetadataForFile(splitFile);
                                    if (splitDexMetadataFile != null) {
                                        this.mResolvedInheritedFiles.add(splitDexMetadataFile);
                                    }
                                }
                            }
                        }
                        File packageInstallDir = new File(appInfo2.getBaseCodePath()).getParentFile();
                        this.mInheritedFilesBase = packageInstallDir;
                        File oatDir = new File(packageInstallDir, "oat");
                        if (oatDir.exists()) {
                            File[] archSubdirs = oatDir.listFiles();
                            if (archSubdirs != null && archSubdirs.length > 0) {
                                String[] instructionSets = InstructionSets.getAllDexCodeInstructionSets();
                                int length2 = archSubdirs.length;
                                int i4 = 0;
                                while (i4 < length2) {
                                    File archSubDir = archSubdirs[i4];
                                    File[] archSubdirs2 = archSubdirs;
                                    if (!ArrayUtils.contains(instructionSets, archSubDir.getName())) {
                                        pkgInfo2 = pkgInfo3;
                                    } else {
                                        pkgInfo2 = pkgInfo3;
                                        this.mResolvedInstructionSets.add(archSubDir.getName());
                                        List<File> oatFiles = Arrays.asList(archSubDir.listFiles());
                                        if (!oatFiles.isEmpty()) {
                                            this.mResolvedInheritedFiles.addAll(oatFiles);
                                        }
                                    }
                                    i4++;
                                    archSubdirs = archSubdirs2;
                                    pkgInfo3 = pkgInfo2;
                                }
                            }
                        }
                        PackageInfo pkgInfo4 = pkgInfo3;
                        if (mayInheritNativeLibs() && removeSplitList.isEmpty()) {
                            File[] libDirs2 = {new File(packageInstallDir, "lib"), new File(packageInstallDir, "lib64")};
                            int length3 = libDirs2.length;
                            int i5 = 0;
                            while (i5 < length3) {
                                File libDir = libDirs2[i5];
                                if (!libDir.exists()) {
                                    libDirs = libDirs2;
                                    appInfo = appInfo2;
                                    removedFiles = removedFiles3;
                                } else if (!libDir.isDirectory()) {
                                    libDirs = libDirs2;
                                    appInfo = appInfo2;
                                    removedFiles = removedFiles3;
                                } else {
                                    List<File> libDirsToInherit3 = new LinkedList<>();
                                    File[] listFiles = libDir.listFiles();
                                    int length4 = listFiles.length;
                                    libDirs = libDirs2;
                                    int i6 = 0;
                                    while (i6 < length4) {
                                        appInfo = appInfo2;
                                        File archSubDir2 = listFiles[i6];
                                        if (!archSubDir2.isDirectory()) {
                                            fileArr = listFiles;
                                            removedFiles2 = removedFiles3;
                                            libDirsToInherit2 = libDirsToInherit3;
                                        } else {
                                            try {
                                                fileArr = listFiles;
                                                removedFiles2 = removedFiles3;
                                                String relLibPath = getRelativePath(archSubDir2, packageInstallDir);
                                                if (!this.mResolvedNativeLibPaths.contains(relLibPath)) {
                                                    this.mResolvedNativeLibPaths.add(relLibPath);
                                                }
                                                File file3 = archSubDir2;
                                                libDirsToInherit2 = libDirsToInherit3;
                                                libDirsToInherit2.addAll(Arrays.asList(archSubDir2.listFiles()));
                                            } catch (IOException e3) {
                                                File file4 = archSubDir2;
                                                removedFiles = removedFiles3;
                                                libDirsToInherit = libDirsToInherit3;
                                                IOException iOException = e3;
                                                Slog.e(TAG, "Skipping linking of native library directory!", e3);
                                                libDirsToInherit.clear();
                                            }
                                        }
                                        i6++;
                                        libDirsToInherit3 = libDirsToInherit2;
                                        appInfo2 = appInfo;
                                        listFiles = fileArr;
                                        removedFiles3 = removedFiles2;
                                    }
                                    appInfo = appInfo2;
                                    removedFiles = removedFiles3;
                                    libDirsToInherit = libDirsToInherit3;
                                    this.mResolvedInheritedFiles.addAll(libDirsToInherit);
                                }
                                i5++;
                                libDirs2 = libDirs;
                                appInfo2 = appInfo;
                                removedFiles3 = removedFiles;
                            }
                        }
                        PackageInfo packageInfo3 = pkgInfo4;
                    } catch (PackageParser.PackageParserException e4) {
                        PackageInfo packageInfo4 = pkgInfo3;
                        ApplicationInfo applicationInfo = appInfo2;
                        File[] fileArr3 = removedFiles3;
                        PackageInfo packageInfo5 = pluginPkgInfo;
                        throw PackageManagerException.from(e4);
                    }
                } else if (stagedSplits.contains(null)) {
                    File[] fileArr4 = removedFiles3;
                    PackageInfo packageInfo6 = pluginPkgInfo;
                } else {
                    throw new PackageManagerException(-2, "Full install must include a base package");
                }
                return;
            }
            throw new PackageManagerException(-2, "No packages staged");
        } catch (IOException e5) {
            IOException iOException2 = e5;
            throw new PackageManagerException(-18, "Failed to resolve stage location", e5);
        }
    }

    @GuardedBy("mLock")
    private void assertApkConsistentLocked(String tag, PackageParser.ApkLite apk) throws PackageManagerException {
        if (!this.mPackageName.equals(apk.packageName)) {
            throw new PackageManagerException(-2, tag + " package " + apk.packageName + " inconsistent with " + this.mPackageName);
        } else if (this.params.appPackageName != null && !this.params.appPackageName.equals(apk.packageName)) {
            throw new PackageManagerException(-2, tag + " specified package " + this.params.appPackageName + " inconsistent with " + apk.packageName);
        } else if (this.mVersionCode != apk.getLongVersionCode() && !apk.isPlugin) {
            throw new PackageManagerException(-2, tag + " version code " + apk.versionCode + " inconsistent with " + this.mVersionCode);
        } else if (!this.mSigningDetails.signaturesMatchExactly(apk.signingDetails)) {
            throw new PackageManagerException(-2, tag + " signatures are inconsistent");
        }
    }

    private boolean isLinkPossible(List<File> fromFiles, File toDir) {
        try {
            StructStat toStat = Os.stat(toDir.getAbsolutePath());
            for (File fromFile : fromFiles) {
                if (Os.stat(fromFile.getAbsolutePath()).st_dev != toStat.st_dev) {
                    return false;
                }
            }
            return true;
        } catch (ErrnoException e) {
            Slog.w(TAG, "Failed to detect if linking possible: " + e);
            return false;
        }
    }

    public int getInstallerUid() {
        int i;
        synchronized (this.mLock) {
            i = this.mInstallerUid;
        }
        return i;
    }

    private static String getRelativePath(File file, File base) throws IOException {
        String pathStr = file.getAbsolutePath();
        String baseStr = base.getAbsolutePath();
        if (pathStr.contains("/.")) {
            throw new IOException("Invalid path (was relative) : " + pathStr);
        } else if (pathStr.startsWith(baseStr)) {
            return pathStr.substring(baseStr.length());
        } else {
            throw new IOException("File: " + pathStr + " outside base: " + baseStr);
        }
    }

    private void createOatDirs(List<String> instructionSets, File fromDir) throws PackageManagerException {
        for (String instructionSet : instructionSets) {
            try {
                this.mPm.mInstaller.createOatDir(fromDir.getAbsolutePath(), instructionSet);
            } catch (Installer.InstallerException e) {
                throw PackageManagerException.from(e);
            }
        }
    }

    private void linkFiles(List<File> fromFiles, File toDir, File fromDir) throws IOException {
        for (File fromFile : fromFiles) {
            String relativePath = getRelativePath(fromFile, fromDir);
            try {
                this.mPm.mInstaller.linkFile(relativePath, fromDir.getAbsolutePath(), toDir.getAbsolutePath());
            } catch (Installer.InstallerException e) {
                throw new IOException("failed linkOrCreateDir(" + relativePath + ", " + fromDir + ", " + toDir + ")", e);
            }
        }
        Slog.d(TAG, "Linked " + fromFiles.size() + " files into " + toDir);
    }

    private static void copyFiles(List<File> fromFiles, File toDir) throws IOException {
        File toFile;
        for (File file : toDir.listFiles()) {
            if (file.getName().endsWith(".tmp")) {
                file.delete();
            }
        }
        Iterator<File> it = fromFiles.iterator();
        while (it.hasNext()) {
            File fromFile = it.next();
            File tmpFile = File.createTempFile("inherit", ".tmp", toDir);
            Slog.d(TAG, "Copying " + fromFile + " to " + tmpFile);
            if (FileUtils.copyFile(fromFile, tmpFile)) {
                try {
                    Os.chmod(tmpFile.getAbsolutePath(), 420);
                    Slog.d(TAG, "Renaming " + tmpFile + " to " + toFile);
                    if (!tmpFile.renameTo(toFile)) {
                        throw new IOException("Failed to rename " + tmpFile + " to " + toFile);
                    }
                } catch (ErrnoException e) {
                    throw new IOException("Failed to chmod " + tmpFile);
                }
            } else {
                throw new IOException("Failed to copy " + fromFile + " to " + tmpFile);
            }
        }
        Slog.d(TAG, "Copied " + fromFiles.size() + " files into " + toDir);
    }

    private static void extractNativeLibraries(File packageDir, String abiOverride, boolean inherit) throws PackageManagerException {
        File libDir = new File(packageDir, "lib");
        if (!inherit) {
            NativeLibraryHelper.removeNativeBinariesFromDirLI(libDir, true);
        }
        NativeLibraryHelper.Handle handle = null;
        try {
            handle = NativeLibraryHelper.Handle.create(packageDir);
            int res = NativeLibraryHelper.copyNativeBinariesWithOverride(handle, libDir, abiOverride);
            if (res == 1) {
                IoUtils.closeQuietly(handle);
                return;
            }
            throw new PackageManagerException(res, "Failed to extract native libraries, res=" + res);
        } catch (IOException e) {
            throw new PackageManagerException(RequestStatus.SYS_ETIMEDOUT, "Failed to extract native libraries", e);
        } catch (Throwable th) {
            IoUtils.closeQuietly(handle);
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public void setPermissionsResult(boolean accepted) {
        if (!this.mSealed) {
            throw new SecurityException("Must be sealed to accept permissions");
        } else if (accepted) {
            synchronized (this.mLock) {
                this.mHwPermissionsAccepted = true;
                this.mPermissionsManuallyAccepted = true;
                this.mHandler.obtainMessage(1).sendToTarget();
            }
        } else {
            destroyInternal();
            dispatchSessionFinished(-115, "User rejected permissions", null);
        }
    }

    public void open() throws IOException {
        boolean wasPrepared;
        if (this.mActiveCount.getAndIncrement() == 0) {
            this.mCallback.onSessionActiveChanged(this, true);
        }
        synchronized (this.mLock) {
            wasPrepared = this.mPrepared;
            if (!this.mPrepared) {
                if (this.stageDir != null) {
                    PackageInstallerService.prepareStageDir(this.stageDir);
                    this.mPrepared = true;
                } else {
                    throw new IllegalArgumentException("stageDir must be set");
                }
            }
        }
        if (!wasPrepared) {
            this.mCallback.onSessionPrepared(this);
        }
    }

    public void close() {
        closeInternal(true);
    }

    private void closeInternal(boolean checkCaller) {
        int activeCount;
        synchronized (this.mLock) {
            if (checkCaller) {
                try {
                    assertCallerIsOwnerOrRootLocked();
                } catch (Throwable th) {
                    while (true) {
                        throw th;
                    }
                }
            }
            activeCount = this.mActiveCount.decrementAndGet();
        }
        if (activeCount == 0) {
            this.mCallback.onSessionActiveChanged(this, false);
        }
    }

    public void abandon() {
        synchronized (this.mLock) {
            assertCallerIsOwnerOrRootLocked();
            if (this.mRelinquished) {
                Slog.d(TAG, "Ignoring abandon after commit relinquished control");
                return;
            }
            destroyInternal();
            dispatchSessionFinished(-115, "Session was abandoned", null);
        }
    }

    /* access modifiers changed from: private */
    public void dispatchSessionFinished(int returnCode, String msg, Bundle extras) {
        IPackageInstallObserver2 observer;
        String packageName;
        synchronized (this.mLock) {
            this.mFinalStatus = returnCode;
            this.mFinalMessage = msg;
            observer = this.mRemoteObserver;
            packageName = this.mPackageName;
        }
        if (observer != null) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = packageName;
            args.arg2 = msg;
            args.arg3 = extras;
            args.arg4 = observer;
            args.argi1 = returnCode;
            this.mHandler.obtainMessage(2, args).sendToTarget();
        }
        boolean isNewInstall = false;
        boolean success = returnCode == 1;
        if (extras == null || !extras.getBoolean("android.intent.extra.REPLACING")) {
            isNewInstall = true;
        }
        if (success && isNewInstall) {
            this.mPm.sendSessionCommitBroadcast(generateInfo(), this.userId);
        }
        this.mCallback.onSessionFinished(this, success);
    }

    /* access modifiers changed from: private */
    public void destroyInternal() {
        synchronized (this.mLock) {
            this.mSealed = true;
            this.mDestroyed = true;
            Iterator<RevocableFileDescriptor> it = this.mFds.iterator();
            while (it.hasNext()) {
                it.next().revoke();
            }
            Iterator<FileBridge> it2 = this.mBridges.iterator();
            while (it2.hasNext()) {
                it2.next().forceClose();
            }
        }
        if (this.stageDir != null) {
            try {
                this.mPm.mInstaller.rmPackageDir(this.stageDir.getAbsolutePath());
            } catch (Installer.InstallerException e) {
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            dumpLocked(pw);
        }
    }

    @GuardedBy("mLock")
    private void dumpLocked(IndentingPrintWriter pw) {
        pw.println("Session " + this.sessionId + ":");
        pw.increaseIndent();
        pw.printPair(ATTR_USER_ID, Integer.valueOf(this.userId));
        pw.printPair("mOriginalInstallerUid", Integer.valueOf(this.mOriginalInstallerUid));
        pw.printPair("mInstallerPackageName", this.mInstallerPackageName);
        pw.printPair("mInstallerUid", Integer.valueOf(this.mInstallerUid));
        pw.printPair(ATTR_CREATED_MILLIS, Long.valueOf(this.createdMillis));
        pw.printPair("stageDir", this.stageDir);
        pw.printPair("stageCid", this.stageCid);
        pw.println();
        this.params.dump(pw);
        pw.printPair("mClientProgress", Float.valueOf(this.mClientProgress));
        pw.printPair("mProgress", Float.valueOf(this.mProgress));
        pw.printPair("mSealed", Boolean.valueOf(this.mSealed));
        pw.printPair("mPermissionsManuallyAccepted", Boolean.valueOf(this.mPermissionsManuallyAccepted));
        pw.printPair("mRelinquished", Boolean.valueOf(this.mRelinquished));
        pw.printPair("mDestroyed", Boolean.valueOf(this.mDestroyed));
        pw.printPair("mFds", Integer.valueOf(this.mFds.size()));
        pw.printPair("mBridges", Integer.valueOf(this.mBridges.size()));
        pw.printPair("mFinalStatus", Integer.valueOf(this.mFinalStatus));
        pw.printPair("mFinalMessage", this.mFinalMessage);
        pw.println();
        pw.decreaseIndent();
    }

    private static void writeGrantedRuntimePermissionsLocked(XmlSerializer out, String[] grantedRuntimePermissions) throws IOException {
        if (grantedRuntimePermissions != null) {
            for (String permission : grantedRuntimePermissions) {
                out.startTag(null, TAG_GRANTED_RUNTIME_PERMISSION);
                XmlUtils.writeStringAttribute(out, "name", permission);
                out.endTag(null, TAG_GRANTED_RUNTIME_PERMISSION);
            }
        }
    }

    private static File buildAppIconFile(int sessionId2, File sessionsDir) {
        return new File(sessionsDir, "app_icon." + sessionId2 + ".png");
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x016a, code lost:
        r9.endTag(null, TAG_SESSION);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0170, code lost:
        return;
     */
    public void write(XmlSerializer out, File sessionsDir) throws IOException {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                out.startTag(null, TAG_SESSION);
                XmlUtils.writeIntAttribute(out, ATTR_SESSION_ID, this.sessionId);
                XmlUtils.writeIntAttribute(out, ATTR_USER_ID, this.userId);
                XmlUtils.writeStringAttribute(out, ATTR_INSTALLER_PACKAGE_NAME, this.mInstallerPackageName);
                XmlUtils.writeIntAttribute(out, ATTR_INSTALLER_UID, this.mInstallerUid);
                XmlUtils.writeLongAttribute(out, ATTR_CREATED_MILLIS, this.createdMillis);
                if (this.stageDir != null) {
                    XmlUtils.writeStringAttribute(out, ATTR_SESSION_STAGE_DIR, this.stageDir.getAbsolutePath());
                }
                if (this.stageCid != null) {
                    XmlUtils.writeStringAttribute(out, ATTR_SESSION_STAGE_CID, this.stageCid);
                }
                XmlUtils.writeBooleanAttribute(out, ATTR_PREPARED, isPrepared());
                XmlUtils.writeBooleanAttribute(out, ATTR_SEALED, isSealed());
                XmlUtils.writeIntAttribute(out, ATTR_MODE, this.params.mode);
                XmlUtils.writeIntAttribute(out, ATTR_INSTALL_FLAGS, this.params.installFlags);
                XmlUtils.writeIntAttribute(out, ATTR_INSTALL_LOCATION, this.params.installLocation);
                XmlUtils.writeLongAttribute(out, ATTR_SIZE_BYTES, this.params.sizeBytes);
                XmlUtils.writeStringAttribute(out, ATTR_APP_PACKAGE_NAME, this.params.appPackageName);
                XmlUtils.writeStringAttribute(out, ATTR_APP_LABEL, this.params.appLabel);
                XmlUtils.writeUriAttribute(out, ATTR_ORIGINATING_URI, this.params.originatingUri);
                XmlUtils.writeIntAttribute(out, ATTR_ORIGINATING_UID, this.params.originatingUid);
                XmlUtils.writeUriAttribute(out, ATTR_REFERRER_URI, this.params.referrerUri);
                XmlUtils.writeStringAttribute(out, ATTR_ABI_OVERRIDE, this.params.abiOverride);
                XmlUtils.writeStringAttribute(out, ATTR_VOLUME_UUID, this.params.volumeUuid);
                XmlUtils.writeIntAttribute(out, ATTR_INSTALL_REASON, this.params.installReason);
                writeGrantedRuntimePermissionsLocked(out, this.params.grantedRuntimePermissions);
                File appIconFile = buildAppIconFile(this.sessionId, sessionsDir);
                if (this.params.appIcon == null && appIconFile.exists()) {
                    appIconFile.delete();
                } else if (!(this.params.appIcon == null || appIconFile.lastModified() == this.params.appIconLastModified)) {
                    Slog.w(TAG, "Writing changed icon " + appIconFile);
                    FileOutputStream os = null;
                    try {
                        os = new FileOutputStream(appIconFile);
                        this.params.appIcon.compress(Bitmap.CompressFormat.PNG, 90, os);
                        IoUtils.closeQuietly(os);
                    } catch (IOException e) {
                        try {
                            Slog.w(TAG, "Failed to write icon " + appIconFile + ": " + e.getMessage());
                            IoUtils.closeQuietly(os);
                        } catch (Throwable th) {
                            IoUtils.closeQuietly(os);
                            throw th;
                        }
                    }
                    this.params.appIconLastModified = appIconFile.lastModified();
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003d  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003f  */
    private static String[] readGrantedRuntimePermissions(XmlPullParser in) throws IOException, XmlPullParserException {
        List<String> permissions = null;
        int outerDepth = in.getDepth();
        while (true) {
            int next = in.next();
            int type = next;
            if (next == 1 || (type == 3 && in.getDepth() <= outerDepth)) {
                if (permissions != null) {
                    return null;
                }
                String[] permissionsArray = new String[permissions.size()];
                permissions.toArray(permissionsArray);
                return permissionsArray;
            } else if (!(type == 3 || type == 4 || !TAG_GRANTED_RUNTIME_PERMISSION.equals(in.getName()))) {
                String permission = XmlUtils.readStringAttribute(in, "name");
                if (permissions == null) {
                    permissions = new ArrayList<>();
                }
                permissions.add(permission);
            }
        }
        if (permissions != null) {
        }
    }

    public static PackageInstallerSession readFromXml(XmlPullParser in, PackageInstallerService.InternalCallback callback, Context context, PackageManagerService pm, Looper installerThread, File sessionsDir) throws IOException, XmlPullParserException {
        XmlPullParser xmlPullParser = in;
        int sessionId2 = XmlUtils.readIntAttribute(xmlPullParser, ATTR_SESSION_ID);
        int userId2 = XmlUtils.readIntAttribute(xmlPullParser, ATTR_USER_ID);
        String installerPackageName = XmlUtils.readStringAttribute(xmlPullParser, ATTR_INSTALLER_PACKAGE_NAME);
        PackageManagerService packageManagerService = pm;
        int installerUid = XmlUtils.readIntAttribute(xmlPullParser, ATTR_INSTALLER_UID, packageManagerService.getPackageUid(installerPackageName, 8192, userId2));
        long createdMillis2 = XmlUtils.readLongAttribute(xmlPullParser, ATTR_CREATED_MILLIS);
        String stageDirRaw = XmlUtils.readStringAttribute(xmlPullParser, ATTR_SESSION_STAGE_DIR);
        File stageDir2 = stageDirRaw != null ? new File(stageDirRaw) : null;
        String stageCid2 = XmlUtils.readStringAttribute(xmlPullParser, ATTR_SESSION_STAGE_CID);
        boolean prepared = XmlUtils.readBooleanAttribute(xmlPullParser, ATTR_PREPARED, true);
        boolean sealed = XmlUtils.readBooleanAttribute(xmlPullParser, ATTR_SEALED);
        PackageInstaller.SessionParams params2 = new PackageInstaller.SessionParams(-1);
        params2.mode = XmlUtils.readIntAttribute(xmlPullParser, ATTR_MODE);
        params2.installFlags = XmlUtils.readIntAttribute(xmlPullParser, ATTR_INSTALL_FLAGS);
        params2.installLocation = XmlUtils.readIntAttribute(xmlPullParser, ATTR_INSTALL_LOCATION);
        params2.sizeBytes = XmlUtils.readLongAttribute(xmlPullParser, ATTR_SIZE_BYTES);
        params2.appPackageName = XmlUtils.readStringAttribute(xmlPullParser, ATTR_APP_PACKAGE_NAME);
        params2.appIcon = XmlUtils.readBitmapAttribute(xmlPullParser, ATTR_APP_ICON);
        params2.appLabel = XmlUtils.readStringAttribute(xmlPullParser, ATTR_APP_LABEL);
        params2.originatingUri = XmlUtils.readUriAttribute(xmlPullParser, ATTR_ORIGINATING_URI);
        params2.originatingUid = XmlUtils.readIntAttribute(xmlPullParser, ATTR_ORIGINATING_UID, -1);
        params2.referrerUri = XmlUtils.readUriAttribute(xmlPullParser, ATTR_REFERRER_URI);
        params2.abiOverride = XmlUtils.readStringAttribute(xmlPullParser, ATTR_ABI_OVERRIDE);
        params2.volumeUuid = XmlUtils.readStringAttribute(xmlPullParser, ATTR_VOLUME_UUID);
        params2.installReason = XmlUtils.readIntAttribute(xmlPullParser, ATTR_INSTALL_REASON);
        params2.grantedRuntimePermissions = readGrantedRuntimePermissions(in);
        File appIconFile = buildAppIconFile(sessionId2, sessionsDir);
        if (appIconFile.exists()) {
            params2.appIcon = BitmapFactory.decodeFile(appIconFile.getAbsolutePath());
            params2.appIconLastModified = appIconFile.lastModified();
        }
        File file = appIconFile;
        String str = stageDirRaw;
        String str2 = installerPackageName;
        int i = userId2;
        PackageInstallerSession packageInstallerSession = new PackageInstallerSession(callback, context, packageManagerService, installerThread, sessionId2, userId2, installerPackageName, installerUid, params2, createdMillis2, stageDir2, stageCid2, prepared, sealed);
        return packageInstallerSession;
    }
}
