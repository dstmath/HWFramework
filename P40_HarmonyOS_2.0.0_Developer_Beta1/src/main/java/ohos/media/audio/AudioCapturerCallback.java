package ohos.media.audio;

import java.util.List;

public abstract class AudioCapturerCallback {
    public abstract void onCapturerConfigChanged(List<AudioCapturerConfig> list);
}
