package ohos.com.sun.org.apache.xerces.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import ohos.com.sun.org.apache.xerces.internal.impl.ExternalSubsetResolver;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLEntityDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLDTDDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.ext.EntityResolver2;

public class EntityResolver2Wrapper implements ExternalSubsetResolver {
    protected EntityResolver2 fEntityResolver;

    public EntityResolver2Wrapper() {
    }

    public EntityResolver2Wrapper(EntityResolver2 entityResolver2) {
        setEntityResolver(entityResolver2);
    }

    public void setEntityResolver(EntityResolver2 entityResolver2) {
        this.fEntityResolver = entityResolver2;
    }

    public EntityResolver2 getEntityResolver() {
        return this.fEntityResolver;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.ExternalSubsetResolver
    public XMLInputSource getExternalSubset(XMLDTDDescription xMLDTDDescription) throws XNIException, IOException {
        if (this.fEntityResolver == null) {
            return null;
        }
        String rootName = xMLDTDDescription.getRootName();
        String baseSystemId = xMLDTDDescription.getBaseSystemId();
        try {
            InputSource externalSubset = this.fEntityResolver.getExternalSubset(rootName, baseSystemId);
            if (externalSubset != null) {
                return createXMLInputSource(externalSubset, baseSystemId);
            }
            return null;
        } catch (SAXException e) {
            e = e;
            Exception exception = e.getException();
            if (exception != null) {
                e = exception;
            }
            throw new XNIException(e);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver
    public XMLInputSource resolveEntity(XMLResourceIdentifier xMLResourceIdentifier) throws XNIException, IOException {
        String str;
        if (this.fEntityResolver == null) {
            return null;
        }
        String publicId = xMLResourceIdentifier.getPublicId();
        String literalSystemId = xMLResourceIdentifier.getLiteralSystemId();
        String baseSystemId = xMLResourceIdentifier.getBaseSystemId();
        if (xMLResourceIdentifier instanceof XMLDTDDescription) {
            str = "[dtd]";
        } else {
            str = xMLResourceIdentifier instanceof XMLEntityDescription ? ((XMLEntityDescription) xMLResourceIdentifier).getEntityName() : null;
        }
        if (publicId == null && literalSystemId == null) {
            return null;
        }
        try {
            InputSource resolveEntity = this.fEntityResolver.resolveEntity(str, publicId, baseSystemId, literalSystemId);
            if (resolveEntity != null) {
                return createXMLInputSource(resolveEntity, baseSystemId);
            }
            return null;
        } catch (SAXException e) {
            e = e;
            Exception exception = e.getException();
            if (exception != null) {
                e = exception;
            }
            throw new XNIException(e);
        }
    }

    private XMLInputSource createXMLInputSource(InputSource inputSource, String str) {
        String publicId = inputSource.getPublicId();
        String systemId = inputSource.getSystemId();
        InputStream byteStream = inputSource.getByteStream();
        Reader characterStream = inputSource.getCharacterStream();
        String encoding = inputSource.getEncoding();
        XMLInputSource xMLInputSource = new XMLInputSource(publicId, systemId, str);
        xMLInputSource.setByteStream(byteStream);
        xMLInputSource.setCharacterStream(characterStream);
        xMLInputSource.setEncoding(encoding);
        return xMLInputSource;
    }
}
