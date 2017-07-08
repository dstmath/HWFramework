package gov.nist.javax.sip.parser;

import gov.nist.core.NameValue;
import gov.nist.javax.sip.header.ParametersHeader;
import java.text.ParseException;

public abstract class ParametersParser extends HeaderParser {
    protected ParametersParser(Lexer lexer) {
        super(lexer);
    }

    protected ParametersParser(String buffer) {
        super(buffer);
    }

    protected void parse(ParametersHeader parametersHeader) throws ParseException {
        this.lexer.SPorHT();
        while (this.lexer.lookAhead(0) == ';') {
            this.lexer.consume(1);
            this.lexer.SPorHT();
            parametersHeader.setParameter(nameValue());
            this.lexer.SPorHT();
        }
    }

    protected void parseNameValueList(ParametersHeader parametersHeader) throws ParseException {
        parametersHeader.removeParameters();
        while (true) {
            this.lexer.SPorHT();
            NameValue nv = nameValue();
            parametersHeader.setParameter(nv.getName(), (String) nv.getValueAsObject());
            this.lexer.SPorHT();
            if (this.lexer.lookAhead(0) == ';') {
                this.lexer.consume(1);
            } else {
                return;
            }
        }
    }
}
