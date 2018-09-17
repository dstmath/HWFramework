package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.PChargingFunctionAddresses;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.ParametersParser;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

public class PChargingFunctionAddressesParser extends ParametersParser implements TokenTypes {
    public PChargingFunctionAddressesParser(String charging) {
        super(charging);
    }

    protected PChargingFunctionAddressesParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("parse");
        }
        try {
            headerName(TokenTypes.P_CHARGING_FUNCTION_ADDRESSES);
            PChargingFunctionAddresses chargingFunctionAddresses = new PChargingFunctionAddresses();
            while (this.lexer.lookAhead(0) != 10) {
                parseParameter(chargingFunctionAddresses);
                this.lexer.SPorHT();
                char la = this.lexer.lookAhead(0);
                if (la != 10 && la != 0) {
                    this.lexer.match(59);
                    this.lexer.SPorHT();
                }
            }
            super.parse(chargingFunctionAddresses);
            if (debug) {
                dbg_leave("parse");
            }
            return chargingFunctionAddresses;
        } catch (ParseException ex) {
            throw ex;
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }

    protected void parseParameter(PChargingFunctionAddresses chargingFunctionAddresses) throws ParseException {
        if (debug) {
            dbg_enter("parseParameter");
        }
        try {
            chargingFunctionAddresses.setMultiParameter(nameValue('='));
        } finally {
            if (debug) {
                dbg_leave("parseParameter");
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        String[] r = new String[]{"P-Charging-Function-Addresses: ccf=\"test str\"; ecf=token\n", "P-Charging-Function-Addresses: ccf=192.1.1.1; ccf=192.1.1.2; ecf=192.1.1.3; ecf=192.1.1.4\n", "P-Charging-Function-Addresses: ccf=[5555::b99:c88:d77:e66]; ccf=[5555::a55:b44:c33:d22]; ecf=[5555::1ff:2ee:3dd:4cc]; ecf=[5555::6aa:7bb:8cc:9dd]\n"};
        for (int i = 0; i < r.length; i++) {
            PChargingFunctionAddressesParser parser = new PChargingFunctionAddressesParser(r[i]);
            System.out.println("original = " + r[i]);
            System.out.println("encoded = " + ((PChargingFunctionAddresses) parser.parse()).encode());
        }
    }
}
