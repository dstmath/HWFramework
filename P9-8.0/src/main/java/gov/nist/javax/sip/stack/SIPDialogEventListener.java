package gov.nist.javax.sip.stack;

import java.util.EventListener;

public interface SIPDialogEventListener extends EventListener {
    void dialogErrorEvent(SIPDialogErrorEvent sIPDialogErrorEvent);
}
