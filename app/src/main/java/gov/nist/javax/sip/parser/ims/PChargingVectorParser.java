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
        try {
            headerName(TokenTypes.P_VECTOR_CHARGING);
            PChargingVector chargingVector = new PChargingVector();
            while (this.lexer.lookAhead(0) != '\n') {
                parseParameter(chargingVector);
                this.lexer.SPorHT();
                char la = this.lexer.lookAhead(0);
                if (!(la == '\n' || la == '\u0000')) {
                    this.lexer.match(59);
                    this.lexer.SPorHT();
                }
            }
            break;
            super.parse(chargingVector);
            if (chargingVector.getParameter(ParameterNamesIms.ICID_VALUE) == null) {
                throw new ParseException("Missing a required Parameter : icid-value", 0);
            }
            if (debug) {
                dbg_leave("parse");
            }
            return chargingVector;
        } catch (ParseException ex) {
            throw ex;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }

    protected void parseParameter(PChargingVector chargingVector) throws ParseException {
        if (debug) {
            dbg_enter("parseParameter");
        }
        try {
            chargingVector.setParameter(nameValue('='));
            if (debug) {
                dbg_leave("parseParameter");
            }
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("parseParameter");
            }
        }
    }
}
