package gov.nist.javax.sip;

import javax.sip.SipListener;

public interface SipListenerExt extends SipListener {
    void processDialogTimeout(DialogTimeoutEvent dialogTimeoutEvent);
}
