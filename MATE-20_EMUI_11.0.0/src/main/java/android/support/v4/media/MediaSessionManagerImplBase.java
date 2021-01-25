package android.support.v4.media;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaSessionManager;
import android.support.v4.util.ObjectsCompat;
import android.text.TextUtils;
import android.util.Log;

/* access modifiers changed from: package-private */
public class MediaSessionManagerImplBase implements MediaSessionManager.MediaSessionManagerImpl {
    private static final boolean DEBUG = MediaSessionManager.DEBUG;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String PERMISSION_MEDIA_CONTENT_CONTROL = "android.permission.MEDIA_CONTENT_CONTROL";
    private static final String PERMISSION_STATUS_BAR_SERVICE = "android.permission.STATUS_BAR_SERVICE";
    private static final String TAG = "MediaSessionManager";
    ContentResolver mContentResolver = this.mContext.getContentResolver();
    Context mContext;

    MediaSessionManagerImplBase(Context context) {
        this.mContext = context;
    }

    @Override // android.support.v4.media.MediaSessionManager.MediaSessionManagerImpl
    public Context getContext() {
        return this.mContext;
    }

    @Override // android.support.v4.media.MediaSessionManager.MediaSessionManagerImpl
    public boolean isTrustedForMediaControl(@NonNull MediaSessionManager.RemoteUserInfoImpl userInfo) {
        try {
            if (this.mContext.getPackageManager().getApplicationInfo(userInfo.getPackageName(), 0).uid != userInfo.getUid()) {
                if (DEBUG) {
                    Log.d(TAG, "Package name " + userInfo.getPackageName() + " doesn't match with the uid " + userInfo.getUid());
                }
                return DEBUG;
            } else if (isPermissionGranted(userInfo, PERMISSION_STATUS_BAR_SERVICE) || isPermissionGranted(userInfo, PERMISSION_MEDIA_CONTENT_CONTROL) || userInfo.getUid() == 1000 || isEnabledNotificationListener(userInfo)) {
                return true;
            } else {
                return DEBUG;
            }
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG) {
                Log.d(TAG, "Package " + userInfo.getPackageName() + " doesn't exist");
            }
            return DEBUG;
        }
    }

    private boolean isPermissionGranted(MediaSessionManager.RemoteUserInfoImpl userInfo, String permission) {
        if (userInfo.getPid() < 0) {
            if (this.mContext.getPackageManager().checkPermission(permission, userInfo.getPackageName()) == 0) {
                return true;
            }
            return DEBUG;
        } else if (this.mContext.checkPermission(permission, userInfo.getPid(), userInfo.getUid()) == 0) {
            return true;
        } else {
            return DEBUG;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isEnabledNotificationListener(@NonNull MediaSessionManager.RemoteUserInfoImpl userInfo) {
        String[] components;
        String enabledNotifListeners = Settings.Secure.getString(this.mContentResolver, ENABLED_NOTIFICATION_LISTENERS);
        if (enabledNotifListeners != null) {
            for (String str : enabledNotifListeners.split(":")) {
                ComponentName component = ComponentName.unflattenFromString(str);
                if (component != null && component.getPackageName().equals(userInfo.getPackageName())) {
                    return true;
                }
            }
        }
        return DEBUG;
    }

    /* access modifiers changed from: package-private */
    public static class RemoteUserInfo implements MediaSessionManager.RemoteUserInfoImpl {
        private String mPackageName;
        private int mPid;
        private int mUid;

        RemoteUserInfo(String packageName, int pid, int uid) {
            this.mPackageName = packageName;
            this.mPid = pid;
            this.mUid = uid;
        }

        @Override // android.support.v4.media.MediaSessionManager.RemoteUserInfoImpl
        public String getPackageName() {
            return this.mPackageName;
        }

        @Override // android.support.v4.media.MediaSessionManager.RemoteUserInfoImpl
        public int getPid() {
            return this.mPid;
        }

        @Override // android.support.v4.media.MediaSessionManager.RemoteUserInfoImpl
        public int getUid() {
            return this.mUid;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof RemoteUserInfo)) {
                return MediaSessionManagerImplBase.DEBUG;
            }
            RemoteUserInfo otherUserInfo = (RemoteUserInfo) obj;
            if (TextUtils.equals(this.mPackageName, otherUserInfo.mPackageName) && this.mPid == otherUserInfo.mPid && this.mUid == otherUserInfo.mUid) {
                return true;
            }
            return MediaSessionManagerImplBase.DEBUG;
        }

        public int hashCode() {
            return ObjectsCompat.hash(this.mPackageName, Integer.valueOf(this.mPid), Integer.valueOf(this.mUid));
        }
    }
}
