package ohos.com.sun.xml.internal.stream;

import java.util.NoSuchElementException;
import ohos.javax.xml.stream.EventFilter;
import ohos.javax.xml.stream.XMLEventReader;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.events.XMLEvent;
import ohos.javax.xml.stream.util.EventReaderDelegate;

public class EventFilterSupport extends EventReaderDelegate {
    EventFilter fEventFilter;

    public EventFilterSupport(XMLEventReader xMLEventReader, EventFilter eventFilter) {
        setParent(xMLEventReader);
        this.fEventFilter = eventFilter;
    }

    public Object next() {
        try {
            return nextEvent();
        } catch (XMLStreamException unused) {
            throw new NoSuchElementException();
        }
    }

    public boolean hasNext() {
        try {
            return peek() != null;
        } catch (XMLStreamException unused) {
            return false;
        }
    }

    public XMLEvent nextEvent() throws XMLStreamException {
        if (EventFilterSupport.super.hasNext()) {
            XMLEvent nextEvent = EventFilterSupport.super.nextEvent();
            if (this.fEventFilter.accept(nextEvent)) {
                return nextEvent;
            }
            return nextEvent();
        }
        throw new NoSuchElementException();
    }

    public XMLEvent nextTag() throws XMLStreamException {
        if (EventFilterSupport.super.hasNext()) {
            XMLEvent nextTag = EventFilterSupport.super.nextTag();
            if (this.fEventFilter.accept(nextTag)) {
                return nextTag;
            }
            return nextTag();
        }
        throw new NoSuchElementException();
    }

    public XMLEvent peek() throws XMLStreamException {
        while (true) {
            XMLEvent peek = EventFilterSupport.super.peek();
            if (peek == null) {
                return null;
            }
            if (this.fEventFilter.accept(peek)) {
                return peek;
            }
            EventFilterSupport.super.next();
        }
    }
}
