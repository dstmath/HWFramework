package gov.nist.javax.sip.parser.extensions;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.extensions.MinSE;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.ParametersParser;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class MinSEParser extends ParametersParser {
    public MinSEParser(String text) {
        super(text);
    }

    protected MinSEParser(Lexer lexer) {
        super(lexer);
    }

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        MinSE minse = new MinSE();
        if (debug) {
            dbg_enter("parse");
        }
        try {
            headerName(TokenTypes.MINSE_TO);
            try {
                minse.setExpires(Integer.parseInt(this.lexer.getNextId()));
                this.lexer.SPorHT();
                super.parse(minse);
                return minse;
            } catch (NumberFormatException e) {
                throw createParseException("bad integer format");
            } catch (InvalidArgumentException ex) {
                throw createParseException(ex.getMessage());
            }
        } finally {
            if (debug) {
                dbg_leave("parse");
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        String[] to;
        for (String str : new String[]{"Min-SE: 30\n", "Min-SE: 45;some-param=somevalue\n"}) {
            MinSE t = (MinSE) new MinSEParser(str).parse();
            System.out.println("encoded = " + t.encode());
            System.out.println("\ntime=" + t.getExpires());
            if (t.getParameter("some-param") != null) {
                System.out.println("some-param=" + t.getParameter("some-param"));
            }
        }
    }
}
