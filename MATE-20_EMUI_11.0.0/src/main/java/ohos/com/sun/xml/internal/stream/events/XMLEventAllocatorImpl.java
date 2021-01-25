package ohos.com.sun.xml.internal.stream.events;

import java.util.List;
import ohos.com.sun.org.apache.xerces.internal.impl.PropertyManager;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceContextWrapper;
import ohos.com.sun.org.apache.xerces.internal.util.NamespaceSupport;
import ohos.javax.xml.namespace.QName;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamReader;
import ohos.javax.xml.stream.events.XMLEvent;
import ohos.javax.xml.stream.util.XMLEventAllocator;
import ohos.javax.xml.stream.util.XMLEventConsumer;

public class XMLEventAllocatorImpl implements XMLEventAllocator {
    public XMLEvent allocate(XMLStreamReader xMLStreamReader) throws XMLStreamException {
        if (xMLStreamReader != null) {
            return getXMLEvent(xMLStreamReader);
        }
        throw new XMLStreamException("Reader cannot be null");
    }

    public void allocate(XMLStreamReader xMLStreamReader, XMLEventConsumer xMLEventConsumer) throws XMLStreamException {
        XMLEvent xMLEvent = getXMLEvent(xMLStreamReader);
        if (xMLEvent != null) {
            xMLEventConsumer.add(xMLEvent);
        }
    }

    public XMLEventAllocator newInstance() {
        return new XMLEventAllocatorImpl();
    }

    /* access modifiers changed from: package-private */
    public XMLEvent getXMLEvent(XMLStreamReader xMLStreamReader) {
        switch (xMLStreamReader.getEventType()) {
            case 1:
                StartElementEvent startElementEvent = new StartElementEvent(getQName(xMLStreamReader));
                fillAttributes(startElementEvent, xMLStreamReader);
                if (((Boolean) xMLStreamReader.getProperty("javax.xml.stream.isNamespaceAware")).booleanValue()) {
                    fillNamespaceAttributes(startElementEvent, xMLStreamReader);
                    setNamespaceContext(startElementEvent, xMLStreamReader);
                }
                startElementEvent.setLocation(xMLStreamReader.getLocation());
                return startElementEvent;
            case 2:
                EndElementEvent endElementEvent = new EndElementEvent(getQName(xMLStreamReader));
                endElementEvent.setLocation(xMLStreamReader.getLocation());
                if (!((Boolean) xMLStreamReader.getProperty("javax.xml.stream.isNamespaceAware")).booleanValue()) {
                    return endElementEvent;
                }
                fillNamespaceAttributes(endElementEvent, xMLStreamReader);
                return endElementEvent;
            case 3:
                ProcessingInstructionEvent processingInstructionEvent = new ProcessingInstructionEvent(xMLStreamReader.getPITarget(), xMLStreamReader.getPIData());
                processingInstructionEvent.setLocation(xMLStreamReader.getLocation());
                return processingInstructionEvent;
            case 4:
                CharacterEvent characterEvent = new CharacterEvent(xMLStreamReader.getText());
                characterEvent.setLocation(xMLStreamReader.getLocation());
                return characterEvent;
            case 5:
                CommentEvent commentEvent = new CommentEvent(xMLStreamReader.getText());
                commentEvent.setLocation(xMLStreamReader.getLocation());
                return commentEvent;
            case 6:
                CharacterEvent characterEvent2 = new CharacterEvent(xMLStreamReader.getText(), false, true);
                characterEvent2.setLocation(xMLStreamReader.getLocation());
                return characterEvent2;
            case 7:
                StartDocumentEvent startDocumentEvent = new StartDocumentEvent();
                startDocumentEvent.setVersion(xMLStreamReader.getVersion());
                startDocumentEvent.setEncoding(xMLStreamReader.getEncoding());
                if (xMLStreamReader.getCharacterEncodingScheme() != null) {
                    startDocumentEvent.setDeclaredEncoding(true);
                } else {
                    startDocumentEvent.setDeclaredEncoding(false);
                }
                startDocumentEvent.setStandalone(xMLStreamReader.isStandalone());
                startDocumentEvent.setLocation(xMLStreamReader.getLocation());
                return startDocumentEvent;
            case 8:
                EndDocumentEvent endDocumentEvent = new EndDocumentEvent();
                endDocumentEvent.setLocation(xMLStreamReader.getLocation());
                return endDocumentEvent;
            case 9:
                EntityReferenceEvent entityReferenceEvent = new EntityReferenceEvent(xMLStreamReader.getLocalName(), new EntityDeclarationImpl(xMLStreamReader.getLocalName(), xMLStreamReader.getText()));
                entityReferenceEvent.setLocation(xMLStreamReader.getLocation());
                return entityReferenceEvent;
            case 10:
            default:
                return null;
            case 11:
                DTDEvent dTDEvent = new DTDEvent(xMLStreamReader.getText());
                dTDEvent.setLocation(xMLStreamReader.getLocation());
                List list = (List) xMLStreamReader.getProperty(PropertyManager.STAX_ENTITIES);
                if (!(list == null || list.size() == 0)) {
                    dTDEvent.setEntities(list);
                }
                List list2 = (List) xMLStreamReader.getProperty(PropertyManager.STAX_NOTATIONS);
                if (list2 == null || list2.size() == 0) {
                    return dTDEvent;
                }
                dTDEvent.setNotations(list2);
                return dTDEvent;
            case 12:
                CharacterEvent characterEvent3 = new CharacterEvent(xMLStreamReader.getText(), true);
                characterEvent3.setLocation(xMLStreamReader.getLocation());
                return characterEvent3;
        }
    }

    /* access modifiers changed from: protected */
    public XMLEvent getNextEvent(XMLStreamReader xMLStreamReader) throws XMLStreamException {
        xMLStreamReader.next();
        return getXMLEvent(xMLStreamReader);
    }

    /* access modifiers changed from: protected */
    public void fillAttributes(StartElementEvent startElementEvent, XMLStreamReader xMLStreamReader) {
        int attributeCount = xMLStreamReader.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            QName attributeName = xMLStreamReader.getAttributeName(i);
            AttributeImpl attributeImpl = new AttributeImpl();
            attributeImpl.setName(attributeName);
            attributeImpl.setAttributeType(xMLStreamReader.getAttributeType(i));
            attributeImpl.setSpecified(xMLStreamReader.isAttributeSpecified(i));
            attributeImpl.setValue(xMLStreamReader.getAttributeValue(i));
            startElementEvent.addAttribute(attributeImpl);
        }
    }

    /* access modifiers changed from: protected */
    public void fillNamespaceAttributes(StartElementEvent startElementEvent, XMLStreamReader xMLStreamReader) {
        int namespaceCount = xMLStreamReader.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            String namespaceURI = xMLStreamReader.getNamespaceURI(i);
            String namespacePrefix = xMLStreamReader.getNamespacePrefix(i);
            if (namespacePrefix == null) {
                namespacePrefix = "";
            }
            startElementEvent.addNamespaceAttribute(new NamespaceImpl(namespacePrefix, namespaceURI));
        }
    }

    /* access modifiers changed from: protected */
    public void fillNamespaceAttributes(EndElementEvent endElementEvent, XMLStreamReader xMLStreamReader) {
        int namespaceCount = xMLStreamReader.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            String namespaceURI = xMLStreamReader.getNamespaceURI(i);
            String namespacePrefix = xMLStreamReader.getNamespacePrefix(i);
            if (namespacePrefix == null) {
                namespacePrefix = "";
            }
            endElementEvent.addNamespace(new NamespaceImpl(namespacePrefix, namespaceURI));
        }
    }

    private void setNamespaceContext(StartElementEvent startElementEvent, XMLStreamReader xMLStreamReader) {
        startElementEvent.setNamespaceContext(new NamespaceContextWrapper(new NamespaceSupport(xMLStreamReader.getNamespaceContext().getNamespaceContext())));
    }

    private QName getQName(XMLStreamReader xMLStreamReader) {
        return new QName(xMLStreamReader.getNamespaceURI(), xMLStreamReader.getLocalName(), xMLStreamReader.getPrefix());
    }
}
