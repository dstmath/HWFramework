package ohos.com.sun.xml.internal.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import ohos.com.sun.org.apache.xerces.internal.impl.Constants;
import ohos.com.sun.org.apache.xerces.internal.impl.PropertyManager;
import ohos.com.sun.xml.internal.stream.writers.XMLDOMWriterImpl;
import ohos.com.sun.xml.internal.stream.writers.XMLEventWriterImpl;
import ohos.com.sun.xml.internal.stream.writers.XMLStreamWriterImpl;
import ohos.javax.xml.stream.XMLEventWriter;
import ohos.javax.xml.stream.XMLOutputFactory;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamWriter;
import ohos.javax.xml.transform.Result;
import ohos.javax.xml.transform.dom.DOMResult;
import ohos.javax.xml.transform.stax.StAXResult;
import ohos.javax.xml.transform.stream.StreamResult;

public class XMLOutputFactoryImpl extends XMLOutputFactory {
    private static final boolean DEBUG = false;
    private boolean fPropertyChanged;
    private PropertyManager fPropertyManager = new PropertyManager(2);
    boolean fReuseInstance = false;
    private XMLStreamWriterImpl fStreamWriter = null;

    public XMLEventWriter createXMLEventWriter(OutputStream outputStream) throws XMLStreamException {
        return createXMLEventWriter(outputStream, null);
    }

    public XMLEventWriter createXMLEventWriter(OutputStream outputStream, String str) throws XMLStreamException {
        return new XMLEventWriterImpl(createXMLStreamWriter(outputStream, str));
    }

    public XMLEventWriter createXMLEventWriter(Result result) throws XMLStreamException {
        if (result instanceof StAXResult) {
            StAXResult stAXResult = (StAXResult) result;
            if (stAXResult.getXMLEventWriter() != null) {
                return stAXResult.getXMLEventWriter();
            }
        }
        return new XMLEventWriterImpl(createXMLStreamWriter(result));
    }

    public XMLEventWriter createXMLEventWriter(Writer writer) throws XMLStreamException {
        return new XMLEventWriterImpl(createXMLStreamWriter(writer));
    }

    public XMLStreamWriter createXMLStreamWriter(Result result) throws XMLStreamException {
        if (result instanceof StreamResult) {
            return createXMLStreamWriter((StreamResult) result, (String) null);
        }
        if (result instanceof DOMResult) {
            return new XMLDOMWriterImpl((DOMResult) result);
        }
        if (result instanceof StAXResult) {
            StAXResult stAXResult = (StAXResult) result;
            if (stAXResult.getXMLStreamWriter() != null) {
                return stAXResult.getXMLStreamWriter();
            }
            throw new UnsupportedOperationException("Result of type " + result + " is not supported");
        } else if (result.getSystemId() != null) {
            return createXMLStreamWriter((Result) new StreamResult(result.getSystemId()));
        } else {
            throw new UnsupportedOperationException("Result of type " + result + " is not supported. Supported result types are: DOMResult, StAXResult and StreamResult.");
        }
    }

    public XMLStreamWriter createXMLStreamWriter(Writer writer) throws XMLStreamException {
        return createXMLStreamWriter(toStreamResult(null, writer, null), (String) null);
    }

    public XMLStreamWriter createXMLStreamWriter(OutputStream outputStream) throws XMLStreamException {
        return createXMLStreamWriter(outputStream, (String) null);
    }

    public XMLStreamWriter createXMLStreamWriter(OutputStream outputStream, String str) throws XMLStreamException {
        return createXMLStreamWriter(toStreamResult(outputStream, null, null), str);
    }

    public Object getProperty(String str) throws IllegalArgumentException {
        if (str == null) {
            throw new IllegalArgumentException("Property not supported");
        } else if (this.fPropertyManager.containsProperty(str)) {
            return this.fPropertyManager.getProperty(str);
        } else {
            throw new IllegalArgumentException("Property not supported");
        }
    }

    public boolean isPropertySupported(String str) {
        if (str == null) {
            return false;
        }
        return this.fPropertyManager.containsProperty(str);
    }

    public void setProperty(String str, Object obj) throws IllegalArgumentException {
        if (str == null || obj == null || !this.fPropertyManager.containsProperty(str)) {
            throw new IllegalArgumentException("Property " + str + "is not supported");
        }
        if (str == Constants.REUSE_INSTANCE || str.equals(Constants.REUSE_INSTANCE)) {
            this.fReuseInstance = ((Boolean) obj).booleanValue();
            if (this.fReuseInstance) {
                throw new IllegalArgumentException("Property " + str + " is not supported: XMLStreamWriters are not Thread safe");
            }
        } else {
            this.fPropertyChanged = true;
        }
        this.fPropertyManager.setProperty(str, obj);
    }

    /* access modifiers changed from: package-private */
    public StreamResult toStreamResult(OutputStream outputStream, Writer writer, String str) {
        StreamResult streamResult = new StreamResult();
        streamResult.setOutputStream(outputStream);
        streamResult.setWriter(writer);
        streamResult.setSystemId(str);
        return streamResult;
    }

    /* access modifiers changed from: package-private */
    public XMLStreamWriter createXMLStreamWriter(StreamResult streamResult, String str) throws XMLStreamException {
        try {
            if (!this.fReuseInstance || this.fStreamWriter == null || !this.fStreamWriter.canReuse() || this.fPropertyChanged) {
                XMLStreamWriterImpl xMLStreamWriterImpl = new XMLStreamWriterImpl(streamResult, str, new PropertyManager(this.fPropertyManager));
                this.fStreamWriter = xMLStreamWriterImpl;
                return xMLStreamWriterImpl;
            }
            this.fStreamWriter.reset();
            this.fStreamWriter.setOutput(streamResult, str);
            return this.fStreamWriter;
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }
}
