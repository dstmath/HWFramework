package com.huawei.android.media.session;

import android.content.ComponentName;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.util.Log;
import java.util.List;

public class MediaSessionManagerEx {
    private static final String TAG = "MediaSessionManagerEx";

    public static List<MediaController> getActiveSessionsForUser(MediaSessionManager mediaSessionManager, ComponentName notificationListener, int userId) {
        if (mediaSessionManager != null) {
            return mediaSessionManager.getActiveSessionsForUser(notificationListener, userId);
        }
        Log.e(TAG, "MediaSessionManager is null");
        return null;
    }
}
