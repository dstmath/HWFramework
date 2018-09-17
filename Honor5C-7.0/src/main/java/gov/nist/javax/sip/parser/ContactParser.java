package gov.nist.javax.sip.parser;

import gov.nist.javax.sip.header.Contact;
import gov.nist.javax.sip.header.ContactList;
import gov.nist.javax.sip.header.SIPHeader;
import java.text.ParseException;

public class ContactParser extends AddressParametersParser {
    public ContactParser(String contact) {
        super(contact);
    }

    protected ContactParser(Lexer lexer) {
        super(lexer);
        this.lexer = lexer;
    }

    public SIPHeader parse() throws ParseException {
        headerName(TokenTypes.CONTACT);
        ContactList retval = new ContactList();
        while (true) {
            Contact contact = new Contact();
            if (this.lexer.lookAhead(0) == '*') {
                char next = this.lexer.lookAhead(1);
                if (next == ' ' || next == '\t' || next == '\r' || next == '\n') {
                    this.lexer.match(42);
                    contact.setWildCardFlag(true);
                } else {
                    super.parse(contact);
                }
            } else {
                super.parse(contact);
            }
            retval.add((SIPHeader) contact);
            this.lexer.SPorHT();
            char la = this.lexer.lookAhead(0);
            if (la != ',') {
                break;
            }
            this.lexer.match(44);
            this.lexer.SPorHT();
        }
        if (la == '\n' || la == '\u0000') {
            return retval;
        }
        throw createParseException("unexpected char");
    }
}
