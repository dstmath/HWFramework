package ohos.javax.xml.stream.events;

import java.util.Iterator;
import ohos.javax.xml.namespace.QName;

public interface EndElement extends XMLEvent {
    QName getName();

    Iterator getNamespaces();
}
