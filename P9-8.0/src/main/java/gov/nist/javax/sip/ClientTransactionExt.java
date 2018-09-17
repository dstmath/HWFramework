package gov.nist.javax.sip;

import javax.sip.ClientTransaction;
import javax.sip.address.Hop;

public interface ClientTransactionExt extends ClientTransaction, TransactionExt {
    void alertIfStillInCallingStateBy(int i);

    Hop getNextHop();

    boolean isSecure();

    void setNotifyOnRetransmit(boolean z);
}
