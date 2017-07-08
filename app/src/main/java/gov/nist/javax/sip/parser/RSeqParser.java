package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.RSeq;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.RSeqHeader;

public class RSeqParser extends HeaderParser {
    public RSeqParser(String rseq) {
        super(rseq);
    }

    protected RSeqParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("RSeqParser.parse");
        }
        RSeq rseq = new RSeq();
        try {
            headerName(TokenTypes.RSEQ);
            rseq.setHeaderName(RSeqHeader.NAME);
            rseq.setSeqNumber(Long.parseLong(this.lexer.number()));
            this.lexer.SPorHT();
            this.lexer.match(10);
            if (debug) {
                dbg_leave("RSeqParser.parse");
            }
            return rseq;
        } catch (InvalidArgumentException ex) {
            throw createParseException(ex.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("RSeqParser.parse");
            }
        }
    }
}
