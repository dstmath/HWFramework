package com.huawei.systemmanager.optimize;

import android.hsm.MediaTransactWrapper;
import java.util.Set;

public class HwMediaTransactWrapperEx {
    public static Set<Integer> playingMusicUidSet() {
        return MediaTransactWrapper.playingMusicUidSet();
    }

    public static void musicPausedOrStopped(int uid, int pid) {
        MediaTransactWrapper.musicPausedOrStopped(uid, pid);
    }
}
