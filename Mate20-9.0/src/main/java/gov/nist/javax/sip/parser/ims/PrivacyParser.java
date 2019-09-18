package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.Privacy;
import gov.nist.javax.sip.header.ims.PrivacyList;
import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.io.PrintStream;
import java.text.ParseException;

public class PrivacyParser extends HeaderParser implements TokenTypes {
    public PrivacyParser(String privacyType) {
        super(privacyType);
    }

    protected PrivacyParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        String str;
        if (debug) {
            dbg_enter("PrivacyParser.parse");
        }
        PrivacyList privacyList = new PrivacyList();
        try {
            headerName(TokenTypes.PRIVACY);
            while (this.lexer.lookAhead(0) != 10) {
                this.lexer.SPorHT();
                Privacy privacy = new Privacy();
                privacy.setHeaderName("Privacy");
                this.lexer.match(4095);
                privacy.setPrivacy(this.lexer.getNextToken().getTokenValue());
                this.lexer.SPorHT();
                privacyList.add(privacy);
                while (this.lexer.lookAhead(0) == ';') {
                    this.lexer.match(59);
                    this.lexer.SPorHT();
                    Privacy privacy2 = new Privacy();
                    this.lexer.match(4095);
                    privacy2.setPrivacy(this.lexer.getNextToken().getTokenValue());
                    this.lexer.SPorHT();
                    privacyList.add(privacy2);
                }
            }
            return privacyList;
        } finally {
            if (debug) {
                str = "PrivacyParser.parse";
                dbg_leave(str);
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        String[] rou = {"Privacy: none\n", "Privacy: none;id;user\n"};
        for (int i = 0; i < rou.length; i++) {
            PrintStream printStream = System.out;
            printStream.println("encoded = " + ((PrivacyList) new PrivacyParser(rou[i]).parse()).encode());
        }
    }
}
