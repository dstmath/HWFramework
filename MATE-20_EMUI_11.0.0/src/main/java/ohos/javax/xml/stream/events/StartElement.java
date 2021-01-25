package ohos.javax.xml.stream.events;

import java.util.Iterator;
import ohos.javax.xml.namespace.NamespaceContext;
import ohos.javax.xml.namespace.QName;

public interface StartElement extends XMLEvent {
    Attribute getAttributeByName(QName qName);

    Iterator getAttributes();

    QName getName();

    NamespaceContext getNamespaceContext();

    String getNamespaceURI(String str);

    Iterator getNamespaces();
}
