package ohos.com.sun.org.apache.xerces.internal.xni;

import java.util.Enumeration;

public interface Augmentations {
    Object getItem(String str);

    Enumeration keys();

    Object putItem(String str, Object obj);

    void removeAllItems();

    Object removeItem(String str);
}
