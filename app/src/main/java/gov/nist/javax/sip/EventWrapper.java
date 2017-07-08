package gov.nist.javax.sip;

import gov.nist.javax.sip.stack.SIPTransaction;
import java.util.EventObject;

class EventWrapper {
    protected EventObject sipEvent;
    protected SIPTransaction transaction;

    EventWrapper(EventObject sipEvent, SIPTransaction transaction) {
        this.sipEvent = sipEvent;
        this.transaction = transaction;
    }
}
