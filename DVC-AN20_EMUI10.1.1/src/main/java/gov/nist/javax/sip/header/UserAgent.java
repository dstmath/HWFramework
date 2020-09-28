package gov.nist.javax.sip.header;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.sip.header.UserAgentHeader;

public class UserAgent extends SIPHeader implements UserAgentHeader {
    private static final long serialVersionUID = 4561239179796364295L;
    protected List productTokens = new LinkedList();

    private String encodeProduct() {
        StringBuffer tokens = new StringBuffer();
        ListIterator it = this.productTokens.listIterator();
        while (it.hasNext()) {
            tokens.append((String) it.next());
        }
        return tokens.toString();
    }

    @Override // javax.sip.header.UserAgentHeader
    public void addProductToken(String pt) {
        this.productTokens.add(pt);
    }

    public UserAgent() {
        super("User-Agent");
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeProduct();
    }

    @Override // javax.sip.header.UserAgentHeader
    public ListIterator getProduct() {
        List list = this.productTokens;
        if (list == null || list.isEmpty()) {
            return null;
        }
        return this.productTokens.listIterator();
    }

    @Override // javax.sip.header.UserAgentHeader
    public void setProduct(List product) throws ParseException {
        if (product != null) {
            this.productTokens = product;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, UserAgent, setProduct(), the  product parameter is null");
    }

    @Override // java.lang.Object, javax.sip.header.Header, gov.nist.core.GenericObject
    public Object clone() {
        UserAgent retval = (UserAgent) super.clone();
        List list = this.productTokens;
        if (list != null) {
            retval.productTokens = new LinkedList(list);
        }
        return retval;
    }
}
