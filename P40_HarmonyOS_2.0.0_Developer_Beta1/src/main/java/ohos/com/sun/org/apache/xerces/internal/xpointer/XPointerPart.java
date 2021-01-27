package ohos.com.sun.org.apache.xerces.internal.xpointer;

import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;

public interface XPointerPart {
    public static final int EVENT_ELEMENT_EMPTY = 2;
    public static final int EVENT_ELEMENT_END = 1;
    public static final int EVENT_ELEMENT_START = 0;

    String getSchemeData();

    String getSchemeName();

    boolean isChildFragmentResolved() throws XNIException;

    boolean isFragmentResolved() throws XNIException;

    void parseXPointer(String str) throws XNIException;

    boolean resolveXPointer(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations, int i) throws XNIException;

    void setSchemeData(String str);

    void setSchemeName(String str);
}
