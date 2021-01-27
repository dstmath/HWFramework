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

    /* access modifiers changed from: protected */
    public AddressImpl nameAddr() throws ParseException {
        String name;
        if (debug) {
            dbg_enter("nameAddr");
        }
        try {
            if (this.lexer.lookAhead(0) == '<') {
                this.lexer.consume(1);
                this.lexer.selectLexer("sip_urlLexer");
                this.lexer.SPorHT();
                GenericURI uri = new URLParser((Lexer) this.lexer).uriReference(true);
                AddressImpl retval = new AddressImpl();
                retval.setAddressType(1);
                retval.setURI(uri);
                this.lexer.SPorHT();
                this.lexer.match(62);
                return retval;
            }
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
            GenericURI uri2 = new URLParser((Lexer) this.lexer).uriReference(true);
            new AddressImpl();
            addr.setAddressType(1);
            addr.setURI(uri2);
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

    /* JADX WARNING: Removed duplicated region for block: B:31:0x0073 A[DONT_GENERATE] */
    public AddressImpl address(boolean inclParams) throws ParseException {
        AddressImpl retval;
        char la;
        if (debug) {
            dbg_enter("address");
        }
        int k = 0;
        while (true) {
            try {
                if (!this.lexer.hasMoreChars() || (la = this.lexer.lookAhead(k)) == '<' || la == '\"' || la == ':') {
                    break;
                } else if (la == '/') {
                    break;
                } else if (la != 0) {
                    k++;
                } else {
                    throw createParseException("unexpected EOL");
                }
            } finally {
                if (debug) {
                    dbg_leave("address");
                }
            }
        }
        char la2 = this.lexer.lookAhead(k);
        if (la2 != '<') {
            if (la2 != '\"') {
                if (la2 != ':') {
                    if (la2 != '/') {
                        throw createParseException("Bad address spec");
                    }
                }
                retval = new AddressImpl();
                GenericURI uri = new URLParser((Lexer) this.lexer).uriReference(inclParams);
                retval.setAddressType(2);
                retval.setURI(uri);
                return retval;
            }
        }
        retval = nameAddr();
        return retval;
    }
}
