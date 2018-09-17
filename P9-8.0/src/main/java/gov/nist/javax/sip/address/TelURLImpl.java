package gov.nist.javax.sip.address;

import gov.nist.core.NameValueList;
import gov.nist.core.Separators;
import java.text.ParseException;
import java.util.Iterator;
import javax.sip.address.TelURL;

public class TelURLImpl extends GenericURI implements TelURL {
    private static final long serialVersionUID = 5873527320305915954L;
    protected TelephoneNumber telephoneNumber;

    public TelURLImpl() {
        this.scheme = "tel";
    }

    public void setTelephoneNumber(TelephoneNumber telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getIsdnSubAddress() {
        return this.telephoneNumber.getIsdnSubaddress();
    }

    public String getPostDial() {
        return this.telephoneNumber.getPostDial();
    }

    public String getScheme() {
        return this.scheme;
    }

    public boolean isGlobal() {
        return this.telephoneNumber.isGlobal();
    }

    public boolean isSipURI() {
        return false;
    }

    public void setGlobal(boolean global) {
        this.telephoneNumber.setGlobal(global);
    }

    public void setIsdnSubAddress(String isdnSubAddress) {
        this.telephoneNumber.setIsdnSubaddress(isdnSubAddress);
    }

    public void setPostDial(String postDial) {
        this.telephoneNumber.setPostDial(postDial);
    }

    public void setPhoneNumber(String telephoneNumber) {
        this.telephoneNumber.setPhoneNumber(telephoneNumber);
    }

    public String getPhoneNumber() {
        return this.telephoneNumber.getPhoneNumber();
    }

    public String toString() {
        return this.scheme + Separators.COLON + this.telephoneNumber.encode();
    }

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        buffer.append(this.scheme).append(':');
        this.telephoneNumber.encode(buffer);
        return buffer;
    }

    public Object clone() {
        TelURLImpl retval = (TelURLImpl) super.clone();
        if (this.telephoneNumber != null) {
            retval.telephoneNumber = (TelephoneNumber) this.telephoneNumber.clone();
        }
        return retval;
    }

    public String getParameter(String parameterName) {
        return this.telephoneNumber.getParameter(parameterName);
    }

    public void setParameter(String name, String value) {
        this.telephoneNumber.setParameter(name, value);
    }

    public Iterator<String> getParameterNames() {
        return this.telephoneNumber.getParameterNames();
    }

    public NameValueList getParameters() {
        return this.telephoneNumber.getParameters();
    }

    public void removeParameter(String name) {
        this.telephoneNumber.removeParameter(name);
    }

    public void setPhoneContext(String phoneContext) throws ParseException {
        if (phoneContext == null) {
            removeParameter("phone-context");
        } else {
            setParameter("phone-context", phoneContext);
        }
    }

    public String getPhoneContext() {
        return getParameter("phone-context");
    }
}
