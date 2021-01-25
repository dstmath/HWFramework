package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.AddressParametersHeader;
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

    @Override // gov.nist.javax.sip.parser.HeaderParser
    public SIPHeader parse() throws ParseException {
        char la;
        PathList pathList = new PathList();
        if (debug) {
            dbg_enter("PathParser.parse");
        }
        try {
            this.lexer.match(TokenTypes.PATH);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            while (true) {
                Path path = new Path();
                super.parse((AddressParametersHeader) path);
                pathList.add((PathList) path);
                this.lexer.SPorHT();
                la = this.lexer.lookAhead(0);
                if (la != ',') {
                    break;
                }
                this.lexer.match(44);
                this.lexer.SPorHT();
            }
            if (la == '\n') {
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
