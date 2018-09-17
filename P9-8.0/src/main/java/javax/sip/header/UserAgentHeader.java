package javax.sip.header;

import java.text.ParseException;
import java.util.List;
import java.util.ListIterator;

public interface UserAgentHeader extends Header {
    public static final String NAME = "User-Agent";

    void addProductToken(String str);

    ListIterator getProduct();

    void setProduct(List list) throws ParseException;
}
