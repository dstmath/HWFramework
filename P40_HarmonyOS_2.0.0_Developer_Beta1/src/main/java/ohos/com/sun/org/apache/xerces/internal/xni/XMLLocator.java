package ohos.com.sun.org.apache.xerces.internal.xni;

public interface XMLLocator {
    String getBaseSystemId();

    int getCharacterOffset();

    int getColumnNumber();

    String getEncoding();

    String getExpandedSystemId();

    int getLineNumber();

    String getLiteralSystemId();

    String getPublicId();

    String getXMLVersion();
}
