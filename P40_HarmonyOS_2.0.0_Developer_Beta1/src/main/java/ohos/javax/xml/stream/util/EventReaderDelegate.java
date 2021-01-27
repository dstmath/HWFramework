package ohos.javax.xml.stream.util;

import ohos.javax.xml.stream.XMLEventReader;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.events.XMLEvent;

public class EventReaderDelegate implements XMLEventReader {
    private XMLEventReader reader;

    public EventReaderDelegate() {
    }

    public EventReaderDelegate(XMLEventReader xMLEventReader) {
        this.reader = xMLEventReader;
    }

    public void setParent(XMLEventReader xMLEventReader) {
        this.reader = xMLEventReader;
    }

    public XMLEventReader getParent() {
        return this.reader;
    }

    @Override // ohos.javax.xml.stream.XMLEventReader
    public XMLEvent nextEvent() throws XMLStreamException {
        return this.reader.nextEvent();
    }

    @Override // java.util.Iterator
    public Object next() {
        return this.reader.next();
    }

    @Override // ohos.javax.xml.stream.XMLEventReader, java.util.Iterator
    public boolean hasNext() {
        return this.reader.hasNext();
    }

    @Override // ohos.javax.xml.stream.XMLEventReader
    public XMLEvent peek() throws XMLStreamException {
        return this.reader.peek();
    }

    @Override // ohos.javax.xml.stream.XMLEventReader
    public void close() throws XMLStreamException {
        this.reader.close();
    }

    @Override // ohos.javax.xml.stream.XMLEventReader
    public String getElementText() throws XMLStreamException {
        return this.reader.getElementText();
    }

    @Override // ohos.javax.xml.stream.XMLEventReader
    public XMLEvent nextTag() throws XMLStreamException {
        return this.reader.nextTag();
    }

    @Override // ohos.javax.xml.stream.XMLEventReader
    public Object getProperty(String str) throws IllegalArgumentException {
        return this.reader.getProperty(str);
    }

    @Override // java.util.Iterator
    public void remove() {
        this.reader.remove();
    }
}
