package ohos.com.sun.xml.internal.stream.events;

import java.util.Iterator;
import ohos.javax.xml.namespace.NamespaceContext;
import ohos.javax.xml.namespace.QName;
import ohos.javax.xml.stream.Location;
import ohos.javax.xml.stream.XMLEventFactory;
import ohos.javax.xml.stream.events.Attribute;
import ohos.javax.xml.stream.events.Characters;
import ohos.javax.xml.stream.events.Comment;
import ohos.javax.xml.stream.events.DTD;
import ohos.javax.xml.stream.events.EndDocument;
import ohos.javax.xml.stream.events.EndElement;
import ohos.javax.xml.stream.events.EntityDeclaration;
import ohos.javax.xml.stream.events.EntityReference;
import ohos.javax.xml.stream.events.Namespace;
import ohos.javax.xml.stream.events.ProcessingInstruction;
import ohos.javax.xml.stream.events.StartDocument;
import ohos.javax.xml.stream.events.StartElement;

public class XMLEventFactoryImpl extends XMLEventFactory {
    Location location = null;

    public Attribute createAttribute(String str, String str2) {
        AttributeImpl attributeImpl = new AttributeImpl(str, str2);
        Location location2 = this.location;
        if (location2 != null) {
            attributeImpl.setLocation(location2);
        }
        return attributeImpl;
    }

    public Attribute createAttribute(QName qName, String str) {
        return createAttribute(qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart(), str);
    }

    public Attribute createAttribute(String str, String str2, String str3, String str4) {
        AttributeImpl attributeImpl = new AttributeImpl(str, str2, str3, str4, (String) null);
        Location location2 = this.location;
        if (location2 != null) {
            attributeImpl.setLocation(location2);
        }
        return attributeImpl;
    }

    public Characters createCData(String str) {
        CharacterEvent characterEvent = new CharacterEvent(str, true);
        Location location2 = this.location;
        if (location2 != null) {
            characterEvent.setLocation(location2);
        }
        return characterEvent;
    }

    public Characters createCharacters(String str) {
        CharacterEvent characterEvent = new CharacterEvent(str);
        Location location2 = this.location;
        if (location2 != null) {
            characterEvent.setLocation(location2);
        }
        return characterEvent;
    }

    public Comment createComment(String str) {
        CommentEvent commentEvent = new CommentEvent(str);
        Location location2 = this.location;
        if (location2 != null) {
            commentEvent.setLocation(location2);
        }
        return commentEvent;
    }

    public DTD createDTD(String str) {
        DTDEvent dTDEvent = new DTDEvent(str);
        Location location2 = this.location;
        if (location2 != null) {
            dTDEvent.setLocation(location2);
        }
        return dTDEvent;
    }

    public EndDocument createEndDocument() {
        EndDocumentEvent endDocumentEvent = new EndDocumentEvent();
        Location location2 = this.location;
        if (location2 != null) {
            endDocumentEvent.setLocation(location2);
        }
        return endDocumentEvent;
    }

    public EndElement createEndElement(QName qName, Iterator it) {
        return createEndElement(qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart());
    }

    public EndElement createEndElement(String str, String str2, String str3) {
        EndElementEvent endElementEvent = new EndElementEvent(str, str2, str3);
        Location location2 = this.location;
        if (location2 != null) {
            endElementEvent.setLocation(location2);
        }
        return endElementEvent;
    }

    public EndElement createEndElement(String str, String str2, String str3, Iterator it) {
        EndElementEvent endElementEvent = new EndElementEvent(str, str2, str3);
        if (it != null) {
            while (it.hasNext()) {
                endElementEvent.addNamespace((Namespace) it.next());
            }
        }
        Location location2 = this.location;
        if (location2 != null) {
            endElementEvent.setLocation(location2);
        }
        return endElementEvent;
    }

    public EntityReference createEntityReference(String str, EntityDeclaration entityDeclaration) {
        EntityReferenceEvent entityReferenceEvent = new EntityReferenceEvent(str, entityDeclaration);
        Location location2 = this.location;
        if (location2 != null) {
            entityReferenceEvent.setLocation(location2);
        }
        return entityReferenceEvent;
    }

    public Characters createIgnorableSpace(String str) {
        CharacterEvent characterEvent = new CharacterEvent(str, false, true);
        Location location2 = this.location;
        if (location2 != null) {
            characterEvent.setLocation(location2);
        }
        return characterEvent;
    }

    public Namespace createNamespace(String str) {
        NamespaceImpl namespaceImpl = new NamespaceImpl(str);
        Location location2 = this.location;
        if (location2 != null) {
            namespaceImpl.setLocation(location2);
        }
        return namespaceImpl;
    }

    public Namespace createNamespace(String str, String str2) {
        NamespaceImpl namespaceImpl = new NamespaceImpl(str, str2);
        Location location2 = this.location;
        if (location2 != null) {
            namespaceImpl.setLocation(location2);
        }
        return namespaceImpl;
    }

    public ProcessingInstruction createProcessingInstruction(String str, String str2) {
        ProcessingInstructionEvent processingInstructionEvent = new ProcessingInstructionEvent(str, str2);
        Location location2 = this.location;
        if (location2 != null) {
            processingInstructionEvent.setLocation(location2);
        }
        return processingInstructionEvent;
    }

    public Characters createSpace(String str) {
        CharacterEvent characterEvent = new CharacterEvent(str);
        Location location2 = this.location;
        if (location2 != null) {
            characterEvent.setLocation(location2);
        }
        return characterEvent;
    }

    public StartDocument createStartDocument() {
        StartDocumentEvent startDocumentEvent = new StartDocumentEvent();
        Location location2 = this.location;
        if (location2 != null) {
            startDocumentEvent.setLocation(location2);
        }
        return startDocumentEvent;
    }

    public StartDocument createStartDocument(String str) {
        StartDocumentEvent startDocumentEvent = new StartDocumentEvent(str);
        Location location2 = this.location;
        if (location2 != null) {
            startDocumentEvent.setLocation(location2);
        }
        return startDocumentEvent;
    }

    public StartDocument createStartDocument(String str, String str2) {
        StartDocumentEvent startDocumentEvent = new StartDocumentEvent(str, str2);
        Location location2 = this.location;
        if (location2 != null) {
            startDocumentEvent.setLocation(location2);
        }
        return startDocumentEvent;
    }

    public StartDocument createStartDocument(String str, String str2, boolean z) {
        StartDocumentEvent startDocumentEvent = new StartDocumentEvent(str, str2, z);
        Location location2 = this.location;
        if (location2 != null) {
            startDocumentEvent.setLocation(location2);
        }
        return startDocumentEvent;
    }

    public StartElement createStartElement(QName qName, Iterator it, Iterator it2) {
        return createStartElement(qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart(), it, it2);
    }

    public StartElement createStartElement(String str, String str2, String str3) {
        StartElementEvent startElementEvent = new StartElementEvent(str, str2, str3);
        Location location2 = this.location;
        if (location2 != null) {
            startElementEvent.setLocation(location2);
        }
        return startElementEvent;
    }

    public StartElement createStartElement(String str, String str2, String str3, Iterator it, Iterator it2) {
        return createStartElement(str, str2, str3, it, it2, null);
    }

    public StartElement createStartElement(String str, String str2, String str3, Iterator it, Iterator it2, NamespaceContext namespaceContext) {
        StartElementEvent startElementEvent = new StartElementEvent(str, str2, str3);
        startElementEvent.addAttributes(it);
        startElementEvent.addNamespaceAttributes(it2);
        startElementEvent.setNamespaceContext(namespaceContext);
        Location location2 = this.location;
        if (location2 != null) {
            startElementEvent.setLocation(location2);
        }
        return startElementEvent;
    }

    public void setLocation(Location location2) {
        this.location = location2;
    }
}
