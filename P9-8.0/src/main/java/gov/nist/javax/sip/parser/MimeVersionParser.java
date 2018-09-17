package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.MimeVersion;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;

public class MimeVersionParser extends HeaderParser {
    public MimeVersionParser(String mimeVersion) {
        super(mimeVersion);
    }

    protected MimeVersionParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("MimeVersionParser.parse");
        }
        MimeVersion mimeVersion = new MimeVersion();
        try {
            headerName(TokenTypes.MIME_VERSION);
            mimeVersion.setHeaderName("MIME-Version");
            mimeVersion.setMajorVersion(Integer.parseInt(this.lexer.number()));
            this.lexer.match(46);
            mimeVersion.setMinorVersion(Integer.parseInt(this.lexer.number()));
            this.lexer.SPorHT();
            this.lexer.match(10);
            if (debug) {
                dbg_leave("MimeVersionParser.parse");
            }
            return mimeVersion;
        } catch (InvalidArgumentException ex) {
            throw createParseException(ex.getMessage());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("MimeVersionParser.parse");
            }
        }
    }
}
