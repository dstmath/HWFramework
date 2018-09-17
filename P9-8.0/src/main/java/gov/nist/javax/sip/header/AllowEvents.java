package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.AllowEventsHeader;

public final class AllowEvents extends SIPHeader implements AllowEventsHeader {
    private static final long serialVersionUID = 617962431813193114L;
    protected String eventType;

    public AllowEvents() {
        super("Allow-Events");
    }

    public AllowEvents(String m) {
        super("Allow-Events");
        this.eventType = m;
    }

    public void setEventType(String eventType) throws ParseException {
        if (eventType == null) {
            throw new NullPointerException("JAIN-SIP Exception,AllowEvents, setEventType(), the eventType parameter is null");
        }
        this.eventType = eventType;
    }

    public String getEventType() {
        return this.eventType;
    }

    protected String encodeBody() {
        return this.eventType;
    }
}
