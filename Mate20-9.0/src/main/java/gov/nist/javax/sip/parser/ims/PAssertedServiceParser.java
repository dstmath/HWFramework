package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.PAssertedService;
import gov.nist.javax.sip.header.ims.ParameterNamesIms;
import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class PAssertedServiceParser extends HeaderParser implements TokenTypes {
    protected PAssertedServiceParser(Lexer lexer) {
        super(lexer);
    }

    public PAssertedServiceParser(String pas) {
        super(pas);
    }

    public SIPHeader parse() throws ParseException {
        PAssertedService pps;
        if (debug) {
            dbg_enter("PAssertedServiceParser.parse");
        }
        try {
            this.lexer.match(TokenTypes.P_ASSERTED_SERVICE);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            pps = new PAssertedService();
            String urn = this.lexer.getBuffer();
            if (urn.contains(ParameterNamesIms.SERVICE_ID)) {
                if (urn.contains(ParameterNamesIms.SERVICE_ID_LABEL)) {
                    if (!urn.split("3gpp-service.")[1].trim().equals("")) {
                        pps.setSubserviceIdentifiers(urn.split(ParameterNamesIms.SERVICE_ID_LABEL)[1]);
                    } else {
                        throw new InvalidArgumentException("URN should atleast have one sub-service");
                    }
                } else if (!urn.contains(ParameterNamesIms.APPLICATION_ID_LABEL)) {
                    try {
                        throw new InvalidArgumentException("URN is not well formed");
                    } catch (InvalidArgumentException e) {
                        e.printStackTrace();
                    }
                } else if (!urn.split("3gpp-application.")[1].trim().equals("")) {
                    pps.setApplicationIdentifiers(urn.split(ParameterNamesIms.APPLICATION_ID_LABEL)[1]);
                } else {
                    try {
                        throw new InvalidArgumentException("URN should atleast have one sub-application");
                    } catch (InvalidArgumentException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        } catch (InvalidArgumentException e3) {
            e3.printStackTrace();
        } catch (Throwable th) {
            if (debug) {
                dbg_enter("PAssertedServiceParser.parse");
            }
            throw th;
        }
        super.parse();
        if (debug) {
            dbg_enter("PAssertedServiceParser.parse");
        }
        return pps;
    }
}
