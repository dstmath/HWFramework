package ohos.javax.xml.stream;

import java.util.Iterator;
import ohos.javax.xml.namespace.NamespaceContext;
import ohos.javax.xml.namespace.QName;
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

public abstract class XMLEventFactory {
    static final String DEFAULIMPL = "ohos.com.sun.xml.internal.stream.events.XMLEventFactoryImpl";
    static final String JAXPFACTORYID = "ohos.javax.xml.stream.XMLEventFactory";

    public abstract Attribute createAttribute(String str, String str2);

    public abstract Attribute createAttribute(String str, String str2, String str3, String str4);

    public abstract Attribute createAttribute(QName qName, String str);

    public abstract Characters createCData(String str);

    public abstract Characters createCharacters(String str);

    public abstract Comment createComment(String str);

    public abstract DTD createDTD(String str);

    public abstract EndDocument createEndDocument();

    public abstract EndElement createEndElement(String str, String str2, String str3);

    public abstract EndElement createEndElement(String str, String str2, String str3, Iterator it);

    public abstract EndElement createEndElement(QName qName, Iterator it);

    public abstract EntityReference createEntityReference(String str, EntityDeclaration entityDeclaration);

    public abstract Characters createIgnorableSpace(String str);

    public abstract Namespace createNamespace(String str);

    public abstract Namespace createNamespace(String str, String str2);

    public abstract ProcessingInstruction createProcessingInstruction(String str, String str2);

    public abstract Characters createSpace(String str);

    public abstract StartDocument createStartDocument();

    public abstract StartDocument createStartDocument(String str);

    public abstract StartDocument createStartDocument(String str, String str2);

    public abstract StartDocument createStartDocument(String str, String str2, boolean z);

    public abstract StartElement createStartElement(String str, String str2, String str3);

    public abstract StartElement createStartElement(String str, String str2, String str3, Iterator it, Iterator it2);

    public abstract StartElement createStartElement(String str, String str2, String str3, Iterator it, Iterator it2, NamespaceContext namespaceContext);

    public abstract StartElement createStartElement(QName qName, Iterator it, Iterator it2);

    public abstract void setLocation(Location location);

    protected XMLEventFactory() {
    }

    public static XMLEventFactory newInstance() throws FactoryConfigurationError {
        return (XMLEventFactory) FactoryFinder.find(XMLEventFactory.class, DEFAULIMPL);
    }

    public static XMLEventFactory newFactory() throws FactoryConfigurationError {
        return (XMLEventFactory) FactoryFinder.find(XMLEventFactory.class, DEFAULIMPL);
    }

    public static XMLEventFactory newInstance(String str, ClassLoader classLoader) throws FactoryConfigurationError {
        return (XMLEventFactory) FactoryFinder.find(XMLEventFactory.class, str, classLoader, null);
    }

    public static XMLEventFactory newFactory(String str, ClassLoader classLoader) throws FactoryConfigurationError {
        return (XMLEventFactory) FactoryFinder.find(XMLEventFactory.class, str, classLoader, null);
    }
}
