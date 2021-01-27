package ohos.com.sun.org.apache.xerces.internal.impl.xs.opti;

import java.io.IOException;
import ohos.com.sun.org.apache.xerces.internal.impl.XMLErrorReporter;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import ohos.com.sun.org.apache.xerces.internal.impl.xs.XSMessageFormatter;
import ohos.com.sun.org.apache.xerces.internal.util.XMLAttributesImpl;
import ohos.com.sun.org.apache.xerces.internal.util.XMLChar;
import ohos.com.sun.org.apache.xerces.internal.xni.Augmentations;
import ohos.com.sun.org.apache.xerces.internal.xni.NamespaceContext;
import ohos.com.sun.org.apache.xerces.internal.xni.QName;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLAttributes;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLLocator;
import ohos.com.sun.org.apache.xerces.internal.xni.XMLString;
import ohos.com.sun.org.apache.xerces.internal.xni.XNIException;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLEntityResolver;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLInputSource;
import ohos.com.sun.org.apache.xerces.internal.xni.parser.XMLParserConfiguration;
import ohos.org.w3c.dom.Document;

public class SchemaDOMParser extends DefaultXMLDocumentHandler {
    public static final String ERROR_REPORTER = "http://apache.org/xml/properties/internal/error-reporter";
    public static final String GENERATE_SYNTHETIC_ANNOTATION = "http://apache.org/xml/features/generate-synthetic-annotations";
    XMLParserConfiguration config;
    private int fAnnotationDepth = -1;
    private ElementImpl fCurrentAnnotationElement;
    private int fDepth = -1;
    private XMLAttributes fEmptyAttr = new XMLAttributesImpl();
    XMLErrorReporter fErrorReporter;
    private boolean fGenerateSyntheticAnnotation = false;
    private BooleanStack fHasNonSchemaAttributes = new BooleanStack();
    private int fInnerAnnotationDepth = -1;
    protected XMLLocator fLocator;
    protected NamespaceContext fNamespaceContext = null;
    private BooleanStack fSawAnnotation = new BooleanStack();
    SchemaDOM schemaDOM;

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endDocument(Augmentations augmentations) throws XNIException {
    }

    public SchemaDOMParser(XMLParserConfiguration xMLParserConfiguration) {
        this.config = xMLParserConfiguration;
        xMLParserConfiguration.setDocumentHandler(this);
        xMLParserConfiguration.setDTDHandler(this);
        xMLParserConfiguration.setDTDContentModelHandler(this);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startDocument(XMLLocator xMLLocator, String str, NamespaceContext namespaceContext, Augmentations augmentations) throws XNIException {
        this.fErrorReporter = (XMLErrorReporter) this.config.getProperty("http://apache.org/xml/properties/internal/error-reporter");
        this.fGenerateSyntheticAnnotation = this.config.getFeature(GENERATE_SYNTHETIC_ANNOTATION);
        this.fHasNonSchemaAttributes.clear();
        this.fSawAnnotation.clear();
        this.schemaDOM = new SchemaDOM();
        this.fCurrentAnnotationElement = null;
        this.fAnnotationDepth = -1;
        this.fInnerAnnotationDepth = -1;
        this.fDepth = -1;
        this.fLocator = xMLLocator;
        this.fNamespaceContext = namespaceContext;
        this.schemaDOM.setDocumentURI(xMLLocator.getExpandedSystemId());
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void comment(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (this.fAnnotationDepth > -1) {
            this.schemaDOM.comment(xMLString);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void processingInstruction(String str, XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (this.fAnnotationDepth > -1) {
            this.schemaDOM.processingInstruction(str, xMLString);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void characters(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (this.fInnerAnnotationDepth == -1) {
            for (int i = xMLString.offset; i < xMLString.offset + xMLString.length; i++) {
                if (!XMLChar.isSpace(xMLString.ch[i])) {
                    this.fErrorReporter.reportError(this.fLocator, XSMessageFormatter.SCHEMA_DOMAIN, "s4s-elt-character", new Object[]{new String(xMLString.ch, i, (xMLString.length + xMLString.offset) - i)}, (short) 1);
                    return;
                }
            }
            return;
        }
        this.schemaDOM.characters(xMLString);
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        this.fDepth++;
        int i = this.fAnnotationDepth;
        if (i != -1) {
            int i2 = this.fDepth;
            if (i2 == i + 1) {
                this.fInnerAnnotationDepth = i2;
                this.schemaDOM.startAnnotationElement(qName, xMLAttributes);
            } else {
                this.schemaDOM.startAnnotationElement(qName, xMLAttributes);
                return;
            }
        } else if (qName.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA && qName.localpart == SchemaSymbols.ELT_ANNOTATION) {
            if (this.fGenerateSyntheticAnnotation) {
                if (this.fSawAnnotation.size() > 0) {
                    this.fSawAnnotation.pop();
                }
                this.fSawAnnotation.push(true);
            }
            this.fAnnotationDepth = this.fDepth;
            this.schemaDOM.startAnnotation(qName, xMLAttributes, this.fNamespaceContext);
            this.fCurrentAnnotationElement = this.schemaDOM.startElement(qName, xMLAttributes, this.fLocator.getLineNumber(), this.fLocator.getColumnNumber(), this.fLocator.getCharacterOffset());
            return;
        } else if (qName.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA && this.fGenerateSyntheticAnnotation) {
            this.fSawAnnotation.push(false);
            this.fHasNonSchemaAttributes.push(hasNonSchemaAttributes(qName, xMLAttributes));
        }
        this.schemaDOM.startElement(qName, xMLAttributes, this.fLocator.getLineNumber(), this.fLocator.getColumnNumber(), this.fLocator.getCharacterOffset());
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void emptyElement(QName qName, XMLAttributes xMLAttributes, Augmentations augmentations) throws XNIException {
        String str;
        String str2;
        if (!this.fGenerateSyntheticAnnotation || this.fAnnotationDepth != -1 || qName.uri != SchemaSymbols.URI_SCHEMAFORSCHEMA || qName.localpart == SchemaSymbols.ELT_ANNOTATION || !hasNonSchemaAttributes(qName, xMLAttributes)) {
            if (this.fAnnotationDepth != -1) {
                this.schemaDOM.startAnnotationElement(qName, xMLAttributes);
            } else if (qName.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA && qName.localpart == SchemaSymbols.ELT_ANNOTATION) {
                this.schemaDOM.startAnnotation(qName, xMLAttributes, this.fNamespaceContext);
            }
            ElementImpl emptyElement = this.schemaDOM.emptyElement(qName, xMLAttributes, this.fLocator.getLineNumber(), this.fLocator.getColumnNumber(), this.fLocator.getCharacterOffset());
            if (this.fAnnotationDepth != -1) {
                this.schemaDOM.endAnnotationElement(qName);
            } else if (qName.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA && qName.localpart == SchemaSymbols.ELT_ANNOTATION) {
                this.schemaDOM.endAnnotation(qName, emptyElement);
            }
        } else {
            this.schemaDOM.startElement(qName, xMLAttributes, this.fLocator.getLineNumber(), this.fLocator.getColumnNumber(), this.fLocator.getCharacterOffset());
            xMLAttributes.removeAllAttributes();
            String prefix = this.fNamespaceContext.getPrefix(SchemaSymbols.URI_SCHEMAFORSCHEMA);
            if (prefix.length() == 0) {
                str = SchemaSymbols.ELT_ANNOTATION;
            } else {
                str = prefix + ':' + SchemaSymbols.ELT_ANNOTATION;
            }
            this.schemaDOM.startAnnotation(str, xMLAttributes, this.fNamespaceContext);
            if (prefix.length() == 0) {
                str2 = SchemaSymbols.ELT_DOCUMENTATION;
            } else {
                str2 = prefix + ':' + SchemaSymbols.ELT_DOCUMENTATION;
            }
            this.schemaDOM.startAnnotationElement(str2, xMLAttributes);
            this.schemaDOM.charactersRaw("SYNTHETIC_ANNOTATION");
            this.schemaDOM.endSyntheticAnnotationElement(str2, false);
            this.schemaDOM.endSyntheticAnnotationElement(str, true);
            this.schemaDOM.endElement();
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endElement(QName qName, Augmentations augmentations) throws XNIException {
        String str;
        String str2;
        int i = this.fAnnotationDepth;
        if (i > -1) {
            int i2 = this.fInnerAnnotationDepth;
            int i3 = this.fDepth;
            if (i2 == i3) {
                this.fInnerAnnotationDepth = -1;
                this.schemaDOM.endAnnotationElement(qName);
                this.schemaDOM.endElement();
            } else if (i == i3) {
                this.fAnnotationDepth = -1;
                this.schemaDOM.endAnnotation(qName, this.fCurrentAnnotationElement);
                this.schemaDOM.endElement();
            } else {
                this.schemaDOM.endAnnotationElement(qName);
            }
        } else {
            if (qName.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA && this.fGenerateSyntheticAnnotation) {
                boolean pop = this.fHasNonSchemaAttributes.pop();
                boolean pop2 = this.fSawAnnotation.pop();
                if (pop && !pop2) {
                    String prefix = this.fNamespaceContext.getPrefix(SchemaSymbols.URI_SCHEMAFORSCHEMA);
                    if (prefix.length() == 0) {
                        str = SchemaSymbols.ELT_ANNOTATION;
                    } else {
                        str = prefix + ':' + SchemaSymbols.ELT_ANNOTATION;
                    }
                    this.schemaDOM.startAnnotation(str, this.fEmptyAttr, this.fNamespaceContext);
                    if (prefix.length() == 0) {
                        str2 = SchemaSymbols.ELT_DOCUMENTATION;
                    } else {
                        str2 = prefix + ':' + SchemaSymbols.ELT_DOCUMENTATION;
                    }
                    this.schemaDOM.startAnnotationElement(str2, this.fEmptyAttr);
                    this.schemaDOM.charactersRaw("SYNTHETIC_ANNOTATION");
                    this.schemaDOM.endSyntheticAnnotationElement(str2, false);
                    this.schemaDOM.endSyntheticAnnotationElement(str, true);
                }
            }
            this.schemaDOM.endElement();
        }
        this.fDepth--;
    }

    private boolean hasNonSchemaAttributes(QName qName, XMLAttributes xMLAttributes) {
        int length = xMLAttributes.getLength();
        for (int i = 0; i < length; i++) {
            String uri = xMLAttributes.getURI(i);
            if (!(uri == null || uri == SchemaSymbols.URI_SCHEMAFORSCHEMA || uri == NamespaceContext.XMLNS_URI || (uri == NamespaceContext.XML_URI && xMLAttributes.getQName(i) == SchemaSymbols.ATT_XML_LANG && qName.localpart == SchemaSymbols.ELT_SCHEMA))) {
                return true;
            }
        }
        return false;
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void ignorableWhitespace(XMLString xMLString, Augmentations augmentations) throws XNIException {
        if (this.fAnnotationDepth != -1) {
            this.schemaDOM.characters(xMLString);
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void startCDATA(Augmentations augmentations) throws XNIException {
        if (this.fAnnotationDepth != -1) {
            this.schemaDOM.startAnnotationCDATA();
        }
    }

    @Override // ohos.com.sun.org.apache.xerces.internal.impl.xs.opti.DefaultXMLDocumentHandler, ohos.com.sun.org.apache.xerces.internal.xni.XMLDocumentHandler
    public void endCDATA(Augmentations augmentations) throws XNIException {
        if (this.fAnnotationDepth != -1) {
            this.schemaDOM.endAnnotationCDATA();
        }
    }

    public Document getDocument() {
        return this.schemaDOM;
    }

    public void setFeature(String str, boolean z) {
        this.config.setFeature(str, z);
    }

    public boolean getFeature(String str) {
        return this.config.getFeature(str);
    }

    public void setProperty(String str, Object obj) {
        this.config.setProperty(str, obj);
    }

    public Object getProperty(String str) {
        return this.config.getProperty(str);
    }

    public void setEntityResolver(XMLEntityResolver xMLEntityResolver) {
        this.config.setEntityResolver(xMLEntityResolver);
    }

    public void parse(XMLInputSource xMLInputSource) throws IOException {
        this.config.parse(xMLInputSource);
    }

    public void reset() {
        ((SchemaParsingConfig) this.config).reset();
    }

    public void resetNodePool() {
        ((SchemaParsingConfig) this.config).resetNodePool();
    }

    private static final class BooleanStack {
        private boolean[] fData;
        private int fDepth;

        public int size() {
            return this.fDepth;
        }

        public void push(boolean z) {
            ensureCapacity(this.fDepth + 1);
            boolean[] zArr = this.fData;
            int i = this.fDepth;
            this.fDepth = i + 1;
            zArr[i] = z;
        }

        public boolean pop() {
            boolean[] zArr = this.fData;
            int i = this.fDepth - 1;
            this.fDepth = i;
            return zArr[i];
        }

        public void clear() {
            this.fDepth = 0;
        }

        private void ensureCapacity(int i) {
            boolean[] zArr = this.fData;
            if (zArr == null) {
                this.fData = new boolean[32];
            } else if (zArr.length <= i) {
                boolean[] zArr2 = new boolean[(zArr.length * 2)];
                System.arraycopy(zArr, 0, zArr2, 0, zArr.length);
                this.fData = zArr2;
            }
        }
    }
}
