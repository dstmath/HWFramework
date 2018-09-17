package org.ccil.cowan.tagsoup;

import gov.nist.javax.sip.parser.TokenTypes;
import java.io.PrintWriter;
import java.io.Writer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class PYXWriter implements ScanHandler, ContentHandler, LexicalHandler {
    private static char[] dummy;
    private String attrName;
    private PrintWriter theWriter;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: org.ccil.cowan.tagsoup.PYXWriter.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: org.ccil.cowan.tagsoup.PYXWriter.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: org.ccil.cowan.tagsoup.PYXWriter.<clinit>():void");
    }

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
                    switch (buff[i]) {
                        case TokenTypes.HT /*9*/:
                            this.theWriter.print("\\t");
                            break;
                        case TokenTypes.BACKSLASH /*92*/:
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
