package gov.nist.javax.sip.header;

import gov.nist.core.Separators;
import gov.nist.javax.sip.message.SIPRequest;
import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.CSeqHeader;

public class CSeq extends SIPHeader implements CSeqHeader {
    private static final long serialVersionUID = -5405798080040422910L;
    protected String method;
    protected Long seqno;

    public CSeq() {
        super("CSeq");
    }

    public CSeq(long seqno2, String method2) {
        this();
        this.seqno = Long.valueOf(seqno2);
        this.method = SIPRequest.getCannonicalName(method2);
    }

    @Override // gov.nist.javax.sip.header.SIPObject, javax.sip.header.Header, gov.nist.core.GenericObject
    public boolean equals(Object other) {
        if (!(other instanceof CSeqHeader)) {
            return false;
        }
        CSeqHeader o = (CSeqHeader) other;
        if (getSeqNumber() != o.getSeqNumber() || !getMethod().equals(o.getMethod())) {
            return false;
        }
        return true;
    }

    @Override // gov.nist.javax.sip.header.SIPHeader, gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public String encode() {
        return this.headerName + Separators.COLON + Separators.SP + encodeBody() + Separators.NEWLINE;
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeBody(new StringBuffer()).toString();
    }

    /* access modifiers changed from: protected */
    @Override // gov.nist.javax.sip.header.SIPHeader
    public StringBuffer encodeBody(StringBuffer buffer) {
        buffer.append(this.seqno);
        buffer.append(Separators.SP);
        buffer.append(this.method.toUpperCase());
        return buffer;
    }

    @Override // javax.sip.header.AllowHeader
    public String getMethod() {
        return this.method;
    }

    @Override // javax.sip.header.RSeqHeader
    public void setSeqNumber(long sequenceNumber) throws InvalidArgumentException {
        if (sequenceNumber < 0) {
            throw new InvalidArgumentException("JAIN-SIP Exception, CSeq, setSequenceNumber(), the sequence number parameter is < 0 : " + sequenceNumber);
        } else if (sequenceNumber <= 2147483648L) {
            this.seqno = Long.valueOf(sequenceNumber);
        } else {
            throw new InvalidArgumentException("JAIN-SIP Exception, CSeq, setSequenceNumber(), the sequence number parameter is too large : " + sequenceNumber);
        }
    }

    @Override // javax.sip.header.RSeqHeader
    public void setSequenceNumber(int sequenceNumber) throws InvalidArgumentException {
        setSeqNumber((long) sequenceNumber);
    }

    @Override // javax.sip.header.AllowHeader
    public void setMethod(String meth) throws ParseException {
        if (meth != null) {
            this.method = SIPRequest.getCannonicalName(meth);
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, CSeq, setMethod(), the meth parameter is null");
    }

    @Override // javax.sip.header.RSeqHeader
    public int getSequenceNumber() {
        Long l = this.seqno;
        if (l == null) {
            return 0;
        }
        return l.intValue();
    }

    @Override // javax.sip.header.RSeqHeader
    public long getSeqNumber() {
        return this.seqno.longValue();
    }
}
