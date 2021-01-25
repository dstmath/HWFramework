package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;

public interface XMLEntityHandler {
    void endEntity(String str, Augmentations augmentations) throws IOException, XNIException;

    void startEntity(String str, XMLResourceIdentifier xMLResourceIdentifier, String str2, Augmentations augmentations) throws XNIException;
}
