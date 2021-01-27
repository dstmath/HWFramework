package com.android.server;

import android.app.ActivityManager;
import android.app.ActivityManagerInternal;
import android.app.IActivityManager;
import android.app.IUidObserver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.app.ResolverActivity;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.ZygoteInit;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.function.pooled.PooledLambda;
import com.android.server.pm.DumpState;
import com.android.server.pm.Installer;
import com.android.server.wm.ActivityTaskManagerInternal;
import dalvik.system.DexFile;
import dalvik.system.VMRuntime;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class PinnerService extends SystemService {
    private static final int ARY_SIZ = 2;
    private static final long B_TO_G = 1073741824;
    private static final boolean DEBUG = false;
    private static final boolean ENV_MAPLE = ZygoteInit.sIsMygote;
    private static final int IDX_FST = 0;
    private static final int IDX_SEC = 1;
    private static final int KEY_CAMERA = 0;
    private static final int KEY_HOME = 1;
    private static final int MATCH_FLAGS = 851968;
    private static final int MAX_CAMERA_PIN_SIZE = 83886080;
    private static final int MAX_HOME_PIN_SIZE = 6291456;
    private static final int PAGE_SIZE = ((int) Os.sysconf(OsConstants._SC_PAGESIZE));
    private static final String PIN_META_FILENAME = "pinlist.meta";
    private static final String TAG = "PinnerService";
    private final IActivityManager mAm;
    private final ActivityManagerInternal mAmInternal;
    private final ActivityTaskManagerInternal mAtmInternal;
    private BinderService mBinderService;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.PinnerService.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.PACKAGE_REPLACED".equals(intent.getAction())) {
                String packageName = intent.getData().getSchemeSpecificPart();
                ArraySet<String> updatedPackages = new ArraySet<>();
                updatedPackages.add(packageName);
                PinnerService.this.update(updatedPackages, true);
            }
        }
    };
    private final Context mContext;
    private Installer mInstaller;
    @GuardedBy({"this"})
    private final ArrayMap<Integer, Integer> mPendingRepin = new ArrayMap<>();
    private final ArraySet<Integer> mPinKeys = new ArraySet<>();
    @GuardedBy({"this"})
    private final ArrayMap<Integer, PinnedApp> mPinnedApps = new ArrayMap<>();
    @GuardedBy({"this"})
    private final ArrayList<PinnedFile> mPinnedFiles = new ArrayList<>();
    private PinnerHandler mPinnerHandler = null;
    private final UserManager mUserManager;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AppKey {
    }

    public void setInstaller(Installer installer) {
        this.mInstaller = installer;
    }

    public PinnerService(Context context) {
        super(context);
        this.mContext = context;
        boolean shouldPinCamera = context.getResources().getBoolean(17891494);
        boolean shouldPinHome = context.getResources().getBoolean(17891495);
        if (shouldPinCamera) {
            this.mPinKeys.add(0);
        }
        if (shouldPinHome) {
            this.mPinKeys.add(1);
        }
        this.mPinnerHandler = new PinnerHandler(BackgroundThread.get().getLooper());
        this.mAtmInternal = (ActivityTaskManagerInternal) LocalServices.getService(ActivityTaskManagerInternal.class);
        this.mAmInternal = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
        this.mAm = ActivityManager.getService();
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        registerUidListener();
        registerUserSetupCompleteListener();
    }

    @Override // com.android.server.SystemService
    public void onStart() {
        this.mBinderService = new BinderService();
        publishBinderService("pinner", this.mBinderService);
        publishLocalService(PinnerService.class, this);
        this.mPinnerHandler.obtainMessage(4001).sendToTarget();
        sendPinAppsMessage(0);
    }

    @Override // com.android.server.SystemService
    public void onSwitchUser(int userHandle) {
        if (!this.mUserManager.isManagedProfile(userHandle)) {
            sendPinAppsMessage(userHandle);
        }
    }

    @Override // com.android.server.SystemService
    public void onUnlockUser(int userHandle) {
        if (!this.mUserManager.isManagedProfile(userHandle)) {
            sendPinAppsMessage(userHandle);
        }
    }

    public void update(ArraySet<String> updatedPackages, boolean force) {
        int currentUser = ActivityManager.getCurrentUser();
        for (int i = this.mPinKeys.size() - 1; i >= 0; i--) {
            int key = this.mPinKeys.valueAt(i).intValue();
            ApplicationInfo info = getInfoForKey(key, currentUser);
            if (info != null && updatedPackages.contains(info.packageName)) {
                Slog.i(TAG, "Updating pinned files for " + info.packageName + " force=" + force);
                sendPinAppMessage(key, currentUser, force);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePinOnStart() {
        String[] filesToPin;
        int deviceMemory;
        if (SystemProperties.get("dalvik.vm.boot-image", "").endsWith("apex.art")) {
            filesToPin = this.mContext.getResources().getStringArray(17235983);
        } else {
            filesToPin = this.mContext.getResources().getStringArray(17236005);
        }
        String ssClassPath = System.getenv("SYSTEMSERVERCLASSPATH");
        int length = filesToPin.length;
        int deviceMemory2 = Integer.MIN_VALUE;
        for (int deviceMemory3 = 0; deviceMemory3 < length; deviceMemory3++) {
            String fileToPin = filesToPin[deviceMemory3];
            if (fileToPin == null || !fileToPin.contains(":")) {
                deviceMemory = deviceMemory2;
            } else {
                try {
                    String[] tmpArray = fileToPin.split(":");
                    if (tmpArray.length != 2) {
                        Slog.w(TAG, fileToPin + " is not valid");
                    } else {
                        if (deviceMemory2 == Integer.MIN_VALUE) {
                            deviceMemory2 = (int) Math.ceil(((double) Process.getTotalMemory()) / 1.073741824E9d);
                        }
                        if (deviceMemory2 >= Integer.parseInt(tmpArray[0])) {
                            fileToPin = tmpArray[1];
                            deviceMemory = deviceMemory2;
                        } else {
                            continue;
                        }
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "Errors happened when pin file " + fileToPin, e);
                }
            }
            if (ENV_MAPLE && checkShouldPinSkip(ssClassPath, fileToPin)) {
                Slog.i(TAG, "handlePinOnStart for maple skip to pin: " + fileToPin);
            } else if (ENV_MAPLE || fileToPin == null || !fileToPin.startsWith("/system/lib64/libmaple")) {
                PinnedFile pf = pinFile(fileToPin, Integer.MAX_VALUE, false);
                if (pf == null) {
                    Slog.e(TAG, "Failed to pin file = " + fileToPin);
                } else {
                    synchronized (this) {
                        this.mPinnedFiles.add(pf);
                    }
                }
            }
            deviceMemory2 = deviceMemory;
        }
    }

    private void registerUserSetupCompleteListener() {
        final Uri userSetupCompleteUri = Settings.Secure.getUriFor("user_setup_complete");
        this.mContext.getContentResolver().registerContentObserver(userSetupCompleteUri, false, new ContentObserver(null) {
            /* class com.android.server.PinnerService.AnonymousClass2 */

            @Override // android.database.ContentObserver
            public void onChange(boolean selfChange, Uri uri) {
                if (userSetupCompleteUri.equals(uri)) {
                    PinnerService.this.sendPinAppMessage(1, ActivityManager.getCurrentUser(), true);
                }
            }
        }, -1);
    }

    private void registerUidListener() {
        try {
            this.mAm.registerUidObserver(new IUidObserver.Stub() {
                /* class com.android.server.PinnerService.AnonymousClass3 */

                public void onUidGone(int uid, boolean disabled) throws RemoteException {
                    PinnerService.this.mPinnerHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$PinnerService$3$RQBbrt9b8esLBxJImxDgVTsP34I.INSTANCE, PinnerService.this, Integer.valueOf(uid)));
                }

                public void onUidActive(int uid) throws RemoteException {
                    PinnerService.this.mPinnerHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$PinnerService$3$3Ta6TX4Jq9YbpUYE5Y0r8Xt8rBw.INSTANCE, PinnerService.this, Integer.valueOf(uid)));
                }

                public void onUidIdle(int uid, boolean disabled) throws RemoteException {
                }

                public void onUidStateChanged(int uid, int procState, long procStateSeq) throws RemoteException {
                }

                public void onUidCachedChanged(int uid, boolean cached) throws RemoteException {
                }
            }, 10, 0, "system");
        } catch (RemoteException e) {
            Slog.e(TAG, "Failed to register uid observer", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleUidGone(int uid) {
        updateActiveState(uid, false);
        synchronized (this) {
            int key = this.mPendingRepin.getOrDefault(Integer.valueOf(uid), -1).intValue();
            if (key != -1) {
                this.mPendingRepin.remove(Integer.valueOf(uid));
                pinApp(key, ActivityManager.getCurrentUser(), false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    public void handleUidActive(int uid) {
        updateActiveState(uid, true);
    }

    private void updateActiveState(int uid, boolean active) {
        synchronized (this) {
            for (int i = this.mPinnedApps.size() - 1; i >= 0; i--) {
                PinnedApp app = this.mPinnedApps.valueAt(i);
                if (app.uid == uid) {
                    app.active = active;
                }
            }
        }
    }

    private void unpinApp(int key) {
        ArrayList<PinnedFile> pinnedAppFiles;
        synchronized (this) {
            PinnedApp app = this.mPinnedApps.get(Integer.valueOf(key));
            if (app != null) {
                this.mPinnedApps.remove(Integer.valueOf(key));
                pinnedAppFiles = new ArrayList<>(app.mFiles);
            } else {
                return;
            }
        }
        Iterator<PinnedFile> it = pinnedAppFiles.iterator();
        while (it.hasNext()) {
            it.next().close();
        }
    }

    private boolean isResolverActivity(ActivityInfo info) {
        return ResolverActivity.class.getName().equals(info.name);
    }

    private ApplicationInfo getCameraInfo(int userHandle) {
        ApplicationInfo info = getApplicationInfoForIntent(new Intent("android.media.action.STILL_IMAGE_CAMERA"), userHandle, false);
        if (info == null) {
            info = getApplicationInfoForIntent(new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE"), userHandle, false);
        }
        if (info == null) {
            return getApplicationInfoForIntent(new Intent("android.media.action.STILL_IMAGE_CAMERA"), userHandle, true);
        }
        return info;
    }

    private ApplicationInfo getHomeInfo(int userHandle) {
        return getApplicationInfoForIntent(this.mAtmInternal.getHomeIntent(), userHandle, false);
    }

    private ApplicationInfo getApplicationInfoForIntent(Intent intent, int userHandle, boolean defaultToSystemApp) {
        ResolveInfo resolveInfo;
        if (intent == null || (resolveInfo = this.mContext.getPackageManager().resolveActivityAsUser(intent, MATCH_FLAGS, userHandle)) == null) {
            return null;
        }
        if (!isResolverActivity(resolveInfo.activityInfo)) {
            return resolveInfo.activityInfo.applicationInfo;
        }
        if (!defaultToSystemApp) {
            return null;
        }
        ApplicationInfo systemAppInfo = null;
        for (ResolveInfo info : this.mContext.getPackageManager().queryIntentActivitiesAsUser(intent, MATCH_FLAGS, userHandle)) {
            if ((info.activityInfo.applicationInfo.flags & 1) != 0) {
                if (systemAppInfo != null) {
                    return null;
                }
                systemAppInfo = info.activityInfo.applicationInfo;
            }
        }
        return systemAppInfo;
    }

    private void sendPinAppsMessage(int userHandle) {
        this.mPinnerHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$PinnerService$GeEX8XoHeV0LEszxat7jOSlrs4.INSTANCE, this, Integer.valueOf(userHandle)));
    }

    /* access modifiers changed from: private */
    public void pinApps(int userHandle) {
        for (int i = this.mPinKeys.size() - 1; i >= 0; i--) {
            pinApp(this.mPinKeys.valueAt(i).intValue(), userHandle, true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendPinAppMessage(int key, int userHandle, boolean force) {
        this.mPinnerHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$PinnerService$6bekYOn4YXi0x7vYNWO40QyAs8.INSTANCE, this, Integer.valueOf(key), Integer.valueOf(userHandle), Boolean.valueOf(force)));
    }

    /* access modifiers changed from: private */
    public void pinApp(int key, int userHandle, boolean force) {
        int uid = getUidForKey(key);
        if (force || uid == -1) {
            unpinApp(key);
            ApplicationInfo info = getInfoForKey(key, userHandle);
            if (info != null) {
                pinApp(key, info);
                return;
            }
            return;
        }
        synchronized (this) {
            this.mPendingRepin.put(Integer.valueOf(uid), Integer.valueOf(key));
        }
    }

    private int getUidForKey(int key) {
        int i;
        synchronized (this) {
            PinnedApp existing = this.mPinnedApps.get(Integer.valueOf(key));
            if (existing == null || !existing.active) {
                i = -1;
            } else {
                i = existing.uid;
            }
        }
        return i;
    }

    private ApplicationInfo getInfoForKey(int key, int userHandle) {
        if (key == 0) {
            return getCameraInfo(userHandle);
        }
        if (key != 1) {
            return null;
        }
        return getHomeInfo(userHandle);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getNameForKey(int key) {
        if (key == 0) {
            return "Camera";
        }
        if (key != 1) {
            return null;
        }
        return "Home";
    }

    private int getSizeLimitForKey(int key) {
        if (key == 0) {
            return 83886080;
        }
        if (key != 1) {
            return 0;
        }
        return MAX_HOME_PIN_SIZE;
    }

    private void pinApp(int key, ApplicationInfo appInfo) {
        if (appInfo != null) {
            PinnedApp pinnedApp = new PinnedApp(appInfo);
            synchronized (this) {
                this.mPinnedApps.put(Integer.valueOf(key), pinnedApp);
            }
            int pinSizeLimit = getSizeLimitForKey(key);
            String apk = appInfo.sourceDir;
            PinnedFile pf = pinFile(apk, pinSizeLimit, true);
            if (pf == null) {
                Slog.e(TAG, "Failed to pin " + apk);
                return;
            }
            synchronized (this) {
                pinnedApp.mFiles.add(pf);
            }
            String arch = "arm";
            if (appInfo.primaryCpuAbi != null) {
                if (VMRuntime.is64BitAbi(appInfo.primaryCpuAbi)) {
                    arch = arch + "64";
                }
            } else if (VMRuntime.is64BitAbi(Build.SUPPORTED_ABIS[0])) {
                arch = arch + "64";
            }
            String baseCodePath = appInfo.getBaseCodePath();
            String[] files = null;
            try {
                if (!ENV_MAPLE) {
                    files = DexFile.getDexFileOutputPaths(baseCodePath, arch);
                } else if ((appInfo.hwFlags & DumpState.DUMP_SERVICE_PERMISSIONS) == 0) {
                    files = this.mInstaller.getDexFileOutputPaths(baseCodePath, arch, appInfo.uid);
                }
            } catch (FileNotFoundException e) {
            } catch (Installer.InstallerException ie) {
                Slog.w(TAG, "mInstaller.getDexFileOutputPaths InstallerException", ie);
            }
            if (files != null) {
                for (String file : files) {
                    PinnedFile pf2 = pinFile(file, pinSizeLimit, false);
                    if (pf2 != null) {
                        synchronized (this) {
                            pinnedApp.mFiles.add(pf2);
                        }
                    }
                }
            }
        }
    }

    private static PinnedFile pinFile(String fileToPin, int maxBytesToPin, boolean attemptPinIntrospection) {
        PinRangeSource pinRangeSource;
        ZipFile fileAsZip = null;
        InputStream pinRangeStream = null;
        if (attemptPinIntrospection) {
            try {
                fileAsZip = maybeOpenZip(fileToPin);
            } catch (Throwable th) {
                safeClose((Closeable) null);
                safeClose((Closeable) null);
                throw th;
            }
        }
        if (fileAsZip != null) {
            pinRangeStream = maybeOpenPinMetaInZip(fileAsZip, fileToPin);
        }
        Slog.d(TAG, "pinRangeStream: " + pinRangeStream);
        if (pinRangeStream != null) {
            pinRangeSource = new PinRangeSourceStream(pinRangeStream);
        } else {
            pinRangeSource = new PinRangeSourceStatic(0, Integer.MAX_VALUE);
        }
        PinnedFile pinFileRanges = pinFileRanges(fileToPin, maxBytesToPin, pinRangeSource);
        safeClose(pinRangeStream);
        safeClose(fileAsZip);
        return pinFileRanges;
    }

    private static ZipFile maybeOpenZip(String fileName) {
        try {
            return new ZipFile(fileName);
        } catch (IOException ex) {
            Slog.w(TAG, String.format("could not open \"%s\" as zip: pinning as blob", fileName), ex);
            return null;
        }
    }

    private static InputStream maybeOpenPinMetaInZip(ZipFile zipFile, String fileName) {
        ZipEntry pinMetaEntry = zipFile.getEntry(PIN_META_FILENAME);
        if (pinMetaEntry == null) {
            return null;
        }
        try {
            return zipFile.getInputStream(pinMetaEntry);
        } catch (IOException ex) {
            Slog.w(TAG, String.format("error reading pin metadata \"%s\": pinning as blob", fileName), ex);
            return null;
        }
    }

    /* access modifiers changed from: private */
    public static abstract class PinRangeSource {
        /* access modifiers changed from: package-private */
        public abstract boolean read(PinRange pinRange);

        private PinRangeSource() {
        }
    }

    /* access modifiers changed from: private */
    public static final class PinRangeSourceStatic extends PinRangeSource {
        private boolean mDone = false;
        private final int mPinLength;
        private final int mPinStart;

        PinRangeSourceStatic(int pinStart, int pinLength) {
            super();
            this.mPinStart = pinStart;
            this.mPinLength = pinLength;
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.PinnerService.PinRangeSource
        public boolean read(PinRange outPinRange) {
            outPinRange.start = this.mPinStart;
            outPinRange.length = this.mPinLength;
            boolean done = this.mDone;
            this.mDone = true;
            return !done;
        }
    }

    /* access modifiers changed from: private */
    public static final class PinRangeSourceStream extends PinRangeSource {
        private boolean mDone = false;
        private final DataInputStream mStream;

        PinRangeSourceStream(InputStream stream) {
            super();
            this.mStream = new DataInputStream(stream);
        }

        /* access modifiers changed from: package-private */
        @Override // com.android.server.PinnerService.PinRangeSource
        public boolean read(PinRange outPinRange) {
            if (!this.mDone) {
                try {
                    outPinRange.start = this.mStream.readInt();
                    outPinRange.length = this.mStream.readInt();
                } catch (IOException e) {
                    this.mDone = true;
                }
            }
            return !this.mDone;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:73:0x014d  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x0160  */
    private static PinnedFile pinFileRanges(String fileToPin, int maxBytesToPin, PinRangeSource pinRangeSource) {
        FileDescriptor fd;
        long address;
        int mapSize;
        ErrnoException ex;
        ErrnoException ex2;
        int maxBytesToPin2;
        int bytesPinned;
        FileDescriptor fd2 = new FileDescriptor();
        long address2 = -1;
        int mapSize2 = 0;
        try {
            fd = Os.open(fileToPin, OsConstants.O_RDONLY | OsConstants.O_CLOEXEC | OsConstants.O_NOFOLLOW, 0);
            try {
                mapSize = (int) Math.min(Os.fstat(fd).st_size, 2147483647L);
                try {
                    address = Os.mmap(0, (long) mapSize, OsConstants.PROT_READ, OsConstants.MAP_SHARED, fd, 0);
                } catch (ErrnoException e) {
                    ex2 = e;
                    mapSize2 = mapSize;
                    fd2 = fd;
                    try {
                        Slog.e(TAG, "Could not pin file " + fileToPin, ex2);
                        safeClose(fd2);
                        if (address2 >= 0) {
                        }
                        return null;
                    } catch (Throwable th) {
                        ex = th;
                        fd = fd2;
                        address = address2;
                        mapSize = mapSize2;
                        safeClose(fd);
                        if (address >= 0) {
                            safeMunmap(address, (long) mapSize);
                        }
                        throw ex;
                    }
                } catch (Throwable th2) {
                    ex = th2;
                    address = -1;
                    safeClose(fd);
                    if (address >= 0) {
                    }
                    throw ex;
                }
            } catch (ErrnoException e2) {
                ex2 = e2;
                fd2 = fd;
                Slog.e(TAG, "Could not pin file " + fileToPin, ex2);
                safeClose(fd2);
                if (address2 >= 0) {
                }
                return null;
            } catch (Throwable th3) {
                ex = th3;
                address = -1;
                mapSize = 0;
                safeClose(fd);
                if (address >= 0) {
                }
                throw ex;
            }
            try {
                PinRange pinRange = new PinRange();
                if (maxBytesToPin % PAGE_SIZE != 0) {
                    try {
                        bytesPinned = 0;
                        maxBytesToPin2 = maxBytesToPin - (maxBytesToPin % PAGE_SIZE);
                    } catch (ErrnoException e3) {
                        ex2 = e3;
                        mapSize2 = mapSize;
                        address2 = address;
                        fd2 = fd;
                        Slog.e(TAG, "Could not pin file " + fileToPin, ex2);
                        safeClose(fd2);
                        if (address2 >= 0) {
                        }
                        return null;
                    } catch (Throwable th4) {
                        ex = th4;
                        safeClose(fd);
                        if (address >= 0) {
                        }
                        throw ex;
                    }
                } else {
                    maxBytesToPin2 = maxBytesToPin;
                    bytesPinned = 0;
                }
                while (true) {
                    if (bytesPinned >= maxBytesToPin2) {
                        break;
                    }
                    try {
                        if (pinRangeSource.read(pinRange)) {
                            int pinStart = pinRange.start;
                            int pinLength = pinRange.length;
                            int pinStart2 = clamp(0, pinStart, mapSize);
                            int pinLength2 = Math.min(maxBytesToPin2 - bytesPinned, clamp(0, pinLength, mapSize - pinStart2)) + (pinStart2 % PAGE_SIZE);
                            int pinStart3 = pinStart2 - (pinStart2 % PAGE_SIZE);
                            if (pinLength2 % PAGE_SIZE != 0) {
                                pinLength2 += PAGE_SIZE - (pinLength2 % PAGE_SIZE);
                            }
                            int pinLength3 = clamp(0, pinLength2, maxBytesToPin2 - bytesPinned);
                            if (pinLength3 > 0) {
                                Os.mlock(((long) pinStart3) + address, (long) pinLength3);
                            }
                            bytesPinned += pinLength3;
                        }
                    } catch (ErrnoException e4) {
                        ex2 = e4;
                        mapSize2 = mapSize;
                        address2 = address;
                        fd2 = fd;
                        Slog.e(TAG, "Could not pin file " + fileToPin, ex2);
                        safeClose(fd2);
                        if (address2 >= 0) {
                        }
                        return null;
                    } catch (Throwable th5) {
                        ex = th5;
                        safeClose(fd);
                        if (address >= 0) {
                        }
                        throw ex;
                    }
                }
                try {
                    try {
                        PinnedFile pinnedFile = new PinnedFile(address, mapSize, fileToPin, bytesPinned);
                        safeClose(fd);
                        if (-1 >= 0) {
                            safeMunmap(-1, (long) mapSize);
                        }
                        return pinnedFile;
                    } catch (ErrnoException e5) {
                        ex2 = e5;
                        mapSize2 = mapSize;
                        address2 = address;
                        fd2 = fd;
                        Slog.e(TAG, "Could not pin file " + fileToPin, ex2);
                        safeClose(fd2);
                        if (address2 >= 0) {
                        }
                        return null;
                    } catch (Throwable th6) {
                        ex = th6;
                        mapSize = mapSize;
                        safeClose(fd);
                        if (address >= 0) {
                        }
                        throw ex;
                    }
                } catch (ErrnoException e6) {
                    ex2 = e6;
                    mapSize2 = mapSize;
                    address2 = address;
                    fd2 = fd;
                    Slog.e(TAG, "Could not pin file " + fileToPin, ex2);
                    safeClose(fd2);
                    if (address2 >= 0) {
                    }
                    return null;
                } catch (Throwable th7) {
                    ex = th7;
                    safeClose(fd);
                    if (address >= 0) {
                    }
                    throw ex;
                }
            } catch (ErrnoException e7) {
                ex2 = e7;
                mapSize2 = mapSize;
                address2 = address;
                fd2 = fd;
                Slog.e(TAG, "Could not pin file " + fileToPin, ex2);
                safeClose(fd2);
                if (address2 >= 0) {
                }
                return null;
            } catch (Throwable th8) {
                ex = th8;
                safeClose(fd);
                if (address >= 0) {
                }
                throw ex;
            }
        } catch (ErrnoException e8) {
            ex2 = e8;
            Slog.e(TAG, "Could not pin file " + fileToPin, ex2);
            safeClose(fd2);
            if (address2 >= 0) {
                safeMunmap(address2, (long) mapSize2);
            }
            return null;
        } catch (Throwable th9) {
            ex = th9;
            fd = fd2;
            address = -1;
            mapSize = 0;
            safeClose(fd);
            if (address >= 0) {
            }
            throw ex;
        }
    }

    private static int clamp(int min, int value, int max) {
        return Math.max(min, Math.min(value, max));
    }

    /* access modifiers changed from: private */
    public static void safeMunmap(long address, long mapSize) {
        try {
            Os.munmap(address, mapSize);
        } catch (ErrnoException ex) {
            Slog.w(TAG, "ignoring error in unmap", ex);
        }
    }

    private static void safeClose(FileDescriptor fd) {
        if (fd != null && fd.valid()) {
            try {
                Os.close(fd);
            } catch (ErrnoException ex) {
                if (ex.errno == OsConstants.EBADF) {
                    throw new AssertionError(ex);
                }
            }
        }
    }

    private static void safeClose(Closeable thing) {
        if (thing != null) {
            try {
                thing.close();
            } catch (IOException ex) {
                Slog.w(TAG, "ignoring error closing resource: " + thing, ex);
            }
        }
    }

    private final class BinderService extends Binder {
        private BinderService() {
        }

        /* access modifiers changed from: protected */
        @Override // android.os.Binder
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(PinnerService.this.mContext, PinnerService.TAG, pw)) {
                synchronized (PinnerService.this) {
                    long totalSize = 0;
                    Iterator it = PinnerService.this.mPinnedFiles.iterator();
                    while (it.hasNext()) {
                        PinnedFile pinnedFile = (PinnedFile) it.next();
                        pw.format("%s %s\n", pinnedFile.fileName, Integer.valueOf(pinnedFile.bytesPinned));
                        totalSize += (long) pinnedFile.bytesPinned;
                    }
                    pw.println();
                    for (Integer num : PinnerService.this.mPinnedApps.keySet()) {
                        int key = num.intValue();
                        PinnedApp app = (PinnedApp) PinnerService.this.mPinnedApps.get(Integer.valueOf(key));
                        pw.print(PinnerService.this.getNameForKey(key));
                        pw.print(" uid=");
                        pw.print(app.uid);
                        pw.print(" active=");
                        pw.print(app.active);
                        pw.println();
                        Iterator<PinnedFile> it2 = ((PinnedApp) PinnerService.this.mPinnedApps.get(Integer.valueOf(key))).mFiles.iterator();
                        while (it2.hasNext()) {
                            PinnedFile pf = it2.next();
                            pw.print("  ");
                            pw.format("%s %s\n", pf.fileName, Integer.valueOf(pf.bytesPinned));
                            totalSize += (long) pf.bytesPinned;
                        }
                    }
                    pw.format("Total size: %s\n", Long.valueOf(totalSize));
                    pw.println();
                    if (!PinnerService.this.mPendingRepin.isEmpty()) {
                        pw.print("Pending repin: ");
                        for (Integer num2 : PinnerService.this.mPendingRepin.values()) {
                            pw.print(PinnerService.this.getNameForKey(num2.intValue()));
                            pw.print(' ');
                        }
                        pw.println();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class PinnedFile implements AutoCloseable {
        final int bytesPinned;
        final String fileName;
        private long mAddress;
        final int mapSize;

        PinnedFile(long address, int mapSize2, String fileName2, int bytesPinned2) {
            this.mAddress = address;
            this.mapSize = mapSize2;
            this.fileName = fileName2;
            this.bytesPinned = bytesPinned2;
        }

        @Override // java.lang.AutoCloseable
        public void close() {
            long j = this.mAddress;
            if (j >= 0) {
                PinnerService.safeMunmap(j, (long) this.mapSize);
                this.mAddress = -1;
            }
        }

        @Override // java.lang.Object
        public void finalize() {
            close();
        }
    }

    /* access modifiers changed from: package-private */
    public static final class PinRange {
        int length;
        int start;

        PinRange() {
        }
    }

    /* access modifiers changed from: private */
    public final class PinnedApp {
        boolean active;
        final ArrayList<PinnedFile> mFiles;
        final int uid;

        private PinnedApp(ApplicationInfo appInfo) {
            this.mFiles = new ArrayList<>();
            this.uid = appInfo.uid;
            this.active = PinnerService.this.mAmInternal.isUidActive(this.uid);
        }
    }

    /* access modifiers changed from: package-private */
    public final class PinnerHandler extends Handler {
        static final int PIN_ONSTART_MSG = 4001;

        public PinnerHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what != PIN_ONSTART_MSG) {
                super.handleMessage(msg);
            } else {
                PinnerService.this.handlePinOnStart();
            }
        }
    }

    private boolean checkShouldPinSkip(String ssClassPath, String fileToPin) {
        if (ssClassPath == null || fileToPin == null || !fileToPin.startsWith("/system/framework/oat/")) {
            return false;
        }
        String fileName = fileToPin.substring(fileToPin.lastIndexOf(47) + 1);
        if (!fileName.endsWith(".odex") && !fileName.endsWith(".vdex")) {
            return false;
        }
        String nameString = fileName.substring(0, fileName.length() - 5);
        return ssClassPath.contains(nameString + ".jar");
    }
}
