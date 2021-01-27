package ohos.com.sun.org.apache.xerces.internal.xs.datatypes;

import java.util.List;
import ohos.com.sun.org.apache.xerces.internal.xs.XSException;

public interface ByteList extends List {
    boolean contains(byte b);

    int getLength();

    byte item(int i) throws XSException;
}
