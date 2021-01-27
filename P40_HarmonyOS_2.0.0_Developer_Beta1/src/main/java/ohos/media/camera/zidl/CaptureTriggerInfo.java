package ohos.media.camera.zidl;

import java.util.Objects;

public class CaptureTriggerInfo {
    private final int id;
    private final long lastFrameNumber;

    public CaptureTriggerInfo(int i, long j) {
        this.id = i;
        this.lastFrameNumber = j;
    }

    public int getId() {
        return this.id;
    }

    public long getLastFrameNumber() {
        return this.lastFrameNumber;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CaptureTriggerInfo)) {
            return false;
        }
        CaptureTriggerInfo captureTriggerInfo = (CaptureTriggerInfo) obj;
        return this.id == captureTriggerInfo.id && this.lastFrameNumber == captureTriggerInfo.lastFrameNumber;
    }

    public int hashCode() {
        return Objects.hash(Integer.valueOf(this.id), Long.valueOf(this.lastFrameNumber));
    }
}
