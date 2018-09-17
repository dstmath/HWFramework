package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.PPreferredService;
import gov.nist.javax.sip.header.ims.ParameterNamesIms;
import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class PPreferredServiceParser extends HeaderParser implements TokenTypes {
    protected PPreferredServiceParser(Lexer lexer) {
        super(lexer);
    }

    public PPreferredServiceParser(String pps) {
        super(pps);
    }

    public SIPHeader parse() throws ParseException {
        PPreferredService pps;
        if (debug) {
            dbg_enter("PPreferredServiceParser.parse");
        }
        try {
            this.lexer.match(TokenTypes.P_PREFERRED_SERVICE);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            pps = new PPreferredService();
            String urn = this.lexer.getBuffer();
            if (urn.contains(ParameterNamesIms.SERVICE_ID)) {
                if (urn.contains(ParameterNamesIms.SERVICE_ID_LABEL)) {
                    String serviceID = urn.split("3gpp-service.")[1];
                    if (serviceID.trim().equals("")) {
                        throw new InvalidArgumentException("URN should atleast have one sub-service");
                    }
                    pps.setSubserviceIdentifiers(serviceID);
                } else if (urn.contains(ParameterNamesIms.APPLICATION_ID_LABEL)) {
                    String appID = urn.split(ParameterNamesIms.APPLICATION_ID_LABEL)[1];
                    if (appID.trim().equals("")) {
                        try {
                            throw new InvalidArgumentException("URN should atleast have one sub-application");
                        } catch (InvalidArgumentException e) {
                            e.printStackTrace();
                        }
                    } else {
                        pps.setApplicationIdentifiers(appID);
                    }
                } else {
                    try {
                        throw new InvalidArgumentException("URN is not well formed");
                    } catch (InvalidArgumentException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        } catch (InvalidArgumentException e22) {
            e22.printStackTrace();
        } catch (Throwable th) {
            if (debug) {
                dbg_enter("PPreferredServiceParser.parse");
            }
        }
        super.parse();
        if (debug) {
            dbg_enter("PPreferredServiceParser.parse");
        }
        return pps;
    }
}
