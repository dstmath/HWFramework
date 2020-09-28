package org.ccil.cowan.tagsoup;

import java.io.PrintWriter;
import java.io.Writer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class PYXWriter implements ScanHandler, ContentHandler, LexicalHandler {
    private static char[] dummy = new char[1];
    private String attrName;
    private PrintWriter theWriter;

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void adup(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.println(this.attrName);
        this.attrName = null;
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void aname(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.print('A');
        this.theWriter.write(buff, offset, length);
        this.theWriter.print(' ');
        this.attrName = new String(buff, offset, length);
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void aval(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.write(buff, offset, length);
        this.theWriter.println();
        this.attrName = null;
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void cmnt(char[] buff, int offset, int length) throws SAXException {
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void entity(char[] buff, int offset, int length) throws SAXException {
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public int getEntity() {
        return 0;
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void eof(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.close();
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void etag(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.print(')');
        this.theWriter.write(buff, offset, length);
        this.theWriter.println();
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void decl(char[] buff, int offset, int length) throws SAXException {
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void gi(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.print('(');
        this.theWriter.write(buff, offset, length);
        this.theWriter.println();
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void cdsect(char[] buff, int offset, int length) throws SAXException {
        pcdata(buff, offset, length);
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void pcdata(char[] buff, int offset, int length) throws SAXException {
        if (length != 0) {
            boolean inProgress = false;
            int length2 = length + offset;
            for (int i = offset; i < length2; i++) {
                if (buff[i] == '\n') {
                    if (inProgress) {
                        this.theWriter.println();
                    }
                    this.theWriter.println("-\\n");
                    inProgress = false;
                } else {
                    if (!inProgress) {
                        this.theWriter.print('-');
                    }
                    char c = buff[i];
                    if (c == '\t') {
                        this.theWriter.print("\\t");
                    } else if (c != '\\') {
                        this.theWriter.print(buff[i]);
                    } else {
                        this.theWriter.print("\\\\");
                    }
                    inProgress = true;
                }
            }
            if (inProgress) {
                this.theWriter.println();
            }
        }
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void pitarget(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.print('?');
        this.theWriter.write(buff, offset, length);
        this.theWriter.write(32);
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void pi(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.write(buff, offset, length);
        this.theWriter.println();
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void stagc(char[] buff, int offset, int length) throws SAXException {
    }

    @Override // org.ccil.cowan.tagsoup.ScanHandler
    public void stage(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.println("!");
    }

    @Override // org.xml.sax.ContentHandler
    public void characters(char[] buff, int offset, int length) throws SAXException {
        pcdata(buff, offset, length);
    }

    @Override // org.xml.sax.ContentHandler
    public void endDocument() throws SAXException {
        this.theWriter.close();
    }

    @Override // org.xml.sax.ContentHandler
    public void endElement(String uri, String localname, String qname) throws SAXException {
        if (qname.length() == 0) {
            qname = localname;
        }
        this.theWriter.print(')');
        this.theWriter.println(qname);
    }

    @Override // org.xml.sax.ContentHandler
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    @Override // org.xml.sax.ContentHandler
    public void ignorableWhitespace(char[] buff, int offset, int length) throws SAXException {
        characters(buff, offset, length);
    }

    @Override // org.xml.sax.ContentHandler
    public void processingInstruction(String target, String data) throws SAXException {
        this.theWriter.print('?');
        this.theWriter.print(target);
        this.theWriter.print(' ');
        this.theWriter.println(data);
    }

    public void setDocumentLocator(Locator locator) {
    }

    @Override // org.xml.sax.ContentHandler
    public void skippedEntity(String name) throws SAXException {
    }

    @Override // org.xml.sax.ContentHandler
    public void startDocument() throws SAXException {
    }

    @Override // org.xml.sax.ContentHandler
    public void startElement(String uri, String localname, String qname, Attributes atts) throws SAXException {
        if (qname.length() == 0) {
            qname = localname;
        }
        this.theWriter.print('(');
        this.theWriter.println(qname);
        int length = atts.getLength();
        for (int i = 0; i < length; i++) {
            String qname2 = atts.getQName(i);
            if (qname2.length() == 0) {
                qname2 = atts.getLocalName(i);
            }
            this.theWriter.print('A');
            this.theWriter.print(qname2);
            this.theWriter.print(' ');
            this.theWriter.println(atts.getValue(i));
        }
    }

    @Override // org.xml.sax.ContentHandler
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    @Override // org.xml.sax.ext.LexicalHandler
    public void comment(char[] ch, int start, int length) throws SAXException {
        cmnt(ch, start, length);
    }

    @Override // org.xml.sax.ext.LexicalHandler
    public void endCDATA() throws SAXException {
    }

    @Override // org.xml.sax.ext.LexicalHandler
    public void endDTD() throws SAXException {
    }

    @Override // org.xml.sax.ext.LexicalHandler
    public void endEntity(String name) throws SAXException {
    }

    @Override // org.xml.sax.ext.LexicalHandler
    public void startCDATA() throws SAXException {
    }

    @Override // org.xml.sax.ext.LexicalHandler
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    }

    @Override // org.xml.sax.ext.LexicalHandler
    public void startEntity(String name) throws SAXException {
    }

    public PYXWriter(Writer w) {
        if (w instanceof PrintWriter) {
            this.theWriter = (PrintWriter) w;
        } else {
            this.theWriter = new PrintWriter(w);
        }
    }
}
