package ohos.com.sun.org.apache.xerces.internal.xs;

import java.util.List;

public interface XSObjectList extends List {
    int getLength();

    XSObject item(int i);
}
