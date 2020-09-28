package javax.sip.header;

import java.text.ParseException;
import java.util.List;
import java.util.ListIterator;

public interface ServerHeader extends Header {
    public static final String NAME = "Server";

    void addProductToken(String str);

    ListIterator getProduct();

    void setProduct(List list) throws ParseException;
}
