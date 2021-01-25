package ohos.media.common.adapter;

import android.media.session.MediaSessionManager;
import ohos.media.common.sessioncore.AVCallerUserInfo;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public final class AVCallerUserInfoAdapter {
    private static final Logger LOGGER = LoggerFactory.getImageLogger(AVDescriptionAdapter.class);

    public static AVCallerUserInfo getAVCallerUserInfo(MediaSessionManager.RemoteUserInfo remoteUserInfo) {
        if (remoteUserInfo != null) {
            return new AVCallerUserInfo(remoteUserInfo.getPackageName(), remoteUserInfo.getPid(), remoteUserInfo.getUid());
        }
        LOGGER.error("getAVCallerUserInfo failed, remoteUserInfo is null", new Object[0]);
        return null;
    }

    public static MediaSessionManager.RemoteUserInfo getRemoteUserInfo(AVCallerUserInfo aVCallerUserInfo) {
        if (aVCallerUserInfo != null) {
            return new MediaSessionManager.RemoteUserInfo(aVCallerUserInfo.getCallerPackageName(), aVCallerUserInfo.getCallerPid(), aVCallerUserInfo.getCallerUid());
        }
        LOGGER.error("getRemoteUserInfo failed, remoteUserInfo is null", new Object[0]);
        return null;
    }
}
