package gov.nist.javax.sip.header;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.sip.header.ServerHeader;

public class Server extends SIPHeader implements ServerHeader {
    private static final long serialVersionUID = -3587764149383342973L;
    protected List productTokens = new LinkedList();

    private String encodeProduct() {
        StringBuffer tokens = new StringBuffer();
        ListIterator it = this.productTokens.listIterator();
        while (it.hasNext()) {
            tokens.append((String) it.next());
            if (!it.hasNext()) {
                break;
            }
            tokens.append('/');
        }
        return tokens.toString();
    }

    @Override // javax.sip.header.ServerHeader
    public void addProductToken(String pt) {
        this.productTokens.add(pt);
    }

    public Server() {
        super("Server");
    }

    @Override // gov.nist.javax.sip.header.SIPHeader
    public String encodeBody() {
        return encodeProduct();
    }

    @Override // javax.sip.header.ServerHeader
    public ListIterator getProduct() {
        List list = this.productTokens;
        if (list == null || list.isEmpty()) {
            return null;
        }
        return this.productTokens.listIterator();
    }

    @Override // javax.sip.header.ServerHeader
    public void setProduct(List product) throws ParseException {
        if (product != null) {
            this.productTokens = product;
            return;
        }
        throw new NullPointerException("JAIN-SIP Exception, UserAgent, setProduct(), the  product parameter is null");
    }
}
