package ohos.com.sun.org.apache.xerces.internal.xs;

import java.util.List;

public interface XSNamespaceItemList extends List {
    int getLength();

    XSNamespaceItem item(int i);
}
