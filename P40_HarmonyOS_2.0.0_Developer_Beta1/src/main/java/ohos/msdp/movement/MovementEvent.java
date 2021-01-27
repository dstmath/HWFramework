package ohos.msdp.movement;

public class MovementEvent {
    private int eventType;
    private int movement;
    private long timestamp;

    public MovementEvent(int i, int i2, long j) {
        this.movement = i;
        this.eventType = i2;
        this.timestamp = j;
    }

    public int getMovement() {
        return this.movement;
    }

    public int getEventType() {
        return this.eventType;
    }

    public long getTimestamp() {
        return this.timestamp;
    }
}
