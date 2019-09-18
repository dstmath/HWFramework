package org.apache.harmony.xml.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class TextImpl extends CharacterDataImpl implements Text {
    public TextImpl(DocumentImpl document, String data) {
        super(document, data);
    }

    public String getNodeName() {
        return "#text";
    }

    public short getNodeType() {
        return 3;
    }

    public final Text splitText(int offset) throws DOMException {
        Text newText = this.document.createTextNode(substringData(offset, getLength() - offset));
        deleteData(0, offset);
        Node refNode = getNextSibling();
        if (refNode == null) {
            getParentNode().appendChild(newText);
        } else {
            getParentNode().insertBefore(newText, refNode);
        }
        return this;
    }

    public final boolean isElementContentWhitespace() {
        return false;
    }

    public final String getWholeText() {
        StringBuilder result = new StringBuilder();
        for (TextImpl n = firstTextNodeInCurrentRun(); n != null; n = n.nextTextNode()) {
            n.appendDataTo(result);
        }
        return result.toString();
    }

    public final Text replaceWholeText(String content) throws DOMException {
        Node parent = getParentNode();
        Text result = null;
        TextImpl n = firstTextNodeInCurrentRun();
        while (n != null) {
            if (n != this || content == null || content.length() <= 0) {
                Node toRemove = n;
                n = n.nextTextNode();
                parent.removeChild(toRemove);
            } else {
                setData(content);
                result = this;
                n = n.nextTextNode();
            }
        }
        return result;
    }

    private TextImpl firstTextNodeInCurrentRun() {
        TextImpl firstTextInCurrentRun = this;
        for (Node p = getPreviousSibling(); p != null; p = p.getPreviousSibling()) {
            short nodeType = p.getNodeType();
            if (nodeType != 3 && nodeType != 4) {
                break;
            }
            firstTextInCurrentRun = (TextImpl) p;
        }
        return firstTextInCurrentRun;
    }

    private TextImpl nextTextNode() {
        Node nextSibling = getNextSibling();
        TextImpl textImpl = null;
        if (nextSibling == null) {
            return null;
        }
        short nodeType = nextSibling.getNodeType();
        if (nodeType == 3 || nodeType == 4) {
            textImpl = (TextImpl) nextSibling;
        }
        return textImpl;
    }

    public final TextImpl minimize() {
        if (getLength() == 0) {
            this.parent.removeChild(this);
            return null;
        }
        Node previous = getPreviousSibling();
        if (previous == null || previous.getNodeType() != 3) {
            return this;
        }
        TextImpl previousText = (TextImpl) previous;
        previousText.buffer.append(this.buffer);
        this.parent.removeChild(this);
        return previousText;
    }
}
