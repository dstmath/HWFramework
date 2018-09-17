package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.PUserDatabase;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.ParametersParser;
import gov.nist.javax.sip.parser.TokenTypes;
import java.text.ParseException;

public class PUserDatabaseParser extends ParametersParser implements TokenTypes {
    public PUserDatabaseParser(String databaseName) {
        super(databaseName);
    }

    public PUserDatabaseParser(Lexer lexer) {
        super(lexer);
    }

    public SIPHeader parse() throws ParseException {
        if (debug) {
            dbg_enter("PUserDatabase.parse");
        }
        try {
            this.lexer.match(TokenTypes.P_USER_DATABASE);
            this.lexer.SPorHT();
            this.lexer.match(58);
            this.lexer.SPorHT();
            PUserDatabase userDatabase = new PUserDatabase();
            parseheader(userDatabase);
            return userDatabase;
        } finally {
            if (debug) {
                dbg_leave("PUserDatabase.parse");
            }
        }
    }

    private void parseheader(PUserDatabase userDatabase) throws ParseException {
        StringBuffer dbname = new StringBuffer();
        this.lexer.match(60);
        while (this.lexer.hasMoreChars()) {
            char next = this.lexer.getNextChar();
            if (!(next == '>' || next == 10)) {
                dbname.append(next);
            }
        }
        userDatabase.setDatabaseName(dbname.toString());
        super.parse(userDatabase);
    }
}
