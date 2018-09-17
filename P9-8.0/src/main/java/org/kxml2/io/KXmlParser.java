package org.kxml2.io;

import android.icu.impl.PatternTokenizer;
import android.icu.util.ULocale;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.XMLConstants;
import javax.xml.transform.OutputKeys;
import libcore.internal.StringPool;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class KXmlParser implements XmlPullParser, Closeable {
    private static final char[] ANY = new char[]{'A', 'N', 'Y'};
    private static final int ATTLISTDECL = 13;
    private static final char[] COMMENT_DOUBLE_DASH = new char[]{'-', '-'};
    private static final Map<String, String> DEFAULT_ENTITIES = new HashMap();
    private static final char[] DOUBLE_QUOTE = new char[]{'\"'};
    private static final int ELEMENTDECL = 11;
    private static final char[] EMPTY = new char[]{'E', 'M', 'P', 'T', 'Y'};
    private static final char[] END_CDATA = new char[]{']', ']', '>'};
    private static final char[] END_COMMENT = new char[]{'-', '-', '>'};
    private static final char[] END_PROCESSING_INSTRUCTION = new char[]{'?', '>'};
    private static final int ENTITYDECL = 12;
    private static final String FEATURE_RELAXED = "http://xmlpull.org/v1/doc/features.html#relaxed";
    private static final char[] FIXED = new char[]{'F', 'I', 'X', 'E', 'D'};
    private static final String ILLEGAL_TYPE = "Wrong event type";
    private static final char[] IMPLIED = new char[]{'I', 'M', 'P', 'L', 'I', 'E', 'D'};
    private static final char[] NDATA = new char[]{'N', 'D', 'A', 'T', 'A'};
    private static final char[] NOTATION = new char[]{'N', 'O', 'T', 'A', 'T', 'I', 'O', 'N'};
    private static final int NOTATIONDECL = 14;
    private static final int PARAMETER_ENTITY_REF = 15;
    private static final String PROPERTY_LOCATION = "http://xmlpull.org/v1/doc/properties.html#location";
    private static final String PROPERTY_XMLDECL_STANDALONE = "http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone";
    private static final String PROPERTY_XMLDECL_VERSION = "http://xmlpull.org/v1/doc/properties.html#xmldecl-version";
    private static final char[] PUBLIC = new char[]{'P', 'U', 'B', 'L', 'I', 'C'};
    private static final char[] REQUIRED = new char[]{'R', 'E', 'Q', 'U', 'I', 'R', 'E', 'D'};
    private static final char[] SINGLE_QUOTE = new char[]{PatternTokenizer.SINGLE_QUOTE};
    private static final char[] START_ATTLIST = new char[]{'<', '!', 'A', 'T', 'T', 'L', 'I', 'S', 'T'};
    private static final char[] START_CDATA = new char[]{'<', '!', '[', 'C', 'D', 'A', 'T', 'A', '['};
    private static final char[] START_COMMENT = new char[]{'<', '!', '-', '-'};
    private static final char[] START_DOCTYPE = new char[]{'<', '!', 'D', 'O', 'C', 'T', 'Y', 'P', 'E'};
    private static final char[] START_ELEMENT = new char[]{'<', '!', 'E', 'L', 'E', 'M', 'E', 'N', 'T'};
    private static final char[] START_ENTITY = new char[]{'<', '!', 'E', 'N', 'T', 'I', 'T', 'Y'};
    private static final char[] START_NOTATION = new char[]{'<', '!', 'N', 'O', 'T', 'A', 'T', 'I', 'O', 'N'};
    private static final char[] START_PROCESSING_INSTRUCTION = new char[]{'<', '?'};
    private static final char[] SYSTEM = new char[]{'S', 'Y', 'S', 'T', 'E', 'M'};
    private static final String UNEXPECTED_EOF = "Unexpected EOF";
    private static final int XML_DECLARATION = 998;
    private int attributeCount;
    private String[] attributes = new String[16];
    private char[] buffer = new char[8192];
    private StringBuilder bufferCapture;
    private int bufferStartColumn;
    private int bufferStartLine;
    private Map<String, Map<String, String>> defaultAttributes;
    private boolean degenerated;
    private int depth;
    private Map<String, char[]> documentEntities;
    private String[] elementStack = new String[16];
    private String encoding;
    private String error;
    private boolean isWhitespace;
    private boolean keepNamespaceAttributes;
    private int limit = 0;
    private String location;
    private String name;
    private String namespace;
    private ContentSource nextContentSource;
    private int[] nspCounts = new int[4];
    private String[] nspStack = new String[8];
    private boolean parsedTopLevelStartTag;
    private int position = 0;
    private String prefix;
    private boolean processDocDecl;
    private boolean processNsp;
    private String publicId;
    private Reader reader;
    private boolean relaxed;
    private String rootElementName;
    private Boolean standalone;
    public final StringPool stringPool = new StringPool();
    private String systemId;
    private String text;
    private int type;
    private boolean unresolved;
    private String version;

    static class ContentSource {
        private final char[] buffer;
        private final int limit;
        private final ContentSource next;
        private final int position;

        ContentSource(ContentSource next, char[] buffer, int position, int limit) {
            this.next = next;
            this.buffer = buffer;
            this.position = position;
            this.limit = limit;
        }
    }

    enum ValueContext {
        ATTRIBUTE,
        TEXT,
        ENTITY_DECLARATION
    }

    static {
        DEFAULT_ENTITIES.put("lt", "<");
        DEFAULT_ENTITIES.put("gt", ">");
        DEFAULT_ENTITIES.put("amp", "&");
        DEFAULT_ENTITIES.put("apos", "'");
        DEFAULT_ENTITIES.put("quot", "\"");
    }

    public void keepNamespaceAttributes() {
        this.keepNamespaceAttributes = true;
    }

    private boolean adjustNsp() throws XmlPullParserException {
        String attrName;
        int cut;
        boolean any = false;
        int i = 0;
        while (i < (this.attributeCount << 2)) {
            String prefix;
            attrName = this.attributes[i + 2];
            cut = attrName.indexOf(58);
            if (cut != -1) {
                prefix = attrName.substring(0, cut);
                attrName = attrName.substring(cut + 1);
            } else if (attrName.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                prefix = attrName;
                attrName = null;
            } else {
                i += 4;
            }
            if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                int[] iArr = this.nspCounts;
                int i2 = this.depth;
                int i3 = iArr[i2];
                iArr[i2] = i3 + 1;
                int j = i3 << 1;
                this.nspStack = ensureCapacity(this.nspStack, j + 2);
                this.nspStack[j] = attrName;
                this.nspStack[j + 1] = this.attributes[i + 3];
                if (attrName != null && this.attributes[i + 3].isEmpty()) {
                    checkRelaxed("illegal empty namespace");
                }
                if (this.keepNamespaceAttributes) {
                    this.attributes[i] = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
                    any = true;
                } else {
                    Object obj = this.attributes;
                    i2 = i + 4;
                    Object obj2 = this.attributes;
                    int i4 = this.attributeCount - 1;
                    this.attributeCount = i4;
                    System.arraycopy(obj, i2, obj2, i, (i4 << 2) - i);
                    i -= 4;
                }
            } else {
                any = true;
            }
            i += 4;
        }
        if (any) {
            i = (this.attributeCount << 2) - 4;
            while (i >= 0) {
                attrName = this.attributes[i + 2];
                cut = attrName.indexOf(58);
                if (cut != 0 || (this.relaxed ^ 1) == 0) {
                    if (cut != -1) {
                        String attrPrefix = attrName.substring(0, cut);
                        attrName = attrName.substring(cut + 1);
                        String attrNs = getNamespace(attrPrefix);
                        if (attrNs != null || (this.relaxed ^ 1) == 0) {
                            this.attributes[i] = attrNs;
                            this.attributes[i + 1] = attrPrefix;
                            this.attributes[i + 2] = attrName;
                        } else {
                            throw new RuntimeException("Undefined Prefix: " + attrPrefix + " in " + this);
                        }
                    }
                    i -= 4;
                } else {
                    throw new RuntimeException("illegal attribute name: " + attrName + " at " + this);
                }
            }
        }
        cut = this.name.indexOf(58);
        if (cut == 0) {
            checkRelaxed("illegal tag name: " + this.name);
        }
        if (cut != -1) {
            this.prefix = this.name.substring(0, cut);
            this.name = this.name.substring(cut + 1);
        }
        this.namespace = getNamespace(this.prefix);
        if (this.namespace == null) {
            if (this.prefix != null) {
                checkRelaxed("undefined prefix: " + this.prefix);
            }
            this.namespace = "";
        }
        return any;
    }

    private String[] ensureCapacity(String[] arr, int required) {
        if (arr.length >= required) {
            return arr;
        }
        String[] bigger = new String[(required + 16)];
        System.arraycopy(arr, 0, bigger, 0, arr.length);
        return bigger;
    }

    private void checkRelaxed(String errorMessage) throws XmlPullParserException {
        if (!this.relaxed) {
            throw new XmlPullParserException(errorMessage, this, null);
        } else if (this.error == null) {
            this.error = "Error: " + errorMessage;
        }
    }

    public int next() throws XmlPullParserException, IOException {
        return next(false);
    }

    public int nextToken() throws XmlPullParserException, IOException {
        return next(true);
    }

    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int next(boolean justOneToken) throws IOException, XmlPullParserException {
        if (this.reader == null) {
            throw new XmlPullParserException("setInput() must be called first.", this, null);
        }
        if (this.type == 3) {
            this.depth--;
        }
        if (this.degenerated) {
            this.degenerated = false;
            this.type = 3;
            return this.type;
        }
        if (this.error != null) {
            if (justOneToken) {
                this.text = this.error;
                this.type = 9;
                this.error = null;
                return this.type;
            }
            this.error = null;
        }
        this.type = peekType(false);
        if (this.type == XML_DECLARATION) {
            readXmlDeclaration();
            this.type = peekType(false);
        }
        this.text = null;
        this.isWhitespace = true;
        this.prefix = null;
        this.name = null;
        this.namespace = null;
        this.attributeCount = -1;
        boolean throwOnResolveFailure = justOneToken ^ 1;
        while (true) {
            switch (this.type) {
                case 1:
                    return this.type;
                case 2:
                    parseStartTag(false, throwOnResolveFailure);
                    return this.type;
                case 3:
                    readEndTag();
                    return this.type;
                case 4:
                    this.text = readValue('<', justOneToken ^ 1, throwOnResolveFailure, ValueContext.TEXT);
                    if (this.depth == 0 && this.isWhitespace) {
                        this.type = 7;
                        break;
                    }
                case 5:
                    read(START_CDATA);
                    this.text = readUntil(END_CDATA, true);
                    break;
                case 6:
                    if (justOneToken) {
                        StringBuilder entityTextBuilder = new StringBuilder();
                        readEntity(entityTextBuilder, true, throwOnResolveFailure, ValueContext.TEXT);
                        this.text = entityTextBuilder.toString();
                        break;
                    }
                case 8:
                    read(START_PROCESSING_INSTRUCTION);
                    String processingInstruction = readUntil(END_PROCESSING_INSTRUCTION, justOneToken);
                    if (justOneToken) {
                        this.text = processingInstruction;
                        break;
                    }
                    break;
                case 9:
                    String commentText = readComment(justOneToken);
                    if (justOneToken) {
                        this.text = commentText;
                        break;
                    }
                    break;
                case 10:
                    readDoctype(justOneToken);
                    if (this.parsedTopLevelStartTag) {
                        throw new XmlPullParserException("Unexpected token", this, null);
                    }
                    break;
                default:
                    throw new XmlPullParserException("Unexpected token", this, null);
            }
            if (!(this.depth == 0 && (this.type == 6 || this.type == 4 || this.type == 5))) {
                if (justOneToken) {
                    return this.type;
                }
                if (this.type == 7) {
                    this.text = null;
                }
                int peek = peekType(false);
                if (this.text == null || (this.text.isEmpty() ^ 1) == 0 || peek >= 4) {
                    this.type = peek;
                } else {
                    this.type = 4;
                    return this.type;
                }
            }
        }
        throw new XmlPullParserException("Unexpected token", this, null);
    }

    private String readUntil(char[] delimiter, boolean returnText) throws IOException, XmlPullParserException {
        int start = this.position;
        StringBuilder result = null;
        if (returnText && this.text != null) {
            result = new StringBuilder();
            result.append(this.text);
        }
        while (true) {
            if (this.position + delimiter.length > this.limit) {
                if (start < this.position && returnText) {
                    if (result == null) {
                        result = new StringBuilder();
                    }
                    result.append(this.buffer, start, this.position - start);
                }
                if (fillBuffer(delimiter.length)) {
                    start = this.position;
                } else {
                    checkRelaxed(UNEXPECTED_EOF);
                    this.type = 9;
                    return null;
                }
            }
            int i = 0;
            while (i < delimiter.length) {
                if (this.buffer[this.position + i] != delimiter[i]) {
                    this.position++;
                } else {
                    i++;
                }
            }
            int end = this.position;
            this.position += delimiter.length;
            if (!returnText) {
                return null;
            }
            if (result == null) {
                return this.stringPool.get(this.buffer, start, end - start);
            }
            result.append(this.buffer, start, end - start);
            return result.toString();
        }
    }

    private void readXmlDeclaration() throws IOException, XmlPullParserException {
        if (!(this.bufferStartLine == 0 && this.bufferStartColumn == 0 && this.position == 0)) {
            checkRelaxed("processing instructions must not start with xml");
        }
        read(START_PROCESSING_INSTRUCTION);
        parseStartTag(true, true);
        if (this.attributeCount < 1 || (OutputKeys.VERSION.equals(this.attributes[2]) ^ 1) != 0) {
            checkRelaxed("version expected");
        }
        this.version = this.attributes[3];
        int pos = 1;
        if (1 < this.attributeCount && OutputKeys.ENCODING.equals(this.attributes[6])) {
            this.encoding = this.attributes[7];
            pos = 2;
        }
        if (pos < this.attributeCount && OutputKeys.STANDALONE.equals(this.attributes[(pos * 4) + 2])) {
            String st = this.attributes[(pos * 4) + 3];
            if ("yes".equals(st)) {
                this.standalone = Boolean.TRUE;
            } else if ("no".equals(st)) {
                this.standalone = Boolean.FALSE;
            } else {
                checkRelaxed("illegal standalone value: " + st);
            }
            pos++;
        }
        if (pos != this.attributeCount) {
            checkRelaxed("unexpected attributes in XML declaration");
        }
        this.isWhitespace = true;
        this.text = null;
    }

    private String readComment(boolean returnText) throws IOException, XmlPullParserException {
        read(START_COMMENT);
        if (this.relaxed) {
            return readUntil(END_COMMENT, returnText);
        }
        String commentText = readUntil(COMMENT_DOUBLE_DASH, returnText);
        if (peekCharacter() != 62) {
            throw new XmlPullParserException("Comments may not contain --", this, null);
        }
        this.position++;
        return commentText;
    }

    private void readDoctype(boolean saveDtdText) throws IOException, XmlPullParserException {
        read(START_DOCTYPE);
        int startPosition = -1;
        if (saveDtdText) {
            this.bufferCapture = new StringBuilder();
            startPosition = this.position;
        }
        try {
            skip();
            this.rootElementName = readName();
            readExternalId(true, true);
            skip();
            if (peekCharacter() == 91) {
                readInternalSubset();
            }
            skip();
            read('>');
            skip();
        } finally {
            if (saveDtdText) {
                this.bufferCapture.append(this.buffer, 0, this.position);
                this.bufferCapture.delete(0, startPosition);
                this.text = this.bufferCapture.toString();
                this.bufferCapture = null;
            }
        }
    }

    private boolean readExternalId(boolean requireSystemName, boolean assignFields) throws IOException, XmlPullParserException {
        skip();
        int c = peekCharacter();
        if (c == 83) {
            read(SYSTEM);
        } else if (c != 80) {
            return false;
        } else {
            read(PUBLIC);
            skip();
            if (assignFields) {
                this.publicId = readQuotedId(true);
            } else {
                readQuotedId(false);
            }
        }
        skip();
        if (!requireSystemName) {
            int delimiter = peekCharacter();
            if (!(delimiter == 34 || delimiter == 39)) {
                return true;
            }
        }
        if (assignFields) {
            this.systemId = readQuotedId(true);
        } else {
            readQuotedId(false);
        }
        return true;
    }

    private String readQuotedId(boolean returnText) throws IOException, XmlPullParserException {
        char[] delimiter;
        int quote = peekCharacter();
        if (quote == 34) {
            delimiter = DOUBLE_QUOTE;
        } else if (quote == 39) {
            delimiter = SINGLE_QUOTE;
        } else {
            throw new XmlPullParserException("Expected a quoted string", this, null);
        }
        this.position++;
        return readUntil(delimiter, returnText);
    }

    private void readInternalSubset() throws IOException, XmlPullParserException {
        read('[');
        while (true) {
            skip();
            if (peekCharacter() == 93) {
                this.position++;
                return;
            }
            switch (peekType(true)) {
                case 8:
                    read(START_PROCESSING_INSTRUCTION);
                    readUntil(END_PROCESSING_INSTRUCTION, false);
                    break;
                case 9:
                    readComment(false);
                    break;
                case 11:
                    readElementDeclaration();
                    break;
                case 12:
                    readEntityDeclaration();
                    break;
                case 13:
                    readAttributeListDeclaration();
                    break;
                case 14:
                    readNotationDeclaration();
                    break;
                case 15:
                    throw new XmlPullParserException("Parameter entity references are not supported", this, null);
                default:
                    throw new XmlPullParserException("Unexpected token", this, null);
            }
        }
    }

    private void readElementDeclaration() throws IOException, XmlPullParserException {
        read(START_ELEMENT);
        skip();
        readName();
        readContentSpec();
        skip();
        read('>');
    }

    private void readContentSpec() throws IOException, XmlPullParserException {
        skip();
        char c = peekCharacter();
        if (c == '(') {
            int c2;
            int depth = 0;
            do {
                if (c2 == 40) {
                    depth++;
                } else if (c2 == 41) {
                    depth--;
                } else if (c2 == -1) {
                    throw new XmlPullParserException("Unterminated element content spec", this, null);
                }
                this.position++;
                c2 = peekCharacter();
            } while (depth > 0);
            if (c2 == 42 || c2 == 63 || c2 == 43) {
                this.position++;
            }
        } else if (c2 == EMPTY[0]) {
            read(EMPTY);
        } else if (c2 == ANY[0]) {
            read(ANY);
        } else {
            throw new XmlPullParserException("Expected element content spec", this, null);
        }
    }

    private void readAttributeListDeclaration() throws IOException, XmlPullParserException {
        read(START_ATTLIST);
        skip();
        String elementName = readName();
        while (true) {
            skip();
            if (peekCharacter() == 62) {
                this.position++;
                return;
            }
            String attributeName = readName();
            skip();
            if (this.position + 1 < this.limit || (fillBuffer(2) ^ 1) == 0) {
                int c;
                if (this.buffer[this.position] == NOTATION[0] && this.buffer[this.position + 1] == NOTATION[1]) {
                    read(NOTATION);
                    skip();
                }
                if (peekCharacter() == 40) {
                    this.position++;
                    while (true) {
                        skip();
                        readName();
                        skip();
                        c = peekCharacter();
                        if (c == 41) {
                            this.position++;
                            break;
                        } else if (c == 124) {
                            this.position++;
                        } else {
                            throw new XmlPullParserException("Malformed attribute type", this, null);
                        }
                    }
                }
                readName();
                skip();
                c = peekCharacter();
                if (c == 35) {
                    this.position++;
                    c = peekCharacter();
                    if (c == 82) {
                        read(REQUIRED);
                    } else if (c == 73) {
                        read(IMPLIED);
                    } else if (c == 70) {
                        read(FIXED);
                    } else {
                        throw new XmlPullParserException("Malformed attribute type", this, null);
                    }
                    skip();
                    c = peekCharacter();
                }
                if (c == 34 || c == 39) {
                    this.position++;
                    String value = readValue((char) c, true, true, ValueContext.ATTRIBUTE);
                    if (peekCharacter() == c) {
                        this.position++;
                    }
                    defineAttributeDefault(elementName, attributeName, value);
                }
            } else {
                throw new XmlPullParserException("Malformed attribute list", this, null);
            }
        }
    }

    private void defineAttributeDefault(String elementName, String attributeName, String value) {
        if (this.defaultAttributes == null) {
            this.defaultAttributes = new HashMap();
        }
        Map<String, String> elementAttributes = (Map) this.defaultAttributes.get(elementName);
        if (elementAttributes == null) {
            elementAttributes = new HashMap();
            this.defaultAttributes.put(elementName, elementAttributes);
        }
        elementAttributes.put(attributeName, value);
    }

    private void readEntityDeclaration() throws IOException, XmlPullParserException {
        String entityValue;
        read(START_ENTITY);
        boolean generalEntity = true;
        skip();
        if (peekCharacter() == 37) {
            generalEntity = false;
            this.position++;
            skip();
        }
        String name = readName();
        skip();
        int quote = peekCharacter();
        if (quote == 34 || quote == 39) {
            this.position++;
            entityValue = readValue((char) quote, true, false, ValueContext.ENTITY_DECLARATION);
            if (peekCharacter() == quote) {
                this.position++;
            }
        } else if (readExternalId(true, false)) {
            entityValue = "";
            skip();
            if (peekCharacter() == NDATA[0]) {
                read(NDATA);
                skip();
                readName();
            }
        } else {
            throw new XmlPullParserException("Expected entity value or external ID", this, null);
        }
        if (generalEntity && this.processDocDecl) {
            if (this.documentEntities == null) {
                this.documentEntities = new HashMap();
            }
            this.documentEntities.put(name, entityValue.toCharArray());
        }
        skip();
        read('>');
    }

    private void readNotationDeclaration() throws IOException, XmlPullParserException {
        read(START_NOTATION);
        skip();
        readName();
        if (readExternalId(false, false)) {
            skip();
            read('>');
            return;
        }
        throw new XmlPullParserException("Expected external ID or public ID for notation", this, null);
    }

    private void readEndTag() throws IOException, XmlPullParserException {
        read('<');
        read('/');
        this.name = readName();
        skip();
        read('>');
        int sp = (this.depth - 1) * 4;
        if (this.depth == 0) {
            checkRelaxed("read end tag " + this.name + " with no tags open");
            this.type = 9;
            return;
        }
        if (this.name.equals(this.elementStack[sp + 3])) {
            this.namespace = this.elementStack[sp];
            this.prefix = this.elementStack[sp + 1];
            this.name = this.elementStack[sp + 2];
        } else if (!this.relaxed) {
            throw new XmlPullParserException("expected: /" + this.elementStack[sp + 3] + " read: " + this.name, this, null);
        }
    }

    private int peekType(boolean inDeclaration) throws IOException, XmlPullParserException {
        int i = 4;
        if (this.position >= this.limit && (fillBuffer(1) ^ 1) != 0) {
            return 1;
        }
        switch (this.buffer[this.position]) {
            case '%':
                if (inDeclaration) {
                    i = 15;
                }
                return i;
            case '&':
                return 6;
            case '<':
                if (this.position + 3 < this.limit || (fillBuffer(4) ^ 1) == 0) {
                    switch (this.buffer[this.position + 1]) {
                        case '!':
                            switch (this.buffer[this.position + 2]) {
                                case '-':
                                    return 9;
                                case 'A':
                                    return 13;
                                case 'D':
                                    return 10;
                                case 'E':
                                    switch (this.buffer[this.position + 3]) {
                                        case 'L':
                                            return 11;
                                        case 'N':
                                            return 12;
                                    }
                                    break;
                                case 'N':
                                    return 14;
                                case '[':
                                    return 5;
                            }
                            throw new XmlPullParserException("Unexpected <!", this, null);
                        case '/':
                            return 3;
                        case '?':
                            if ((this.position + 5 < this.limit || fillBuffer(6)) && ((this.buffer[this.position + 2] == ULocale.PRIVATE_USE_EXTENSION || this.buffer[this.position + 2] == 'X') && ((this.buffer[this.position + 3] == 'm' || this.buffer[this.position + 3] == 'M') && ((this.buffer[this.position + 4] == 'l' || this.buffer[this.position + 4] == 'L') && this.buffer[this.position + 5] == ' ')))) {
                                return XML_DECLARATION;
                            }
                            return 8;
                        default:
                            return 2;
                    }
                }
                throw new XmlPullParserException("Dangling <", this, null);
            default:
                return 4;
        }
    }

    private void parseStartTag(boolean xmldecl, boolean throwOnResolveFailure) throws IOException, XmlPullParserException {
        int i;
        int i2;
        if (!xmldecl) {
            read('<');
        }
        this.name = readName();
        this.attributeCount = 0;
        while (true) {
            skip();
            if (this.position < this.limit || (fillBuffer(1) ^ 1) == 0) {
                int c = this.buffer[this.position];
                if (!xmldecl) {
                    if (c != 47) {
                        if (c == 62) {
                            this.position++;
                            break;
                        }
                    }
                    this.degenerated = true;
                    this.position++;
                    skip();
                    read('>');
                    break;
                } else if (c == 63) {
                    this.position++;
                    read('>');
                    return;
                }
                String attrName = readName();
                i = this.attributeCount;
                this.attributeCount = i + 1;
                i2 = i * 4;
                this.attributes = ensureCapacity(this.attributes, i2 + 4);
                this.attributes[i2] = "";
                this.attributes[i2 + 1] = null;
                this.attributes[i2 + 2] = attrName;
                skip();
                if (this.position >= this.limit && (fillBuffer(1) ^ 1) != 0) {
                    checkRelaxed(UNEXPECTED_EOF);
                    return;
                } else if (this.buffer[this.position] == '=') {
                    this.position++;
                    skip();
                    if (this.position < this.limit || (fillBuffer(1) ^ 1) == 0) {
                        char delimiter = this.buffer[this.position];
                        if (delimiter == PatternTokenizer.SINGLE_QUOTE || delimiter == '\"') {
                            this.position++;
                        } else if (this.relaxed) {
                            delimiter = ' ';
                        } else {
                            throw new XmlPullParserException("attr value delimiter missing!", this, null);
                        }
                        this.attributes[i2 + 3] = readValue(delimiter, true, throwOnResolveFailure, ValueContext.ATTRIBUTE);
                        if (delimiter != ' ' && peekCharacter() == delimiter) {
                            this.position++;
                        }
                    } else {
                        checkRelaxed(UNEXPECTED_EOF);
                        return;
                    }
                } else if (this.relaxed) {
                    this.attributes[i2 + 3] = attrName;
                } else {
                    checkRelaxed("Attr.value missing f. " + attrName);
                    this.attributes[i2 + 3] = attrName;
                }
            } else {
                checkRelaxed(UNEXPECTED_EOF);
                return;
            }
        }
        i = this.depth;
        this.depth = i + 1;
        int sp = i * 4;
        if (this.depth == 1) {
            this.parsedTopLevelStartTag = true;
        }
        this.elementStack = ensureCapacity(this.elementStack, sp + 4);
        this.elementStack[sp + 3] = this.name;
        if (this.depth >= this.nspCounts.length) {
            int[] bigger = new int[(this.depth + 4)];
            System.arraycopy(this.nspCounts, 0, bigger, 0, this.nspCounts.length);
            this.nspCounts = bigger;
        }
        this.nspCounts[this.depth] = this.nspCounts[this.depth - 1];
        if (this.processNsp) {
            adjustNsp();
        } else {
            this.namespace = "";
        }
        if (this.defaultAttributes != null) {
            Map<String, String> elementDefaultAttributes = (Map) this.defaultAttributes.get(this.name);
            if (elementDefaultAttributes != null) {
                for (Entry<String, String> entry : elementDefaultAttributes.entrySet()) {
                    if (getAttributeValue(null, (String) entry.getKey()) == null) {
                        i = this.attributeCount;
                        this.attributeCount = i + 1;
                        i2 = i * 4;
                        this.attributes = ensureCapacity(this.attributes, i2 + 4);
                        this.attributes[i2] = "";
                        this.attributes[i2 + 1] = null;
                        this.attributes[i2 + 2] = (String) entry.getKey();
                        this.attributes[i2 + 3] = (String) entry.getValue();
                    }
                }
            }
        }
        this.elementStack[sp] = this.namespace;
        this.elementStack[sp + 1] = this.prefix;
        this.elementStack[sp + 2] = this.name;
    }

    private void readEntity(StringBuilder out, boolean isEntityToken, boolean throwOnResolveFailure, ValueContext valueContext) throws IOException, XmlPullParserException {
        int start = out.length();
        char[] cArr = this.buffer;
        int i = this.position;
        this.position = i + 1;
        if (cArr[i] != '&') {
            throw new AssertionError();
        }
        out.append('&');
        while (true) {
            int c = peekCharacter();
            if (c == 59) {
                out.append(';');
                this.position++;
                String code = out.substring(start + 1, out.length() - 1);
                if (isEntityToken) {
                    this.name = code;
                }
                if (code.startsWith("#")) {
                    try {
                        if (code.startsWith("#x")) {
                            c = Integer.parseInt(code.substring(2), 16);
                        } else {
                            c = Integer.parseInt(code.substring(1));
                        }
                        out.delete(start, out.length());
                        out.appendCodePoint(c);
                        this.unresolved = false;
                        return;
                    } catch (NumberFormatException e) {
                        throw new XmlPullParserException("Invalid character reference: &" + code);
                    } catch (IllegalArgumentException e2) {
                        throw new XmlPullParserException("Invalid character reference: &" + code);
                    }
                } else if (valueContext != ValueContext.ENTITY_DECLARATION) {
                    String defaultEntity = (String) DEFAULT_ENTITIES.get(code);
                    if (defaultEntity != null) {
                        out.delete(start, out.length());
                        this.unresolved = false;
                        out.append(defaultEntity);
                        return;
                    }
                    if (this.documentEntities != null) {
                        char[] resolved = (char[]) this.documentEntities.get(code);
                        if (resolved != null) {
                            out.delete(start, out.length());
                            this.unresolved = false;
                            if (this.processDocDecl) {
                                pushContentSource(resolved);
                            } else {
                                out.append(resolved);
                            }
                            return;
                        }
                    }
                    if (this.systemId != null) {
                        out.delete(start, out.length());
                        return;
                    }
                    this.unresolved = true;
                    if (throwOnResolveFailure) {
                        checkRelaxed("unresolved: &" + code + ";");
                    }
                    return;
                } else {
                    return;
                }
            } else if (c >= 128 || ((c >= 48 && c <= 57) || ((c >= 97 && c <= 122) || ((c >= 65 && c <= 90) || c == 95 || c == 45 || c == 35)))) {
                this.position++;
                out.append((char) c);
            } else if (!this.relaxed) {
                throw new XmlPullParserException("unterminated entity ref", this, null);
            } else {
                return;
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:96:0x014f  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x005c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String readValue(char delimiter, boolean resolveEntities, boolean throwOnResolveFailure, ValueContext valueContext) throws IOException, XmlPullParserException {
        int start = this.position;
        StringBuilder result = null;
        if (valueContext == ValueContext.TEXT && this.text != null) {
            result = new StringBuilder();
            result.append(this.text);
        }
        while (true) {
            if (this.position >= this.limit) {
                if (start < this.position) {
                    if (result == null) {
                        result = new StringBuilder();
                    }
                    result.append(this.buffer, start, this.position - start);
                }
                if (fillBuffer(1)) {
                    start = this.position;
                } else {
                    return result != null ? result.toString() : "";
                }
            }
            char c = this.buffer[this.position];
            if (c == delimiter || ((delimiter == ' ' && (c <= ' ' || c == '>')) || (c == '&' && (resolveEntities ^ 1) != 0))) {
                if (result != null) {
                    return this.stringPool.get(this.buffer, start, this.position - start);
                }
                result.append(this.buffer, start, this.position - start);
                return result.toString();
            } else if (c == 13 || ((c == 10 && valueContext == ValueContext.ATTRIBUTE) || c == '&' || c == '<' || ((c == ']' && valueContext == ValueContext.TEXT) || (c == '%' && valueContext == ValueContext.ENTITY_DECLARATION)))) {
                if (result == null) {
                    result = new StringBuilder();
                }
                result.append(this.buffer, start, this.position - start);
                if (c == 13) {
                    if ((this.position + 1 < this.limit || fillBuffer(2)) && this.buffer[this.position + 1] == 10) {
                        this.position++;
                    }
                    c = valueContext == ValueContext.ATTRIBUTE ? ' ' : 10;
                } else if (c == 10) {
                    c = ' ';
                } else if (c == '&') {
                    this.isWhitespace = false;
                    readEntity(result, false, throwOnResolveFailure, valueContext);
                    start = this.position;
                } else if (c == '<') {
                    if (valueContext == ValueContext.ATTRIBUTE) {
                        checkRelaxed("Illegal: \"<\" inside attribute value");
                    }
                    this.isWhitespace = false;
                } else if (c == ']') {
                    if ((this.position + 2 < this.limit || fillBuffer(3)) && this.buffer[this.position + 1] == ']' && this.buffer[this.position + 2] == '>') {
                        checkRelaxed("Illegal: \"]]>\" outside CDATA section");
                    }
                    this.isWhitespace = false;
                } else if (c == '%') {
                    throw new XmlPullParserException("This parser doesn't support parameter entities", this, null);
                } else {
                    throw new AssertionError();
                }
                this.position++;
                result.append(c);
                start = this.position;
            } else {
                int i;
                boolean z = this.isWhitespace;
                if (c <= ' ') {
                    i = 1;
                } else {
                    i = 0;
                }
                this.isWhitespace = i & z;
                this.position++;
            }
        }
        if (result != null) {
        }
    }

    private void read(char expected) throws IOException, XmlPullParserException {
        char c = peekCharacter();
        if (c != expected) {
            checkRelaxed("expected: '" + expected + "' actual: '" + ((char) c) + "'");
            if (c == 65535) {
                return;
            }
        }
        this.position++;
    }

    private void read(char[] chars) throws IOException, XmlPullParserException {
        if (this.position + chars.length <= this.limit || (fillBuffer(chars.length) ^ 1) == 0) {
            for (int i = 0; i < chars.length; i++) {
                if (this.buffer[this.position + i] != chars[i]) {
                    checkRelaxed("expected: \"" + new String(chars) + "\" but was \"" + new String(this.buffer, this.position, chars.length) + "...\"");
                }
            }
            this.position += chars.length;
            return;
        }
        checkRelaxed("expected: '" + new String(chars) + "' but was EOF");
    }

    private int peekCharacter() throws IOException, XmlPullParserException {
        if (this.position < this.limit || fillBuffer(1)) {
            return this.buffer[this.position];
        }
        return -1;
    }

    private boolean fillBuffer(int minimum) throws IOException, XmlPullParserException {
        while (this.nextContentSource != null) {
            if (this.position < this.limit) {
                throw new XmlPullParserException("Unbalanced entity!", this, null);
            }
            popContentSource();
            if (this.limit - this.position >= minimum) {
                return true;
            }
        }
        for (int i = 0; i < this.position; i++) {
            if (this.buffer[i] == 10) {
                this.bufferStartLine++;
                this.bufferStartColumn = 0;
            } else {
                this.bufferStartColumn++;
            }
        }
        if (this.bufferCapture != null) {
            this.bufferCapture.append(this.buffer, 0, this.position);
        }
        if (this.limit != this.position) {
            this.limit -= this.position;
            System.arraycopy(this.buffer, this.position, this.buffer, 0, this.limit);
        } else {
            this.limit = 0;
        }
        this.position = 0;
        do {
            int total = this.reader.read(this.buffer, this.limit, this.buffer.length - this.limit);
            if (total == -1) {
                return false;
            }
            this.limit += total;
        } while (this.limit < minimum);
        return true;
    }

    private String readName() throws IOException, XmlPullParserException {
        if (this.position < this.limit || (fillBuffer(1) ^ 1) == 0) {
            int start = this.position;
            StringBuilder result = null;
            char c = this.buffer[this.position];
            if ((c < 'a' || c > 'z') && !((c >= 'A' && c <= 'Z') || c == '_' || c == ':' || c >= 192 || this.relaxed)) {
                checkRelaxed("name expected");
                return "";
            }
            this.position++;
            while (true) {
                if (this.position >= this.limit) {
                    if (result == null) {
                        result = new StringBuilder();
                    }
                    result.append(this.buffer, start, this.position - start);
                    if (!fillBuffer(1)) {
                        return result.toString();
                    }
                    start = this.position;
                }
                c = this.buffer[this.position];
                if ((c >= 'a' && c <= 'z') || ((c >= 'A' && c <= 'Z') || ((c >= '0' && c <= '9') || c == '_' || c == '-' || c == ':' || c == '.' || c >= 183))) {
                    this.position++;
                } else if (result == null) {
                    return this.stringPool.get(this.buffer, start, this.position - start);
                } else {
                    result.append(this.buffer, start, this.position - start);
                    return result.toString();
                }
            }
        }
        checkRelaxed("name expected");
        return "";
    }

    private void skip() throws IOException, XmlPullParserException {
        while (true) {
            if ((this.position < this.limit || fillBuffer(1)) && this.buffer[this.position] <= 32) {
                this.position++;
            } else {
                return;
            }
        }
    }

    public void setInput(Reader reader) throws XmlPullParserException {
        this.reader = reader;
        this.type = 0;
        this.parsedTopLevelStartTag = false;
        this.name = null;
        this.namespace = null;
        this.degenerated = false;
        this.attributeCount = -1;
        this.encoding = null;
        this.version = null;
        this.standalone = null;
        if (reader != null) {
            this.position = 0;
            this.limit = 0;
            this.bufferStartLine = 0;
            this.bufferStartColumn = 0;
            this.depth = 0;
            this.documentEntities = null;
        }
    }

    /* JADX WARNING: Missing block: B:40:0x0153, code:
            if (r6 == -1) goto L_0x005d;
     */
    /* JADX WARNING: Missing block: B:41:0x0155, code:
            r12 = r17.buffer;
            r13 = r17.limit;
            r17.limit = r13 + 1;
            r12[r13] = (char) r6;
     */
    /* JADX WARNING: Missing block: B:43:0x016a, code:
            r10 = new java.lang.String(r17.buffer, 0, r17.limit);
            r7 = r10.indexOf(javax.xml.transform.OutputKeys.ENCODING);
     */
    /* JADX WARNING: Missing block: B:44:0x0180, code:
            if (r7 == -1) goto L_0x005d;
     */
    /* JADX WARNING: Missing block: B:45:0x0183, code:
            r8 = r7;
     */
    /* JADX WARNING: Missing block: B:46:0x0189, code:
            if (r10.charAt(r8) == '\"') goto L_0x0197;
     */
    /* JADX WARNING: Missing block: B:48:0x0191, code:
            if (r10.charAt(r8) == android.icu.impl.PatternTokenizer.SINGLE_QUOTE) goto L_0x0197;
     */
    /* JADX WARNING: Missing block: B:49:0x0193, code:
            r7 = r8 + 1;
     */
    /* JADX WARNING: Missing block: B:50:0x0197, code:
            r7 = r8 + 1;
            r19 = r10.substring(r7, r10.indexOf(r10.charAt(r8), r7));
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setInput(InputStream is, String charset) throws XmlPullParserException {
        this.position = 0;
        this.limit = 0;
        boolean detectCharset = charset == null;
        if (is == null) {
            throw new IllegalArgumentException("is == null");
        }
        if (detectCharset) {
            int i;
            int firstFourBytes = 0;
            while (this.limit < 4) {
                try {
                    i = is.read();
                    if (i == -1) {
                        break;
                    }
                    firstFourBytes = (firstFourBytes << 8) | i;
                    char[] cArr = this.buffer;
                    int i2 = this.limit;
                    this.limit = i2 + 1;
                    cArr[i2] = (char) i;
                } catch (Exception e) {
                    throw new XmlPullParserException("Invalid stream or encoding: " + e, this, e);
                }
            }
            if (this.limit == 4) {
                switch (firstFourBytes) {
                    case -131072:
                        charset = "UTF-32LE";
                        this.limit = 0;
                        break;
                    case 60:
                        charset = "UTF-32BE";
                        this.buffer[0] = '<';
                        this.limit = 1;
                        break;
                    case 65279:
                        charset = "UTF-32BE";
                        this.limit = 0;
                        break;
                    case 3932223:
                        charset = "UTF-16BE";
                        this.buffer[0] = '<';
                        this.buffer[1] = '?';
                        this.limit = 2;
                        break;
                    case 1006632960:
                        charset = "UTF-32LE";
                        this.buffer[0] = '<';
                        this.limit = 1;
                        break;
                    case 1006649088:
                        charset = "UTF-16LE";
                        this.buffer[0] = '<';
                        this.buffer[1] = '?';
                        this.limit = 2;
                        break;
                    case 1010792557:
                        do {
                            i = is.read();
                            break;
                        } while (i != 62);
                        break;
                    default:
                        if ((-65536 & firstFourBytes) != -16842752) {
                            if ((-65536 & firstFourBytes) != -131072) {
                                if ((firstFourBytes & -256) == -272908544) {
                                    charset = "UTF-8";
                                    this.buffer[0] = this.buffer[3];
                                    this.limit = 1;
                                    break;
                                }
                            }
                            charset = "UTF-16LE";
                            this.buffer[0] = (char) ((this.buffer[3] << 8) | this.buffer[2]);
                            this.limit = 1;
                            break;
                        }
                        charset = "UTF-16BE";
                        this.buffer[0] = (char) ((this.buffer[2] << 8) | this.buffer[3]);
                        this.limit = 1;
                        break;
                        break;
                }
            }
        }
        if (charset == null) {
            charset = "UTF-8";
        }
        int savedLimit = this.limit;
        setInput(new InputStreamReader(is, charset));
        this.encoding = charset;
        this.limit = savedLimit;
        if (!detectCharset && peekCharacter() == 65279) {
            this.limit--;
            System.arraycopy(this.buffer, 1, this.buffer, 0, this.limit);
        }
    }

    public void close() throws IOException {
        if (this.reader != null) {
            this.reader.close();
        }
    }

    public boolean getFeature(String feature) {
        if (XmlPullParser.FEATURE_PROCESS_NAMESPACES.equals(feature)) {
            return this.processNsp;
        }
        if (FEATURE_RELAXED.equals(feature)) {
            return this.relaxed;
        }
        if (XmlPullParser.FEATURE_PROCESS_DOCDECL.equals(feature)) {
            return this.processDocDecl;
        }
        return false;
    }

    public String getInputEncoding() {
        return this.encoding;
    }

    public void defineEntityReplacementText(String entity, String value) throws XmlPullParserException {
        if (this.processDocDecl) {
            throw new IllegalStateException("Entity replacement text may not be defined with DOCTYPE processing enabled.");
        } else if (this.reader == null) {
            throw new IllegalStateException("Entity replacement text must be defined after setInput()");
        } else {
            if (this.documentEntities == null) {
                this.documentEntities = new HashMap();
            }
            this.documentEntities.put(entity, value.toCharArray());
        }
    }

    public Object getProperty(String property) {
        if (property.equals(PROPERTY_XMLDECL_VERSION)) {
            return this.version;
        }
        if (property.equals(PROPERTY_XMLDECL_STANDALONE)) {
            return this.standalone;
        }
        if (!property.equals(PROPERTY_LOCATION)) {
            return null;
        }
        return this.location != null ? this.location : this.reader.toString();
    }

    public String getRootElementName() {
        return this.rootElementName;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public String getPublicId() {
        return this.publicId;
    }

    public int getNamespaceCount(int depth) {
        if (depth <= this.depth) {
            return this.nspCounts[depth];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getNamespacePrefix(int pos) {
        return this.nspStack[pos * 2];
    }

    public String getNamespaceUri(int pos) {
        return this.nspStack[(pos * 2) + 1];
    }

    public String getNamespace(String prefix) {
        if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            return "http://www.w3.org/XML/1998/namespace";
        }
        if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        for (int i = (getNamespaceCount(this.depth) << 1) - 2; i >= 0; i -= 2) {
            if (prefix == null) {
                if (this.nspStack[i] == null) {
                    return this.nspStack[i + 1];
                }
            } else if (prefix.equals(this.nspStack[i])) {
                return this.nspStack[i + 1];
            }
        }
        return null;
    }

    public int getDepth() {
        return this.depth;
    }

    public String getPositionDescription() {
        StringBuilder buf = new StringBuilder(this.type < TYPES.length ? TYPES[this.type] : "unknown");
        buf.append(' ');
        if (this.type == 2 || this.type == 3) {
            if (this.degenerated) {
                buf.append("(empty) ");
            }
            buf.append('<');
            if (this.type == 3) {
                buf.append('/');
            }
            if (this.prefix != null) {
                buf.append("{").append(this.namespace).append("}").append(this.prefix).append(":");
            }
            buf.append(this.name);
            int cnt = this.attributeCount * 4;
            for (int i = 0; i < cnt; i += 4) {
                buf.append(' ');
                if (this.attributes[i + 1] != null) {
                    buf.append("{").append(this.attributes[i]).append("}").append(this.attributes[i + 1]).append(":");
                }
                buf.append(this.attributes[i + 2]).append("='").append(this.attributes[i + 3]).append("'");
            }
            buf.append('>');
        } else if (this.type != 7) {
            if (this.type != 4) {
                buf.append(getText());
            } else if (this.isWhitespace) {
                buf.append("(whitespace)");
            } else {
                String text = getText();
                if (text.length() > 16) {
                    text = text.substring(0, 16) + "...";
                }
                buf.append(text);
            }
        }
        buf.append("@").append(getLineNumber()).append(":").append(getColumnNumber());
        if (this.location != null) {
            buf.append(" in ");
            buf.append(this.location);
        } else if (this.reader != null) {
            buf.append(" in ");
            buf.append(this.reader.toString());
        }
        return buf.toString();
    }

    public int getLineNumber() {
        int result = this.bufferStartLine;
        for (int i = 0; i < this.position; i++) {
            if (this.buffer[i] == 10) {
                result++;
            }
        }
        return result + 1;
    }

    public int getColumnNumber() {
        int result = this.bufferStartColumn;
        for (int i = 0; i < this.position; i++) {
            if (this.buffer[i] == 10) {
                result = 0;
            } else {
                result++;
            }
        }
        return result + 1;
    }

    public boolean isWhitespace() throws XmlPullParserException {
        if (this.type == 4 || this.type == 7 || this.type == 5) {
            return this.isWhitespace;
        }
        throw new XmlPullParserException(ILLEGAL_TYPE, this, null);
    }

    public String getText() {
        if (this.type < 4 || (this.type == 6 && this.unresolved)) {
            return null;
        }
        if (this.text == null) {
            return "";
        }
        return this.text;
    }

    public char[] getTextCharacters(int[] poslen) {
        String text = getText();
        if (text == null) {
            poslen[0] = -1;
            poslen[1] = -1;
            return null;
        }
        char[] result = text.toCharArray();
        poslen[0] = 0;
        poslen[1] = result.length;
        return result;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getName() {
        return this.name;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public boolean isEmptyElementTag() throws XmlPullParserException {
        if (this.type == 2) {
            return this.degenerated;
        }
        throw new XmlPullParserException(ILLEGAL_TYPE, this, null);
    }

    public int getAttributeCount() {
        return this.attributeCount;
    }

    public String getAttributeType(int index) {
        return "CDATA";
    }

    public boolean isAttributeDefault(int index) {
        return false;
    }

    public String getAttributeNamespace(int index) {
        if (index < this.attributeCount) {
            return this.attributes[index * 4];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributeName(int index) {
        if (index < this.attributeCount) {
            return this.attributes[(index * 4) + 2];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributePrefix(int index) {
        if (index < this.attributeCount) {
            return this.attributes[(index * 4) + 1];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributeValue(int index) {
        if (index < this.attributeCount) {
            return this.attributes[(index * 4) + 3];
        }
        throw new IndexOutOfBoundsException();
    }

    public String getAttributeValue(String namespace, String name) {
        int i = (this.attributeCount * 4) - 4;
        while (i >= 0) {
            if (this.attributes[i + 2].equals(name) && (namespace == null || this.attributes[i].equals(namespace))) {
                return this.attributes[i + 3];
            }
            i -= 4;
        }
        return null;
    }

    public int getEventType() throws XmlPullParserException {
        return this.type;
    }

    public int nextTag() throws XmlPullParserException, IOException {
        next();
        if (this.type == 4 && this.isWhitespace) {
            next();
        }
        if (this.type == 3 || this.type == 2) {
            return this.type;
        }
        throw new XmlPullParserException("unexpected type", this, null);
    }

    public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {
        if (type != this.type || ((namespace != null && (namespace.equals(getNamespace()) ^ 1) != 0) || (name != null && (name.equals(getName()) ^ 1) != 0))) {
            throw new XmlPullParserException("expected: " + TYPES[type] + " {" + namespace + "}" + name, this, null);
        }
    }

    public String nextText() throws XmlPullParserException, IOException {
        if (this.type != 2) {
            throw new XmlPullParserException("precondition: START_TAG", this, null);
        }
        String result;
        next();
        if (this.type == 4) {
            result = getText();
            next();
        } else {
            result = "";
        }
        if (this.type == 3) {
            return result;
        }
        throw new XmlPullParserException("END_TAG expected", this, null);
    }

    public void setFeature(String feature, boolean value) throws XmlPullParserException {
        if (XmlPullParser.FEATURE_PROCESS_NAMESPACES.equals(feature)) {
            this.processNsp = value;
        } else if (XmlPullParser.FEATURE_PROCESS_DOCDECL.equals(feature)) {
            this.processDocDecl = value;
        } else if (FEATURE_RELAXED.equals(feature)) {
            this.relaxed = value;
        } else {
            throw new XmlPullParserException("unsupported feature: " + feature, this, null);
        }
    }

    public void setProperty(String property, Object value) throws XmlPullParserException {
        if (property.equals(PROPERTY_LOCATION)) {
            this.location = String.valueOf(value);
            return;
        }
        throw new XmlPullParserException("unsupported property: " + property);
    }

    private void pushContentSource(char[] newBuffer) {
        this.nextContentSource = new ContentSource(this.nextContentSource, this.buffer, this.position, this.limit);
        this.buffer = newBuffer;
        this.position = 0;
        this.limit = newBuffer.length;
    }

    private void popContentSource() {
        this.buffer = this.nextContentSource.buffer;
        this.position = this.nextContentSource.position;
        this.limit = this.nextContentSource.limit;
        this.nextContentSource = this.nextContentSource.next;
    }
}
