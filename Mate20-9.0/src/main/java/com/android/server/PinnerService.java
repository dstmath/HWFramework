package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.app.ResolverActivity;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.server.os.HwBootFail;
import com.android.server.pm.DumpState;
import com.android.server.pm.Installer;
import dalvik.system.DexFile;
import dalvik.system.VMRuntime;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class PinnerService extends SystemService {
    private static final int ARY_SIZ = 2;
    private static final long B_TO_G = 1073741824;
    private static final boolean DEBUG = false;
    private static final boolean ENV_MAPLE = (Os.getenv("MAPLE_RUNTIME") != null);
    private static final int IDX_FST = 0;
    private static final int IDX_SEC = 1;
    private static final int MAX_CAMERA_PIN_SIZE = 83886080;
    private static final int PAGE_SIZE = ((int) Os.sysconf(OsConstants._SC_PAGESIZE));
    private static final String PIN_META_FILENAME = "pinlist.meta";
    private static final String TAG = "PinnerService";
    private BinderService mBinderService;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == "android.intent.action.PACKAGE_REPLACED") {
                String packageName = intent.getData().getSchemeSpecificPart();
                ArraySet<String> updatedPackages = new ArraySet<>();
                updatedPackages.add(packageName);
                PinnerService.this.update(updatedPackages);
            }
        }
    };
    /* access modifiers changed from: private */
    public final Context mContext;
    private Installer mInstaller;
    private final ArrayList<PinnedFile> mPinnedCameraFiles = new ArrayList<>();
    private final ArrayList<PinnedFile> mPinnedFiles = new ArrayList<>();
    private PinnerHandler mPinnerHandler = null;
    private final boolean mShouldPinCamera;

    private final class BinderService extends Binder {
        private BinderService() {
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(PinnerService.this.mContext, PinnerService.TAG, pw)) {
                long totalSize = 0;
                Iterator it = PinnerService.this.snapshotPinnedFiles().iterator();
                while (it.hasNext()) {
                    PinnedFile pinnedFile = (PinnedFile) it.next();
                    pw.format("%s %s\n", new Object[]{pinnedFile.fileName, Integer.valueOf(pinnedFile.bytesPinned)});
                    totalSize += (long) pinnedFile.bytesPinned;
                }
                pw.format("Total size: %s\n", new Object[]{Long.valueOf(totalSize)});
            }
        }
    }

    static final class PinRange {
        int length;
        int start;

        PinRange() {
        }
    }

    private static abstract class PinRangeSource {
        /* access modifiers changed from: package-private */
        public abstract boolean read(PinRange pinRange);

        private PinRangeSource() {
        }
    }

    private static final class PinRangeSourceStatic extends PinRangeSource {
        private boolean mDone = false;
        private final int mPinLength;
        private final int mPinStart;

        PinRangeSourceStatic(int pinStart, int pinLength) {
            super();
            this.mPinStart = pinStart;
            this.mPinLength = pinLength;
        }

        /* access modifiers changed from: package-private */
        public boolean read(PinRange outPinRange) {
            outPinRange.start = this.mPinStart;
            outPinRange.length = this.mPinLength;
            boolean done = this.mDone;
            this.mDone = true;
            return !done;
        }
    }

    private static final class PinRangeSourceStream extends PinRangeSource {
        private boolean mDone = false;
        private final DataInputStream mStream;

        PinRangeSourceStream(InputStream stream) {
            super();
            this.mStream = new DataInputStream(stream);
        }

        /* access modifiers changed from: package-private */
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

    private static final class PinnedFile implements AutoCloseable {
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

        public void close() {
            if (this.mAddress >= 0) {
                PinnerService.safeMunmap(this.mAddress, (long) this.mapSize);
                this.mAddress = -1;
            }
        }

        public void finalize() {
            close();
        }
    }

    final class PinnerHandler extends Handler {
        static final int PIN_CAMERA_MSG = 4000;
        static final int PIN_ONSTART_MSG = 4001;

        public PinnerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PIN_CAMERA_MSG /*4000*/:
                    PinnerService.this.handlePinCamera(msg.arg1);
                    return;
                case PIN_ONSTART_MSG /*4001*/:
                    PinnerService.this.handlePinOnStart();
                    return;
                default:
                    super.handleMessage(msg);
                    return;
            }
        }
    }

    public void setInstaller(Installer installer) {
        this.mInstaller = installer;
    }

    public PinnerService(Context context) {
        super(context);
        this.mContext = context;
        this.mShouldPinCamera = context.getResources().getBoolean(17957001);
        this.mPinnerHandler = new PinnerHandler(BackgroundThread.get().getLooper());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addDataScheme("package");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    public void onStart() {
        this.mBinderService = new BinderService();
        publishBinderService("pinner", this.mBinderService);
        publishLocalService(PinnerService.class, this);
        this.mPinnerHandler.obtainMessage(4001).sendToTarget();
        this.mPinnerHandler.obtainMessage(4000, 0, 0).sendToTarget();
    }

    public void onSwitchUser(int userHandle) {
        this.mPinnerHandler.obtainMessage(4000, userHandle, 0).sendToTarget();
    }

    public void update(ArraySet<String> updatedPackages) {
        ApplicationInfo cameraInfo = getCameraInfo(0);
        if (cameraInfo != null && updatedPackages.contains(cameraInfo.packageName)) {
            Slog.i(TAG, "Updating pinned files.");
            this.mPinnerHandler.obtainMessage(4000, 0, 0).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public void handlePinOnStart() {
        String[] filesToPin = this.mContext.getResources().getStringArray(17236000);
        String ssClassPath = System.getenv("SYSTEMSERVERCLASSPATH");
        int deviceMemory = Integer.MAX_VALUE;
        for (String fileToPin : filesToPin) {
            if (fileToPin != null && fileToPin.contains(":")) {
                try {
                    String[] tmpArray = fileToPin.split(":");
                    if (tmpArray.length != 2) {
                        Slog.w(TAG, fileToPin + " is not valid");
                    } else {
                        if (deviceMemory == Integer.MAX_VALUE) {
                            deviceMemory = (int) Math.ceil(((double) Process.getTotalMemory()) / 1.073741824E9d);
                        }
                        if (deviceMemory >= Integer.parseInt(tmpArray[0])) {
                            fileToPin = tmpArray[1];
                        } else {
                            continue;
                        }
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "Errors happened when pin file " + fileToPin, e);
                }
            }
            int deviceMemory2 = deviceMemory;
            if (ENV_MAPLE && checkShouldPinSkip(ssClassPath, fileToPin)) {
                Slog.i(TAG, "handlePinOnStart for maple skip to pin: " + fileToPin);
            } else if (ENV_MAPLE || fileToPin == null || !fileToPin.startsWith("/system/lib64/libmaple")) {
                PinnedFile pf = pinFile(fileToPin, HwBootFail.STAGE_BOOT_SUCCESS, false);
                if (pf == null) {
                    Slog.e(TAG, "Failed to pin file = " + fileToPin);
                } else {
                    synchronized (this) {
                        this.mPinnedFiles.add(pf);
                    }
                }
            }
            deviceMemory = deviceMemory2;
        }
    }

    /* access modifiers changed from: private */
    public void handlePinCamera(int userHandle) {
        if (this.mShouldPinCamera) {
            pinCamera(userHandle);
        }
    }

    private void unpinCameraApp() {
        ArrayList<PinnedFile> pinnedCameraFiles;
        synchronized (this) {
            pinnedCameraFiles = new ArrayList<>(this.mPinnedCameraFiles);
            this.mPinnedCameraFiles.clear();
        }
        Iterator<PinnedFile> it = pinnedCameraFiles.iterator();
        while (it.hasNext()) {
            it.next().close();
        }
    }

    private boolean isResolverActivity(ActivityInfo info) {
        return ResolverActivity.class.getName().equals(info.name);
    }

    private ApplicationInfo getCameraInfo(int userHandle) {
        ResolveInfo cameraResolveInfo = this.mContext.getPackageManager().resolveActivityAsUser(new Intent("android.media.action.STILL_IMAGE_CAMERA"), 851968, userHandle);
        if (cameraResolveInfo != null && !isResolverActivity(cameraResolveInfo.activityInfo)) {
            return cameraResolveInfo.activityInfo.applicationInfo;
        }
        return null;
    }

    private boolean pinCamera(int userHandle) {
        ApplicationInfo cameraInfo = getCameraInfo(userHandle);
        if (cameraInfo == null) {
            return false;
        }
        unpinCameraApp();
        PinnedFile pf = pinFile(cameraInfo.sourceDir, 83886080, true);
        if (pf == null) {
            Slog.e(TAG, "Failed to pin " + camAPK);
            return false;
        }
        synchronized (this) {
            this.mPinnedCameraFiles.add(pf);
        }
        String arch = "arm";
        if (cameraInfo.primaryCpuAbi != null) {
            if (VMRuntime.is64BitAbi(cameraInfo.primaryCpuAbi)) {
                arch = arch + "64";
            }
        } else if (VMRuntime.is64BitAbi(Build.SUPPORTED_ABIS[0])) {
            arch = arch + "64";
        }
        String baseCodePath = cameraInfo.getBaseCodePath();
        String[] files = null;
        try {
            if (!ENV_MAPLE) {
                files = DexFile.getDexFileOutputPaths(baseCodePath, arch);
            } else if ((cameraInfo.hwFlags & DumpState.DUMP_SERVICE_PERMISSIONS) == 0) {
                files = this.mInstaller.getDexFileOutputPaths(baseCodePath, arch, cameraInfo.uid);
            }
        } catch (FileNotFoundException e) {
        } catch (Installer.InstallerException ie) {
            Slog.w(TAG, "mInstaller.getDexFileOutputPaths InstallerException", ie);
        }
        if (files == null) {
            return true;
        }
        PinnedFile pinnedFile = pf;
        for (String file : files) {
            PinnedFile pf2 = pinFile(file, 83886080, false);
            if (pf2 != null) {
                synchronized (this) {
                    this.mPinnedCameraFiles.add(pf2);
                }
            }
        }
        return true;
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
            pinRangeSource = new PinRangeSourceStatic(0, HwBootFail.STAGE_BOOT_SUCCESS);
        }
        PinnedFile pinFileRanges = pinFileRanges(fileToPin, maxBytesToPin, pinRangeSource);
        safeClose((Closeable) pinRangeStream);
        safeClose((Closeable) fileAsZip);
        return pinFileRanges;
    }

    private static ZipFile maybeOpenZip(String fileName) {
        try {
            return new ZipFile(fileName);
        } catch (IOException ex) {
            Slog.w(TAG, String.format("could not open \"%s\" as zip: pinning as blob", new Object[]{fileName}), ex);
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
            Slog.w(TAG, String.format("error reading pin metadata \"%s\": pinning as blob", new Object[]{fileName}), ex);
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:78:0x0132  */
    /* JADX WARNING: Removed duplicated region for block: B:83:0x0142  */
    private static PinnedFile pinFileRanges(String fileToPin, int maxBytesToPin, PinRangeSource pinRangeSource) {
        long address;
        FileDescriptor fd;
        FileDescriptor fileDescriptor;
        int maxBytesToPin2;
        int bytesPinned;
        String str = fileToPin;
        FileDescriptor fd2 = new FileDescriptor();
        long address2 = -1;
        int i = 0;
        int mapSize = 0;
        try {
            fd = Os.open(str, OsConstants.O_NOFOLLOW | OsConstants.O_RDONLY | OsConstants.O_CLOEXEC, 0);
            try {
                mapSize = (int) Math.min(Os.fstat(fd).st_size, 2147483647L);
                try {
                    address = Os.mmap(0, (long) mapSize, OsConstants.PROT_READ, OsConstants.MAP_SHARED, fd, 0);
                } catch (ErrnoException e) {
                    ex = e;
                    int i2 = mapSize;
                    int i3 = maxBytesToPin;
                    fd2 = fd;
                    try {
                        Slog.e(TAG, "Could not pin file " + str, ex);
                        safeClose(fd2);
                        if (address2 >= 0) {
                            safeMunmap(address2, (long) mapSize);
                        }
                        return null;
                    } catch (Throwable th) {
                        ex = th;
                        fd = fd2;
                        address = address2;
                        safeClose(fd);
                        if (address >= 0) {
                            safeMunmap(address, (long) mapSize);
                        }
                        throw ex;
                    }
                } catch (Throwable th2) {
                    ex = th2;
                    int i4 = mapSize;
                    FileDescriptor fileDescriptor2 = fd;
                    int i5 = maxBytesToPin;
                    address = -1;
                    safeClose(fd);
                    if (address >= 0) {
                    }
                    throw ex;
                }
            } catch (ErrnoException e2) {
                ex = e2;
                int i6 = maxBytesToPin;
                fd2 = fd;
                Slog.e(TAG, "Could not pin file " + str, ex);
                safeClose(fd2);
                if (address2 >= 0) {
                }
                return null;
            } catch (Throwable th3) {
                ex = th3;
                FileDescriptor fileDescriptor3 = fd;
                int i7 = maxBytesToPin;
                address = -1;
                safeClose(fd);
                if (address >= 0) {
                }
                throw ex;
            }
            try {
                PinRange pinRange = new PinRange();
                if (maxBytesToPin % PAGE_SIZE != 0) {
                    try {
                        maxBytesToPin2 = maxBytesToPin - (maxBytesToPin % PAGE_SIZE);
                    } catch (ErrnoException e3) {
                        ex = e3;
                        int i8 = maxBytesToPin;
                        fd2 = fd;
                        address2 = address;
                        Slog.e(TAG, "Could not pin file " + str, ex);
                        safeClose(fd2);
                        if (address2 >= 0) {
                        }
                        return null;
                    } catch (Throwable th4) {
                        ex = th4;
                        int i9 = maxBytesToPin;
                        safeClose(fd);
                        if (address >= 0) {
                        }
                        throw ex;
                    }
                } else {
                    maxBytesToPin2 = maxBytesToPin;
                }
                bytesPinned = 0;
                while (true) {
                    if (bytesPinned >= maxBytesToPin2) {
                        PinRangeSource pinRangeSource2 = pinRangeSource;
                        break;
                    }
                    try {
                        if (pinRangeSource.read(pinRange)) {
                            int pinStart = pinRange.start;
                            int pinLength = pinRange.length;
                            int pinStart2 = clamp(i, pinStart, mapSize);
                            int pinLength2 = Math.min(maxBytesToPin2 - bytesPinned, clamp(i, pinLength, mapSize - pinStart2)) + (pinStart2 % PAGE_SIZE);
                            int pinStart3 = pinStart2 - (pinStart2 % PAGE_SIZE);
                            if (pinLength2 % PAGE_SIZE != 0) {
                                pinLength2 += PAGE_SIZE - (pinLength2 % PAGE_SIZE);
                            }
                            int pinLength3 = clamp(i, pinLength2, maxBytesToPin2 - bytesPinned);
                            if (pinLength3 > 0) {
                                int i10 = pinStart3;
                                Os.mlock(((long) pinStart3) + address, (long) pinLength3);
                            }
                            bytesPinned += pinLength3;
                            i = 0;
                        }
                    } catch (ErrnoException e4) {
                        ex = e4;
                        fd2 = fd;
                        address2 = address;
                        Slog.e(TAG, "Could not pin file " + str, ex);
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
            } catch (ErrnoException e5) {
                ex = e5;
                int i11 = mapSize;
                fileDescriptor = fd;
                int i12 = maxBytesToPin;
                fd2 = fileDescriptor;
                address2 = address;
                Slog.e(TAG, "Could not pin file " + str, ex);
                safeClose(fd2);
                if (address2 >= 0) {
                }
                return null;
            } catch (Throwable th6) {
                ex = th6;
                int i13 = mapSize;
                FileDescriptor fileDescriptor4 = fd;
                int i14 = maxBytesToPin;
                safeClose(fd);
                if (address >= 0) {
                }
                throw ex;
            }
            try {
                r1 = r1;
                int mapSize2 = mapSize;
                FileDescriptor fd3 = fd;
                try {
                    PinnedFile pinnedFile = new PinnedFile(address, mapSize, str, bytesPinned);
                    safeClose(fd3);
                    if (-1 >= 0) {
                        safeMunmap(-1, (long) mapSize2);
                    }
                    return pinnedFile;
                } catch (ErrnoException e6) {
                    ex = e6;
                    mapSize = mapSize2;
                    fd2 = fd3;
                    address2 = address;
                    Slog.e(TAG, "Could not pin file " + str, ex);
                    safeClose(fd2);
                    if (address2 >= 0) {
                    }
                    return null;
                } catch (Throwable th7) {
                    ex = th7;
                    mapSize = mapSize2;
                    fd = fd3;
                    safeClose(fd);
                    if (address >= 0) {
                    }
                    throw ex;
                }
            } catch (ErrnoException e7) {
                ex = e7;
                int i15 = mapSize;
                fileDescriptor = fd;
                fd2 = fileDescriptor;
                address2 = address;
                Slog.e(TAG, "Could not pin file " + str, ex);
                safeClose(fd2);
                if (address2 >= 0) {
                }
                return null;
            } catch (Throwable th8) {
                ex = th8;
                int i16 = mapSize;
                FileDescriptor fileDescriptor5 = fd;
                safeClose(fd);
                if (address >= 0) {
                }
                throw ex;
            }
        } catch (ErrnoException e8) {
            ex = e8;
            int i17 = maxBytesToPin;
            Slog.e(TAG, "Could not pin file " + str, ex);
            safeClose(fd2);
            if (address2 >= 0) {
            }
            return null;
        } catch (Throwable th9) {
            ex = th9;
            int i18 = maxBytesToPin;
            fd = fd2;
            address = address2;
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

    /* access modifiers changed from: private */
    public synchronized ArrayList<PinnedFile> snapshotPinnedFiles() {
        ArrayList<PinnedFile> pinnedFiles;
        pinnedFiles = new ArrayList<>(this.mPinnedFiles.size() + this.mPinnedCameraFiles.size());
        pinnedFiles.addAll(this.mPinnedFiles);
        pinnedFiles.addAll(this.mPinnedCameraFiles);
        return pinnedFiles;
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
