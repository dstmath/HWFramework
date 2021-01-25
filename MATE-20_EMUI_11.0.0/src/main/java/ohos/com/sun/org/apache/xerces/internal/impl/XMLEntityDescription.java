package ohos.com.sun.org.apache.xerces.internal.impl;

import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;

public interface XMLEntityDescription extends XMLResourceIdentifier {
    String getEntityName();

    void setEntityName(String str);
}
