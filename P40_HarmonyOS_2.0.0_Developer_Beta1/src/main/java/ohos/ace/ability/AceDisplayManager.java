package ohos.ace.ability;

import java.util.Optional;
import ohos.agp.window.wmc.DisplayManagerWrapper;
import ohos.app.Context;

public class AceDisplayManager {
    private static final float DEFAULT_REFRESH_RATE = 60.0f;
    private static final String LOG_TAG = "AceDisplayManager";
    private static AceDisplayManager instance = null;
    private static float refreshRateFPS = 60.0f;

    public static void initRefreshRate(Context context) {
        synchronized (AceDisplayManager.class) {
            Optional<DisplayManagerWrapper.DisplayWrapper> defaultDisplay = DisplayManagerWrapper.getInstance().getDefaultDisplay(context);
            if (defaultDisplay.isPresent()) {
                refreshRateFPS = defaultDisplay.get().getRefreshRate();
            }
        }
    }
}
