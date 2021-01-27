package ohos.org.w3c.dom.events;

public class EventException extends RuntimeException {
    public static final short UNSPECIFIED_EVENT_TYPE_ERR = 0;
    public short code;

    public EventException(short s, String str) {
        super(str);
        this.code = s;
    }
}
