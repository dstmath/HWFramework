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

    public void adup(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.println(this.attrName);
        this.attrName = null;
    }

    public void aname(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.print('A');
        this.theWriter.write(buff, offset, length);
        this.theWriter.print(' ');
        this.attrName = new String(buff, offset, length);
    }

    public void aval(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.write(buff, offset, length);
        this.theWriter.println();
        this.attrName = null;
    }

    public void cmnt(char[] buff, int offset, int length) throws SAXException {
    }

    public void entity(char[] buff, int offset, int length) throws SAXException {
    }

    public int getEntity() {
        return 0;
    }

    public void eof(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.close();
    }

    public void etag(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.print(')');
        this.theWriter.write(buff, offset, length);
        this.theWriter.println();
    }

    public void decl(char[] buff, int offset, int length) throws SAXException {
    }

    public void gi(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.print('(');
        this.theWriter.write(buff, offset, length);
        this.theWriter.println();
    }

    public void cdsect(char[] buff, int offset, int length) throws SAXException {
        pcdata(buff, offset, length);
    }

    public void pcdata(char[] buff, int offset, int length) throws SAXException {
        if (length != 0) {
            boolean inProgress = false;
            length += offset;
            for (int i = offset; i < length; i++) {
                if (buff[i] == 10) {
                    if (inProgress) {
                        this.theWriter.println();
                    }
                    this.theWriter.println("-\\n");
                    inProgress = false;
                } else {
                    if (!inProgress) {
                        this.theWriter.print('-');
                    }
                    switch (buff[i]) {
                        case 9:
                            this.theWriter.print("\\t");
                            break;
                        case '\\':
                            this.theWriter.print("\\\\");
                            break;
                        default:
                            this.theWriter.print(buff[i]);
                            break;
                    }
                    inProgress = true;
                }
            }
            if (inProgress) {
                this.theWriter.println();
            }
        }
    }

    public void pitarget(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.print('?');
        this.theWriter.write(buff, offset, length);
        this.theWriter.write(32);
    }

    public void pi(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.write(buff, offset, length);
        this.theWriter.println();
    }

    public void stagc(char[] buff, int offset, int length) throws SAXException {
    }

    public void stage(char[] buff, int offset, int length) throws SAXException {
        this.theWriter.println("!");
    }

    public void characters(char[] buff, int offset, int length) throws SAXException {
        pcdata(buff, offset, length);
    }

    public void endDocument() throws SAXException {
        this.theWriter.close();
    }

    public void endElement(String uri, String localname, String qname) throws SAXException {
        if (qname.length() == 0) {
            qname = localname;
        }
        this.theWriter.print(')');
        this.theWriter.println(qname);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
    }

    public void ignorableWhitespace(char[] buff, int offset, int length) throws SAXException {
        characters(buff, offset, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        this.theWriter.print('?');
        this.theWriter.print(target);
        this.theWriter.print(' ');
        this.theWriter.println(data);
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void skippedEntity(String name) throws SAXException {
    }

    public void startDocument() throws SAXException {
    }

    public void startElement(String uri, String localname, String qname, Attributes atts) throws SAXException {
        if (qname.length() == 0) {
            qname = localname;
        }
        this.theWriter.print('(');
        this.theWriter.println(qname);
        int length = atts.getLength();
        for (int i = 0; i < length; i++) {
            qname = atts.getQName(i);
            if (qname.length() == 0) {
                qname = atts.getLocalName(i);
            }
            this.theWriter.print('A');
            this.theWriter.print(qname);
            this.theWriter.print(' ');
            this.theWriter.println(atts.getValue(i));
        }
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        cmnt(ch, start, length);
    }

    public void endCDATA() throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    }

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
