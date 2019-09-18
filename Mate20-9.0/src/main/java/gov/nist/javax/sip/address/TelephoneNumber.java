package gov.nist.javax.sip.address;

import gov.nist.core.GenericObject;
import gov.nist.core.NameValue;
import gov.nist.core.NameValueList;
import gov.nist.core.Separators;
import java.util.Iterator;

public class TelephoneNumber extends NetObject {
    public static final String ISUB = "isub";
    public static final String PHONE_CONTEXT_TAG = "context-tag";
    public static final String POSTDIAL = "postdial";
    public static final String PROVIDER_TAG = "provider-tag";
    protected boolean isglobal;
    protected NameValueList parameters = new NameValueList();
    protected String phoneNumber;

    public void deleteParm(String name) {
        this.parameters.delete(name);
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public String getPostDial() {
        return (String) this.parameters.getValue("postdial");
    }

    public String getIsdnSubaddress() {
        return (String) this.parameters.getValue("isub");
    }

    public boolean hasPostDial() {
        return this.parameters.getValue("postdial") != null;
    }

    public boolean hasParm(String pname) {
        return this.parameters.hasNameValue(pname);
    }

    public boolean hasIsdnSubaddress() {
        return hasParm("isub");
    }

    public boolean isGlobal() {
        return this.isglobal;
    }

    public void removePostDial() {
        this.parameters.delete("postdial");
    }

    public void removeIsdnSubaddress() {
        deleteParm("isub");
    }

    public void setParameters(NameValueList p) {
        this.parameters = p;
    }

    public void setGlobal(boolean g) {
        this.isglobal = g;
    }

    public void setPostDial(String p) {
        this.parameters.set(new NameValue("postdial", p));
    }

    public void setParm(String name, Object value) {
        this.parameters.set(new NameValue(name, value));
    }

    public void setIsdnSubaddress(String isub) {
        setParm("isub", isub);
    }

    public void setPhoneNumber(String num) {
        this.phoneNumber = num;
    }

    public String encode() {
        return encode(new StringBuffer()).toString();
    }

    public StringBuffer encode(StringBuffer buffer) {
        if (this.isglobal) {
            buffer.append('+');
        }
        buffer.append(this.phoneNumber);
        if (!this.parameters.isEmpty()) {
            buffer.append(Separators.SEMICOLON);
            this.parameters.encode(buffer);
        }
        return buffer;
    }

    public String getParameter(String name) {
        Object val = this.parameters.getValue(name);
        if (val == null) {
            return null;
        }
        if (val instanceof GenericObject) {
            return ((GenericObject) val).encode();
        }
        return val.toString();
    }

    public Iterator<String> getParameterNames() {
        return this.parameters.getNames();
    }

    public void removeParameter(String parameter) {
        this.parameters.delete(parameter);
    }

    public void setParameter(String name, String value) {
        this.parameters.set(new NameValue(name, value));
    }

    public Object clone() {
        TelephoneNumber retval = (TelephoneNumber) super.clone();
        if (this.parameters != null) {
            retval.parameters = (NameValueList) this.parameters.clone();
        }
        return retval;
    }

    public NameValueList getParameters() {
        return this.parameters;
    }
}
