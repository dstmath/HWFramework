package ohos.com.sun.xml.internal.stream;

import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.javax.xml.stream.XMLEventReader;
import ohos.javax.xml.stream.XMLStreamReader;

public class StaxXMLInputSource {
    XMLEventReader fEventReader;
    boolean fHasResolver = false;
    XMLInputSource fInputSource;
    XMLStreamReader fStreamReader;

    public StaxXMLInputSource(XMLStreamReader xMLStreamReader) {
        this.fStreamReader = xMLStreamReader;
    }

    public StaxXMLInputSource(XMLEventReader xMLEventReader) {
        this.fEventReader = xMLEventReader;
    }

    public StaxXMLInputSource(XMLInputSource xMLInputSource) {
        this.fInputSource = xMLInputSource;
    }

    public StaxXMLInputSource(XMLInputSource xMLInputSource, boolean z) {
        this.fInputSource = xMLInputSource;
        this.fHasResolver = z;
    }

    public XMLStreamReader getXMLStreamReader() {
        return this.fStreamReader;
    }

    public XMLEventReader getXMLEventReader() {
        return this.fEventReader;
    }

    public XMLInputSource getXMLInputSource() {
        return this.fInputSource;
    }

    public boolean hasXMLStreamOrXMLEventReader() {
        return (this.fStreamReader == null && this.fEventReader == null) ? false : true;
    }

    public boolean hasResolver() {
        return this.fHasResolver;
    }
}
