package android.support.v4.media;

import android.content.Context;
import android.media.session.MediaSessionManager;
import android.support.annotation.RequiresApi;
import android.support.v4.media.MediaSessionManager;

@RequiresApi(28)
class MediaSessionManagerImplApi28 extends MediaSessionManagerImplApi21 {
    MediaSessionManager mObject;

    static final class RemoteUserInfo implements MediaSessionManager.RemoteUserInfoImpl {
        MediaSessionManager.RemoteUserInfo mObject;

        RemoteUserInfo(String packageName, int pid, int uid) {
            this.mObject = new MediaSessionManager.RemoteUserInfo(packageName, pid, uid);
        }

        public String getPackageName() {
            return this.mObject.getPackageName();
        }

        public int getPid() {
            return this.mObject.getPid();
        }

        public int getUid() {
            return this.mObject.getUid();
        }
    }

    MediaSessionManagerImplApi28(Context context) {
        super(context);
        this.mObject = (android.media.session.MediaSessionManager) context.getSystemService("media_session");
    }

    public boolean isTrustedForMediaControl(MediaSessionManager.RemoteUserInfoImpl userInfo) {
        if (userInfo instanceof RemoteUserInfo) {
            return this.mObject.isTrustedForMediaControl(((RemoteUserInfo) userInfo).mObject);
        }
        return false;
    }
}
