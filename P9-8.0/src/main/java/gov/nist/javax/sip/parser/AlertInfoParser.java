package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.AlertInfo;
import gov.nist.javax.sip.header.AlertInfoList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class AlertInfoParser extends ParametersParser {
    public AlertInfoParser(String alertInfo) {
        super(alertInfo);
    }

    protected AlertInfoParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("AlertInfoParser.parse");
        }
        AlertInfoList list = new AlertInfoList();
        try {
            headerName(TokenTypes.ALERT_INFO);
            while (this.lexer.lookAhead(0) != 10) {
                AlertInfo alertInfo = new AlertInfo();
                alertInfo.setHeaderName("Alert-Info");
                while (true) {
                    this.lexer.SPorHT();
                    if (this.lexer.lookAhead(0) == '<') {
                        this.lexer.match(60);
                        alertInfo.setAlertInfo(new URLParser((Lexer) this.lexer).uriReference(true));
                        this.lexer.match(62);
                    } else {
                        alertInfo.setAlertInfo(this.lexer.byteStringNoSemicolon());
                    }
                    this.lexer.SPorHT();
                    super.parse(alertInfo);
                    list.add((SIPHeader) alertInfo);
                    if (this.lexer.lookAhead(0) == ',') {
                        this.lexer.match(44);
                    }
                }
            }
            return list;
        } finally {
            if (debug) {
                dbg_leave("AlertInfoParser.parse");
            }
        }
    }
}
