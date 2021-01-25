package ohos.media.sessioncore.adapter;

import android.content.ComponentName;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import java.util.List;
import java.util.stream.Collectors;
import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.media.common.adapter.AVCallerUserInfoAdapter;
import ohos.media.common.sessioncore.AVCallerUserInfo;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public class AVSessionManagerAdapter {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AVSessionManagerAdapter.class);
    private MediaSessionManager adMediaSessionManager;

    public AVSessionManagerAdapter(Context context) {
        if (context.getHostContext() instanceof android.content.Context) {
            Object systemService = ((android.content.Context) context.getHostContext()).getSystemService("media_session");
            if (systemService instanceof MediaSessionManager) {
                this.adMediaSessionManager = (MediaSessionManager) systemService;
            } else {
                LOGGER.error("MediaSessionManager type is incorrect", new Object[0]);
                throw new IllegalArgumentException("MediaSessionManager cannot be got");
            }
        } else {
            LOGGER.error("context type is incorrect", new Object[0]);
            throw new IllegalArgumentException("context should not be null");
        }
    }

    public boolean isTrustedForMediaControl(AVCallerUserInfo aVCallerUserInfo) {
        return this.adMediaSessionManager.isTrustedForMediaControl(AVCallerUserInfoAdapter.getRemoteUserInfo(aVCallerUserInfo));
    }

    public List<AVControllerAdapter> getActiveAVControllers(ElementName elementName) {
        List<MediaController> activeSessions = this.adMediaSessionManager.getActiveSessions(elementName == null ? null : new ComponentName(elementName.getBundleName(), elementName.getShortClassName()));
        if (activeSessions != null) {
            return (List) activeSessions.stream().map($$Lambda$AVSessionManagerAdapter$s8X0lAY96THShdj6qrhmuvVQdc.INSTANCE).collect(Collectors.toList());
        }
        LOGGER.error("getActiveAVControllers failed, mediaControllers is null", new Object[0]);
        return null;
    }

    static /* synthetic */ AVControllerAdapter lambda$getActiveAVControllers$0(MediaController mediaController) {
        return new AVControllerAdapter(mediaController);
    }
}
