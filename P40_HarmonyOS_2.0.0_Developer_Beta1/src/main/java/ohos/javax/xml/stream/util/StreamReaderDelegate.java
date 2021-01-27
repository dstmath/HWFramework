package ohos.javax.xml.stream.util;

import ohos.javax.xml.namespace.NamespaceContext;
import ohos.javax.xml.namespace.QName;
import ohos.javax.xml.stream.Location;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamReader;

public class StreamReaderDelegate implements XMLStreamReader {
    private XMLStreamReader reader;

    public StreamReaderDelegate() {
    }

    public StreamReaderDelegate(XMLStreamReader xMLStreamReader) {
        this.reader = xMLStreamReader;
    }

    public void setParent(XMLStreamReader xMLStreamReader) {
        this.reader = xMLStreamReader;
    }

    public XMLStreamReader getParent() {
        return this.reader;
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public int next() throws XMLStreamException {
        return this.reader.next();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public int nextTag() throws XMLStreamException {
        return this.reader.nextTag();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getElementText() throws XMLStreamException {
        return this.reader.getElementText();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public void require(int i, String str, String str2) throws XMLStreamException {
        this.reader.require(i, str, str2);
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public boolean hasNext() throws XMLStreamException {
        return this.reader.hasNext();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public void close() throws XMLStreamException {
        this.reader.close();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getNamespaceURI(String str) {
        return this.reader.getNamespaceURI(str);
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public NamespaceContext getNamespaceContext() {
        return this.reader.getNamespaceContext();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public boolean isStartElement() {
        return this.reader.isStartElement();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public boolean isEndElement() {
        return this.reader.isEndElement();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public boolean isCharacters() {
        return this.reader.isCharacters();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public boolean isWhiteSpace() {
        return this.reader.isWhiteSpace();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getAttributeValue(String str, String str2) {
        return this.reader.getAttributeValue(str, str2);
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public int getAttributeCount() {
        return this.reader.getAttributeCount();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public QName getAttributeName(int i) {
        return this.reader.getAttributeName(i);
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getAttributePrefix(int i) {
        return this.reader.getAttributePrefix(i);
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getAttributeNamespace(int i) {
        return this.reader.getAttributeNamespace(i);
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getAttributeLocalName(int i) {
        return this.reader.getAttributeLocalName(i);
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getAttributeType(int i) {
        return this.reader.getAttributeType(i);
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getAttributeValue(int i) {
        return this.reader.getAttributeValue(i);
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public boolean isAttributeSpecified(int i) {
        return this.reader.isAttributeSpecified(i);
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public int getNamespaceCount() {
        return this.reader.getNamespaceCount();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getNamespacePrefix(int i) {
        return this.reader.getNamespacePrefix(i);
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getNamespaceURI(int i) {
        return this.reader.getNamespaceURI(i);
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public int getEventType() {
        return this.reader.getEventType();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getText() {
        return this.reader.getText();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public int getTextCharacters(int i, char[] cArr, int i2, int i3) throws XMLStreamException {
        return this.reader.getTextCharacters(i, cArr, i2, i3);
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public char[] getTextCharacters() {
        return this.reader.getTextCharacters();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public int getTextStart() {
        return this.reader.getTextStart();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public int getTextLength() {
        return this.reader.getTextLength();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getEncoding() {
        return this.reader.getEncoding();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public boolean hasText() {
        return this.reader.hasText();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public Location getLocation() {
        return this.reader.getLocation();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public QName getName() {
        return this.reader.getName();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getLocalName() {
        return this.reader.getLocalName();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public boolean hasName() {
        return this.reader.hasName();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getNamespaceURI() {
        return this.reader.getNamespaceURI();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getPrefix() {
        return this.reader.getPrefix();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getVersion() {
        return this.reader.getVersion();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public boolean isStandalone() {
        return this.reader.isStandalone();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public boolean standaloneSet() {
        return this.reader.standaloneSet();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getCharacterEncodingScheme() {
        return this.reader.getCharacterEncodingScheme();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getPITarget() {
        return this.reader.getPITarget();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public String getPIData() {
        return this.reader.getPIData();
    }

    @Override // ohos.javax.xml.stream.XMLStreamReader
    public Object getProperty(String str) {
        return this.reader.getProperty(str);
    }
}
