package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.Server;
import java.text.ParseException;

public class ServerParser extends HeaderParser {
    public ServerParser(String server) {
        super(server);
    }

    protected ServerParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("ServerParser.parse");
        }
        Server server = new Server();
        try {
            headerName(TokenTypes.SERVER);
            if (this.lexer.lookAhead(0) == 10) {
                throw createParseException("empty header");
            }
            while (this.lexer.lookAhead(0) != 10 && this.lexer.lookAhead(0) != 0) {
                if (this.lexer.lookAhead(0) == '(') {
                    server.addProductToken('(' + this.lexer.comment() + ')');
                } else {
                    int marker = this.lexer.markInputPosition();
                    String tok = this.lexer.getString('/');
                    if (tok.charAt(tok.length() - 1) == 10) {
                        tok = tok.trim();
                    }
                    server.addProductToken(tok);
                }
            }
            if (debug) {
                dbg_leave("ServerParser.parse");
            }
            return server;
        } catch (ParseException e) {
            this.lexer.rewindInputPosition(0);
            server.addProductToken(this.lexer.getRest().trim());
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("ServerParser.parse");
            }
        }
    }
}
