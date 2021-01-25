package ohos.com.sun.org.apache.xerces.internal.xs;

public interface XSImplementation {
    XSLoader createXSLoader(StringList stringList) throws XSException;

    StringList getRecognizedVersions();
}
