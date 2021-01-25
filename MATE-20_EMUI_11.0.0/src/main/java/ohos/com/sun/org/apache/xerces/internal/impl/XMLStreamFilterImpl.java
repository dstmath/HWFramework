package ohos.com.sun.org.apache.xerces.internal.impl;

import java.io.PrintStream;
import ohos.javax.xml.namespace.NamespaceContext;
import ohos.javax.xml.namespace.QName;
import ohos.javax.xml.stream.Location;
import ohos.javax.xml.stream.StreamFilter;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamReader;

public class XMLStreamFilterImpl implements XMLStreamReader {
    private int fCurrentEvent;
    private boolean fEventAccepted = false;
    private boolean fStreamAdvancedByHasNext = false;
    private StreamFilter fStreamFilter = null;
    private XMLStreamReader fStreamReader = null;

    public XMLStreamFilterImpl(XMLStreamReader xMLStreamReader, StreamFilter streamFilter) {
        this.fStreamReader = xMLStreamReader;
        this.fStreamFilter = streamFilter;
        try {
            if (this.fStreamFilter.accept(this.fStreamReader)) {
                this.fEventAccepted = true;
            } else {
                findNextEvent();
            }
        } catch (XMLStreamException e) {
            PrintStream printStream = System.err;
            printStream.println("Error while creating a stream Filter" + e);
        }
    }

    /* access modifiers changed from: protected */
    public void setStreamFilter(StreamFilter streamFilter) {
        this.fStreamFilter = streamFilter;
    }

    public int next() throws XMLStreamException {
        if (!this.fStreamAdvancedByHasNext || !this.fEventAccepted) {
            int findNextEvent = findNextEvent();
            if (findNextEvent != -1) {
                return findNextEvent;
            }
            throw new IllegalStateException("The stream reader has reached the end of the document, or there are no more  items to return");
        }
        this.fStreamAdvancedByHasNext = false;
        return this.fCurrentEvent;
    }

    public int nextTag() throws XMLStreamException {
        int i;
        if (!this.fStreamAdvancedByHasNext || !this.fEventAccepted || !((i = this.fCurrentEvent) == 1 || i == 1)) {
            int findNextTag = findNextTag();
            if (findNextTag != -1) {
                return findNextTag;
            }
            throw new IllegalStateException("The stream reader has reached the end of the document, or there are no more  items to return");
        }
        this.fStreamAdvancedByHasNext = false;
        return this.fCurrentEvent;
    }

    public boolean hasNext() throws XMLStreamException {
        if (!this.fStreamReader.hasNext()) {
            return false;
        }
        if (!this.fEventAccepted) {
            int findNextEvent = findNextEvent();
            this.fCurrentEvent = findNextEvent;
            if (findNextEvent == -1) {
                return false;
            }
            this.fStreamAdvancedByHasNext = true;
        }
        return true;
    }

    private int findNextEvent() throws XMLStreamException {
        this.fStreamAdvancedByHasNext = false;
        while (this.fStreamReader.hasNext()) {
            this.fCurrentEvent = this.fStreamReader.next();
            if (this.fStreamFilter.accept(this.fStreamReader)) {
                this.fEventAccepted = true;
                return this.fCurrentEvent;
            }
        }
        int i = this.fCurrentEvent;
        if (i == 8) {
            return i;
        }
        return -1;
    }

    private int findNextTag() throws XMLStreamException {
        this.fStreamAdvancedByHasNext = false;
        while (this.fStreamReader.hasNext()) {
            this.fCurrentEvent = this.fStreamReader.nextTag();
            if (this.fStreamFilter.accept(this.fStreamReader)) {
                this.fEventAccepted = true;
                return this.fCurrentEvent;
            }
        }
        int i = this.fCurrentEvent;
        if (i == 8) {
            return i;
        }
        return -1;
    }

    public void close() throws XMLStreamException {
        this.fStreamReader.close();
    }

    public int getAttributeCount() {
        return this.fStreamReader.getAttributeCount();
    }

    public QName getAttributeName(int i) {
        return this.fStreamReader.getAttributeName(i);
    }

    public String getAttributeNamespace(int i) {
        return this.fStreamReader.getAttributeNamespace(i);
    }

    public String getAttributePrefix(int i) {
        return this.fStreamReader.getAttributePrefix(i);
    }

    public String getAttributeType(int i) {
        return this.fStreamReader.getAttributeType(i);
    }

    public String getAttributeValue(int i) {
        return this.fStreamReader.getAttributeValue(i);
    }

    public String getAttributeValue(String str, String str2) {
        return this.fStreamReader.getAttributeValue(str, str2);
    }

    public String getCharacterEncodingScheme() {
        return this.fStreamReader.getCharacterEncodingScheme();
    }

    public String getElementText() throws XMLStreamException {
        return this.fStreamReader.getElementText();
    }

    public String getEncoding() {
        return this.fStreamReader.getEncoding();
    }

    public int getEventType() {
        return this.fStreamReader.getEventType();
    }

    public String getLocalName() {
        return this.fStreamReader.getLocalName();
    }

    public Location getLocation() {
        return this.fStreamReader.getLocation();
    }

    public QName getName() {
        return this.fStreamReader.getName();
    }

    public NamespaceContext getNamespaceContext() {
        return this.fStreamReader.getNamespaceContext();
    }

    public int getNamespaceCount() {
        return this.fStreamReader.getNamespaceCount();
    }

    public String getNamespacePrefix(int i) {
        return this.fStreamReader.getNamespacePrefix(i);
    }

    public String getNamespaceURI() {
        return this.fStreamReader.getNamespaceURI();
    }

    public String getNamespaceURI(int i) {
        return this.fStreamReader.getNamespaceURI(i);
    }

    public String getNamespaceURI(String str) {
        return this.fStreamReader.getNamespaceURI(str);
    }

    public String getPIData() {
        return this.fStreamReader.getPIData();
    }

    public String getPITarget() {
        return this.fStreamReader.getPITarget();
    }

    public String getPrefix() {
        return this.fStreamReader.getPrefix();
    }

    public Object getProperty(String str) throws IllegalArgumentException {
        return this.fStreamReader.getProperty(str);
    }

    public String getText() {
        return this.fStreamReader.getText();
    }

    public char[] getTextCharacters() {
        return this.fStreamReader.getTextCharacters();
    }

    public int getTextCharacters(int i, char[] cArr, int i2, int i3) throws XMLStreamException {
        return this.fStreamReader.getTextCharacters(i, cArr, i2, i3);
    }

    public int getTextLength() {
        return this.fStreamReader.getTextLength();
    }

    public int getTextStart() {
        return this.fStreamReader.getTextStart();
    }

    public String getVersion() {
        return this.fStreamReader.getVersion();
    }

    public boolean hasName() {
        return this.fStreamReader.hasName();
    }

    public boolean hasText() {
        return this.fStreamReader.hasText();
    }

    public boolean isAttributeSpecified(int i) {
        return this.fStreamReader.isAttributeSpecified(i);
    }

    public boolean isCharacters() {
        return this.fStreamReader.isCharacters();
    }

    public boolean isEndElement() {
        return this.fStreamReader.isEndElement();
    }

    public boolean isStandalone() {
        return this.fStreamReader.isStandalone();
    }

    public boolean isStartElement() {
        return this.fStreamReader.isStartElement();
    }

    public boolean isWhiteSpace() {
        return this.fStreamReader.isWhiteSpace();
    }

    public void require(int i, String str, String str2) throws XMLStreamException {
        this.fStreamReader.require(i, str, str2);
    }

    public boolean standaloneSet() {
        return this.fStreamReader.standaloneSet();
    }

    public String getAttributeLocalName(int i) {
        return this.fStreamReader.getAttributeLocalName(i);
    }
}
