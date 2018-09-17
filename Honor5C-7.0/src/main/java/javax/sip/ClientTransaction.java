package javax.sip;

import javax.sip.address.Hop;
import javax.sip.message.Request;

public interface ClientTransaction extends Transaction {
    void alertIfStillInCallingStateBy(int i);

    Request createAck() throws SipException;

    Request createCancel() throws SipException;

    Hop getNextHop();

    void sendRequest() throws SipException;

    void setNotifyOnRetransmit(boolean z);
}
