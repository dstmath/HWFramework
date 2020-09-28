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
import java.text.ParseException;

public class SecurityAgreeParser extends HeaderParser {
    public SecurityAgreeParser(String security) {
        super(security);
    }

    protected SecurityAgreeParser(Lexer lexer) {
        super(lexer);
    }

    /* access modifiers changed from: protected */
    public void parseParameter(SecurityAgree header) throws ParseException {
        if (debug) {
            dbg_enter("parseParameter");
        }
        try {
            header.setParameter(nameValue('='));
        } finally {
            if (debug) {
                dbg_leave("parseParameter");
            }
        }
    }

    public SIPHeaderList parse(SecurityAgree header) throws ParseException {
        SIPHeaderList list;
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
        this.lexer.match(4095);
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
        while (true) {
            try {
                if (this.lexer.lookAhead(0) == '\n') {
                    break;
                }
                parseParameter(header);
                this.lexer.SPorHT();
                char laInLoop = this.lexer.lookAhead(0);
                if (laInLoop == '\n') {
                    break;
                } else if (laInLoop == 0) {
                    break;
                } else {
                    if (laInLoop == ',') {
                        list.add((SIPHeader) header);
                        if (header.getClass().isInstance(new SecurityClient())) {
                            header = new SecurityClient();
                        } else if (header.getClass().isInstance(new SecurityServer())) {
                            header = new SecurityServer();
                        } else if (header.getClass().isInstance(new SecurityVerify())) {
                            header = new SecurityVerify();
                        }
                        this.lexer.match(44);
                        this.lexer.SPorHT();
                        this.lexer.match(4095);
                        header.setSecurityMechanism(this.lexer.getNextToken().getTokenValue());
                    }
                    this.lexer.SPorHT();
                    if (this.lexer.lookAhead(0) == ';') {
                        this.lexer.match(59);
                    }
                    this.lexer.SPorHT();
                }
            } catch (ParseException ex) {
                throw ex;
            }
        }
        list.add((SIPHeader) header);
        return list;
    }
}
