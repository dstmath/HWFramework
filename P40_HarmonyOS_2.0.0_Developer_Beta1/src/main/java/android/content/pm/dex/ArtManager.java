package android.content.pm.dex;

import android.annotation.SystemApi;
import android.content.Context;
import android.content.pm.dex.ArtManager;
import android.content.pm.dex.ISnapshotRuntimeProfileCallback;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.SettingsStringUtil;
import android.util.Slog;
import java.io.File;
import java.io.IOException;
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

    public ArtManager(Context context, IArtManager manager) {
        this.mContext = context;
        this.mArtManager = manager;
    }

    public void snapshotRuntimeProfile(int profileType, String packageName, String codePath, Executor executor, SnapshotRuntimeProfileCallback callback) {
        Slog.d(TAG, "Requesting profile snapshot for " + packageName + SettingsStringUtil.DELIMITER + codePath);
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

    /* access modifiers changed from: private */
    public static class SnapshotRuntimeProfileCallbackDelegate extends ISnapshotRuntimeProfileCallback.Stub {
        private final SnapshotRuntimeProfileCallback mCallback;
        private final Executor mExecutor;

        private SnapshotRuntimeProfileCallbackDelegate(SnapshotRuntimeProfileCallback callback, Executor executor) {
            this.mCallback = callback;
            this.mExecutor = executor;
        }

        public /* synthetic */ void lambda$onSuccess$0$ArtManager$SnapshotRuntimeProfileCallbackDelegate(ParcelFileDescriptor profileReadFd) {
            this.mCallback.onSuccess(profileReadFd);
        }

        @Override // android.content.pm.dex.ISnapshotRuntimeProfileCallback
        public void onSuccess(ParcelFileDescriptor profileReadFd) {
            this.mExecutor.execute(new Runnable(profileReadFd) {
                /* class android.content.pm.dex.$$Lambda$ArtManager$SnapshotRuntimeProfileCallbackDelegate$OOdGv4iFxuVpH2kzFMr8KwX3X8s */
                private final /* synthetic */ ParcelFileDescriptor f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ArtManager.SnapshotRuntimeProfileCallbackDelegate.this.lambda$onSuccess$0$ArtManager$SnapshotRuntimeProfileCallbackDelegate(this.f$1);
                }
            });
        }

        public /* synthetic */ void lambda$onError$1$ArtManager$SnapshotRuntimeProfileCallbackDelegate(int errCode) {
            this.mCallback.onError(errCode);
        }

        @Override // android.content.pm.dex.ISnapshotRuntimeProfileCallback
        public void onError(int errCode) {
            this.mExecutor.execute(new Runnable(errCode) {
                /* class android.content.pm.dex.$$Lambda$ArtManager$SnapshotRuntimeProfileCallbackDelegate$m2Wpsf6LxhWt_1tS6tQt3B8QcGo */
                private final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    ArtManager.SnapshotRuntimeProfileCallbackDelegate.this.lambda$onError$1$ArtManager$SnapshotRuntimeProfileCallbackDelegate(this.f$1);
                }
            });
        }
    }

    public static String getProfileName(String splitName) {
        if (splitName == null) {
            return "primary.prof";
        }
        return splitName + ".split.prof";
    }

    public static String getHarmonyProfileName(String hapName) {
        if (hapName == null) {
            return "primary.prof";
        }
        return hapName + ".prof";
    }

    public static String getCurrentProfilePath(String packageName, int userId, String splitName) {
        return new File(Environment.getDataProfilesDePackageDirectory(userId, packageName), getProfileName(splitName)).getAbsolutePath();
    }

    public static String getCurrentHarmonyProfilePath(String packageName, int userId, String hapName) {
        try {
            return new File(Environment.getDataProfilesDePackageDirectory(userId, packageName), getHarmonyProfileName(hapName)).getCanonicalPath();
        } catch (IOException e) {
            return null;
        }
    }

    public static File getProfileSnapshotFileForName(String packageName, String profileName) {
        File profileDir = Environment.getDataRefProfilesDePackageDirectory(packageName);
        return new File(profileDir, profileName + ".snapshot");
    }
}
