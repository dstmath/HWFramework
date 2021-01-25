package com.android.server.pm;

import android.app.admin.DevicePolicyEventLogger;
import android.app.admin.DevicePolicyManagerInternal;
import android.content.Context;
import android.content.IIntentReceiver;
import android.content.IIntentSender;
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
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.ParcelableException;
import android.os.RemoteException;
import android.os.RevocableFileDescriptor;
import android.os.SELinux;
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
import android.util.SparseIntArray;
import android.util.apk.ApkSignatureVerifier;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.content.PackageHelper;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.internal.util.Preconditions;
import com.android.internal.util.XmlUtils;
import com.android.server.HwServiceExFactory;
import com.android.server.LocalServices;
import com.android.server.pm.Installer;
import com.android.server.pm.PackageInstallerService;
import com.android.server.pm.PackageInstallerSession;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.dex.DexManager;
import com.android.server.security.VerityUtils;
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
    private static final String ATTR_COMMITTED = "committed";
    private static final String ATTR_CREATED_MILLIS = "createdMillis";
    private static final String ATTR_INSTALLER_PACKAGE_NAME = "installerPackageName";
    private static final String ATTR_INSTALLER_UID = "installerUid";
    private static final String ATTR_INSTALL_FLAGS = "installFlags";
    private static final String ATTR_INSTALL_LOCATION = "installLocation";
    private static final String ATTR_INSTALL_REASON = "installRason";
    private static final String ATTR_IS_APPLIED = "isApplied";
    private static final String ATTR_IS_FAILED = "isFailed";
    private static final String ATTR_IS_READY = "isReady";
    private static final String ATTR_MODE = "mode";
    private static final String ATTR_MULTI_PACKAGE = "multiPackage";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_ORIGINATING_UID = "originatingUid";
    private static final String ATTR_ORIGINATING_URI = "originatingUri";
    private static final String ATTR_PARENT_SESSION_ID = "parentSessionId";
    private static final String ATTR_PREPARED = "prepared";
    private static final String ATTR_REFERRER_URI = "referrerUri";
    private static final String ATTR_SEALED = "sealed";
    private static final String ATTR_SESSION_ID = "sessionId";
    private static final String ATTR_SESSION_STAGE_CID = "sessionStageCid";
    private static final String ATTR_SESSION_STAGE_DIR = "sessionStageDir";
    private static final String ATTR_SIZE_BYTES = "sizeBytes";
    private static final String ATTR_STAGED_SESSION = "stagedSession";
    private static final String ATTR_STAGED_SESSION_ERROR_CODE = "errorCode";
    private static final String ATTR_STAGED_SESSION_ERROR_MESSAGE = "errorMessage";
    private static final String ATTR_UPDATED_MILLIS = "updatedMillis";
    private static final String ATTR_USER_ID = "userId";
    private static final String ATTR_VOLUME_UUID = "volumeUuid";
    private static final int[] EMPTY_CHILD_SESSION_ARRAY = new int[0];
    private static final boolean LOGD = true;
    private static final int MSG_COMMIT = 1;
    private static final int MSG_ON_PACKAGE_INSTALLED = 2;
    private static final String PROPERTY_NAME_INHERIT_NATIVE = "pi.inherit_native_on_dont_kill";
    private static final String REMOVE_SPLIT_MARKER_EXTENSION = ".removed";
    private static final String TAG = "PackageInstallerSession";
    static final String TAG_CHILD_SESSION = "childSession";
    private static final String TAG_GRANTED_RUNTIME_PERMISSION = "granted-runtime-permission";
    static final String TAG_SESSION = "session";
    private static final String TAG_WHITELISTED_RESTRICTED_PERMISSION = "whitelisted-restricted-permission";
    private static final FileFilter sAddedFilter = new FileFilter() {
        /* class com.android.server.pm.PackageInstallerSession.AnonymousClass1 */

        @Override // java.io.FileFilter
        public boolean accept(File file) {
            if (!file.isDirectory() && !file.getName().endsWith(PackageInstallerSession.REMOVE_SPLIT_MARKER_EXTENSION) && !DexMetadataHelper.isDexMetadataFile(file) && !VerityUtils.isFsveritySignatureFile(file)) {
                return true;
            }
            return false;
        }
    };
    private static final FileFilter sRemovedFilter = new FileFilter() {
        /* class com.android.server.pm.PackageInstallerSession.AnonymousClass2 */

        @Override // java.io.FileFilter
        public boolean accept(File file) {
            if (!file.isDirectory() && file.getName().endsWith(PackageInstallerSession.REMOVE_SPLIT_MARKER_EXTENSION)) {
                return true;
            }
            return false;
        }
    };
    final long createdMillis;
    private final AtomicInteger mActiveCount = new AtomicInteger();
    @GuardedBy({"mLock"})
    private final ArrayList<FileBridge> mBridges = new ArrayList<>();
    private final PackageInstallerService.InternalCallback mCallback;
    @GuardedBy({"mLock"})
    private SparseIntArray mChildSessionIds = new SparseIntArray();
    @GuardedBy({"mLock"})
    private float mClientProgress = 0.0f;
    @GuardedBy({"mLock"})
    private boolean mCommitted = false;
    private final Context mContext;
    @GuardedBy({"mLock"})
    private boolean mDestroyed = false;
    @GuardedBy({"mLock"})
    private final ArrayList<RevocableFileDescriptor> mFds = new ArrayList<>();
    @GuardedBy({"mLock"})
    private String mFinalMessage;
    @GuardedBy({"mLock"})
    private int mFinalStatus;
    private final Handler mHandler;
    private final Handler.Callback mHandlerCallback = new Handler.Callback() {
        /* class com.android.server.pm.PackageInstallerSession.AnonymousClass3 */

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            int i = msg.what;
            if (i == 1) {
                PackageInstallerSession.this.handleCommit();
            } else if (i == 2) {
                SomeArgs args = (SomeArgs) msg.obj;
                String packageName = (String) args.arg1;
                String message = (String) args.arg2;
                Bundle extras = (Bundle) args.arg3;
                IPackageInstallObserver2 observer = (IPackageInstallObserver2) args.arg4;
                int returnCode = args.argi1;
                args.recycle();
                try {
                    observer.onPackageInstalled(packageName, returnCode, message, extras);
                } catch (RemoteException e) {
                }
            }
            return true;
        }
    };
    private boolean mHwPermissionsAccepted = true;
    @GuardedBy({"mLock"})
    private File mInheritedFilesBase;
    @GuardedBy({"mLock"})
    private String mInstallerPackageName;
    @GuardedBy({"mLock"})
    private int mInstallerUid;
    @GuardedBy({"mLock"})
    private float mInternalProgress = 0.0f;
    private boolean mIsMergePlugin = false;
    private final Object mLock = new Object();
    private final int mOriginalInstallerUid;
    @GuardedBy({"mLock"})
    private String mPackageName;
    @GuardedBy({"mLock"})
    private int mParentSessionId;
    @GuardedBy({"mLock"})
    private boolean mPermissionsManuallyAccepted = false;
    private IHwPluginPackage mPluginPackage;
    @GuardedBy({"mLock"})
    private long mPluginVersionCode;
    private final PackageManagerService mPm;
    @GuardedBy({"mLock"})
    private boolean mPrepared = false;
    @GuardedBy({"mLock"})
    private float mProgress = 0.0f;
    @GuardedBy({"mLock"})
    private boolean mRelinquished = false;
    @GuardedBy({"mLock"})
    private IPackageInstallObserver2 mRemoteObserver;
    @GuardedBy({"mLock"})
    private float mReportedProgress = -1.0f;
    @GuardedBy({"mLock"})
    private File mResolvedBaseFile;
    @GuardedBy({"mLock"})
    private final List<File> mResolvedInheritedFiles = new ArrayList();
    @GuardedBy({"mLock"})
    private final List<String> mResolvedInstructionSets = new ArrayList();
    @GuardedBy({"mLock"})
    private final List<String> mResolvedNativeLibPaths = new ArrayList();
    @GuardedBy({"mLock"})
    private File mResolvedStageDir;
    @GuardedBy({"mLock"})
    private final List<File> mResolvedStagedFiles = new ArrayList();
    @GuardedBy({"mLock"})
    private boolean mSealed = false;
    private final PackageSessionProvider mSessionProvider;
    @GuardedBy({"mLock"})
    private boolean mShouldBeSealed = false;
    @GuardedBy({"mLock"})
    private PackageParser.SigningDetails mSigningDetails;
    @GuardedBy({"mLock"})
    private boolean mStagedSessionApplied;
    @GuardedBy({"mLock"})
    private int mStagedSessionErrorCode = 0;
    @GuardedBy({"mLock"})
    private String mStagedSessionErrorMessage;
    @GuardedBy({"mLock"})
    private boolean mStagedSessionFailed;
    @GuardedBy({"mLock"})
    private boolean mStagedSessionReady;
    private final StagingManager mStagingManager;
    @GuardedBy({"mLock"})
    private boolean mVerityFound;
    @GuardedBy({"mLock"})
    private long mVersionCode;
    final PackageInstaller.SessionParams params;
    final int sessionId;
    final String stageCid;
    final File stageDir;
    @GuardedBy({"mLock"})
    private long updatedMillis;
    final int userId;

    @GuardedBy({"mLock"})
    private boolean isInstallerDeviceOwnerOrAffiliatedProfileOwnerLocked() {
        DevicePolicyManagerInternal dpmi;
        if (this.userId == UserHandle.getUserId(this.mInstallerUid) && (dpmi = (DevicePolicyManagerInternal) LocalServices.getService(DevicePolicyManagerInternal.class)) != null && dpmi.canSilentlyInstallPackage(this.mInstallerPackageName, this.mInstallerUid)) {
            return true;
        }
        return false;
    }

    @GuardedBy({"mLock"})
    private boolean needToAskForPermissionsLocked() {
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
            return true;
        }
        return false;
    }

    public PackageInstallerSession(PackageInstallerService.InternalCallback callback, Context context, PackageManagerService pm, PackageSessionProvider sessionProvider, Looper looper, StagingManager stagingManager, int sessionId2, int userId2, String installerPackageName, int installerUid, PackageInstaller.SessionParams params2, long createdMillis2, File stageDir2, String stageCid2, boolean prepared, boolean committed, boolean sealed, int[] childSessionIds, int parentSessionId, boolean isReady, boolean isFailed, boolean isApplied, int stagedSessionErrorCode, String stagedSessionErrorMessage) {
        int[] iArr = childSessionIds;
        this.mCallback = callback;
        this.mContext = context;
        this.mPm = pm;
        this.mSessionProvider = sessionProvider;
        this.mHandler = new Handler(looper, this.mHandlerCallback);
        this.mStagingManager = stagingManager;
        this.sessionId = sessionId2;
        this.userId = userId2;
        this.mOriginalInstallerUid = installerUid;
        this.mInstallerPackageName = installerPackageName;
        this.mInstallerUid = installerUid;
        this.params = params2;
        this.createdMillis = createdMillis2;
        this.updatedMillis = createdMillis2;
        this.stageDir = stageDir2;
        this.stageCid = stageCid2;
        this.mShouldBeSealed = sealed;
        if (iArr != null) {
            int i = 0;
            for (int length = iArr.length; i < length; length = length) {
                this.mChildSessionIds.put(iArr[i], 0);
                i++;
                iArr = childSessionIds;
            }
        }
        this.mParentSessionId = parentSessionId;
        if (!params2.isMultiPackage) {
            if ((stageDir2 == null) == (stageCid2 == null)) {
                throw new IllegalArgumentException("Exactly one of stageDir or stageCid stage must be set");
            }
        }
        this.mPrepared = prepared;
        this.mCommitted = committed;
        this.mStagedSessionReady = isReady;
        this.mStagedSessionFailed = isFailed;
        this.mStagedSessionApplied = isApplied;
        this.mStagedSessionErrorCode = stagedSessionErrorCode;
        this.mStagedSessionErrorMessage = stagedSessionErrorMessage != null ? stagedSessionErrorMessage : "";
        if ((params2.installFlags & 32) != 0) {
            boolean z = false;
            this.mHwPermissionsAccepted = (SystemProperties.getBoolean("ro.config.hwRemoveADBMonitor", false) || HwAdbManager.autoPermitInstall()) ? true : z;
        }
        this.mPluginPackage = HwServiceExFactory.getHwPluginPackage(this.mPm, null);
    }

    private boolean shouldScrubData(int callingUid) {
        return callingUid >= 10000 && getInstallerUid() != callingUid;
    }

    public PackageInstaller.SessionInfo generateInfoForCaller(boolean includeIcon, int callingUid) {
        return generateInfoInternal(includeIcon, shouldScrubData(callingUid));
    }

    public PackageInstaller.SessionInfo generateInfoScrubbed(boolean includeIcon) {
        return generateInfoInternal(includeIcon, true);
    }

    private PackageInstaller.SessionInfo generateInfoInternal(boolean includeIcon, boolean scrubData) {
        PackageInstaller.SessionInfo info = new PackageInstaller.SessionInfo();
        synchronized (this.mLock) {
            info.sessionId = this.sessionId;
            info.userId = this.userId;
            info.installerPackageName = this.mInstallerPackageName;
            info.resolvedBaseCodePath = this.mResolvedBaseFile != null ? this.mResolvedBaseFile.getAbsolutePath() : null;
            info.progress = this.mProgress;
            info.sealed = this.mSealed;
            info.isCommitted = this.mCommitted;
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
            if (!scrubData) {
                info.originatingUri = this.params.originatingUri;
            }
            info.originatingUid = this.params.originatingUid;
            if (!scrubData) {
                info.referrerUri = this.params.referrerUri;
            }
            info.grantedRuntimePermissions = this.params.grantedRuntimePermissions;
            info.whitelistedRestrictedPermissions = this.params.whitelistedRestrictedPermissions;
            info.installFlags = this.params.installFlags;
            info.isMultiPackage = this.params.isMultiPackage;
            info.isStaged = this.params.isStaged;
            info.parentSessionId = this.mParentSessionId;
            info.childSessionIds = this.mChildSessionIds.copyKeys();
            if (info.childSessionIds == null) {
                info.childSessionIds = EMPTY_CHILD_SESSION_ARRAY;
            }
            info.isStagedSessionApplied = this.mStagedSessionApplied;
            info.isStagedSessionReady = this.mStagedSessionReady;
            info.isStagedSessionFailed = this.mStagedSessionFailed;
            info.setStagedSessionErrorCode(this.mStagedSessionErrorCode, this.mStagedSessionErrorMessage);
            info.updatedMillis = this.updatedMillis;
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

    /* access modifiers changed from: package-private */
    public boolean isCommitted() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mCommitted;
        }
        return z;
    }

    public boolean isStagedAndInTerminalState() {
        boolean z;
        synchronized (this.mLock) {
            z = this.params.isStaged && (this.mStagedSessionApplied || this.mStagedSessionFailed);
        }
        return z;
    }

    @GuardedBy({"mLock"})
    private void assertPreparedAndNotSealedLocked(String cookie) {
        assertPreparedAndNotCommittedOrDestroyedLocked(cookie);
        if (this.mSealed) {
            throw new SecurityException(cookie + " not allowed after sealing");
        }
    }

    @GuardedBy({"mLock"})
    private void assertPreparedAndNotCommittedOrDestroyedLocked(String cookie) {
        assertPreparedAndNotDestroyedLocked(cookie);
        if (this.mCommitted) {
            throw new SecurityException(cookie + " not allowed after commit");
        }
    }

    @GuardedBy({"mLock"})
    private void assertPreparedAndNotDestroyedLocked(String cookie) {
        if (!this.mPrepared) {
            throw new IllegalStateException(cookie + " before prepared");
        } else if (this.mDestroyed) {
            throw new SecurityException(cookie + " not allowed after destruction");
        }
    }

    @GuardedBy({"mLock"})
    private File resolveStageDirLocked() throws IOException {
        if (this.mResolvedStageDir == null) {
            File file = this.stageDir;
            if (file != null) {
                this.mResolvedStageDir = file;
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

    @GuardedBy({"mLock"})
    private void computeProgressLocked(boolean forcePublish) {
        this.mProgress = MathUtils.constrain(this.mClientProgress * 0.8f, 0.0f, 0.8f) + MathUtils.constrain(this.mInternalProgress * 0.2f, 0.0f, 0.2f);
        if (forcePublish || ((double) Math.abs(this.mProgress - this.mReportedProgress)) >= 0.01d) {
            float f = this.mProgress;
            this.mReportedProgress = f;
            this.mCallback.onSessionProgressChanged(this, f);
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

    /* JADX INFO: finally extract failed */
    private ParcelFileDescriptor doWriteInternal(String name, long offsetBytes, long lengthBytes, ParcelFileDescriptor incomingFd) throws IOException {
        FileBridge bridge;
        RevocableFileDescriptor fd;
        File stageDir2;
        ErrnoException e;
        FileDescriptor targetFd;
        Throwable th;
        synchronized (this.mLock) {
            assertCallerIsOwnerOrRootLocked();
            assertPreparedAndNotSealedLocked("openWrite");
            if (PackageInstaller.ENABLE_REVOCABLE_FD) {
                RevocableFileDescriptor fd2 = new RevocableFileDescriptor();
                this.mFds.add(fd2);
                fd = fd2;
                bridge = null;
            } else {
                FileBridge bridge2 = new FileBridge();
                this.mBridges.add(bridge2);
                fd = null;
                bridge = bridge2;
            }
            stageDir2 = resolveStageDirLocked();
        }
        try {
            if (FileUtils.isValidExtFilename(name)) {
                long identity = Binder.clearCallingIdentity();
                try {
                    File target = new File(stageDir2, name);
                    Binder.restoreCallingIdentity(identity);
                    FileDescriptor targetFd2 = Os.open(target.getAbsolutePath(), OsConstants.O_CREAT | OsConstants.O_WRONLY, 420);
                    Os.chmod(target.getAbsolutePath(), 420);
                    TurboZoneFunctionForApkFile.setTurboZoneKeyFileFlag(targetFd2, Long.valueOf(lengthBytes), this.params);
                    if (stageDir2 != null && lengthBytes > 0) {
                        try {
                            ((StorageManager) this.mContext.getSystemService(StorageManager.class)).allocateBytes(targetFd2, lengthBytes, PackageHelper.translateAllocateFlags(this.params.installFlags));
                        } catch (ErrnoException e2) {
                            e = e2;
                            throw e.rethrowAsIOException();
                        }
                    }
                    if (offsetBytes > 0) {
                        Os.lseek(targetFd2, offsetBytes, OsConstants.SEEK_SET);
                    }
                    if (incomingFd != null) {
                        int callingUid = Binder.getCallingUid();
                        if (callingUid == 0 || callingUid == 1000 || callingUid == 2000) {
                            try {
                                targetFd = targetFd2;
                                try {
                                    FileUtils.copy(incomingFd.getFileDescriptor(), targetFd2, lengthBytes, null, $$Lambda$_14QHG018Z6p13d3hzJuGTWnNeo.INSTANCE, new FileUtils.ProgressListener(new Int64Ref(0)) {
                                        /* class com.android.server.pm.$$Lambda$PackageInstallerSession$0Oqu1oanLjaOBEcFPtJVCRQ0lHs */
                                        private final /* synthetic */ Int64Ref f$1;

                                        {
                                            this.f$1 = r2;
                                        }

                                        @Override // android.os.FileUtils.ProgressListener
                                        public final void onProgress(long j) {
                                            PackageInstallerSession.this.lambda$doWriteInternal$0$PackageInstallerSession(this.f$1, j);
                                        }
                                    });
                                    try {
                                        IoUtils.closeQuietly(targetFd);
                                        IoUtils.closeQuietly(incomingFd);
                                        synchronized (this.mLock) {
                                            try {
                                                if (PackageInstaller.ENABLE_REVOCABLE_FD) {
                                                    this.mFds.remove(fd);
                                                } else {
                                                    bridge.forceClose();
                                                    this.mBridges.remove(bridge);
                                                }
                                            } catch (Throwable th2) {
                                                th = th2;
                                                throw th;
                                            }
                                        }
                                        return null;
                                    } catch (ErrnoException e3) {
                                        e = e3;
                                        throw e.rethrowAsIOException();
                                    }
                                } catch (Throwable th3) {
                                    th = th3;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                targetFd = targetFd2;
                                IoUtils.closeQuietly(targetFd);
                                IoUtils.closeQuietly(incomingFd);
                                synchronized (this.mLock) {
                                    try {
                                        if (PackageInstaller.ENABLE_REVOCABLE_FD) {
                                            this.mFds.remove(fd);
                                        } else {
                                            bridge.forceClose();
                                            this.mBridges.remove(bridge);
                                        }
                                    } catch (Throwable th5) {
                                        th = th5;
                                    }
                                }
                                throw th;
                            }
                        } else {
                            throw new SecurityException("Reverse mode only supported from shell or system");
                        }
                    } else if (PackageInstaller.ENABLE_REVOCABLE_FD) {
                        fd.init(this.mContext, targetFd2);
                        return fd.getRevocableFileDescriptor();
                    } else {
                        bridge.setTargetFile(targetFd2);
                        bridge.start();
                        return new ParcelFileDescriptor(bridge.getClientSocket());
                    }
                } catch (Throwable th6) {
                    Binder.restoreCallingIdentity(identity);
                    throw th6;
                }
            } else {
                throw new IllegalArgumentException("Invalid name: " + name);
            }
        } catch (ErrnoException e4) {
            e = e4;
            throw e.rethrowAsIOException();
        }
    }

    public /* synthetic */ void lambda$doWriteInternal$0$PackageInstallerSession(Int64Ref last, long progress) {
        if (this.params.sizeBytes > 0) {
            last.value = progress;
            addClientProgress(((float) (progress - last.value)) / ((float) this.params.sizeBytes));
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

    @GuardedBy({"mLock"})
    private void assertCallerIsOwnerOrRootLocked() {
        int callingUid = Binder.getCallingUid();
        if (callingUid != 0 && callingUid != this.mInstallerUid) {
            throw new SecurityException("Session does not belong to uid " + callingUid);
        }
    }

    @GuardedBy({"mLock"})
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
        if (hasParentSessionId()) {
            throw new IllegalStateException("Session " + this.sessionId + " is a child of multi-package session " + this.mParentSessionId + " and may not be committed directly.");
        } else if (markAsCommitted(statusReceiver, forTransfer)) {
            if (isMultiPackage()) {
                IntentSender childIntentSender = new ChildStatusIntentReceiver(this.mChildSessionIds.clone(), statusReceiver).getIntentSender();
                RuntimeException commitException = null;
                boolean commitFailed = false;
                for (int i = this.mChildSessionIds.size() - 1; i >= 0; i--) {
                    try {
                        if (!this.mSessionProvider.getSession(this.mChildSessionIds.keyAt(i)).markAsCommitted(childIntentSender, forTransfer)) {
                            commitFailed = true;
                        }
                    } catch (RuntimeException e) {
                        commitException = e;
                    }
                }
                if (commitException != null) {
                    throw commitException;
                } else if (commitFailed) {
                    return;
                }
            }
            this.mHandler.obtainMessage(1).sendToTarget();
        }
    }

    private boolean isInstallWithHwHepManager() {
        synchronized (this.mLock) {
            boolean isContainHepFile = isContainHepFile();
            Slog.i(TAG, "isInstallWithHwHepManager isHep:" + this.params.isHep + ",isContainHep:" + isContainHepFile);
            if (!this.params.isHep || !isContainHepFile) {
                return false;
            }
            try {
                if (this.mPm.getHwPMSEx().installHepApp(resolveStageDirLocked()) == 0) {
                    dispatchSessionFinished(1, "HepManager install", null);
                } else {
                    dispatchSessionFinished(RequestStatus.SYS_ETIMEDOUT, "HepManager install failed", null);
                }
            } catch (IOException e) {
                Slog.e(TAG, "installHepApp IOException!");
            } finally {
                destroyInternal();
            }
            return true;
        }
    }

    @GuardedBy({"mLock"})
    private boolean isContainHepFile() {
        File[] files;
        File file = this.stageDir;
        if (file == null || !file.exists() || (files = this.stageDir.listFiles()) == null) {
            return false;
        }
        for (File getFile : files) {
            if (getFile.isFile() && getFile.getName().toLowerCase().endsWith(".hep")) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public class ChildStatusIntentReceiver {
        private final SparseIntArray mChildSessionsRemaining;
        private final IIntentSender.Stub mLocalSender;
        private final IntentSender mStatusReceiver;

        private ChildStatusIntentReceiver(SparseIntArray remainingSessions, IntentSender statusReceiver) {
            this.mLocalSender = new IIntentSender.Stub() {
                /* class com.android.server.pm.PackageInstallerSession.ChildStatusIntentReceiver.AnonymousClass1 */

                public void send(int code, Intent intent, String resolvedType, IBinder whitelistToken, IIntentReceiver finishedReceiver, String requiredPermission, Bundle options) {
                    ChildStatusIntentReceiver.this.statusUpdate(intent);
                }
            };
            this.mChildSessionsRemaining = remainingSessions;
            this.mStatusReceiver = statusReceiver;
        }

        public IntentSender getIntentSender() {
            return new IntentSender(this.mLocalSender);
        }

        public void statusUpdate(Intent intent) {
            PackageInstallerSession.this.mHandler.post(new Runnable(intent) {
                /* class com.android.server.pm.$$Lambda$PackageInstallerSession$ChildStatusIntentReceiver$CIWymiEKCzNknV3an6tFtcz5Mc */
                private final /* synthetic */ Intent f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    PackageInstallerSession.ChildStatusIntentReceiver.this.lambda$statusUpdate$0$PackageInstallerSession$ChildStatusIntentReceiver(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$statusUpdate$0$PackageInstallerSession$ChildStatusIntentReceiver(Intent intent) {
            if (this.mChildSessionsRemaining.size() != 0) {
                int sessionId = intent.getIntExtra("android.content.pm.extra.SESSION_ID", 0);
                int status = intent.getIntExtra("android.content.pm.extra.STATUS", 1);
                int sessionIndex = this.mChildSessionsRemaining.indexOfKey(sessionId);
                if (status == 0) {
                    this.mChildSessionsRemaining.removeAt(sessionIndex);
                    if (this.mChildSessionsRemaining.size() == 0) {
                        try {
                            intent.putExtra("android.content.pm.extra.SESSION_ID", PackageInstallerSession.this.sessionId);
                            this.mStatusReceiver.sendIntent(PackageInstallerSession.this.mContext, 0, intent, null, null);
                        } catch (IntentSender.SendIntentException e) {
                        }
                    }
                } else if (-1 == status) {
                    try {
                        this.mStatusReceiver.sendIntent(PackageInstallerSession.this.mContext, 0, intent, null, null);
                    } catch (IntentSender.SendIntentException e2) {
                    }
                } else {
                    intent.putExtra("android.content.pm.extra.SESSION_ID", PackageInstallerSession.this.sessionId);
                    this.mChildSessionsRemaining.clear();
                    try {
                        this.mStatusReceiver.sendIntent(PackageInstallerSession.this.mContext, 0, intent, null, null);
                    } catch (IntentSender.SendIntentException e3) {
                    }
                }
            }
        }
    }

    public boolean markAsCommitted(IntentSender statusReceiver, boolean forTransfer) {
        boolean wasSealed;
        Preconditions.checkNotNull(statusReceiver);
        List<PackageInstallerSession> childSessions = getChildSessions();
        synchronized (this.mLock) {
            assertCallerIsOwnerOrRootLocked();
            assertPreparedAndNotDestroyedLocked("commit");
            this.mRemoteObserver = new PackageInstallerService.PackageInstallObserverAdapter(this.mContext, statusReceiver, this.sessionId, isInstallerDeviceOwnerOrAffiliatedProfileOwnerLocked(), this.userId).getBinder();
            if (forTransfer) {
                this.mContext.enforceCallingOrSelfPermission("android.permission.INSTALL_PACKAGES", null);
                if (this.mInstallerUid == this.mOriginalInstallerUid) {
                    throw new IllegalArgumentException("Session has not been transferred");
                }
            } else if (this.mInstallerUid != this.mOriginalInstallerUid) {
                throw new IllegalArgumentException("Session has been transferred");
            }
            if (isInstallWithHwHepManager()) {
                Slog.i(TAG, "commit in install hep!");
                return false;
            } else if (this.mCommitted) {
                return true;
            } else {
                wasSealed = this.mSealed;
                if (!this.mSealed) {
                    try {
                        sealAndValidateLocked(childSessions);
                    } catch (IOException e) {
                        throw new IllegalArgumentException(e);
                    } catch (PackageManagerException e2) {
                        destroyInternal();
                        dispatchSessionFinished(e2.error, ExceptionUtils.getCompleteMessage(e2), null);
                        return false;
                    }
                }
                this.mClientProgress = 1.0f;
                computeProgressLocked(true);
                this.mActiveCount.incrementAndGet();
                this.mCommitted = true;
            }
        }
        if (!wasSealed) {
            this.mCallback.onSessionSealedBlocking(this);
        }
        return true;
    }

    private List<PackageInstallerSession> getChildSessions() {
        List<PackageInstallerSession> childSessions = null;
        if (isMultiPackage()) {
            int[] childSessionIds = getChildSessionIds();
            childSessions = new ArrayList<>(childSessionIds.length);
            for (int childSessionId : childSessionIds) {
                childSessions.add(this.mSessionProvider.getSession(childSessionId));
            }
        }
        return childSessions;
    }

    @GuardedBy({"mLock"})
    private void assertMultiPackageConsistencyLocked(List<PackageInstallerSession> childSessions) throws PackageManagerException {
        for (PackageInstallerSession childSession : childSessions) {
            if (childSession != null) {
                assertConsistencyWithLocked(childSession);
            }
        }
    }

    @GuardedBy({"mLock"})
    private void assertConsistencyWithLocked(PackageInstallerSession other) throws PackageManagerException {
        if (this.params.isStaged != other.params.isStaged) {
            throw new PackageManagerException(-120, "Multipackage Inconsistency: session " + other.sessionId + " and session " + this.sessionId + " have inconsistent staged settings");
        } else if (this.params.getEnableRollback() != other.params.getEnableRollback()) {
            throw new PackageManagerException(-120, "Multipackage Inconsistency: session " + other.sessionId + " and session " + this.sessionId + " have inconsistent rollback settings");
        }
    }

    @GuardedBy({"mLock"})
    private void sealAndValidateLocked(List<PackageInstallerSession> childSessions) throws PackageManagerException, IOException {
        int i;
        assertNoWriteFileTransfersOpenLocked();
        assertPreparedAndNotDestroyedLocked("sealing of session");
        boolean anotherSessionAlreadyInProgress = true;
        this.mSealed = true;
        if (childSessions != null) {
            assertMultiPackageConsistencyLocked(childSessions);
        }
        if (this.params.isStaged) {
            PackageInstallerSession activeSession = this.mStagingManager.getActiveSession();
            if (activeSession == null || this.sessionId == (i = activeSession.sessionId) || this.mParentSessionId == i) {
                anotherSessionAlreadyInProgress = false;
            }
            if (anotherSessionAlreadyInProgress) {
                throw new PackageManagerException(-119, "There is already in-progress committed staged session " + activeSession.sessionId, null);
            }
        }
        if (!this.params.isMultiPackage) {
            PackageInfo pkgInfo = this.mPm.getPackageInfo(this.params.appPackageName, 67108928, this.userId);
            resolveStageDirLocked();
            try {
                if ((this.params.installFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0) {
                    validateApexInstallLocked();
                } else {
                    validateApkInstallLocked(pkgInfo);
                }
            } catch (PackageManagerException e) {
                throw e;
            } catch (Throwable e2) {
                throw new PackageManagerException(e2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void sealAndValidateIfNecessary() {
        synchronized (this.mLock) {
            if (this.mShouldBeSealed) {
                if (isStagedAndInTerminalState()) {
                }
            }
            return;
        }
        List<PackageInstallerSession> childSessions = getChildSessions();
        synchronized (this.mLock) {
            try {
                sealAndValidateLocked(childSessions);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } catch (PackageManagerException e2) {
                Slog.e(TAG, "Package not valid", e2);
                destroyInternal();
                dispatchSessionFinished(e2.error, ExceptionUtils.getCompleteMessage(e2), null);
            }
        }
    }

    public void markUpdated() {
        synchronized (this.mLock) {
            this.updatedMillis = System.currentTimeMillis();
        }
    }

    public void transfer(String packageName) {
        Preconditions.checkNotNull(packageName);
        ApplicationInfo newOwnerAppInfo = this.mPm.getApplicationInfo(packageName, 0, this.userId);
        if (newOwnerAppInfo == null) {
            throw new ParcelableException(new PackageManager.NameNotFoundException(packageName));
        } else if (this.mPm.checkUidPermission("android.permission.INSTALL_PACKAGES", newOwnerAppInfo.uid) != 0) {
            throw new SecurityException("Destination package " + packageName + " does not have the android.permission.INSTALL_PACKAGES permission");
        } else if (this.params.areHiddenOptionsSet()) {
            List<PackageInstallerSession> childSessions = getChildSessions();
            synchronized (this.mLock) {
                assertCallerIsOwnerOrRootLocked();
                assertPreparedAndNotSealedLocked("transfer");
                try {
                    sealAndValidateLocked(childSessions);
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
    /* access modifiers changed from: public */
    private void handleCommit() {
        if (isInstallerDeviceOwnerOrAffiliatedProfileOwnerLocked()) {
            DevicePolicyEventLogger.createEvent(112).setAdmin(this.mInstallerPackageName).write();
        }
        if (this.params.isStaged) {
            this.mStagingManager.commitSession(this);
            destroyInternal();
            dispatchSessionFinished(1, "Session staged", null);
            Slog.d(TAG, "start handleCommit(), StagingManager.commitSession");
        } else if ((this.params.installFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0) {
            destroyInternal();
            dispatchSessionFinished(RequestStatus.SYS_ETIMEDOUT, "APEX packages can only be installed using staged sessions.", null);
        } else {
            List<PackageInstallerSession> childSessions = getChildSessions();
            try {
                synchronized (this.mLock) {
                    commitNonStagedLocked(childSessions);
                }
            } catch (PackageManagerException e) {
                String completeMsg = ExceptionUtils.getCompleteMessage(e);
                Slog.e(TAG, "Commit of session " + this.sessionId + " failed: " + completeMsg);
                destroyInternal();
                dispatchSessionFinished(e.error, completeMsg, null);
            }
        }
    }

    @GuardedBy({"mLock"})
    private void commitNonStagedLocked(List<PackageInstallerSession> childSessions) throws PackageManagerException {
        PackageManagerService.ActiveInstallSession committingSession = makeSessionActiveLocked();
        if (committingSession != null) {
            if (this.params.hdbEncode != null) {
                if (!HwAdbManager.startHdbVerification(this.params.hdbArgs, this.params.hdbArgIndex, this.params.hdbEncode)) {
                    throw new PackageManagerException(RequestStatus.SYS_ETIMEDOUT, "Failure [INSTALL_HDB_VERIFY_FAILED]");
                }
            } else if (!this.mHwPermissionsAccepted && HwAdbManager.startPackageInstallerForConfirm(this.mContext, this.sessionId)) {
                Slog.d(TAG, "start PackageInstallerActivity success, close current install!");
                closeInternal(false);
                return;
            }
            if (isMultiPackage()) {
                List<PackageManagerService.ActiveInstallSession> activeChildSessions = new ArrayList<>(childSessions.size());
                boolean success = true;
                PackageManagerException failure = null;
                for (int i = 0; i < childSessions.size(); i++) {
                    try {
                        PackageManagerService.ActiveInstallSession activeSession = childSessions.get(i).makeSessionActiveLocked();
                        if (activeSession != null) {
                            activeChildSessions.add(activeSession);
                        }
                    } catch (PackageManagerException e) {
                        failure = e;
                        success = false;
                    }
                }
                if (!success) {
                    try {
                        this.mRemoteObserver.onPackageInstalled((String) null, failure.error, failure.getLocalizedMessage(), (Bundle) null);
                    } catch (RemoteException e2) {
                    }
                } else {
                    this.mPm.installStage(activeChildSessions);
                }
            } else {
                this.mPm.installStage(committingSession);
            }
        }
    }

    @GuardedBy({"mLock"})
    private PackageManagerService.ActiveInstallSession makeSessionActiveLocked() throws PackageManagerException {
        IPackageInstallObserver2 localObserver;
        UserHandle user;
        if (this.mRelinquished) {
            throw new PackageManagerException(RequestStatus.SYS_ETIMEDOUT, "Session relinquished");
        } else if (this.mDestroyed) {
            throw new PackageManagerException(RequestStatus.SYS_ETIMEDOUT, "Session destroyed");
        } else if (this.mSealed) {
            if ((this.params.installFlags & DumpState.DUMP_INTENT_FILTER_VERIFIERS) != 0) {
                localObserver = null;
            } else {
                if (!((PackageInstaller.SessionParams) this.params).isMultiPackage) {
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
                        return null;
                    }
                    if (needToAskForPermissionsLocked()) {
                        Intent intent = new Intent("android.content.pm.action.CONFIRM_INSTALL");
                        intent.setPackage(this.mPm.getPackageInstallerPackageName());
                        intent.putExtra("android.content.pm.extra.SESSION_ID", this.sessionId);
                        try {
                            this.mRemoteObserver.onUserActionRequired(intent);
                        } catch (RemoteException e) {
                        }
                        closeInternal(false);
                        return null;
                    }
                    if (this.params.mode == 2 || this.mIsMergePlugin) {
                        try {
                            List<File> fromFiles = this.mResolvedInheritedFiles;
                            File toDir = resolveStageDirLocked();
                            Slog.d(TAG, "Inherited files: " + this.mResolvedInheritedFiles);
                            if (!this.mResolvedInheritedFiles.isEmpty()) {
                                if (this.mInheritedFilesBase == null) {
                                    throw new IllegalStateException("mInheritedFilesBase == null");
                                }
                            }
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
                            if (isLinkPossible(fromFiles, toDir)) {
                                linkFiles(fromFiles, toDir, this.mInheritedFilesBase);
                            } else {
                                copyFiles(fromFiles, toDir, this.mInheritedFilesBase);
                            }
                        } catch (IOException e2) {
                            throw new PackageManagerException(-4, "Failed to inherit existing install", e2);
                        }
                    }
                    this.mInternalProgress = 0.5f;
                    computeProgressLocked(true);
                    extractNativeLibraries(this.mResolvedStageDir, this.params.abiOverride, mayInheritNativeLibs());
                }
                localObserver = new IPackageInstallObserver2.Stub() {
                    /* class com.android.server.pm.PackageInstallerSession.AnonymousClass4 */

                    public void onUserActionRequired(Intent intent) {
                        throw new IllegalStateException();
                    }

                    public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) {
                        PackageInstallerSession.this.destroyInternal();
                        PackageInstallerSession.this.dispatchSessionFinished(returnCode, msg, extras);
                    }
                };
            }
            if ((this.params.installFlags & 64) != 0) {
                user = UserHandle.ALL;
            } else {
                user = new UserHandle(this.userId);
            }
            this.mRelinquished = true;
            return new PackageManagerService.ActiveInstallSession(this.mPackageName, this.stageDir, localObserver, this.params, this.mInstallerPackageName, this.mInstallerUid, user, this.mSigningDetails);
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

    @GuardedBy({"mLock"})
    private void validateApexInstallLocked() throws PackageManagerException {
        File[] addedFiles = this.mResolvedStageDir.listFiles(sAddedFilter);
        if (ArrayUtils.isEmpty(addedFiles)) {
            throw new PackageManagerException(-2, "No packages staged");
        } else if (ArrayUtils.size(addedFiles) <= 1) {
            this.mResolvedBaseFile = addedFiles[0];
        } else {
            throw new PackageManagerException(-2, "Too many files for apex install");
        }
    }

    /* JADX INFO: Multiple debug info for r2v25 'libDirsToInherit'  java.util.List<java.io.File>: [D('libDirsToInherit' java.util.List<java.io.File>), D('archSubDir' java.io.File)] */
    /* JADX INFO: Multiple debug info for r2v26 'libDirsToInherit'  java.util.List<java.io.File>: [D('libDirsToInherit' java.util.List<java.io.File>), D('archSubDir' java.io.File)] */
    @GuardedBy({"mLock"})
    private void validateApkInstallLocked(PackageInfo pkgInfo) throws PackageManagerException {
        PackageParser.ApkLite existingBase;
        PackageParser.PackageLite existing;
        File[] libDirs;
        List<File> libDirsToInherit;
        PackageParser.ApkLite existingBase2;
        File[] fileArr;
        List<File> libDirsToInherit2;
        int i;
        File[] removedFiles;
        this.mPackageName = null;
        this.mVersionCode = -1;
        this.mPluginVersionCode = -1;
        this.mSigningDetails = PackageParser.SigningDetails.UNKNOWN;
        this.mResolvedBaseFile = null;
        this.mResolvedStagedFiles.clear();
        this.mResolvedInheritedFiles.clear();
        if (this.params.mode == 2 && (pkgInfo == null || pkgInfo.applicationInfo == null)) {
            throw new PackageManagerException(-2, "Missing existing base package");
        }
        this.mVerityFound = PackageManagerServiceUtils.isApkVerityEnabled() && this.params.mode == 2 && VerityUtils.hasFsverity(pkgInfo.applicationInfo.getBaseCodePath());
        try {
            resolveStageDirLocked();
            File[] removedFiles2 = this.mResolvedStageDir.listFiles(sRemovedFilter);
            List<String> removeSplitList = new ArrayList<>();
            if (!ArrayUtils.isEmpty(removedFiles2)) {
                for (File removedFile : removedFiles2) {
                    String fileName = removedFile.getName();
                    removeSplitList.add(fileName.substring(0, fileName.length() - REMOVE_SPLIT_MARKER_EXTENSION.length()));
                }
            }
            File[] addedFiles = this.mResolvedStageDir.listFiles(sAddedFilter);
            if (!ArrayUtils.isEmpty(addedFiles) || removeSplitList.size() != 0) {
                ArraySet<String> stagedSplits = new ArraySet<>();
                int length = addedFiles.length;
                boolean isMergePluginInit = false;
                PackageParser.ApkLite baseApk = null;
                PackageInfo pkgInfo2 = pkgInfo;
                int i2 = 0;
                while (i2 < length) {
                    File addedFile = addedFiles[i2];
                    try {
                        PackageParser.ApkLite apk = PackageParser.parseApkLite(addedFile, 32);
                        if (stagedSplits.add(apk.splitName)) {
                            if (this.mPluginPackage != null) {
                                if (!apk.isPlugin && this.mVersionCode < 0) {
                                    this.mVersionCode = apk.getLongVersionCode();
                                } else if (apk.isPlugin && this.mPluginVersionCode < 0) {
                                    this.mPluginVersionCode = apk.getLongVersionCode();
                                }
                                if (this.mPackageName == null) {
                                    this.mPackageName = apk.packageName;
                                }
                                if (isMergePluginInit || this.params.mode != 1) {
                                    removedFiles = removedFiles2;
                                } else {
                                    if (pkgInfo2 == null) {
                                        removedFiles = removedFiles2;
                                        pkgInfo2 = this.mPm.getPackageInfo(this.mPackageName, 0, this.userId);
                                    } else {
                                        removedFiles = removedFiles2;
                                    }
                                    if (!(pkgInfo2 == null || pkgInfo2.applicationInfo == null || !pkgInfo2.applicationInfo.hasPlugin())) {
                                        this.mIsMergePlugin = true;
                                    }
                                    isMergePluginInit = true;
                                }
                                Slog.d(TAG, "MergePlugin pkgInfo: " + pkgInfo2);
                            } else {
                                removedFiles = removedFiles2;
                            }
                            if (this.mPackageName == null) {
                                this.mPackageName = apk.packageName;
                                this.mVersionCode = apk.getLongVersionCode();
                            }
                            if (this.mSigningDetails == PackageParser.SigningDetails.UNKNOWN) {
                                this.mSigningDetails = apk.signingDetails;
                            }
                            assertApkConsistentLocked(String.valueOf(addedFile), apk);
                            String targetName = apk.splitName == null ? "base.apk" : "split_" + apk.splitName + ".apk";
                            if (FileUtils.isValidExtFilename(targetName)) {
                                File targetFile = new File(this.mResolvedStageDir, targetName);
                                resolveAndStageFile(addedFile, targetFile);
                                if (apk.splitName == null) {
                                    this.mResolvedBaseFile = targetFile;
                                    baseApk = apk;
                                }
                                File dexMetadataFile = DexMetadataHelper.findDexMetadataForFile(addedFile);
                                if (dexMetadataFile != null) {
                                    if (FileUtils.isValidExtFilename(dexMetadataFile.getName())) {
                                        resolveAndStageFile(dexMetadataFile, new File(this.mResolvedStageDir, DexMetadataHelper.buildDexMetadataPathForApk(targetName)));
                                    } else {
                                        throw new PackageManagerException(-2, "Invalid filename: " + dexMetadataFile);
                                    }
                                }
                                i2++;
                                removedFiles2 = removedFiles;
                            } else {
                                throw new PackageManagerException(-2, "Invalid filename: " + targetName);
                            }
                        } else {
                            throw new PackageManagerException(-2, "Split " + apk.splitName + " was defined multiple times");
                        }
                    } catch (PackageParser.PackageParserException e) {
                        throw PackageManagerException.from(e);
                    }
                }
                PackageInstaller.SessionParams sessionParams = this.params;
                sessionParams.removeSplitList = removeSplitList;
                sessionParams.addSplitList = new ArrayList(stagedSplits);
                this.params.addSplitList.remove((Object) null);
                if (removeSplitList.size() > 0) {
                    if (pkgInfo2 != null) {
                        for (String splitName : removeSplitList) {
                            if (!ArrayUtils.contains(pkgInfo2.splitNames, splitName)) {
                                throw new PackageManagerException(-2, "Split not found: " + splitName);
                            }
                        }
                        if (this.mPackageName == null) {
                            this.mPackageName = pkgInfo2.packageName;
                            this.mVersionCode = pkgInfo2.getLongVersionCode();
                        }
                        if (this.mSigningDetails == PackageParser.SigningDetails.UNKNOWN) {
                            try {
                                this.mSigningDetails = ApkSignatureVerifier.unsafeGetCertsWithoutVerification(pkgInfo2.applicationInfo.sourceDir, 1);
                            } catch (PackageParser.PackageParserException e2) {
                                throw new PackageManagerException(-2, "Couldn't obtain signatures from base APK");
                            }
                        }
                    } else {
                        throw new PackageManagerException(-2, "Missing existing base package for " + this.mPackageName);
                    }
                }
                if (this.mIsMergePlugin) {
                    try {
                        PackageParser.PackageLite existPackage = PackageParser.parsePackageLite(new File(pkgInfo2.applicationInfo.getCodePath()), 0);
                        PackageParser.ApkLite existBase = PackageParser.parseApkLite(new File(pkgInfo2.applicationInfo.getBaseCodePath()), 32);
                        if (this.mPluginPackage.checkVersion(existBase, this.mVersionCode, this.mPluginVersionCode)) {
                            this.mPluginPackage.installMergePlugin(existPackage, stagedSplits, this.mResolvedInheritedFiles, this.mResolvedInstructionSets, mayInheritNativeLibs() ? this.mResolvedNativeLibPaths : null);
                        }
                        this.mInheritedFilesBase = new File(pkgInfo2.applicationInfo.getBaseCodePath()).getParentFile();
                        assertApkConsistentLocked(IHwPluginPackage.TAG_MERGEPLUGIN_BASE, existBase);
                    } catch (PackageParser.PackageParserException e3) {
                        throw PackageManagerException.from(e3);
                    }
                } else {
                    if (((PackageInstaller.SessionParams) this.params).mode != 1) {
                        ApplicationInfo appInfo = pkgInfo2.applicationInfo;
                        try {
                            PackageParser.PackageLite existing2 = PackageParser.parsePackageLite(new File(appInfo.getCodePath()), 0);
                            PackageParser.ApkLite existingBase3 = PackageParser.parseApkLite(new File(appInfo.getBaseCodePath()), 32);
                            assertApkConsistentLocked("Existing base", existingBase3);
                            if (this.mResolvedBaseFile == null) {
                                this.mResolvedBaseFile = new File(appInfo.getBaseCodePath());
                                resolveInheritedFile(this.mResolvedBaseFile);
                                File baseDexMetadataFile = DexMetadataHelper.findDexMetadataForFile(this.mResolvedBaseFile);
                                if (baseDexMetadataFile != null) {
                                    resolveInheritedFile(baseDexMetadataFile);
                                }
                                baseApk = existingBase3;
                            }
                            if (!ArrayUtils.isEmpty(existing2.splitNames)) {
                                for (int i3 = 0; i3 < existing2.splitNames.length; i3++) {
                                    String splitName2 = existing2.splitNames[i3];
                                    File splitFile = new File(existing2.splitCodePaths[i3]);
                                    boolean splitRemoved = removeSplitList.contains(splitName2);
                                    if (!stagedSplits.contains(splitName2) && !splitRemoved) {
                                        resolveInheritedFile(splitFile);
                                        File splitDexMetadataFile = DexMetadataHelper.findDexMetadataForFile(splitFile);
                                        if (splitDexMetadataFile != null) {
                                            resolveInheritedFile(splitDexMetadataFile);
                                        }
                                    }
                                }
                            }
                            File packageInstallDir = new File(appInfo.getBaseCodePath()).getParentFile();
                            this.mInheritedFilesBase = packageInstallDir;
                            File oatDir = new File(packageInstallDir, "oat");
                            List<File> listOatDir = new ArrayList<>();
                            listOatDir.add(oatDir);
                            if (oatDir.exists() && isLinkPossible(listOatDir, this.mResolvedStageDir)) {
                                File[] archSubdirs = oatDir.listFiles();
                                if (archSubdirs != null && archSubdirs.length > 0) {
                                    String[] instructionSets = InstructionSets.getAllDexCodeInstructionSets();
                                    int length2 = archSubdirs.length;
                                    int i4 = 0;
                                    while (i4 < length2) {
                                        File archSubDir = archSubdirs[i4];
                                        if (!ArrayUtils.contains(instructionSets, archSubDir.getName())) {
                                            i = length2;
                                        } else {
                                            i = length2;
                                            this.mResolvedInstructionSets.add(archSubDir.getName());
                                            List<File> oatFiles = Arrays.asList(archSubDir.listFiles());
                                            if (!oatFiles.isEmpty()) {
                                                this.mResolvedInheritedFiles.addAll(oatFiles);
                                            }
                                        }
                                        i4++;
                                        archSubdirs = archSubdirs;
                                        length2 = i;
                                    }
                                }
                            }
                            if (mayInheritNativeLibs() && removeSplitList.isEmpty()) {
                                int i5 = 0;
                                File[] libDirs2 = {new File(packageInstallDir, "lib"), new File(packageInstallDir, "lib64")};
                                int length3 = libDirs2.length;
                                while (i5 < length3) {
                                    File libDir = libDirs2[i5];
                                    if (!libDir.exists()) {
                                        libDirs = libDirs2;
                                        existing = existing2;
                                        existingBase = existingBase3;
                                    } else if (!libDir.isDirectory()) {
                                        libDirs = libDirs2;
                                        existing = existing2;
                                        existingBase = existingBase3;
                                    } else {
                                        List<File> libDirsToInherit3 = new LinkedList<>();
                                        File[] listFiles = libDir.listFiles();
                                        libDirs = libDirs2;
                                        int length4 = listFiles.length;
                                        existing = existing2;
                                        int i6 = 0;
                                        while (i6 < length4) {
                                            File archSubDir2 = listFiles[i6];
                                            if (!archSubDir2.isDirectory()) {
                                                fileArr = listFiles;
                                                existingBase2 = existingBase3;
                                                libDirsToInherit2 = libDirsToInherit3;
                                            } else {
                                                try {
                                                    String relLibPath = getRelativePath(archSubDir2, packageInstallDir);
                                                    fileArr = listFiles;
                                                    existingBase2 = existingBase3;
                                                    if (!this.mResolvedNativeLibPaths.contains(relLibPath)) {
                                                        this.mResolvedNativeLibPaths.add(relLibPath);
                                                    }
                                                    libDirsToInherit2 = libDirsToInherit3;
                                                    libDirsToInherit2.addAll(Arrays.asList(archSubDir2.listFiles()));
                                                } catch (IOException e4) {
                                                    existingBase = existingBase3;
                                                    libDirsToInherit = libDirsToInherit3;
                                                    Slog.e(TAG, "Skipping linking of native library directory!", e4);
                                                    libDirsToInherit.clear();
                                                }
                                            }
                                            i6++;
                                            libDirsToInherit3 = libDirsToInherit2;
                                            length4 = length4;
                                            listFiles = fileArr;
                                            existingBase3 = existingBase2;
                                        }
                                        existingBase = existingBase3;
                                        libDirsToInherit = libDirsToInherit3;
                                        this.mResolvedInheritedFiles.addAll(libDirsToInherit);
                                    }
                                    i5++;
                                    libDirs2 = libDirs;
                                    existing2 = existing;
                                    existingBase3 = existingBase;
                                }
                            }
                        } catch (PackageParser.PackageParserException e5) {
                            throw PackageManagerException.from(e5);
                        }
                    } else if (!stagedSplits.contains(null)) {
                        throw new PackageManagerException(-2, "Full install must include a base package");
                    }
                    if (baseApk.useEmbeddedDex) {
                        for (File file : this.mResolvedStagedFiles) {
                            if (file.getName().endsWith(".apk")) {
                                if (!DexManager.auditUncompressedDexInApk(file.getPath())) {
                                    throw new PackageManagerException(-2, "Some dex are not uncompressed and aligned correctly for " + this.mPackageName);
                                }
                            }
                        }
                    }
                    if (baseApk.isSplitRequired && stagedSplits.size() <= 1) {
                        throw new PackageManagerException(-28, "Missing split for " + this.mPackageName);
                    }
                }
            } else {
                throw new PackageManagerException(-2, "No packages staged");
            }
        } catch (IOException e6) {
            throw new PackageManagerException(-18, "Failed to resolve stage location", e6);
        }
    }

    private void resolveAndStageFile(File origFile, File targetFile) throws PackageManagerException {
        this.mResolvedStagedFiles.add(targetFile);
        maybeRenameFile(origFile, targetFile);
        File originalSignature = new File(VerityUtils.getFsveritySignatureFilePath(origFile.getPath()));
        if (originalSignature.exists()) {
            if (!this.mVerityFound) {
                this.mVerityFound = true;
                if (this.mResolvedStagedFiles.size() > 1) {
                    throw new PackageManagerException(-118, "Some file is missing fs-verity signature");
                }
            }
            File stagedSignature = new File(VerityUtils.getFsveritySignatureFilePath(targetFile.getPath()));
            maybeRenameFile(originalSignature, stagedSignature);
            this.mResolvedStagedFiles.add(stagedSignature);
        } else if (this.mVerityFound) {
            throw new PackageManagerException(-118, "Missing corresponding fs-verity signature to " + origFile);
        }
    }

    private void resolveInheritedFile(File origFile) {
        this.mResolvedInheritedFiles.add(origFile);
        File fsveritySignatureFile = new File(VerityUtils.getFsveritySignatureFilePath(origFile.getPath()));
        if (fsveritySignatureFile.exists()) {
            this.mResolvedInheritedFiles.add(fsveritySignatureFile);
        }
    }

    @GuardedBy({"mLock"})
    private void assertApkConsistentLocked(String tag, PackageParser.ApkLite apk) throws PackageManagerException {
        if (!this.mPackageName.equals(apk.packageName)) {
            throw new PackageManagerException(-2, tag + " package " + apk.packageName + " inconsistent with " + this.mPackageName);
        } else if (this.params.appPackageName == null || this.params.appPackageName.equals(apk.packageName)) {
            boolean pluginConsistent = true;
            IHwPluginPackage iHwPluginPackage = this.mPluginPackage;
            if (iHwPluginPackage != null) {
                pluginConsistent = iHwPluginPackage.assertPluginConsistent(tag, apk, this.mVersionCode, this.mPluginVersionCode);
            }
            if (this.mVersionCode != apk.getLongVersionCode() && pluginConsistent) {
                throw new PackageManagerException(-2, tag + " version code " + apk.versionCode + " inconsistent with " + this.mVersionCode);
            } else if (!this.mSigningDetails.signaturesMatchExactly(apk.signingDetails)) {
                throw new PackageManagerException(-2, tag + " signatures are inconsistent");
            }
        } else {
            throw new PackageManagerException(-2, tag + " specified package " + this.params.appPackageName + " inconsistent with " + apk.packageName);
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

    public long getUpdatedMillis() {
        long j;
        synchronized (this.mLock) {
            j = this.updatedMillis;
        }
        return j;
    }

    /* access modifiers changed from: package-private */
    public String getInstallerPackageName() {
        String str;
        synchronized (this.mLock) {
            str = this.mInstallerPackageName;
        }
        return str;
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

    private void copyFiles(List<File> fromFiles, File toDir, File fromDir) throws IOException {
        File[] listFiles = toDir.listFiles();
        for (File file : listFiles) {
            if (file.getName().endsWith(".tmp")) {
                file.delete();
            }
        }
        int i = 1;
        boolean isTryBindLink = SystemProperties.getBoolean("ro.plugin.useBindMount", true);
        for (File fromFile : fromFiles) {
            String relativePath = getRelativePath(fromFile, fromDir);
            if (isTryBindLink) {
                File[] fileArr = new File[i];
                fileArr[0] = fromFile;
                if (isLinkPossible(Arrays.asList(fileArr), toDir)) {
                    File[] fileArr2 = new File[i];
                    fileArr2[0] = fromFile;
                    linkFiles(Arrays.asList(fileArr2), toDir, fromDir);
                    Slog.d(TAG, "linkFile success: " + relativePath + " from: " + fromDir + " -> " + toDir);
                } else {
                    try {
                        if (this.mPm.mInstaller.bindFile(relativePath, fromDir.getAbsolutePath(), toDir.getAbsolutePath())) {
                            Slog.d(TAG, "bindFile success: " + relativePath + " for: " + fromDir + " -> " + toDir);
                        }
                    } catch (Installer.InstallerException e) {
                        Slog.e(TAG, "failed bindFile :" + fromFile + " -> " + toDir);
                    }
                }
            }
            File fileToDir = new File(toDir + getRelativePath(fromFile.getParentFile(), fromDir));
            File tmpFile = File.createTempFile("inherit", ".tmp", fileToDir);
            Slog.d(TAG, "Copying " + fromFile + " to " + tmpFile);
            if (FileUtils.copyFile(fromFile, tmpFile)) {
                try {
                    Os.chmod(tmpFile.getAbsolutePath(), 420);
                    File toFile = new File(fileToDir, fromFile.getName());
                    Slog.d(TAG, "Renaming " + tmpFile + " to " + toFile);
                    if (tmpFile.renameTo(toFile)) {
                        i = 1;
                    } else {
                        throw new IOException("Failed to rename " + tmpFile + " to " + toFile);
                    }
                } catch (ErrnoException e2) {
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

    /* access modifiers changed from: package-private */
    public void addChildSessionIdInternal(int sessionId2) {
        this.mChildSessionIds.put(sessionId2, 0);
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
                    boolean isPathExist = false;
                    String path = "";
                    if (!TextUtils.isEmpty(this.params.existingPath)) {
                        File file = new File(this.params.existingPath);
                        isPathExist = file.exists();
                        path = file.getCanonicalPath();
                    }
                    Slog.i(TAG, "existingPath:" + this.params.existingPath + ",isPathExist:" + isPathExist);
                    if (!isPathExist || TextUtils.isEmpty(path) || this.mPm.checkUidPermission("android.permission.INSTALL_PACKAGES", Binder.getCallingUid()) != 0) {
                        this.params.existingPath = null;
                        PackageInstallerService.prepareStageDir(this.stageDir);
                    } else {
                        installAppWithExistingPath(path);
                    }
                } else if (!this.params.isMultiPackage) {
                    throw new IllegalArgumentException("stageDir must be set");
                }
                this.mPrepared = true;
            }
        }
        if (!wasPrepared) {
            this.mCallback.onSessionPrepared(this);
        }
    }

    private void installAppWithExistingPath(String existingPath) throws IOException {
        Slog.i(TAG, "install with existing path:" + this.params.existingPath);
        try {
            this.mPm.mInstaller.renameAppInstallPath(existingPath, this.stageDir.getCanonicalPath(), false);
            if (!SELinux.restoreconRecursive(this.stageDir)) {
                throw new IOException("Failed to restorecon session dir: " + this.stageDir);
            }
        } catch (Installer.InstallerException e) {
            throw new IOException("Failed to renameAppInstallPath: " + this.params.existingPath);
        }
    }

    public void close() {
        closeInternal(true);
    }

    private void closeInternal(boolean checkCaller) {
        int activeCount;
        synchronized (this.mLock) {
            if (checkCaller) {
                assertCallerIsOwnerOrRootLocked();
            }
            activeCount = this.mActiveCount.decrementAndGet();
        }
        if (activeCount == 0) {
            this.mCallback.onSessionActiveChanged(this, false);
        }
    }

    public void abandon() {
        if (!hasParentSessionId()) {
            synchronized (this.mLock) {
                assertCallerIsOwnerOrRootLocked();
                if (!isStagedAndInTerminalState()) {
                    if (this.mCommitted && this.params.isStaged) {
                        synchronized (this.mLock) {
                            this.mDestroyed = true;
                        }
                        this.mStagingManager.abortCommittedSession(this);
                        cleanStageDir();
                    }
                    if (this.mRelinquished) {
                        Slog.d(TAG, "Ignoring abandon after commit relinquished control");
                        return;
                    }
                    destroyInternal();
                    dispatchSessionFinished(-115, "Session was abandoned", null);
                    return;
                }
                return;
            }
        }
        throw new IllegalStateException("Session " + this.sessionId + " is a child of multi-package session " + this.mParentSessionId + " and may not be abandoned directly.");
    }

    public boolean isMultiPackage() {
        return this.params.isMultiPackage;
    }

    public boolean isStaged() {
        return this.params.isStaged;
    }

    public int[] getChildSessionIds() {
        int[] childSessionIds = this.mChildSessionIds.copyKeys();
        if (childSessionIds != null) {
            return childSessionIds;
        }
        return EMPTY_CHILD_SESSION_ARRAY;
    }

    public void addChildSessionId(int childSessionId) {
        PackageInstallerSession childSession = this.mSessionProvider.getSession(childSessionId);
        if (childSession == null || ((childSession.hasParentSessionId() && childSession.mParentSessionId != this.sessionId) || childSession.mCommitted || childSession.mDestroyed)) {
            throw new IllegalStateException("Unable to add child session " + childSessionId + " as it does not exist or is in an invalid state.");
        }
        synchronized (this.mLock) {
            assertCallerIsOwnerOrRootLocked();
            assertPreparedAndNotSealedLocked("addChildSessionId");
            if (this.mChildSessionIds.indexOfKey(childSessionId) < 0) {
                childSession.setParentSessionId(this.sessionId);
                addChildSessionIdInternal(childSessionId);
            }
        }
    }

    public void removeChildSessionId(int sessionId2) {
        PackageInstallerSession session = this.mSessionProvider.getSession(sessionId2);
        synchronized (this.mLock) {
            int indexOfSession = this.mChildSessionIds.indexOfKey(sessionId2);
            if (session != null) {
                session.setParentSessionId(-1);
            }
            if (indexOfSession >= 0) {
                this.mChildSessionIds.removeAt(indexOfSession);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setParentSessionId(int parentSessionId) {
        synchronized (this.mLock) {
            if (parentSessionId != -1) {
                if (this.mParentSessionId != -1) {
                    throw new IllegalStateException("The parent of " + this.sessionId + " is alreadyset to " + this.mParentSessionId);
                }
            }
            this.mParentSessionId = parentSessionId;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasParentSessionId() {
        return this.mParentSessionId != -1;
    }

    public int getParentSessionId() {
        return this.mParentSessionId;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchSessionFinished(int returnCode, String msg, Bundle extras) {
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
        if (success && isNewInstall && this.mPm.mInstallerService.okToSendBroadcasts()) {
            this.mPm.sendSessionCommitBroadcast(generateInfoScrubbed(true), this.userId);
        }
        this.mCallback.onSessionFinished(this, success);
    }

    /* access modifiers changed from: package-private */
    public void setStagedSessionReady() {
        synchronized (this.mLock) {
            this.mStagedSessionReady = true;
            this.mStagedSessionApplied = false;
            this.mStagedSessionFailed = false;
            this.mStagedSessionErrorCode = 0;
            this.mStagedSessionErrorMessage = "";
        }
        this.mCallback.onStagedSessionChanged(this);
    }

    /* access modifiers changed from: package-private */
    public void setStagedSessionFailed(int errorCode, String errorMessage) {
        synchronized (this.mLock) {
            this.mStagedSessionReady = false;
            this.mStagedSessionApplied = false;
            this.mStagedSessionFailed = true;
            this.mStagedSessionErrorCode = errorCode;
            this.mStagedSessionErrorMessage = errorMessage;
            Slog.d(TAG, "Marking session " + this.sessionId + " as failed: " + errorMessage);
        }
        cleanStageDir();
        this.mCallback.onStagedSessionChanged(this);
    }

    /* access modifiers changed from: package-private */
    public void setStagedSessionApplied() {
        synchronized (this.mLock) {
            this.mStagedSessionReady = false;
            this.mStagedSessionApplied = true;
            this.mStagedSessionFailed = false;
            this.mStagedSessionErrorCode = 0;
            this.mStagedSessionErrorMessage = "";
            Slog.d(TAG, "Marking session " + this.sessionId + " as applied");
        }
        cleanStageDir();
        this.mCallback.onStagedSessionChanged(this);
    }

    /* access modifiers changed from: package-private */
    public boolean isStagedSessionReady() {
        return this.mStagedSessionReady;
    }

    /* access modifiers changed from: package-private */
    public boolean isStagedSessionApplied() {
        return this.mStagedSessionApplied;
    }

    /* access modifiers changed from: package-private */
    public boolean isStagedSessionFailed() {
        return this.mStagedSessionFailed;
    }

    /* access modifiers changed from: package-private */
    public int getStagedSessionErrorCode() {
        return this.mStagedSessionErrorCode;
    }

    /* access modifiers changed from: package-private */
    public String getStagedSessionErrorMessage() {
        return this.mStagedSessionErrorMessage;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void destroyInternal() {
        synchronized (this.mLock) {
            this.mSealed = true;
            if (!this.params.isStaged || isStagedAndInTerminalState()) {
                this.mDestroyed = true;
            }
            Iterator<RevocableFileDescriptor> it = this.mFds.iterator();
            while (it.hasNext()) {
                it.next().revoke();
            }
            Iterator<FileBridge> it2 = this.mBridges.iterator();
            while (it2.hasNext()) {
                it2.next().forceClose();
            }
        }
        if (this.stageDir != null && !this.params.isStaged && this.stageDir.exists()) {
            destroyInternalExistingPath();
            try {
                this.mPm.mInstaller.rmPackageDir(this.stageDir.getAbsolutePath());
            } catch (Installer.InstallerException e) {
            }
        }
    }

    private void destroyInternalExistingPath() {
        if (!TextUtils.isEmpty(this.params.existingPath)) {
            try {
                File[] files = this.stageDir.listFiles();
                if (files != null) {
                    for (File getFile : files) {
                        if (!getFile.isFile()) {
                            this.mPm.mInstaller.rmPackageDir(getFile.getCanonicalPath());
                        }
                    }
                }
                this.mPm.mInstaller.renameAppInstallPath(this.stageDir.getCanonicalPath(), this.params.existingPath, true);
            } catch (Installer.InstallerException | IOException e) {
                Slog.i(TAG, "destroyInternal rename revert failed");
            }
            if (!SELinux.restoreconRecursive(new File(this.params.existingPath))) {
                Slog.i(TAG, "Failed to restorecon session dir: " + this.params.existingPath);
            }
        }
    }

    private void cleanStageDir() {
        if (isMultiPackage()) {
            for (int childSessionId : getChildSessionIds()) {
                PackageInstallerSession sessionGotFromId = this.mSessionProvider.getSession(childSessionId);
                if (sessionGotFromId != null) {
                    sessionGotFromId.cleanStageDir();
                }
            }
            return;
        }
        try {
            this.mPm.mInstaller.rmPackageDir(this.stageDir.getAbsolutePath());
        } catch (Installer.InstallerException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(IndentingPrintWriter pw) {
        synchronized (this.mLock) {
            dumpLocked(pw);
        }
    }

    @GuardedBy({"mLock"})
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
        pw.printPair("mCommitted", Boolean.valueOf(this.mCommitted));
        pw.printPair("mSealed", Boolean.valueOf(this.mSealed));
        pw.printPair("mPermissionsManuallyAccepted", Boolean.valueOf(this.mPermissionsManuallyAccepted));
        pw.printPair("mRelinquished", Boolean.valueOf(this.mRelinquished));
        pw.printPair("mDestroyed", Boolean.valueOf(this.mDestroyed));
        pw.printPair("mFds", Integer.valueOf(this.mFds.size()));
        pw.printPair("mBridges", Integer.valueOf(this.mBridges.size()));
        pw.printPair("mFinalStatus", Integer.valueOf(this.mFinalStatus));
        pw.printPair("mFinalMessage", this.mFinalMessage);
        pw.printPair("params.isMultiPackage", Boolean.valueOf(this.params.isMultiPackage));
        pw.printPair("params.isStaged", Boolean.valueOf(this.params.isStaged));
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

    private static void writeWhitelistedRestrictedPermissionsLocked(XmlSerializer out, List<String> whitelistedRestrictedPermissions) throws IOException {
        if (whitelistedRestrictedPermissions != null) {
            int permissionCount = whitelistedRestrictedPermissions.size();
            for (int i = 0; i < permissionCount; i++) {
                out.startTag(null, TAG_WHITELISTED_RESTRICTED_PERMISSION);
                XmlUtils.writeStringAttribute(out, "name", whitelistedRestrictedPermissions.get(i));
                out.endTag(null, TAG_WHITELISTED_RESTRICTED_PERMISSION);
            }
        }
    }

    private static File buildAppIconFile(int sessionId2, File sessionsDir) {
        return new File(sessionsDir, "app_icon." + sessionId2 + ".png");
    }

    /* access modifiers changed from: package-private */
    public void write(XmlSerializer out, File sessionsDir) throws IOException {
        synchronized (this.mLock) {
            if (!this.mDestroyed) {
                out.startTag(null, TAG_SESSION);
                XmlUtils.writeIntAttribute(out, ATTR_SESSION_ID, this.sessionId);
                XmlUtils.writeIntAttribute(out, ATTR_USER_ID, this.userId);
                XmlUtils.writeStringAttribute(out, ATTR_INSTALLER_PACKAGE_NAME, this.mInstallerPackageName);
                XmlUtils.writeIntAttribute(out, ATTR_INSTALLER_UID, this.mInstallerUid);
                XmlUtils.writeLongAttribute(out, ATTR_CREATED_MILLIS, this.createdMillis);
                XmlUtils.writeLongAttribute(out, ATTR_UPDATED_MILLIS, this.updatedMillis);
                if (this.stageDir != null) {
                    XmlUtils.writeStringAttribute(out, ATTR_SESSION_STAGE_DIR, this.stageDir.getAbsolutePath());
                }
                if (this.stageCid != null) {
                    XmlUtils.writeStringAttribute(out, ATTR_SESSION_STAGE_CID, this.stageCid);
                }
                XmlUtils.writeBooleanAttribute(out, ATTR_PREPARED, isPrepared());
                XmlUtils.writeBooleanAttribute(out, ATTR_COMMITTED, isCommitted());
                XmlUtils.writeBooleanAttribute(out, ATTR_SEALED, isSealed());
                XmlUtils.writeBooleanAttribute(out, ATTR_MULTI_PACKAGE, this.params.isMultiPackage);
                XmlUtils.writeBooleanAttribute(out, ATTR_STAGED_SESSION, this.params.isStaged);
                XmlUtils.writeBooleanAttribute(out, ATTR_IS_READY, this.mStagedSessionReady);
                XmlUtils.writeBooleanAttribute(out, ATTR_IS_FAILED, this.mStagedSessionFailed);
                XmlUtils.writeBooleanAttribute(out, ATTR_IS_APPLIED, this.mStagedSessionApplied);
                XmlUtils.writeIntAttribute(out, ATTR_STAGED_SESSION_ERROR_CODE, this.mStagedSessionErrorCode);
                XmlUtils.writeStringAttribute(out, ATTR_STAGED_SESSION_ERROR_MESSAGE, this.mStagedSessionErrorMessage);
                XmlUtils.writeIntAttribute(out, ATTR_PARENT_SESSION_ID, this.mParentSessionId);
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
                writeWhitelistedRestrictedPermissionsLocked(out, this.params.whitelistedRestrictedPermissions);
                File appIconFile = buildAppIconFile(this.sessionId, sessionsDir);
                if (this.params.appIcon == null && appIconFile.exists()) {
                    appIconFile.delete();
                } else if (!(this.params.appIcon == null || appIconFile.lastModified() == this.params.appIconLastModified)) {
                    Slog.w(TAG, "Writing changed icon " + appIconFile);
                    FileOutputStream os = null;
                    try {
                        os = new FileOutputStream(appIconFile);
                        this.params.appIcon.compress(Bitmap.CompressFormat.PNG, 90, os);
                    } catch (IOException e) {
                        Slog.w(TAG, "Failed to write icon " + appIconFile + ": " + e.getMessage());
                    } finally {
                        IoUtils.closeQuietly(os);
                    }
                    this.params.appIconLastModified = appIconFile.lastModified();
                }
                int[] childSessionIds = getChildSessionIds();
                for (int childSessionId : childSessionIds) {
                    out.startTag(null, TAG_CHILD_SESSION);
                    XmlUtils.writeIntAttribute(out, ATTR_SESSION_ID, childSessionId);
                    out.endTag(null, TAG_CHILD_SESSION);
                }
                out.endTag(null, TAG_SESSION);
            }
        }
    }

    private static boolean isStagedSessionStateValid(boolean isReady, boolean isApplied, boolean isFailed) {
        return (!isReady && !isApplied && !isFailed) || (isReady && !isApplied && !isFailed) || ((!isReady && isApplied && !isFailed) || (!isReady && !isApplied && isFailed));
    }

    public static PackageInstallerSession readFromXml(XmlPullParser in, PackageInstallerService.InternalCallback callback, Context context, PackageManagerService pm, Looper installerThread, StagingManager stagingManager, File sessionsDir, PackageSessionProvider sessionProvider) throws IOException, XmlPullParserException {
        int outerDepth;
        List<Integer> childSessionIds;
        int type;
        int[] childSessionIdsArray;
        int sessionId2 = XmlUtils.readIntAttribute(in, ATTR_SESSION_ID);
        int userId2 = XmlUtils.readIntAttribute(in, ATTR_USER_ID);
        String installerPackageName = XmlUtils.readStringAttribute(in, ATTR_INSTALLER_PACKAGE_NAME);
        int installerUid = XmlUtils.readIntAttribute(in, ATTR_INSTALLER_UID, pm.getPackageUid(installerPackageName, 8192, userId2));
        long createdMillis2 = XmlUtils.readLongAttribute(in, ATTR_CREATED_MILLIS);
        XmlUtils.readLongAttribute(in, ATTR_UPDATED_MILLIS);
        String stageDirRaw = XmlUtils.readStringAttribute(in, ATTR_SESSION_STAGE_DIR);
        File stageDir2 = stageDirRaw != null ? new File(stageDirRaw) : null;
        String stageCid2 = XmlUtils.readStringAttribute(in, ATTR_SESSION_STAGE_CID);
        int i = 1;
        boolean prepared = XmlUtils.readBooleanAttribute(in, ATTR_PREPARED, true);
        boolean committed = XmlUtils.readBooleanAttribute(in, ATTR_COMMITTED);
        boolean sealed = XmlUtils.readBooleanAttribute(in, ATTR_SEALED);
        int parentSessionId = XmlUtils.readIntAttribute(in, ATTR_PARENT_SESSION_ID, -1);
        PackageInstaller.SessionParams params2 = new PackageInstaller.SessionParams(-1);
        params2.isMultiPackage = XmlUtils.readBooleanAttribute(in, ATTR_MULTI_PACKAGE, false);
        params2.isStaged = XmlUtils.readBooleanAttribute(in, ATTR_STAGED_SESSION, false);
        params2.mode = XmlUtils.readIntAttribute(in, ATTR_MODE);
        params2.installFlags = XmlUtils.readIntAttribute(in, ATTR_INSTALL_FLAGS);
        params2.installLocation = XmlUtils.readIntAttribute(in, ATTR_INSTALL_LOCATION);
        params2.sizeBytes = XmlUtils.readLongAttribute(in, ATTR_SIZE_BYTES);
        params2.appPackageName = XmlUtils.readStringAttribute(in, ATTR_APP_PACKAGE_NAME);
        params2.appIcon = XmlUtils.readBitmapAttribute(in, ATTR_APP_ICON);
        params2.appLabel = XmlUtils.readStringAttribute(in, ATTR_APP_LABEL);
        params2.originatingUri = XmlUtils.readUriAttribute(in, ATTR_ORIGINATING_URI);
        params2.originatingUid = XmlUtils.readIntAttribute(in, ATTR_ORIGINATING_UID, -1);
        params2.referrerUri = XmlUtils.readUriAttribute(in, ATTR_REFERRER_URI);
        params2.abiOverride = XmlUtils.readStringAttribute(in, ATTR_ABI_OVERRIDE);
        params2.volumeUuid = XmlUtils.readStringAttribute(in, ATTR_VOLUME_UUID);
        params2.installReason = XmlUtils.readIntAttribute(in, ATTR_INSTALL_REASON);
        File appIconFile = buildAppIconFile(sessionId2, sessionsDir);
        if (appIconFile.exists()) {
            params2.appIcon = BitmapFactory.decodeFile(appIconFile.getAbsolutePath());
            params2.appIconLastModified = appIconFile.lastModified();
        }
        boolean isReady = XmlUtils.readBooleanAttribute(in, ATTR_IS_READY);
        boolean isFailed = XmlUtils.readBooleanAttribute(in, ATTR_IS_FAILED);
        boolean isApplied = XmlUtils.readBooleanAttribute(in, ATTR_IS_APPLIED);
        int stagedSessionErrorCode = XmlUtils.readIntAttribute(in, ATTR_STAGED_SESSION_ERROR_CODE, 0);
        String stagedSessionErrorMessage = XmlUtils.readStringAttribute(in, ATTR_STAGED_SESSION_ERROR_MESSAGE);
        if (isStagedSessionStateValid(isReady, isApplied, isFailed)) {
            List<String> grantedRuntimePermissions = new ArrayList<>();
            List<String> whitelistedRestrictedPermissions = new ArrayList<>();
            List<Integer> childSessionIds2 = new ArrayList<>();
            int outerDepth2 = in.getDepth();
            while (true) {
                int type2 = in.next();
                if (type2 == i) {
                    outerDepth = outerDepth2;
                    childSessionIds = childSessionIds2;
                    type = type2;
                    break;
                }
                type = type2;
                if (type == 3 && in.getDepth() <= outerDepth2) {
                    outerDepth = outerDepth2;
                    childSessionIds = childSessionIds2;
                    break;
                } else if (type == 3) {
                    isApplied = isApplied;
                    i = 1;
                } else if (type == 4) {
                    isApplied = isApplied;
                    i = 1;
                } else {
                    if (TAG_GRANTED_RUNTIME_PERMISSION.equals(in.getName())) {
                        grantedRuntimePermissions.add(XmlUtils.readStringAttribute(in, "name"));
                    }
                    if (TAG_WHITELISTED_RESTRICTED_PERMISSION.equals(in.getName())) {
                        whitelistedRestrictedPermissions.add(XmlUtils.readStringAttribute(in, "name"));
                    }
                    if (TAG_CHILD_SESSION.equals(in.getName())) {
                        childSessionIds2.add(Integer.valueOf(XmlUtils.readIntAttribute(in, ATTR_SESSION_ID, -1)));
                        isApplied = isApplied;
                        outerDepth2 = outerDepth2;
                        i = 1;
                    } else {
                        isApplied = isApplied;
                        outerDepth2 = outerDepth2;
                        i = 1;
                    }
                }
            }
            if (grantedRuntimePermissions.size() > 0) {
                params2.grantedRuntimePermissions = (String[]) grantedRuntimePermissions.stream().toArray($$Lambda$PackageInstallerSession$7SecathzbWSLkwYbdNW1Dgq0jU.INSTANCE);
            }
            if (whitelistedRestrictedPermissions.size() > 0) {
                params2.whitelistedRestrictedPermissions = whitelistedRestrictedPermissions;
            }
            if (childSessionIds.size() > 0) {
                childSessionIdsArray = childSessionIds.stream().mapToInt($$Lambda$PackageInstallerSession$fMSKA3sU8iwLB8uwZHGaXjhFI.INSTANCE).toArray();
            } else {
                childSessionIdsArray = EMPTY_CHILD_SESSION_ARRAY;
            }
            return new PackageInstallerSession(callback, context, pm, sessionProvider, installerThread, stagingManager, sessionId2, userId2, installerPackageName, installerUid, params2, createdMillis2, stageDir2, stageCid2, prepared, committed, sealed, childSessionIdsArray, parentSessionId, isReady, isFailed, isApplied, stagedSessionErrorCode, stagedSessionErrorMessage);
        }
        throw new IllegalArgumentException("Can't restore staged session with invalid state.");
    }

    static /* synthetic */ String[] lambda$readFromXml$1(int x$0) {
        return new String[x$0];
    }

    static int readChildSessionIdFromXml(XmlPullParser in) {
        return XmlUtils.readIntAttribute(in, ATTR_SESSION_ID, -1);
    }
}
