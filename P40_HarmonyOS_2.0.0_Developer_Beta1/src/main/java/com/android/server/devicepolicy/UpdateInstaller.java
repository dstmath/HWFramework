package com.android.server.devicepolicy;

import android.app.admin.DevicePolicyEventLogger;
import android.app.admin.StartInstallingUpdateCallback;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.FileUtils;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;
import com.android.server.devicepolicy.DevicePolicyManagerService;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/* access modifiers changed from: package-private */
public abstract class UpdateInstaller {
    static final String TAG = "UpdateInstaller";
    private StartInstallingUpdateCallback mCallback;
    private DevicePolicyConstants mConstants;
    protected Context mContext;
    protected File mCopiedUpdateFile;
    private DevicePolicyManagerService.Injector mInjector;
    private ParcelFileDescriptor mUpdateFileDescriptor;

    public abstract void installUpdateInThread();

    protected UpdateInstaller(Context context, ParcelFileDescriptor updateFileDescriptor, StartInstallingUpdateCallback callback, DevicePolicyManagerService.Injector injector, DevicePolicyConstants constants) {
        this.mContext = context;
        this.mCallback = callback;
        this.mUpdateFileDescriptor = updateFileDescriptor;
        this.mInjector = injector;
        this.mConstants = constants;
    }

    public void startInstallUpdate() {
        this.mCopiedUpdateFile = null;
        if (!isBatteryLevelSufficient()) {
            notifyCallbackOnError(5, "The battery level must be above " + this.mConstants.BATTERY_THRESHOLD_NOT_CHARGING + " while not charging orabove " + this.mConstants.BATTERY_THRESHOLD_CHARGING + " while charging");
            return;
        }
        Thread thread = new Thread(new Runnable() {
            /* class com.android.server.devicepolicy.$$Lambda$UpdateInstaller$CxDofI1o0YOUvaV_mdNG4ke1uck */

            @Override // java.lang.Runnable
            public final void run() {
                UpdateInstaller.this.lambda$startInstallUpdate$0$UpdateInstaller();
            }
        });
        thread.setPriority(10);
        thread.start();
    }

    public /* synthetic */ void lambda$startInstallUpdate$0$UpdateInstaller() {
        this.mCopiedUpdateFile = copyUpdateFileToDataOtaPackageDir();
        if (this.mCopiedUpdateFile == null) {
            notifyCallbackOnError(1, "Error while copying file.");
        } else {
            installUpdateInThread();
        }
    }

    private boolean isBatteryLevelSufficient() {
        Intent batteryStatus = this.mContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        float batteryPercentage = calculateBatteryPercentage(batteryStatus);
        return batteryStatus.getIntExtra("plugged", -1) > 0 ? batteryPercentage >= ((float) this.mConstants.BATTERY_THRESHOLD_CHARGING) : batteryPercentage >= ((float) this.mConstants.BATTERY_THRESHOLD_NOT_CHARGING);
    }

    private float calculateBatteryPercentage(Intent batteryStatus) {
        return ((float) (batteryStatus.getIntExtra("level", -1) * 100)) / ((float) batteryStatus.getIntExtra("scale", -1));
    }

    private File copyUpdateFileToDataOtaPackageDir() {
        try {
            File destination = createNewFileWithPermissions();
            copyToFile(destination);
            return destination;
        } catch (IOException e) {
            Log.w(TAG, "Failed to copy update file to OTA directory", e);
            notifyCallbackOnError(1, Log.getStackTraceString(e));
            return null;
        }
    }

    private File createNewFileWithPermissions() throws IOException {
        File destination = File.createTempFile("update", ".zip", new File(Environment.getDataDirectory() + "/ota_package"));
        FileUtils.setPermissions(destination, 484, -1, -1);
        return destination;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001a, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001b, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001e, code lost:
        throw r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0021, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0022, code lost:
        $closeResource(r1, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0025, code lost:
        throw r2;
     */
    private void copyToFile(File destination) throws IOException {
        OutputStream out = new FileOutputStream(destination);
        InputStream in = new ParcelFileDescriptor.AutoCloseInputStream(this.mUpdateFileDescriptor);
        FileUtils.copy(in, out);
        $closeResource(null, in);
        $closeResource(null, out);
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    /* access modifiers changed from: package-private */
    public void cleanupUpdateFile() {
        File file = this.mCopiedUpdateFile;
        if (file != null && file.exists()) {
            this.mCopiedUpdateFile.delete();
        }
    }

    /* access modifiers changed from: protected */
    public void notifyCallbackOnError(int errorCode, String errorMessage) {
        cleanupUpdateFile();
        DevicePolicyEventLogger.createEvent(74).setInt(errorCode).write();
        try {
            this.mCallback.onStartInstallingUpdateError(errorCode, errorMessage);
        } catch (RemoteException e) {
            Log.d(TAG, "Error while calling callback", e);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyCallbackOnSuccess() {
        cleanupUpdateFile();
        this.mInjector.powerManagerReboot("deviceowner");
    }
}
