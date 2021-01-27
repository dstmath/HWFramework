package org.ksoap2.kdom;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class Document extends Node {
    String encoding;
    protected int rootIndex = -1;
    Boolean standalone;

    public String getEncoding() {
        return this.encoding;
    }

    public void setEncoding(String enc) {
        this.encoding = enc;
    }

    public void setStandalone(Boolean standalone2) {
        this.standalone = standalone2;
    }

    public Boolean getStandalone() {
        return this.standalone;
    }

    public String getName() {
        return "#document";
    }

    @Override // org.ksoap2.kdom.Node
    public void addChild(int index, int type, Object child) {
        if (type == 2) {
            this.rootIndex = index;
        } else {
            int i = this.rootIndex;
            if (i >= index) {
                this.rootIndex = i + 1;
            }
        }
        super.addChild(index, type, child);
    }

    @Override // org.ksoap2.kdom.Node
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(0, null, null);
        parser.nextToken();
        this.encoding = parser.getInputEncoding();
        this.standalone = (Boolean) parser.getProperty("http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone");
        super.parse(parser);
        if (parser.getEventType() != 1) {
            throw new RuntimeException("Document end expected!");
        }
    }

    @Override // org.ksoap2.kdom.Node
    public void removeChild(int index) {
        int i = this.rootIndex;
        if (index == i) {
            this.rootIndex = -1;
        } else if (index < i) {
            this.rootIndex = i - 1;
        }
        super.removeChild(index);
    }

    public Element getRootElement() {
        int i = this.rootIndex;
        if (i != -1) {
            return (Element) getChild(i);
        }
        throw new RuntimeException("Document has no root element!");
    }

    @Override // org.ksoap2.kdom.Node
    public void write(XmlSerializer writer) throws IOException {
        writer.startDocument(this.encoding, this.standalone);
        writeChildren(writer);
        writer.endDocument();
    }
}
