package gov.nist.javax.sip.stack;

import gov.nist.javax.sip.message.SIPMessage;

public interface RawMessageChannel {
    void processMessage(SIPMessage sIPMessage) throws Exception;
}
