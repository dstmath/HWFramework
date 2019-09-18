package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.PChargingVector;
import gov.nist.javax.sip.header.ims.ParameterNamesIms;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.ParametersParser;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

public class PChargingVectorParser extends ParametersParser implements TokenTypes {
    public PChargingVectorParser(String chargingVector) {
        super(chargingVector);
    }

    protected PChargingVectorParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("parse");
        }
        headerName(TokenTypes.P_VECTOR_CHARGING);
        PChargingVector chargingVector = new PChargingVector();
        while (true) {
            try {
                if (this.lexer.lookAhead(0) != 10) {
                    parseParameter(chargingVector);
                    this.lexer.SPorHT();
                    char la = this.lexer.lookAhead(0);
                    if (la != 10) {
                        if (la == 0) {
                            break;
                        }
                        this.lexer.match(59);
                        this.lexer.SPorHT();
                    }
                }
            } catch (ParseException ex) {
                throw ex;
            } catch (Throwable th) {
                if (debug) {
                    dbg_leave("parse");
                }
                throw th;
            }
        }
        super.parse(chargingVector);
        if (chargingVector.getParameter(ParameterNamesIms.ICID_VALUE) != null) {
            if (debug) {
                dbg_leave("parse");
            }
            return chargingVector;
        }
        throw new ParseException("Missing a required Parameter : icid-value", 0);
    }

    /* access modifiers changed from: protected */
    public void parseParameter(PChargingVector chargingVector) throws ParseException {
        String str;
        if (debug) {
            dbg_enter("parseParameter");
        }
        try {
            chargingVector.setParameter(nameValue('='));
        } finally {
            if (debug) {
                str = "parseParameter";
                dbg_leave(str);
            }
        }
    }
}
