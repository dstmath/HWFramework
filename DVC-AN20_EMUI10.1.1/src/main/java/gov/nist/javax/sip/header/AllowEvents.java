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

    @Override // javax.sip.header.AllowEventsHeader
    public void setEventType(String eventType2) throws ParseException {
        if (eventType2 != null) {
            this.eventType = eventType2;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception,AllowEvents, setEventType(), the eventType parameter is null");
    }

    @Override // javax.sip.header.AllowEventsHeader
    public String getEventType() {
        return this.eventType;
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return this.eventType;
    }
}
