package ohos.media.common.utils;

import android.os.Bundle;
import android.os.SystemClock;
import ohos.utils.PacMap;
import ohos.utils.adapter.PacMapUtils;

public final class AVUtils {
    private AVUtils() {
    }

    public static long elapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    public static Bundle convert2Bundle(PacMap pacMap) {
        return PacMapUtils.convertIntoBundle(pacMap);
    }

    public static PacMap convert2PacMap(Bundle bundle) {
        return PacMapUtils.convertFromBundle(bundle);
    }
}
