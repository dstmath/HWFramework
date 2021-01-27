package ohos.com.sun.org.apache.xerces.internal.xni;

public interface XMLAttributes {
    int addAttribute(QName qName, String str, String str2);

    Augmentations getAugmentations(int i);

    Augmentations getAugmentations(String str);

    Augmentations getAugmentations(String str, String str2);

    int getIndex(String str);

    int getIndex(String str, String str2);

    int getLength();

    String getLocalName(int i);

    void getName(int i, QName qName);

    String getNonNormalizedValue(int i);

    String getPrefix(int i);

    String getQName(int i);

    QName getQualifiedName(int i);

    String getType(int i);

    String getType(String str);

    String getType(String str, String str2);

    String getURI(int i);

    String getValue(int i);

    String getValue(String str);

    String getValue(String str, String str2);

    boolean isSpecified(int i);

    void removeAllAttributes();

    void removeAttributeAt(int i);

    void setAugmentations(int i, Augmentations augmentations);

    void setName(int i, QName qName);

    void setNonNormalizedValue(int i, String str);

    void setSpecified(int i, boolean z);

    void setType(int i, String str);

    void setValue(int i, String str);

    void setValue(int i, String str, XMLString xMLString);
}
