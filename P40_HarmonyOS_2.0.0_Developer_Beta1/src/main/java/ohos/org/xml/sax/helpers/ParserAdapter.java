package ohos.org.xml.sax.helpers;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import ohos.javax.xml.XMLConstants;
import ohos.org.xml.sax.AttributeList;
import ohos.org.xml.sax.Attributes;
import ohos.org.xml.sax.ContentHandler;
import ohos.org.xml.sax.DTDHandler;
import ohos.org.xml.sax.DocumentHandler;
import ohos.org.xml.sax.EntityResolver;
import ohos.org.xml.sax.ErrorHandler;
import ohos.org.xml.sax.InputSource;
import ohos.org.xml.sax.Locator;
import ohos.org.xml.sax.Parser;
import ohos.org.xml.sax.SAXException;
import ohos.org.xml.sax.SAXNotRecognizedException;
import ohos.org.xml.sax.SAXNotSupportedException;
import ohos.org.xml.sax.SAXParseException;
import ohos.org.xml.sax.XMLReader;

public class ParserAdapter implements XMLReader, DocumentHandler {
    private static final String FEATURES = "http://xml.org/sax/features/";
    private static final String NAMESPACES = "http://xml.org/sax/features/namespaces";
    private static final String NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";
    private static final String XMLNS_URIs = "http://xml.org/sax/features/xmlns-uris";
    private static SecuritySupport ss = new SecuritySupport();
    private AttributeListAdapter attAdapter;
    private AttributesImpl atts;
    ContentHandler contentHandler;
    DTDHandler dtdHandler;
    EntityResolver entityResolver;
    ErrorHandler errorHandler;
    Locator locator;
    private String[] nameParts;
    private boolean namespaces;
    private NamespaceSupport nsSupport;
    private Parser parser;
    private boolean parsing;
    private boolean prefixes;
    private boolean uris;

    public ParserAdapter() throws SAXException {
        this.parsing = false;
        this.nameParts = new String[3];
        this.parser = null;
        this.atts = null;
        this.namespaces = true;
        this.prefixes = false;
        this.uris = false;
        this.entityResolver = null;
        this.dtdHandler = null;
        this.contentHandler = null;
        this.errorHandler = null;
        String systemProperty = ss.getSystemProperty("org.xml.sax.parser");
        try {
            setup(ParserFactory.makeParser());
        } catch (ClassNotFoundException e) {
            throw new SAXException("Cannot find SAX1 driver class " + systemProperty, e);
        } catch (IllegalAccessException e2) {
            throw new SAXException("SAX1 driver class " + systemProperty + " found but cannot be loaded", e2);
        } catch (InstantiationException e3) {
            throw new SAXException("SAX1 driver class " + systemProperty + " loaded but cannot be instantiated", e3);
        } catch (ClassCastException unused) {
            throw new SAXException("SAX1 driver class " + systemProperty + " does not implement ohos.org.xml.sax.Parser");
        } catch (NullPointerException unused2) {
            throw new SAXException("System property org.xml.sax.parser not specified");
        }
    }

    public ParserAdapter(Parser parser2) {
        this.parsing = false;
        this.nameParts = new String[3];
        this.parser = null;
        this.atts = null;
        this.namespaces = true;
        this.prefixes = false;
        this.uris = false;
        this.entityResolver = null;
        this.dtdHandler = null;
        this.contentHandler = null;
        this.errorHandler = null;
        setup(parser2);
    }

    private void setup(Parser parser2) {
        if (parser2 != null) {
            this.parser = parser2;
            this.atts = new AttributesImpl();
            this.nsSupport = new NamespaceSupport();
            this.attAdapter = new AttributeListAdapter();
            return;
        }
        throw new NullPointerException("Parser argument must not be null");
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void setFeature(String str, boolean z) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str.equals("http://xml.org/sax/features/namespaces")) {
            checkNotParsing("feature", str);
            this.namespaces = z;
            if (!this.namespaces && !this.prefixes) {
                this.prefixes = true;
            }
        } else if (str.equals("http://xml.org/sax/features/namespace-prefixes")) {
            checkNotParsing("feature", str);
            this.prefixes = z;
            if (!this.prefixes && !this.namespaces) {
                this.namespaces = true;
            }
        } else if (str.equals(XMLNS_URIs)) {
            checkNotParsing("feature", str);
            this.uris = z;
        } else {
            throw new SAXNotRecognizedException("Feature: " + str);
        }
    }

    @Override // ohos.org.xml.sax.XMLReader
    public boolean getFeature(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (str.equals("http://xml.org/sax/features/namespaces")) {
            return this.namespaces;
        }
        if (str.equals("http://xml.org/sax/features/namespace-prefixes")) {
            return this.prefixes;
        }
        if (str.equals(XMLNS_URIs)) {
            return this.uris;
        }
        throw new SAXNotRecognizedException("Feature: " + str);
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void setProperty(String str, Object obj) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotRecognizedException("Property: " + str);
    }

    @Override // ohos.org.xml.sax.XMLReader
    public Object getProperty(String str) throws SAXNotRecognizedException, SAXNotSupportedException {
        throw new SAXNotRecognizedException("Property: " + str);
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void setEntityResolver(EntityResolver entityResolver2) {
        this.entityResolver = entityResolver2;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public EntityResolver getEntityResolver() {
        return this.entityResolver;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void setDTDHandler(DTDHandler dTDHandler) {
        this.dtdHandler = dTDHandler;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public DTDHandler getDTDHandler() {
        return this.dtdHandler;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void setContentHandler(ContentHandler contentHandler2) {
        this.contentHandler = contentHandler2;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public ContentHandler getContentHandler() {
        return this.contentHandler;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void setErrorHandler(ErrorHandler errorHandler2) {
        this.errorHandler = errorHandler2;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void parse(String str) throws IOException, SAXException {
        parse(new InputSource(str));
    }

    @Override // ohos.org.xml.sax.XMLReader
    public void parse(InputSource inputSource) throws IOException, SAXException {
        if (!this.parsing) {
            setupParser();
            this.parsing = true;
            try {
                this.parser.parse(inputSource);
                this.parsing = false;
            } finally {
                this.parsing = false;
            }
        } else {
            throw new SAXException("Parser is already in use");
        }
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void setDocumentLocator(Locator locator2) {
        this.locator = locator2;
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.setDocumentLocator(locator2);
        }
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void startDocument() throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.startDocument();
        }
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void endDocument() throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.endDocument();
        }
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void startElement(String str, AttributeList attributeList) throws SAXException {
        int i;
        String str2;
        String str3;
        if (this.namespaces) {
            this.nsSupport.pushContext();
            int length = attributeList.getLength();
            int i2 = 0;
            while (true) {
                i = -1;
                if (i2 >= length) {
                    break;
                }
                String name = attributeList.getName(i2);
                if (name.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
                    int indexOf = name.indexOf(58);
                    if (indexOf == -1 && name.length() == 5) {
                        str3 = "";
                    } else if (indexOf == 5) {
                        str3 = name.substring(indexOf + 1);
                    }
                    String value = attributeList.getValue(i2);
                    if (!this.nsSupport.declarePrefix(str3, value)) {
                        reportError("Illegal Namespace prefix: " + str3);
                    } else {
                        ContentHandler contentHandler2 = this.contentHandler;
                        if (contentHandler2 != null) {
                            contentHandler2.startPrefixMapping(str3, value);
                        }
                    }
                }
                i2++;
            }
            this.atts.clear();
            int i3 = 0;
            Vector vector = null;
            while (i3 < length) {
                String name2 = attributeList.getName(i3);
                String type = attributeList.getType(i3);
                String value2 = attributeList.getValue(i3);
                if (name2.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
                    int indexOf2 = name2.indexOf(58);
                    if (indexOf2 == i && name2.length() == 5) {
                        str2 = "";
                    } else if (indexOf2 != 5) {
                        str2 = null;
                    } else {
                        str2 = name2.substring(6);
                    }
                    if (str2 != null) {
                        if (this.prefixes) {
                            if (this.uris) {
                                AttributesImpl attributesImpl = this.atts;
                                NamespaceSupport namespaceSupport = this.nsSupport;
                                attributesImpl.addAttribute("http://www.w3.org/XML/1998/namespace", str2, name2.intern(), type, value2);
                            } else {
                                this.atts.addAttribute("", "", name2.intern(), type, value2);
                            }
                        }
                        i3++;
                        i = -1;
                    }
                }
                try {
                    String[] processName = processName(name2, true, true);
                    this.atts.addAttribute(processName[0], processName[1], processName[2], type, value2);
                } catch (SAXException e) {
                    if (vector == null) {
                        vector = new Vector();
                    }
                    vector.addElement(e);
                    this.atts.addAttribute("", name2, name2, type, value2);
                }
                i3++;
                i = -1;
            }
            if (!(vector == null || this.errorHandler == null)) {
                for (int i4 = 0; i4 < vector.size(); i4++) {
                    this.errorHandler.error((SAXParseException) vector.elementAt(i4));
                }
            }
            if (this.contentHandler != null) {
                String[] processName2 = processName(str, false, false);
                this.contentHandler.startElement(processName2[0], processName2[1], processName2[2], this.atts);
            }
        } else if (this.contentHandler != null) {
            this.attAdapter.setAttributeList(attributeList);
            this.contentHandler.startElement("", "", str.intern(), this.attAdapter);
        }
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void endElement(String str) throws SAXException {
        if (!this.namespaces) {
            ContentHandler contentHandler2 = this.contentHandler;
            if (contentHandler2 != null) {
                contentHandler2.endElement("", "", str.intern());
                return;
            }
            return;
        }
        String[] processName = processName(str, false, false);
        ContentHandler contentHandler3 = this.contentHandler;
        if (contentHandler3 != null) {
            contentHandler3.endElement(processName[0], processName[1], processName[2]);
            Enumeration declaredPrefixes = this.nsSupport.getDeclaredPrefixes();
            while (declaredPrefixes.hasMoreElements()) {
                this.contentHandler.endPrefixMapping((String) declaredPrefixes.nextElement());
            }
        }
        this.nsSupport.popContext();
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void characters(char[] cArr, int i, int i2) throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.characters(cArr, i, i2);
        }
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void ignorableWhitespace(char[] cArr, int i, int i2) throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.ignorableWhitespace(cArr, i, i2);
        }
    }

    @Override // ohos.org.xml.sax.DocumentHandler
    public void processingInstruction(String str, String str2) throws SAXException {
        ContentHandler contentHandler2 = this.contentHandler;
        if (contentHandler2 != null) {
            contentHandler2.processingInstruction(str, str2);
        }
    }

    private void setupParser() {
        if (this.prefixes || this.namespaces) {
            this.nsSupport.reset();
            if (this.uris) {
                this.nsSupport.setNamespaceDeclUris(true);
            }
            EntityResolver entityResolver2 = this.entityResolver;
            if (entityResolver2 != null) {
                this.parser.setEntityResolver(entityResolver2);
            }
            DTDHandler dTDHandler = this.dtdHandler;
            if (dTDHandler != null) {
                this.parser.setDTDHandler(dTDHandler);
            }
            ErrorHandler errorHandler2 = this.errorHandler;
            if (errorHandler2 != null) {
                this.parser.setErrorHandler(errorHandler2);
            }
            this.parser.setDocumentHandler(this);
            this.locator = null;
            return;
        }
        throw new IllegalStateException();
    }

    private String[] processName(String str, boolean z, boolean z2) throws SAXException {
        String[] processName = this.nsSupport.processName(str, this.nameParts, z);
        if (processName != null) {
            return processName;
        }
        if (!z2) {
            reportError("Undeclared prefix: " + str);
            String[] strArr = new String[3];
            strArr[1] = "";
            strArr[0] = "";
            strArr[2] = str.intern();
            return strArr;
        }
        throw makeException("Undeclared prefix: " + str);
    }

    /* access modifiers changed from: package-private */
    public void reportError(String str) throws SAXException {
        ErrorHandler errorHandler2 = this.errorHandler;
        if (errorHandler2 != null) {
            errorHandler2.error(makeException(str));
        }
    }

    private SAXParseException makeException(String str) {
        Locator locator2 = this.locator;
        if (locator2 != null) {
            return new SAXParseException(str, locator2);
        }
        return new SAXParseException(str, null, null, -1, -1);
    }

    private void checkNotParsing(String str, String str2) throws SAXNotSupportedException {
        if (this.parsing) {
            throw new SAXNotSupportedException("Cannot change " + str + ' ' + str2 + " while parsing");
        }
    }

    /* access modifiers changed from: package-private */
    public final class AttributeListAdapter implements Attributes {
        private AttributeList qAtts;

        @Override // ohos.org.xml.sax.Attributes
        public int getIndex(String str, String str2) {
            return -1;
        }

        @Override // ohos.org.xml.sax.Attributes
        public String getLocalName(int i) {
            return "";
        }

        @Override // ohos.org.xml.sax.Attributes
        public String getType(String str, String str2) {
            return null;
        }

        @Override // ohos.org.xml.sax.Attributes
        public String getURI(int i) {
            return "";
        }

        @Override // ohos.org.xml.sax.Attributes
        public String getValue(String str, String str2) {
            return null;
        }

        AttributeListAdapter() {
        }

        /* access modifiers changed from: package-private */
        public void setAttributeList(AttributeList attributeList) {
            this.qAtts = attributeList;
        }

        @Override // ohos.org.xml.sax.Attributes
        public int getLength() {
            return this.qAtts.getLength();
        }

        @Override // ohos.org.xml.sax.Attributes
        public String getQName(int i) {
            return this.qAtts.getName(i).intern();
        }

        @Override // ohos.org.xml.sax.Attributes
        public String getType(int i) {
            return this.qAtts.getType(i).intern();
        }

        @Override // ohos.org.xml.sax.Attributes
        public String getValue(int i) {
            return this.qAtts.getValue(i);
        }

        @Override // ohos.org.xml.sax.Attributes
        public int getIndex(String str) {
            int length = ParserAdapter.this.atts.getLength();
            for (int i = 0; i < length; i++) {
                if (this.qAtts.getName(i).equals(str)) {
                    return i;
                }
            }
            return -1;
        }

        @Override // ohos.org.xml.sax.Attributes
        public String getType(String str) {
            return this.qAtts.getType(str).intern();
        }

        @Override // ohos.org.xml.sax.Attributes
        public String getValue(String str) {
            return this.qAtts.getValue(str);
        }
    }
}
