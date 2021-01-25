package gov.nist.javax.sip.parser.extensions;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.extensions.Replaces;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.ParametersParser;
import gov.nist.javax.sip.parser.TokenTypes;
import java.io.PrintStream;
import java.text.ParseException;

public class ReplacesParser extends ParametersParser {
    public ReplacesParser(String callID) {
        super(callID);
    }

    protected ReplacesParser(Lexer lexer) {
        super(lexer);
    }

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("parse");
        }
        try {
            headerName(TokenTypes.REPLACES_TO);
            Replaces replaces = new Replaces();
            this.lexer.SPorHT();
            String callId = this.lexer.byteStringNoSemicolon();
            this.lexer.SPorHT();
            super.parse(replaces);
            replaces.setCallId(callId);
            return replaces;
        } finally {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        String[] to = {"Replaces: 12345th5z8z\n", "Replaces: 12345th5z8z;to-tag=tozght6-45;from-tag=fromzght789-337-2\n"};
        for (int i = 0; i < to.length; i++) {
            Replaces t = (Replaces) new ReplacesParser(to[i]).parse();
            PrintStream printStream = System.out;
            printStream.println("Parsing => " + to[i]);
            PrintStream printStream2 = System.out;
            printStream2.print("encoded = " + t.encode() + "==> ");
            PrintStream printStream3 = System.out;
            printStream3.println("callId " + t.getCallId() + " from-tag=" + t.getFromTag() + " to-tag=" + t.getToTag());
        }
    }
}
