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
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructStat;
import android.util.ArraySet;
import android.util.Slog;
import com.android.internal.app.ResolverActivity;
import com.android.internal.os.BackgroundThread;
import com.android.internal.util.DumpUtils;
import com.android.server.am.HwBroadcastRadarUtil;
import dalvik.system.DexFile;
import dalvik.system.VMRuntime;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public final class PinnerService extends SystemService {
    private static final boolean DEBUG = false;
    private static final String TAG = "PinnerService";
    private final long MAX_CAMERA_PIN_SIZE = 83886080;
    private BinderService mBinderService;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == "android.intent.action.PACKAGE_REPLACED") {
                String packageName = intent.getData().getSchemeSpecificPart();
                ArraySet<String> updatedPackages = new ArraySet();
                updatedPackages.add(packageName);
                PinnerService.this.update(updatedPackages);
            }
        }
    };
    private final Context mContext;
    private final ArrayList<PinnedFile> mPinnedCameraFiles = new ArrayList();
    private final ArrayList<PinnedFile> mPinnedFiles = new ArrayList();
    private PinnerHandler mPinnerHandler = null;
    private final boolean mShouldPinCamera;

    private final class BinderService extends Binder {
        /* synthetic */ BinderService(PinnerService this$0, BinderService -this1) {
            this();
        }

        private BinderService() {
        }

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(PinnerService.this.mContext, PinnerService.TAG, pw)) {
                pw.println("Pinned Files:");
                synchronized (this) {
                    int i;
                    for (i = 0; i < PinnerService.this.mPinnedFiles.size(); i++) {
                        pw.println(((PinnedFile) PinnerService.this.mPinnedFiles.get(i)).mFilename);
                    }
                    for (i = 0; i < PinnerService.this.mPinnedCameraFiles.size(); i++) {
                        pw.println(((PinnedFile) PinnerService.this.mPinnedCameraFiles.get(i)).mFilename);
                    }
                }
            }
        }
    }

    private static class PinnedFile {
        long mAddress;
        String mFilename;
        long mLength;

        PinnedFile(long address, long length, String filename) {
            this.mAddress = address;
            this.mLength = length;
            this.mFilename = filename;
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

    public PinnerService(Context context) {
        super(context);
        this.mContext = context;
        this.mShouldPinCamera = context.getResources().getBoolean(17956988);
        this.mPinnerHandler = new PinnerHandler(BackgroundThread.get().getLooper());
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
    }

    public void onStart() {
        this.mBinderService = new BinderService(this, null);
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

    private void handlePinOnStart() {
        String[] filesToPin = this.mContext.getResources().getStringArray(17236001);
        synchronized (this) {
            for (int i = 0; i < filesToPin.length; i++) {
                PinnedFile pf = pinFile(filesToPin[i], 0, 0, 0);
                if (pf != null) {
                    this.mPinnedFiles.add(pf);
                } else {
                    Slog.e(TAG, "Failed to pin file = " + filesToPin[i]);
                }
            }
        }
    }

    private void handlePinCamera(int userHandle) {
        if (this.mShouldPinCamera) {
            synchronized (this) {
                boolean success = pinCamera(userHandle);
            }
        }
    }

    private boolean alreadyPinned(int userHandle) {
        ApplicationInfo cameraInfo = getCameraInfo(userHandle);
        if (cameraInfo == null) {
            return false;
        }
        for (int i = 0; i < this.mPinnedCameraFiles.size(); i++) {
            if (((PinnedFile) this.mPinnedCameraFiles.get(i)).mFilename.equals(cameraInfo.sourceDir)) {
                return true;
            }
        }
        return false;
    }

    private void unpinCameraApp() {
        for (int i = 0; i < this.mPinnedCameraFiles.size(); i++) {
            unpinFile((PinnedFile) this.mPinnedCameraFiles.get(i));
        }
        this.mPinnedCameraFiles.clear();
    }

    private boolean isResolverActivity(ActivityInfo info) {
        return ResolverActivity.class.getName().equals(info.name);
    }

    private ApplicationInfo getCameraInfo(int userHandle) {
        ResolveInfo cameraResolveInfo = this.mContext.getPackageManager().resolveActivityAsUser(new Intent("android.media.action.STILL_IMAGE_CAMERA"), 851968, userHandle);
        if (cameraResolveInfo == null || isResolverActivity(cameraResolveInfo.activityInfo)) {
            return null;
        }
        return cameraResolveInfo.activityInfo.applicationInfo;
    }

    private boolean pinCamera(int userHandle) {
        ApplicationInfo cameraInfo = getCameraInfo(userHandle);
        if (cameraInfo == null) {
            return false;
        }
        unpinCameraApp();
        String camAPK = cameraInfo.sourceDir;
        PinnedFile pf = pinFile(camAPK, 0, 0, 83886080);
        if (pf == null) {
            Slog.e(TAG, "Failed to pin " + camAPK);
            return false;
        }
        this.mPinnedCameraFiles.add(pf);
        String arch = "arm";
        if (cameraInfo.primaryCpuAbi != null && VMRuntime.is64BitAbi(cameraInfo.primaryCpuAbi)) {
            arch = arch + "64";
        } else if (VMRuntime.is64BitAbi(Build.SUPPORTED_ABIS[0])) {
            arch = arch + "64";
        }
        String[] files = null;
        try {
            files = DexFile.getDexFileOutputPaths(cameraInfo.getBaseCodePath(), arch);
        } catch (IOException e) {
        }
        if (files == null) {
            return true;
        }
        for (String file : files) {
            pf = pinFile(file, 0, 0, 83886080);
            if (pf != null) {
                this.mPinnedCameraFiles.add(pf);
            }
        }
        return true;
    }

    private static PinnedFile pinFile(String fileToPin, long offset, long length, long maxSize) {
        FileDescriptor fd = new FileDescriptor();
        try {
            fd = Os.open(fileToPin, (OsConstants.O_RDONLY | OsConstants.O_CLOEXEC) | OsConstants.O_NOFOLLOW, OsConstants.O_RDONLY);
            StructStat sb = Os.fstat(fd);
            if (offset + length > sb.st_size) {
                Os.close(fd);
                Slog.e(TAG, "Failed to pin file " + fileToPin + ", request extends beyond end of file.  offset + length =  " + (offset + length) + ", file length = " + sb.st_size);
                return null;
            }
            if (length == 0) {
                length = sb.st_size - offset;
            }
            if (maxSize <= 0 || length <= maxSize) {
                long address = Os.mmap(0, length, OsConstants.PROT_READ, OsConstants.MAP_PRIVATE, fd, offset);
                Os.close(fd);
                Os.mlock(address, length);
                return new PinnedFile(address, length, fileToPin);
            }
            Slog.e(TAG, "Could not pin file " + fileToPin + ", size = " + length + ", maxSize = " + maxSize);
            Os.close(fd);
            return null;
        } catch (ErrnoException e) {
            Slog.e(TAG, "Could not pin file " + fileToPin + " with error " + e.getMessage());
            if (fd.valid()) {
                try {
                    Os.close(fd);
                } catch (ErrnoException eClose) {
                    Slog.e(TAG, "Failed to close fd, error = " + eClose.getMessage());
                }
            }
            return null;
        }
    }

    private static boolean unpinFile(PinnedFile pf) {
        try {
            Os.munlock(pf.mAddress, pf.mLength);
            return true;
        } catch (ErrnoException e) {
            Slog.e(TAG, "Failed to unpin file " + pf.mFilename + " with error " + e.getMessage());
            return false;
        }
    }
}
