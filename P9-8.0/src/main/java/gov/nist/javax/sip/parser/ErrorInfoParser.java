package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.ErrorInfo;
import gov.nist.javax.sip.header.ErrorInfoList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class ErrorInfoParser extends ParametersParser {
    public ErrorInfoParser(String errorInfo) {
        super(errorInfo);
    }

    protected ErrorInfoParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("ErrorInfoParser.parse");
        }
        ErrorInfoList list = new ErrorInfoList();
        try {
            headerName(TokenTypes.ERROR_INFO);
            while (this.lexer.lookAhead(0) != 10) {
                while (true) {
                    ErrorInfo errorInfo = new ErrorInfo();
                    errorInfo.setHeaderName("Error-Info");
                    this.lexer.SPorHT();
                    this.lexer.match(60);
                    errorInfo.setErrorInfo(new URLParser((Lexer) this.lexer).uriReference(true));
                    this.lexer.match(62);
                    this.lexer.SPorHT();
                    super.parse(errorInfo);
                    list.add((SIPHeader) errorInfo);
                    if (this.lexer.lookAhead(0) == ',') {
                        this.lexer.match(44);
                    }
                }
            }
            return list;
        } finally {
            if (debug) {
                dbg_leave("ErrorInfoParser.parse");
            }
        }
    }
}
