package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.SIPHeaderList;
import gov.nist.javax.sip.header.ims.SecurityAgree;
import gov.nist.javax.sip.header.ims.SecurityClient;
import gov.nist.javax.sip.header.ims.SecurityClientList;
import gov.nist.javax.sip.header.ims.SecurityServer;
import gov.nist.javax.sip.header.ims.SecurityServerList;
import gov.nist.javax.sip.header.ims.SecurityVerify;
import gov.nist.javax.sip.header.ims.SecurityVerifyList;
import gov.nist.javax.sip.parser.HeaderParser;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

public class SecurityAgreeParser extends HeaderParser {
    public SecurityAgreeParser(String security) {
        super(security);
    }

    protected SecurityAgreeParser(Lexer lexer) {
        super(lexer);
    }

    protected void parseParameter(SecurityAgree header) throws ParseException {
        if (debug) {
            dbg_enter("parseParameter");
        }
        try {
            header.setParameter(nameValue('='));
            if (debug) {
                dbg_leave("parseParameter");
            }
        } catch (Throwable th) {
            if (debug) {
                dbg_leave("parseParameter");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public SIPHeaderList parse(SecurityAgree header) throws ParseException {
        SIPHeaderList list;
        ParseException ex;
        if (header.getClass().isInstance(new SecurityClient())) {
            list = new SecurityClientList();
        } else if (header.getClass().isInstance(new SecurityServer())) {
            list = new SecurityServerList();
        } else if (!header.getClass().isInstance(new SecurityVerify())) {
            return null;
        } else {
            list = new SecurityVerifyList();
        }
        this.lexer.SPorHT();
        this.lexer.match(TokenTypes.ID);
        header.setSecurityMechanism(this.lexer.getNextToken().getTokenValue());
        this.lexer.SPorHT();
        char la = this.lexer.lookAhead(0);
        if (la == '\n') {
            list.add((SIPHeader) header);
            return list;
        }
        if (la == ';') {
            this.lexer.match(59);
        }
        this.lexer.SPorHT();
        SIPHeader header2 = header;
        while (this.lexer.lookAhead(0) != '\n') {
            parseParameter(header2);
            this.lexer.SPorHT();
            char laInLoop = this.lexer.lookAhead(0);
            if (!(laInLoop == '\n' || laInLoop == '\u0000')) {
                SIPHeader header3;
                if (laInLoop == ',') {
                    list.add(header2);
                    if (header2.getClass().isInstance(new SecurityClient())) {
                        header3 = new SecurityClient();
                    } else {
                        try {
                            if (header2.getClass().isInstance(new SecurityServer())) {
                                header3 = new SecurityServer();
                            } else if (header2.getClass().isInstance(new SecurityVerify())) {
                                header3 = new SecurityVerify();
                            } else {
                                header3 = header2;
                            }
                        } catch (ParseException e) {
                            ex = e;
                            header3 = header2;
                        }
                    }
                    try {
                        this.lexer.match(44);
                        this.lexer.SPorHT();
                        this.lexer.match(TokenTypes.ID);
                        header3.setSecurityMechanism(this.lexer.getNextToken().getTokenValue());
                    } catch (ParseException e2) {
                        ex = e2;
                    }
                } else {
                    header3 = header2;
                }
                this.lexer.SPorHT();
                if (this.lexer.lookAhead(0) == ';') {
                    this.lexer.match(59);
                }
                this.lexer.SPorHT();
                header2 = header3;
            }
        }
        list.add(header2);
        return list;
    }
}
