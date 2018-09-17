package android.media.session;

import android.app.PendingIntent;
import android.content.Context;

public interface HwMediaSessionManager {
    void updateTargetInService(PendingIntent pendingIntent, Context context);
}
