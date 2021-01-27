package gov.nist.javax.sip;

import gov.nist.javax.sip.stack.SIPTransaction;
import java.util.EventObject;

class EventWrapper {
    protected EventObject sipEvent;
    protected SIPTransaction transaction;

    EventWrapper(EventObject sipEvent2, SIPTransaction transaction2) {
        this.sipEvent = sipEvent2;
        this.transaction = transaction2;
    }
}
