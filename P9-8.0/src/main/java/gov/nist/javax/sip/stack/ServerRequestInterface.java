package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.message.SIPRequest;

public interface ServerRequestInterface {
    void processRequest(SIPRequest sIPRequest, MessageChannel messageChannel);
}
