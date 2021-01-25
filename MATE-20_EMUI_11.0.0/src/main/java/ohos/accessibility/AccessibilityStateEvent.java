package ohos.accessibility;

public class AccessibilityStateEvent {
    public static final int EVENT_ACCESSIBILITY_STATE_CHANGED = 1;
    public static final int EVENT_TOUCH_BROWSE_STATE_CHANGED = 2;
    private String eventMsg;
    private int eventResult;
    private int eventType;

    public int getEventType() {
        return this.eventType;
    }

    public int getEventResult() {
        return this.eventResult;
    }

    public String getEventMsg() {
        return this.eventMsg;
    }

    public void setEventType(int i) {
        this.eventType = i;
    }

    public void setEventResult(int i) {
        this.eventResult = i;
    }

    public void setEventMsg(String str) {
        this.eventMsg = str;
    }
}
