package ohos.aafwk.ability;

import ohos.annotation.SystemApi;
import ohos.media.image.PixelMap;

@SystemApi
public class MissionInformation {
    private static final int COLOR_MIN = 0;
    private PixelMap icon;
    private String label;

    public MissionInformation() {
        this(null, null);
    }

    public MissionInformation(String str) {
        this(str, null);
    }

    public MissionInformation(String str, PixelMap pixelMap) {
        this.label = str;
        this.icon = pixelMap;
    }

    public String getLabel() {
        return this.label;
    }

    public PixelMap getIcon() {
        return this.icon;
    }

    public void setLabel(String str) {
        this.label = str;
    }

    public void setIcon(PixelMap pixelMap) {
        this.icon = pixelMap;
    }
}
