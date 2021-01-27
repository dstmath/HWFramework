package ohos.media.common.sessioncore;

import java.util.List;
import ohos.media.common.AVMetadata;
import ohos.utils.PacMap;

public abstract class AVControllerCallback {
    public void onAVMetadataChanged(AVMetadata aVMetadata) {
    }

    public void onAVPlaybackInfoChanged(AVPlaybackInfo aVPlaybackInfo) {
    }

    public void onAVPlaybackStateChanged(AVPlaybackState aVPlaybackState) {
    }

    public void onAVQueueChanged(List<AVQueueElement> list) {
    }

    public void onAVQueueTitleChanged(CharSequence charSequence) {
    }

    public void onAVSessionDestroyed() {
    }

    public void onAVSessionEvent(String str, PacMap pacMap) {
    }

    public void onOptionsChanged(PacMap pacMap) {
    }
}
