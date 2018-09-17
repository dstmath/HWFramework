package javax.sip;

import javax.sip.message.Response;

public interface ServerTransaction extends Transaction {
    void enableRetransmissionAlerts() throws SipException;

    ServerTransaction getCanceledInviteTransaction();

    void sendResponse(Response response) throws SipException, InvalidArgumentException;
}
