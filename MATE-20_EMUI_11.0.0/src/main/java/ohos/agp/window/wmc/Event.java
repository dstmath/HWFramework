package ohos.agp.window.wmc;

public class Event {
    private EventType mEventType;
    private Point2D mPoint;

    public Event(EventType eventType, Point2D point2D) {
        this.mEventType = eventType;
        this.mPoint = point2D;
    }

    public EventType getEventType() {
        return this.mEventType;
    }

    public Point2D getPoint() {
        return this.mPoint;
    }

    public enum EventType {
        TOUCH_DOWN(100),
        TOUCH_UP(101),
        TOUCH_MOTION(102),
        UNKNOWN(-100);
        
        private final int value;

        private EventType(int i) {
            this.value = i;
        }

        public int getValue() {
            return this.value;
        }
    }
}
