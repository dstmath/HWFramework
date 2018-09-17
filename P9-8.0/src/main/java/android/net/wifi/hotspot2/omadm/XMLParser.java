package android.net.wifi.hotspot2.omadm;

import android.text.TextUtils;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLParser extends DefaultHandler {
    private XMLNode mCurrent = null;
    private XMLNode mRoot = null;

    public XMLNode parse(String text) throws IOException, SAXException {
        if (TextUtils.isEmpty(text)) {
            throw new IOException("XML string not provided");
        }
        this.mRoot = null;
        this.mCurrent = null;
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(new InputSource(new StringReader(text)), this);
            return this.mRoot;
        } catch (ParserConfigurationException pce) {
            throw new SAXException(pce);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        XMLNode parent = this.mCurrent;
        this.mCurrent = new XMLNode(parent, qName);
        if (this.mRoot == null) {
            this.mRoot = this.mCurrent;
        } else if (parent == null) {
            throw new SAXException("More than one root nodes");
        } else {
            parent.addChild(this.mCurrent);
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals(this.mCurrent.getTag())) {
            this.mCurrent.close();
            this.mCurrent = this.mCurrent.getParent();
            return;
        }
        throw new SAXException("End tag '" + qName + "' doesn't match current node: " + this.mCurrent);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        this.mCurrent.addText(new String(ch, start, length));
    }
}
