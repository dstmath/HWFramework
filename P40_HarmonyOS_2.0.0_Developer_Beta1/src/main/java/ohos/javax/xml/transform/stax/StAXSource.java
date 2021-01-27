package ohos.javax.xml.transform.stax;

import ohos.javax.xml.stream.XMLEventReader;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamReader;
import ohos.javax.xml.stream.events.XMLEvent;
import ohos.javax.xml.transform.Source;

public class StAXSource implements Source {
    public static final String FEATURE = "http://ohos.javax.xml.transform.stax.StAXSource/feature";
    private String systemId = null;
    private XMLEventReader xmlEventReader = null;
    private XMLStreamReader xmlStreamReader = null;

    public StAXSource(XMLEventReader xMLEventReader) throws XMLStreamException {
        if (xMLEventReader != null) {
            XMLEvent peek = xMLEventReader.peek();
            int eventType = peek.getEventType();
            if (eventType == 7 || eventType == 1) {
                this.xmlEventReader = xMLEventReader;
                this.systemId = peek.getLocation().getSystemId();
                return;
            }
            throw new IllegalStateException("StAXSource(XMLEventReader) with XMLEventReader not in XMLStreamConstants.START_DOCUMENT or XMLStreamConstants.START_ELEMENT state");
        }
        throw new IllegalArgumentException("StAXSource(XMLEventReader) with XMLEventReader == null");
    }

    public StAXSource(XMLStreamReader xMLStreamReader) {
        if (xMLStreamReader != null) {
            int eventType = xMLStreamReader.getEventType();
            if (eventType == 7 || eventType == 1) {
                this.xmlStreamReader = xMLStreamReader;
                this.systemId = xMLStreamReader.getLocation().getSystemId();
                return;
            }
            throw new IllegalStateException("StAXSource(XMLStreamReader) with XMLStreamReadernot in XMLStreamConstants.START_DOCUMENT or XMLStreamConstants.START_ELEMENT state");
        }
        throw new IllegalArgumentException("StAXSource(XMLStreamReader) with XMLStreamReader == null");
    }

    public XMLEventReader getXMLEventReader() {
        return this.xmlEventReader;
    }

    public XMLStreamReader getXMLStreamReader() {
        return this.xmlStreamReader;
    }

    @Override // ohos.javax.xml.transform.Source
    public void setSystemId(String str) {
        throw new UnsupportedOperationException("StAXSource#setSystemId(systemId) cannot set the system identifier for a StAXSource");
    }

    @Override // ohos.javax.xml.transform.Source
    public String getSystemId() {
        return this.systemId;
    }
}
