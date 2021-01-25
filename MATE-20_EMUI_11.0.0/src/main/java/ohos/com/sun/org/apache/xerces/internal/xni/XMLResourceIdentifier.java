package ohos.com.sun.org.apache.xerces.internal.xni;

public interface XMLResourceIdentifier {
    String getBaseSystemId();

    String getExpandedSystemId();

    String getLiteralSystemId();

    String getNamespace();

    String getPublicId();

    void setBaseSystemId(String str);

    void setExpandedSystemId(String str);

    void setLiteralSystemId(String str);

    void setNamespace(String str);

    void setPublicId(String str);
}
