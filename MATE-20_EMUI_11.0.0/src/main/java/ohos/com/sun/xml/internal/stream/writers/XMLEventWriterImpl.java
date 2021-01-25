package ohos.com.sun.xml.internal.stream.writers;

import java.util.Iterator;
import ohos.javax.xml.namespace.NamespaceContext;
import ohos.javax.xml.namespace.QName;
import ohos.javax.xml.stream.XMLEventReader;
import ohos.javax.xml.stream.XMLEventWriter;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamWriter;
import ohos.javax.xml.stream.events.Attribute;
import ohos.javax.xml.stream.events.Characters;
import ohos.javax.xml.stream.events.Comment;
import ohos.javax.xml.stream.events.DTD;
import ohos.javax.xml.stream.events.EntityReference;
import ohos.javax.xml.stream.events.Namespace;
import ohos.javax.xml.stream.events.ProcessingInstruction;
import ohos.javax.xml.stream.events.StartDocument;
import ohos.javax.xml.stream.events.StartElement;
import ohos.javax.xml.stream.events.XMLEvent;

public class XMLEventWriterImpl implements XMLEventWriter {
    private static final boolean DEBUG = false;
    private XMLStreamWriter fStreamWriter;

    public XMLEventWriterImpl(XMLStreamWriter xMLStreamWriter) {
        this.fStreamWriter = xMLStreamWriter;
    }

    public void add(XMLEventReader xMLEventReader) throws XMLStreamException {
        if (xMLEventReader != null) {
            while (xMLEventReader.hasNext()) {
                add(xMLEventReader.nextEvent());
            }
            return;
        }
        throw new XMLStreamException("Event reader shouldn't be null");
    }

    public void add(XMLEvent xMLEvent) throws XMLStreamException {
        switch (xMLEvent.getEventType()) {
            case 1:
                StartElement asStartElement = xMLEvent.asStartElement();
                QName name = asStartElement.getName();
                this.fStreamWriter.writeStartElement(name.getPrefix(), name.getLocalPart(), name.getNamespaceURI());
                Iterator namespaces = asStartElement.getNamespaces();
                while (namespaces.hasNext()) {
                    Namespace namespace = (Namespace) namespaces.next();
                    this.fStreamWriter.writeNamespace(namespace.getPrefix(), namespace.getNamespaceURI());
                }
                Iterator attributes = asStartElement.getAttributes();
                while (attributes.hasNext()) {
                    Attribute attribute = (Attribute) attributes.next();
                    QName name2 = attribute.getName();
                    this.fStreamWriter.writeAttribute(name2.getPrefix(), name2.getNamespaceURI(), name2.getLocalPart(), attribute.getValue());
                }
                return;
            case 2:
                this.fStreamWriter.writeEndElement();
                return;
            case 3:
                ProcessingInstruction processingInstruction = (ProcessingInstruction) xMLEvent;
                this.fStreamWriter.writeProcessingInstruction(processingInstruction.getTarget(), processingInstruction.getData());
                return;
            case 4:
                Characters asCharacters = xMLEvent.asCharacters();
                if (asCharacters.isCData()) {
                    this.fStreamWriter.writeCData(asCharacters.getData());
                    return;
                } else {
                    this.fStreamWriter.writeCharacters(asCharacters.getData());
                    return;
                }
            case 5:
                this.fStreamWriter.writeComment(((Comment) xMLEvent).getText());
                return;
            case 6:
            default:
                return;
            case 7:
                StartDocument startDocument = (StartDocument) xMLEvent;
                try {
                    this.fStreamWriter.writeStartDocument(startDocument.getCharacterEncodingScheme(), startDocument.getVersion());
                    return;
                } catch (XMLStreamException unused) {
                    this.fStreamWriter.writeStartDocument(startDocument.getVersion());
                    return;
                }
            case 8:
                this.fStreamWriter.writeEndDocument();
                return;
            case 9:
                this.fStreamWriter.writeEntityRef(((EntityReference) xMLEvent).getName());
                return;
            case 10:
                Attribute attribute2 = (Attribute) xMLEvent;
                QName name3 = attribute2.getName();
                this.fStreamWriter.writeAttribute(name3.getPrefix(), name3.getNamespaceURI(), name3.getLocalPart(), attribute2.getValue());
                return;
            case 11:
                this.fStreamWriter.writeDTD(((DTD) xMLEvent).getDocumentTypeDeclaration());
                return;
            case 12:
                Characters characters = (Characters) xMLEvent;
                if (characters.isCData()) {
                    this.fStreamWriter.writeCData(characters.getData());
                    return;
                }
                return;
            case 13:
                Namespace namespace2 = (Namespace) xMLEvent;
                this.fStreamWriter.writeNamespace(namespace2.getPrefix(), namespace2.getNamespaceURI());
                return;
        }
    }

    public void close() throws XMLStreamException {
        this.fStreamWriter.close();
    }

    public void flush() throws XMLStreamException {
        this.fStreamWriter.flush();
    }

    public NamespaceContext getNamespaceContext() {
        return this.fStreamWriter.getNamespaceContext();
    }

    public String getPrefix(String str) throws XMLStreamException {
        return this.fStreamWriter.getPrefix(str);
    }

    public void setDefaultNamespace(String str) throws XMLStreamException {
        this.fStreamWriter.setDefaultNamespace(str);
    }

    public void setNamespaceContext(NamespaceContext namespaceContext) throws XMLStreamException {
        this.fStreamWriter.setNamespaceContext(namespaceContext);
    }

    public void setPrefix(String str, String str2) throws XMLStreamException {
        this.fStreamWriter.setPrefix(str, str2);
    }
}
