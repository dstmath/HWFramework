package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.GenericURI;
import java.text.ParseException;

public class AddressParser extends Parser {
    public AddressParser(Lexer lexer) {
        this.lexer = lexer;
        this.lexer.selectLexer("charLexer");
    }

    public AddressParser(String address) {
        this.lexer = new Lexer("charLexer", address);
    }

    protected AddressImpl nameAddr() throws ParseException {
        if (debug) {
            dbg_enter("nameAddr");
        }
        try {
            GenericURI uri;
            AddressImpl retval;
            if (this.lexer.lookAhead(0) == '<') {
                this.lexer.consume(1);
                this.lexer.selectLexer("sip_urlLexer");
                this.lexer.SPorHT();
                uri = new URLParser((Lexer) this.lexer).uriReference(true);
                retval = new AddressImpl();
                retval.setAddressType(1);
                retval.setURI(uri);
                this.lexer.SPorHT();
                this.lexer.match(62);
                return retval;
            }
            String name;
            AddressImpl addr = new AddressImpl();
            addr.setAddressType(1);
            if (this.lexer.lookAhead(0) == '\"') {
                name = this.lexer.quotedString();
                this.lexer.SPorHT();
            } else {
                name = this.lexer.getNextToken('<');
            }
            addr.setDisplayName(name.trim());
            this.lexer.match(60);
            this.lexer.SPorHT();
            uri = new URLParser((Lexer) this.lexer).uriReference(true);
            retval = new AddressImpl();
            addr.setAddressType(1);
            addr.setURI(uri);
            this.lexer.SPorHT();
            this.lexer.match(62);
            if (debug) {
                dbg_leave("nameAddr");
            }
            return addr;
        } finally {
            if (debug) {
                dbg_leave("nameAddr");
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:27:0x0052  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public AddressImpl address(boolean inclParams) throws ParseException {
        char la;
        Throwable th;
        AddressImpl retval;
        if (debug) {
            dbg_enter("address");
        }
        int k = 0;
        while (this.lexer.hasMoreChars()) {
            try {
                la = this.lexer.lookAhead(k);
                if (la == '<' || la == '\"' || la == ':' || la == '/') {
                    break;
                } else if (la == 0) {
                    throw createParseException("unexpected EOL");
                } else {
                    k++;
                }
            } catch (Throwable th2) {
                th = th2;
                if (debug) {
                }
                throw th;
            }
        }
        la = this.lexer.lookAhead(k);
        if (la == '<' || la == '\"') {
            retval = nameAddr();
        } else if (la == ':' || la == '/') {
            AddressImpl retval2 = new AddressImpl();
            try {
                GenericURI uri = new URLParser((Lexer) this.lexer).uriReference(inclParams);
                retval2.setAddressType(2);
                retval2.setURI(uri);
                retval = retval2;
            } catch (Throwable th3) {
                th = th3;
                if (debug) {
                    dbg_leave("address");
                }
                throw th;
            }
        } else {
            throw createParseException("Bad address spec");
        }
        if (debug) {
            dbg_leave("address");
        }
        return retval;
    }
}
