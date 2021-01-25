package ohos.javax.xml.stream;

import java.util.Iterator;
import ohos.javax.xml.stream.events.XMLEvent;

public interface XMLEventReader extends Iterator {
    void close() throws XMLStreamException;

    String getElementText() throws XMLStreamException;

    Object getProperty(String str) throws IllegalArgumentException;

    @Override // java.util.Iterator
    boolean hasNext();

    XMLEvent nextEvent() throws XMLStreamException;

    XMLEvent nextTag() throws XMLStreamException;

    XMLEvent peek() throws XMLStreamException;
}
