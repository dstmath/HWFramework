package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.Privacy;
import gov.nist.javax.sip.header.ims.PrivacyList;
import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

public class PrivacyParser extends HeaderParser implements TokenTypes {
    public PrivacyParser(String privacyType) {
        super(privacyType);
    }

    protected PrivacyParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
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
                privacyList.add((SIPHeader) privacy);
                while (this.lexer.lookAhead(0) == ';') {
                    this.lexer.match(59);
                    this.lexer.SPorHT();
                    privacy = new Privacy();
                    this.lexer.match(4095);
                    privacy.setPrivacy(this.lexer.getNextToken().getTokenValue());
                    this.lexer.SPorHT();
                    privacyList.add((SIPHeader) privacy);
                }
            }
            return privacyList;
        } finally {
            if (debug) {
                dbg_leave("PrivacyParser.parse");
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        String[] rou = new String[]{"Privacy: none\n", "Privacy: none;id;user\n"};
        for (String privacyParser : rou) {
            System.out.println("encoded = " + ((PrivacyList) new PrivacyParser(privacyParser).parse()).encode());
        }
    }
}
