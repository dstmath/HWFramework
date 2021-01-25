package ohos.com.sun.xml.internal.stream;

import java.util.NoSuchElementException;
import ohos.com.sun.xml.internal.stream.events.XMLEventAllocatorImpl;
import ohos.javax.xml.stream.XMLEventReader;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamReader;
import ohos.javax.xml.stream.events.EntityReference;
import ohos.javax.xml.stream.events.XMLEvent;
import ohos.javax.xml.stream.util.XMLEventAllocator;

public class XMLEventReaderImpl implements XMLEventReader {
    private XMLEvent fLastEvent;
    private XMLEvent fPeekedEvent;
    protected XMLEventAllocator fXMLEventAllocator;
    protected XMLStreamReader fXMLReader;

    public XMLEventReaderImpl(XMLStreamReader xMLStreamReader) throws XMLStreamException {
        this.fXMLReader = xMLStreamReader;
        this.fXMLEventAllocator = (XMLEventAllocator) xMLStreamReader.getProperty("javax.xml.stream.allocator");
        if (this.fXMLEventAllocator == null) {
            this.fXMLEventAllocator = new XMLEventAllocatorImpl();
        }
        this.fPeekedEvent = this.fXMLEventAllocator.allocate(this.fXMLReader);
    }

    public boolean hasNext() {
        if (this.fPeekedEvent != null) {
            return true;
        }
        try {
            return this.fXMLReader.hasNext();
        } catch (XMLStreamException unused) {
            return false;
        }
    }

    public XMLEvent nextEvent() throws XMLStreamException {
        XMLEvent xMLEvent = this.fPeekedEvent;
        if (xMLEvent != null) {
            this.fLastEvent = xMLEvent;
            this.fPeekedEvent = null;
            return this.fLastEvent;
        } else if (this.fXMLReader.hasNext()) {
            this.fXMLReader.next();
            XMLEvent allocate = this.fXMLEventAllocator.allocate(this.fXMLReader);
            this.fLastEvent = allocate;
            return allocate;
        } else {
            this.fLastEvent = null;
            throw new NoSuchElementException();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    public void close() throws XMLStreamException {
        this.fXMLReader.close();
    }

    public String getElementText() throws XMLStreamException {
        if (this.fLastEvent.getEventType() == 1) {
            EntityReference entityReference = this.fPeekedEvent;
            if (entityReference != null) {
                String str = null;
                this.fPeekedEvent = null;
                int eventType = entityReference.getEventType();
                if (eventType == 4 || eventType == 6 || eventType == 12) {
                    str = entityReference.asCharacters().getData();
                } else if (eventType == 9) {
                    str = entityReference.getDeclaration().getReplacementText();
                } else if (!(eventType == 5 || eventType == 3)) {
                    if (eventType == 1) {
                        throw new XMLStreamException("elementGetText() function expects text only elment but START_ELEMENT was encountered.", entityReference.getLocation());
                    } else if (eventType == 2) {
                        return "";
                    }
                }
                StringBuffer stringBuffer = new StringBuffer();
                if (str != null && str.length() > 0) {
                    stringBuffer.append(str);
                }
                EntityReference nextEvent = nextEvent();
                while (nextEvent.getEventType() != 2) {
                    if (eventType == 4 || eventType == 6 || eventType == 12) {
                        str = nextEvent.asCharacters().getData();
                    } else if (eventType == 9) {
                        str = nextEvent.getDeclaration().getReplacementText();
                    } else if (!(eventType == 5 || eventType == 3)) {
                        if (eventType == 8) {
                            throw new XMLStreamException("unexpected end of document when reading element text content");
                        } else if (eventType == 1) {
                            throw new XMLStreamException("elementGetText() function expects text only elment but START_ELEMENT was encountered.", nextEvent.getLocation());
                        } else {
                            throw new XMLStreamException("Unexpected event type " + eventType, nextEvent.getLocation());
                        }
                    }
                    if (str != null && str.length() > 0) {
                        stringBuffer.append(str);
                    }
                    nextEvent = nextEvent();
                }
                return stringBuffer.toString();
            }
            String elementText = this.fXMLReader.getElementText();
            this.fLastEvent = this.fXMLEventAllocator.allocate(this.fXMLReader);
            return elementText;
        }
        throw new XMLStreamException("parser must be on START_ELEMENT to read next text", this.fLastEvent.getLocation());
    }

    public Object getProperty(String str) throws IllegalArgumentException {
        return this.fXMLReader.getProperty(str);
    }

    public XMLEvent nextTag() throws XMLStreamException {
        XMLEvent xMLEvent = this.fPeekedEvent;
        if (xMLEvent != null) {
            this.fPeekedEvent = null;
            int eventType = xMLEvent.getEventType();
            if ((xMLEvent.isCharacters() && xMLEvent.asCharacters().isWhiteSpace()) || eventType == 3 || eventType == 5 || eventType == 7) {
                xMLEvent = nextEvent();
                eventType = xMLEvent.getEventType();
            }
            while (true) {
                if ((!xMLEvent.isCharacters() || !xMLEvent.asCharacters().isWhiteSpace()) && eventType != 3 && eventType != 5) {
                    break;
                }
                xMLEvent = nextEvent();
                eventType = xMLEvent.getEventType();
            }
            if (eventType == 1 || eventType == 2) {
                return xMLEvent;
            }
            throw new XMLStreamException("expected start or end tag", xMLEvent.getLocation());
        }
        this.fXMLReader.nextTag();
        XMLEvent allocate = this.fXMLEventAllocator.allocate(this.fXMLReader);
        this.fLastEvent = allocate;
        return allocate;
    }

    public Object next() {
        try {
            return nextEvent();
        } catch (XMLStreamException e) {
            this.fLastEvent = null;
            NoSuchElementException noSuchElementException = new NoSuchElementException(e.getMessage());
            noSuchElementException.initCause(e.getCause());
            throw noSuchElementException;
        }
    }

    public XMLEvent peek() throws XMLStreamException {
        XMLEvent xMLEvent = this.fPeekedEvent;
        if (xMLEvent != null) {
            return xMLEvent;
        }
        if (!hasNext()) {
            return null;
        }
        this.fXMLReader.next();
        this.fPeekedEvent = this.fXMLEventAllocator.allocate(this.fXMLReader);
        return this.fPeekedEvent;
    }
}
