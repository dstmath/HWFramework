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

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("PPreferredServiceParser.parse");
        }
        try {
            this.lexer.match(TokenTypes.P_PREFERRED_SERVICE);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            PPreferredService pps = new PPreferredService();
            String urn = this.lexer.getBuffer();
            if (urn.contains(ParameterNamesIms.SERVICE_ID)) {
                if (urn.contains(ParameterNamesIms.SERVICE_ID_LABEL)) {
                    String serviceID = urn.split("3gpp-service.")[1];
                    if (!serviceID.trim().equals("")) {
                        pps.setSubserviceIdentifiers(serviceID);
                    } else {
                        try {
                            throw new InvalidArgumentException("URN should atleast have one sub-service");
                        } catch (InvalidArgumentException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (urn.contains(ParameterNamesIms.APPLICATION_ID_LABEL)) {
                    String appID = urn.split(ParameterNamesIms.APPLICATION_ID_LABEL)[1];
                    if (!appID.trim().equals("")) {
                        pps.setApplicationIdentifiers(appID);
                    } else {
                        try {
                            throw new InvalidArgumentException("URN should atleast have one sub-application");
                        } catch (InvalidArgumentException e2) {
                            e2.printStackTrace();
                        }
                    }
                } else {
                    try {
                        throw new InvalidArgumentException("URN is not well formed");
                    } catch (InvalidArgumentException e3) {
                        e3.printStackTrace();
                    }
                }
            }
            super.parse();
            return pps;
        } finally {
            if (debug) {
                dbg_enter("PPreferredServiceParser.parse");
            }
        }
    }
}
