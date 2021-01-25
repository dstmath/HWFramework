package ohos.javax.xml.stream;

import java.io.OutputStream;
import java.io.Writer;
import ohos.javax.xml.transform.Result;

public abstract class XMLOutputFactory {
    static final String DEFAULIMPL = "ohos.com.sun.xml.internal.stream.XMLOutputFactoryImpl";
    public static final String IS_REPAIRING_NAMESPACES = "javax.xml.stream.isRepairingNamespaces";

    public abstract XMLEventWriter createXMLEventWriter(OutputStream outputStream) throws XMLStreamException;

    public abstract XMLEventWriter createXMLEventWriter(OutputStream outputStream, String str) throws XMLStreamException;

    public abstract XMLEventWriter createXMLEventWriter(Writer writer) throws XMLStreamException;

    public abstract XMLEventWriter createXMLEventWriter(Result result) throws XMLStreamException;

    public abstract XMLStreamWriter createXMLStreamWriter(OutputStream outputStream) throws XMLStreamException;

    public abstract XMLStreamWriter createXMLStreamWriter(OutputStream outputStream, String str) throws XMLStreamException;

    public abstract XMLStreamWriter createXMLStreamWriter(Writer writer) throws XMLStreamException;

    public abstract XMLStreamWriter createXMLStreamWriter(Result result) throws XMLStreamException;

    public abstract Object getProperty(String str) throws IllegalArgumentException;

    public abstract boolean isPropertySupported(String str);

    public abstract void setProperty(String str, Object obj) throws IllegalArgumentException;

    protected XMLOutputFactory() {
    }

    public static XMLOutputFactory newInstance() throws FactoryConfigurationError {
        return (XMLOutputFactory) FactoryFinder.find(XMLOutputFactory.class, DEFAULIMPL);
    }

    public static XMLOutputFactory newFactory() throws FactoryConfigurationError {
        return (XMLOutputFactory) FactoryFinder.find(XMLOutputFactory.class, DEFAULIMPL);
    }

    public static XMLInputFactory newInstance(String str, ClassLoader classLoader) throws FactoryConfigurationError {
        return (XMLInputFactory) FactoryFinder.find(XMLInputFactory.class, str, classLoader, null);
    }

    public static XMLOutputFactory newFactory(String str, ClassLoader classLoader) throws FactoryConfigurationError {
        return (XMLOutputFactory) FactoryFinder.find(XMLOutputFactory.class, str, classLoader, null);
    }
}
