package gov.nist.javax.sip.header;

import java.util.Calendar;
import javax.sip.header.DateHeader;

public class SIPDateHeader extends SIPHeader implements DateHeader {
    private static final long serialVersionUID = 1734186339037274664L;
    protected SIPDate date;

    public SIPDateHeader() {
        super("Date");
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return this.date.encode();
    }

    public void setDate(SIPDate d) {
        this.date = d;
    }

    @Override // javax.sip.header.DateHeader
    public void setDate(Calendar dat) {
        if (dat != null) {
            this.date = new SIPDate(dat.getTime().getTime());
        }
    }

    @Override // javax.sip.header.DateHeader
    public Calendar getDate() {
        SIPDate sIPDate = this.date;
        if (sIPDate == null) {
            return null;
        }
        return sIPDate.getJavaCal();
    }

    @Override // gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        SIPDateHeader retval = (SIPDateHeader) super.clone();
        SIPDate sIPDate = this.date;
        if (sIPDate != null) {
            retval.date = (SIPDate) sIPDate.clone();
        }
        return retval;
    }
}
