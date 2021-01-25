package gov.nist.javax.sip.header;

import java.util.ListIterator;

public class ExtensionHeaderList extends SIPHeaderList<ExtensionHeaderImpl> {
    private static final long serialVersionUID = 4681326807149890197L;

    @Override // gov.nist.javax.sip.header.SIPHeaderList, gov.nist.core.GenericObject, java.lang.Object
    public Object clone() {
        ExtensionHeaderList retval = new ExtensionHeaderList(this.headerName);
        retval.clonehlist(this.hlist);
        return retval;
    }

    public ExtensionHeaderList(String hName) {
        super(ExtensionHeaderImpl.class, hName);
    }

    public ExtensionHeaderList() {
        super(ExtensionHeaderImpl.class, null);
    }

    @Override // gov.nist.javax.sip.header.SIPHeaderList, gov.nist.javax.sip.header.SIPHeader, gov.nist.javax.sip.header.SIPObject, gov.nist.core.GenericObject
    public String encode() {
        StringBuffer retval = new StringBuffer();
        ListIterator<ExtensionHeaderImpl> it = listIterator();
        while (it.hasNext()) {
            retval.append(it.next().encode());
        }
        return retval.toString();
    }
}
