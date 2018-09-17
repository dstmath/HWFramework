package com.huawei.android.media.session;

import android.content.ComponentName;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import java.util.List;

public class MediaSessionManagerEx {
    public static List<MediaController> getActiveSessionsForUser(MediaSessionManager mediaSessionManager, ComponentName notificationListener, int userId) {
        return mediaSessionManager.getActiveSessionsForUser(notificationListener, userId);
    }
}
