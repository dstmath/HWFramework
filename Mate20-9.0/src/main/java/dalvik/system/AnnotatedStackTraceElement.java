package dalvik.system;

public class AnnotatedStackTraceElement {
    private Object blockedOn;
    private Object[] heldLocks;
    private StackTraceElement stackTraceElement;

    private AnnotatedStackTraceElement() {
    }

    public StackTraceElement getStackTraceElement() {
        return this.stackTraceElement;
    }

    public Object[] getHeldLocks() {
        return this.heldLocks;
    }

    public Object getBlockedOn() {
        return this.blockedOn;
    }
}
