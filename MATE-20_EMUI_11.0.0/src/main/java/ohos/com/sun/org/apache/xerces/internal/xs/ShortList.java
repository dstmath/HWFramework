package ohos.com.sun.org.apache.xerces.internal.xs;

import java.util.List;

public interface ShortList extends List {
    boolean contains(short s);

    int getLength();

    short item(int i) throws XSException;
}
