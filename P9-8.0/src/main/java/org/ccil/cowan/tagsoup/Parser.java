package org.ccil.cowan.tagsoup;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

public class Parser extends DefaultHandler implements ScanHandler, XMLReader, LexicalHandler {
    public static final String CDATAElementsFeature = "http://www.ccil.org/~cowan/tagsoup/features/cdata-elements";
    private static boolean DEFAULT_BOGONS_EMPTY = false;
    private static boolean DEFAULT_CDATA_ELEMENTS = true;
    private static boolean DEFAULT_DEFAULT_ATTRIBUTES = true;
    private static boolean DEFAULT_IGNORABLE_WHITESPACE = false;
    private static boolean DEFAULT_IGNORE_BOGONS = false;
    private static boolean DEFAULT_NAMESPACES = true;
    private static boolean DEFAULT_RESTART_ELEMENTS = true;
    private static boolean DEFAULT_ROOT_BOGONS = true;
    private static boolean DEFAULT_TRANSLATE_COLONS = false;
    public static final String XML11Feature = "http://xml.org/sax/features/xml-1.1";
    public static final String autoDetectorProperty = "http://www.ccil.org/~cowan/tagsoup/properties/auto-detector";
    public static final String bogonsEmptyFeature = "http://www.ccil.org/~cowan/tagsoup/features/bogons-empty";
    public static final String defaultAttributesFeature = "http://www.ccil.org/~cowan/tagsoup/features/default-attributes";
    private static char[] etagchars = new char[]{'<', '/', '>'};
    public static final String externalGeneralEntitiesFeature = "http://xml.org/sax/features/external-general-entities";
    public static final String externalParameterEntitiesFeature = "http://xml.org/sax/features/external-parameter-entities";
    public static final String ignorableWhitespaceFeature = "http://www.ccil.org/~cowan/tagsoup/features/ignorable-whitespace";
    public static final String ignoreBogonsFeature = "http://www.ccil.org/~cowan/tagsoup/features/ignore-bogons";
    public static final String isStandaloneFeature = "http://xml.org/sax/features/is-standalone";
    private static String legal = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-'()+,./:=?;!*#@$_%";
    public static final String lexicalHandlerParameterEntitiesFeature = "http://xml.org/sax/features/lexical-handler/parameter-entities";
    public static final String lexicalHandlerProperty = "http://xml.org/sax/properties/lexical-handler";
    public static final String namespacePrefixesFeature = "http://xml.org/sax/features/namespace-prefixes";
    public static final String namespacesFeature = "http://xml.org/sax/features/namespaces";
    public static final String resolveDTDURIsFeature = "http://xml.org/sax/features/resolve-dtd-uris";
    public static final String restartElementsFeature = "http://www.ccil.org/~cowan/tagsoup/features/restart-elements";
    public static final String rootBogonsFeature = "http://www.ccil.org/~cowan/tagsoup/features/root-bogons";
    public static final String scannerProperty = "http://www.ccil.org/~cowan/tagsoup/properties/scanner";
    public static final String schemaProperty = "http://www.ccil.org/~cowan/tagsoup/properties/schema";
    public static final String stringInterningFeature = "http://xml.org/sax/features/string-interning";
    public static final String translateColonsFeature = "http://www.ccil.org/~cowan/tagsoup/features/translate-colons";
    public static final String unicodeNormalizationCheckingFeature = "http://xml.org/sax/features/unicode-normalization-checking";
    public static final String useAttributes2Feature = "http://xml.org/sax/features/use-attributes2";
    public static final String useEntityResolver2Feature = "http://xml.org/sax/features/use-entity-resolver2";
    public static final String useLocator2Feature = "http://xml.org/sax/features/use-locator2";
    public static final String validationFeature = "http://xml.org/sax/features/validation";
    public static final String xmlnsURIsFeature = "http://xml.org/sax/features/xmlns-uris";
    private boolean CDATAElements = DEFAULT_CDATA_ELEMENTS;
    private boolean bogonsEmpty = DEFAULT_BOGONS_EMPTY;
    private boolean defaultAttributes = DEFAULT_DEFAULT_ATTRIBUTES;
    private boolean ignorableWhitespace = DEFAULT_IGNORABLE_WHITESPACE;
    private boolean ignoreBogons = DEFAULT_IGNORE_BOGONS;
    private boolean namespaces = DEFAULT_NAMESPACES;
    private boolean restartElements = DEFAULT_RESTART_ELEMENTS;
    private boolean rootBogons = DEFAULT_ROOT_BOGONS;
    private String theAttributeName;
    private AutoDetector theAutoDetector;
    private char[] theCommentBuffer;
    private ContentHandler theContentHandler = this;
    private DTDHandler theDTDHandler = this;
    private boolean theDoctypeIsPresent;
    private String theDoctypeName;
    private String theDoctypePublicId;
    private String theDoctypeSystemId;
    private int theEntity;
    private EntityResolver theEntityResolver = this;
    private ErrorHandler theErrorHandler = this;
    private HashMap theFeatures = new HashMap();
    private LexicalHandler theLexicalHandler = this;
    private Element theNewElement;
    private Element thePCDATA;
    private String thePITarget;
    private Element theSaved;
    private Scanner theScanner;
    private Schema theSchema;
    private Element theStack;
    private boolean translateColons = DEFAULT_TRANSLATE_COLONS;
    private boolean virginStack;

    public Parser() {
        this.theFeatures.put(namespacesFeature, truthValue(DEFAULT_NAMESPACES));
        this.theFeatures.put(namespacePrefixesFeature, Boolean.FALSE);
        this.theFeatures.put(externalGeneralEntitiesFeature, Boolean.FALSE);
        this.theFeatures.put(externalParameterEntitiesFeature, Boolean.FALSE);
        this.theFeatures.put(isStandaloneFeature, Boolean.FALSE);
        this.theFeatures.put(lexicalHandlerParameterEntitiesFeature, Boolean.FALSE);
        this.theFeatures.put(resolveDTDURIsFeature, Boolean.TRUE);
        this.theFeatures.put(stringInterningFeature, Boolean.TRUE);
        this.theFeatures.put(useAttributes2Feature, Boolean.FALSE);
        this.theFeatures.put(useLocator2Feature, Boolean.FALSE);
        this.theFeatures.put(useEntityResolver2Feature, Boolean.FALSE);
        this.theFeatures.put(validationFeature, Boolean.FALSE);
        this.theFeatures.put(xmlnsURIsFeature, Boolean.FALSE);
        this.theFeatures.put(xmlnsURIsFeature, Boolean.FALSE);
        this.theFeatures.put(XML11Feature, Boolean.FALSE);
        this.theFeatures.put(ignoreBogonsFeature, truthValue(DEFAULT_IGNORE_BOGONS));
        this.theFeatures.put(bogonsEmptyFeature, truthValue(DEFAULT_BOGONS_EMPTY));
        this.theFeatures.put(rootBogonsFeature, truthValue(DEFAULT_ROOT_BOGONS));
        this.theFeatures.put(defaultAttributesFeature, truthValue(DEFAULT_DEFAULT_ATTRIBUTES));
        this.theFeatures.put(translateColonsFeature, truthValue(DEFAULT_TRANSLATE_COLONS));
        this.theFeatures.put(restartElementsFeature, truthValue(DEFAULT_RESTART_ELEMENTS));
        this.theFeatures.put(ignorableWhitespaceFeature, truthValue(DEFAULT_IGNORABLE_WHITESPACE));
        this.theFeatures.put(CDATAElementsFeature, truthValue(DEFAULT_CDATA_ELEMENTS));
        this.theNewElement = null;
        this.theAttributeName = null;
        this.theDoctypeIsPresent = false;
        this.theDoctypePublicId = null;
        this.theDoctypeSystemId = null;
        this.theDoctypeName = null;
        this.thePITarget = null;
        this.theStack = null;
        this.theSaved = null;
        this.thePCDATA = null;
        this.theEntity = 0;
        this.virginStack = true;
        this.theCommentBuffer = new char[2000];
    }

    private static Boolean truthValue(boolean b) {
        return b ? Boolean.TRUE : Boolean.FALSE;
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        Boolean b = (Boolean) this.theFeatures.get(name);
        if (b != null) {
            return b.booleanValue();
        }
        throw new SAXNotRecognizedException("Unknown feature " + name);
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (((Boolean) this.theFeatures.get(name)) == null) {
            throw new SAXNotRecognizedException("Unknown feature " + name);
        }
        if (value) {
            this.theFeatures.put(name, Boolean.TRUE);
        } else {
            this.theFeatures.put(name, Boolean.FALSE);
        }
        if (name.equals(namespacesFeature)) {
            this.namespaces = value;
        } else if (name.equals(ignoreBogonsFeature)) {
            this.ignoreBogons = value;
        } else if (name.equals(bogonsEmptyFeature)) {
            this.bogonsEmpty = value;
        } else if (name.equals(rootBogonsFeature)) {
            this.rootBogons = value;
        } else if (name.equals(defaultAttributesFeature)) {
            this.defaultAttributes = value;
        } else if (name.equals(translateColonsFeature)) {
            this.translateColons = value;
        } else if (name.equals(restartElementsFeature)) {
            this.restartElements = value;
        } else if (name.equals(ignorableWhitespaceFeature)) {
            this.ignorableWhitespace = value;
        } else if (name.equals(CDATAElementsFeature)) {
            this.CDATAElements = value;
        }
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals(lexicalHandlerProperty)) {
            return this.theLexicalHandler == this ? null : this.theLexicalHandler;
        } else if (name.equals(scannerProperty)) {
            return this.theScanner;
        } else {
            if (name.equals(schemaProperty)) {
                return this.theSchema;
            }
            if (name.equals(autoDetectorProperty)) {
                return this.theAutoDetector;
            }
            throw new SAXNotRecognizedException("Unknown property " + name);
        }
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (name.equals(lexicalHandlerProperty)) {
            if (value == null) {
                this.theLexicalHandler = this;
            } else if (value instanceof LexicalHandler) {
                this.theLexicalHandler = (LexicalHandler) value;
            } else {
                throw new SAXNotSupportedException("Your lexical handler is not a LexicalHandler");
            }
        } else if (name.equals(scannerProperty)) {
            if (value instanceof Scanner) {
                this.theScanner = (Scanner) value;
                return;
            }
            throw new SAXNotSupportedException("Your scanner is not a Scanner");
        } else if (name.equals(schemaProperty)) {
            if (value instanceof Schema) {
                this.theSchema = (Schema) value;
                return;
            }
            throw new SAXNotSupportedException("Your schema is not a Schema");
        } else if (!name.equals(autoDetectorProperty)) {
            throw new SAXNotRecognizedException("Unknown property " + name);
        } else if (value instanceof AutoDetector) {
            this.theAutoDetector = (AutoDetector) value;
        } else {
            throw new SAXNotSupportedException("Your auto-detector is not an AutoDetector");
        }
    }

    public void setEntityResolver(EntityResolver resolver) {
        if (resolver == null) {
            resolver = this;
        }
        this.theEntityResolver = resolver;
    }

    public EntityResolver getEntityResolver() {
        return this.theEntityResolver == this ? null : this.theEntityResolver;
    }

    public void setDTDHandler(DTDHandler handler) {
        if (handler == null) {
            handler = this;
        }
        this.theDTDHandler = handler;
    }

    public DTDHandler getDTDHandler() {
        return this.theDTDHandler == this ? null : this.theDTDHandler;
    }

    public void setContentHandler(ContentHandler handler) {
        if (handler == null) {
            handler = this;
        }
        this.theContentHandler = handler;
    }

    public ContentHandler getContentHandler() {
        return this.theContentHandler == this ? null : this.theContentHandler;
    }

    public void setErrorHandler(ErrorHandler handler) {
        if (handler == null) {
            handler = this;
        }
        this.theErrorHandler = handler;
    }

    public ErrorHandler getErrorHandler() {
        return this.theErrorHandler == this ? null : this.theErrorHandler;
    }

    public void parse(InputSource input) throws IOException, SAXException {
        setup();
        Reader r = getReader(input);
        this.theContentHandler.startDocument();
        this.theScanner.resetDocumentLocator(input.getPublicId(), input.getSystemId());
        if (this.theScanner instanceof Locator) {
            this.theContentHandler.setDocumentLocator((Locator) this.theScanner);
        }
        if (!this.theSchema.getURI().equals("")) {
            this.theContentHandler.startPrefixMapping(this.theSchema.getPrefix(), this.theSchema.getURI());
        }
        this.theScanner.scan(r, this);
    }

    public void parse(String systemid) throws IOException, SAXException {
        parse(new InputSource(systemid));
    }

    private void setup() {
        if (this.theSchema == null) {
            this.theSchema = new HTMLSchema();
        }
        if (this.theScanner == null) {
            this.theScanner = new HTMLScanner();
        }
        if (this.theAutoDetector == null) {
            this.theAutoDetector = new AutoDetector() {
                public Reader autoDetectingReader(InputStream i) {
                    return new InputStreamReader(i);
                }
            };
        }
        this.theStack = new Element(this.theSchema.getElementType("<root>"), this.defaultAttributes);
        this.thePCDATA = new Element(this.theSchema.getElementType("<pcdata>"), this.defaultAttributes);
        this.theNewElement = null;
        this.theAttributeName = null;
        this.thePITarget = null;
        this.theSaved = null;
        this.theEntity = 0;
        this.virginStack = true;
        this.theDoctypeSystemId = null;
        this.theDoctypePublicId = null;
        this.theDoctypeName = null;
    }

    private Reader getReader(InputSource s) throws SAXException, IOException {
        Reader r = s.getCharacterStream();
        InputStream i = s.getByteStream();
        String encoding = s.getEncoding();
        String publicid = s.getPublicId();
        String systemid = s.getSystemId();
        if (r != null) {
            return r;
        }
        if (i == null) {
            i = getInputStream(publicid, systemid);
        }
        if (encoding == null) {
            return this.theAutoDetector.autoDetectingReader(i);
        }
        try {
            return new InputStreamReader(i, encoding);
        } catch (UnsupportedEncodingException e) {
            return new InputStreamReader(i);
        }
    }

    private InputStream getInputStream(String publicid, String systemid) throws IOException, SAXException {
        return new URL(new URL("file", "", System.getProperty("user.dir") + "/."), systemid).openConnection().getInputStream();
    }

    public void adup(char[] buff, int offset, int length) throws SAXException {
        if (this.theNewElement != null && this.theAttributeName != null) {
            this.theNewElement.setAttribute(this.theAttributeName, null, this.theAttributeName);
            this.theAttributeName = null;
        }
    }

    public void aname(char[] buff, int offset, int length) throws SAXException {
        if (this.theNewElement != null) {
            this.theAttributeName = makeName(buff, offset, length).toLowerCase(Locale.ROOT);
        }
    }

    public void aval(char[] buff, int offset, int length) throws SAXException {
        if (this.theNewElement != null && this.theAttributeName != null) {
            this.theNewElement.setAttribute(this.theAttributeName, null, expandEntities(new String(buff, offset, length)));
            this.theAttributeName = null;
        }
    }

    private String expandEntities(String src) {
        int refStart = -1;
        int len = src.length();
        char[] dst = new char[len];
        int i = 0;
        int dstlen = 0;
        while (i < len) {
            char ch = src.charAt(i);
            int dstlen2 = dstlen + 1;
            dst[dstlen] = ch;
            if (ch == '&' && refStart == -1) {
                refStart = dstlen2;
            } else if (!(refStart == -1 || Character.isLetter(ch) || Character.isDigit(ch) || ch == '#')) {
                if (ch == ';') {
                    int ent = lookupEntity(dst, refStart, (dstlen2 - refStart) - 1);
                    if (ent > 65535) {
                        ent -= HTMLModels.M_OPTION;
                        dst[refStart - 1] = (char) ((ent >> 10) + 55296);
                        dst[refStart] = (char) ((ent & 1023) + 56320);
                        dstlen2 = refStart + 1;
                    } else if (ent != 0) {
                        dst[refStart - 1] = (char) ent;
                        dstlen2 = refStart;
                    }
                    refStart = -1;
                } else {
                    refStart = -1;
                }
            }
            i++;
            dstlen = dstlen2;
        }
        return new String(dst, 0, dstlen);
    }

    public void entity(char[] buff, int offset, int length) throws SAXException {
        this.theEntity = lookupEntity(buff, offset, length);
    }

    private int lookupEntity(char[] buff, int offset, int length) {
        if (length < 1) {
            return 0;
        }
        if (buff[offset] != '#') {
            return this.theSchema.getEntity(new String(buff, offset, length));
        }
        if (length <= 1 || !(buff[offset + 1] == 'x' || buff[offset + 1] == 'X')) {
            try {
                return Integer.parseInt(new String(buff, offset + 1, length - 1), 10);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        try {
            return Integer.parseInt(new String(buff, offset + 2, length - 2), 16);
        } catch (NumberFormatException e2) {
            return 0;
        }
    }

    public void eof(char[] buff, int offset, int length) throws SAXException {
        if (this.virginStack) {
            rectify(this.thePCDATA);
        }
        while (this.theStack.next() != null) {
            pop();
        }
        if (!this.theSchema.getURI().equals("")) {
            this.theContentHandler.endPrefixMapping(this.theSchema.getPrefix());
        }
        this.theContentHandler.endDocument();
    }

    public void etag(char[] buff, int offset, int length) throws SAXException {
        if (!etag_cdata(buff, offset, length)) {
            etag_basic(buff, offset, length);
        }
    }

    public boolean etag_cdata(char[] buff, int offset, int length) throws SAXException {
        String currentName = this.theStack.name();
        if (this.CDATAElements && (this.theStack.flags() & 2) != 0) {
            boolean realTag = length == currentName.length();
            if (realTag) {
                for (int i = 0; i < length; i++) {
                    if (Character.toLowerCase(buff[offset + i]) != Character.toLowerCase(currentName.charAt(i))) {
                        realTag = false;
                        break;
                    }
                }
            }
            if (!realTag) {
                this.theContentHandler.characters(etagchars, 0, 2);
                this.theContentHandler.characters(buff, offset, length);
                this.theContentHandler.characters(etagchars, 2, 1);
                this.theScanner.startCDATA();
                return true;
            }
        }
        return false;
    }

    public void etag_basic(char[] buff, int offset, int length) throws SAXException {
        this.theNewElement = null;
        String name;
        if (length != 0) {
            ElementType type = this.theSchema.getElementType(makeName(buff, offset, length));
            if (type != null) {
                name = type.name();
            } else {
                return;
            }
        }
        name = this.theStack.name();
        boolean inNoforce = false;
        Element sp = this.theStack;
        while (sp != null && !sp.name().equals(name)) {
            if ((sp.flags() & 4) != 0) {
                inNoforce = true;
            }
            sp = sp.next();
        }
        if (sp != null && sp.next() != null && sp.next().next() != null) {
            if (inNoforce) {
                sp.preclose();
            } else {
                while (this.theStack != sp) {
                    restartablyPop();
                }
                pop();
            }
            while (this.theStack.isPreclosed()) {
                pop();
            }
            restart(null);
        }
    }

    private void restart(Element e) throws SAXException {
        while (this.theSaved != null && this.theStack.canContain(this.theSaved)) {
            if (e == null || this.theSaved.canContain(e)) {
                Element next = this.theSaved.next();
                push(this.theSaved);
                this.theSaved = next;
            } else {
                return;
            }
        }
    }

    private void pop() throws SAXException {
        if (this.theStack != null) {
            String name = this.theStack.name();
            String localName = this.theStack.localName();
            String namespace = this.theStack.namespace();
            String prefix = prefixOf(name);
            if (!this.namespaces) {
                localName = "";
                namespace = localName;
            }
            this.theContentHandler.endElement(namespace, localName, name);
            if (foreign(prefix, namespace)) {
                this.theContentHandler.endPrefixMapping(prefix);
            }
            Attributes atts = this.theStack.atts();
            for (int i = atts.getLength() - 1; i >= 0; i--) {
                String attNamespace = atts.getURI(i);
                String attPrefix = prefixOf(atts.getQName(i));
                if (foreign(attPrefix, attNamespace)) {
                    this.theContentHandler.endPrefixMapping(attPrefix);
                }
            }
            this.theStack = this.theStack.next();
        }
    }

    private void restartablyPop() throws SAXException {
        Element popped = this.theStack;
        pop();
        if (this.restartElements && (popped.flags() & 1) != 0) {
            popped.anonymize();
            popped.setNext(this.theSaved);
            this.theSaved = popped;
        }
    }

    private void push(Element e) throws SAXException {
        String name = e.name();
        String localName = e.localName();
        String namespace = e.namespace();
        String prefix = prefixOf(name);
        e.clean();
        if (!this.namespaces) {
            localName = "";
            namespace = localName;
        }
        if (this.virginStack && localName.equalsIgnoreCase(this.theDoctypeName)) {
            try {
                this.theEntityResolver.resolveEntity(this.theDoctypePublicId, this.theDoctypeSystemId);
            } catch (IOException e2) {
            }
        }
        if (foreign(prefix, namespace)) {
            this.theContentHandler.startPrefixMapping(prefix, namespace);
        }
        Attributes atts = e.atts();
        int len = atts.getLength();
        for (int i = 0; i < len; i++) {
            String attNamespace = atts.getURI(i);
            String attPrefix = prefixOf(atts.getQName(i));
            if (foreign(attPrefix, attNamespace)) {
                this.theContentHandler.startPrefixMapping(attPrefix, attNamespace);
            }
        }
        this.theContentHandler.startElement(namespace, localName, name, e.atts());
        e.setNext(this.theStack);
        this.theStack = e;
        this.virginStack = false;
        if (this.CDATAElements && (this.theStack.flags() & 2) != 0) {
            this.theScanner.startCDATA();
        }
    }

    private String prefixOf(String name) {
        int i = name.indexOf(58);
        String prefix = "";
        if (i != -1) {
            return name.substring(0, i);
        }
        return prefix;
    }

    private boolean foreign(String prefix, String namespace) {
        int i;
        if (prefix.equals("") || namespace.equals("")) {
            i = 1;
        } else {
            i = namespace.equals(this.theSchema.getURI());
        }
        return i ^ 1;
    }

    public void decl(char[] buff, int offset, int length) throws SAXException {
        String name = null;
        String systemid = null;
        String publicid = null;
        String[] v = split(new String(buff, offset, length));
        if (v.length > 0 && "DOCTYPE".equalsIgnoreCase(v[0])) {
            if (!this.theDoctypeIsPresent) {
                this.theDoctypeIsPresent = true;
                if (v.length > 1) {
                    name = v[1];
                    if (v.length > 3 && "SYSTEM".equals(v[2])) {
                        systemid = v[3];
                    } else if (v.length > 3 && "PUBLIC".equals(v[2])) {
                        publicid = v[3];
                        systemid = v.length > 4 ? v[4] : "";
                    }
                }
            } else {
                return;
            }
        }
        publicid = trimquotes(publicid);
        systemid = trimquotes(systemid);
        if (name != null) {
            publicid = cleanPublicid(publicid);
            this.theLexicalHandler.startDTD(name, publicid, systemid);
            this.theLexicalHandler.endDTD();
            this.theDoctypeName = name;
            this.theDoctypePublicId = publicid;
            if (this.theScanner instanceof Locator) {
                this.theDoctypeSystemId = ((Locator) this.theScanner).getSystemId();
                try {
                    this.theDoctypeSystemId = new URL(new URL(this.theDoctypeSystemId), systemid).toString();
                } catch (Exception e) {
                }
            }
        }
    }

    private static String trimquotes(String in) {
        if (in == null) {
            return in;
        }
        int length = in.length();
        if (length == 0) {
            return in;
        }
        char s = in.charAt(0);
        if (s == in.charAt(length - 1) && (s == '\'' || s == '\"')) {
            in = in.substring(1, in.length() - 1);
        }
        return in;
    }

    private static String[] split(String val) throws IllegalArgumentException {
        val = val.trim();
        if (val.length() == 0) {
            return new String[0];
        }
        ArrayList l = new ArrayList();
        int s = 0;
        int sq = 0;
        int dq = 0;
        char lastc = 0;
        int len = val.length();
        int e = 0;
        while (e < len) {
            char c = val.charAt(e);
            if (dq == 0 && c == '\'' && lastc != '\\') {
                sq ^= 1;
                if (s < 0) {
                    s = e;
                }
            } else if (sq == 0 && c == '\"' && lastc != '\\') {
                dq ^= 1;
                if (s < 0) {
                    s = e;
                }
            } else if (sq == 0 && (dq ^ 1) != 0) {
                if (Character.isWhitespace(c)) {
                    if (s >= 0) {
                        l.add(val.substring(s, e));
                    }
                    s = -1;
                } else if (s < 0 && c != ' ') {
                    s = e;
                }
            }
            lastc = c;
            e++;
        }
        l.add(val.substring(s, e));
        return (String[]) l.toArray(new String[0]);
    }

    private String cleanPublicid(String src) {
        if (src == null) {
            return null;
        }
        int len = src.length();
        StringBuffer dst = new StringBuffer(len);
        boolean suppressSpace = true;
        for (int i = 0; i < len; i++) {
            char ch = src.charAt(i);
            if (legal.indexOf(ch) != -1) {
                dst.append(ch);
                suppressSpace = false;
            } else if (!suppressSpace) {
                dst.append(' ');
                suppressSpace = true;
            }
        }
        return dst.toString().trim();
    }

    public void gi(char[] buff, int offset, int length) throws SAXException {
        if (this.theNewElement == null) {
            String name = makeName(buff, offset, length);
            if (name != null) {
                ElementType type = this.theSchema.getElementType(name);
                if (type == null) {
                    if (!this.ignoreBogons) {
                        this.theSchema.elementType(name, this.bogonsEmpty ? 0 : -1, this.rootBogons ? -1 : Integer.MAX_VALUE, 0);
                        if (!this.rootBogons) {
                            this.theSchema.parent(name, this.theSchema.rootElementType().name());
                        }
                        type = this.theSchema.getElementType(name);
                    } else {
                        return;
                    }
                }
                this.theNewElement = new Element(type, this.defaultAttributes);
            }
        }
    }

    public void cdsect(char[] buff, int offset, int length) throws SAXException {
        this.theLexicalHandler.startCDATA();
        pcdata(buff, offset, length);
        this.theLexicalHandler.endCDATA();
    }

    public void pcdata(char[] buff, int offset, int length) throws SAXException {
        if (length != 0) {
            boolean allWhite = true;
            for (int i = 0; i < length; i++) {
                if (!Character.isWhitespace(buff[offset + i])) {
                    allWhite = false;
                }
            }
            if (!allWhite || (this.theStack.canContain(this.thePCDATA) ^ 1) == 0) {
                rectify(this.thePCDATA);
                this.theContentHandler.characters(buff, offset, length);
            } else if (this.ignorableWhitespace) {
                this.theContentHandler.ignorableWhitespace(buff, offset, length);
            }
        }
    }

    public void pitarget(char[] buff, int offset, int length) throws SAXException {
        if (this.theNewElement == null) {
            this.thePITarget = makeName(buff, offset, length).replace(':', '_');
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void pi(char[] buff, int offset, int length) throws SAXException {
        if (this.theNewElement == null && this.thePITarget != null && !"xml".equalsIgnoreCase(this.thePITarget)) {
            if (length > 0 && buff[length - 1] == '?') {
                length--;
            }
            this.theContentHandler.processingInstruction(this.thePITarget, new String(buff, offset, length));
            this.thePITarget = null;
        }
    }

    public void stagc(char[] buff, int offset, int length) throws SAXException {
        if (this.theNewElement != null) {
            rectify(this.theNewElement);
            if (this.theStack.model() == 0) {
                etag_basic(buff, offset, length);
            }
        }
    }

    public void stage(char[] buff, int offset, int length) throws SAXException {
        if (this.theNewElement != null) {
            rectify(this.theNewElement);
            etag_basic(buff, offset, length);
        }
    }

    public void cmnt(char[] buff, int offset, int length) throws SAXException {
        this.theLexicalHandler.comment(buff, offset, length);
    }

    private void rectify(Element e) throws SAXException {
        Element sp;
        while (true) {
            sp = this.theStack;
            while (sp != null && !sp.canContain(e)) {
                sp = sp.next();
            }
            if (sp == null) {
                ElementType parentType = e.parent();
                if (parentType == null) {
                    break;
                }
                Element parent = new Element(parentType, this.defaultAttributes);
                parent.setNext(e);
                e = parent;
            } else {
                break;
            }
        }
        if (sp != null) {
            while (this.theStack != sp && this.theStack != null && this.theStack.next() != null && this.theStack.next().next() != null) {
                restartablyPop();
            }
            while (e != null) {
                Element nexte = e.next();
                if (!e.name().equals("<pcdata>")) {
                    push(e);
                }
                e = nexte;
                restart(nexte);
            }
            this.theNewElement = null;
        }
    }

    public int getEntity() {
        return this.theEntity;
    }

    private String makeName(char[] buff, int offset, int length) {
        StringBuffer dst = new StringBuffer(length + 2);
        boolean seenColon = false;
        boolean start = true;
        while (true) {
            int length2 = length;
            length = length2 - 1;
            if (length2 <= 0) {
                break;
            }
            char ch = buff[offset];
            if (Character.isLetter(ch) || ch == '_') {
                start = false;
                dst.append(ch);
            } else if (Character.isDigit(ch) || ch == '-' || ch == '.') {
                if (start) {
                    dst.append('_');
                }
                start = false;
                dst.append(ch);
            } else if (ch == ':' && (seenColon ^ 1) != 0) {
                seenColon = true;
                if (start) {
                    dst.append('_');
                }
                start = true;
                if (this.translateColons) {
                    ch = '_';
                }
                dst.append(ch);
            }
            offset++;
        }
        int dstLength = dst.length();
        if (dstLength == 0 || dst.charAt(dstLength - 1) == ':') {
            dst.append('_');
        }
        return dst.toString().intern();
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void startDTD(String name, String publicid, String systemid) throws SAXException {
    }

    public void startEntity(String name) throws SAXException {
    }
}
