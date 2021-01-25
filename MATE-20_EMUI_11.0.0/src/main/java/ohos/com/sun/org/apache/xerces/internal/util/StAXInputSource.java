package ohos.com.sun.org.apache.xerces.internal.util;

import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.javax.xml.stream.XMLEventReader;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamReader;

public final class StAXInputSource extends XMLInputSource {
    private final boolean fConsumeRemainingContent;
    private final XMLEventReader fEventReader;
    private final XMLStreamReader fStreamReader;

    public StAXInputSource(XMLStreamReader xMLStreamReader) {
        this(xMLStreamReader, false);
    }

    public StAXInputSource(XMLStreamReader xMLStreamReader, boolean z) {
        super(null, xMLStreamReader.getLocation().getSystemId(), null);
        this.fStreamReader = xMLStreamReader;
        this.fEventReader = null;
        this.fConsumeRemainingContent = z;
    }

    public StAXInputSource(XMLEventReader xMLEventReader) {
        this(xMLEventReader, false);
    }

    public StAXInputSource(XMLEventReader xMLEventReader, boolean z) {
        super(null, getEventReaderSystemId(xMLEventReader), null);
        if (xMLEventReader != null) {
            this.fStreamReader = null;
            this.fEventReader = xMLEventReader;
            this.fConsumeRemainingContent = z;
            return;
        }
        throw new IllegalArgumentException("XMLEventReader parameter cannot be null.");
    }

    public XMLStreamReader getXMLStreamReader() {
        return this.fStreamReader;
    }

    public XMLEventReader getXMLEventReader() {
        return this.fEventReader;
    }

    public boolean shouldConsumeRemainingContent() {
        return this.fConsumeRemainingContent;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource
    public void setSystemId(String str) {
        throw new UnsupportedOperationException("Cannot set the system ID on a StAXInputSource");
    }

    private static String getEventReaderSystemId(XMLEventReader xMLEventReader) {
        if (xMLEventReader == null) {
            return null;
        }
        try {
            return xMLEventReader.peek().getLocation().getSystemId();
        } catch (XMLStreamException unused) {
            return null;
        }
    }
}
