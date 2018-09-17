package android.media.session;

import android.app.PendingIntent;
import android.content.Context;

public class HwMediaSessionManagerDummy implements HwMediaSessionManager {
    private static HwMediaSessionManager mInstance = new HwMediaSessionManagerDummy();

    public static HwMediaSessionManager getDefault() {
        return mInstance;
    }

    public void updateTargetInService(PendingIntent mbr, Context context) {
    }
}
