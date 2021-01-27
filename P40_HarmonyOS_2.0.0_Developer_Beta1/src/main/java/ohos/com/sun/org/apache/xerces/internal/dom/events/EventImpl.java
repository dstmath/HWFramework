package ohos.com.sun.org.apache.xerces.internal.dom.events;

import ohos.org.w3c.dom.events.Event;
import ohos.org.w3c.dom.events.EventTarget;

public class EventImpl implements Event {
    public boolean bubbles = true;
    public boolean cancelable = false;
    public EventTarget currentTarget;
    public short eventPhase;
    public boolean initialized = false;
    public boolean preventDefault = false;
    public boolean stopPropagation = false;
    public EventTarget target;
    protected long timeStamp = System.currentTimeMillis();
    public String type = null;

    public void initEvent(String str, boolean z, boolean z2) {
        this.type = str;
        this.bubbles = z;
        this.cancelable = z2;
        this.initialized = true;
    }

    public boolean getBubbles() {
        return this.bubbles;
    }

    public boolean getCancelable() {
        return this.cancelable;
    }

    public EventTarget getCurrentTarget() {
        return this.currentTarget;
    }

    public short getEventPhase() {
        return this.eventPhase;
    }

    public EventTarget getTarget() {
        return this.target;
    }

    public String getType() {
        return this.type;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void stopPropagation() {
        this.stopPropagation = true;
    }

    public void preventDefault() {
        this.preventDefault = true;
    }
}
