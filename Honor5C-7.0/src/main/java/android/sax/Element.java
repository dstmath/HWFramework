package android.sax;

import android.net.ProxyInfo;
import java.util.ArrayList;
import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

public class Element {
    Children children;
    final int depth;
    EndElementListener endElementListener;
    EndTextElementListener endTextElementListener;
    final String localName;
    final Element parent;
    ArrayList<Element> requiredChilden;
    StartElementListener startElementListener;
    final String uri;
    boolean visited;

    Element(Element parent, String uri, String localName, int depth) {
        this.parent = parent;
        this.uri = uri;
        this.localName = localName;
        this.depth = depth;
    }

    public Element getChild(String localName) {
        return getChild(ProxyInfo.LOCAL_EXCL_LIST, localName);
    }

    public Element getChild(String uri, String localName) {
        if (this.endTextElementListener != null) {
            throw new IllegalStateException("This element already has an end text element listener. It cannot have children.");
        }
        if (this.children == null) {
            this.children = new Children();
        }
        return this.children.getOrCreate(this, uri, localName);
    }

    public Element requireChild(String localName) {
        return requireChild(ProxyInfo.LOCAL_EXCL_LIST, localName);
    }

    public Element requireChild(String uri, String localName) {
        Element child = getChild(uri, localName);
        if (this.requiredChilden == null) {
            this.requiredChilden = new ArrayList();
            this.requiredChilden.add(child);
        } else if (!this.requiredChilden.contains(child)) {
            this.requiredChilden.add(child);
        }
        return child;
    }

    public void setElementListener(ElementListener elementListener) {
        setStartElementListener(elementListener);
        setEndElementListener(elementListener);
    }

    public void setTextElementListener(TextElementListener elementListener) {
        setStartElementListener(elementListener);
        setEndTextElementListener(elementListener);
    }

    public void setStartElementListener(StartElementListener startElementListener) {
        if (this.startElementListener != null) {
            throw new IllegalStateException("Start element listener has already been set.");
        }
        this.startElementListener = startElementListener;
    }

    public void setEndElementListener(EndElementListener endElementListener) {
        if (this.endElementListener != null) {
            throw new IllegalStateException("End element listener has already been set.");
        }
        this.endElementListener = endElementListener;
    }

    public void setEndTextElementListener(EndTextElementListener endTextElementListener) {
        if (this.endTextElementListener != null) {
            throw new IllegalStateException("End text element listener has already been set.");
        } else if (this.children != null) {
            throw new IllegalStateException("This element already has children. It cannot have an end text element listener.");
        } else {
            this.endTextElementListener = endTextElementListener;
        }
    }

    public String toString() {
        return toString(this.uri, this.localName);
    }

    static String toString(String uri, String localName) {
        StringBuilder append = new StringBuilder().append("'");
        if (!uri.equals(ProxyInfo.LOCAL_EXCL_LIST)) {
            localName = uri + ":" + localName;
        }
        return append.append(localName).append("'").toString();
    }

    void resetRequiredChildren() {
        ArrayList<Element> requiredChildren = this.requiredChilden;
        if (requiredChildren != null) {
            for (int i = requiredChildren.size() - 1; i >= 0; i--) {
                ((Element) requiredChildren.get(i)).visited = false;
            }
        }
    }

    void checkRequiredChildren(Locator locator) throws SAXParseException {
        ArrayList<Element> requiredChildren = this.requiredChilden;
        if (requiredChildren != null) {
            int i = requiredChildren.size() - 1;
            while (i >= 0) {
                Element child = (Element) requiredChildren.get(i);
                if (child.visited) {
                    i--;
                } else {
                    throw new BadXmlException("Element named " + this + " is missing required" + " child element named " + child + ".", locator);
                }
            }
        }
    }
}
