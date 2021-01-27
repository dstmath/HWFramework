package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import java.text.ParseException;
import javax.sip.header.EventHeader;

public class Event extends ParametersHeader implements EventHeader {
    private static final long serialVersionUID = -6458387810431874841L;
    protected String eventType;

    public Event() {
        super("Event");
    }

    @Override // javax.sip.header.EventHeader
    public void setEventType(String eventType2) throws ParseException {
        if (eventType2 != null) {
            this.eventType = eventType2;
            return;
        }
        throw new NullPointerException(" the eventType is null");
    }

    @Override // javax.sip.header.EventHeader
    public String getEventType() {
        return this.eventType;
    }

    @Override // javax.sip.header.EventHeader
    public void setEventId(String eventId) throws ParseException {
        if (eventId != null) {
            setParameter(ParameterNames.ID, eventId);
            return;
        }
        throw new NullPointerException(" the eventId parameter is null");
    }

    @Override // javax.sip.header.EventHeader
    public String getEventId() {
        return getParameter(ParameterNames.ID);
    }

    @Override // gov.nist.javax.sip.header.ParametersHeader, gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public StringBuffer encodeBody(StringBuffer buffer) {
        String str = this.eventType;
        if (str != null) {
            buffer.append(str);
        }
        if (!this.parameters.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        return buffer;
    }

    public boolean match(Event matchTarget) {
        if (matchTarget.eventType == null && this.eventType != null) {
            return false;
        }
        if (matchTarget.eventType != null && this.eventType == null) {
            return false;
        }
        if (this.eventType == null && matchTarget.eventType == null) {
            return false;
        }
        if (getEventId() == null && matchTarget.getEventId() != null) {
            return false;
        }
        if ((getEventId() != null && matchTarget.getEventId() == null) || !matchTarget.eventType.equalsIgnoreCase(this.eventType)) {
            return false;
        }
        if (getEventId() == matchTarget.getEventId() || getEventId().equalsIgnoreCase(matchTarget.getEventId())) {
            return true;
        }
        return false;
    }
}
