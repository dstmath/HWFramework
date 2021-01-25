package ohos.javax.xml.stream.util;

import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.events.XMLEvent;

public interface XMLEventConsumer {
    void add(XMLEvent xMLEvent) throws XMLStreamException;
}
