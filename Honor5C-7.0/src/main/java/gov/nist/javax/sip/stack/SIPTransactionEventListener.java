package gov.nist.javax.sip.stack;

import java.util.EventListener;

public interface SIPTransactionEventListener extends EventListener {
    void transactionErrorEvent(SIPTransactionErrorEvent sIPTransactionErrorEvent);
}
