package ohos.com.sun.org.apache.xerces.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLResourceIdentifier;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.grammars.XMLGrammarDescription;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.org.w3c.dom.ls.LSInput;
import ohos.org.w3c.dom.ls.LSResourceResolver;

public class DOMEntityResolverWrapper implements XMLEntityResolver {
    private static final String XML_TYPE = "http://www.w3.org/TR/REC-xml";
    private static final String XSD_TYPE = "http://www.w3.org/2001/XMLSchema";
    protected LSResourceResolver fEntityResolver;

    public DOMEntityResolverWrapper() {
    }

    public DOMEntityResolverWrapper(LSResourceResolver lSResourceResolver) {
        setEntityResolver(lSResourceResolver);
    }

    public void setEntityResolver(LSResourceResolver lSResourceResolver) {
        this.fEntityResolver = lSResourceResolver;
    }

    public LSResourceResolver getEntityResolver() {
        return this.fEntityResolver;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver
    public XMLInputSource resolveEntity(XMLResourceIdentifier xMLResourceIdentifier) throws XNIException, IOException {
        LSInput lSInput;
        LSResourceResolver lSResourceResolver = this.fEntityResolver;
        if (lSResourceResolver == null) {
            return null;
        }
        if (xMLResourceIdentifier == null) {
            lSInput = lSResourceResolver.resolveResource((String) null, (String) null, (String) null, (String) null, (String) null);
        } else {
            lSInput = lSResourceResolver.resolveResource(getType(xMLResourceIdentifier), xMLResourceIdentifier.getNamespace(), xMLResourceIdentifier.getPublicId(), xMLResourceIdentifier.getLiteralSystemId(), xMLResourceIdentifier.getBaseSystemId());
        }
        if (lSInput == null) {
            return null;
        }
        String publicId = lSInput.getPublicId();
        String systemId = lSInput.getSystemId();
        String baseURI = lSInput.getBaseURI();
        InputStream byteStream = lSInput.getByteStream();
        Reader characterStream = lSInput.getCharacterStream();
        String encoding = lSInput.getEncoding();
        String stringData = lSInput.getStringData();
        XMLInputSource xMLInputSource = new XMLInputSource(publicId, systemId, baseURI);
        if (characterStream != null) {
            xMLInputSource.setCharacterStream(characterStream);
        } else if (byteStream != null) {
            xMLInputSource.setByteStream(byteStream);
        } else if (!(stringData == null || stringData.length() == 0)) {
            xMLInputSource.setCharacterStream(new StringReader(stringData));
        }
        xMLInputSource.setEncoding(encoding);
        return xMLInputSource;
    }

    private String getType(XMLResourceIdentifier xMLResourceIdentifier) {
        return (!(xMLResourceIdentifier instanceof XMLGrammarDescription) || !"http://www.w3.org/2001/XMLSchema".equals(((XMLGrammarDescription) xMLResourceIdentifier).getGrammarType())) ? "http://www.w3.org/TR/REC-xml" : "http://www.w3.org/2001/XMLSchema";
    }
}
