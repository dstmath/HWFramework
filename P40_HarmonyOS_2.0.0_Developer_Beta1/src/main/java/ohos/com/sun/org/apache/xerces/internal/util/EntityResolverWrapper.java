package ohos.com.sun.org.apache.xerces.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;

public class EntityResolverWrapper implements XMLEntityResolver {
    protected EntityResolver fEntityResolver;

    public EntityResolverWrapper() {
    }

    public EntityResolverWrapper(EntityResolver entityResolver) {
        setEntityResolver(entityResolver);
    }

    public void setEntityResolver(EntityResolver entityResolver) {
        this.fEntityResolver = entityResolver;
    }

    public EntityResolver getEntityResolver() {
        return this.fEntityResolver;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver
    public XMLInputSource resolveEntity(XMLResourceIdentifier xMLResourceIdentifier) throws XNIException, IOException {
        EntityResolver entityResolver;
        String publicId = xMLResourceIdentifier.getPublicId();
        String expandedSystemId = xMLResourceIdentifier.getExpandedSystemId();
        if (!((publicId == null && expandedSystemId == null) || (entityResolver = this.fEntityResolver) == null)) {
            try {
                InputSource resolveEntity = entityResolver.resolveEntity(publicId, expandedSystemId);
                if (resolveEntity != null) {
                    String publicId2 = resolveEntity.getPublicId();
                    String systemId = resolveEntity.getSystemId();
                    String baseSystemId = xMLResourceIdentifier.getBaseSystemId();
                    InputStream byteStream = resolveEntity.getByteStream();
                    Reader characterStream = resolveEntity.getCharacterStream();
                    String encoding = resolveEntity.getEncoding();
                    XMLInputSource xMLInputSource = new XMLInputSource(publicId2, systemId, baseSystemId);
                    xMLInputSource.setByteStream(byteStream);
                    xMLInputSource.setCharacterStream(characterStream);
                    xMLInputSource.setEncoding(encoding);
                    return xMLInputSource;
                }
            } catch (SAXException e) {
                e = e;
                Exception exception = e.getException();
                if (exception != null) {
                    e = exception;
                }
                throw new XNIException(e);
            }
        }
        return null;
    }
}
