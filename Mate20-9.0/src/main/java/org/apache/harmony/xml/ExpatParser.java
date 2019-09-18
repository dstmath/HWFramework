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

class ExpatParser {
    private static final int BUFFER_SIZE = 8096;
    static final String CHARACTER_ENCODING = "UTF-16";
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final String OUTSIDE_START_ELEMENT = "Attributes can only be used within the scope of startElement().";
    private static final int TIMEOUT = 20000;
    /* access modifiers changed from: private */
    public int attributeCount;
    /* access modifiers changed from: private */
    public long attributePointer;
    private final ExpatAttributes attributes;
    private final String encoding;
    /* access modifiers changed from: private */
    public boolean inStartElement;
    private final Locator locator;
    /* access modifiers changed from: private */
    public long pointer;
    /* access modifiers changed from: private */
    public final String publicId;
    /* access modifiers changed from: private */
    public final String systemId;
    private final ExpatReader xmlReader;

    private static class ClonedAttributes extends ExpatAttributes {
        /* access modifiers changed from: private */
        public static final Attributes EMPTY;
        private final int length;
        private final long parserPointer;
        private long pointer;

        static {
            ClonedAttributes clonedAttributes = new ClonedAttributes(0, 0, 0);
            EMPTY = clonedAttributes;
        }

        private ClonedAttributes(long parserPointer2, long pointer2, int length2) {
            this.parserPointer = parserPointer2;
            this.pointer = pointer2;
            this.length = length2;
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

        /* access modifiers changed from: protected */
        public synchronized void finalize() throws Throwable {
            try {
                if (this.pointer != 0) {
                    freeAttributes(this.pointer);
                    this.pointer = 0;
                }
                super.finalize();
            } catch (Throwable th) {
                super.finalize();
                throw th;
            }
        }
    }

    private class CurrentAttributes extends ExpatAttributes {
        private CurrentAttributes() {
        }

        public long getParserPointer() {
            return ExpatParser.this.pointer;
        }

        public long getPointer() {
            if (ExpatParser.this.inStartElement) {
                return ExpatParser.this.attributePointer;
            }
            throw new IllegalStateException(ExpatParser.OUTSIDE_START_ELEMENT);
        }

        public int getLength() {
            if (ExpatParser.this.inStartElement) {
                return ExpatParser.this.attributeCount;
            }
            throw new IllegalStateException(ExpatParser.OUTSIDE_START_ELEMENT);
        }
    }

    private static class EntityParser extends ExpatParser {
        private int depth;

        private EntityParser(String encoding, ExpatReader xmlReader, long pointer, String publicId, String systemId) {
            super(encoding, xmlReader, pointer, publicId, systemId);
            this.depth = 0;
        }

        /* access modifiers changed from: package-private */
        public void startElement(String uri, String localName, String qName, long attributePointer, int attributeCount) throws SAXException {
            int i = this.depth;
            this.depth = i + 1;
            if (i > 0) {
                ExpatParser.super.startElement(uri, localName, qName, attributePointer, attributeCount);
            }
        }

        /* access modifiers changed from: package-private */
        public void endElement(String uri, String localName, String qName) throws SAXException {
            int i = this.depth - 1;
            this.depth = i;
            if (i > 0) {
                ExpatParser.super.endElement(uri, localName, qName);
            }
        }

        /* access modifiers changed from: protected */
        public synchronized void finalize() throws Throwable {
        }
    }

    private class ExpatLocator implements Locator {
        private ExpatLocator() {
        }

        public String getPublicId() {
            return ExpatParser.this.publicId;
        }

        public String getSystemId() {
            return ExpatParser.this.systemId;
        }

        public int getLineNumber() {
            return ExpatParser.this.line();
        }

        public int getColumnNumber() {
            return ExpatParser.this.column();
        }

        public String toString() {
            return "Locator[publicId: " + ExpatParser.this.publicId + ", systemId: " + ExpatParser.this.systemId + ", line: " + getLineNumber() + ", column: " + getColumnNumber() + "]";
        }
    }

    private static class ParseException extends SAXParseException {
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

    ExpatParser(String encoding2, ExpatReader xmlReader2, boolean processNamespaces, String publicId2, String systemId2) {
        this.inStartElement = false;
        this.attributeCount = -1;
        this.attributePointer = 0;
        this.locator = new ExpatLocator();
        this.attributes = new CurrentAttributes();
        this.publicId = publicId2;
        this.systemId = systemId2;
        this.xmlReader = xmlReader2;
        this.encoding = encoding2 == null ? DEFAULT_ENCODING : encoding2;
        this.pointer = initialize(this.encoding, processNamespaces);
    }

    private ExpatParser(String encoding2, ExpatReader xmlReader2, long pointer2, String publicId2, String systemId2) {
        this.inStartElement = false;
        this.attributeCount = -1;
        this.attributePointer = 0;
        this.locator = new ExpatLocator();
        this.attributes = new CurrentAttributes();
        this.encoding = encoding2;
        this.xmlReader = xmlReader2;
        this.pointer = pointer2;
        this.systemId = systemId2;
        this.publicId = publicId2;
    }

    /* access modifiers changed from: package-private */
    public void startElement(String uri, String localName, String qName, long attributePointer2, int attributeCount2) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            try {
                this.inStartElement = true;
                this.attributePointer = attributePointer2;
                this.attributeCount = attributeCount2;
                contentHandler.startElement(uri, localName, qName, this.attributes);
            } finally {
                this.inStartElement = false;
                this.attributeCount = -1;
                this.attributePointer = 0;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void endElement(String uri, String localName, String qName) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.endElement(uri, localName, qName);
        }
    }

    /* access modifiers changed from: package-private */
    public void text(char[] text, int length) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.characters(text, 0, length);
        }
    }

    /* access modifiers changed from: package-private */
    public void comment(char[] text, int length) throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.comment(text, 0, length);
        }
    }

    /* access modifiers changed from: package-private */
    public void startCdata() throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startCDATA();
        }
    }

    /* access modifiers changed from: package-private */
    public void endCdata() throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endCDATA();
        }
    }

    /* access modifiers changed from: package-private */
    public void startNamespace(String prefix, String uri) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.startPrefixMapping(prefix, uri);
        }
    }

    /* access modifiers changed from: package-private */
    public void endNamespace(String prefix) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.endPrefixMapping(prefix);
        }
    }

    /* access modifiers changed from: package-private */
    public void startDtd(String name, String publicId2, String systemId2) throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.startDTD(name, publicId2, systemId2);
        }
    }

    /* access modifiers changed from: package-private */
    public void endDtd() throws SAXException {
        LexicalHandler lexicalHandler = this.xmlReader.lexicalHandler;
        if (lexicalHandler != null) {
            lexicalHandler.endDTD();
        }
    }

    /* access modifiers changed from: package-private */
    public void processingInstruction(String target, String data) throws SAXException {
        ContentHandler contentHandler = this.xmlReader.contentHandler;
        if (contentHandler != null) {
            contentHandler.processingInstruction(target, data);
        }
    }

    /* access modifiers changed from: package-private */
    public void notationDecl(String name, String publicId2, String systemId2) throws SAXException {
        DTDHandler dtdHandler = this.xmlReader.dtdHandler;
        if (dtdHandler != null) {
            dtdHandler.notationDecl(name, publicId2, systemId2);
        }
    }

    /* access modifiers changed from: package-private */
    public void unparsedEntityDecl(String name, String publicId2, String systemId2, String notationName) throws SAXException {
        DTDHandler dtdHandler = this.xmlReader.dtdHandler;
        if (dtdHandler != null) {
            dtdHandler.unparsedEntityDecl(name, publicId2, systemId2, notationName);
        }
    }

    /* access modifiers changed from: package-private */
    public void handleExternalEntity(String context, String publicId2, String systemId2) throws SAXException, IOException {
        long pointer2;
        String systemId3;
        String systemId4 = systemId2;
        EntityResolver entityResolver = this.xmlReader.entityResolver;
        if (entityResolver != null) {
            if (this.systemId != null) {
                try {
                    URI systemUri = new URI(systemId4);
                    if (systemUri.isAbsolute() || systemUri.isOpaque()) {
                        systemId3 = systemId4;
                    } else {
                        systemId3 = new URI(this.systemId).resolve(systemUri).toString();
                    }
                    systemId4 = systemId3;
                } catch (Exception e) {
                    System.logI("Could not resolve '" + systemId4 + "' relative to '" + this.systemId + "' at " + this.locator, e);
                }
            }
            InputSource inputSource = entityResolver.resolveEntity(publicId2, systemId4);
            if (inputSource != null) {
                String encoding2 = pickEncoding(inputSource);
                long pointer3 = createEntityParser(this.pointer, context);
                try {
                    r6 = r6;
                    String str = systemId4;
                    EntityResolver entityResolver2 = entityResolver;
                    pointer2 = pointer3;
                    try {
                        EntityParser entityParser = new EntityParser(encoding2, this.xmlReader, pointer3, inputSource.getPublicId(), inputSource.getSystemId());
                        parseExternalEntity(entityParser, inputSource);
                        releaseParser(pointer2);
                    } catch (Throwable th) {
                        th = th;
                        releaseParser(pointer2);
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    String str2 = systemId4;
                    EntityResolver entityResolver3 = entityResolver;
                    pointer2 = pointer3;
                    releaseParser(pointer2);
                    throw th;
                }
            }
        }
    }

    private String pickEncoding(InputSource inputSource) {
        if (inputSource.getCharacterStream() != null) {
            return CHARACTER_ENCODING;
        }
        String encoding2 = inputSource.getEncoding();
        return encoding2 == null ? DEFAULT_ENCODING : encoding2;
    }

    private void parseExternalEntity(ExpatParser entityParser, InputSource inputSource) throws IOException, SAXException {
        Reader reader = inputSource.getCharacterStream();
        if (reader != null) {
            try {
                entityParser.append("<externalEntity>");
                entityParser.parseFragment(reader);
                entityParser.append("</externalEntity>");
            } finally {
                IoUtils.closeQuietly((AutoCloseable) reader);
            }
        } else {
            InputStream in = inputSource.getByteStream();
            if (in != null) {
                try {
                    entityParser.append("<externalEntity>".getBytes(entityParser.encoding));
                    entityParser.parseFragment(in);
                    entityParser.append("</externalEntity>".getBytes(entityParser.encoding));
                } finally {
                    IoUtils.closeQuietly((AutoCloseable) in);
                }
            } else {
                String systemId2 = inputSource.getSystemId();
                if (systemId2 != null) {
                    InputStream in2 = openUrl(systemId2);
                    try {
                        entityParser.append("<externalEntity>".getBytes(entityParser.encoding));
                        entityParser.parseFragment(in2);
                        entityParser.append("</externalEntity>".getBytes(entityParser.encoding));
                    } finally {
                        IoUtils.closeQuietly((AutoCloseable) in2);
                    }
                } else {
                    throw new ParseException("No input specified.", this.locator);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void append(String xml) throws SAXException {
        try {
            appendString(this.pointer, xml, false);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator);
        }
    }

    /* access modifiers changed from: package-private */
    public void append(char[] xml, int offset, int length) throws SAXException {
        try {
            appendChars(this.pointer, xml, offset, length);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator);
        }
    }

    /* access modifiers changed from: package-private */
    public void append(byte[] xml) throws SAXException {
        append(xml, 0, xml.length);
    }

    /* access modifiers changed from: package-private */
    public void append(byte[] xml, int offset, int length) throws SAXException {
        try {
            appendBytes(this.pointer, xml, offset, length);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator);
        }
    }

    /* access modifiers changed from: package-private */
    public void parseDocument(InputStream in) throws IOException, SAXException {
        startDocument();
        parseFragment(in);
        finish();
        endDocument();
    }

    /* access modifiers changed from: package-private */
    public void parseDocument(Reader in) throws IOException, SAXException {
        startDocument();
        parseFragment(in);
        finish();
        endDocument();
    }

    private void parseFragment(Reader in) throws IOException, SAXException {
        char[] buffer = new char[4048];
        while (true) {
            int read = in.read(buffer);
            int length = read;
            if (read != -1) {
                try {
                    appendChars(this.pointer, buffer, 0, length);
                } catch (ExpatException e) {
                    throw new ParseException(e.getMessage(), this.locator);
                }
            } else {
                return;
            }
        }
    }

    private void parseFragment(InputStream in) throws IOException, SAXException {
        byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            int read = in.read(buffer);
            int length = read;
            if (read != -1) {
                try {
                    appendBytes(this.pointer, buffer, 0, length);
                } catch (ExpatException e) {
                    throw new ParseException(e.getMessage(), this.locator);
                }
            } else {
                return;
            }
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

    /* access modifiers changed from: package-private */
    public void finish() throws SAXException {
        try {
            appendString(this.pointer, "", true);
        } catch (ExpatException e) {
            throw new ParseException(e.getMessage(), this.locator);
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void finalize() throws Throwable {
        try {
            if (this.pointer != 0) {
                release(this.pointer);
                this.pointer = 0;
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
            throw th;
        }
    }

    static {
        staticInitialize("");
    }

    /* access modifiers changed from: private */
    public int line() {
        return line(this.pointer);
    }

    /* access modifiers changed from: private */
    public int column() {
        return column(this.pointer);
    }

    /* access modifiers changed from: package-private */
    public Attributes cloneAttributes() {
        if (!this.inStartElement) {
            throw new IllegalStateException(OUTSIDE_START_ELEMENT);
        } else if (this.attributeCount == 0) {
            return ClonedAttributes.EMPTY;
        } else {
            ClonedAttributes clonedAttributes = new ClonedAttributes(this.pointer, cloneAttributes(this.attributePointer, this.attributeCount), this.attributeCount);
            return clonedAttributes;
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
