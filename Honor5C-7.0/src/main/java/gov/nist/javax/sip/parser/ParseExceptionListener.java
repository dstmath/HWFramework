package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.message.SIPMessage;
import java.text.ParseException;

public interface ParseExceptionListener {
    void handleException(ParseException parseException, SIPMessage sIPMessage, Class cls, String str, String str2) throws ParseException;
}
