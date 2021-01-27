package ohos.com.sun.org.apache.xerces.internal.xs.datatypes;

import ohos.javax.xml.namespace.QName;

public interface XSQName {
    QName getJAXPQName();

    ohos.com.sun.org.apache.xerces.internal.xni.QName getXNIQName();
}
