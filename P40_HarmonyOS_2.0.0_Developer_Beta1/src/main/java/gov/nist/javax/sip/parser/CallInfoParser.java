package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.CallInfo;
import gov.nist.javax.sip.header.CallInfoList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class CallInfoParser extends ParametersParser {
    public CallInfoParser(String callInfo) {
        super(callInfo);
    }

    protected CallInfoParser(Lexer lexer) {
        super(lexer);
    }

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("CallInfoParser.parse");
        }
        CallInfoList list = new CallInfoList();
        try {
            headerName(TokenTypes.CALL_INFO);
            while (this.lexer.lookAhead(0) != '\n') {
                CallInfo callInfo = new CallInfo();
                callInfo.setHeaderName("Call-Info");
                this.lexer.SPorHT();
                this.lexer.match(60);
                callInfo.setInfo(new URLParser((Lexer) this.lexer).uriReference(true));
                this.lexer.match(62);
                this.lexer.SPorHT();
                super.parse(callInfo);
                list.add((CallInfoList) callInfo);
                while (this.lexer.lookAhead(0) == ',') {
                    this.lexer.match(44);
                    this.lexer.SPorHT();
                    CallInfo callInfo2 = new CallInfo();
                    this.lexer.SPorHT();
                    this.lexer.match(60);
                    callInfo2.setInfo(new URLParser((Lexer) this.lexer).uriReference(true));
                    this.lexer.match(62);
                    this.lexer.SPorHT();
                    super.parse(callInfo2);
                    list.add((CallInfoList) callInfo2);
                }
            }
            return list;
        } finally {
            if (debug) {
                dbg_leave("CallInfoParser.parse");
            }
        }
    }
}
