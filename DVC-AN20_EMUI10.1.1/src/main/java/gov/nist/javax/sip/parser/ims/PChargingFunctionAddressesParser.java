package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.PChargingFunctionAddresses;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.ParametersParser;
import gov.nist.javax.sip.parser.TokenTypes;
import java.io.PrintStream;
import java.text.ParseException;

public class PChargingFunctionAddressesParser extends ParametersParser implements TokenTypes {
    public PChargingFunctionAddressesParser(String charging) {
        super(charging);
    }

    protected PChargingFunctionAddressesParser(Lexer lexer) {
        super(lexer);
    }

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("parse");
        }
        try {
            headerName(TokenTypes.P_CHARGING_FUNCTION_ADDRESSES);
            PChargingFunctionAddresses chargingFunctionAddresses = new PChargingFunctionAddresses();
            while (true) {
                try {
                    if (this.lexer.lookAhead(0) == '\n') {
                        break;
                    }
                    parseParameter(chargingFunctionAddresses);
                    this.lexer.SPorHT();
                    char la = this.lexer.lookAhead(0);
                    if (la == '\n') {
                        break;
                    } else if (la == 0) {
                        break;
                    } else {
                        this.lexer.match(59);
                        this.lexer.SPorHT();
                    }
                } catch (ParseException ex) {
                    throw ex;
                }
            }
            super.parse(chargingFunctionAddresses);
            return chargingFunctionAddresses;
        } finally {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void parseParameter(PChargingFunctionAddresses chargingFunctionAddresses) throws ParseException {
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
        String[] r = {"P-Charging-Function-Addresses: ccf=\"test str\"; ecf=token\n", "P-Charging-Function-Addresses: ccf=192.1.1.1; ccf=192.1.1.2; ecf=192.1.1.3; ecf=192.1.1.4\n", "P-Charging-Function-Addresses: ccf=[5555::b99:c88:d77:e66]; ccf=[5555::a55:b44:c33:d22]; ecf=[5555::1ff:2ee:3dd:4cc]; ecf=[5555::6aa:7bb:8cc:9dd]\n"};
        for (int i = 0; i < r.length; i++) {
            PChargingFunctionAddressesParser parser = new PChargingFunctionAddressesParser(r[i]);
            PrintStream printStream = System.out;
            printStream.println("original = " + r[i]);
            PrintStream printStream2 = System.out;
            printStream2.println("encoded = " + ((PChargingFunctionAddresses) parser.parse()).encode());
        }
    }
}
