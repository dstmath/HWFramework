package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.SubjectHeader;

public class Subject extends SIPHeader implements SubjectHeader {
    private static final long serialVersionUID = -6479220126758862528L;
    protected String subject;

    public Subject() {
        super("Subject");
    }

    public String encodeBody() {
        if (this.subject != null) {
            return this.subject;
        }
        return "";
    }

    public void setSubject(String subject) throws ParseException {
        if (subject == null) {
            throw new NullPointerException("JAIN-SIP Exception,  Subject, setSubject(), the subject parameter is null");
        }
        this.subject = subject;
    }

    public String getSubject() {
        return this.subject;
    }
}
