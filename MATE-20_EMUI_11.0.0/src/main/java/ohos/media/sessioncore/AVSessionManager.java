package ohos.media.sessioncore;

import java.util.List;
import java.util.stream.Collectors;
import ohos.app.Context;
import ohos.bundle.ElementName;
import ohos.media.common.sessioncore.AVCallerUserInfo;
import ohos.media.sessioncore.adapter.AVControllerAdapter;
import ohos.media.sessioncore.adapter.AVSessionManagerAdapter;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public final class AVSessionManager {
    private static final Logger LOGGER = LoggerFactory.getAudioLogger(AVSessionManager.class);
    private static volatile AVSessionManager sSessionManager;
    private AVSessionManagerAdapter mAdapter;

    public static AVSessionManager getSessionManager(Context context) {
        if (context == null || context.getApplicationContext() == null) {
            LOGGER.error("[AVSessionManager] context should not be null.", new Object[0]);
            throw new IllegalArgumentException("context should not be null");
        }
        if (sSessionManager == null) {
            synchronized (AVSessionManager.class) {
                if (sSessionManager == null) {
                    sSessionManager = new AVSessionManager(context.getApplicationContext());
                }
            }
        }
        return sSessionManager;
    }

    private AVSessionManager(Context context) {
        this.mAdapter = new AVSessionManagerAdapter(context);
    }

    public boolean isControlTrusted(AVCallerUserInfo aVCallerUserInfo) {
        if (aVCallerUserInfo != null) {
            return this.mAdapter.isTrustedForMediaControl(aVCallerUserInfo);
        }
        LOGGER.error("[AVSessionManager] userInfo should not be null.", new Object[0]);
        throw new IllegalArgumentException("userInfo should not be null");
    }

    public List<AVController> getActiveAVControllers(ElementName elementName) {
        return (List) this.mAdapter.getActiveAVControllers(elementName).stream().map($$Lambda$AVSessionManager$ZK59LGZFMiotlWBhy7PITyocZEY.INSTANCE).collect(Collectors.toList());
    }

    static /* synthetic */ AVController lambda$getActiveAVControllers$0(AVControllerAdapter aVControllerAdapter) {
        return new AVController(aVControllerAdapter);
    }
}
