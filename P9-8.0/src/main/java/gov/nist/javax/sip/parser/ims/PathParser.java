package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.Path;
import gov.nist.javax.sip.header.ims.PathList;
import gov.nist.javax.sip.parser.AddressParametersParser;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

public class PathParser extends AddressParametersParser implements TokenTypes {
    public PathParser(String path) {
        super(path);
    }

    protected PathParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        PathList pathList = new PathList();
        if (debug) {
            dbg_enter("PathParser.parse");
        }
        try {
            char la;
            this.lexer.match(TokenTypes.PATH);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            while (true) {
                Path path = new Path();
                super.parse(path);
                pathList.add((SIPHeader) path);
                this.lexer.SPorHT();
                la = this.lexer.lookAhead(0);
                if (la != ',') {
                    break;
                }
                this.lexer.match(44);
                this.lexer.SPorHT();
            }
            if (la == 10) {
                return pathList;
            }
            throw createParseException("unexpected char");
        } finally {
            if (debug) {
                dbg_leave("PathParser.parse");
            }
        }
    }
}
