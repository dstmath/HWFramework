package ohos.com.sun.org.apache.xerces.internal.xs;

import java.util.Map;

public interface XSNamedMap extends Map {
    int getLength();

    XSObject item(int i);

    XSObject itemByName(String str, String str2);
}
