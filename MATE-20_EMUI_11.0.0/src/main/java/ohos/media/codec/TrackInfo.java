package ohos.media.codec;

import java.util.HashMap;
import ohos.media.common.Format;

public class TrackInfo {
    private int status = 0;
    private final HashMap<Integer, Format> supportedTracksInfoMap = new HashMap<>();
    private final HashMap<Integer, Format> unsupportedTracksInfoMap = new HashMap<>();

    private void setStatus(int i) {
        this.status = i;
    }

    public int getStatus() {
        return this.status;
    }

    private boolean setTracksInfoMap(int i, Format format, boolean z) {
        if (z) {
            this.supportedTracksInfoMap.put(Integer.valueOf(i), format);
            return true;
        }
        this.unsupportedTracksInfoMap.put(Integer.valueOf(i), format);
        return true;
    }

    public HashMap<Integer, Format> getSupportedTracksInfoMap() {
        return this.supportedTracksInfoMap;
    }

    public HashMap<Integer, Format> getUnsupportedTracksInfoMap() {
        return this.unsupportedTracksInfoMap;
    }
}
