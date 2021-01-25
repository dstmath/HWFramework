package ohos.multimodalinput.event;

import java.util.UUID;
import ohos.utils.Parcel;
import ohos.utils.Sequenceable;

public abstract class MultimodalEvent implements Sequenceable {
    public static final int BUILTIN_KEY = 4;
    public static final int DAY_MODE = 5;
    public static final int DEFAULT_TYPE = -1;
    public static final int KEYBOARD = 1;
    public static final int MOUSE = 2;
    public static final int MUTE = 91;
    public static final int NAVIGATION_DOWN = 281;
    public static final int NAVIGATION_LEFT = 282;
    public static final int NAVIGATION_RIGHT = 283;
    public static final int NAVIGATION_UP = 280;
    public static final int NIGHT_MODE = 4;
    public static final int ROTATION = 5;
    public static final int SPEECH = 6;
    public static final int STYLUS = 3;
    public static final int TOUCH_PANEL = 0;
    public static final int UNSUPPORTED_DEVICE = -1;
    protected int highLevelEvent = -1;
    protected final UUID uuid = UUID.randomUUID();

    public abstract String getDeviceId();

    public abstract int getInputDeviceId();

    public abstract long getOccurredTime();

    public abstract int getSourceDevice();

    public boolean isHighLevelInput() {
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean marshalling(Parcel parcel) {
        return false;
    }

    @Override // ohos.utils.Sequenceable
    public boolean unmarshalling(Parcel parcel) {
        return false;
    }

    public boolean isSameEvent(UUID uuid2) {
        return uuid2 == this.uuid;
    }

    public int getHighLevelEvent() {
        return this.highLevelEvent;
    }

    public UUID getUuid() {
        return this.uuid;
    }
}
