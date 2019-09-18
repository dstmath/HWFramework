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

    public void setEventType(String eventType2) throws ParseException {
        if (eventType2 != null) {
            this.eventType = eventType2;
            return;
        }
        throw new NullPointerException(" the eventType is null");
    }

    public String getEventType() {
        return this.eventType;
    }

    public void setEventId(String eventId) throws ParseException {
        if (eventId != null) {
            setParameter(ParameterNames.ID, eventId);
            return;
        }
        throw new NullPointerException(" the eventId parameter is null");
    }

    public String getEventId() {
        return getParameter(ParameterNames.ID);
    }

    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    public StringBuffer encodeBody(StringBuffer buffer) {
        if (this.eventType != null) {
            buffer.append(this.eventType);
        }
        if (!this.parameters.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        return buffer;
    }

    public boolean match(Event matchTarget) {
        boolean z = false;
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
        if (getEventId() != null && matchTarget.getEventId() == null) {
            return false;
        }
        if (matchTarget.eventType.equalsIgnoreCase(this.eventType) && (getEventId() == matchTarget.getEventId() || getEventId().equalsIgnoreCase(matchTarget.getEventId()))) {
            z = true;
        }
        return z;
    }
}
