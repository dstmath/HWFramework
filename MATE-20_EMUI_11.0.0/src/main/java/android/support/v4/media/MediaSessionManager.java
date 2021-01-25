package android.support.v4.media;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaSessionManagerImplApi28;
import android.support.v4.media.MediaSessionManagerImplBase;
import android.util.Log;

public final class MediaSessionManager {
    static final boolean DEBUG = Log.isLoggable(TAG, 3);
    static final String TAG = "MediaSessionManager";
    private static final Object sLock = new Object();
    private static volatile MediaSessionManager sSessionManager;
    MediaSessionManagerImpl mImpl;

    interface MediaSessionManagerImpl {
        Context getContext();

        boolean isTrustedForMediaControl(RemoteUserInfoImpl remoteUserInfoImpl);
    }

    interface RemoteUserInfoImpl {
        String getPackageName();

        int getPid();

        int getUid();
    }

    @NonNull
    public static MediaSessionManager getSessionManager(@NonNull Context context) {
        MediaSessionManager manager = sSessionManager;
        if (manager == null) {
            synchronized (sLock) {
                manager = sSessionManager;
                if (manager == null) {
                    sSessionManager = new MediaSessionManager(context.getApplicationContext());
                    manager = sSessionManager;
                }
            }
        }
        return manager;
    }

    private MediaSessionManager(Context context) {
        if (Build.VERSION.SDK_INT >= 28) {
            this.mImpl = new MediaSessionManagerImplApi28(context);
        } else if (Build.VERSION.SDK_INT >= 21) {
            this.mImpl = new MediaSessionManagerImplApi21(context);
        } else {
            this.mImpl = new MediaSessionManagerImplBase(context);
        }
    }

    public boolean isTrustedForMediaControl(@NonNull RemoteUserInfo userInfo) {
        if (userInfo != null) {
            return this.mImpl.isTrustedForMediaControl(userInfo.mImpl);
        }
        throw new IllegalArgumentException("userInfo should not be null");
    }

    /* access modifiers changed from: package-private */
    public Context getContext() {
        return this.mImpl.getContext();
    }

    public static final class RemoteUserInfo {
        public static final String LEGACY_CONTROLLER = "android.media.session.MediaController";
        RemoteUserInfoImpl mImpl;

        public RemoteUserInfo(@NonNull String packageName, int pid, int uid) {
            if (Build.VERSION.SDK_INT >= 28) {
                this.mImpl = new MediaSessionManagerImplApi28.RemoteUserInfo(packageName, pid, uid);
            } else {
                this.mImpl = new MediaSessionManagerImplBase.RemoteUserInfo(packageName, pid, uid);
            }
        }

        @NonNull
        public String getPackageName() {
            return this.mImpl.getPackageName();
        }

        public int getPid() {
            return this.mImpl.getPid();
        }

        public int getUid() {
            return this.mImpl.getUid();
        }

        public boolean equals(@Nullable Object obj) {
            return this.mImpl.equals(obj);
        }

        public int hashCode() {
            return this.mImpl.hashCode();
        }
    }
}
