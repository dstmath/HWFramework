package ohos.agp.window.service;

import java.util.Optional;
import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.window.wmc.DisplayManagerWrapper;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class DisplayManager {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) LogDomain.END, "DisplayManager");
    private static volatile DisplayManager sInstance;

    private DisplayManager() {
    }

    public static DisplayManager getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        synchronized (DisplayManager.class) {
            if (sInstance == null) {
                sInstance = new DisplayManager();
            }
        }
        return sInstance;
    }

    public Optional<Display> getDefaultDisplay(Context context) {
        Optional<DisplayManagerWrapper.DisplayWrapper> defaultDisplay = DisplayManagerWrapper.getInstance().getDefaultDisplay(context);
        if (defaultDisplay.isPresent()) {
            return Optional.of(new Display(defaultDisplay.get()));
        }
        HiLog.error(LABEL, "DisplayManager getDefaultDisplay return null", new Object[0]);
        return Optional.empty();
    }
}
