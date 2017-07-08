package org.apache.harmony.xml;

import android.icu.text.PluralRules;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import libcore.io.IoUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xmlpull.v1.XmlPullParser;

class ExpatParser {
    private static final int BUFFER_SIZE = 8096;
    static final String CHARACTER_ENCODING = "UTF-16";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String OUTSIDE_START_ELEMENT = "Attributes can only be used within the scope of startElement().";
    private static final int TIMEOUT = 20000;
    private int attributeCount;
    private long attributePointer;
    private final ExpatAttributes attributes;
    private final String encoding;
    private boolean inStartElement;
    private final Locator locator;
    private long pointer;
    private final String publicId;
    private final String systemId;
    private final ExpatReader xmlReader;

    private static class ClonedAttributes extends ExpatAttributes {
        private static final Attributes EMPTY = null;
        private final int length;
        private final long parserPointer;
        private long pointer;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.harmony.xml.ExpatParser.ClonedAttributes.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.ClonedAttributes.<clinit>():void");
        }

        private ClonedAttributes(long parserPointer, long pointer, int length) {
            this.parserPointer = parserPointer;
            this.pointer = pointer;
            this.length = length;
        }

        public long getParserPointer() {
            return this.parserPointer;
        }

        public long getPointer() {
            return this.pointer;
        }

        public int getLength() {
            return this.length;
        }

        protected synchronized void finalize() throws Throwable {
            try {
                if (this.pointer != 0) {
                    freeAttributes(this.pointer);
                    this.pointer = 0;
                }
                super.finalize();
            } catch (Throwable th) {
                super.finalize();
            }
        }
    }

    private class CurrentAttributes extends ExpatAttributes {
        final /* synthetic */ ExpatParser this$0;

        /* synthetic */ CurrentAttributes(ExpatParser this$0, CurrentAttributes currentAttributes) {
            this(this$0);
        }

        private CurrentAttributes(ExpatParser this$0) {
            this.this$0 = this$0;
        }

        public long getParserPointer() {
            return this.this$0.pointer;
        }

        public long getPointer() {
            if (this.this$0.inStartElement) {
                return this.this$0.attributePointer;
            }
            throw new IllegalStateException(ExpatParser.OUTSIDE_START_ELEMENT);
        }

        public int getLength() {
            if (this.this$0.inStartElement) {
                return this.this$0.attributeCount;
            }
            throw new IllegalStateException(ExpatParser.OUTSIDE_START_ELEMENT);
        }
    }

    private static class EntityParser extends ExpatParser {
        private int depth;

        /* synthetic */ EntityParser(String encoding, ExpatReader xmlReader, long pointer, String publicId, String systemId, EntityParser entityParser) {
            this(encoding, xmlReader, pointer, publicId, systemId);
        }

        private EntityParser(String encoding, ExpatReader xmlReader, long pointer, String publicId, String systemId) {
            super(encoding, xmlReader, pointer, publicId, systemId, null);
            this.depth = 0;
        }

        void startElement(String uri, String localName, String qName, long attributePointer, int attributeCount) throws SAXException {
            int i = this.depth;
            this.depth = i + 1;
            if (i > 0) {
                super.startElement(uri, localName, qName, attributePointer, attributeCount);
            }
        }

        void endElement(String uri, String localName, String qName) throws SAXException {
            int i = this.depth - 1;
            this.depth = i;
            if (i > 0) {
                super.endElement(uri, localName, qName);
            }
        }

        protected synchronized void finalize() throws Throwable {
        }
    }

    private class ExpatLocator implements Locator {
        final /* synthetic */ ExpatParser this$0;

        /* synthetic */ ExpatLocator(ExpatParser this$0, ExpatLocator expatLocator) {
            this(this$0);
        }

        private ExpatLocator(ExpatParser this$0) {
            this.this$0 = this$0;
        }

        public String getPublicId() {
            return this.this$0.publicId;
        }

        public String getSystemId() {
            return this.this$0.systemId;
        }

        public int getLineNumber() {
            return this.this$0.line();
        }

        public int getColumnNumber() {
            return this.this$0.column();
        }

        public String toString() {
            return "Locator[publicId: " + this.this$0.publicId + ", systemId: " + this.this$0.systemId + ", line: " + getLineNumber() + ", column: " + getColumnNumber() + "]";
        }
    }

    private static class ParseException extends SAXParseException {
        /* synthetic */ ParseException(String message, Locator locator, ParseException parseException) {
            this(message, locator);
        }

        private ParseException(String message, Locator locator) {
            super(makeMessage(message, locator), locator);
        }

        private static String makeMessage(String message, Locator locator) {
            return makeMessage(message, locator.getLineNumber(), locator.getColumnNumber());
        }

        private static String makeMessage(String message, int line, int column) {
            return "At line " + line + ", column " + column + PluralRules.KEYWORD_RULE_SEPARATOR + message;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.apache.harmony.xml.ExpatParser.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.apache.harmony.xml.ExpatParser.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.apache.harmony.xml.ExpatParser.<clinit>():void");
    }

    /* synthetic */ ExpatParser(String encoding, ExpatReader xmlReader, long pointer, String publicId, String systemId, ExpatParser expatParser) {
        this(encoding, xmlReader, pointer, publicId, systemId);
    }

    private native void appendBytes(long j, byte[] bArr, int i, int i2) throws SAXException, ExpatException;

    private native void appendChars(long j, char[] cArr, int i, int i2) throws SAXException, ExpatException;

    private native void appendString(long j, String str, boolean z) throws SAXException, ExpatException;

    private static native long cloneAttributes(long j, int i);

    private static native int column(long j);

    private static native long createEntityParser(long j, String str);

    private native long initialize(String str, boolean z);

    private static native int line(long j);

    private native void release(long j);

    private static native void releaseParser(long j);

    private static native void staticInitialize(String str);

    ExpatParser(String encoding, ExpatReader xmlReader, boolean processNamespaces, String publicId, String systemId) {
        this.inStartElement = false;
        this.attributeCount = -1;
        this.attributePointer = 0;
        this.locator = new ExpatLocator();
        this.attributes = new CurrentAttributes();
        this.publicId = publicId;
        this.systemId = systemId;
        this.xmlReader = xmlReader;
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        this.encoding = encoding;
        this.pointer = initialize(this.encoding, processNamespaces);
    }

    private ExpatParser(String encoding, ExpatReader xmlReader, long pointer, String publicId, String systemId) {
        this.inStartElement = false;
        this.attributeCount = -1;
        this.attributePointer = 0;
        this.locator = new ExpatLocator();
        this.attributes = new CurrentAttributes();
        this.encoding = encoding;
        this.xmlReader = xmlReader;
        this.pointer = pointer;
        this.systemId = systemId;
        this.publicId = publicId;
    }

    void startElement(String uri, String localName, String qName, long attributePointer, int attributeCount) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            try {
                this.inStartElement = true;
                this.attributePointer = attributePointer;
                this.attributeCount = attributeCount;
                contentHandler.startElement(uri, localName, qName, this.attributes);
            } finally {
                this.inStartElement = false;
                this.attributeCount = -1;
                this.attributePointer = 0;
            }
        }
    }

    void endElement(String uri, String localName, String qName) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.endElement(uri, localName, qName);
        }
    }

    void text(char[] text, int length) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.characters(text, 0, length);
        }
    }

    void comment(char[] text, int length) throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.comment(text, 0, length);
        }
    }

    void startCdata() throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startCDATA();
        }
    }

    void endCdata() throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endCDATA();
        }
    }

    void startNamespace(String prefix, String uri) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.startPrefixMapping(prefix, uri);
        }
    }

    void endNamespace(String prefix) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.endPrefixMapping(prefix);
        }
    }

    void startDtd(String name, String publicId, String systemId) throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startDTD(name, publicId, systemId);
        }
    }

    void endDtd() throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endDTD();
        }
    }

    void processingInstruction(String target, String data) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.processingInstruction(target, data);
        }
    }

    void notationDecl(String name, String publicId, String systemId) throws SAXException {
        DTDHandler dtdHandler = this.xmlReader.dtdHandler;
        if (dtdHandler != null) {
            dtdHandler.notationDecl(name, publicId, systemId);
        }
    }

    void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {
        DTDHandler dtdHandler = this.xmlReader.dtdHandler;
        if (dtdHandler != null) {
            dtdHandler.unparsedEntityDecl(name, publicId, systemId, notationName);
        }
    }

    void handleExternalEntity(String context, String publicId, String systemId) throws SAXException, IOException {
        EntityResolver entityResolver = this.xmlReader.entityResolver;
        if (entityResolver != null) {
            if (this.systemId != null) {
                try {
                    URI systemUri = new URI(systemId);
                    if (!(systemUri.isAbsolute() || systemUri.isOpaque())) {
                        systemId = new URI(this.systemId).resolve(systemUri).toString();
                    }
                } catch (Exception e) {
                    System.logI("Could not resolve '" + systemId + "' relative to" + " '" + this.systemId + "' at " + this.locator, e);
                }
            }
            InputSource inputSource = entityResolver.resolveEntity(publicId, systemId);
            if (inputSource != null) {
                String encoding = pickEncoding(inputSource);
                long pointer = createEntityParser(this.pointer, context);
                try {
                    parseExternalEntity(new EntityParser(encoding, this.xmlReader, pointer, inputSource.getPublicId(), inputSource.getSystemId(), null), inputSource);
                } finally {
                    releaseParser(pointer);
                }
            }
        }
    }

    private String pickEncoding(InputSource inputSource) {
        if (inputSource.getCharacterStream() != null) {
            return CHARACTER_ENCODING;
        }
        String encoding = inputSource.getEncoding();
        if (encoding == null) {
            encoding = DEFAULT_ENCODING;
        }
        return encoding;
    }

    private void parseExternalEntity(ExpatParser entityParser, InputSource inputSource) throws IOException, SAXException {
        AutoCloseable reader = inputSource.getCharacterStream();
        if (reader != null) {
            try {
                entityParser.append("<externalEntity>");
                entityParser.parseFragment((Reader) reader);
                entityParser.append("</externalEntity>");
            } finally {
                IoUtils.closeQuietly(reader);
            }
        } else {
            AutoCloseable in = inputSource.getByteStream();
            if (in != null) {
                try {
                    entityParser.append("<externalEntity>".getBytes(entityParser.encoding));
                    entityParser.parseFragment((InputStream) in);
                    entityParser.append("</externalEntity>".getBytes(entityParser.encoding));
                } finally {
                    IoUtils.closeQuietly(in);
                }
            } else {
                String systemId = inputSource.getSystemId();
                if (systemId == null) {
                    throw new ParseException("No input specified.", this.locator, null);
                }
                in = openUrl(systemId);
                try {
                    entityParser.append("<externalEntity>".getBytes(entityParser.encoding));
                    entityParser.parseFragment((InputStream) in);
                    entityParser.append("</externalEntity>".getBytes(entityParser.encoding));
                } finally {
                    IoUtils.closeQuietly(in);
                }
            }
        }
    }

    void append(String xml) throws SAXException {
        try {
            appendString(this.pointer, xml, false);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator, null);
        }
    }

    void append(char[] xml, int offset, int length) throws SAXException {
        try {
            appendChars(this.pointer, xml, offset, length);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator, null);
        }
    }

    void append(byte[] xml) throws SAXException {
        append(xml, 0, xml.length);
    }

    void append(byte[] xml, int offset, int length) throws SAXException {
        try {
            appendBytes(this.pointer, xml, offset, length);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator, null);
        }
    }

    void parseDocument(InputStream in) throws IOException, SAXException {
        startDocument();
        parseFragment(in);
        finish();
        endDocument();
    }

    void parseDocument(Reader in) throws IOException, SAXException {
        startDocument();
        parseFragment(in);
        finish();
        endDocument();
    }

    private void parseFragment(Reader in) throws IOException, SAXException {
        char[] buffer = new char[4048];
        while (true) {
            int length = in.read(buffer);
            if (length != -1) {
                try {
                    appendChars(this.pointer, buffer, 0, length);
                } catch (ExpatException e) {
                    throw new ParseException(e.getMessage(), this.locator, null);
                }
            }
            return;
        }
    }

    private void parseFragment(InputStream in) throws IOException, SAXException {
        byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            int length = in.read(buffer);
            if (length != -1) {
                try {
                    appendBytes(this.pointer, buffer, 0, length);
                } catch (ExpatException e) {
                    throw new ParseException(e.getMessage(), this.locator, null);
                }
            }
            return;
        }
    }

    private void startDocument() throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.setDocumentLocator(this.locator);
            contentHandler.startDocument();
        }
    }

    private void endDocument() throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.endDocument();
        }
    }

    void finish() throws SAXException {
        try {
            appendString(this.pointer, XmlPullParser.NO_NAMESPACE, true);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator, null);
        }
    }

    protected synchronized void finalize() throws Throwable {
        try {
            if (this.pointer != 0) {
                release(this.pointer);
                this.pointer = 0;
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    private int line() {
        return line(this.pointer);
    }

    private int column() {
        return column(this.pointer);
    }

    Attributes cloneAttributes() {
        if (!this.inStartElement) {
            throw new IllegalStateException(OUTSIDE_START_ELEMENT);
        } else if (this.attributeCount == 0) {
            return ClonedAttributes.EMPTY;
        } else {
            return new ClonedAttributes(cloneAttributes(this.attributePointer, this.attributeCount), this.attributeCount, null);
        }
    }

    static InputStream openUrl(String url) throws IOException {
        try {
            URLConnection urlConnection = new URL(url).openConnection();
            urlConnection.setConnectTimeout(TIMEOUT);
            urlConnection.setReadTimeout(TIMEOUT);
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(false);
            return urlConnection.getInputStream();
        } catch (Exception e) {
            IOException ioe = new IOException("Couldn't open " + url);
            ioe.initCause(e);
            throw ioe;
        }
    }
}
