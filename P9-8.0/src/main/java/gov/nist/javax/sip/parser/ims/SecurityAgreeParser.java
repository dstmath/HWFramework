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

    protected void parseParameter(SecurityAgree header) throws ParseException {
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
        this.lexer.match(4095);
        header.setSecurityMechanism(this.lexer.getNextToken().getTokenValue());
        this.lexer.SPorHT();
        char la = this.lexer.lookAhead(0);
        if (la == 10) {
            list.add((SIPHeader) header);
            return list;
        }
        SIPHeader header2;
        if (la == ';') {
            this.lexer.match(59);
        }
        this.lexer.SPorHT();
        while (true) {
            SIPHeader header3;
            try {
                header2 = header3;
                if (this.lexer.lookAhead(0) == 10) {
                    break;
                }
                parseParameter(header2);
                this.lexer.SPorHT();
                char laInLoop = this.lexer.lookAhead(0);
                if (laInLoop == 10 || laInLoop == 0) {
                    break;
                }
                if (laInLoop == ',') {
                    list.add(header2);
                    if (header2.getClass().isInstance(new SecurityClient())) {
                        header3 = new SecurityClient();
                    } else if (header2.getClass().isInstance(new SecurityServer())) {
                        header3 = new SecurityServer();
                    } else if (header2.getClass().isInstance(new SecurityVerify())) {
                        header3 = new SecurityVerify();
                    } else {
                        header3 = header2;
                    }
                    try {
                        this.lexer.match(44);
                        this.lexer.SPorHT();
                        this.lexer.match(4095);
                        header3.setSecurityMechanism(this.lexer.getNextToken().getTokenValue());
                    } catch (ParseException e) {
                        ex = e;
                        throw ex;
                    }
                }
                header3 = header2;
                this.lexer.SPorHT();
                if (this.lexer.lookAhead(0) == ';') {
                    this.lexer.match(59);
                }
                this.lexer.SPorHT();
            } catch (ParseException e2) {
                ex = e2;
                header3 = header2;
                throw ex;
            }
        }
        list.add(header2);
        return list;
    }
}
