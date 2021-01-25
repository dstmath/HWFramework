package ohos.javax.xml.stream;

import ohos.javax.xml.namespace.NamespaceContext;
import ohos.javax.xml.stream.events.XMLEvent;
import ohos.javax.xml.stream.util.XMLEventConsumer;

public interface XMLEventWriter extends XMLEventConsumer {
    void add(XMLEventReader xMLEventReader) throws XMLStreamException;

    @Override // ohos.javax.xml.stream.util.XMLEventConsumer
    void add(XMLEvent xMLEvent) throws XMLStreamException;

    void close() throws XMLStreamException;

    void flush() throws XMLStreamException;

    NamespaceContext getNamespaceContext();

    String getPrefix(String str) throws XMLStreamException;

    void setDefaultNamespace(String str) throws XMLStreamException;

    void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException;

    void setPrefix(String str, String str2) throws XMLStreamException;
}
