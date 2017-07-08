package org.apache.harmony.xml.dom;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Node;

public final class CDATASectionImpl extends TextImpl implements CDATASection {
    public CDATASectionImpl(DocumentImpl document, String data) {
        super(document, data);
    }

    public String getNodeName() {
        return "#cdata-section";
    }

    public short getNodeType() {
        return (short) 4;
    }

    public void split() {
        if (needsSplitting()) {
            Node parent = getParentNode();
            String[] parts = getData().split("\\]\\]>");
            parent.insertBefore(new CDATASectionImpl(this.document, parts[0] + "]]"), this);
            for (int p = 1; p < parts.length - 1; p++) {
                parent.insertBefore(new CDATASectionImpl(this.document, ">" + parts[p] + "]]"), this);
            }
            setData(">" + parts[parts.length - 1]);
        }
    }

    public boolean needsSplitting() {
        return this.buffer.indexOf("]]>") != -1;
    }

    public TextImpl replaceWithText() {
        TextImpl replacement = new TextImpl(this.document, getData());
        this.parent.insertBefore(replacement, this);
        this.parent.removeChild(this);
        return replacement;
    }
}
