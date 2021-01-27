package ohos.com.sun.org.apache.xerces.internal.xs.datatypes;

import java.util.List;

public interface ObjectList extends List {
    @Override // java.util.List, java.util.Collection, ohos.com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList
    boolean contains(Object obj);

    int getLength();

    Object item(int i);
}
