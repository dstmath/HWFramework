package ohos.media.audio;

import java.util.List;

public abstract class AudioRendererCallback {
    public abstract void onRendererConfigChanged(List<AudioRendererInfo> list);
}
