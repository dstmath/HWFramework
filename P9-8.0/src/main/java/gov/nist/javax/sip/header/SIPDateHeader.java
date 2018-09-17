package gov.nist.javax.sip.header;

import java.util.Calendar;
import javax.sip.header.DateHeader;

public class SIPDateHeader extends SIPHeader implements DateHeader {
    private static final long serialVersionUID = 1734186339037274664L;
    protected SIPDate date;

    public SIPDateHeader() {
        super("Date");
    }

    public String encodeBody() {
        return this.date.encode();
    }

    public void setDate(SIPDate d) {
        this.date = d;
    }

    public void setDate(Calendar dat) {
        if (dat != null) {
            this.date = new SIPDate(dat.getTime().getTime());
        }
    }

    public Calendar getDate() {
        if (this.date == null) {
            return null;
        }
        return this.date.getJavaCal();
    }

    public Object clone() {
        SIPDateHeader retval = (SIPDateHeader) super.clone();
        if (this.date != null) {
            retval.date = (SIPDate) this.date.clone();
        }
        return retval;
    }
}
