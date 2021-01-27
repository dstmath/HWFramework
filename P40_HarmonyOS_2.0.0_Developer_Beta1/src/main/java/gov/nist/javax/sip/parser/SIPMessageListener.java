package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.message.SIPMessage;

public interface SIPMessageListener extends ParseExceptionListener {
    @Override // gov.nist.javax.sip.stack.RawMessageChannel
    void processMessage(SIPMessage sIPMessage) throws Exception;
}
