package android.support.v4.media;

import android.content.Context;
import android.media.session.MediaSessionManager;
import android.support.annotation.RequiresApi;
import android.support.v4.media.MediaSessionManager;

/* access modifiers changed from: package-private */
@RequiresApi(28)
public class MediaSessionManagerImplApi28 extends MediaSessionManagerImplApi21 {
    MediaSessionManager mObject;

    MediaSessionManagerImplApi28(Context context) {
        super(context);
        this.mObject = (MediaSessionManager) context.getSystemService("media_session");
    }

    @Override // android.support.v4.media.MediaSessionManagerImplApi21, android.support.v4.media.MediaSessionManagerImplBase, android.support.v4.media.MediaSessionManager.MediaSessionManagerImpl
    public boolean isTrustedForMediaControl(MediaSessionManager.RemoteUserInfoImpl userInfo) {
        if (userInfo instanceof RemoteUserInfo) {
            return this.mObject.isTrustedForMediaControl(((RemoteUserInfo) userInfo).mObject);
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public static final class RemoteUserInfo implements MediaSessionManager.RemoteUserInfoImpl {
        MediaSessionManager.RemoteUserInfo mObject;

        RemoteUserInfo(String packageName, int pid, int uid) {
            this.mObject = new MediaSessionManager.RemoteUserInfo(packageName, pid, uid);
        }

        @Override // android.support.v4.media.MediaSessionManager.RemoteUserInfoImpl
        public String getPackageName() {
            return this.mObject.getPackageName();
        }

        @Override // android.support.v4.media.MediaSessionManager.RemoteUserInfoImpl
        public int getPid() {
            return this.mObject.getPid();
        }

        @Override // android.support.v4.media.MediaSessionManager.RemoteUserInfoImpl
        public int getUid() {
            return this.mObject.getUid();
        }
    }
}
