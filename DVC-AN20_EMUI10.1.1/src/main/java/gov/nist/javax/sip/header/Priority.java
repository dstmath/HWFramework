package gov.nist.javax.sip.header;

import java.text.ParseException;
import javax.sip.header.PriorityHeader;

public class Priority extends SIPHeader implements PriorityHeader {
    public static final String EMERGENCY = "emergency";
    public static final String NON_URGENT = "non-urgent";
    public static final String NORMAL = "normal";
    public static final String URGENT = "urgent";
    private static final long serialVersionUID = 3837543366074322106L;
    protected String priority;

    public Priority() {
        super("Priority");
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return this.priority;
    }

    @Override // javax.sip.header.PriorityHeader
    public String getPriority() {
        return this.priority;
    }

    @Override // javax.sip.header.PriorityHeader
    public void setPriority(String p) throws ParseException {
        if (p != null) {
            this.priority = p;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception,Priority, setPriority(), the priority parameter is null");
    }
}
