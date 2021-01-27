package ohos.msdp.motion;

public class DeviceMotionEvent {
    public static final int MOTION_DIRECTION_DEFAULT = 0;
    private int direction = 0;
    private int type = 0;

    public DeviceMotionEvent(int i, int i2) {
        this.type = i;
        this.direction = i2;
    }

    public int getType() {
        return this.type;
    }

    public int getDirection() {
        return this.direction;
    }
}
