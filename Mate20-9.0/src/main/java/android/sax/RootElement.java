package android.sax;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class RootElement extends Element {
    final Handler handler;

    class Handler extends DefaultHandler {
        StringBuilder bodyBuilder = null;
        Element current = null;
        int depth = -1;
        Locator locator;

        Handler() {
        }

        public void setDocumentLocator(Locator locator2) {
            this.locator = locator2;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            int depth2 = this.depth + 1;
            this.depth = depth2;
            if (depth2 == 0) {
                startRoot(uri, localName, attributes);
            } else if (this.bodyBuilder == null) {
                if (depth2 == this.current.depth + 1) {
                    Children children = this.current.children;
                    if (children != null) {
                        Element child = children.get(uri, localName);
                        if (child != null) {
                            start(child, attributes);
                        }
                    }
                }
            } else {
                throw new BadXmlException("Encountered mixed content within text element named " + this.current + ".", this.locator);
            }
        }

        /* access modifiers changed from: package-private */
        public void startRoot(String uri, String localName, Attributes attributes) throws SAXException {
            Element root = RootElement.this;
            if (root.uri.compareTo(uri) == 0 && root.localName.compareTo(localName) == 0) {
                start(root, attributes);
                return;
            }
            throw new BadXmlException("Root element name does not match. Expected: " + root + ", Got: " + Element.toString(uri, localName), this.locator);
        }

        /* access modifiers changed from: package-private */
        public void start(Element e, Attributes attributes) {
            this.current = e;
            if (e.startElementListener != null) {
                e.startElementListener.start(attributes);
            }
            if (e.endTextElementListener != null) {
                this.bodyBuilder = new StringBuilder();
            }
            e.resetRequiredChildren();
            e.visited = true;
        }

        public void characters(char[] buffer, int start, int length) throws SAXException {
            if (this.bodyBuilder != null) {
                this.bodyBuilder.append(buffer, start, length);
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            Element current2 = this.current;
            if (this.depth == current2.depth) {
                current2.checkRequiredChildren(this.locator);
                if (current2.endElementListener != null) {
                    current2.endElementListener.end();
                }
                if (this.bodyBuilder != null) {
                    String body = this.bodyBuilder.toString();
                    this.bodyBuilder = null;
                    current2.endTextElementListener.end(body);
                }
                this.current = current2.parent;
            }
            this.depth--;
        }
    }

    public RootElement(String uri, String localName) {
        super(null, uri, localName, 0);
        this.handler = new Handler();
    }

    public RootElement(String localName) {
        this("", localName);
    }

    public ContentHandler getContentHandler() {
        return this.handler;
    }
}
