package android.content.pm.dex;

import android.annotation.SystemApi;
import android.content.Context;
import android.content.pm.dex.ArtManager;
import android.content.pm.dex.ISnapshotRuntimeProfileCallback;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Slog;
import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.Executor;

@SystemApi
public class ArtManager {
    public static final int PROFILE_APPS = 0;
    public static final int PROFILE_BOOT_IMAGE = 1;
    public static final int SNAPSHOT_FAILED_CODE_PATH_NOT_FOUND = 1;
    public static final int SNAPSHOT_FAILED_INTERNAL_ERROR = 2;
    public static final int SNAPSHOT_FAILED_PACKAGE_NOT_FOUND = 0;
    private static final String TAG = "ArtManager";
    private final IArtManager mArtManager;
    private final Context mContext;

    @Retention(RetentionPolicy.SOURCE)
    public @interface ProfileType {
    }

    public static abstract class SnapshotRuntimeProfileCallback {
        public abstract void onError(int i);

        public abstract void onSuccess(ParcelFileDescriptor parcelFileDescriptor);
    }

    private static class SnapshotRuntimeProfileCallbackDelegate extends ISnapshotRuntimeProfileCallback.Stub {
        private final SnapshotRuntimeProfileCallback mCallback;
        private final Executor mExecutor;

        private SnapshotRuntimeProfileCallbackDelegate(SnapshotRuntimeProfileCallback callback, Executor executor) {
            this.mCallback = callback;
            this.mExecutor = executor;
        }

        public void onSuccess(ParcelFileDescriptor profileReadFd) {
            this.mExecutor.execute(new Runnable(profileReadFd) {
                private final /* synthetic */ ParcelFileDescriptor f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ArtManager.SnapshotRuntimeProfileCallbackDelegate.this.mCallback.onSuccess(this.f$1);
                }
            });
        }

        public void onError(int errCode) {
            this.mExecutor.execute(new Runnable(errCode) {
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    ArtManager.SnapshotRuntimeProfileCallbackDelegate.this.mCallback.onError(this.f$1);
                }
            });
        }
    }

    public ArtManager(Context context, IArtManager manager) {
        this.mContext = context;
        this.mArtManager = manager;
    }

    public void snapshotRuntimeProfile(int profileType, String packageName, String codePath, Executor executor, SnapshotRuntimeProfileCallback callback) {
        Slog.d(TAG, "Requesting profile snapshot for " + packageName + ":" + codePath);
        try {
            this.mArtManager.snapshotRuntimeProfile(profileType, packageName, codePath, new SnapshotRuntimeProfileCallbackDelegate(callback, executor), this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public boolean isRuntimeProfilingEnabled(int profileType) {
        try {
            return this.mArtManager.isRuntimeProfilingEnabled(profileType, this.mContext.getOpPackageName());
        } catch (RemoteException e) {
            throw e.rethrowAsRuntimeException();
        }
    }

    public static String getProfileName(String splitName) {
        if (splitName == null) {
            return "primary.prof";
        }
        return splitName + ".split.prof";
    }

    public static String getCurrentProfilePath(String packageName, int userId, String splitName) {
        return new File(Environment.getDataProfilesDePackageDirectory(userId, packageName), getProfileName(splitName)).getAbsolutePath();
    }

    public static File getProfileSnapshotFileForName(String packageName, String profileName) {
        File profileDir = Environment.getDataRefProfilesDePackageDirectory(packageName);
        return new File(profileDir, profileName + ".snapshot");
    }
}
