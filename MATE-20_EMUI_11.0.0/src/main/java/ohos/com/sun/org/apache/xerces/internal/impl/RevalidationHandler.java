package ohos.com.sun.org.apache.xerces.internal.impl;

import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLDocumentFilter;

public interface RevalidationHandler extends XMLDocumentFilter {
    boolean characterData(String str, Augmentations augmentations);
}
