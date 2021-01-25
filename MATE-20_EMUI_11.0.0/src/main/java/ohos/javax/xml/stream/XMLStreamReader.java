package ohos.javax.xml.stream;

import ohos.javax.xml.namespace.NamespaceContext;
import ohos.javax.xml.namespace.QName;

public interface XMLStreamReader extends XMLStreamConstants {
    void close() throws XMLStreamException;

    int getAttributeCount();

    String getAttributeLocalName(int i);

    QName getAttributeName(int i);

    String getAttributeNamespace(int i);

    String getAttributePrefix(int i);

    String getAttributeType(int i);

    String getAttributeValue(int i);

    String getAttributeValue(String str, String str2);

    String getCharacterEncodingScheme();

    String getElementText() throws XMLStreamException;

    String getEncoding();

    int getEventType();

    String getLocalName();

    Location getLocation();

    QName getName();

    NamespaceContext getNamespaceContext();

    int getNamespaceCount();

    String getNamespacePrefix(int i);

    String getNamespaceURI();

    String getNamespaceURI(int i);

    String getNamespaceURI(String str);

    String getPIData();

    String getPITarget();

    String getPrefix();

    Object getProperty(String str) throws IllegalArgumentException;

    String getText();

    int getTextCharacters(int i, char[] cArr, int i2, int i3) throws XMLStreamException;

    char[] getTextCharacters();

    int getTextLength();

    int getTextStart();

    String getVersion();

    boolean hasName();

    boolean hasNext() throws XMLStreamException;

    boolean hasText();

    boolean isAttributeSpecified(int i);

    boolean isCharacters();

    boolean isEndElement();

    boolean isStandalone();

    boolean isStartElement();

    boolean isWhiteSpace();

    int next() throws XMLStreamException;

    int nextTag() throws XMLStreamException;

    void require(int i, String str, String str2) throws XMLStreamException;

    boolean standaloneSet();
}
