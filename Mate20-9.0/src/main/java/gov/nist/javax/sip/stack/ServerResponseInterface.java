package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.message.SIPResponse;

public interface ServerResponseInterface {
    void processResponse(SIPResponse sIPResponse, MessageChannel messageChannel);

    void processResponse(SIPResponse sIPResponse, MessageChannel messageChannel, SIPDialog sIPDialog);
}
