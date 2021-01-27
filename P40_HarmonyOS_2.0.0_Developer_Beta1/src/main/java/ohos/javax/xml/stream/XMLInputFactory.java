package ohos.javax.xml.stream;

import java.io.InputStream;
import java.io.Reader;
import ohos.javax.xml.stream.util.XMLEventAllocator;
import ohos.javax.xml.transform.Source;

public abstract class XMLInputFactory {
    public static final String ALLOCATOR = "javax.xml.stream.allocator";
    static final String DEFAULIMPL = "ohos.com.sun.xml.internal.stream.XMLInputFactoryImpl";
    public static final String IS_COALESCING = "javax.xml.stream.isCoalescing";
    public static final String IS_NAMESPACE_AWARE = "javax.xml.stream.isNamespaceAware";
    public static final String IS_REPLACING_ENTITY_REFERENCES = "javax.xml.stream.isReplacingEntityReferences";
    public static final String IS_SUPPORTING_EXTERNAL_ENTITIES = "javax.xml.stream.isSupportingExternalEntities";
    public static final String IS_VALIDATING = "javax.xml.stream.isValidating";
    public static final String REPORTER = "javax.xml.stream.reporter";
    public static final String RESOLVER = "javax.xml.stream.resolver";
    public static final String SUPPORT_DTD = "javax.xml.stream.supportDTD";

    public abstract XMLEventReader createFilteredReader(XMLEventReader xMLEventReader, EventFilter eventFilter) throws XMLStreamException;

    public abstract XMLStreamReader createFilteredReader(XMLStreamReader xMLStreamReader, StreamFilter streamFilter) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(InputStream inputStream) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(InputStream inputStream, String str) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(Reader reader) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(String str, InputStream inputStream) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(String str, Reader reader) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(XMLStreamReader xMLStreamReader) throws XMLStreamException;

    public abstract XMLEventReader createXMLEventReader(Source source) throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(InputStream inputStream) throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(InputStream inputStream, String str) throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(Reader reader) throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(String str, InputStream inputStream) throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(String str, Reader reader) throws XMLStreamException;

    public abstract XMLStreamReader createXMLStreamReader(Source source) throws XMLStreamException;

    public abstract XMLEventAllocator getEventAllocator();

    public abstract Object getProperty(String str) throws IllegalArgumentException;

    public abstract XMLReporter getXMLReporter();

    public abstract XMLResolver getXMLResolver();

    public abstract boolean isPropertySupported(String str);

    public abstract void setEventAllocator(XMLEventAllocator xMLEventAllocator);

    public abstract void setProperty(String str, Object obj) throws IllegalArgumentException;

    public abstract void setXMLReporter(XMLReporter xMLReporter);

    public abstract void setXMLResolver(XMLResolver xMLResolver);

    protected XMLInputFactory() {
    }

    public static XMLInputFactory newInstance() throws FactoryConfigurationError {
        return (XMLInputFactory) FactoryFinder.find(XMLInputFactory.class, DEFAULIMPL);
    }

    public static XMLInputFactory newFactory() throws FactoryConfigurationError {
        return (XMLInputFactory) FactoryFinder.find(XMLInputFactory.class, DEFAULIMPL);
    }

    public static XMLInputFactory newInstance(String str, ClassLoader classLoader) throws FactoryConfigurationError {
        return (XMLInputFactory) FactoryFinder.find(XMLInputFactory.class, str, classLoader, null);
    }

    public static XMLInputFactory newFactory(String str, ClassLoader classLoader) throws FactoryConfigurationError {
        return (XMLInputFactory) FactoryFinder.find(XMLInputFactory.class, str, classLoader, null);
    }
}
