package ohos.com.sun.xml.internal.stream;

import java.io.IOException;
import java.io.InputStream;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.javax.xml.stream.XMLEventReader;
import ohos.javax.xml.stream.XMLResolver;
import ohos.javax.xml.stream.XMLStreamException;
import ohos.javax.xml.stream.XMLStreamReader;

public class StaxEntityResolverWrapper {
    XMLResolver fStaxResolver;

    public StaxEntityResolverWrapper(XMLResolver xMLResolver) {
        this.fStaxResolver = xMLResolver;
    }

    public void setStaxEntityResolver(XMLResolver xMLResolver) {
        this.fStaxResolver = xMLResolver;
    }

    public XMLResolver getStaxEntityResolver() {
        return this.fStaxResolver;
    }

    public StaxXMLInputSource resolveEntity(XMLResourceIdentifier xMLResourceIdentifier) throws XNIException, IOException {
        try {
            return getStaxInputSource(this.fStaxResolver.resolveEntity(xMLResourceIdentifier.getPublicId(), xMLResourceIdentifier.getLiteralSystemId(), xMLResourceIdentifier.getBaseSystemId(), (String) null));
        } catch (XMLStreamException e) {
            throw new XNIException((Exception) e);
        }
    }

    /* access modifiers changed from: package-private */
    public StaxXMLInputSource getStaxInputSource(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof InputStream) {
            return new StaxXMLInputSource(new XMLInputSource((String) null, (String) null, (String) null, (InputStream) obj, (String) null));
        }
        if (obj instanceof XMLStreamReader) {
            return new StaxXMLInputSource((XMLStreamReader) obj);
        }
        if (obj instanceof XMLEventReader) {
            return new StaxXMLInputSource((XMLEventReader) obj);
        }
        return null;
    }
}
